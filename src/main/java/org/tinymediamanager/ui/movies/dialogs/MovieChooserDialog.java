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
package org.tinymediamanager.ui.movies.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.ScraperMetadataConfig;
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
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.thirdparty.trakttv.SyncTraktTvTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.TmmSplitPane;
import org.tinymediamanager.ui.components.combobox.MediaScraperComboBox;
import org.tinymediamanager.ui.components.combobox.ScraperMetadataConfigCheckComboBox;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.components.table.TmmTableFormat;
import org.tinymediamanager.ui.components.table.TmmTableModel;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog.ImageType;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.movies.MovieChooserModel;
import org.tinymediamanager.ui.renderer.BorderTableCellRenderer;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieChooser.
 *
 * @author Manuel Laggner
 */
public class MovieChooserDialog extends TmmDialog implements ActionListener {
  private static final long                                              serialVersionUID      = -3104541519073924724L;

  private static final Logger                                            LOGGER                = LoggerFactory.getLogger(MovieChooserDialog.class);

  private MovieList                                                      movieList             = MovieList.getInstance();
  private Movie                                                          movieToScrape;
  private MediaScraper                                                   mediaScraper;
  private List<MediaScraper>                                             artworkScrapers;
  private List<MediaScraper>                                             trailerScrapers;
  private boolean                                                        continueQueue         = true;
  private boolean                                                        navigateBack          = false;

  private SortedList<MovieChooserModel>                                  searchResultEventList = null;
  private EventList<Person>                                              castMemberEventList   = null;
  private MovieChooserModel                                              selectedResult        = null;

  private SearchTask                                                     activeSearchTask;

  /**
   * UI components
   */
  private JTextField                                                     textFieldSearchString;
  private MediaScraperComboBox                                           cbScraper;
  private TmmTable                                                       tableSearchResults;
  private JLabel                                                         lblTitle;
  private JTextArea                                                      taMovieDescription;
  private ImageLabel                                                     lblMoviePoster;
  private JLabel                                                         lblProgressAction;
  private JProgressBar                                                   progressBar;
  private JLabel                                                         lblTagline;
  private JButton                                                        okButton;
  private JLabel                                                         lblPath;
  private JComboBox                                                      cbLanguage;
  private JLabel                                                         lblOriginalTitle;
  private TmmTable                                                       tableCastMembers;
  private ScraperMetadataConfigCheckComboBox<MovieScraperMetadataConfig> cbScraperConfig;

  /**
   * Create the dialog.
   *
   * @param movie
   *          the movie
   * @param queueIndex
   *          the actual index in the queue
   * @param queueSize
   *          the queue size
   */
  public MovieChooserDialog(Movie movie, int queueIndex, int queueSize) {
    super(BUNDLE.getString("moviechooser.search") + (queueSize > 1 ? " " + (queueIndex + 1) + "/" + queueSize : ""), "movieChooser");

    mediaScraper = movieList.getDefaultMediaScraper();
    artworkScrapers = movieList.getDefaultArtworkScrapers();
    trailerScrapers = movieList.getDefaultTrailerScrapers();

    // table format for the search result
    searchResultEventList = new SortedList<>(
        new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(MovieChooserModel.class)),
        new SearchResultScoreComparator());

    DefaultEventTableModel<MovieChooserModel> searchResultTableModel = new TmmTableModel<>(searchResultEventList, new SearchResultTableFormat());

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
        panelPath.add(lblPath, "cell 0 0, growx, wmin 0");
      }

      {
        final JButton btnPlay = new JButton(IconManager.PLAY_INV);
        btnPlay.setFocusable(false);
        btnPlay.addActionListener(e -> {
          MediaFile mf = movieToScrape.getMediaFiles(MediaFileType.VIDEO).get(0);
          try {
            TmmUIHelper.openFile(mf.getFileAsPath());
          }
          catch (Exception ex) {
            LOGGER.error("open file", ex);
            MessageManager.instance
                .pushMessage(new Message(MessageLevel.ERROR, mf, "message.erroropenfile", new String[] { ":", ex.getLocalizedMessage() }));
          }
        });
        panelPath.add(btnPlay, "cell 1 0");
      }
      setTopIformationPanel(panelPath);
    }

    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new MigLayout("", "[600lp:900lp,grow]", "[][shrink 0][250lp:350lp,grow][shrink 0][][]"));
    getContentPane().add(contentPanel, BorderLayout.CENTER);

    {
      JPanel panelSearchField = new JPanel();
      contentPanel.add(panelSearchField, "cell 0 0,grow");
      panelSearchField.setLayout(new MigLayout("insets 0", "[][][grow][]", "[]2lp[]"));
      {
        JLabel lblScraper = new TmmLabel(BUNDLE.getString("scraper"));
        panelSearchField.add(lblScraper, "cell 0 0,alignx right");
      }
      {
        cbScraper = new MediaScraperComboBox(movieList.getAvailableMediaScrapers());
        MediaScraper defaultScraper = movieList.getDefaultMediaScraper();
        cbScraper.setSelectedItem(defaultScraper);
        cbScraper.setAction(new ChangeScraperAction());
        panelSearchField.add(cbScraper, "cell 1 0,growx");
      }
      {
        // also attach the actionlistener to the textfield to trigger the search on enter in the textfield
        ActionListener searchAction = arg0 -> searchMovie(textFieldSearchString.getText(), false);

        textFieldSearchString = new JTextField();
        textFieldSearchString.addActionListener(searchAction);
        panelSearchField.add(textFieldSearchString, "cell 2 0,growx");
        textFieldSearchString.setColumns(10);

        JButton btnSearch = new JButton(BUNDLE.getString("Button.search"));
        panelSearchField.add(btnSearch, "cell 3 0");
        btnSearch.setIcon(IconManager.SEARCH_INV);
        btnSearch.addActionListener(searchAction);
      }
      {
        JLabel lblLanguage = new TmmLabel(BUNDLE.getString("metatag.language"));
        panelSearchField.add(lblLanguage, "cell 0 1,alignx right");
        cbLanguage = new JComboBox(MediaLanguages.valuesSorted());
        cbLanguage.setSelectedItem(MovieModuleManager.SETTINGS.getScraperLanguage());
        cbLanguage.addActionListener(e -> searchMovie(textFieldSearchString.getText(), false));
        panelSearchField.add(cbLanguage, "cell 1 1");
      }
    }
    {
      contentPanel.add(new JSeparator(), "cell 0 1,growx");
    }
    {
      JSplitPane splitPane = new TmmSplitPane();
      splitPane.setResizeWeight(0.5);
      contentPanel.add(splitPane, "cell 0 2,grow");
      {
        JPanel panelSearchResults = new JPanel();
        splitPane.setLeftComponent(panelSearchResults);
        panelSearchResults.setLayout(new MigLayout("insets 0", "[200lp:300lp,grow]", "[150lp:300lp,grow]"));
        {
          JScrollPane scrollPane = new JScrollPane();
          panelSearchResults.add(scrollPane, "cell 0 0,grow");
          tableSearchResults = new TmmTable(searchResultTableModel);
          tableSearchResults.configureScrollPane(scrollPane);
          scrollPane.setViewportView(tableSearchResults);
        }
      }
      {
        JPanel panelSearchDetail = new JPanel();
        splitPane.setRightComponent(panelSearchDetail);
        panelSearchDetail.setLayout(new MigLayout("", "[150lp:n,grow][300lp:500lp,grow]", "[]2lp[]2lp[][150lp:n][50lp:100lp,grow]"));
        {
          lblTitle = new JLabel("");
          TmmFontHelper.changeFont(lblTitle, 1.167, Font.BOLD);
          panelSearchDetail.add(lblTitle, "cell 1 0, wmin 0");
        }
        {
          lblOriginalTitle = new JLabel("");
          panelSearchDetail.add(lblOriginalTitle, "cell 1 1,wmin 0");
        }
        {
          lblTagline = new JLabel("");
          panelSearchDetail.add(lblTagline, "cell 1 2, wmin 0");
        }
        {
          lblMoviePoster = new ImageLabel(false);
          panelSearchDetail.add(lblMoviePoster, "cell 0 0 1 4,grow");
        }
        {
          JScrollPane scrollPane = new JScrollPane();
          panelSearchDetail.add(scrollPane, "cell 1 3,grow");
          scrollPane.setBorder(null);
          {
            taMovieDescription = new ReadOnlyTextArea();
            scrollPane.setViewportView(taMovieDescription);
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
      JLabel lblScrapeFollowingItems = new TmmLabel(BUNDLE.getString("chooser.scrape"));
      contentPanel.add(lblScrapeFollowingItems, "cell 0 4,growx");

      cbScraperConfig = new ScraperMetadataConfigCheckComboBox(MovieScraperMetadataConfig.values());
      contentPanel.add(cbScraperConfig, "cell 0 5,grow, wmin 0");
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
        if (queueSize > 1) {
          JButton abortButton = new JButton(BUNDLE.getString("Button.abortqueue"));
          abortButton.setIcon(IconManager.STOP_INV);
          abortButton.setActionCommand("Abort");
          abortButton.addActionListener(this);
          addButton(abortButton);

          if (queueIndex > 0) {
            JButton backButton = new JButton(BUNDLE.getString("Button.back"));
            backButton.setIcon(IconManager.BACK_INV);
            backButton.setActionCommand("Back");
            backButton.addActionListener(this);
            addButton(backButton);
          }
        }

        JButton cancelButton = new JButton(BUNDLE.getString("Button.cancel"));
        cancelButton.setIcon(IconManager.CANCEL_INV);
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(this);
        addButton(cancelButton);

        okButton = new JButton(BUNDLE.getString("Button.ok"));
        okButton.setIcon(IconManager.APPLY_INV);
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);
        addButton(okButton);
      }
    }

    // install and save the comparator on the Table
    TableComparatorChooser.install(tableSearchResults, searchResultEventList, TableComparatorChooser.SINGLE_COLUMN);

    // double click to take the result
    tableSearchResults.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() >= 2 && !e.isConsumed() && e.getButton() == MouseEvent.BUTTON1 && okButton.isEnabled()) {
          actionPerformed(new ActionEvent(okButton, ActionEvent.ACTION_PERFORMED, "OK"));
        }
      }
    });

    // add a change listener for the async loaded meta data
    PropertyChangeListener listener = evt -> {
      String property = evt.getPropertyName();
      if ("scraped".equals(property)) {
        castMemberEventList.clear();
        int row = tableSearchResults.convertRowIndexToModel(tableSearchResults.getSelectedRow());
        if (row > -1) {
          MovieChooserModel model = searchResultEventList.get(row);
          castMemberEventList.addAll(model.getCastMembers());
          lblOriginalTitle.setText(model.getOriginalTitle());
          lblTagline.setText(model.getTagline());
          if (!model.getPosterUrl().equals(lblMoviePoster.getImageUrl())) {
            lblMoviePoster.setImageUrl(model.getPosterUrl());
          }
          taMovieDescription.setText(model.getOverview());
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
        selectedResult.removePropertyChangeListener(listener);
      }
      if (index > -1 && index < searchResultEventList.size()) {
        MovieChooserModel model = searchResultEventList.get(index);
        castMemberEventList.addAll(model.getCastMembers());
        lblMoviePoster.setImageUrl(model.getPosterUrl());
        lblTitle.setText(model.getCombinedName());
        lblOriginalTitle.setText(model.getOriginalTitle());
        lblTagline.setText(model.getTagline());
        taMovieDescription.setText(model.getOverview());

        selectedResult = model;
        selectedResult.addPropertyChangeListener(listener);
      }
      else {
        selectedResult = null;
      }

      ListSelectionModel lsm = (ListSelectionModel) e.getSource();
      if (!lsm.isSelectionEmpty()) {
        int selectedRow = lsm.getMinSelectionIndex();
        selectedRow = tableSearchResults.convertRowIndexToModel(selectedRow);
        try {
          MovieChooserModel model = searchResultEventList.get(selectedRow);
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

    {
      movieToScrape = movie;
      progressBar.setVisible(false);
      cbScraperConfig.setSelectedItems(MovieModuleManager.SETTINGS.getScraperMetadataConfig());

      textFieldSearchString.setText(movieToScrape.getTitle());
      lblPath.setText(movieToScrape.getPathNIO().resolve(movieToScrape.getMediaFiles(MediaFileType.VIDEO).get(0).getFilename()).toString());
      // initial search with IDs
      searchMovie(textFieldSearchString.getText(), true);
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if ("OK".equals(e.getActionCommand())) {
      int row = tableSearchResults.getSelectedRow();
      if (row >= 0) {
        MovieChooserModel model = searchResultEventList.get(row);
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
          List<MovieScraperMetadataConfig> scraperConfig = cbScraperConfig.getSelectedItems();
          movieToScrape.setMetadata(md, scraperConfig);

          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

          // get images?
          if (ScraperMetadataConfig.containsAnyArtwork(scraperConfig)) {
            // let the user choose the images
            if (!MovieModuleManager.SETTINGS.isScrapeBestImage()) {
              if (scraperConfig.contains(MovieScraperMetadataConfig.POSTER)) {
                chooseArtwork(MediaFileType.POSTER);
              }
              if (scraperConfig.contains(MovieScraperMetadataConfig.FANART) || scraperConfig.contains(MovieScraperMetadataConfig.EXTRAFANART)) {
                chooseArtwork(MediaFileType.FANART);
              }
              if (scraperConfig.contains(MovieScraperMetadataConfig.BANNER)) {
                chooseArtwork(MediaFileType.BANNER);
              }
              if (scraperConfig.contains(MovieScraperMetadataConfig.LOGO)) {
                chooseArtwork(MediaFileType.LOGO);
              }
              if (scraperConfig.contains(MovieScraperMetadataConfig.CLEARLOGO)) {
                chooseArtwork(MediaFileType.CLEARLOGO);
              }
              if (scraperConfig.contains(MovieScraperMetadataConfig.CLEARART)) {
                chooseArtwork(MediaFileType.CLEARART);
              }
              if (scraperConfig.contains(MovieScraperMetadataConfig.DISCART)) {
                chooseArtwork(MediaFileType.DISC);
              }
              if (scraperConfig.contains(MovieScraperMetadataConfig.THUMB) || scraperConfig.contains(MovieScraperMetadataConfig.EXTRATHUMB)) {
                chooseArtwork(MediaFileType.THUMB);
              }
              if (scraperConfig.contains(MovieScraperMetadataConfig.KEYART)) {
                chooseArtwork(MediaFileType.KEYART);
              }
            }
            else {
              // get artwork asynchronous
              model.startArtworkScrapeTask(movieToScrape, scraperConfig);
            }
          }

          // get trailers?
          if (scraperConfig.contains(MovieScraperMetadataConfig.TRAILER)) {
            model.startTrailerScrapeTask(movieToScrape);
          }

          // if configured - sync with trakt.tv
          if (MovieModuleManager.SETTINGS.getSyncTrakt()) {
            TmmTask task = new SyncTraktTvTask(Collections.singletonList(movieToScrape), null);
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

    // navigate back
    if ("Back".equals(e.getActionCommand())) {
      navigateBack = true;
      setVisible(false);
    }
  }

  private void chooseArtwork(MediaFileType mediaFileType) {
    ImageType imageType;
    List<String> extrathumbs = null;
    List<String> extrafanarts = null;

    switch (mediaFileType) {
      case POSTER:
        if (MovieModuleManager.SETTINGS.getPosterFilenames().isEmpty()) {
          return;
        }
        imageType = ImageType.POSTER;
        break;

      case FANART:
        if (MovieModuleManager.SETTINGS.getFanartFilenames().isEmpty()) {
          return;
        }
        imageType = ImageType.FANART;
        if (MovieModuleManager.SETTINGS.isImageExtraThumbs()) {
          extrathumbs = new ArrayList<>();
        }
        if (MovieModuleManager.SETTINGS.isImageExtraFanart()) {
          extrafanarts = new ArrayList<>();
        }
        break;

      case BANNER:
        if (MovieModuleManager.SETTINGS.getBannerFilenames().isEmpty()) {
          return;
        }
        imageType = ImageType.BANNER;
        break;

      case LOGO:
        if (MovieModuleManager.SETTINGS.getLogoFilenames().isEmpty()) {
          return;
        }
        imageType = ImageType.LOGO;
        break;

      case CLEARLOGO:
        if (MovieModuleManager.SETTINGS.getClearlogoFilenames().isEmpty()) {
          return;
        }
        imageType = ImageType.CLEARLOGO;
        break;

      case CLEARART:
        if (MovieModuleManager.SETTINGS.getClearartFilenames().isEmpty()) {
          return;
        }
        imageType = ImageType.CLEARART;
        break;

      case DISC:
        if (MovieModuleManager.SETTINGS.getDiscartFilenames().isEmpty()) {
          return;
        }
        imageType = ImageType.DISC;
        break;

      case THUMB:
        if (MovieModuleManager.SETTINGS.getThumbFilenames().isEmpty()) {
          return;
        }
        imageType = ImageType.THUMB;
        break;

      case KEYART:
        if (MovieModuleManager.SETTINGS.getKeyartFilenames().isEmpty()) {
          return;
        }
        imageType = ImageType.KEYART;
        break;

      default:
        return;
    }

    String imageUrl = ImageChooserDialog.chooseImage(this, movieToScrape.getIds(), imageType, artworkScrapers, extrathumbs, extrafanarts,
        MediaType.MOVIE);

    movieToScrape.setArtworkUrl(imageUrl, mediaFileType);
    if (StringUtils.isNotBlank(imageUrl)) {
      movieToScrape.downloadArtwork(mediaFileType);
    }

    // set extrathumbs and extrafanarts
    if (extrathumbs != null) {
      movieToScrape.setExtraThumbs(extrathumbs);
      if (!extrathumbs.isEmpty()) {
        movieToScrape.downloadArtwork(MediaFileType.EXTRATHUMB);
      }
    }

    if (extrafanarts != null) {
      movieToScrape.setExtraFanarts(extrafanarts);
      if (!extrafanarts.isEmpty()) {
        movieToScrape.downloadArtwork(MediaFileType.EXTRAFANART);
      }
    }
  }

  private void searchMovie(String searchTerm, boolean withIds) {
    if (activeSearchTask != null && !activeSearchTask.isDone()) {
      activeSearchTask.cancel();
    }
    activeSearchTask = new SearchTask(searchTerm, movieToScrape, withIds);
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

  @Override
  public void dispose() {
    if (activeSearchTask != null && !activeSearchTask.isDone()) {
      activeSearchTask.cancel();
    }
    super.dispose();
  }

  public boolean isContinueQueue() {
    return continueQueue;
  }

  public boolean isNavigateBack() {
    return navigateBack;
  }

  /******************************************************************************
   * helper classes
   ******************************************************************************/
  private class ChangeScraperAction extends AbstractAction {
    private static final long serialVersionUID = -4365761222995534769L;

    private ChangeScraperAction() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      mediaScraper = (MediaScraper) cbScraper.getSelectedItem();
      searchMovie(textFieldSearchString.getText(), false);
    }
  }

  private class SearchTask extends SwingWorker<Void, Void> {
    private String                  searchTerm;
    private Movie                   movie;
    private boolean                 withIds;
    private MediaLanguages          language;

    private List<MediaSearchResult> searchResult;
    boolean                         cancel = false;

    private SearchTask(String searchTerm, Movie movie, boolean withIds) {
      this.searchTerm = searchTerm;
      this.movie = movie;
      this.withIds = withIds;
      this.language = (MediaLanguages) cbLanguage.getSelectedItem();
    }

    @Override
    public Void doInBackground() {
      startProgressBar(BUNDLE.getString("chooser.searchingfor") + " " + searchTerm);
      searchResult = movieList.searchMovie(searchTerm, movie.getYear(), withIds ? movie.getIds() : null, mediaScraper, language);
      return null;
    }

    public void cancel() {
      cancel = true;
    }

    @Override
    public void done() {
      if (!cancel) {
        searchResultEventList.clear();
        if (searchResult == null || searchResult.isEmpty()) {
          // display empty result
          searchResultEventList.add(MovieChooserModel.emptyResult);
        }
        else {
          MediaScraper mpFromResult = null;
          for (MediaSearchResult result : searchResult) {
            if (mpFromResult == null) {
              mpFromResult = movieList.getMediaScraperById(result.getProviderId());
            }
            if (mpFromResult == null) {
              // still null? maybe we have a Kodi scraper here where the getProdiverId comes from the sub-scraper; take the scraper from the dropdown
              mpFromResult = (MediaScraper) cbScraper.getSelectedItem();
            }
            searchResultEventList.add(new MovieChooserModel(movieToScrape, mpFromResult, artworkScrapers, trailerScrapers, result, language));
            // get metadataProvider from searchresult
          }
        }
        if (!searchResultEventList.isEmpty()) { // only one result
          tableSearchResults.setRowSelectionInterval(0, 0); // select first row
        }
      }
      stopProgressBar();
    }
  }

  private class ScrapeTask extends SwingWorker<Void, Void> {
    private MovieChooserModel model;

    private ScrapeTask(MovieChooserModel model) {
      this.model = model;
    }

    @Override
    public Void doInBackground() {
      startProgressBar(BUNDLE.getString("chooser.scrapeing") + " " + model.getTitle());

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
   * inner class for representing the result table
   */
  private static class SearchResultTableFormat extends TmmTableFormat<MovieChooserModel> {
    private SearchResultTableFormat() {
      Comparator<MovieChooserModel> searchResultComparator = new SearchResultTitleComparator();
      Comparator<String> stringComparator = new StringComparator();

      FontMetrics fontMetrics = getFontMetrics();

      /*
       * title
       */
      Column col = new Column(BUNDLE.getString("chooser.searchresult"), "title", result -> result, MovieChooserModel.class);
      col.setColumnComparator(searchResultComparator);
      col.setCellRenderer(new SearchResultRenderer());
      addColumn(col);

      /*
       * year
       */
      col = new Column(BUNDLE.getString("metatag.year"), "year", MovieChooserModel::getYear, String.class);
      col.setColumnComparator(stringComparator);
      col.setColumnResizeable(false);
      col.setMinWidth((int) (fontMetrics.stringWidth("2000") * 1.2f));
      col.setMaxWidth((int) (fontMetrics.stringWidth("2000") * 1.4f));
      addColumn(col);
    }
  }

  /**
   * inner class for sorting the search results by score (descending)
   */
  private static class SearchResultScoreComparator implements Comparator<MovieChooserModel> {
    @Override
    public int compare(MovieChooserModel o1, MovieChooserModel o2) {
      return Float.compare(o2.getScore(), o1.getScore());
    }
  }

  /**
   * inner class for sorting the search results by name
   */
  private static class SearchResultTitleComparator implements Comparator<MovieChooserModel> {
    private Collator stringCollator;

    private SearchResultTitleComparator() {
      RuleBasedCollator defaultCollator = (RuleBasedCollator) RuleBasedCollator.getInstance();
      try {
        // default collator ignores whitespaces
        // using hack from http://stackoverflow.com/questions/16567287/java-collation-ignores-space
        stringCollator = new RuleBasedCollator(defaultCollator.getRules().replace("<'\u005f'", "<' '<'\u005f'"));
      }
      catch (Exception e) {
        stringCollator = defaultCollator;
      }
    }

    @Override
    public int compare(MovieChooserModel o1, MovieChooserModel o2) {
      if (stringCollator != null) {
        String titleMovie1 = StrgUtils.normalizeString(o1.getTitle().toLowerCase(Locale.ROOT));
        String titleMovie2 = StrgUtils.normalizeString(o2.getTitle().toLowerCase(Locale.ROOT));
        return stringCollator.compare(titleMovie1, titleMovie2);
      }
      return o1.getTitle().toLowerCase(Locale.ROOT).compareTo(o2.getTitle().toLowerCase(Locale.ROOT));
    }
  }

  /**
   * inner class for representing the cast table
   */
  private static class CastMemberTableFormat implements TableFormat<Person> {
    @Override
    public int getColumnCount() {
      return 2;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return BUNDLE.getString("metatag.name");

        case 1:
          return BUNDLE.getString("metatag.role");
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
  }

  /**
   * inner class to render the search result
   */
  public static class SearchResultRenderer extends BorderTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      if (value instanceof MovieChooserModel) {
        MovieChooserModel result = (MovieChooserModel) value;

        String text = result.getTitle();

        if (result.isDuplicate()) {
          setHorizontalTextPosition(SwingConstants.LEADING);
          setIconTextGap(10);
          setIcon(IconManager.WARN);
          setToolTipText(BUNDLE.getString("moviechooser.duplicate.desc"));
        }
        else {
          setIcon(null);
        }

        return super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
      }
      return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
  }
}
