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
package org.tinymediamanager.ui.tvshows.filters;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowActor;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.tvshows.AbstractTvShowUIFilter;

/**
 * This class implements a cast/crew filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowCastFilter extends AbstractTvShowUIFilter {
  private JTextField textField;

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes) {
    String filterText = textField.getText();
    if (StringUtils.isBlank(filterText)) {
      return true;
    }

    Pattern pattern = Pattern.compile("(?i)" + Pattern.quote(filterText));

    // first: filter on the base cast of the TV show
    for (TvShowActor actor : tvShow.getActors()) {
      Matcher matcher = pattern.matcher(actor.getName());
      if (matcher.find()) {
        return true;
      }
    }

    // second: filter director/writer and guests from episodes
    for (TvShowEpisode episode : episodes) {
      Matcher matcher = pattern.matcher(episode.getDirector());
      if (matcher.find()) {
        return true;
      }
      matcher = pattern.matcher(episode.getWriter());
      if (matcher.find()) {
        return true;
      }
      for (TvShowActor actor : episode.getGuests()) {
        matcher = pattern.matcher(actor.getName());
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
