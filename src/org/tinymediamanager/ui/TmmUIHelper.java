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
import java.awt.FileDialog;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.transaction.NotSupportedException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.ui.components.JNativeFileChooser;

import chrriis.dj.nativeswing.swtimpl.components.JDirectoryDialog;
import chrriis.dj.nativeswing.swtimpl.components.JFileDialog;

/**
 * The Class TmmUIHelper.
 * 
 * @author Manuel Laggner
 */
public class TmmUIHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(TmmUIHelper.class);
  @SuppressWarnings("rawtypes")
  public static Class         swt    = null;

  private static File         lastDir;

  public static void init() throws ClassNotFoundException {
    // if (SystemUtils.IS_OS_LINUX) {
    swt = ClassLoader.getSystemClassLoader().loadClass("org.eclipse.swt.widgets.FileDialog");
  }

  public static File selectDirectory(String title) {
    if (swt != null) {
      // try to instantiate the native SWT filechooser
      try {
        return openDirectoryChooser(title);
      }
      catch (Exception e) {
        LOGGER.warn("cannot open directory chooser" + e.getMessage());
      }
      catch (Error e) {
        LOGGER.warn("cannot open directory chooser" + e.getMessage());
      }
    }

    // on mac try to take the AWT FileDialog
    if ((SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) && SystemUtils.IS_JAVA_1_6) {
      try {
        // open directory chooser
        return openDirectoryDialog(title);
      }
      catch (Exception e) {
        LOGGER.warn("cannot open AWT directory chooser" + e.getMessage());
      }
      catch (Error e) {
        LOGGER.warn("cannot open AWT directory chooser" + e.getMessage());
      }
      finally {
        // reset system property
        System.setProperty("apple.awt.fileDialogForDirectories", "false");
      }
    }

    // fallback to JFileChooser
    return openJFileChooser(JFileChooser.DIRECTORIES_ONLY, title);

  }

  private static File openJFileChooser(int mode, String dialogTitle) {
    if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
      // in OSX set the Quaqua laf
      LookAndFeel old = UIManager.getLookAndFeel();
      try {
        Set includes = new HashSet();
        includes.add("ColorChooser");
        includes.add("FileChooser");
        includes.add("Component");
        includes.add("Browser");
        includes.add("Tree");
        includes.add("SplitPane");
        // QuaquaManager.setIncludedUIs(includes);
        // call via reflection to get rid of a direct dependency
        Class<?> c = Class.forName("ch.randelshofer.quaqua.QuaquaManager");
        Method method = c.getDeclaredMethod("setIncludedUIs", Set.class);
        method.invoke(null, includes);
        UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
        JNativeFileChooser fileChooser = new JNativeFileChooser();
        if (lastDir != null) {
          fileChooser.setCurrentDirectory(lastDir);
        }
        fileChooser.setFileSelectionMode(mode);
        fileChooser.setDialogTitle(dialogTitle);

        int result = fileChooser.showOpenDialog(MainWindow.getFrame());

        if (old != null) {
          try {
            UIManager.setLookAndFeel(old);
          }
          catch (Exception ignored) {
          } // shouldn't get here
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
        else {
          return null;
        }
      }
      catch (Throwable ex) {
      }
    }

    // fallback
    JNativeFileChooser fileChooser = new JNativeFileChooser();
    fileChooser.setFileSelectionMode(mode);
    if (lastDir != null) {
      fileChooser.setCurrentDirectory(lastDir);
    }
    fileChooser.setDialogTitle(dialogTitle);

    int result = fileChooser.showOpenDialog(MainWindow.getFrame());

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

  private static File openDirectoryChooser(String title) throws Exception, Error {
    if (swt != null) {
      Window window = SwingUtilities.getWindowAncestor(MainWindow.getFrame().getGlassPane());
      JDirectoryDialog directoryDialog = new JDirectoryDialog();
      directoryDialog.setTitle(title);
      if (lastDir != null) {
        directoryDialog.setSelectedDirectory(lastDir.getAbsolutePath());
      }
      directoryDialog.show(window);

      if (StringUtils.isNotEmpty(directoryDialog.getSelectedDirectory())) {
        lastDir = new File(directoryDialog.getSelectedDirectory());
        return new File(directoryDialog.getSelectedDirectory());
      }
      else {
        return null;
      }
    }
    throw new Exception("native DirectoryChooser not found");
  }

  private static File openDirectoryDialog(String title) throws Exception, Error {
    if (!SystemUtils.IS_OS_MAC && !SystemUtils.IS_OS_MAC_OSX) {
      throw new Exception("not on osx");
    }

    // set system property to choose directories
    System.setProperty("apple.awt.fileDialogForDirectories", "true");

    FileDialog chooser = new FileDialog(MainWindow.getFrame(), title);
    if (lastDir != null) {
      chooser.setDirectory(lastDir.getAbsolutePath());
    }
    chooser.setVisible(true);

    // reset system property
    System.setProperty("apple.awt.fileDialogForDirectories", "false");

    if (StringUtils.isNotEmpty(chooser.getFile())) {
      lastDir = new File(chooser.getDirectory());
      return new File(chooser.getDirectory() + File.separator + chooser.getFile());
    }
    else {
      return null;
    }
  }

  public static File selectFile(String title) {
    if (swt != null) {
      // try to instantiate the native GTK filechooser
      try {
        return openFileChooser(title);
      }
      catch (Exception e) {
        LOGGER.warn("cannot open filechooser" + e.getMessage());
      }
      catch (UnsatisfiedLinkError e) {
        LOGGER.warn("cannot find native" + e.getMessage());
      }
    }

    // try to open AWT dialog on OSX
    if ((SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) && SystemUtils.IS_JAVA_1_6) {
      try {
        // open file chooser
        return openFileDialog(title);
      }
      catch (Exception e) {
        LOGGER.warn("cannot open AWT filechooser" + e.getMessage());
      }
      catch (Error e) {
        LOGGER.warn("cannot open AWT filechooser" + e.getMessage());
      }
    }

    // fallback to JFileChooser

    return openJFileChooser(JFileChooser.FILES_ONLY, title);

  }

  private static File openFileChooser(String title) throws Exception, Error {
    if (swt != null) {
      Window window = SwingUtilities.getWindowAncestor(MainWindow.getFrame().getGlassPane());
      JFileDialog fileDialog = new JFileDialog();
      fileDialog.setTitle(title);
      if (lastDir != null) {
        fileDialog.setParentDirectory(lastDir.getAbsolutePath());
      }
      fileDialog.show(window);
      if (StringUtils.isNotEmpty(fileDialog.getSelectedFileName())) {
        lastDir = new File(fileDialog.getParentDirectory());
        return new File(fileDialog.getParentDirectory() + File.separator + fileDialog.getSelectedFileName());
      }
      else {
        return null;
      }
    }
    throw new Exception("native FileChooser not found");
  }

  private static File openFileDialog(String title) throws Exception, Error {
    FileDialog chooser = new FileDialog(MainWindow.getFrame(), title);
    if (lastDir != null) {
      chooser.setDirectory(lastDir.getAbsolutePath());
    }
    chooser.setVisible(true);

    if (StringUtils.isNotEmpty(chooser.getFile())) {
      lastDir = new File(chooser.getDirectory());
      return new File(chooser.getDirectory() + File.separator + chooser.getFile());
    }
    else {
      return null;
    }
  }

  public static void openFile(File file) throws Exception {
    String fileType = "." + FilenameUtils.getExtension(file.getName());
    if (StringUtils.isNotBlank(Globals.settings.getMediaPlayer()) && Globals.settings.getAllSupportedFileTypes().contains(fileType)) {
      if (SystemUtils.IS_OS_MAC_OSX) {
        Runtime.getRuntime().exec(new String[] { "open", Globals.settings.getMediaPlayer(), file.getAbsolutePath() });
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
      throw new NotSupportedException();
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
      throw new NotSupportedException();
    }
  }
}