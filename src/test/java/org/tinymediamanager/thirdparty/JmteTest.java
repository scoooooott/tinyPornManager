package org.tinymediamanager.thirdparty;

import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.MovieExporter;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;

public class JmteTest extends BasicTest {

  @BeforeClass
  public static void init() throws Exception {
    deleteSettingsFolder();
    Settings.getInstance(getSettingsFolder());

    TmmModuleManager.getInstance().startUp();
    MovieModuleManager.getInstance().startUp();

    createFakeMovie("JmteTest");
    Utils.extractTemplates();
  }

  @AfterClass
  public static void shutdown() throws Exception {
    MovieModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
  }

  @Test
  public void testList() throws Exception {
    MovieList ml = MovieList.getInstance();
    MovieExporter exporter = new MovieExporter(Paths.get("templates", "ListExampleHtml"));
    exporter.export(ml.getMovies(), Paths.get(getSettingsFolder(), "ListExampleHtml"));
    exporter = new MovieExporter(Paths.get("templates", "ListExampleCsv"));
    exporter.export(ml.getMovies(), Paths.get(getSettingsFolder(), "ListExampleCsv"));
    exporter = new MovieExporter(Paths.get("templates", "ListExampleXml"));
    exporter.export(ml.getMovies(), Paths.get(getSettingsFolder(), "ListExampleXml"));
  }

  @Test
  public void testDetail() throws Exception {
    MovieList ml = MovieList.getInstance();
    MovieExporter exporter = new MovieExporter(Paths.get("templates", "DetailExampleHtml"));
    exporter.export(ml.getMovies(), Paths.get(getSettingsFolder(), "DetailExampleHtml"));
    exporter = new MovieExporter(Paths.get("templates", "DetailExample2Html"));
    exporter.export(ml.getMovies(), Paths.get(getSettingsFolder(), "DetailExample2Html"));
  }
}
