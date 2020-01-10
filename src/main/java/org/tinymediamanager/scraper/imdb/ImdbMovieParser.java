/*
 * Copyright 2012 - 2020 Manuel Laggner
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

import static org.tinymediamanager.scraper.imdb.ImdbMetadataProvider.CAT_TITLE;
import static org.tinymediamanager.scraper.imdb.ImdbMetadataProvider.cleanString;
import static org.tinymediamanager.scraper.imdb.ImdbMetadataProvider.executor;
import static org.tinymediamanager.scraper.imdb.ImdbMetadataProvider.providerInfo;

import java.util.Locale;
import java.util.Map;
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
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviders;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.MediaSearchAndScrapeOptions;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMediaProvider;
import org.tinymediamanager.scraper.interfaces.IMovieMetadataProvider;
import org.tinymediamanager.scraper.util.MetadataUtil;

/**
 * The class ImdbMovieParser is used to parse the movie sites at imdb.com
 *
 * @author Manuel Laggner
 */
public class ImdbMovieParser extends ImdbParser {
  private static final Logger  LOGGER                  = LoggerFactory.getLogger(ImdbMovieParser.class);
  private static final Pattern UNWANTED_SEARCH_RESULTS = Pattern.compile(".*\\((TV Series|TV Episode|Short|Video Game)\\).*");

  ImdbMovieParser() {
    super(MediaType.MOVIE);
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
  protected MediaMetadata getMetadata(MediaSearchAndScrapeOptions options) throws ScrapeException, MissingIdException, NothingFoundException {
    return getMovieMetadata((MovieSearchAndScrapeOptions) options);
  }

  @Override
  protected String getSearchCategory() {
    return CAT_TITLE;
  }

  @Override
  protected CountryCode getCountry() {
    return MovieModuleManager.SETTINGS.getCertificationCountry();
  }

  MediaMetadata getMovieMetadata(MovieSearchAndScrapeOptions options) throws ScrapeException, MissingIdException, NothingFoundException {
    MediaMetadata md = new MediaMetadata(providerInfo.getId());

    // check if there is a md in the result
    if (options.getMetadata() != null) {
      LOGGER.debug("IMDB: got metadata from cache: {}", options.getMetadata());
      return options.getMetadata();
    }

    String imdbId = "";

    // imdbId from searchResult
    if (options.getSearchResult() != null) {
      imdbId = options.getSearchResult().getIMDBId();
    }

    // imdbid from scraper option
    if (!MetadataUtil.isValidImdbId(imdbId)) {
      imdbId = options.getImdbId();
    }

    if (!MetadataUtil.isValidImdbId(imdbId)) {
      LOGGER.warn("not possible to scrape from IMDB - imdbId found");
      throw new MissingIdException(MediaMetadata.IMDB);
    }

    LOGGER.debug("IMDB: getMetadata(imdbId): {}", imdbId);
    md.setId(providerInfo.getId(), imdbId);

    ExecutorCompletionService<Document> compSvcImdb = new ExecutorCompletionService<>(executor);
    ExecutorCompletionService<MediaMetadata> compSvcTmdb = new ExecutorCompletionService<>(executor);

    // worker for imdb request (/reference)
    String url = IMDB_SITE + "title/" + imdbId + "/reference";
    Callable<Document> worker = new ImdbWorker(url, options.getLanguage().getLanguage(), getCountry().getAlpha2());
    Future<Document> futureReference = compSvcImdb.submit(worker);

    // worker for imdb request (/plotsummary) (from chosen site)
    Future<Document> futurePlotsummary;
    url = IMDB_SITE + "title/" + imdbId + "/plotsummary";
    worker = new ImdbWorker(url, options.getLanguage().getLanguage(), getCountry().getAlpha2());
    futurePlotsummary = compSvcImdb.submit(worker);

    // worker for imdb request (/releaseinfo)
    Future<Document> futureReleaseinfo;
    url = IMDB_SITE + "title/" + imdbId + "/releaseinfo";
    worker = new ImdbWorker(url, options.getLanguage().getLanguage(), getCountry().getAlpha2());
    futureReleaseinfo = compSvcImdb.submit(worker);

    // worker for imdb keywords (/keywords)
    Future<Document> futureKeywords = null;
    if (isScrapeKeywordsPage()) {
      url = IMDB_SITE + "title/" + imdbId + "/keywords";
      worker = new ImdbWorker(url, options.getLanguage().getLanguage(), getCountry().getAlpha2());
      futureKeywords = compSvcImdb.submit(worker);
    }

    // worker for tmdb request
    Future<MediaMetadata> futureTmdb = null;
    if (isUseTmdbForMovies() || isScrapeCollectionInfo()) {
      Callable<MediaMetadata> worker2 = new TmdbMovieWorker(options);
      futureTmdb = compSvcTmdb.submit(worker2);
    }

    Document doc;
    try {
      doc = futureReference.get();
      parseReferencePage(doc, options, md);

      doc = futurePlotsummary.get();
      parsePlotsummaryPage(doc, options, md);

      // title also from chosen site if we are not scraping akas.imdb.com
      Element title = doc.getElementById("tn15title");
      if (title != null) {
        Element element;
        // title
        Elements elements = title.getElementsByClass("main");
        if (!elements.isEmpty()) {
          element = elements.first();
          String movieTitle = cleanString(element.ownText());
          md.setTitle(movieTitle);
        }
      }

      if (futureKeywords != null) {
        doc = futureKeywords.get();
        parseKeywordsPage(doc, options, md);
      }

      // get the release info page
      Document releaseinfoDoc = futureReleaseinfo.get();
      // parse original title here!!
      parseReleaseinfoPageAKAs(releaseinfoDoc, options, md);

      // did we get a release date?
      if (md.getReleaseDate() == null || ImdbMetadataProvider.providerInfo.getConfig().getValueAsBool("localReleaseDate")) {
        // get the date from the releaseinfo page
        parseReleaseinfoPage(releaseinfoDoc, options, md);
      }

      // if everything worked so far, we can set the given id
      md.setId(providerInfo.getId(), imdbId);
    }
    catch (Exception e) {
      LOGGER.error("problem while scraping: {}", e.getMessage());
      throw new ScrapeException(e);
    }

    if (md.getIds().isEmpty()) {
      LOGGER.warn("nothing found");
      throw new NothingFoundException();
    }

    // get data from tmdb?
    if (futureTmdb != null && (isUseTmdbForMovies() || isScrapeCollectionInfo())) {
      try {
        MediaMetadata tmdbMd = futureTmdb.get();
        if (tmdbMd != null) {
          // provide all IDs
          for (Map.Entry<String, Object> entry : tmdbMd.getIds().entrySet()) {
            md.setId(entry.getKey(), entry.getValue());
          }

          if (isUseTmdbForMovies()) {
            // title
            if (StringUtils.isNotBlank(tmdbMd.getTitle())) {
              md.setTitle(tmdbMd.getTitle());
            }
            // original title
            if (StringUtils.isNotBlank(tmdbMd.getOriginalTitle())) {
              md.setOriginalTitle(tmdbMd.getOriginalTitle());
            }
            // tagline
            if (StringUtils.isNotBlank(tmdbMd.getTagline())) {
              md.setTagline(tmdbMd.getTagline());
            }
            // plot
            if (StringUtils.isNotBlank(tmdbMd.getPlot())) {
              md.setPlot(tmdbMd.getPlot());
            }
            // collection info
            if (StringUtils.isNotBlank(tmdbMd.getCollectionName())) {
              md.setCollectionName(tmdbMd.getCollectionName());
            }
          }

          if (ImdbMetadataProvider.providerInfo.getConfig().getValueAsBool("scrapeCollectionInfo")) {
            md.setCollectionName(tmdbMd.getCollectionName());
          }
        }
      }
      catch (Exception e) {
        getLogger().debug("could not get data from tmdb: {}", e.getMessage());
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

  // AKAs and original title
  private MediaMetadata parseReleaseinfoPageAKAs(Document doc, MediaSearchAndScrapeOptions options, MediaMetadata md) {
    // <table id="akas" class="subpage_data spEven2Col">
    // <tr class="even">
    // <td>(original title)</td>
    // <td>Intouchables</td>
    // </tr>

    // need to search all tables for correct ID, since the UNIQUE id is used multiple times - thanks for nothing :p
    for (Element table : doc.getElementsByTag("table")) {
      if (table.id().equalsIgnoreCase("akas")) {
        Elements rows = table.getElementsByTag("tr");
        for (Element row : rows) {
          Element c1 = row.getElementsByTag("td").get(0);
          Element c2 = row.getElementsByTag("td").get(1);
          if (c1 != null && c1.text().toLowerCase(Locale.ROOT).contains("original title")) {
            md.setOriginalTitle(c2.text());
            break;
          }
        }
      }
    }

    // alternative; new way with table classes
    // <tr class="ipl-zebra-list__item aka-item">
    // <td class="aka-item__name">Germany</td>
    // <td class="aka-item__title">Avatar - Aufbruch nach Pandora</td>
    // </tr>
    Elements rows = doc.getElementsByClass("aka-item");
    for (Element row : rows) {
      Element country = row.getElementsByClass("aka-item__name").first();
      Element title = row.getElementsByClass("aka-item__title").first();
      if (country != null && country.text().toLowerCase(Locale.ROOT).contains("original title")) {
        md.setOriginalTitle(title.text());
        break;
      }
    }

    return md;
  }

  private static class TmdbMovieWorker implements Callable<MediaMetadata> {
    private MovieSearchAndScrapeOptions options;

    TmdbMovieWorker(MovieSearchAndScrapeOptions options) {
      this.options = options;
    }

    @Override
    public MediaMetadata call() {
      try {
        IMediaProvider tmdb = MediaProviders.getProviderById(MediaMetadata.TMDB);
        if (tmdb == null) {
          return null;
        }

        MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions(this.options);
        options.setMetadataScraper(new MediaScraper(ScraperType.MOVIE, tmdb));
        return ((IMovieMetadataProvider) tmdb).getMetadata(options);
      }
      catch (Exception e) {
        return null;
      }
    }
  }
}
