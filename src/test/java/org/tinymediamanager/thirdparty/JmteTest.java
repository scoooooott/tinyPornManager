package org.tinymediamanager.thirdparty;

import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.ExportTemplate;
import org.tinymediamanager.core.MediaEntityExporter.TemplateType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.MovieExporter;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.tvshow.TvShowExporter;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;

public class JmteTest extends BasicTest {

  @BeforeClass
  public static void init() throws Exception {
    deleteSettingsFolder();
    Settings.getInstance(getSettingsFolder());

    TmmModuleManager.getInstance().startUp();
    MovieModuleManager.getInstance().startUp();
    TvShowModuleManager.getInstance().startUp();

    createFakeMovie("Movie 1");
    createFakeMovie("Another Movie");
    createFakeMovie("Cool Movie");

    createFakeShow("Best Show");
    createFakeShow("THE show");
    createFakeShow("Show 3");

    Utils.extractTemplates(true); // always extract fresh
  }

  @AfterClass
  public static void shutdown() throws Exception {
    TvShowModuleManager.getInstance().shutDown();
    MovieModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
  }

  @Test
  public void testAllMovieTemplates() throws Exception {
    MovieList ml = MovieList.getInstance();
    for (ExportTemplate t : MovieExporter.findTemplates(TemplateType.MOVIE)) {
      System.out.println("\nTEMPLATE: " + t.getPath());
      MovieExporter ex = new MovieExporter(Paths.get(t.getPath()));
      ex.export(ml.getMovies(), Paths.get(getSettingsFolder(), t.getName()));
    }
  }

  @Test
  public void testAllTvShowTemplates() throws Exception {
    TvShowList tv = TvShowList.getInstance();
    for (ExportTemplate t : TvShowExporter.findTemplates(TemplateType.TV_SHOW)) {
      System.out.println("\nTEMPLATE: " + t.getPath());
      TvShowExporter ex = new TvShowExporter(Paths.get(t.getPath()));
      ex.export(tv.getTvShows(), Paths.get(getSettingsFolder(), t.getName()));
    }
  }
}
