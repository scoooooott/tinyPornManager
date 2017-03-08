package org.tinymediamanager.thirdparty;

import java.nio.file.Paths;

import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.movie.MovieExporter;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;

public class JmteTest extends BasicTest {

  @Test
  public void testList() throws Exception {

    TmmModuleManager.getInstance().startUp();
    MovieModuleManager.getInstance().startUp();
    MovieList ml = MovieList.getInstance();

    MovieExporter exporter = new MovieExporter(Paths.get("templates", "ListExampleHtml"));
    exporter.export(ml.getMovies(), Paths.get(getSettingsFolder(), "ListExampleHtml"));
    exporter = new MovieExporter(Paths.get("templates", "ListExampleCsv"));
    exporter.export(ml.getMovies(), Paths.get(getSettingsFolder(), "ListExampleCsv"));
    exporter = new MovieExporter(Paths.get("templates", "ListExampleXml"));
    exporter.export(ml.getMovies(), Paths.get(getSettingsFolder(), "ListExampleXml"));

    MovieModuleManager.getInstance().shutDown();
  }

  @Test
  public void testDetail() throws Exception {

    MovieModuleManager.getInstance().startUp();
    MovieList ml = MovieList.getInstance();
    MovieExporter exporter = new MovieExporter(Paths.get("templates", "DetailExampleHtml"));
    exporter.export(ml.getMovies(), Paths.get(getSettingsFolder(), "DetailExampleHtml"));
    exporter = new MovieExporter(Paths.get("templates", "DetailExample2Html"));
    exporter.export(ml.getMovies(), Paths.get(getSettingsFolder(), "DetailExample2Html"));

    MovieModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
  }
}
