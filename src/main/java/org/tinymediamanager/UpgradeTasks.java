/*
 * Copyright 2012 - 2015 Manuel Laggner
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
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.util.StrgUtils;

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
      v = "2.6.9"; // set version for other updates
    }

    // ****************************************************
    // PLEASE MAKE THIS TO RUN MULTIPLE TIMES WITHOUT ERROR
    // NEEDED FOR NIGHTLY SNAPSHOTS ET ALL
    // ****************************************************

    // upgrade to v2.7 (OR DO THIS IF WE ARE INSIDE IDE)
    if (StrgUtils.compareVersion(v, "2.7") < 0 || ReleaseInfo.isSvnBuild()) {

      // migrate to config dir
      moveToConfigFolder(new File("movies.db"));
      moveToConfigFolder(new File("tvshows.db"));
      moveToConfigFolder(new File("scraper_imdb.conf"));
      moveToConfigFolder(new File("tmm_ui.prop"));

    }
  }

  private static void moveToConfigFolder(File f) {
    if (f.exists()) {
      File fnew = new File(Settings.getInstance().getSettingsFolder(), f.getName());
      try {
        Utils.moveFileSafe(f, fnew);
      }
      catch (IOException e) {
        LOGGER.warn("error moving " + f);
      }
    }
  }

  /**
   * performs some upgrade tasks from one version to another<br>
   * <b>make sure, this upgrade can run multiple times (= needed for nightlies!!!)
   * 
   * @param oldVersion
   *          our current version
   */
  public static void performUpgradeTasksAfterDatabaseLoading(String oldVersion) {
    // MovieList movieList = MovieList.getInstance();
    // TvShowList tvShowList = TvShowList.getInstance();
    String v = "" + oldVersion;

    if (StringUtils.isBlank(v)) {
      v = "2.6.9"; // set version for other updates
    }

    // ****************************************************
    // PLEASE MAKE THIS TO RUN MULTIPLE TIMES WITHOUT ERROR
    // NEEDED FOR NIGHTLY SNAPSHOTS ET ALL
    // ****************************************************

    // upgrade to v2.7
    if (StrgUtils.compareVersion(v, "2.7") < 0 || ReleaseInfo.isSvnBuild()) {
      // delete tmm.odb; objectdb.conf; log dir
      FileUtils.deleteQuietly(new File("tmm.odb"));
      FileUtils.deleteQuietly(new File("tmm.odb$"));
      FileUtils.deleteQuietly(new File("objectdb.conf"));
      FileUtils.deleteQuietly(new File("log"));
      Globals.settings.removeSubtitleFileType(".idx"); // aww, we never removed...

      // cleaup of native folder
      cleanupNativeFolder();

      // We do not migrate settings!
      // We cannot determine, if a user has unset a value, or the default changed!
      // So reSet some default values, but ONLY for release ONCE;
      // else every start of prerel/nightly would reset this over and over again
      if (ReleaseInfo.isReleaseBuild()) {
        Globals.settings.getMovieSettings().setImageBanner(true);
        Globals.settings.getMovieSettings().setImageLogo(true);
        Globals.settings.getMovieSettings().setImageClearart(true);
        Globals.settings.getMovieSettings().setImageDiscart(true);
        Globals.settings.getMovieSettings().setImageThumb(true);
        Globals.settings.getMovieSettings().setUseTrailerPreference(true);
        Globals.settings.writeDefaultSettings(); // activate default plugins
      }
    }
  }

  /**
   * cleanup the native folder
   * 
   * only the specified folders should survive
   * 
   * Windows: windows-x86 windows-x64 Linux: linux-x86 linux-x64 Mac OSX: mac-x86 mac-x64
   */
  private static void cleanupNativeFolder() {
    // no cleanup in SVN
    if (ReleaseInfo.isSvnBuild()) {
      return;
    }

    try {
      File[] nativeFiles = new File("native").listFiles();
      if (nativeFiles == null) {
        return;
      }

      for (File file : nativeFiles) {
        if (!file.isDirectory()) {
          continue;
        }

        if (Platform.isWindows() && !"windows-x86".equals(file.getName()) && !"windows-x64".equals(file.getName())) {
          FileUtils.deleteQuietly(file);
        }
        else if (Platform.isLinux() && !"linux-x86".equals(file.getName()) && !"linux-x64".equals(file.getName())) {
          FileUtils.deleteQuietly(file);
        }
        else if (Platform.isMac() && !"mac-x86".equals(file.getName()) && !"mac-x64".equals(file.getName())) {
          FileUtils.deleteQuietly(file);
        }
      }
    }
    catch (Exception e) {
      LOGGER.warn("failed to cleanup native folder: " + e.getMessage());
    }
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
