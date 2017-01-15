/*
 *
 *  * Copyright 2012 - 2016 Manuel Laggner
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.tinymediamanager.core.movie.connector;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.filenaming.MovieNfoNaming;

public class MovieConnectorTest {

  @Test
  public void testMovieToXbmcConnector() {
    try {
      Files.createDirectories(Paths.get("target/test-classes/movie_nfo_out/"));
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
    try {
      // load data from a given NFO (with unsupported tags)
      MovieNfoParser parser = MovieNfoParser.parseNfo(Paths.get("target/test-classes/movie_nfo/kodi.nfo"));
      Movie movie = parser.toMovie();
      movie.setPath("target/test-classes/movie_nfo_out/");
      movie.addToMediaFiles(new MediaFile(Paths.get("target/test-classes/movie_nfo/kodi.nfo"), MediaFileType.NFO));

      // and write it again
      IMovieConnector connector = new MovieToXbmcConnector(movie);
      connector.write(Collections.singletonList(MovieNfoNaming.MOVIE_NFO));
    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }

    try {
      // load data from a given NFO (with unsupported tags)
      MovieNfoParser parser = MovieNfoParser.parseNfo(Paths.get("target/test-classes/movie_nfo/kodi2.nfo"));
      Movie movie = parser.toMovie();
      MediaFile video = new MediaFile(Paths.get("target/test-classes/movie_nfo_out/test2.avi"));
      movie.addToMediaFiles(video);
      movie.setPath("target/test-classes/movie_nfo_out/");
      movie.addToMediaFiles(new MediaFile(Paths.get("target/test-classes/movie_nfo/kodi2.nfo"), MediaFileType.NFO));

      // and write it again
      IMovieConnector connector = new MovieToKodiConnector(movie);
      connector.write(Collections.singletonList(MovieNfoNaming.FILENAME_NFO));
    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }

    try {
      // load data from a given NFO (with unsupported tags)
      MovieNfoParser parser = MovieNfoParser.parseNfo(Paths.get("target/test-classes/movie_nfo/mediaportal.nfo"));
      Movie movie = parser.toMovie();
      MediaFile video = new MediaFile(Paths.get("target/test-classes/movie_nfo_out/test3.avi"));
      movie.addToMediaFiles(video);
      movie.setPath("target/test-classes/movie_nfo_out/");
      movie.addToMediaFiles(new MediaFile(Paths.get("target/test-classes/movie_nfo/mediaportal.nfo"), MediaFileType.NFO));

      // and write it again
      IMovieConnector connector = new MovieToMediaportalConnector(movie);
      connector.write(Collections.singletonList(MovieNfoNaming.FILENAME_NFO));
    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }
  }
}
