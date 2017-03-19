/*
 * Copyright 2012 - 2017 Manuel Laggner
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
package org.tinymediamanager.ui;

import java.awt.Desktop;
import java.awt.FileDialog;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.ui.components.NativeFileChooser;

/**
 * The Class TmmUIHelper.
 * 
 * @author Manuel Laggner
 */
public class TmmUIHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(TmmUIHelper.class);

  public static Path selectDirectory(String title, String initialPath) {
    // on mac try to take the AWT FileDialog
    if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
      try {
        // open directory chooser
        return openDirectoryDialog(title, initialPath);
      }
      catch (Exception | Error e) {
        LOGGER.warn("cannot open AWT directory chooser" + e.getMessage());
      }
      finally {
        // reset system property
        System.setProperty("apple.awt.fileDialogForDirectories", "false");
      }
    }

    // open JFileChooser
    return openJFileChooser(JFileChooser.DIRECTORIES_ONLY, title, initialPath, true, null, null);
  }

  private static Path openDirectoryDialog(String title, String initialPath) throws Exception, Error {
    // set system property to choose directories
    System.setProperty("apple.awt.fileDialogForDirectories", "true");

    FileDialog chooser = new FileDialog(MainWindow.getFrame(), title);
    if (StringUtils.isNotBlank(initialPath)) {
      Path path = Paths.get(initialPath);
      if (Files.exists(path)) {
        chooser.setDirectory(path.toFile().getAbsolutePath());
      }
    }
    chooser.setVisible(true);

    // reset system property
    System.setProperty("apple.awt.fileDialogForDirectories", "false");

    if (StringUtils.isNotEmpty(chooser.getFile())) {
      return Paths.get(chooser.getDirectory(), chooser.getFile());
    }
    else {
      return null;
    }
  }

  private static Path openJFileChooser(int mode, String dialogTitle, String initialPath, boolean open, String filename,
      FileNameExtensionFilter filter) {
    JFileChooser fileChooser = null;
    // are we forced to open the legacy file chooser?
    if ("true".equals(System.getProperty("tmm.legacy.filechooser"))) {
      fileChooser = new JFileChooser();
    }
    else if (StringUtils.isNotBlank(initialPath)) {
      Path path = Paths.get(initialPath);
      if (Files.exists(path)) {
        fileChooser = new NativeFileChooser(path.toFile());
      }
    }

    if (fileChooser == null) {
      fileChooser = new NativeFileChooser();
    }

    fileChooser.setFileSelectionMode(mode);
    fileChooser.setDialogTitle(dialogTitle);

    int result = -1;
    if (open) {
      result = fileChooser.showOpenDialog(MainWindow.getFrame());
    }
    else {
      if (StringUtils.isNotBlank(filename)) {
        fileChooser.setSelectedFile(new File(filename));
        fileChooser.setFileFilter(filter);
      }
      result = fileChooser.showSaveDialog(MainWindow.getFrame());
    }

    if (result == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile().toPath();
    }

    return null;
  }

  public static Path selectFile(String title, String initialPath) {
    // try to open AWT dialog on OSX
    if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
      try {
        // open file chooser
        return openFileDialog(title, initialPath, FileDialog.LOAD, null);
      }
      catch (Exception e) {
        LOGGER.warn("cannot open AWT filechooser" + e.getMessage());
      }
      catch (Error e) {
        LOGGER.warn("cannot open AWT filechooser" + e.getMessage());
      }
    }

    // open JFileChooser
    return openJFileChooser(JFileChooser.FILES_ONLY, title, initialPath, true, null, null);
  }

  private static Path openFileDialog(String title, String initialPath, int mode, String filename) throws Exception, Error {
    FileDialog chooser = new FileDialog(MainWindow.getFrame(), title, mode);
    if (StringUtils.isNotBlank(initialPath)) {
      Path path = Paths.get(initialPath);
      if (Files.exists(path)) {
        chooser.setDirectory(path.toFile().getAbsolutePath());
      }
    }
    if (mode == FileDialog.SAVE) {
      chooser.setFile(filename);
    }
    chooser.setVisible(true);

    if (StringUtils.isNotEmpty(chooser.getFile())) {
      return Paths.get(chooser.getDirectory(), chooser.getFile());
    }
    else {
      return null;
    }
  }

  public static Path saveFile(String title, String initialPath, String filename, FileNameExtensionFilter filter) {
    // try to open AWT dialog on OSX
    if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
      try {
        // open file chooser
        return openFileDialog(title, initialPath, FileDialog.SAVE, filename);
      }
      catch (Exception e) {
        LOGGER.warn("cannot open AWT filechooser" + e.getMessage());
      }
      catch (Error e) {
        LOGGER.warn("cannot open AWT filechooser" + e.getMessage());
      }
    }

    return openJFileChooser(JFileChooser.FILES_ONLY, title, initialPath, false, filename, filter);
  }

  public static void openFile(Path file) throws Exception {
    String fileType = "." + FilenameUtils.getExtension(file.getFileName().toString());
    String abs = file.toAbsolutePath().toString();

    if (StringUtils.isNotBlank(Globals.settings.getMediaPlayer()) && Globals.settings.getAllSupportedFileTypes().contains(fileType)) {
      if (SystemUtils.IS_OS_MAC_OSX) {
        Runtime.getRuntime().exec(new String[] { "open", Globals.settings.getMediaPlayer(), "--args", abs });
      }
      else {
        Runtime.getRuntime().exec(new String[] { Globals.settings.getMediaPlayer(), abs });
      }
    }
    else if (SystemUtils.IS_OS_WINDOWS) {
      // use explorer directly - ship around access exceptions and the unresolved network bug
      // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6780505
      Runtime.getRuntime().exec(new String[] { "explorer", abs });
    }
    else if (SystemUtils.IS_OS_LINUX) {
      // try all different starters
      boolean started = false;
      try {
        Runtime.getRuntime().exec(new String[] { "xdg-open", abs });
        started = true;
      }
      catch (IOException e) {
      }

      if (!started) {
        try {
          Runtime.getRuntime().exec(new String[] { "kde-open", abs });
          started = true;
        }
        catch (IOException e) {
        }
      }

      if (!started) {
        try {
          Runtime.getRuntime().exec(new String[] { "gnome-open", abs });
          started = true;
        }
        catch (IOException e) {
        }
      }

      if (!started && Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(file.toFile());
      }
    }
    else if (Desktop.isDesktopSupported()) {
      Desktop.getDesktop().open(file.toFile());

    }
    else {
      throw new UnsupportedOperationException();
    }
  }

  public static void browseUrl(String url) throws Exception {
    if (Desktop.isDesktopSupported()) {
      Desktop.getDesktop().browse(new URI(url));
    }
    else if (SystemUtils.IS_OS_LINUX) {
      // try all different starters
      boolean started = false;
      try {
        Runtime.getRuntime().exec(new String[] { "gnome-open", url });
        started = true;
      }
      catch (IOException e) {
      }

      if (!started) {
        try {
          Runtime.getRuntime().exec(new String[] { "kde-open", url });
          started = true;
        }
        catch (IOException e) {
        }
      }

      if (!started) {
        try {
          Runtime.getRuntime().exec(new String[] { "xdg-open", url });
          started = true;
        }
        catch (IOException e) {
        }
      }
    }
    else {
      throw new UnsupportedOperationException();
    }
  }
}
