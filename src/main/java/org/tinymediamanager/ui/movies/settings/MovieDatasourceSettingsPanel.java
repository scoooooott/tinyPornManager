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
package org.tinymediamanager.ui.movies.settings;

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
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.CertificationStyle;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;

import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieDatasourceSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieDatasourceSettingsPanel extends JPanel {
  private static final long           serialVersionUID = -7580437046944123496L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieSettings               settings         = MovieModuleManager.SETTINGS;
  private JTextField                  tfAddBadword;
  private JList<String>               listBadWords;
  private JList<String>               listDataSources;
  private JList<String>               listIgnore;
  private JButton                     btnRemoveDatasource;
  private JButton                     btnAddDatasource;
  private JButton                     btnAddIgnore;
  private JButton                     btnRemoveIgnore;
  private JButton                     btnRemoveBadWord;
  private JButton                     btnAddBadWord;

  /**
   * Instantiates a new movie settings panel.
   */
  public MovieDatasourceSettingsPanel() {
    // UI initializations
    initComponents();
    initDataBindings();

    // data init
    // listeners
    btnRemoveDatasource.addActionListener(arg0 -> {
      int row = listDataSources.getSelectedIndex();
      if (row != -1) { // nothing selected
        String path = MovieModuleManager.SETTINGS.getMovieDataSource().get(row);
        String[] choices = { BUNDLE.getString("Button.continue"), BUNDLE.getString("Button.abort") }; //$NON-NLS-1$
        int decision = JOptionPane.showOptionDialog(null, String.format(BUNDLE.getString("Settings.movie.datasource.remove.info"), path),
            BUNDLE.getString("Settings.datasource.remove"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices,
            BUNDLE.getString("Button.abort")); //$NON-NLS-1$
        if (decision == 0) {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          MovieModuleManager.SETTINGS.removeMovieDataSources(path);
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      }
    });
    btnAddDatasource.addActionListener(arg0 -> {
      String path = TmmProperties.getInstance().getProperty("movie.datasource.path"); //$NON-NLS-1$
      Path file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.datasource.folderchooser"), path); //$NON-NLS-1$
      if (file != null && Files.isDirectory(file)) {
        settings.addMovieDataSources(file.toAbsolutePath().toString());
      }
    });

    btnAddIgnore.addActionListener(e -> {
      String path = TmmProperties.getInstance().getProperty("movie.ignore.path"); //$NON-NLS-1$
      Path file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.ignore"), path); //$NON-NLS-1$
      if (file != null && Files.isDirectory(file)) {
        settings.addSkipFolder(file.toAbsolutePath().toString());
      }
    });

    btnRemoveIgnore.addActionListener(e -> {
      int row = listIgnore.getSelectedIndex();
      if (row != -1) { // nothing selected
        String ingore = settings.getSkipFolder().get(row);
        settings.removeSkipFolder(ingore);
      }
    });

    btnAddBadWord.addActionListener(e -> {
      if (StringUtils.isNotEmpty(tfAddBadword.getText())) {
        MovieModuleManager.SETTINGS.addBadWord(tfAddBadword.getText());
        tfAddBadword.setText("");
      }
    });

    btnRemoveBadWord.addActionListener(arg0 -> {
      int row = listBadWords.getSelectedIndex();
      if (row != -1) {
        String badWord = MovieModuleManager.SETTINGS.getBadWord().get(row);
        MovieModuleManager.SETTINGS.removeBadWord(badWord);
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp:n][300lp,grow][25lp][300lp,grow]", "[][100lp,grow][20lp][][100lp,grow][20lp][][][][][grow]"));
    {
      JLabel lblDatasourcesT = new JLabel(BUNDLE.getString("Settings.source")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblDatasourcesT, 1.16667, Font.BOLD);
      add(lblDatasourcesT, "cell 0 0 2 1");
    }
    {
      JScrollPane scrollPaneDataSources = new JScrollPane();
      add(scrollPaneDataSources, "flowx,cell 1 1,grow");

      listDataSources = new JList<>();
      scrollPaneDataSources.setViewportView(listDataSources);
    }
    {
      JPanel panelButtonsDatasource = new JPanel();
      add(panelButtonsDatasource, "cell 1 1,aligny top");
      panelButtonsDatasource.setLayout(new MigLayout("", "[]", "[][][]"));

      btnAddDatasource = new JButton(IconManager.ADD_INV);
      panelButtonsDatasource.add(btnAddDatasource, "cell 0 0");
      btnAddDatasource.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$
      btnAddDatasource.setMargin(new Insets(2, 2, 2, 2));

      btnRemoveDatasource = new JButton(IconManager.REMOVE_INV);
      panelButtonsDatasource.add(btnRemoveDatasource, "cell 0 1");
      btnRemoveDatasource.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
      btnRemoveDatasource.setMargin(new Insets(2, 2, 2, 2));
    }

    {
      JLabel lblIngoreT = new JLabel(BUNDLE.getString("Settings.ignore"));
      TmmFontHelper.changeFont(lblIngoreT, 1.16667, Font.BOLD);
      add(lblIngoreT, "cell 0 3 2 1");

      JLabel lblBadWordsT = new JLabel(BUNDLE.getString("Settings.movie.badwords")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblBadWordsT, 1.16667, Font.BOLD);
      add(lblBadWordsT, "flowx,cell 2 3 2 1");

      JLabel lblBadWordsDesc = new JLabel(IconManager.HINT);
      lblBadWordsDesc.setToolTipText(BUNDLE.getString("Settings.movie.badwords.hint")); //$NON-NLS-1$
      add(lblBadWordsDesc, "cell 2 3 2 1");
    }

    {
      JScrollPane scrollPaneIgnore = new JScrollPane();
      add(scrollPaneIgnore, "flowx,cell 1 4,grow");

      listIgnore = new JList<>();
      scrollPaneIgnore.setViewportView(listIgnore);

      JPanel panelIgnoreButtons = new JPanel();
      add(panelIgnoreButtons, "cell 1 4,aligny top");
      panelIgnoreButtons.setLayout(new MigLayout("", "[]", "[][][]"));

      btnAddIgnore = new JButton(IconManager.ADD_INV);
      panelIgnoreButtons.add(btnAddIgnore, "cell 0 0");
      btnAddIgnore.setToolTipText(BUNDLE.getString("Settings.addignore")); //$NON-NLS-1$
      btnAddIgnore.setMargin(new Insets(2, 2, 2, 2));

      btnRemoveIgnore = new JButton(IconManager.REMOVE_INV);
      panelIgnoreButtons.add(btnRemoveIgnore, "cell 0 1");
      btnRemoveIgnore.setToolTipText(BUNDLE.getString("Settings.removeignore")); //$NON-NLS-1$
      btnRemoveIgnore.setMargin(new Insets(2, 2, 2, 2));
    }
    {
      JPanel panelBadWords = new JPanel();
      add(panelBadWords, "cell 3 4,grow");
      panelBadWords.setLayout(new MigLayout("insets 0", "[][]", "[100lp,grow][]"));

      JScrollPane scrollPaneBadWords = new JScrollPane();
      panelBadWords.add(scrollPaneBadWords, "cell 0 0,grow");

      listBadWords = new JList<>();
      scrollPaneBadWords.setViewportView(listBadWords);

      btnRemoveBadWord = new JButton(IconManager.REMOVE_INV);
      panelBadWords.add(btnRemoveBadWord, "cell 1 0,aligny bottom");
      btnRemoveBadWord.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
      btnRemoveBadWord.setMargin(new Insets(2, 2, 2, 2));

      tfAddBadword = new JTextField();
      panelBadWords.add(tfAddBadword, "cell 0 1,growx");
      tfAddBadword.setColumns(10);

      btnAddBadWord = new JButton(IconManager.ADD_INV);
      panelBadWords.add(btnAddBadWord, "cell 1 1");
      btnAddBadWord.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$
      btnAddBadWord.setMargin(new Insets(2, 2, 2, 2));
    }
  }

  /*
   * helper for displaying the combobox with an example
   */
  private class CertificationStyleWrapper {
    private CertificationStyle style;

    @Override
    public String toString() {
      String bundleTag = BUNDLE.getString("Settings.certification." + style.name().toLowerCase());
      return bundleTag.replace("{}", CertificationStyle.formatCertification(Certification.DE_FSK16, style));
    }
  }

  protected void initDataBindings() {
    BeanProperty<MovieSettings, List<String>> settingsBeanProperty_6 = BeanProperty.create("badWord");
    JListBinding<String, MovieSettings, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, listBadWords);
    jListBinding.bind();
    //
    BeanProperty<MovieSettings, List<String>> settingsBeanProperty_4 = BeanProperty.create("movieDataSource");
    JListBinding<String, MovieSettings, JList> jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_4, listDataSources);
    jListBinding_1.bind();
    //
    BeanProperty<MovieSettings, List<String>> settingsBeanProperty_12 = BeanProperty.create("skipFolder");
    JListBinding<String, MovieSettings, JList> jListBinding_2 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_12, listIgnore);
    jListBinding_2.bind();
  }
}
