package org.tinymediamanager.ui.movies;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.scraper.Trailer;
import org.tinymediamanager.ui.TableColumnAdjuster;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MovieTrailerPanel extends JPanel {

  /** The movie selection model. */
  private MovieSelectionModel movieSelectionModel;
  private JTable              table;
  private TableColumnAdjuster tca = null;

  /**
   * Instantiates a new movie details panel.
   * 
   * @param model
   *          the model
   */
  public MovieTrailerPanel(MovieSelectionModel model) {
    this.movieSelectionModel = model;
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"), }));

    JScrollPane scrollPane = new JScrollPane();
    add(scrollPane, "2, 2, fill, fill");

    table = new JTable();
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    scrollPane.setViewportView(table);
    initDataBindings();

    tca = new TableColumnAdjuster(table);
    tca.setColumnDataIncluded(true);
    tca.setColumnHeaderIncluded(true);
  }

  public void adjustColumns() {
    tca.adjustColumns();
  }

  protected void initDataBindings() {
    BeanProperty<MovieSelectionModel, List<Trailer>> movieSelectionModelBeanProperty = BeanProperty.create("selectedMovie.trailers");
    JTableBinding<Trailer, MovieSelectionModel, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty, table);
    //
    BeanProperty<Trailer, String> trailerBeanProperty = BeanProperty.create("name");
    jTableBinding.addColumnBinding(trailerBeanProperty).setColumnName("Name").setEditable(false);
    //
    BeanProperty<Trailer, String> trailerBeanProperty_1 = BeanProperty.create("provider");
    jTableBinding.addColumnBinding(trailerBeanProperty_1).setColumnName("Source").setEditable(false);
    //
    BeanProperty<Trailer, String> trailerBeanProperty_2 = BeanProperty.create("quality");
    jTableBinding.addColumnBinding(trailerBeanProperty_2).setColumnName("Resolution").setEditable(false);
    //
    BeanProperty<Trailer, String> trailerBeanProperty_3 = BeanProperty.create("url");
    jTableBinding.addColumnBinding(trailerBeanProperty_3).setColumnName("Url").setEditable(false);
    //
    jTableBinding.setEditable(false);
    jTableBinding.bind();
  }
}
