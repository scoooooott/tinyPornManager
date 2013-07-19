/*
 * Copyright 2012 - 2013 Manuel Laggner
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
import java.net.URI;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.transaction.NotSupportedException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ui.components.JNativeFileChooser;

import chrriis.dj.nativeswing.swtimpl.components.JDirectoryDialog;
import chrriis.dj.nativeswing.swtimpl.components.JFileDialog;

/**
 * The Class TmmUIHelper.
 * 
 * @author Manuel Laggner
 */
public class TmmUIHelper {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(TmmUIHelper.class);

  /** The swt. */
  @SuppressWarnings("rawtypes")
  public static Class         swt    = null;

  /**
   * Inits the.
   */
  public static void init() {
    try {
      if (SystemUtils.IS_OS_LINUX) {
        swt = ClassLoader.getSystemClassLoader().loadClass("org.eclipse.swt.widgets.FileDialog");
      }
    }
    catch (Exception e) {
      LOGGER.warn("cannot open init filedialog" + e.getMessage());
    }
    catch (Error e) {
      LOGGER.warn("cannot open init filedialog" + e.getMessage());
    }
  }

  /**
   * Select a directory.
   * 
   * @param title
   *          the title
   * @return the file
   */
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
    if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
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

  /**
   * Select a file or directory using the JFileChooser.
   * 
   * @param mode
   *          the mode
   * @param dialogTitle
   *          the dialog title
   * @return the file
   */
  private static File openJFileChooser(int mode, String dialogTitle) {
    JNativeFileChooser fileChooser = new JNativeFileChooser();
    fileChooser.setFileSelectionMode(mode);
    fileChooser.setDialogTitle(dialogTitle);

    int result = fileChooser.showOpenDialog(MainWindow.getFrame());

    if (result == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile();
    }

    return null;
  }

  /**
   * Opens the native directory chooser.
   * 
   * @param title
   *          the title
   * @return the file
   * @throws Exception
   *           the exception
   * @throws Error
   *           the error
   */
  private static File openDirectoryChooser(String title) throws Exception, Error {
    if (swt != null) {
      Window window = SwingUtilities.getWindowAncestor(MainWindow.getFrame().getGlassPane());
      JDirectoryDialog directoryDialog = new JDirectoryDialog();
      directoryDialog.setTitle(title);
      directoryDialog.show(window);

      if (StringUtils.isNotEmpty(directoryDialog.getSelectedDirectory())) {
        return new File(directoryDialog.getSelectedDirectory());
      }
      else {
        return null;
      }
    }
    throw new Exception("native DirectoryChooser not found");
  }

  /**
   * Open AWT directory dialog (on osx).
   * 
   * @param title
   *          the title
   * @return the file
   * @throws Exception
   *           the exception
   * @throws Error
   *           the error
   */
  private static File openDirectoryDialog(String title) throws Exception, Error {
    if (!SystemUtils.IS_OS_MAC && !SystemUtils.IS_OS_MAC_OSX) {
      throw new Exception("not on osx");
    }

    // set system property to choose directories
    System.setProperty("apple.awt.fileDialogForDirectories", "true");

    FileDialog chooser = new FileDialog(MainWindow.getFrame(), title);
    chooser.setVisible(true);

    // reset system property
    System.setProperty("apple.awt.fileDialogForDirectories", "false");

    if (StringUtils.isNotEmpty(chooser.getFile())) {
      return new File(chooser.getDirectory() + File.separator + chooser.getFile());
    }
    else {
      return null;
    }
  }

  /**
   * Select file.
   * 
   * @param title
   *          the title
   * @return the file
   */
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
    if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
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

  /**
   * Open the swt file chooser.
   * 
   * @param title
   *          the title
   * @return the file
   * @throws Exception
   *           the exception
   * @throws Error
   *           the error
   */
  private static File openFileChooser(String title) throws Exception, Error {
    if (swt != null) {
      Window window = SwingUtilities.getWindowAncestor(MainWindow.getFrame().getGlassPane());
      JFileDialog fileDialog = new JFileDialog();
      fileDialog.setTitle(title);
      fileDialog.show(window);
      if (StringUtils.isNotEmpty(fileDialog.getSelectedFileName())) {
        return new File(fileDialog.getParentDirectory() + File.separator + fileDialog.getSelectedFileName());
      }
      else {
        return null;
      }
    }
    throw new Exception("native FileChooser not found");
  }

  /**
   * Open the AWT file dialog.
   * 
   * @param title
   *          the title
   * @return the file
   * @throws Exception
   *           the exception
   * @throws Error
   *           the error
   */
  private static File openFileDialog(String title) throws Exception, Error {
    FileDialog chooser = new FileDialog(MainWindow.getFrame(), title);
    chooser.setVisible(true);

    if (StringUtils.isNotEmpty(chooser.getFile())) {
      return new File(chooser.getDirectory() + File.separator + chooser.getFile());
    }
    else {
      return null;
    }
  }

  /**
   * Open the file with the systems default application
   * 
   * @param file
   * @throws Exception
   */
  public static void openFile(File file) throws Exception {
    if (SystemUtils.IS_OS_WINDOWS) {
      // use explorer directly - ship around access exceptions and the unresolved network bug
      // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6780505
      Runtime.getRuntime().exec(new String[] { "explorer", file.getAbsolutePath() });
    }
    else if (Desktop.isDesktopSupported()) {
      Desktop.getDesktop().open(file);
    }
    else if (SystemUtils.IS_OS_LINUX) {
      Runtime.getRuntime().exec(new String[] { "xdg-open", file.getAbsolutePath() });
    }
    else {
      throw new NotSupportedException();
    }
  }

  /**
   * Browse to the given url using the systems default browser
   * 
   * @param url
   * @throws Exception
   */
  public static void browseUrl(String url) throws Exception {
    if (Desktop.isDesktopSupported()) {
      Desktop.getDesktop().browse(new URI(url));
    }
    else if (SystemUtils.IS_OS_LINUX) {
      Runtime.getRuntime().exec(new String[] { "xdg-open", url });
    }
    else {
      throw new NotSupportedException();
    }
  }
}
