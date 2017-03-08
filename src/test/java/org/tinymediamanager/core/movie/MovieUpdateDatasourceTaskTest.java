package org.tinymediamanager.core.movie;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.tasks.MovieUpdateDatasourceTask;
import org.tinymediamanager.core.movie.tasks.MovieUpdateDatasourceTask2;
import org.tinymediamanager.thirdparty.MediaInfoUtils;

/**
 * This class cannot run, since Settings() is STATIC<br>
 * run these test individually (for now)
 * 
 * @author Myron Boyle
 *
 */
public class MovieUpdateDatasourceTaskTest {

  private static final int    NUMBER_OF_EXPECTED_MOVIES = 31;
  private static final int    NUMBER_OF_STACKED_MOVIES  = 7;
  private static final int    NUMBER_OF_DISC_MOVIES     = 6;

  private static final String FOLDER                    = "target/testdata/udsMovie";

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    MediaInfoUtils.loadMediaInfo();
    Settings.getInstance(FOLDER); // can only instantiate ONCE, so recycle folder
  }

  @Before
  public void setUpBeforeTest() throws Exception {
    // do not use @BeforeClass b/c of static settings
    TmmModuleManager.getInstance().startUp();
    MovieModuleManager.getInstance().startUp();

    // just a copy; we might have another movie test which uses these files
    FileUtils.copyDirectory(new File("target/test-classes/testmovies"), new File(FOLDER, "testmovies"));
    MovieModuleManager.MOVIE_SETTINGS.addMovieDataSources(FOLDER + "/testmovies");
    MovieModuleManager.MOVIE_SETTINGS.setDetectMovieMultiDir(true); // parse MMD
  }

  @After
  public void tearDownAfterClass() throws Exception {
    MovieModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
    Utils.deleteDirectoryRecursive(Paths.get(FOLDER, "testmovies"));
    Files.delete(new File(FOLDER, "movies.db"));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void udsOld() throws Exception {
    MovieUpdateDatasourceTask task = new MovieUpdateDatasourceTask();
    task.run();
    showEntries();
  }

  @Test
  public void udsNew() throws Exception {
    MovieUpdateDatasourceTask2 task = new MovieUpdateDatasourceTask2();
    task.run();
    showEntries();
  }

  private void showEntries() {
    int stack = 0;
    int disc = 0;
    for (Movie m : MovieList.getInstance().getMovies()) {
      System.out.println(m.getTitle() + " - " + m.getPathNIO());
      if (m.isStacked()) {
        stack++;
      }
      if (m.isDisc()) {
        disc++;
      }
    }
    Assert.assertEquals("Amount of movies does not match!", NUMBER_OF_EXPECTED_MOVIES, MovieList.getInstance().getMovieCount());
    Assert.assertEquals("Amount of stacked movies does not match!", NUMBER_OF_STACKED_MOVIES, stack);
    Assert.assertEquals("Amount of disc folders does not match!", NUMBER_OF_DISC_MOVIES, disc);
  }
}
