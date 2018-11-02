/*
 * Copyright 2012 - 2018 Manuel Laggner
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
package org.tinymediamanager.ui.tvshows.dialogs;

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
import java.text.Normalizer;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.tasks.TvShowEpisodeScrapeTask;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.trakttv.SyncTraktTvTask;
import org.tinymediamanager.ui.BorderTableCellRenderer;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.TmmSplitPane;
import org.tinymediamanager.ui.components.combobox.MediaScraperComboBox;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.components.table.TmmTableFormat;
import org.tinymediamanager.ui.components.table.TmmTableModel;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog.ImageType;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.tvshows.TvShowChooserModel;
import org.tinymediamanager.ui.tvshows.panels.TvShowScraperMetadataPanel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowChooserDialog.
 *
 * @author Manuel Laggner
 */
public class TvShowChooserDialog extends TmmDialog implements ActionListener {
  private static final long              serialVersionUID      = 2371518113606870230L;
  private static final Logger            LOGGER                = LoggerFactory.getLogger(TvShowChooserDialog.class);

  private TvShowList                     tvShowList            = TvShowList.getInstance();
  private TvShow                         tvShowToScrape;
  private SortedList<TvShowChooserModel> searchResultEventList = null;
  private TvShowChooserModel             selectedResult        = null;
  private TvShowScraperMetadataConfig    scraperMetadataConfig = new TvShowScraperMetadataConfig();
  private MediaScraper                   mediaScraper;
  private List<MediaScraper>             artworkScrapers;
  private boolean                        continueQueue         = true;
  private boolean                        navigateBack          = false;

  private SearchTask                     activeSearchTask;

  private JTextField                     textFieldSearchString;
  private MediaScraperComboBox           cbScraper;
  private JComboBox<MediaLanguages>      cbLanguage;
  private TmmTable                       tableSearchResults;
  private JLabel                         lblTtitle;
  private JTextArea                      taOverview;
  private ImageLabel                     lblTvShowPoster;
  private JLabel                         lblProgressAction;
  private JProgressBar                   progressBar;
  private JButton                        okButton;
  private JLabel                         lblPath;
  private JLabel                         lblOriginalTitle;

  /**
   * Instantiates a new tv show chooser dialog.
   *
   * @param tvShow
   *          the tv show
   * @param queueIndex
   *          the actual index in the queue
   * @param queueSize
   *          the queue size
   */
  public TvShowChooserDialog(TvShow tvShow, int queueIndex, int queueSize) {
    super(BUNDLE.getString("tvshowchooser.search") + (queueSize > 1 ? " " + (queueIndex + 1) + "/" + queueSize : ""), "tvShowChooser"); //$NON-NLS-1$

    // copy the values
    TvShowScraperMetadataConfig settings = TvShowModuleManager.SETTINGS.getScraperMetadataConfig();
    mediaScraper = tvShowList.getDefaultMediaScraper();
    artworkScrapers = tvShowList.getAvailableArtworkScrapers();

    scraperMetadataConfig.setTitle(settings.isTitle());
    scraperMetadataConfig.setPlot(settings.isPlot());
    scraperMetadataConfig.setRating(settings.isRating());
    scraperMetadataConfig.setRuntime(settings.isRuntime());
    scraperMetadataConfig.setYear(settings.isYear());
    scraperMetadataConfig.setAired(settings.isAired());
    scraperMetadataConfig.setStatus(settings.isStatus());
    scraperMetadataConfig.setCertification(settings.isCertification());
    scraperMetadataConfig.setCast(settings.isCast());
    scraperMetadataConfig.setGenres(settings.isGenres());
    scraperMetadataConfig.setArtwork(settings.isArtwork());

    // tableSearchResults format for the search result
    searchResultEventList = new SortedList<>(
        new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(TvShowChooserModel.class)),
        new SearchResultScoreComparator());

    DefaultEventTableModel<TvShowChooserModel> searchResultTableModel = new TmmTableModel<>(searchResultEventList, new SearchResultTableFormat());

    {
      final JPanel panelPath = new JPanel();
      panelPath.setLayout(new MigLayout("", "[grow]", "[]"));
      {
        lblPath = new JLabel("");
        TmmFontHelper.changeFont(lblPath, 1.16667, Font.BOLD);
        panelPath.add(lblPath, "cell 0 0, growx, wmin 0");
      }

      setTopIformationPanel(panelPath);
    }

    /* UI components */
    JPanel contentPanel = new JPanel();
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new MigLayout("", "[800lp:n,grow]", "[][shrink 0][250lp:300lp,grow][shrink 0][][]"));
    {
      JPanel panelSearchField = new JPanel();
      contentPanel.add(panelSearchField, "cell 0 0,grow");
      panelSearchField.setLayout(new MigLayout("", "[][][grow][]", "[23px][]"));
      {
        JLabel lblScraper = new TmmLabel(BUNDLE.getString("scraper")); //$NON-NLS-1$
        panelSearchField.add(lblScraper, "cell 0 0,alignx right");
      }
      {
        cbScraper = new MediaScraperComboBox(tvShowList.getAvailableMediaScrapers());
        MediaScraper defaultScraper = tvShowList.getDefaultMediaScraper();
        cbScraper.setSelectedItem(defaultScraper);
        cbScraper.setAction(new ChangeScraperAction());
        panelSearchField.add(cbScraper, "cell 1 0,growx");
      }
      {
        // also attach the actionlistener to the textfield to trigger the search on enter in the textfield
        ActionListener searchAction = arg0 -> searchTvShow(textFieldSearchString.getText(), null);

        textFieldSearchString = new JTextField();
        textFieldSearchString.addActionListener(searchAction);
        panelSearchField.add(textFieldSearchString, "cell 2 0,growx");
        textFieldSearchString.setColumns(10);

        JButton btnSearch = new JButton(BUNDLE.getString("Button.search")); //$NON-NLS-1$
        btnSearch.setIcon(IconManager.SEARCH_INV);
        panelSearchField.add(btnSearch, "cell 3 0");
        btnSearch.addActionListener(searchAction);
        getRootPane().setDefaultButton(btnSearch);
      }
      {
        JLabel lblLanguage = new TmmLabel("Language");
        panelSearchField.add(lblLanguage, "cell 0 1,alignx right");
      }
      {
        cbLanguage = new JComboBox<>();
        cbLanguage.setModel(new DefaultComboBoxModel<>(MediaLanguages.values()));
        cbLanguage.setSelectedItem(TvShowModuleManager.SETTINGS.getScraperLanguage());
        cbLanguage.addActionListener(e -> searchTvShow(textFieldSearchString.getText(), null));
        panelSearchField.add(cbLanguage, "cell 1 1,growx");
      }
    }
    {
      JSeparator separator = new JSeparator();
      contentPanel.add(separator, "cell 0 1,growx");
    }
    {
      JSplitPane splitPane = new TmmSplitPane();
      splitPane.setResizeWeight(0.5);
      contentPanel.add(splitPane, "cell 0 2,grow");
      {
        JPanel panelSearchResults = new JPanel();
        splitPane.setLeftComponent(panelSearchResults);
        panelSearchResults.setLayout(new MigLayout("", "[200lp:300lp,grow]", "[150lp:300lp,grow]"));
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
        panelSearchDetail.setLayout(new MigLayout("", "[150lp:n,grow][300lp:500lp,grow]", "[][][150lp:200lp,grow]"));
        {
          lblTtitle = new JLabel("");
          TmmFontHelper.changeFont(lblTtitle, 1.166, Font.BOLD);
          panelSearchDetail.add(lblTtitle, "cell 1 0,wmin 0");
        }
        {
          lblTvShowPoster = new ImageLabel(false);
          panelSearchDetail.add(lblTvShowPoster, "cell 0 0 1 3,grow");
        }
        {
          lblOriginalTitle = new JLabel("");
          panelSearchDetail.add(lblOriginalTitle, "cell 1 1,wmin 0");
        }
        {
          JScrollPane scrollPane = new JScrollPane();
          scrollPane.setBorder(null);
          panelSearchDetail.add(scrollPane, "cell 1 2,grow");

          taOverview = new ReadOnlyTextArea();
          scrollPane.setViewportView(taOverview);
        }
      }
    }
    {
      JSeparator separator = new JSeparator();
      contentPanel.add(separator, "cell 0 3,growx");
    }
    {
      JLabel lblScrapeFollowingItems = new TmmLabel(BUNDLE.getString("chooser.scrape")); //$NON-NLS-1$
      contentPanel.add(lblScrapeFollowingItems, "cell 0 4,growx,aligny top");
    }
    {
      JPanel panelScraperMetadataSetting = new TvShowScraperMetadataPanel(scraperMetadataConfig);
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
        if (queueSize > 1) {
          JButton abortButton = new JButton(BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
          abortButton.setActionCommand("Abort");
          abortButton.addActionListener(this);
          abortButton.setIcon(IconManager.STOP_INV);
          addButton(abortButton);

          if (queueIndex > 0) {
            JButton backButton = new JButton(BUNDLE.getString("Button.back")); //$NON-NLS-1$
            backButton.setIcon(IconManager.BACK_INV);
            backButton.setActionCommand("Back");
            backButton.addActionListener(this);
            addButton(backButton);
          }
        }
        JButton cancelButton = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
        cancelButton.setActionCommand("Cancel");
        cancelButton.setIcon(IconManager.CANCEL_INV);
        cancelButton.addActionListener(this);
        addButton(cancelButton);

        okButton = new JButton(BUNDLE.getString("Button.ok")); //$NON-NLS-1$
        okButton.setActionCommand("OK");
        okButton.setIcon(IconManager.APPLY_INV);
        okButton.addActionListener(this);
        addDefaultButton(okButton);
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
        int row = tableSearchResults.convertRowIndexToModel(tableSearchResults.getSelectedRow());
        if (row > -1) {
          TvShowChooserModel model = searchResultEventList.get(row);
          lblOriginalTitle.setText(model.getOriginalTitle());
          if (!model.getPosterUrl().equals(lblTvShowPoster.getImageUrl())) {
            lblTvShowPoster.setImageUrl(model.getPosterUrl());
          }
          taOverview.setText(model.getOverview());
        }
      }
    };

    tableSearchResults.getSelectionModel().addListSelectionListener(e -> {
      if (e.getValueIsAdjusting()) {
        return;
      }

      int index = tableSearchResults.convertRowIndexToModel(tableSearchResults.getSelectedRow());
      if (selectedResult != null) {
        selectedResult.removePropertyChangeListener(listener);
      }
      if (index > -1 && index < searchResultEventList.size()) {
        TvShowChooserModel model = searchResultEventList.get(index);
        lblTvShowPoster.setImageUrl(model.getPosterUrl());
        lblTtitle.setText(model.getCombinedName());
        lblOriginalTitle.setText(model.getOriginalTitle());
        taOverview.setText(model.getOverview());

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
          TvShowChooserModel model = searchResultEventList.get(selectedRow);
          if (model != TvShowChooserModel.emptyResult && !model.isScraped()) {
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
      tvShowToScrape = tvShow;
      progressBar.setVisible(false);

      lblPath.setText(tvShowToScrape.getPathNIO().toString());
      textFieldSearchString.setText(tvShowToScrape.getTitle());
      searchTvShow(textFieldSearchString.getText(), tvShowToScrape);
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if ("OK".equals(e.getActionCommand())) {
      int row = tableSearchResults.getSelectedRow();
      if (row >= 0) {
        TvShowChooserModel model = searchResultEventList.get(row);
        if (model != TvShowChooserModel.emptyResult) {
          // when scraping was not successful, abort saving
          if (!model.isScraped()) {
            MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "TvShowChooser", "message.scrape.threadcrashed"));
            return;
          }

          MediaMetadata md = model.getMetadata();

          // did the user want to choose the images?
          if (!TvShowModuleManager.SETTINGS.isScrapeBestImage()) {
            md.clearMediaArt();
          }

          // set scraped metadata
          tvShowToScrape.setMetadata(md, scraperMetadataConfig);

          // get the episode list for display?
          if (TvShowModuleManager.SETTINGS.isDisplayMissingEpisodes()) {
            tvShowToScrape.setDummyEpisodes(model.getEpisodesForDisplay());
            tvShowToScrape.saveToDb();
          }

          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

          // get images?
          if (scraperMetadataConfig.isArtwork()) {
            // let the user choose the images
            if (!TvShowModuleManager.SETTINGS.isScrapeBestImage()) {
              chooseArtwork(MediaFileType.POSTER);
              chooseArtwork(MediaFileType.FANART);
              chooseArtwork(MediaFileType.BANNER);
              chooseArtwork(MediaFileType.LOGO);
              chooseArtwork(MediaFileType.CLEARLOGO);
              chooseArtwork(MediaFileType.CLEARART);
              chooseArtwork(MediaFileType.THUMB);
            }
            else {
              // get artwork asynchronous
              model.startArtworkScrapeTask(tvShowToScrape, scraperMetadataConfig);
            }
          }

          // scrape episodes
          if (scraperMetadataConfig.isEpisodes()) {
            List<TvShowEpisode> episodesToScrape = tvShowToScrape.getEpisodesToScrape();
            // scrape episodes in a task
            if (!episodesToScrape.isEmpty()) {
              TvShowEpisodeScrapeTask task = new TvShowEpisodeScrapeTask(episodesToScrape, mediaScraper, scraperMetadataConfig);
              task.setLanguage(model.getLanguage());
              TmmTaskManager.getInstance().addUnnamedTask(task);
            }
          }

          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

          if (TvShowModuleManager.SETTINGS.getSyncTrakt()) {
            TmmTask task = new SyncTraktTvTask(null, Collections.singletonList(tvShowToScrape));
            TmmTaskManager.getInstance().addUnnamedTask(task);
          }

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
    List<String> extrafanarts = null;

    switch (mediaFileType) {
      case POSTER:
        if (TvShowModuleManager.SETTINGS.getPosterFilenames().isEmpty()) {
          return;
        }
        imageType = ImageType.POSTER;
        break;

      case FANART:
        if (TvShowModuleManager.SETTINGS.getFanartFilenames().isEmpty()) {
          return;
        }
        imageType = ImageType.FANART;
        extrafanarts = new ArrayList<>();
        break;

      case BANNER:
        if (TvShowModuleManager.SETTINGS.getBannerFilenames().isEmpty()) {
          return;
        }
        imageType = ImageType.BANNER;
        break;

      case LOGO:
        if (TvShowModuleManager.SETTINGS.getLogoFilenames().isEmpty()) {
          return;
        }
        imageType = ImageType.LOGO;
        break;

      case CLEARLOGO:
        if (TvShowModuleManager.SETTINGS.getClearlogoFilenames().isEmpty()) {
          return;
        }
        imageType = ImageType.CLEARLOGO;
        break;

      case CLEARART:
        if (TvShowModuleManager.SETTINGS.getClearartFilenames().isEmpty()) {
          return;
        }
        imageType = ImageType.CLEARART;
        break;

      case THUMB:
        if (TvShowModuleManager.SETTINGS.getThumbFilenames().isEmpty()) {
          return;
        }
        imageType = ImageType.THUMB;
        break;

      default:
        return;
    }

    String imageUrl = ImageChooserDialog.chooseImage(this, tvShowToScrape.getIds(), imageType, artworkScrapers, null, extrafanarts,
        MediaType.TV_SHOW);

    tvShowToScrape.setArtworkUrl(imageUrl, mediaFileType);
    if (StringUtils.isNotBlank(imageUrl)) {
      tvShowToScrape.downloadArtwork(mediaFileType);
    }

    // set extrafanarts
    if (mediaFileType == MediaFileType.FANART) {
      tvShowToScrape.setExtraFanartUrls(extrafanarts);
      if (extrafanarts.size() > 0) {
        tvShowToScrape.downloadArtwork(MediaFileType.EXTRAFANART);
      }
    }
  }

  public boolean isContinueQueue() {
    return continueQueue;
  }

  public boolean isNavigateBack() {
    return navigateBack;
  }

  private void searchTvShow(String searchTerm, TvShow show) {
    if (activeSearchTask != null && !activeSearchTask.isDone()) {
      activeSearchTask.cancel();
    }
    activeSearchTask = new SearchTask(searchTerm, show);
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

  private class SearchTask extends SwingWorker<Void, Void> {
    private String                  searchTerm;
    private TvShow                  show;
    private MediaLanguages          language;

    private List<MediaSearchResult> searchResult;
    boolean                         cancel = false;

    private SearchTask(String searchTerm, TvShow show) {
      this.searchTerm = searchTerm;
      this.show = show;
      this.language = (MediaLanguages) cbLanguage.getSelectedItem();
    }

    @Override
    public Void doInBackground() {
      startProgressBar(BUNDLE.getString("chooser.searchingfor") + " " + searchTerm); //$NON-NLS-1$
      searchResult = tvShowList.searchTvShow(searchTerm, show, mediaScraper, language);
      return null;
    }

    public void cancel() {
      cancel = true;
    }

    @Override
    public void done() {
      if (!cancel) {
        searchResultEventList.clear();
        if (searchResult.size() == 0) {
          // display empty result
          searchResultEventList.add(TvShowChooserModel.emptyResult);
        }
        else {
          MediaScraper mpFromResult = null;
          for (MediaSearchResult result : searchResult) {
            if (mpFromResult == null) {
              mpFromResult = tvShowList.getMediaScraperById(result.getProviderId());
            }
            searchResultEventList.add(new TvShowChooserModel(mpFromResult, artworkScrapers, result, language));
          }
        }

        if (!searchResultEventList.isEmpty()) {
          tableSearchResults.setRowSelectionInterval(0, 0); // select first row
        }
      }
      stopProgressBar();
    }
  }

  private class ScrapeTask extends SwingWorker<Void, Void> {
    private TvShowChooserModel model;

    private ScrapeTask(TvShowChooserModel model) {
      this.model = model;
    }

    @Override
    public Void doInBackground() {
      startProgressBar(BUNDLE.getString("chooser.scrapeing") + " " + model.getTitle()); //$NON-NLS-1$

      // disable ok button as long as its scraping
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
   * Shows the dialog and returns whether the work on the queue should be continued.
   * 
   * @return true, if successful
   */
  public boolean showDialog() {
    setVisible(true);
    return continueQueue;
  }

  private class ChangeScraperAction extends AbstractAction {
    private static final long serialVersionUID = -3537728352474538431L;

    ChangeScraperAction() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      mediaScraper = (MediaScraper) cbScraper.getSelectedItem();
      searchTvShow(textFieldSearchString.getText(), tvShowToScrape);
    }
  }

  /**
   * inner class for representing the result tableSearchResults
   */
  private static class SearchResultTableFormat extends TmmTableFormat<TvShowChooserModel> {
    private SearchResultTableFormat() {
      Comparator<TvShowChooserModel> searchResultComparator = new TvShowChooserDialog.SearchResultTitleComparator();
      Comparator<String> stringComparator = new StringComparator();

      FontMetrics fontMetrics = getFontMetrics();

      /*
       * title
       */
      Column col = new Column(BUNDLE.getString("chooser.searchresult"), "title", result -> result, TvShowChooserModel.class);
      col.setColumnComparator(searchResultComparator);
      col.setCellRenderer(new SearchResultRenderer());
      addColumn(col);

      /*
       * year
       */
      col = new Column(BUNDLE.getString("metatag.year"), "year", TvShowChooserModel::getYear, String.class);
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
  private static class SearchResultScoreComparator implements Comparator<TvShowChooserModel> {
    @Override
    public int compare(TvShowChooserModel o1, TvShowChooserModel o2) {
      return Float.compare(o2.getScore(), o1.getScore());
    }
  }

  /**
   * inner class for sorting the search results by name
   */
  private static class SearchResultTitleComparator implements Comparator<TvShowChooserModel> {
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
    public int compare(TvShowChooserModel o1, TvShowChooserModel o2) {
      if (stringCollator != null) {
        String titleTvShow1 = Normalizer.normalize(o1.getTitle().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        String titleTvShow2 = Normalizer.normalize(o2.getTitle().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return stringCollator.compare(titleTvShow1, titleTvShow2);
      }
      return o1.getTitle().toLowerCase(Locale.ROOT).compareTo(o2.getTitle().toLowerCase(Locale.ROOT));
    }
  }

  /**
   * inner class to render the search result
   */
  public static class SearchResultRenderer extends BorderTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      if (value instanceof TvShowChooserModel) {
        TvShowChooserModel result = (TvShowChooserModel) value;
        return super.getTableCellRendererComponent(table, result.getTitle(), isSelected, hasFocus, row, column);
      }
      return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
  }
}
