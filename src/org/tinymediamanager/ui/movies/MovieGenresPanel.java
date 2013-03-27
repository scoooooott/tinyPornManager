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
package org.tinymediamanager.ui.movies;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.scraper.MediaGenres;

/**
 * The Class MovieGenresPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieGenresPanel extends JPanel {

  /** The Constant serialVersionUID. */
  private static final long   serialVersionUID = -6585642654072040266L;

  /** The Constant LOGGER. */
  private static final Logger LOGGER           = Logger.getLogger(MovieGenresPanel.class);

  /** The model. */
  private MovieSelectionModel movieSelectionModel;

  /**
   * Instantiates a new movie genres panel.
   * 
   * @param model
   *          the model
   */
  public MovieGenresPanel(MovieSelectionModel model) {
    this.movieSelectionModel = model;
    setOpaque(false);

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String property = propertyChangeEvent.getPropertyName();
        Object source = propertyChangeEvent.getSource();

        // react on selection of a movie or change of genres
        if ((source.getClass() == MovieSelectionModel.class && "selectedMovie".equals(property))
            || (source.getClass() == Movie.class && "genre".equals(property))) {
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
    List<MediaGenres> genres = movieSelectionModel.getSelectedMovie().getGenres();
    for (MediaGenres genre : genres) {
      try {
        StringBuilder sb = new StringBuilder("/images/genres/");
        sb.append(genre.name().toLowerCase());
        sb.append(".png");
        Icon image = new ImageIcon(MovieGenresPanel.class.getResource(sb.toString()));
        JLabel lblImage = new JLabel(image);
        add(lblImage);
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
