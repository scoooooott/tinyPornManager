/*
 * Copyright 2012 - 2016 Manuel Laggner
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
package org.tinymediamanager;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.tvshow.TvShowList;

import com.sun.jna.Platform;

/**
 * The class UpdateTasks. To perform needed update tasks
 * 
 * @author Manuel Laggner / Myron Boyle
 */
public class UpgradeTasks {
  private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeTasks.class);

  public static void performUpgradeTasksBeforeDatabaseLoading(String oldVersion) {
    String v = "" + oldVersion;
    if (StringUtils.isBlank(v)) {
      v = "3"; // set version for other updates
    }

    // ****************************************************
    // PLEASE MAKE THIS TO RUN MULTIPLE TIMES WITHOUT ERROR
    // NEEDED FOR NIGHTLY SNAPSHOTS ET ALL
    // SVN BUILD IS ALSO CONSIDERED AS LOWER !!!
    // ****************************************************

    // upgrade to v3 (OR DO THIS IF WE ARE INSIDE IDE)
    // if (StrgUtils.compareVersion(v, "3") < 0) {
    // LOGGER.info("Performing upgrade tasks to version 3");
    // }

  }

  /**
   * performs some upgrade tasks from one version to another<br>
   * <b>make sure, this upgrade can run multiple times (= needed for nightlies!!!)
   * 
   * @param oldVersion
   *          our current version
   */
  public static void performUpgradeTasksAfterDatabaseLoading(String oldVersion) {
    MovieList movieList = MovieList.getInstance();
    TvShowList tvShowList = TvShowList.getInstance();

    String v = "" + oldVersion;

    if (StringUtils.isBlank(v)) {
      v = "3"; // set version for other updates
    }

    // ****************************************************
    // PLEASE MAKE THIS TO RUN MULTIPLE TIMES WITHOUT ERROR
    // NEEDED FOR NIGHTLY SNAPSHOTS ET ALL
    // SVN BUILD IS ALSO CONSIDERED AS LOWER !!!
    // ****************************************************

    // upgrade to v3
    // if (StrgUtils.compareVersion(v, "3") < 0) {
    // LOGGER.info("Performing database upgrade tasks to version 3");
    // }
  }

  /**
   * rename downloaded files (getdown.jar, ...)
   */
  public static void renameDownloadedFiles() {
    // self updater
    File file = new File("getdown-new.jar");
    if (file.exists() && file.length() > 100000) {
      File cur = new File("getdown.jar");
      if (file.length() != cur.length() || !cur.exists()) {
        try {
          FileUtils.copyFile(file, cur);
        }
        catch (IOException e) {
          LOGGER.error("Could not update the updater!");
        }
      }
    }

    // exe launchers
    if (Platform.isWindows()) {
      file = new File("tinyMediaManager.new");
      if (file.exists() && file.length() > 10000 && file.length() < 50000) {
        File cur = new File("tinyMediaManager.exe");
        try {
          FileUtils.copyFile(file, cur);
        }
        catch (IOException e) {
          LOGGER.error("Could not update tmm!");
        }
      }
      file = new File("tinyMediaManagerUpd.new");
      if (file.exists() && file.length() > 10000 && file.length() < 50000) {
        File cur = new File("tinyMediaManagerUpd.exe");
        try {
          FileUtils.copyFile(file, cur);
        }
        catch (IOException e) {
          LOGGER.error("Could not update the updater!");
        }
      }
      file = new File("tinyMediaManagerCMD.new");
      if (file.exists() && file.length() > 10000 && file.length() < 50000) {
        File cur = new File("tinyMediaManagerCMD.exe");
        try {
          FileUtils.copyFile(file, cur);
        }
        catch (IOException e) {
          LOGGER.error("Could not update CMD TMM!");
        }
      }
    }

    // OSX launcher
    if (Platform.isMac()) {
      file = new File("JavaApplicationStub.new");
      if (file.exists() && file.length() > 0) {
        File cur = new File("../../MacOS/JavaApplicationStub");
        try {
          FileUtils.copyFile(file, cur);
        }
        catch (IOException e) {
          LOGGER.error("Could not update JavaApplicationStub");
        }
      }
    }

    // OSX Info.plist
    if (Platform.isMac()) {
      file = new File("Info.plist");
      if (file.exists() && file.length() > 0) {
        File cur = new File("../../Info.plist");
        try {
          FileUtils.copyFile(file, cur);
        }
        catch (IOException e) {
          LOGGER.error("Could not update JavaApplicationStub");
        }
      }
    }
  }
}
