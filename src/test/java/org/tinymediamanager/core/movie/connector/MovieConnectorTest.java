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

package org.tinymediamanager.core.movie.connector;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.filenaming.MovieNfoNaming;

public class MovieConnectorTest extends BasicTest {

  @BeforeClass
  public static void setup() {
    deleteSettingsFolder();
    Settings.getInstance(getSettingsFolder());
    try {
      Files.createDirectories(Paths.get(getSettingsFolder(), "movie_nfo_out"));
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  public void testMovieToXbmcConnectorKodi() {
    try {
      // load data from a given NFO (with unsupported tags)
      MovieNfoParser parser = MovieNfoParser.parseNfo(Paths.get("target/test-classes/movie_nfo/kodi.nfo"));
      Movie movie = parser.toMovie();
      movie.setPath(Paths.get(getSettingsFolder(), "movie_nfo_out").toString());
      movie.addToMediaFiles(new MediaFile(Paths.get(getSettingsFolder(), "movie_nfo/kodi.nfo"), MediaFileType.NFO));

      // and write it again
      IMovieConnector connector = new MovieToXbmcConnector(movie);
      connector.write(Collections.singletonList(MovieNfoNaming.MOVIE_NFO));
    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  public void testMovieToXbmcConnectorKodi2() {
    try {
      // load data from a given NFO (with unsupported tags)
      MovieNfoParser parser = MovieNfoParser.parseNfo(Paths.get("target/test-classes/movie_nfo/kodi2.nfo"));
      Movie movie = parser.toMovie();
      MediaFile video = new MediaFile(Paths.get(getSettingsFolder(), "movie_nfo_out/test2.avi"));
      movie.addToMediaFiles(video);
      movie.setPath(Paths.get(getSettingsFolder(), "movie_nfo_out").toString());
      movie.addToMediaFiles(new MediaFile(Paths.get(getSettingsFolder(), "movie_nfo/kodi2.nfo"), MediaFileType.NFO));

      // and write it again
      IMovieConnector connector = new MovieToKodiConnector(movie);
      connector.write(Collections.singletonList(MovieNfoNaming.FILENAME_NFO));
    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  public void testMovieToXbmcConnectorMpLegacy() {
    try {
      // load data from a given NFO (with unsupported tags)
      MovieNfoParser parser = MovieNfoParser.parseNfo(Paths.get("target/test-classes/movie_nfo/mp-legacy.nfo"));
      Movie movie = parser.toMovie();
      MediaFile video = new MediaFile(Paths.get(getSettingsFolder(), "movie_nfo_out/test3.avi"));
      movie.addToMediaFiles(video);
      movie.setPath(Paths.get(getSettingsFolder(), "movie_nfo_out").toString());
      movie.addToMediaFiles(new MediaFile(Paths.get(getSettingsFolder(), "movie_nfo/mp-legacy.nfo"), MediaFileType.NFO));

      // and write it again
      IMovieConnector connector = new MovieToMpLegacyConnector(movie);
      connector.write(Collections.singletonList(MovieNfoNaming.FILENAME_NFO));
    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }
  }
}
