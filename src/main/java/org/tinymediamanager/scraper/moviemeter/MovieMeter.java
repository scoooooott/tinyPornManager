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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;

import org.tinymediamanager.scraper.http.TmmHttpClient;
import org.tinymediamanager.scraper.moviemeter.services.FilmService;
import org.tinymediamanager.scraper.moviemeter.services.SearchService;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.bind.DateTypeAdapter;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class MovieMeter {
  // the base API url
  public static final String API_URL       = "http://www.moviemeter.nl/api/";
  // the api key query parameter; hast to be supplied at all calls
  public static final String PARAM_API_KEY = "api_key";

  private Retrofit           restAdapter;
  private boolean            isDebug;
  private String             apiKey;

  public MovieMeter() {
    this.apiKey = "";
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  static class ErrorResponse {
    @SerializedName("message")
    String msg;

    @Override
    public String toString() {
      return msg;
    }
  }

  /**
   * Set the {@link retrofit.RestAdapter} log level.
   *
   * @param isDebug
   *          If true, the log level is set to {@link retrofit.RestAdapter.LogLevel#FULL}. Otherwise {@link retrofit.RestAdapter.LogLevel#NONE}.
   */
  public MovieMeter setIsDebug(boolean isDebug) {
    this.isDebug = isDebug;
    // if (restAdapter != null) {
    // restAdapter.setLogLevel(isDebug ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE);
    // }
    return this;
  }

  /**
   * Create a new {@link retrofit.RestAdapter.Builder}. Override this to e.g. set your own client or executor.
   *
   * @return A {@link retrofit.RestAdapter.Builder} with no modifications.
   */
  protected Retrofit.Builder newRestAdapterBuilder() {
    return new Retrofit.Builder();
  }

  /**
   * Return the current {@link retrofit.RestAdapter} instance. If none exists (first call), builds a new one.
   */
  protected Retrofit getRestAdapter() {
    if (restAdapter == null) {
      Retrofit.Builder builder = newRestAdapterBuilder();
      builder.baseUrl(API_URL);
      builder.addConverterFactory(GsonConverterFactory.create(getGsonBuilder().create()));
      builder.client(TmmHttpClient.newBuilder().addInterceptor(new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
          Request request = chain.request();
          HttpUrl url = request.url().newBuilder().addQueryParameter(PARAM_API_KEY, apiKey).build();
          request = request.newBuilder().url(url).build();
          return chain.proceed(request);
        }
      }).build());
      // if (isDebug) {
      // builder.setLogLevel(RestAdapter.LogLevel.FULL);
      // }
      restAdapter = builder.build();
    }
    return restAdapter;
  }

  protected GsonBuilder getGsonBuilder() {
    GsonBuilder builder = new GsonBuilder();
    // class types
    builder.registerTypeAdapter(Integer.class, new JsonDeserializer<Integer>() {
      @Override
      public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
          return Integer.valueOf(json.getAsInt());
        }
        catch (NumberFormatException e) {
          return 0;
        }
      }
    });
    builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
    builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
    return builder;
  }

  public FilmService getFilmService() {
    return getRestAdapter().create(FilmService.class);
  }

  public SearchService getSearchService() {
    return getRestAdapter().create(SearchService.class);
  }
}
