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

import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.TmmLabel;

import net.miginfocom.swing.MigLayout;

class FileTypesSettingsPanel extends JPanel {
  private static final long           serialVersionUID = 9136097757447080369L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private Settings                    settings         = Settings.getInstance();
  private JTextField                  tfVideoFiletype;
  private JList<String>               listVideoFiletypes;
  private JTextField                  tfSubtitleFiletype;
  private JList<String>               listSubtitleFiletypes;
  private JTextField                  tfAudioFiletype;
  private JList<String>               listAudioFiletypes;
  private JTextField                  tfCleanupFiletype;
  private JList<String>               listCleanupFiletypes;

  private JButton                     btnAddAudioFiletype;
  private JButton                     btnAddSubtitleFiletype;
  private JButton                     btnAddVideoFiletype;
  private JButton                     btnAddCleanupFiletype;
  private JButton                     btnRemoveCleanupFiletype;
  private JButton                     btnRemoveAudioFiletype;
  private JButton                     btnRemoveSubtitleFiletype;
  private JButton                     btnRemoveVideoFiletype;

  /**
   * Instantiates a new general settings panel.
   */
  FileTypesSettingsPanel() {
    // UI init
    initComponents();
    initDataBindings();

    // data init
    btnAddVideoFiletype.addActionListener(e -> {
      if (StringUtils.isNotEmpty(tfVideoFiletype.getText())) {
        Globals.settings.addVideoFileType(tfVideoFiletype.getText());
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
        Globals.settings.addSubtitleFileType(tfSubtitleFiletype.getText());
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
        Globals.settings.addAudioFileType(tfAudioFiletype.getText());
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
    btnAddCleanupFiletype.addActionListener(e -> {
      if (StringUtils.isNotEmpty(tfCleanupFiletype.getText())) {
        try {
          Pattern.compile(tfCleanupFiletype.getText());
        }
        catch (PatternSyntaxException ex) {
          JOptionPane.showMessageDialog(null, BUNDLE.getString("message.regex.error"));
          return;
        }
        Globals.settings.addCleanupFileType(tfCleanupFiletype.getText());
        tfCleanupFiletype.setText("");
      }
    });
    btnRemoveCleanupFiletype.addActionListener(arg0 -> {
      int row = listCleanupFiletypes.getSelectedIndex();
      if (row != -1) {
        String prefix = Globals.settings.getCleanupFileType().get(row);
        Globals.settings.removeCleanupFileType(prefix);
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[grow]", "[][15lp!][][15lp!][][15lp!][]"));
    {
      JPanel panelVideoFiletypes = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][100lp][][grow]", "[]"));

      JLabel lblVideoFiletypesT = new TmmLabel(BUNDLE.getString("Settings.videofiletypes"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelVideoFiletypes, lblVideoFiletypesT, true);
      add(collapsiblePanel, "cell 0 0,growx, wmin 0");
      {
        JScrollPane scrollPaneVideoFiletypes = new JScrollPane();
        panelVideoFiletypes.add(scrollPaneVideoFiletypes, "cell 1 0,grow");

        listVideoFiletypes = new JList<>();
        scrollPaneVideoFiletypes.setViewportView(listVideoFiletypes);

        btnRemoveVideoFiletype = new JButton(IconManager.REMOVE_INV);
        panelVideoFiletypes.add(btnRemoveVideoFiletype, "cell 2 0,aligny bottom, growx");
        btnRemoveVideoFiletype.setToolTipText(BUNDLE.getString("Button.remove"));

        tfVideoFiletype = new JTextField();
        panelVideoFiletypes.add(tfVideoFiletype, "cell 1 1,growx");

        btnAddVideoFiletype = new JButton(IconManager.ADD_INV);
        panelVideoFiletypes.add(btnAddVideoFiletype, "cell 2 1,growx");
        btnAddVideoFiletype.setToolTipText(BUNDLE.getString("Button.add"));
      }
    }
    {
      JPanel panelSubtitleFiletypes = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][100lp][][grow]", "[]"));

      JLabel lblSubtitleFiletypesT = new TmmLabel(BUNDLE.getString("Settings.extrafiletypes"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelSubtitleFiletypes, lblSubtitleFiletypesT, true);
      add(collapsiblePanel, "cell 0 2,growx,wmin 0");
      {
        JScrollPane scrollPaneSubtitleFiletypes = new JScrollPane();
        panelSubtitleFiletypes.add(scrollPaneSubtitleFiletypes, "cell 1 0,grow");

        listSubtitleFiletypes = new JList<>();
        scrollPaneSubtitleFiletypes.setViewportView(listSubtitleFiletypes);

        btnRemoveSubtitleFiletype = new JButton(IconManager.REMOVE_INV);
        panelSubtitleFiletypes.add(btnRemoveSubtitleFiletype, "cell 2 0,aligny bottom, growx");
        btnRemoveSubtitleFiletype.setToolTipText(BUNDLE.getString("Button.remove"));

        tfSubtitleFiletype = new JTextField();
        panelSubtitleFiletypes.add(tfSubtitleFiletype, "cell 1 1,growx");

        btnAddSubtitleFiletype = new JButton(IconManager.ADD_INV);
        panelSubtitleFiletypes.add(btnAddSubtitleFiletype, "cell 2 1");
        btnAddSubtitleFiletype.setToolTipText(BUNDLE.getString("Button.add"));
      }
    }
    {
      JPanel panelAudioFiletypes = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][100lp][][grow]", "[]"));

      JLabel lblAudioFiletypesT = new TmmLabel(BUNDLE.getString("Settings.audiofiletypes"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelAudioFiletypes, lblAudioFiletypesT, true);
      add(collapsiblePanel, "cell 0 4,growx,wmin 0");
      {
        JScrollPane scrollPaneAudioFiletypes = new JScrollPane();
        panelAudioFiletypes.add(scrollPaneAudioFiletypes, "cell 1 0,grow");

        listAudioFiletypes = new JList<>();
        scrollPaneAudioFiletypes.setViewportView(listAudioFiletypes);

        btnRemoveAudioFiletype = new JButton(IconManager.REMOVE_INV);
        panelAudioFiletypes.add(btnRemoveAudioFiletype, "cell 2 0,aligny bottom, growx");
        btnRemoveAudioFiletype.setToolTipText(BUNDLE.getString("Button.remove"));

        tfAudioFiletype = new JTextField();
        panelAudioFiletypes.add(tfAudioFiletype, "cell 1 1,growx");

        btnAddAudioFiletype = new JButton(IconManager.ADD_INV);
        panelAudioFiletypes.add(btnAddAudioFiletype, "cell 2 1, growx");
        btnAddAudioFiletype.setToolTipText(BUNDLE.getString("Button.add"));
      }
    }
    {
      JPanel panelCleanupFiletypes = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][300lp][][grow]", "[]"));

      JLabel lblCleanupFiletypesT = new TmmLabel(BUNDLE.getString("Settings.unwantedfiletypes"), H3);
      JLabel lblCleanupFiletypesHelpT = new TmmLabel(BUNDLE.getString("Settings.cleanupfiles.help"));
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelCleanupFiletypes, lblCleanupFiletypesT, true);
      add(collapsiblePanel, "cell 0 6,growx,wmin 0");
      {
        JScrollPane scrollPaneCleanupFiletypes = new JScrollPane();
        panelCleanupFiletypes.add(scrollPaneCleanupFiletypes, "cell 1 0,grow");

        listCleanupFiletypes = new JList<>();
        scrollPaneCleanupFiletypes.setViewportView(listCleanupFiletypes);

        btnRemoveCleanupFiletype = new JButton(IconManager.REMOVE_INV);
        panelCleanupFiletypes.add(btnRemoveCleanupFiletype, "cell 2 0,aligny bottom, growx");
        btnRemoveCleanupFiletype.setToolTipText(BUNDLE.getString("Button.remove"));

        tfCleanupFiletype = new JTextField();
        panelCleanupFiletypes.add(tfCleanupFiletype, "cell 1 1,growx");

        btnAddCleanupFiletype = new JButton(IconManager.ADD_INV);
        panelCleanupFiletypes.add(btnAddCleanupFiletype, "cell 2 1, growx");
        btnAddCleanupFiletype.setToolTipText(BUNDLE.getString("Button.add"));

        panelCleanupFiletypes.add(lblCleanupFiletypesHelpT, "cell 1 2");

        JButton btnHelp = new JButton(BUNDLE.getString("tmm.help"));
        btnHelp.addActionListener(e -> {
          String url = StringEscapeUtils.unescapeHtml4("https://gitlab.com/tinyMediaManager/tinyMediaManager/wikis/General-Settings#file-types");
          try {
            TmmUIHelper.browseUrl(url);
          }
          catch (Exception e1) {
            MessageManager.instance
                .pushMessage(new Message(Message.MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":", e1.getLocalizedMessage() }));
          }
        });
        panelCleanupFiletypes.add(btnHelp, "cell 1 2");

      }
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
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_12 = BeanProperty.create("cleanupFileType");
    JListBinding<String, Settings, JList> jListBinding_4 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_12, listCleanupFiletypes);
    jListBinding_4.bind();
  }
}
