/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.omdb.entities.MovieEntity;
import org.tinymediamanager.scraper.omdb.entities.MovieSearch;
import org.tinymediamanager.scraper.omdb.service.Controller;
import org.tinymediamanager.scraper.util.ListUtils;

/**
 * Movie part of the metadata provider
 *
 * @author Wolfgang Janes
 */
public class OmdbMovieMetadataProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(OmdbMovieMetadataProvider.class);
  private Controller          controller;

  public OmdbMovieMetadataProvider() {
    this.controller = new Controller();
  }

  /**
   * constructor for verbose testing
   *
   * @param verbose
   *          verbose mode
   **/
  OmdbMovieMetadataProvider(boolean verbose) {
    this.controller = new Controller(verbose);
  }

  /**
   * Getting Media Infos
   * 
   * @param query
   *          the query options
   * @return Results to one Movie
   * @throws Exception
   *           any uncaught exception here
   */
  public MediaMetadata getMetadata(MediaScrapeOptions query) throws Exception {
    LOGGER.debug("scrape()" + query.toString());

    MediaMetadata metadata = new MediaMetadata(OmdbMetadataProvider.providerinfo.getId());
    DateFormat format = new SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH);
    LOGGER.info("========= BEGIN OMDB Scraping");

    MovieEntity result = null;
    try {
      result = controller.getScrapeDataById(query.getId(OmdbMetadataProvider.providerinfo.getId()), "movie", true);
    }
    catch (Exception e) {
      LOGGER.error("error searching: " + e.getMessage());
    }

    if (result == null) {
      LOGGER.warn("no result found");
      return metadata;
    }

    metadata.setTitle(result.title);
    try {
      metadata.setYear(Integer.parseInt(result.year));
    }
    catch (NumberFormatException ignored) {
    }

    metadata.addCertification(Certification.findCertification(result.rated));
    metadata.setReleaseDate(format.parse(result.released));

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
      MediaCastMember director = new MediaCastMember(MediaCastMember.CastType.DIRECTOR);
      director.setName(d.trim());
      metadata.addCastMember(director);
    }

    String[] writers = result.writer.split(",");
    for (String w : writers) {
      MediaCastMember writer = new MediaCastMember(MediaCastMember.CastType.WRITER);
      writer.setName(w.trim());
      metadata.addCastMember(writer);
    }

    String[] actors = result.actors.split(",");
    for (String a : actors) {
      MediaCastMember actor = new MediaCastMember(MediaCastMember.CastType.ACTOR);
      actor.setName(a.trim());
      metadata.addCastMember(actor);
    }

    metadata.setSpokenLanguages(getResult(result.language, ","));
    metadata.setCountries(getResult(result.country, ","));

    try {
      metadata.setRating(Double.parseDouble(result.imdbRating));
      metadata.setVoteCount(Integer.parseInt(result.imdbVotes));
    }
    catch (NumberFormatException ignored) {
    }

    if (StringUtils.isNotBlank(result.poster)) {
      MediaArtwork artwork = new MediaArtwork(OmdbMetadataProvider.providerinfo.getId(), MediaArtwork.MediaArtworkType.POSTER);
      artwork.setDefaultUrl(result.poster);
      metadata.addMediaArt(artwork);
    }

    return metadata;
  }

  /**
   * Getting the search results
   * 
   * @param query
   *          options
   * @return List of results
   * @throws Exception
   *           any uncaught exception here
   */

  public List<MediaSearchResult> search(MediaSearchOptions query) throws Exception {
    LOGGER.debug("search() " + query.toString());
    List<MediaSearchResult> mediaResult = new ArrayList<>();

    MovieSearch resultList = null;
    try {
      LOGGER.info("========= BEGIN OMDB Scraper Search for Movie: " + query.getQuery());
      resultList = controller.getMovieSearchInfo(query.getQuery(), "movie", null);
    }
    catch (Exception e) {
      LOGGER.error("error searching: " + e.getMessage());
      return mediaResult;
    }

    if (resultList == null) {
      LOGGER.warn("no result from omdbapi");
      return mediaResult;
    }

    for (MovieEntity entity : ListUtils.nullSafe(resultList.search)) {
      MediaSearchResult result = new MediaSearchResult(OmdbMetadataProvider.providerinfo.getId(), MediaType.MOVIE);

      result.setTitle(entity.title);
      result.setIMDBId(entity.imdbID);
      try {
        result.setYear(Integer.parseInt(entity.year));
      }
      catch (NumberFormatException ignored) {
      }
      result.setPosterUrl(entity.poster);

      mediaResult.add(result);
    }

    return mediaResult;
  }

  /**
   *
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
}
