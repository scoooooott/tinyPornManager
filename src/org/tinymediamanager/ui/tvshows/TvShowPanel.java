/*
 * Copyright 2012 - 2013 Manuel Laggner
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
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.gpl.JSplitButton.JSplitButton;
import org.gpl.JSplitButton.action.SplitButtonActionListener;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowEpisode;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowSeason;
import org.tinymediamanager.core.tvshow.tasks.TvShowEpisodeScrapeTask;
import org.tinymediamanager.core.tvshow.tasks.TvShowReloadMediaInformationTask;
import org.tinymediamanager.core.tvshow.tasks.TvShowRenameTask;
import org.tinymediamanager.core.tvshow.tasks.TvShowScrapeTask;
import org.tinymediamanager.core.tvshow.tasks.TvShowUpdateDatasourceTask;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.PopupListener;
import org.tinymediamanager.ui.TmmSwingWorker;
import org.tinymediamanager.ui.TreeUI;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.JSearchTextField;
import org.tinymediamanager.ui.components.ZebraJTree;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog.ImageType;
import org.tinymediamanager.ui.tvshows.TvShowExtendedMatcher.SearchOptions;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowBatchEditorDialog;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowChooserDialog;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowEditorDialog;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowEpisodeEditorDialog;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowScrapeMetadataDialog;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.JTattooUtilities;

/**
 * The Class TvShowPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowPanel extends JPanel {
  private static final long           serialVersionUID              = -1923811385292825136L;
  private static final ResourceBundle BUNDLE                        = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private TvShowTreeModel             treeModel;
  private TvShowSelectionModel        tvShowSelectionModel;
  private TvShowSeasonSelectionModel  tvShowSeasonSelectionModel;
  private TvShowEpisodeSelectionModel tvShowEpisodeSelectionModel;
  private TvShowList                  tvShowList                    = TvShowList.getInstance();

  private JTree                       tree;
  private JPanel                      panelRight;
  private JMenu                       menu;
  private JLabel                      lblTvShows;
  private JLabel                      lblEpisodes;

  private final Action                actionUpdateDatasources       = new UpdateDatasourcesAction(false);
  private final Action                actionUpdateDatasources2      = new UpdateDatasourcesAction(true);
  private final Action                actionUpdateTvShow            = new UpdateTvShowAction();
  private final Action                actionScrape                  = new SingleScrapeAction(false);
  private final Action                actionScrape2                 = new SingleScrapeAction(true);
  private final Action                actionScrapeSelected          = new SelectedScrapeAction();
  private final Action                actionScrapeNewItems          = new ScrapeNewItemsAction();
  private final Action                actionEdit                    = new EditAction(false);
  private final Action                actionEdit2                   = new EditAction(true);
  private final Action                actionRemove2                 = new RemoveAction(true);
  private final Action                actionChangeSeasonPoster2     = new ChangeSeasonPosterAction(true);
  private final Action                actionBatchEdit               = new BatchEditAction();
  private final Action                actionScrapeEpisodes          = new ScrapeEpisodesAction();
  private final Action                actionRewriteTvShowNfo        = new RewriteTvShowNfoAction();
  private final Action                actionRewriteTvShowEpisodeNfo = new RewriteTvShowEpisodeNfoAction();
  private final Action                actionRename                  = new RenameAction();
  private final Action                actionMediaInformation        = new MediaInformationAction(false);
  private final Action                actionMediaInformation2       = new MediaInformationAction(true);
  private final Action                actionClearImageCache         = new TvShowClearImageCacheAction();

  private int                         width                         = 0;
  private JTextField                  textField;

  /**
   * Instantiates a new tv show panel.
   */
  public TvShowPanel() {
    super();

    treeModel = new TvShowTreeModel(tvShowList.getTvShows());
    tvShowSelectionModel = new TvShowSelectionModel();
    tvShowSeasonSelectionModel = new TvShowSeasonSelectionModel();
    tvShowEpisodeSelectionModel = new TvShowEpisodeSelectionModel();

    // build menu
    menu = new JMenu(BUNDLE.getString("tmm.tvshows")); //$NON-NLS-1$
    JFrame mainFrame = MainWindow.getFrame();
    JMenuBar menuBar = mainFrame.getJMenuBar();
    menuBar.add(menu);

    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("850px:grow"), FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

    JSplitPane splitPane = new JSplitPane();
    splitPane.setContinuousLayout(true);
    add(splitPane, "2, 2, fill, fill");

    JPanel panelTvShowTree = new JPanel();
    splitPane.setLeftComponent(panelTvShowTree);
    panelTvShowTree.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("3px:grow"), FormFactory.RELATED_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, }));

    textField = new JSearchTextField();
    panelTvShowTree.add(textField, "4, 1, right, bottom");
    textField.setColumns(12);
    textField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(final DocumentEvent e) {
        applyFilter();
      }

      @Override
      public void removeUpdate(final DocumentEvent e) {
        applyFilter();
      }

      @Override
      public void changedUpdate(final DocumentEvent e) {
        applyFilter();
      }

      public void applyFilter() {
        TvShowTreeModel filteredModel = (TvShowTreeModel) tree.getModel();
        if (StringUtils.isNotBlank(textField.getText())) {
          filteredModel.setFilter(SearchOptions.TEXT, textField.getText());
        }
        else {
          filteredModel.removeFilter(SearchOptions.TEXT);
        }

        filteredModel.filter(tree);
      }
    });

    JToggleButton btnFilter = new JToggleButton(BUNDLE.getString("movieextendedsearch.filter")); //$NON-NLS-1$
    panelTvShowTree.add(btnFilter, "6, 1, default, bottom");

    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    panelTvShowTree.add(scrollPane, "2, 3, 5, 1, fill, fill");

    JToolBar toolBar = new JToolBar();
    toolBar.setRollover(true);
    toolBar.setFloatable(false);
    toolBar.setOpaque(false);
    panelTvShowTree.add(toolBar, "2, 1");

    // toolBar.add(actionUpdateDatasources);
    final JSplitButton buttonUpdateDatasource = new JSplitButton(new ImageIcon(getClass().getResource(
        "/org/tinymediamanager/ui/images/Folder-Sync.png")));
    // temp fix for size of the button
    buttonUpdateDatasource.setText("   ");
    buttonUpdateDatasource.setHorizontalAlignment(JButton.LEFT);
    // buttonScrape.setMargin(new Insets(2, 2, 2, 24));
    buttonUpdateDatasource.setSplitWidth(18);
    buttonUpdateDatasource.addSplitButtonActionListener(new SplitButtonActionListener() {
      public void buttonClicked(ActionEvent e) {
        actionUpdateDatasources.actionPerformed(e);
      }

      public void splitButtonClicked(ActionEvent e) {
        // build the popupmenu on the fly
        buttonUpdateDatasource.getPopupMenu().removeAll();
        buttonUpdateDatasource.getPopupMenu().add(new JMenuItem(actionUpdateDatasources2));
        buttonUpdateDatasource.getPopupMenu().addSeparator();
        for (String ds : Globals.settings.getTvShowSettings().getTvShowDataSource()) {
          buttonUpdateDatasource.getPopupMenu().add(new JMenuItem(new UpdateSingleDatasourceAction(ds)));
        }
        buttonUpdateDatasource.getPopupMenu().addSeparator();
        buttonUpdateDatasource.getPopupMenu().add(new JMenuItem(actionUpdateTvShow));
        buttonUpdateDatasource.getPopupMenu().pack();
      }
    });

    JPopupMenu popup = new JPopupMenu("popup");
    buttonUpdateDatasource.setPopupMenu(popup);
    toolBar.add(buttonUpdateDatasource);

    JSplitButton buttonScrape = new JSplitButton(new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
    // temp fix for size of the button
    buttonScrape.setText("   ");
    buttonScrape.setHorizontalAlignment(JButton.LEFT);
    buttonScrape.setSplitWidth(18);

    // register for listener
    buttonScrape.addSplitButtonActionListener(new SplitButtonActionListener() {
      @Override
      public void buttonClicked(ActionEvent e) {
        actionScrape.actionPerformed(e);
      }

      @Override
      public void splitButtonClicked(ActionEvent e) {
      }
    });

    popup = new JPopupMenu("popup");
    JMenuItem item = new JMenuItem(actionScrape2);
    popup.add(item);
    // item = new JMenuItem(actionScrapeUnscraped);
    // popup.add(item);
    item = new JMenuItem(actionScrapeSelected);
    popup.add(item);
    item = new JMenuItem(actionScrapeNewItems);
    popup.add(item);
    buttonScrape.setPopupMenu(popup);
    toolBar.add(buttonScrape);
    toolBar.add(actionEdit);

    JButton btnMediaInformation = new JButton();
    btnMediaInformation.setAction(actionMediaInformation);
    toolBar.add(btnMediaInformation);

    // install drawing of full with
    tree = new ZebraJTree(treeModel) {
      private static final long serialVersionUID = 1L;

      @Override
      public void paintComponent(Graphics g) {
        width = this.getWidth();
        super.paintComponent(g);
      }
    };

    TreeUI ui = new TreeUI() {
      @Override
      protected void paintRow(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded,
          boolean hasBeenExpanded, boolean isLeaf) {
        bounds.width = width - bounds.x;
        super.paintRow(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
      }
    };
    tree.setUI(ui);

    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setCellRenderer(new TvShowTreeCellRenderer());
    scrollPane.setViewportView(tree);

    JPanel panelHeader = new JPanel() {
      private static final long serialVersionUID = -6914183798172482157L;

      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);
        JTattooUtilities.fillHorGradient(g, AbstractLookAndFeel.getTheme().getColHeaderColors(), 0, 0, getWidth(), getHeight());
      }
    };
    scrollPane.setColumnHeaderView(panelHeader);
    panelHeader.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("center:20px"), ColumnSpec.decode("center:20px"), ColumnSpec.decode("center:20px") },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblTvShowsColumn = new JLabel(BUNDLE.getString("metatag.tvshow")); //$NON-NLS-1$
    lblTvShowsColumn.setHorizontalAlignment(JLabel.CENTER);
    panelHeader.add(lblTvShowsColumn, "2, 1");

    JLabel lblNfoColumn = new JLabel("");
    lblNfoColumn.setHorizontalAlignment(JLabel.CENTER);
    lblNfoColumn.setIcon(new ImageIcon(TvShowPanel.class.getResource("/org/tinymediamanager/ui/images/Info.png")));
    lblNfoColumn.setToolTipText(BUNDLE.getString("metatag.nfo"));//$NON-NLS-1$
    panelHeader.add(lblNfoColumn, "4, 1");

    JLabel lblImageColumn = new JLabel("");
    lblImageColumn.setHorizontalAlignment(JLabel.CENTER);
    lblImageColumn.setIcon(new ImageIcon(TvShowPanel.class.getResource("/org/tinymediamanager/ui/images/Image.png")));
    lblImageColumn.setToolTipText(BUNDLE.getString("metatag.images"));//$NON-NLS-1$
    panelHeader.add(lblImageColumn, "5, 1");

    JLabel lblSubtitleColumn = new JLabel("");
    lblSubtitleColumn.setHorizontalAlignment(JLabel.CENTER);
    lblSubtitleColumn.setIcon(new ImageIcon(TvShowPanel.class.getResource("/org/tinymediamanager/ui/images/subtitle.png")));
    lblSubtitleColumn.setToolTipText(BUNDLE.getString("metatag.subtitles"));//$NON-NLS-1$
    panelHeader.add(lblSubtitleColumn, "6, 1");

    JPanel panel = new JPanel();
    panelTvShowTree.add(panel, "2, 5, 3, 1, fill, fill");
    panel
        .setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
            FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
            FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblTvShowsT = new JLabel(BUNDLE.getString("metatag.tvshows") + ":"); //$NON-NLS-1$
    panel.add(lblTvShowsT, "1, 2, fill, fill");

    lblTvShows = new JLabel("");
    panel.add(lblTvShows, "3, 2");

    JLabel labelSlash = new JLabel("/");
    panel.add(labelSlash, "5, 2");

    JLabel lblEpisodesT = new JLabel(BUNDLE.getString("metatag.episodes") + ":"); //$NON-NLS-1$
    panel.add(lblEpisodesT, "7, 2");

    lblEpisodes = new JLabel("");
    panel.add(lblEpisodes, "9, 2");

    JLayeredPane layeredPaneRight = new JLayeredPane();
    layeredPaneRight.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default"), ColumnSpec.decode("default:grow") }, new RowSpec[] {
        RowSpec.decode("default"), RowSpec.decode("default:grow") }));
    panelRight = new JPanel();
    layeredPaneRight.add(panelRight, "1, 1, 2, 2, fill, fill");
    layeredPaneRight.setLayer(panelRight, 0);

    // glass pane
    final TvShowExtendedSearchPanel panelExtendedSearch = new TvShowExtendedSearchPanel(treeModel, tree);
    panelExtendedSearch.setVisible(false);
    // panelMovieList.add(panelExtendedSearch, "2, 5, 2, 1, fill, fill");
    btnFilter.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        if (panelExtendedSearch.isVisible() == true) {
          panelExtendedSearch.setVisible(false);
        }
        else {
          panelExtendedSearch.setVisible(true);
        }
      }
    });
    layeredPaneRight.add(panelExtendedSearch, "1, 1, fill, fill");
    layeredPaneRight.setLayer(panelExtendedSearch, 1);

    splitPane.setRightComponent(layeredPaneRight);
    panelRight.setLayout(new CardLayout(0, 0));

    JPanel panelTvShow = new TvShowInformationPanel(tvShowSelectionModel);
    panelRight.add(panelTvShow, "tvShow");

    JPanel panelTvShowSeason = new TvShowSeasonInformationPanel(tvShowSeasonSelectionModel);
    panelRight.add(panelTvShowSeason, "tvShowSeason");

    JPanel panelTvShowEpisode = new TvShowEpisodeInformationPanel(tvShowEpisodeSelectionModel);
    panelRight.add(panelTvShowEpisode, "tvShowEpisode");

    tree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node != null) {
          // click on a tv show
          if (node.getUserObject() instanceof TvShow) {
            TvShow tvShow = (TvShow) node.getUserObject();
            tvShowSelectionModel.setSelectedTvShow(tvShow);
            CardLayout cl = (CardLayout) (panelRight.getLayout());
            cl.show(panelRight, "tvShow");
          }

          // click on a season
          if (node.getUserObject() instanceof TvShowSeason) {
            TvShowSeason tvShowSeason = (TvShowSeason) node.getUserObject();
            tvShowSeasonSelectionModel.setSelectedTvShowSeason(tvShowSeason);
            CardLayout cl = (CardLayout) (panelRight.getLayout());
            cl.show(panelRight, "tvShowSeason");
          }

          // click on an episode
          if (node.getUserObject() instanceof TvShowEpisode) {
            TvShowEpisode tvShowEpisode = (TvShowEpisode) node.getUserObject();
            tvShowEpisodeSelectionModel.setSelectedTvShowEpisode(tvShowEpisode);
            CardLayout cl = (CardLayout) (panelRight.getLayout());
            cl.show(panelRight, "tvShowEpisode");
          }
        }
        else {
          // check if there is at least one tv show in the model
          TvShowRootTreeNode root = (TvShowRootTreeNode) tree.getModel().getRoot();
          if (root.getChildCount() == 0) {
            // sets an inital show
            tvShowSelectionModel.setSelectedTvShow(null);
          }
        }
      }
    });

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentHidden(ComponentEvent e) {
        menu.setVisible(false);
        super.componentHidden(e);
      }

      @Override
      public void componentShown(ComponentEvent e) {
        menu.setVisible(true);
        super.componentHidden(e);
      }
    });

    // further initializations
    init();
    initDataBindings();

    // selecting first TV show at startup
    if (tvShowList.getTvShows() != null && tvShowList.getTvShows().size() > 0) {
      DefaultMutableTreeNode firstLeaf = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) tree.getModel().getRoot()).getFirstChild();
      tree.setSelectionPath(new TreePath(((DefaultMutableTreeNode) firstLeaf.getParent()).getPath()));
      tree.setSelectionPath(new TreePath(firstLeaf.getPath()));
    }
  }

  /**
   * Inits the.
   */
  private void init() {
    // build menu
    buildMenu();

  }

  /**
   * Builds the menu.
   */
  private void buildMenu() {
    // menu items
    menu.add(actionUpdateDatasources2);
    menu.addSeparator();

    JMenu menuScrape = new JMenu(BUNDLE.getString("Button.scrape")); //$NON-NLS-1$
    menuScrape.add(actionScrape2);
    menuScrape.add(actionScrapeSelected);
    menuScrape.add(actionScrapeNewItems);
    menu.add(menuScrape);

    JMenu menuEdit = new JMenu(BUNDLE.getString("Button.edit")); //$NON-NLS-1$
    menuEdit.add(actionEdit2);
    menuEdit.add(actionChangeSeasonPoster2);
    menu.add(actionBatchEdit);
    menu.add(menuEdit);
    menu.add(actionRewriteTvShowNfo);
    menu.add(actionRewriteTvShowEpisodeNfo);

    // menu.add(actionScrapeUnscraped);
    // menu.add(actionScrapeMetadataSelected);
    // menu.addSeparator();
    // menu.add(actionEditMovie2);

    menu.add(actionRename);
    menu.add(actionMediaInformation2);
    menu.add(actionClearImageCache);
    // menu.add(actionExport);
    menu.addSeparator();
    menu.add(actionRemove2);

    // popup menu
    JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.add(actionScrape2);
    popupMenu.add(actionScrapeSelected);
    popupMenu.add(actionScrapeEpisodes);
    popupMenu.add(actionScrapeNewItems);
    // popupMenu.add(actionScrapeMetadataSelected);
    popupMenu.addSeparator();
    popupMenu.add(actionUpdateTvShow);
    popupMenu.addSeparator();
    popupMenu.add(actionEdit2);
    popupMenu.add(actionChangeSeasonPoster2);
    popupMenu.add(actionBatchEdit);
    popupMenu.add(actionRewriteTvShowNfo);
    popupMenu.add(actionRewriteTvShowEpisodeNfo);
    // popupMenu.add(actionBatchEdit);
    popupMenu.add(actionRename);
    popupMenu.add(actionMediaInformation2);
    // popupMenu.add(actionExport);
    popupMenu.add(actionClearImageCache);
    popupMenu.addSeparator();
    popupMenu.add(actionRemove2);
    popupMenu.addSeparator();
    popupMenu.add(new ExpandAllAction(tree));
    popupMenu.add(new CollapseAllAction(tree));

    MouseListener popupListener = new PopupListener(popupMenu, tree);
    tree.addMouseListener(popupListener);
  }

  /**
   * Gets the selected tv shows.
   * 
   * @return the selected tv shows
   */
  private List<TvShow> getSelectedTvShows() {
    List<TvShow> selectedTvShows = new ArrayList<TvShow>();

    TreePath[] paths = tree.getSelectionPaths();

    // filter out all tv shows from the selection
    if (paths != null) {
      for (TreePath path : paths) {
        if (path.getPathCount() > 1) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          if (node.getUserObject() instanceof TvShow) {
            TvShow tvShow = (TvShow) node.getUserObject();
            selectedTvShows.add(tvShow);
          }
        }
      }
    }

    return selectedTvShows;
  }

  private List<TvShowEpisode> getSelectedEpisodes() {
    List<TvShowEpisode> episodes = new ArrayList<TvShowEpisode>();

    for (Object obj : getSelectedObjects()) {
      if (obj instanceof TvShowEpisode) {
        TvShowEpisode episode = (TvShowEpisode) obj;
        if (!episodes.contains(episode)) {
          episodes.add(episode);
        }
      }
      else if (obj instanceof TvShowSeason) {
        TvShowSeason season = (TvShowSeason) obj;
        for (TvShowEpisode episode : season.getEpisodes()) {
          if (!episodes.contains(episode)) {
            episodes.add(episode);
          }
        }
      }
      else if (obj instanceof TvShow) {
        TvShow tvShow = (TvShow) obj;
        for (TvShowEpisode episode : tvShow.getEpisodes()) {
          if (!episodes.contains(episode)) {
            episodes.add(episode);
          }
        }
      }
    }

    return episodes;
  }

  /**
   * Gets the selected objects.
   * 
   * @return the selected objects
   */
  private List<Object> getSelectedObjects() {
    List<Object> selectedObjects = new ArrayList<Object>();

    TreePath[] paths = tree.getSelectionPaths();

    // filter out all objects from the selection
    if (paths != null) {
      for (TreePath path : paths) {
        if (path.getPathCount() > 1) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
          selectedObjects.add(node.getUserObject());
        }
      }
    }

    return selectedObjects;
  }

  /**
   * The Class UpdateDatasourcesAction.
   * 
   * @author Manuel Laggner
   */
  private class UpdateDatasourcesAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 5704371143505653741L;

    /**
     * Instantiates a new update datasources action.
     * 
     * @param withTitle
     *          the with title
     */
    public UpdateDatasourcesAction(boolean withTitle) {
      if (withTitle) {
        putValue(NAME, BUNDLE.getString("update.datasource")); //$NON-NLS-1$
      }
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Folder-Sync.png")));
      putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Folder-Sync.png")));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      @SuppressWarnings("rawtypes")
      TmmSwingWorker task = new TvShowUpdateDatasourceTask();
      if (!MainWindow.executeMainTask(task)) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("onlyoneoperation")); //$NON-NLS-1$
      }
    }
  }

  private class UpdateSingleDatasourceAction extends AbstractAction {
    private static final long serialVersionUID = 1520541175183435685L;
    private String            datasource;

    public UpdateSingleDatasourceAction(String datasource) {
      putValue(NAME, datasource);
      this.datasource = datasource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      @SuppressWarnings("rawtypes")
      TmmSwingWorker task = new TvShowUpdateDatasourceTask(datasource);
      if (!MainWindow.executeMainTask(task)) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("onlyoneoperation")); //$NON-NLS-1$
      }
    }
  }

  private class UpdateTvShowAction extends AbstractAction {
    private static final long serialVersionUID = 7216738427209633666L;

    public UpdateTvShowAction() {
      putValue(NAME, BUNDLE.getString("tvshow.update")); //$NON-NLS-1$
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Folder-Sync.png")));
      putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Folder-Sync.png")));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      List<TvShow> selectedTvShows = getSelectedTvShows();
      List<File> tvShowFolders = new ArrayList<File>();

      if (selectedTvShows.isEmpty()) {
        return;
      }

      for (TvShow tvShow : selectedTvShows) {
        tvShowFolders.add(new File(tvShow.getPath()));
      }

      @SuppressWarnings("rawtypes")
      TmmSwingWorker task = new TvShowUpdateDatasourceTask(tvShowFolders);
      if (!MainWindow.executeMainTask(task)) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("onlyoneoperation")); //$NON-NLS-1$
      }
    }
  }

  /**
   * The Class SingleScrapeAction.
   * 
   * @author Manuel Laggner
   */
  private class SingleScrapeAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 641704453374845709L;

    /**
     * Instantiates a new SingleScrapeAction.
     * 
     * @param withTitle
     *          the with title
     */
    public SingleScrapeAction(boolean withTitle) {
      if (withTitle) {
        putValue(NAME, BUNDLE.getString("tvshow.scrape.selected")); //$NON-NLS-1$
      }
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
      putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.scrape.selected")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      List<TvShow> selectedTvShows = getSelectedTvShows();

      for (TvShow tvShow : selectedTvShows) {
        // display tv show chooser
        TvShowChooserDialog chooser = new TvShowChooserDialog(tvShow, selectedTvShows.size() > 1 ? true : false);
        if (!chooser.showDialog()) {
          break;
        }
      }
    }
  }

  private class ScrapeEpisodesAction extends AbstractAction {
    private static final long serialVersionUID = -75916665265142730L;

    public ScrapeEpisodesAction() {
      putValue(NAME, BUNDLE.getString("tvshowepisode.scrape")); //$NON-NLS-1$
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
      putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
      List<TvShowEpisode> episodes = getSelectedEpisodes();

      TvShowEpisodeScrapeTask task = new TvShowEpisodeScrapeTask(episodes);
      Globals.executor.execute(task);
    }
  }

  private class ScrapeNewItemsAction extends AbstractAction {
    private static final long serialVersionUID = -3365542777082781952L;

    public ScrapeNewItemsAction() {
      putValue(NAME, BUNDLE.getString("tvshow.scrape.newitems")); //$NON-NLS-1$
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
      putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      List<TvShow> newTvShows = new ArrayList<TvShow>();
      List<TvShowEpisode> newEpisodes = new ArrayList<TvShowEpisode>();

      for (TvShow tvShow : new ArrayList<TvShow>(tvShowList.getTvShows())) {
        // if there is at least one new episode and no scraper id we assume the TV show is new
        if (tvShow.isNewlyAdded() && !tvShow.isScraped()) {
          newTvShows.add(tvShow);
          continue;
        }
        // else: check every episode if there is a new episode
        for (TvShowEpisode episode : tvShow.getEpisodes()) {
          if (episode.isNewlyAdded() && !episode.isScraped()) {
            newEpisodes.add(episode);
          }
        }
      }

      // now start the scrape tasks
      // epsiode scraping can run in background
      TvShowEpisodeScrapeTask task = new TvShowEpisodeScrapeTask(newEpisodes);
      Globals.executor.execute(task);

      // whereas tv show scraping has to run in foreground
      for (TvShow tvShow : newTvShows) {
        TvShowChooserDialog chooser = new TvShowChooserDialog(tvShow, newTvShows.size() > 1 ? true : false);
        if (!chooser.showDialog()) {
          break;
        }
      }
    }
  }

  /**
   * The Class EditAction.
   * 
   * @author Manuel Laggner
   */
  private class EditAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -3911290901017607679L;

    /**
     * Instantiates a new edits the action.
     * 
     * @param withTitle
     *          the with title
     */
    public EditAction(boolean withTitle) {
      if (withTitle) {
        putValue(NAME, BUNDLE.getString("tvshow.edit")); //$NON-NLS-1$
      }
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Pencil.png")));
      putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Pencil.png")));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.edit")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      List<Object> selectedObjects = getSelectedObjects();

      for (Object obj : selectedObjects) {
        // display tv show editor
        if (obj instanceof TvShow) {
          TvShow tvShow = (TvShow) obj;
          TvShowEditorDialog editor = new TvShowEditorDialog(tvShow, selectedObjects.size() > 1 ? true : false);
          if (!editor.showDialog()) {
            break;
          }
        }
        // display tv episode editor
        if (obj instanceof TvShowEpisode) {
          TvShowEpisode tvShowEpisode = (TvShowEpisode) obj;
          TvShowEpisodeEditorDialog editor = new TvShowEpisodeEditorDialog(tvShowEpisode, selectedObjects.size() > 1 ? true : false);
          if (!editor.showDialog()) {
            break;
          }
        }
      }
    }
  }

  private class RemoveAction extends AbstractAction {
    private static final long serialVersionUID = -2355545751433709417L;

    public RemoveAction(boolean withTitle) {
      if (withTitle) {
        putValue(NAME, BUNDLE.getString("tvshow.remove")); //$NON-NLS-1$
      }
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Cross.png")));
      putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Cross.png")));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.remove")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      List<Object> selectedObjects = getSelectedObjects();

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

  /**
   * The Class ChangeSeasonPosterAction.
   * 
   * @author Manuel Laggner
   */
  private class ChangeSeasonPosterAction extends AbstractAction {
    private static final long serialVersionUID = 8356413227405772558L;

    /**
     * Instantiates a new change season poster action.
     * 
     * @param withTitle
     *          the with title
     */
    public ChangeSeasonPosterAction(boolean withTitle) {
      if (withTitle) {
        putValue(NAME, BUNDLE.getString("tvshow.changeseasonposter")); //$NON-NLS-1$
      }
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Pencil.png")));
      putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Pencil.png")));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.changeseasonposter")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      List<Object> selectedObjects = getSelectedObjects();

      for (Object obj : selectedObjects) {
        // display image chooser
        if (obj instanceof TvShowSeason) {
          TvShowSeason season = (TvShowSeason) obj;
          ImageLabel imageLabel = new ImageLabel();
          ImageChooserDialog dialog = new ImageChooserDialog(season.getTvShow().getIds(), ImageType.SEASON, tvShowList.getArtworkProviders(),
              imageLabel, null, null, MediaType.TV_SHOW);
          dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
          dialog.setVisible(true);

          if (StringUtils.isNotBlank(imageLabel.getImageUrl())) {
            season.setPosterUrl(imageLabel.getImageUrl());
            season.getTvShow().writeSeasonPoster(season.getSeason());
          }
        }
      }
    }
  }

  private class BatchEditAction extends AbstractAction {
    private static final long serialVersionUID = -1193886444149690516L;

    /**
     * Instantiates a new batch edit action.
     */
    public BatchEditAction() {
      putValue(NAME, BUNDLE.getString("tvshow.bulkedit")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.bulkedit.desc")); //$NON-NLS-1$
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Pencil.png")));
      putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Pencil.png")));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
      List<Object> selectedObjects = getSelectedObjects();
      List<TvShow> selectedTvShows = new ArrayList<TvShow>();
      List<TvShowEpisode> selectedEpisodes = new ArrayList<TvShowEpisode>();

      for (Object obj : selectedObjects) {
        // display tv show editor
        if (obj instanceof TvShow) {
          TvShow tvShow = (TvShow) obj;
          selectedTvShows.add(tvShow);
        }
        // display tv episode editor
        if (obj instanceof TvShowEpisode) {
          TvShowEpisode tvShowEpisode = (TvShowEpisode) obj;
          selectedEpisodes.add(tvShowEpisode);
        }
      }

      TvShowBatchEditorDialog dialog = new TvShowBatchEditorDialog(selectedTvShows, selectedEpisodes);
      dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
      dialog.setVisible(true);
    }
  }

  private class SelectedScrapeAction extends AbstractAction {
    private static final long serialVersionUID = 699165862194137592L;

    /**
     * Instantiates a new UnscrapedScrapeAction.
     */
    public SelectedScrapeAction() {
      putValue(NAME, BUNDLE.getString("tvshow.scrape.selected.force")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.scrape.selected.force.desc")); //$NON-NLS-1$
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
      putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      List<TvShow> selectedTvShows = getSelectedTvShows();

      if (selectedTvShows.size() > 0) {
        // scrapeTask = new ScrapeTask(selectedMovies);
        TvShowScrapeMetadataDialog dialog = new TvShowScrapeMetadataDialog(BUNDLE.getString("tvshow.scrape.selected.force")); //$NON-NLS-1$
        dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
        dialog.setVisible(true);
        // get options from dialog
        TvShowSearchAndScrapeOptions options = dialog.getTvShowSearchAndScrapeConfig();
        // do we want to scrape?
        if (dialog.shouldStartScrape()) {
          // scrape
          @SuppressWarnings("rawtypes")
          TmmSwingWorker scrapeTask = new TvShowScrapeTask(selectedTvShows, true, options);
          if (!MainWindow.executeMainTask(scrapeTask)) {
            JOptionPane.showMessageDialog(null, BUNDLE.getString("onlyoneoperation")); //$NON-NLS-1$
          }
        }
        dialog.dispose();
      }
    }
  }

  private class RewriteTvShowNfoAction extends AbstractAction {
    private static final long serialVersionUID = -6575156436788397648L;

    public RewriteTvShowNfoAction() {
      putValue(NAME, BUNDLE.getString("tvshow.rewritenfo")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final List<TvShow> selectedTvShows = getSelectedTvShows();

      // rewrite selected NFOs
      Globals.executor.execute(new Runnable() {
        @Override
        public void run() {
          for (TvShow tvShow : selectedTvShows) {
            tvShow.writeNFO();
          }
        }
      });
    }
  }

  private class RewriteTvShowEpisodeNfoAction extends AbstractAction {
    private static final long serialVersionUID = 5762347331284295996L;

    public RewriteTvShowEpisodeNfoAction() {
      putValue(NAME, BUNDLE.getString("tvshowepisode.rewritenfo")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final List<TvShowEpisode> selectedEpisodes = getSelectedEpisodes();

      // rewrite selected NFOs
      Globals.executor.execute(new Runnable() {
        @Override
        public void run() {
          for (TvShowEpisode episode : selectedEpisodes) {
            episode.writeNFO();
          }
        }
      });
    }
  }

  /**
   * The Class RenameAction.
   * 
   * @author Manuel Laggner
   */
  private class RenameAction extends AbstractAction {
    private static final long serialVersionUID = -8988748633666277616L;

    /**
     * Instantiates a new rename action.
     */
    public RenameAction() {
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/rename-icon.png")));
      putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/rename-icon.png")));
      putValue(NAME, BUNDLE.getString("tvshow.rename")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.rename")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      List<TvShow> selectedTvShows = getSelectedTvShows();
      Set<TvShowEpisode> selectedEpisodes = new HashSet<TvShowEpisode>();

      // add all episodes which are not part of a selected tv show
      for (Object obj : getSelectedObjects()) {
        if (obj instanceof TvShowEpisode) {
          TvShowEpisode episode = (TvShowEpisode) obj;
          if (!selectedTvShows.contains(episode.getTvShow())) {
            selectedEpisodes.add(episode);
          }
        }
        if (obj instanceof TvShowSeason) {
          TvShowSeason season = (TvShowSeason) obj;
          for (TvShowEpisode episode : season.getEpisodes()) {
            selectedEpisodes.add(episode);
          }
        }
      }

      // rename
      @SuppressWarnings("rawtypes")
      TmmSwingWorker renameTask = new TvShowRenameTask(selectedTvShows, new ArrayList<TvShowEpisode>(selectedEpisodes), true);
      if (!MainWindow.executeMainTask(renameTask)) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("onlyoneoperation")); //$NON-NLS-1$
      }
    }
  }

  protected void initDataBindings() {
    BeanProperty<TvShowList, Integer> tvShowListBeanProperty = BeanProperty.create("tvShowCount");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowList, Integer, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowList,
        tvShowListBeanProperty, lblTvShows, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<TvShowList, Integer> tvShowListBeanProperty_1 = BeanProperty.create("episodeCount");
    AutoBinding<TvShowList, Integer, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowList,
        tvShowListBeanProperty_1, lblEpisodes, jLabelBeanProperty);
    autoBinding_1.bind();
  }

  public class MediaInformationAction extends AbstractAction {
    private static final long serialVersionUID = -1274423130095036944L;

    public MediaInformationAction(boolean withTitle) {
      if (withTitle) {
        putValue(NAME, BUNDLE.getString("movie.updatemediainfo")); //$NON-NLS-1$
      }
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/mediainfo.png")));
      putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/mediainfo.png")));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.updatemediainfo")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      List<TvShow> selectedTvShows = getSelectedTvShows();
      List<TvShowEpisode> selectedEpisodes = new ArrayList<TvShowEpisode>();

      // add all episodes which are not part of a selected tv show
      for (Object obj : getSelectedObjects()) {
        if (obj instanceof TvShowEpisode) {
          TvShowEpisode episode = (TvShowEpisode) obj;
          if (!selectedTvShows.contains(episode.getTvShow())) {
            selectedEpisodes.add(episode);
          }
        }
      }

      // get data of all files within all selected movies
      if (selectedTvShows.size() > 0 || selectedEpisodes.size() > 0) {
        @SuppressWarnings("rawtypes")
        TmmSwingWorker task = new TvShowReloadMediaInformationTask(selectedTvShows, selectedEpisodes);
        if (!MainWindow.executeMainTask(task)) {
          JOptionPane.showMessageDialog(null, BUNDLE.getString("onlyoneoperation")); //$NON-NLS-1$
        }
      }
    }
  }

  public class CollapseAllAction extends AbstractAction {
    private static final long serialVersionUID = -1444530142931061317L;

    private JTree             tree;

    public CollapseAllAction(JTree tree) {
      this.tree = tree;
      putValue(NAME, BUNDLE.getString("tree.collapseall")); //$NON-NLS-1$  
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      for (int i = this.tree.getRowCount() - 1; i >= 0; i--) {
        this.tree.collapseRow(i);
      }
    }
  }

  public class ExpandAllAction extends AbstractAction {
    private static final long serialVersionUID = 6191727607109012198L;

    private JTree             tree;

    public ExpandAllAction(JTree tree) {
      this.tree = tree;
      putValue(NAME, BUNDLE.getString("tree.expandall")); //$NON-NLS-1$  
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int i = 0;
      do {
        this.tree.expandRow(i++);
      } while (i < this.tree.getRowCount());
    }
  }

  public class TvShowClearImageCacheAction extends AbstractAction {
    private static final long serialVersionUID = 3452373237085274937L;

    public TvShowClearImageCacheAction() {
      putValue(NAME, BUNDLE.getString("tvshow.clearimagecache")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
      List<TvShow> selectedTvShows = getSelectedTvShows();
      List<TvShowEpisode> selectedEpisodes = new ArrayList<TvShowEpisode>();

      // add all episodes which are not part of a selected tv show
      for (Object obj : getSelectedObjects()) {
        if (obj instanceof TvShowEpisode) {
          TvShowEpisode episode = (TvShowEpisode) obj;
          if (!selectedTvShows.contains(episode.getTvShow())) {
            selectedEpisodes.add(episode);
          }
        }
      }

      // clear the cache
      MainWindow.getActiveInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      for (TvShow tvShow : selectedTvShows) {
        ImageCache.clearImageCacheForMediaEntity(tvShow);
      }

      for (TvShowEpisode episode : selectedEpisodes) {
        ImageCache.clearImageCacheForMediaEntity(episode);
      }
      MainWindow.getActiveInstance().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }
}
