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
package org.tinymediamanager.ui.moviesets.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;

import org.apache.commons.lang3.LocaleUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMovieSetMetadataProvider;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.ReadOnlyTextPane;
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
  private static final long          serialVersionUID = -1023959850452480592L;
  private static final Logger        LOGGER           = LoggerFactory.getLogger(MovieSetChooserDialog.class);

  private MovieSet                   movieSetToScrape;
  private List<MovieSetChooserModel> movieSetsFound   = ObservableCollections.observableList(new ArrayList<MovieSetChooserModel>());
  private boolean                    continueQueue    = true;

  private JLabel                     lblProgressAction;
  private JProgressBar               progressBar;
  private JTextField                 tfMovieSetName;
  private JTable                     tableMovieSets;
  private JTextArea                  lblMovieSetName;
  private ImageLabel                 lblMovieSetPoster;
  private JTable                     tableMovies;
  private JCheckBox                  cbAssignMovies;
  private JButton                    btnOk;
  private JTextPane                  tpPlot;

  /**
   * Instantiates a new movie set chooser panel.
   * 
   * @param movieSet
   *          the movie set
   */
  public MovieSetChooserDialog(MovieSet movieSet, boolean inQueue) {
    super(BUNDLE.getString("movieset.search"), "movieSetChooser"); //$NON-NLS-1$

    movieSetToScrape = movieSet;

    {
      JPanel panelHeader = new JPanel();
      panelHeader.setLayout(new MigLayout("", "[grow][]", "[]"));

      tfMovieSetName = new JTextField();
      panelHeader.add(tfMovieSetName, "cell 0 0,growx");
      tfMovieSetName.setColumns(10);

      JButton btnSearch = new JButton(new SearchAction());
      panelHeader.add(btnSearch, "cell 1 0");

      setTopIformationPanel(panelHeader);
    }
    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new MigLayout("", "[950lp,grow]", "[500,grow]"));

      JSplitPane splitPane = new JSplitPane();
      splitPane.setContinuousLayout(true);
      splitPane.setResizeWeight(0.5);
      panelContent.add(splitPane, "cell 0 0,grow");
      {
        JPanel panelResults = new JPanel();
        panelResults.setLayout(new MigLayout("", "[200lp:300lp,grow]", "[300lp,grow]"));
        JScrollPane panelSearchResults = new JScrollPane();
        panelResults.add(panelSearchResults, "cell 0 0,grow");
        splitPane.setLeftComponent(panelResults);
        {
          tableMovieSets = new JTable();
          panelSearchResults.setViewportView(tableMovieSets);
          tableMovieSets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
          tableMovieSets.setBorder(new LineBorder(new Color(0, 0, 0)));
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
        }
      }
      {
        JPanel panelSearchDetail = new JPanel();
        splitPane.setRightComponent(panelSearchDetail);
        panelSearchDetail.setLayout(new MigLayout("", "[150lp,grow 60][450lp,grow]", "[][250lp,grow][150lp][]"));
        {
          lblMovieSetName = new ReadOnlyTextArea("");
          lblMovieSetName.setOpaque(false);
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

          tableMovies = new JTable();
          scrollPane.setViewportView(tableMovies);
        }
        {
          cbAssignMovies = new JCheckBox(BUNDLE.getString("movieset.movie.assign")); //$NON-NLS-1$
          cbAssignMovies.setSelected(true);
          panelSearchDetail.add(cbAssignMovies, "cell 0 3 2 1,growx,aligny top");
        }
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
        JButton btnAbort = new JButton(BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
        btnAbort.setActionCommand("Abort");
        btnAbort.setToolTipText(BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
        btnAbort.setIcon(IconManager.PROCESS_STOP);
        btnAbort.addActionListener(this);
        addButton(btnAbort);
      }

      JButton btnCancel = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
      btnCancel.setActionCommand("Cancel");
      btnCancel.setToolTipText(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
      btnCancel.setIcon(IconManager.CANCEL_INV);
      btnCancel.addActionListener(this);
      addButton(btnCancel);

      btnOk = new JButton(BUNDLE.getString("Button.ok")); //$NON-NLS-1$
      btnOk.setActionCommand("Save");
      btnOk.setToolTipText(BUNDLE.getString("Button.ok")); //$NON-NLS-1$
      btnOk.setIcon(IconManager.APPLY_INV);
      btnOk.addActionListener(this);
      addDefaultButton(btnOk);
    }

    initDataBindings();

    // adjust table columns
    tableMovies.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tableMovies.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

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
      startProgressBar(BUNDLE.getString("chooser.searchingfor") + " " + searchTerm); //$NON-NLS-1$
      try {
        List<MediaScraper> sets = MediaScraper.getMediaScrapers(ScraperType.MOVIE_SET);
        if (sets != null && sets.size() > 0) {
          MediaScraper first = sets.get(0); // just get first
          IMovieSetMetadataProvider mp = (IMovieSetMetadataProvider) first.getMediaProvider();

          MediaSearchOptions options = new MediaSearchOptions(MediaType.MOVIE_SET, searchTerm);
          options.setLanguage(LocaleUtils.toLocale(MovieModuleManager.SETTINGS.getScraperLanguage().name()));
          List<MediaSearchResult> movieSets = mp.search(options);
          movieSetsFound.clear();
          if (movieSets.size() == 0) {
            movieSetsFound.add(MovieSetChooserModel.emptyResult);
          }
          else {
            for (MediaSearchResult collection : movieSets) {
              MovieSetChooserModel model = new MovieSetChooserModel(collection);
              movieSetsFound.add(model);
            }
          }
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
      putValue(NAME, BUNDLE.getString("Button.search")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movieset.search")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.SEARCH);
      putValue(LARGE_ICON_KEY, IconManager.SEARCH);
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
          movieSetToScrape.setTitle(model.getName());
          movieSetToScrape.setPlot(model.getOverview());
          // movieSetToScrape.setArtworkUrl(model.getPosterUrl(), MediaFileType.POSTER);
          // movieSetToScrape.setArtworkUrl(model.getFanartUrl(), MediaFileType.FANART);
          movieSetToScrape.setTmdbId(model.getTmdbId());
          movieSetToScrape.saveToDb();

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
          if (MovieModuleManager.SETTINGS.getMovieScraperMetadataConfig().isArtwork()) {
            // get artwork asynchronous
            model.startArtworkScrapeTask(movieSetToScrape, MovieModuleManager.SETTINGS.getMovieScraperMetadataConfig());
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
      startProgressBar(BUNDLE.getString("chooser.scrapeing") + " " + model.getName()); //$NON-NLS-1$

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

  protected void initDataBindings() {
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
    BeanProperty<JTextArea, String> jTextAreaBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextArea, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, tableMovieSets, jTableBeanProperty_1,
        lblMovieSetName, jTextAreaBeanProperty);
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
  }
}
