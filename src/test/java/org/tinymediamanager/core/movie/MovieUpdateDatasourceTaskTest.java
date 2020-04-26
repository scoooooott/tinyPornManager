package org.tinymediamanager.core.movie;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.tasks.MovieUpdateDatasourceTask;

/**
 * This class cannot run, since Settings() is STATIC<br>
 * run these test individually (for now)
 * 
 * @author Myron Boyle
 *
 */
public class MovieUpdateDatasourceTaskTest extends BasicTest {

  private static final int NUMBER_OF_EXPECTED_MOVIES = 65;
  private static final int NUMBER_OF_STACKED_MOVIES  = 12;
  private static final int NUMBER_OF_DISC_MOVIES     = 6;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    // MediaInfoUtils.loadMediaInfo(); // unneeded here for UDS. does not work on buildserver
    deleteSettingsFolder();
    Settings.getInstance(getSettingsFolder());
  }

  @Before
  public void setUpBeforeTest() throws Exception {
    TmmModuleManager.getInstance().startUp();
    MovieModuleManager.getInstance().startUp();

    // just a copy; we might have another movie test which uses these files
    FileUtils.copyDirectory(new File("target/test-classes/testmovies"), new File(getSettingsFolder(), "testmovies"));
    MovieModuleManager.SETTINGS.addMovieDataSources(getSettingsFolder() + "/testmovies");
  }

  @After
  public void tearDownAfterTest() throws Exception {
    MovieModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
    Utils.deleteDirectoryRecursive(Paths.get(getSettingsFolder(), "testmovies"));
    Files.delete(new File(getSettingsFolder(), "movies.db"));
  }

  @Test
  public void udsNew() throws Exception {
    MovieUpdateDatasourceTask task = new MovieUpdateDatasourceTask();
    task.run();

    // let the propertychangeevents finish
    Thread.sleep(1000);

    showEntries();
  }

  private void showEntries() {
    int stack = 0;
    int disc = 0;
    for (Movie m : MovieList.getInstance().getMovies()) {
      System.out.println(rpad(m.getTitle(), 30) + "(Disc:" + rpad(m.isDisc(), 5) + " Stack:" + rpad(m.isStacked(), 5) + " Multi:"
          + rpad(m.isMultiMovieDir(), 5) + ")\t" + m.getPathNIO());
      if (m.isStacked()) {
        stack++;
      }
      if (m.isDisc()) {
        disc++;
      }
    }
    assertEqual("Amount of movies does not match!", NUMBER_OF_EXPECTED_MOVIES, MovieList.getInstance().getMovieCount());
    assertEqual("Amount of stacked movies does not match!", NUMBER_OF_STACKED_MOVIES, stack);
    assertEqual("Amount of disc folders does not match!", NUMBER_OF_DISC_MOVIES, disc);
  }

  public static String rpad(Object s, int n) {
    return String.format("%1$-" + n + "s", s);
  }
}
