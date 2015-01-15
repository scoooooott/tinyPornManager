package org.tinymediamanager.core;

import java.io.File;

import org.junit.Test;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.MovieTrailer;
import org.tinymediamanager.core.movie.entities.Movie;

public class TrailerDownloadTest {

  @Test
  public void downloadTrailer() {
    MediaFile mf = new MediaFile(new File("/path/to", "movie.nfo"), MediaFileType.NFO);
    Movie m = new Movie();
    m.addToMediaFiles(mf);

    MovieTrailer t = new MovieTrailer();
    t.setUrl("http://de.clip-1.filmtrailer.com/9507_31566_a_1.wmv?log_var=72|491100001-1|-");
    m.addTrailer(t);

    m.downloadTrailer(t);
  }
}
