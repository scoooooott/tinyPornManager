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
package org.tinymediamanager.ui.movies.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.trakttv.SyncTraktTvTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.ReadOnlyTextPane;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.MediaScraperComboBox;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog.ImageType;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.movies.MovieChooserModel;
import org.tinymediamanager.ui.movies.MovieScraperMetadataPanel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieChooser.
 * 
 * @author Manuel Laggner
 */
public class MovieChooserDialog extends TmmDialog implements ActionListener {
  private static final long          serialVersionUID      = -3104541519073924724L;

  private static final Logger        LOGGER                = LoggerFactory.getLogger(MovieChooserDialog.class);

  private MovieList                  movieList             = MovieList.getInstance();
  private Movie                      movieToScrape;
  private List<MovieChooserModel>    moviesFound           = ObservableCollections.observableList(new ArrayList<MovieChooserModel>());
  private MovieScraperMetadataConfig scraperMetadataConfig = new MovieScraperMetadataConfig();
  private MediaScraper               mediaScraper;
  private List<MediaScraper>         artworkScrapers;
  private List<MediaScraper>         trailerScrapers;
  private boolean                    continueQueue         = true;

  private EventList<Person>          castMemberEventList   = null;
  private MovieChooserModel          selectedResult        = null;

  private SearchTask                 activeSearchTask;

  /**
   * UI components
   */
  private JTextField                 textFieldSearchString;
  private MediaScraperComboBox       cbScraper;
  private JTable                     tableSearchResults;
  private JLabel                     lblTitle;
  private JTextPane                  tpMovieDescription;
  private ImageLabel                 lblMoviePoster;
  private JLabel                     lblProgressAction;
  private JProgressBar               progressBar;
  private JLabel                     lblTagline;
  private JButton                    okButton;
  private JLabel                     lblPath;
  private JComboBox                  cbLanguage;
  private JLabel                     lblOriginalTitle;
  private TmmTable                   tableCastMembers;

  /**
   * Create the dialog.
   * 
   * @param movie
   *          the movie
   * @param inQueue
   *          the in queue
   */
  public MovieChooserDialog(Movie movie, boolean inQueue) {
    super(BUNDLE.getString("moviechooser.search"), "movieChooser"); //$NON-NLS-1$

    // copy the values
    MovieScraperMetadataConfig settings = MovieModuleManager.SETTINGS.getMovieScraperMetadataConfig();
    mediaScraper = movieList.getDefaultMediaScraper();
    artworkScrapers = movieList.getDefaultArtworkScrapers();
    trailerScrapers = movieList.getDefaultTrailerScrapers();

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
    scraperMetadataConfig.setTags(settings.isTags());

    // table format for the castmembers
    castMemberEventList = GlazedLists.threadSafeList(new ObservableElementList<>(new BasicEventList<>(), GlazedLists.beanConnector(Person.class)));
    DefaultEventTableModel<Person> castMemberTableModel = new DefaultEventTableModel<>(GlazedListsSwing.swingThreadProxyList(castMemberEventList),
        new CastMemberTableFormat());

    {
      final JPanel panelPath = new JPanel();
      // contentPanel.add(panelPath, "cell 0 0");
      panelPath.setLayout(new MigLayout("", "[grow][]", "[]"));
      {
        lblPath = new JLabel("");
        TmmFontHelper.changeFont(lblPath, 1.16667, Font.BOLD);
        panelPath.add(lblPath, "cell 0 0");
      }

      {
        final JButton btnPlay = new JButton(IconManager.PLAY_SMALL);
        btnPlay.setFocusable(false);
        btnPlay.addActionListener(e -> {
          MediaFile mf = movieToScrape.getMediaFiles(MediaFileType.VIDEO).get(0);
          try {
            TmmUIHelper.openFile(mf.getFileAsPath());
          }
          catch (Exception ex) {
            LOGGER.error("open file", e);
            MessageManager.instance
                .pushMessage(new Message(MessageLevel.ERROR, mf, "message.erroropenfile", new String[] { ":", ex.getLocalizedMessage() }));
          }
        });
        panelPath.add(btnPlay, "cell 1 0");
      }
      setTopIformationPanel(panelPath);
    }

    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new MigLayout("", "[800lp:n,grow]", "[][shrink 0][250lp:350lp,grow][shrink 0][][]"));
    getContentPane().add(contentPanel, BorderLayout.CENTER);

    {
      JPanel panelSearchField = new JPanel();
      contentPanel.add(panelSearchField, "cell 0 0,grow");
      panelSearchField.setLayout(new MigLayout("insets 0", "[][][grow][]", "[]2lp[]"));
      {
        JLabel lblScraper = new TmmLabel(BUNDLE.getString("scraper")); //$NON-NLS-1$
        panelSearchField.add(lblScraper, "cell 0 0,alignx right");
      }
      {
        // cbScraper = new JComboBox(MovieScrapers.values());
        cbScraper = new MediaScraperComboBox(movieList.getAvailableMediaScrapers());
        MediaScraper defaultScraper = movieList.getDefaultMediaScraper();
        cbScraper.setSelectedItem(defaultScraper);
        cbScraper.setAction(new ChangeScraperAction());
        panelSearchField.add(cbScraper, "cell 1 0,growx");
      }
      {
        // also attach the actionlistener to the textfield to trigger the search on enter in the textfield
        ActionListener searchAction = arg0 -> searchMovie(textFieldSearchString.getText(), null);

        textFieldSearchString = new JTextField();
        textFieldSearchString.addActionListener(searchAction);
        panelSearchField.add(textFieldSearchString, "cell 2 0,growx");
        textFieldSearchString.setColumns(10);

        JButton btnSearch = new JButton(BUNDLE.getString("Button.search")); //$NON-NLS-1$
        panelSearchField.add(btnSearch, "cell 3 0");
        btnSearch.setIcon(IconManager.SEARCH);
        btnSearch.addActionListener(searchAction);
      }
      {
        JLabel lblLanguage = new TmmLabel(BUNDLE.getString("metatag.language")); //$NON-NLS-1$
        panelSearchField.add(lblLanguage, "cell 0 1,alignx right");
        cbLanguage = new JComboBox(MediaLanguages.values());
        cbLanguage.setSelectedItem(MovieModuleManager.SETTINGS.getScraperLanguage());
        cbLanguage.addActionListener(e -> searchMovie(textFieldSearchString.getText(), null));
        panelSearchField.add(cbLanguage, "cell 1 1");
      }
    }
    {
      contentPanel.add(new JSeparator(), "cell 0 1,growx");
    }
    {
      JSplitPane splitPane = new JSplitPane();
      splitPane.setResizeWeight(0.5);
      splitPane.setContinuousLayout(true);
      contentPanel.add(splitPane, "cell 0 2,grow");
      {
        JPanel panelSearchResults = new JPanel();
        splitPane.setLeftComponent(panelSearchResults);
        panelSearchResults.setLayout(new MigLayout("insets 0", "[200lp:300lp,grow]", "[150lp:300lp,grow]"));
        {
          {
            JScrollPane scrollPane = new JScrollPane();
            panelSearchResults.add(scrollPane, "cell 0 0,grow");
            tableSearchResults = new JTable();
            scrollPane.setViewportView(tableSearchResults);
            tableSearchResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tableSearchResults.setBorder(new LineBorder(new Color(0, 0, 0)));
            ListSelectionModel rowSM = tableSearchResults.getSelectionModel();
            rowSM.addListSelectionListener(e -> {
              // Ignore extra messages.
              if (e.getValueIsAdjusting())
                return;

              ListSelectionModel lsm = (ListSelectionModel) e.getSource();
              if (!lsm.isSelectionEmpty()) {
                int selectedRow = lsm.getMinSelectionIndex();
                selectedRow = tableSearchResults.convertRowIndexToModel(selectedRow);
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
            });
            tableSearchResults.addMouseListener(new MouseAdapter() {
              @Override
              public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && !e.isConsumed() && e.getButton() == MouseEvent.BUTTON1) {
                  actionPerformed(new ActionEvent(okButton, ActionEvent.ACTION_PERFORMED, "OK"));
                }
              }
            });
          }
        }
      }
      {
        JPanel panelSearchDetail = new JPanel();
        splitPane.setRightComponent(panelSearchDetail);
        panelSearchDetail.setLayout(new MigLayout("", "[150lp:n,grow][300lp:500lp,grow]", "[]2lp[]2lp[][150lp:n][50lp:100lp,grow]"));
        {
          lblTitle = new JLabel("");
          TmmFontHelper.changeFont(lblTitle, 1.167, Font.BOLD);
          panelSearchDetail.add(lblTitle, "cell 1 0");
        }
        {
          lblOriginalTitle = new JLabel("");
          panelSearchDetail.add(lblOriginalTitle, "cell 1 1");
        }
        {
          lblTagline = new JLabel("");
          panelSearchDetail.add(lblTagline, "cell 1 2");
        }
        {
          lblMoviePoster = new ImageLabel(false);
          panelSearchDetail.add(lblMoviePoster, "cell 0 0 1 4,grow");
        }
        {
          JScrollPane scrollPane = new JScrollPane();
          panelSearchDetail.add(scrollPane, "cell 1 3,growx");
          scrollPane.setBorder(null);
          {
            tpMovieDescription = new ReadOnlyTextPane();
            tpMovieDescription.setOpaque(false);
            scrollPane.setViewportView(tpMovieDescription);
          }
        }
        {
          JScrollPane scrollPane = new JScrollPane();
          panelSearchDetail.add(scrollPane, "cell 0 4 2 1,grow");
          {
            tableCastMembers = new TmmTable(castMemberTableModel);
            tableCastMembers.configureScrollPane(scrollPane);
            scrollPane.setViewportView(tableCastMembers);
          }
        }
      }
    }
    {
      JSeparator separator = new JSeparator();
      contentPanel.add(separator, "cell 0 3,growx");
    }
    {
      JLabel lblScrapeFollowingItems = new TmmLabel(BUNDLE.getString("chooser.scrape")); //$NON-NLS-1$
      contentPanel.add(lblScrapeFollowingItems, "cell 0 4,growx");

      JPanel panelScraperMetadataSetting = new MovieScraperMetadataPanel(scraperMetadataConfig);
      contentPanel.add(panelScraperMetadataSetting, "cell 0 5,grow");
    }

    {
      {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new MigLayout("", "[][grow]", "[]"));

        progressBar = new JProgressBar();
        infoPanel.add(progressBar, "cell 0 0");

        lblProgressAction = new JLabel("");
        infoPanel.add(lblProgressAction, "cell 1 0");

        setBottomInformationPanel(infoPanel);
      }
      {
        if (inQueue) {
          JButton abortButton = new JButton(BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
          abortButton.setIcon(IconManager.PROCESS_STOP);
          abortButton.setActionCommand("Abort");
          abortButton.addActionListener(this);
          addButton(abortButton);
        }

        JButton cancelButton = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
        cancelButton.setIcon(IconManager.CANCEL_INV);
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(this);
        addButton(cancelButton);

        okButton = new JButton(BUNDLE.getString("Button.ok")); //$NON-NLS-1$
        okButton.setIcon(IconManager.APPLY_INV);
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);
        addButton(okButton);
      }
    }

    initDataBindings();

    // add a change listener for the cast members
    PropertyChangeListener listener = evt -> {
      String property = evt.getPropertyName();
      if ("castMembers".equals(property)) {
        castMemberEventList.clear();
        int row = tableSearchResults.convertRowIndexToModel(tableSearchResults.getSelectedRow());
        if (row > -1) {
          MovieChooserModel model = moviesFound.get(row);
          castMemberEventList.addAll(model.getCastMembers());
        }
      }
    };

    tableSearchResults.getSelectionModel().addListSelectionListener(e -> {
      if (e.getValueIsAdjusting()) {
        return;
      }

      int index = tableSearchResults.convertRowIndexToModel(tableSearchResults.getSelectedRow());
      castMemberEventList.clear();
      if (selectedResult != null) {
        removePropertyChangeListener(listener);
      }
      if (index > -1) {
        MovieChooserModel model = moviesFound.get(index);
        selectedResult = model;
        castMemberEventList.addAll(model.getCastMembers());
        selectedResult.addPropertyChangeListener(listener);
      }
      else {
        selectedResult = null;
      }
    });

    {
      movieToScrape = movie;
      progressBar.setVisible(false);

      // adjust column name
      tableSearchResults.getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("chooser.searchresult"));
      textFieldSearchString.setText(movieToScrape.getTitle());
      searchMovie(textFieldSearchString.getText(), movieToScrape);

      lblPath.setText(movieToScrape.getPathNIO().resolve(movieToScrape.getMediaFiles(MediaFileType.VIDEO).get(0).getFilename()).toString());
    }

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if ("OK".equals(e.getActionCommand())) {
      int row = tableSearchResults.getSelectedRow();
      if (row >= 0) {
        MovieChooserModel model = moviesFound.get(row);
        if (model != MovieChooserModel.emptyResult) {
          // when scraping was not successful, abort saving
          if (!model.isScraped()) {
            MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "MovieChooser", "message.scrape.threadcrashed"));
            return;
          }

          MediaMetadata md = model.getMetadata();

          // did the user want to choose the images?
          if (!MovieModuleManager.SETTINGS.isScrapeBestImage()) {
            md.clearMediaArt();
          }

          // set scraped metadata
          movieToScrape.setMetadata(md, scraperMetadataConfig);

          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

          // get images?
          if (scraperMetadataConfig.isArtwork()) {
            // let the user choose the images
            if (!MovieModuleManager.SETTINGS.isScrapeBestImage()) {
              // poster
              {
                ImageLabel lblImage = new ImageLabel();
                ImageChooserDialog dialog = new ImageChooserDialog(movieToScrape.getIds(), ImageType.POSTER, artworkScrapers, lblImage, null, null,
                    MediaType.MOVIE);
                dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
                dialog.setVisible(true);
                movieToScrape.setArtworkUrl(lblImage.getImageUrl(), MediaFileType.POSTER);
                movieToScrape.downloadArtwork(MediaFileType.POSTER);
              }

              // fanart
              {
                ImageLabel lblImage = new ImageLabel();
                List<String> extrathumbs = new ArrayList<>();
                List<String> extrafanarts = new ArrayList<>();
                ImageChooserDialog dialog = new ImageChooserDialog(movieToScrape.getIds(), ImageType.FANART, artworkScrapers, lblImage, extrathumbs,
                    extrafanarts, MediaType.MOVIE);
                dialog.setVisible(true);
                movieToScrape.setArtworkUrl(lblImage.getImageUrl(), MediaFileType.FANART);
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

              // banner
              if (!MovieModuleManager.SETTINGS.getBannerFilenames().isEmpty()) {
                ImageLabel lblImage = new ImageLabel();
                ImageChooserDialog dialog = new ImageChooserDialog(movieToScrape.getIds(), ImageType.BANNER, artworkScrapers, lblImage, null, null,
                    MediaType.MOVIE);
                dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
                dialog.setVisible(true);
                movieToScrape.setArtworkUrl(lblImage.getImageUrl(), MediaFileType.BANNER);
                movieToScrape.downloadArtwork(MediaFileType.BANNER);
              }

              // logo
              if (!MovieModuleManager.SETTINGS.getLogoFilenames().isEmpty()) {
                ImageLabel lblImage = new ImageLabel();
                ImageChooserDialog dialog = new ImageChooserDialog(movieToScrape.getIds(), ImageType.LOGO, artworkScrapers, lblImage, null, null,
                    MediaType.MOVIE);
                dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
                dialog.setVisible(true);
                movieToScrape.setArtworkUrl(lblImage.getImageUrl(), MediaFileType.LOGO);
                movieToScrape.downloadArtwork(MediaFileType.LOGO);
              }

              // clearlogo
              if (!MovieModuleManager.SETTINGS.getClearlogoFilenames().isEmpty()) {
                ImageLabel lblImage = new ImageLabel();
                ImageChooserDialog dialog = new ImageChooserDialog(movieToScrape.getIds(), ImageType.CLEARLOGO, artworkScrapers, lblImage, null, null,
                    MediaType.MOVIE);
                dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
                dialog.setVisible(true);
                movieToScrape.setArtworkUrl(lblImage.getImageUrl(), MediaFileType.CLEARLOGO);
                movieToScrape.downloadArtwork(MediaFileType.CLEARLOGO);
              }

              // clearart
              if (!MovieModuleManager.SETTINGS.getClearartFilenames().isEmpty()) {
                ImageLabel lblImage = new ImageLabel();
                ImageChooserDialog dialog = new ImageChooserDialog(movieToScrape.getIds(), ImageType.CLEARART, artworkScrapers, lblImage, null, null,
                    MediaType.MOVIE);
                dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
                dialog.setVisible(true);
                movieToScrape.setArtworkUrl(lblImage.getImageUrl(), MediaFileType.CLEARART);
                movieToScrape.downloadArtwork(MediaFileType.CLEARART);
              }

              // discart
              if (!MovieModuleManager.SETTINGS.getDiscartFilenames().isEmpty()) {
                ImageLabel lblImage = new ImageLabel();
                ImageChooserDialog dialog = new ImageChooserDialog(movieToScrape.getIds(), ImageType.DISC, artworkScrapers, lblImage, null, null,
                    MediaType.MOVIE);
                dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
                dialog.setVisible(true);
                movieToScrape.setArtworkUrl(lblImage.getImageUrl(), MediaFileType.DISC);
                movieToScrape.downloadArtwork(MediaFileType.DISC);
              }

              // thumb
              if (!MovieModuleManager.SETTINGS.getThumbFilenames().isEmpty()) {
                ImageLabel lblImage = new ImageLabel();
                ImageChooserDialog dialog = new ImageChooserDialog(movieToScrape.getIds(), ImageType.THUMB, artworkScrapers, lblImage, null, null,
                    MediaType.MOVIE);
                dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
                dialog.setVisible(true);
                movieToScrape.setArtworkUrl(lblImage.getImageUrl(), MediaFileType.THUMB);
                movieToScrape.downloadArtwork(MediaFileType.THUMB);
              }
            }
            else {
              // get artwork asynchronous
              model.startArtworkScrapeTask(movieToScrape, scraperMetadataConfig);
            }
          }

          // get trailers?
          if (scraperMetadataConfig.isTrailer()) {
            model.startTrailerScrapeTask(movieToScrape);
          }

          // if configured - sync with trakt.tv
          if (MovieModuleManager.SETTINGS.getSyncTrakt()) {
            TmmTask task = new SyncTraktTvTask(Arrays.asList(movieToScrape), null);
            TmmTaskManager.getInstance().addUnnamedTask(task);
          }

          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

          setVisible(false);
        }
      }
    }

    // cancel
    if ("Cancel".equals(e.getActionCommand())) {
      setVisible(false);
    }

    // Abort queue
    if ("Abort".equals(e.getActionCommand())) {
      continueQueue = false;
      setVisible(false);
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
    SwingUtilities.invokeLater(() -> {
      lblProgressAction.setText(description);
      progressBar.setVisible(true);
      progressBar.setIndeterminate(true);
    });
  }

  private void stopProgressBar() {
    SwingUtilities.invokeLater(() -> {
      lblProgressAction.setText("");
      progressBar.setVisible(false);
      progressBar.setIndeterminate(false);
    });
  }

  /**
   * Shows the dialog and returns whether the work on the queue should be continued.
   * 
   * @return true, if successful
   */
  public boolean showDialog() {
    // pack();
    // setLocationRelativeTo(MainWindow.getActiveInstance());
    setVisible(true);
    return continueQueue;
  }

  @Override
  public void dispose() {
    if (activeSearchTask != null && !activeSearchTask.isDone()) {
      activeSearchTask.cancel();
    }
    super.dispose();
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
      mediaScraper = (MediaScraper) cbScraper.getSelectedItem();
      searchMovie(textFieldSearchString.getText(), movieToScrape);
    }
  }

  private class SearchTask extends SwingWorker<Void, Void> {
    private String                  searchTerm;
    private Movie                   movie;
    private List<MediaSearchResult> searchResult;
    private MediaLanguages          language;
    boolean                         cancel = false;

    public SearchTask(String searchTerm, Movie movie) {
      this.searchTerm = searchTerm;
      this.movie = movie;
      this.language = (MediaLanguages) cbLanguage.getSelectedItem();
    }

    @Override
    public Void doInBackground() {
      startProgressBar(BUNDLE.getString("chooser.searchingfor") + " " + searchTerm); //$NON-NLS-1$
      searchResult = movieList.searchMovie(searchTerm, movie, mediaScraper, language);
      return null;
    }

    public void cancel() {
      cancel = true;
    }

    @Override
    public void done() {
      if (!cancel) {
        moviesFound.clear();
        if (searchResult == null || searchResult.size() == 0) {
          // display empty result
          moviesFound.add(MovieChooserModel.emptyResult);
        }
        else {
          MediaScraper mpFromResult = null;
          for (MediaSearchResult result : searchResult) {
            if (mpFromResult == null) {
              mpFromResult = movieList.getMediaScraperById(result.getProviderId());
            }
            moviesFound.add(new MovieChooserModel(mpFromResult, artworkScrapers, trailerScrapers, result, language));
            // get metadataProvider from searchresult
          }
        }
        if (!moviesFound.isEmpty()) { // only one result
          tableSearchResults.setRowSelectionInterval(0, 0); // select first row
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
      startProgressBar(BUNDLE.getString("chooser.scrapeing") + " " + model.getTitle()); //$NON-NLS-1$

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

  /**
   * inner class for representing the table
   */
  private static class CastMemberTableFormat implements AdvancedTableFormat<Person> {
    @Override
    public int getColumnCount() {
      return 2;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return BUNDLE.getString("metatag.name");//$NON-NLS-1$

        case 1:
          return BUNDLE.getString("metatag.role");//$NON-NLS-1$
      }
      throw new IllegalStateException();
    }

    @Override
    public Object getColumnValue(Person castMember, int column) {
      switch (column) {
        case 0:
          return castMember.getName();

        case 1:
          return castMember.getRole();
      }
      throw new IllegalStateException();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getColumnClass(int column) {
      switch (column) {
        case 0:
        case 1:
          return String.class;
      }
      throw new IllegalStateException();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Comparator getColumnComparator(int column) {
      return null;
    }
  }

  protected void initDataBindings() {
    JTableBinding<MovieChooserModel, List<MovieChooserModel>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ,
        moviesFound, tableSearchResults);
    //
    BeanProperty<MovieChooserModel, String> movieChooserModelBeanProperty = BeanProperty.create("combinedName");
    jTableBinding.addColumnBinding(movieChooserModelBeanProperty).setEditable(false);
    //
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_1 = BeanProperty.create("selectedElement.overview");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextPane, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, tableSearchResults,
        jTableBeanProperty_1, tpMovieDescription, jTextPaneBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_2 = BeanProperty.create("selectedElement.posterUrl");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imageUrl");
    AutoBinding<JTable, String, ImageLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, tableSearchResults,
        jTableBeanProperty_2, lblMoviePoster, imageLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.tagline");
    BeanProperty<JLabel, String> jTextAreaBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, tableSearchResults, jTableBeanProperty,
        lblTagline, jTextAreaBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_3 = BeanProperty.create("selectedElement.combinedName");
    BeanProperty<JLabel, String> jTextAreaBeanProperty_1 = BeanProperty.create("text");
    AutoBinding<JTable, String, JLabel, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, tableSearchResults,
        jTableBeanProperty_3, lblTitle, jTextAreaBeanProperty_1);
    autoBinding_3.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_4 = BeanProperty.create("selectedElement.originalTitle");
    AutoBinding<JTable, String, JLabel, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ, tableSearchResults,
        jTableBeanProperty_4, lblOriginalTitle, jTextAreaBeanProperty);
    autoBinding_4.bind();
  }
}
