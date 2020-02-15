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
package org.tinymediamanager.ui.settings;

import static org.tinymediamanager.ui.TmmFontHelper.H3;

import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.ImageCache.CacheType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.SettingsPanelFactory;
import org.tinymediamanager.ui.components.TmmLabel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class MiscSettingsPanel.
 * 
 * @author Manuel Laggner
 */
class MiscSettingsPanel extends JPanel {
  private static final long           serialVersionUID = 500841588272296493L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private Settings                    settings         = Settings.getInstance();
  private JComboBox                   cbImageCacheQuality;
  private JCheckBox                   chckbxImageCache;
  private JCheckBox                   chckbxDeleteTrash;

  /**
   * Instantiates a new general settings panel.
   */
  MiscSettingsPanel() {
    initComponents();
    initDataBindings();

  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp:n,grow]", "[]"));
    {
      JPanel panelMisc = SettingsPanelFactory.createSettingsPanel();

      JLabel lblMiscT = new TmmLabel(BUNDLE.getString("Settings.misc"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelMisc, lblMiscT, true);
      add(collapsiblePanel, "cell 0 0,growx, wmin 0");
      {
        chckbxImageCache = new JCheckBox(BUNDLE.getString("Settings.imagecache"));
        panelMisc.add(chckbxImageCache, "cell 1 0 2 1");

        JLabel lblImageCacheQuality = new JLabel(BUNDLE.getString("Settings.imagecachetype"));
        panelMisc.add(lblImageCacheQuality, "cell 2 1");

        cbImageCacheQuality = new JComboBox(ImageCache.CacheType.values());
        panelMisc.add(cbImageCacheQuality, "cell 2 1");

        chckbxDeleteTrash = new JCheckBox(BUNDLE.getString("Settings.deletetrash"));
        panelMisc.add(chckbxDeleteTrash, "cell 1 2 2 1");
      }
    }
  }

  protected void initDataBindings() {
    BeanProperty<Settings, CacheType> settingsBeanProperty_7 = BeanProperty.create("imageCacheType");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<Settings, CacheType, JComboBox, Object> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_7, cbImageCacheQuality, jComboBoxBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_9 = BeanProperty.create("imageCache");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, chckbxImageCache, jCheckBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_10 = BeanProperty.create("deleteTrashOnExit");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_10, chckbxDeleteTrash, jCheckBoxBeanProperty);
    autoBinding_10.bind();
  }
}
