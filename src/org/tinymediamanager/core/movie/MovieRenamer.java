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
package org.tinymediamanager.core.movie;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileSubtitle;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;

/**
 * The Class MovieRenamer.
 * 
 * @author Manuel Laggner / Myron Boyle
 */
public class MovieRenamer {

  /** The Constant LOGGER. */
  private final static Logger LOGGER = LoggerFactory.getLogger(MovieRenamer.class);

  private static void renameSubtitles(Movie m) {
    // build language lists
    Set<String> langArray = Utils.KEY_TO_LOCALE_MAP.keySet();

    // the filename of movie, to remove from subtitle, to ease parsing
    String vname = Utils.cleanStackingMarkers(m.getMediaFiles(MediaFileType.VIDEO).get(0).getBasename()).toLowerCase();

    for (MediaFile sub : m.getMediaFiles(MediaFileType.SUBTITLE)) {
      String lang = "";
      String forced = "";
      String shortname = sub.getBasename().toLowerCase().replace(vname, "");
      if (sub.getFilename().toLowerCase().contains("forced")) {
        // add "forced" prior language
        forced = ".forced";
        shortname = shortname.replaceAll("forced", "");
      }
      shortname = shortname.replaceAll("\\p{Punct}", "").trim();

      for (String l : langArray) {
        if (shortname.endsWith(l.toLowerCase())) {
          lang = Utils.getDisplayLanguage(l);
          LOGGER.debug("found language '" + l + "' in subtitle; displaying it as '" + lang + "'");
          break;
        }
      }

      String newSubName = "";

      if (sub.getStacking() == 0) {
        // fine, so match to first movie file
        MediaFile mf = m.getMediaFiles(MediaFileType.VIDEO).get(0);
        newSubName = mf.getBasename() + forced;
        if (!lang.isEmpty()) {
          newSubName += "." + lang;
        }
      }
      else {
        // with stacking info; try to match
        for (MediaFile mf : m.getMediaFiles(MediaFileType.VIDEO)) {
          if (mf.getStacking() == sub.getStacking()) {
            newSubName = mf.getBasename() + forced;
            if (!lang.isEmpty()) {
              newSubName += "." + lang;
            }
          }
        }
      }
      newSubName += "." + sub.getExtension();

      File newFile = new File(m.getPath(), newSubName);
      try {
        moveFile(sub.getFile(), newFile);
        m.removeFromMediaFiles(sub);
        MediaFile mf = new MediaFile(newFile);
        MediaFileSubtitle mfs = new MediaFileSubtitle();
        if (!lang.isEmpty()) {
          mfs.setLanguage(lang);
        }
        if (!forced.isEmpty()) {
          mfs.setForced(true);
        }
        mfs.setCodec(sub.getExtension());
        mf.setContainerFormat(sub.getExtension()); // set containerformat, so mediainfo deos not overwrite our new array
        mf.addSubtitle(mfs);
        m.addToMediaFiles(mf);
      }
      catch (Exception e) {
        LOGGER.error("error moving subtitles", e);
      }
    } // end MF loop
    m.saveToDb();
  }

  /**
   * Rename movie.
   * 
   * @param movie
   *          the movie
   */
  public static void renameMovie(Movie movie) {

    // check if a datasource is set
    if (StringUtils.isEmpty(movie.getDataSource())) {
      LOGGER.error("no Datasource set");
      return;
    }

    LOGGER.info("Renaming movie: " + movie.getTitle());
    LOGGER.debug("movie year: " + movie.getYear());
    LOGGER.debug("movie path: " + movie.getPath());
    LOGGER.debug("path expression: " + Globals.settings.getMovieSettings().getMovieRenamerPathname());
    LOGGER.debug("file expression: " + Globals.settings.getMovieSettings().getMovieRenamerFilename());

    String newPathname = createDestination(Globals.settings.getMovieSettings().getMovieRenamerPathname(), movie);
    String oldPathname = movie.getPath();

    if (!newPathname.isEmpty()) {
      newPathname = movie.getDataSource() + File.separator + newPathname;

      File srcDir = new File(oldPathname);
      File destDir = new File(newPathname);
      // move directory if needed
      if (!srcDir.equals(destDir)) {
        boolean ok = false;
        try {
          // FileUtils.moveDirectory(srcDir, destDir);
          ok = moveDirectorySafe(srcDir, destDir);
          if (ok) {
            movie.updateMediaFilePath(srcDir, destDir);
            movie.setPath(newPathname);
            movie.saveToDb();
          }
        }
        catch (Exception e) {
          LOGGER.error("error moving folder: ", e);
        }
        if (!ok) {
          // FIXME: when we were not able to rename folder, display error msg
          // and abort!!!
          return;
        }
      }
    }
    else {
      LOGGER.info("Folder rename settings were empty - NOT renaming folder");
    }

    // all the good & needed mediafiles
    ArrayList<MediaFile> needed = new ArrayList<MediaFile>();
    ArrayList<MediaFile> cleanup = new ArrayList<MediaFile>();

    // if empty, do not rename file, but DO move them to movie root
    boolean renameFiles = !Globals.settings.getMovieSettings().getMovieRenamerFilename().isEmpty();

    // ######################################################################
    // ## rename NFO
    // ######################################################################
    MediaFile mf = movie.getMediaFiles(MediaFileType.NFO).get(0);
    if (mf != null) {
      cleanup.add(new MediaFile(mf)); // mark old file for cleanup (clone current)
      String newFilename = mf.getFilename();
      String newPath = movie.getPath() + File.separator;

      for (MovieNfoNaming name : Globals.settings.getMovieSettings().getMovieNfoFilenames()) {
        MediaFile newMF = new MediaFile(mf);
        newFilename = movie.getNfoFilename(name);
        File newFile = new File(newPath, newFilename);
        try {
          boolean ok = copyFile(mf.getFile(), newFile);
          if (ok) {
            // movie.setNfoFilename(newFilename); // TODO remove when work completely with MediaFiles
            newMF.setPath(newPath);
            newMF.setFilename(newFilename);
          }
        }
        catch (Exception e) {
          LOGGER.error("error renaming Nfo", e);
        }
        needed.add(newMF);
      }
    }

    // ######################################################################
    // ## rename POSTER
    // ######################################################################
    mf = movie.getMediaFiles(MediaFileType.POSTER).get(0);
    if (mf != null) {
      cleanup.add(new MediaFile(mf)); // mark old file for cleanup (clone current)
      String newFilename = mf.getFilename();
      String newPath = movie.getPath() + File.separator;

      for (MoviePosterNaming name : Globals.settings.getMovieSettings().getMoviePosterFilenames()) {
        newFilename = movie.getPosterFilename(name);
        String curExt = mf.getExtension();
        if (curExt.equalsIgnoreCase("tbn")) {
          String cont = mf.getContainerFormat();
          if (cont.equalsIgnoreCase("PNG")) {
            curExt = "png";
          }
          else if (cont.equalsIgnoreCase("JPEG")) {
            curExt = "jpg";
          }
        }
        if (!curExt.equals(FilenameUtils.getExtension(newFilename))) {
          // match extension to not rename PNG to JPG and vice versa
          continue;
        }
        MediaFile newMF = new MediaFile(mf);
        File newFile = new File(newPath, newFilename);
        try {
          boolean ok = copyFile(mf.getFile(), newFile);
          if (ok) {
            newMF.setPath(newPath);
            newMF.setFilename(newFilename);
          }
        }
        catch (Exception e) {
          LOGGER.error("error renaming poster", e);
        }
        needed.add(newMF);
      }
    }

    // ######################################################################
    // ## rename FANART
    // ######################################################################
    mf = movie.getMediaFiles(MediaFileType.FANART).get(0);
    if (mf != null) {
      cleanup.add(new MediaFile(mf)); // mark old file for cleanup (clone current)
      String newFilename = mf.getFilename();
      String newPath = movie.getPath() + File.separator;

      for (MovieFanartNaming name : Globals.settings.getMovieSettings().getMovieFanartFilenames()) {
        newFilename = movie.getFanartFilename(name);
        String curExt = mf.getExtension();
        if (curExt.equalsIgnoreCase("tbn")) {
          String cont = mf.getContainerFormat();
          if (cont.equalsIgnoreCase("PNG")) {
            curExt = "png";
          }
          else if (cont.equalsIgnoreCase("JPEG")) {
            curExt = "jpg";
          }
        }
        if (!mf.getExtension().equals(FilenameUtils.getExtension(newFilename))) {
          // match extension to not rename PNG to JPG and vice versa
          continue;
        }
        MediaFile newMF = new MediaFile(mf);
        File newFile = new File(newPath, newFilename);
        try {
          boolean ok = copyFile(mf.getFile(), newFile);
          if (ok) {
            newMF.setPath(newPath);
            newMF.setFilename(newFilename);
          }
        }
        catch (Exception e) {
          LOGGER.error("error renaming fanart", e);
        }
        needed.add(newMF);
      }
    }

    // ######################################################################
    // ## rename TRAILER
    // ######################################################################
    mf = movie.getMediaFiles(MediaFileType.TRAILER).get(0);
    if (mf != null) {
      cleanup.add(new MediaFile(mf)); // mark old file for cleanup (clone current)
      String newFilename = mf.getFilename();
      String newPath = movie.getPath() + File.separator;
      String fileExtension = FilenameUtils.getExtension(mf.getFilename());
      newFilename = createDestination(Globals.settings.getMovieSettings().getMovieRenamerFilename(), movie) + "-trailer." + fileExtension;
      MediaFile newMF = new MediaFile(mf);
      File newFile = new File(newPath, newFilename);
      try {
        moveFile(mf.getFile(), newFile);
        newMF.setPath(newPath);
        newMF.setFilename(newFilename);
      }
      catch (Exception e) {
        LOGGER.error("error renaming trailer", e);
      }
      needed.add(newMF);
    }

    // ######################################################################
    // ## rename SUBTITLE
    // ######################################################################
    for (MediaFile sub : movie.getMediaFiles(MediaFileType.SUBTITLE)) {
      needed.add(sub); // keep all subtitles, will be cleaned afterwards
    }

    // ######################################################################
    // ## rename UNKNOWN
    // ######################################################################
    for (MediaFile unk : movie.getMediaFiles(MediaFileType.UNKNOWN)) {
      needed.add(unk); // keep all unknown
    }

    // ######################################################################
    // ## rename VIDEO
    // ######################################################################
    for (MediaFile vid : movie.getMediaFiles(MediaFileType.VIDEO)) {
      LOGGER.info("rename file " + vid.getFile().getAbsolutePath());

      String newFilename = vid.getFilename();
      String newPath = movie.getPath() + File.separator;
      String fileExtension = FilenameUtils.getExtension(vid.getFilename());

      if (!movie.isDisc()) {
        cleanup.add(new MediaFile(vid)); // mark old file for cleanup (clone current)
        if (renameFiles) {
          // create new filename according to template
          newFilename = createDestination(Globals.settings.getMovieSettings().getMovieRenamerFilename(), movie);
          // is there any stacking information in the filename?
          // use vid.getStacking() != 0 for custom stacking format?
          String stacking = Utils.getStackingMarker(vid.getFilename());
          if (!stacking.isEmpty()) {
            newFilename += " " + stacking;
          }
          else if (vid.getStacking() != 0) {
            newFilename += " CD" + vid.getStacking();
          }
          newFilename += "." + fileExtension;
        }

        MediaFile newMF = new MediaFile(vid);
        File newFile = new File(newPath, newFilename);
        try {
          moveFile(vid.getFile(), newFile);
          newMF.setPath(newPath);
          newMF.setFilename(newFilename);
        }
        catch (FileNotFoundException e) {
          LOGGER.error("error moving video file - file not found", e);
        }
        catch (Exception e) {
          LOGGER.error("error moving video file", e);
        }
        needed.add(newMF);
      }
      else {
        LOGGER.info("Movie is a DVD/BluRay disc folder - NOT renaming file");
        needed.add(vid); // but keep it
      }
    }

    // remove duplicate MediaFiles
    Set<MediaFile> newMFs = new LinkedHashSet<MediaFile>(needed);
    needed.clear();
    needed.addAll(newMFs);

    movie.removeAllMediaFiles();
    movie.addToMediaFiles(needed);
    movie.saveToDb();

    // cleanup & rename subtitle files
    renameSubtitles(movie);

    movie.gatherMediaFileInformation(false);
    movie.saveToDb();

    // ######################################################################
    // ## CLEANUP
    // ######################################################################
    LOGGER.info("Cleanup...");
    for (int i = cleanup.size() - 1; i >= 0; i--) {
      // cleanup files which are not needed
      if (!needed.contains(cleanup.get(i))) {
        MediaFile cl = cleanup.get(i);
        if (cl.getFile().exists()) { // unneded, but for not diplaying wrong deletes in logger...
          LOGGER.debug("Deleting " + cl.getFilename());
          FileUtils.deleteQuietly(cl.getFile()); // delete cleanup file
        }
        File[] list = cl.getFile().getParentFile().listFiles();
        if (list != null && list.length == 0) {
          // if directory is empty, delete it as well
          LOGGER.debug("Deleting empty Directory" + cl.getFile().getParentFile().getAbsolutePath());
          FileUtils.deleteQuietly(cl.getFile().getParentFile());
        }
      }
    }
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
      LOGGER.error("Movie renaming aborted.");
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
