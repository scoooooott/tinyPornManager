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
package org.tinymediamanager.ui.tvshows.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowSubtitleChooserDialog;

/**
 * The MovieSubtitleSearchAction - search for subtitles for all selected movies
 * 
 * @author Manuel Laggner
 */
public class TvShowSubtitleSearchAction extends AbstractAction {
  private static final long           serialVersionUID = -6006932119900795735L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TvShowSubtitleSearchAction() {
    putValue(NAME, BUNDLE.getString("tvshow.search.subtitle")); //$NON-NLS-1$
    putValue(SMALL_ICON, IconManager.SUBTITLE);
    putValue(LARGE_ICON_KEY, IconManager.SUBTITLE);
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.search.subtitle")); //$NON-NLS-1$
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    List<TvShowEpisode> episodes = TvShowUIModule.getInstance().getSelectionModel().getSelectedEpisodes();

    for (TvShowEpisode episode : episodes) {
      // no subtitle download for discs
      if (episode.isDisc()) {
        continue;
      }

      // show the dialog for every movie video file (multi part video files problem)
      for (MediaFile mediaFile : episode.getMediaFiles(MediaFileType.VIDEO)) {
        TvShowSubtitleChooserDialog dialogEpisodeSubtitleChooser = new TvShowSubtitleChooserDialog(episode, mediaFile, episodes.size() > 1);
        if (!dialogEpisodeSubtitleChooser.showDialog()) {
          return;
        }
      }
    }
  }
}
