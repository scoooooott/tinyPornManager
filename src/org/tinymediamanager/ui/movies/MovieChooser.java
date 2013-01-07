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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.ScraperMetadataConfig;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieScrapers;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.ui.ImageChooser;
import org.tinymediamanager.ui.ImageChooser.ImageType;
import org.tinymediamanager.ui.ImageLabel;
import org.tinymediamanager.ui.TmmWindowSaver;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieChooser.
 */
public class MovieChooser extends JDialog implements ActionListener {

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID      = 1L;

  /** The static LOGGER. */
  private static final Logger         LOGGER                = Logger.getLogger(MovieChooser.class);

  /** The content panel. */
  private final JPanel                contentPanel          = new JPanel();

  /** The movie list. */
  private MovieList                   movieList             = MovieList.getInstance();

  /** The movie to scrape. */
  private Movie                       movieToScrape;

  /** The text field search string. */
  private JTextField                  textFieldSearchString;

  /** The cb scraper. */
  private JComboBox                   cbScraper;

  /** The table. */
  private JTable                      table;

  /** The lbl movie name. */
  private JTextArea                   lblMovieName;

  /** The tp movie description. */
  private JTextPane                   tpMovieDescription;

  /** The lbl movie poster. */
  private ImageLabel                  lblMoviePoster;

  /** The lbl progress action. */
  private JLabel                      lblProgressAction;

  /** The progress bar. */
  private JProgressBar                progressBar;

  /** The movies found. */
  private List<MovieChooserModel>     moviesFound           = ObservableCollections.observableList(new ArrayList<MovieChooserModel>());

  /** The lbl tagline. */
  private JTextArea                   lblTagline;

  /** The scraper metadata config. */
  private ScraperMetadataConfig       scraperMetadataConfig = new ScraperMetadataConfig();

  /** The metadata provider. */
  private IMediaMetadataProvider      metadataProvider;

  /** The artwork providers. */
  private List<IMediaArtworkProvider> artworkProviders;

  /** The trailer providers. */
  private List<IMediaTrailerProvider> trailerProviders;

  /**
   * Create the dialog.
   * 
   * @param movie
   *          the movie
   */
  public MovieChooser(Movie movie) {
    setTitle("search movie");
    setName("movieChooser");
    TmmWindowSaver.loadSettings(this);
    setIconImage(Globals.logo);
    setModal(true);
    // setBounds(5, 5, 1111, 643);

    // copy the values
    ScraperMetadataConfig settings = Globals.settings.getScraperMetadataConfig();
    metadataProvider = movieList.getMetadataProvider();
    artworkProviders = movieList.getArtworkProviders();
    trailerProviders = movieList.getTrailerProviders();

    scraperMetadataConfig.setTitle(settings.isTitle());
    scraperMetadataConfig.setOriginalTitle(settings.isOriginalTitle());
    scraperMetadataConfig.setTagline(settings.isTagline());
    scraperMetadataConfig.setPlot(settings.isPlot());
    scraperMetadataConfig.setRating(settings.isRating());
    scraperMetadataConfig.setRuntime(settings.isRuntime());
    scraperMetadataConfig.setYear(settings.isYear());
    scraperMetadataConfig.setCertification(settings.isCertification());
    scraperMetadataConfig.setCast(settings.isCast());
    scraperMetadataConfig.setGenres(settings.isGenres());
    scraperMetadataConfig.setArtwork(settings.isArtwork());
    scraperMetadataConfig.setTrailer(settings.isTrailer());

    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("fill:403px:grow"), FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
    {
      JPanel panelSearchField = new JPanel();
      contentPanel.add(panelSearchField, "1, 2, fill, fill");
      panelSearchField.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
          ColumnSpec.decode("right:default"), }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
          FormFactory.DEFAULT_ROWSPEC, }));
      {
        JLabel lblScraper = new JLabel("Scraper");
        panelSearchField.add(lblScraper, "2, 1, right, default");
      }
      {
        cbScraper = new JComboBox(MovieScrapers.values());
        MovieScrapers defaultScraper = Globals.settings.getMovieScraper();
        cbScraper.setSelectedItem(defaultScraper);
        cbScraper.setAction(new ChangeScraperAction());
        panelSearchField.add(cbScraper, "4, 1, fill, default");
      }
      {
        textFieldSearchString = new JTextField();
        panelSearchField.add(textFieldSearchString, "2, 3, 5, 1, fill, default");
        textFieldSearchString.setColumns(10);
      }

      {
        JButton btnSearch = new JButton("Search");
        panelSearchField.add(btnSearch, "7, 3");
        btnSearch.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
            searchMovie(textFieldSearchString.getText(), "");
          }
        });
        getRootPane().setDefaultButton(btnSearch);
      }
    }
    {
      JSplitPane splitPane = new JSplitPane();
      splitPane.setContinuousLayout(true);
      contentPanel.add(splitPane, "1, 4, fill, fill");
      {
        JPanel panelSearchResults = new JPanel();
        splitPane.setLeftComponent(panelSearchResults);
        panelSearchResults.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("350px:grow"), },
            new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("fill:max(212px;default):grow"), }));
        {
          {
            JScrollPane scrollPane = new JScrollPane();
            panelSearchResults.add(scrollPane, "2, 2, fill, fill");
            table = new JTable();
            scrollPane.setViewportView(table);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setBorder(new LineBorder(new Color(0, 0, 0)));
            ListSelectionModel rowSM = table.getSelectionModel();
            rowSM.addListSelectionListener(new ListSelectionListener() {
              public void valueChanged(ListSelectionEvent e) {
                // Ignore extra messages.
                if (e.getValueIsAdjusting())
                  return;

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (!lsm.isSelectionEmpty()) {
                  int selectedRow = lsm.getMinSelectionIndex();
                  selectedRow = table.convertRowIndexToModel(selectedRow);
                  try {
                    MovieChooserModel model = moviesFound.get(selectedRow);
                    if (!model.isScraped()) {
                      ScrapeTask task = new ScrapeTask(model);
                      task.execute();

                    }
                  }
                  catch (Exception ex) {

                  }
                }
              }
            });
          }
        }
      }
      {
        JPanel panelSearchDetail = new JPanel();
        splitPane.setRightComponent(panelSearchDetail);
        panelSearchDetail.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:150px"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(300px;default):grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
            FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("250px"),
            FormFactory.PARAGRAPH_GAP_ROWSPEC, RowSpec.decode("top:default:grow"), }));
        {
          lblMovieName = new JTextArea("");
          lblMovieName.setLineWrap(true);
          lblMovieName.setOpaque(false);
          lblMovieName.setWrapStyleWord(true);
          lblMovieName.setFont(new Font("Dialog", Font.BOLD, 14));
          panelSearchDetail.add(lblMovieName, "2, 1, 3, 1, fill, top");
        }
        {
          lblTagline = new JTextArea("");
          lblTagline.setLineWrap(true);
          lblTagline.setOpaque(false);
          lblTagline.setWrapStyleWord(true);
          lblTagline.setEditable(false);
          panelSearchDetail.add(lblTagline, "2, 2, 3, 1");
        }
        {
          lblMoviePoster = new ImageLabel();// new JLabel("");
          panelSearchDetail.add(lblMoviePoster, "2, 4, fill, fill");
        }
        {
          JPanel panel = new JPanel();
          panelSearchDetail.add(panel, "4, 4, fill, fill");
          panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
              FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
              FormFactory.DEFAULT_ROWSPEC, }));
        }
        {
          JScrollPane scrollPane = new JScrollPane();
          panelSearchDetail.add(scrollPane, "2, 6, 3, 1, fill, fill");
          {
            tpMovieDescription = new JTextPane();
            scrollPane.setViewportView(tpMovieDescription);
          }
        }
      }
    }
    {
      JLabel lblScrapeFollowingItems = new JLabel("Scrape following items");
      contentPanel.add(lblScrapeFollowingItems, "1, 6");
    }
    {
      JPanel panelScraperMetadataSetting = new MovieScraperMetadataPanel(scraperMetadataConfig);
      contentPanel.add(panelScraperMetadataSetting, "1, 7, fill, fill");
    }

    {
      JPanel buttonPane = new JPanel();
      contentPanel.add(buttonPane, "1, 9");
      {
        JButton okButton = new JButton("Ok");
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);
        buttonPane.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("max(82dlu;default)"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), ColumnSpec.decode("100px"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
            ColumnSpec.decode("100px"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"), }));
        {
          progressBar = new JProgressBar();
          buttonPane.add(progressBar, "2, 2");
        }
        {
          lblProgressAction = new JLabel("");
          buttonPane.add(lblProgressAction, "4, 2");
        }
        buttonPane.add(okButton, "5, 2, fill, top");
      }
      {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(this);
        buttonPane.add(cancelButton, "7, 2, fill, top");
      }
    }

    {
      movieToScrape = movie;
      progressBar.setVisible(false);
      initDataBindings();

      textFieldSearchString.setText(movieToScrape.getName());
      // searchMovie(textFieldSearchString.getText(),
      // movieToScrape.getImdbId());

      // initial search only by name
      searchMovie(textFieldSearchString.getText(), "");
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  /**
   * Action performed.
   * 
   * @param e
   *          the e
   */
  public void actionPerformed(ActionEvent e) {
    if ("OK".equals(e.getActionCommand())) {
      int row = table.getSelectedRow();
      if (row >= 0) {
        MovieChooserModel model = moviesFound.get(row);
        MediaMetadata md = model.getMetadata();

        // did the user want to choose the images?
        if (!Globals.settings.isScrapeBestImage()) {
          md.clearMediaArt();
        }

        // set scraped metadata
        movieToScrape.setMetadata(md);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // get images?
        if (Globals.settings.getScraperMetadataConfig().isArtwork()) {
          // let the user choose the images
          if (!Globals.settings.isScrapeBestImage()) {
            // poster
            {
              ImageLabel lblImage = new ImageLabel();
              ImageChooser dialog = new ImageChooser(movieToScrape.getImdbId(), movieToScrape.getTmdbId(), ImageType.POSTER, lblImage, null);
              dialog.setVisible(true);
              movieToScrape.setPosterUrl(lblImage.getImageUrl());
              movieToScrape.writeImages(true, false);
            }

            // fanart
            {
              ImageLabel lblImage = new ImageLabel();
              List<String> extrathumbs = new ArrayList<String>();
              ImageChooser dialog = new ImageChooser(movieToScrape.getImdbId(), movieToScrape.getTmdbId(), ImageType.FANART, lblImage, extrathumbs);
              dialog.setVisible(true);
              movieToScrape.setFanartUrl(lblImage.getImageUrl());
              movieToScrape.writeImages(false, true);
              movieToScrape.downloadExtraThumbs(extrathumbs);
            }
          }
          else {
            // get artwork directly from provider
            List<MediaArtwork> artwork = model.getArtwork();
            movieToScrape.setArtwork(artwork);
          }
        }

        // get trailers?
        if (Globals.settings.getScraperMetadataConfig().isTrailer()) {
          List<MediaTrailer> trailers = model.getTrailers();
          movieToScrape.setTrailers(trailers);
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        this.setVisible(false);
        dispose();
      }
    }
    if ("Cancel".equals(e.getActionCommand())) {
      this.setVisible(false);
      dispose();
    }

  }

  /**
   * Search movie.
   * 
   * @param searchTerm
   *          the search term
   * @param imdbId
   *          the imdb id
   */
  private void searchMovie(String searchTerm, String imdbId) {
    SearchTask task = new SearchTask(searchTerm, imdbId);
    task.execute();
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
  }

  /**
   * Stop progress bar.
   */
  private void stopProgressBar() {
    lblProgressAction.setText("");
    progressBar.setVisible(false);
    progressBar.setIndeterminate(false);
  }

  /**
   * The Class SearchTask.
   */
  private class SearchTask extends SwingWorker<Void, Void> {

    /** The search term. */
    private String searchTerm;

    /** The imdb id. */
    private String imdbId;

    /**
     * Instantiates a new search task.
     * 
     * @param searchTerm
     *          the search term
     * @param imdbId
     *          the imdb id
     */
    public SearchTask(String searchTerm, String imdbId) {
      this.searchTerm = searchTerm;
      this.imdbId = imdbId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    public Void doInBackground() {
      startProgressBar("searching for: " + searchTerm);
      List<MediaSearchResult> searchResult = movieList.searchMovie(searchTerm, imdbId, metadataProvider);
      moviesFound.clear();
      for (MediaSearchResult result : searchResult) {
        moviesFound.add(new MovieChooserModel(metadataProvider, artworkProviders, trailerProviders, result));
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
  }

  /**
   * The Class ScrapeTask.
   */
  private class ScrapeTask extends SwingWorker<Void, Void> {

    /** The model. */
    private MovieChooserModel model;

    /**
     * Instantiates a new scrape task.
     * 
     * @param model
     *          the model
     */
    public ScrapeTask(MovieChooserModel model) {
      this.model = model;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    public Void doInBackground() {
      startProgressBar("scraping: " + model.getName());
      model.scrapeMetaData();

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
  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    JTableBinding<MovieChooserModel, List<MovieChooserModel>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ,
        moviesFound, table);
    //
    BeanProperty<MovieChooserModel, String> movieChooserModelBeanProperty = BeanProperty.create("combinedName");
    jTableBinding.addColumnBinding(movieChooserModelBeanProperty).setColumnName("Search result").setEditable(false);
    //
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_1 = BeanProperty.create("selectedElement.overview");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextPane, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_1,
        tpMovieDescription, jTextPaneBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_2 = BeanProperty.create("selectedElement.posterUrl");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imageUrl");
    AutoBinding<JTable, String, ImageLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_2,
        lblMoviePoster, imageLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.tagline");
    BeanProperty<JTextArea, String> jTextAreaBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextArea, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty,
        lblTagline, jTextAreaBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_3 = BeanProperty.create("selectedElement.combinedName");
    BeanProperty<JTextArea, String> jTextAreaBeanProperty_1 = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextArea, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_3,
        lblMovieName, jTextAreaBeanProperty_1);
    autoBinding_3.bind();
  }

  /**
   * The Class ChangeScraperAction.
   */
  private class ChangeScraperAction extends AbstractAction {

    /**
     * Instantiates a new sort action.
     */
    public ChangeScraperAction() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      MovieScrapers selectedScraper = (MovieScrapers) cbScraper.getSelectedItem();
      metadataProvider = MovieList.getInstance().getMetadataProvider(selectedScraper);
      searchMovie(textFieldSearchString.getText(), "");
    }
  }
}
