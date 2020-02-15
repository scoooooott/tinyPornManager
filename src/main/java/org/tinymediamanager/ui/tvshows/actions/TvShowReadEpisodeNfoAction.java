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
import org.tinymediamanager.core.tvshow.connector.TvShowEpisodeNfoParser;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * The class TvShowReadEpisodeNfoAction. Used to rewrite the NFOs for selected episodes
 * 
 * @author Manuel Laggner
 */
public class TvShowReadEpisodeNfoAction extends TmmAction {
  private static final long           serialVersionUID = 5762347331284295996L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  public TvShowReadEpisodeNfoAction() {
    putValue(NAME, BUNDLE.getString("tvshowepisode.readnfo"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshowepisode.readnfo.desc"));
  }

  @Override
  protected void processAction(ActionEvent e) {
    final List<TvShowEpisode> selectedEpisodes = TvShowUIModule.getInstance().getSelectionModel().getSelectedEpisodes();

    if (selectedEpisodes.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected"));
      return;
    }

    // rewrite selected NFOs
    TmmTaskManager.getInstance()
        .addUnnamedTask(new TmmTask(BUNDLE.getString("tvshowepisode.readnfo"), selectedEpisodes.size(), TaskType.BACKGROUND_TASK) {
          @Override
          protected void doInBackground() {
            int i = 0;
            for (TvShowEpisode episode : selectedEpisodes) {
              TvShowEpisode tempEpisode = null;

              // process all registered NFOs
              for (MediaFile mf : episode.getMediaFiles(MediaFileType.NFO)) {
                // at the first NFO we get a episode object
                if (tempEpisode == null) {
                  try {
                    List<TvShowEpisode> episodesFromNfo = TvShowEpisodeNfoParser.parseNfo(mf.getFileAsPath()).toTvShowEpisodes();
                    for (TvShowEpisode ep : episodesFromNfo) {
                      if (episode.getSeason() == ep.getSeason() && episode.getEpisode() == ep.getEpisode()) {
                        tempEpisode = ep;
                        break;
                      }
                    }
                  }
                  catch (Exception ignored) {
                  }
                  continue;
                }

                // every other NFO gets merged into that temp. episode object
                if (tempEpisode != null) {
                  try {
                    List<TvShowEpisode> episodesFromNfo = TvShowEpisodeNfoParser.parseNfo(mf.getFileAsPath()).toTvShowEpisodes();
                    for (TvShowEpisode ep : episodesFromNfo) {
                      if (episode.getSeason() == ep.getSeason() && episode.getEpisode() == ep.getEpisode()) {
                        tempEpisode.merge(ep);
                        break;
                      }
                    }
                  }
                  catch (Exception ignored) {
                  }
                }
              }

              // did we get movie data from our NFOs
              if (tempEpisode != null) {
                // force merge it to the actual movie object
                episode.forceMerge(tempEpisode);
                episode.saveToDb();
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
