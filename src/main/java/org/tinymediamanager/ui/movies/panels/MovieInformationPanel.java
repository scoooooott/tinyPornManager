package org.tinymediamanager.ui.movies.panels;

import static org.tinymediamanager.core.Constants.*;

import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.ColumnLayout;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.StarRater;
import org.tinymediamanager.ui.converter.VoteCountConverter;
import org.tinymediamanager.ui.movies.MovieSelectionModel;
import org.tinymediamanager.ui.panels.MediaInformationLogosPanel;

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

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieInformationPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieInformationPanel extends JPanel {

  private static final long           serialVersionUID = -8527284262749511617L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieSelectionModel         movieSelectionModel;
  private final ImageIcon             imageEmtpy       = new ImageIcon();
  private ImageIcon                   imageUnwatched;

  private StarRater                   panelRatingStars;
  private JLabel                      lblMovieName;
  private JLabel                      lblRating;
  private JLabel                      lblVoteCount;
  private JLabel                      lblTagline;
  private JTextPane                   tpOverview;
  private JPanel                      panelTopRight;
  private JLabel                      lblYear;
  private JLabel                      lblImdbid;
  private JLabel                      lblRunningTime;
  private JLabel                      lblTmdbid;
  private JLabel                      lblGenres;
  private JTextPane                   tpPlot;
  private JPanel                      panelTop1;
  private JScrollPane                 scrollPane;
  private JPanel                      panelBottomRight;
  private JPanel                      panelLeft;
  private ImageLabel                  lblMoviePoster;
  private JLabel                      lblPosterSize;
  private ImageLabel                  lblMovieFanart;
  private JLabel                      lblFanartSize;
  private MediaInformationLogosPanel  panelLogos;

  /**
   * Instantiates a new movie information panel.
   * 
   * @param movieSelectionModel
   *          the movie selection model
   */
  public MovieInformationPanel(MovieSelectionModel movieSelectionModel) {
    this.movieSelectionModel = movieSelectionModel;

    try {
      imageUnwatched = new ImageIcon(MovieInformationPanel.class.getResource("/org/tinymediamanager/ui/images/unwatched.png"));
    }
    catch (Exception e) {
      imageUnwatched = imageEmtpy;
    }

    putClientProperty("class", "roundedPanel");
    setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("70dlu:grow"), FormSpecs.UNRELATED_GAP_COLSPEC,
            ColumnSpec.decode("200dlu:grow(2)"), FormSpecs.UNRELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.PARAGRAPH_GAP_ROWSPEC, RowSpec.decode("fill:default"), FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), FormSpecs.PARAGRAPH_GAP_ROWSPEC, }));

    panelLeft = new JPanel();
    panelLeft.setLayout(new ColumnLayout());
    add(panelLeft, "2, 2, 1, 5, fill, fill");

    lblMoviePoster = new ImageLabel(false, false, true);
    lblMoviePoster.setDesiredAspectRatio(2 / 3f);
    panelLeft.add(lblMoviePoster);
    lblMoviePoster.setAlternativeText(BUNDLE.getString("image.notfound.poster")); //$NON-NLS-1$
    lblMoviePoster.enableLightbox();
    lblPosterSize = new JLabel(BUNDLE.getString("mediafiletype.poster")); //$NON-NLS-1$
    panelLeft.add(lblPosterSize);
    panelLeft.add(Box.createVerticalStrut(20));

    lblMovieFanart = new ImageLabel(false, false, true);
    lblMovieFanart.setDesiredAspectRatio(9 / 16f);
    panelLeft.add(lblMovieFanart);
    lblMovieFanart.setAlternativeText(BUNDLE.getString("image.notfound.fanart"));
    lblMovieFanart.enableLightbox();
    lblFanartSize = new JLabel(BUNDLE.getString("mediafiletype.fanart")); //$NON-NLS-1$
    panelLeft.add(lblFanartSize);

    panelTopRight = new JPanel();
    add(panelTopRight, "4, 2, fill, fill");
    panelTopRight.setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.UNRELATED_GAP_COLSPEC, FormFactory.MIN_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { RowSpec.decode("fill:default"), FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));
    lblMovieName = new JLabel("");
    panelTopRight.add(lblMovieName, "2, 1, 3, 1, fill, fill");
    TmmFontHelper.changeFont(lblMovieName, 1.33, Font.BOLD);

    panelTopRight.add(new JSeparator(), "2, 3, 3, 1");

    panelTop1 = new JPanel();
    panelTopRight.add(panelTop1, "2, 5, 3, 1, fill, fill");
    panelTop1.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(50dlu;min):grow"),
            FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(50dlu;min):grow"), },
        new RowSpec[] { FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

    JLabel lblYearT = new JLabel(BUNDLE.getString("metatag.year")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblYearT, Font.BOLD);
    panelTop1.add(lblYearT, "1, 1");

    lblYear = new JLabel("");
    panelTop1.add(lblYear, "3, 1");

    JLabel lblImdbIdT = new JLabel(BUNDLE.getString("metatag.imdb")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblImdbIdT, Font.BOLD);
    panelTop1.add(lblImdbIdT, "5, 1");

    lblImdbid = new JLabel("");
    panelTop1.add(lblImdbid, "7, 1");

    JLabel lblRunningTimeT = new JLabel(BUNDLE.getString("metatag.runtime")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblRunningTimeT, Font.BOLD);
    panelTop1.add(lblRunningTimeT, "1, 3");

    lblRunningTime = new JLabel("");
    panelTop1.add(lblRunningTime, "3, 3");

    JLabel lblTmdbIdT = new JLabel(BUNDLE.getString("metatag.tmdb")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblTmdbIdT, Font.BOLD);
    panelTop1.add(lblTmdbIdT, "5, 3");

    lblTmdbid = new JLabel("");
    panelTop1.add(lblTmdbid, "7, 3");

    JLabel lblGenresT = new JLabel(BUNDLE.getString("metatag.genre")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblGenresT, Font.BOLD);
    panelTop1.add(lblGenresT, "1, 5");

    lblGenres = new JLabel("");
    panelTop1.add(lblGenres, "3, 5, 5, 1");

    JSeparator separator = new JSeparator();
    panelTopRight.add(separator, "2, 7, 3, 1");

    JPanel panelRating = new JPanel();
    panelTopRight.add(panelRating, "2, 9, 3, 1");
    panelRating.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { RowSpec.decode("24px"), }));

    lblRating = new JLabel("");
    panelRating.add(lblRating, "3, 1, left, center");

    lblVoteCount = new JLabel("");
    panelRating.add(lblVoteCount, "5, 1, left, center");

    panelRatingStars = new StarRater(10, 1);
    panelRating.add(panelRatingStars, "1, 1, left, center");
    panelRatingStars.setEnabled(false);

    separator = new JSeparator();
    panelTopRight.add(separator, "2, 11, 3, 1");

    panelLogos = new MediaInformationLogosPanel();
    panelTopRight.add(panelLogos, "2, 13, 3, 1, left, default");

    separator = new JSeparator();
    panelTopRight.add(separator, "2, 15, 3, 1");

    JLabel lblTaglineT = new JLabel(BUNDLE.getString("metatag.tagline")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblTaglineT, Font.BOLD);
    panelTopRight.add(lblTaglineT, "2, 17");

    lblTagline = new JLabel();
    panelTopRight.add(lblTagline, "2, 19, 3, 1");

    JLabel lblPlotT = new JLabel(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblPlotT, Font.BOLD);
    panelTopRight.add(lblPlotT, "2, 21");

    scrollPane = new JScrollPane();
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBorder(null);
    panelTopRight.add(scrollPane, "2, 23, 3, 1, fill, fill");

    tpPlot = new JTextPane();
    scrollPane.setViewportView(tpPlot);
    tpPlot.setOpaque(false);
    tpPlot.setFocusable(false);
    tpPlot.setEditable(false);

    add(new JSeparator(), "4, 4");

    panelBottomRight = new MovieDetailsPanel(movieSelectionModel);
    add(panelBottomRight, "4, 6, fill, fill");

    // beansbinding init
    initDataBindings();

    // manual coded binding
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String property = propertyChangeEvent.getPropertyName();
        Object source = propertyChangeEvent.getSource();
        // react on selection of a movie and change of a movie
        if (source instanceof MovieSelectionModel || (source instanceof Movie && MEDIA_FILES.equals(property))) {
          Movie movie = null;
          if (source instanceof MovieSelectionModel) {
            movie = ((MovieSelectionModel) source).getSelectedMovie();
          }
          if (source instanceof Movie) {
            movie = (Movie) source;
          }

          if (movie != null) {
            setPoster(movie);
            setFanart(movie);
            panelLogos.setMediaInformationSource(movie);

            if (movie.isWatched()) {
              // lblUnwatched.setIcon(imageEmtpy);
            }
            else {
              // lblUnwatched.setIcon(imageUnwatched);
            }
          }
        }
        if ((source.getClass() == Movie.class && FANART.equals(property))) {
          Movie movie = (Movie) source;
          setFanart(movie);
        }
        if ((source.getClass() == Movie.class && POSTER.equals(property))) {
          Movie movie = (Movie) source;
          setPoster(movie);
        }
      }
    };

    movieSelectionModel.addPropertyChangeListener(propertyChangeListener);
  }

  private void setPoster(Movie movie) {
    lblMoviePoster.clearImage();
    lblMoviePoster.setImagePath(movie.getArtworkFilename(MediaFileType.POSTER));
    Dimension posterSize = movie.getArtworkDimension(MediaFileType.POSTER);
    if (posterSize.width > 0 && posterSize.height > 0) {
      lblPosterSize.setText(BUNDLE.getString("mediafiletype.poster") + " - " + posterSize.width + "x" + posterSize.height); //$NON-NLS-1$
    }
    else {
      lblPosterSize.setText(BUNDLE.getString("mediafiletype.poster")); //$NON-NLS-1$
    }
  }

  private void setFanart(Movie movie) {
    lblMovieFanart.clearImage();
    lblMovieFanart.setImagePath(movie.getArtworkFilename(MediaFileType.FANART));
    Dimension fanartSize = movie.getArtworkDimension(MediaFileType.FANART);
    if (fanartSize.width > 0 && fanartSize.height > 0) {
      lblFanartSize.setText(BUNDLE.getString("mediafiletype.fanart") + " - " + fanartSize.width + "x" + fanartSize.height); //$NON-NLS-1$
    }
    else {
      lblFanartSize.setText(BUNDLE.getString("mediafiletype.fanart")); //$NON-NLS-1$
    }
  }

  protected void initDataBindings() {
    BeanProperty<MovieSelectionModel, Float> movieSelectionModelBeanProperty_1 = BeanProperty.create("selectedMovie.rating");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, Float, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_1, lblRating, jLabelBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<StarRater, Float> starRaterBeanProperty = BeanProperty.create("rating");
    AutoBinding<MovieSelectionModel, Float, StarRater, Float> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_1, panelRatingStars, starRaterBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_14 = BeanProperty.create("selectedMovie.plot");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, String, JTextPane, String> autoBinding_15 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_14, tpOverview, jTextPaneBeanProperty);
    autoBinding_15.bind();
    //
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty_2 = BeanProperty.create("selectedMovie.votes");
    AutoBinding<MovieSelectionModel, Integer, JLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_2, lblVoteCount, jLabelBeanProperty);
    autoBinding_2.setConverter(new VoteCountConverter());
    autoBinding_2.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_8 = BeanProperty.create("selectedMovie.year");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_8, lblYear, jLabelBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_12 = BeanProperty.create("selectedMovie.imdbId");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_12, lblImdbid, jLabelBeanProperty);
    autoBinding_10.bind();
    //
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty_13 = BeanProperty.create("selectedMovie.runtime");
    AutoBinding<MovieSelectionModel, Integer, JLabel, String> autoBinding_14 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_13, lblRunningTime, jLabelBeanProperty);
    autoBinding_14.bind();
    //
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty_15 = BeanProperty.create("selectedMovie.tmdbId");
    AutoBinding<MovieSelectionModel, Integer, JLabel, String> autoBinding_16 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_15, lblTmdbid, jLabelBeanProperty);
    autoBinding_16.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_16 = BeanProperty.create("selectedMovie.genresAsString");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_17 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_16, lblGenres, jLabelBeanProperty);
    autoBinding_17.bind();
    //
    AutoBinding<MovieSelectionModel, String, JTextPane, String> autoBinding_18 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_14, tpPlot, jTextPaneBeanProperty);
    autoBinding_18.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_3 = BeanProperty.create("selectedMovie.tagline");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_3, lblTagline, jLabelBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_4 = BeanProperty.create("selectedMovie.title");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_4, lblMovieName, jLabelBeanProperty);
    autoBinding_5.bind();
  }
}
