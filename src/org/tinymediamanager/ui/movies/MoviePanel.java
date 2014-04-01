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
package org.tinymediamanager.ui.movies;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.commons.lang3.StringUtils;
import org.gpl.JSplitButton.JSplitButton;
import org.gpl.JSplitButton.action.SplitButtonActionListener;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.BorderCellRenderer;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.IconRenderer;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.JSearchTextField;
import org.tinymediamanager.ui.components.ZebraJTable;
import org.tinymediamanager.ui.movies.actions.MovieAssignMovieSetAction;
import org.tinymediamanager.ui.movies.actions.MovieBatchEditAction;
import org.tinymediamanager.ui.movies.actions.MovieClearImageCacheAction;
import org.tinymediamanager.ui.movies.actions.MovieEditAction;
import org.tinymediamanager.ui.movies.actions.MovieExportAction;
import org.tinymediamanager.ui.movies.actions.MovieFindMissingAction;
import org.tinymediamanager.ui.movies.actions.MovieMediaInformationAction;
import org.tinymediamanager.ui.movies.actions.MovieRemoveAction;
import org.tinymediamanager.ui.movies.actions.MovieRenameAction;
import org.tinymediamanager.ui.movies.actions.MovieRenamePreviewAction;
import org.tinymediamanager.ui.movies.actions.MovieRewriteNfoAction;
import org.tinymediamanager.ui.movies.actions.MovieSelectedScrapeAction;
import org.tinymediamanager.ui.movies.actions.MovieSelectedScrapeMetadataAction;
import org.tinymediamanager.ui.movies.actions.MovieSingleScrapeAction;
import org.tinymediamanager.ui.movies.actions.MovieUnscrapedScrapeAction;
import org.tinymediamanager.ui.movies.actions.MovieUpdateDatasourceAction;
import org.tinymediamanager.ui.movies.actions.MovieUpdateSingleDatasourceAction;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MoviePanel.
 * 
 * @author Manuel Laggner
 */
public class MoviePanel extends JPanel {

  /** The Constant BUNDLE. */
  private static final ResourceBundle   BUNDLE                       = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The Constant serialVersionUID. */
  private static final long             serialVersionUID             = 1L;

  /** The logger. */
  private final static Logger           LOGGER                       = LoggerFactory.getLogger(MoviePanel.class);

  /** The movie list. */
  private MovieList                     movieList;

  /** The text field. */
  private JTextField                    textField;

  /** The table. */
  private ZebraJTable                   table;

  /** The action update data sources. */
  private final Action                  actionUpdateDataSources      = new MovieUpdateDatasourceAction(false);

  /** The action update data sources. */
  private final Action                  actionUpdateDataSources2     = new MovieUpdateDatasourceAction(true);

  /** The action scrape. */
  private final Action                  actionScrape                 = new MovieSingleScrapeAction(false);

  /** The action scrape. */
  private final Action                  actionScrape2                = new MovieSingleScrapeAction(true);

  /** The action edit movie. */
  private final Action                  actionEditMovie              = new MovieEditAction(false);

  /** The action edit movie. */
  private final Action                  actionEditMovie2             = new MovieEditAction(true);

  /** The action scrape unscraped movies. */
  private final Action                  actionScrapeUnscraped        = new MovieUnscrapedScrapeAction();

  /** The action scrape selected movies. */
  private final Action                  actionScrapeSelected         = new MovieSelectedScrapeAction();

  /** The action scrape metadata selected. */
  private final Action                  actionScrapeMetadataSelected = new MovieSelectedScrapeMetadataAction();
  private final Action                  actionAssignMovieSets        = new MovieAssignMovieSetAction();
  private final Action                  actionRenamerPreview         = new MovieRenamePreviewAction();

  /** The action rename. */
  private final Action                  actionRename                 = new MovieRenameAction(false);

  /** The action rename2. */
  private final Action                  actionRename2                = new MovieRenameAction(true);

  /** The action remove2. */
  private final Action                  actionRemove2                = new MovieRemoveAction();

  /** The action export. */
  private final Action                  actionExport                 = new MovieExportAction();

  private final Action                  actionRewriteNfo             = new MovieRewriteNfoAction();

  /** The panel movie count. */
  private JPanel                        panelMovieCount;

  /** The lbl movie count. */
  private JLabel                        lblMovieCount;

  /** The lbl movie count int. */
  private JLabel                        lblMovieCountTotal;

  /** The btn ren. */
  private JButton                       btnRen;

  /** The menu. */
  private JMenu                         menu;

  /** The movie table model. */
  private DefaultEventTableModel<Movie> movieTableModel;

  /** The movie selection model. */
  MovieSelectionModel                   movieSelectionModel;

  /** The sorted movies. */
  private SortedList<Movie>             sortedMovies;

  /** The text filtered movies. */
  private FilterList<Movie>             textFilteredMovies;

  /** The panel extended search. */
  private JPanel                        panelExtendedSearch;

  /** The lbl movie count of. */
  private JLabel                        lblMovieCountOf;

  /** The lbl movie count filtered. */
  private JLabel                        lblMovieCountFiltered;

  /** The split pane horizontal. */
  private JSplitPane                    splitPaneHorizontal;

  /** The panel right. */
  private MovieInformationPanel         panelRight;

  /** The btn media information. */
  private JButton                       btnMediaInformation;

  /** The action media information. */
  private final Action                  actionMediaInformation       = new MovieMediaInformationAction(false);

  /** The action media information2. */
  private final Action                  actionMediaInformation2      = new MovieMediaInformationAction(true);

  /** The action batch edit. */
  private final Action                  actionBatchEdit              = new MovieBatchEditAction();

  private final Action                  actionClearImageCache        = new MovieClearImageCacheAction();

  /**
   * Create the panel.
   */
  public MoviePanel() {
    super();
    // load movielist
    LOGGER.debug("loading MovieList");
    movieList = MovieList.getInstance();
    sortedMovies = new SortedList<Movie>(GlazedListsSwing.swingThreadProxyList(movieList.getMovies()), new MovieComparator());
    sortedMovies.setMode(SortedList.AVOID_MOVING_ELEMENTS);

    // build menu
    menu = new JMenu(BUNDLE.getString("tmm.movies")); //$NON-NLS-1$
    JFrame mainFrame = MainWindow.getFrame();
    JMenuBar menuBar = mainFrame.getJMenuBar();
    menuBar.add(menu);

    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("850px:grow"), FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), }));

    splitPaneHorizontal = new JSplitPane();
    splitPaneHorizontal.setContinuousLayout(true);
    add(splitPaneHorizontal, "2, 2, fill, fill");

    JPanel panelMovieList = new JPanel();
    splitPaneHorizontal.setLeftComponent(panelMovieList);
    panelMovieList.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { RowSpec.decode("26px"),
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:max(200px;default):grow"), FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JToolBar toolBar = new JToolBar();
    toolBar.setRollover(true);
    toolBar.setFloatable(false);
    toolBar.setOpaque(false);
    panelMovieList.add(toolBar, "2, 1, left, fill");

    // udpate datasource
    // toolBar.add(actionUpdateDataSources);
    final JSplitButton buttonUpdateDatasource = new JSplitButton(IconManager.REFRESH);
    // temp fix for size of the button
    buttonUpdateDatasource.setText("   ");
    buttonUpdateDatasource.setHorizontalAlignment(JButton.LEFT);
    // buttonScrape.setMargin(new Insets(2, 2, 2, 24));
    buttonUpdateDatasource.setSplitWidth(18);
    buttonUpdateDatasource.addSplitButtonActionListener(new SplitButtonActionListener() {
      public void buttonClicked(ActionEvent e) {
        actionUpdateDataSources.actionPerformed(e);
      }

      public void splitButtonClicked(ActionEvent e) {
        // build the popupmenu on the fly
        buttonUpdateDatasource.getPopupMenu().removeAll();
        JMenuItem item = new JMenuItem(actionUpdateDataSources2);
        buttonUpdateDatasource.getPopupMenu().add(item);
        buttonUpdateDatasource.getPopupMenu().addSeparator();
        for (String ds : Globals.settings.getMovieSettings().getMovieDataSource()) {
          buttonUpdateDatasource.getPopupMenu().add(new JMenuItem(new MovieUpdateSingleDatasourceAction(ds)));
        }

        buttonUpdateDatasource.getPopupMenu().pack();
      }
    });

    JPopupMenu popup = new JPopupMenu("popup");
    buttonUpdateDatasource.setPopupMenu(popup);
    toolBar.add(buttonUpdateDatasource);

    JSplitButton buttonScrape = new JSplitButton(IconManager.SEARCH);
    // temp fix for size of the button
    buttonScrape.setText("   ");
    buttonScrape.setHorizontalAlignment(JButton.LEFT);
    // buttonScrape.setMargin(new Insets(2, 2, 2, 24));
    buttonScrape.setSplitWidth(18);

    // register for listener
    buttonScrape.addSplitButtonActionListener(new SplitButtonActionListener() {
      public void buttonClicked(ActionEvent e) {
        actionScrape.actionPerformed(e);
      }

      public void splitButtonClicked(ActionEvent e) {
      }
    });

    popup = new JPopupMenu("popup");
    JMenuItem item = new JMenuItem(actionScrape2);
    popup.add(item);
    item = new JMenuItem(actionScrapeUnscraped);
    popup.add(item);
    item = new JMenuItem(actionScrapeSelected);
    popup.add(item);
    buttonScrape.setPopupMenu(popup);
    toolBar.add(buttonScrape);

    toolBar.add(actionEditMovie);

    btnRen = new JButton("REN");
    btnRen.setAction(actionRename);
    toolBar.add(btnRen);

    btnMediaInformation = new JButton("MI");
    btnMediaInformation.setAction(actionMediaInformation);
    toolBar.add(btnMediaInformation);

    // textField = new JTextField();
    textField = new JSearchTextField();
    panelMovieList.add(textField, "3, 1, right, bottom");
    textField.setColumns(13);

    // table = new JTable();
    // build JTable

    MatcherEditor<Movie> textMatcherEditor = new TextComponentMatcherEditor<Movie>(textField, new MovieFilterator());
    MovieMatcherEditor movieMatcherEditor = new MovieMatcherEditor();
    FilterList<Movie> extendedFilteredMovies = new FilterList<Movie>(sortedMovies, movieMatcherEditor);
    textFilteredMovies = new FilterList<Movie>(extendedFilteredMovies, textMatcherEditor);
    movieSelectionModel = new MovieSelectionModel(sortedMovies, textFilteredMovies, movieMatcherEditor);
    movieTableModel = new DefaultEventTableModel<Movie>(GlazedListsSwing.swingThreadProxyList(textFilteredMovies), new MovieTableFormat());
    table = new ZebraJTable(movieTableModel);

    movieTableModel.addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent arg0) {
        lblMovieCountFiltered.setText(String.valueOf(movieTableModel.getRowCount()));
        // select first movie if nothing is selected
        ListSelectionModel selectionModel = table.getSelectionModel();
        if (selectionModel.isSelectionEmpty() && movieTableModel.getRowCount() > 0) {
          selectionModel.setSelectionInterval(0, 0);
        }
        if (selectionModel.isSelectionEmpty() && movieTableModel.getRowCount() == 0) {
          movieSelectionModel.setSelectedMovie(null);
        }
      }
    });

    // install and save the comparator on the Table
    movieSelectionModel.setTableComparatorChooser(TableComparatorChooser.install(table, sortedMovies, TableComparatorChooser.SINGLE_COLUMN));

    // table = new MyTable();
    table.setFont(new Font("Dialog", Font.PLAIN, 11));
    // scrollPane.setViewportView(table);

    // JScrollPane scrollPane = new JScrollPane(table);
    JScrollPane scrollPane = ZebraJTable.createStripedJScrollPane(table);
    panelMovieList.add(scrollPane, "2, 3, 4, 1, fill, fill");

    JToggleButton filterButton = new JToggleButton(IconManager.FILTER);
    filterButton.setToolTipText(BUNDLE.getString("movieextendedsearch.options")); //$NON-NLS-1$
    panelMovieList.add(filterButton, "5, 1, right, bottom");

    panelExtendedSearch = new MovieExtendedSearchPanel(movieSelectionModel);
    panelExtendedSearch.setVisible(false);
    // panelMovieList.add(panelExtendedSearch, "2, 5, 2, 1, fill, fill");
    filterButton.addActionListener(new ActionListener() {
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

    JPanel panelStatus = new JPanel();
    panelMovieList.add(panelStatus, "2, 6, 2, 1");
    panelStatus.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("1px"),
        ColumnSpec.decode("146px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { RowSpec
        .decode("fill:default:grow"), }));

    panelMovieCount = new JPanel();
    panelStatus.add(panelMovieCount, "3, 1, left, fill");

    lblMovieCount = new JLabel(BUNDLE.getString("tmm.movies") + ":"); //$NON-NLS-1$
    panelMovieCount.add(lblMovieCount);

    lblMovieCountFiltered = new JLabel("");
    panelMovieCount.add(lblMovieCountFiltered);

    lblMovieCountOf = new JLabel(BUNDLE.getString("tmm.of")); //$NON-NLS-1$
    panelMovieCount.add(lblMovieCountOf);

    lblMovieCountTotal = new JLabel("");
    panelMovieCount.add(lblMovieCountTotal);

    JLayeredPane layeredPaneRight = new JLayeredPane();
    layeredPaneRight.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default"), ColumnSpec.decode("default:grow") }, new RowSpec[] {
        RowSpec.decode("default"), RowSpec.decode("default:grow") }));
    panelRight = new MovieInformationPanel(movieSelectionModel);
    layeredPaneRight.add(panelRight, "1, 1, 2, 2, fill, fill");
    layeredPaneRight.setLayer(panelRight, 0);

    // glass pane
    layeredPaneRight.add(panelExtendedSearch, "1, 1, fill, fill");
    layeredPaneRight.setLayer(panelExtendedSearch, 1);

    splitPaneHorizontal.setRightComponent(layeredPaneRight);
    splitPaneHorizontal.setContinuousLayout(true);

    // beansbinding init
    initDataBindings();

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
  }

  private void buildMenu() {
    // disable donator functions
    if (!Globals.isDonator()) {
      actionRenamerPreview.setEnabled(false);
      actionAssignMovieSets.setEnabled(false);
    }
    // menu items
    menu.add(actionUpdateDataSources2);
    final JMenu menuUpdateDatasources = new JMenu(BUNDLE.getString("update.datasource")); //$NON-NLS-1$
    final JMenu menuFindMissingMovies = new JMenu(BUNDLE.getString("movie.findmissing")); //$NON-NLS-1$
    menuUpdateDatasources.addMenuListener(new MenuListener() {
      @Override
      public void menuCanceled(MenuEvent arg0) {
      }

      @Override
      public void menuDeselected(MenuEvent arg0) {
      }

      @Override
      public void menuSelected(MenuEvent arg0) {
        menuUpdateDatasources.removeAll();
        for (String ds : Globals.settings.getMovieSettings().getMovieDataSource()) {
          JMenuItem item = new JMenuItem(new MovieUpdateSingleDatasourceAction(ds));
          menuUpdateDatasources.add(item);

          item = new JMenuItem(new MovieFindMissingAction(ds));
          menuFindMissingMovies.add(item);

        }
      }
    });
    menu.add(menuUpdateDatasources);

    menu.add(new MovieFindMissingAction());
    menu.add(menuFindMissingMovies);

    menu.addSeparator();

    JMenu menuScrape = new JMenu(BUNDLE.getString("Button.scrape")); //$NON-NLS-1$
    menuScrape.add(actionScrape2);
    menuScrape.add(actionScrapeSelected);
    menuScrape.add(actionScrapeUnscraped);
    menuScrape.add(actionScrapeMetadataSelected);
    menuScrape.add(actionAssignMovieSets);
    menu.add(menuScrape);

    JMenu menuEdit = new JMenu(BUNDLE.getString("Button.edit")); //$NON-NLS-1$
    menuEdit.add(actionEditMovie2);
    menuEdit.add(actionBatchEdit);
    menuEdit.add(actionRename2);
    menuEdit.add(actionRenamerPreview);

    menu.add(menuEdit);
    menu.add(actionRewriteNfo);
    menu.addSeparator();
    menu.add(actionMediaInformation2);
    menu.add(actionExport);
    menu.add(actionRemove2);
    menu.addSeparator();
    menu.add(actionClearImageCache);

    // popup menu
    JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.add(actionScrape2);
    popupMenu.add(actionScrapeSelected);
    popupMenu.add(actionScrapeMetadataSelected);
    popupMenu.add(actionAssignMovieSets);
    popupMenu.addSeparator();
    popupMenu.add(actionEditMovie2);
    popupMenu.add(actionBatchEdit);
    popupMenu.add(actionRewriteNfo);
    popupMenu.add(actionRename2);
    popupMenu.add(actionRenamerPreview);
    popupMenu.add(actionMediaInformation2);
    popupMenu.add(actionExport);
    popupMenu.addSeparator();
    popupMenu.add(actionClearImageCache);
    popupMenu.addSeparator();
    popupMenu.add(actionRemove2);

    MouseListener mouseListener = new MovieTableMouseListener(popupMenu, table);
    table.addMouseListener(mouseListener);
  }

  /**
   * further initializations.
   */
  private void init() {
    // build menu
    buildMenu();

    // moviename column
    table.getColumnModel().getColumn(0).setCellRenderer(new BorderCellRenderer());
    table.getColumnModel().getColumn(0).setIdentifier("title"); //$NON-NLS-1$

    // year column
    table.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(35);
    table.getTableHeader().getColumnModel().getColumn(1).setMinWidth(35);
    table.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(50);
    table.getTableHeader().getColumnModel().getColumn(1).setIdentifier("year"); //$NON-NLS-1$

    // NFO column
    table.getTableHeader().getColumnModel().getColumn(2).setHeaderRenderer(new IconRenderer(BUNDLE.getString("tmm.nfo"))); //$NON-NLS-1$
    table.getTableHeader().getColumnModel().getColumn(2).setMaxWidth(20);
    table.getColumnModel().getColumn(2).setHeaderValue(IconManager.INFO);
    table.getTableHeader().getColumnModel().getColumn(2).setIdentifier("nfo"); //$NON-NLS-1$

    // Images column
    table.getTableHeader().getColumnModel().getColumn(3).setHeaderRenderer(new IconRenderer(BUNDLE.getString("tmm.images"))); //$NON-NLS-1$
    table.getTableHeader().getColumnModel().getColumn(3).setMaxWidth(20);
    table.getColumnModel().getColumn(3).setHeaderValue(IconManager.IMAGE);
    table.getTableHeader().getColumnModel().getColumn(3).setIdentifier("images"); //$NON-NLS-1$

    // trailer column
    table.getTableHeader().getColumnModel().getColumn(4).setHeaderRenderer(new IconRenderer(BUNDLE.getString("tmm.trailer"))); //$NON-NLS-1$
    table.getTableHeader().getColumnModel().getColumn(4).setMaxWidth(20);
    table.getColumnModel().getColumn(4).setHeaderValue(IconManager.CLAPBOARD);
    table.getTableHeader().getColumnModel().getColumn(4).setIdentifier("trailer"); //$NON-NLS-1$

    // subtitles column
    table.getTableHeader().getColumnModel().getColumn(5).setHeaderRenderer(new IconRenderer(BUNDLE.getString("tmm.subtitles"))); //$NON-NLS-1$
    table.getTableHeader().getColumnModel().getColumn(5).setMaxWidth(20);
    table.getColumnModel().getColumn(5).setHeaderValue(IconManager.SUBTITLE);
    table.getTableHeader().getColumnModel().getColumn(5).setIdentifier("subtitle"); //$NON-NLS-1$

    table.setSelectionModel(movieSelectionModel.getSelectionModel());
    // selecting first movie at startup
    if (movieList.getMovies() != null && movieList.getMovies().size() > 0) {
      ListSelectionModel selectionModel = table.getSelectionModel();
      if (selectionModel.isSelectionEmpty()) {
        selectionModel.setSelectionInterval(0, 0);
      }
    }

    // hide columns if needed
    if (!Globals.settings.getMovieSettings().isYearColumnVisible()) {
      table.hideColumn("year"); //$NON-NLS-1$
    }
    if (!Globals.settings.getMovieSettings().isNfoColumnVisible()) {
      table.hideColumn("nfo"); //$NON-NLS-1$
    }
    if (!Globals.settings.getMovieSettings().isImageColumnVisible()) {
      table.hideColumn("images"); //$NON-NLS-1$
    }
    if (!Globals.settings.getMovieSettings().isTrailerColumnVisible()) {
      table.hideColumn("trailer"); //$NON-NLS-1$
    }
    if (!Globals.settings.getMovieSettings().isSubtitleColumnVisible()) {
      table.hideColumn("subtitle"); //$NON-NLS-1$
    }

    // and add a propertychangelistener to the columnhider
    PropertyChangeListener settingsPropertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof MovieSettings) {
          if ("yearColumnVisible".equals(evt.getPropertyName())) {
            setColumnVisibility("year", (Boolean) evt.getNewValue()); //$NON-NLS-1$
          }
          if ("nfoColumnVisible".equals(evt.getPropertyName())) {
            setColumnVisibility("nfo", (Boolean) evt.getNewValue());
          }
          if ("imageColumnVisible".equals(evt.getPropertyName())) {
            setColumnVisibility("images", (Boolean) evt.getNewValue()); //$NON-NLS-1$
          }
          if ("trailerColumnVisible".equals(evt.getPropertyName())) {
            setColumnVisibility("trailer", (Boolean) evt.getNewValue()); //$NON-NLS-1$
          }
          if ("subtitleColumnVisible".equals(evt.getPropertyName())) {
            setColumnVisibility("subtitle", (Boolean) evt.getNewValue()); //$NON-NLS-1$
          }
        }
      }

      private void setColumnVisibility(Object identifier, Boolean visible) {
        if (visible) {
          table.showColumn(identifier);
        }
        else {
          table.hideColumn(identifier);
        }

      }
    };

    Globals.settings.getMovieSettings().addPropertyChangeListener(settingsPropertyChangeListener);

    // initialize filteredCount
    lblMovieCountFiltered.setText(String.valueOf(movieTableModel.getRowCount()));

    addKeyListener();
  }

  /**
   * Gets the split pane horizontal.
   * 
   * @return the split pane horizontal
   */
  public JSplitPane getSplitPaneHorizontal() {
    return splitPaneHorizontal;
  }

  /**
   * Gets the split pane vertical.
   * 
   * @return the split pane vertical
   */
  public JSplitPane getSplitPaneVertical() {
    return panelRight.getSplitPaneVertical();
  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    //
    BeanProperty<MovieList, Integer> movieListBeanProperty = BeanProperty.create("movieCount");
    AutoBinding<MovieList, Integer, JLabel, String> autoBinding_20 = Bindings.createAutoBinding(UpdateStrategy.READ, movieList,
        movieListBeanProperty, lblMovieCountTotal, jLabelBeanProperty);
    autoBinding_20.bind();
    //
  }

  private void addKeyListener() {
    table.addKeyListener(new KeyListener() {
      private long   lastKeypress = 0;
      private String searchTerm   = "";

      @Override
      public void keyTyped(KeyEvent arg0) {
        long now = System.currentTimeMillis();
        if (now - lastKeypress > 500) {
          searchTerm = "";
        }
        lastKeypress = now;

        if (arg0.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
          searchTerm += arg0.getKeyChar();
        }

        if (StringUtils.isNotBlank(searchTerm)) {
          TableModel model = table.getModel();
          for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0) instanceof Movie) {
              String title = ((Movie) model.getValueAt(i, 0)).getTitleSortable().toLowerCase();
              if (title.startsWith(searchTerm)) {
                ListSelectionModel selectionModel = table.getSelectionModel();
                selectionModel.setSelectionInterval(i, i);
                table.scrollRectToVisible(new Rectangle(table.getCellRect(i, 0, true)));
                break;
              }
            }
          }
        }
      }

      @Override
      public void keyReleased(KeyEvent arg0) {
      }

      @Override
      public void keyPressed(KeyEvent arg0) {
      }
    });
  }
}
