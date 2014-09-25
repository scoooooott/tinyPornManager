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
import org.tinymediamanager.scraper.util.Url;

/**
 * UpdaterTasks checks if there's a new update for TMM
 * 
 * @author Myron BOyle
 */
public class UpdaterTask extends SwingWorker<Boolean, Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdaterTask.class);

  /**
   * Instantiates a new updater task.
   */
  public UpdaterTask() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public Boolean doInBackground() {
    try {
      Thread.currentThread().setName("updateThread");
      LOGGER.info("Checking for updates...");
      // Thread.sleep(10000);

      // get update url
      Properties prop = new Properties();
      prop.load(new FileInputStream(new File("getdown.txt")));
      String updateUrl = prop.getProperty("appbase");
      prop.clear();

      // download remote checksum file
      Url upd = new Url(updateUrl + "/digest.txt");
      String online = IOUtils.toString(upd.getInputStream(), "UTF-8");
      if (online == null || !online.contains("tmm.jar")) {
        LOGGER.error("Update task failed! Error downloading remote checksum information.");
        return false;
      }

      // and compare with our local
      String local = FileUtils.readFileToString(new File("digest.txt"), "UTF-8");
      if (!local.equals(online)) {
        LOGGER.info("Update needed...");
        return true;
      }
      else {
        LOGGER.info("Already up2date :)");
      }
    }
    catch (Exception e) {
      LOGGER.error("Update task failed!" + e.getMessage());
    }
    return false;
  }
}
