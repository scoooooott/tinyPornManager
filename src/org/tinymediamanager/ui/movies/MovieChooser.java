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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
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
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.ui.ImageLabel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieChooser.
 */
public class MovieChooser extends JDialog implements ActionListener {
  /** The static LOGGER */
  private static final Logger     LOGGER       = Logger.getLogger(MovieChooser.class);

  /** The content panel. */
  private final JPanel            contentPanel = new JPanel();

  /** The movie to scrape. */
  private Movie                   movieToScrape;

  /** The text field search string. */
  private JTextField              textFieldSearchString;

  /** The table. */
  private JTable                  table;

  /** The lbl movie name. */
  private JLabel                  lblMovieName;

  /** The tp movie description. */
  private JTextPane               tpMovieDescription;

  /** The lbl movie poster. */
  private ImageLabel              lblMoviePoster;

  /** The lbl progress action. */
  private JLabel                  lblProgressAction;

  /** The progress bar. */
  private JProgressBar            progressBar;

  /** The movies found. */
  private List<MovieChooserModel> moviesFound  = ObservableCollections.observableList(new ArrayList<MovieChooserModel>());
  private JLabel                  lblTagline;

  /**
   * Create the dialog.
   * 
   * @param movie
   *          the movie
   */
  public MovieChooser(Movie movie) {
    setTitle("search movie");
    setIconImage(Globals.logo);
    setModal(true);
    setBounds(5, 5, 1111, 643);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("fill:403px:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
    {
      JPanel panelSearchField = new JPanel();
      contentPanel.add(panelSearchField, "1, 2, fill, fill");
      panelSearchField.setLayout(new FormLayout(
          new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow"), ColumnSpec.decode("right:default"), },
          new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, }));
      {
        textFieldSearchString = new JTextField();
        panelSearchField.add(textFieldSearchString, "2, 1, fill, default");
        textFieldSearchString.setColumns(10);
      }

      {
        JButton btnSearch = new JButton("Search");
        panelSearchField.add(btnSearch, "3, 1");
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
        panelSearchResults.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("350px:grow"), }, new RowSpec[] {
            FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("fill:max(212px;default):grow"), }));
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
                  } catch (Exception ex) {

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
        panelSearchDetail.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:150px"), FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("max(300px;default):grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("250px"), FormFactory.PARAGRAPH_GAP_ROWSPEC, RowSpec.decode("top:default:grow"), }));
        {
          lblMovieName = new JLabel("");
          lblMovieName.setFont(new Font("Dialog", Font.BOLD, 14));
          panelSearchDetail.add(lblMovieName, "2, 1, 3, 1, fill, top");
        }
        {
          lblTagline = new JLabel("");
          panelSearchDetail.add(lblTagline, "2, 2, 3, 1");
        }
        {
          lblMoviePoster = new ImageLabel();// new JLabel("");
          panelSearchDetail.add(lblMoviePoster, "2, 4, fill, fill");
        }
        {
          JPanel panel = new JPanel();
          panelSearchDetail.add(panel, "4, 4, fill, fill");
          panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
              ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
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
      JPanel buttonPane = new JPanel();
      contentPanel.add(buttonPane, "1, 6");
      {
        JButton okButton = new JButton("Ok");
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);
        buttonPane.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("max(82dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), ColumnSpec.decode("100px"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("100px"), }, new RowSpec[] {
            FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"), }));
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
  public void actionPerformed(ActionEvent e) {
    if ("OK".equals(e.getActionCommand())) {
      int row = table.getSelectedRow();
      if (row >= 0) {
        movieToScrape.setMetadata(moviesFound.get(row).getMetadata());
        this.setVisible(false);
      }
    }
    if ("Cancel".equals(e.getActionCommand())) {
      this.setVisible(false);
    }

  }

  /**
   * Search movie.
   * 
   * @param searchTerm
   *          the search term
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
    private String imdbId;

    /**
     * Instantiates a new search task.
     * 
     * @param searchTerm
     *          the search term
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
      MovieList movieList = MovieList.getInstance();
      List<MediaSearchResult> searchResult = movieList.searchMovie(searchTerm, imdbId);
      moviesFound.clear();
      for (MediaSearchResult result : searchResult) {
        moviesFound.add(new MovieChooserModel(movieList.getMetadataProvider(), result));
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

  protected void initDataBindings() {
    JTableBinding<MovieChooserModel, List<MovieChooserModel>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, moviesFound, table);
    //
    BeanProperty<MovieChooserModel, String> movieChooserModelBeanProperty = BeanProperty.create("combinedName");
    jTableBinding.addColumnBinding(movieChooserModelBeanProperty).setColumnName("Searsch result").setEditable(false);
    //
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_1 = BeanProperty.create("selectedElement.overview");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextPane, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_1, tpMovieDescription,
        jTextPaneBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_2 = BeanProperty.create("selectedElement.posterUrl");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imageUrl");
    AutoBinding<JTable, String, ImageLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_2, lblMoviePoster,
        imageLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_3 = BeanProperty.create("selectedElement.combinedName");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JLabel, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_3, lblMovieName, jLabelBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.tagline");
    AutoBinding<JTable, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty, lblTagline, jLabelBeanProperty);
    autoBinding.bind();
  }
}
