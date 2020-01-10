/*
 * Copyright 2012 - 2020 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tinymediamanager.core.movie;

import org.junit.Test;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaTrailer;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.filenaming.MovieTrailerNaming;
import org.tinymediamanager.core.tasks.TrailerDownloadTask;
import org.tinymediamanager.core.tasks.YoutubeDownloadTask;
import org.tinymediamanager.scraper.util.youtube.model.Extension;

import java.io.File;
import java.nio.file.Paths;
import java.util.Locale;

import static org.junit.Assert.fail;

public class ITMediaTrailerDownloadTest {

  @Test
  public void downloadDirectTrailerTest() {
    // apple
    try {
      Locale.setDefault(new Locale("en", "US"));
      Movie m = new Movie();
      m.setPath("target/test-classes/testmovies/trailerdownload/");
      MediaFile mf = new MediaFile(Paths.get("target/test-classes/testmovies/trailerdownload/movie1.avi"), MediaFileType.VIDEO);
      m.addToMediaFiles(mf);

      MediaTrailer t = new MediaTrailer();
      t.setUrl("http://movietrailers.apple.com/movies/disney/coco/coco-trailer-3_h480p.mov");
      m.addTrailer(t);

      String filename = m.getTrailerFilename(MovieTrailerNaming.FILENAME_TRAILER);

      TrailerDownloadTask task = new TrailerDownloadTask(t, m, filename);
      Thread thread = new Thread(task);
      thread.start();
      while (thread.isAlive()) {
        Thread.sleep(1000);
      }

      File trailer = new File("target/test-classes/testmovies/trailerdownload/", "movie1-trailer.mov");
      if (!trailer.exists()) {
        fail();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void downloadYoutubeTrailerTest() {
    // youtube
    try {
      Locale.setDefault(new Locale("en", "US"));
      Movie m = new Movie();
      m.setPath("target/test-classes/testmovies/trailerdownload/");
      MediaFile mf = new MediaFile(Paths.get("target/test-classes/testmovies/trailerdownload/movie2.avi"), MediaFileType.VIDEO);
      m.addToMediaFiles(mf);

      MediaTrailer t = new MediaTrailer();
      t.setProvider("youtube");
      t.setQuality("720P");
      t.setUrl("https://www.youtube.com/watch?v=zNCz4mQzfEI");
      m.addTrailer(t);

      YoutubeDownloadTask task = new YoutubeDownloadTask(t, m,
              m.getTrailerFilename(MovieTrailerNaming.FILENAME_TRAILER) + "." + Extension.MP4.getText());
      Thread thread = new Thread(task);
      thread.start();
      while (thread.isAlive()) {
        Thread.sleep(1000);
      }

      File trailer = new File("target/test-classes/testmovies/trailerdownload/", "movie2-trailer.mp4");
      if (!trailer.exists()) {
        fail();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
}
