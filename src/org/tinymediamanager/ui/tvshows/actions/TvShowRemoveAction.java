/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowEpisode;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * The class TvShowRemoveAction. To remove selected elements
 * 
 * @author Manuel Laggner
 */
public class TvShowRemoveAction extends AbstractAction {
  private static final long           serialVersionUID = -2355545751433709417L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TvShowRemoveAction(boolean withTitle) {
    if (withTitle) {
      putValue(NAME, BUNDLE.getString("tvshow.remove")); //$NON-NLS-1$
    }
    putValue(LARGE_ICON_KEY, IconManager.CROSS);
    putValue(SMALL_ICON, IconManager.CROSS);
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.remove")); //$NON-NLS-1$
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    List<Object> selectedObjects = TvShowUIModule.getInstance().getSelectionModel().getSelectedObjects();

    for (Object obj : selectedObjects) {
      // display tv show editor
      if (obj instanceof TvShow) {
        TvShow tvShow = (TvShow) obj;
        TvShowList.getInstance().removeTvShow(tvShow);
      }
      // display tv episode editor
      if (obj instanceof TvShowEpisode) {
        TvShowEpisode tvShowEpisode = (TvShowEpisode) obj;
        tvShowEpisode.getTvShow().removeEpisode(tvShowEpisode);
      }
    }
  }
}