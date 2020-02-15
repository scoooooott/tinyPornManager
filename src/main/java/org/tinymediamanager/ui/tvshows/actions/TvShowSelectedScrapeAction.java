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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.TvShowEpisodeScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.tasks.TvShowScrapeTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowScrapeMetadataDialog;

/**
 * The class TvShowSelectedScrapeAction. To scrape all selected TV shows
 * 
 * @author Manuel Laggner
 */
public class TvShowSelectedScrapeAction extends TmmAction {
  private static final long           serialVersionUID = 699165862194137592L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  public TvShowSelectedScrapeAction() {
    putValue(NAME, BUNDLE.getString("tvshow.scrape.selected.force"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.scrape.selected.force.desc"));
    putValue(LARGE_ICON_KEY, IconManager.SEARCH);
    putValue(SMALL_ICON, IconManager.SEARCH);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<TvShow> selectedTvShows = TvShowUIModule.getInstance().getSelectionModel().getSelectedTvShows();

    if (selectedTvShows.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected"));
      return;
    }

    TvShowScrapeMetadataDialog dialog = new TvShowScrapeMetadataDialog(BUNDLE.getString("tvshow.scrape.selected.force"));
    dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
    dialog.setVisible(true);

    // get options from dialog
    TvShowSearchAndScrapeOptions options = dialog.getTvShowSearchAndScrapeOptions();
    List<TvShowScraperMetadataConfig> tvShowScraperMetadataConfig = dialog.getTvShowScraperMetadataConfig();
    List<TvShowEpisodeScraperMetadataConfig> episodeScraperMetadataConfig = dialog.getTvShowEpisodeScraperMetadataConfig();

    // do we want to scrape?
    if (dialog.shouldStartScrape()) {
      // scrape
      TmmThreadPool scrapeTask = new TvShowScrapeTask(selectedTvShows, true, options, tvShowScraperMetadataConfig, episodeScraperMetadataConfig);
      TmmTaskManager.getInstance().addMainTask(scrapeTask);
    }
  }
}
