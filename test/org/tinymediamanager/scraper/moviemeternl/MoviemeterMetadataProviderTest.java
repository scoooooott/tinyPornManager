package org.tinymediamanager.scraper.moviemeternl;

import java.util.ArrayList;

import org.junit.Test;
import org.tinymediamanager.scraper.moviemeternl.model.Film;
import org.tinymediamanager.scraper.moviemeternl.model.FilmDetail;

public class MoviemeterMetadataProviderTest {
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
