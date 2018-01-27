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
package org.tinymediamanager.ui.moviesets.panels;

import static org.tinymediamanager.core.Constants.FANART;
import static org.tinymediamanager.core.Constants.POSTER;

import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.ui.ColumnLayout;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.components.table.TmmTableModel;
import org.tinymediamanager.ui.moviesets.MovieInMovieSetTableFormat;
import org.tinymediamanager.ui.moviesets.MovieSetSelectionModel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieSetInformationPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieSetInformationPanel extends JPanel {
  private static final long            serialVersionUID = -8166784589262658147L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle  BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private JLabel                       lblMovieSetName;
  private ImageLabel                   lblFanart;
  private JLabel                       lblFanartSize;
  private ImageLabel                   lblPoster;
  private JLabel                       lblPosterSize;
  private JTextArea                    taOverview;
  private TmmTable                     tableAssignedMovies;

  private final MovieSetSelectionModel selectionModel;
  private final EventList<Movie>       movieEventList;

  public MovieSetInformationPanel(MovieSetSelectionModel setSelectionModel) {
    this.selectionModel = setSelectionModel;

    movieEventList = new ObservableElementList<>(GlazedListsSwing.swingThreadProxyList(new BasicEventList<>()),
        GlazedLists.beanConnector(Movie.class));

    initComponents();

    // beansbinding init
    initDataBindings();

    // manual coded binding
    PropertyChangeListener propertyChangeListener = propertyChangeEvent -> {
      String property = propertyChangeEvent.getPropertyName();
      Object source = propertyChangeEvent.getSource();
      // react on selection of a movie and change of a movie set
      if ((source.getClass() == MovieSetSelectionModel.class && "selectedMovieSet".equals(property))
          || (source.getClass() == MovieSet.class && "movies".equals(property))) {
        movieEventList.clear();
        movieEventList.addAll(selectionModel.getSelectedMovieSet().getMovies());
      }
      if (source.getClass() == MovieSetSelectionModel.class && "selectedMovieSet".equals(property)) {
        MovieSetSelectionModel model = (MovieSetSelectionModel) source;
        setFanart(model.getSelectedMovieSet());
        setPoster(model.getSelectedMovieSet());
      }
      if ((source instanceof MovieSet && FANART.equals(property))) {
        MovieSet movieSet = (MovieSet) source;
        setFanart(movieSet);
      }
      if ((source instanceof MovieSet && POSTER.equals(property))) {
        MovieSet movieSet = (MovieSet) source;
        setPoster(movieSet);
      }
    };

    selectionModel.addPropertyChangeListener(propertyChangeListener);

    // select first entry

  }

  private void initComponents() {
    setLayout(new MigLayout("", "[100lp:100lp,grow][300lp:300lp,grow 250]", "[grow]"));
    {
      JPanel panelLeft = new JPanel();
      panelLeft.setLayout(new ColumnLayout());
      add(panelLeft, "cell 0 0,grow");

      lblPoster = new ImageLabel(false, false, true);
      lblPoster.setDesiredAspectRatio(2 / 3f);
      panelLeft.add(lblPoster);
      lblPoster.enableLightbox();
      lblPosterSize = new JLabel(BUNDLE.getString("mediafiletype.poster")); //$NON-NLS-1$
      panelLeft.add(lblPosterSize);
      panelLeft.add(Box.createVerticalStrut(20));

      lblFanart = new ImageLabel(false, false, true);
      lblFanart.setDesiredAspectRatio(16 / 9f);
      panelLeft.add(lblFanart);
      lblFanart.enableLightbox();
      lblFanartSize = new JLabel(BUNDLE.getString("mediafiletype.fanart")); //$NON-NLS-1$
      panelLeft.add(lblFanartSize);
      panelLeft.add(Box.createVerticalStrut(20));
    }
    {
      JPanel panelRight = new JPanel();
      add(panelRight, "cell 1 0,grow");
      panelRight.setLayout(new MigLayout("", "[450lp,grow]", "[][shrink 0][][250lp:350lp,grow][][shrink 0][][350lp,grow]"));

      {
        lblMovieSetName = new JLabel("");
        panelRight.add(lblMovieSetName, "cell 0 0, wmin 0");
        TmmFontHelper.changeFont(lblMovieSetName, 1.33, Font.BOLD);
      }
      {
        panelRight.add(new JSeparator(), "cell 0 1,growx");
      }
      {
        JLabel lblPlot = new JLabel(BUNDLE.getString("metatag.plot"));
        panelRight.add(lblPlot, "cell 0 2");
        TmmFontHelper.changeFont(lblPlot, Font.BOLD);

        JScrollPane scrollPaneOverview = new JScrollPane();
        panelRight.add(scrollPaneOverview, "cell 0 3,grow");

        taOverview = new ReadOnlyTextArea();
        scrollPaneOverview.setViewportView(taOverview);
      }
      {
        panelRight.add(new JSeparator(), "cell 0 5,growx");
      }
      {

        final DefaultEventTableModel<Movie> movieTableModel = new TmmTableModel<>(movieEventList, new MovieInMovieSetTableFormat());
        tableAssignedMovies = new TmmTable(movieTableModel);
        tableAssignedMovies.adjustColumnPreferredWidths(3);
        JScrollPane scrollPane = new JScrollPane(tableAssignedMovies);
        tableAssignedMovies.configureScrollPane(scrollPane);
        panelRight.add(scrollPane, "cell 0 7,grow");
      }
    }
  }

  private void setPoster(MovieSet movieSet) {
    // only reset if there was a real change
    if (movieSet.getArtworkFilename(MediaFileType.POSTER).equals(lblPoster.getImagePath())) {
      return;
    }

    lblPoster.clearImage();
    lblPoster.setImagePath(movieSet.getArtworkFilename(MediaFileType.POSTER));
    Dimension posterSize = movieSet.getArtworkDimension(MediaFileType.POSTER);
    if (posterSize.width > 0 && posterSize.height > 0) {
      lblPosterSize.setText(BUNDLE.getString("mediafiletype.poster") + " - " + posterSize.width + "x" + posterSize.height); //$NON-NLS-1$
    }
    else {
      lblPosterSize.setText(BUNDLE.getString("mediafiletype.poster")); //$NON-NLS-1$
    }
  }

  private void setFanart(MovieSet movieSet) {
    // only reset if there was a real change
    if (movieSet.getArtworkFilename(MediaFileType.FANART).equals(lblFanart.getImagePath())) {
      return;
    }

    lblFanart.clearImage();
    lblFanart.setImagePath(movieSet.getArtworkFilename(MediaFileType.FANART));
    Dimension fanartSize = movieSet.getArtworkDimension(MediaFileType.FANART);
    if (fanartSize.width > 0 && fanartSize.height > 0) {
      lblFanartSize.setText(BUNDLE.getString("mediafiletype.fanart") + " - " + fanartSize.width + "x" + fanartSize.height); //$NON-NLS-1$
    }
    else {
      lblFanartSize.setText(BUNDLE.getString("mediafiletype.fanart")); //$NON-NLS-1$
    }
  }

  protected void initDataBindings() {
    BeanProperty<MovieSetSelectionModel, String> tvShowSelectionModelBeanProperty = BeanProperty.create("selectedMovieSet.title");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSetSelectionModel, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty, lblMovieSetName, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSetSelectionModel, String> tvShowSelectionModelBeanProperty_1 = BeanProperty.create("selectedMovieSet.plot");
    BeanProperty<JTextArea, String> JTextAreaBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSetSelectionModel, String, JTextArea, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty_1, taOverview, JTextAreaBeanProperty);
    autoBinding_1.bind();
  }
}
