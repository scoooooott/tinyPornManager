package org.tinymediamanager.thirdparty;

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

    MovieExporter.export(ml.getMovies(), "listExampleHTML.jmte");
    MovieExporter.export(ml.getMovies(), "listExampleCSV.jmte");
    MovieExporter.export(ml.getMovies(), "listExampleXML.jmte");

    Globals.shutdownDatabase();
  }

  @Test
  public void testDetail() throws Exception {

    Globals.startDatabase();
    MovieList ml = MovieList.getInstance();
    ml.loadMoviesFromDatabase();

    MovieExporter.export(ml.getMovies(), "detailPurpleBytes.jmte");

    Globals.shutdownDatabase();
  }

}
