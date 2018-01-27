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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.MediaScraperComboBox;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog.ImageType;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.tvshows.TvShowChooserModel;
import org.tinymediamanager.ui.tvshows.panels.TvShowScraperMetadataPanel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowChooserDialog.
 *
 * @author Manuel Laggner
 */
public class TvShowChooserDialog extends TmmDialog implements ActionListener {
  private static final long           serialVersionUID      = 2371518113606870230L;
  private static final Logger         LOGGER                = LoggerFactory.getLogger(TvShowChooserDialog.class);

  private TvShowList                  tvShowList            = TvShowList.getInstance();
  private TvShow                      tvShowToScrape;
  private List<TvShowChooserModel>    tvShowsFound          = ObservableCollections.observableList(new ArrayList<TvShowChooserModel>());
  private TvShowScraperMetadataConfig scraperMetadataConfig = new TvShowScraperMetadataConfig();
  private MediaScraper                mediaScraper;
  private List<MediaScraper>          artworkScrapers;
  private boolean                     continueQueue         = true;
  private boolean                     navigateBack          = false;

  private JTextField                  textFieldSearchString;
  private MediaScraperComboBox        cbScraper;
  private JComboBox<MediaLanguages>   cbLanguage;
  private JTable                      table;
  private JLabel                      lblTvShowName;
  private JTextArea                   tpTvShowOverview;
  private ImageLabel                  lblTvShowPoster;
  private JLabel                      lblProgressAction;
  private JProgressBar                progressBar;
  private JButton                     okButton;
  private JLabel                      lblPath;

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
    // trailerProviders = tvShowList.getTrailerProviders();

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
      JSplitPane splitPane = new JSplitPane();
      splitPane.setResizeWeight(0.5);
      splitPane.setContinuousLayout(true);
      contentPanel.add(splitPane, "cell 0 2,grow");
      {
        JPanel panelSearchResults = new JPanel();
        splitPane.setLeftComponent(panelSearchResults);
        panelSearchResults.setLayout(new MigLayout("", "[200lp:300lp,grow]", "[150lp:300lp,grow]"));
        {
          {
            JScrollPane scrollPane = new JScrollPane();
            panelSearchResults.add(scrollPane, "cell 0 0,grow");
            table = new JTable();
            scrollPane.setViewportView(table);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setBorder(new LineBorder(new Color(0, 0, 0)));
            ListSelectionModel rowSM = table.getSelectionModel();
            rowSM.addListSelectionListener(e -> {
              // Ignore extra messages.
              if (e.getValueIsAdjusting())
                return;

              ListSelectionModel lsm = (ListSelectionModel) e.getSource();
              if (!lsm.isSelectionEmpty()) {
                int selectedRow = lsm.getMinSelectionIndex();
                selectedRow = table.convertRowIndexToModel(selectedRow);
                try {
                  TvShowChooserModel model = tvShowsFound.get(selectedRow);
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
            table.addMouseListener(new MouseAdapter() {
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
        panelSearchDetail.setLayout(new MigLayout("", "[150lp:n,grow][300lp:500lp,grow]", "[][150lp:200lp,grow]"));
        {
          lblTvShowName = new JLabel("");
          TmmFontHelper.changeFont(lblTvShowName, 1.166, Font.BOLD);
          panelSearchDetail.add(lblTvShowName, "cell 0 0 2 1,growx, aligny top, wmin 0");
        }
        {
          lblTvShowPoster = new ImageLabel(false);
          panelSearchDetail.add(lblTvShowPoster, "cell 0 1,grow");
        }
        {
          JScrollPane scrollPane = new JScrollPane();
          scrollPane.setBorder(null);
          panelSearchDetail.add(scrollPane, "cell 1 1,grow");

          tpTvShowOverview = new ReadOnlyTextArea();
          scrollPane.setViewportView(tpTvShowOverview);
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

    {
      tvShowToScrape = tvShow;
      progressBar.setVisible(false);
      initDataBindings();

      // set column name - windowbuilder pro crashes otherwise
      table.getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("chooser.searchresult")); //$NON-NLS-1$
      lblPath.setText(tvShowToScrape.getPathNIO().toString());
      textFieldSearchString.setText(tvShowToScrape.getTitle());
      searchTvShow(textFieldSearchString.getText(), tvShowToScrape);
    }

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if ("OK".equals(e.getActionCommand())) {
      int row = table.getSelectedRow();
      if (row >= 0) {
        TvShowChooserModel model = tvShowsFound.get(row);
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
              // poster
              {
                ImageLabel lblImage = new ImageLabel();
                ImageChooserDialog dialog = new ImageChooserDialog(tvShowToScrape.getIds(), ImageType.POSTER, artworkScrapers, lblImage, null, null,
                    MediaType.TV_SHOW);
                dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
                dialog.setVisible(true);
                tvShowToScrape.setArtworkUrl(lblImage.getImageUrl(), MediaFileType.POSTER);
                tvShowToScrape.downloadArtwork(MediaFileType.POSTER);
              }

              // fanart
              {
                ImageLabel lblImage = new ImageLabel();
                List<String> extrathumbs = new ArrayList<>();
                List<String> extrafanarts = new ArrayList<>();
                ImageChooserDialog dialog = new ImageChooserDialog(tvShowToScrape.getIds(), ImageType.FANART, artworkScrapers, lblImage, extrathumbs,
                    extrafanarts, MediaType.TV_SHOW);
                dialog.setVisible(true);
                tvShowToScrape.setArtworkUrl(lblImage.getImageUrl(), MediaFileType.FANART);
                tvShowToScrape.downloadArtwork(MediaFileType.FANART);
              }

              // banner
              {
                ImageLabel lblImage = new ImageLabel();
                ImageChooserDialog dialog = new ImageChooserDialog(tvShowToScrape.getIds(), ImageType.BANNER, artworkScrapers, lblImage, null, null,
                    MediaType.TV_SHOW);
                dialog.setVisible(true);
                tvShowToScrape.setArtworkUrl(lblImage.getImageUrl(), MediaFileType.BANNER);
                tvShowToScrape.downloadArtwork(MediaFileType.BANNER);
              }
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
              TvShowEpisodeScrapeTask task = new TvShowEpisodeScrapeTask(episodesToScrape, mediaScraper);
              task.setLanguage(model.getLanguage());
              TmmTaskManager.getInstance().addUnnamedTask(task);
            }
          }

          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

          if (TvShowModuleManager.SETTINGS.getSyncTrakt()) {
            TmmTask task = new SyncTraktTvTask(null, Arrays.asList(tvShowToScrape));
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

  public boolean isContinueQueue() {
    return continueQueue;
  }

  public boolean isNavigateBack() {
    return navigateBack;
  }

  private void searchTvShow(String searchTerm, TvShow show) {
    SearchTask task = new SearchTask(searchTerm, show);
    task.execute();
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
    private String         searchTerm;
    private TvShow         show;
    private MediaLanguages language;

    public SearchTask(String searchTerm, TvShow show) {
      this.searchTerm = searchTerm;
      this.show = show;
      this.language = (MediaLanguages) cbLanguage.getSelectedItem();
    }

    @Override
    public Void doInBackground() {
      startProgressBar(BUNDLE.getString("chooser.searchingfor") + " " + searchTerm); //$NON-NLS-1$
      List<MediaSearchResult> searchResult = tvShowList.searchTvShow(searchTerm, show, mediaScraper, language);
      tvShowsFound.clear();
      if (searchResult.size() == 0) {
        // display empty result
        tvShowsFound.add(TvShowChooserModel.emptyResult);
      }
      else {
        MediaScraper mpFromResult = null;
        for (MediaSearchResult result : searchResult) {
          if (mpFromResult == null) {
            mpFromResult = tvShowList.getMediaScraperById(result.getProviderId());
          }
          tvShowsFound.add(new TvShowChooserModel(mpFromResult, artworkScrapers, result, language));
        }
      }

      if (tvShowsFound.size() == 1) { // only one result
        table.setRowSelectionInterval(0, 0); // select first row
      }

      return null;
    }

    @Override
    public void done() {
      stopProgressBar();
    }
  }

  private class ScrapeTask extends SwingWorker<Void, Void> {
    private TvShowChooserModel model;

    ScrapeTask(TvShowChooserModel model) {
      this.model = model;
    }

    @Override
    public Void doInBackground() {
      startProgressBar(BUNDLE.getString("chooser.scrapeing") + " " + model.getName()); //$NON-NLS-1$

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
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    JTableBinding<TvShowChooserModel, List<TvShowChooserModel>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ,
        tvShowsFound, table);
    //
    BeanProperty<TvShowChooserModel, String> tvShowChooserModelBeanProperty = BeanProperty.create("combinedName");
    jTableBinding.addColumnBinding(tvShowChooserModelBeanProperty).setEditable(false);
    //
    bindings.add(jTableBinding);
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_1 = BeanProperty.create("selectedElement.overview");
    BeanProperty<JTextArea, String> JTextAreaBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextArea, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_1,
        tpTvShowOverview, JTextAreaBeanProperty);
    bindings.add(autoBinding_1);
    autoBinding_1.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_2 = BeanProperty.create("selectedElement.posterUrl");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imageUrl");
    AutoBinding<JTable, String, ImageLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_2,
        lblTvShowPoster, imageLabelBeanProperty);
    bindings.add(autoBinding_2);
    autoBinding_2.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_3 = BeanProperty.create("selectedElement.combinedName");
    BeanProperty<JLabel, String> jLabelBeanProperty_1 = BeanProperty.create("text");
    AutoBinding<JTable, String, JLabel, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_3,
        lblTvShowName, jLabelBeanProperty_1);
    bindings.add(autoBinding_3);
    autoBinding_3.bind();
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
}
