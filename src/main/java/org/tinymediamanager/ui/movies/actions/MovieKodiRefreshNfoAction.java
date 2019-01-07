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
package org.tinymediamanager.ui.movies.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.thirdparty.KodiRPC;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.movies.MovieUIModule;

/**
 * Action to trigger a Kodi library refresh on selected items
 * 
 * @author Myron Boyle
 */
public class MovieKodiRefreshNfoAction extends TmmAction {
  private static final long           serialVersionUID = -6731682301579049379L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public MovieKodiRefreshNfoAction() {
    putValue(LARGE_ICON_KEY, IconManager.NFO);
    putValue(SMALL_ICON, IconManager.NFO);
    putValue(NAME, BUNDLE.getString("kodi.rpc.refreshnfo")); //$NON-NLS-1$
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<Movie> movies = new ArrayList<>(MovieUIModule.getInstance().getSelectionModel().getSelectedMovies());

    if (movies == null || movies.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected")); //$NON-NLS-1$
      return;
    }

    KodiRPC.getInstance().refreshMovieFromNfo(movies);
  }
}