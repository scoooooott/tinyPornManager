/*
 * Copyright 2012 - 2014 Manuel Laggner
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileSubtitle;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;

/**
 * The class MovieRenamerPreview. To create a preview of the movie renamer (dry run)
 * 
 * @author Manuel Laggner / Myron Boyle
 */
public class MovieRenamerPreview {

  public static MovieRenamerPreviewContainer renameMovie(Movie movie) {
    MovieRenamerPreviewContainer container = new MovieRenamerPreviewContainer(movie);

    List<MediaFile> oldFiles = new ArrayList<MediaFile>();
    Set<MediaFile> newFiles = new LinkedHashSet<MediaFile>();

    String newVideoFileName = "";

    // VIDEO needs to be renamed first, since all others depend on that name!!!
    for (MediaFile mf : movie.getMediaFiles(MediaFileType.VIDEO)) {
      oldFiles.add(new MediaFile(mf));
      MediaFile ftr = generateFilename(movie, mf, newVideoFileName).get(0); // there can be only one
      newFiles.add(ftr);
      if (newVideoFileName.isEmpty()) {
        // so remember first renamed video file basename (w/o stacking or extension)
        newVideoFileName = Utils.cleanStackingMarkers(ftr.getBasename());
      }
    }

    // all the other MFs...
    for (MediaFile mf : movie.getMediaFilesExceptType(MediaFileType.VIDEO)) {
      oldFiles.add(new MediaFile(mf));
      newFiles.addAll(generateFilename(movie, mf, newVideoFileName)); // N:M
    }

    // movie folder needs a rename?
    File oldMovieFolder = new File(movie.getPath());
    container.newPath = MovieRenamer.createDestinationForFoldername(Globals.settings.getMovieSettings().getMovieRenamerPathname(), movie)
        + File.separator;
    File newMovieFolder = new File(movie.getDataSource(), container.newPath);

    if (!oldMovieFolder.equals(newMovieFolder)) {
      container.needsRename = true;

      // update already the "old" files with new path, so we can simply do a contains check ;)
      for (MediaFile omf : oldFiles) {
        omf.replacePathForRenamedFolder(oldMovieFolder, newMovieFolder);
      }
    }

    // change status of MFs, if they have been added or not
    for (MediaFile mf : newFiles) {
      if (!oldFiles.contains(mf)) {
        container.needsRename = true;
        break;
      }
    }

    for (MediaFile mf : oldFiles) {
      if (!newFiles.contains(mf)) {
        container.needsRename = true;
        break;
      }
    }

    container.newMediaFiles.addAll(newFiles);
    return container;
  }

  /**
   * generates renamed filename(s) per MF
   * 
   * @param movie
   *          the movie (for datasource, path)
   * @param mf
   *          the MF
   * @param videoFileName
   *          the basename of the renamed videoFileName (saved earlier)
   * @return list of renamed filename
   */
  private static ArrayList<MediaFile> generateFilename(Movie movie, MediaFile mf, String videoFileName) {
    // return list of all generated MFs
    ArrayList<MediaFile> newFiles = new ArrayList<MediaFile>();

    String newPathname = MovieRenamer.createDestinationForFoldername(Globals.settings.getMovieSettings().getMovieRenamerPathname(), movie);
    String movieDir = movie.getDataSource() + File.separatorChar + newPathname + File.separatorChar;

    String newFilename = videoFileName;
    if (newFilename == null || newFilename.isEmpty()) {
      newFilename = MovieRenamer.createDestinationForFilename(Globals.settings.getMovieSettings().getMovieRenamerFilename(), movie);
    }

    switch (mf.getType()) {
      case VIDEO:
        MediaFile vid = new MediaFile(mf);
        if (movie.isDisc() || mf.isDiscFile()) {
          // just replace new path and return file (do not change names!)
          vid.replacePathForRenamedFolder(new File(movie.getPath()), new File(movieDir));
        }
        else {
          newFilename += getStackingString(mf);
          newFilename += "." + mf.getExtension();
          vid.setFile(new File(movieDir + newFilename));
        }
        newFiles.add(vid);
        break;

      case TRAILER:
        MediaFile trail = new MediaFile(mf);
        newFilename += "-trailer." + mf.getExtension();
        trail.setFile(new File(movieDir + newFilename));
        newFiles.add(trail);
        break;

      case SUBTITLE:
        String lang = "";
        String forced = "";
        List<MediaFileSubtitle> mfsl = mf.getSubtitles();

        if (mfsl != null && mfsl.size() > 0) {
          MediaFileSubtitle mfs = mfsl.get(0);
          lang = mfs.getLanguage();
          if (mfs.isForced()) {
            forced = ".forced";
          }
        }
        newFilename += getStackingString(mf);
        newFilename += forced;
        if (!lang.isEmpty()) {
          newFilename += "." + lang;
        }
        newFilename += "." + mf.getExtension();
        MediaFile sub = new MediaFile(mf);
        sub.setFile(new File(movieDir + newFilename));
        newFiles.add(sub);
        break;

      case NFO:
        List<MovieNfoNaming> nfonames = new ArrayList<MovieNfoNaming>();
        if (movie.isMultiMovieDir()) {
          // Fixate the name regardless of setting
          nfonames.add(MovieNfoNaming.FILENAME_NFO);
        }
        else {
          nfonames = Globals.settings.getMovieSettings().getMovieNfoFilenames();
        }
        for (MovieNfoNaming name : nfonames) {
          newFilename = movie.getNfoFilename(name, videoFileName);
          MediaFile nfo = new MediaFile(mf);
          nfo.setFile(new File(movieDir + newFilename));
          newFiles.add(nfo);
        }
        break;

      case POSTER:
        List<MoviePosterNaming> posternames = new ArrayList<MoviePosterNaming>();
        if (movie.isMultiMovieDir()) {
          // Fixate the name regardless of setting
          posternames.add(MoviePosterNaming.FILENAME_POSTER_JPG);
          posternames.add(MoviePosterNaming.FILENAME_POSTER_PNG);
        }
        else {
          posternames = Globals.settings.getMovieSettings().getMoviePosterFilenames();
        }
        for (MoviePosterNaming name : posternames) {
          newFilename = movie.getPosterFilename(name, videoFileName);
          if (newFilename != null && !newFilename.isEmpty()) {
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
          }
          MediaFile pos = new MediaFile(mf);
          pos.setFile(new File(movieDir + newFilename));
          newFiles.add(pos);
        }
        break;

      case FANART:
        List<MovieFanartNaming> fanartnames = new ArrayList<MovieFanartNaming>();
        if (movie.isMultiMovieDir()) {
          // Fixate the name regardless of setting
          fanartnames.add(MovieFanartNaming.FILENAME_FANART_JPG);
          fanartnames.add(MovieFanartNaming.FILENAME_FANART_PNG);
        }
        else {
          fanartnames = Globals.settings.getMovieSettings().getMovieFanartFilenames();
        }
        for (MovieFanartNaming name : fanartnames) {
          newFilename = movie.getFanartFilename(name, videoFileName);
          if (newFilename != null && !newFilename.isEmpty()) {
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
          }
          MediaFile fan = new MediaFile(mf);
          fan.setFile(new File(movieDir + newFilename));
          newFiles.add(fan);
        }
        break;

      default:
        // return 1:1, only with renamed path
        MediaFile def = new MediaFile(mf);
        def.replacePathForRenamedFolder(new File(movie.getPath()), new File(movieDir));
        newFiles.add(def);
        break;
    }

    return newFiles;
  }

  /**
   * returns "delimiter + stackingString" for use in filename
   * 
   * @param mf
   *          a mediaFile
   * @return eg ".CD1" dependent of settings
   */
  private static String getStackingString(MediaFile mf) {
    String stacking = Utils.getStackingMarker(mf.getFilename());
    String delimiter = " ";
    if (Globals.settings.getMovieSettings().isMovieRenamerSpaceSubstitution()) {
      delimiter = Globals.settings.getMovieSettings().getMovieRenamerSpaceReplacement();
    }
    if (!stacking.isEmpty()) {
      return delimiter + stacking;
    }
    else if (mf.getStacking() != 0) {
      return delimiter + "CD" + mf.getStacking();
    }
    return "";
  }
}
