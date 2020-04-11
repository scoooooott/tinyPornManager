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
package org.tinymediamanager.ui.movies.panels;

import static org.tinymediamanager.core.Constants.FANART;
import static org.tinymediamanager.core.Constants.MEDIA_FILES;
import static org.tinymediamanager.core.Constants.MEDIA_INFORMATION;
import static org.tinymediamanager.core.Constants.POSTER;

import java.awt.Cursor;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.ColumnLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.FlatButton;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.StarRater;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.converter.CertificationImageConverter;
import org.tinymediamanager.ui.converter.RatingConverter;
import org.tinymediamanager.ui.converter.RuntimeConverter;
import org.tinymediamanager.ui.converter.VoteCountConverter;
import org.tinymediamanager.ui.converter.ZeroIdConverter;
import org.tinymediamanager.ui.movies.MovieOtherIdsConverter;
import org.tinymediamanager.ui.movies.MovieSelectionModel;
import org.tinymediamanager.ui.panels.MediaInformationLogosPanel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieInformationPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieInformationPanel extends JPanel {
  private static final Logger         LOGGER           = LoggerFactory.getLogger(MovieInformationPanel.class);
  private static final long           serialVersionUID = -8527284262749511617L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private final MovieSelectionModel   movieSelectionModel;

  /** UI components */
  private StarRater                   starRater;
  private JLabel                      lblMovieName;
  private JLabel                      lblRating;
  private JLabel                      lblVoteCount;
  private JLabel                      lblTagline;
  private JLabel                      lblYear;
  private LinkLabel                   lblImdbid;
  private JLabel                      lblRunningTime;
  private LinkLabel                   lblTmdbid;
  private JTextArea                   taGenres;
  private JTextArea                   taPlot;
  private ImageLabel                  lblMoviePoster;
  private JLabel                      lblPosterSize;
  private ImageLabel                  lblMovieFanart;
  private JLabel                      lblFanartSize;
  private JLabel                      lblCertification;
  private JTextArea                   taOtherIds;
  private MediaInformationLogosPanel  panelLogos;
  private JSeparator                  sepLogos;
  private JLabel                      lblOriginalTitle;
  private JButton                     btnPlay;
  private JScrollPane                 scrollPane;
  private JTextArea                   taProduction;
  private JTextArea                   taTags;
  private JLabel                      lblEdition;
  private LinkLabel                   lblMoviePath;
  private JLabel                      lblMovieSet;
  private JLabel                      lblSpokenLanguages;
  private JLabel                      lblCountry;
  private JLabel                      lblReleaseDate;
  private JTextArea                   taNote;
  private JLabel                      lblCertificationLogo;
  private LinkLabel                   lblTraktTvId;

  /**
   * Instantiates a new movie information panel.
   * 
   * @param movieSelectionModel
   *          the movie selection model
   */
  public MovieInformationPanel(MovieSelectionModel movieSelectionModel) {
    this.movieSelectionModel = movieSelectionModel;

    initComponents();

    // beansbinding init
    initDataBindings();

    // action listeners
    lblTmdbid.addActionListener(arg0 -> {
      String url = "https://www.themoviedb.org/movie/" + lblTmdbid.getText();
      try {
        TmmUIHelper.browseUrl(url);
      }
      catch (Exception e) {
        LOGGER.error("browse to tmdbid", e);
        MessageManager.instance
            .pushMessage(new Message(Message.MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":", e.getLocalizedMessage() }));
      }
    });

    lblImdbid.addActionListener(arg0 -> {
      String url = "https://www.imdb.com/title/" + lblImdbid.getText();
      try {
        TmmUIHelper.browseUrl(url);
      }
      catch (Exception e) {
        LOGGER.error("browse to imdbid", e);
        MessageManager.instance
            .pushMessage(new Message(Message.MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":", e.getLocalizedMessage() }));
      }
    });

    lblTraktTvId.addActionListener(arg0 -> {
      String url = "https://trakt.tv/movies/" + lblTraktTvId.getText();
      try {
        TmmUIHelper.browseUrl(url);
      }
      catch (Exception e) {
        LOGGER.error("browse to trakt.tv", e);
        MessageManager.instance
            .pushMessage(new Message(Message.MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":", e.getLocalizedMessage() }));
      }
    });

    lblMoviePath.addActionListener(arg0 -> {
      if (!StringUtils.isEmpty(lblMoviePath.getText())) {
        // get the location from the label
        Path path = Paths.get(lblMoviePath.getText());
        try {
          // check whether this location exists
          if (Files.exists(path)) {
            TmmUIHelper.openFile(path);
          }
        }
        catch (Exception ex) {
          LOGGER.error("open filemanager", ex);
          MessageManager.instance
              .pushMessage(new Message(Message.MessageLevel.ERROR, path, "message.erroropenfolder", new String[] { ":", ex.getLocalizedMessage() }));
        }
      }
    });

    // manual coded binding
    PropertyChangeListener propertyChangeListener = propertyChangeEvent -> {
      String property = propertyChangeEvent.getPropertyName();
      Object source = propertyChangeEvent.getSource();
      // react on selection of a movie and change of a movie

      if (source.getClass() != MovieSelectionModel.class) {
        return;
      }

      MovieSelectionModel selectionModel = (MovieSelectionModel) source;
      Movie movie = selectionModel.getSelectedMovie();

      if ("selectedMovie".equals(property) || POSTER.equals(property)) {
        setPoster(movie);
      }

      if ("selectedMovie".equals(property) || FANART.equals(property)) {
        setFanart(movie);
      }

      if ("selectedMovie".equals(property) || MEDIA_FILES.equals(property) || MEDIA_INFORMATION.equals(property)) {
        panelLogos.setMediaInformationSource(movie);
      }

      if ("selectedMovie".equals(property)) {
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
      }
    };

    movieSelectionModel.addPropertyChangeListener(propertyChangeListener);

    btnPlay.addActionListener(e -> {
      MediaFile mf = movieSelectionModel.getSelectedMovie().getMainVideoFile();
      if (StringUtils.isNotBlank(mf.getFilename())) {
        try {
          TmmUIHelper.openFile(mf.getFileAsPath());
        }
        catch (Exception ex) {
          LOGGER.error("open file", ex);
          MessageManager.instance
              .pushMessage(new Message(Message.MessageLevel.ERROR, mf, "message.erroropenfile", new String[] { ":", ex.getLocalizedMessage() }));
        }
      }
    });
  }

  private void initComponents() {
    putClientProperty("class", "roundedPanel");
    setLayout(new MigLayout("", "[100lp:100lp,grow][300lp:300lp,grow 250]", "[][grow]"));

    {
      JPanel panelLeft = new JPanel();
      panelLeft.setLayout(new ColumnLayout());
      add(panelLeft, "cell 0 0 1 2,grow");

      lblMoviePoster = new ImageLabel(false, false, true);
      lblMoviePoster.setDesiredAspectRatio(2 / 3f);
      lblMoviePoster.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      panelLeft.add(lblMoviePoster);

      lblMoviePoster.enableLightbox();
      lblPosterSize = new JLabel(BUNDLE.getString("mediafiletype.poster"));
      panelLeft.add(lblPosterSize);

      panelLeft.add(Box.createVerticalStrut(20));

      lblMovieFanart = new ImageLabel(false, false, true);
      lblMovieFanart.setDesiredAspectRatio(16 / 9f);
      lblMovieFanart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      panelLeft.add(lblMovieFanart);
      lblMovieFanart.enableLightbox();
      lblFanartSize = new JLabel(BUNDLE.getString("mediafiletype.fanart"));
      panelLeft.add(lblFanartSize);
    }
    {
      JPanel panelTitle = new JPanel();
      add(panelTitle, "cell 1 0,grow");
      panelTitle.setLayout(new MigLayout("insets 0 0 n n", "[grow][]", "[][][shrink 0]"));

      {
        lblMovieName = new TmmLabel("", 1.33);
        panelTitle.add(lblMovieName, "flowx,cell 0 0,wmin 0,growx");
      }
      {
        btnPlay = new FlatButton(IconManager.PLAY_LARGE);
        panelTitle.add(btnPlay, "cell 1 0 1 2,aligny top");
      }
      {
        lblOriginalTitle = new JLabel("");
        panelTitle.add(lblOriginalTitle, "cell 0 1,growx,wmin 0");
      }
      {
        panelTitle.add(new JSeparator(), "cell 0 2 2 1,growx");
      }
    }
    {
      JPanel panelRight = new JPanel();
      panelRight.setLayout(new MigLayout("insets n 0 n n, hidemode 2", "[100lp,grow]", "[shrink 0][][shrink 0][][][][][shrink 0][][grow,top][][]"));

      scrollPane = new JScrollPane(panelRight);
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      add(scrollPane, "cell 1 1,grow, wmin 0");

      {
        JPanel panelTopDetails = new JPanel();
        panelRight.add(panelTopDetails, "cell 0 0,grow");
        panelTopDetails.setLayout(new MigLayout("insets 0", "[][][40lp][][grow][]", "[]2lp[]2lp[grow]2lp[]2lp[]2lp[]2lp[]2lp[]"));

        {
          JLabel lblYearT = new TmmLabel(BUNDLE.getString("metatag.year"));
          panelTopDetails.add(lblYearT, "cell 0 0");

          lblYear = new JLabel("");
          panelTopDetails.add(lblYear, "cell 1 0,growx");
        }
        {
          JLabel lblImdbIdT = new TmmLabel(BUNDLE.getString("metatag.imdb"));
          panelTopDetails.add(lblImdbIdT, "cell 3 0");

          lblImdbid = new LinkLabel("");
          panelTopDetails.add(lblImdbid, "cell 4 0");
        }
        {
          lblCertificationLogo = new JLabel("");
          panelTopDetails.add(lblCertificationLogo, "cell 5 0 1 3, top");
        }
        {
          JLabel lblReleaseDateT = new TmmLabel(BUNDLE.getString("metatag.releasedate"));
          panelTopDetails.add(lblReleaseDateT, "cell 0 1");

          lblReleaseDate = new JLabel("");
          panelTopDetails.add(lblReleaseDate, "cell 1 1");
        }
        {
          JLabel lblTmdbIdT = new TmmLabel(BUNDLE.getString("metatag.tmdb"));
          panelTopDetails.add(lblTmdbIdT, "cell 3 1");

          lblTmdbid = new LinkLabel("");
          panelTopDetails.add(lblTmdbid, "cell 4 1");
        }
        {
          JLabel lblCertificationT = new TmmLabel(BUNDLE.getString("metatag.certification"));
          panelTopDetails.add(lblCertificationT, "cell 0 2");

          lblCertification = new JLabel("");
          panelTopDetails.add(lblCertification, "cell 1 2,growx");
        }
        {
          JLabel lblTraktTvIdT = new TmmLabel("Trakt.tv ID");
          panelTopDetails.add(lblTraktTvIdT, "cell 3 2");

          lblTraktTvId = new LinkLabel();
          panelTopDetails.add(lblTraktTvId, "cell 4 2");
        }
        {
          JLabel lblOtherIdsT = new TmmLabel(BUNDLE.getString("metatag.otherids"));
          panelTopDetails.add(lblOtherIdsT, "cell 3 3");

          taOtherIds = new ReadOnlyTextArea();
          panelTopDetails.add(taOtherIds, "cell 4 3 2 1,growx,wmin 0");
        }
        {
          JLabel lblRunningTimeT = new TmmLabel(BUNDLE.getString("metatag.runtime"));
          panelTopDetails.add(lblRunningTimeT, "cell 0 3,aligny top");

          lblRunningTime = new JLabel("");
          panelTopDetails.add(lblRunningTime, "cell 1 3,aligny top");
        }
        {
          JLabel lblGenresT = new TmmLabel(BUNDLE.getString("metatag.genre"));
          panelTopDetails.add(lblGenresT, "cell 0 4");

          taGenres = new ReadOnlyTextArea();
          panelTopDetails.add(taGenres, "cell 1 4 5 1,growx,wmin 0");
        }
        {
          JLabel lblProductionT = new TmmLabel(BUNDLE.getString("metatag.production"));
          panelTopDetails.add(lblProductionT, "cell 0 5");

          taProduction = new ReadOnlyTextArea();
          panelTopDetails.add(taProduction, "cell 1 5 5 1,growx,wmin 0");
        }
        {
          JLabel lblCountryT = new TmmLabel(BUNDLE.getString("metatag.country"));
          panelTopDetails.add(lblCountryT, "cell 0 6");

          lblCountry = new JLabel("");
          panelTopDetails.add(lblCountry, "cell 1 6 5 1,wmin 0");
        }
        {
          JLabel lblSpokenLanguagesT = new TmmLabel(BUNDLE.getString("metatag.spokenlanguages"));
          panelTopDetails.add(lblSpokenLanguagesT, "cell 0 7,wmin 0");

          lblSpokenLanguages = new JLabel("");
          panelTopDetails.add(lblSpokenLanguages, "cell 1 7 5 1");
        }
      }

      {
        panelRight.add(new JSeparator(), "cell 0 1,growx");
      }

      {
        starRater = new StarRater(10, 1);
        panelRight.add(starRater, "flowx,cell 0 2,aligny center");
        starRater.setEnabled(false);

        lblRating = new JLabel("");
        panelRight.add(lblRating, "cell 0 2,aligny center");

        lblVoteCount = new JLabel("");
        panelRight.add(lblVoteCount, "cell 0 2,aligny center");
      }

      {
        sepLogos = new JSeparator();
        panelRight.add(sepLogos, "cell 0 3,growx");
      }

      {
        panelLogos = new MediaInformationLogosPanel();
        panelRight.add(panelLogos, "cell 0 4,wmin 0");
      }

      {
        panelRight.add(new JSeparator(), "cell 0 5,growx");
      }

      {
        JLabel lblTaglineT = new TmmLabel(BUNDLE.getString("metatag.tagline"));
        panelRight.add(lblTaglineT, "cell 0 6,alignx left,aligny top");

        lblTagline = new JLabel();
        panelRight.add(lblTagline, "cell 0 7,growx,wmin 0,aligny top");
      }

      {
        JLabel lblPlotT = new TmmLabel(BUNDLE.getString("metatag.plot"));
        panelRight.add(lblPlotT, "cell 0 8,alignx left,aligny top");

        taPlot = new ReadOnlyTextArea();
        panelRight.add(taPlot, "cell 0 9,growx,wmin 0,aligny top");
      }
      {
        panelRight.add(new JSeparator(), "cell 0 10,growx");
      }
      {
        JPanel panelBottomDetails = new JPanel();
        panelRight.add(panelBottomDetails, "cell 0 11,grow");
        panelBottomDetails.setLayout(new MigLayout("insets 0", "[][200lp,grow]", "[]2lp[]2lp[]2lp[]2lp[]"));
        {
          JLabel lblMoviesetT = new TmmLabel(BUNDLE.getString("metatag.movieset"));
          panelBottomDetails.add(lblMoviesetT, "cell 0 0");

          lblMovieSet = new JLabel("");
          panelBottomDetails.add(lblMovieSet, "cell 1 0,growx,wmin 0");
        }
        {
          JLabel lblEditionT = new TmmLabel(BUNDLE.getString("metatag.edition"));
          panelBottomDetails.add(lblEditionT, "cell 0 1");

          lblEdition = new JLabel("");
          panelBottomDetails.add(lblEdition, "cell 1 1,growx,wmin 0");
        }
        {
          JLabel lblTagsT = new TmmLabel(BUNDLE.getString("metatag.tags"));
          panelBottomDetails.add(lblTagsT, "cell 0 2");

          taTags = new ReadOnlyTextArea();
          panelBottomDetails.add(taTags, "cell 1 2,growx,wmin 0");
        }
        {
          JLabel lblMoviePathT = new TmmLabel(BUNDLE.getString("metatag.path"));
          panelBottomDetails.add(lblMoviePathT, "cell 0 3");

          lblMoviePath = new LinkLabel("");
          panelBottomDetails.add(lblMoviePath, "cell 1 3,growx,wmin 0");
        }
        {
          JLabel lblNoteT = new TmmLabel(BUNDLE.getString("metatag.note"));
          panelBottomDetails.add(lblNoteT, "cell 0 4");

          taNote = new ReadOnlyTextArea();
          panelBottomDetails.add(taNote, "cell 1 4,growx,wmin 0");
        }
      }
    }
  }

  private void setPoster(Movie movie) {
    lblMoviePoster.clearImage();
    lblMoviePoster.setImagePath(movie.getArtworkFilename(MediaFileType.POSTER));
    Dimension posterSize = movie.getArtworkDimension(MediaFileType.POSTER);
    if (posterSize.width > 0 && posterSize.height > 0) {
      lblPosterSize.setText(BUNDLE.getString("mediafiletype.poster") + " - " + posterSize.width + "x" + posterSize.height);
    }
    else {
      lblPosterSize.setText(BUNDLE.getString("mediafiletype.poster"));
    }
  }

  private void setFanart(Movie movie) {
    lblMovieFanart.clearImage();
    lblMovieFanart.setImagePath(movie.getArtworkFilename(MediaFileType.FANART));
    Dimension fanartSize = movie.getArtworkDimension(MediaFileType.FANART);
    if (fanartSize.width > 0 && fanartSize.height > 0) {
      lblFanartSize.setText(BUNDLE.getString("mediafiletype.fanart") + " - " + fanartSize.width + "x" + fanartSize.height);
    }
    else {
      lblFanartSize.setText(BUNDLE.getString("mediafiletype.fanart"));
    }
  }

  protected void initDataBindings() {
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty_2 = BeanProperty.create("selectedMovie.rating.votes");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, Integer, JLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_2, lblVoteCount, jLabelBeanProperty);
    autoBinding_2.setConverter(new VoteCountConverter());
    autoBinding_2.bind();
    //
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty_8 = BeanProperty.create("selectedMovie.year");
    AutoBinding<MovieSelectionModel, Integer, JLabel, String> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_8, lblYear, jLabelBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_12 = BeanProperty.create("selectedMovie.imdbId");
    BeanProperty<JTextArea, String> jTextAreaBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, String, JTextArea, String> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_12, lblImdbid, jTextAreaBeanProperty);
    autoBinding_10.bind();
    //
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty_13 = BeanProperty.create("selectedMovie.runtime");
    AutoBinding<MovieSelectionModel, Integer, JLabel, String> autoBinding_14 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_13, lblRunningTime, jLabelBeanProperty);
    autoBinding_14.setConverter(new RuntimeConverter());
    autoBinding_14.bind();
    //
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty_15 = BeanProperty.create("selectedMovie.tmdbId");
    AutoBinding<MovieSelectionModel, Integer, JTextArea, String> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_15, lblTmdbid, jTextAreaBeanProperty);
    autoBinding_7.setConverter(new ZeroIdConverter());
    autoBinding_7.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_16 = BeanProperty.create("selectedMovie.genresAsString");
    AutoBinding<MovieSelectionModel, String, JTextArea, String> autoBinding_17 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_16, taGenres, jTextAreaBeanProperty);
    autoBinding_17.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_14 = BeanProperty.create("selectedMovie.plot");
    AutoBinding<MovieSelectionModel, String, JTextArea, String> autoBinding_18 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_14, taPlot, jTextAreaBeanProperty);
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
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty = BeanProperty.create("selectedMovie.certification.name");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty, lblCertification, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_6 = BeanProperty.create("selectedMovie.originalTitle");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_6, lblOriginalTitle, jLabelBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<MovieSelectionModel, Map<String, Object>> movieSelectionModelBeanProperty_5 = BeanProperty.create("selectedMovie.ids");
    AutoBinding<MovieSelectionModel, Map<String, Object>, JTextArea, String> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ,
        movieSelectionModel, movieSelectionModelBeanProperty_5, taOtherIds, jTextAreaBeanProperty);
    autoBinding_6.setConverter(new MovieOtherIdsConverter());
    autoBinding_6.bind();
    //
    BeanProperty<MovieSelectionModel, Float> movieSelectionModelBeanProperty_7 = BeanProperty.create("selectedMovie.rating.ratingNormalized");
    BeanProperty<StarRater, Float> starRaterBeanProperty = BeanProperty.create("rating");
    AutoBinding<MovieSelectionModel, Float, StarRater, Float> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_7, starRater, starRaterBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<MovieSelectionModel, Movie> movieSelectionModelBeanProperty_9 = BeanProperty.create("selectedMovie");
    BeanProperty<JLabel, String> jLabelBeanProperty_1 = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, Movie, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_9, lblRating, jLabelBeanProperty_1);
    autoBinding_1.setConverter(new RatingConverter<>());
    autoBinding_1.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_1 = BeanProperty.create("selectedMovie.releaseDateAsString");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_1, lblReleaseDate, jLabelBeanProperty);
    autoBinding_11.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_10 = BeanProperty.create("selectedMovie.productionCompany");
    AutoBinding<MovieSelectionModel, String, JTextArea, String> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_10, taProduction, jTextAreaBeanProperty);
    autoBinding_12.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_11 = BeanProperty.create("selectedMovie.country");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_13 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_11, lblCountry, jLabelBeanProperty);
    autoBinding_13.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_17 = BeanProperty.create("selectedMovie.spokenLanguages");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_15 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_17, lblSpokenLanguages, jLabelBeanProperty);
    autoBinding_15.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_18 = BeanProperty.create("selectedMovie.movieSetTitle");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_16 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_18, lblMovieSet, jLabelBeanProperty);
    autoBinding_16.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_19 = BeanProperty.create("selectedMovie.edition.title");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_19 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_19, lblEdition, jLabelBeanProperty);
    autoBinding_19.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_20 = BeanProperty.create("selectedMovie.tagsAsString");
    AutoBinding<MovieSelectionModel, String, JTextArea, String> autoBinding_20 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_20, taTags, jTextAreaBeanProperty);
    autoBinding_20.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_21 = BeanProperty.create("selectedMovie.path");
    BeanProperty<LinkLabel, String> linkLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, String, LinkLabel, String> autoBinding_21 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_21, lblMoviePath, linkLabelBeanProperty);
    autoBinding_21.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_22 = BeanProperty.create("selectedMovie.note");
    AutoBinding<MovieSelectionModel, String, JTextArea, String> autoBinding_22 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_22, taNote, jTextAreaBeanProperty);
    autoBinding_22.bind();
    //
    BeanProperty<MovieSelectionModel, MediaCertification> movieSelectionModelBeanProperty_23 = BeanProperty.create("selectedMovie.certification");
    BeanProperty<JLabel, Icon> jLabelBeanProperty_2 = BeanProperty.create("icon");
    AutoBinding<MovieSelectionModel, MediaCertification, JLabel, Icon> autoBinding_23 = Bindings.createAutoBinding(UpdateStrategy.READ,
        movieSelectionModel, movieSelectionModelBeanProperty_23, lblCertificationLogo, jLabelBeanProperty_2);
    autoBinding_23.setConverter(new CertificationImageConverter());
    autoBinding_23.bind();
    //
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty_24 = BeanProperty.create("selectedMovie.traktTvId");
    AutoBinding<MovieSelectionModel, Integer, JTextArea, String> autoBinding_24 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_24, lblTraktTvId, jTextAreaBeanProperty);
    autoBinding_24.setConverter(new ZeroIdConverter());
    autoBinding_24.bind();
  }
}
