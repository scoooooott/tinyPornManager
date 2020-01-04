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

import java.util.List;
import java.util.Locale;

import org.tinymediamanager.scraper.mpdbtv.entities.MovieEntity;
import org.tinymediamanager.scraper.mpdbtv.entities.SearchEntity;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MpdbService {

  @GET("search/{apikey}/{username}/{subscriptionkey}/movies/{searchstring}")
  Call<List<SearchEntity>> movieSearch(@Path("apikey") String apikey, @Path("username") String username,
      @Path("subscriptionkey") String subscriptionkey, @Path("searchstring") String searchString, @Query("language") Locale lang,
      @Query("saga") boolean saga, @Query("_format") String format);

  @GET("movies/{apikey}/{username}/{subscriptionkey}/{id}")
  Call<MovieEntity> movieScrapebyID(@Path("apikey") String apikey, @Path("username") String username, @Path("subscriptionkey") String subscriptionkey,
      @Path("id") int mpdb_id, @Query("language") Locale lang, @Query("typeId") String id, @Query("_format") String format);

}
