package org.tinymediamanager.scraper.moviemeternl;

import java.util.ArrayList;

import org.junit.Test;
import org.tinymediamanager.scraper.moviemeternl.model.Film;
import org.tinymediamanager.scraper.moviemeternl.model.FilmDetail;
import org.tinymediamanager.scraper.moviemeternl.model.FilmJson;
import org.tinymediamanager.scraper.moviemeternl.model.SearchJson;

public class MoviemeterMetadataProviderTest {

  // //////////////
  // V2 JSON API
  // //////////////

  @Test
  public void JsonTestV2() {
    MoviemeterJSONApi mm = new MoviemeterJSONApi();
    SearchJson[] result = mm.filmSearch("avatar");
    for (SearchJson movie : result) {
      System.out.println(movie);
    }

    FilmJson movie = mm.filmDetail(17552); // avatar
    System.out.println(movie);

    FilmJson movie2 = mm.filmDetail("tt0499549"); // avatar
    for (FilmJson.MMActors a : movie2.getActors()) {
      System.out.println(a.getName());
    }

  }

  // //////////////
  // OLD V1 API
  // //////////////

  @Test
  public void listMethods() {
    MoviemeterApi mm = new MoviemeterApi();
    Object token;
    try {
      token = mm.methodCall("system.listMethods", null);
      System.out.println(token);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void test() {
    MoviemeterApi mm = new MoviemeterApi();
    ArrayList<Film> response = mm.filmSearch("avatar");
    mm.closeSession();
  }

  @Test
  public void detail() {
    MoviemeterApi mm = new MoviemeterApi();
    FilmDetail fd = mm.filmDetail(17552);
    System.out.println(fd.toString());
    // mm.filmImages(17552);
    mm.closeSession();
  }

  @Test
  public void imdb() {
    MoviemeterApi mm = new MoviemeterApi();
    FilmDetail fd = mm.filmSearchImdb("tt0499549");
    System.out.println(fd.getTitle());
    mm.closeSession();
  }
}
