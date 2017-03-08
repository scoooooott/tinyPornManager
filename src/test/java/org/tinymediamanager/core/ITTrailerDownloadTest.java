package org.tinymediamanager.core;

import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Paths;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieTrailer;
import org.tinymediamanager.core.movie.tasks.MovieTrailerDownloadTask;

public class ITTrailerDownloadTest {

  @Test
  public void downloadTrailerTest() {
    try {
      Locale.setDefault(new Locale("en", "US"));
      Movie m = new Movie();
      m.setPath(".");
      MediaFile mf = new MediaFile(Paths.get("movie.avi"), MediaFileType.VIDEO);
      m.addToMediaFiles(mf);

      MovieTrailer t = new MovieTrailer();
      t.setUrl("http://de.clip-1.filmtrailer.com/9507_31566_a_1.wmv?log_var=72|491100001-1|-");
      m.addTrailer(t);

      MovieTrailerDownloadTask task = new MovieTrailerDownloadTask(t, m);
      Thread thread = new Thread(task);
      thread.start();
      while (thread.isAlive()) {
        Thread.sleep(1000);
      }

      File trailer = new File(".", "movie-trailer.wmv");
      if (!trailer.exists()) {
        fail();
      }

      // cleanup
      FileUtils.deleteQuietly(trailer);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
}
