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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.Utils;

/**
 * The Class MovieRenamer.
 */
public class MovieRenamer {

  /** The Constant LOGGER. */
  private final static Logger LOGGER = Logger.getLogger(MovieRenamer.class);

  /**
   * deletes all unselected Nfo variants
   * 
   * @param movie
   */
  private static void cleanupNfos(Movie movie) {
    List<MovieNfoNaming> setup = Globals.settings.getMovieNfoFilenames();
    MovieNfoNaming[] all = MovieNfoNaming.values();
    for (MovieNfoNaming unused : all) {
      if (!setup.contains(unused)) {
        FileUtils.deleteQuietly(new File(movie.getNfoFilename(unused)));
      }
    }
  }

  /**
   * deletes all unselected poster variants
   * 
   * @param movie
   */
  private static void cleanupPosters(Movie movie) {
    List<MoviePosterNaming> setup = Globals.settings.getMoviePosterFilenames();
    MoviePosterNaming[] all = MoviePosterNaming.values();
    for (MoviePosterNaming unused : all) {
      if (!setup.contains(unused)) {
        FileUtils.deleteQuietly(new File(movie.getPosterFilename(unused)));
      }
    }
  }

  /**
   * deletes all unselected poster variants
   * 
   * @param movie
   */
  private static void cleanupFanarts(Movie movie) {
    List<MovieFanartNaming> setup = Globals.settings.getMovieFanartFilenames();
    MovieFanartNaming[] all = MovieFanartNaming.values();
    for (MovieFanartNaming unused : all) {
      if (!setup.contains(unused)) {
        FileUtils.deleteQuietly(new File(movie.getFanartFilename(unused)));
      }
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
    LOGGER.debug("movie name: " + movie.getName());
    LOGGER.debug("movie originalname: " + movie.getOriginalName());
    LOGGER.debug("movie year: " + movie.getYear());
    LOGGER.debug("path expression: " + Globals.settings.getMovieRenamerPathname());
    String newPathname = createDestination(Globals.settings.getMovieRenamerPathname(), movie);
    String oldPathname = movie.getPath();

    newPathname = movie.getDataSource() + File.separator + newPathname;

    // move directory if needed
    if (!StringUtils.equals(oldPathname, newPathname)) {
      File srcDir = new File(oldPathname);
      File destDir = new File(newPathname);
      try {
        FileUtils.moveDirectory(srcDir, destDir);
        LOGGER.debug("moved folder " + oldPathname + " to " + newPathname);
        movie.setPath(newPathname);
      }
      catch (IOException e) {
        LOGGER.error("move folder", e);
      }
    }

    LOGGER.debug("file expression: " + Globals.settings.getMovieRenamerFilename());

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
      cleanupNfos(movie);
    }

    // copies poster to selected variants and does a cleanup afterwards
    if (!movie.getPoster().isEmpty()) {
      String oldPosterFile = movie.getPoster();
      String newPosterFile = "";
      for (MoviePosterNaming name : Globals.settings.getMoviePosterFilenames()) {
        newPosterFile = movie.getPosterFilename(name);
        try {
          copyFile(oldPosterFile, newPosterFile);
          movie.setPoster(FilenameUtils.getName(newPosterFile));
        }
        catch (Exception e) {
          LOGGER.error("error renaming poster", e);
        }
      }
      cleanupPosters(movie);
    }

    // copies fanarts to selected variants and does a cleanup afterwards
    if (!movie.getFanart().isEmpty()) {
      String oldFanartFile = movie.getFanart();
      String newFanartFile = "";
      for (MovieFanartNaming name : Globals.settings.getMovieFanartFilenames()) {
        newFanartFile = movie.getFanartFilename(name);
        try {
          copyFile(oldFanartFile, newFanartFile);
          movie.setFanart(FilenameUtils.getName(newFanartFile));
        }
        catch (Exception e) {
          LOGGER.error("error renaming fanart", e);
        }
      }
      cleanupFanarts(movie);
    }

    movie.saveToDb();
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
    if (newDestination.contains("$T")) {
      newDestination = newDestination.replaceAll("\\$T", movie.getName());
    }

    // replace token first letter of title ($1)
    if (newDestination.contains("$1")) {
      newDestination = newDestination.replaceAll("\\$1", movie.getName().substring(0, 1));
    }

    // replace token year ($Y)
    if (newDestination.contains("$Y")) {
      newDestination = newDestination.replaceAll("\\$Y", movie.getYear());
    }

    // replace token orignal title ($O)
    if (newDestination.contains("$O")) {
      newDestination = newDestination.replaceAll("\\$O", movie.getOriginalName());
    }

    // replace token IMDBid ($I)
    if (newDestination.contains("$I")) {
      newDestination = newDestination.replaceAll("\\$I", movie.getImdbId());
    }

    // replace token sort title ($E)
    if (newDestination.contains("$E")) {
      newDestination = newDestination.replaceAll("\\$E", movie.getSortTitle());
    }

    // replace token resolution ($R)
    if (newDestination.contains("$R")) {
      newDestination = newDestination.replaceAll("\\$R", movie.getMediaFiles().get(0).getVideoResolution());
    }

    // replace token audio codec + channels ($A)
    if (newDestination.contains("$A")) {
      newDestination = newDestination.replaceAll("\\$A", movie.getMediaFiles().get(0).getAudioCodec() + "-"
          + movie.getMediaFiles().get(0).getAudioChannels());
    }

    // replace token video codec + channels ($V)
    if (newDestination.contains("$V")) {
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
