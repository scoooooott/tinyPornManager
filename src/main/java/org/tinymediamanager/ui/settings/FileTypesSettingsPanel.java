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
package org.tinymediamanager.ui.settings;

import java.awt.Font;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;

import net.miginfocom.swing.MigLayout;

public class FileTypesSettingsPanel extends JPanel {
  private static final long           serialVersionUID = 9136097757447080369L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private Settings                    settings         = Settings.getInstance();
  private JTextField                  tfVideoFiletype;
  private JList<String>               listVideoFiletypes;
  private JTextField                  tfSubtitleFiletype;
  private JList<String>               listSubtitleFiletypes;
  private JList<String>               listAudioFiletypes;
  private JTextField                  tfAudioFiletype;
  private JButton                     btnAddAudioFiletype;
  private JButton                     btnAddSubtitleFiletype;
  private JButton                     btnAddVideoFiletype;
  private JButton                     btnRemoveAudioFiletype;
  private JButton                     btnRemoveSubtitleFiletype;
  private JButton                     btnRemoveVideoFiletype;

  /**
   * Instantiates a new general settings panel.
   */
  public FileTypesSettingsPanel() {
    // UI init
    initComponents();
    initDataBindings();

    // data init
    btnAddVideoFiletype.addActionListener(e -> {
      if (StringUtils.isNotEmpty(tfVideoFiletype.getText())) {
        Globals.settings.addVideoFileTypes(tfVideoFiletype.getText());
        tfVideoFiletype.setText("");
      }
    });
    btnRemoveVideoFiletype.addActionListener(arg0 -> {
      int row = listVideoFiletypes.getSelectedIndex();
      if (row != -1) {
        String prefix = Globals.settings.getVideoFileType().get(row);
        Globals.settings.removeVideoFileType(prefix);
      }
    });
    btnAddSubtitleFiletype.addActionListener(e -> {
      if (StringUtils.isNotEmpty(tfSubtitleFiletype.getText())) {
        Globals.settings.addSubtitleFileTypes(tfSubtitleFiletype.getText());
        tfSubtitleFiletype.setText("");
      }
    });
    btnRemoveSubtitleFiletype.addActionListener(arg0 -> {
      int row = listSubtitleFiletypes.getSelectedIndex();
      if (row != -1) {
        String prefix = Globals.settings.getSubtitleFileType().get(row);
        Globals.settings.removeSubtitleFileType(prefix);
      }
    });
    btnAddAudioFiletype.addActionListener(e -> {
      if (StringUtils.isNotEmpty(tfAudioFiletype.getText())) {
        Globals.settings.addAudioFileTypes(tfAudioFiletype.getText());
        tfAudioFiletype.setText("");
      }
    });
    btnRemoveAudioFiletype.addActionListener(arg0 -> {
      int row = listAudioFiletypes.getSelectedIndex();
      if (row != -1) {
        String prefix = Globals.settings.getAudioFileType().get(row);
        Globals.settings.removeAudioFileType(prefix);
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp][150lp][][25lp][150lp][][25lp][150lp][]", "[][400lp][]"));
    {
      final JLabel lblVideoFiletypesT = new JLabel(BUNDLE.getString("Settings.videofiletypes")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblVideoFiletypesT, 1.16667, Font.BOLD);
      add(lblVideoFiletypesT, "cell 0 0 2 1");

      JScrollPane scrollPaneVideoFiletypes = new JScrollPane();
      add(scrollPaneVideoFiletypes, "cell 1 1,grow");

      listVideoFiletypes = new JList<>();
      scrollPaneVideoFiletypes.setViewportView(listVideoFiletypes);

      btnRemoveVideoFiletype = new JButton(IconManager.REMOVE_INV);
      add(btnRemoveVideoFiletype, "cell 2 1,aligny bottom");
      btnRemoveVideoFiletype.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$

      tfVideoFiletype = new JTextField();
      add(tfVideoFiletype, "cell 1 2,growx");
      tfVideoFiletype.setColumns(10);

      btnAddVideoFiletype = new JButton(IconManager.ADD_INV);
      add(btnAddVideoFiletype, "cell 2 2");
      btnAddVideoFiletype.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    }
    {
      final JLabel lblSubtitleFiletypeT = new JLabel(BUNDLE.getString("Settings.extrafiletypes")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblSubtitleFiletypeT, 1.16667, Font.BOLD);
      add(lblSubtitleFiletypeT, "cell 3 0 2 1");

      JScrollPane scrollPaneSubtitleFiletypes = new JScrollPane();
      add(scrollPaneSubtitleFiletypes, "cell 4 1,grow");

      listSubtitleFiletypes = new JList<>();
      scrollPaneSubtitleFiletypes.setViewportView(listSubtitleFiletypes);

      btnRemoveSubtitleFiletype = new JButton(IconManager.REMOVE_INV);
      add(btnRemoveSubtitleFiletype, "cell 5 1,aligny bottom");
      btnRemoveSubtitleFiletype.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$

      tfSubtitleFiletype = new JTextField();
      add(tfSubtitleFiletype, "cell 4 2,growx");
      tfSubtitleFiletype.setColumns(10);

      btnAddSubtitleFiletype = new JButton(IconManager.ADD_INV);
      add(btnAddSubtitleFiletype, "cell 5 2");
      btnAddSubtitleFiletype.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    }
    {
      final JLabel lblAudioFiletypeT = new JLabel(BUNDLE.getString("Settings.audiofiletypes")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblAudioFiletypeT, 1.16667, Font.BOLD);
      add(lblAudioFiletypeT, "cell 6 0 2 1");

      JScrollPane scrollPaneAudioFiletypes = new JScrollPane();
      add(scrollPaneAudioFiletypes, "cell 7 1,grow");

      listAudioFiletypes = new JList<>();
      scrollPaneAudioFiletypes.setViewportView(listAudioFiletypes);

      btnRemoveAudioFiletype = new JButton(IconManager.REMOVE_INV);
      add(btnRemoveAudioFiletype, "cell 8 1,aligny bottom");
      btnRemoveAudioFiletype.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$

      tfAudioFiletype = new JTextField();
      add(tfAudioFiletype, "cell 7 2,growx");
      tfAudioFiletype.setColumns(10);

      btnAddAudioFiletype = new JButton(IconManager.ADD_INV);
      add(btnAddAudioFiletype, "cell 8 2");
      btnAddAudioFiletype.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    }
  }

  @SuppressWarnings("rawtypes")
  protected void initDataBindings() {
    BeanProperty<Settings, List<String>> settingsBeanProperty_5 = BeanProperty.create("videoFileType");
    JListBinding<String, Settings, JList> jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_5, listVideoFiletypes);
    jListBinding_1.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_6 = BeanProperty.create("subtitleFileType");
    JListBinding<String, Settings, JList> jListBinding_2 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, listSubtitleFiletypes);
    jListBinding_2.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_11 = BeanProperty.create("audioFileType");
    JListBinding<String, Settings, JList> jListBinding_3 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_11, listAudioFiletypes);
    jListBinding_3.bind();
  }
}
