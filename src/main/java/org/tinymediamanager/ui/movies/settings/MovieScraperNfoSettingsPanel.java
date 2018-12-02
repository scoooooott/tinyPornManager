/*
 * Copyright 2012 - 2018 Manuel Laggner
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

import static org.tinymediamanager.ui.TmmFontHelper.H3;

import java.awt.event.ItemListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.CertificationStyle;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.movie.connector.MovieConnectors;
import org.tinymediamanager.core.movie.filenaming.MovieNfoNaming;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.SettingsPanelFactory;
import org.tinymediamanager.ui.components.TmmLabel;

import net.miginfocom.swing.MigLayout;

/**
 * The class {@link MovieScraperSettingsPanel} is used to display NFO related settings.
 * 
 * @author Manuel Laggner
 */
class MovieScraperNfoSettingsPanel extends JPanel {
  private static final long                    serialVersionUID = -299825914193235308L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle          BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieSettings                        settings         = MovieModuleManager.SETTINGS;
  private JComboBox<MovieConnectors>           cbNfoFormat;
  private JCheckBox                            cbMovieNfoFilename1;
  private JCheckBox                            cbMovieNfoFilename2;
  private JCheckBox                            cbMovieNfoFilename3;
  private JComboBox<CertificationStyleWrapper> cbCertificationStyle;
  private JCheckBox                            chckbxWriteCleanNfo;
  private JComboBox<MediaLanguages>            cbNfoLanguage;

  private ItemListener                         checkBoxListener;
  private ItemListener                         comboBoxListener;

  /**
   * Instantiates a new movie scraper settings panel.
   */
  MovieScraperNfoSettingsPanel() {
    checkBoxListener = e -> checkChanges();
    comboBoxListener = e -> checkChanges();

    // UI init
    initComponents();
    initDataBindings();

    // data init
    // set default certification style when changing NFO style
    cbNfoFormat.addItemListener(e -> {
      if (cbNfoFormat.getSelectedItem() == MovieConnectors.MP) {
        for (int i = 0; i < cbCertificationStyle.getItemCount(); i++) {
          CertificationStyleWrapper wrapper = cbCertificationStyle.getItemAt(i);
          if (wrapper.style == CertificationStyle.TECHNICAL) {
            cbCertificationStyle.setSelectedItem(wrapper);
            break;
          }
        }
      }
      else if (cbNfoFormat.getSelectedItem() == MovieConnectors.XBMC) {
        for (int i = 0; i < cbCertificationStyle.getItemCount(); i++) {
          CertificationStyleWrapper wrapper = cbCertificationStyle.getItemAt(i);
          if (wrapper.style == CertificationStyle.LARGE) {
            cbCertificationStyle.setSelectedItem(wrapper);
            break;
          }
        }
      }
    });

    // implement checkBoxListener for preset events
    settings.addPropertyChangeListener(evt -> {
      if ("preset".equals(evt.getPropertyName())) {
        buildCheckBoxes();
        buildComboBoxes();
      }
    });

    buildCheckBoxes();
    buildComboBoxes();
  }

  private void buildCheckBoxes() {
    cbMovieNfoFilename1.removeItemListener(checkBoxListener);
    cbMovieNfoFilename2.removeItemListener(checkBoxListener);
    cbMovieNfoFilename3.removeItemListener(checkBoxListener);
    clearSelection(cbMovieNfoFilename1, cbMovieNfoFilename2, cbMovieNfoFilename3);

    // NFO filenames
    List<MovieNfoNaming> movieNfoFilenames = settings.getNfoFilenames();
    if (movieNfoFilenames.contains(MovieNfoNaming.FILENAME_NFO)) {
      cbMovieNfoFilename1.setSelected(true);
    }
    if (movieNfoFilenames.contains(MovieNfoNaming.MOVIE_NFO)) {
      cbMovieNfoFilename2.setSelected(true);
    }
    if (movieNfoFilenames.contains(MovieNfoNaming.DISC_NFO)) {
      cbMovieNfoFilename3.setSelected(true);
    }

    cbMovieNfoFilename1.addItemListener(checkBoxListener);
    cbMovieNfoFilename2.addItemListener(checkBoxListener);
    cbMovieNfoFilename3.addItemListener(checkBoxListener);
  }

  private void clearSelection(JCheckBox... checkBoxes) {
    for (JCheckBox checkBox : checkBoxes) {
      checkBox.setSelected(false);
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

  private void initComponents() {
    setLayout(new MigLayout("", "[grow]", "[]"));
    {
      JPanel panelNfo = SettingsPanelFactory.createSettingsPanel();

      JLabel lblNfoT = new TmmLabel(BUNDLE.getString("Settings.nfo"), H3); //$NON-NLS-1$
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelNfo, lblNfoT, true);
      add(collapsiblePanel, "cell 0 0,growx, wmin 0");
      {
        JLabel lblNfoFormat = new JLabel(BUNDLE.getString("Settings.nfoFormat")); //$NON-NLS-1$
        panelNfo.add(lblNfoFormat, "cell 1 0 2 1");

        cbNfoFormat = new JComboBox(MovieConnectors.values());
        panelNfo.add(cbNfoFormat, "cell 1 0");

        {
          JPanel panelNfoFormat = new JPanel();
          panelNfo.add(panelNfoFormat, "cell 1 1 2 1");
          panelNfoFormat.setLayout(new MigLayout("insets 0", "[][]", "[][][]"));

          JLabel lblNfoFileNaming = new JLabel(BUNDLE.getString("Settings.nofFileNaming")); //$NON-NLS-1$
          panelNfoFormat.add(lblNfoFileNaming, "cell 0 0");

          cbMovieNfoFilename1 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + ".nfo"); //$NON-NLS-1$
          panelNfoFormat.add(cbMovieNfoFilename1, "cell 1 0");

          cbMovieNfoFilename2 = new JCheckBox("movie.nfo"); //$NON-NLS-1$
          panelNfoFormat.add(cbMovieNfoFilename2, "cell 1 1");

          cbMovieNfoFilename3 = new JCheckBox("<html>VIDEO_TS / VIDEO_TS.nfo<br />BDMV / index.nfo</html>"); //$NON-NLS-1$
          panelNfoFormat.add(cbMovieNfoFilename3, "cell 1 2");
        }

        chckbxWriteCleanNfo = new JCheckBox(BUNDLE.getString("Settings.writecleannfo")); //$NON-NLS-1$
        panelNfo.add(chckbxWriteCleanNfo, "cell 1 2 2 1");

        JLabel lblNfoLanguage = new JLabel(BUNDLE.getString("Settings.nfolanguage")); //$NON-NLS-1$
        panelNfo.add(lblNfoLanguage, "cell 1 4 2 1");

        cbNfoLanguage = new JComboBox(MediaLanguages.values());
        panelNfo.add(cbNfoLanguage, "cell 1 4");

        JLabel lblNfoLanguageDesc = new JLabel(BUNDLE.getString("Settings.nfolanguage.desc")); //$NON-NLS-1$
        panelNfo.add(lblNfoLanguageDesc, "cell 2 5");

        JLabel lblCertificationStyle = new JLabel(BUNDLE.getString("Settings.certificationformat")); //$NON-NLS-1$
        panelNfo.add(lblCertificationStyle, "flowx,cell 1 7 2 1");

        cbCertificationStyle = new JComboBox();
        panelNfo.add(cbCertificationStyle, "cell 1 7");
      }
    }
  }

  /**
   * check changes of checkboxes
   */
  private void checkChanges() {
    // set NFO filenames
    settings.clearNfoFilenames();
    if (cbMovieNfoFilename1.isSelected()) {
      settings.addNfoFilename(MovieNfoNaming.FILENAME_NFO);
    }
    if (cbMovieNfoFilename2.isSelected()) {
      settings.addNfoFilename(MovieNfoNaming.MOVIE_NFO);
    }
    if (cbMovieNfoFilename3.isSelected()) {
      settings.addNfoFilename(MovieNfoNaming.DISC_NFO);
    }

    CertificationStyleWrapper wrapper = (CertificationStyleWrapper) cbCertificationStyle.getSelectedItem();
    if (wrapper != null && settings.getCertificationStyle() != wrapper.style) {
      settings.setCertificationStyle(wrapper.style);
    }
  }

  /*
   * helper for displaying the combobox with an example
   */
  private class CertificationStyleWrapper {
    private CertificationStyle style;

    @Override
    public String toString() {
      String bundleTag = BUNDLE.getString("Settings.certification." + style.name().toLowerCase());
      return bundleTag.replace("{}", CertificationStyle.formatCertification(Certification.DE_FSK16, style));
    }
  }

  protected void initDataBindings() {
    BeanProperty<MovieSettings, MovieConnectors> settingsBeanProperty_11 = BeanProperty.create("movieConnector");
    BeanProperty<JComboBox<MovieConnectors>, Object> jComboBoxBeanProperty_1 = BeanProperty.create("selectedItem");
    AutoBinding<MovieSettings, MovieConnectors, JComboBox<MovieConnectors>, Object> autoBinding_9 = Bindings
        .createAutoBinding(UpdateStrategy.READ_WRITE, settings, settingsBeanProperty_11, cbNfoFormat, jComboBoxBeanProperty_1);
    autoBinding_9.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty = BeanProperty.create("writeCleanNfo");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty, chckbxWriteCleanNfo, jCheckBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<MovieSettings, MediaLanguages> movieSettingsBeanProperty_1 = BeanProperty.create("nfoLanguage");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<MovieSettings, MediaLanguages, JComboBox, Object> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_1, cbNfoLanguage, jComboBoxBeanProperty);
    autoBinding_3.bind();
  }
}
