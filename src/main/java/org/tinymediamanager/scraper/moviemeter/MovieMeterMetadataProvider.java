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
package org.tinymediamanager.scraper.moviemeter;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMovieImdbMetadataProvider;
import org.tinymediamanager.scraper.interfaces.IMovieMetadataProvider;
import org.tinymediamanager.scraper.moviemeter.entities.MMActor;
import org.tinymediamanager.scraper.moviemeter.entities.MMDirector;
import org.tinymediamanager.scraper.moviemeter.entities.MMFilm;
import org.tinymediamanager.scraper.util.ApiKey;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.MetadataUtil;

/**
 * The Class MoviemeterMetadataProvider. A meta data provider for the site moviemeter.nl
 *
 * @author Myron Boyle (myron0815@gmx.net)
 */
public class MovieMeterMetadataProvider implements IMovieMetadataProvider, IMovieImdbMetadataProvider {
  public static final String       ID           = "moviemeter";

  private static final Logger      LOGGER       = LoggerFactory.getLogger(MovieMeterMetadataProvider.class);
  private static final String      TMM_API_KEY  = ApiKey.decryptApikey("GK5bRYdcKs3WZzOCa1fOQfIeAJVsBP7buUYjc0q4x2/jX66BlSUDKDAcgN/L0JnM");

  private static MovieMeter        api;
  private static MediaProviderInfo providerInfo = createMediaProviderInfo();

  private static MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo(ID, "moviemeter.nl",
        "<html><h3>Moviemeter.nl</h3><br />A dutch movie database.<br /><br />Available languages: NL</html>",
        MovieMeterMetadataProvider.class.getResource("/org/tinymediamanager/scraper/moviemeter_nl.png"));

    // configure/load settings
    providerInfo.getConfig().addText("apiKey", "", true);
    providerInfo.getConfig().addBoolean("scrapeLanguageNames", true);
    providerInfo.getConfig().load();

    return providerInfo;
  }

  // thread safe initialization of the API
  private static synchronized void initAPI() throws ScrapeException {
    if (api == null) {
      try {
        api = new MovieMeter();
        // api.setIsDebug(true);
      }
      catch (Exception e) {
        LOGGER.error("MoviemeterMetadataProvider", e);
        throw new ScrapeException(e);
      }
    }

    String userApiKey = providerInfo.getConfig().getValue("apiKey");

    // check if the API should change from current key to user key
    if (StringUtils.isNotBlank(userApiKey) && !userApiKey.equals(api.getApiKey())) {
      api.setApiKey(userApiKey);
    }

    // check if the API should change from current key to tmm key
    if (StringUtils.isBlank(userApiKey) && !TMM_API_KEY.equals(api.getApiKey())) {
      api.setApiKey(TMM_API_KEY);
    }
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public MediaMetadata getMetadata(MovieSearchAndScrapeOptions options) throws ScrapeException, MissingIdException, NothingFoundException {
    // lazy loading of the api
    initAPI();

    LOGGER.debug("getMetadata(): {}", options);
    // check if there is a md in the result
    if (options.getMetadata() != null) {
      LOGGER.debug("MovieMeter: getMetadata from cache");
      return options.getMetadata();
    }

    // get ids to scrape
    MediaMetadata md = new MediaMetadata(providerInfo.getId());

    int mmId = options.getIdAsInt(providerInfo.getId());

    // imdbid
    String imdbId = options.getImdbId();

    if (StringUtils.isBlank(imdbId) && mmId == 0) {
      LOGGER.warn("not possible to scrape from Moviemeter.bl - no mmId/imdbId found");
      throw new MissingIdException(MediaMetadata.IMDB, providerInfo.getId());
    }

    // scrape
    MMFilm fd = null;
    Exception savedException = null;
    synchronized (api) {
      if (mmId != 0) {
        LOGGER.debug("getMetadata(mmId): {}", mmId);
        try {
          fd = api.getFilmService().getMovieInfo(mmId).execute().body();
        }
        catch (Exception e) {
          LOGGER.warn("Error getting movie via MovieMeter id: {}", e.getMessage());
          savedException = e;
        }
      }
      else if (StringUtils.isNotBlank(imdbId)) {
        LOGGER.debug("filmSearchImdb(imdbId): {}", imdbId);
        try {
          fd = api.getFilmService().getMovieInfoByImdbId(imdbId).execute().body();
        }
        catch (Exception e) {
          LOGGER.warn("Error getting movie via IMDB id: {}", e.getMessage());
          savedException = e;
        }
      }
    }

    // if there has been a saved exception and we did not find anything - throw the exception
    if (fd == null && savedException != null) {
      throw new ScrapeException(savedException);
    }

    if (fd == null) {
      LOGGER.warn("did not find anything");
      throw new NothingFoundException();
    }

    md.setId(MediaMetadata.IMDB, fd.imdb);
    md.setTitle(fd.title);
    md.setYear(fd.year);
    md.setPlot(fd.plot);
    md.setTagline(fd.plot.length() > 150 ? fd.plot.substring(0, 150) : fd.plot);

    MediaRating mediaRating = new MediaRating("moviemeter");
    mediaRating.setRating((float) fd.average);
    mediaRating.setMaxValue(5);
    mediaRating.setVotes(fd.votes_count);
    md.addRating(mediaRating);

    md.setId(providerInfo.getId(), fd.id);
    try {
      md.setRuntime(fd.duration);
    }
    catch (Exception e) {
      md.setRuntime(0);
    }

    for (String g : fd.genres) {
      md.addGenre(getTmmGenre(g));
    }

    // Poster
    MediaArtwork ma = new MediaArtwork(providerInfo.getId(), MediaArtwork.MediaArtworkType.POSTER);
    ma.setPreviewUrl(fd.posters.small);
    ma.setDefaultUrl(fd.posters.large);
    ma.setLanguage(options.getLanguage().getLanguage());
    md.addMediaArt(ma);

    for (String country : fd.countries) {
      if (providerInfo.getConfig().getValueAsBool("scrapeLanguageNames")) {
        md.addCountry(LanguageUtils.getLocalizedCountryForLanguage(options.getLanguage().getLanguage(), country));
      }
      else {
        md.addCountry(country);
      }
    }

    for (MMActor a : fd.actors) {
      Person cm = new Person(Person.Type.ACTOR, a.name);
      md.addCastMember(cm);
    }

    for (MMDirector d : fd.directors) {
      Person cm = new Person(Person.Type.DIRECTOR, d.name);
      md.addCastMember(cm);
    }

    return md;
  }

  @Override
  public SortedSet<MediaSearchResult> search(MovieSearchAndScrapeOptions options) throws ScrapeException {
    // lazy loading of the api
    initAPI();

    LOGGER.debug("search(): {}", options);
    SortedSet<MediaSearchResult> results = new TreeSet<>();

    String imdb = options.getImdbId();
    String searchString = options.getSearchQuery();
    int myear = options.getSearchYear();

    if (StringUtils.isBlank(searchString) && !MetadataUtil.isValidImdbId(imdb)) {
      LOGGER.debug("cannot search without a search string");
      return results;
    }

    searchString = MetadataUtil.removeNonSearchCharacters(searchString);

    if (MetadataUtil.isValidImdbId(searchString)) {
      // hej, our entered value was an IMDBid :)
      imdb = searchString;
    }

    List<MMFilm> moviesFound = new ArrayList<>();
    MMFilm fd = null;

    Exception savedException = null;
    synchronized (api) {
      // 1. "search" with IMDBid (get details, well)
      if (StringUtils.isNotEmpty(imdb)) {
        try {
          fd = api.getFilmService().getMovieInfoByImdbId(imdb).execute().body();
          LOGGER.debug("found result with IMDB id");
        }
        catch (Exception e) {
          LOGGER.warn("Error searching by IMDB id: {}", e.getMessage());
          savedException = e;
        }
      }

      // 2. try with searchString
      if (fd == null) {
        try {
          moviesFound.addAll(api.getSearchService().searchFilm(searchString).execute().body());
          LOGGER.debug("found {} results", moviesFound.size());
        }
        catch (Exception e) {
          LOGGER.warn("Error searching: {}", e.getMessage());
          savedException = e;
        }
      }
    }

    // if there has been a saved exception and we did not find anything - throw the exception
    if (fd == null && savedException != null) {
      throw new ScrapeException(savedException);
    }

    if (fd != null) { // imdb film detail page
      MediaSearchResult sr = new MediaSearchResult(providerInfo.getId(), options.getMediaType());
      sr.setId(String.valueOf(fd.id));
      sr.setIMDBId(imdb);
      sr.setTitle(fd.title);
      sr.setUrl(fd.url);
      sr.setYear(fd.year);
      sr.setScore(1);
      results.add(sr);
    }
    for (MMFilm film : moviesFound) {
      MediaSearchResult sr = new MediaSearchResult(providerInfo.getId(), options.getMediaType());
      sr.setId(String.valueOf(film.id));
      sr.setIMDBId(imdb);
      sr.setTitle(film.title);
      sr.setUrl(film.url);
      sr.setYear(film.year);

      // compare score based on names
      sr.calculateScore(options);
      results.add(sr);
    }

    return results;
  }

  /*
   * Maps scraper Genres to internal TMM genres
   */
  private MediaGenres getTmmGenre(String genre) {
    MediaGenres g = null;
    if (genre.isEmpty()) {
      return g;
    }
    // @formatter:off
    else if (genre.equals("Actie")) {
      g = MediaGenres.ACTION;
    } else if (genre.equals("Animatie")) {
      g = MediaGenres.ANIMATION;
    } else if (genre.equals("Avontuur")) {
      g = MediaGenres.ADVENTURE;
    } else if (genre.equals("Documentaire")) {
      g = MediaGenres.DOCUMENTARY;
    } else if (genre.equals("Drama")) {
      g = MediaGenres.DRAMA;
    } else if (genre.equals("Erotiek")) {
      g = MediaGenres.EROTIC;
    } else if (genre.equals("Familie")) {
      g = MediaGenres.FAMILY;
    } else if (genre.equals("Fantasy")) {
      g = MediaGenres.FANTASY;
    } else if (genre.equals("Film noir")) {
      g = MediaGenres.FILM_NOIR;
    } else if (genre.equals("Horror")) {
      g = MediaGenres.HORROR;
    } else if (genre.equals("Komedie")) {
      g = MediaGenres.COMEDY;
    } else if (genre.equals("Misdaad")) {
      g = MediaGenres.CRIME;
    } else if (genre.equals("Muziek")) {
      g = MediaGenres.MUSIC;
    } else if (genre.equals("Mystery")) {
      g = MediaGenres.MYSTERY;
    } else if (genre.equals("Oorlog")) {
      g = MediaGenres.WAR;
    } else if (genre.equals("Roadmovie")) {
      g = MediaGenres.ROAD_MOVIE;
    } else if (genre.equals("Romantiek")) {
      g = MediaGenres.ROMANCE;
    } else if (genre.equals("Sciencefiction")) {
      g = MediaGenres.SCIENCE_FICTION;
    } else if (genre.equals("Thriller")) {
      g = MediaGenres.THRILLER;
    } else if (genre.equals("Western")) {
      g = MediaGenres.WESTERN;
    }
    // @formatter:on
    if (g == null) {
      g = MediaGenres.getGenre(genre);
    }
    return g;
  }
}
