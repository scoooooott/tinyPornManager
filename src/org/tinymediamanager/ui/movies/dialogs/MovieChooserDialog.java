/*
 * Copyright 2012 - 2014 Manuel Laggner
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
package org.tinymediamanager.ui.movies.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieScrapers;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog.ImageType;
import org.tinymediamanager.ui.movies.MovieChooserModel;
import org.tinymediamanager.ui.movies.MovieScraperMetadataPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieChooser.
 * 
 * @author Manuel Laggner
 */
public class MovieChooserDialog extends JDialog implements ActionListener {
  private static final long                                                 serialVersionUID      = -3104541519073924724L;
  private static final ResourceBundle                                       BUNDLE                = ResourceBundle.getBundle(
                                                                                                      "messages", new UTF8Control());                     //$NON-NLS-1$
  private static final Logger                                               LOGGER                = LoggerFactory.getLogger(MovieChooserDialog.class);

  private MovieList                                                         movieList             = MovieList.getInstance();
  private Movie                                                             movieToScrape;
  private List<MovieChooserModel>                                           moviesFound           = ObservableCollections
                                                                                                      .observableList(new ArrayList<MovieChooserModel>());
  private MovieScraperMetadataConfig                                        scraperMetadataConfig = new MovieScraperMetadataConfig();
  private IMediaMetadataProvider                                            metadataProvider;
  private List<IMediaArtworkProvider>                                       artworkProviders;
  private List<IMediaTrailerProvider>                                       trailerProviders;
  private boolean                                                           continueQueue         = true;

  private SearchTask                                                        activeSearchTask;

  /**
   * UI components
   */
  private final JPanel                                                      contentPanel          = new JPanel();
  private JTextField                                                        textFieldSearchString;
  private JComboBox                                                         cbScraper;
  private JTable                                                            table;
  private JLabel                                                            lblMovieName;
  private JTextPane                                                         tpMovieDescription;
  private ImageLabel                                                        lblMoviePoster;
  private JLabel                                                            lblProgressAction;
  private JProgressBar                                                      progressBar;
  private JLabel                                                            lblTagline;
  private JButton                                                           okButton;
  private JLabel                                                            lblPath;

  private JTableBinding<MovieChooserModel, List<MovieChooserModel>, JTable> jTableBinding;
  private AutoBinding<JTable, String, JLabel, String>                       autoBinding;
  private AutoBinding<JTable, String, JTextPane, String>                    autoBinding_1;
  private AutoBinding<JTable, String, ImageLabel, String>                   autoBinding_2;
  private AutoBinding<JTable, String, JLabel, String>                       autoBinding_3;

  /**
   * Create the dialog.
   * 
   * @param movie
   *          the movie
   * @param inQueue
   *          the in queue
   */
  public MovieChooserDialog(Movie movie, boolean inQueue) {
    setTitle(BUNDLE.getString("moviechooser.search")); //$NON-NLS-1$
    setName("movieChooser");
    setBounds(5, 5, 960, 642);
    TmmWindowSaver.loadSettings(this);
    setIconImage(MainWindow.LOGO);
    setModal(true);

    // copy the values
    MovieScraperMetadataConfig settings = Globals.settings.getMovieScraperMetadataConfig();
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
    scraperMetadataConfig.setCollection(settings.isCollection());

    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("800px:grow"),
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("fill:300px:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));
    {
      lblPath = new JLabel("");
      contentPanel.add(lblPath, "2, 2");
    }
    {
      JPanel panelSearchField = new JPanel();
      contentPanel.add(panelSearchField, "2, 4, fill, fill");
      panelSearchField.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("right:max(100px;default)"), }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, }));
      {
        JLabel lblScraper = new JLabel(BUNDLE.getString("scraper")); //$NON-NLS-1$
        panelSearchField.add(lblScraper, "2, 1, right, default");
      }
      {
        cbScraper = new JComboBox(MovieScrapers.values());
        MovieScrapers defaultScraper = MovieModuleManager.MOVIE_SETTINGS.getMovieScraper();
        cbScraper.setSelectedItem(defaultScraper);
        cbScraper.setAction(new ChangeScraperAction());
        panelSearchField.add(cbScraper, "4, 1, fill, default");
      }
      {
        textFieldSearchString = new JTextField();
        panelSearchField.add(textFieldSearchString, "6, 1, fill, default");
        textFieldSearchString.setColumns(10);
      }

      {
        JButton btnSearch = new JButton(BUNDLE.getString("Button.search")); //$NON-NLS-1$
        panelSearchField.add(btnSearch, "8, 1, fill, default");
        btnSearch.setIcon(IconManager.SEARCH);
        btnSearch.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
            searchMovie(textFieldSearchString.getText(), null);
          }
        });
        getRootPane().setDefaultButton(btnSearch);
      }
    }
    {
      JSplitPane splitPane = new JSplitPane();
      splitPane.setResizeWeight(0.5);
      splitPane.setContinuousLayout(true);
      contentPanel.add(splitPane, "2, 6, fill, fill");
      {
        JPanel panelSearchResults = new JPanel();
        splitPane.setLeftComponent(panelSearchResults);
        panelSearchResults.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("200px:grow"), },
            new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("fill:260px:grow"), }));
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
                    if (model != MovieChooserModel.emptyResult && !model.isScraped()) {
                      ScrapeTask task = new ScrapeTask(model);
                      task.execute();
                    }
                  }
                  catch (Exception ex) {
                    LOGGER.warn(ex.getMessage());
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
        panelSearchDetail.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("350px:grow"),
            FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, RowSpec.decode("20px"),
            FormFactory.PARAGRAPH_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), }));
        {
          lblMovieName = new JLabel("");
          TmmFontHelper.changeFont(lblMovieName, 1.167, Font.BOLD);
          panelSearchDetail.add(lblMovieName, "2, 1, default, top");
        }
        {
          lblTagline = new JLabel("");
          panelSearchDetail.add(lblTagline, "2, 2, default, top");
        }
        {
          JPanel panel = new JPanel();
          panelSearchDetail.add(panel, "2, 4, fill, fill");
          panel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("150px"), FormFactory.RELATED_GAP_COLSPEC,
              ColumnSpec.decode("200px:grow"), }, new RowSpec[] { RowSpec.decode("240px"), }));
          {
            lblMoviePoster = new ImageLabel(false);
            panel.add(lblMoviePoster, "1, 1, fill, fill");
            lblMoviePoster.setAlternativeText("");
          }
          {
            JScrollPane scrollPane = new JScrollPane();
            panel.add(scrollPane, "3, 1, fill, fill");
            scrollPane.setBorder(null);
            {
              tpMovieDescription = new JTextPane();
              tpMovieDescription.setOpaque(false);
              tpMovieDescription.setEditable(false);
              scrollPane.setViewportView(tpMovieDescription);
            }
          }
        }
      }
    }
    {
      JLabel lblScrapeFollowingItems = new JLabel(BUNDLE.getString("chooser.scrape")); //$NON-NLS-1$
      contentPanel.add(lblScrapeFollowingItems, "2, 8");
    }
    {
      JPanel panelScraperMetadataSetting = new MovieScraperMetadataPanel(scraperMetadataConfig);
      contentPanel.add(panelScraperMetadataSetting, "2, 9, fill, fill");
    }

    {
      JPanel bottomPane = new JPanel();
      contentPanel.add(bottomPane, "2, 11");
      {
        bottomPane.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("max(82dlu;default)"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
            FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC }));
        {
          progressBar = new JProgressBar();
          bottomPane.add(progressBar, "2, 2");
        }
        {
          lblProgressAction = new JLabel("");
          bottomPane.add(lblProgressAction, "4, 2");
        }
        {
          JPanel buttonPane = new JPanel();
          bottomPane.add(buttonPane, "5, 2, fill, fill");
          EqualsLayout layout = new EqualsLayout(5);
          layout.setMinWidth(100);
          buttonPane.setLayout(layout);
          okButton = new JButton(BUNDLE.getString("Button.ok")); //$NON-NLS-1$
          okButton.setIcon(IconManager.APPLY);
          buttonPane.add(okButton);
          okButton.setActionCommand("OK");
          okButton.addActionListener(this);

          JButton cancelButton = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
          cancelButton.setIcon(IconManager.CANCEL);
          buttonPane.add(cancelButton);
          cancelButton.setActionCommand("Cancel");
          cancelButton.addActionListener(this);

          if (inQueue) {
            JButton abortButton = new JButton(BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
            abortButton.setIcon(IconManager.PROCESS_STOP);
            buttonPane.add(abortButton);
            abortButton.setActionCommand("Abort");
            abortButton.addActionListener(this);
          }
        }
      }
    }

    {
      movieToScrape = movie;
      progressBar.setVisible(false);
      initDataBindings();

      // adjust column name
      table.getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("chooser.searchresult"));
      lblPath.setText(movieToScrape.getPath() + File.separatorChar + movieToScrape.getMediaFiles(MediaFileType.VIDEO).get(0).getFilename());
      textFieldSearchString.setText(movieToScrape.getTitle());
      searchMovie(textFieldSearchString.getText(), movieToScrape);
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if ("OK".equals(e.getActionCommand())) {
      int row = table.getSelectedRow();
      if (row >= 0) {
        MovieChooserModel model = moviesFound.get(row);
        if (model != MovieChooserModel.emptyResult) {
          MediaMetadata md = model.getMetadata();

          // did the user want to choose the images?
          if (!MovieModuleManager.MOVIE_SETTINGS.isScrapeBestImage()) {
            md.clearMediaArt();
          }

          // set scraped metadata
          movieToScrape.setMetadata(md, scraperMetadataConfig);

          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

          // get images?
          if (scraperMetadataConfig.isArtwork()) {
            // let the user choose the images
            if (!MovieModuleManager.MOVIE_SETTINGS.isScrapeBestImage()) {
              // poster
              {
                ImageLabel lblImage = new ImageLabel();
                ImageChooserDialog dialog = new ImageChooserDialog(movieToScrape.getIds(), ImageType.POSTER, artworkProviders, lblImage, null, null,
                    MediaType.MOVIE);
                dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
                dialog.setVisible(true);
                movieToScrape.setPosterUrl(lblImage.getImageUrl());
                movieToScrape.downloadArtwork(MediaFileType.POSTER);
              }

              // fanart
              {
                ImageLabel lblImage = new ImageLabel();
                List<String> extrathumbs = new ArrayList<String>();
                List<String> extrafanarts = new ArrayList<String>();
                ImageChooserDialog dialog = new ImageChooserDialog(movieToScrape.getIds(), ImageType.FANART, artworkProviders, lblImage, extrathumbs,
                    extrafanarts, MediaType.MOVIE);
                dialog.setVisible(true);
                movieToScrape.setFanartUrl(lblImage.getImageUrl());
                movieToScrape.downloadArtwork(MediaFileType.FANART);

                // set extrathumbs and extrafanarts
                movieToScrape.setExtraThumbs(extrathumbs);
                movieToScrape.setExtraFanarts(extrafanarts);
                if (extrafanarts.size() > 0) {
                  movieToScrape.downloadArtwork(MediaFileType.EXTRAFANART);
                }

                if (extrathumbs.size() > 0) {
                  movieToScrape.downloadArtwork(MediaFileType.EXTRATHUMB);
                }
              }
            }
            else {
              // get artwork directly from provider
              List<MediaArtwork> artwork = model.getArtwork();
              movieToScrape.setArtwork(artwork, scraperMetadataConfig);
            }
          }

          // get trailers?
          if (scraperMetadataConfig.isTrailer()) {
            List<MediaTrailer> trailers = model.getTrailers();
            // add local trailers!
            for (MediaFile mf : movieToScrape.getMediaFiles(MediaFileType.TRAILER)) {
              LOGGER.debug("adding local trailer " + mf.getFilename());
              MediaTrailer mt = new MediaTrailer();
              mt.setName(mf.getFilename());
              mt.setProvider("downloaded");
              mt.setQuality(mf.getVideoFormat());
              mt.setInNfo(false);
              mt.setUrl(mf.getFile().toURI().toString());
              trailers.add(0, mt); // add as first
            }
            movieToScrape.setTrailers(trailers);
          }

          // rewrite the complete NFO
          movieToScrape.writeNFO();

          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

          this.setVisible(false);
          dispose();
        }
      }
    }

    // cancel
    if ("Cancel".equals(e.getActionCommand())) {
      this.setVisible(false);
      dispose();
    }

    // Abort queue
    if ("Abort".equals(e.getActionCommand())) {
      continueQueue = false;
      this.setVisible(false);
      dispose();
    }

  }

  private void searchMovie(String searchTerm, Movie movie) {
    if (activeSearchTask != null && !activeSearchTask.isDone()) {
      activeSearchTask.cancel();
    }
    activeSearchTask = new SearchTask(searchTerm, movie);
    activeSearchTask.execute();
  }

  private void startProgressBar(final String description) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        lblProgressAction.setText(description);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
      }
    });
  }

  private void stopProgressBar() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        lblProgressAction.setText("");
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
      }
    });
  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, moviesFound, table);
    //
    BeanProperty<MovieChooserModel, String> movieChooserModelBeanProperty = BeanProperty.create("combinedName");
    jTableBinding.addColumnBinding(movieChooserModelBeanProperty).setEditable(false); //$NON-NLS-1$
    //
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_1 = BeanProperty.create("selectedElement.overview");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_1, tpMovieDescription, jTextPaneBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_2 = BeanProperty.create("selectedElement.posterUrl");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imageUrl");
    autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_2, lblMoviePoster, imageLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.tagline");
    BeanProperty<JLabel, String> jTextAreaBeanProperty = BeanProperty.create("text");
    autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty, lblTagline, jTextAreaBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_3 = BeanProperty.create("selectedElement.combinedName");
    BeanProperty<JLabel, String> jTextAreaBeanProperty_1 = BeanProperty.create("text");
    autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_3, lblMovieName, jTextAreaBeanProperty_1);
    autoBinding_3.bind();
  }

  /**
   * Shows the dialog and returns whether the work on the queue should be continued.
   * 
   * @return true, if successful
   */
  public boolean showDialog() {
    pack();
    setLocationRelativeTo(MainWindow.getActiveInstance());
    setVisible(true);
    return continueQueue;
  }

  @Override
  public void dispose() {
    if (activeSearchTask != null && !activeSearchTask.isDone()) {
      activeSearchTask.cancel();
    }
    super.dispose();
    jTableBinding.unbind();
    autoBinding.unbind();
    autoBinding_1.unbind();
    autoBinding_2.unbind();
    autoBinding_3.unbind();
  }

  /******************************************************************************
   * helper classes
   ******************************************************************************/
  private class ChangeScraperAction extends AbstractAction {
    private static final long serialVersionUID = -4365761222995534769L;

    public ChangeScraperAction() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MovieScrapers selectedScraper = (MovieScrapers) cbScraper.getSelectedItem();
      metadataProvider = MovieList.getInstance().getMetadataProvider(selectedScraper);
      searchMovie(textFieldSearchString.getText(), null);
    }
  }

  private class SearchTask extends SwingWorker<Void, Void> {
    private String                  searchTerm;
    private Movie                   movie;
    private List<MediaSearchResult> searchResult;
    boolean                         cancel = false;

    public SearchTask(String searchTerm, Movie movie) {
      this.searchTerm = searchTerm;
      this.movie = movie;
    }

    @Override
    public Void doInBackground() {
      startProgressBar(BUNDLE.getString("chooser.searchingfor") + " " + searchTerm); //$NON-NLS-1$
      searchResult = movieList.searchMovie(searchTerm, movie, metadataProvider);
      return null;
    }

    public void cancel() {
      cancel = true;
    }

    @Override
    public void done() {
      if (!cancel) {
        moviesFound.clear();
        if (searchResult.size() == 0) {
          // display empty result
          moviesFound.add(MovieChooserModel.emptyResult);
        }
        else {
          IMediaMetadataProvider mpFromResult = null;
          for (MediaSearchResult result : searchResult) {
            if (mpFromResult == null) {
              mpFromResult = MovieList.getInstance().getMetadataProvider(result.getProviderId());
            }
            moviesFound.add(new MovieChooserModel(mpFromResult, artworkProviders, trailerProviders, result));
            // get metadataProvider from searchresult
          }
        }
        if (moviesFound.size() == 1) { // only one result
          table.setRowSelectionInterval(0, 0); // select first row
        }
      }
      stopProgressBar();
    }
  }

  private class ScrapeTask extends SwingWorker<Void, Void> {
    private MovieChooserModel model;

    public ScrapeTask(MovieChooserModel model) {
      this.model = model;
    }

    @Override
    public Void doInBackground() {
      startProgressBar(BUNDLE.getString("chooser.scrapeing") + " " + model.getName()); //$NON-NLS-1$

      // disable button as long as its scraping
      okButton.setEnabled(false);
      model.scrapeMetaData();
      okButton.setEnabled(true);
      return null;
    }

    @Override
    public void done() {
      stopProgressBar();
    }
  }
}
