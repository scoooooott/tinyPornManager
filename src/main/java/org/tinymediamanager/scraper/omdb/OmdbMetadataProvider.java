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

package org.tinymediamanager.scraper.omdb;

import static org.tinymediamanager.core.entities.Person.Type.ACTOR;
import static org.tinymediamanager.core.entities.Person.Type.DIRECTOR;
import static org.tinymediamanager.core.entities.Person.Type.WRITER;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMovieImdbMetadataProvider;
import org.tinymediamanager.scraper.interfaces.IMovieMetadataProvider;
import org.tinymediamanager.scraper.omdb.entities.MovieEntity;
import org.tinymediamanager.scraper.omdb.entities.MovieRating;
import org.tinymediamanager.scraper.omdb.entities.MovieSearch;
import org.tinymediamanager.scraper.omdb.service.Controller;
import org.tinymediamanager.scraper.util.ApiKey;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.MediaIdUtil;
import org.tinymediamanager.scraper.util.MetadataUtil;

/**
 * Central metadata provider class
 *
 * @author Wolfgang Janes
 */
public class OmdbMetadataProvider implements IMovieMetadataProvider, IMovieImdbMetadataProvider { // , ITvShowMetadataProvider {
  public static final String             ID           = "omdbapi";

  private static final Logger            LOGGER       = LoggerFactory.getLogger(OmdbMetadataProvider.class);
  private static final MediaProviderInfo providerInfo = createMediaProviderInfo();
  private static final String            API_KEY      = ApiKey.decryptApikey("Isuaab2ym89iI1hOtF94nQ==");

  private Controller                     controller;

  public OmdbMetadataProvider() {
    this.controller = new Controller(false);
  }

  private static MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo(ID, "omdbapi.com",
        "<html><h3>Omdbapi.com</h3><br />The OMDb API is a RESTful web service to obtain movie information. All content and images on the site are contributed and maintained by our users. <br /><br />TinyMediaManager offers a limited access to OMDb (10 calls per 15 seconds). If you want to use OMDb with more than this restricted access, you should become a patron of OMDb (https://www.patreon.com/join/omdb)<br /><br />Available languages: EN</html>",
        OmdbMetadataProvider.class.getResource("/org/tinymediamanager/scraper/omdbapi.png"));

    providerInfo.getConfig().addText("apiKey", "", true);
    providerInfo.getConfig().load();
    return providerInfo;
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public String getId() {
    return ID;
  }

  private String getApiKey() {
    String apiKey = providerInfo.getConfig().getValue("apiKey");
    if (StringUtils.isNotBlank(apiKey)) {
      return apiKey;
    }

    return API_KEY;
  }

  @Override
  public MediaMetadata getMetadata(MovieSearchAndScrapeOptions query) throws ScrapeException, MissingIdException, NothingFoundException {
    LOGGER.debug("getMetadata(): {}", query);

    MediaMetadata metadata = new MediaMetadata(OmdbMetadataProvider.providerInfo.getId());

    String apiKey = getApiKey();
    if (StringUtils.isBlank(apiKey)) {
      LOGGER.warn("no API key found");
      return metadata;
    }

    // id from the options
    String imdbId = query.getImdbId();

    // id from omdb proxy?
    if (!MetadataUtil.isValidImdbId(imdbId)) {
      imdbId = query.getIdAsString(getProviderInfo().getId());
    }

    // still no imdb id but tmdb id? get it from tmdb
    if (!MetadataUtil.isValidImdbId(imdbId) && query.getTmdbId() > 0) {
      imdbId = MediaIdUtil.getImdbIdViaTmdbId(query.getTmdbId());
    }

    // imdbid check
    if (!MetadataUtil.isValidImdbId(imdbId)) {
      LOGGER.warn("no imdb id found");
      throw new MissingIdException(MediaMetadata.IMDB);
    }

    DateFormat format = new SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH);
    LOGGER.info("========= BEGIN OMDB Scraping");

    MovieEntity result = null;
    try {
      if (API_KEY.equals(apiKey)) {
        OmdbConnectionCounter.trackConnections();
      }
      result = controller.getScrapeDataById(apiKey, imdbId, "movie", true);
    }
    catch (Exception e) {
      LOGGER.error("error searching: {}", e.getMessage());
      throw new ScrapeException(e);
    }

    if (result == null) {
      LOGGER.warn("no result found");
      throw new NothingFoundException();
    }

    metadata.setTitle(result.title);
    try {
      metadata.setYear(Integer.parseInt(result.year));
    }
    catch (NumberFormatException e) {
      LOGGER.trace("could not parse year: {}", e.getMessage());
    }

    metadata.addCertification(MediaCertification.findCertification(result.rated));
    try {
      metadata.setReleaseDate(format.parse(result.released));
    }
    catch (Exception ignored) {
    }

    Pattern p = Pattern.compile("\\d+");
    Matcher m = p.matcher(result.runtime);
    while (m.find()) {
      try {
        metadata.setRuntime(Integer.parseInt(m.group()));
      }
      catch (NumberFormatException ignored) {
      }
    }

    String[] genres = result.genre.split(",");
    for (String genre : genres) {
      genre = genre.trim();
      MediaGenres mediaGenres = MediaGenres.getGenre(genre);
      metadata.addGenre(mediaGenres);
    }

    metadata.setPlot(result.plot);

    String[] directors = result.director.split(",");
    for (String d : directors) {
      Person director = new Person(DIRECTOR);
      director.setName(d.trim());
      metadata.addCastMember(director);
    }

    String[] writers = result.writer.split(",");
    for (String w : writers) {
      Person writer = new Person(WRITER);
      writer.setName(w.trim());
      metadata.addCastMember(writer);
    }

    String[] actors = result.actors.split(",");
    for (String a : actors) {
      Person actor = new Person(ACTOR);
      actor.setName(a.trim());
      metadata.addCastMember(actor);
    }

    metadata.setSpokenLanguages(getResult(result.language, ","));
    metadata.setCountries(getResult(result.country, ","));

    try {
      MediaRating rating = new MediaRating("imdb");
      rating.setRating(Float.parseFloat(result.imdbRating));
      rating.setVotes(MetadataUtil.parseInt(result.imdbVotes));
      rating.setMaxValue(10);
      metadata.addRating(rating);
    }
    catch (NumberFormatException e) {
      LOGGER.trace("could not parse rating/vote count: {}", e.getMessage());
    }
    try {
      MediaRating rating = new MediaRating("metascore");
      rating.setRating(Float.parseFloat(result.metascore));
      rating.setMaxValue(100);
      metadata.addRating(rating);
    }
    catch (NumberFormatException e) {
      LOGGER.trace("could not parse rating/vote count: {}", e.getMessage());
    }
    // use rotten tomates from the Ratings block
    for (MovieRating movieRating : ListUtils.nullSafe(result.ratings)) {
      switch (movieRating.source) {
        case "Rotten Tomatoes":
          try {
            MediaRating rating = new MediaRating("rottenTomatoes");
            rating.setRating(Integer.parseInt(movieRating.value.replace("%", "")));
            rating.setMaxValue(100);
            metadata.addRating(rating);
          }
          catch (Exception ignored) {
          }
          break;
      }
    }

    if (StringUtils.isNotBlank(result.poster)) {
      MediaArtwork artwork = new MediaArtwork(OmdbMetadataProvider.providerInfo.getId(), MediaArtwork.MediaArtworkType.POSTER);
      artwork.setDefaultUrl(result.poster);
      metadata.addMediaArt(artwork);
    }

    return metadata;

  }

  @Override
  public SortedSet<MediaSearchResult> search(MovieSearchAndScrapeOptions query) throws ScrapeException {
    LOGGER.debug("search(): {}", query);
    SortedSet<MediaSearchResult> mediaResult = new TreeSet<>();

    String apiKey = getApiKey();
    if (StringUtils.isBlank(apiKey)) {
      LOGGER.warn("no API key found");
      return mediaResult;
    }

    MovieSearch resultList;
    try {
      LOGGER.info("========= BEGIN OMDB Scraper Search for Movie: {}", query.getSearchQuery());
      if (API_KEY.equals(apiKey)) {
        OmdbConnectionCounter.trackConnections();
      }
      resultList = controller.getMovieSearchInfo(apiKey, query.getSearchQuery(), "movie", null);
    }
    catch (Exception e) {
      LOGGER.error("error searching: {}", e.getMessage());
      throw new ScrapeException(e);
    }

    if (resultList == null) {
      LOGGER.warn("no result from omdbapi");
      return mediaResult;
    }

    for (MovieEntity entity : ListUtils.nullSafe(resultList.search)) {
      MediaSearchResult result = new MediaSearchResult(OmdbMetadataProvider.providerInfo.getId(), MediaType.MOVIE);

      result.setTitle(entity.title);
      result.setIMDBId(entity.imdbID);
      try {
        result.setYear(Integer.parseInt(entity.year));
      }
      catch (NumberFormatException e) {
        LOGGER.trace("could not parse year: {}", e.getMessage());
      }
      result.setPosterUrl(entity.poster);

      // calcuate the result score
      result.calculateScore(query);
      mediaResult.add(result);
    }

    return mediaResult;
  }

  // @Override
  // public List<MediaEpisode> getEpisodeList(MediaScrapeOptions query) throws Exception {
  //
  // LOGGER.debug("scrape() Episodes " + query.toString());
  // List<MediaEpisode> mediaEpisode = new ArrayList<>();
  // SeasonSearch season;
  // season = null;
  // MovieEntity result = null;
  // MovieEntity episodes;
  //
  // // First scrape the id to get the total number of Seasons
  // try {
  // if(API_KEY.equals(apiKey)){
  // OmdbConnectionCounter.trackConnections();
  // }
  // LOGGER.debug("Getting TotalSeasons From Scraping");
  // result = controller.getScrapeDataById(apiKey, query.getId(OmdbMetadataProvider.providerinfo.getId()), "series", true);
  // }
  // catch (Exception e) {
  // LOGGER.error("error scraping: " + e.getMessage());
  // }
  //
  // for (int i = 1; i <= Integer.parseInt(result.totalSeasons); i++) {
  // LOGGER.debug("Scrape Season " + i);
  // season = controller.getSeasonsById(apiKey, query.getId(OmdbMetadataProvider.providerinfo.getId()), "series", i);
  //
  // for (SeasonEntity episodeResult : ListUtils.nullSafe(season.episodes)) {
  // MediaEpisode mediaResult = new MediaEpisode(OmdbMetadataProvider.providerinfo.getId());
  // episodes = controller.getEpisodesBySeasons(apiKey, query.getId(OmdbMetadataProvider.providerinfo.getId()), "series", i,
  // Integer.parseInt(episodeResult.episode));
  //
  // mediaResult.season = i;
  // mediaResult.plot = episodes.plot;
  // try {
  // mediaResult.episode = Integer.parseInt(episodeResult.episode);
  // mediaResult.rating = Integer.parseInt(episodes.imdbVotes);
  // }
  // catch (NumberFormatException ignored) {
  //
  // }
  // mediaResult.title = episodes.title;
  //
  // mediaEpisode.add(mediaResult);
  //
  // }
  //
  // }
  //
  // return mediaEpisode;
  //
  // }

  /**
   * return a list of results that were separated by a delimiter
   *
   * @param input
   *          result from API
   * @param delimiter
   *          used delimiter
   * @return List of results
   */
  private List<String> getResult(String input, String delimiter) {
    String[] result = input.split(delimiter);
    List<String> output = new ArrayList<>();

    for (String r : result) {
      output.add(r.trim());
    }

    return output;
  }

  /**
   * set the Debugmode for JUnit Testing
   *
   * @param verbose
   *          Boolean for debug mode
   */
  void setVerbose(boolean verbose) {
    controller = new Controller(verbose);
  }
}
