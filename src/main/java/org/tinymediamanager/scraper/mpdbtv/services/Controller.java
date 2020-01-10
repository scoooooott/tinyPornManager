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
package org.tinymediamanager.scraper.mpdbtv.services;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.exceptions.HttpException;
import org.tinymediamanager.scraper.http.TmmHttpClient;
import org.tinymediamanager.scraper.mpdbtv.entities.MovieEntity;
import org.tinymediamanager.scraper.mpdbtv.entities.SearchEntity;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.internal.bind.DateTypeAdapter;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Controller {

  private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);
  private Retrofit            retrofit;

  public Controller() {
    this(false);
  }

  public Controller(boolean debug) {
    OkHttpClient.Builder builder = TmmHttpClient.newBuilder();
    if (debug) {
      HttpLoggingInterceptor logging = new HttpLoggingInterceptor(LOGGER::debug);
      logging.setLevel(HttpLoggingInterceptor.Level.BODY);
      builder.addInterceptor(logging);
    }
    builder.addInterceptor(chain -> {
      Request request = chain.request();
      Response response = chain.proceed(request);
      if (response.code() != 200) {
        throw new HttpException(response.code(), response.message());
      }
      return response;
    });
    retrofit = buildRetrofitInstance(builder.build());

  }

  private GsonBuilder getGsonBuilder() {
    GsonBuilder builder = new GsonBuilder();
    // class types
    builder.registerTypeAdapter(Integer.class, (JsonDeserializer<Integer>) (json, typeOfT, context) -> {
      try {
        return json.getAsInt();
      }
      catch (NumberFormatException e) {
        return 0;
      }
    });
    builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
    return builder;
  }

  public List<SearchEntity> getSearchInformation(String apikey, String username, String subscriptionkey, String searchstring, Locale language,
      boolean saga, String format) throws IOException {

    return getService().movieSearch(apikey, username, subscriptionkey, searchstring, language, saga, format).execute().body();
  }

  public MovieEntity getScrapeInformation(String apikey, String username, String subscriptionkey, int id, Locale language, String typeId,
      String format) throws IOException {

    return getService().movieScrapebyID(apikey, username, subscriptionkey, id, language, typeId, format).execute().body();
  }

  private MpdbService getService() {
    return retrofit.create(MpdbService.class);
  }

  private Retrofit buildRetrofitInstance(OkHttpClient client) {
    return new Retrofit.Builder().client(client).baseUrl("http://mpdb.tv/api/v1/")
        .addConverterFactory(GsonConverterFactory.create(getGsonBuilder().create())).build();
  }

}
