package org.tinymediamanager.thirdparty;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.MovieList;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class FreemarkerTest {

  @Test
  public void testList() throws Exception {

    Globals.startDatabase();
    MovieList ml = MovieList.getInstance();
    ml.loadMoviesFromDatabase();
    
    Configuration cfg = new Configuration();
    cfg.setDirectoryForTemplateLoading(new File("templates"));
    cfg.setObjectWrapper(new DefaultObjectWrapper());
    
    Template temp = cfg.getTemplate("listExampleHTML.ftl");

    Map root = new HashMap();
    root.put("movies", ml.getMovies());

    // TODO: set output based on name (or whatever)
    Writer out = new OutputStreamWriter(System.out);
    temp.process(root, out);
    out.flush();

    Globals.shutdownDatabase();
  }

  @Test
  public void testSingle() throws Exception {

    Globals.startDatabase();
    MovieList ml = MovieList.getInstance();
    ml.loadMoviesFromDatabase();
    
    Configuration cfg = new Configuration();
    cfg.setDirectoryForTemplateLoading(new File("templates"));
    cfg.setObjectWrapper(new DefaultObjectWrapper());
    
    Template temp = cfg.getTemplate("detailPurpleBytes.ftl");

    Map root = new HashMap();
    root.put("movie", ml.getMovies().get(1));

    // TODO: set output based on name (or whatever)
    Writer out = new OutputStreamWriter(System.out);
    temp.process(root, out);
    out.flush();

    Globals.shutdownDatabase();
  }

}
