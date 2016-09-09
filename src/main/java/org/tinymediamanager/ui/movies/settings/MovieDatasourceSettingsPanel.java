/*
 * Copyright 2012 - 2016 Manuel Laggner
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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.CertificationStyle;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.movie.connector.MovieConnectors;
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
  private static final long                    serialVersionUID = -7580437046944123496L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle          BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieSettings                        settings         = MovieModuleManager.MOVIE_SETTINGS;
  private JComboBox<MovieConnectors>           cbNfoFormat;
  private JCheckBox                            cbMovieNfoFilename1;
  private JCheckBox                            cbMovieNfoFilename2;
  private JCheckBox                            cbMovieNfoFilename3;
  private JTextField                           tfAddBadword;
  private JList<String>                        listBadWords;
  private JList<String>                        listDataSources;
  private JList<String>                        listIgnore;
  private JComboBox<CertificationStyleWrapper> cbCertificationStyle;
  private JButton                              btnRemoveDatasource;
  private JButton                              btnAddDatasource;
  private JButton                              btnAddIgnore;
  private JButton                              btnRemoveIgnore;
  private JButton                              btnRemoveBadWord;
  private JButton                              btnAddBadWord;

  /**
   * Instantiates a new movie settings panel.
   */
  public MovieDatasourceSettingsPanel() {
    // UI initializations
    initComponents();
    initDataBindings();

    // logic initializations

    // data init
    // NFO filenames
    List<MovieNfoNaming> movieNfoFilenames = settings.getMovieNfoFilenames();
    if (movieNfoFilenames.contains(MovieNfoNaming.FILENAME_NFO)) {
      cbMovieNfoFilename1.setSelected(true);
    }
    if (movieNfoFilenames.contains(MovieNfoNaming.MOVIE_NFO)) {
      cbMovieNfoFilename2.setSelected(true);
    }
    if (movieNfoFilenames.contains(MovieNfoNaming.DISC_NFO)) {
      cbMovieNfoFilename3.setSelected(true);
    }

    // certification examples
    for (CertificationStyle style : CertificationStyle.values()) {
      CertificationStyleWrapper wrapper = new CertificationStyleWrapper();
      wrapper.style = style;
      cbCertificationStyle.addItem(wrapper);
      if (style == settings.getMovieCertificationStyle()) {
        cbCertificationStyle.setSelectedItem(wrapper);
      }
    }

    // listeners
    btnRemoveDatasource.addActionListener(arg0 -> {
      int row = listDataSources.getSelectedIndex();
      if (row != -1) { // nothing selected
        String path = MovieModuleManager.MOVIE_SETTINGS.getMovieDataSource().get(row);
        String[] choices = { BUNDLE.getString("Button.continue"), BUNDLE.getString("Button.abort") }; //$NON-NLS-1$
        int decision = JOptionPane.showOptionDialog(null, String.format(BUNDLE.getString("Settings.movie.datasource.remove.info"), path),
            BUNDLE.getString("Settings.datasource.remove"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices,
            BUNDLE.getString("Button.abort")); //$NON-NLS-1$
        if (decision == 0) {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          MovieModuleManager.MOVIE_SETTINGS.removeMovieDataSources(path);
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      }
    });
    btnAddDatasource.addActionListener(arg0 -> {
      Path file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.datasource.folderchooser")); //$NON-NLS-1$
      if (file != null && Files.isDirectory(file)) {
        settings.addMovieDataSources(file.toAbsolutePath().toString());
      }
    });

    btnAddIgnore.addActionListener(e -> {
      Path file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.ignore")); //$NON-NLS-1$
      if (file != null && Files.isDirectory(file)) {
        settings.addMovieSkipFolder(file.toAbsolutePath().toString());
      }
    });

    btnRemoveIgnore.addActionListener(e -> {
      int row = listIgnore.getSelectedIndex();
      if (row != -1) { // nothing selected
        String ingore = settings.getMovieSkipFolders().get(row);
        settings.removeMovieSkipFolder(ingore);
      }
    });

    btnAddBadWord.addActionListener(e -> {
      if (StringUtils.isNotEmpty(tfAddBadword.getText())) {
        MovieModuleManager.MOVIE_SETTINGS.addBadWord(tfAddBadword.getText());
        tfAddBadword.setText("");
      }
    });

    btnRemoveBadWord.addActionListener(arg0 -> {
      int row = listBadWords.getSelectedIndex();
      if (row != -1) {
        String badWord = MovieModuleManager.MOVIE_SETTINGS.getBadWords().get(row);
        MovieModuleManager.MOVIE_SETTINGS.removeBadWord(badWord);
      }
    });

    // item listener
    cbMovieNfoFilename1.addItemListener(e -> checkChanges());
    cbMovieNfoFilename2.addItemListener(e -> checkChanges());
    cbMovieNfoFilename3.addItemListener(e -> checkChanges());

    cbCertificationStyle.addItemListener(e -> checkChanges());

    // set default certification style when changing NFO style
    cbNfoFormat.addItemListener(e -> {
      if (cbNfoFormat.getSelectedItem() == MovieConnectors.MP) {
        for (int i = 0; i < cbCertificationStyle.getItemCount(); i++) {
          CertificationStyleWrapper wrapper = cbCertificationStyle.getItemAt(i);
          if (wrapper.style == CertificationStyle.TECHNICAL) {
            cbCertificationStyle.setSelectedItem(wrapper);
            break;
          }
        }
      }
      else if (cbNfoFormat.getSelectedItem() == MovieConnectors.XBMC) {
        for (int i = 0; i < cbCertificationStyle.getItemCount(); i++) {
          CertificationStyleWrapper wrapper = cbCertificationStyle.getItemAt(i);
          if (wrapper.style == CertificationStyle.LARGE) {
            cbCertificationStyle.setSelectedItem(wrapper);
            break;
          }
        }
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp:n][300lp,grow][][300lp,grow]", "[][100lp,grow][20lp][][100lp,grow][20lp][][][][][grow]"));
    {
      final JLabel lblDatasourcesT = new JLabel(BUNDLE.getString("Settings.source")); //$NON-NLS-1$
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
      final JPanel panelButtonsDatasource = new JPanel();
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
      final JLabel lblIngoreT = new JLabel(BUNDLE.getString("Settings.ignore"));
      TmmFontHelper.changeFont(lblIngoreT, 1.16667, Font.BOLD);
      add(lblIngoreT, "cell 0 3 2 1");

      final JLabel lblBadWordsT = new JLabel(BUNDLE.getString("Settings.movie.badwords")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblBadWordsT, 1.16667, Font.BOLD);
      add(lblBadWordsT, "flowx,cell 2 3 2 1");

      final JLabel lblBadWordsDesc = new JLabel(IconManager.HINT);
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
      final JPanel panelBadWords = new JPanel();
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
    {
      final JLabel lblNfoT = new JLabel(BUNDLE.getString("Settings.nfo")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblNfoT, 1.16667, Font.BOLD);
      add(lblNfoT, "cell 0 6 2 1");
    }
    {

      JLabel lblNfoFormat = new JLabel(BUNDLE.getString("Settings.nfoFormat")); //$NON-NLS-1$
      add(lblNfoFormat, "flowx,cell 1 7");
      {
        final JPanel panelNfoFormat = new JPanel();
        add(panelNfoFormat, "cell 1 8,grow");
        panelNfoFormat.setLayout(new MigLayout("insets 0", "[][]", "[][][]"));

        JLabel lblNfoFileNaming = new JLabel(BUNDLE.getString("Settings.nofFileNaming")); //$NON-NLS-1$
        panelNfoFormat.add(lblNfoFileNaming, "cell 0 0");

        cbMovieNfoFilename1 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + ".nfo"); //$NON-NLS-1$
        panelNfoFormat.add(cbMovieNfoFilename1, "cell 1 0");

        cbMovieNfoFilename2 = new JCheckBox("movie.nfo"); //$NON-NLS-1$
        panelNfoFormat.add(cbMovieNfoFilename2, "cell 1 1");

        cbMovieNfoFilename3 = new JCheckBox(BUNDLE.getString("Settings.nfo.discstyle")); //$NON-NLS-1$
        panelNfoFormat.add(cbMovieNfoFilename3, "cell 1 2");
      }

      final JLabel lblCertificationStyle = new JLabel(BUNDLE.getString("Settings.certificationformat")); //$NON-NLS-1$
      add(lblCertificationStyle, "flowx,cell 1 9 3 1");

      cbCertificationStyle = new JComboBox();
      add(cbCertificationStyle, "cell 1 9 3 1");
    }
    cbNfoFormat = new JComboBox(MovieConnectors.values());
    add(cbNfoFormat, "cell 1 7 3 1");
  }

  /**
   * check changes of checkboxes
   */
  private void checkChanges() {
    // set NFO filenames
    settings.clearMovieNfoFilenames();
    if (cbMovieNfoFilename1.isSelected()) {
      settings.addMovieNfoFilename(MovieNfoNaming.FILENAME_NFO);
    }
    if (cbMovieNfoFilename2.isSelected()) {
      settings.addMovieNfoFilename(MovieNfoNaming.MOVIE_NFO);
    }
    if (cbMovieNfoFilename3.isSelected()) {
      settings.addMovieNfoFilename(MovieNfoNaming.DISC_NFO);
    }

    CertificationStyleWrapper wrapper = (CertificationStyleWrapper) cbCertificationStyle.getSelectedItem();
    if (wrapper != null && settings.getMovieCertificationStyle() != wrapper.style) {
      settings.setMovieCertificationStyle(wrapper.style);
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
    BeanProperty<MovieSettings, MovieConnectors> settingsBeanProperty_10 = BeanProperty.create("movieConnector");
    BeanProperty<JComboBox<MovieConnectors>, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<MovieSettings, MovieConnectors, JComboBox<MovieConnectors>, Object> autoBinding_9 = Bindings
        .createAutoBinding(UpdateStrategy.READ_WRITE, settings, settingsBeanProperty_10, cbNfoFormat, jComboBoxBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<MovieSettings, List<String>> settingsBeanProperty_6 = BeanProperty.create("badWords");
    JListBinding<String, MovieSettings, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, listBadWords);
    jListBinding.bind();
    //
    BeanProperty<MovieSettings, List<String>> settingsBeanProperty_4 = BeanProperty.create("movieDataSource");
    JListBinding<String, MovieSettings, JList> jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_4, listDataSources);
    jListBinding_1.bind();
    //
    BeanProperty<MovieSettings, List<String>> settingsBeanProperty_12 = BeanProperty.create("movieSkipFolders");
    JListBinding<String, MovieSettings, JList> jListBinding_2 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_12, listIgnore);
    jListBinding_2.bind();
  }
}
