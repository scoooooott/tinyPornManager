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
package org.tinymediamanager.core.movie;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.tinymediamanager.core.MediaFileType;
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

    LinkedHashMap<String, MediaFile> oldFiles = new LinkedHashMap<>();
    Set<MediaFile> newFiles = new LinkedHashSet<>();

    String newVideoBasename = "";
    if (MovieModuleManager.SETTINGS.getRenamerFilename().trim().isEmpty()) {
      // we are NOT renaming any files, so we keep the same name on renaming ;)
      newVideoBasename = movie.getVideoBasenameWithoutStacking();
    }
    else {
      // since we rename, generate the new basename
      MediaFile ftr = MovieRenamer.generateFilename(movie, movie.getMediaFiles(MediaFileType.VIDEO).get(0), newVideoBasename).get(0);
      newVideoBasename = FilenameUtils.getBaseName(ftr.getFilenameWithoutStacking());
    }

    // VIDEO needs to be renamed first, since all others depend on that name!!!
    for (MediaFile mf : movie.getMediaFiles(MediaFileType.VIDEO)) {
      oldFiles.put(mf.getFileAsPath().toString(), new MediaFile(mf));
      MediaFile ftr = MovieRenamer.generateFilename(movie, mf, newVideoBasename).get(0); // there can be only one
      newFiles.add(ftr);
    }

    // all the other MFs...
    for (MediaFile mf : movie.getMediaFilesExceptType(MediaFileType.VIDEO)) {
      oldFiles.put(mf.getFileAsPath().toString(), new MediaFile(mf));
      newFiles.addAll(MovieRenamer.generateFilename(movie, mf, newVideoBasename)); // N:M
    }

    // movie folder needs a rename?
    Path oldMovieFolder = movie.getPathNIO();
    String pattern = MovieModuleManager.SETTINGS.getRenamerPathname();
    if (pattern.isEmpty()) {
      // same
      container.newPath = Paths.get(movie.getDataSource()).relativize(movie.getPathNIO());
    }
    else {
      container.newPath = Paths.get(MovieRenamer.createDestinationForFoldername(pattern, movie));
    }
    Path newMovieFolder = Paths.get(movie.getDataSource()).resolve(container.newPath);

    if (!oldMovieFolder.equals(newMovieFolder)) {
      container.needsRename = true;
      // update already the "old" files with new path, so we can simply do a contains check ;)
      for (MediaFile omf : oldFiles.values()) {
        omf.replacePathForRenamedFolder(oldMovieFolder, newMovieFolder);
      }
    }

    // change status of MFs, if they have been added or not
    for (MediaFile mf : newFiles) {
      if (!oldFiles.containsKey(mf.getFileAsPath().toString())) {
        // System.out.println(mf);
        container.needsRename = true;
        break;
      }
    }

    for (MediaFile mf : oldFiles.values()) {
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
