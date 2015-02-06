package org.tinymediamanager.scraper.traktv;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.scraper.trakttv.TraktTv;

import com.uwetrottmann.trakt.v2.TraktV2;

public class TraktTvTest {

  private static final String  AUTH = "929a98484be4bdfd4aa0a8c676cafda2669d087dc9ba672367af522d9bdebb88";
  private static final TraktTv t    = new TraktTv(AUTH);

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    TmmModuleManager.getInstance().startUp();
    MovieModuleManager.getInstance().startUp();
    TvShowModuleManager.getInstance().startUp();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    TmmModuleManager.getInstance().shutDown();
    MovieModuleManager.getInstance().shutDown();
    TvShowModuleManager.getInstance().shutDown();
  }

  @Test
  public void auth() {
    try {
      String sampleState = new BigInteger(130, new SecureRandom()).toString(32);
      OAuthClientRequest req = TraktV2.getAuthorizationRequest("a8e7e30fd7fd3f397b6e079f9f023e790f9cbd80a2be57c104089174fa8c6d89",
          "urn:ietf:wg:oauth:2.0:oob", "asdf", "tinyMediaManager");
      System.out.println(req.getLocationUri());

      // Url url;
      // String auth = "";
      // try {
      // url = new Url(req.getLocationUri());
      // InputStream in = url.getInputStream();
      // System.out.println(url.getUrl());
      // String online = IOUtils.toString(url.getInputStream(), "UTF-8");
      // in.close();
      // auth = StrgUtils.substr(online, ".*([a-fA-F0-9]{40}).*");
      // System.out.println(auth);
      // }
      // catch (MalformedURLException e) {
      // e.printStackTrace();
      // }
      // catch (IOException e) {
      // e.printStackTrace();
      // }
      // catch (InterruptedException e) {
      // e.printStackTrace();
      // }

      // OAuthClientRequest atr = TraktV2.getAccessTokenRequest("a8e7e30fd7fd3f397b6e079f9f023e790f9cbd80a2be57c104089174fa8c6d89",
      // "ab297a186a44a374c91ade21b9b76a7709c6411bf5bab8c9480ef4a3488426b1", "urn:ietf:wg:oauth:2.0:oob", AUTH);
      // System.out.println(atr.toString());
      // Url url;
      // String auth = "";
      // try {
      // url = new Url(req.getLocationUri());
      // InputStream in = url.getInputStream();
      // System.out.println(url.getUrl());
      // Response response = Jsoup.connect(url.toString()).followRedirects(true).execute();
      // System.out.println(response.url());
      // // Document doc = Jsoup.parse(in, "UTF-8", "");
      // // System.out.println(doc.baseUri());
      // // auth = StrgUtils.substr(online, ".*([a-fA-F0-9]{40}).*");
      // // System.out.println(auth);
      // in.close();
      // }
      // catch (MalformedURLException e) {
      // e.printStackTrace();
      // }
      // catch (IOException e) {
      // e.printStackTrace();
      // }
      // catch (InterruptedException e) {
      // e.printStackTrace();
      // }

      OAuthAccessTokenResponse resp = TraktV2.getAccessToken("a8e7e30fd7fd3f397b6e079f9f023e790f9cbd80a2be57c104089174fa8c6d89",
          "ab297a186a44a374c91ade21b9b76a7709c6411bf5bab8c9480ef4a3488426b1", "urn:ietf:wg:oauth:2.0:oob", AUTH);
      System.out.println(resp.toString());

    }
    catch (OAuthSystemException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (OAuthProblemException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void syncTraktMovieCollection() {
    t.syncTraktMovieCollection();
  }

  @Test
  public void syncTraktMovieWatched() {
    t.syncTraktMovieWatched();
  }

  @Test
  public void syncTraktTvShowCollection() {
    t.syncTraktTvShowCollection();
  }

  @Test
  public void syncTraktTvShowWatched() {
    // t.syncTraktTvShowWatched();
  }

  // @Test
  // public void getTvLib() {
  // List<TvShow> shows = t.getManager().userService().libraryShowsWatched(Globals.settings.getTraktUsername(), Extended.MIN);
  // System.out.println(shows.size());
  // }
  //
  // @Test
  // public void getGenres() {
  // List<Genre> mg = t.getManager().genreService().movies();
  // mg.addAll(t.getManager().genreService().shows());
  // for (Genre genre : mg) {
  // System.out.println(genre.name);
  // }
  // }

  @Test
  public void clearTvShows() {
    // t.clearTraktTvShowCollection();
  }

  @Test
  public void clearMovies() {
    t.clearTraktMovies();
  }

}
