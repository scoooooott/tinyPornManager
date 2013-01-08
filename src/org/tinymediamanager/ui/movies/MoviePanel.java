/*
 * Copyright 2012 Manuel Laggner
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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
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
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.gpl.JSplitButton.JSplitButton;
import org.gpl.JSplitButton.action.SplitButtonActionListener;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.WindowConfig;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieCast;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieRenamer;
import org.tinymediamanager.core.movie.MovieScrapeTask;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.ui.BorderCellRenderer;
import org.tinymediamanager.ui.CertificationImageConverter;
import org.tinymediamanager.ui.IconRenderer;
import org.tinymediamanager.ui.ImageLabel;
import org.tinymediamanager.ui.JSearchTextField;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.MyTable;
import org.tinymediamanager.ui.StarRater;
import org.tinymediamanager.ui.VoteCountConverter;
import org.tinymediamanager.ui.WatchedIconConverter;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MoviePanel.
 */
public class MoviePanel extends JPanel {

  /** The Constant serialVersionUID. */
  private static final long      serialVersionUID         = 1L;

  /** The logger. */
  private final static Logger    LOGGER                   = Logger.getLogger(MoviePanel.class);

  /** The movie list. */
  private MovieList              movieList;

  /** The text field. */
  private JTextField             textField;

  /** The table. */
  private JTable                 table;

  /** The action update data sources. */
  private final Action           actionUpdateDataSources  = new UpdateDataSourcesAction(false);

  /** The action update data sources. */
  private final Action           actionUpdateDataSources2 = new UpdateDataSourcesAction(true);

  /** The action scrape. */
  private final Action           actionScrape             = new SingleScrapeAction(false);

  /** The action scrape. */
  private final Action           actionScrape2            = new SingleScrapeAction(true);

  /** The text pane. */
  private JTextPane              textPane;

  /** The lbl movie name. */
  private JLabel                 lblMovieName;

  /** The lbl movie background. */
  private ImageLabel             lblMovieBackground;

  /** The lbl movie poster. */
  private ImageLabel             lblMoviePoster;

  /** The table cast. */
  private JTable                 tableCast;

  /** The lbl original name. */
  private JLabel                 lblTagline;

  /** The action edit movie. */
  private final Action           actionEditMovie          = new EditAction(false);

  /** The action edit movie. */
  private final Action           actionEditMovie2         = new EditAction(true);

  /** The action scrape unscraped movies. */
  private final Action           actionScrapeUnscraped    = new UnscrapedScrapeAction();

  /** The action scrape selected movies. */
  private final Action           actionScrapeSelected     = new SelectedScrapeAction();

  /** The action rename. */
  private final Action           actionRename             = new RenameAction(false);

  /** The action rename2. */
  private final Action           actionRename2            = new RenameAction(true);

  /** The action remove2. */
  private final Action           actionRemove2            = new RemoveAction(true);

  /** The panel rating. */
  private StarRater              panelRatingStars;

  /** The label progressAction. */
  private JLabel                 lblProgressAction;

  /** The progress bar. */
  private JProgressBar           progressBar;

  /** The scrape task. */
  private MovieScrapeTask        scrapeTask;

  /** The label rating. */
  private JLabel                 lblRating;

  /** The button cancelScraper. */
  private JButton                btnCancelScraper;

  /** The panel progress bar. */
  private JPanel                 panelProgressBar;

  /** The panel movie count. */
  private JPanel                 panelMovieCount;

  /** The lbl movie count. */
  private JLabel                 lblMovieCount;

  /** The lbl movie count int. */
  private JLabel                 lblMovieCountTotal;

  /** The panel top. */
  private JPanel                 panelTop;

  /** The tabbed pane movie details. */
  private JTabbedPane            tabbedPaneMovieDetails;

  /** The panel overview. */
  private JPanel                 panelOverview;

  /** The panel movie cast. */
  private JPanel                 panelMovieCast;

  /** The panel details. */
  private JPanel                 panelDetails;

  /** The lbl director t. */
  private JLabel                 lblDirectorT;

  /** The lbl director. */
  private JLabel                 lblDirector;

  /** The lbl writer t. */
  private JLabel                 lblWriterT;

  /** The lbl writer. */
  private JLabel                 lblWriter;

  /** The lbl actors. */
  private JLabel                 lblActors;

  /** The lbl vote count. */
  private JLabel                 lblVoteCount;

  /** The btn ren. */
  private JButton                btnRen;

  /** The panel media information. */
  private JPanel                 panelMediaInformation;

  /** The lbl actor thumb. */
  private ImageLabel             lblActorThumb;

  /** The menu. */
  private JMenu                  menu;

  /** The movie table model. */
  private EventTableModel<Movie> movieTableModel;

  /** The movie selection model. */
  private MovieSelectionModel    movieSelectionModel;

  /** The sorted movies. */
  private SortedList<Movie>      sortedMovies;

  /** The text filtered movies. */
  private FilterList<Movie>      textFilteredMovies;

  /** The panel extended search. */
  private JPanel                 panelExtendedSearch;

  /** The panel movie trailer. */
  private MovieTrailerPanel      panelMovieTrailer;

  /** The panel movie logos. */
  private JPanel                 panelMovieLogos;

  /** The lbl certification image. */
  private JLabel                 lblCertificationImage;

  /** The lbl movie count of. */
  private JLabel                 lblMovieCountOf;

  /** The lbl movie count filtered. */
  private JLabel                 lblMovieCountFiltered;

  /** The lbl watched image. */
  private JLabel                 lblWatchedImage;

  /** The panel watched image. */
  private JPanel                 panelWatchedImage;

  /** The split pane horizontal. */
  private JSplitPane             splitPaneHorizontal;

  /** The split pane vertical. */
  private JSplitPane             splitPaneVertical;

  private WindowConfig           windowConfig;

  /**
   * Create the panel.
   */
  public MoviePanel() {
    super();
    // load movielist
    LOGGER.debug("loading MovieList");
    movieList = MovieList.getInstance();
    sortedMovies = new SortedList<Movie>(movieList.getMovies(), new MovieComparator());
    sortedMovies.setMode(SortedList.AVOID_MOVING_ELEMENTS);
    // movieSelectionModel = new MovieSelectionModel(sortedMovies);
    windowConfig = Globals.settings.getWindowConfig();

    // build menu
    menu = new JMenu("Movies");
    JFrame mainFrame = MainWindow.getFrame();
    JMenuBar menuBar = mainFrame.getJMenuBar();
    menuBar.add(menu);

    setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), FormFactory.DEFAULT_ROWSPEC, }));

    splitPaneHorizontal = new JSplitPane();
    splitPaneHorizontal.setContinuousLayout(true);
    add(splitPaneHorizontal, "2, 2, fill, fill");

    JPanel panelMovieList = new JPanel();
    splitPaneHorizontal.setLeftComponent(panelMovieList);
    panelMovieList.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("200px:grow"),
        ColumnSpec.decode("150px:grow"), }, new RowSpec[] { RowSpec.decode("26px"), FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("fill:max(200px;default):grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JToolBar toolBar = new JToolBar();
    toolBar.setRollover(true);
    toolBar.setFloatable(false);
    toolBar.setOpaque(false);
    panelMovieList.add(toolBar, "2, 1, left, fill");

    JButton buttonUpdateDataSources = toolBar.add(actionUpdateDataSources);
    JSplitButton buttonScrape = new JSplitButton(new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
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

    JPopupMenu popup = new JPopupMenu("popup");
    JMenuItem item = new JMenuItem(actionScrape2);
    popup.add(item);
    item = new JMenuItem(actionScrapeUnscraped);
    popup.add(item);
    item = new JMenuItem(actionScrapeSelected);
    popup.add(item);
    buttonScrape.setPopupMenu(popup);
    toolBar.add(buttonScrape);

    JButton buttonEdit = toolBar.add(actionEditMovie);

    btnRen = new JButton("REN");
    btnRen.setAction(actionRename);
    toolBar.add(btnRen);

    // textField = new JTextField();
    textField = new JSearchTextField();
    panelMovieList.add(textField, "3, 1, right, bottom");
    textField.setColumns(10);

    // table = new JTable();
    // build JTable

    MatcherEditor<Movie> textMatcherEditor = new TextComponentMatcherEditor<Movie>(textField, new MovieFilterator());
    MovieMatcherEditor movieMatcherEditor = new MovieMatcherEditor();
    FilterList<Movie> extendedFilteredMovies = new FilterList<Movie>(sortedMovies, movieMatcherEditor);
    textFilteredMovies = new FilterList<Movie>(extendedFilteredMovies, textMatcherEditor);
    movieSelectionModel = new MovieSelectionModel(sortedMovies, textFilteredMovies, movieMatcherEditor);
    movieTableModel = new EventTableModel<Movie>(textFilteredMovies, new MovieTableFormat());
    table = new MyTable(movieTableModel);

    movieTableModel.addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent arg0) {
        lblMovieCountFiltered.setText(String.valueOf(movieTableModel.getRowCount()));
        // select first movie if nothing is selected
        ListSelectionModel selectionModel = table.getSelectionModel();
        if (selectionModel.isSelectionEmpty() && movieTableModel.getRowCount() > 0) {
          selectionModel.setSelectionInterval(0, 0);
        }
      }
    });

    // install and save the comparator on the Table
    movieSelectionModel.setTableComparatorChooser(TableComparatorChooser.install(table, sortedMovies, TableComparatorChooser.SINGLE_COLUMN));

    // table = new MyTable();
    table.setFont(new Font("Dialog", Font.PLAIN, 11));
    // scrollPane.setViewportView(table);

    // JScrollPane scrollPane = new JScrollPane(table);
    JScrollPane scrollPane = MyTable.createStripedJScrollPane(table);
    panelMovieList.add(scrollPane, "2, 3, 2, 1, fill, fill");

    panelExtendedSearch = new MovieExtendedSearchPanel(movieSelectionModel);
    panelMovieList.add(panelExtendedSearch, "2, 5, 2, 1, fill, fill");

    JPanel panelRight = new JPanel();
    splitPaneHorizontal.setRightComponent(panelRight);
    panelRight
        .setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("650px:grow"), }, new RowSpec[] { RowSpec.decode("fill:default:grow"), }));

    splitPaneVertical = new JSplitPane();
    splitPaneVertical.setBorder(null);
    splitPaneVertical.setResizeWeight(0.9);
    splitPaneVertical.setContinuousLayout(true);
    splitPaneVertical.setOneTouchExpandable(true);
    splitPaneVertical.setOrientation(JSplitPane.VERTICAL_SPLIT);
    splitPaneHorizontal.setContinuousLayout(true);
    panelRight.add(splitPaneVertical, "1, 1, fill, fill");

    panelTop = new JPanel();
    panelTop.setBorder(null);
    splitPaneVertical.setTopComponent(panelTop);
    // panelMovieDetails.add(panelMovieHeaderImages, "2, 2, fill, fill");
    panelTop.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("300px:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { RowSpec.decode("fill:default"),
        RowSpec.decode("top:pref:grow"), }));

    JPanel panelMovieHeader = new JPanel();
    panelTop.add(panelMovieHeader, "2, 1, 3, 1, fill, top");
    panelMovieHeader.setBorder(null);
    panelMovieHeader.setLayout(new BorderLayout(0, 0));

    JPanel panelMovieTitle = new JPanel();
    panelMovieHeader.add(panelMovieTitle, BorderLayout.NORTH);
    panelMovieTitle.setLayout(new BorderLayout(0, 0));
    lblMovieName = new JLabel("");
    // panelMovieHeader.add(lblMovieName, BorderLayout.NORTH);
    panelMovieTitle.add(lblMovieName);
    lblMovieName.setFont(new Font("Dialog", Font.BOLD, 16));

    panelWatchedImage = new JPanel();
    panelMovieTitle.add(panelWatchedImage, BorderLayout.EAST);

    lblWatchedImage = new JLabel("");
    panelWatchedImage.add(lblWatchedImage);

    JPanel panelRatingTagline = new JPanel();
    panelMovieHeader.add(panelRatingTagline, BorderLayout.CENTER);
    panelRatingTagline.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("24px"), FormFactory.DEFAULT_ROWSPEC, }));

    lblRating = new JLabel("");
    panelRatingTagline.add(lblRating, "2, 2, left, center");

    lblVoteCount = new JLabel("");
    panelRatingTagline.add(lblVoteCount, "3, 2, left, center");

    panelRatingStars = new StarRater(5, 2);
    panelRatingTagline.add(panelRatingStars, "1, 2, left, top");
    panelRatingStars.setEnabled(false);

    lblTagline = new JLabel();
    panelRatingTagline.add(lblTagline, "1, 3, 3, 1, default, center");

    panelMovieLogos = new JPanel();
    panelMovieHeader.add(panelMovieLogos, BorderLayout.EAST);

    lblCertificationImage = new JLabel();
    panelMovieLogos.add(lblCertificationImage);

    JLayeredPane layeredPaneImages = new JLayeredPane();
    panelTop.add(layeredPaneImages, "1, 2, 4, 1, fill, fill");
    layeredPaneImages.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("max(10px;default)"), ColumnSpec.decode("left:120px"),
        ColumnSpec.decode("default:grow"), }, new RowSpec[] { RowSpec.decode("max(10px;default)"), RowSpec.decode("top:180px"),
        RowSpec.decode("fill:default:grow"), }));

    lblMovieBackground = new ImageLabel(false, true);
    layeredPaneImages.add(lblMovieBackground, "1, 1, 3, 3, fill, fill");

    lblMoviePoster = new ImageLabel();
    layeredPaneImages.setLayer(lblMoviePoster, 1);
    layeredPaneImages.add(lblMoviePoster, "2, 2, fill, fill");

    JPanel panelGenres = new MovieGenresPanel(movieSelectionModel);
    layeredPaneImages.setLayer(panelGenres, 2);
    layeredPaneImages.add(panelGenres, "2, 2, 2, 2, right, bottom");

    JPanel panelBottom = new JPanel();
    panelBottom.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("496px:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
        RowSpec.decode("fill:default:grow"), }));

    tabbedPaneMovieDetails = new JTabbedPane(JTabbedPane.TOP);
    panelBottom.add(tabbedPaneMovieDetails, "1, 2, fill, fill");
    splitPaneVertical.setBottomComponent(panelBottom);

    panelDetails = new MovieDetailsPanel(movieSelectionModel);
    tabbedPaneMovieDetails.addTab("Details", null, panelDetails, null);

    panelOverview = new JPanel();
    tabbedPaneMovieDetails.addTab("Overview", null, panelOverview, null);
    panelOverview.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("241px:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
        RowSpec.decode("fill:default:grow"), }));
    // panelMovieDetails.add(tabbedPaneMovieDetails, "2, 3, fill, fill");

    JScrollPane scrollPaneOverview = new JScrollPane();
    panelOverview.add(scrollPaneOverview, "1, 2, fill, fill");

    textPane = new JTextPane();
    scrollPaneOverview.setViewportView(textPane);

    panelMovieCast = new JPanel();
    panelMovieCast.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(39dlu;default)"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("125px"),
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("80px"),
        RowSpec.decode("default:grow"), FormFactory.NARROW_LINE_GAP_ROWSPEC, }));

    lblDirectorT = new JLabel("Director");
    panelMovieCast.add(lblDirectorT, "2, 2");

    lblDirector = new JLabel("");
    lblDirectorT.setLabelFor(lblDirector);
    panelMovieCast.add(lblDirector, "4, 2, 3, 1");

    lblWriterT = new JLabel("Writer");
    panelMovieCast.add(lblWriterT, "2, 4");

    lblWriter = new JLabel("");
    lblWriterT.setLabelFor(lblWriter);
    panelMovieCast.add(lblWriter, "4, 4, 3, 1");

    tabbedPaneMovieDetails.addTab("Cast", null, panelMovieCast, null);

    lblActors = new JLabel("Actors");
    panelMovieCast.add(lblActors, "2, 6, default, top");

    JScrollPane scrollPaneMovieCast = new JScrollPane();
    lblActors.setLabelFor(scrollPaneMovieCast);
    panelMovieCast.add(scrollPaneMovieCast, "4, 6, 1, 2");

    tableCast = new JTable();
    scrollPaneMovieCast.setViewportView(tableCast);

    lblActorThumb = new ImageLabel();
    panelMovieCast.add(lblActorThumb, "6, 2, 1, 6, fill, fill");

    panelMediaInformation = new MovieMediaInformationPanel(movieSelectionModel);
    tabbedPaneMovieDetails.addTab("Media Information", null, panelMediaInformation, null);

    panelMovieTrailer = new MovieTrailerPanel(movieSelectionModel);
    tabbedPaneMovieDetails.addTab("Trailer", null, panelMovieTrailer, null);

    JPanel panelStatus = new JPanel();
    add(panelStatus, "2, 3, fill, fill");
    panelStatus.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("1px"),
        ColumnSpec.decode("146px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { RowSpec
        .decode("fill:default:grow"), }));

    panelMovieCount = new JPanel();
    panelStatus.add(panelMovieCount, "3, 1, left, fill");

    lblMovieCount = new JLabel("Movies:");
    panelMovieCount.add(lblMovieCount);

    lblMovieCountFiltered = new JLabel("");
    panelMovieCount.add(lblMovieCountFiltered);

    lblMovieCountOf = new JLabel("of");
    panelMovieCount.add(lblMovieCountOf);

    lblMovieCountTotal = new JLabel("");
    panelMovieCount.add(lblMovieCountTotal);

    panelProgressBar = new JPanel();
    panelStatus.add(panelProgressBar, "5, 1, right, fill");

    lblProgressAction = new JLabel("");
    panelProgressBar.add(lblProgressAction);

    progressBar = new JProgressBar();
    panelProgressBar.add(progressBar);

    btnCancelScraper = new JButton("");
    panelProgressBar.add(btnCancelScraper);
    btnCancelScraper.setVisible(false);
    btnCancelScraper.setContentAreaFilled(false);
    btnCancelScraper.setBorderPainted(false);
    btnCancelScraper.setBorder(null);
    btnCancelScraper.setMargin(new Insets(0, 0, 0, 0));
    btnCancelScraper.setIcon(new ImageIcon(MoviePanel.class.getResource("/org/tinymediamanager/ui/images/Button_Stop.png")));
    btnCancelScraper.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        // scrapeTask.cancel(false);
        scrapeTask.cancel();
      }
    });
    progressBar.setVisible(false);

    // beansbinding init
    initDataBindings();

    // menu items
    menu.add(actionUpdateDataSources2);
    menu.addSeparator();
    menu.add(actionScrape2);
    menu.add(actionScrapeSelected);
    menu.add(actionScrapeUnscraped);
    menu.addSeparator();
    menu.add(actionEditMovie2);
    menu.add(actionRename2);
    menu.add(actionRemove2);
    menu.addSeparator();

    // debug menu
    JMenu debug = new JMenu("Debug");
    JMenuItem clearDatabase = new JMenuItem("clear database");
    debug.add(clearDatabase);
    clearDatabase.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        // delete all movies from the database
        MovieList movieList = MovieList.getInstance();
        movieList.removeMovies();
        JOptionPane.showMessageDialog(null, "Database cleared. Please restart tinyMediaManager");
      }
    });
    JMenuItem clearCache = new JMenuItem("clear cache");
    debug.add(clearCache);
    clearCache.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        File cache = new File("cache");
        if (cache.exists()) {
          try {
            FileUtils.deleteDirectory(cache);
          }
          catch (Exception e) {
          }
        }
      }
    });

    menu.add(debug);

    // popup menu
    JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.add(actionScrape2);
    popupMenu.add(actionScrapeSelected);
    popupMenu.addSeparator();
    popupMenu.add(actionEditMovie2);
    popupMenu.add(actionRename2);
    popupMenu.add(actionRemove2);

    MouseListener popupListener = new PopupListener(popupMenu);
    table.addMouseListener(popupListener);

    // moviename column
    table.getColumnModel().getColumn(0).setCellRenderer(new BorderCellRenderer());

    // year column
    table.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(35);
    table.getTableHeader().getColumnModel().getColumn(1).setMinWidth(35);
    table.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(50);

    // NFO column
    table.getTableHeader().getColumnModel().getColumn(2).setHeaderRenderer(new IconRenderer());
    table.getTableHeader().getColumnModel().getColumn(2).setMaxWidth(20);
    URL imageURL = MainWindow.class.getResource("images/File.png");
    if (imageURL != null) {
      table.getColumnModel().getColumn(2).setHeaderValue(new ImageIcon(imageURL));
    }

    // poster column
    table.getTableHeader().getColumnModel().getColumn(3).setHeaderRenderer(new IconRenderer());
    table.getTableHeader().getColumnModel().getColumn(3).setMaxWidth(20);
    imageURL = null;
    imageURL = MainWindow.class.getResource("images/Image.png");
    if (imageURL != null) {
      table.getColumnModel().getColumn(3).setHeaderValue(new ImageIcon(imageURL));
    }

    table.setSelectionModel(movieSelectionModel.getSelectionModel());
    // table.getSelectionModel().addListSelectionListener(movieSelectionModel);
    // TableRowSorter sorter = new TableRowSorter(table.getModel());
    // table.setRowSorter(sorter);

    // selecting first movie at startup
    if (movieList.getMovies() != null && movieList.getMovies().size() > 0) {
      ListSelectionModel selectionModel = table.getSelectionModel();
      if (selectionModel.isSelectionEmpty()) {
        selectionModel.setSelectionInterval(0, 0);
      }

      if (tableCast.getModel().getRowCount() > 0) {
        tableCast.getSelectionModel().setSelectionInterval(0, 0);
      }
      else {
        lblActorThumb.setImageUrl("");
      }
    }

    // changes upon movie selection
    tableCast.getModel().addTableModelListener(new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        // change to the first actor on movie change
        if (tableCast.getModel().getRowCount() > 0) {
          tableCast.getSelectionModel().setSelectionInterval(0, 0);
        }
        else {
          lblActorThumb.setImageUrl("");
        }
      }
    });

    // initialize filteredCount
    lblMovieCountFiltered.setText(String.valueOf(movieTableModel.getRowCount()));
  }

  /**
   * The Class UpdateDataSourcesAction.
   */
  private class UpdateDataSourcesAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new UpdateDataSourcesAction.
     * 
     * @param withTitle
     *          the with title
     */
    public UpdateDataSourcesAction(boolean withTitle) {
      // putValue(NAME, "UDS");
      if (withTitle) {
        putValue(NAME, "Update data sources");
        putValue(LARGE_ICON_KEY, "");
      }
      else {
        putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Folder-Sync.png")));
        putValue(SHORT_DESCRIPTION, "Update data sources");
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      UpdateDataSourcesTask task = new UpdateDataSourcesTask();
      task.execute();
    }
  }

  /**
   * The Class SingleScrapeAction.
   */
  private class SingleScrapeAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new SingleScrapeAction.
     * 
     * @param withTitle
     *          the with title
     */
    public SingleScrapeAction(boolean withTitle) {
      if (withTitle) {
        putValue(NAME, "Scrape selected movies");
        putValue(LARGE_ICON_KEY, "");
      }
      else {
        putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
        putValue(SHORT_DESCRIPTION, "Search & scrape movie");
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      // for (int row : table.getSelectedRows()) {
      // row = table.convertRowIndexToModel(row);
      // Movie movie = movieList.getMovies().get(row);
      // MovieChooser dialogMovieChooser = new MovieChooser(movie);
      // dialogMovieChooser.pack();
      // dialogMovieChooser.setVisible(true);
      // }
      List<Movie> selectedMovies = new ArrayList<Movie>();
      // save all selected movies in an extra list (maybe scraping of one movie
      // changes the whole list)
      for (Movie movie : movieSelectionModel.getSelectedMovies()) {
        selectedMovies.add(movie);
      }
      for (Movie movie : selectedMovies) {
        MovieChooser dialogMovieChooser = new MovieChooser(movie);
        dialogMovieChooser.pack();
        dialogMovieChooser.setVisible(true);
      }
    }

  }

  /**
   * The Class UnscrapedScrapeAction.
   */
  private class UnscrapedScrapeAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new UnscrapedScrapeAction.
     */
    public UnscrapedScrapeAction() {
      putValue(NAME, "Scrape unscraped movies - force best match");
      putValue(SHORT_DESCRIPTION, "Search & scrape all unscraped movies");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {

      List<Movie> unscrapedMovies = movieList.getUnscrapedMovies();
      // scrapeTask = new ScrapeTask(unscrapedMovies);
      scrapeTask = new MovieScrapeTask(unscrapedMovies, lblProgressAction, progressBar, btnCancelScraper);
      scrapeTask.execute();
    }
  }

  /**
   * The Class UnscrapedScrapeAction.
   */
  private class SelectedScrapeAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new UnscrapedScrapeAction.
     */
    public SelectedScrapeAction() {
      putValue(NAME, "Scrape selected movies - force best match");
      putValue(SHORT_DESCRIPTION, "Search & scrape all selected movies");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      // List<Movie> selectedMovies = new ArrayList<Movie>();
      // for (int row : table.getSelectedRows()) {
      // row = table.convertRowIndexToModel(row);
      // selectedMovies.add(movieList.getMovies().get(row));
      // }
      List<Movie> selectedMovies = new ArrayList<Movie>();
      for (Movie movie : movieSelectionModel.getSelectedMovies()) {
        selectedMovies.add(movie);
      }

      if (selectedMovies.size() > 0) {
        // scrapeTask = new ScrapeTask(selectedMovies);
        scrapeTask = new MovieScrapeTask(selectedMovies, lblProgressAction, progressBar, btnCancelScraper);
        scrapeTask.execute();
      }
    }
  }

  /**
   * The Class EditAction.
   */
  private class EditAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new EditAction.
     * 
     * @param withTitle
     *          the with title
     */
    public EditAction(boolean withTitle) {
      if (withTitle) {
        putValue(LARGE_ICON_KEY, "");
        putValue(NAME, "Edit movie");
      }
      else {
        putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Pencil.png")));
        putValue(SHORT_DESCRIPTION, "Edit movie");
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      // for (int row : table.getSelectedRows()) {
      // row = table.convertRowIndexToModel(row);
      // Movie movie = movieList.getMovies().get(row);
      // MovieEditor dialogMovieEditor = new MovieEditor(movie);
      // // dialogMovieEditor.pack();
      // dialogMovieEditor.setVisible(true);
      // }
      for (Movie movie : movieSelectionModel.getSelectedMovies()) {
        MovieEditor dialogMovieEditor = new MovieEditor(movie);
        dialogMovieEditor.setVisible(true);
      }
    }
  }

  /**
   * The Class RemoveAction.
   */
  private class RemoveAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new RemoveAction.
     * 
     * @param withTitle
     *          the with title
     */
    public RemoveAction(boolean withTitle) {
      if (withTitle) {
        putValue(LARGE_ICON_KEY, "");
        putValue(NAME, "Remove selected movies");
      }
      else {
        // putValue(LARGE_ICON_KEY, new
        // ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Pencil.png")));
        putValue(SHORT_DESCRIPTION, "Remove selected movies");
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
      // List<Movie> movies = new ArrayList<Movie>();

      // // get seletected movies
      // for (int row : table.getSelectedRows()) {
      // row = table.convertRowIndexToModel(row);
      // Movie movie = movieList.getMovies().get(row);
      // movies.add(movie);
      // }

      List<Movie> movies = movieSelectionModel.getSelectedMovies();

      // remove selected movies
      if (movies.size() > 0) {
        for (int i = 0; i < movies.size(); i++) {
          movieList.removeMovie(movies.get(i));
        }
      }
    }

  }

  /**
   * The Class UpdateDataSourcesTask.
   */
  private class UpdateDataSourcesTask extends SwingWorker<Void, Void> {
    /**
     * Instantiates a new scrape task.
     * 
     */
    public UpdateDataSourcesTask() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    public Void doInBackground() {
      try {
        for (String path : Globals.settings.getMovieDataSource()) {
          startProgressBar("Updating " + path);
          movieList.findMoviesInPath(path);
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }

    /*
     * Executed in event dispatching thread
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    public void done() {
      stopProgressBar();
    }

    /**
     * Start progress bar.
     * 
     * @param description
     *          the description
     */
    private void startProgressBar(String description) {
      lblProgressAction.setText(description);
      progressBar.setVisible(true);
      progressBar.setIndeterminate(true);
      // btnCancelScraper.setVisible(true);
    }

    /**
     * Stop progress bar.
     */
    private void stopProgressBar() {
      lblProgressAction.setText("");
      progressBar.setIndeterminate(false);
      progressBar.setVisible(false);
      // btnCancelScraper.setVisible(false);
    }
  }

  /**
   * The Class RenameAction.
   */
  private class RenameAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new rename action.
     * 
     * @param withTitle
     *          the with title
     */
    public RenameAction(boolean withTitle) {
      if (withTitle) {
        putValue(LARGE_ICON_KEY, "");
        putValue(NAME, "Rename selected movies");
      }
      else {
        putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/rename-icon.png")));
        putValue(SHORT_DESCRIPTION, "rename selected movies");
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      // check if renaming options are set
      if (StringUtils.isEmpty(Globals.settings.getMovieRenamerPathname()) || StringUtils.isEmpty(Globals.settings.getMovieRenamerFilename())) {
        JOptionPane.showMessageDialog(null, "renaming options are not set");
        return;
      }
      // check is renaming options make sense
      if (!Globals.settings.getMovieRenamerPathname().contains("$") || !Globals.settings.getMovieRenamerFilename().contains("$")) {
        JOptionPane.showMessageDialog(null, "renaming options without pattern are not allowed");
        return;
      }

      // for (int row : table.getSelectedRows()) {
      // row = table.convertRowIndexToModel(row);
      // Movie movie = movieList.getMovies().get(row);
      // MovieRenamer.renameMovie(movie);
      // }
      for (Movie movie : movieSelectionModel.getSelectedMovies()) {
        MovieRenamer.renameMovie(movie);
      }
    }
  }

  /**
   * The listener interface for receiving popup events. The class that is
   * interested in processing a popup event implements this interface, and the
   * object created with that class is registered with a component using the
   * component's <code>addPopupListener<code> method. When
   * the popup event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see PopupEvent
   */
  private class PopupListener extends MouseAdapter {

    /** The popup. */
    private JPopupMenu popup;

    /**
     * Instantiates a new popup listener.
     * 
     * @param popupMenu
     *          the popup menu
     */
    PopupListener(JPopupMenu popupMenu) {
      popup = popupMenu;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e) {
      maybeShowPopup(e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent e) {
      if (table.getSelectedRow() != -1) {
        maybeShowPopup(e);
      }
    }

    /**
     * Maybe show popup.
     * 
     * @param e
     *          the e
     */
    private void maybeShowPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        popup.show(e.getComponent(), e.getX(), e.getY());
      }
    }
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
    return splitPaneVertical;
  }

  protected void initDataBindings() {
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty = BeanProperty.create("selectedMovie.nameForUi");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty, lblMovieName, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSelectionModel, Float> movieSelectionModelBeanProperty_1 = BeanProperty.create("selectedMovie.rating");
    AutoBinding<MovieSelectionModel, Float, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_1, lblRating, jLabelBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<StarRater, Float> starRaterBeanProperty = BeanProperty.create("rating");
    AutoBinding<MovieSelectionModel, Float, StarRater, Float> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_1, panelRatingStars, starRaterBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_4 = BeanProperty.create("selectedMovie.fanart");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imagePath");
    AutoBinding<MovieSelectionModel, String, ImageLabel, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_4, lblMovieBackground, imageLabelBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_5 = BeanProperty.create("selectedMovie.poster");
    AutoBinding<MovieSelectionModel, String, ImageLabel, String> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_5, lblMoviePoster, imageLabelBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_14 = BeanProperty.create("selectedMovie.overview");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, String, JTextPane, String> autoBinding_15 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_14, textPane, jTextPaneBeanProperty);
    autoBinding_15.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_15 = BeanProperty.create("selectedMovie.director");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_16 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_15, lblDirector, jLabelBeanProperty);
    autoBinding_16.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_16 = BeanProperty.create("selectedMovie.writer");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_17 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_16, lblWriter, jLabelBeanProperty);
    autoBinding_17.bind();
    //
    BeanProperty<MovieSelectionModel, List<MovieCast>> movieSelectionModelBeanProperty_17 = BeanProperty.create("selectedMovie.cast");
    JTableBinding<MovieCast, MovieSelectionModel, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_17, tableCast);
    //
    BeanProperty<MovieCast, String> movieCastBeanProperty = BeanProperty.create("name");
    jTableBinding.addColumnBinding(movieCastBeanProperty).setColumnName("Name").setEditable(false);
    //
    BeanProperty<MovieCast, String> movieCastBeanProperty_1 = BeanProperty.create("character");
    jTableBinding.addColumnBinding(movieCastBeanProperty_1).setColumnName("Character").setEditable(false);
    //
    jTableBinding.setEditable(false);
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.thumb");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty_1 = BeanProperty.create("imageUrl");
    AutoBinding<JTable, String, ImageLabel, String> autoBinding_18 = Bindings.createAutoBinding(UpdateStrategy.READ, tableCast, jTableBeanProperty,
        lblActorThumb, imageLabelBeanProperty_1);
    autoBinding_18.bind();
    //
    BeanProperty<MovieList, Integer> movieListBeanProperty = BeanProperty.create("movieCount");
    AutoBinding<MovieList, Integer, JLabel, String> autoBinding_20 = Bindings.createAutoBinding(UpdateStrategy.READ, movieList,
        movieListBeanProperty, lblMovieCountTotal, jLabelBeanProperty);
    autoBinding_20.bind();
    //
    BeanProperty<MovieSelectionModel, Boolean> movieSelectionModelBeanProperty_19 = BeanProperty.create("selectedMovie.hasRating");
    BeanProperty<StarRater, Boolean> starRaterBeanProperty_1 = BeanProperty.create("visible");
    AutoBinding<MovieSelectionModel, Boolean, StarRater, Boolean> autoBinding_21 = Bindings.createAutoBinding(UpdateStrategy.READ,
        movieSelectionModel, movieSelectionModelBeanProperty_19, panelRatingStars, starRaterBeanProperty_1);
    autoBinding_21.bind();
    //
    BeanProperty<JLabel, Boolean> jLabelBeanProperty_1 = BeanProperty.create("visible");
    AutoBinding<MovieSelectionModel, Boolean, JLabel, Boolean> autoBinding_22 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_19, lblRating, jLabelBeanProperty_1);
    autoBinding_22.bind();
    //
    AutoBinding<MovieSelectionModel, Boolean, JLabel, Boolean> autoBinding_23 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_19, lblVoteCount, jLabelBeanProperty_1);
    autoBinding_23.bind();
    //
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty_2 = BeanProperty.create("selectedMovie.votes");
    AutoBinding<MovieSelectionModel, Integer, JLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_2, lblVoteCount, jLabelBeanProperty);
    autoBinding_2.setConverter(new VoteCountConverter());
    autoBinding_2.bind();
    //
    BeanProperty<MovieSelectionModel, Certification> movieSelectionModelBeanProperty_6 = BeanProperty.create("selectedMovie.certification");
    BeanProperty<JLabel, Icon> jLabelBeanProperty_2 = BeanProperty.create("icon");
    AutoBinding<MovieSelectionModel, Certification, JLabel, Icon> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ,
        movieSelectionModel, movieSelectionModelBeanProperty_6, lblCertificationImage, jLabelBeanProperty_2);
    autoBinding_7.setConverter(new CertificationImageConverter());
    autoBinding_7.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_3 = BeanProperty.create("selectedMovie.tagline");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_3, lblTagline, jLabelBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<MovieSelectionModel, Boolean> movieSelectionModelBeanProperty_7 = BeanProperty.create("selectedMovie.watched");
    AutoBinding<MovieSelectionModel, Boolean, JLabel, Icon> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_7, lblWatchedImage, jLabelBeanProperty_2);
    autoBinding_8.setConverter(new WatchedIconConverter());
    autoBinding_8.bind();
    //
  }
}