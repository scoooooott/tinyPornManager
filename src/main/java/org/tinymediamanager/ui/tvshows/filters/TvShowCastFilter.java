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
package org.tinymediamanager.ui.tvshows.filters;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.tvshows.AbstractTvShowUIFilter;

/**
 * This class implements a cast/crew filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowCastFilter extends AbstractTvShowUIFilter {
  private JTextField textField;

  @Override
  public String getId() {
    return "tvShowCast";
  }

  @Override
  public String getFilterValueAsString() {
    return textField.getText();
  }

  @Override
  public void setFilterValue(Object value) {
    if (value != null && value instanceof String) {
      textField.setText((String) value);
    }
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes) {
    String filterText = textField.getText();
    if (StringUtils.isBlank(filterText)) {
      return true;
    }

    Pattern pattern = Pattern.compile("(?i)" + Pattern.quote(filterText));

    // first: filter on the base cast of the TV show
    for (Person actor : tvShow.getActors()) {
      Matcher matcher = pattern.matcher(actor.getName());
      if (matcher.find()) {
        return true;
      }
    }

    // second: filter director/writer and guests from episodes
    for (TvShowEpisode episode : episodes) {
      for (Person director : episode.getDirectors()) {
        if (StringUtils.isNotBlank(director.getName())) {
          Matcher matcher = pattern.matcher(director.getName());
          if (matcher.find()) {
            return true;
          }
        }
      }
      for (Person writer : episode.getWriters()) {
        if (StringUtils.isNotBlank(writer.getName())) {
          Matcher matcher = pattern.matcher(writer.getName());
          if (matcher.find()) {
            return true;
          }
        }
      }
      for (Person actor : episode.getGuests()) {
        if (StringUtils.isNotBlank(actor.getName())) {
          Matcher matcher = pattern.matcher(actor.getName());
          if (matcher.find()) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("movieextendedsearch.cast")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    textField = new JTextField();
    return textField;
  }
}
