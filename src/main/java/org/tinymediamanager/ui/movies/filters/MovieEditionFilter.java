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
package org.tinymediamanager.ui.movies.filters;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.movie.MovieEdition;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * this class is used for a movie edition filter
 * 
 * @author Manuel Laggner
 */
public class MovieEditionFilter extends AbstractCheckComboBoxMovieUIFilter<MovieEdition> {
  private final MovieEdition.MovieEditionComparator comparator;

  public MovieEditionFilter() {
    super();
    comparator = new MovieEdition.MovieEditionComparator();
    buildAndInstallEditionArray();
    MovieEdition.addListener(evt -> SwingUtilities.invokeLater(this::buildAndInstallEditionArray));
  }

  @Override
  public String getId() {
    return "movieCertification";
  }

  @Override
  public boolean accept(Movie movie) {
    List<MovieEdition> selectedItems = checkComboBox.getSelectedItems();
    return selectedItems.contains(movie.getEdition());
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.edition"));
  }

  private void buildAndInstallEditionArray() {
    List<MovieEdition> editions = new ArrayList<>();

    for (MovieEdition movieEdition : MovieEdition.values()) {
      if (StringUtils.isNotBlank(movieEdition.toString())) {
        editions.add(movieEdition);
      }
    }

    editions.sort(comparator);
    setValues(editions);
  }

  @Override
  protected String parseTypeToString(MovieEdition type) throws Exception {
    return type.name();
  }

  @Override
  protected MovieEdition parseStringToType(String string) throws Exception {
    return MovieEdition.getMovieEditionFromString(string);
  }
}
