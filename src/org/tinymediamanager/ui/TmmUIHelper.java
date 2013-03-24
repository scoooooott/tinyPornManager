/*
 * Copyright 2012 Manuel Laggner
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

import java.awt.FileDialog;
import java.awt.Window;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import chrriis.dj.nativeswing.swtimpl.components.JDirectoryDialog;
import chrriis.dj.nativeswing.swtimpl.components.JFileDialog;

/**
 * @author Manuel Laggner
 * 
 */
public class TmmUIHelper {

  @SuppressWarnings("rawtypes")
  public static Class swt;

  public static void init() {
    try {
      swt = ClassLoader.getSystemClassLoader().loadClass("org.eclipse.swt.widgets.FileDialog");
    }
    catch (Exception e) {
    }
    catch (Error e) {
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
      }
      catch (Error e) {
      }
    }

    // on mac try to take the AWT FileDialog
    if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
      try {
        // open directory chooser
        return openDirectoryDialog(title);
      }
      catch (Exception e) {
      }
      catch (Error e) {
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
   * Select a file or directory using the JFileChooser
   * 
   * @param mode
   *          the mode
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
        e.printStackTrace();
      }
      catch (UnsatisfiedLinkError e) {
      }
    }

    // try to open AWT dialog on OSX
    if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
      try {
        // open file chooser
        return openFileDialog(title);
      }
      catch (Exception e) {
      }
      catch (Error e) {
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
}
