package org.tinymediamanager.thirdparty;

import java.io.File;

import org.junit.Test;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.movie.MovieExporter;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;

public class JmteTest {

  @Test
  public void testList() throws Exception {

    TmmModuleManager.getInstance().startUp();
    MovieModuleManager.getInstance().startUp();
    MovieList ml = MovieList.getInstance();

    MovieExporter exporter = new MovieExporter("templates" + File.separator + "ListExampleHtml");
    exporter.export(ml.getMovies(), "export" + File.separator + "ListExampleHtml");
    exporter = new MovieExporter("templates" + File.separator + "ListExampleCsv");
    exporter.export(ml.getMovies(), "export" + File.separator + "ListExampleCsv");
    exporter = new MovieExporter("templates" + File.separator + "ListExampleXml");
    exporter.export(ml.getMovies(), "export" + File.separator + "ListExampleXml");

    MovieModuleManager.getInstance().shutDown();
  }

  @Test
  public void testDetail() throws Exception {

    MovieModuleManager.getInstance().startUp();
    MovieList ml = MovieList.getInstance();
    MovieExporter exporter = new MovieExporter("templates" + File.separator + "DetailExampleHtml");
    exporter.export(ml.getMovies(), "export" + File.separator + "DetailExampleHtml");
    exporter = new MovieExporter("templates" + File.separator + "DetailExample2Html");
    exporter.export(ml.getMovies(), "export" + File.separator + "DetailExample2Html");

    MovieModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
  }
}
