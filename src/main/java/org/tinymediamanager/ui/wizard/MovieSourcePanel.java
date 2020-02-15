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
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;

import net.miginfocom.swing.MigLayout;

/**
 * The class MovieSourcePanel is used to maintain the movie data sources in the wizard
 * 
 * @author Manuel Laggner
 */
class MovieSourcePanel extends JPanel {
  private static final long           serialVersionUID = -8346420911623937902L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private final MovieSettings         settings         = MovieModuleManager.SETTINGS;

  private JList<String>               listDataSources;

  public MovieSourcePanel() {
    initComponents();
    initDataBindings();
  }

  /*
   * init components
   */
  private void initComponents() {
    setLayout(new MigLayout("", "[grow]", "[][200lp,grow]"));
    {
      JLabel lblMovieDataSources = new JLabel(BUNDLE.getString("wizard.movie.datasources"));
      TmmFontHelper.changeFont(lblMovieDataSources, 1.3333, Font.BOLD);
      add(lblMovieDataSources, "cell 0 0");
    }
    JPanel panelMovieDataSources = new JPanel();

    add(panelMovieDataSources, "cell 0 1,grow");
    panelMovieDataSources.setLayout(new MigLayout("", "[grow][]", "[][200lp,grow]"));
    {
      JTextArea tpDatasourceHint = new ReadOnlyTextArea(BUNDLE.getString("wizard.datasource.hint"));
      panelMovieDataSources.add(tpDatasourceHint, "cell 0 0 2 1,grow");
    }
    {
      JScrollPane scrollPaneDataSources = new JScrollPane();
      panelMovieDataSources.add(scrollPaneDataSources, "cell 0 1,grow");

      listDataSources = new JList<>();
      scrollPaneDataSources.setViewportView(listDataSources);
    }
    {
      JButton btnAdd = new JButton(IconManager.ADD_INV);
      panelMovieDataSources.add(btnAdd, "flowy,cell 1 1,aligny top");
      btnAdd.setToolTipText(BUNDLE.getString("Button.add"));
      btnAdd.setMargin(new Insets(2, 2, 2, 2));

      JButton btnRemove = new JButton(IconManager.REMOVE_INV);
      panelMovieDataSources.add(btnRemove, "cell 1 1");
      btnRemove.setToolTipText(BUNDLE.getString("Button.remove"));
      btnRemove.setMargin(new Insets(2, 2, 2, 2));
      btnRemove.addActionListener(arg0 -> {
        int row = listDataSources.getSelectedIndex();
        if (row != -1) { // nothing selected
          String path = MovieModuleManager.SETTINGS.getMovieDataSource().get(row);
          String[] choices = { BUNDLE.getString("Button.continue"), BUNDLE.getString("Button.abort") };
          int decision = JOptionPane.showOptionDialog(null, String.format(BUNDLE.getString("Settings.movie.datasource.remove.info"), path),
              BUNDLE.getString("Settings.datasource.remove"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices,
              BUNDLE.getString("Button.abort"));
          if (decision == JOptionPane.YES_OPTION) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            MovieModuleManager.SETTINGS.removeMovieDataSources(path);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }
        }
      });
      btnAdd.addActionListener(arg0 -> {
        String path = TmmProperties.getInstance().getProperty("movie.datasource.path");
        Path file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.datasource.folderchooser"), path);
        if (file != null && Files.isDirectory(file)) {
          MovieModuleManager.SETTINGS.addMovieDataSources(file.toAbsolutePath().toString());
        }
      });
    }
  }

  protected void initDataBindings() {
    BeanProperty<MovieSettings, List<String>> settingsBeanProperty_4 = BeanProperty.create("movieDataSource");
    JListBinding<String, MovieSettings, JList> jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_4, listDataSources);
    jListBinding_1.bind();
  }
}
