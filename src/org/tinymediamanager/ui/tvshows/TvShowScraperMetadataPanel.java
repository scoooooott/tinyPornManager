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
package org.tinymediamanager.ui.tvshows;

import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowScraperMetadataPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowScraperMetadataPanel extends JPanel {

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = 2417066912659769559L;

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The config. */
  private TvShowScraperMetadataConfig config;

  /** The chckbx title. */
  private JCheckBox                   chckbxTitle;

  /** The chckbx original title. */
  private JCheckBox                   chckbxOriginalTitle;

  /** The chckbx tagline. */
  private JCheckBox                   chckbxTagline;

  /** The chckbx plot. */
  private JCheckBox                   chckbxPlot;

  /** The chckbx rating. */
  private JCheckBox                   chckbxRating;

  /** The chckbx runtime. */
  private JCheckBox                   chckbxRuntime;

  /** The chckbx year. */
  private JCheckBox                   chckbxYear;

  /** The chckbx certification. */
  private JCheckBox                   chckbxCertification;

  /** The chckbx cast. */
  private JCheckBox                   chckbxCast;

  /** The chckbx genres. */
  private JCheckBox                   chckbxGenres;

  /** The chckbx artwork. */
  private JCheckBox                   chckbxArtwork;

  /** The chckbx trailer. */
  private JCheckBox                   chckbxTrailer;
  private JCheckBox                   chckbxScrapeEpisodes;

  /**
   * Instantiates a new tv show scraper metadata panel.
   * 
   * @param config
   *          the config
   */
  public TvShowScraperMetadataPanel(TvShowScraperMetadataConfig config) {
    this.config = config;
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    chckbxTitle = new JCheckBox(BUNDLE.getString("metatag.title")); //$NON-NLS-1$
    add(chckbxTitle, "2, 2");

    chckbxOriginalTitle = new JCheckBox(BUNDLE.getString("metatag.originaltitle")); //$NON-NLS-1$
    add(chckbxOriginalTitle, "4, 2");

    chckbxTagline = new JCheckBox(BUNDLE.getString("metatag.tagline")); //$NON-NLS-1$
    add(chckbxTagline, "6, 2");

    chckbxPlot = new JCheckBox(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
    add(chckbxPlot, "8, 2");

    chckbxRating = new JCheckBox(BUNDLE.getString("metatag.rating")); //$NON-NLS-1$
    add(chckbxRating, "2, 4");

    chckbxRuntime = new JCheckBox(BUNDLE.getString("metatag.runtime")); //$NON-NLS-1$
    add(chckbxRuntime, "4, 4");

    chckbxYear = new JCheckBox(BUNDLE.getString("metatag.year")); //$NON-NLS-1$
    add(chckbxYear, "6, 4");

    chckbxCertification = new JCheckBox(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
    add(chckbxCertification, "8, 4");

    chckbxCast = new JCheckBox(BUNDLE.getString("metatag.cast")); //$NON-NLS-1$
    add(chckbxCast, "2, 6");

    chckbxGenres = new JCheckBox(BUNDLE.getString("metatag.genre")); //$NON-NLS-1$
    add(chckbxGenres, "4, 6");

    chckbxArtwork = new JCheckBox(BUNDLE.getString("metatag.artwork")); //$NON-NLS-1$
    add(chckbxArtwork, "6, 6");

    chckbxTrailer = new JCheckBox(BUNDLE.getString("metatag.trailer")); //$NON-NLS-1$
    add(chckbxTrailer, "8, 6");

    chckbxScrapeEpisodes = new JCheckBox("Scrape episodes with valid episode/season number too");
    add(chckbxScrapeEpisodes, "2, 8, 7, 1");

    initDataBindings();
  }

  protected void initDataBindings() {
    BeanProperty<TvShowScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty = BeanProperty.create("title");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<TvShowScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty, chckbxTitle, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<TvShowScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_1 = BeanProperty.create("originalTitle");
    AutoBinding<TvShowScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, scraperMetadataConfigBeanProperty_1, chckbxOriginalTitle, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<TvShowScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_2 = BeanProperty.create("tagline");
    AutoBinding<TvShowScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, scraperMetadataConfigBeanProperty_2, chckbxTagline, jCheckBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<TvShowScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_3 = BeanProperty.create("plot");
    AutoBinding<TvShowScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, scraperMetadataConfigBeanProperty_3, chckbxPlot, jCheckBoxBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<TvShowScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_4 = BeanProperty.create("rating");
    AutoBinding<TvShowScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, scraperMetadataConfigBeanProperty_4, chckbxRating, jCheckBoxBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<TvShowScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_5 = BeanProperty.create("runtime");
    AutoBinding<TvShowScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, scraperMetadataConfigBeanProperty_5, chckbxRuntime, jCheckBoxBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<TvShowScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_6 = BeanProperty.create("year");
    AutoBinding<TvShowScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, scraperMetadataConfigBeanProperty_6, chckbxYear, jCheckBoxBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<TvShowScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_7 = BeanProperty.create("certification");
    AutoBinding<TvShowScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, scraperMetadataConfigBeanProperty_7, chckbxCertification, jCheckBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<TvShowScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_8 = BeanProperty.create("cast");
    AutoBinding<TvShowScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, scraperMetadataConfigBeanProperty_8, chckbxCast, jCheckBoxBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<TvShowScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_9 = BeanProperty.create("genres");
    AutoBinding<TvShowScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, scraperMetadataConfigBeanProperty_9, chckbxGenres, jCheckBoxBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<TvShowScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_10 = BeanProperty.create("artwork");
    AutoBinding<TvShowScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, scraperMetadataConfigBeanProperty_10, chckbxArtwork, jCheckBoxBeanProperty);
    autoBinding_10.bind();
    //
    BeanProperty<TvShowScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_11 = BeanProperty.create("trailer");
    AutoBinding<TvShowScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, scraperMetadataConfigBeanProperty_11, chckbxTrailer, jCheckBoxBeanProperty);
    autoBinding_11.bind();
    //
    BeanProperty<TvShowScraperMetadataConfig, Boolean> tvShowScraperMetadataConfigBeanProperty = BeanProperty.create("episodes");
    AutoBinding<TvShowScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, tvShowScraperMetadataConfigBeanProperty, chckbxScrapeEpisodes, jCheckBoxBeanProperty);
    autoBinding_12.bind();
  }
}
