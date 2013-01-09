package org.tinymediamanager.core;

import org.junit.Test;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.scraper.MediaTrailer;

public class TrailerDownloadTest {

  @Test
  public void downloadTrailer() {
    Movie m = new Movie();
    m.setNfoFilename(System.getProperty("java.io.tmpdir") + "Das Bourne Vermächtnis (2012).nfo");

    MediaTrailer t = new MediaTrailer();
    t.setUrl("http://de.clip-1.filmtrailer.com/9507_31566_a_1.wmv?log_var=72|491100001-1|-");
    m.addTrailer(t);

    m.downladTtrailer(t);
  }
}
