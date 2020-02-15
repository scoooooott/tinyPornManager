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
package org.tinymediamanager.ui.movies.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.movie.tasks.MovieFindMissingTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.actions.TmmAction;

/**
 * The class MovieFindMissingAction. To find missing movies on the datasources
 * 
 * @author Manuel Laggner
 * 
 */
public class MovieFindMissingAction extends TmmAction {
  private static final long           serialVersionUID = 7873846965534352231L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private String                      datasource       = null;

  /**
   * find missing movies in the given datasource
   * 
   * @param datasource
   *          the data source to find missing movies
   */
  public MovieFindMissingAction(String datasource) {
    this.datasource = datasource;
    setValues();
  }

  /**
   * find missing movies in all datasources
   */
  public MovieFindMissingAction() {
    setValues();
  }

  private void setValues() {
    if (StringUtils.isNotBlank(datasource)) {
      putValue(NAME, datasource);
    }
    else {
      putValue(NAME, BUNDLE.getString("movie.findmissing.all"));
    }
    putValue(SMALL_ICON, IconManager.SEARCH);
    putValue(LARGE_ICON_KEY, IconManager.SEARCH);
  }

  @Override
  protected void processAction(ActionEvent e) {
    TmmThreadPool task = null;
    if (StringUtils.isNotBlank(datasource)) {
      task = new MovieFindMissingTask(datasource);
    }
    else {
      task = new MovieFindMissingTask();
    }
    TmmTaskManager.getInstance().addMainTask(task);
  }
}
