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
package org.tinymediamanager.ui.tvshows.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JOptionPane;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.thirdparty.KodiRPC;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * The Class TvShowEditAction. To edit TV shows/episodes
 * 
 * @author Manuel Laggner
 */
public class TvShowKodiRefreshNfoAction extends TmmAction {
  private static final long           serialVersionUID = -3911290901017607679L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  public TvShowKodiRefreshNfoAction() {
    putValue(LARGE_ICON_KEY, IconManager.NFO);
    putValue(SMALL_ICON, IconManager.NFO);
    putValue(NAME, BUNDLE.getString("kodi.rpc.refreshnfo"));
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<Object> selectedObjects = TvShowUIModule.getInstance().getSelectionModel().getSelectedObjects();

    if (selectedObjects.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected"));
      return;
    }

    List<TvShowEpisode> eps = new ArrayList<>();
    Set<TvShow> shows = new HashSet<>();

    // if we specify show, we want it recursive for all episodes
    // so remove all single episode calls to not sent them twice...
    for (Object obj : selectedObjects) {
      if (obj instanceof TvShow) {
        TvShow show = (TvShow) obj;
        shows.add(show);
      }
      if (obj instanceof TvShowEpisode) {
        TvShowEpisode episode = (TvShowEpisode) obj;
        eps.add(episode);
      }
      if (obj instanceof TvShowSeason) {
        TvShowSeason season = (TvShowSeason) obj;
        eps.addAll(season.getEpisodes());
      }
    }

    // remove all EPs, where we already have the show
    for (int i = eps.size() - 1; i >= 0; i--) {
      TvShowEpisode ep = eps.get(i);
      if (shows.contains(ep.getTvShow())) {
        eps.remove(i);
      }
    }

    // update show + all EPs
    for (Object obj : shows) {
      MediaEntity me = (MediaEntity) obj;
      KodiRPC.getInstance().refreshFromNfo(me);
    }
    // update single EP only
    for (Object obj : eps) {
      MediaEntity me = (MediaEntity) obj;
      KodiRPC.getInstance().refreshFromNfo(me);
    }

  }
}
