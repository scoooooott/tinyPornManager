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
package org.tinymediamanager.ui.movies.filters;

import java.util.Calendar;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.movies.AbstractMovieUIFilter;

/**
 * this class is used for a year movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieYearFilter extends AbstractMovieUIFilter {
  JSpinner spinner;

  @Override
  public boolean accept(Movie movie) {
    Integer year = (Integer) spinner.getValue();
    if (movie.getYear().equals(year.toString())) {
      return true;
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new JLabel(BUNDLE.getString("metatag.year")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    int year = Calendar.getInstance().get(Calendar.YEAR);
    spinner = new JSpinner(new SpinnerNumberModel(year, 0, 3000, 1));
    spinner.setEditor(new JSpinner.NumberEditor(spinner, "#"));
    return spinner;
  }
}
