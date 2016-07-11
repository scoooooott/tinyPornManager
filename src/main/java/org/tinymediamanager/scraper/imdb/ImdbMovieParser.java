/*
 * Copyright 2012 - 2016 Manuel Laggner
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

import static org.tinymediamanager.scraper.imdb.ImdbMetadataProvider.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.util.MetadataUtil;

/**
 * The class ImdbMovieParser is used to parse the movie sites at imdb.com
 * 
 * @author Manuel Laggner
 */
public class ImdbMovieParser extends ImdbParser {
  private static final Logger  LOGGER                  = LoggerFactory.getLogger(ImdbMovieParser.class);
  private static final Pattern UNWANTED_SEARCH_RESULTS = Pattern.compile(".*\\((TV Series|TV Episode|Short|Video Game)\\).*");

  private ImdbSiteDefinition   imdbSite;

  public ImdbMovieParser(ImdbSiteDefinition imdbSite) {
    super(MediaType.MOVIE);
    this.imdbSite = imdbSite;
  }

  @Override
  protected Pattern getUnwantedSearchResultPattern() {
    if (ImdbMetadataProvider.providerInfo.getConfig().getValueAsBool("filterUnwantedCategories")) {
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

    ExecutorCompletionService<Document> compSvcImdb = new ExecutorCompletionService<>(executor);
    ExecutorCompletionService<MediaMetadata> compSvcTmdb = new ExecutorCompletionService<>(executor);

    // worker for imdb request (/combined) (everytime from www.imdb.com)
    // StringBuilder sb = new StringBuilder(imdbSite.getSite());
    StringBuilder sb = new StringBuilder(ImdbSiteDefinition.IMDB_COM.getSite());
    sb.append("title/");
    sb.append(imdbId);
    sb.append("/combined");
    Callable<Document> worker = new ImdbWorker(sb.toString(), options.getLanguage().name(), options.getCountry().getAlpha2(), imdbSite);
    Future<Document> futureCombined = compSvcImdb.submit(worker);

    // worker for imdb request (/plotsummary) (from chosen site)
    Future<Document> futurePlotsummary;
    sb = new StringBuilder(imdbSite.getSite());
    sb.append("title/");
    sb.append(imdbId);
    sb.append("/plotsummary");

    worker = new ImdbWorker(sb.toString(), options.getLanguage().name(), options.getCountry().getAlpha2(), imdbSite);
    futurePlotsummary = compSvcImdb.submit(worker);

    // worker for tmdb request
    Future<MediaMetadata> futureTmdb = null;
    if (ImdbMetadataProvider.providerInfo.getConfig().getValueAsBool("useTmdb")
        || ImdbMetadataProvider.providerInfo.getConfig().getValueAsBool("scrapeCollectionInfo")) {
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
    doc = futurePlotsummary.get();
    parsePlotsummaryPage(doc, options, md);

    // title also from chosen site if we are not scraping akas.imdb.com
    if (imdbSite != ImdbSiteDefinition.IMDB_COM) {
      Element title = doc.getElementById("tn15title");
      if (title != null) {
        Element element;
        // title
        Elements elements = title.getElementsByClass("main");
        if (elements.size() > 0) {
          element = elements.first();
          String movieTitle = cleanString(element.ownText());
          md.setTitle(movieTitle);
        }
      }
    }

    // did we get a release date?
    if (md.getReleaseDate() == null || md.getReleaseDate() == MediaMetadata.INITIAL_DATE) {
      // get the date from the releaseinfo page
      Future<Document> futureReleaseinfo;
      sb = new StringBuilder(imdbSite.getSite());
      sb.append("title/");
      sb.append(imdbId);
      sb.append("/releaseinfo");

      worker = new ImdbWorker(sb.toString(), options.getLanguage().name(), options.getCountry().getAlpha2(), imdbSite);
      futureReleaseinfo = compSvcImdb.submit(worker);
      doc = futureReleaseinfo.get();
      parseReleaseinfoPage(doc, options, md);
    }

    // get data from tmdb?
    if (futureTmdb != null && (ImdbMetadataProvider.providerInfo.getConfig().getValueAsBool("useTmdb")
        || ImdbMetadataProvider.providerInfo.getConfig().getValueAsBool("scrapeCollectionInfo"))) {
      try {
        MediaMetadata tmdbMd = futureTmdb.get();
        if (ImdbMetadataProvider.providerInfo.getConfig().getValueAsBool("useTmdb") && tmdbMd != null && StringUtils.isNotBlank(tmdbMd.getPlot())) {
          // tmdbid
          md.setId(MediaMetadata.TMDB, tmdbMd.getId(MediaMetadata.TMDB));
          // title
          md.setTitle(tmdbMd.getTitle());
          // original title
          md.setOriginalTitle(tmdbMd.getOriginalTitle());
          // tagline
          md.setTagline(tmdbMd.getTagline());
          // plot
          md.setPlot(tmdbMd.getPlot());
          // collection info
          md.setCollectionName(tmdbMd.getCollectionName());
          md.setId(MediaMetadata.TMDB_SET, tmdbMd.getId(MediaMetadata.TMDB_SET));
        }
        if (ImdbMetadataProvider.providerInfo.getConfig().getValueAsBool("scrapeCollectionInfo") && tmdbMd != null) {
          md.setId(MediaMetadata.TMDB_SET, tmdbMd.getId(MediaMetadata.TMDB_SET));
          md.setCollectionName(tmdbMd.getCollectionName());
        }
        md.setId(tmdbMd.getProviderId(), tmdbMd.getId(tmdbMd.getProviderId()));
      }
      catch (Exception ignored) {
      }
    }

    // if we have still no original title, take the title
    if (StringUtils.isBlank(md.getOriginalTitle())) {
      md.setOriginalTitle(md.getTitle());
    }

    // populate id
    md.setId(ImdbMetadataProvider.providerInfo.getId(), imdbId);

    return md;
  }

  private MediaMetadata parseReleaseinfoPage(Document doc, MediaScrapeOptions options, MediaMetadata md) {
    Element tableReleaseDates = doc.getElementById("release_dates");
    if (tableReleaseDates != null) {
      Elements columns = tableReleaseDates.getElementsByClass("release_date");
      if (columns != null && !columns.isEmpty()) {
        Element td = columns.first();
        try {
          SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);
          Date parsedDate = sdf.parse(td.text());
          md.setReleaseDate(parsedDate);
        }
        catch (ParseException otherformat) {
          try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.US);
            Date parsedDate = sdf.parse(td.text());
            md.setReleaseDate(parsedDate);
          }
          catch (ParseException ignored) {
          }
        }
      }
    }

    return md;
  }
}
