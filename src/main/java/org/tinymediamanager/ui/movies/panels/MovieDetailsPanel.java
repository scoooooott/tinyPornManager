/*
 * Copyright 2012 - 2015 Manuel Laggner
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.movies.MovieSelectionModel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

/**
 * The Class MovieDetailsPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieDetailsPanel extends JPanel {
  private static final long           serialVersionUID = 6273970118830324299L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private final static Logger         LOGGER           = LoggerFactory.getLogger(MovieDetailsPanel.class);

  private MovieSelectionModel         movieSelectionModel;

  private JLabel                      lblOriginalTitle;
  private JLabel                      lblProduction;
  private JLabel                      lblCertification;
  private JLabel                      lblTags;
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

    setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.UNRELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC,
            new RowSpec(RowSpec.CENTER, Sizes.bounded(Sizes.MINIMUM, Sizes.constant("15px", false), Sizes.constant("50px", false)), 0),
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

    JLabel lblOriginalTitleT = new JLabel(BUNDLE.getString("metatag.originaltitle")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblOriginalTitleT, Font.BOLD);
    add(lblOriginalTitleT, "2, 2");

    lblOriginalTitle = new JLabel("");
    add(lblOriginalTitle, "4, 2");

    JLabel lblReleaseDateT = new JLabel(BUNDLE.getString("metatag.releasedate")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblReleaseDateT, Font.BOLD);
    add(lblReleaseDateT, "2, 4");
    lblReleaseDateT.setLabelFor(lblReleaseDate);

    lblReleaseDate = new JLabel("");
    add(lblReleaseDate, "4, 4");

    JLabel lblCertificationT = new JLabel(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblCertificationT, Font.BOLD);
    add(lblCertificationT, "2, 6");
    lblCertificationT.setLabelFor(lblCertification);

    lblCertification = new JLabel("");
    add(lblCertification, "4, 6");

    JLabel lblProductionT = new JLabel(BUNDLE.getString("metatag.production")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblProductionT, Font.BOLD);
    add(lblProductionT, "2, 8, default, top");
    lblProductionT.setLabelFor(lblProduction);

    lblProduction = new JLabel();
    add(lblProduction, "4, 8");

    JLabel lblCountryT = new JLabel(BUNDLE.getString("metatag.country")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblCountryT, Font.BOLD);
    add(lblCountryT, "2, 10");
    lblCountryT.setLabelFor(lblCountry);

    lblCountry = new JLabel("");
    add(lblCountry, "4, 10");

    JLabel lblSpokenLanguagesT = new JLabel(BUNDLE.getString("metatag.spokenlanguages")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblSpokenLanguagesT, Font.BOLD);
    add(lblSpokenLanguagesT, "2, 12");
    lblSpokenLanguagesT.setLabelFor(lblSpokenLanguages);

    lblSpokenLanguages = new JLabel("");
    add(lblSpokenLanguages, "4, 12");

    JLabel lblMoviesetT = new JLabel(BUNDLE.getString("metatag.movieset")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblMoviesetT, Font.BOLD);
    add(lblMoviesetT, "2, 14");
    lblMoviesetT.setLabelFor(lblMovieSet);

    lblMovieSet = new JLabel("");
    add(lblMovieSet, "4, 14");

    JLabel lblTagsT = new JLabel(BUNDLE.getString("metatag.tags")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblTagsT, Font.BOLD);
    add(lblTagsT, "2, 16");
    lblTagsT.setLabelFor(lblTags);

    lblTags = new JLabel("");
    add(lblTags, "4, 16");

    JLabel lblMoviePathT = new JLabel(BUNDLE.getString("metatag.path")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblMoviePathT, Font.BOLD);
    add(lblMoviePathT, "2, 18, default, top");
    lblMoviePathT.setLabelFor(lblMoviePath);

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
            MessageManager.instance
                .pushMessage(new Message(MessageLevel.ERROR, path, "message.erroropenfolder", new String[] { ":", ex.getLocalizedMessage() }));
          }
        }
      }
    });
    lblMoviePathT.setLabelFor(lblMoviePath);
    lblMoviePathT.setLabelFor(lblMoviePath);
    add(lblMoviePath, "2, 20, 3, 1");

    initDataBindings();
  }

  protected void initDataBindings() {
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_6 = BeanProperty.create("selectedMovie.originalTitle");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_6, lblOriginalTitle, jLabelBeanProperty);
    autoBinding_7.bind();
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
  }
}
