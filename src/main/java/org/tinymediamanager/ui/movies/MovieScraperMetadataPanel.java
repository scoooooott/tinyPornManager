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
package org.tinymediamanager.ui.movies;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieScraperMetadataPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieScraperMetadataPanel extends JPanel {
  private static final long           serialVersionUID = 1053348917399322570L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieScraperMetadataConfig  config;

  /**
   * UI Elements
   */
  private JCheckBox                   chckbxTitle;
  private JCheckBox                   chckbxOriginalTitle;
  private JCheckBox                   chckbxTagline;
  private JCheckBox                   chckbxPlot;
  private JCheckBox                   chckbxRating;
  private JCheckBox                   chckbxRuntime;
  private JCheckBox                   chckbxYear;
  private JCheckBox                   chckbxCertification;
  private JCheckBox                   chckbxCast;
  private JCheckBox                   chckbxGenres;
  private JCheckBox                   chckbxArtwork;
  private JCheckBox                   chckbxTrailer;
  private JCheckBox                   chckbxCollection;
  private JCheckBox                   chckbxTags;
  private JLabel                      lblMovieSetHint;
  private JPanel                      panelSelectButtons;

  /**
   * Instantiates a new movie scraper metadata panel.
   * 
   * @param config
   *          the config
   */
  public MovieScraperMetadataPanel(MovieScraperMetadataConfig config) {
    this.config = config;
    initComponents();
  }

  private void initComponents() {
    setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, ColumnSpec.decode("15dlu"), FormSpecs.DEFAULT_COLSPEC,
            ColumnSpec.decode("15dlu"), FormSpecs.DEFAULT_COLSPEC, ColumnSpec.decode("15dlu"), FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, }));

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

    JPanel panelMovieSet = new JPanel();
    panelMovieSet.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    chckbxCollection = new JCheckBox(BUNDLE.getString("metatag.movieset")); //$NON-NLS-1$
    panelMovieSet.add(chckbxCollection);

    lblMovieSetHint = new JLabel(IconManager.HINT);
    lblMovieSetHint.setToolTipText(BUNDLE.getString("Settings.movieset.scraper.hint")); //$NON-NLS-1$
    panelMovieSet.add(lblMovieSetHint);
    add(panelMovieSet, "2, 8");

    chckbxTags = new JCheckBox(BUNDLE.getString("metatag.tags")); //$NON-NLS-1$
    add(chckbxTags, "4, 8");

    panelSelectButtons = new JPanel();
    add(panelSelectButtons, "2, 10, 3, 1, fill, fill");
    panelSelectButtons.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

    JButton btnSelectAll = new JButton(IconManager.CHECK_ALL);
    btnSelectAll.setToolTipText(BUNDLE.getString("Button.select.all")); //$NON-NLS-1$
    btnSelectAll.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setCheckBoxState(true);
      }
    });
    panelSelectButtons.add(btnSelectAll);

    JButton btnDeSelectAll = new JButton(IconManager.UNCHECK_ALL);
    btnDeSelectAll.setToolTipText(BUNDLE.getString("Button.select.none")); //$NON-NLS-1$
    btnDeSelectAll.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setCheckBoxState(false);
      }
    });
    panelSelectButtons.add(btnDeSelectAll);

    initDataBindings();
  }

  private void setCheckBoxState(boolean state) {
    for (JCheckBox checkBox : getAllCheckBoxes(MovieScraperMetadataPanel.this)) {
      checkBox.setSelected(state);
    }
  }

  private List<JCheckBox> getAllCheckBoxes(final Container container) {
    Component[] comps = container.getComponents();
    List<JCheckBox> compList = new ArrayList<>();
    for (Component comp : comps) {
      if (comp instanceof JCheckBox) {
        compList.add((JCheckBox) comp);
      }
      if (comp instanceof Container) {
        compList.addAll(getAllCheckBoxes((Container) comp));
      }
    }
    return compList;
  }

  protected void initDataBindings() {
    BeanProperty<MovieScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty = BeanProperty.create("title");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty, chckbxTitle, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_1 = BeanProperty.create("originalTitle");
    AutoBinding<MovieScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_1, chckbxOriginalTitle, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<MovieScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_2 = BeanProperty.create("tagline");
    AutoBinding<MovieScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_2, chckbxTagline, jCheckBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<MovieScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_3 = BeanProperty.create("plot");
    AutoBinding<MovieScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_3, chckbxPlot, jCheckBoxBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<MovieScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_4 = BeanProperty.create("rating");
    AutoBinding<MovieScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_4, chckbxRating, jCheckBoxBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<MovieScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_5 = BeanProperty.create("runtime");
    AutoBinding<MovieScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_5, chckbxRuntime, jCheckBoxBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<MovieScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_6 = BeanProperty.create("year");
    AutoBinding<MovieScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_6, chckbxYear, jCheckBoxBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<MovieScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_7 = BeanProperty.create("certification");
    AutoBinding<MovieScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_7, chckbxCertification, jCheckBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<MovieScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_8 = BeanProperty.create("cast");
    AutoBinding<MovieScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_8, chckbxCast, jCheckBoxBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<MovieScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_9 = BeanProperty.create("genres");
    AutoBinding<MovieScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty_9, chckbxGenres, jCheckBoxBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<MovieScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_10 = BeanProperty.create("artwork");
    AutoBinding<MovieScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, scraperMetadataConfigBeanProperty_10, chckbxArtwork, jCheckBoxBeanProperty);
    autoBinding_10.bind();
    //
    BeanProperty<MovieScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_11 = BeanProperty.create("trailer");
    AutoBinding<MovieScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, scraperMetadataConfigBeanProperty_11, chckbxTrailer, jCheckBoxBeanProperty);
    autoBinding_11.bind();
    //
    BeanProperty<MovieScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty_12 = BeanProperty.create("collection");
    AutoBinding<MovieScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, scraperMetadataConfigBeanProperty_12, chckbxCollection, jCheckBoxBeanProperty);
    autoBinding_12.bind();
    //
    BeanProperty<MovieScraperMetadataConfig, Boolean> movieScraperMetadataConfigBeanProperty = BeanProperty.create("tags");
    AutoBinding<MovieScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_13 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, movieScraperMetadataConfigBeanProperty, chckbxTags, jCheckBoxBeanProperty);
    autoBinding_13.bind();
  }
}
