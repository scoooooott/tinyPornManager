/*
 * Copyright 2012 - 2017 Manuel Laggner
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.moviesets.MovieSetUIModule;

public class DebugDumpMovieSet extends AbstractAction {
  private static final long           serialVersionUID = -8473181347332963044L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public DebugDumpMovieSet() {
    putValue(NAME, BUNDLE.getString("debug.entity.dump")); //$NON-NLS-1$
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("debug.entity.dump.desc")); //$NON-NLS-1$
    // putValue(LARGE_ICON_KEY, Debug.EDIT);
    // putValue(SMALL_ICON, Debug.EDIT);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    List<MovieSet> selectedMovieSets = new ArrayList<>(MovieSetUIModule.getInstance().getSelectionModel().getSelectedMovieSets());
    for (MovieSet ms : selectedMovieSets) {
      MovieModuleManager.getInstance().dump(ms);
    }
  }
}
