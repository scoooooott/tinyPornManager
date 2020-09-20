package com.scott.pornhub;

import com.scott.pornhub.entities.GuestSession;
import com.scott.pornhub.entities.RequestToken;
import com.scott.pornhub.entities.Session;
import com.scott.pornhub.exceptions.PornhubAuthenticationFailedException;
import com.scott.pornhub.services.AuthenticationService;
import java.io.IOException;
import javax.annotation.Nullable;
import okhttp3.Authenticator;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class PornhubAuthenticator implements Authenticator {

    private final Pornhub pornhub;

    public PornhubAuthenticator(Pornhub pornhub) {
        this.pornhub = pornhub;
    }

    @Override
    public Request authenticate(@Nullable Route route, Response response) throws IOException {
        return handleRequest(response, pornhub);
    }

    @Nullable
    public static Request handleRequest(Response response, Pornhub pornhub) throws IOException {
        if (response.request().url().pathSegments().get(0).equals(Pornhub.PATH_AUTHENTICATION)) {
            return null;
        }

        if (responseCount(response) >= 2) {
            throw new PornhubAuthenticationFailedException(30,
                    "Authentication failed: You do not have permissions to access the service.");
        }

        HttpUrl.Builder urlBuilder = response.request().url().newBuilder();

        // prefer account session if both are available
        if (pornhub.useAccountSession()) {
            if (pornhub.getUsername() == null || pornhub.getPassword() == null) {
                throw new PornhubAuthenticationFailedException(26, "You must provide a username and password.");
            }
            String session = acquireAccountSession(pornhub);
            if (session == null) {
                return null; // failed to retrieve session, give up
            }
            urlBuilder.setEncodedQueryParameter(Pornhub.PARAM_SESSION_ID, session);
        } else if (pornhub.useGuestSession()) {
            String session = acquireGuestSession(pornhub);
            if (session == null) {
                return null; // failed to retrieve session, give up
            }
            urlBuilder.setEncodedQueryParameter(Pornhub.PARAM_GUEST_SESSION_ID, pornhub.getGuestSessionId());
        } else {
            throw new PornhubAuthenticationFailedException(30,
                    "Authentication failed: You do not have permissions to access the service.");
        }

        return response.request().newBuilder().url(urlBuilder.build()).build();
    }

    @Nullable
    public static String acquireAccountSession(Pornhub pornhub) throws IOException {
        AuthenticationService authService = pornhub.getRetrofit().create(AuthenticationService.class);

        RequestToken token = authService.requestToken().execute().body();
        if (token == null) {
            return null;
        }

        token = authService.validateToken(pornhub.getUsername(), pornhub.getPassword(), token.request_token).execute().body();
        if (token == null) {
            return null;
        }

        Session session = authService.createSession(token.request_token).execute().body();
        if (session == null) {
            return null;
        }

        pornhub.setSessionId(session.session_id);
        return session.session_id;
    }

    @Nullable
    public static String acquireGuestSession(Pornhub pornhub) throws IOException {
        AuthenticationService authService = pornhub.getRetrofit().create(AuthenticationService.class);
        GuestSession session = authService.createGuestSession().execute().body();
        if (session == null) {
            return null;
        }

        pornhub.setGuestSessionId(session.guest_session_id);
        return session.guest_session_id;
    }

    private static int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
