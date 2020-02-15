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
package org.tinymediamanager.ui.moviesets.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.actions.TmmAction;

/**
 * @author Manuel Laggner
 * 
 */
public class MovieSetAddAction extends TmmAction {
  private static final long           serialVersionUID = 819724436270051906L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  /**
   * Instantiates a new adds the movie set action.
   */
  public MovieSetAddAction() {
    putValue(NAME, BUNDLE.getString("movieset.add.desc"));
    putValue(LARGE_ICON_KEY, IconManager.ADD);
    putValue(SMALL_ICON, IconManager.ADD);
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("movieset.add.desc"));
  }

  @Override
  protected void processAction(ActionEvent e) {
    String name = JOptionPane.showInputDialog(MainWindow.getActiveInstance(), BUNDLE.getString("movieset.title"), "", JOptionPane.QUESTION_MESSAGE);
    if (StringUtils.isNotEmpty(name)) {
      MovieSet movieSet = new MovieSet(name);
      movieSet.saveToDb();
      MovieList.getInstance().addMovieSet(movieSet);
    }
  }
}
