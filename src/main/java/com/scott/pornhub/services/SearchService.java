package com.scott.pornhub.services;

import com.scott.pornhub.entities.CollectionResultsPage;
import com.scott.pornhub.entities.CompanyResultsPage;
import com.scott.pornhub.entities.KeywordResultsPage;
import com.scott.pornhub.entities.MediaResultsPage;
import com.scott.pornhub.entities.MovieResultsPage;
import com.scott.pornhub.entities.PersonResultsPage;
import com.scott.pornhub.entities.TvShowResultsPage;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SearchService {

    /**
     * Search for companies.
     *
     * @see <a href="https://developers.themoviedb.org/3/search/search-companies">Documentation</a>
     */
    @GET("search/company")
    Call<CompanyResultsPage> company(
        @Query("query") String query,
        @Query("page") Integer page
    );

    /**
     * Search for collections.
     *
     * @see <a href="https://developers.themoviedb.org/3/search/search-collections">Documentation</a>
     */
    @GET("search/collection")
    Call<CollectionResultsPage> collection(
        @Query("query") String query,
        @Query("page") Integer page,
        @Query("language") String language
    );

    /**
     * Search for keywords.
     *
     * @see <a href="https://developers.themoviedb.org/3/search/search-keywords">Documentation</a>
     */
    @GET("search/keyword")
    Call<KeywordResultsPage> keyword(
        @Query("query") String query,
        @Query("page") Integer page
    );

    /**
     * Search for movies.
     * <p>example:
     * https://www.pornhub.com/video/search?search=%E5%90%8C%E6%B5%8E%E5%A4%A7%E5%AD%A6%E9%AB%98%E6%95%B0&p=professional&hd=1&min_duration=10&max_duration=30
     * https://www.pornhub.com/video/search?search=%E5%90%8C%E6%B5%8E%E5%A4%A7%E5%AD%A6%E9%AB%98%E6%95%B0&p=professional&hd=1&min_duration=10&max_duration=30&page=2</p>
     *
     * @see <a href="https://developers.themoviedb.org/3/search/search-movies">Documentation</a>
     */
    @GET("video/search")
    Call<String> movie(
        @Query("search") String query,
        @Query("page") Integer page,
        @Query("hd") Integer isHd,
        @Query("min_duration") Integer minDuration,
        @Query("max_duration") Integer maxDuration,
        @Query("p") String production,
        @Query("o") String filter,
        @Query("t") String time
    );

    /**
     * Search multiple models in a single request. Multi search currently supports searching for movies, tv shows and
     * people in a single request.
     *
     * @see <a href="https://developers.themoviedb.org/3/search/multi-search">Documentation</a>
     */
    @GET("search/multi")
    Call<MediaResultsPage> multi(
        @Query("query") String query,
        @Query("page") Integer page,
        @Query("language") String language,
        @Query("region") String region,
        @Query("include_adult") Boolean includeAdult
    );

    /**
     * Search for people.
     *
     * @see <a href="https://developers.themoviedb.org/3/search/search-people">Documentation</a>
     */
    @GET("search/person")
    Call<PersonResultsPage> person(
        @Query("query") String query,
        @Query("page") Integer page,
        @Query("language") String language,
        @Query("region") String region,
        @Query("include_adult") Boolean includeAdult
    );

    /**
     * Search for TV shows.
     *
     * @see <a href="https://developers.themoviedb.org/3/search/search-tv-shows">Documentation</a>
     */
    @GET("search/tv")
    Call<TvShowResultsPage> tv(
        @Query("query") String query,
        @Query("page") Integer page,
        @Query("language") String language,
        @Query("first_air_date_year") Integer firstAirDateYear
    );
}
