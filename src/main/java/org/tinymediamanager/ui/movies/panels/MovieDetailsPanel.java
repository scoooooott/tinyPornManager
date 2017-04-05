/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import java.awt.Font;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.movie.MovieEdition;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.movies.MovieSelectionModel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieDetailsPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieDetailsPanel extends JPanel {
  private static final long           serialVersionUID = 6273970118830324299L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private final static Logger         LOGGER           = LoggerFactory.getLogger(MovieDetailsPanel.class);

  private MovieSelectionModel         movieSelectionModel;

  /** UI components */
  private JLabel                      lblOriginalTitle;
  private JLabel                      lblProduction;
  private JLabel                      lblTags;
  private JLabel                      lblEdition;
  private LinkLabel                   lblMoviePath;
  private JLabel                      lblMovieSet;
  private JLabel                      lblSpokenLanguages;
  private JLabel                      lblCountry;
  private JLabel                      lblReleaseDate;

  /**
   * Instantiates a new movie details panel.
   * 
   * @param model
   *          the model
   */
  public MovieDetailsPanel(MovieSelectionModel model) {
    this.movieSelectionModel = model;
    initComponents();

    initDataBindings();

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
              .pushMessage(new Message(MessageLevel.ERROR, path, "message.erroropenfolder", new String[] { ":", ex.getLocalizedMessage() }));
        }
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("insets 0", "[][10lp][grow]", "[]2lp[]2lp[]2lp[]2lp[]2lp[]2lp[]2lp[]2lp[]"));

    JLabel lblOriginalTitleT = new JLabel(BUNDLE.getString("metatag.originaltitle")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblOriginalTitleT, Font.BOLD);
    add(lblOriginalTitleT, "cell 0 0");

    lblOriginalTitle = new JLabel("");
    add(lblOriginalTitle, "cell 2 0,growx");

    JLabel lblReleaseDateT = new JLabel(BUNDLE.getString("metatag.releasedate")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblReleaseDateT, Font.BOLD);
    add(lblReleaseDateT, "cell 0 1");

    lblReleaseDate = new JLabel("");
    add(lblReleaseDate, "cell 2 1,growx");

    JLabel lblProductionT = new JLabel(BUNDLE.getString("metatag.production")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblProductionT, Font.BOLD);
    add(lblProductionT, "cell 0 2");

    lblProduction = new JLabel();
    add(lblProduction, "cell 2 2,growx");

    JLabel lblCountryT = new JLabel(BUNDLE.getString("metatag.country")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblCountryT, Font.BOLD);
    add(lblCountryT, "cell 0 3");

    lblCountry = new JLabel("");
    add(lblCountry, "cell 2 3,growx");

    JLabel lblSpokenLanguagesT = new JLabel(BUNDLE.getString("metatag.spokenlanguages")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblSpokenLanguagesT, Font.BOLD);
    add(lblSpokenLanguagesT, "cell 0 4");

    lblSpokenLanguages = new JLabel("");
    add(lblSpokenLanguages, "cell 2 4,growx");

    JLabel lblMoviesetT = new JLabel(BUNDLE.getString("metatag.movieset")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblMoviesetT, Font.BOLD);
    add(lblMoviesetT, "cell 0 5");

    lblMovieSet = new JLabel("");
    add(lblMovieSet, "cell 2 5,growx");

    JLabel lblEditionT = new JLabel(BUNDLE.getString("metatag.edition")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblEditionT, Font.BOLD);
    add(lblEditionT, "cell 0 6");

    lblEdition = new JLabel("");
    add(lblEdition, "cell 2 6,growx");

    JLabel lblTagsT = new JLabel(BUNDLE.getString("metatag.tags")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblTagsT, Font.BOLD);
    add(lblTagsT, "cell 0 7");

    lblTags = new JLabel("");
    add(lblTags, "cell 2 7,growx");

    JLabel lblMoviePathT = new JLabel(BUNDLE.getString("metatag.path")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblMoviePathT, Font.BOLD);
    add(lblMoviePathT, "cell 0 8");

    lblMoviePath = new LinkLabel("");
    add(lblMoviePath, "cell 2 8,growx");
  }

  protected void initDataBindings() {
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_6 = BeanProperty.create("selectedMovie.originalTitle");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_6, lblOriginalTitle, jLabelBeanProperty);
    autoBinding_7.bind();
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
    BeanProperty<LinkLabel, String> linkLabelBeanProperty = BeanProperty.create("text");
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
    //
    BeanProperty<MovieSelectionModel, MovieEdition> movieSelectionModelBeanProperty_9 = BeanProperty.create("selectedMovie.edition.title");
    AutoBinding<MovieSelectionModel, MovieEdition, JLabel, String> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ,
        movieSelectionModel, movieSelectionModelBeanProperty_9, lblEdition, jLabelBeanProperty);
    autoBinding_8.bind();
  }
}
