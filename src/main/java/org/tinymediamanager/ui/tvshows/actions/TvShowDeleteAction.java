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
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * The class TvShowDeleteAction. To remove selected elements and delete it from the data source
 * 
 * @author Manuel Laggner
 */
public class TvShowDeleteAction extends TmmAction {
  private static final long           serialVersionUID = -2355545751433709417L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  public TvShowDeleteAction() {
    putValue(NAME, BUNDLE.getString("tvshow.delete"));
    putValue(SMALL_ICON, IconManager.DELETE_FOREVER);
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.delete.hint"));
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<Object> selectedObjects = TvShowUIModule.getInstance().getSelectionModel().getSelectedObjects();

    // display warning and ask the user again
    Object[] options = { BUNDLE.getString("Button.yes"), BUNDLE.getString("Button.no") };
    int answer = JOptionPane.showOptionDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tvshow.delete.desc"),
        BUNDLE.getString("tvshow.delete"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
    if (answer != JOptionPane.YES_OPTION) {
      return;
    }

    if (selectedObjects.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected"));
      return;
    }

    for (Object obj : selectedObjects) {
      // delete a whole TV show
      if (obj instanceof TvShow) {
        TvShow tvShow = (TvShow) obj;
        TvShowList.getInstance().deleteTvShow(tvShow);
      }
      // delete a season
      if (obj instanceof TvShowSeason) {
        TvShowSeason season = (TvShowSeason) obj;
        List<TvShowEpisode> episodes = new ArrayList<>(season.getEpisodes());
        for (TvShowEpisode episode : episodes) {
          season.getTvShow().deleteEpisode(episode);
        }
      }
      // delete episodes
      if (obj instanceof TvShowEpisode) {
        TvShowEpisode tvShowEpisode = (TvShowEpisode) obj;
        tvShowEpisode.getTvShow().deleteEpisode(tvShowEpisode);
      }
    }
  }
}
