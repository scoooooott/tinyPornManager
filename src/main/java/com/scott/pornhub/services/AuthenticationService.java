package com.scott.pornhub.services;

import com.scott.pornhub.Pornhub;
import com.scott.pornhub.entities.GuestSession;
import com.scott.pornhub.entities.RequestToken;
import com.scott.pornhub.entities.Session;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AuthenticationService {

    /**
     * Requests authentication Token.
     */
    @GET(Pornhub.PATH_AUTHENTICATION + "/token/new")
    Call<RequestToken> requestToken();

    /**
     * Attempts to Login with a Request Token and Username/Password.
     *
     * @param username      Username of TMDb Account.
     * @param password      Password of TMDb Account.
     * @param request_token The Token you requested.
     */
    @GET(Pornhub.PATH_AUTHENTICATION + "/token/validate_with_login")
    Call<RequestToken> validateToken(
            @Query("username") String username,
            @Query("password") String password,
            @Query("request_token") String request_token
    );

    /**
     * Creates TvSeason with the Request Token you validated with your username/password.
     *
     * @param request_token The Token you requested.
     */
    @GET(Pornhub.PATH_AUTHENTICATION + "/session/new")
    Call<Session> createSession(
            @Query("request_token") String request_token
    );

    /**
     * Creates Guest TvSeason
     */
    @GET(Pornhub.PATH_AUTHENTICATION + "/guest_session/new")
    Call<GuestSession> createGuestSession();
}
