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
package org.tinymediamanager.ui.movies;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.MediaGenres;

/**
 * The Class MovieGenresPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieGenresPanel extends JPanel {
  private static final long   serialVersionUID = -6585642654072040266L;
  private static final Logger LOGGER           = LoggerFactory.getLogger(MovieGenresPanel.class);

  private MovieSelectionModel movieSelectionModel;

  public MovieGenresPanel(MovieSelectionModel model) {
    this.movieSelectionModel = model;
    setOpaque(false);

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String property = propertyChangeEvent.getPropertyName();
        Object source = propertyChangeEvent.getSource();

        // react on selection of a movie or change of genres
        if ((source.getClass() == MovieSelectionModel.class && "selectedMovie".equals(property))
            || (source.getClass() == Movie.class && "genre".equals(property))
            || (source.getClass() == Movie.class && Constants.VIDEO_IN_3D.equals(property))) {
          buildImages();
        }
      }
    };

    movieSelectionModel.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Builds the images.
   */
  private void buildImages() {
    removeAll();
    List<MediaGenres> genres = new ArrayList<MediaGenres>(movieSelectionModel.getSelectedMovie().getGenres());
    // first look for 3d
    if (movieSelectionModel.getSelectedMovie().isVideoIn3D()) {
      if (!genres.contains("3D")) {
        genres.add(0, MediaGenres.getGenre("3D"));
      }
    }

    for (MediaGenres genre : genres) {
      try {
        StringBuilder sb = new StringBuilder("/images/genres/");
        sb.append(genre.name().toLowerCase());
        sb.append(".png");
        Icon image = new ImageIcon(MovieGenresPanel.class.getResource(sb.toString()));
        JLabel lblImage = new JLabel(image);
        add(lblImage);
      }
      catch (NullPointerException e) {
        LOGGER.warn("genre image for genre " + genre.name() + " not available");
      }
      catch (Exception e) {
        LOGGER.warn(e.getMessage());
      }
    }
    // add unknown if there is no genre
    if (genres == null || genres.size() == 0) {
      try {
        Icon image = new ImageIcon(MovieGenresPanel.class.getResource("/images/genres/unknown.png"));
        JLabel lblImage = new JLabel(image);
        add(lblImage);
      }
      catch (Exception e) {
        LOGGER.warn(e.getMessage());
      }
    }
  }

}
