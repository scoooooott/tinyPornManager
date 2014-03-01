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
import java.util.List;

import org.tinymediamanager.core.MediaFile;

/**
 * The class MovieRenamerPreviewContainter. To hold all relevant data for the renamer preview
 * 
 * @author Manuel Laggner
 */
public class MovieRenamerPreviewContainer {

  Movie           movie;
  String          oldPath       = "";
  String          newPath       = "";
  List<MediaFile> newMediaFiles = new ArrayList<MediaFile>();
  boolean         needsRename   = false;

  public MovieRenamerPreviewContainer(Movie movie) {
    this.movie = movie;
    this.oldPath = new File(movie.getDataSource()).toURI().relativize(new File(movie.getPath()).toURI()).getPath();
  }

  public Movie getMovie() {
    return movie;
  }

  public String getOldPath() {
    return oldPath;
  }

  public String getNewPath() {
    return newPath;
  }

  public List<MediaFile> getOldMediaFiles() {
    return movie.getMediaFiles();
  }

  public List<MediaFile> getNewMediaFiles() {
    return newMediaFiles;
  }

  public boolean isNeedsRename() {
    return needsRename;
  }
}
