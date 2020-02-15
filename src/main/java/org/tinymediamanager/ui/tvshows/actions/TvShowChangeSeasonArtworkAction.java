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
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowSeasonEditorDialog;

/**
 * The Class TvShowChangeSeasonArtworkAction. To change the season artwork
 * 
 * @author Manuel Laggner
 */
public class TvShowChangeSeasonArtworkAction extends TmmAction {
  private static final long           serialVersionUID = 8356413227405772558L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  public TvShowChangeSeasonArtworkAction() {
    putValue(NAME, BUNDLE.getString("tvshow.changeseasonartwork"));
    putValue(LARGE_ICON_KEY, IconManager.EDIT);
    putValue(SMALL_ICON, IconManager.EDIT);
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.changeseasonartwork"));
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<Object> selectedObjects = TvShowUIModule.getInstance().getSelectionModel().getSelectedObjects();
    List<TvShowSeason> selectTvShowSeasons = new ArrayList<>();

    for (Object obj : selectedObjects) {
      // display image chooser
      if (obj instanceof TvShowSeason) {
        selectTvShowSeasons.add((TvShowSeason) obj);
      }
    }

    if (selectTvShowSeasons.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected"));
      return;
    }

    int selectedCount = selectTvShowSeasons.size();
    int index = 0;

    do {
      TvShowSeason season = selectTvShowSeasons.get(index);
      TvShowSeasonEditorDialog editor = new TvShowSeasonEditorDialog(season, index, selectedCount);
      editor.setVisible(true);
      if (!editor.isContinueQueue()) {
        break;
      }

      if (editor.isNavigateBack()) {
        index -= 1;
      }
      else {
        index += 1;
      }
    } while (index < selectedCount);
  }
}
