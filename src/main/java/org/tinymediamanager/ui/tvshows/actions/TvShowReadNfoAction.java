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
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskHandle.TaskType;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.connector.TvShowNfoParser;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * The class TvShowReadNfoAction. To rewrite the NFOs of the selected TV shows
 * 
 * @author Manuel Laggner
 */
public class TvShowReadNfoAction extends TmmAction {
  private static final long           serialVersionUID = -6575156436788397648L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  public TvShowReadNfoAction() {
    putValue(NAME, BUNDLE.getString("tvshow.readnfo"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.readnfo.desc"));
  }

  @Override
  protected void processAction(ActionEvent e) {
    final List<TvShow> selectedTvShows = TvShowUIModule.getInstance().getSelectionModel().getSelectedTvShows();

    if (selectedTvShows.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected"));
      return;
    }

    // rewrite selected NFOs
    TmmTaskManager.getInstance().addUnnamedTask(new TmmTask(BUNDLE.getString("tvshow.rewritenfo"), selectedTvShows.size(), TaskType.BACKGROUND_TASK) {
      @Override
      protected void doInBackground() {
        int i = 0;
        for (TvShow tvShow : selectedTvShows) {
          TvShow tempTvShow = null;

          // process all registered NFOs
          for (MediaFile mf : tvShow.getMediaFiles(MediaFileType.NFO)) {
            // at the first NFO we get a movie object
            if (tempTvShow == null) {
              try {
                tempTvShow = TvShowNfoParser.parseNfo(mf.getFileAsPath()).toTvShow();
              }
              catch (Exception ignored) {
              }
              continue;
            }

            // every other NFO gets merged into that temp. movie object
            if (tempTvShow != null) {
              try {
                tempTvShow.merge(TvShowNfoParser.parseNfo(mf.getFileAsPath()).toTvShow());
              }
              catch (Exception ignored) {
              }
            }
          }

          // did we get movie data from our NFOs
          if (tempTvShow != null) {
            // force merge it to the actual movie object
            tvShow.forceMerge(tempTvShow);
            tvShow.saveToDb();
          }
          publishState(++i);
          if (cancel) {
            break;
          }
        }
      }
    });
  }
}
