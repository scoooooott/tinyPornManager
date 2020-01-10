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
package org.tinymediamanager.core.tasks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.util.UrlUtil;

/**
 * UpdaterTasks checks if there's a new update for TMM
 * 
 * @author Myron Boyle
 */
public class UpdaterTask extends SwingWorker<Boolean, Void> {
  private static final Logger LOGGER      = LoggerFactory.getLogger(UpdaterTask.class);
  private String              changelog   = "";
  private boolean             forceUpdate = false;

  /**
   * Instantiates a new updater task.
   */
  public UpdaterTask() {
  }

  @Override
  public Boolean doInBackground() {
    if (ReleaseInfo.isGitBuild()) {
      return false;
    }

    Path getdownFile = Paths.get("getdown.txt");
    Path digestFile = Paths.get("digest.txt");

    ArrayList<String> updateUrls = new ArrayList<>();
    try {
      Thread.currentThread().setName("updateThread");
      LOGGER.info("Checking for updates...");

      // read getdown.txt (IOEx on any error)
      for (String line : readLines(new FileReader(getdownFile.toFile()))) {
        String[] kv = line.split("=");
        if ("appbase".equals(kv[0].trim()) || "mirror".equals(kv[0].trim())) {
          updateUrls.add(kv[1].trim());
        }
      }

      boolean valid = false;
      String remoteDigest = "";
      String remoteUrl = "";
      // try to download from all our mirrors
      for (String uu : updateUrls) {
        if (!uu.endsWith("/")) {
          uu += '/';
        }

        String url = uu + "digest.txt";

        LOGGER.trace("Checking {}", uu);
        try {
          remoteDigest = UrlUtil.getStringFromUrl(url);
          if (remoteDigest != null && remoteDigest.contains("tmm.jar")) {
            remoteDigest = remoteDigest.trim();
            valid = true; // bingo!
            remoteUrl = uu;
          }
        }
        catch (InterruptedException | InterruptedIOException e) {
          // do not swallow these Exceptions
          Thread.currentThread().interrupt();
        }
        catch (Exception e) {
          LOGGER.warn("Unable to download from url {} - {}", url, e.getMessage());
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
      String localDigest = Utils.readFileToString(digestFile);
      localDigest = localDigest.trim();
      if (!localDigest.equals(remoteDigest)) {
        LOGGER.info("Update needed...");

        String remoteGD = UrlUtil.getStringFromUrl(remoteUrl + "getdown.txt");
        if (remoteGD.contains("forceUpdate")) {
          forceUpdate = true;
        }

        // download changelog.txt for preview
        changelog = UrlUtil.getStringFromUrl(remoteUrl + "changelog.txt");
        return true;
      }
      else {
        LOGGER.info("Already up2date :)");
      }
    }
    catch (InterruptedException | InterruptedIOException e) {
      // do not swallow these Exceptions
      Thread.currentThread().interrupt();
    }
    catch (Exception e) {
      LOGGER.error("Update task failed badly! {}", e.getMessage());

      try {
        // try a hardcoded "backup url" for GD.txt, where we could specify a new location :)
        LOGGER.info("Trying fallback...");
        String fallback = "https://www.tinymediamanager.org";
        if (ReleaseInfo.isPreRelease()) {
          fallback += "/getdown_prerelease.txt";
        }
        else if (ReleaseInfo.isNightly()) {
          fallback += "/getdown_nightly.txt";
        }
        else {
          fallback += "/getdown.txt";
        }

        String gd = UrlUtil.getStringFromUrl(fallback);
        if (StringUtils.isBlank(gd) || !gd.contains("appbase")) {
          throw new Exception("could not even download our fallback");
        }
        Utils.writeStringToFile(getdownFile, gd);
        return true;
      }
      catch (InterruptedException | InterruptedIOException e2) {
        // do not swallow these Exceptions
        Thread.currentThread().interrupt();
      }
      catch (Exception e2) {
        LOGGER.error("Update fallback failed! {}", e2.getMessage());
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "Update check failed :(", e2.getMessage()));
      }
    }
    return false;
  }

  /**
   * Reads the contents of the supplied input stream into a list of lines. Closes the reader on successful or failed completion.
   */
  private static List<String> readLines(Reader in) throws IOException {
    List<String> lines = new ArrayList<>();
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

  /**
   * when forced, do not ask for confirmation dialog.
   * 
   * @return true/false
   */
  public boolean isForcedUpdate() {
    return forceUpdate;
  }
}
