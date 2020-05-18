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
package org.tinymediamanager.ui.tvshows.filters;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * This class implements a production company/studio filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowStudioFilter extends AbstractTvShowUIFilter {
  private JTextField textField;

  @Override
  public String getId() {
    return "tvShowStudio";
  }

  @Override
  public String getFilterValueAsString() {
    return textField.getText();
  }

  @Override
  public void setFilterValue(Object value) {
    if (value instanceof String) {
      textField.setText((String) value);
    }
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes, boolean invert) {
    String filterText = StrgUtils.normalizeString(textField.getText());
    if (StringUtils.isBlank(filterText)) {
      return true;
    }

    try {
      Pattern pattern = Pattern.compile(filterText, Pattern.CASE_INSENSITIVE);

      // first: filter on the production companies of the Tv show
      boolean foundShow = false;
      Matcher matcher = pattern.matcher(StrgUtils.normalizeString(tvShow.getProductionCompany()));
      if (matcher.find()) {
        foundShow = true;
      }

      // if we found anything in the show we can quit here
      if (!invert && foundShow) {
        return true;
      }
      else if (invert && foundShow) {
        return false;
      }

      // second: filter production company from the episodes
      for (TvShowEpisode episode : episodes) {
        boolean foundEpisode = false;

        matcher = pattern.matcher(StrgUtils.normalizeString(episode.getProductionCompany()));
        if (matcher.find()) {
          foundEpisode = true;
        }

        // if there is a match in this episode, we can stop
        if (invert && !foundEpisode) {
          return true;
        }
        else if (!invert && foundEpisode) {
          return true;
        }
      }
    }
    catch (Exception e) {
      // if any exceptions are thrown, just return true
      return true;
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("tvshowextendedsearch.studio"));
  }

  @Override
  protected JComponent createFilterComponent() {
    textField = new JTextField();
    return textField;
  }
}
