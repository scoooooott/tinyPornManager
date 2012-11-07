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
package org.tinymediamanager.ui;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
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
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

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
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieCast;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieRenamer;
import org.tinymediamanager.scraper.MediaSearchResult;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MoviePanel.
 */
public class MoviePanel extends JPanel {

  /** The logger. */
  private final static Logger LOGGER                  = Logger.getLogger(MoviePanel.class);

  /** The movie list. */
  private MovieList           movieList;

  /** The text field. */
  private JTextField          textField;

  /** The table. */
  private JTable              table;

  /** The action update data sources. */
  private final Action        actionUpdateDataSources = new UpdateDataSourcesAction();

  /** The action scrape. */
  private final Action        actionScrape            = new SingleScrapeAction();

  /** The action scrape. */
  private final Action        actionScrape2           = new SingleScrapeAction2();

  /** The text pane. */
  private JTextPane           textPane;

  /** The lbl movie name. */
  private JLabel              lblMovieName;

  /** The lbl movie background. */
  private ImageLabel          lblMovieBackground;

  /** The lbl movie poster. */
  private ImageLabel          lblMoviePoster;

  /** The table cast. */
  private JTable              tableCast;

  /** The lbl original name. */
  private JLabel              lblOriginalName;

  /** The action edit movie. */
  private final Action        actionEditMovie         = new EditAction();

  /** The action scrape unscraped movies. */
  private final Action        actionScrapeUnscraped   = new UnscrapedScrapeAction();

  /** The action scrape selected movies. */
  private final Action        actionScrapeSelected    = new SelectedScrapeAction();

  /** The panel rating. */
  private StarRater           panelRatingStars;

  /** The label progressAction. */
  private JLabel              lblProgressAction;

  /** The progress bar. */
  private JProgressBar        progressBar;

  /** The scrape task. */
  private ScrapeTask          scrapeTask;

  /** The label rating. */
  private JLabel              lblRating;

  /** The button cancelScraper. */
  private JButton             btnCancelScraper;

  /** The lbl movie path. */
  private LinkLabel           lblMoviePath;
  private JPanel              panelProgressBar;
  private JPanel              panelMovieCount;
  private JLabel              lblMovieCount;
  private JLabel              lblMovieCountInt;
  private JPanel              panelTop;
  private JTabbedPane         tabbedPaneMovieDetails;
  private JPanel              panelOverview;
  private JPanel              panelMovieCast;
  private JPanel              panelDetails;
  private JLabel              lblMoviePathT;
  private JLabel              lblDirectorT;
  private JLabel              lblDirector;
  private JLabel              lblWriterT;
  private JLabel              lblWriter;
  private JLabel              lblActors;
  private JLabel              lblProductionT;
  private JLabel              lblProduction;
  private JLabel              lblGenresT;
  private JLabel              lblGenres;
  private JLabel              lblCertificationT;
  private JLabel              lblCertification;
  private JLabel              lblImdbIdT;
  private JLabel              lblTmdbIdT;
  private LinkLabel           lblImdbId;
  private LinkLabel           lblTmdbId;
  private JLabel              lblRuntimeT;
  private JLabel              lblRuntime;
  private JLabel              lblMinutes;
  private JLabel              lblVoteCount;
  private JLabel              lblVoteCountT;
  private JButton             btnRen;
  private final Action        actionRename            = new RenameAction();
  private JPanel              panelMediaInformation;
  private JLabel              lblFilesT;
  private JScrollPane         scrollPaneFiles;
  private JTable              tableFiles;
  private JLabel              lblPath2;
  private ImageLabel          lblActorThumb;

  /**
   * Create the panel.
   */
  public MoviePanel() {
    // load movielist
    LOGGER.debug("loading MovieList");
    movieList = MovieList.getInstance();

    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("248px:grow"), FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), FormFactory.DEFAULT_ROWSPEC, }));

    JSplitPane splitPaneHorizontal = new JSplitPane();
    splitPaneHorizontal.setContinuousLayout(true);
    add(splitPaneHorizontal, "2, 2, fill, fill");

    JPanel panelMovieList = new JPanel();
    splitPaneHorizontal.setLeftComponent(panelMovieList);
    panelMovieList.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(75dlu;default)"),
        ColumnSpec.decode("max(200px;pref):grow"), }, new RowSpec[] { RowSpec.decode("26px"), FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("fill:max(200px;default):grow"), }));

    JToolBar toolBar = new JToolBar();
    toolBar.setBorder(null);
    toolBar.setRollover(true);
    toolBar.setFloatable(false);
    panelMovieList.add(toolBar, "2, 1, fill, fill");

    JButton buttonUpdateDataSources = toolBar.add(actionUpdateDataSources);
    JSplitButton buttonScrape = new JSplitButton(new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
    buttonScrape.setHorizontalAlignment(JButton.LEFT);
    buttonScrape.setMargin(new Insets(2, 2, 2, 14));
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

    textField = new JTextField();
    panelMovieList.add(textField, "3, 1, right, bottom");
    textField.setColumns(10);

    // table = new JTable();
    table = new MyTable();
    table.setFont(new Font("Dialog", Font.PLAIN, 11));
    // scrollPane.setViewportView(table);

    // JScrollPane scrollPane = new JScrollPane();
    JScrollPane scrollPane = MyTable.createStripedJScrollPane(table);
    panelMovieList.add(scrollPane, "2, 3, 2, 1, fill, fill");

    JPanel panelRight = new JPanel();
    splitPaneHorizontal.setRightComponent(panelRight);
    panelRight.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), },
        new RowSpec[] { RowSpec.decode("fill:default:grow"), }));

    JSplitPane splitPaneVertical = new JSplitPane();
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
    panelTop.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("400px:grow"), FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("right:250px:grow"), }, new RowSpec[] { RowSpec.decode("fill:default"), RowSpec.decode("fill:pref:grow"), }));

    JPanel panelMovieHeader = new JPanel();
    panelTop.add(panelMovieHeader, "1, 1, 3, 1, fill, fill");
    panelMovieHeader.setBorder(null);
    panelMovieHeader
        .setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("400px:grow"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("right:250px:grow"), }, new RowSpec[] { RowSpec.decode("25px"),
            RowSpec.decode("20px"), }));

    lblMovieName = new JLabel("");
    panelMovieHeader.add(lblMovieName, "2, 1, 3, 1, left, top");
    lblMovieName.setFont(new Font("Dialog", Font.BOLD, 20));

    lblOriginalName = new JLabel("");
    panelMovieHeader.add(lblOriginalName, "2, 2");

    JPanel panelRating = new JPanel();
    panelMovieHeader.add(panelRating, "4, 2, right, default");

    lblVoteCount = new JLabel("");
    panelRating.add(lblVoteCount);

    lblVoteCountT = new JLabel("Votes:");
    lblVoteCountT.setVisible(false);
    panelRating.add(lblVoteCountT);

    lblRating = new JLabel("");
    panelRating.add(lblRating);

    panelRatingStars = new StarRater(10);
    panelRating.add(panelRatingStars);
    panelRatingStars.setEnabled(false);

    JLayeredPane layeredPaneImages = new JLayeredPane();
    panelTop.add(layeredPaneImages, "1, 2, 3, 1, fill, fill");
    layeredPaneImages.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("max(10px;default)"), ColumnSpec.decode("left:120px"),
        ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        RowSpec.decode("max(10px;default)"), RowSpec.decode("top:180px"), RowSpec.decode("fill:default:grow"), }));

    lblMovieBackground = new ImageLabel(false);
    layeredPaneImages.add(lblMovieBackground, "1, 3, 3, 3, fill, fill");

    lblMoviePoster = new ImageLabel();
    layeredPaneImages.setLayer(lblMoviePoster, 1);
    layeredPaneImages.add(lblMoviePoster, "2, 4, fill, fill");

    JPanel panelBottom = new JPanel();
    panelBottom.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("496px:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
        RowSpec.decode("fill:default:grow"), }));

    tabbedPaneMovieDetails = new JTabbedPane(JTabbedPane.TOP);
    panelBottom.add(tabbedPaneMovieDetails, "1, 2, fill, fill");
    splitPaneVertical.setBottomComponent(panelBottom);

    panelDetails = new JPanel();
    tabbedPaneMovieDetails.addTab("Details", null, panelDetails, null);
    panelDetails.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(120px;default)"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("25px"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(120px;default)"), FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("max(12dlu;default):grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, }));

    lblGenresT = new JLabel("Genres");
    panelDetails.add(lblGenresT, "2, 2");
    lblGenresT.setLabelFor(lblGenres);

    lblGenres = new JLabel("");
    panelDetails.add(lblGenres, "4, 2, 7, 1");

    lblRuntimeT = new JLabel("Runtime");
    panelDetails.add(lblRuntimeT, "2, 4");
    lblRuntimeT.setLabelFor(lblRuntime);

    lblRuntime = new JLabel("");
    panelDetails.add(lblRuntime, "4, 4");

    lblMinutes = new JLabel("min");
    panelDetails.add(lblMinutes, "6, 4");

    lblCertificationT = new JLabel("Certification");
    panelDetails.add(lblCertificationT, "2, 6");
    lblCertificationT.setLabelFor(lblCertification);

    lblCertification = new JLabel("");
    panelDetails.add(lblCertification, "4, 6, 7, 1");

    lblProductionT = new JLabel("Production");
    panelDetails.add(lblProductionT, "2, 8");
    lblProductionT.setLabelFor(lblProduction);

    lblProduction = new JLabel("");
    panelDetails.add(lblProduction, "4, 8, 7, 1");

    lblImdbIdT = new JLabel("IMDB Id");
    panelDetails.add(lblImdbIdT, "2, 10");

    lblImdbId = new LinkLabel("");
    lblImdbId.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        try {
          Desktop.getDesktop().browse(new URI("http://www.imdb.com/title/" + lblImdbId.getNormalText()));
        }
        catch (Exception e) {
          LOGGER.error("browse to imdbid", e);
        }
      }
    });
    lblImdbIdT.setLabelFor(lblImdbId);

    panelDetails.add(lblImdbId, "4, 10, 3, 1, left, default");

    lblTmdbIdT = new JLabel("TMDB Id");
    panelDetails.add(lblTmdbIdT, "8, 10");

    lblTmdbId = new LinkLabel("");
    lblTmdbId.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        try {
          Desktop.getDesktop().browse(new URI("http://www.themoviedb.org/movie/" + lblTmdbId.getNormalText()));
        }
        catch (Exception e) {
          LOGGER.error("browse to tmdbid", e);
        }
      }
    });
    lblTmdbIdT.setLabelFor(lblTmdbId);
    panelDetails.add(lblTmdbId, "10, 10, left, default");

    lblMoviePathT = new JLabel("Path");
    panelDetails.add(lblMoviePathT, "2, 12");

    lblMoviePath = new LinkLabel("");
    lblMoviePath.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (!StringUtils.isEmpty(lblMoviePath.getNormalText())) {
          try {
            // get the location from the label
            File path = new File(lblMoviePath.getNormalText());
            // check whether this location exists
            if (path.exists()) {
              Desktop.getDesktop().open(path);
            }
          }
          catch (Exception ex) {
            LOGGER.error("open filemanager", ex);
          }
        }
      }
    });
    lblMoviePathT.setLabelFor(lblMoviePath);

    panelDetails.add(lblMoviePath, "4, 12, 7, 1, left, default");

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
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(165dlu;default):grow"), FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("max(150px;default)"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

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
    panelMovieCast.add(scrollPaneMovieCast, "4, 6");

    tableCast = new JTable();
    scrollPaneMovieCast.setViewportView(tableCast);

    lblActorThumb = new ImageLabel();
    panelMovieCast.add(lblActorThumb, "6, 6, fill, fill");

    panelMediaInformation = new JPanel();
    tabbedPaneMovieDetails.addTab("Media Information", null, panelMediaInformation, null);
    panelMediaInformation.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(207dlu;default):grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

    JLabel lblPathT2 = new JLabel("Path");
    panelMediaInformation.add(lblPathT2, "2, 2");

    lblPath2 = new JLabel("");
    lblPathT2.setLabelFor(lblPath2);
    panelMediaInformation.add(lblPath2, "4, 2");

    lblFilesT = new JLabel("Files");
    panelMediaInformation.add(lblFilesT, "2, 4, default, top");

    scrollPaneFiles = new JScrollPane();
    panelMediaInformation.add(scrollPaneFiles, "4, 4, fill, fill");

    tableFiles = new JTable();
    lblFilesT.setLabelFor(tableFiles);
    scrollPaneFiles.setViewportView(tableFiles);

    JPanel panelStatus = new JPanel();
    add(panelStatus, "2, 3, fill, fill");
    panelStatus.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("1px"),
        ColumnSpec.decode("146px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { RowSpec
        .decode("fill:default:grow"), }));

    panelMovieCount = new JPanel();
    panelStatus.add(panelMovieCount, "3, 1, left, fill");

    lblMovieCount = new JLabel("Movie count:");
    panelMovieCount.add(lblMovieCount);

    lblMovieCountInt = new JLabel("");
    panelMovieCount.add(lblMovieCountInt);

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
        scrapeTask.cancel(false);
      }
    });
    progressBar.setVisible(false);

    textField.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        newFilter();
      }

      public void insertUpdate(DocumentEvent e) {
        newFilter();
      }

      public void removeUpdate(DocumentEvent e) {
        newFilter();
      }
    });

    initDataBindings();

    TableRowSorter sorter = new TableRowSorter(table.getModel());
    table.setRowSorter(sorter);

    // moviename column
    table.getColumnModel().getColumn(0).setCellRenderer(new BorderCellRenderer());

    // year column
    table.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(35);
    table.getTableHeader().getColumnModel().getColumn(1).setMinWidth(35);
    table.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(50);

    // NFO column
    table.getTableHeader().getColumnModel().getColumn(2).setHeaderRenderer(new IconRenderer());
    table.getTableHeader().getColumnModel().getColumn(2).setMaxWidth(20);
    URL imageURL = MoviePanel.class.getResource("images/File.png");
    if (imageURL != null) {
      table.getColumnModel().getColumn(2).setHeaderValue(new ImageIcon(imageURL));
    }

    // poster column
    table.getTableHeader().getColumnModel().getColumn(3).setHeaderRenderer(new IconRenderer());
    table.getTableHeader().getColumnModel().getColumn(3).setMaxWidth(20);
    imageURL = null;
    imageURL = MoviePanel.class.getResource("images/Image.png");
    if (imageURL != null) {
      table.getColumnModel().getColumn(3).setHeaderValue(new ImageIcon(imageURL));
    }

    // LoadingTask task = new LoadingTask();
    // task.execute();

    // selecting first movie at startup
    if (movieList.getMovies() != null && movieList.getMovies().size() > 0) {
      ListSelectionModel selectionModel = table.getSelectionModel();
      if (selectionModel.isSelectionEmpty()) {
        selectionModel.setSelectionInterval(0, 0);
      }
    }
  }

  /**
   * The Class UpdateDataSourcesAction.
   */
  private class UpdateDataSourcesAction extends AbstractAction {

    /**
     * Instantiates a new UpdateDataSourcesAction.
     */
    public UpdateDataSourcesAction() {
      // putValue(NAME, "UDS");
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Folder-Sync.png")));
      putValue(SHORT_DESCRIPTION, "Update data sources");
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

    /**
     * Instantiates a new SingleScrapeAction.
     */
    public SingleScrapeAction() {
      // putValue(NAME, "SCR");
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
      putValue(SHORT_DESCRIPTION, "Search & scrape movie");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      for (int row : table.getSelectedRows()) {
        row = table.convertRowIndexToModel(row);
        Movie movie = movieList.getMovies().get(row);
        MovieChooser dialogMovieChooser = new MovieChooser(movie);
        dialogMovieChooser.pack();
        dialogMovieChooser.setVisible(true);
      }
    }

  }

  /**
   * The Class SingleScrapeAction.
   */
  private class SingleScrapeAction2 extends SingleScrapeAction {

    /**
     * Instantiates a new SingleScrapeAction.
     */
    public SingleScrapeAction2() {
      putValue(NAME, "Scrape selected movies");
      putValue(LARGE_ICON_KEY, "");
      // putValue(SHORT_DESCRIPTION, "Search & scrape movie");
    }

  }

  /**
   * The Class UnscrapedScrapeAction.
   */
  private class UnscrapedScrapeAction extends AbstractAction {

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
      scrapeTask = new ScrapeTask(unscrapedMovies);
      scrapeTask.execute();
    }
  }

  /**
   * The Class UnscrapedScrapeAction.
   */
  private class SelectedScrapeAction extends AbstractAction {

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
      List<Movie> selectedMovies = new ArrayList<Movie>();
      for (int row : table.getSelectedRows()) {
        row = table.convertRowIndexToModel(row);
        selectedMovies.add(movieList.getMovies().get(row));
      }
      if (selectedMovies.size() > 0) {
        scrapeTask = new ScrapeTask(selectedMovies);
        scrapeTask.execute();
      }
    }
  }

  /**
   * Update the row filter regular expression from the expression in the text
   * box.
   */
  private void newFilter() {
    RowFilter rf = null;
    TableRowSorter sorter = (TableRowSorter) table.getRowSorter();

    // only update, if text is longer than 2 characters
    if (textField.getText().length() > 2) {
      try {
        // If current expression doesn't parse, don't update.
        String filterText = "(?i)" + textField.getText();
        rf = RowFilter.regexFilter(filterText, 0);
      }
      catch (java.util.regex.PatternSyntaxException e) {
        sorter.setRowFilter(rf);
        return;
      }
    }
    if (rf == null && sorter.getRowFilter() == null) {
      return;
    }
    sorter.setRowFilter(rf);
  }

  /**
   * The Class EditAction.
   */
  private class EditAction extends AbstractAction {

    /**
     * Instantiates a new EditAction.
     */
    public EditAction() {
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Pencil.png")));
      putValue(SHORT_DESCRIPTION, "Edit movie");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      for (int row : table.getSelectedRows()) {
        row = table.convertRowIndexToModel(row);
        Movie movie = movieList.getMovies().get(row);
        MovieEditor dialogMovieEditor = new MovieEditor(movie);
        // dialogMovieEditor.pack();
        dialogMovieEditor.setVisible(true);
      }
    }
  }

  /**
   * The Class ScrapeTask.
   */
  private class ScrapeTask extends SwingWorker<Void, Void> {

    /** The movies to scrape. */
    private List<Movie> moviesToScrape;

    /**
     * Instantiates a new scrape task.
     * 
     * @param moviesToScrape
     *          the movies to scrape
     */
    public ScrapeTask(List<Movie> moviesToScrape) {
      this.moviesToScrape = moviesToScrape;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    public Void doInBackground() {
      int movieCount = moviesToScrape.size();
      int counter = 0;
      for (Movie movie : moviesToScrape) {
        if (isCancelled()) {
          return null;
        }

        counter++;
        startProgressBar("scraping: " + movie.getName(), 100 * counter / movieCount);
        List<MediaSearchResult> results = movieList.searchMovie(movie.getName());
        if (results != null && !results.isEmpty()) {
          MediaSearchResult result1 = results.get(0);
          // check if there is an other result with 100% score
          if (results.size() > 1) {
            MediaSearchResult result2 = results.get(1);
            // if both results have 100% score - do not take any result
            if (result1.getScore() == 1 && result2.getScore() == 1) {
              continue;
            }
          }
          try {
            movie.setMetadata(movieList.getMetadataProvider().getMetaData(result1));
          }
          catch (Exception e) {
            LOGGER.error("movie.setMetadata", e);
          }
        }
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
     * @param value
     *          the value
     */
    private void startProgressBar(String description, int value) {
      lblProgressAction.setText(description);
      progressBar.setVisible(true);
      progressBar.setValue(value);
      btnCancelScraper.setVisible(true);
    }

    /**
     * Stop progress bar.
     */
    private void stopProgressBar() {
      lblProgressAction.setText("");
      progressBar.setVisible(false);
      btnCancelScraper.setVisible(false);
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
      for (String path : Globals.settings.getMovieDataSource()) {
        startProgressBar("Updating " + path);
        movieList.findMoviesInPath(path);
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

  private class RenameAction extends AbstractAction {
    public RenameAction() {
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/rename-icon.png")));
      putValue(SHORT_DESCRIPTION, "rename selected movies");
    }

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

      for (int row : table.getSelectedRows()) {
        row = table.convertRowIndexToModel(row);
        Movie movie = movieList.getMovies().get(row);
        MovieRenamer.renameMovie(movie);
      }
    }
  }

  protected void initDataBindings() {
    BeanProperty<MovieList, List<Movie>> movieListBeanProperty = BeanProperty.create("movies");
    JTableBinding<Movie, MovieList, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, movieList, movieListBeanProperty,
        table);
    //
    BeanProperty<Movie, String> movieBeanProperty = BeanProperty.create("name");
    jTableBinding.addColumnBinding(movieBeanProperty).setColumnName("Title").setEditable(false);
    //
    BeanProperty<Movie, String> movieBeanProperty_1 = BeanProperty.create("year");
    jTableBinding.addColumnBinding(movieBeanProperty_1).setColumnName("Year").setEditable(false);
    //
    BeanProperty<Movie, Boolean> movieBeanProperty_2 = BeanProperty.create("hasNfoFile");
    JTableBinding<Movie, MovieList, JTable>.ColumnBinding columnBinding = jTableBinding.addColumnBinding(movieBeanProperty_2);
    columnBinding.setColumnName("NFO");
    columnBinding.setEditable(false);
    columnBinding.setColumnClass(ImageIcon.class);
    columnBinding.setConverter(new ImageIconConverter());
    //
    BeanProperty<Movie, Boolean> movieBeanProperty_3 = BeanProperty.create("hasImages");
    JTableBinding<Movie, MovieList, JTable>.ColumnBinding columnBinding_1 = jTableBinding.addColumnBinding(movieBeanProperty_3);
    columnBinding_1.setColumnName("Images");
    columnBinding_1.setEditable(false);
    columnBinding_1.setColumnClass(ImageIcon.class);
    columnBinding_1.setConverter(new ImageIconConverter());
    //
    jTableBinding.setEditable(false);
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.overview");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextPane, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty, textPane,
        jTextPaneBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<JTable, List<MovieCast>> jTableBeanProperty_3 = BeanProperty.create("selectedElement.actors");
    JTableBinding<MovieCast, JTable, JTable> jTableBinding_1 = SwingBindings.createJTableBinding(UpdateStrategy.READ, table, jTableBeanProperty_3,
        tableCast);
    //
    BeanProperty<MovieCast, String> movieCastBeanProperty = BeanProperty.create("name");
    jTableBinding_1.addColumnBinding(movieCastBeanProperty).setColumnName("Name");
    //
    BeanProperty<MovieCast, String> movieCastBeanProperty_1 = BeanProperty.create("character");
    jTableBinding_1.addColumnBinding(movieCastBeanProperty_1).setColumnName("Role");
    //
    jTableBinding_1.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_1 = BeanProperty.create("selectedElement.nameForUi");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_1,
        lblMovieName, jLabelBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_4 = BeanProperty.create("selectedElement.originalName");
    AutoBinding<JTable, String, JLabel, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_4,
        lblOriginalName, jLabelBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_2 = BeanProperty.create("selectedElement.fanart");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imagePath");
    AutoBinding<JTable, String, ImageLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_2,
        lblMovieBackground, imageLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_5 = BeanProperty.create("selectedElement.poster");
    AutoBinding<JTable, String, ImageLabel, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_5,
        lblMoviePoster, imageLabelBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<JTable, Float> jTableBeanProperty_6 = BeanProperty.create("selectedElement.rating");
    BeanProperty<StarRater, Float> starRaterBeanProperty = BeanProperty.create("rating");
    AutoBinding<JTable, Float, StarRater, Float> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_6,
        panelRatingStars, starRaterBeanProperty);
    autoBinding_5.bind();
    //
    AutoBinding<JTable, Float, JLabel, String> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_6,
        lblRating, jLabelBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_8 = BeanProperty.create("selectedElement.path");
    AutoBinding<JTable, String, JLabel, String> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_8,
        lblMoviePath, jLabelBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<MovieList, Integer> movieListBeanProperty_1 = BeanProperty.create("movieCount");
    AutoBinding<MovieList, Integer, JLabel, String> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ, movieList,
        movieListBeanProperty_1, lblMovieCountInt, jLabelBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<JTable, Boolean> jTableBeanProperty_7 = BeanProperty.create("selectedElement.hasRating");
    BeanProperty<JLabel, Boolean> jLabelBeanProperty_1 = BeanProperty.create("visible");
    AutoBinding<JTable, Boolean, JLabel, Boolean> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_7,
        lblRating, jLabelBeanProperty_1);
    autoBinding_7.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_9 = BeanProperty.create("selectedElement.writer");
    AutoBinding<JTable, String, JLabel, String> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_9,
        lblWriter, jLabelBeanProperty);
    autoBinding_10.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_10 = BeanProperty.create("selectedElement.director");
    AutoBinding<JTable, String, JLabel, String> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_10,
        lblDirector, jLabelBeanProperty);
    autoBinding_11.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_11 = BeanProperty.create("selectedElement.productionCompany");
    AutoBinding<JTable, String, JLabel, String> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_11,
        lblProduction, jLabelBeanProperty);
    autoBinding_12.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_12 = BeanProperty.create("selectedElement.genresAsString");
    AutoBinding<JTable, String, JLabel, String> autoBinding_13 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_12,
        lblGenres, jLabelBeanProperty);
    autoBinding_13.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_14 = BeanProperty.create("selectedElement.imdbId");
    AutoBinding<JTable, String, JLabel, String> autoBinding_15 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_14,
        lblImdbId, jLabelBeanProperty);
    autoBinding_15.bind();
    //
    BeanProperty<JTable, Integer> jTableBeanProperty_15 = BeanProperty.create("selectedElement.tmdbId");
    AutoBinding<JTable, Integer, JLabel, String> autoBinding_16 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_15,
        lblTmdbId, jLabelBeanProperty);
    autoBinding_16.bind();
    //
    BeanProperty<JTable, Integer> jTableBeanProperty_16 = BeanProperty.create("selectedElement.runtime");
    AutoBinding<JTable, Integer, JLabel, String> autoBinding_17 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_16,
        lblRuntime, jLabelBeanProperty);
    autoBinding_17.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_13 = BeanProperty.create("selectedElement.certification.name");
    AutoBinding<JTable, String, JLabel, String> autoBinding_14 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_13,
        lblCertification, jLabelBeanProperty);
    autoBinding_14.bind();
    //
    BeanProperty<JTable, Integer> jTableBeanProperty_17 = BeanProperty.create("selectedElement.votes");
    AutoBinding<JTable, Integer, JLabel, String> autoBinding_18 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_17,
        lblVoteCount, jLabelBeanProperty);
    autoBinding_18.bind();
    //
    AutoBinding<JTable, Boolean, JLabel, Boolean> autoBinding_19 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_7,
        lblVoteCountT, jLabelBeanProperty_1);
    autoBinding_19.bind();
    //
    AutoBinding<JTable, Boolean, JLabel, Boolean> autoBinding_20 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_7,
        lblVoteCount, jLabelBeanProperty_1);
    autoBinding_20.bind();
    //
    AutoBinding<JTable, String, JLabel, String> autoBinding_21 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_8,
        lblPath2, jLabelBeanProperty);
    autoBinding_21.bind();
    //
    BeanProperty<JTable, List<MediaFile>> jTableBeanProperty_18 = BeanProperty.create("selectedElement.mediaFiles");
    JTableBinding<MediaFile, JTable, JTable> jTableBinding_2 = SwingBindings.createJTableBinding(UpdateStrategy.READ, table, jTableBeanProperty_18,
        tableFiles);
    //
    BeanProperty<MediaFile, String> mediaFileBeanProperty = BeanProperty.create("filename");
    jTableBinding_2.addColumnBinding(mediaFileBeanProperty).setColumnName("Filename").setEditable(false);
    //
    BeanProperty<MediaFile, Long> mediaFileBeanProperty_1 = BeanProperty.create("filesize");
    jTableBinding_2.addColumnBinding(mediaFileBeanProperty_1).setColumnName("Size").setEditable(false);
    //
    jTableBinding_2.setEditable(false);
    jTableBinding_2.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_19 = BeanProperty.create("selectedElement.thumb");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty_1 = BeanProperty.create("imageUrl");
    AutoBinding<JTable, String, ImageLabel, String> autoBinding_22 = Bindings.createAutoBinding(UpdateStrategy.READ, tableCast,
        jTableBeanProperty_19, lblActorThumb, imageLabelBeanProperty_1);
    autoBinding_22.bind();
  }
}
