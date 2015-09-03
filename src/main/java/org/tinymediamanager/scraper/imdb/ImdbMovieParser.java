/*
 * Copyright 2012 - 2015 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.scraper.imdb;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.util.MetadataUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import static org.tinymediamanager.scraper.imdb.ImdbMetadataProvider.*;

/**
 * The class ImdbMovieParser is used to parse the movie sites at imdb.com
 * 
 * @author Manuel Laggner
 */
public class ImdbMovieParser extends ImdbParser {
  private static final Logger  LOGGER                  = LoggerFactory.getLogger(ImdbMovieParser.class);
  private static final Pattern UNWANTED_SEARCH_RESULTS = Pattern
      .compile(".*\\((TV Series|TV Episode|Short|Video Game)\\).*");

  private ImdbSiteDefinition imdbSite;

  public ImdbMovieParser(ImdbSiteDefinition imdbSite) {
    super(MediaType.MOVIE);
    this.imdbSite = imdbSite;
  }

  @Override
  protected Pattern getUnwantedSearchResultPattern() {
    if (ImdbMetadataProviderConfig.SETTINGS.filterUnwantedCategories) {
      return UNWANTED_SEARCH_RESULTS;
    }
    return null;
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected ImdbSiteDefinition getImdbSite() {
    return imdbSite;
  }

  @Override
  protected MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
    return getMovieMetadata(options);
  }

  @Override
  protected String getSearchCategory() {
    return CAT_TITLE;
  }

  MediaMetadata getMovieMetadata(MediaScrapeOptions options) throws Exception {
    MediaMetadata md = new MediaMetadata(providerInfo.getId());

    // check if there is a md in the result
    if (options.getResult() != null && options.getResult().getMediaMetadata() != null) {
      LOGGER.debug("IMDB: getMetadata from cache: " + options.getResult());
      return options.getResult().getMediaMetadata();
    }

    String imdbId = "";

    // imdbId from searchResult
    if (options.getResult() != null) {
      imdbId = options.getResult().getIMDBId();
    }

    // imdbid from scraper option
    if (!MetadataUtil.isValidImdbId(imdbId)) {
      imdbId = options.getImdbId();
    }

    if (!MetadataUtil.isValidImdbId(imdbId)) {
      return md;
    }

    LOGGER.debug("IMDB: getMetadata(imdbId): " + imdbId);
    md.setId(providerInfo.getId(), imdbId);

    ExecutorCompletionService<Document> compSvcImdb = new ExecutorCompletionService<Document>(executor);
    ExecutorCompletionService<MediaMetadata> compSvcTmdb = new ExecutorCompletionService<MediaMetadata>(executor);

    // worker for imdb request (/combined) (everytime from www.imdb.com)
    // StringBuilder sb = new StringBuilder(imdbSite.getSite());
    StringBuilder sb = new StringBuilder(ImdbSiteDefinition.IMDB_COM.getSite());
    sb.append("title/");
    sb.append(imdbId);
    sb.append("/combined");
    Callable<Document> worker = new ImdbWorker(sb.toString(), options.getLanguage().name(),
        options.getCountry().getAlpha2(), imdbSite);
    Future<Document> futureCombined = compSvcImdb.submit(worker);

    // worker for imdb request (/plotsummary) (from chosen site)
    Future<Document> futurePlotsummary = null;
    sb = new StringBuilder(imdbSite.getSite());
    sb.append("title/");
    sb.append(imdbId);
    sb.append("/plotsummary");

    worker = new ImdbWorker(sb.toString(), options.getLanguage().name(), options.getCountry().getAlpha2(), imdbSite);
    futurePlotsummary = compSvcImdb.submit(worker);

    // worker for tmdb request
    Future<MediaMetadata> futureTmdb = null;
    if (ImdbMetadataProviderConfig.SETTINGS.useTmdb || ImdbMetadataProviderConfig.SETTINGS.scrapeCollectionInfo) {
      Callable<MediaMetadata> worker2 = new TmdbWorker(imdbId, options.getLanguage(), options.getCountry());
      futureTmdb = compSvcTmdb.submit(worker2);
    }

    Document doc;
    doc = futureCombined.get();

    parseCombinedPage(doc, options, md);

    /*
     * plot from /plotsummary
     */
    // build the url
    doc = null;
    doc = futurePlotsummary.get();

    // imdb.com has another site structure
    if (imdbSite == ImdbSiteDefinition.IMDB_COM) {
      Elements zebraList = doc.getElementsByClass("zebraList");
      if (zebraList != null && !zebraList.isEmpty()) {
        Elements odd = zebraList.get(0).getElementsByClass("odd");
        if (odd.isEmpty()) {
          odd = zebraList.get(0).getElementsByClass("even"); // sometimes imdb has even
        }
        if (odd.size() > 0) {
          Elements p = odd.get(0).getElementsByTag("p");
          if (p.size() > 0) {
            String plot = cleanString(p.get(0).ownText());
            md.storeMetadata(MediaMetadata.PLOT, plot);
          }
        }
      }
    }
    else {
      Element wiki = doc.getElementById("swiki.2.1");
      if (wiki != null) {
        String plot = cleanString(wiki.ownText());
        md.storeMetadata(MediaMetadata.PLOT, plot);
      }
    }

    // title also from chosen site if we are not scraping akas.imdb.com
    if (imdbSite != ImdbSiteDefinition.IMDB_COM) {
      Element title = doc.getElementById("tn15title");
      if (title != null) {
        Element element = null;
        // title
        Elements elements = title.getElementsByClass("main");
        if (elements.size() > 0) {
          element = elements.first();
          String movieTitle = cleanString(element.ownText());
          md.storeMetadata(MediaMetadata.TITLE, movieTitle);
        }
      }
    }
    // }

    // get data from tmdb?
    if (ImdbMetadataProviderConfig.SETTINGS.useTmdb || ImdbMetadataProviderConfig.SETTINGS.scrapeCollectionInfo) {
      MediaMetadata tmdbMd = futureTmdb.get();
      if (ImdbMetadataProviderConfig.SETTINGS.useTmdb && tmdbMd != null
          && StringUtils.isNotBlank(tmdbMd.getStringValue(MediaMetadata.PLOT))) {
        // tmdbid
        md.setId(MediaMetadata.TMDB, tmdbMd.getId(MediaMetadata.TMDB));
        // title
        md.storeMetadata(MediaMetadata.TITLE, tmdbMd.getStringValue(MediaMetadata.TITLE));
        // original title
        md.storeMetadata(MediaMetadata.ORIGINAL_TITLE, tmdbMd.getStringValue(MediaMetadata.ORIGINAL_TITLE));
        // tagline
        md.storeMetadata(MediaMetadata.TAGLINE, tmdbMd.getStringValue(MediaMetadata.TAGLINE));
        // plot
        md.storeMetadata(MediaMetadata.PLOT, tmdbMd.getStringValue(MediaMetadata.PLOT));
        // collection info
        md.storeMetadata(MediaMetadata.COLLECTION_NAME, tmdbMd.getStringValue(MediaMetadata.COLLECTION_NAME));
        md.storeMetadata(MediaMetadata.TMDB_SET, tmdbMd.getIntegerValue(MediaMetadata.TMDB_SET));
      }
      if (ImdbMetadataProviderConfig.SETTINGS.scrapeCollectionInfo && tmdbMd != null) {
        md.storeMetadata(MediaMetadata.TMDB_SET, tmdbMd.getIntegerValue(MediaMetadata.TMDB_SET));
        md.storeMetadata(MediaMetadata.COLLECTION_NAME, tmdbMd.getStringValue(MediaMetadata.COLLECTION_NAME));
      }
    }

    // if we have still no original title, take the title
    if (StringUtils.isBlank(md.getStringValue(MediaMetadata.ORIGINAL_TITLE))) {
      md.storeMetadata(MediaMetadata.ORIGINAL_TITLE, md.getStringValue(MediaMetadata.TITLE));
    }

    return md;
  }
}
