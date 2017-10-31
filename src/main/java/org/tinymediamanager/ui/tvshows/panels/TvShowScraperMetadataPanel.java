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
package org.tinymediamanager.ui.tvshows.panels;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;

import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowScraperMetadataPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowScraperMetadataPanel extends JPanel {
  private static final long           serialVersionUID = 2417066912659769559L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private TvShowScraperMetadataConfig config;

  private JCheckBox                   chckbxTitle;
  private JCheckBox                   chckbxPlot;
  private JCheckBox                   chckbxRating;
  private JCheckBox                   chckbxRuntime;
  private JCheckBox                   chckbxYear;
  private JCheckBox                   chckbxCertification;
  private JCheckBox                   chckbxCast;
  private JCheckBox                   chckbxGenres;
  private JCheckBox                   chckbxArtwork;
  private JCheckBox                   chckbxScrapeEpisodes;
  private JCheckBox                   chckbxAired;
  private JCheckBox                   chckbxStatus;

  /**
   * Instantiates a new tv show scraper metadata panel.
   * 
   * @param config
   *          the config
   */
  public TvShowScraperMetadataPanel(TvShowScraperMetadataConfig config) {
    this.config = config;
    initComponents();
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[][20lp:n][][20lp:n][][20lp:n][]", "[][][][][]"));

    chckbxTitle = new JCheckBox(BUNDLE.getString("metatag.title")); //$NON-NLS-1$
    add(chckbxTitle, "cell 0 0");

    chckbxPlot = new JCheckBox(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
    add(chckbxPlot, "cell 2 0");

    chckbxRating = new JCheckBox(BUNDLE.getString("metatag.rating")); //$NON-NLS-1$
    add(chckbxRating, "cell 4 0");

    chckbxRuntime = new JCheckBox(BUNDLE.getString("metatag.runtime")); //$NON-NLS-1$
    add(chckbxRuntime, "cell 6 0");

    chckbxYear = new JCheckBox(BUNDLE.getString("metatag.year")); //$NON-NLS-1$
    add(chckbxYear, "cell 0 1");

    chckbxAired = new JCheckBox(BUNDLE.getString("metatag.aired")); //$NON-NLS-1$
    add(chckbxAired, "cell 2 1");

    chckbxStatus = new JCheckBox(BUNDLE.getString("metatag.status")); //$NON-NLS-1$
    add(chckbxStatus, "cell 4 1");

    chckbxCertification = new JCheckBox(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
    add(chckbxCertification, "cell 6 1");

    chckbxCast = new JCheckBox(BUNDLE.getString("metatag.cast")); //$NON-NLS-1$
    add(chckbxCast, "cell 0 2");

    chckbxGenres = new JCheckBox(BUNDLE.getString("metatag.genre")); //$NON-NLS-1$
    add(chckbxGenres, "cell 2 2");

    chckbxArtwork = new JCheckBox(BUNDLE.getString("metatag.artwork")); //$NON-NLS-1$
    add(chckbxArtwork, "cell 4 2");

    chckbxScrapeEpisodes = new JCheckBox(BUNDLE.getString("tvshow.scrapeepisodeseasondata")); //$NON-NLS-1$
    add(chckbxScrapeEpisodes, "cell 0 3 7 1,aligny top");

    JButton btnSelectAll = new JButton(IconManager.CHECK_ALL);
    add(btnSelectAll, "flowx,cell 0 4 7 1");
    btnSelectAll.setToolTipText(BUNDLE.getString("Button.select.all")); //$NON-NLS-1$
    btnSelectAll.addActionListener(e -> setCheckBoxState(true));

    initDataBindings();

    JButton btnDeSelectAll = new JButton(IconManager.UNCHECK_ALL);
    add(btnDeSelectAll, "cell 0 4");
    btnDeSelectAll.setToolTipText(BUNDLE.getString("Button.select.none")); //$NON-NLS-1$
    btnDeSelectAll.addActionListener(e -> setCheckBoxState(false));
  }

  private void setCheckBoxState(boolean state) {
    for (JCheckBox checkBox : getAllCheckBoxes(TvShowScraperMetadataPanel.this)) {
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
    BeanProperty<TvShowScraperMetadataConfig, Boolean> scraperMetadataConfigBeanProperty = BeanProperty.create("title");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<TvShowScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, config,
        scraperMetadataConfigBeanProperty, chckbxTitle, jCheckBoxBeanProperty);
    autoBinding.bind();
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
    BeanProperty<TvShowScraperMetadataConfig, Boolean> tvShowScraperMetadataConfigBeanProperty = BeanProperty.create("episodes");
    AutoBinding<TvShowScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, tvShowScraperMetadataConfigBeanProperty, chckbxScrapeEpisodes, jCheckBoxBeanProperty);
    autoBinding_12.bind();
    //
    BeanProperty<TvShowScraperMetadataConfig, Boolean> tvShowScraperMetadataConfigBeanProperty_1 = BeanProperty.create("aired");
    AutoBinding<TvShowScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, tvShowScraperMetadataConfigBeanProperty_1, chckbxAired, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<TvShowScraperMetadataConfig, Boolean> tvShowScraperMetadataConfigBeanProperty_2 = BeanProperty.create("status");
    AutoBinding<TvShowScraperMetadataConfig, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        config, tvShowScraperMetadataConfigBeanProperty_2, chckbxStatus, jCheckBoxBeanProperty);
    autoBinding_2.bind();
  }
}
