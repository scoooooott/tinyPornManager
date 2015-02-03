/*
 * Copyright 2012 - 2014 Manuel Laggner
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
package org.tinymediamanager.scraper.moviemeternl;

import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.moviemeternl.model.FilmJson;
import org.tinymediamanager.scraper.moviemeternl.model.SearchJson;
import org.tinymediamanager.scraper.util.Url;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MoviemeterJSONApi {

  private static final Logger LOGGER  = LoggerFactory.getLogger(MoviemeterJSONApi.class); ;

  private static final String SERVICE = "http://www.moviemeter.nl/api/film/";
  private static final String APIKEY  = "&api_key=ubc7uztcv0hgmsbkuknab0e4k9qmwnfd";

  public MoviemeterJSONApi() {
  }

  /**
   * searches for a movie
   * 
   * @param search
   * @return array of simple listing with MMid
   */
  public SearchJson[] filmSearch(String search) {
    SearchJson[] ret = new SearchJson[0];
    try {
      Url u = new Url(SERVICE + "&q=" + URLEncoder.encode(search, "UTF-8") + APIKEY);
      String json = IOUtils.toString(u.getInputStream());

      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      ret = mapper.readValue(json, SearchJson[].class);
    }
    catch (Exception e) {
      LOGGER.error("Error searching movie: " + search);
    }
    return ret;
  }

  /**
   * retrieve movie details using moviemeter ID
   * 
   * @param filmId
   *          moviemeter id
   * @return
   */
  public FilmJson filmDetail(int filmId) {
    FilmJson ret = new FilmJson();
    try {
      Url u = new Url(SERVICE + filmId + APIKEY);
      String json = IOUtils.toString(u.getInputStream());

      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      ret = mapper.readValue(json, FilmJson.class);
    }
    catch (Exception e) {
      LOGGER.error("Error searching movie: " + filmId);
    }
    return ret;
  }

  /**
   * retrieve movie details using IMDB ID
   * 
   * @param imdbId
   *          IMDB id
   * @return
   */
  public FilmJson filmDetail(String imdbId) {
    FilmJson ret = new FilmJson();
    try {
      Url u = new Url(SERVICE + imdbId + APIKEY);
      String json = IOUtils.toString(u.getInputStream());

      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      ret = mapper.readValue(json, FilmJson.class);
    }
    catch (Exception e) {
      LOGGER.error("Error searching movie: " + imdbId);
    }
    return ret;
  }
}
