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

package org.tinymediamanager.ui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ObjectProperty;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.tvshow.TvShowEpisodeNaming;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

;

/**
 * The Class TvShowSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowSettingsPanel extends JPanel {
  private static final long           serialVersionUID = -675729644848101096L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private Settings                    settings         = Settings.getInstance();

  private JTable                      tableTvShowSources;
  private JTextField                  tfWhitespaceSeparator;
  private JCheckBox                   chckbxAddSeason;
  private JCheckBox                   chckbxAddShow;
  private JCheckBox                   chckbxAddEpisodeTitle;
  private JCheckBox                   chckbxUseWhitespaceSeparator;
  private JRadioButton                rdbtnRawNumber;
  private JRadioButton                rdbtnSeasonEpisode;
  private JRadioButton                rdbtnSxe;
  private final ButtonGroup           buttonGroup      = new ButtonGroup();

  /**
   * Instantiates a new tv show settings panel.
   */
  public TvShowSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, }));

    JPanel panelTvShowDataSources = new JPanel();

    panelTvShowDataSources.setBorder(new TitledBorder(null,
        BUNDLE.getString("Settings.tvshowdatasource"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelTvShowDataSources, "2, 2, fill, top");
    panelTvShowDataSources.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(72dlu;default)"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(66dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("max(44dlu;default)"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("100px:grow"), }));

    JScrollPane scrollPane = new JScrollPane();
    panelTvShowDataSources.add(scrollPane, "2, 2, 5, 1, fill, fill");

    tableTvShowSources = new JTable();
    scrollPane.setViewportView(tableTvShowSources);

    JPanel panelTvShowSourcesButtons = new JPanel();
    panelTvShowDataSources.add(panelTvShowSourcesButtons, "8, 2, default, top");
    panelTvShowSourcesButtons.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JButton btnAdd = new JButton(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    btnAdd.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        File file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.tvshowdatasource.folderchooser")); //$NON-NLS-1$
        if (file != null && file.exists() && file.isDirectory()) {
          settings.getTvShowSettings().addTvShowDataSources(file.getAbsolutePath());
        }
      }
    });

    panelTvShowSourcesButtons.add(btnAdd, "2, 1, fill, top");

    JButton btnRemove = new JButton(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
    btnRemove.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int row = tableTvShowSources.convertRowIndexToModel(tableTvShowSources.getSelectedRow());
        if (row != -1) { // nothing selected
          String path = Globals.settings.getTvShowSettings().getTvShowDataSource().get(row);
          String[] choices = { BUNDLE.getString("Button.continue"), BUNDLE.getString("Button.abort") }; //$NON-NLS-1$
          int decision = JOptionPane.showOptionDialog(null, String.format(BUNDLE.getString("Settings.tvshowdatasource.remove.info"), path),
              BUNDLE.getString("Settings.datasource.remove"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices,
              BUNDLE.getString("Button.abort")); //$NON-NLS-1$
          if (decision == 0) {
            Globals.settings.getTvShowSettings().removeTvShowDataSources(path);
          }
        }
      }
    });
    panelTvShowSourcesButtons.add(btnRemove, "2, 3, fill, top");

    // the panel renamer
    JPanel panelRenamer = new JPanel();
    panelRenamer.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.renamer"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$

    add(panelRenamer, "2, 4, fill, fill");
    panelRenamer.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.MIN_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.MIN_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.UNRELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    chckbxAddSeason = new JCheckBox("add season to filename");
    chckbxAddSeason.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        createRenamerExample();
      }
    });
    panelRenamer.add(chckbxAddSeason, "2, 2, 3, 1");

    rdbtnSeasonEpisode = new JRadioButton("classic - S01E01");
    rdbtnSeasonEpisode.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        checkChanges();
        createRenamerExample();
      }
    });
    buttonGroup.add(rdbtnSeasonEpisode);
    panelRenamer.add(rdbtnSeasonEpisode, "6, 2");

    chckbxAddShow = new JCheckBox("add TV show name to filename");
    chckbxAddShow.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        checkChanges();
        createRenamerExample();
      }
    });
    panelRenamer.add(chckbxAddShow, "2, 4, 3, 1");

    rdbtnSxe = new JRadioButton("x as separator - 1x01");
    rdbtnSxe.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        checkChanges();
        createRenamerExample();
      }
    });
    buttonGroup.add(rdbtnSxe);
    panelRenamer.add(rdbtnSxe, "6, 4");

    chckbxAddEpisodeTitle = new JCheckBox("add episode title to filename");
    chckbxAddEpisodeTitle.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        checkChanges();
        createRenamerExample();
      }
    });
    panelRenamer.add(chckbxAddEpisodeTitle, "2, 6, 3, 1");

    rdbtnRawNumber = new JRadioButton("raw number - 101");
    rdbtnRawNumber.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        checkChanges();
        createRenamerExample();
      }
    });
    buttonGroup.add(rdbtnRawNumber);
    panelRenamer.add(rdbtnRawNumber, "6, 6");

    chckbxUseWhitespaceSeparator = new JCheckBox("use whitespace separator");
    chckbxUseWhitespaceSeparator.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
        createRenamerExample();
      }
    });
    panelRenamer.add(chckbxUseWhitespaceSeparator, "2, 8");

    tfWhitespaceSeparator = new JTextField();
    tfWhitespaceSeparator.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent arg0) {
        createRenamerExample();
      }

      @Override
      public void insertUpdate(DocumentEvent arg0) {
        createRenamerExample();
      }

      @Override
      public void changedUpdate(DocumentEvent arg0) {
        createRenamerExample();
      }
    });
    panelRenamer.add(tfWhitespaceSeparator, "4, 8, fill, default");
    tfWhitespaceSeparator.setColumns(10);

    JLabel lblExampleT = new JLabel("Example");
    panelRenamer.add(lblExampleT, "2, 10");

    JLabel lblExample = new JLabel("");
    panelRenamer.add(lblExample, "2, 12, 5, 1");

    initDataBindings();

    {
      // set radio buttons and white space separator
      if (StringUtils.isNotBlank(settings.getTvShowSettings().getRenamerSeparator())) {
        chckbxUseWhitespaceSeparator.setSelected(true);
      }

      switch (settings.getTvShowSettings().getRenamerFormat()) {
        case NUMBER:
          rdbtnRawNumber.setSelected(true);
          break;

        case WITH_SE:
          rdbtnSeasonEpisode.setSelected(true);
          break;

        case WITH_X:
          rdbtnSxe.setSelected(true);
          break;
      }
    }
  }

  private void checkChanges() {
    if (!chckbxUseWhitespaceSeparator.isSelected()) {
      tfWhitespaceSeparator.setText("");
    }

    if (rdbtnRawNumber.isSelected()) {
      settings.getTvShowSettings().setRenamerFormat(TvShowEpisodeNaming.NUMBER);
    }
    else if (rdbtnSeasonEpisode.isSelected()) {
      settings.getTvShowSettings().setRenamerFormat(TvShowEpisodeNaming.WITH_SE);
    }
    else if (rdbtnSxe.isSelected()) {
      settings.getTvShowSettings().setRenamerFormat(TvShowEpisodeNaming.WITH_X);
    }
  }

  private void createRenamerExample() {
    // TODO create TV show renamer example
  }

  protected void initDataBindings() {
    BeanProperty<Settings, List<String>> settingsBeanProperty_4 = BeanProperty.create("tvShowSettings.tvShowDataSource");
    JTableBinding<String, Settings, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, settings, settingsBeanProperty_4,
        tableTvShowSources);
    //
    ObjectProperty<String> stringObjectProperty = ObjectProperty.create();
    jTableBinding.addColumnBinding(stringObjectProperty).setColumnName("Source");
    //
    jTableBinding.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty = BeanProperty.create("tvShowSettings.renamerAddSeason");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxAddSeason, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_1 = BeanProperty.create("tvShowSettings.renamerAddShow");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, chckbxAddShow, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_2 = BeanProperty.create("tvShowSettings.renamerAddTitle");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, chckbxAddEpisodeTitle, jCheckBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<JTextField, Boolean> jTextFieldBeanProperty = BeanProperty.create("enabled");
    AutoBinding<JCheckBox, Boolean, JTextField, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ,
        chckbxUseWhitespaceSeparator, jCheckBoxBeanProperty, tfWhitespaceSeparator, jTextFieldBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_3 = BeanProperty.create("tvShowSettings.renamerSeparator");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_3, tfWhitespaceSeparator, jTextFieldBeanProperty_1);
    autoBinding_4.bind();
  }
}
