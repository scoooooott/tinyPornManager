/*
 * Copyright 2012 - 2015 Manuel Laggner
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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.trakttv.ClearTraktTvTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.panels.ScrollablePanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

;

/**
 * The Class TvShowSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowSettingsPanel extends ScrollablePanel {
  private static final long           serialVersionUID = -675729644848101096L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private Settings                    settings         = Settings.getInstance();
  private JLabel                      lblImageCache;
  private JCheckBox                   chckbxImageCache;
  private JLabel                      lblImageCacheHint;
  private JCheckBox                   chckbxTraktTv;
  private final JButton               btnClearTraktTvShows;
  private JCheckBox                   cbDvdOrder;
  private JList<String>               listDatasources;
  private JList<String>               listExclude;

  /**
   * Instantiates a new tv show settings panel.
   */
  public TvShowSettingsPanel() {
    setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
            FormFactory.DEFAULT_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
            RowSpec.decode("default:grow"), }));

    {
      JPanel panelTvShowDataSources = new JPanel();

      panelTvShowDataSources
          .setBorder(new TitledBorder(null, BUNDLE.getString("Settings.tvshowdatasource"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
      add(panelTvShowDataSources, "2, 2, fill, top");
      panelTvShowDataSources.setLayout(new FormLayout(
          new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
              FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("50dlu:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
              FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.UNRELATED_GAP_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("200dlu:grow(2)"),
              FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
              FormSpecs.RELATED_GAP_COLSPEC, },
          new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("160px:grow"),
              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.RELATED_GAP_ROWSPEC, }));

      JLabel lblDataSource = new JLabel(BUNDLE.getString("Settings.source")); //$NON-NLS-1$
      panelTvShowDataSources.add(lblDataSource, "2, 2, 5, 1");

      JLabel lblSkipFolders = new JLabel(BUNDLE.getString("Settings.ignore"));//$NON-NLS-1$
      panelTvShowDataSources.add(lblSkipFolders, "12, 2, 3, 1");

      JScrollPane scrollPaneDatasource = new JScrollPane();
      panelTvShowDataSources.add(scrollPaneDatasource, "2, 4, 5, 1, fill, fill");

      listDatasources = new JList<>();
      scrollPaneDatasource.setViewportView(listDatasources);

      JPanel panelTvShowSourcesButtons = new JPanel();
      panelTvShowDataSources.add(panelTvShowSourcesButtons, "8, 4, default, top");
      panelTvShowSourcesButtons.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.DEFAULT_COLSPEC, },
          new RowSpec[] { FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

      JButton btnAdd = new JButton(IconManager.ADD_INV);
      btnAdd.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$
      btnAdd.setMargin(new Insets(2, 2, 2, 2));
      btnAdd.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          File file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.tvshowdatasource.folderchooser")); //$NON-NLS-1$
          if (file != null && file.exists() && file.isDirectory()) {
            settings.getTvShowSettings().addTvShowDataSources(file.getAbsolutePath());
          }
        }
      });

      panelTvShowSourcesButtons.add(btnAdd, "1, 1, fill, top");

      JButton btnRemove = new JButton(IconManager.REMOVE_INV);
      btnRemove.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
      btnRemove.setMargin(new Insets(2, 2, 2, 2));
      btnRemove.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          int row = listDatasources.getSelectedIndex();
          if (row != -1) { // nothing selected
            String path = Globals.settings.getTvShowSettings().getTvShowDataSource().get(row);
            String[] choices = { BUNDLE.getString("Button.continue"), BUNDLE.getString("Button.abort") }; //$NON-NLS-1$
            int decision = JOptionPane.showOptionDialog(null, String.format(BUNDLE.getString("Settings.tvshowdatasource.remove.info"), path),
                BUNDLE.getString("Settings.datasource.remove"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices,
                BUNDLE.getString("Button.abort")); //$NON-NLS-1$
            if (decision == 0) {
              setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
              Globals.settings.getTvShowSettings().removeTvShowDataSources(path);
              setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
          }
        }
      });
      panelTvShowSourcesButtons.add(btnRemove, "1, 3, fill, top");

      JScrollPane scrollPane = new JScrollPane();
      panelTvShowDataSources.add(scrollPane, "12, 4, fill, fill");

      listExclude = new JList<>();
      scrollPane.setViewportView(listExclude);

      JPanel panelSkipFolderButtons = new JPanel();
      panelTvShowDataSources.add(panelSkipFolderButtons, "14, 4, fill, fill");
      panelSkipFolderButtons.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.DEFAULT_COLSPEC, },
          new RowSpec[] { FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

      JButton btnAddSkipFolder = new JButton(IconManager.ADD_INV);
      btnAddSkipFolder.setToolTipText(BUNDLE.getString("Settings.addignore")); //$NON-NLS-1$
      btnAddSkipFolder.setMargin(new Insets(2, 2, 2, 2));
      btnAddSkipFolder.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          File file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.ignore")); //$NON-NLS-1$
          if (file != null && file.exists() && file.isDirectory()) {
            settings.getTvShowSettings().addTvShowSkipFolder(file.getAbsolutePath());
          }
        }
      });
      panelSkipFolderButtons.add(btnAddSkipFolder, "1, 1");

      JButton btnRemoveSkipFolder = new JButton(IconManager.REMOVE_INV);
      btnRemoveSkipFolder.setToolTipText(BUNDLE.getString("Settings.removeignore")); //$NON-NLS-1$
      btnRemoveSkipFolder.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveSkipFolder.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          int row = listExclude.getSelectedIndex();
          if (row != -1) { // nothing selected
            String ingore = settings.getTvShowSettings().getTvShowSkipFolders().get(row);
            settings.getTvShowSettings().removeTvShowSkipFolder(ingore);
          }
        }
      });
      panelSkipFolderButtons.add(btnRemoveSkipFolder, "1, 3");

      JLabel lblDvdOrder = new JLabel(BUNDLE.getString("Settings.dvdorder")); //$NON-NLS-1$
      panelTvShowDataSources.add(lblDvdOrder, "2, 6, right, default");

      cbDvdOrder = new JCheckBox("");
      panelTvShowDataSources.add(cbDvdOrder, "4, 6");

      lblImageCache = new JLabel(BUNDLE.getString("Settings.imagecacheimport")); //$NON-NLS-1$
      panelTvShowDataSources.add(lblImageCache, "2, 8, right, default");

      chckbxImageCache = new JCheckBox("");
      panelTvShowDataSources.add(chckbxImageCache, "4, 8");

      lblImageCacheHint = new JLabel(BUNDLE.getString("Settings.imagecacheimporthint")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblImageCacheHint, 0.833);
      panelTvShowDataSources.add(lblImageCacheHint, "6, 8, 9, 1");
    }

    JPanel panel = new JPanel();
    add(panel, "2, 4, fill, fill");
    panel.setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
            FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

    JLabel lblTraktTv = new JLabel(BUNDLE.getString("Settings.trakt"));//$NON-NLS-1$
    panel.add(lblTraktTv, "2, 2");

    chckbxTraktTv = new JCheckBox("");
    panel.add(chckbxTraktTv, "4, 2");
    btnClearTraktTvShows = new JButton(BUNDLE.getString("Settings.trakt.cleartvshows"));//$NON-NLS-1$
    btnClearTraktTvShows.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int confirm = JOptionPane.showOptionDialog(null, BUNDLE.getString("Settings.trakt.cleartvshows.hint"),
            BUNDLE.getString("Settings.trakt.cleartvshows"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null); //$NON-NLS-1$
        if (confirm == JOptionPane.YES_OPTION) {
          TmmTask task = new ClearTraktTvTask(false, true);
          TmmTaskManager.getInstance().addUnnamedTask(task);
        }
      }
    });
    panel.add(btnClearTraktTvShows, "6, 2");

    initDataBindings();

    if (!Globals.isDonator()) {
      chckbxTraktTv.setSelected(false);
      chckbxTraktTv.setEnabled(false);
      btnClearTraktTvShows.setEnabled(false);
    }
  }

  protected void initDataBindings() {
    BeanProperty<Settings, Boolean> settingsBeanProperty = BeanProperty.create("tvShowSettings.syncTrakt");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxTraktTv, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_1 = BeanProperty.create("tvShowSettings.dvdOrder");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, cbDvdOrder, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_2 = BeanProperty.create("tvShowSettings.tvShowDataSource");
    JListBinding<String, Settings, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings, settingsBeanProperty_2,
        listDatasources);
    jListBinding.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_3 = BeanProperty.create("tvShowSettings.tvShowSkipFolders");
    JListBinding<String, Settings, JList> jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_3, listExclude);
    jListBinding_1.bind();
  }
}
