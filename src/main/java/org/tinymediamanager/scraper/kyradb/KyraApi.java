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
package org.tinymediamanager.scraper.kyradb;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;

import org.tinymediamanager.scraper.http.TmmHttpClient;
import org.tinymediamanager.scraper.kyradb.services.CastService;
import org.tinymediamanager.scraper.kyradb.services.MovieService;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.bind.DateTypeAdapter;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * https://www.kyradb.com/api
 */
public class KyraApi {
  private static final String BASE_URL       = "https://www.kyradb.com/api10/";
  // the api key query parameter; hast to be supplied at all calls
  public static final String  PARAM_API_KEY  = "Apikey";
  public static final String  PARAM_USER_KEY = "Userkey";

  private Retrofit            restAdapter;
  private String              apiKey;
  private String              userKey;

  /**
   * Creates a new instance of the API
   */
  public KyraApi() {
    this.apiKey = "";
    this.userKey = "";
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getApiKey() {
    return this.apiKey;
  }

  public void setUserKey(String userKey) {
    this.userKey = userKey;
  }

  public String getUserKey() {
    return this.userKey;
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
      builder.baseUrl(BASE_URL);
      builder.addConverterFactory(GsonConverterFactory.create(getGsonBuilder().create()));
      builder.client(TmmHttpClient.newBuilder(true).addInterceptor(new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
          Request original = chain.request();
          Request.Builder request = original.newBuilder().method(original.method(), original.body());

          request.addHeader(PARAM_API_KEY, apiKey);
          request.addHeader(PARAM_USER_KEY, userKey);
          request.addHeader("Content-type", "application/json");

          Response response = chain.proceed(request.build());
          return response;
        }
      }).build());
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

  public MovieService getMovieService() {
    return getRestAdapter().create(MovieService.class);
  }

  public CastService getCastService() {
    return getRestAdapter().create(CastService.class);
  }
}
