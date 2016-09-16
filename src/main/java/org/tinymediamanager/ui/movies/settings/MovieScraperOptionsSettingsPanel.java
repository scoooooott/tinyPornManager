/*
 * Copyright 2012 - 2016 Manuel Laggner
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
package org.tinymediamanager.ui.movies.settings;

import java.awt.Font;
import java.util.Hashtable;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextPane;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.movies.MovieScraperMetadataPanel;

import net.miginfocom.swing.MigLayout;

/**
 * The class MovieScraperOptionsPanel is used to maintain options for the Scrapers
 * 
 * @author Manuel Laggner
 */
public class MovieScraperOptionsSettingsPanel extends JPanel {
  private static final long           serialVersionUID = 2878589950137166661L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieSettings               settings         = MovieModuleManager.MOVIE_SETTINGS;
  private JSlider                     sliderThreshold;
  private JCheckBox                   chckbxAutomaticallyScrapeImages;
  private JCheckBox                   chckbxImageLanguage;

  public MovieScraperOptionsSettingsPanel() {
    // UI init
    initComponents();
    initDataBindings();

    // data init
    Hashtable<Integer, JLabel> labelTable = new java.util.Hashtable<>();
    labelTable.put(100, new JLabel("1.0"));
    labelTable.put(75, new JLabel("0.75"));
    labelTable.put(50, new JLabel("0.50"));
    labelTable.put(25, new JLabel("0.25"));
    labelTable.put(0, new JLabel("0.0"));
    sliderThreshold.setLabelTable(labelTable);
    sliderThreshold.setValue((int) (settings.getScraperThreshold() * 100));
    sliderThreshold.addChangeListener(arg0 -> settings.setScraperThreshold(sliderThreshold.getValue() / 100.0));
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp][25lp][400lp][]", "[][][20lp][][][][20lp][][][]"));
    {
      final JLabel lblScraperOptionsT = new JLabel(BUNDLE.getString("scraper.metadata.defaults")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblScraperOptionsT, 1.16667, Font.BOLD);
      add(lblScraperOptionsT, "cell 0 0 3 1");
    }
    {
      final JPanel panelScraperMetadata = new MovieScraperMetadataPanel(Settings.getInstance().getMovieScraperMetadataConfig());
      add(panelScraperMetadata, "cell 1 1 3 1,grow");
    }
    {
      final JLabel lblArtworkScrapeT = new JLabel(BUNDLE.getString("Settings.images")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblArtworkScrapeT, 1.16667, Font.BOLD);
      add(lblArtworkScrapeT, "cell 0 3 3 1");
    }
    {
      chckbxAutomaticallyScrapeImages = new JCheckBox(BUNDLE.getString("Settings.default.autoscrape"));
      add(chckbxAutomaticallyScrapeImages, "cell 1 4 2 1");
    }
    {
      chckbxImageLanguage = new JCheckBox(BUNDLE.getString("Settings.default.autoscrape.language"));
      add(chckbxImageLanguage, "cell 2 5");
    }
    {
      final JLabel lblAutomaticScrapeT = new JLabel(BUNDLE.getString("Settings.automaticscraper")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblAutomaticScrapeT, 1.16667, Font.BOLD);
      add(lblAutomaticScrapeT, "cell 0 7 3 1");
    }
    {
      final JLabel lblScraperThreshold = new JLabel(BUNDLE.getString("Settings.scraperTreshold")); //$NON-NLS-1$
      add(lblScraperThreshold, "flowx,cell 1 8 2 1,aligny center");

      sliderThreshold = new JSlider();
      sliderThreshold.setMinorTickSpacing(5);
      sliderThreshold.setMajorTickSpacing(10);
      sliderThreshold.setPaintTicks(true);
      sliderThreshold.setPaintLabels(true);
      add(sliderThreshold, "cell 1 8 2 1,growx");

      final JTextPane tpScraperThresholdHint = new JTextPane();
      tpScraperThresholdHint.setOpaque(false);
      TmmFontHelper.changeFont(tpScraperThresholdHint, 0.833);
      tpScraperThresholdHint.setText(BUNDLE.getString("Settings.scraperTreshold.hint")); //$NON-NLS-1$
      add(tpScraperThresholdHint, "cell 1 9 3 1");
    }
  }

  protected void initDataBindings() {
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty = BeanProperty.create("scrapeBestImage");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxAutomaticallyScrapeImages, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty_1 = BeanProperty.create("enabled");
    AutoBinding<JCheckBox, Boolean, JCheckBox, Boolean> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ,
        chckbxAutomaticallyScrapeImages, jCheckBoxBeanProperty, chckbxImageLanguage, jCheckBoxBeanProperty_1);
    autoBinding_10.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_10 = BeanProperty.create("imageLanguagePriority");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_10, chckbxImageLanguage, jCheckBoxBeanProperty);
    autoBinding_11.bind();
  }
}
