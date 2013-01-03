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

    // move movie files
    LOGGER.debug("file expression: " + Globals.settings.getMovieRenamerFilename());
    int i = 1;
    List<String> newFiles = new ArrayList<String>();
    // for (String file : movie.getMovieFiles()) {
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

      // do we need to rename the nfo?
      if (i == 1 && Globals.settings.getMovieNfoFilenames().contains(MovieNfoNaming.FILENAME_NFO)) {
        String oldNfoFile = movie.getPath() + File.separator + fileWithoutExtension + ".nfo";
        String newNfoFile = movie.getPath() + File.separator + newFilename + ".nfo";
        try {
          moveFile(oldNfoFile, newNfoFile);
        }
        catch (Exception e) {
          LOGGER.error("move nfo", e);
        }
      }

      // do we need to rename the posters?
      if (i == 1
          && (Globals.settings.getMoviePosterFilenames().contains(MoviePosterNaming.FILENAME_JPG) || Globals.settings.getMoviePosterFilenames()
              .contains(MoviePosterNaming.FILENAME_TBN))) {
        // poster as jpg
        if (Globals.settings.getMoviePosterFilenames().contains(MoviePosterNaming.FILENAME_JPG)) {
          String oldPosterFile = movie.getPath() + File.separator + fileWithoutExtension + ".jpg";
          String newPosterFile = movie.getPath() + File.separator + newFilename + ".jpg";
          try {
            moveFile(oldPosterFile, newPosterFile);
            movie.setPoster(FilenameUtils.getName(newPosterFile));
          }
          catch (Exception e) {
            LOGGER.error("move nfo", e);
          }
        }

        // poster as tbn
        if (Globals.settings.getMoviePosterFilenames().contains(MoviePosterNaming.FILENAME_TBN)) {
          String oldPosterFile = movie.getPath() + File.separator + fileWithoutExtension + ".tbn";
          String newPosterFile = movie.getPath() + File.separator + newFilename + ".tbn";
          try {
            moveFile(oldPosterFile, newPosterFile);
            movie.setPoster(FilenameUtils.getName(newPosterFile));
          }
          catch (Exception e) {
            LOGGER.error("move nfo", e);
          }
        }
      }

      // to we need to rename the fanart?
      if (i == 1 && Globals.settings.getMovieFanartFilenames().contains(MovieFanartNaming.FILENAME_JPG)) {
        String oldFanartFile = movie.getPath() + File.separator + fileWithoutExtension + "-fanart.jpg";
        String newFanartFile = movie.getPath() + File.separator + newFilename + "-fanart.jpg";
        try {
          moveFile(oldFanartFile, newFanartFile);
          movie.setFanart(FilenameUtils.getName(newFanartFile));
        }
        catch (Exception e) {
          LOGGER.error("move nfo", e);
        }
      }

      newFilename = movie.getPath() + File.separator + newFilename + "." + fileExtension;

      // movie file
      try {
        moveFile(oldFilename, newFilename);
        file.setFilename(FilenameUtils.getName(newFilename));
        // newFiles.add(FilenameUtils.getName(newFilename));
      }
      catch (Exception e) {
        LOGGER.error("move file", e);
        // newFiles.add(file);
      }

      i++;
    }

    // movie.setMovieFiles(newFiles);
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
  private static String createDestination(String template, Movie movie) {
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

    // replace illegal characters (ie : in windows path is not allowed
    newDestination.replace(":", "");

    return newDestination;
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
  private static void moveFile(String oldFilename, String newFilename) throws Exception {
    if (!oldFilename.equals(newFilename)) {
      File oldFile = new File(oldFilename);
      if (oldFile.exists()) {
        File newFile = new File(newFilename);
        FileUtils.moveFile(oldFile, newFile);
        LOGGER.debug("moved file " + oldFilename + " to " + newFilename);
      }
      else {
        throw new FileNotFoundException(oldFilename);
      }
    }
  }
}
