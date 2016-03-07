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
package org.tinymediamanager.ui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.tinymediamanager.Globals;
import org.tinymediamanager.ui.components.NativeFileChooser;

/**
 * The Class TmmUIHelper.
 * 
 * @author Manuel Laggner
 */
public class TmmUIHelper {
  private static File lastDir;

  public static File selectDirectory(String title) {
    // open JFileChooser
    return openJFileChooser(JFileChooser.DIRECTORIES_ONLY, title, true, null, null);
  }

  private static File openJFileChooser(int mode, String dialogTitle, boolean open, String filename, FileNameExtensionFilter filter) {
    JFileChooser fileChooser;
    // are we forced to open the legacy file chooser?
    if ("true".equals(System.getProperty("tmm.legacy.filechooser"))) {
      fileChooser = new JFileChooser();
    }
    else {
      fileChooser = new NativeFileChooser();
    }

    fileChooser.setFileSelectionMode(mode);
    if (lastDir != null) {
      fileChooser.setCurrentDirectory(lastDir);
    }
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
      if (mode == JFileChooser.DIRECTORIES_ONLY) {
        lastDir = fileChooser.getSelectedFile();
      }
      else {
        lastDir = fileChooser.getSelectedFile().getParentFile();
      }
      return fileChooser.getSelectedFile();
    }

    return null;
  }

  public static File selectFile(String title) {
    // open JFileChooser
    return openJFileChooser(JFileChooser.FILES_ONLY, title, true, null, null);
  }

  public static File saveFile(String title, String filename, FileNameExtensionFilter filter) {
    return openJFileChooser(JFileChooser.FILES_ONLY, title, false, filename, filter);
  }

  public static void openFile(File file) throws Exception {
    String fileType = "." + FilenameUtils.getExtension(file.getName());
    if (StringUtils.isNotBlank(Globals.settings.getMediaPlayer()) && Globals.settings.getAllSupportedFileTypes().contains(fileType)) {
      if (SystemUtils.IS_OS_MAC_OSX) {
        Runtime.getRuntime().exec(new String[] { "open", Globals.settings.getMediaPlayer(), "--args", file.getAbsolutePath() });
      }
      else {
        Runtime.getRuntime().exec(new String[] { Globals.settings.getMediaPlayer(), file.getAbsolutePath() });
      }
    }
    else if (SystemUtils.IS_OS_WINDOWS) {
      // use explorer directly - ship around access exceptions and the unresolved network bug
      // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6780505
      Runtime.getRuntime().exec(new String[] { "explorer", file.getAbsolutePath() });
    }
    else if (SystemUtils.IS_OS_LINUX) {
      // try all different starters
      boolean started = false;
      try {
        Runtime.getRuntime().exec(new String[] { "gnome-open", file.getAbsolutePath() });
        started = true;
      }
      catch (IOException e) {
      }

      if (!started) {
        try {
          Runtime.getRuntime().exec(new String[] { "kde-open", file.getAbsolutePath() });
          started = true;
        }
        catch (IOException e) {
        }
      }

      if (!started) {
        try {
          Runtime.getRuntime().exec(new String[] { "xdg-open", file.getAbsolutePath() });
          started = true;
        }
        catch (IOException e) {
        }
      }

      if (!started && Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(file);
      }
    }
    else if (Desktop.isDesktopSupported()) {
      Desktop.getDesktop().open(file);

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
