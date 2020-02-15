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
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowChangeDatasourceDialog;

/**
 * the class {@link TvShowChangeDatasourceAction} - change the datasource of one or more TV shows (incl. moving)
 * 
 * @author Manuel Laggner
 */
public class TvShowChangeDatasourceAction extends TmmAction {
  private static final long           serialVersionUID = -2731782311579049379L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  public TvShowChangeDatasourceAction() {
    putValue(LARGE_ICON_KEY, IconManager.EDIT);
    putValue(SMALL_ICON, IconManager.EDIT);
    putValue(NAME, BUNDLE.getString("tvshow.changedatasource"));
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<TvShow> tvShows = new ArrayList<>(TvShowUIModule.getInstance().getSelectionModel().getSelectedTvShows());

    if (tvShows.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected"));
      return;
    }

    // show the window to choose the new datasource
    TvShowChangeDatasourceDialog dialog = new TvShowChangeDatasourceDialog(tvShows);
    dialog.setVisible(true);
  }
}
