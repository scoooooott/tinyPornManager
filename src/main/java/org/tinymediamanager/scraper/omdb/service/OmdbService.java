package org.tinymediamanager.scraper.omdb.service;

import org.tinymediamanager.scraper.omdb.entities.MovieEntity;
import org.tinymediamanager.scraper.omdb.entities.MovieSearch;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OmdbService {

	@GET("/")
	Call<MovieSearch> movieSearch(@Query("s") String user, @Query("type") String type, @Query("y") String year);

	@GET("/")
  Call<MovieEntity> movieScrapeById(@Query("i") String id,@Query("type") String type, @Query("y") String year, @Query("plot") String plot);

}
