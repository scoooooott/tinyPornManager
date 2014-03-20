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

    MovieExporter.export(ml.getMovies(), "templates" + File.separator + "ListExampleHtml", "export" + File.separator + "ListExampleHtml");
    MovieExporter.export(ml.getMovies(), "templates" + File.separator + "ListExampleCsv", "export" + File.separator + "ListExampleCsv");
    MovieExporter.export(ml.getMovies(), "templates" + File.separator + "ListExampleXml", "export" + File.separator + "ListExampleXml");

    MovieModuleManager.getInstance().shutDown();
  }

  @Test
  public void testDetail() throws Exception {

    MovieModuleManager.getInstance().startUp();
    MovieList ml = MovieList.getInstance();

    MovieExporter.export(ml.getMovies(), "templates" + File.separator + "DetailExampleHtml", "export" + File.separator + "DetailExampleHtml");
    MovieExporter.export(ml.getMovies(), "templates" + File.separator + "DetailExample2Html", "export" + File.separator + "DetailExample2Html");

    MovieModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
  }

}
