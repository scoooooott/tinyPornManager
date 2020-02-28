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
package org.tinymediamanager.ui.movies;

import java.awt.CardLayout;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.thirdparty.KodiRPC;
import org.tinymediamanager.ui.AbstractTmmUIModule;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.components.PopupMenuScroller;
import org.tinymediamanager.ui.movies.actions.DebugDumpMovieAction;
import org.tinymediamanager.ui.movies.actions.MovieAssignMovieSetAction;
import org.tinymediamanager.ui.movies.actions.MovieBatchEditAction;
import org.tinymediamanager.ui.movies.actions.MovieChangeDatasourceAction;
import org.tinymediamanager.ui.movies.actions.MovieCleanUpFilesAction;
import org.tinymediamanager.ui.movies.actions.MovieClearImageCacheAction;
import org.tinymediamanager.ui.movies.actions.MovieCreateOfflineAction;
import org.tinymediamanager.ui.movies.actions.MovieDeleteAction;
import org.tinymediamanager.ui.movies.actions.MovieDownloadMissingArtworkAction;
import org.tinymediamanager.ui.movies.actions.MovieEditAction;
import org.tinymediamanager.ui.movies.actions.MovieExportAction;
import org.tinymediamanager.ui.movies.actions.MovieFindMissingAction;
import org.tinymediamanager.ui.movies.actions.MovieMediaInformationAction;
import org.tinymediamanager.ui.movies.actions.MovieReadNfoAction;
import org.tinymediamanager.ui.movies.actions.MovieRebuildImageCacheAction;
import org.tinymediamanager.ui.movies.actions.MovieRemoveAction;
import org.tinymediamanager.ui.movies.actions.MovieRenameAction;
import org.tinymediamanager.ui.movies.actions.MovieRenamePreviewAction;
import org.tinymediamanager.ui.movies.actions.MovieRewriteNfoAction;
import org.tinymediamanager.ui.movies.actions.MovieSelectedScrapeAction;
import org.tinymediamanager.ui.movies.actions.MovieSelectedScrapeMetadataAction;
import org.tinymediamanager.ui.movies.actions.MovieSingleScrapeAction;
import org.tinymediamanager.ui.movies.actions.MovieSubtitleDownloadAction;
import org.tinymediamanager.ui.movies.actions.MovieSubtitleSearchAction;
import org.tinymediamanager.ui.movies.actions.MovieSyncSelectedTraktTvAction;
import org.tinymediamanager.ui.movies.actions.MovieSyncTraktTvAction;
import org.tinymediamanager.ui.movies.actions.MovieSyncWatchedTraktTvAction;
import org.tinymediamanager.ui.movies.actions.MovieToggleWatchedFlagAction;
import org.tinymediamanager.ui.movies.actions.MovieTrailerDownloadAction;
import org.tinymediamanager.ui.movies.actions.MovieUnscrapedScrapeAction;
import org.tinymediamanager.ui.movies.actions.MovieUpdateAction;
import org.tinymediamanager.ui.movies.actions.MovieUpdateDatasourceAction;
import org.tinymediamanager.ui.movies.actions.MovieUpdateSingleDatasourceAction;
import org.tinymediamanager.ui.movies.dialogs.MovieFilterDialog;
import org.tinymediamanager.ui.movies.panels.MovieArtworkPanel;
import org.tinymediamanager.ui.movies.panels.MovieCastPanel;
import org.tinymediamanager.ui.movies.panels.MovieInformationPanel;
import org.tinymediamanager.ui.movies.panels.MovieListPanel;
import org.tinymediamanager.ui.movies.panels.MovieMediaInformationPanel;
import org.tinymediamanager.ui.movies.panels.TrailerPanel;
import org.tinymediamanager.ui.movies.settings.MovieSettingsNode;
import org.tinymediamanager.ui.settings.TmmSettingsNode;
import org.tinymediamanager.ui.thirdparty.KodiRPCMenu;

import net.miginfocom.swing.MigLayout;

/**
 * The class MovieUIModule is the general access point to all movie related UI operations
 *
 * @author Manuel Laggner
 */
public class MovieUIModule extends AbstractTmmUIModule {
  private static final String       ID       = "movies";

  private static MovieUIModule      instance = null;

  private final MovieListPanel      listPanel;

  private final MovieSelectionModel selectionModel;

  private TmmSettingsNode           settingsNode;

  private final MovieFilterDialog   movieFilterDialog;

  private MovieUIModule() {

    listPanel = new MovieListPanel();
    selectionModel = listPanel.getSelectionModel();

    super.listPanel = listPanel;

    detailPanel = new JPanel();
    detailPanel.setOpaque(false);
    detailPanel.setLayout(new MigLayout("insets 0", "[grow]", "[grow]"));

    // need this panel for layouting
    JPanel dataPanel = new JPanel();
    dataPanel.setOpaque(false);
    dataPanel.setLayout(new CardLayout());
    detailPanel.add(dataPanel, "cell 0 0, grow");

    // tabbed pane containing the movie data
    JTabbedPane tabbedPane = new MainTabbedPane() {
      private static final long serialVersionUID = 1234548865608767661L;

      @Override
      public void updateUI() {
        putClientProperty("leftBorder", "half");
        putClientProperty("bottomBorder", Boolean.FALSE);
        super.updateUI();
      }
    };

    tabbedPane.add(BUNDLE.getString("metatag.details"), new MovieInformationPanel(selectionModel));
    tabbedPane.add(BUNDLE.getString("metatag.cast"), new MovieCastPanel(selectionModel));
    tabbedPane.add(BUNDLE.getString("metatag.mediafiles"), new MovieMediaInformationPanel(selectionModel));
    tabbedPane.add(BUNDLE.getString("metatag.artwork"), new MovieArtworkPanel(selectionModel));
    tabbedPane.add(BUNDLE.getString("metatag.trailer"), new TrailerPanel(selectionModel));
    dataPanel.add(tabbedPane);

    movieFilterDialog = new MovieFilterDialog(selectionModel);

    createActions();
    createPopupMenu();
    registerAccelerators();

    // settings node
    settingsNode = new MovieSettingsNode();

    // further initializations
    init();
  }

  private void init() {
    // apply stored UI filters
    if (MovieModuleManager.SETTINGS.isStoreUiFilters()) {
      SwingUtilities.invokeLater(() -> {
        MovieList.getInstance().searchDuplicates();
        selectionModel.setFilterValues(MovieModuleManager.SETTINGS.getUiFilters());
      });
    }

    // init the table panel
    listPanel.init();
  }

  public static MovieUIModule getInstance() {
    if (instance == null) {
      instance = new MovieUIModule();
    }
    return instance;
  }

  public MovieSelectionModel getSelectionModel() {
    return selectionModel;
  }

  private void createActions() {
    searchAction = createAndRegisterAction(MovieSingleScrapeAction.class);
    editAction = createAndRegisterAction(MovieEditAction.class);
    updateAction = createAndRegisterAction(MovieUpdateDatasourceAction.class);
    renameAction = createAndRegisterAction(MovieRenameAction.class);
  }

  private void createPopupMenu() {
    popupMenu = new JPopupMenu();
    popupMenu.add(createAndRegisterAction(MovieSingleScrapeAction.class));
    popupMenu.add(createAndRegisterAction(MovieSelectedScrapeAction.class));
    popupMenu.add(createAndRegisterAction(MovieUnscrapedScrapeAction.class));
    popupMenu.add(createAndRegisterAction(MovieSelectedScrapeMetadataAction.class));
    popupMenu.add(createAndRegisterAction(MovieAssignMovieSetAction.class));
    popupMenu.add(createAndRegisterAction(MovieDownloadMissingArtworkAction.class));
    popupMenu.addSeparator();
    popupMenu.add(createAndRegisterAction(MovieUpdateAction.class));
    popupMenu.addSeparator();
    popupMenu.add(createAndRegisterAction(MovieEditAction.class));
    popupMenu.add(createAndRegisterAction(MovieBatchEditAction.class));
    popupMenu.add(createAndRegisterAction(MovieChangeDatasourceAction.class));
    popupMenu.add(createAndRegisterAction(MovieToggleWatchedFlagAction.class));
    popupMenu.add(createAndRegisterAction(MovieRewriteNfoAction.class));
    popupMenu.add(createAndRegisterAction(MovieReadNfoAction.class));
    popupMenu.add(createAndRegisterAction(MovieRenameAction.class));
    popupMenu.add(createAndRegisterAction(MovieRenamePreviewAction.class));
    popupMenu.add(createAndRegisterAction(MovieMediaInformationAction.class));
    popupMenu.add(createAndRegisterAction(MovieExportAction.class));
    popupMenu.addSeparator();
    popupMenu.add(createAndRegisterAction(MovieTrailerDownloadAction.class));
    popupMenu.add(createAndRegisterAction(MovieSubtitleSearchAction.class));
    popupMenu.add(createAndRegisterAction(MovieSubtitleDownloadAction.class));
    popupMenu.addSeparator();
    popupMenu.add(createAndRegisterAction(MovieSyncTraktTvAction.class));
    popupMenu.add(createAndRegisterAction(MovieSyncWatchedTraktTvAction.class));
    popupMenu.add(createAndRegisterAction(MovieSyncSelectedTraktTvAction.class));
    JMenu kodiRPCMenu = KodiRPCMenu.KodiMenuRightClickMovies();
    popupMenu.add(kodiRPCMenu);
    popupMenu.addSeparator();
    popupMenu.add(createAndRegisterAction(MovieCleanUpFilesAction.class));
    popupMenu.add(createAndRegisterAction(MovieClearImageCacheAction.class));
    popupMenu.add(createAndRegisterAction(MovieRebuildImageCacheAction.class));
    popupMenu.addSeparator();
    popupMenu.add(createAndRegisterAction(MovieRemoveAction.class));
    popupMenu.add(createAndRegisterAction(MovieDeleteAction.class));

    if (Globals.isDebug()) {
      final JMenu debugMenu = new JMenu("Debug");
      debugMenu.add(new DebugDumpMovieAction());
      popupMenu.addSeparator();
      popupMenu.add(debugMenu);
    }

    // activate/deactivate menu items based on some status
    popupMenu.addPopupMenuListener(new PopupMenuListener() {
      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        kodiRPCMenu.setText(KodiRPC.getInstance().getVersion());
        if (KodiRPC.getInstance().isConnected()) {
          kodiRPCMenu.setEnabled(true);
        }
        else {
          kodiRPCMenu.setEnabled(false);
        }
      }

      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      }

      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {
      }
    });

    listPanel.setPopupMenu(popupMenu);

    // update popup menu
    updatePopupMenu = new JPopupMenu();
    PopupMenuScroller.setScrollerFor(updatePopupMenu, 20, 25, 2, 5);
    updatePopupMenu.addPopupMenuListener(new PopupMenuListener() {
      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        updatePopupMenu.removeAll();
        updatePopupMenu.add(createAndRegisterAction(MovieUpdateDatasourceAction.class));
        updatePopupMenu.addSeparator();
        for (String ds : MovieModuleManager.SETTINGS.getMovieDataSource()) {
          updatePopupMenu.add(new MovieUpdateSingleDatasourceAction(ds));
        }
        updatePopupMenu.addSeparator();
        updatePopupMenu.add(createAndRegisterAction(MovieUpdateAction.class));
        updatePopupMenu.addSeparator();
        updatePopupMenu.add(createAndRegisterAction(MovieFindMissingAction.class));
        updatePopupMenu.add(createAndRegisterAction(MovieCreateOfflineAction.class));
        updatePopupMenu.pack();
      }

      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      }

      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {
      }
    });

    // search popup menu
    searchPopupMenu = new JPopupMenu();
    searchPopupMenu.add(createAndRegisterAction(MovieSingleScrapeAction.class));
    searchPopupMenu.add(createAndRegisterAction(MovieSelectedScrapeAction.class));
    searchPopupMenu.add(createAndRegisterAction(MovieUnscrapedScrapeAction.class));
    searchPopupMenu.add(createAndRegisterAction(MovieSelectedScrapeMetadataAction.class));

    // edit popup menu
    editPopupMenu = new JPopupMenu();
    editPopupMenu.add(createAndRegisterAction(MovieEditAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieBatchEditAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieChangeDatasourceAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieToggleWatchedFlagAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieRewriteNfoAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieReadNfoAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieRenameAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieRenamePreviewAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieMediaInformationAction.class));
    editPopupMenu.addSeparator();
    editPopupMenu.add(createAndRegisterAction(MovieSyncTraktTvAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieSyncSelectedTraktTvAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieSyncWatchedTraktTvAction.class));
    editPopupMenu.addSeparator();
    editPopupMenu.add(createAndRegisterAction(MovieCleanUpFilesAction.class));
    editPopupMenu.addSeparator();
    editPopupMenu.add(createAndRegisterAction(MovieExportAction.class));

    // rename popup menu
    renamePopupMenu = new JPopupMenu();
    renamePopupMenu.add(createAndRegisterAction(MovieRenameAction.class));
    renamePopupMenu.add(createAndRegisterAction(MovieRenamePreviewAction.class));
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
    return BUNDLE.getString("tmm.movies");
  }

  @Override
  public JPanel getDetailPanel() {
    return detailPanel;
  }

  @Override
  public Action getSearchAction() {
    return searchAction;
  }

  @Override
  public JPopupMenu getSearchMenu() {
    return searchPopupMenu;
  }

  @Override
  public Action getEditAction() {
    return editAction;
  }

  @Override
  public JPopupMenu getEditMenu() {
    return editPopupMenu;
  }

  @Override
  public Action getUpdateAction() {
    return updateAction;
  }

  @Override
  public JPopupMenu getUpdateMenu() {
    return updatePopupMenu;
  }

  @Override
  public Action getRenameAction() {
    return renameAction;
  }

  @Override
  public JPopupMenu getRenameMenu() {
    return renamePopupMenu;
  }

  @Override
  public TmmSettingsNode getSettingsNode() {
    return settingsNode;
  }

  public void setFilterDialogVisible(boolean selected) {
    movieFilterDialog.setVisible(selected);
  }
}
