package com.scott.pornhub.services;

import com.scott.pornhub.entities.AppendToResponse;
import com.scott.pornhub.entities.DiscoverFilter;
import com.scott.pornhub.entities.Keyword;
import com.scott.pornhub.entities.MovieResultsPage;
import com.scott.pornhub.entities.PornhubDate;
import com.scott.pornhub.enumerations.SortBy;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface KeywordsService {
    /**
     * Get keyword by id.
     *
     * @param keywordId A BaseKeyword TMDb id.
     */
    @GET("keyword/{keyword_id}")
    Call<Keyword> summary(
            @Path("keyword_id") Integer keywordId
    );

    /**
     * Get keyword by id.
     *
     * @param keywordId        A BaseKeyword TMDb id.
     * @param appendToResponse <em>Optional.</em> extra requests to append to the result. <b>Accepted Value(s):</b> movies
     */
    @GET("keyword/{keyword_id}")
    Call<Keyword> summary(
            @Path("keyword_id") Integer keywordId,
            @Query("append_to_response") AppendToResponse appendToResponse
    );

    /**
     * Get keyword by id.
     *
     * @param keywordId        A BaseKeyword TMDb id.
     * @param appendToResponse <em>Optional.</em> extra requests to append to the result. <b>Accepted Value(s):</b> movies
     * @param options          <em>Optional.</em> parameters for the appended extra results.
     */
    @GET("keyword/{keyword_id}")
    Call<Keyword> summary(
            @Path("keyword_id") Integer keywordId,
            @Query("append_to_response") AppendToResponse appendToResponse,
            @QueryMap Map<String, String> options
    );

    /**
     * Get the movies that belong to a keyword.
     *
     * Is highly recommend using {@link DiscoverService#discoverMovie(
     *String, String, SortBy, String, String, String, Boolean, Boolean,
     * Integer, Integer, PornhubDate, PornhubDate, PornhubDate, PornhubDate, Integer,
     * Integer, Float, Float, DiscoverFilter, DiscoverFilter, DiscoverFilter,
     * DiscoverFilter, DiscoverFilter, DiscoverFilter, Integer, DiscoverFilter,
     * Integer, Integer, DiscoverFilter, String, DiscoverFilter) discoverMovie}
     * instead of this method as it is much more flexible.
     *
     * @param keywordId A BaseKeyword TMDb id.
     */
    @GET("keyword/{keyword_id}/movies")
    Call<MovieResultsPage> movies(
            @Path("keyword_id") Integer keywordId
    );
}
