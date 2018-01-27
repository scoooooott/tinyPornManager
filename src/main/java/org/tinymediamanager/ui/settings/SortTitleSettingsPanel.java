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
package org.tinymediamanager.ui.settings;

import java.awt.Font;
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
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;

import net.miginfocom.swing.MigLayout;

/**
 * The class SortTitleSettingsPanel is used to maintain the sort title prefixes
 * 
 * @author Manuel Laggner
 */
public class SortTitleSettingsPanel extends JPanel {
  private static final long           serialVersionUID = 1857926059556024932L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private Settings                    settings         = Settings.getInstance();

  private JList<String>               listSortPrefixes;
  private JTextField                  tfSortPrefix;
  private JButton                     btnRemoveSortPrefix;
  private JButton                     btnAddSortPrefix;

  public SortTitleSettingsPanel() {
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
    setLayout(new MigLayout("", "[25lp][150lp][grow]", "[][][400lp][]"));
    {
      final JLabel lblSortingT = new JLabel(BUNDLE.getString("Settings.sorting")); // $NON-NLS-1$
      TmmFontHelper.changeFont(lblSortingT, 1.16667, Font.BOLD);
      add(lblSortingT, "cell 0 0 2 1");
    }
    {
      final JTextArea tpSortingHint = new ReadOnlyTextArea(BUNDLE.getString("Settings.sorting.info")); // $NON-NLS-1$
      add(tpSortingHint, "cell 1 1 2 1,grow");
    }
    {
      final JScrollPane scrollPane = new JScrollPane();
      add(scrollPane, "cell 1 2,grow");
      listSortPrefixes = new JList<>();
      scrollPane.setViewportView(listSortPrefixes);
    }
    {
      btnRemoveSortPrefix = new JButton(IconManager.REMOVE_INV);
      btnRemoveSortPrefix.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
      add(btnRemoveSortPrefix, "cell 2 2,aligny bottom");
    }
    {
      tfSortPrefix = new JTextField();
      add(tfSortPrefix, "cell 1 3,growx");
      tfSortPrefix.setColumns(10);
    }
    {
      btnAddSortPrefix = new JButton(IconManager.ADD_INV);
      btnAddSortPrefix.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$
      add(btnAddSortPrefix, "cell 2 3");
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
