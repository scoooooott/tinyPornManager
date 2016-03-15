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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.tvshows.AbstractTvShowUIFilter;

/**
 * This class implements a tag filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowTagFilter extends AbstractTvShowUIFilter {
  private TvShowList        tvShowList = TvShowList.getInstance();

  private JComboBox<String> comboBox;

  public TvShowTagFilter() {
    super();
    buildAndInstallTagsArray();
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        buildAndInstallTagsArray();
      }
    };
    tvShowList.addPropertyChangeListener(Constants.TAG, propertyChangeListener);
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new JLabel(BUNDLE.getString("movieextendedsearch.tag")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    comboBox = new JComboBox<>();
    return comboBox;
  }

  private void buildAndInstallTagsArray() {
    comboBox.removeAllItems();
    Set<String> tags = new TreeSet<String>(tvShowList.getTagsInTvShows());
    tags.addAll(tvShowList.getTagsInEpisodes());
    for (String tag : tags) {
      comboBox.addItem(tag);
    }
  }

}
