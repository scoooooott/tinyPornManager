/*
 * Copyright 2012 - 2019 Manuel Laggner
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

import javax.swing.JLabel;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * this class is used for a watched movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieCertificationFilter extends AbstractCheckComboBoxMovieUIFilter<Certification> {
  private MovieList movieList = MovieList.getInstance();

  public MovieCertificationFilter() {
    super();
    buildAndInstallCertificationArray();
    PropertyChangeListener propertyChangeListener = evt -> buildAndInstallCertificationArray();
    movieList.addPropertyChangeListener(Constants.CERTIFICATION, propertyChangeListener);
  }

  @Override
  public String getId() {
    return "movieCertification";
  }

  @Override
  public boolean accept(Movie movie) {
    List<Certification> selectedItems = checkComboBox.getSelectedItems();
    return selectedItems.contains(movie.getCertification());
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
  }

  private void buildAndInstallCertificationArray() {
    List<Certification> certifications = new ArrayList<>(movieList.getCertificationsInMovies());
    Collections.sort(certifications);

    setValues(certifications);
  }

  @Override
  protected String parseTypeToString(Certification type) throws Exception {
    return type.name();
  }

  @Override
  protected Certification parseStringToType(String string) throws Exception {
    return Certification.valueOf(string);
  }
}
