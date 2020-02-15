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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowMissingEpisodeListDialog;

public class TvShowMissingEpisodeListAction extends TmmAction {

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

  public TvShowMissingEpisodeListAction() {

    putValue(NAME, BUNDLE.getString("tvshow.missingepisodelist"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.missingepisodelist.desc"));
    putValue(LARGE_ICON_KEY, IconManager.SEARCH);
    putValue(SMALL_ICON, IconManager.SEARCH);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
  }

  @Override
  protected void processAction(ActionEvent e) {

    List<Object> selectedObjects = TvShowUIModule.getInstance().getSelectionModel().getSelectedObjects();
    List<TvShow> selectedTvShows = new ArrayList<>();

    for (Object obj : selectedObjects) {
      // get all selected TV Shows
      if (obj instanceof TvShow) {
        TvShow tvShow = (TvShow) obj;
        selectedTvShows.add(tvShow);
      }
      // get all selected Seasons
      if (obj instanceof TvShowSeason) {
        TvShowSeason tvShowSeason = (TvShowSeason) obj;
        if (!selectedTvShows.contains(tvShowSeason.getTvShow())) {
          selectedTvShows.add(tvShowSeason.getTvShow());
        }
      }
    }

    if (selectedTvShows.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected"));
      return;
    }

    TvShowMissingEpisodeListDialog dialog = new TvShowMissingEpisodeListDialog(selectedTvShows);
    dialog.setVisible(true);

  }

}
