/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;

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

    String newVideoBasename = "";
    if (MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerFilename().trim().isEmpty()) {
      // we are NOT renaming any files, so we keep the same name on renaming ;)
      newVideoBasename = Utils.cleanStackingMarkers(movie.getMediaFiles(MediaFileType.VIDEO).get(0).getBasename());
    }
    else {
      // since we rename, generate the new basename
      MediaFile ftr = MovieRenamer.generateFilename(movie, movie.getMediaFiles(MediaFileType.VIDEO).get(0), newVideoBasename).get(0);
      newVideoBasename = Utils.cleanStackingMarkers(ftr.getBasename());
    }

    // VIDEO needs to be renamed first, since all others depend on that name!!!
    for (MediaFile mf : movie.getMediaFiles(MediaFileType.VIDEO)) {
      oldFiles.add(new MediaFile(mf));
      MediaFile ftr = MovieRenamer.generateFilename(movie, mf, newVideoBasename).get(0); // there can be only one
      newFiles.add(ftr);
    }

    // all the other MFs...
    for (MediaFile mf : movie.getMediaFilesExceptType(MediaFileType.VIDEO)) {
      oldFiles.add(new MediaFile(mf));
      newFiles.addAll(MovieRenamer.generateFilename(movie, mf, newVideoBasename)); // N:M
    }

    // movie folder needs a rename?
    File oldMovieFolder = new File(movie.getPath());
    String pattern = MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerPathname();
    if (pattern.isEmpty()) {
      // same
      container.newPath = Utils.relPath(movie.getDataSource(), movie.getPath());
    }
    else {
      container.newPath = MovieRenamer.createDestinationForFoldername(pattern, movie);
    }
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
        // System.out.println(mf);
        container.needsRename = true;
        break;
      }
    }

    for (MediaFile mf : oldFiles) {
      if (!newFiles.contains(mf)) {
        // System.out.println(mf);
        container.needsRename = true;
        break;
      }
    }

    container.newMediaFiles.addAll(newFiles);
    return container;
  }
}
