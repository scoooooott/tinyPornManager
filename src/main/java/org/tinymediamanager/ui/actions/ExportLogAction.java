package org.tinymediamanager.ui.actions;

import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
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
import org.tinymediamanager.InMemoryAppender;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;

/**
 * the class {@link ExportLogAction} is used to prepare debugging logs
 *
 * @author Manuel Laggner
 */
public class ExportLogAction extends TmmAction {
  private static final Logger         LOGGER           = LoggerFactory.getLogger(ExportLogAction.class);
  private static final long           serialVersionUID = -1578568721825387890L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public ExportLogAction() {
    putValue(NAME, BUNDLE.getString("tmm.exportlogs")); //$NON-NLS-1$
  }

  @Override
  protected void processAction(ActionEvent e) {
    // open the log download window
    Path file = null;
    try {
      String path = TmmProperties.getInstance().getProperty("exportlogs.path");
      file = TmmUIHelper.saveFile(BUNDLE.getString("BugReport.savelogs"), path, "tmm_logs.zip", //$NON-NLS-1$
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
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      Appender appender = lc.getLogger("ROOT").getAppender("INMEMORY");
      if (appender instanceof InMemoryAppender) {
        try (InputStream is = new ByteArrayInputStream(((InMemoryAppender) appender).getLog().getBytes())) {
          ZipEntry ze = new ZipEntry("trace.log");
          zos.putNextEntry(ze);

          IOUtils.copy(is, zos);
          zos.closeEntry();
        }
        catch (Exception e) {
          LOGGER.warn("could not append trace file to the zip file: {}", e.getMessage());
        }
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

      // attach config files, but not DB
      File[] data = new File("data").listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File directory, String filename) {
          return !filename.matches(".*\\.db$"); // not DB
        }
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
