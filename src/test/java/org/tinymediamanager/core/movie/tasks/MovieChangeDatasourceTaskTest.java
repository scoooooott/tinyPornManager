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

package org.tinymediamanager.core.movie.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;

public class MovieChangeDatasourceTaskTest extends BasicTest {
  @BeforeClass
  public static void init() throws Exception {
    deleteSettingsFolder();
    Settings.getInstance(getSettingsFolder());
  }

  @Test
  public void testMoveNormalMovie() throws Exception {
    String sourceFolder = Paths.get("target/test-classes/testmovies_from").toAbsolutePath().toString();
    String destinationFolder = Paths.get("target/test-classes/testmovies_to").toAbsolutePath().toString();

    // cleanup
    FileUtils.deleteQuietly(new File(sourceFolder));
    FileUtils.deleteQuietly(new File(destinationFolder));

    // first copy our movie files to a safe new place to do not interfere with other unit tests
    Files.createDirectories(Paths.get(sourceFolder));
    FileUtils.copyDirectory(new File("target/test-classes/testmovies/Single"), new File(sourceFolder + "/Single"));

    Movie movie = new Movie();
    movie.setDataSource(sourceFolder);
    movie.setPath(sourceFolder + "/Single");
    movie.setMultiMovieDir(false);

    MediaFile mf = new MediaFile(Paths.get(movie.getPathNIO().toString(), "singlefile.avi"));
    movie.addToMediaFiles(mf);

    Files.createDirectories(Paths.get(destinationFolder));
    MovieChangeDatasourceTask task = new MovieChangeDatasourceTask(Collections.singletonList(movie), destinationFolder);
    task.run();

    assertThat(movie.getDataSource()).isEqualTo(destinationFolder);

    // VIDEO
    mf = movie.getMainVideoFile();
    assertThat(mf.getFileAsPath()).isEqualTo(Paths.get(destinationFolder + "/Single/singlefile.avi").toAbsolutePath());
    assertThat(Files.exists(mf.getFileAsPath())).isEqualTo(true);
  }

  @Test
  public void testMovieMMDMovie() throws Exception {
    String sourceFolder = Paths.get("target/test-classes/testmovies_from").toAbsolutePath().toString();
    String destinationFolder = Paths.get("target/test-classes/testmovies_to").toAbsolutePath().toString();

    // cleanup
    FileUtils.deleteQuietly(new File(sourceFolder));
    FileUtils.deleteQuietly(new File(destinationFolder));

    // first copy our movie files to a safe new place to do not interfere with other unit tests
    Files.createDirectories(Paths.get(sourceFolder));
    FileUtils.copyDirectory(new File("target/test-classes/testmovies/Multi1"), new File(sourceFolder + "/Multi1"));

    Movie movie = new Movie();
    movie.setDataSource(sourceFolder);
    movie.setPath(sourceFolder + "/Multi1");
    movie.setMultiMovieDir(true);

    MediaFile mf = new MediaFile(Paths.get(movie.getPathNIO().toString(), "multifile1.avi"));
    movie.addToMediaFiles(mf);
    mf = new MediaFile(Paths.get(movie.getPathNIO().toString(), "multifile1-poster.png"));
    movie.addToMediaFiles(mf);
    mf = new MediaFile(Paths.get(movie.getPathNIO().toString(), "multifile1-fanart.png"));
    movie.addToMediaFiles(mf);

    Files.createDirectories(Paths.get(destinationFolder));
    MovieChangeDatasourceTask task = new MovieChangeDatasourceTask(Collections.singletonList(movie), destinationFolder);
    task.run();

    assertThat(movie.getDataSource()).isEqualTo(destinationFolder);

    // VIDEO
    mf = movie.getMainVideoFile();
    assertThat(mf.getFileAsPath()).isEqualTo(Paths.get(destinationFolder + "/Multi1/multifile1.avi").toAbsolutePath());
    assertThat(Files.exists(mf.getFileAsPath())).isEqualTo(true);

    // POSTER
    mf = movie.getMediaFiles(MediaFileType.POSTER).get(0);
    assertThat(mf.getFileAsPath()).isEqualTo(Paths.get(destinationFolder + "/Multi1/multifile1-poster.png").toAbsolutePath());
    assertThat(Files.exists(mf.getFileAsPath())).isEqualTo(true);

    // FANART
    mf = movie.getMediaFiles(MediaFileType.FANART).get(0);
    assertThat(mf.getFileAsPath()).isEqualTo(Paths.get(destinationFolder + "/Multi1/multifile1-fanart.png").toAbsolutePath());
    assertThat(Files.exists(mf.getFileAsPath())).isEqualTo(true);

    // CHECK that multifile2.avi has been left behind
    assertThat(Files.exists(Paths.get(sourceFolder, "Multi1", "multifile2.avi"))).isEqualTo(true);
    assertThat(Files.exists(Paths.get(destinationFolder, "Multi1", "multifile2.avi"))).isEqualTo(false);
  }
}
