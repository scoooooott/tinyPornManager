package org.tinymediamanager.ui;

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

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieChooserModel;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.SearchQuery;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MovieChooser extends JDialog implements ActionListener {

  private final JPanel            contentPanel = new JPanel();

  private Movie                   movieToScrape;
  private JTextField              textFieldSearchString;
  private JTable                  table;
  private JLabel                  lblMovieName;
  private JTextPane               tpMovieDescription;
  private ImageLabel              lblMoviePoster;
  private JLabel                  lblProgressAction;
  private JProgressBar            progressBar;

  private List<MovieChooserModel> moviesFound  = ObservableCollections.observableList(new ArrayList<MovieChooserModel>());

  /**
   * Create the dialog.
   */
  public MovieChooser(Movie movie) {
    setModal(true);
    setBounds(100, 100, 858, 643);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("max(500px;default):grow"), }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("fill:403px:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
    {
      JPanel panelSearchField = new JPanel();
      contentPanel.add(panelSearchField, "1, 2, fill, fill");
      panelSearchField.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
          ColumnSpec.decode("right:default"), }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, }));
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
            searchMovie(textFieldSearchString.getText());
          }
        });
      }
    }
    {
      JSplitPane splitPane = new JSplitPane();
      splitPane.setContinuousLayout(true);
      contentPanel.add(splitPane, "1, 4, fill, fill");
      {
        JPanel panelSearchResults = new JPanel();
        splitPane.setLeftComponent(panelSearchResults);
        panelSearchResults.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
            ColumnSpec.decode("max(250px;default):grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
            RowSpec.decode("fill:max(212px;default):grow"), }));
        {
          table = new JTable();
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
                }
                catch (Exception ex) {

                }
              }
            }
          });
          panelSearchResults.add(table, "2, 2, fill, fill");
        }
      }
      {
        JPanel panelSearchDetail = new JPanel();
        splitPane.setRightComponent(panelSearchDetail);
        panelSearchDetail.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("left:150px"), FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("max(300px;default):grow"), }, new RowSpec[] { RowSpec.decode("30px"), RowSpec.decode("250px"),
            FormFactory.PARAGRAPH_GAP_ROWSPEC, RowSpec.decode("top:default:grow"), }));
        {
          lblMovieName = new JLabel("");
          lblMovieName.setFont(new Font("Dialog", Font.BOLD, 20));
          panelSearchDetail.add(lblMovieName, "1, 1, 3, 1, fill, top");
        }
        {
          lblMoviePoster = new ImageLabel();// new JLabel("");
          panelSearchDetail.add(lblMoviePoster, "1, 2, fill, fill");
        }
        {
          JScrollPane scrollPane = new JScrollPane();
          panelSearchDetail.add(scrollPane, "1, 4, 3, 1, fill, fill");
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
        JButton okButton = new JButton("OK");
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);
        buttonPane.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("max(82dlu;default)"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), ColumnSpec.decode("54px"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
            ColumnSpec.decode("81px"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"), }));
        {
          progressBar = new JProgressBar();
          buttonPane.add(progressBar, "2, 2");
        }
        {
          lblProgressAction = new JLabel("");
          buttonPane.add(lblProgressAction, "4, 2");
        }
        buttonPane.add(okButton, "5, 2, fill, top");
        getRootPane().setDefaultButton(okButton);
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
      searchMovie(textFieldSearchString.getText());
    }
  }

  public void actionPerformed(ActionEvent e) {
    if ("OK".equals(e.getActionCommand())) {
      int row = table.getSelectedRow();
      movieToScrape.setMetadata(moviesFound.get(row).getMetadata());
      this.setVisible(false);
    }
    if ("Cancel".equals(e.getActionCommand())) {
      this.setVisible(false);
    }

  }

  private void searchMovie(String searchTerm) {
    SearchTask task = new SearchTask(searchTerm);
    task.execute();
  }

  private void startProgressBar(String description) {
    lblProgressAction.setText(description);
    progressBar.setVisible(true);
    progressBar.setIndeterminate(true);
  }

  private void stopProgressBar() {
    lblProgressAction.setText("");
    progressBar.setVisible(false);
    progressBar.setIndeterminate(false);
  }

  private class SearchTask extends SwingWorker<Void, Void> {

    private String searchTerm;

    public SearchTask(String searchTerm) {
      this.searchTerm = searchTerm;
    }

    @Override
    public Void doInBackground() {
      startProgressBar("searching for: " + searchTerm);
      TmdbMetadataProvider tmdb = TmdbMetadataProvider.getInstance();
      try {
        List<MediaSearchResult> searchResult = tmdb.search(new SearchQuery(MediaType.MOVIE, SearchQuery.Field.QUERY, searchTerm));
        moviesFound.clear();
        for (MediaSearchResult result : searchResult) {
          moviesFound.add(new MovieChooserModel(tmdb, result));
        }

      }
      catch (Exception e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

      return null;
    }

    /*
     * Executed in event dispatching thread
     */
    @Override
    public void done() {
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
      startProgressBar("scraping: " + model.getName());
      model.scrapeMetaData();

      return null;
    }

    /*
     * Executed in event dispatching thread
     */
    @Override
    public void done() {
      stopProgressBar();
    }
  }

  protected void initDataBindings() {
    JTableBinding<MovieChooserModel, List<MovieChooserModel>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ,
        moviesFound, table);
    //
    BeanProperty<MovieChooserModel, String> movieChooserModelBeanProperty = BeanProperty.create("combinedName");
    jTableBinding.addColumnBinding(movieChooserModelBeanProperty).setColumnName("New Column");
    //
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_1 = BeanProperty.create("selectedElement.overview");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextPane, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_1,
        tpMovieDescription, jTextPaneBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_2 = BeanProperty.create("selectedElement.posterUrl");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imageUrl");
    AutoBinding<JTable, String, ImageLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_2,
        lblMoviePoster, imageLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_3 = BeanProperty.create("selectedElement.combinedName");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JLabel, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_3,
        lblMovieName, jLabelBeanProperty);
    autoBinding_3.bind();
  }
}
