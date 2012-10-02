package org.tinymediamanager.ui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieCast;
import org.tinymediamanager.core.movie.MovieList;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MoviePanel extends JPanel {
  private MovieList    movieList               = MovieList.getInstance();

  private JTextField   textField;
  private JTable       table;
  private final Action actionUpdateDataSources = new SwingAction();
  private final Action actionScrape            = new SwingAction_1();
  private JTextPane    textPane;
  private JLabel       lblMovieName;
  private ImageLabel   lblMovieBackground;
  private ImageLabel   lblMoviePoster;
  private JTable       tableCast;
  private JLabel       lblOriginalName;
  private final Action actionEditMovie         = new SwingAction_2();

  /**
   * Create the panel.
   */
  public MoviePanel() {
    setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("248px:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:27px:grow"), FormFactory.RELATED_GAP_ROWSPEC, }));

    JSplitPane splitPane = new JSplitPane();
    splitPane.setContinuousLayout(true);
    add(splitPane, "1, 2, fill, fill");

    JPanel panelMovieList = new JPanel();
    splitPane.setLeftComponent(panelMovieList);
    panelMovieList.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        ColumnSpec.decode("max(200px;default):grow"), }, new RowSpec[] { RowSpec.decode("26px"), FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("fill:max(200px;default):grow"), }));

    JToolBar toolBar = new JToolBar();
    toolBar.setRollover(true);
    toolBar.setFloatable(false);
    panelMovieList.add(toolBar, "2, 1, fill, fill");

    JButton buttonUpdateDataSources = toolBar.add(actionUpdateDataSources);
    JButton buttonScrape = toolBar.add(actionScrape);
    JButton buttonEdit = toolBar.add(actionEditMovie);

    textField = new JTextField();
    panelMovieList.add(textField, "3, 1, right, bottom");
    textField.setColumns(10);

    JScrollPane scrollPane = new JScrollPane();
    panelMovieList.add(scrollPane, "2, 3, 2, 1, fill, fill");

    table = new JTable();
    table.setFont(new Font("Dialog", Font.PLAIN, 11));
    scrollPane.setViewportView(table);
    table.setBorder(UIManager.getBorder("Tree.editorBorder"));

    JPanel panelMovieDetails = new JPanel();
    splitPane.setRightComponent(panelMovieDetails);
    panelMovieDetails.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("400px:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("right:250px"), },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("50px"), RowSpec.decode("fill:max(461px;default):grow"),
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), }));

    JPanel panelMovieHeader = new JPanel();
    panelMovieHeader.setBorder(null);
    panelMovieDetails.add(panelMovieHeader, "2, 2, 3, 1, fill, fill");
    panelMovieHeader.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("400px:grow"), FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("right:250px"), }, new RowSpec[] { RowSpec.decode("30px"), FormFactory.DEFAULT_ROWSPEC, }));

    lblMovieName = new JLabel("");
    panelMovieHeader.add(lblMovieName, "1, 1, 3, 1, left, top");
    lblMovieName.setFont(new Font("Dialog", Font.BOLD, 20));

    lblOriginalName = new JLabel("");
    panelMovieHeader.add(lblOriginalName, "1, 2");

    JLayeredPane layeredPaneImages = new JLayeredPane();
    panelMovieDetails.add(layeredPaneImages, "2, 3, 3, 1, fill, fill");
    layeredPaneImages.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("max(10px;default)"), ColumnSpec.decode("left:100px"),
        ColumnSpec.decode("default:grow"), }, new RowSpec[] { RowSpec.decode("max(10px;default)"), RowSpec.decode("top:150px"),
        RowSpec.decode("fill:default:grow"), }));

    lblMovieBackground = new ImageLabel();
    layeredPaneImages.add(lblMovieBackground, "1, 1, 3, 3, fill, fill");

    lblMoviePoster = new ImageLabel();
    layeredPaneImages.setLayer(lblMoviePoster, 1);
    layeredPaneImages.add(lblMoviePoster, "2, 2, fill, fill");

    JScrollPane scrollPaneOverview = new JScrollPane();
    panelMovieDetails.add(scrollPaneOverview, "2, 5, fill, fill");

    textPane = new JTextPane();
    scrollPaneOverview.setViewportView(textPane);

    JScrollPane scrollPaneMovieCast = new JScrollPane();
    panelMovieDetails.add(scrollPaneMovieCast, "4, 5, fill, fill");

    tableCast = new JTable();
    scrollPaneMovieCast.setViewportView(tableCast);

    initDataBindings();

    TableRowSorter sorter = new TableRowSorter(table.getModel());
    table.setRowSorter(sorter);

    textField.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        newFilter();
      }

      public void insertUpdate(DocumentEvent e) {
        newFilter();
      }

      public void removeUpdate(DocumentEvent e) {
        newFilter();
      }
    });

    // year column
    table.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(35);
    table.getTableHeader().getColumnModel().getColumn(1).setMinWidth(35);
    table.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(50);

    // NFO column
    table.getTableHeader().getColumnModel().getColumn(2).setHeaderRenderer(new IconRenderer());
    table.getTableHeader().getColumnModel().getColumn(2).setMaxWidth(20);
    URL imageURL = MoviePanel.class.getResource("images/File.png");
    if (imageURL != null) {
      table.getColumnModel().getColumn(2).setHeaderValue(new ImageIcon(imageURL));
    }

    // poster column
    table.getTableHeader().getColumnModel().getColumn(3).setHeaderRenderer(new IconRenderer());
    table.getTableHeader().getColumnModel().getColumn(3).setMaxWidth(20);
    imageURL = null;
    imageURL = MoviePanel.class.getResource("images/Image.png");
    if (imageURL != null) {
      table.getColumnModel().getColumn(3).setHeaderValue(new ImageIcon(imageURL));
    }

  }

  private class SwingAction extends AbstractAction {
    public SwingAction() {
      // putValue(NAME, "UDS");
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Folder-Sync.png")));
      putValue(SHORT_DESCRIPTION, "Update data sources");
    }

    public void actionPerformed(ActionEvent e) {
      movieList.updateDataSources();
    }
  }

  private class SwingAction_1 extends AbstractAction {
    public SwingAction_1() {
      // putValue(NAME, "SCR");
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
      putValue(SHORT_DESCRIPTION, "Search & scrape movie");
    }

    public void actionPerformed(ActionEvent e) {
      for (int row : table.getSelectedRows()) {
        row = table.convertRowIndexToModel(row);
        Movie movie = movieList.getMovies().get(row);
        MovieChooser dialogMovieChooser = new MovieChooser(movie);
        dialogMovieChooser.pack();
        dialogMovieChooser.setVisible(true);
      }
    }

  }

  /**
   * Update the row filter regular expression from the expression in the text
   * box.
   */
  private void newFilter() {
    RowFilter rf = null;
    TableRowSorter sorter = (TableRowSorter) table.getRowSorter();

    // only update, if text is longer than 2 characters
    if (textField.getText().length() > 2) {
      try {
        // If current expression doesn't parse, don't update.
        String filterText = "(?i)" + textField.getText();
        rf = RowFilter.regexFilter(filterText, 0);
      }
      catch (java.util.regex.PatternSyntaxException e) {
        sorter.setRowFilter(rf);
        return;
      }
    }
    sorter.setRowFilter(rf);
  }

  protected void initDataBindings() {
    BeanProperty<MovieList, List<Movie>> movieListBeanProperty = BeanProperty.create("movies");
    JTableBinding<Movie, MovieList, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, movieList, movieListBeanProperty,
        table);
    //
    BeanProperty<Movie, String> movieBeanProperty = BeanProperty.create("name");
    jTableBinding.addColumnBinding(movieBeanProperty).setColumnName("Title").setEditable(false);
    //
    BeanProperty<Movie, String> movieBeanProperty_1 = BeanProperty.create("year");
    jTableBinding.addColumnBinding(movieBeanProperty_1).setColumnName("Year").setEditable(false);
    //
    BeanProperty<Movie, Boolean> movieBeanProperty_2 = BeanProperty.create("hasNfoFile");
    JTableBinding<Movie, MovieList, JTable>.ColumnBinding columnBinding = jTableBinding.addColumnBinding(movieBeanProperty_2);
    columnBinding.setColumnName("NFO");
    columnBinding.setEditable(false);
    columnBinding.setColumnClass(ImageIcon.class);
    columnBinding.setConverter(new ImageIconConverter());
    //
    BeanProperty<Movie, Boolean> movieBeanProperty_3 = BeanProperty.create("hasImages");
    JTableBinding<Movie, MovieList, JTable>.ColumnBinding columnBinding_1 = jTableBinding.addColumnBinding(movieBeanProperty_3);
    columnBinding_1.setColumnName("Poster");
    columnBinding_1.setEditable(false);
    columnBinding_1.setColumnClass(ImageIcon.class);
    columnBinding_1.setConverter(new ImageIconConverter());
    //
    jTableBinding.setEditable(false);
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.overview");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextPane, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty, textPane,
        jTextPaneBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<JTable, List<MovieCast>> jTableBeanProperty_3 = BeanProperty.create("selectedElement.actors");
    JTableBinding<MovieCast, JTable, JTable> jTableBinding_1 = SwingBindings.createJTableBinding(UpdateStrategy.READ, table, jTableBeanProperty_3,
        tableCast);
    //
    BeanProperty<MovieCast, String> movieCastBeanProperty = BeanProperty.create("name");
    jTableBinding_1.addColumnBinding(movieCastBeanProperty).setColumnName("Name");
    //
    BeanProperty<MovieCast, String> movieCastBeanProperty_1 = BeanProperty.create("character");
    jTableBinding_1.addColumnBinding(movieCastBeanProperty_1).setColumnName("Role");
    //
    jTableBinding_1.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_1 = BeanProperty.create("selectedElement.nameForUi");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_1,
        lblMovieName, jLabelBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_4 = BeanProperty.create("selectedElement.originalName");
    AutoBinding<JTable, String, JLabel, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_4,
        lblOriginalName, jLabelBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_2 = BeanProperty.create("selectedElement.fanart");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imagePath");
    AutoBinding<JTable, String, ImageLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_2,
        lblMovieBackground, imageLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_5 = BeanProperty.create("selectedElement.poster");
    AutoBinding<JTable, String, ImageLabel, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty_5,
        lblMoviePoster, imageLabelBeanProperty);
    autoBinding_4.bind();
  }

  private class SwingAction_2 extends AbstractAction {
    public SwingAction_2() {
      // putValue(NAME, "EDIT");
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Pencil.png")));
      putValue(SHORT_DESCRIPTION, "Edit movie");
    }

    public void actionPerformed(ActionEvent e) {
      for (int row : table.getSelectedRows()) {
        row = table.convertRowIndexToModel(row);
        Movie movie = movieList.getMovies().get(row);
        MovieEditor dialogMovieEditor = new MovieEditor(movie);
        // dialogMovieEditor.pack();
        dialogMovieEditor.setVisible(true);
      }
    }
  }

}
