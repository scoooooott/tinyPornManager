package org.tinymediamanager.thirdparty;

import java.io.File;

import org.junit.Test;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.MovieExporter;
import org.tinymediamanager.core.movie.MovieList;

public class JmteTest {

  @Test
  public void testList() throws Exception {

    Globals.startDatabase();
    MovieList ml = MovieList.getInstance();
    ml.loadMoviesFromDatabase();

    MovieExporter.export(ml.getMovies(), "templates" + File.separator + "ListExampleHtml", "export" + File.separator + "ListExampleHtml");
    MovieExporter.export(ml.getMovies(), "templates" + File.separator + "ListExampleCsv", "export" + File.separator + "ListExampleCsv");
    MovieExporter.export(ml.getMovies(), "templates" + File.separator + "ListExampleXml", "export" + File.separator + "ListExampleXml");

    Globals.shutdownDatabase();
  }

  @Test
  public void testDetail() throws Exception {

    Globals.startDatabase();
    MovieList ml = MovieList.getInstance();
    ml.loadMoviesFromDatabase();

    MovieExporter.export(ml.getMovies(), "templates" + File.separator + "DetailExampleHtml", "export" + File.separator + "DetailExampleHtml");
    MovieExporter.export(ml.getMovies(), "templates" + File.separator + "DetailExample2Html", "export" + File.separator + "DetailExample2Html");

    Globals.shutdownDatabase();
  }

}
