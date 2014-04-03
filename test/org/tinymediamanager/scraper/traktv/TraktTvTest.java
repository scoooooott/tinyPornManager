package org.tinymediamanager.scraper.traktv;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.scraper.trakttv.TraktTv;

import com.jakewharton.trakt.entities.Genre;
import com.jakewharton.trakt.entities.TvShow;
import com.jakewharton.trakt.enumerations.Extended;

public class TraktTvTest {

  // private static final TraktTv t = new TraktTv(Globals.settings.getTraktUsername(), Globals.settings.getTraktPassword(),
  // Globals.settings.getTraktAPI());
  private static final TraktTv t = new TraktTv("myon0815", "65cd87a93dbfcf5c1743f000c1a43d8cc93ee7b8", "8653ed31627e0fcc74f4ed7d92ce9ca1");

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    TmmModuleManager.getInstance().startUp();
    MovieModuleManager.getInstance().startUp();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    TmmModuleManager.getInstance().shutDown();
    MovieModuleManager.getInstance().shutDown();
  }

  @Test
  public void sendMyMoviesToTrakt() {
    t.sendMyMoviesToTrakt();
  }

  @Test
  public void getTvLib() {
    List<TvShow> shows = t.getManager().userService().libraryShowsWatched(t.getUserName(), Extended.DEFAULT);
    System.out.println(shows.size());
  }

  @Test
  public void getGenres() {
    List<Genre> mg = t.getManager().genreService().movies();
    mg.addAll(t.getManager().genreService().shows());
    for (Genre genre : mg) {
      System.out.println(genre.name);
    }
  }

  @Test
  public void updatedWatchedMoviesFromTrakt() {
    t.updatedWatchedMoviesFromTrakt();
  }
}
