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
package org.tinymediamanager.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.tinymediamanager.Globals;
import org.tinymediamanager.core.ImageCacheTask;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieSet;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The RebuildImageCacheAction to rebuild the whole image cache
 * 
 * @author Manuel Laggner
 */
public class RebuildImageCacheAction extends AbstractAction {
  private static final long           serialVersionUID = -9178351750617647813L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public RebuildImageCacheAction() {
    putValue(NAME, BUNDLE.getString("tmm.rebuildimagecache")); //$NON-NLS-1$
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tmm.rebuildimagecache")); //$NON-NLS-1$
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent arg0) {
    if (!Globals.settings.isImageCache()) {
      JOptionPane.showMessageDialog(null, "Image cache is not activated!");
      return;
    }

    List<File> imageFiles = new ArrayList<File>();
    // movie list
    List<Movie> movies = new ArrayList<Movie>(MovieList.getInstance().getMovies());
    for (Movie movie : movies) {
      imageFiles.addAll(movie.getImagesToCache());
    }

    // moviesets
    List<MovieSet> movieSets = new ArrayList<MovieSet>(MovieList.getInstance().getMovieSetList());
    for (MovieSet movieSet : movieSets) {
      imageFiles.addAll(movieSet.getImagesToCache());
    }

    // tv dhows
    List<TvShow> tvShows = new ArrayList<TvShow>(TvShowList.getInstance().getTvShows());
    for (TvShow tvShow : tvShows) {
      imageFiles.addAll(tvShow.getImagesToCache());
    }

    ImageCacheTask task = new ImageCacheTask(imageFiles);
    Globals.executor.execute(task);
  }
}