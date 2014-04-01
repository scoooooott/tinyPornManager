package org.tinymediamanager.scraper.traktv;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.scraper.trakttv.TraktTv;

public class TraktTvTest {

  private static final TraktTv t = new TraktTv(Globals.settings.getTraktUsername(), Globals.settings.getTraktPassword(),
                                     Globals.settings.getTraktAPI());

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
    t.sendMyMoviesToTrakt(true);
  }

  @Test
  public void updatedWatchedMoviesFromTrakt() {
    t.updatedWatchedMoviesFromTrakt();
  }
}
