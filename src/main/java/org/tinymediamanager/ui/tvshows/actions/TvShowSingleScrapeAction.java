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
package org.tinymediamanager.ui.tvshows.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowChooserDialog;

/**
 * The Class TvShowSingleScrapeAction. Scrape with the TV show chooser dialog
 * 
 * @author Manuel Laggner
 */
public class TvShowSingleScrapeAction extends AbstractAction {
  private static final long           serialVersionUID = 641704453374845709L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TvShowSingleScrapeAction(boolean withTitle) {
    if (withTitle) {
      putValue(NAME, BUNDLE.getString("tvshow.scrape.selected")); //$NON-NLS-1$
    }
    putValue(LARGE_ICON_KEY, IconManager.SEARCH);
    putValue(SMALL_ICON, IconManager.SEARCH);
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.scrape.selected")); //$NON-NLS-1$
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    List<TvShow> selectedTvShows = TvShowUIModule.getInstance().getSelectionModel().getSelectedTvShows();

    for (TvShow tvShow : selectedTvShows) {
      // display tv show chooser
      TvShowChooserDialog chooser = new TvShowChooserDialog(tvShow, selectedTvShows.size() > 1 ? true : false);
      if (!chooser.showDialog()) {
        break;
      }
    }
  }
}