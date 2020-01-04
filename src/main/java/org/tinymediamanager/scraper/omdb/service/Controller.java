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

package org.tinymediamanager.scraper.omdb.service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.http.TmmHttpClient;
import org.tinymediamanager.scraper.omdb.entities.MovieEntity;
import org.tinymediamanager.scraper.omdb.entities.MovieSearch;
import org.tinymediamanager.scraper.omdb.entities.SeasonSearch;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.bind.DateTypeAdapter;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Controller {
  private static final Logger LOGGER   = LoggerFactory.getLogger(Controller.class);
  private Retrofit            retrofit = null;

  public Controller() {
    this(false);
  }

  /**
   * setting up the retrofit object with further debugging options if needed
   *
   * @param debug
   *          true or false
   */
  public Controller(boolean debug) {
    OkHttpClient.Builder builder = TmmHttpClient.newBuilder();
    if (debug) {
      HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
        @Override
        public void log(String s) {
          LOGGER.debug(s);
        }
      });
      logging.setLevel(Level.BODY); // BASIC?!
      builder.addInterceptor(logging);
    }
    retrofit = buildRetrofitInstance(builder.build());
  }

  private GsonBuilder getGsonBuilder() {
    GsonBuilder builder = new GsonBuilder();
    // class types
    builder.registerTypeAdapter(Integer.class, new JsonDeserializer<Integer>() {
      @Override
      public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
          return json.getAsInt();
        }
        catch (NumberFormatException e) {
          return 0;
        }
      }
    });
    builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
    return builder;
  }

  /**
   * call the search Info
   *
   * @param searchTerm
   *          the movie name
   * @param type
   *          the entity type to search for (movie/series)
   * @param year
   *          set the year of the movie (optional)
   * @return the {@link MovieSearch} item
   * @throws IOException
   *           any exception that could occur
   */
  public MovieSearch getMovieSearchInfo(String apiKey, String searchTerm, String type, String year) throws IOException {
    return getService().movieSearch(apiKey, searchTerm, type, year).execute().body();
  }

  /**
   * call the scrape service via ID search
   *
   * @param id
   *          the ID to search for
   * @param type
   *          the entity type to search for (movie/series)
   * @param full
   *          scrape full info
   * @return the {@link MovieEntity} item
   * @throws IOException
   *           any exception that could occur
   */
  public MovieEntity getScrapeDataById(String apiKey, String id, String type, boolean full) throws IOException {
    String plotStyle = "short";
    if (full) {
      plotStyle = "full";
    }

    return getService().movieScrapeById(apiKey, id, type, null, plotStyle).execute().body();
  }

  public SeasonSearch getSeasonsById(String apiKey, String id, String type, int season) throws IOException {
    return getService().seasonScrapeById(apiKey, id, type, season).execute().body();
  }

  public MovieEntity getEpisodesBySeasons(String apiKey, String id, String type, int season, int episode) throws IOException {
    return getService().episodeScrapeById(apiKey, id, type, season, episode).execute().body();
  }

  /**
   * Returns the created Retrofit Service
   *
   * @return retrofit object
   */
  private OmdbService getService() {
    return retrofit.create(OmdbService.class);
  }

  /**
   * Builder Class for retrofit Object
   *
   * @param client
   *          the http client
   * @return a new retrofit object.
   */
  private Retrofit buildRetrofitInstance(OkHttpClient client) {
    return new Retrofit.Builder().client(client).baseUrl("http://www.omdbapi.com")
        .addConverterFactory(GsonConverterFactory.create(getGsonBuilder().create())).build();
  }
}
