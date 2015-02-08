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

import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.moviesets.MovieSetUIModule;

/**
 * @author Manuel Laggner
 * 
 */
public class MovieSetRemoveAction extends AbstractAction {
  private static final long           serialVersionUID = -9030996266835702009L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /**
   * Instantiates a new removes the movie set action.
   */
  public MovieSetRemoveAction(boolean withTitle) {
    if (withTitle) {
      putValue(NAME, BUNDLE.getString("movieset.remove.desc")); //$NON-NLS-1$
    }
    putValue(LARGE_ICON_KEY, IconManager.LIST_REMOVE);
    putValue(SMALL_ICON, IconManager.LIST_REMOVE);
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("movieset.remove.desc")); //$NON-NLS-1$
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    List<MovieSet> selectedMovieSets = MovieSetUIModule.getInstance().getSelectionModel().getSelectedMovieSets();

    for (int i = 0; i < selectedMovieSets.size(); i++) {
      MovieSet movieSet = selectedMovieSets.get(i);
      MovieList.getInstance().removeMovieSet(movieSet);
    }

  }
}
