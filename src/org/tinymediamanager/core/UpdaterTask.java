/*
 * Copyright 2012 - 2014 Manuel Laggner
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
package org.tinymediamanager.core;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.scraper.util.Url;

/**
 * UpdaterTasks checks if there's a new update for TMM
 * 
 * @author Myron BOyle
 */
public class UpdaterTask extends SwingWorker<Boolean, Void> {
  private static final Logger LOGGER    = LoggerFactory.getLogger(UpdaterTask.class);

  private String              changelog = "";

  /**
   * Instantiates a new updater task.
   */
  public UpdaterTask() {
  }

  @Override
  public Boolean doInBackground() {
    try {
      if (ReleaseInfo.getVersion().equals("SVN")) {
        return false;
      }

      Thread.currentThread().setName("updateThread");
      LOGGER.info("Checking for updates...");
      File file = new File("getdown.txt");
      if (!file.exists()) {
        // we are a live instance and have no getdown.txt file? WTF?
        // we _might_ use an fallback here... but for now inform the user
        LOGGER.warn("getdown.txt not found - please reinstal TMM!");
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "Please reinstal tinyMediaManager!", "Update check failed badly"));
        return false;
      }

      // get update url
      Properties prop = new Properties();
      prop.load(new FileInputStream(file));
      String updateUrl = prop.getProperty("appbase");
      prop.clear();

      // download remote checksum file
      Url upd = new Url(updateUrl + "/digest.txt");
      String online = IOUtils.toString(upd.getInputStream(), "UTF-8");
      if (online == null || !online.contains("tmm.jar")) {
        throw new Exception("Error downloading remote checksum information."); // for fallback
      }

      // and compare with our local
      String local = FileUtils.readFileToString(new File("digest.txt"), "UTF-8");
      if (!local.equals(online)) {
        LOGGER.info("Update needed...");
        // download changelog.txt for preview
        upd = new Url(updateUrl + "/changelog.txt");
        changelog = IOUtils.toString(upd.getInputStream(), "UTF-8");
        return true;
      }
      else {
        LOGGER.info("Already up2date :)");
      }
    }
    catch (Exception e) {
      LOGGER.error("Update task failed! " + e.getMessage());

      // when eg google shuts down suddenly, or we have a corrupted HTML download,
      // try a "backup url" for GD.txt, where we could specify a new location :)
      String fallback = "http://www.tinymediamanager.org/";
      if (ReleaseInfo.isPreRelease()) {
        fallback += "getdown_prerelease.txt";
      }
      else if (ReleaseInfo.isNightly()) {
        fallback += "getdown_nightly.txt";
      }
      else {
        fallback += "getdown.txt";
      }

      try {
        LOGGER.info("Trying fallback");
        Url upd = new Url(fallback);
        String gd = IOUtils.toString(upd.getInputStream(), "UTF-8");
        if (gd == null || gd.isEmpty() || !gd.contains("appbase")) {
          // download corrupted; or a 404 html page downloaded (since we do not use that yet :p)
          return false;
        }
        FileUtils.writeStringToFile(new File("getdown.txt"), gd);
        return true;
      }
      catch (Exception e2) {
        LOGGER.error("Update fallback failed!" + e.getMessage());
      }
    }
    return false;
  }

  public String getChangelog() {
    return changelog;
  }
}
