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
package org.tinymediamanager.core.tvshow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.movie.Movie;

/**
 * The TvShow renamer
 * 
 * @author Myron Boyle
 */
public class TvShowRenamer {

  /** The Constant LOGGER. */
  private final static Logger LOGGER = LoggerFactory.getLogger(TvShowRenamer.class);

  /**
   * add leadingZero if only 1 char
   * 
   * @param num
   *          the number
   * @return the string with a leading 0
   */
  private static String lz(int num) {
    return String.format("%02d", num);
  }

  /**
   * replaces all the invalid filename character from a string
   * 
   * @param name
   *          the string to clean
   * @return the cleaned string
   */
  private static String cleanForFilename(String name) {
    return name.replaceAll("([\"\\:<>|/?*])", "");
  }

  /**
   * generates the filename of a TvShow MediaFile according to settings
   * 
   * @param mf
   *          MediaFile
   */
  public static String generateFilename(TvShowSeason season, MediaFile mf) {
    String filename = "";
    String s = "";
    String e = "";
    String delim = "";

    TvShowEpisodeNaming form = Globals.settings.getTvShowSettings().getRenamerFormat();
    String separator = Globals.settings.getTvShowSettings().getRenamerSeparator();

    String show = cleanForFilename(season.getTvShow().getTitle());
    if (Globals.settings.getTvShowSettings().getRenamerAddShow()) {
      filename = filename + show;
    }

    List<TvShowEpisode> eps = TvShowList.getInstance().getTvEpisodesByFile(mf.getFile());
    // generate SEE-title string appended
    for (int i = 0; i < eps.size(); i++) {
      TvShowEpisode ep = eps.get(i);

      filename = filename + separator;
      // TODO: handle upper/lower case and leadingZero or not
      switch (form) {
        case WITH_SE:
          s = "S" + lz(season.getSeason());
          e = "E" + lz(ep.getEpisode());
          break;
        case WITH_X:
          s = String.valueOf(season.getSeason());
          e = lz(ep.getEpisode());
          delim = "x";
          break;
        case NUMBER:
          s = String.valueOf(season.getSeason());
          e = lz(ep.getEpisode());
          break;
        default:
          break;
      }
      if (Globals.settings.getTvShowSettings().getRenamerAddSeason()) {
        filename = filename + s;
      }
      filename = filename + delim;
      filename = filename + e;

      if (Globals.settings.getTvShowSettings().getRenamerAddTitle()) {
        String epTitle = cleanForFilename(ep.getTitle());
        if (epTitle.matches("[0-9]+.*") && separator.equals(".")) {
          // EP title starts with a number, so "S01E01.1 Day in..." could be misleading parsed
          // as sub-episode E01.1 - override separator for that hardcoded!
          filename = filename + '_';
        }
        else {
          filename = filename + separator;
        }
        filename = filename + epTitle;
      }
    }
    if (filename.startsWith(separator)) {
      filename = filename.substring(separator.length());
    }

    return filename;
  }

  private void renameFile(TvShowSeason season, MediaFile mf) {
    String filenane = generateFilename(season, mf);
    // TODO: rename file

  }

  public void renameSeason(TvShowSeason season) {

    // TODO: rename/generate season folder

    List<MediaFile> mfs = season.getMediaFiles();
    for (MediaFile mf : mfs) {
      renameFile(season, mf);
    }
  }

  /**
   * Rename TvShow.
   * 
   * @param TvShow
   *          the TvShow
   */
  public static void renameTvShow(TvShow show) {

    // check if a datasource is set
    if (StringUtils.isEmpty(show.getDataSource())) {
      LOGGER.error("no Datasource set");
      return;
    }

    // all the good & needed mediafiles
    ArrayList<MediaFile> needed = new ArrayList<MediaFile>();
    ArrayList<MediaFile> cleanup = new ArrayList<MediaFile>();

    LOGGER.info("Renaming TvShow: " + show.getTitle());
    LOGGER.debug("TvShow year: " + show.getYear());
    LOGGER.debug("TvShow path: " + show.getPath());

  }

  /**
   * modified version of commons-io FileUtils.moveDirectory();<br>
   * since renameTo() might not work in first place, retry it up to 5 times.<br>
   * (better wait 5 sec for success, than always copying a 50gig directory ;)<br>
   * <b>And NO, we're NOT doing a copy+delete as fallback!</b>
   * 
   * @param srcDir
   *          the directory to be moved
   * @param destDir
   *          the destination directory
   * @return true, if successful
   * @throws IOException
   *           if an IO error occurs moving the file
   * @author Myron Boyle
   */
  public static boolean moveDirectorySafe(File srcDir, File destDir) throws IOException {
    // rip-off from
    // http://svn.apache.org/repos/asf/commons/proper/io/trunk/src/main/java/org/apache/commons/io/FileUtils.java
    if (srcDir == null) {
      throw new NullPointerException("Source must not be null");
    }
    if (destDir == null) {
      throw new NullPointerException("Destination must not be null");
    }
    LOGGER.debug("try to move folder " + srcDir.getPath() + " to " + destDir.getPath());
    if (!srcDir.exists()) {
      throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
    }
    if (!srcDir.isDirectory()) {
      throw new IOException("Source '" + srcDir + "' is not a directory");
    }
    if (destDir.exists()) {
      throw new FileExistsException("Destination '" + destDir + "' already exists");
    }
    if (!destDir.getParentFile().exists()) {
      // create parent folder structure, else renameTo does not work
      destDir.getParentFile().mkdirs();
    }

    // rename folder; try 5 times and wait a sec
    boolean rename = false;
    for (int i = 0; i < 5; i++) {
      rename = srcDir.renameTo(destDir);
      if (rename) {
        break; // ok it worked, step out
      }
      try {
        LOGGER.debug("rename did not work - sleep a while and try again...");
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {
        LOGGER.warn("I'm so excited - could not sleep");
      }
    }

    // ok, we tried it 5 times - it still seems to be locked somehow. Continue
    // with copying as fallback
    // NOOO - we don't like to have some files copied and some not.

    if (!rename) {
      LOGGER.error("Failed to rename directory '" + srcDir + " to " + destDir.getPath());
      LOGGER.error("Tv show renaming aborted.");
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, srcDir.getPath(), "message.renamer.failedrename"));
      return false;
    }
    else {
      LOGGER.info("Successfully moved folder " + srcDir.getPath() + " to " + destDir.getPath());
      return true;
    }
  }

  /**
   * Creates the new file/folder name according to template string
   * 
   * @param template
   *          the template
   * @param movie
   *          the movie
   * @return the string
   */
  protected static String createDestination(String template, Movie movie) {
    String newDestination = template;

    // replace token title ($T)
    if (newDestination.contains("$T")) {
      newDestination = newDestination.replaceAll("\\$T", movie.getTitle());
    }

    // replace token first letter of title ($1)
    if (newDestination.contains("$1")) {
      newDestination = newDestination.replaceAll("\\$1", movie.getTitle().substring(0, 1));
    }

    // replace token year ($Y)
    if (newDestination.contains("$Y")) {
      newDestination = newDestination.replaceAll("\\$Y", movie.getYear());
    }

    // replace token orignal title ($O)
    if (newDestination.contains("$O")) {
      newDestination = newDestination.replaceAll("\\$O", movie.getOriginalTitle());
    }

    // replace token IMDBid ($I)
    if (newDestination.contains("$I")) {
      newDestination = newDestination.replaceAll("\\$I", movie.getImdbId());
    }

    // replace token sort title ($E)
    if (newDestination.contains("$E")) {
      newDestination = newDestination.replaceAll("\\$E", movie.getSortTitle());
    }

    if (movie.getMediaFiles(MediaFileType.VIDEO).size() > 0) {
      MediaFile mf = movie.getMediaFiles(MediaFileType.VIDEO).get(0);
      // replace token resolution ($R)
      if (newDestination.contains("$R")) {
        newDestination = newDestination.replaceAll("\\$R", mf.getVideoResolution());
      }

      // replace token audio codec + channels ($A)
      if (newDestination.contains("$A")) {
        newDestination = newDestination.replaceAll("\\$A", mf.getAudioCodec() + (mf.getAudioCodec().isEmpty() ? "" : "-") + mf.getAudioChannels());
      }

      // replace token video codec + channels ($V)
      if (newDestination.contains("$V")) {
        newDestination = newDestination.replaceAll("\\$V", mf.getVideoCodec() + (mf.getVideoCodec().isEmpty() ? "" : "-") + mf.getVideoFormat());
      }
    }
    else {
      // no mediafiles; remove at least token (if available)
      newDestination = newDestination.replaceAll("\\$R", "");
      newDestination = newDestination.replaceAll("\\$A", "");
      newDestination = newDestination.replaceAll("\\$V", "");
    }

    // replace token media source (BluRay|DVD|TV|...) ($S)
    // if (newDestination.contains("$S")) {
    // newDestination = newDestination.replaceAll("\\$S",
    // movie.getMediaSource());
    // }

    // replace illegal characters
    // http://msdn.microsoft.com/en-us/library/windows/desktop/aa365247%28v=vs.85%29.aspx
    newDestination = newDestination.replaceAll("([\"\\:<>|/?*])", "");
    // replace empty brackets
    newDestination = newDestination.replaceAll("\\(\\)", "");

    return newDestination.trim();
  }

  /**
   * Move file.
   * 
   * @param oldFilename
   *          the old filename
   * @param newFilename
   *          the new filename
   * @throws Exception
   *           the exception
   */
  public static void moveFile(File oldFilename, File newFilename) throws Exception {
    if (!oldFilename.equals(newFilename)) {
      LOGGER.info("move file " + oldFilename + " to " + newFilename);
      if (newFilename.exists()) {
        // overwrite?
        LOGGER.warn(newFilename + " exists - do nothing.");
      }
      else {
        if (oldFilename.exists()) {
          FileUtils.moveFile(oldFilename, newFilename);
        }
        else {
          throw new FileNotFoundException(oldFilename.getAbsolutePath());
        }
      }
    }
  }

  /**
   * copies or moves file.
   * 
   * @param oldFilename
   *          the old filename
   * @param newFilename
   *          the new filename
   * @throws Exception
   *           the exception
   */
  public static boolean copyFile(File oldFilename, File newFilename) throws Exception {
    if (!oldFilename.equals(newFilename)) {
      LOGGER.info("copy file " + oldFilename + " to " + newFilename);
      if (newFilename.exists()) {
        // overwrite?
        LOGGER.warn(newFilename + " exists - do nothing.");
        return true;
      }
      else {
        if (oldFilename.exists()) {
          FileUtils.copyFile(oldFilename, newFilename, true);
          return true;
        }
        else {
          throw new FileNotFoundException(oldFilename.getAbsolutePath());
        }
      }
    }
    else { // file is the same
      return false;
    }
  }
}
