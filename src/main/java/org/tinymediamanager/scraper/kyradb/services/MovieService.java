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
