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
package org.tinymediamanager.ui.moviesets.dialogs;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.ScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSetScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieSetSearchAndScrapeOptions;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.interfaces.IMovieSetMetadataProvider;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.ReadOnlyTextPane;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.TmmSplitPane;
import org.tinymediamanager.ui.components.combobox.ScraperMetadataConfigCheckComboBox;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.moviesets.MovieSetChooserModel;
import org.tinymediamanager.ui.moviesets.MovieSetChooserModel.MovieInSet;

import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieSetChooserPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieSetChooserDialog extends TmmDialog implements ActionListener {
  private static final long                                                 serialVersionUID = -1023959850452480592L;
  private static final Logger                                               LOGGER           = LoggerFactory.getLogger(MovieSetChooserDialog.class);

  private MovieSet                                                          movieSetToScrape;
  private List<MovieSetChooserModel>                                        movieSetsFound   = ObservableCollections
      .observableList(new ArrayList<>());
  private boolean                                                           continueQueue    = true;

  private JLabel                                                            lblProgressAction;
  private JProgressBar                                                      progressBar;
  private JTextField                                                        tfMovieSetName;
  private JTable                                                            tableMovieSets;
  private JLabel                                                            lblMovieSetName;
  private ImageLabel                                                        lblMovieSetPoster;
  private JTable                                                            tableMovies;
  private JCheckBox                                                         cbAssignMovies;
  private JButton                                                           btnOk;
  private JTextPane                                                         tpPlot;
  private ScraperMetadataConfigCheckComboBox<MovieSetScraperMetadataConfig> cbScraperConfig;

  /**
   * Instantiates a new movie set chooser panel.
   * 
   * @param movieSet
   *          the movie set
   */
  public MovieSetChooserDialog(MovieSet movieSet, boolean inQueue) {
    super(BUNDLE.getString("movieset.search"), "movieSetChooser");

    movieSetToScrape = movieSet;

    {
      JPanel panelHeader = new JPanel();
      panelHeader.setLayout(new MigLayout("", "[grow][]", "[]"));

      // also attach the actionlistener to the textfield to trigger the search on enter in the textfield
      Action searchAction = new SearchAction();

      tfMovieSetName = new JTextField();
      tfMovieSetName.addActionListener(searchAction);
      panelHeader.add(tfMovieSetName, "cell 0 0,growx");
      tfMovieSetName.setColumns(10);

      JButton btnSearch = new JButton(searchAction);
      panelHeader.add(btnSearch, "cell 1 0");

      setTopIformationPanel(panelHeader);
    }
    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new MigLayout("", "[950lp,grow]", "[500,grow][][][]"));

      JSplitPane splitPane = new TmmSplitPane();
      splitPane.setResizeWeight(0.5);
      panelContent.add(splitPane, "cell 0 0,grow");
      {
        JPanel panelResults = new JPanel();
        panelResults.setLayout(new MigLayout("", "[200lp:300lp,grow]", "[300lp,grow]"));
        JScrollPane panelSearchResults = new JScrollPane();
        panelResults.add(panelSearchResults, "cell 0 0,grow");
        splitPane.setLeftComponent(panelResults);
        {
          tableMovieSets = new TmmTable();
          panelSearchResults.setViewportView(tableMovieSets);
          tableMovieSets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
          ListSelectionModel rowSM = tableMovieSets.getSelectionModel();
          rowSM.addListSelectionListener(e -> {
            // Ignore extra messages.
            if (e.getValueIsAdjusting())
              return;

            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (!lsm.isSelectionEmpty()) {
              int selectedRow = lsm.getMinSelectionIndex();
              selectedRow = tableMovieSets.convertRowIndexToModel(selectedRow);
              try {
                MovieSetChooserModel model = movieSetsFound.get(selectedRow);
                if (model != MovieSetChooserModel.emptyResult && !model.isScraped()) {
                  ScrapeTask task = new ScrapeTask(model);
                  task.execute();

                }
              }
              catch (Exception ex) {
                LOGGER.warn(ex.getMessage());
              }
            }
          });
          tableMovieSets.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              if (e.getClickCount() >= 2 && !e.isConsumed() && e.getButton() == MouseEvent.BUTTON1) {
                actionPerformed(new ActionEvent(btnOk, ActionEvent.ACTION_PERFORMED, "Save"));
              }
            }
          });
        }
      }
      {
        JPanel panelSearchDetail = new JPanel();
        splitPane.setRightComponent(panelSearchDetail);
        panelSearchDetail.setLayout(new MigLayout("", "[150lp,grow 60][450lp,grow]", "[][250lp,grow][150lp][]"));
        {
          lblMovieSetName = new JLabel("");
          TmmFontHelper.changeFont(lblMovieSetName, 1.166, Font.BOLD);
          panelSearchDetail.add(lblMovieSetName, "cell 0 0 2 1,growx");
        }
        {
          lblMovieSetPoster = new ImageLabel();
          panelSearchDetail.add(lblMovieSetPoster, "cell 0 1,grow");
        }
        {
          JScrollPane scrollPane = new JScrollPane();
          panelSearchDetail.add(scrollPane, "cell 1 1,grow");

          tpPlot = new ReadOnlyTextPane();
          scrollPane.setViewportView(tpPlot);
        }
        {
          JScrollPane scrollPane = new JScrollPane();
          panelSearchDetail.add(scrollPane, "cell 0 2 2 1,grow");

          tableMovies = new TmmTable();
          scrollPane.setViewportView(tableMovies);
        }
        {
          cbAssignMovies = new JCheckBox(BUNDLE.getString("movieset.movie.assign"));
          cbAssignMovies.setSelected(true);
          panelSearchDetail.add(cbAssignMovies, "cell 0 3 2 1,growx,aligny top");
        }
      }
      {
        JSeparator separator = new JSeparator();
        panelContent.add(separator, "cell 0 1,growx");
      }
      {
        JLabel lblScrapeFollowingItems = new TmmLabel(BUNDLE.getString("chooser.scrape"));
        panelContent.add(lblScrapeFollowingItems, "cell 0 2");

        cbScraperConfig = new ScraperMetadataConfigCheckComboBox(MovieSetScraperMetadataConfig.values());
        panelContent.add(cbScraperConfig, "cell 0 3,growx, wmin 0");
      }
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
      if (inQueue) {
        JButton btnAbort = new JButton(BUNDLE.getString("Button.abortqueue"));
        btnAbort.setActionCommand("Abort");
        btnAbort.setToolTipText(BUNDLE.getString("Button.abortqueue"));
        btnAbort.setIcon(IconManager.STOP_INV);
        btnAbort.addActionListener(this);
        addButton(btnAbort);
      }

      JButton btnCancel = new JButton(BUNDLE.getString("Button.cancel"));
      btnCancel.setActionCommand("Cancel");
      btnCancel.setToolTipText(BUNDLE.getString("Button.cancel"));
      btnCancel.setIcon(IconManager.CANCEL_INV);
      btnCancel.addActionListener(this);
      addButton(btnCancel);

      btnOk = new JButton(BUNDLE.getString("Button.ok"));
      btnOk.setActionCommand("Save");
      btnOk.setToolTipText(BUNDLE.getString("Button.ok"));
      btnOk.setIcon(IconManager.APPLY_INV);
      btnOk.addActionListener(this);
      addDefaultButton(btnOk);
    }

    bindingGroup = initDataBindings();

    // adjust table columns
    tableMovies.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tableMovies.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

    cbScraperConfig.setSelectedItems(MovieSetScraperMetadataConfig.values());

    tableMovieSets.getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("chooser.searchresult"));
    tfMovieSetName.setText(movieSet.getTitle());
    searchMovie();
  }

  private void searchMovie() {
    SearchTask task = new SearchTask(tfMovieSetName.getText());
    task.execute();
  }

  private class SearchTask extends SwingWorker<Void, Void> {
    private String searchTerm;

    public SearchTask(String searchTerm) {
      this.searchTerm = searchTerm;
    }

    @Override
    public Void doInBackground() {
      startProgressBar(BUNDLE.getString("chooser.searchingfor") + " " + searchTerm);
      try {
        List<MediaScraper> sets = MediaScraper.getMediaScrapers(ScraperType.MOVIE_SET);
        if (sets != null && !sets.isEmpty()) {
          MediaScraper first = sets.get(0); // just get first
          IMovieSetMetadataProvider mp = (IMovieSetMetadataProvider) first.getMediaProvider();

          MovieSetSearchAndScrapeOptions options = new MovieSetSearchAndScrapeOptions();
          options.setSearchQuery(searchTerm);
          options.setLanguage(MovieModuleManager.SETTINGS.getScraperLanguage());

          List<MediaSearchResult> movieSets = mp.search(options);
          movieSetsFound.clear();
          if (movieSets.isEmpty()) {
            movieSetsFound.add(MovieSetChooserModel.emptyResult);
          }
          else {
            for (MediaSearchResult collection : movieSets) {
              MovieSetChooserModel model = new MovieSetChooserModel(collection);
              movieSetsFound.add(model);
            }
          }
        }

        if (!movieSetsFound.isEmpty()) {
          tableMovieSets.setRowSelectionInterval(0, 0); // select first row
        }
      }
      catch (Exception e1) {
        LOGGER.warn("SearchTask", e1);
      }

      return null;
    }

    @Override
    public void done() {
      stopProgressBar();
    }
  }

  private class SearchAction extends AbstractAction {
    private static final long serialVersionUID = -6561883838396668177L;

    SearchAction() {
      putValue(NAME, BUNDLE.getString("Button.search"));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movieset.search"));
      putValue(SMALL_ICON, IconManager.SEARCH_INV);
      putValue(LARGE_ICON_KEY, IconManager.SEARCH_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      searchMovie();
    }
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    if ("Cancel".equals(arg0.getActionCommand())) {
      // cancel
      setVisible(false);
    }

    if ("Save".equals(arg0.getActionCommand())) {
      // save it
      int row = tableMovieSets.getSelectedRow();
      if (row >= 0) {
        MovieSetChooserModel model = movieSetsFound.get(row);
        if (model != MovieSetChooserModel.emptyResult) {
          // when scraping was not successful, abort saving
          if (!model.isScraped()) {
            MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, "MovieSetChooser", "message.scrape.threadcrashed"));
            return;
          }

          MediaMetadata md = model.getMetadata();

          // set scraped metadata
          List<MovieSetScraperMetadataConfig> scraperConfig = cbScraperConfig.getSelectedItems();
          movieSetToScrape.setMetadata(md, scraperConfig);

          // assign movies
          if (cbAssignMovies.isSelected()) {
            movieSetToScrape.removeAllMovies();
            for (int i = 0; i < model.getMovies().size(); i++) {
              MovieInSet movieInSet = model.getMovies().get(i);
              Movie movie = movieInSet.getMovie();
              if (movie == null) {
                continue;
              }

              // check if the found movie contains a matching set
              if (movie.getMovieSet() != null) {
                // unassign movie from set
                MovieSet mSet = movie.getMovieSet();
                mSet.removeMovie(movie, true);
              }

              movie.setMovieSet(movieSetToScrape);
              movie.writeNFO();
              movie.saveToDb();
              movieSetToScrape.addMovie(movie);
            }

            // and finally save assignments
            movieSetToScrape.saveToDb();
          }

          // get images?
          if (ScraperMetadataConfig.containsAnyArtwork(scraperConfig)) {
            // get artwork asynchronous
            model.startArtworkScrapeTask(movieSetToScrape, scraperConfig);
          }
        }
        setVisible(false);
      }
    }

    // Abort queue
    if ("Abort".equals(arg0.getActionCommand())) {
      continueQueue = false;
      setVisible(false);
    }
  }

  private class ScrapeTask extends SwingWorker<Void, Void> {
    private MovieSetChooserModel model;

    ScrapeTask(MovieSetChooserModel model) {
      this.model = model;
    }

    @Override
    public Void doInBackground() {
      startProgressBar(BUNDLE.getString("chooser.scrapeing") + " " + model.getName());

      // disable ok button as long as its scraping
      btnOk.setEnabled(false);
      model.scrapeMetadata();
      btnOk.setEnabled(true);

      return null;
    }

    @Override
    public void done() {
      stopProgressBar();
    }
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
    setVisible(true);
    return continueQueue;
  }

  protected BindingGroup initDataBindings() {
    JTableBinding<MovieSetChooserModel, List<MovieSetChooserModel>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ,
        movieSetsFound, tableMovieSets);
    //
    BeanProperty<MovieSetChooserModel, String> movieSetChooserModelBeanProperty = BeanProperty.create("name");
    jTableBinding.addColumnBinding(movieSetChooserModelBeanProperty).setEditable(false);
    //
    jTableBinding.bind();
    //
    BeanProperty<JTable, List<MovieInSet>> jTableBeanProperty = BeanProperty.create("selectedElement.movies");
    JTableBinding<MovieInSet, JTable, JTable> jTableBinding_1 = SwingBindings.createJTableBinding(UpdateStrategy.READ, tableMovieSets,
        jTableBeanProperty, tableMovies);
    //
    BeanProperty<MovieInSet, String> movieInSetBeanProperty = BeanProperty.create("name");
    jTableBinding_1.addColumnBinding(movieInSetBeanProperty).setColumnName("Movie").setEditable(false);
    //
    BeanProperty<MovieInSet, String> movieInSetBeanProperty_2 = BeanProperty.create("movie.title");
    jTableBinding_1.addColumnBinding(movieInSetBeanProperty_2).setColumnName("matched movie").setEditable(false);
    //
    jTableBinding_1.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_1 = BeanProperty.create("selectedElement.name");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, tableMovieSets, jTableBeanProperty_1,
        lblMovieSetName, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_2 = BeanProperty.create("selectedElement.posterUrl");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imageUrl");
    AutoBinding<JTable, String, ImageLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, tableMovieSets,
        jTableBeanProperty_2, lblMovieSetPoster, imageLabelBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_3 = BeanProperty.create("selectedElement.overview");
    BeanProperty<JTextPane, String> readOnlyTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextPane, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, tableMovieSets,
        jTableBeanProperty_3, tpPlot, readOnlyTextPaneBeanProperty);
    autoBinding_2.bind();
    //
    BindingGroup bindingGroup = new BindingGroup();
    //
    bindingGroup.addBinding(jTableBinding);
    bindingGroup.addBinding(jTableBinding_1);
    bindingGroup.addBinding(autoBinding);
    bindingGroup.addBinding(autoBinding_1);
    bindingGroup.addBinding(autoBinding_2);
    return bindingGroup;
  }
}
