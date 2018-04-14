/*
 * Copyright 2012 - 2018 Manuel Laggner
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
package org.tinymediamanager.ui.movies.filters;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.movies.AbstractMovieUIFilter;

/**
 * this class is used for a watched movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieCertificationFilter extends AbstractMovieUIFilter {
  private MovieList                movieList = MovieList.getInstance();

  private JComboBox<Certification> comboBox;

  public MovieCertificationFilter() {
    super();
    buildAndInstallCertificationArray();
    PropertyChangeListener propertyChangeListener = evt -> buildAndInstallCertificationArray();
    movieList.addPropertyChangeListener(Constants.AUDIO_CODEC, propertyChangeListener);
  }

  @Override
  public String getId() {
    return "movieCertification";
  }

  @Override
  public String getFilterValueAsString() {
    try {
      return ((Certification) comboBox.getSelectedItem()).name();
    }
    catch (Exception e) {
      return null;
    }
  }

  @Override
  public void setFilterValue(Object value) {
    if (value == null) {
      return;
    }
    if (value instanceof Certification) {
      comboBox.setSelectedItem(value);
    }
    else if (value instanceof String) {
      Certification certification = Certification.valueOf((String) value);
      if (certification != null) {
        comboBox.setSelectedItem(certification);
      }
    }
  }

  @Override
  public boolean accept(Movie movie) {
    Certification cert = (Certification) comboBox.getSelectedItem();
    return cert == movie.getCertification();
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    comboBox = new JComboBox<>(Certification.values());
    return comboBox;
  }

  private void buildAndInstallCertificationArray() {
    // remove the listener to not firing unnecessary events
    comboBox.removeActionListener(actionListener);

    Certification oldValue = (Certification) comboBox.getSelectedItem();
    comboBox.removeAllItems();

    List<Certification> certifications = new ArrayList<>(movieList.getCertificationsInMovies());
    Collections.sort(certifications);
    for (Certification cert : certifications) {
      comboBox.addItem(cert);
    }

    if (oldValue != null) {
      comboBox.setSelectedItem(oldValue);
    }

    // re-add the itemlistener
    comboBox.addActionListener(actionListener);
  }
}
