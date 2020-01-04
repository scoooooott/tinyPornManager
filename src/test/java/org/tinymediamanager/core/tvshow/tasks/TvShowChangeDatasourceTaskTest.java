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

package org.tinymediamanager.core.tvshow.tasks;

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
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;

public class TvShowChangeDatasourceTaskTest extends BasicTest {
  @BeforeClass
  public static void init() throws Exception {
    deleteSettingsFolder();
    Settings.getInstance(getSettingsFolder());
  }

  @Test
  public void testMoveTvShow() throws Exception {
    String sourceFolder = Paths.get("target/test-classes/testtvshows_from").toAbsolutePath().toString();
    String destinationFolder = Paths.get("target/test-classes/testtvshows_to").toAbsolutePath().toString();

    // cleanup
    FileUtils.deleteQuietly(new File(sourceFolder));
    FileUtils.deleteQuietly(new File(destinationFolder));

    // first copy our movie files to a safe new place to do not interfere with other unit tests
    Files.createDirectories(Paths.get(sourceFolder));
    FileUtils.copyDirectory(new File("target/test-classes/testtvshows/Futurama (1999)"), new File(sourceFolder + "/Futurama (1999)"));

    TvShow tvShow = new TvShow();
    tvShow.setDataSource(sourceFolder);
    tvShow.setPath(sourceFolder + "/Futurama (1999)");

    MediaFile mf = new MediaFile(Paths.get(tvShow.getPathNIO().toString(), "poster.jpg"));
    tvShow.addToMediaFiles(mf);

    TvShowEpisode episode = new TvShowEpisode();
    episode.setSeason(1);
    episode.setEpisode(1);
    episode.setPath(sourceFolder + "/Futurama (1999)/Season 1");

    mf = new MediaFile(Paths.get(episode.getPathNIO().toString(), "Futurama - S01E01 - Space Pilot 3000.avi"));
    episode.addToMediaFiles(mf);

    tvShow.addEpisode(episode);

    Files.createDirectories(Paths.get(destinationFolder));
    TvShowChangeDatasourceTask task = new TvShowChangeDatasourceTask(Collections.singletonList(tvShow), destinationFolder);
    task.run();

    assertThat(tvShow.getDataSource()).isEqualTo(destinationFolder);

    // POSTER
    mf = tvShow.getMediaFiles(MediaFileType.POSTER).get(0);
    assertThat(mf.getFileAsPath()).isEqualTo(Paths.get(destinationFolder + "/Futurama (1999)/poster.jpg").toAbsolutePath());

    // VIDEO
    episode = tvShow.getEpisode(1, 1);
    mf = episode.getMainVideoFile();
    assertThat(mf.getFileAsPath())
        .isEqualTo(Paths.get(destinationFolder + "/Futurama (1999)/Season 1/Futurama - S01E01 - Space Pilot 3000.avi").toAbsolutePath());
    assertThat(Files.exists(mf.getFileAsPath())).isEqualTo(true);
  }
}
