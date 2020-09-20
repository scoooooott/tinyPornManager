package com.scott.pornhub.services;

import com.scott.pornhub.entities.AppendToResponse;
import com.scott.pornhub.entities.Company;
import com.scott.pornhub.entities.DiscoverFilter;
import com.scott.pornhub.entities.MovieResultsPage;
import com.scott.pornhub.entities.PornhubDate;
import com.scott.pornhub.enumerations.SortBy;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface CompaniesService {
    /**
     * Get the basic company information for a specific A Company TMDb id.
     *
     * @param companyId A Company TMDb id.
     */
    @GET("company/{company_id}")
    Call<Company> summary(
            @Path("company_id") int companyId
    );

    /**
     * Get the basic company information for a specific A Company TMDb id.
     *
     * @param companyId        A Company TMDb id.
     * @param appendToResponse <em>Optional.</em> extra requests to append to the result. <b>Accepted Value(s):</b> movies
     */
    @GET("company/{company_id}")
    Call<Company> summary(
            @Path("company_id") int companyId,
            @Query("append_to_response") AppendToResponse appendToResponse
    );

    /**
     * Get the basic company information for a specific A Company TMDb id.
     *
     * @param companyId        A Company TMDb id.
     * @param appendToResponse <em>Optional.</em> extra requests to append to the result. <b>Accepted Value(s):</b> movies
     * @param options          <em>Optional.</em> parameters for the appended extra results.
     */
    @GET("company/{company_id}")
    Call<Company> summary(
            @Path("company_id") int companyId,
            @Query("append_to_response") AppendToResponse appendToResponse,
            @QueryMap Map<String, String> options
    );

    /**
     * Get the movies for a specific A Company TMDb id.
     * <p>
     * Is highly recommend using {@link DiscoverService#discoverMovie(
     *String, String, SortBy, String, String, String, Boolean, Boolean,
     * Integer, Integer, PornhubDate, PornhubDate, PornhubDate, PornhubDate, Integer,
     * Integer, Float, Float, DiscoverFilter, DiscoverFilter, DiscoverFilter,
     * DiscoverFilter, DiscoverFilter, DiscoverFilter, Integer, DiscoverFilter,
     * Integer, Integer, DiscoverFilter, String, DiscoverFilter) discoverMovie}
     * instead of this method as it is much more flexible.
     *
     * @param companyId A Company TMDb id.
     */
    @GET("company/{company_id}/movies")
    Call<MovieResultsPage> movies(
            @Path("company_id") int companyId
    );
}

