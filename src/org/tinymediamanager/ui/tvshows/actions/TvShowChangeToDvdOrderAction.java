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
package org.tinymediamanager.ui.tvshows.actions;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.AbstractAction;

import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * The class TvShowChangeToDvdOrderAction. To change to the dvd order after import
 * 
 * @author Manuel Laggner
 */
public class TvShowChangeToDvdOrderAction extends AbstractAction {
  private static final long           serialVersionUID = 8457297935386064655L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TvShowChangeToDvdOrderAction() {
    putValue(NAME, BUNDLE.getString("tvshow.changetodvdorder")); //$NON-NLS-1$
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.changeorder.desc")); //$NON-NLS-1$
    putValue(LARGE_ICON_KEY, IconManager.EDIT);
    putValue(SMALL_ICON, IconManager.EDIT);
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    List<Object> selectedObjects = TvShowUIModule.getInstance().getSelectionModel().getSelectedObjects();
    Set<TvShowEpisode> selectedEpisodes = new HashSet<TvShowEpisode>();

    for (Object obj : selectedObjects) {
      // display tv show editor
      if (obj instanceof TvShow) {
        TvShow tvShow = (TvShow) obj;
        for (TvShowEpisode ep : tvShow.getEpisodes()) {
          selectedEpisodes.add(ep);
        }
      }
      if (obj instanceof TvShowSeason) {
        TvShowSeason season = (TvShowSeason) obj;
        for (TvShowEpisode ep : season.getEpisodes()) {
          selectedEpisodes.add(ep);
        }
      }
      // display tv episode editor
      if (obj instanceof TvShowEpisode) {
        TvShowEpisode tvShowEpisode = (TvShowEpisode) obj;
        selectedEpisodes.add(tvShowEpisode);
      }
    }

    for (TvShowEpisode episode : selectedEpisodes) {
      if (!episode.isDvdOrder()) {
        episode.setDvdOrder(true);
        episode.setDvdSeason(episode.getAiredSeason());
        episode.setDvdEpisode(episode.getAiredEpisode());
        episode.setAiredEpisode(-1);
        episode.setAiredSeason(-1);
        episode.saveToDb();
      }
    }
  }
}