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
package org.tinymediamanager.ui.wizard;

import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JTextPane;

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
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class TvShowSourcePanel is used to maintain the TV show data sources in the wizard
 * 
 * @author Manuel Laggner
 */
class TvShowSourcePanel extends JPanel {
  private static final long           serialVersionUID = -7126616245313008341L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

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
    setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.LINE_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.LINE_GAP_ROWSPEC, }));
    JPanel panelTvShowDataSources = new JPanel();
    add(panelTvShowDataSources, "2, 2, fill, fill");
    panelTvShowDataSources.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("50dlu:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LINE_GAP_ROWSPEC,
            RowSpec.decode("50dlu:grow"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
            RowSpec.decode("default:grow(3)"), FormSpecs.RELATED_GAP_ROWSPEC, }));

    JLabel lblDataSource = new JLabel(BUNDLE.getString("wizard.tvshow.datasources")); //$NON-NLS-1$
    panelTvShowDataSources.add(lblDataSource, "2, 2, 7, 1");

    JTextPane tpDatasourceHint = new JTextPane();
    tpDatasourceHint.setText(BUNDLE.getString("wizard.datasource.hint")); //$NON-NLS-1$
    tpDatasourceHint.setOpaque(false);
    panelTvShowDataSources.add(tpDatasourceHint, "2, 3, 7, 1, fill, fill");

    JScrollPane scrollPaneDataSources = new JScrollPane();
    panelTvShowDataSources.add(scrollPaneDataSources, "2, 5, 5, 1, fill, fill");

    listDataSources = new JList<>();
    scrollPaneDataSources.setViewportView(listDataSources);

    JPanel panelTvShowSourcesButtons = new JPanel();
    panelTvShowDataSources.add(panelTvShowSourcesButtons, "8, 5, fill, top");
    panelTvShowSourcesButtons.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.DEFAULT_COLSPEC, },
        new RowSpec[] { FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

    JButton btnAdd = new JButton(IconManager.LIST_ADD);
    btnAdd.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    btnAdd.setMargin(new Insets(2, 2, 2, 2));
    btnAdd.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String path = TmmProperties.getInstance().getProperty("tvshow.datasource.path");
        Path file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.tvshowdatasource.folderchooser"), path); //$NON-NLS-1$
        if (file != null && Files.isDirectory(file)) {
          settings.addTvShowDataSources(file.toAbsolutePath().toString());
          TmmProperties.getInstance().putProperty("tvshow.datasource.path", file.toAbsolutePath().toString());
        }
      }
    });

    panelTvShowSourcesButtons.add(btnAdd, "1, 1, fill, top");

    JButton btnRemove = new JButton(IconManager.LIST_REMOVE);
    btnRemove.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
    btnRemove.setMargin(new Insets(2, 2, 2, 2));
    btnRemove.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int row = listDataSources.getSelectedIndex();
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
      }
    });
    panelTvShowSourcesButtons.add(btnRemove, "1, 3, fill, top");

    JLabel lblDvdOrder = new JLabel(BUNDLE.getString("Settings.dvdorder")); //$NON-NLS-1$
    panelTvShowDataSources.add(lblDvdOrder, "2, 7, right, default");

    cbDvdOrder = new JCheckBox("");
    panelTvShowDataSources.add(cbDvdOrder, "4, 7");
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
