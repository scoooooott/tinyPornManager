/*
 * Copyright 2013 Manuel Laggner
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
import java.awt.Font;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.movie.MovieCast;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.ui.CertificationImageConverter;
import org.tinymediamanager.ui.ImageLabel;
import org.tinymediamanager.ui.StarRater;
import org.tinymediamanager.ui.VoteCountConverter;
import org.tinymediamanager.ui.WatchedIconConverter;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MovieInformationPanel extends JPanel {

  /** The split pane vertical. */
  private JSplitPane          splitPaneVertical;

  /** The panel top. */
  private JPanel              panelTop;

  /** The panel watched image. */
  private JPanel              panelWatchedImage;

  /** The panel movie logos. */
  private JPanel              panelMovieLogos;

  /** The panel rating. */
  private StarRater           panelRatingStars;

  /** The lbl watched image. */
  private JLabel              lblWatchedImage;

  /** The lbl movie name. */
  private JLabel              lblMovieName;

  /** The label rating. */
  private JLabel              lblRating;

  /** The lbl vote count. */
  private JLabel              lblVoteCount;

  /** The lbl original name. */
  private JLabel              lblTagline;

  /** The lbl certification image. */
  private JLabel              lblCertificationImage;

  /** The lbl movie background. */
  private ImageLabel          lblMovieBackground;

  /** The lbl movie poster. */
  private ImageLabel          lblMoviePoster;

  /** The table cast. */
  private JTable              tableCast;

  /** The tabbed pane movie details. */
  private JTabbedPane         tabbedPaneMovieDetails;

  /** The panel overview. */
  private JPanel              panelOverview;

  /** The panel movie cast. */
  private JPanel              panelMovieCast;

  /** The panel details. */
  private JPanel              panelDetails;

  /** The lbl director t. */
  private JLabel              lblDirectorT;

  /** The lbl director. */
  private JLabel              lblDirector;

  /** The lbl writer t. */
  private JLabel              lblWriterT;

  /** The lbl writer. */
  private JLabel              lblWriter;

  /** The lbl actors. */
  private JLabel              lblActors;

  /** The text pane. */
  private JTextPane           tpOverview;

  /** The panel media information. */
  private JPanel              panelMediaInformation;

  /** The lbl actor thumb. */
  private ImageLabel          lblActorThumb;

  /** The panel movie trailer. */
  private MovieTrailerPanel   panelMovieTrailer;

  /** The movie selection model. */
  private MovieSelectionModel movieSelectionModel;

  /**
   * Instantiates a new movie information panel.
   * 
   * @param movieSelectionModel
   *          the movie selection model
   */
  public MovieInformationPanel(MovieSelectionModel movieSelectionModel) {
    this.movieSelectionModel = movieSelectionModel;

    setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("650px:grow"), }, new RowSpec[] { RowSpec.decode("fill:default:grow"), }));

    splitPaneVertical = new JSplitPane();
    splitPaneVertical.setBorder(null);
    splitPaneVertical.setResizeWeight(0.9);
    splitPaneVertical.setContinuousLayout(true);
    splitPaneVertical.setOneTouchExpandable(true);
    splitPaneVertical.setOrientation(JSplitPane.VERTICAL_SPLIT);
    add(splitPaneVertical, "1, 1, fill, fill");

    panelTop = new JPanel();
    panelTop.setBorder(null);
    splitPaneVertical.setTopComponent(panelTop);
    panelTop.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("300px:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { RowSpec.decode("fill:default"),
        RowSpec.decode("top:pref:grow"), }));

    JPanel panelMovieHeader = new JPanel();
    panelTop.add(panelMovieHeader, "2, 1, 3, 1, fill, top");
    panelMovieHeader.setBorder(null);
    panelMovieHeader.setLayout(new BorderLayout(0, 0));

    JPanel panelMovieTitle = new JPanel();
    panelMovieHeader.add(panelMovieTitle, BorderLayout.NORTH);
    panelMovieTitle.setLayout(new BorderLayout(0, 0));
    lblMovieName = new JLabel("");
    // panelMovieHeader.add(lblMovieName, BorderLayout.NORTH);
    panelMovieTitle.add(lblMovieName);
    lblMovieName.setFont(new Font("Dialog", Font.BOLD, 16));

    panelWatchedImage = new JPanel();
    panelMovieTitle.add(panelWatchedImage, BorderLayout.EAST);

    lblWatchedImage = new JLabel("");
    panelWatchedImage.add(lblWatchedImage);

    JPanel panelRatingTagline = new JPanel();
    panelMovieHeader.add(panelRatingTagline, BorderLayout.CENTER);
    panelRatingTagline.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("24px"), FormFactory.DEFAULT_ROWSPEC, }));

    lblRating = new JLabel("");
    panelRatingTagline.add(lblRating, "2, 2, left, center");

    lblVoteCount = new JLabel("");
    panelRatingTagline.add(lblVoteCount, "3, 2, left, center");

    panelRatingStars = new StarRater(5, 2);
    panelRatingTagline.add(panelRatingStars, "1, 2, left, top");
    panelRatingStars.setEnabled(false);

    lblTagline = new JLabel();
    panelRatingTagline.add(lblTagline, "1, 3, 3, 1, default, center");

    panelMovieLogos = new JPanel();
    panelMovieHeader.add(panelMovieLogos, BorderLayout.EAST);

    lblCertificationImage = new JLabel();
    panelMovieLogos.add(lblCertificationImage);

    JLayeredPane layeredPaneImages = new JLayeredPane();
    panelTop.add(layeredPaneImages, "1, 2, 4, 1, fill, fill");
    layeredPaneImages.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("max(10px;default)"), ColumnSpec.decode("left:120px"),
        ColumnSpec.decode("default:grow"), }, new RowSpec[] { RowSpec.decode("max(10px;default)"), RowSpec.decode("top:180px"),
        RowSpec.decode("fill:default:grow"), }));

    lblMovieBackground = new ImageLabel(false, true);
    layeredPaneImages.add(lblMovieBackground, "1, 1, 3, 3, fill, fill");

    lblMoviePoster = new ImageLabel();
    layeredPaneImages.setLayer(lblMoviePoster, 1);
    layeredPaneImages.add(lblMoviePoster, "2, 2, fill, fill");

    JPanel panelGenres = new MovieGenresPanel(movieSelectionModel);
    layeredPaneImages.setLayer(panelGenres, 2);
    layeredPaneImages.add(panelGenres, "2, 2, 2, 2, right, bottom");

    JPanel panelBottom = new JPanel();
    panelBottom.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("496px:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
        RowSpec.decode("fill:default:grow"), }));

    tabbedPaneMovieDetails = new JTabbedPane(JTabbedPane.TOP);
    panelBottom.add(tabbedPaneMovieDetails, "1, 2, fill, fill");
    splitPaneVertical.setBottomComponent(panelBottom);

    panelDetails = new MovieDetailsPanel(movieSelectionModel);
    tabbedPaneMovieDetails.addTab("Details", null, panelDetails, null);

    panelOverview = new JPanel();
    tabbedPaneMovieDetails.addTab("Overview", null, panelOverview, null);
    panelOverview.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("241px:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
        RowSpec.decode("fill:default:grow"), }));
    // panelMovieDetails.add(tabbedPaneMovieDetails, "2, 3, fill, fill");

    JScrollPane scrollPaneOverview = new JScrollPane();
    panelOverview.add(scrollPaneOverview, "1, 2, fill, fill");

    tpOverview = new JTextPane();
    scrollPaneOverview.setViewportView(tpOverview);

    panelMovieCast = new JPanel();
    panelMovieCast.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(39dlu;default)"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("125px"),
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("80px"),
        RowSpec.decode("default:grow"), FormFactory.NARROW_LINE_GAP_ROWSPEC, }));

    lblDirectorT = new JLabel("Director");
    panelMovieCast.add(lblDirectorT, "2, 2");

    lblDirector = new JLabel("");
    lblDirectorT.setLabelFor(lblDirector);
    panelMovieCast.add(lblDirector, "4, 2, 3, 1");

    lblWriterT = new JLabel("Writer");
    panelMovieCast.add(lblWriterT, "2, 4");

    lblWriter = new JLabel("");
    lblWriterT.setLabelFor(lblWriter);
    panelMovieCast.add(lblWriter, "4, 4, 3, 1");

    tabbedPaneMovieDetails.addTab("Cast", null, panelMovieCast, null);

    lblActors = new JLabel("Actors");
    panelMovieCast.add(lblActors, "2, 6, default, top");

    JScrollPane scrollPaneMovieCast = new JScrollPane();
    lblActors.setLabelFor(scrollPaneMovieCast);
    panelMovieCast.add(scrollPaneMovieCast, "4, 6, 1, 2");

    tableCast = new JTable();
    scrollPaneMovieCast.setViewportView(tableCast);

    lblActorThumb = new ImageLabel();
    panelMovieCast.add(lblActorThumb, "6, 2, 1, 6, fill, fill");

    panelMediaInformation = new MovieMediaInformationPanel(movieSelectionModel);
    tabbedPaneMovieDetails.addTab("Media Information", null, panelMediaInformation, null);

    panelMovieTrailer = new MovieTrailerPanel(movieSelectionModel);
    tabbedPaneMovieDetails.addTab("Trailer", null, panelMovieTrailer, null);

    // beansbinding init
    initDataBindings();
  }

  /**
   * Inits the data bindings.
   */
  private void initDataBindings() {
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty = BeanProperty.create("selectedMovie.nameForUi");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty, lblMovieName, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSelectionModel, Float> movieSelectionModelBeanProperty_1 = BeanProperty.create("selectedMovie.rating");
    AutoBinding<MovieSelectionModel, Float, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_1, lblRating, jLabelBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<StarRater, Float> starRaterBeanProperty = BeanProperty.create("rating");
    AutoBinding<MovieSelectionModel, Float, StarRater, Float> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_1, panelRatingStars, starRaterBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_4 = BeanProperty.create("selectedMovie.fanart");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imagePath");
    AutoBinding<MovieSelectionModel, String, ImageLabel, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_4, lblMovieBackground, imageLabelBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_5 = BeanProperty.create("selectedMovie.poster");
    AutoBinding<MovieSelectionModel, String, ImageLabel, String> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_5, lblMoviePoster, imageLabelBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_14 = BeanProperty.create("selectedMovie.overview");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, String, JTextPane, String> autoBinding_15 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_14, tpOverview, jTextPaneBeanProperty);
    autoBinding_15.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_15 = BeanProperty.create("selectedMovie.director");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_16 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_15, lblDirector, jLabelBeanProperty);
    autoBinding_16.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_16 = BeanProperty.create("selectedMovie.writer");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_17 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_16, lblWriter, jLabelBeanProperty);
    autoBinding_17.bind();
    //
    BeanProperty<MovieSelectionModel, List<MovieCast>> movieSelectionModelBeanProperty_17 = BeanProperty.create("selectedMovie.cast");
    JTableBinding<MovieCast, MovieSelectionModel, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_17, tableCast);
    //
    BeanProperty<MovieCast, String> movieCastBeanProperty = BeanProperty.create("name");
    jTableBinding.addColumnBinding(movieCastBeanProperty).setColumnName("Name").setEditable(false);
    //
    BeanProperty<MovieCast, String> movieCastBeanProperty_1 = BeanProperty.create("character");
    jTableBinding.addColumnBinding(movieCastBeanProperty_1).setColumnName("Character").setEditable(false);
    //
    jTableBinding.setEditable(false);
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.thumb");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty_1 = BeanProperty.create("imageUrl");
    AutoBinding<JTable, String, ImageLabel, String> autoBinding_18 = Bindings.createAutoBinding(UpdateStrategy.READ, tableCast, jTableBeanProperty,
        lblActorThumb, imageLabelBeanProperty_1);
    autoBinding_18.bind();
    //
    BeanProperty<MovieSelectionModel, Boolean> movieSelectionModelBeanProperty_19 = BeanProperty.create("selectedMovie.hasRating");
    BeanProperty<StarRater, Boolean> starRaterBeanProperty_1 = BeanProperty.create("visible");
    AutoBinding<MovieSelectionModel, Boolean, StarRater, Boolean> autoBinding_21 = Bindings.createAutoBinding(UpdateStrategy.READ,
        movieSelectionModel, movieSelectionModelBeanProperty_19, panelRatingStars, starRaterBeanProperty_1);
    autoBinding_21.bind();
    //
    BeanProperty<JLabel, Boolean> jLabelBeanProperty_1 = BeanProperty.create("visible");
    AutoBinding<MovieSelectionModel, Boolean, JLabel, Boolean> autoBinding_22 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_19, lblRating, jLabelBeanProperty_1);
    autoBinding_22.bind();
    //
    AutoBinding<MovieSelectionModel, Boolean, JLabel, Boolean> autoBinding_23 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_19, lblVoteCount, jLabelBeanProperty_1);
    autoBinding_23.bind();
    //
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty_2 = BeanProperty.create("selectedMovie.votes");
    AutoBinding<MovieSelectionModel, Integer, JLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_2, lblVoteCount, jLabelBeanProperty);
    autoBinding_2.setConverter(new VoteCountConverter());
    autoBinding_2.bind();
    //
    BeanProperty<MovieSelectionModel, Certification> movieSelectionModelBeanProperty_6 = BeanProperty.create("selectedMovie.certification");
    BeanProperty<JLabel, Icon> jLabelBeanProperty_2 = BeanProperty.create("icon");
    AutoBinding<MovieSelectionModel, Certification, JLabel, Icon> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ,
        movieSelectionModel, movieSelectionModelBeanProperty_6, lblCertificationImage, jLabelBeanProperty_2);
    autoBinding_7.setConverter(new CertificationImageConverter());
    autoBinding_7.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_3 = BeanProperty.create("selectedMovie.tagline");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_3, lblTagline, jLabelBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<MovieSelectionModel, Boolean> movieSelectionModelBeanProperty_7 = BeanProperty.create("selectedMovie.watched");
    AutoBinding<MovieSelectionModel, Boolean, JLabel, Icon> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_7, lblWatchedImage, jLabelBeanProperty_2);
    autoBinding_8.setConverter(new WatchedIconConverter());
    autoBinding_8.bind();
  }

  /**
   * Inits the panel (steps which has to be done after binding in calling class)
   */
  public void init() {
    if (tableCast.getModel().getRowCount() > 0) {
      tableCast.getSelectionModel().setSelectionInterval(0, 0);
    }
    else {
      lblActorThumb.setImageUrl("");
    }

    // changes upon movie selection
    tableCast.getModel().addTableModelListener(new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        // change to the first actor on movie change
        if (tableCast.getModel().getRowCount() > 0) {
          tableCast.getSelectionModel().setSelectionInterval(0, 0);
        }
        else {
          lblActorThumb.setImageUrl("");
        }
      }
    });
  }

  /**
   * Gets the split pane vertical.
   * 
   * @return the split pane vertical
   */
  public JSplitPane getSplitPaneVertical() {
    return splitPaneVertical;
  }
}
