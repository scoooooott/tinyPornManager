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

package org.tinymediamanager.scraper.kyradb.services;

import org.tinymediamanager.scraper.kyradb.entities.KyraEntity;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MovieService {

  @GET("movie/tmdbid/{tmdbId}/images/animated")
  Call<KyraEntity> getAnimatedImages(@Path("tmdbId") int tmdbId);

  @GET("movie/imdbid/{imdbId}/images/animated")
  Call<KyraEntity> getAnimatedImages(@Path("imdbId") String imdbId);

  @GET("movie/tmdbid/{tmdbId}/images/characterart")
  Call<KyraEntity> getCharacterArt(@Path("tmdbId") int tmdbId);

  @GET("movie/imdbid/{imdbId}/images/characterart")
  Call<KyraEntity> getCharacterArt(@Path("imdbId") String imdbId);

  @GET("movie/tmdbid/{tmdbId}/images/logo")
  Call<KyraEntity> getLogo(@Path("tmdbId") int tmdbId);

  @GET("movie/imdbid/{imdbId}/images/logo")
  Call<KyraEntity> getLogo(@Path("imdbId") String imdbId);

}
