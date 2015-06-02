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
package org.tinymediamanager.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
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
  private static final Logger LOGGER       = LoggerFactory.getLogger(UpdaterTask.class);
  private static final String REGULAR_PATH = "/build";
  private static final String LEGACY_PATH  = "/java6";
  private String              changelog    = "";

  /**
   * Instantiates a new updater task.
   */
  public UpdaterTask() {
  }

  @Override
  public Boolean doInBackground() {
    if (ReleaseInfo.getVersion().equals("SVN")) {
      return false;
    }

    File getdownFile = new File("getdown.txt");
    File digestFile = new File("digest.txt");

    ArrayList<String> updateUrls = new ArrayList<String>();
    try {
      Thread.currentThread().setName("updateThread");
      LOGGER.info("Checking for updates...");

      // read getdown.txt (IOEx on any error)
      for (String line : readLines(new FileReader(getdownFile))) {
        String[] kv = line.split("=");
        if ("appbase".equals(kv[0].trim()) || "mirror".equals(kv[0].trim())) {
          updateUrls.add(kv[1].trim());
        }
      }

      boolean valid = false;
      boolean changeReleasePath = false;
      String remoteDigest = "";
      String remoteUrl = "";
      // try to download from all our mirrors
      for (String uu : updateUrls) {
        try {
          if (!uu.endsWith("/")) {
            uu += '/';
          }

          // we're on legacy update path, but no java6 -> use regular release
          if (uu.contains(LEGACY_PATH)) {
            if (!SystemUtils.IS_JAVA_1_6) {
              changeReleasePath = true;
              uu = uu.replace(LEGACY_PATH, REGULAR_PATH);
            }
          }
          else {
            // we're on regular update path, but java6 -> use legacy release
            if (SystemUtils.IS_JAVA_1_6) {
              changeReleasePath = true;
              uu = uu.replace(REGULAR_PATH, LEGACY_PATH);
            }
          }

          Url upd = new Url(uu + "digest.txt");
          LOGGER.trace("Checking " + uu);
          remoteDigest = IOUtils.toString(upd.getInputStream(), "UTF-8");
          if (remoteDigest != null && remoteDigest.contains("tmm.jar")) {
            valid = true; // bingo!
            remoteUrl = uu;
          }
        }
        catch (Exception e) {
          LOGGER.warn("Unable to download from mirror: " + e.getMessage());
        }
        if (valid) {
          break; // no exception - step out :)
        }
      }

      if (!valid) {
        // we failed to download from all mirrors
        // last chance: throw ex and try really hardcoded mirror
        throw new Exception("Error downloading remote checksum information.");
      }

      // compare with our local
      String localDigest = FileUtils.readFileToString(digestFile, "UTF-8");
      if (!localDigest.equals(remoteDigest)) {
        LOGGER.info("Update needed...");

        if (changeReleasePath) {
          // we're up/downgrading dist - DL txts..
          LOGGER.debug("Switching distribution due to java versoin, preloading correct files.");
          Url gd = new Url(remoteUrl + "getdown.txt");
          String remoteGD = IOUtils.toString(gd.getInputStream(), "UTF-8");
          FileUtils.writeStringToFile(getdownFile, remoteGD, "UTF-8");
          FileUtils.writeStringToFile(digestFile, remoteDigest, "UTF-8");
        }

        // download changelog.txt for preview
        Url upd = new Url(remoteUrl + "changelog.txt");
        changelog = IOUtils.toString(upd.getInputStream(), "UTF-8");
        return true;
      }
      else {
        LOGGER.info("Already up2date :)");
      }
    }
    catch (Exception e) {
      LOGGER.error("Update task failed badly! " + e.getMessage());

      try {
        // try a hardcoded "backup url" for GD.txt, where we could specify a new location :)
        LOGGER.info("Trying fallback...");
        String fallback = "http://www.tinymediamanager.org";
        if (SystemUtils.IS_JAVA_1_6) {
          fallback += LEGACY_PATH;
        }
        if (ReleaseInfo.isPreRelease()) {
          fallback += "/getdown_prerelease.txt";
        }
        else if (ReleaseInfo.isNightly()) {
          fallback += "/getdown_nightly.txt";
        }
        else {
          fallback += "/getdown.txt";
        }
        Url upd = new Url(fallback);
        String gd = IOUtils.toString(upd.getInputStream(), "UTF-8");
        if (gd == null || gd.isEmpty() || !gd.contains("appbase")) {
          throw new Exception("could not even download our fallback");
        }
        FileUtils.writeStringToFile(getdownFile, gd);
        return true;
      }
      catch (Exception e2) {
        LOGGER.error("Update fallback failed!" + e.getMessage());
        MessageManager.instance
            .pushMessage(new Message(MessageLevel.ERROR, "Please reinstal tinyMediaManager!", "Update check failed very badly :("));
      }
    }
    return false;
  }

  /**
   * Reads the contents of the supplied input stream into a list of lines. Closes the reader on successful or failed completion.
   */
  private static List<String> readLines(Reader in) throws IOException {
    List<String> lines = new ArrayList<String>();
    try {
      BufferedReader bin = new BufferedReader(in);
      for (String line = null; (line = bin.readLine()) != null; lines.add(line)) {
      }
    }
    finally {
      if (in != null) {
        try {
          in.close();
        }
        catch (IOException var2) {
          // ok
        }
      }

    }
    return lines;
  }

  public String getChangelog() {
    return changelog;
  }
}
