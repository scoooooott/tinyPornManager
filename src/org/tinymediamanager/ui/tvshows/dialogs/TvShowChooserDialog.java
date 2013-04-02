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
package org.tinymediamanager.ui.tvshows.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieScrapers;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowScrapers;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.ImageLabel;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.tvshows.TvShowChooserModel;
import org.tinymediamanager.ui.tvshows.TvShowScraperMetadataPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowChooserDialog.
 * 
 * @author Manuel Laggner
 */
public class TvShowChooserDialog extends JDialog implements ActionListener {

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID      = 2371518113606870230L;

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE                = ResourceBundle.getBundle("messages", new UTF8Control());                  //$NON-NLS-1$

  /** The static LOGGER. */
  private static final Logger         LOGGER                = Logger.getLogger(TvShowChooserDialog.class);

  /** The content panel. */
  private final JPanel                contentPanel          = new JPanel();

  /** The tv show list. */
  private TvShowList                  tvShowList            = TvShowList.getInstance();

  /** The tv show to scrape. */
  private TvShow                      tvShowToScrape;

  /** The text field search string. */
  private JTextField                  textFieldSearchString;

  /** The cb scraper. */
  private JComboBox                   cbScraper;

  /** The table. */
  private JTable                      table;

  /** The lbl tv show name. */
  private JTextArea                   lblTvShowName;

  /** The tp tv show overview. */
  private JTextPane                   tpTvShowOverview;

  /** The lbl tv show poster. */
  private ImageLabel                  lblTvShowPoster;

  /** The lbl progress action. */
  private JLabel                      lblProgressAction;

  /** The progress bar. */
  private JProgressBar                progressBar;

  /** The tv shows found. */
  private List<TvShowChooserModel>    tvShowsFound          = ObservableCollections.observableList(new ArrayList<TvShowChooserModel>());

  /** The scraper metadata config. */
  private TvShowScraperMetadataConfig scraperMetadataConfig = new TvShowScraperMetadataConfig();

  /** The metadata provider. */
  private IMediaMetadataProvider      metadataProvider;

  /** The artwork providers. */
  private List<IMediaArtworkProvider> artworkProviders;

  /** The trailer providers. */
  private List<IMediaTrailerProvider> trailerProviders;

  /** The continue queue. */
  private boolean                     continueQueue         = true;

  /**
   * Instantiates a new tv show chooser dialog.
   * 
   * @param tvShow
   *          the tv show
   * @param inQueue
   *          the in queue
   */
  public TvShowChooserDialog(TvShow tvShow, boolean inQueue) {
    setTitle(BUNDLE.getString("moviechooser.search")); //$NON-NLS-1$
    setName("tvShowChooser");
    setBounds(5, 5, 1111, 643);
    TmmWindowSaver.loadSettings(this);
    setIconImage(Globals.logo);
    setModal(true);

    // copy the values
    TvShowScraperMetadataConfig settings = Globals.settings.getTvShowScraperMetadataConfig();
    metadataProvider = tvShowList.getMetadataProvider();
    artworkProviders = tvShowList.getArtworkProviders();
    // trailerProviders = tvShowList.getTrailerProviders();

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
        JLabel lblScraper = new JLabel(BUNDLE.getString("scraper")); //$NON-NLS-1$
        panelSearchField.add(lblScraper, "2, 1, right, default");
      }
      {
        cbScraper = new JComboBox(TvShowScrapers.values());
        TvShowScrapers defaultScraper = Globals.settings.getTvShowScraper();
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
        JButton btnSearch = new JButton(BUNDLE.getString("Button.search")); //$NON-NLS-1$
        panelSearchField.add(btnSearch, "7, 3");
        btnSearch.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
            searchTvShow(textFieldSearchString.getText());
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
          lblTvShowName = new JTextArea("");
          lblTvShowName.setLineWrap(true);
          lblTvShowName.setOpaque(false);
          lblTvShowName.setWrapStyleWord(true);
          lblTvShowName.setFont(new Font("Dialog", Font.BOLD, 14));
          panelSearchDetail.add(lblTvShowName, "2, 1, 3, 1, fill, top");
        }
        {
          lblTvShowPoster = new ImageLabel();// new JLabel("");
          panelSearchDetail.add(lblTvShowPoster, "2, 4, fill, fill");
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
            tpTvShowOverview = new JTextPane();
            scrollPane.setViewportView(tpTvShowOverview);
          }
        }
      }
    }
    {
      JLabel lblScrapeFollowingItems = new JLabel(BUNDLE.getString("moviechooser.scrape")); //$NON-NLS-1$
      contentPanel.add(lblScrapeFollowingItems, "1, 6");
    }
    {
      JPanel panelScraperMetadataSetting = new TvShowScraperMetadataPanel(scraperMetadataConfig);
      contentPanel.add(panelScraperMetadataSetting, "1, 7, fill, fill");
    }

    {
      JPanel bottomPane = new JPanel();
      contentPanel.add(bottomPane, "1, 9");
      {
        bottomPane.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("max(82dlu;default)"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
            FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"), }));
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
          JButton okButton = new JButton(BUNDLE.getString("Button.ok")); //$NON-NLS-1$
          buttonPane.add(okButton, "1, 1, fill, top");
          okButton.setActionCommand("OK");
          okButton.addActionListener(this);

          JButton cancelButton = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
          buttonPane.add(cancelButton, "3, 1, fill, top");
          cancelButton.setActionCommand("Cancel");
          cancelButton.addActionListener(this);

          if (inQueue) {
            JButton abortButton = new JButton(BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
            buttonPane.add(abortButton, "5, 1, fill, top");
            abortButton.setActionCommand("Abort");
            abortButton.addActionListener(this);
          }
        }
      }
    }

    {
      tvShowToScrape = tvShow;
      progressBar.setVisible(false);
      initDataBindings();

      textFieldSearchString.setText(tvShowToScrape.getTitle());
      searchTvShow(textFieldSearchString.getText());
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
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
        TvShowChooserModel model = tvShowsFound.get(row);
        if (model != TvShowChooserModel.emptyResult) {
          MediaMetadata md = model.getMetadata();

          // did the user want to choose the images?
          if (!Globals.settings.getMovieSettings().isScrapeBestImage()) {
            md.clearMediaArt();
          }

          // set scraped metadata
          tvShowToScrape.setMetadata(md);

          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

          // get images?
          if (scraperMetadataConfig.isArtwork()) {
            // TODO implement manual image choosing
            // // let the user choose the images
            // if (!Globals.settings.isScrapeBestImage()) {
            // // poster
            // {
            // ImageLabel lblImage = new ImageLabel();
            // MovieImageChooserDialog dialog = new MovieImageChooserDialog(tvShowToScrape.getImdbId(), tvShowToScrape.getTmdbId(),
            // ImageType.POSTER, lblImage, null, null);
            // dialog.setVisible(true);
            // tvShowToScrape.setPosterUrl(lblImage.getImageUrl());
            // tvShowToScrape.writeImages(true, false);
            // }
            //
            // // fanart
            // {
            // ImageLabel lblImage = new ImageLabel();
            // List<String> extrathumbs = new ArrayList<String>();
            // List<String> extrafanarts = new ArrayList<String>();
            // MovieImageChooserDialog dialog = new MovieImageChooserDialog(tvShowToScrape.getImdbId(), tvShowToScrape.getTmdbId(),
            // ImageType.FANART, lblImage, extrathumbs, extrafanarts);
            // dialog.setVisible(true);
            // tvShowToScrape.setFanartUrl(lblImage.getImageUrl());
            // tvShowToScrape.writeImages(false, true);
            //
            // }
            // }
            // else {
            // get artwork directly from provider
            List<MediaArtwork> artwork = model.getArtwork();
            tvShowToScrape.setArtwork(artwork);
            // }
          }

          // TODO do we need trailers?
          // // get trailers?
          // if (scraperMetadataConfig.isTrailer()) {
          // List<MediaTrailer> trailers = model.getTrailers();
          // tvShowToScrape.setTrailers(trailers);
          // }

          // rewrite the complete NFO
          tvShowToScrape.writeNFO();

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

  /**
   * Search tv show.
   * 
   * @param searchTerm
   *          the search term
   */
  private void searchTvShow(String searchTerm) {
    SearchTask task = new SearchTask(searchTerm);
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
   * 
   * @author Manuel Laggner
   */
  private class SearchTask extends SwingWorker<Void, Void> {

    /** The search term. */
    private String searchTerm;

    /**
     * Instantiates a new search task.
     * 
     * @param searchTerm
     *          the search term
     */
    public SearchTask(String searchTerm) {
      this.searchTerm = searchTerm;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    public Void doInBackground() {
      startProgressBar(BUNDLE.getString("moviechooser.searchingfor") + " " + searchTerm); //$NON-NLS-1$
      List<MediaSearchResult> searchResult = tvShowList.searchTvShow(searchTerm, metadataProvider);
      tvShowsFound.clear();
      if (searchResult.size() == 0) {
        // display empty result
        tvShowsFound.add(TvShowChooserModel.emptyResult);
      }
      else {
        for (MediaSearchResult result : searchResult) {
          tvShowsFound.add(new TvShowChooserModel(metadataProvider, artworkProviders, trailerProviders, result));
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
  }

  /**
   * The Class ScrapeTask.
   * 
   * @author Manuel Laggner
   */
  private class ScrapeTask extends SwingWorker<Void, Void> {

    /** The model. */
    private TvShowChooserModel model;

    /**
     * Instantiates a new scrape task.
     * 
     * @param model
     *          the model
     */
    public ScrapeTask(TvShowChooserModel model) {
      this.model = model;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    public Void doInBackground() {
      startProgressBar(BUNDLE.getString("moviechooser.scrapeing") + " " + model.getName()); //$NON-NLS-1$
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
    JTableBinding<TvShowChooserModel, List<TvShowChooserModel>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ,
        tvShowsFound, table);
    //
    BeanProperty<TvShowChooserModel, String> movieChooserModelBeanProperty = BeanProperty.create("combinedName");
    jTableBinding.addColumnBinding(movieChooserModelBeanProperty).setColumnName(BUNDLE.getString("moviechooser.searchresult")).setEditable(false); //$NON-NLS-1$
    //
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_1 = BeanProperty.create("selectedElement.overview");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextPane, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_1,
        tpTvShowOverview, jTextPaneBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_2 = BeanProperty.create("selectedElement.posterUrl");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imageUrl");
    AutoBinding<JTable, String, ImageLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_2,
        lblTvShowPoster, imageLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_3 = BeanProperty.create("selectedElement.combinedName");
    BeanProperty<JTextArea, String> jTextAreaBeanProperty_1 = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextArea, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_3,
        lblTvShowName, jTextAreaBeanProperty_1);
    autoBinding_3.bind();
  }

  /**
   * Shows the dialog and returns whether the work on the queue should be continued.
   * 
   * @return true, if successful
   */
  public boolean showDialog() {
    pack();
    setVisible(true);
    return continueQueue;
  }

  /**
   * The Class ChangeScraperAction.
   * 
   * @author Manuel Laggner
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
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      MovieScrapers selectedScraper = (MovieScrapers) cbScraper.getSelectedItem();
      metadataProvider = MovieList.getInstance().getMetadataProvider(selectedScraper);
      searchTvShow(textFieldSearchString.getText());
    }
  }
}
