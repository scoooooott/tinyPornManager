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

import org.tinymediamanager.scraper.omdb.entities.MovieEntity;
import org.tinymediamanager.scraper.omdb.entities.MovieSearch;
import org.tinymediamanager.scraper.omdb.entities.SeasonSearch;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OmdbService {

  @GET("/")
  Call<MovieSearch> movieSearch(@Query("apikey") String apikey, @Query("s") String user, @Query("type") String type, @Query("y") String year);

  @GET("/")
  Call<MovieEntity> movieScrapeById(@Query("apikey") String apikey, @Query("i") String id, @Query("type") String type, @Query("y") String year,
      @Query("plot") String plot);

  @GET("/")
  Call<SeasonSearch> seasonScrapeById(@Query("apikey") String apikey, @Query("i") String id, @Query("type") String type, @Query("Season") int season);

  @GET("/")
  Call<MovieEntity> episodeScrapeById(@Query("apikey") String apikey, @Query("i") String id, @Query("type") String type, @Query("Season") int season,
      @Query("Episode") int episode);

}
