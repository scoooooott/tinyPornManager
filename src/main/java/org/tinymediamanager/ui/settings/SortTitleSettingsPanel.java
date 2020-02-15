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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.TmmLabel;

import net.miginfocom.swing.MigLayout;

/**
 * The class SortTitleSettingsPanel is used to maintain the sort title prefixes
 * 
 * @author Manuel Laggner
 */
class SortTitleSettingsPanel extends JPanel {
  private static final long           serialVersionUID = 1857926059556024932L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private Settings                    settings         = Settings.getInstance();

  private JList<String>               listSortPrefixes;
  private JTextField                  tfSortPrefix;
  private JButton                     btnRemoveSortPrefix;
  private JButton                     btnAddSortPrefix;

  SortTitleSettingsPanel() {
    // init UI
    initComponents();
    initDataBindings();

    // init data
    btnAddSortPrefix.addActionListener(e -> {
      if (StringUtils.isNotEmpty(tfSortPrefix.getText())) {
        Globals.settings.addTitlePrefix(tfSortPrefix.getText());
        tfSortPrefix.setText("");
        MovieList.getInstance().invalidateTitleSortable();
        TvShowList.getInstance().invalidateTitleSortable();
      }
    });
    btnRemoveSortPrefix.addActionListener(arg0 -> {
      int row = listSortPrefixes.getSelectedIndex();
      if (row != -1) {
        String prefix = Globals.settings.getTitlePrefix().get(row);
        Globals.settings.removeTitlePrefix(prefix);
        MovieList.getInstance().invalidateTitleSortable();
        TvShowList.getInstance().invalidateTitleSortable();
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[grow]", "[]"));
    {
      JPanel panelSorttitle = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][100lp][][grow]", "[]"));

      JLabel lblSorttitleT = new TmmLabel(BUNDLE.getString("Settings.sorting"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelSorttitle, lblSorttitleT, true);
      add(collapsiblePanel, "cell 0 0,growx, wmin 0");
      {
        JTextArea tpSortingHint = new ReadOnlyTextArea(BUNDLE.getString("Settings.sorting.info")); // $NON-NLS-1$
        panelSorttitle.add(tpSortingHint, "cell 1 0 3 1,growx");

        JScrollPane scrollPane = new JScrollPane();
        panelSorttitle.add(scrollPane, "cell 1 1,grow");

        listSortPrefixes = new JList<>();
        scrollPane.setViewportView(listSortPrefixes);

        btnRemoveSortPrefix = new JButton(IconManager.REMOVE_INV);
        btnRemoveSortPrefix.setToolTipText(BUNDLE.getString("Button.remove"));
        panelSorttitle.add(btnRemoveSortPrefix, "cell 2 1,aligny bottom, growx");

        tfSortPrefix = new JTextField();
        panelSorttitle.add(tfSortPrefix, "cell 1 2,growx");

        btnAddSortPrefix = new JButton(IconManager.ADD_INV);
        btnAddSortPrefix.setToolTipText(BUNDLE.getString("Button.add"));
        panelSorttitle.add(btnAddSortPrefix, "cell 2 2, growx");
      }
    }
  }

  @SuppressWarnings("rawtypes")
  protected void initDataBindings() {
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_1 = BeanProperty.create("titlePrefix");
    JListBinding<String, Settings, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings, settingsBeanProperty_1,
        listSortPrefixes);
    jListBinding.bind();
  }
}
