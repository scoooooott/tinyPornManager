package org.tinymediamanager.scraper.mpdbtv.services;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.http.TmmHttpClient;
import org.tinymediamanager.scraper.mpdbtv.entities.MovieEntity;
import org.tinymediamanager.scraper.mpdbtv.entities.SearchEntity;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.internal.bind.DateTypeAdapter;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Controller {

  private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);
  private Retrofit retrofit;



  public Controller() {
    this(false);
  }

  public Controller(boolean debug) {
    OkHttpClient.Builder builder = TmmHttpClient.newBuilder();
    if (debug) {
      HttpLoggingInterceptor logging = new HttpLoggingInterceptor(LOGGER::debug);
      logging.setLevel(HttpLoggingInterceptor.Level.BODY);
      builder.addInterceptor(logging);
    }
    retrofit = buildRetrofitInstance(builder.build());

  }

  private GsonBuilder getGsonBuilder() {
    GsonBuilder builder = new GsonBuilder();
    // class types
    builder.registerTypeAdapter(Integer.class, (JsonDeserializer<Integer>) (json, typeOfT, context) -> {
      try {
        return json.getAsInt();
      }
      catch (NumberFormatException e) {
        return 0;
      }
    });
    builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
    return builder;
  }

  public List<SearchEntity> getSearchInformation(String apikey, String username, String subscriptionkey, String searchstring,
                                                 Locale language, boolean saga, String format ) throws IOException {

    return getService().movieSearch(apikey, username, subscriptionkey, searchstring, language, saga, format).execute().body();
  }

  public MovieEntity getScrapeInformation(String apikey, String username, String subscriptionkey,
                                          int id, Locale language, String typeId, String format) throws IOException {

    return getService().movieScrapebyID(apikey, username, subscriptionkey,id,language,typeId,format).execute().body();
  }

  private MpdbService getService() {
    return retrofit.create(MpdbService.class);
  }

  private Retrofit buildRetrofitInstance(OkHttpClient client) {
    return new Retrofit.Builder().client(client).baseUrl("http://mpdb.tv/api/v1/")
            .addConverterFactory(GsonConverterFactory.create(getGsonBuilder().create())).build();
  }



}
