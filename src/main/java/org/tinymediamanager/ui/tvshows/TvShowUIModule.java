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
package org.tinymediamanager.ui.tvshows;

import java.awt.CardLayout;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.tinymediamanager.Globals;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.AbstractTmmUIModule;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.components.PopupMenuScroller;
import org.tinymediamanager.ui.movies.panels.TrailerPanel;
import org.tinymediamanager.ui.settings.TmmSettingsNode;
import org.tinymediamanager.ui.thirdparty.KodiRPCMenu;
import org.tinymediamanager.ui.tvshows.actions.DebugDumpShowAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowBulkEditAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowChangeDatasourceAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowChangeSeasonArtworkAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowChangeToAiredOrderAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowChangeToDvdOrderAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowCleanUpFilesAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowClearImageCacheAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowDeleteAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowDownloadMissingArtworkAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowEditAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowExportAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowMediaInformationAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowMissingEpisodeListAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowReadEpisodeNfoAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowReadNfoAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowRemoveAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowRenameAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowRewriteEpisodeNfoAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowRewriteNfoAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowScrapeEpisodesAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowScrapeMissingEpisodesAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowScrapeNewItemsAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowSelectedScrapeAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowSingleScrapeAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowSubtitleDownloadAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowSubtitleSearchAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowSyncSelectedTraktTvAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowSyncTraktTvAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowSyncWatchedTraktTvAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowToggleWatchedFlagAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowUpdateAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowUpdateDatasourcesAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowUpdateSingleDatasourceAction;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowFilterDialog;
import org.tinymediamanager.ui.tvshows.panels.TvShowTreePanel;
import org.tinymediamanager.ui.tvshows.panels.episode.TvShowEpisodeCastPanel;
import org.tinymediamanager.ui.tvshows.panels.episode.TvShowEpisodeInformationPanel;
import org.tinymediamanager.ui.tvshows.panels.episode.TvShowEpisodeMediaInformationPanel;
import org.tinymediamanager.ui.tvshows.panels.season.TvShowSeasonInformationPanel;
import org.tinymediamanager.ui.tvshows.panels.season.TvShowSeasonMediaFilesPanel;
import org.tinymediamanager.ui.tvshows.panels.tvshow.TvShowArtworkPanel;
import org.tinymediamanager.ui.tvshows.panels.tvshow.TvShowCastPanel;
import org.tinymediamanager.ui.tvshows.panels.tvshow.TvShowInformationPanel;
import org.tinymediamanager.ui.tvshows.panels.tvshow.TvShowMediaInformationPanel;
import org.tinymediamanager.ui.tvshows.settings.TvShowSettingsNode;

import net.miginfocom.swing.MigLayout;

public class TvShowUIModule extends AbstractTmmUIModule {
  private static final String       ID       = "tvShows";

  private static TvShowUIModule     instance = null;

  final TvShowSelectionModel        tvShowSelectionModel;
  final TvShowSeasonSelectionModel  tvShowSeasonSelectionModel;
  final TvShowEpisodeSelectionModel tvShowEpisodeSelectionModel;

  private final TvShowTreePanel     listPanel;
  private final JPanel              dataPanel;
  private final TvShowFilterDialog  tvShowFilterDialog;

  private TmmSettingsNode           settingsNode;

  private TvShowUIModule() {

    tvShowSelectionModel = new TvShowSelectionModel();
    tvShowSeasonSelectionModel = new TvShowSeasonSelectionModel();
    tvShowEpisodeSelectionModel = new TvShowEpisodeSelectionModel();

    listPanel = new TvShowTreePanel(tvShowSelectionModel);
    super.listPanel = listPanel;

    detailPanel = new JPanel();
    detailPanel.setOpaque(false);
    detailPanel.setLayout(new MigLayout("insets 0", "[grow]", "[grow]"));

    dataPanel = new JPanel();
    dataPanel.setOpaque(false);
    dataPanel.setLayout(new CardLayout());
    detailPanel.add(dataPanel, "cell 0 0, grow");

    // panel for TV shows
    JTabbedPane tvShowDetailPanel = new MainTabbedPane() {
      private static final long serialVersionUID = 3344548865608767661L;

      @Override
      public void updateUI() {
        putClientProperty("leftBorder", "half");
        putClientProperty("bottomBorder", Boolean.FALSE);
        super.updateUI();
      }
    };

    tvShowDetailPanel.add(BUNDLE.getString("metatag.details"), new TvShowInformationPanel(tvShowSelectionModel));
    tvShowDetailPanel.add(BUNDLE.getString("metatag.cast"), new TvShowCastPanel(tvShowSelectionModel));
    tvShowDetailPanel.add(BUNDLE.getString("metatag.mediafiles"), new TvShowMediaInformationPanel(tvShowSelectionModel));
    tvShowDetailPanel.add(BUNDLE.getString("metatag.artwork"), new TvShowArtworkPanel(tvShowSelectionModel));
    tvShowDetailPanel.add(BUNDLE.getString("metatag.trailer"), new TrailerPanel(tvShowSelectionModel));
    dataPanel.add(tvShowDetailPanel, "tvShow");

    // panel for seasons
    JTabbedPane tvShowSeasonDetailPanel = new MainTabbedPane() {
      private static final long serialVersionUID = 3134567895608767661L;

      @Override
      public void updateUI() {
        putClientProperty("leftBorder", "half");
        putClientProperty("bottomBorder", Boolean.FALSE);
        super.updateUI();
      }
    };
    tvShowSeasonDetailPanel.add(BUNDLE.getString("metatag.details"), new TvShowSeasonInformationPanel(tvShowSeasonSelectionModel));
    tvShowSeasonDetailPanel.add(BUNDLE.getString("metatag.mediafiles"), new TvShowSeasonMediaFilesPanel(tvShowSeasonSelectionModel));
    dataPanel.add(tvShowSeasonDetailPanel, "tvShowSeason");

    // panel for episodes
    JTabbedPane tvShowEpisodeDetailPanel = new MainTabbedPane() {
      private static final long serialVersionUID = 3344548905108767661L;

      @Override
      public void updateUI() {
        putClientProperty("leftBorder", "half");
        putClientProperty("bottomBorder", Boolean.FALSE);
        super.updateUI();
      }
    };

    tvShowEpisodeDetailPanel.add(BUNDLE.getString("metatag.details"), new TvShowEpisodeInformationPanel(tvShowEpisodeSelectionModel));
    tvShowEpisodeDetailPanel.add(BUNDLE.getString("metatag.cast"), new TvShowEpisodeCastPanel(tvShowEpisodeSelectionModel));
    tvShowEpisodeDetailPanel.add(BUNDLE.getString("metatag.mediafiles"), new TvShowEpisodeMediaInformationPanel(tvShowEpisodeSelectionModel));
    dataPanel.add(tvShowEpisodeDetailPanel, "tvShowEpisode");

    // glass pane for searching/filtering
    tvShowFilterDialog = new TvShowFilterDialog(listPanel.getTreeTable());

    // create actions and menus
    createActions();
    createPopupMenu();
    registerAccelerators();

    // build settings node
    settingsNode = new TvShowSettingsNode();

    // further initializations
    init();
  }

  private void init() {
    // re-set filters
    if (TvShowModuleManager.SETTINGS.isStoreUiFilters()) {
      SwingUtilities.invokeLater(() -> listPanel.getTreeTable().setFilterValues(TvShowModuleManager.SETTINGS.getUiFilters()));
    }
  }

  public static TvShowUIModule getInstance() {
    if (instance == null) {
      instance = new TvShowUIModule();
    }
    return instance;
  }

  public void setFilterDialogVisible(boolean selected) {
    tvShowFilterDialog.setVisible(selected);
  }

  @Override
  public String getModuleId() {
    return ID;
  }

  @Override
  public JPanel getTabPanel() {
    return listPanel;
  }

  @Override
  public String getTabTitle() {
    return BUNDLE.getString("tmm.tvshows");
  }

  public TvShowSelectionModel getSelectionModel() {
    return tvShowSelectionModel;
  }

  @Override
  public TmmSettingsNode getSettingsNode() {
    return settingsNode;
  }

  private void createActions() {
    searchAction = createAndRegisterAction(TvShowSingleScrapeAction.class);
    editAction = createAndRegisterAction(TvShowEditAction.class);
    updateAction = createAndRegisterAction(TvShowUpdateDatasourcesAction.class);
    renameAction = createAndRegisterAction(TvShowRenameAction.class);
  }

  private void createPopupMenu() {
    // popup menu
    popupMenu = new JPopupMenu();
    popupMenu.add(createAndRegisterAction(TvShowSingleScrapeAction.class));
    popupMenu.add(createAndRegisterAction(TvShowSelectedScrapeAction.class));
    popupMenu.add(createAndRegisterAction(TvShowScrapeEpisodesAction.class));
    popupMenu.add(createAndRegisterAction(TvShowScrapeNewItemsAction.class));
    popupMenu.add(createAndRegisterAction(TvShowScrapeMissingEpisodesAction.class));
    popupMenu.add(createAndRegisterAction(TvShowMissingEpisodeListAction.class));

    popupMenu.addSeparator();

    popupMenu.add(createAndRegisterAction(TvShowUpdateAction.class));

    popupMenu.addSeparator();

    popupMenu.add(createAndRegisterAction(TvShowEditAction.class));
    popupMenu.add(createAndRegisterAction(TvShowBulkEditAction.class));
    popupMenu.add(createAndRegisterAction(TvShowChangeDatasourceAction.class));
    popupMenu.add(createAndRegisterAction(TvShowChangeSeasonArtworkAction.class));
    popupMenu.add(createAndRegisterAction(TvShowToggleWatchedFlagAction.class));
    popupMenu.add(createAndRegisterAction(TvShowRewriteNfoAction.class));
    popupMenu.add(createAndRegisterAction(TvShowReadNfoAction.class));
    popupMenu.add(createAndRegisterAction(TvShowRewriteEpisodeNfoAction.class));
    popupMenu.add(createAndRegisterAction(TvShowReadEpisodeNfoAction.class));
    popupMenu.add(createAndRegisterAction(TvShowRenameAction.class));
    popupMenu.add(createAndRegisterAction(TvShowMediaInformationAction.class));
    popupMenu.add(createAndRegisterAction(TvShowExportAction.class));
    popupMenu.add(createAndRegisterAction(TvShowCleanUpFilesAction.class));
    popupMenu.add(createAndRegisterAction(TvShowClearImageCacheAction.class));

    popupMenu.addSeparator();

    popupMenu.add(createAndRegisterAction(TvShowDownloadMissingArtworkAction.class));
    popupMenu.add(createAndRegisterAction(TvShowSubtitleDownloadAction.class));
    popupMenu.add(createAndRegisterAction(TvShowSubtitleSearchAction.class));

    popupMenu.addSeparator();

    popupMenu.add(createAndRegisterAction(TvShowSyncTraktTvAction.class));
    popupMenu.add(createAndRegisterAction(TvShowSyncWatchedTraktTvAction.class));
    popupMenu.add(createAndRegisterAction(TvShowSyncSelectedTraktTvAction.class));
    JMenu kodiRPCMenu = KodiRPCMenu.KodiMenuRightClickTvShows();
    popupMenu.add(kodiRPCMenu);

    popupMenu.addSeparator();

    popupMenu.add(createAndRegisterAction(TvShowRemoveAction.class));
    popupMenu.add(createAndRegisterAction(TvShowDeleteAction.class));

    if (Globals.isDebug()) {
      final JMenu debugMenu = new JMenu("Debug");
      debugMenu.add(new DebugDumpShowAction());
      popupMenu.addSeparator();
      popupMenu.add(debugMenu);
    }

    listPanel.setPopupMenu(popupMenu);

    // update popup menu
    updatePopupMenu = new JPopupMenu();
    PopupMenuScroller.setScrollerFor(updatePopupMenu, 20, 25, 2, 2);
    updatePopupMenu.addPopupMenuListener(new PopupMenuListener() {
      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        updatePopupMenu.removeAll();
        updatePopupMenu.add(createAndRegisterAction(TvShowUpdateDatasourcesAction.class));
        updatePopupMenu.addSeparator();
        for (String ds : TvShowModuleManager.SETTINGS.getTvShowDataSource()) {
          updatePopupMenu.add(new TvShowUpdateSingleDatasourceAction(ds));
        }
        updatePopupMenu.addSeparator();
        updatePopupMenu.add(new TvShowUpdateAction());
        updatePopupMenu.pack();
      }

      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      }

      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {
      }
    });

    // scrape popup menu
    searchPopupMenu = new JPopupMenu();
    searchPopupMenu.add(createAndRegisterAction(TvShowSingleScrapeAction.class));
    searchPopupMenu.add(createAndRegisterAction(TvShowSelectedScrapeAction.class));
    searchPopupMenu.add(createAndRegisterAction(TvShowScrapeEpisodesAction.class));
    searchPopupMenu.add(createAndRegisterAction(TvShowScrapeNewItemsAction.class));
    searchPopupMenu.add(createAndRegisterAction(TvShowScrapeMissingEpisodesAction.class));

    // edit popupmenu
    editPopupMenu = new JPopupMenu();
    editPopupMenu.add(createAndRegisterAction(TvShowEditAction.class));
    editPopupMenu.add(createAndRegisterAction(TvShowBulkEditAction.class));
    editPopupMenu.add(createAndRegisterAction(TvShowChangeDatasourceAction.class));
    editPopupMenu.add(createAndRegisterAction(TvShowChangeSeasonArtworkAction.class));
    editPopupMenu.add(createAndRegisterAction(TvShowToggleWatchedFlagAction.class));
    editPopupMenu.add(createAndRegisterAction(TvShowRewriteNfoAction.class));
    editPopupMenu.add(createAndRegisterAction(TvShowReadNfoAction.class));
    editPopupMenu.add(createAndRegisterAction(TvShowRewriteEpisodeNfoAction.class));
    editPopupMenu.add(createAndRegisterAction(TvShowReadEpisodeNfoAction.class));
    editPopupMenu.add(createAndRegisterAction(TvShowChangeToDvdOrderAction.class));
    editPopupMenu.add(createAndRegisterAction(TvShowChangeToAiredOrderAction.class));
    editPopupMenu.addSeparator();
    editPopupMenu.add(createAndRegisterAction(TvShowExportAction.class));
    editPopupMenu.addSeparator();
    editPopupMenu.add(createAndRegisterAction(TvShowCleanUpFilesAction.class));
  }

  /**
   * set the selected TV shows. This causes the right sided panel to switch to the TV show information panel
   * 
   * @param tvShow
   *          the selected TV show
   */
  public void setSelectedTvShow(TvShow tvShow) {
    tvShowSelectionModel.setSelectedTvShow(tvShow);
    CardLayout cl = (CardLayout) (dataPanel.getLayout());
    cl.show(dataPanel, "tvShow");
  }

  /**
   * set the selected TV show season. This causes the right sided panel to switch to the season information panel
   * 
   * @param tvShowSeason
   *          the selected season
   */
  public void setSelectedTvShowSeason(TvShowSeason tvShowSeason) {
    tvShowSeasonSelectionModel.setSelectedTvShowSeason(tvShowSeason);
    CardLayout cl = (CardLayout) (dataPanel.getLayout());
    cl.show(dataPanel, "tvShowSeason");
  }

  /**
   * set the selected TV show episode. This cases the right sided panel to switch to the episode information panel
   * 
   * @param tvShowEpisode
   *          the selected episode
   */
  public void setSelectedTvShowEpisode(TvShowEpisode tvShowEpisode) {
    tvShowEpisodeSelectionModel.setSelectedTvShowEpisode(tvShowEpisode);
    CardLayout cl = (CardLayout) (dataPanel.getLayout());
    cl.show(dataPanel, "tvShowEpisode");
  }
}
