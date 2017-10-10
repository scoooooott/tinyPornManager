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
package org.tinymediamanager.ui.movies;

import java.awt.CardLayout;

import javax.swing.Action;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.ui.AbstractTmmUIModule;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.movies.actions.MovieAssignMovieSetAction;
import org.tinymediamanager.ui.movies.actions.MovieBatchEditAction;
import org.tinymediamanager.ui.movies.actions.MovieClearImageCacheAction;
import org.tinymediamanager.ui.movies.actions.MovieDeleteAction;
import org.tinymediamanager.ui.movies.actions.MovieDownloadMissingArtworkAction;
import org.tinymediamanager.ui.movies.actions.MovieEditAction;
import org.tinymediamanager.ui.movies.actions.MovieExportAction;
import org.tinymediamanager.ui.movies.actions.MovieMediaInformationAction;
import org.tinymediamanager.ui.movies.actions.MovieReadNfoAction;
import org.tinymediamanager.ui.movies.actions.MovieRemoveAction;
import org.tinymediamanager.ui.movies.actions.MovieRenameAction;
import org.tinymediamanager.ui.movies.actions.MovieRenamePreviewAction;
import org.tinymediamanager.ui.movies.actions.MovieRewriteNfoAction;
import org.tinymediamanager.ui.movies.actions.MovieSelectedScrapeAction;
import org.tinymediamanager.ui.movies.actions.MovieSelectedScrapeMetadataAction;
import org.tinymediamanager.ui.movies.actions.MovieSetWatchedFlagAction;
import org.tinymediamanager.ui.movies.actions.MovieSingleScrapeAction;
import org.tinymediamanager.ui.movies.actions.MovieSubtitleDownloadAction;
import org.tinymediamanager.ui.movies.actions.MovieSubtitleSearchAction;
import org.tinymediamanager.ui.movies.actions.MovieSyncTraktTvAction;
import org.tinymediamanager.ui.movies.actions.MovieSyncWatchedTraktTvAction;
import org.tinymediamanager.ui.movies.actions.MovieTrailerDownloadAction;
import org.tinymediamanager.ui.movies.actions.MovieUnscrapedScrapeAction;
import org.tinymediamanager.ui.movies.actions.MovieUpdateDatasourceAction;
import org.tinymediamanager.ui.movies.actions.MovieUpdateSingleDatasourceAction;
import org.tinymediamanager.ui.movies.panels.MovieArtworkPanel;
import org.tinymediamanager.ui.movies.panels.MovieCastPanel;
import org.tinymediamanager.ui.movies.panels.MovieExtendedSearchPanel;
import org.tinymediamanager.ui.movies.panels.MovieInformationPanel;
import org.tinymediamanager.ui.movies.panels.MovieListPanel;
import org.tinymediamanager.ui.movies.panels.MovieMediaInformationPanel;
import org.tinymediamanager.ui.movies.panels.MovieTrailerPanel;
import org.tinymediamanager.ui.movies.settings.MovieDatasourceSettingsPanel;
import org.tinymediamanager.ui.movies.settings.MovieImageExtraPanel;
import org.tinymediamanager.ui.movies.settings.MovieImageSettingsPanel;
import org.tinymediamanager.ui.movies.settings.MovieImageTypeSettingsPanel;
import org.tinymediamanager.ui.movies.settings.MovieRenamerSettingsPanel;
import org.tinymediamanager.ui.movies.settings.MovieScraperNfoSettingsPanel;
import org.tinymediamanager.ui.movies.settings.MovieScraperOptionsSettingsPanel;
import org.tinymediamanager.ui.movies.settings.MovieScraperSettingsPanel;
import org.tinymediamanager.ui.movies.settings.MovieSettingsPanel;
import org.tinymediamanager.ui.movies.settings.MovieSubtitleSettingsPanel;
import org.tinymediamanager.ui.movies.settings.MovieTrailerSettingsPanel;
import org.tinymediamanager.ui.settings.TmmSettingsNode;

import net.miginfocom.swing.MigLayout;

/**
 * The class MovieUIModule is the general access point to all movie related UI operations
 *
 * @author Manuel Laggner
 */
public class MovieUIModule extends AbstractTmmUIModule {
  private final static String            ID       = "movies";

  private static MovieUIModule           instance = null;

  private final MovieListPanel           listPanel;
  private final MovieExtendedSearchPanel filterPanel;

  private final MovieSelectionModel      selectionModel;

  private TmmSettingsNode                settingsNode;

  private MovieUIModule() {

    listPanel = new MovieListPanel();
    selectionModel = listPanel.getSelectionModel();

    super.listPanel = listPanel;

    detailPanel = new JPanel();
    detailPanel.setOpaque(false);
    detailPanel.setLayout(new MigLayout("insets 0", "[grow]", "[grow]"));

    // layeredpane for displaying the filter dialog at the top
    JLayeredPane layeredPane = new JLayeredPane();
    layeredPane.setLayout(new MigLayout("insets 0", "[grow]", "[grow]"));
    detailPanel.add(layeredPane, "cell 0 0, grow");

    // need this panel for layouting
    JPanel dataPanel = new JPanel();
    dataPanel.setOpaque(false);
    dataPanel.setLayout(new CardLayout());

    // tabbed pane containing the movie data
    JTabbedPane tabbedPane = new MainTabbedPane();
    tabbedPane.add(BUNDLE.getString("metatag.details"), new MovieInformationPanel(selectionModel)); //$NON-NLS-1$
    tabbedPane.add(BUNDLE.getString("metatag.cast"), new MovieCastPanel(selectionModel)); //$NON-NLS-1$
    tabbedPane.add(BUNDLE.getString("metatag.mediafiles"), new MovieMediaInformationPanel(selectionModel)); //$NON-NLS-1$
    tabbedPane.add(BUNDLE.getString("metatag.artwork"), new MovieArtworkPanel(selectionModel)); //$NON-NLS-1$
    tabbedPane.add(BUNDLE.getString("metatag.trailer"), new MovieTrailerPanel(selectionModel)); //$NON-NLS-1$
    dataPanel.add(tabbedPane);

    layeredPane.add(dataPanel, "cell 0 0, grow");
    layeredPane.setLayer(dataPanel, 0);

    // glass pane for searching/filtering
    filterPanel = new MovieExtendedSearchPanel(selectionModel);
    filterPanel.setVisible(false);

    layeredPane.add(filterPanel, "pos 0 0");
    layeredPane.setLayer(filterPanel, 1);

    createActions();
    createPopupMenu();
    registerAccelerators();

    listPanel.setPopupMenu(popupMenu);

    // create settings node
    settingsNode = new TmmSettingsNode(BUNDLE.getString("Settings.movies"), new MovieSettingsPanel()); //$NON-NLS-1$
    settingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.datasourceandnfo"), new MovieDatasourceSettingsPanel())); //$NON-NLS-1$

    TmmSettingsNode scraperSettingsNode = new TmmSettingsNode(BUNDLE.getString("Settings.scraper"), new MovieScraperSettingsPanel()); //$NON-NLS-1$
    scraperSettingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.scraper.options"), new MovieScraperOptionsSettingsPanel()));//$NON-NLS-1$
    scraperSettingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.nfo"), new MovieScraperNfoSettingsPanel()));//$NON-NLS-1$
    settingsNode.addChild(scraperSettingsNode);

    TmmSettingsNode imageSettingsNode = new TmmSettingsNode(BUNDLE.getString("Settings.images"), new MovieImageSettingsPanel());//$NON-NLS-1$
    imageSettingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.artwork.naming"), new MovieImageTypeSettingsPanel()));//$NON-NLS-1$
    imageSettingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.extraartwork"), new MovieImageExtraPanel()));//$NON-NLS-1$
    settingsNode.addChild(imageSettingsNode);

    settingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.trailer"), new MovieTrailerSettingsPanel()));//$NON-NLS-1$
    settingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.subtitle"), new MovieSubtitleSettingsPanel()));//$NON-NLS-1$
    settingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.renamer"), new MovieRenamerSettingsPanel()));//$NON-NLS-1$

    // further initializations
    init();

    listPanel.setInitialSelection();
  }

  private void init() {
    // apply stored UI filters
    if (MovieModuleManager.SETTINGS.isStoreUiFilters()) {
      SwingUtilities.invokeLater(() -> {
        MovieList.getInstance().searchDuplicates();
        selectionModel.setFilterValues(MovieModuleManager.SETTINGS.getUiFilters());
      });
    }
    // apply sorting
    if (MovieModuleManager.SETTINGS.isStoreUiSorting()) {
      selectionModel.sortMovies(MovieModuleManager.SETTINGS.getSortColumn(), MovieModuleManager.SETTINGS.isSortAscending());
    }
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
    exportAction = createAndRegisterAction(MovieExportAction.class);
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
    popupMenu.add(createAndRegisterAction(MovieEditAction.class));
    popupMenu.add(createAndRegisterAction(MovieBatchEditAction.class));
    popupMenu.add(createAndRegisterAction(MovieSetWatchedFlagAction.class));
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
    popupMenu.addSeparator();
    popupMenu.add(createAndRegisterAction(MovieClearImageCacheAction.class));
    popupMenu.addSeparator();
    popupMenu.add(createAndRegisterAction(MovieRemoveAction.class));
    popupMenu.add(createAndRegisterAction(MovieDeleteAction.class));

    // update popup menu
    updatePopupMenu = new JPopupMenu();
    updatePopupMenu.add(createAndRegisterAction(MovieUpdateDatasourceAction.class));
    updatePopupMenu.addPopupMenuListener(new PopupMenuListener() {
      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        updatePopupMenu.removeAll();
        updatePopupMenu.add(createAndRegisterAction(MovieUpdateDatasourceAction.class));
        updatePopupMenu.addSeparator();
        for (String ds : MovieModuleManager.SETTINGS.getMovieDataSource()) {
          updatePopupMenu.add(new MovieUpdateSingleDatasourceAction(ds));
        }
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
    editPopupMenu.add(createAndRegisterAction(MovieSetWatchedFlagAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieRewriteNfoAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieReadNfoAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieRenameAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieRenamePreviewAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieMediaInformationAction.class));
    editPopupMenu.addSeparator();
    editPopupMenu.add(createAndRegisterAction(MovieSyncTraktTvAction.class));
    editPopupMenu.add(createAndRegisterAction(MovieSyncWatchedTraktTvAction.class));

    // rename popup menu
    renamePopupMenu = new JPopupMenu();
    renamePopupMenu.add(createAndRegisterAction(MovieRenameAction.class));
    renamePopupMenu.add(createAndRegisterAction(MovieRenamePreviewAction.class));
  }

  public void setFilterMenuVisible(boolean visible) {
    filterPanel.setVisible(visible);
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
    return BUNDLE.getString("tmm.movies"); //$NON-NLS-1$ )
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
  public Action getExportAction() {
    return exportAction;
  }

  @Override
  public TmmSettingsNode getSettingsNode() {
    return settingsNode;
  }
}
