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
package org.tinymediamanager.ui.wizard;

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
import javax.swing.JTextArea;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;

import net.miginfocom.swing.MigLayout;

/**
 * The class TvShowSourcePanel is used to maintain the TV show data sources in the wizard
 * 
 * @author Manuel Laggner
 */
class TvShowSourcePanel extends JPanel {
  private static final long           serialVersionUID = -7126616245313008341L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private final TvShowSettings        settings         = TvShowModuleManager.SETTINGS;

  private JList<String>               listDataSources;
  private JCheckBox                   cbDvdOrder;

  public TvShowSourcePanel() {
    initComponents();
    initDataBindings();
  }

  /*
   * init components
   */
  private void initComponents() {
    setLayout(new MigLayout("", "[grow]", "[][grow]"));

    JLabel lblDataSource = new JLabel(BUNDLE.getString("wizard.tvshow.datasources"));
    TmmFontHelper.changeFont(lblDataSource, 1.3333, Font.BOLD);
    add(lblDataSource, "cell 0 0");

    JPanel panelTvShowDataSources = new JPanel();
    add(panelTvShowDataSources, "cell 0 1,grow");
    panelTvShowDataSources.setLayout(new MigLayout("", "[grow][]", "[][grow][]"));

    JTextArea tpDatasourceHint = new ReadOnlyTextArea(BUNDLE.getString("wizard.datasource.hint"));
    panelTvShowDataSources.add(tpDatasourceHint, "cell 0 0 2 1,growx");

    JScrollPane scrollPaneDataSources = new JScrollPane();
    panelTvShowDataSources.add(scrollPaneDataSources, "cell 0 1,grow");

    listDataSources = new JList<>();
    scrollPaneDataSources.setViewportView(listDataSources);

    cbDvdOrder = new JCheckBox(BUNDLE.getString("Settings.dvdorder"));
    panelTvShowDataSources.add(cbDvdOrder, "flowx,cell 0 2");

    JButton btnAdd = new JButton(IconManager.ADD_INV);
    panelTvShowDataSources.add(btnAdd, "flowy,cell 1 1,aligny top");
    btnAdd.setToolTipText(BUNDLE.getString("Button.add"));
    btnAdd.setMargin(new Insets(2, 2, 2, 2));
    btnAdd.addActionListener(arg0 -> {
      String path = TmmProperties.getInstance().getProperty("tvshow.datasource.path");
      Path file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.tvshowdatasource.folderchooser"), path);
      if (file != null && Files.isDirectory(file)) {
        settings.addTvShowDataSources(file.toAbsolutePath().toString());
        TmmProperties.getInstance().putProperty("tvshow.datasource.path", file.toAbsolutePath().toString());
      }
    });

    JButton btnRemove = new JButton(IconManager.REMOVE_INV);
    panelTvShowDataSources.add(btnRemove, "cell 1 1");
    btnRemove.setToolTipText(BUNDLE.getString("Button.remove"));
    btnRemove.setMargin(new Insets(2, 2, 2, 2));
    btnRemove.addActionListener(arg0 -> {
      int row = listDataSources.getSelectedIndex();
      if (row != -1) { // nothing selected
        String path = settings.getTvShowDataSource().get(row);
        String[] choices = { BUNDLE.getString("Button.continue"), BUNDLE.getString("Button.abort") };
        int decision = JOptionPane.showOptionDialog(null, String.format(BUNDLE.getString("Settings.tvshowdatasource.remove.info"), path),
            BUNDLE.getString("Settings.datasource.remove"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices,
            BUNDLE.getString("Button.abort"));
        if (decision == JOptionPane.YES_OPTION) {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          settings.removeTvShowDataSources(path);
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      }
    });
  }

  /*
   * init data bindings
   */
  @SuppressWarnings("rawtypes")
  protected void initDataBindings() {
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    BeanProperty<TvShowSettings, Boolean> settingsBeanProperty_1 = BeanProperty.create("dvdOrder");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, cbDvdOrder, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<TvShowSettings, List<String>> settingsBeanProperty_2 = BeanProperty.create("tvShowDataSource");
    JListBinding<String, TvShowSettings, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, listDataSources);
    jListBinding.bind();
  }
}
