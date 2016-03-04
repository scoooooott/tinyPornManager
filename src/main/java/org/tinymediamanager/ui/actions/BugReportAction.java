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
package org.tinymediamanager.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.net.URLEncoder;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.AbstractAction;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The BugReportAction to send bug reports directly from tmm
 * 
 * @author Manuel Laggner
 */
public class BugReportAction extends AbstractAction {
  private static final long           serialVersionUID = 2468561945547768259L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Logger         LOGGER           = LoggerFactory.getLogger(BugReportAction.class);

  public BugReportAction() {
    putValue(NAME, BUNDLE.getString("BugReport")); //$NON-NLS-1$
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("BugReport")); //$NON-NLS-1$
    putValue(SMALL_ICON, IconManager.BUG);
    putValue(LARGE_ICON_KEY, IconManager.BUG);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // open the log download window
    try {
      File file = TmmUIHelper.saveFile(BUNDLE.getString("BugReport.savelogs"), "tmm_logs.zip"); //$NON-NLS-1$
      if (file != null) {
        writeLogsFile(file);
      }
    }
    catch (Exception ex) {
      LOGGER.error("Could not write logs.zip: " + ex.getMessage());
    }

    // create the url for github
    String baseUrl = "https://github.com/tinyMediaManager/tinyMediaManager/issues/new?labels[]=bug&body=";
    String params = "Version: " + ReleaseInfo.getRealVersion();
    params += "\nBuild: " + ReleaseInfo.getRealBuildDate();
    params += "\nOS: " + System.getProperty("os.name") + " " + System.getProperty("os.version");
    params += "\nJDK: " + System.getProperty("java.version") + " " + System.getProperty("os.arch") + " " + System.getProperty("java.vendor");
    params += "\n\n__What is the actual behaviour?__\n\n";
    params += "\n\n__What is the expected behaviour?__\n\n";
    params += "\n\n__Steps to reproduce:__\n\n";
    params += "\n\n__Additional__\nHave you attached the logfile from the day it happened?";

    String url = "";
    try {
      url = baseUrl + URLEncoder.encode(params, "UTF-8");
      TmmUIHelper.browseUrl(url);
    }
    catch (Exception e1) {
      LOGGER.error("FAQ", e1);
      MessageManager.instance
          .pushMessage(new Message(MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":", e1.getLocalizedMessage() }));
    }
  }

  private void writeLogsFile(File file) throws Exception {
    FileOutputStream os = new FileOutputStream(file);
    ZipOutputStream zos = new ZipOutputStream(os);

    // attach logs
    File[] logs = new File("logs").listFiles(new FilenameFilter() {
      Pattern logPattern = Pattern.compile("tmm\\.log\\.*");

      @Override
      public boolean accept(File directory, String filename) {
        Matcher matcher = logPattern.matcher(filename);
        if (matcher.find()) {
          return true;
        }
        return false;
      }
    });
    if (logs != null) {
      for (File logFile : logs) {
        try {
          FileInputStream in = new FileInputStream(logFile);
          ZipEntry ze = new ZipEntry(logFile.getName());
          zos.putNextEntry(ze);

          IOUtils.copy(in, zos);
          in.close();
          zos.closeEntry();
        }
        catch (Exception e) {
          LOGGER.warn("unable to attach " + logFile.getName() + ": " + e.getMessage());
        }
      }
    }

    try {
      FileInputStream in = new FileInputStream("launcher.log");
      ZipEntry ze = new ZipEntry("launcher.log");
      zos.putNextEntry(ze);

      IOUtils.copy(in, zos);
      in.close();
      zos.closeEntry();
    }
    catch (Exception e) {
      LOGGER.warn("unable to attach launcher.log: " + e.getMessage());
    }

    // attach config file
    try {
      ZipEntry ze = new ZipEntry("config.xml");
      zos.putNextEntry(ze);
      FileInputStream in = new FileInputStream(new File(Settings.getInstance().getSettingsFolder(), "config.xml"));

      IOUtils.copy(in, zos);
      in.close();
      zos.closeEntry();
    }
    catch (Exception e) {
      LOGGER.warn("unable to attach config.xml: " + e.getMessage());
    }

    zos.close();
    os.close();
  }
}