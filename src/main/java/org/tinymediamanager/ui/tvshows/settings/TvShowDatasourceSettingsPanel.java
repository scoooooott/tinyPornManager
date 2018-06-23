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
package org.tinymediamanager.ui.tvshows.settings;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;

import net.miginfocom.swing.MigLayout;

/**
 * The class TvShowDatasourceSettingsPanel is used to display data sources related settings
 * 
 * @author Manuel Laggner
 */
public class TvShowDatasourceSettingsPanel extends JPanel {
  private static final long           serialVersionUID = -675729644848101096L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private TvShowSettings              settings         = TvShowModuleManager.SETTINGS;
  private JCheckBox                   chckbxDvdOrder;
  private JTextField                  tfAddBadword;
  private JList<String>               listBadWords;
  private JList<String>               listDatasources;
  private JList<String>               listExclude;
  private JButton                     btnAddDatasource;
  private JButton                     btnRemoveDatasource;
  private JButton                     btnAddSkipFolder;
  private JButton                     btnRemoveSkipFolder;
  private JButton                     btnRemoveBadWord;
  private JButton                     btnAddBadWord;

  public TvShowDatasourceSettingsPanel() {
    // UI initializations
    initComponents();
    initDataBindings();

    // logic initializations
    btnAddDatasource.addActionListener(arg0 -> {
      String path = TmmProperties.getInstance().getProperty("tvshow.datasource.path"); //$NON-NLS-1$
      Path file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.tvshowdatasource.folderchooser"), path); //$NON-NLS-1$
      if (file != null && Files.isDirectory(file)) {
        settings.addTvShowDataSources(file.toAbsolutePath().toString());
        TmmProperties.getInstance().putProperty("tvshow.datasource.path", file.toAbsolutePath().toString());
      }
    });
    btnRemoveDatasource.addActionListener(arg0 -> {
      int row = listDatasources.getSelectedIndex();
      if (row != -1) { // nothing selected
        String path = settings.getTvShowDataSource().get(row);
        String[] choices = { BUNDLE.getString("Button.continue"), BUNDLE.getString("Button.abort") }; //$NON-NLS-1$
        int decision = JOptionPane.showOptionDialog(null, String.format(BUNDLE.getString("Settings.tvshowdatasource.remove.info"), path),
            BUNDLE.getString("Settings.datasource.remove"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices,
            BUNDLE.getString("Button.abort")); //$NON-NLS-1$
        if (decision == 0) {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          settings.removeTvShowDataSources(path);
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      }
    });
    btnAddSkipFolder.addActionListener(e -> {
      String path = TmmProperties.getInstance().getProperty("tvshow.ignore.path"); //$NON-NLS-1$
      Path file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.ignore"), path); //$NON-NLS-1$
      if (file != null && Files.isDirectory(file)) {
        settings.addSkipFolder(file.toAbsolutePath().toString());
        TmmProperties.getInstance().putProperty("tvshow.ignore.path", file.toAbsolutePath().toString());
      }
    });
    btnRemoveSkipFolder.addActionListener(e -> {
      int row = listExclude.getSelectedIndex();
      if (row != -1) { // nothing selected
        String ingore = settings.getSkipFolder().get(row);
        settings.removeSkipFolder(ingore);
      }
    });
    btnAddBadWord.addActionListener(e -> {
      if (StringUtils.isNotEmpty(tfAddBadword.getText())) {
        TvShowModuleManager.SETTINGS.addBadWord(tfAddBadword.getText());
        tfAddBadword.setText("");
      }
    });
    btnRemoveBadWord.addActionListener(arg0 -> {
      int row = listBadWords.getSelectedIndex();
      if (row != -1) {
        String badWord = TvShowModuleManager.SETTINGS.getBadWord().get(row);
        TvShowModuleManager.SETTINGS.removeBadWord(badWord);
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp][300lp,grow][25lp][300lp,grow]", "[][100lp,grow][][20lp][][100lp,grow][grow 200]"));
    {
      JLabel lblDatasourcesT = new JLabel(BUNDLE.getString("Settings.source")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblDatasourcesT, 1.16667, Font.BOLD);
      add(lblDatasourcesT, "cell 0 0 2 1");
    }

    {
      JScrollPane scrollPaneDatasource = new JScrollPane();
      add(scrollPaneDatasource, "flowx,cell 1 1,grow");

      listDatasources = new JList<>();
      scrollPaneDatasource.setViewportView(listDatasources);

      JPanel panelTvShowSourcesButtons = new JPanel();
      add(panelTvShowSourcesButtons, "cell 1 1,aligny top");

      btnAddDatasource = new JButton(IconManager.ADD_INV);
      btnAddDatasource.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$
      btnAddDatasource.setMargin(new Insets(2, 2, 2, 2));
      panelTvShowSourcesButtons.setLayout(new MigLayout("", "[]", "[][]"));

      panelTvShowSourcesButtons.add(btnAddDatasource, "cell 0 0,growx,aligny top");

      btnRemoveDatasource = new JButton(IconManager.REMOVE_INV);
      btnRemoveDatasource.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
      btnRemoveDatasource.setMargin(new Insets(2, 2, 2, 2));
      panelTvShowSourcesButtons.add(btnRemoveDatasource, "cell 0 1,growx,aligny top");

      chckbxDvdOrder = new JCheckBox(BUNDLE.getString("Settings.dvdorder")); //$NON-NLS-1$
      add(chckbxDvdOrder, "cell 1 2");
    }

    {
      JLabel lblExcludeT = new JLabel(BUNDLE.getString("Settings.ignore"));//$NON-NLS-1$
      TmmFontHelper.changeFont(lblExcludeT, 1.16667, Font.BOLD);
      add(lblExcludeT, "cell 0 4 2 1");

      JLabel lblBadWordsT = new JLabel(BUNDLE.getString("Settings.movie.badwords")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblBadWordsT, 1.16667, Font.BOLD);
      add(lblBadWordsT, "flowx,cell 2 4 2 1");

      JLabel lblBadWordsDesc = new JLabel(IconManager.HINT);
      lblBadWordsDesc.setToolTipText(BUNDLE.getString("Settings.movie.badwords.hint")); //$NON-NLS-1$
      add(lblBadWordsDesc, "cell 2 4 2 1");
    }

    {
      JScrollPane scrollPaneIgnore = new JScrollPane();
      add(scrollPaneIgnore, "flowx,cell 1 5,grow");

      listExclude = new JList<>();
      scrollPaneIgnore.setViewportView(listExclude);

      JPanel panelSkipFolderButtons = new JPanel();
      add(panelSkipFolderButtons, "cell 1 5,aligny top");

      btnAddSkipFolder = new JButton(IconManager.ADD_INV);
      btnAddSkipFolder.setToolTipText(BUNDLE.getString("Settings.addignore")); //$NON-NLS-1$
      btnAddSkipFolder.setMargin(new Insets(2, 2, 2, 2));
      panelSkipFolderButtons.setLayout(new MigLayout("", "[]", "[][]"));
      panelSkipFolderButtons.add(btnAddSkipFolder, "cell 0 0,alignx left,aligny top");

      btnRemoveSkipFolder = new JButton(IconManager.REMOVE_INV);
      btnRemoveSkipFolder.setToolTipText(BUNDLE.getString("Settings.removeignore")); //$NON-NLS-1$
      btnRemoveSkipFolder.setMargin(new Insets(2, 2, 2, 2));
      panelSkipFolderButtons.add(btnRemoveSkipFolder, "cell 0 1,alignx left,aligny top");
    }

    {
      JPanel panelBadWords = new JPanel();
      panelBadWords.setBorder(null); // $NON-NLS-1$
      add(panelBadWords, "cell 3 5,grow");
      panelBadWords.setLayout(new MigLayout("insets 0", "[][]", "[100lp,grow][]"));

      JScrollPane scpBadWords = new JScrollPane();
      panelBadWords.add(scpBadWords, "cell 0 0,grow");

      listBadWords = new JList<>();
      scpBadWords.setViewportView(listBadWords);

      btnRemoveBadWord = new JButton(IconManager.REMOVE_INV);
      btnRemoveBadWord.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
      btnRemoveBadWord.setMargin(new Insets(2, 2, 2, 2));
      panelBadWords.add(btnRemoveBadWord, "cell 1 0,alignx left,aligny bottom");

      tfAddBadword = new JTextField();
      tfAddBadword.setColumns(10);
      panelBadWords.add(tfAddBadword, "cell 0 1,growx,aligny center");

      btnAddBadWord = new JButton(IconManager.ADD_INV);
      btnAddBadWord.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$
      btnAddBadWord.setMargin(new Insets(2, 2, 2, 2));
      panelBadWords.add(btnAddBadWord, "cell 1 1,alignx left,aligny top");
    }
  }

  protected void initDataBindings() {
    BeanProperty<TvShowSettings, Boolean> settingsBeanProperty_1 = BeanProperty.create("dvdOrder");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, chckbxDvdOrder, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<TvShowSettings, List<String>> settingsBeanProperty_2 = BeanProperty.create("tvShowDataSource");
    JListBinding<String, TvShowSettings, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, listDatasources);
    jListBinding.bind();
    //
    BeanProperty<TvShowSettings, List<String>> settingsBeanProperty_3 = BeanProperty.create("skipFolder");
    JListBinding<String, TvShowSettings, JList> jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_3, listExclude);
    jListBinding_1.bind();
    //
    BeanProperty<TvShowSettings, List<String>> settingsBeanProperty_4 = BeanProperty.create("badWord");
    JListBinding<String, TvShowSettings, JList> jListBinding_2 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_4, listBadWords);
    jListBinding_2.bind();
  }
}
