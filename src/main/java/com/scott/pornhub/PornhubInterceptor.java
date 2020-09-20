package com.scott.pornhub;

import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * {@link Interceptor} to add the API key query parameter and if available session information. As it modifies the URL
 * and may retry requests, ensure this is added as an application interceptor (never a network interceptor), otherwise
 * caching will be broken and requests will fail.
 */
public class PornhubInterceptor implements Interceptor {

    private final Pornhub pornhub;


    public PornhubInterceptor(Pornhub pornhub) {
        this.pornhub = pornhub;
    }

    @Override
    public Response intercept(@Nonnull Chain chain) throws IOException {
        return handleIntercept(chain, pornhub);
    }

    /**
     * If the host matches {@link Pornhub#API_HOST} adds a query parameter with the API key.
     */
    public static Response handleIntercept(Chain chain, Pornhub pornhub) throws IOException {
        Request request = chain.request();

        if (!Pornhub.API_HOST.equals(request.url().host())) {
            // do not intercept requests for other hosts
            // this allows the interceptor to be used on a shared okhttp client
            return chain.proceed(request);
        }

        // add (or replace) the API key query parameter
        HttpUrl.Builder urlBuilder = request.url().newBuilder();
        urlBuilder.setEncodedQueryParameter(Pornhub.PARAM_API_KEY, pornhub.apiKey());

        if (pornhub.isLoggedIn()) {
            // add auth only for paths that require it
            List<String> pathSegments = request.url().pathSegments();
            if ((pathSegments.size() >= 2 && pathSegments.get(1).equals("account"))
                    || pathSegments.get(pathSegments.size() - 1).equals("account_states")
                    || pathSegments.get(pathSegments.size() - 1).equals("rating")
                    || !request.method().equals("GET")) {
                addSessionToken(pornhub, urlBuilder);
            }
        }

        Request.Builder builder = request.newBuilder();
        builder.url(urlBuilder.build());
        Response response = chain.proceed(builder.build());

        if (!response.isSuccessful()) {
            // re-try if the server indicates we should
            String retryHeader = response.header("Retry-After");
            if (retryHeader != null) {
                try {
                    int retry = Integer.parseInt(retryHeader);
                    Thread.sleep((int) ((retry + 0.5) * 1000));

                    // close body of unsuccessful response
                    if (response.body() != null) {
                        response.body().close();
                    }
                    // is fine because, unlike a network interceptor, an application interceptor can re-try requests
                    return handleIntercept(chain, pornhub);
                } catch (NumberFormatException | InterruptedException ignored) {
                }
            }
        }

        return response;
    }

    private static void addSessionToken(Pornhub pornhub, HttpUrl.Builder urlBuilder) {
        // prefer account session if both are available
        if (pornhub.getSessionId() != null) {
            urlBuilder.addEncodedQueryParameter(Pornhub.PARAM_SESSION_ID, pornhub.getSessionId());
        } else if (pornhub.getGuestSessionId() != null) {
            urlBuilder.addEncodedQueryParameter(Pornhub.PARAM_GUEST_SESSION_ID, pornhub.getGuestSessionId());
        }
    }

}
