/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.LinkLabel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

/**
 * The Class MovieDetailsPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieDetailsPanel extends JPanel {

  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = 6273970118830324299L;

  /** The logger. */
  private final static Logger         LOGGER           = LoggerFactory.getLogger(MovieDetailsPanel.class);

  /** The movie selection model. */
  private MovieSelectionModel         movieSelectionModel;

  /** The lbl original title t. */
  private JLabel                      lblOriginalTitleT;

  /** The lbl original title. */
  private JLabel                      lblOriginalTitle;

  /** The lbl production t. */
  private JLabel                      lblProductionT;

  /** The lbl production. */
  private JLabel                      lblProduction;

  /** The lbl genres t. */
  private JLabel                      lblGenresT;

  /** The lbl genres. */
  private JLabel                      lblGenres;

  /** The lbl certification t. */
  private JLabel                      lblCertificationT;

  /** The lbl certification. */
  private JLabel                      lblCertification;

  /** The lbl imdb id t. */
  private JLabel                      lblImdbIdT;

  /** The lbl tmdb id t. */
  private JLabel                      lblTmdbIdT;

  /** The lbl imdb id. */
  private LinkLabel                   lblImdbId;

  /** The lbl tmdb id. */
  private LinkLabel                   lblTmdbId;

  /** The lbl runtime t. */
  private JLabel                      lblRuntimeT;

  /** The lbl runtime. */
  private JLabel                      lblRuntime;

  /** The lbl minutes. */
  private JLabel                      lblMinutes;

  /** The lbl tags t. */
  private JLabel                      lblTagsT;

  /** The lbl tags. */
  private JLabel                      lblTags;

  /** The lbl movie path t. */
  private JLabel                      lblMoviePathT;

  /** The lbl movie path. */
  private LinkLabel                   lblMoviePath;

  /** The lbl movieset t. */
  private JLabel                      lblMoviesetT;

  /** The lbl movie set. */
  private JLabel                      lblMovieSet;

  /** The lbl spoken languages t. */
  private JLabel                      lblSpokenLanguagesT;

  /** The lbl spoken languages. */
  private JLabel                      lblSpokenLanguages;
  private JButton                     btnPlay;
  private JLabel                      lblCountryT;
  private JLabel                      lblCountry;
  private JLabel                      lblReleaseDateT;
  private JLabel                      lblReleaseDate;

  /**
   * Instantiates a new movie details panel.
   * 
   * @param model
   *          the model
   */
  public MovieDetailsPanel(MovieSelectionModel model) {
    this.movieSelectionModel = model;

    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.UNRELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC, FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("100px:grow"), FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC, FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("100px:grow"), FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("55px"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
        new RowSpec(RowSpec.CENTER, Sizes.bounded(Sizes.MINIMUM, Sizes.constant("15px", false), Sizes.constant("50px", false)), 0),
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    lblOriginalTitleT = new JLabel(BUNDLE.getString("metatag.originaltitle")); //$NON-NLS-1$
    add(lblOriginalTitleT, "2, 2");

    lblOriginalTitle = new JLabel("");
    add(lblOriginalTitle, "4, 2, 7, 1");

    btnPlay = new JButton("");
    btnPlay.setIcon(IconManager.PLAY);
    btnPlay.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        MediaFile mf = movieSelectionModel.getSelectedMovie().getMediaFiles(MediaFileType.VIDEO).get(0);
        try {
          TmmUIHelper.openFile(mf.getFile());
        }
        catch (Exception e) {
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, mf, "message.erroropenfile", new String[] { ":",
              e.getLocalizedMessage() }));
        }
      }
    });
    add(btnPlay, "12, 2, 1, 5");

    lblGenresT = new JLabel(BUNDLE.getString("metatag.genre")); //$NON-NLS-1$
    add(lblGenresT, "2, 4");
    lblGenresT.setLabelFor(lblGenres);

    lblGenres = new JLabel("");
    add(lblGenres, "4, 4, 7, 1");

    lblRuntimeT = new JLabel(BUNDLE.getString("metatag.runtime")); //$NON-NLS-1$
    add(lblRuntimeT, "2, 6");
    lblRuntimeT.setLabelFor(lblRuntime);

    lblRuntime = new JLabel("");
    add(lblRuntime, "4, 6");

    lblMinutes = new JLabel(BUNDLE.getString("metatag.minutes")); //$NON-NLS-1$
    add(lblMinutes, "6, 6");

    lblReleaseDateT = new JLabel(BUNDLE.getString("metatag.releasedate")); //$NON-NLS-1$
    add(lblReleaseDateT, "8, 6");

    lblReleaseDate = new JLabel("");
    add(lblReleaseDate, "10, 6");

    lblCertificationT = new JLabel(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
    add(lblCertificationT, "2, 8");
    lblCertificationT.setLabelFor(lblCertification);

    lblCertification = new JLabel("");
    add(lblCertification, "4, 8, 7, 1");

    lblProductionT = new JLabel(BUNDLE.getString("metatag.production")); //$NON-NLS-1$
    add(lblProductionT, "2, 10, default, top");
    lblProductionT.setLabelFor(lblProduction);

    lblProduction = new JLabel();
    add(lblProduction, "4, 10, 9, 1");

    lblSpokenLanguagesT = new JLabel(BUNDLE.getString("metatag.spokenlanguages")); //$NON-NLS-1$
    add(lblSpokenLanguagesT, "2, 12");

    lblSpokenLanguages = new JLabel("");
    add(lblSpokenLanguages, "4, 12, 3, 1");

    lblCountryT = new JLabel(BUNDLE.getString("metatag.country")); //$NON-NLS-1$
    add(lblCountryT, "8, 12");

    lblCountry = new JLabel("");
    add(lblCountry, "10, 12, 3, 1");

    lblMoviesetT = new JLabel(BUNDLE.getString("metatag.movieset")); //$NON-NLS-1$
    add(lblMoviesetT, "2, 14");

    lblMovieSet = new JLabel("");
    add(lblMovieSet, "4, 14, 9, 1");

    lblTagsT = new JLabel(BUNDLE.getString("metatag.tags")); //$NON-NLS-1$
    add(lblTagsT, "2, 16");

    lblTags = new JLabel("");
    add(lblTags, "4, 16, 9, 1");

    lblImdbIdT = new JLabel(BUNDLE.getString("metatag.imdb")); //$NON-NLS-1$
    add(lblImdbIdT, "2, 18");

    lblImdbId = new LinkLabel("");
    lblImdbId.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String url = "http://www.imdb.com/title/" + lblImdbId.getNormalText();
        try {
          TmmUIHelper.browseUrl(url);
        }
        catch (Exception e) {
          LOGGER.error("browse to imdbid", e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":",
              e.getLocalizedMessage() }));
        }
      }
    });

    add(lblImdbId, "4, 18, 3, 1, left, default");
    lblImdbIdT.setLabelFor(lblImdbId);

    lblTmdbIdT = new JLabel(BUNDLE.getString("metatag.tmdb")); //$NON-NLS-1$
    add(lblTmdbIdT, "8, 18");

    lblTmdbId = new LinkLabel("");
    lblTmdbId.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String url = "http://www.themoviedb.org/movie/" + lblTmdbId.getNormalText();
        try {
          TmmUIHelper.browseUrl(url);
        }
        catch (Exception e) {
          LOGGER.error("browse to tmdbid", e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":",
              e.getLocalizedMessage() }));
        }
      }
    });
    add(lblTmdbId, "10, 18, 3, 1, left, default");
    lblTmdbIdT.setLabelFor(lblTmdbId);

    lblMoviePathT = new JLabel(BUNDLE.getString("metatag.path")); //$NON-NLS-1$
    add(lblMoviePathT, "2, 20");

    lblMoviePath = new LinkLabel("");
    lblMoviePath.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (!StringUtils.isEmpty(lblMoviePath.getNormalText())) {
          // get the location from the label
          File path = new File(lblMoviePath.getNormalText());
          try {
            // check whether this location exists
            if (path.exists()) {
              TmmUIHelper.openFile(path);
            }
          }
          catch (Exception ex) {
            LOGGER.error("open filemanager", ex);
            MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, path, "message.erroropenfolder", new String[] { ":",
                ex.getLocalizedMessage() }));
          }
        }
      }
    });
    lblMoviePathT.setLabelFor(lblMoviePath);
    lblMoviePathT.setLabelFor(lblMoviePath);
    add(lblMoviePath, "4, 20, 9, 1");

    initDataBindings();
  }

  protected void initDataBindings() {
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_6 = BeanProperty.create("selectedMovie.originalTitle");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_6, lblOriginalTitle, jLabelBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_7 = BeanProperty.create("selectedMovie.genresAsString");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_7, lblGenres, jLabelBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty_8 = BeanProperty.create("selectedMovie.runtime");
    AutoBinding<MovieSelectionModel, Integer, JLabel, String> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_8, lblRuntime, jLabelBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_11 = BeanProperty.create("selectedMovie.imdbId");
    BeanProperty<LinkLabel, String> linkLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, String, LinkLabel, String> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_11, lblImdbId, linkLabelBeanProperty);
    autoBinding_12.bind();
    //
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty_12 = BeanProperty.create("selectedMovie.tmdbId");
    AutoBinding<MovieSelectionModel, Integer, LinkLabel, String> autoBinding_13 = Bindings.createAutoBinding(UpdateStrategy.READ,
        movieSelectionModel, movieSelectionModelBeanProperty_12, lblTmdbId, linkLabelBeanProperty);
    autoBinding_13.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_9 = BeanProperty.create("selectedMovie.certification.name");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_9, lblCertification, jLabelBeanProperty);
    autoBinding_10.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_4 = BeanProperty.create("selectedMovie.tagsAsString");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_4, lblTags, jLabelBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_5 = BeanProperty.create("selectedMovie.productionCompany");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_5, lblProduction, jLabelBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty = BeanProperty.create("selectedMovie.path");
    AutoBinding<MovieSelectionModel, String, LinkLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty, lblMoviePath, linkLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_1 = BeanProperty.create("selectedMovie.movieSetTitle");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_1, lblMovieSet, jLabelBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_2 = BeanProperty.create("selectedMovie.spokenLanguages");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_2, lblSpokenLanguages, jLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_3 = BeanProperty.create("selectedMovie.country");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_3, lblCountry, jLabelBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_10 = BeanProperty.create("selectedMovie.releaseDateAsString");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_10, lblReleaseDate, jLabelBeanProperty);
    autoBinding_6.bind();
  }
}
