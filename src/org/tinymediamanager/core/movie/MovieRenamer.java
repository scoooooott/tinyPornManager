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
package org.tinymediamanager.core.movie;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.Utils;

/**
 * The Class MovieRenamer.
 * 
 * @author Manuel Laggner / Myron Boyle
 */
public class MovieRenamer {

  /** The Constant LOGGER. */
  private final static Logger LOGGER = Logger.getLogger(MovieRenamer.class);

  /**
   * prepares the NFO cleanup;<br>
   * returns a list of ALL nfo names before (movie/file)renaming<br>
   * (no cleanup done yet).
   * 
   * @param m
   *          the m
   * @return the array list
   */
  private static ArrayList<String> prepareCleanupNfos(Movie m) {
    MovieNfoNaming[] all = MovieNfoNaming.values();
    ArrayList<String> oldfiles = new ArrayList<String>();
    for (MovieNfoNaming old : all) {
      oldfiles.add(m.getNfoFilename(old));
    }
    return oldfiles;
  }

  /**
   * deletes all unselected Nfo variants movie object<br>
   * alternatively, set the oldFilename parameter to cleanup an "old" pattern.
   * 
   * @param m
   *          the m
   * @param oldFilenames
   *          the old filenames
   */
  private static void cleanupNfos(Movie m, ArrayList<String> oldFilenames) {
    List<MovieNfoNaming> setup = Globals.settings.getMovieNfoFilenames();
    MovieNfoNaming[] all = MovieNfoNaming.values();
    for (MovieNfoNaming unused : all) {
      if (!setup.contains(unused)) {
        FileUtils.deleteQuietly(new File(m.getNfoFilename(unused)));
      }
      else {
        // this is a needed (new filename) one, so potentially remove from old
        // list
        oldFilenames.remove(m.getNfoFilename(unused));
      }
    }
    // delete what's left
    for (String old : oldFilenames) {
      FileUtils.deleteQuietly(new File(old));
    }
  }

  /**
   * prepares the poster cleanup;<br>
   * returns a list of ALL poster names before (movie/file)renaming<br>
   * (no cleanup done yet).
   * 
   * @param m
   *          the m
   * @return the array list
   */
  private static ArrayList<String> prepareCleanupPosters(Movie m) {
    MoviePosterNaming[] all = MoviePosterNaming.values();
    ArrayList<String> oldfiles = new ArrayList<String>();
    for (MoviePosterNaming old : all) {
      oldfiles.add(m.getPosterFilename(old));
    }
    return oldfiles;
  }

  /**
   * deletes all unselected poster variants movie object<br>
   * alternatively, set the oldFilename parameter to cleanup an "old" pattern.
   * 
   * @param m
   *          the m
   * @param oldFilenames
   *          the old filenames
   */
  private static void cleanupPosters(Movie m, ArrayList<String> oldFilenames) {
    List<MoviePosterNaming> setup = Globals.settings.getMoviePosterFilenames();
    MoviePosterNaming[] all = MoviePosterNaming.values();
    for (MoviePosterNaming unused : all) {
      if (!setup.contains(unused)) {
        FileUtils.deleteQuietly(new File(m.getPosterFilename(unused)));
      }
      else {
        // this is a needed (new filename) one, so potentially remove from old
        // list
        oldFilenames.remove(m.getPosterFilename(unused));
      }
    }
    // delete what's left
    for (String old : oldFilenames) {
      FileUtils.deleteQuietly(new File(old));
    }
  }

  /**
   * prepares the Fanart cleanup;<br>
   * returns a list of ALL fanart names before (movie/file)renaming<br>
   * (no cleanup done yet).
   * 
   * @param m
   *          the m
   * @return the array list
   */
  private static ArrayList<String> prepareCleanupFanarts(Movie m) {
    MovieFanartNaming[] all = MovieFanartNaming.values();
    ArrayList<String> oldfiles = new ArrayList<String>();
    for (MovieFanartNaming old : all) {
      oldfiles.add(m.getFanartFilename(old));
    }
    return oldfiles;
  }

  /**
   * deletes all unselected poster variants of movie object<br>
   * alternatively, set the oldFilename parameter to cleanup an "old" pattern.
   * 
   * @param m
   *          the m
   * @param oldFilenames
   *          the old filenames
   */
  private static void cleanupFanarts(Movie m, ArrayList<String> oldFilenames) {
    List<MovieFanartNaming> setup = Globals.settings.getMovieFanartFilenames();
    MovieFanartNaming[] all = MovieFanartNaming.values();
    for (MovieFanartNaming unused : all) {
      if (!setup.contains(unused)) {
        FileUtils.deleteQuietly(new File(m.getFanartFilename(unused)));
      }
      else {
        // this is a needed (new filename) one, so potentially remove from old
        // list
        oldFilenames.remove(m.getFanartFilename(unused));
      }
    }
    // delete what's left
    for (String old : oldFilenames) {
      FileUtils.deleteQuietly(new File(old));
    }
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
    LOGGER.debug("movie path: " + movie.getPath());
    LOGGER.debug("movie name: " + movie.getTitle());
    LOGGER.debug("movie originalTitle: " + movie.getOriginalTitle());
    LOGGER.debug("movie year: " + movie.getYear());
    LOGGER.debug("path expression: " + Globals.settings.getMovieRenamerPathname());
    String newPathname = createDestination(Globals.settings.getMovieRenamerPathname(), movie);
    String oldPathname = movie.getPath();

    if (!newPathname.isEmpty()) {
      newPathname = movie.getDataSource() + File.separator + newPathname;

      // move directory if needed
      if (!StringUtils.equals(oldPathname, newPathname)) {
        File srcDir = new File(oldPathname);
        File destDir = new File(newPathname);
        boolean ok = false;
        try {
          // FileUtils.moveDirectory(srcDir, destDir);
          ok = moveDirectorySafe(srcDir, destDir);
          if (ok) {
            movie.setPath(newPathname);
          }
        }
        catch (IOException e) {
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

    // prepare the cleanup with "old" name
    ArrayList<String> oldNfos = prepareCleanupNfos(movie);
    ArrayList<String> oldFanarts = prepareCleanupFanarts(movie);
    ArrayList<String> oldPosters = prepareCleanupPosters(movie);

    LOGGER.debug("file expression: " + Globals.settings.getMovieRenamerFilename());
    if (!Globals.settings.getMovieRenamerFilename().isEmpty()) {

      // skip media file renaming on "disc" folder
      if (!movie.isDisc()) {

        // move movie files first
        for (MediaFile file : movie.getMediaFiles()) {
          String newFilename = createDestination(Globals.settings.getMovieRenamerFilename(), movie);

          // get the filetype
          String fileExtension = FilenameUtils.getExtension(file.getFilename());
          String fileWithoutExtension = FilenameUtils.getBaseName(file.getFilename());
          String oldFilename = movie.getPath() + File.separator + file.getFilename();

          // is there any stacking information in the filename?
          String cleanFilename = Utils.cleanStackingMarkers(fileWithoutExtension);
          if (!fileWithoutExtension.equals(cleanFilename)) {
            String stackingInformation = fileWithoutExtension.replace(cleanFilename, "");
            newFilename = newFilename + " " + stackingInformation;
          }

          // movie file
          newFilename = movie.getPath() + File.separator + newFilename + "." + fileExtension;
          try {
            moveFile(oldFilename, newFilename);
            file.setPath(movie.getPath());
            file.setFilename(FilenameUtils.getName(newFilename));
            // newFiles.add(FilenameUtils.getName(newFilename));
          }
          catch (Exception e) {
            LOGGER.error("error moving file", e);
            // newFiles.add(file);
          }
        }

      } // end isDisc

    } // end skip file rename if empty
    else {
      LOGGER.info("File rename settings were empty - NOT renaming files");
    }

    // copies nfo to selected variants and does a cleanup afterwards
    if (!movie.getNfoFilename().isEmpty()) {
      String oldNfoFile = movie.getNfoFilename();
      String newNfoFile = "";
      for (MovieNfoNaming name : Globals.settings.getMovieNfoFilenames()) {
        newNfoFile = movie.getNfoFilename(name);
        try {
          copyFile(oldNfoFile, newNfoFile);
          movie.setNfoFilename(FilenameUtils.getName(newNfoFile));
        }
        catch (Exception e) {
          LOGGER.error("error renaming Nfo", e);
        }
      }
      cleanupNfos(movie, oldNfos);
    }

    // copies poster to selected variants and does a cleanup afterwards
    if (!movie.getPoster().isEmpty()) {
      String oldPosterFile = movie.getPoster();
      String newPosterFile = "";
      for (MoviePosterNaming name : Globals.settings.getMoviePosterFilenames()) {
        newPosterFile = movie.getPosterFilename(name);

        // only store .png as png and .jpg as jpg
        String generatedFiletype = FilenameUtils.getExtension(newPosterFile);
        String providedFiletype = FilenameUtils.getExtension(oldPosterFile);
        if (!generatedFiletype.equals(providedFiletype)) {
          continue;
        }

        try {
          copyFile(oldPosterFile, newPosterFile);
          movie.setPoster(FilenameUtils.getName(newPosterFile));
        }
        catch (Exception e) {
          LOGGER.error("error renaming poster", e);
        }
      }
      cleanupPosters(movie, oldPosters);
    }

    // copies fanarts to selected variants and does a cleanup afterwards
    if (!movie.getFanart().isEmpty()) {
      String oldFanartFile = movie.getFanart();
      String newFanartFile = "";
      for (MovieFanartNaming name : Globals.settings.getMovieFanartFilenames()) {
        newFanartFile = movie.getFanartFilename(name);

        // only store .png as png and .jpg as jpg
        String generatedFiletype = FilenameUtils.getExtension(newFanartFile);
        String providedFiletype = FilenameUtils.getExtension(oldFanartFile);
        if (!generatedFiletype.equals(providedFiletype)) {
          continue;
        }

        try {
          copyFile(oldFanartFile, newFanartFile);
          movie.setFanart(FilenameUtils.getName(newFanartFile));
        }
        catch (Exception e) {
          LOGGER.error("error renaming fanart", e);
        }
      }
      cleanupFanarts(movie, oldFanarts);
    }

    movie.saveToDb();
  }

  /**
   * modified version of commons-io FileUtils.moveDirectory();<br>
   * since renameTo() might not work in first place, retry it up to 5 times.<br>
   * (better wait 5 sec for success, than always copying a 50gig directory ;)<br>
   * And NO, we're NOT doing a copy+delete as fallback!
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
  private static boolean moveDirectorySafe(File srcDir, File destDir) throws IOException {
    // rip-off from
    // http://svn.apache.org/repos/asf/commons/proper/io/trunk/src/main/java/org/apache/commons/io/FileUtils.java
    if (srcDir == null) {
      throw new NullPointerException("Source must not be null");
    }
    if (destDir == null) {
      throw new NullPointerException("Destination must not be null");
    }
    LOGGER.debug("try to move folder " + srcDir.getName() + " to " + destDir.getName());
    if (!srcDir.exists()) {
      throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
    }
    if (!srcDir.isDirectory()) {
      throw new IOException("Source '" + srcDir + "' is not a directory");
    }
    if (destDir.exists()) {
      throw new FileExistsException("Destination '" + destDir + "' already exists");
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
      LOGGER.error("Failed to rename directory '" + srcDir + " to " + destDir.getName());
      LOGGER.error("Movie renaming aborted.");
      return false;
    }
    else {
      LOGGER.info("Successfully moved folder " + srcDir.getName() + " to " + destDir.getName());
      return true;
    }
  }

  /**
   * Creates the destination.
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
    if (newDestination.contains("$T") && StringUtils.isNotEmpty(movie.getTitle())) {
      newDestination = newDestination.replaceAll("\\$T", movie.getTitle());
    }

    // replace token first letter of title ($1)
    if (newDestination.contains("$1") && StringUtils.isNotEmpty(movie.getTitle())) {
      newDestination = newDestination.replaceAll("\\$1", movie.getTitle().substring(0, 1));
    }

    // replace token year ($Y)
    if (newDestination.contains("$Y") && StringUtils.isNotEmpty(movie.getYear())) {
      newDestination = newDestination.replaceAll("\\$Y", movie.getYear());
    }

    // replace token orignal title ($O)
    if (newDestination.contains("$O") && StringUtils.isNotEmpty(movie.getOriginalTitle())) {
      newDestination = newDestination.replaceAll("\\$O", movie.getOriginalTitle());
    }

    // replace token IMDBid ($I)
    if (newDestination.contains("$I") && StringUtils.isNotEmpty(movie.getImdbId())) {
      newDestination = newDestination.replaceAll("\\$I", movie.getImdbId());
    }

    // replace token sort title ($E)
    if (newDestination.contains("$E") && StringUtils.isNotEmpty(movie.getSortTitle())) {
      newDestination = newDestination.replaceAll("\\$E", movie.getSortTitle());
    }

    // replace token resolution ($R)
    if (newDestination.contains("$R") && movie.getMediaFiles().size() > 0) {
      newDestination = newDestination.replaceAll("\\$R", movie.getMediaFiles().get(0).getVideoResolution());
    }

    // replace token audio codec + channels ($A)
    if (newDestination.contains("$A") && movie.getMediaFiles().size() > 0) {
      newDestination = newDestination.replaceAll("\\$A", movie.getMediaFiles().get(0).getAudioCodec() + "-"
          + movie.getMediaFiles().get(0).getAudioChannels());
    }

    // replace token video codec + channels ($V)
    if (newDestination.contains("$V") && movie.getMediaFiles().size() > 0) {
      newDestination = newDestination.replaceAll("\\$V", movie.getMediaFiles().get(0).getVideoCodec() + "-"
          + movie.getMediaFiles().get(0).getVideoFormat());
    }

    // replace token media source (BluRay|DVD|TV|...) ($S)
    // if (newDestination.contains("$S")) {
    // newDestination = newDestination.replaceAll("\\$S",
    // movie.getMediaSource());
    // }

    // replace illegal characters
    // http://msdn.microsoft.com/en-us/library/windows/desktop/aa365247%28v=vs.85%29.aspx
    newDestination = newDestination.replaceAll("([:<>|?*])", "");

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
  public static void moveFile(String oldFilename, String newFilename) throws Exception {
    if (!oldFilename.equals(newFilename)) {
      LOGGER.info("move file " + oldFilename + " to " + newFilename);
      File newFile = new File(newFilename);
      if (newFile.exists()) {
        // overwrite?
        LOGGER.warn(newFilename + " exists - do nothing.");
      }
      else {
        File oldFile = new File(oldFilename);
        if (oldFile.exists()) {
          FileUtils.moveFile(oldFile, newFile);
        }
        else {
          throw new FileNotFoundException(oldFilename);
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
  public static void copyFile(String oldFilename, String newFilename) throws Exception {
    if (!oldFilename.equals(newFilename)) {
      LOGGER.info("copy file " + oldFilename + " to " + newFilename);
      File newFile = new File(newFilename);
      if (newFile.exists()) {
        // overwrite?
        LOGGER.warn(newFilename + " exists - do nothing.");
      }
      else {
        File oldFile = new File(oldFilename);
        if (oldFile.exists()) {
          FileUtils.copyFile(oldFile, newFile, true);
        }
        else {
          throw new FileNotFoundException(oldFilename);
        }
      }
    }
  }
}
