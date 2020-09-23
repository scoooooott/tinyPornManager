package org.tinymediamanager.scraper.pornhub.v2;

import com.scott.pornhub.HtmlConverterFactory;
import com.scott.pornhub.services.MoviesService;
import com.scott.pornhub.services.SearchService;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.http.TmmHttpClient;
import retrofit2.Retrofit;

public class PornhubApi {

    protected static final String PORNHUB_DOMAIN = "www.pornhub.com";
    protected static final String PORNHUB_URL = "https://" + PORNHUB_DOMAIN + "/";

    private static final Logger LOGGER = LoggerFactory.getLogger(PornhubApi.class);

    @Nullable
    private Retrofit retrofit;

    protected Retrofit.Builder retrofitBuilder() {
        return new Retrofit.Builder()
            .baseUrl(PORNHUB_URL)
            .addConverterFactory(HtmlConverterFactory.create(PORNHUB_URL))
            .client(TmmHttpClient.newBuilder(true).build());
    }

    protected Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = retrofitBuilder().build();
        }
        return retrofit;
    }

    public MoviesService moviesService() {
        return getRetrofit().create(MoviesService.class);
    }

    public SearchService searchService() {
        return getRetrofit().create(SearchService.class);
    }
}
