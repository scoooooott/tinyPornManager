package org.tinymediamanager.scraper.mpdbtv.services;

import org.tinymediamanager.scraper.mpdbtv.entities.MovieEntity;
import org.tinymediamanager.scraper.mpdbtv.entities.SearchEntity;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;
import java.util.Locale;

public interface MpdbService {


  @GET("search/{apikey}/{username}/{subscriptionkey}/movies/{searchstring}")
  Call<List<SearchEntity>> movieSearch(@Path("apikey") String apikey,
                                       @Path("username") String username,
                                       @Path("subscriptionkey") String subscriptionkey,
                                       @Path("searchstring") String searchString,
                                       @Query("language") Locale lang,
                                       @Query("saga") boolean saga,
                                       @Query("format") String format);

  @GET("movies/{apikey}/{username}/{subscriptionkey}/{id}")
  Call<MovieEntity> movieScrapebyID(
                                    @Path("apikey") String apikey,
                                    @Path("username") String username,
                                    @Path("subscriptionkey") String subscriptionkey,
                                    @Path("id") int mpdb_id,
                                    @Query("language") Locale lang,
                                    @Query("typeId") String id,
                                    @Query("format") String format);

}
