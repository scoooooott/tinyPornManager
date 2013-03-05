package org.tinymediamanager.thirdparty;

import org.junit.Test;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.MovieExporter;
import org.tinymediamanager.core.movie.MovieList;

public class FreemarkerTest {

  @Test
  public void testList() throws Exception {

    Globals.startDatabase();
    MovieList ml = MovieList.getInstance();
    ml.loadMoviesFromDatabase();

    MovieExporter.export(ml.getMovies(), "listExampleHTML.ftl");

    Globals.shutdownDatabase();
  }

  @Test
  public void testDetail() throws Exception {

    Globals.startDatabase();
    MovieList ml = MovieList.getInstance();
    ml.loadMoviesFromDatabase();

    MovieExporter.export(ml.getMovies(), "detailPurpleBytes.ftl");

    Globals.shutdownDatabase();
  }

}
