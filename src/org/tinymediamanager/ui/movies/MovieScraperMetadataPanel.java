/*
 * Copyright 2012 Manuel Laggner
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

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.ScraperMetadataConfig;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

// TODO: Auto-generated Javadoc
/**
 * The Class MovieScraperMetadataPanel.
 */
public class MovieScraperMetadataPanel extends JPanel {

  /** The config. */
  private ScraperMetadataConfig config;

  /** The chckbx title. */
  private JCheckBox             chckbxTitle;

  /** The chckbx original title. */
  private JCheckBox             chckbxOriginalTitle;

  /** The chckbx tagline. */
  private JCheckBox             chckbxTagline;

  /** The chckbx plot. */
  private JCheckBox             chckbxPlot;

  /** The chckbx rating. */
  private JCheckBox             chckbxRating;

  /** The chckbx runtime. */
  private JCheckBox             chckbxRuntime;

  /** The chckbx year. */
  private JCheckBox             chckbxYear;

  /** The chckbx certification. */
  private JCheckBox             chckbxCertification;

  /** The chckbx cast. */
  private JCheckBox             chckbxCast;

  /** The chckbx genres. */
  private JCheckBox             chckbxGenres;

  /** The chckbx artwork. */
  private JCheckBox             chckbxArtwork;

  /** The chckbx trailer. */
  private JCheckBox             chckbxTrailer;

  /**
   * Instantiates a new movie scraper metadata panel.
   * 
   * @param config
   *          the config
   */
  public MovieScraperMetadataPanel(ScraperMetadataConfig config) {
    this.config = config;
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    chckbxTitle = new JCheckBox("Title");
    add(chckbxTitle, "2, 2");

    chckbxOriginalTitle = new JCheckBox("Original title");
    add(chckbxOriginalTitle, "4, 2");

    chckbxTagline = new JCheckBox("Tagline");
    add(chckbxTagline, "6, 2");

    chckbxPlot = new JCheckBox("Plot");
    add(chckbxPlot, "8, 2");

    chckbxRating = new JCheckBox("Rating");
    add(chckbxRating, "2, 4");

    chckbxRuntime = new JCheckBox("Runtime");
    add(chckbxRuntime, "4, 4");

    chckbxYear = new JCheckBox("Year");
    add(chckbxYear, "6, 4");

    chckbxCertification = new JCheckBox("Certification");
    add(chckbxCertification, "8, 4");

    chckbxCast = new JCheckBox("Cast");
    add(chckbxCast, "2, 6");

    chckbxGenres = new JCheckBox("Genres");
    add(chckbxGenres, "4, 6");

    chckbxArtwork = new JCheckBox("Artwork");
    add(chckbxArtwork, "6, 6");

    chckbxTrailer = new JCheckBox("Trailer");
    add(chckbxTrailer, "8, 6");

    initDataBindings();
  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    BeanProperty<ScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty = BeanProperty.create("title");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<ScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty, chckbxTitle, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<ScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_1 = BeanProperty.create("originalTitle");
    AutoBinding<ScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_1, chckbxOriginalTitle, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<ScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_2 = BeanProperty.create("tagline");
    AutoBinding<ScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_2, chckbxTagline, jCheckBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<ScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_3 = BeanProperty.create("plot");
    AutoBinding<ScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_3, chckbxPlot, jCheckBoxBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<ScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_4 = BeanProperty.create("rating");
    AutoBinding<ScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_4, chckbxRating, jCheckBoxBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<ScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_5 = BeanProperty.create("runtime");
    AutoBinding<ScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_5, chckbxRuntime, jCheckBoxBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<ScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_6 = BeanProperty.create("year");
    AutoBinding<ScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_6, chckbxYear, jCheckBoxBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<ScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_7 = BeanProperty.create("certification");
    AutoBinding<ScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_7, chckbxCertification, jCheckBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<ScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_8 = BeanProperty.create("cast");
    AutoBinding<ScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_8, chckbxCast, jCheckBoxBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<ScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_9 = BeanProperty.create("genres");
    AutoBinding<ScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_9, chckbxGenres, jCheckBoxBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<ScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_10 = BeanProperty.create("artwork");
    AutoBinding<ScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_10, chckbxArtwork, jCheckBoxBeanProperty);
    autoBinding_10.bind();
    //
    BeanProperty<ScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_11 = BeanProperty.create("trailer");
    AutoBinding<ScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_11, chckbxTrailer, jCheckBoxBeanProperty);
    autoBinding_11.bind();
  }
}
