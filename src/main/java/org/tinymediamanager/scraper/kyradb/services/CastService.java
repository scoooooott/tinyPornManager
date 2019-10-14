package org.tinymediamanager.scraper.kyradb.services;

import org.tinymediamanager.scraper.kyradb.entities.KyraEntity;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CastService {

  @GET("cast/tmdbid/{tmdbId}/images/actorart")
  Call<KyraEntity> getActorImages(@Path("tmdbId") int tmdbId);

  // @GET("cast/imdbid/{imdbId}/images/actorart")
  // Call<KyraEntity> getActorImages(@Path("imdbId") String imdbId);

}
