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
package org.tinymediamanager.ui;

import java.awt.Desktop;
import java.awt.FileDialog;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.nfd.NativeFileDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.dialogs.ImagePreviewDialog;

/**
 * The Class TmmUIHelper.
 * 
 * @author Manuel Laggner
 */
public class TmmUIHelper {
  private static final Logger           LOGGER = LoggerFactory.getLogger(TmmUIHelper.class);
  protected static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

  private TmmUIHelper() {
    // private constructor for utility classes
  }

  public static Path selectDirectory(String title, String initialPath) {
    // on macOS/OSX we simply use the AWT FileDialog
    if (SystemUtils.IS_OS_MAC) {
      try {
        // open directory chooser
        return openDirectoryDialog(title, initialPath);
      }
      catch (Exception | Error e) {
        LOGGER.warn("cannot open AWT directory chooser: {}", e.getMessage());
      }
      finally {
        // reset system property
        System.setProperty("apple.awt.fileDialogForDirectories", "false");
      }
    }
    else {
      // try to open with NFD
      try {

        PointerBuffer outPath = MemoryUtil.memAllocPointer(1);

        // check if the initialPath is accessible
        if (StringUtils.isBlank(initialPath) || !Files.exists(Paths.get(initialPath))) {
          initialPath = System.getProperty("user.home");
        }

        try {
          int result = NativeFileDialog.NFD_PickFolder(initialPath, outPath);
          if (result == NativeFileDialog.NFD_OKAY) {
            Path path = Paths.get(outPath.getStringUTF8());
            NativeFileDialog.nNFD_Free(outPath.get(0));
            return path;
          }
          else if (result == NativeFileDialog.NFD_CANCEL) {
            return null;
          }
          else {
            LOGGER.warn("NFD result was ERROR for path {}; trying JFileChooser", initialPath);
          }
        }
        finally {
          MemoryUtil.memFree(outPath);
        }

      }
      catch (Exception | Error e) {
        LOGGER.error("could not call nfd - {}", e.getMessage());
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
        fileChooser = new JFileChooser(path.toFile());
      }
    }

    if (fileChooser == null) {
      fileChooser = new JFileChooser();
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

  public static Path selectFile(String title, String initialPath, FileNameExtensionFilter filter) {
    // on macOS/OSX we simply use the AWT FileDialog
    if (SystemUtils.IS_OS_MAC) {
      try {
        // open file chooser
        return openFileDialog(title, initialPath, FileDialog.LOAD, null);
      }
      catch (Exception | Error e) {
        LOGGER.warn("cannot open AWT filechooser: {}", e.getMessage());
      }
    }
    else {
      // try to open with NFD
      try {
        PointerBuffer outPath = MemoryUtil.memAllocPointer(1);

        // check if the initialPath is accessible
        if (StringUtils.isBlank(initialPath) || !Files.exists(Paths.get(initialPath))) {
          initialPath = System.getProperty("user.home");
        }

        try {
          String filterList = null;
          if (filter != null) {
            filterList = String.join(",", filter.getExtensions());
            filterList = filterList.replaceAll("\\.", "");
          }
          int result = NativeFileDialog.NFD_OpenDialog(filterList, initialPath, outPath);
          if (result == NativeFileDialog.NFD_OKAY) {
            Path path = Paths.get(outPath.getStringUTF8());
            NativeFileDialog.nNFD_Free(outPath.get(0));
            return path;
          }
          else {
            return null;
          }
        }
        finally {
          MemoryUtil.memFree(outPath);
        }

      }
      catch (Exception | Error e) {
        LOGGER.error("could not call nfd - {}", e.getMessage());
      }
    }

    // open JFileChooser
    return openJFileChooser(JFileChooser.FILES_ONLY, title, initialPath, true, null, filter);
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
    // on macOS/OSX we simply use the AWT FileDialog
    if (SystemUtils.IS_OS_MAC) {
      try {
        // open file chooser
        return openFileDialog(title, initialPath, FileDialog.SAVE, filename);
      }
      catch (Exception | Error e) {
        LOGGER.warn("cannot open AWT filechooser: {}", e.getMessage());
      }
    }
    else {
      // try to open with NFD
      try {
        PointerBuffer outPath = MemoryUtil.memAllocPointer(1);

        // check if the initialPath is accessible
        if (StringUtils.isBlank(initialPath) || !Files.exists(Paths.get(initialPath))) {
          initialPath = System.getProperty("user.home");
        }

        try {
          String filterList = null;
          if (filter != null) {
            filterList = String.join(",", filter.getExtensions());
            filterList = filterList.replaceAll("\\.", "");
          }
          int result = NativeFileDialog.NFD_SaveDialog(filterList, initialPath, outPath);
          if (result == NativeFileDialog.NFD_OKAY) {
            Path path = Paths.get(outPath.getStringUTF8());
            NativeFileDialog.nNFD_Free(outPath.get(0));
            return path;
          }
          else {
            return null;
          }
        }
        finally {
          MemoryUtil.memFree(outPath);
        }

      }
      catch (Exception | Error e) {
        LOGGER.error("could not call nfd - {}", e.getMessage());
      }
    }

    return openJFileChooser(JFileChooser.FILES_ONLY, title, initialPath, false, filename, filter);
  }

  public static void openFile(Path file) throws Exception {
    String fileType = "." + FilenameUtils.getExtension(file.getFileName().toString().toLowerCase(Locale.ROOT));
    String abs = file.toAbsolutePath().toString();

    if (StringUtils.isNotBlank(Globals.settings.getMediaPlayer()) && Globals.settings.getAllSupportedFileTypes().contains(fileType)) {
      if (SystemUtils.IS_OS_MAC) {
        exec(new String[] { "open", Globals.settings.getMediaPlayer(), "--args", abs });
      }
      else {
        exec(new String[] { Globals.settings.getMediaPlayer(), abs });
      }
    }
    else if (SystemUtils.IS_OS_WINDOWS) {
      // try to open directly

      try {
        Desktop.getDesktop().open(file.toFile());
      }
      catch (Exception e) {
        // use explorer directly - ship around access exceptions and the unresolved network bug
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6780505
        exec(new String[] { "explorer", abs });
      }
    }
    else if (SystemUtils.IS_OS_LINUX) {
      // try all different starters
      boolean started = false;
      try {
        exec(new String[] { "xdg-open", abs });
        started = true;
      }
      catch (IOException ignored) {
        // nothing to do here
      }

      if (!started) {
        try {
          exec(new String[] { "kde-open", abs });
          started = true;
        }
        catch (IOException ignored) {
          // nothing to do here
        }
      }

      if (!started) {
        try {
          exec(new String[] { "gnome-open", abs });
          started = true;
        }
        catch (IOException ignored) {
          // nothing to do here
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
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      Desktop.getDesktop().browse(new URI(url));
    }
    else if (SystemUtils.IS_OS_LINUX) {
      // try all different starters
      boolean started = false;
      try {
        exec(new String[] { "gnome-open", url });
        started = true;
      }
      catch (IOException ignored) {
      }

      if (!started) {
        try {
          exec(new String[] { "kde-open", url });
          started = true;
        }
        catch (IOException ignored) {
        }
      }

      if (!started) {
        try {
          exec(new String[] { "kde-open5", url });
          started = true;
        }
        catch (IOException ignored) {
        }
      }

      if (!started) {
        try {
          exec(new String[] { "xdg-open", url });
          started = true;
        }
        catch (IOException ignored) {
        }
      }
    }
    else {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * get the column width for a column containing the given icon (icon width + 10%)
   *
   * @param icon
   *          the given icon
   * @return the desired column width
   */
  public static int getColumnWidthForIcon(ImageIcon icon) {
    if (icon == null) {
      return 0;
    }
    return (int) (icon.getIconWidth() * 1.1);
  }

  /**
   * Executes a command line and discards the stdout and stderr of the spawned process.
   *
   * @param cmdline
   *          the command including all parameters
   * @throws IOException
   * @see {@link Runtime#exec(String[])}
   */
  private static void exec(String[] cmdline) throws IOException {
    Process p = Runtime.getRuntime().exec(cmdline);

    // The purpose of the following to threads is to read stdout and stderr from the processes, which are spawned in this class.
    // On some platforms (for sure on Linux) the process might block otherwise, because the internal buffers fill up.
    // MPV for example is quite verbose and blocks up after about 1 min.
    StreamRedirectThread stdoutReader = new StreamRedirectThread(p.getInputStream(), new NirvanaOutputStream());
    StreamRedirectThread stderrReader = new StreamRedirectThread(p.getErrorStream(), new NirvanaOutputStream());
    new Thread(stdoutReader).start();
    new Thread(stderrReader).start();
  }

  /**
   * This OutputStream discards all bytes written to it.
   */
  private static class NirvanaOutputStream extends OutputStream {
    @Override
    public void write(int b) throws IOException {
    }
  }

  /**
   * Reads from an InputStream and writes the contents directly to an OutputStream
   */
  private static class StreamRedirectThread implements Runnable {
    private InputStream  in;
    private OutputStream out;

    public StreamRedirectThread(InputStream in, OutputStream out) {
      super();
      this.in = in;
      this.out = out;
    }

    @Override
    public void run() {
      try {
        int length = -1;
        byte[] buffer = new byte[1024 * 1024];
        while (in != null && (length = in.read(buffer)) >= 0) {
          out.write(buffer, 0, length);
        }
      }
      catch (Exception e) {
        LOGGER.error("Couldn't redirect stream: {}", e.getLocalizedMessage());
      }
    }
  }

  public static LinkLabel createLinkForImage(LinkLabel linklabel, ImageLabel image) {
    linklabel.addActionListener(e -> {
      if (StringUtils.isNotBlank(image.getImagePath())) {
        ImagePreviewDialog dialog = new ImagePreviewDialog(Paths.get(image.getImagePath()));
        dialog.setVisible(true);
      }
      else {
        ImagePreviewDialog dialog = new ImagePreviewDialog(image.getImageUrl());
        dialog.setVisible(true);
      }
    });

    return linklabel;
  }
}
