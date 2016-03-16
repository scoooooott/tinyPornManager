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

import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieActor;
import org.tinymediamanager.core.movie.entities.MovieProducer;
import org.tinymediamanager.ui.movies.AbstractMovieUIFilter;

/**
 * this class is used for a movie cast filter
 * 
 * @author Manuel Laggner
 */
public class MovieCastFilter extends AbstractMovieUIFilter {
  private JTextField textField;

  @Override
  public boolean accept(Movie movie) {
    String name = textField.getText();

    if (StringUtils.isBlank(name)) {
      return true;
    }

    Pattern pattern = Pattern.compile("(?i)" + Pattern.quote(name));
    java.util.regex.Matcher matcher = null;

    // director
    if (StringUtils.isNotEmpty(movie.getDirector())) {
      matcher = pattern.matcher(movie.getDirector());
      if (matcher.find()) {
        return true;
      }
    }

    // writer
    if (StringUtils.isNotEmpty(movie.getWriter())) {
      matcher = pattern.matcher(movie.getWriter());
      if (matcher.find()) {
        return true;
      }
    }

    // actors
    for (MovieActor cast : movie.getActors()) {
      if (StringUtils.isNotEmpty(cast.getName())) {
        matcher = pattern.matcher(cast.getName());
        if (matcher.find()) {
          return true;
        }
      }
    }

    // producers
    for (MovieProducer producer : movie.getProducers()) {
      if (StringUtils.isNotEmpty(producer.getName())) {
        matcher = pattern.matcher(producer.getName());
        if (matcher.find()) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new JLabel(BUNDLE.getString("movieextendedsearch.cast")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    textField = new JTextField();
    return textField;
  }
}
