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
package org.tinymediamanager.ui.moviesets.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.moviesets.MovieSetUIModule;
import org.tinymediamanager.ui.moviesets.dialogs.MovieSetChooserDialog;

/**
 * @author Manuel Laggner
 * 
 */
public class MovieSetSearchAction extends AbstractAction {
  private static final long           serialVersionUID = -2260581786599155278L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /**
   * Instantiates a new search movie set action.
   */
  public MovieSetSearchAction(boolean withTitle) {
    if (withTitle) {
      putValue(NAME, BUNDLE.getString("movieset.search")); //$NON-NLS-1$
    }

    putValue(LARGE_ICON_KEY, IconManager.SEARCH);
    putValue(SMALL_ICON, IconManager.SEARCH);
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("movieset.search")); //$NON-NLS-1$
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    List<MovieSet> selectedMovieSets = MovieSetUIModule.getInstance().getSelectionModel().getSelectedMovieSets();

    for (MovieSet movieSet : selectedMovieSets) {
      MovieSetChooserDialog chooser = new MovieSetChooserDialog(movieSet, selectedMovieSets.size() > 1 ? true : false);
      if (!chooser.showDialog()) {
        break;
      }
    }
  }
}
