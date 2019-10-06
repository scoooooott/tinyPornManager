package org.tinymediamanager.scraper.animated.services;

import org.tinymediamanager.scraper.animated.entities.KyraEntity;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CastService {

  @GET("cast/tmdbid/{tmdbId}/images/actorart")
  Call<KyraEntity> getActorImages(@Path("tmdbId") int tmdbId);

  // @GET("cast/imdbid/{imdbId}/images/actorart")
  // Call<KyraEntity> getActorImages(@Path("imdbId") String imdbId);

}
