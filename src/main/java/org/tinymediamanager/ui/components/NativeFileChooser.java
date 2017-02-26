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
package org.tinymediamanager.ui.components;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

/**
 * This class is used to build an abstraction for the JFileChooser to use a native component whenever possible (JavaFX File Dialog if available)
 *
 * @author Manuel Laggner
 */
public class NativeFileChooser extends JFileChooser {
  private static final long   serialVersionUID = 1713280883666050112L;
  public static final boolean JAVAFX_AVAILABLE = isJavaFXAvailable();
  private JFXFileChooser      fileChooser;
  private JFXDirectoryChooser directoryChooser;
  private List<File>          currentFiles;
  private File                currentFile;

  private static boolean isJavaFXAvailable() {
    try {
      Class.forName("javafx.stage.FileChooser");
      // set an option to do not close JFX environment too early
      Class<?> clazz = Class.forName("javafx.application.Platform");
      Method method = clazz.getMethod("setImplicitExit", boolean.class);
      method.invoke(null, Boolean.FALSE);
      // no exception till here; initialize the JavaFX environment
      Class.forName("javafx.embed.swing.JFXPanel").newInstance();
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }

  public NativeFileChooser() {
    initFxFileChooser(null);
  }

  public NativeFileChooser(String currentDirectoryPath) {
    super(currentDirectoryPath);
    initFxFileChooser(new File(currentDirectoryPath));
  }

  public NativeFileChooser(File currentDirectory) {
    super(currentDirectory);
    initFxFileChooser(currentDirectory);
  }

  public NativeFileChooser(FileSystemView fsv) {
    super(fsv);
    initFxFileChooser(fsv.getDefaultDirectory());
  }

  public NativeFileChooser(File currentDirectory, FileSystemView fsv) {
    super(currentDirectory, fsv);
    initFxFileChooser(currentDirectory);
  }

  public NativeFileChooser(String currentDirectoryPath, FileSystemView fsv) {
    super(currentDirectoryPath, fsv);
    initFxFileChooser(new File(currentDirectoryPath));
  }

  @Override
  public int showOpenDialog(Component parent) throws HeadlessException {
    // try to invoke the File/DirectoryDialog of JavaFX
    if (JAVAFX_AVAILABLE) {
      try {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Runnable runnable = new Runnable() {
          @Override
          public void run() {
            if (isDirectorySelectionEnabled()) {
              currentFile = directoryChooser.showDialog();
            }
            else {
              if (isMultiSelectionEnabled()) {
                currentFiles = fileChooser.showOpenMultipleDialog();
              }
              else {
                currentFile = fileChooser.showOpenDialog();
              }
            }
            countDownLatch.countDown();
          }
        };

        Class<?> clazz = Class.forName("javafx.application.Platform");
        Method method = clazz.getMethod("runLater", Runnable.class);
        method.invoke(null, runnable);
        countDownLatch.await();

        if (isMultiSelectionEnabled()) {
          if (currentFiles != null) {
            return JFileChooser.APPROVE_OPTION;
          }
          else {
            return JFileChooser.CANCEL_OPTION;
          }
        }
        else {
          if (currentFile != null) {
            return JFileChooser.APPROVE_OPTION;
          }
          else {
            return JFileChooser.CANCEL_OPTION;
          }
        }
      }
      catch (Exception e) {
      }
    }

    // we're still here - show the JFileChooser as fallback
    return super.showOpenDialog(parent);
  }

  @Override
  public int showSaveDialog(Component parent) throws HeadlessException {
    if (!JAVAFX_AVAILABLE) {
      return super.showSaveDialog(parent);
    }

    final CountDownLatch countDownLatch = new CountDownLatch(1);
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        // parent.setEnabled(false);
        if (isDirectorySelectionEnabled()) {
          currentFile = directoryChooser.showDialog();
        }
        else {
          currentFile = fileChooser.showSaveDialog();
        }
        countDownLatch.countDown();
        // parent.setEnabled(true);
      }

    };

    try {
      Class<?> clazz = Class.forName("javafx.application.Platform");
      Method method = clazz.getMethod("runLater", Runnable.class);
      method.invoke(null, runnable);
      countDownLatch.await();
    }
    catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }
    catch (Exception e) {
      return JFileChooser.CANCEL_OPTION;
    }

    if (currentFile != null) {
      return JFileChooser.APPROVE_OPTION;
    }
    else {
      return JFileChooser.CANCEL_OPTION;
    }
  }

  @Override
  public int showDialog(Component parent, String approveButtonText) throws HeadlessException {
    if (!JAVAFX_AVAILABLE) {
      return super.showDialog(parent, approveButtonText);
    }
    return showOpenDialog(parent);
  }

  @Override
  public File[] getSelectedFiles() {
    if (!JAVAFX_AVAILABLE) {
      return super.getSelectedFiles();
    }
    if (currentFiles == null) {
      return null;
    }
    return currentFiles.toArray(new File[currentFiles.size()]);
  }

  @Override
  public File getSelectedFile() {
    if (!JAVAFX_AVAILABLE) {
      return super.getSelectedFile();
    }
    return currentFile;
  }

  @Override
  public void setSelectedFiles(File[] selectedFiles) {
    if (!JAVAFX_AVAILABLE) {
      super.setSelectedFiles(selectedFiles);
      return;
    }
    if (selectedFiles == null || selectedFiles.length == 0) {
      currentFiles = null;
    }
    else {
      setSelectedFile(selectedFiles[0]);
      currentFiles = new ArrayList<>(Arrays.asList(selectedFiles));
    }
  }

  @Override
  public void setSelectedFile(File file) {
    if (!JAVAFX_AVAILABLE) {
      super.setSelectedFile(file);
      return;
    }
    currentFile = file;
    if (file != null) {
      if (file.isDirectory()) {
        fileChooser.setInitialDirectory(file.getAbsoluteFile());

        if (directoryChooser != null) {
          directoryChooser.setInitialDirectory(file.getAbsoluteFile());
        }
      }
      else if (file.isFile()) {
        fileChooser.setInitialDirectory(file.getParentFile());
        fileChooser.setInitialFileName(file.getName());

        if (directoryChooser != null) {
          directoryChooser.setInitialDirectory(file.getParentFile());
        }
      }
      else {
        // okay, no dir and no file - just get the filename out of it
        if (directoryChooser == null) {
          fileChooser.setInitialFileName(file.getName());
        }
      }
    }
  }

  @Override
  public void setFileSelectionMode(int mode) {
    super.setFileSelectionMode(mode);
    if (!JAVAFX_AVAILABLE) {
      return;
    }
    if (mode == DIRECTORIES_ONLY) {
      if (directoryChooser == null) {
        try {
          directoryChooser = new JFXDirectoryChooser();
        }
        catch (Exception e) {
          return;
        }
      }
      setSelectedFile(currentFile); // Set file again, so directory chooser will be affected by it
      setDialogTitle(getDialogTitle());
    }
  }

  @Override
  public void setDialogTitle(String dialogTitle) {
    if (!JAVAFX_AVAILABLE) {
      super.setDialogTitle(dialogTitle);
      return;
    }
    fileChooser.setTitle(dialogTitle);
    if (directoryChooser != null) {
      directoryChooser.setTitle(dialogTitle);
    }
  }

  @Override
  public String getDialogTitle() {
    if (!JAVAFX_AVAILABLE) {
      return super.getDialogTitle();
    }
    return fileChooser.getTitle();
  }

  @Override
  public void changeToParentDirectory() {
    if (!JAVAFX_AVAILABLE) {
      super.changeToParentDirectory();
      return;
    }
    File parentDir = fileChooser.getInitialDirectory().getParentFile();
    if (parentDir.isDirectory()) {
      fileChooser.setInitialDirectory(parentDir);
      if (directoryChooser != null) {
        directoryChooser.setInitialDirectory(parentDir);
      }
    }
  }

  @Override
  public void addChoosableFileFilter(FileFilter filter) {
    super.addChoosableFileFilter(filter);
    if (!JAVAFX_AVAILABLE || filter == null) {
      return;
    }
    if (filter.getClass().equals(FileNameExtensionFilter.class)) {
      FileNameExtensionFilter f = (FileNameExtensionFilter) filter;

      List<String> ext = new ArrayList<>();
      for (String extension : f.getExtensions()) {
        ext.add(extension.replaceAll("^\\*?\\.?(.*)$", "*.$1"));
      }
      fileChooser.addExtensionFilter(f.getDescription(), ext);
    }
  }

  @Override
  public void setAcceptAllFileFilterUsed(boolean bool) {
    boolean differs = isAcceptAllFileFilterUsed() ^ bool;
    super.setAcceptAllFileFilterUsed(bool);
    if (!JAVAFX_AVAILABLE) {
      return;
    }
    if (differs) {
      if (bool) {
        fileChooser.addExtensionFilter("All files", Arrays.asList("*.*"));
      }
      else {
        // ToDo
        // for (Iterator<FileChooser.ExtensionFilter> it = fileChooser.getExtensionFilters().iterator(); it.hasNext();) {
        // FileChooser.ExtensionFilter filter = it.next();
        // if (filter.getExtensions().contains("*.*")) {
        // it.remove();
        // }
        // }
      }
    }
  }

  @Override
  public void setCurrentDirectory(File dir) {
    if (fileChooser != null) {
      fileChooser.setInitialDirectory(dir);
    }
    else if (directoryChooser != null) {
      directoryChooser.setInitialDirectory(dir);
    }
    else {
      super.setCurrentDirectory(dir);
    }
  };

  private void initFxFileChooser(File currentFile) {
    if (JAVAFX_AVAILABLE) {
      try {
        fileChooser = new JFXFileChooser();
        this.currentFile = currentFile;
        if (currentFile != null) {
          setSelectedFile(currentFile);
        }
      }
      catch (Exception ignored) {
      }
    }
  }
}
