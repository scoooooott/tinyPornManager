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
package org.tinymediamanager.ui.moviesets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
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
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieSet;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.ui.ImageLabel;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.moviesets.MovieSetChooserModel.MovieInSet;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.omertron.themoviedbapi.model.Collection;

/**
 * The Class MovieSetChooserPanel.
 */
public class MovieSetChooser extends JDialog implements ActionListener {

  /** The static LOGGER. */
  private static final Logger        LOGGER         = Logger.getLogger(MovieSetChooser.class);

  /** The movie set to edit. */
  private MovieSet                   movieSetToEdit;

  /** The lbl progress action. */
  private JLabel                     lblProgressAction;

  /** The progress bar. */
  private JProgressBar               progressBar;

  /** The tf movie set name. */
  private JTextField                 tfMovieSetName;

  /** The table movie sets. */
  private JTable                     tableMovieSets;

  /** The lbl movie name. */
  private JTextArea                  lblMovieSetName;

  /** The lbl movie poster. */
  private ImageLabel                 lblMovieSetPoster;

  /** The movies found. */
  private List<MovieSetChooserModel> movieSetsFound = ObservableCollections.observableList(new ArrayList<MovieSetChooserModel>());

  /** The action search. */
  private final Action               actionSearch   = new SearchAction();

  /** The table movies. */
  private JTable                     tableMovies;

  /** The cb assign movies. */
  private JCheckBox                  cbAssignMovies;

  /**
   * Instantiates a new movie set chooser panel.
   * 
   * @param movieSet
   *          the movie set
   */
  public MovieSetChooser(MovieSet movieSet) {
    setTitle("search movie set");
    setName("movieSetChooser");
    setBounds(5, 5, 865, 578);
    TmmWindowSaver.loadSettings(this);
    setIconImage(Globals.logo);
    setModal(true);

    movieSetToEdit = movieSet;

    getContentPane().setLayout(new BorderLayout(0, 0));

    JPanel panelHeader = new JPanel();
    getContentPane().add(panelHeader, BorderLayout.NORTH);
    panelHeader.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("114px:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"),
        FormFactory.RELATED_GAP_ROWSPEC, }));
    {
      tfMovieSetName = new JTextField();
      panelHeader.add(tfMovieSetName, "2, 2, fill, fill");
      tfMovieSetName.setColumns(10);
    }
    {
      JButton btnSearch = new JButton("");
      btnSearch.setAction(actionSearch);
      panelHeader.add(btnSearch, "4, 2, fill, top");
    }
    {
      JSplitPane splitPane = new JSplitPane();
      splitPane.setResizeWeight(0.5);
      getContentPane().add(splitPane, BorderLayout.CENTER);
      {
        JScrollPane panelSearchResults = new JScrollPane();
        splitPane.setLeftComponent(panelSearchResults);
        {
          tableMovieSets = new JTable();
          panelSearchResults.setViewportView(tableMovieSets);
          tableMovieSets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
          tableMovieSets.setBorder(new LineBorder(new Color(0, 0, 0)));
          ListSelectionModel rowSM = tableMovieSets.getSelectionModel();
          rowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
              // Ignore extra messages.
              if (e.getValueIsAdjusting())
                return;

              ListSelectionModel lsm = (ListSelectionModel) e.getSource();
              if (!lsm.isSelectionEmpty()) {
                int selectedRow = lsm.getMinSelectionIndex();
                selectedRow = tableMovieSets.convertRowIndexToModel(selectedRow);
                try {
                  MovieSetChooserModel model = movieSetsFound.get(selectedRow);
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
      {
        JPanel panelSearchDetail = new JPanel();
        splitPane.setRightComponent(panelSearchDetail);
        panelSearchDetail.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:150px"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(300px;default):grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
            FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("250px"), FormFactory.PARAGRAPH_GAP_ROWSPEC,
            RowSpec.decode("top:default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
        {
          lblMovieSetName = new JTextArea("");
          lblMovieSetName.setLineWrap(true);
          lblMovieSetName.setOpaque(false);
          lblMovieSetName.setWrapStyleWord(true);
          lblMovieSetName.setFont(new Font("Dialog", Font.BOLD, 14));
          panelSearchDetail.add(lblMovieSetName, "2, 1, 3, 1, fill, top");
        }
        {
          lblMovieSetPoster = new ImageLabel();// new JLabel("");
          panelSearchDetail.add(lblMovieSetPoster, "2, 3, fill, fill");
        }
        {
          JPanel panel = new JPanel();
          panelSearchDetail.add(panel, "4, 3, fill, fill");
          panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
              FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
              FormFactory.DEFAULT_ROWSPEC, }));
        }
        {

          JScrollPane scrollPane = new JScrollPane();
          panelSearchDetail.add(scrollPane, "2, 5, 3, 1, fill, fill");
          {
            tableMovies = new JTable();
            scrollPane.setViewportView(tableMovies);
          }

        }
        {
          cbAssignMovies = new JCheckBox("Assign movies to this movieset");
          panelSearchDetail.add(cbAssignMovies, "2, 7, 3, 1");
        }
      }
    }

    {
      JPanel buttonPane = new JPanel();
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      {
        buttonPane.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("185px"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("18px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("100px"), },
            new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"), }));
        {
          progressBar = new JProgressBar();
          buttonPane.add(progressBar, "2, 2, fill, center");
        }
        {
          lblProgressAction = new JLabel("");
          buttonPane.add(lblProgressAction, "4, 2, fill, center");
        }
        {
          JButton btnSave = new JButton("Save");
          btnSave.setActionCommand("Save");
          btnSave.setToolTipText("Save movie set metadata");
          btnSave.addActionListener(this);
          buttonPane.add(btnSave, "8, 2, fill, top");
        }
        {
          JButton btnCancel = new JButton("Cancel");
          btnCancel.setActionCommand("Cancel");
          btnCancel.setToolTipText("Cancel");
          btnCancel.addActionListener(this);
          buttonPane.add(btnCancel, "10, 2, fill, top");
        }
      }
    }
    initDataBindings();

    {
      tfMovieSetName.setText(movieSet.getName());
      searchMovie();
    }

  }

  /**
   * Search movie.
   * 
   */
  private void searchMovie() {
    SearchTask task = new SearchTask(tfMovieSetName.getText());
    task.execute();
  }

  /**
   * The Class SearchTask.
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
      startProgressBar("searching for: " + searchTerm);
      TmdbMetadataProvider mp;
      try {
        mp = new TmdbMetadataProvider();
        List<Collection> movieSets = mp.searchMovieSets(searchTerm);
        movieSetsFound.clear();

        for (Collection collection : movieSets) {
          MovieSetChooserModel model = new MovieSetChooserModel(collection);
          movieSetsFound.add(model);
        }

      }
      catch (Exception e1) {
        LOGGER.warn("SearchTask", e1);
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
   * The Class SearchAction.
   */
  private class SearchAction extends AbstractAction {

    /**
     * Instantiates a new search action.
     */
    public SearchAction() {
      putValue(NAME, "Search");
      putValue(SHORT_DESCRIPTION, "Search movie set metadata");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      searchMovie();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent arg0) {
    if ("Cancel".equals(arg0.getActionCommand())) {
      // cancel
      setVisible(false);
      dispose();
    }

    if ("Save".equals(arg0.getActionCommand())) {
      // save it
      int row = tableMovieSets.getSelectedRow();
      if (row >= 0) {
        MovieSetChooserModel model = movieSetsFound.get(row);
        movieSetToEdit.setName(model.getName());

        // assign movies
        if (cbAssignMovies.isSelected()) {
          movieSetToEdit.removeAllMovies();
          for (MovieInSet movieInSet : model.getMovies()) {
            Movie movie = movieInSet.getMovie();
            if (movie == null) {
              continue;
            }

            // check if the found movie contains a matching set
            if (movie.getMovieSet() != null) {
              // unassign movie from set
              MovieSet mSet = movie.getMovieSet();
              mSet.removeMovie(movie);
            }

            movie.setMovieSet(movieSetToEdit);
            movie.saveToDb();

            movieSetToEdit.addMovie(movie);
          }
        }

      }
      setVisible(false);
      dispose();
    }
  }

  /**
   * The Class ScrapeTask.
   */
  private class ScrapeTask extends SwingWorker<Void, Void> {

    /** The model. */
    private MovieSetChooserModel model;

    /**
     * Instantiates a new scrape task.
     * 
     * @param model
     *          the model
     */
    public ScrapeTask(MovieSetChooserModel model) {
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
      model.scrapeMetadata();

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
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    JTableBinding<MovieSetChooserModel, List<MovieSetChooserModel>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ,
        movieSetsFound, tableMovieSets);
    //
    BeanProperty<MovieSetChooserModel, String> movieSetChooserModelBeanProperty = BeanProperty.create("name");
    jTableBinding.addColumnBinding(movieSetChooserModelBeanProperty).setColumnName("Name").setEditable(false);
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
    BeanProperty<MovieInSet, Integer> movieInSetBeanProperty_1 = BeanProperty.create("tmdbId");
    jTableBinding_1.addColumnBinding(movieInSetBeanProperty_1).setColumnName("TMDB Id").setEditable(false);
    //
    BeanProperty<MovieInSet, String> movieInSetBeanProperty_2 = BeanProperty.create("movie.name");
    jTableBinding_1.addColumnBinding(movieInSetBeanProperty_2).setColumnName("matched Movie").setEditable(false);
    //
    jTableBinding_1.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_1 = BeanProperty.create("selectedElement.name");
    BeanProperty<JTextArea, String> jTextAreaBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextArea, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, tableMovieSets,
        jTableBeanProperty_1, lblMovieSetName, jTextAreaBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_2 = BeanProperty.create("selectedElement.posterUrl");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imageUrl");
    AutoBinding<JTable, String, ImageLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, tableMovieSets,
        jTableBeanProperty_2, lblMovieSetPoster, imageLabelBeanProperty);
    autoBinding_1.bind();
  }
}
