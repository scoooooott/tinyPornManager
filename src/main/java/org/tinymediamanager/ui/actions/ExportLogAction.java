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

package org.tinymediamanager.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.ui.TmmUIHelper;

/**
 * the class {@link ExportLogAction} is used to prepare debugging logs
 *
 * @author Manuel Laggner
 */
public class ExportLogAction extends TmmAction {
  private static final Logger         LOGGER           = LoggerFactory.getLogger(ExportLogAction.class);
  private static final long           serialVersionUID = -1578568721825387890L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  public ExportLogAction() {
    putValue(NAME, BUNDLE.getString("tmm.exportlogs"));
  }

  @Override
  protected void processAction(ActionEvent e) {
    // open the log download window
    Path file = null;
    try {
      String path = TmmProperties.getInstance().getProperty("exportlogs.path");
      file = TmmUIHelper.saveFile(BUNDLE.getString("BugReport.savelogs"), path, "tmm_logs.zip",
          new FileNameExtensionFilter("Zip files", ".zip"));
      if (file != null) {
        writeLogsFile(file.toFile());
        TmmProperties.getInstance().putProperty("exportlogs.path", file.toAbsolutePath().toString());
      }
    }
    catch (Exception ex) {
      LOGGER.error("Could not write logs.zip: {}", ex.getMessage());
      MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, file != null ? file.toString() : "", "message.erroropenfile",
          new String[] { ":", ex.getLocalizedMessage() }));
    }
  }

  private void writeLogsFile(File file) throws Exception {
    try (FileOutputStream os = new FileOutputStream(file); ZipOutputStream zos = new ZipOutputStream(os)) {

      // trace logs

      try (FileInputStream in = new FileInputStream("logs" + File.separator + "trace.log")) {
        ZipEntry ze = new ZipEntry("trace.log");
        zos.putNextEntry(ze);

        IOUtils.copy(in, zos);
        zos.closeEntry();
      }
      catch (Exception e) {
        LOGGER.warn("could not append trace file to the zip file: {}", e.getMessage());
      }

      // attach logs
      File[] logs = new File("logs").listFiles(new FilenameFilter() {
        Pattern logPattern = Pattern.compile("tmm\\.log\\.*");

        @Override
        public boolean accept(File directory, String filename) {
          Matcher matcher = logPattern.matcher(filename);
          return matcher.find();
        }
      });
      if (logs != null) {
        for (File logFile : logs) {
          try (FileInputStream in = new FileInputStream(logFile)) {

            ZipEntry ze = new ZipEntry(logFile.getName());
            zos.putNextEntry(ze);

            IOUtils.copy(in, zos);
            zos.closeEntry();
          }
          catch (Exception e) {
            LOGGER.warn("unable to attach {} - {}", logFile.getName(), e.getMessage());
          }
        }
      }

      try (FileInputStream in = new FileInputStream("launcher.log")) {
        ZipEntry ze = new ZipEntry("launcher.log");
        zos.putNextEntry(ze);

        IOUtils.copy(in, zos);
        in.close();
        zos.closeEntry();
      }
      catch (Exception e) {
        LOGGER.warn("unable to attach launcher.log: {}", e.getMessage());
      }

      // attach config files, but not DB
      File[] data = new File(Globals.DATA_FOLDER).listFiles((directory, filename) -> {
        return !filename.matches(".*\\.db$"); // not DB
      });
      if (data != null) {
        for (File dataFile : data) {
          try (FileInputStream in = new FileInputStream(dataFile)) {

            ZipEntry ze = new ZipEntry(dataFile.getName());
            zos.putNextEntry(ze);

            IOUtils.copy(in, zos);
            zos.closeEntry();
          }
          catch (Exception e) {
            LOGGER.warn("unable to attach {} - {}", dataFile.getName(), e.getMessage());
          }
        }
      }
    }
  }
}
