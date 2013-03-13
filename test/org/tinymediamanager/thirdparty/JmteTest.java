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

    MovieExporter.export(ml.getMovies(), "listExample.html.jmte");
    MovieExporter.export(ml.getMovies(), "listExample.csv.jmte");
    MovieExporter.export(ml.getMovies(), "listExample.xml.jmte");

    Globals.shutdownDatabase();
  }

  @Test
  public void testDetail() throws Exception {

    Globals.startDatabase();
    MovieList ml = MovieList.getInstance();
    ml.loadMoviesFromDatabase();

    MovieExporter.export(ml.getMovies(), "detailPurpleBytes2.html.jmte");

    Globals.shutdownDatabase();
  }

}
