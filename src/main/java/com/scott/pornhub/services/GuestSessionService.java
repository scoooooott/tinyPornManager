package com.scott.pornhub.services;

import com.scott.pornhub.entities.MovieResultsPage;
import com.scott.pornhub.entities.TvEpisodeResultsPage;
import com.scott.pornhub.entities.TvShowResultsPage;
import com.scott.pornhub.enumerations.SortBy;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GuestSessionService {

    /**
     * Get the rated movies for a guest session.
     *
     * @param language <em>Optional.</em> ISO 639-1 code.
     * @param sortBy   <em>Optional.</em> Sort the results. Allowed Values: created_at.asc, created_at.desc
     */
    @GET("guest_session/{id}/rated/movies")
    Call<MovieResultsPage> ratedMovies(
            @Path("id") String session_id,
            @Query("language") String language,
            @Query("sort_by") SortBy sortBy

    );

    /**
     * Get the rated TV shows for a guest session.
     *
     * @param language <em>Optional.</em> ISO 639-1 code.
     * @param sortBy   <em>Optional.</em> Sort the results. Allowed Values: created_at.asc, created_at.desc
     */
    @GET("guest_session/{id}/rated/tv")
    Call<TvShowResultsPage> ratedTvShows(
            @Path("id") String session_id,
            @Query("language") String language,
            @Query("sort_by") SortBy sortBy
    );

    /**
     * Get the rated TV episodes for a guest session.
     *
     * @param language <em>Optional.</em> ISO 639-1 code.
     * @param sortBy   <em>Optional.</em> Sort the results. Allowed Values: created_at.asc, created_at.desc
     */
    @GET("guest_session/{id}/rated/tv/episodes")
    Call<TvEpisodeResultsPage> ratedTvEpisodes(
            @Path("id") String session_id,
            @Query("language") String language,
            @Query("sort_by") SortBy sortBy
    );
}
