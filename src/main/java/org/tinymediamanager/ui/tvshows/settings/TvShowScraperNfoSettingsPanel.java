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
package org.tinymediamanager.ui.tvshows.settings;

import static org.tinymediamanager.ui.TmmFontHelper.H3;

import java.awt.event.ItemListener;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.DateField;
import org.tinymediamanager.core.CertificationStyle;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.core.tvshow.connector.TvShowConnectors;
import org.tinymediamanager.core.tvshow.filenaming.TvShowEpisodeNfoNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowNfoNaming;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.SettingsPanelFactory;
import org.tinymediamanager.ui.components.TmmLabel;

import net.miginfocom.swing.MigLayout;

/**
 * The class {@link TvShowScraperSettingsPanel} is used to display NFO related settings.
 * 
 * @author Manuel Laggner
 */
class TvShowScraperNfoSettingsPanel extends JPanel {
  private static final long                    serialVersionUID = 4999827736720726395L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle          BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private TvShowSettings                       settings         = TvShowModuleManager.SETTINGS;
  private JComboBox<TvShowConnectors>          cbNfoFormat;
  private JComboBox<CertificationStyleWrapper> cbCertificationStyle;
  private JCheckBox                            chckbxWriteCleanNfo;
  private JComboBox<MediaLanguages>            cbNfoLanguage;
  private JComboBox<DateField>                 cbDatefield;
  private JCheckBox                            chckbxEpisodeNfo1;
  private JCheckBox                            chckbxTvShowNfo1;

  private ItemListener                         checkBoxListener;
  private ItemListener                         comboBoxListener;

  /**
   * Instantiates a new movie scraper settings panel.
   */
  TvShowScraperNfoSettingsPanel() {
    checkBoxListener = e -> checkChanges();
    comboBoxListener = e -> checkChanges();

    // UI init
    initComponents();
    initDataBindings();

    // data init

    // implement checkBoxListener for preset events
    settings.addPropertyChangeListener(evt -> {
      if ("preset".equals(evt.getPropertyName())) {
        buildComboBoxes();
      }
    });

    buildCheckBoxes();
    buildComboBoxes();
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[grow]", "[]"));
    {
      JPanel panelNfo = SettingsPanelFactory.createSettingsPanel();

      JLabel lblNfoT = new TmmLabel(BUNDLE.getString("Settings.nfo"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelNfo, lblNfoT, true);
      add(collapsiblePanel, "cell 0 0,growx, wmin 0");
      {
        JLabel lblNfoFormatT = new JLabel(BUNDLE.getString("Settings.nfoFormat"));
        panelNfo.add(lblNfoFormatT, "cell 1 0 2 1");

        cbNfoFormat = new JComboBox(TvShowConnectors.values());
        panelNfo.add(cbNfoFormat, "cell 1 0");

        {
          JLabel lblNfoFileNaming = new JLabel(BUNDLE.getString("Settings.nofFileNaming"));
          panelNfo.add(lblNfoFileNaming, "cell 1 1 2 1");

          JPanel panel = new JPanel();
          panelNfo.add(panel, "cell 2 2");
          panel.setLayout(new MigLayout("insets 0", "[][]", ""));

          JLabel lblTvShow = new JLabel(BUNDLE.getString("metatag.tvshow"));
          panel.add(lblTvShow, "cell 0 0");

          chckbxTvShowNfo1 = new JCheckBox("tvshow.nfo");
          panel.add(chckbxTvShowNfo1, "cell 1 0");

          JLabel lblEpisode = new JLabel(BUNDLE.getString("metatag.episode"));
          panel.add(lblEpisode, "cell 0 1");

          chckbxEpisodeNfo1 = new JCheckBox(BUNDLE.getString("Settings.tvshow.episodename") + ".nfo");
          panel.add(chckbxEpisodeNfo1, "cell 1 1");
        }

        JLabel lblNfoDatefield = new JLabel(BUNDLE.getString("Settings.dateadded"));
        panelNfo.add(lblNfoDatefield, "cell 1 4 2 1");

        cbDatefield = new JComboBox(DateField.values());
        panelNfo.add(cbDatefield, "cell 1 4");

        JLabel lblNfoLanguage = new JLabel(BUNDLE.getString("Settings.nfolanguage"));
        panelNfo.add(lblNfoLanguage, "cell 1 5 2 1");

        cbNfoLanguage = new JComboBox(MediaLanguages.valuesSorted());
        panelNfo.add(cbNfoLanguage, "cell 1 5");

        JLabel lblNfoLanguageDesc = new JLabel(BUNDLE.getString("Settings.nfolanguage.desc"));
        panelNfo.add(lblNfoLanguageDesc, "cell 2 6");

        JLabel lblCertificationFormatT = new JLabel(BUNDLE.getString("Settings.certificationformat"));
        panelNfo.add(lblCertificationFormatT, "cell 1 7 2 1");

        cbCertificationStyle = new JComboBox();
        panelNfo.add(cbCertificationStyle, "cell 1 7");

        chckbxWriteCleanNfo = new JCheckBox(BUNDLE.getString("Settings.writecleannfo"));
        panelNfo.add(chckbxWriteCleanNfo, "cell 1 8 2 1");
      }
    }
  }

  /**
   * check changes of checkboxes
   */
  private void checkChanges() {
    CertificationStyleWrapper wrapper = (CertificationStyleWrapper) cbCertificationStyle.getSelectedItem();
    if (wrapper != null && settings.getCertificationStyle() != wrapper.style) {
      settings.setCertificationStyle(wrapper.style);
    }

    // set NFO filenames
    settings.clearNfoFilenames();
    if (chckbxTvShowNfo1.isSelected()) {
      settings.addNfoFilename(TvShowNfoNaming.TV_SHOW);
    }

    settings.clearEpisodeNfoFilenames();
    if (chckbxEpisodeNfo1.isSelected()) {
      settings.addEpisodeNfoFilename(TvShowEpisodeNfoNaming.FILENAME);
    }
  }

  private void buildComboBoxes() {
    cbCertificationStyle.removeItemListener(comboBoxListener);
    cbCertificationStyle.removeAllItems();

    // certification examples
    for (CertificationStyle style : CertificationStyle.values()) {
      CertificationStyleWrapper wrapper = new CertificationStyleWrapper();
      wrapper.style = style;
      cbCertificationStyle.addItem(wrapper);
      if (style == settings.getCertificationStyle()) {
        cbCertificationStyle.setSelectedItem(wrapper);
      }
    }

    cbCertificationStyle.addItemListener(comboBoxListener);
  }

  private void buildCheckBoxes() {
    chckbxTvShowNfo1.removeItemListener(checkBoxListener);
    chckbxEpisodeNfo1.removeItemListener(checkBoxListener);
    clearSelection(chckbxTvShowNfo1, chckbxEpisodeNfo1);

    // NFO filenames
    List<TvShowNfoNaming> tvShowNfoNamings = settings.getNfoFilenames();
    if (tvShowNfoNamings.contains(TvShowNfoNaming.TV_SHOW)) {
      chckbxTvShowNfo1.setSelected(true);
    }

    List<TvShowEpisodeNfoNaming> TvShowEpisodeNfoNamings = settings.getEpisodeNfoFilenames();
    if (TvShowEpisodeNfoNamings.contains(TvShowEpisodeNfoNaming.FILENAME)) {
      chckbxEpisodeNfo1.setSelected(true);
    }

    chckbxTvShowNfo1.addItemListener(checkBoxListener);
    chckbxEpisodeNfo1.addItemListener(checkBoxListener);
  }

  private void clearSelection(JCheckBox... checkBoxes) {
    for (JCheckBox checkBox : checkBoxes) {
      checkBox.setSelected(false);
    }
  }

  /*
   * helper for displaying the combobox with an example
   */
  private class CertificationStyleWrapper {
    private CertificationStyle style;

    @Override
    public String toString() {
      String bundleTag = BUNDLE.getString("Settings.certification." + style.name().toLowerCase(Locale.ROOT));
      return bundleTag.replace("{}", CertificationStyle.formatCertification(MediaCertification.DE_FSK16, style));
    }
  }

  protected void initDataBindings() {
    BeanProperty<TvShowSettings, TvShowConnectors> tvShowSettingsBeanProperty = BeanProperty.create("tvShowConnector");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<TvShowSettings, TvShowConnectors, JComboBox, Object> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty, cbNfoFormat, jComboBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<TvShowSettings, Boolean> tvShowSettingsBeanProperty_1 = BeanProperty.create("writeCleanNfo");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty_1, chckbxWriteCleanNfo, jCheckBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<TvShowSettings, MediaLanguages> tvShowSettingsBeanProperty_2 = BeanProperty.create("nfoLanguage");
    AutoBinding<TvShowSettings, MediaLanguages, JComboBox, Object> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty_2, cbNfoLanguage, jComboBoxBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<TvShowSettings, DateField> tvShowSettingsBeanProperty_3 = BeanProperty.create("nfoDateAddedField");
    AutoBinding<TvShowSettings, DateField, JComboBox, Object> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty_3, cbDatefield, jComboBoxBeanProperty);
    autoBinding_5.bind();
  }
}
