/*
 * Copyright 2012 - 2014 Manuel Laggner
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
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
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ObjectProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.connector.MovieConnectors;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ScrollablePanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieSettingsPanel extends ScrollablePanel {
  private static final long           serialVersionUID = -7580437046944123496L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private Settings                    settings         = Settings.getInstance();

  /**
   * UI elements
   */
  private JTable                      tableMovieSources;
  private JComboBox                   cbNfoFormat;
  private JCheckBox                   cbMovieNfoFilename1;
  private JCheckBox                   cbMovieNfoFilename2;
  private JCheckBox                   chckbxMultipleMoviesPerFolder;
  private JCheckBox                   chckbxImageCache;
  private JTextField                  tfAddBadword;
  private JList                       listBadWords;
  private JCheckBox                   chckbxYear;
  private JCheckBox                   chckbxTrailer;
  private JCheckBox                   chckbxSubtitles;
  private JCheckBox                   chckbxImages;
  private JCheckBox                   chckbxNfo;
  private JCheckBox                   chckbxRuntimeFromMf;
  private JCheckBox                   chckbxTraktTv;

  /**
   * Instantiates a new movie settings panel.
   */
  public MovieSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JPanel panelGeneral = new JPanel();
    panelGeneral.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.general"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelGeneral, "2, 2, fill, fill");
    panelGeneral.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

    JLabel lblVisiblecolumns = new JLabel(BUNDLE.getString("Settings.movie.visiblecolumns")); //$NON-NLS-1$
    panelGeneral.add(lblVisiblecolumns, "2, 2, right, default");

    chckbxYear = new JCheckBox(BUNDLE.getString("metatag.year")); //$NON-NLS-1$
    panelGeneral.add(chckbxYear, "4, 2");

    chckbxNfo = new JCheckBox(BUNDLE.getString("metatag.nfo")); //$NON-NLS-1$
    panelGeneral.add(chckbxNfo, "6, 2");

    chckbxImages = new JCheckBox(BUNDLE.getString("metatag.images")); //$NON-NLS-1$
    panelGeneral.add(chckbxImages, "8, 2");

    chckbxTrailer = new JCheckBox(BUNDLE.getString("metatag.trailer")); //$NON-NLS-1$
    panelGeneral.add(chckbxTrailer, "4, 4");

    chckbxSubtitles = new JCheckBox(BUNDLE.getString("metatag.subtitles")); //$NON-NLS-1$
    panelGeneral.add(chckbxSubtitles, "6, 4");

    JSeparator separator_4 = new JSeparator();
    panelGeneral.add(separator_4, "2, 6, 7, 1");

    JLabel lblImageCache = new JLabel(BUNDLE.getString("Settings.imagecacheimport"));
    panelGeneral.add(lblImageCache, "2, 8, right, default");

    chckbxImageCache = new JCheckBox(BUNDLE.getString("Settings.imagecacheimporthint")); //$NON-NLS-1$
    TmmFontHelper.changeFont(chckbxImageCache, 0.833);
    panelGeneral.add(chckbxImageCache, "4, 8, 5, 1");

    JLabel lblRuntimeFromMedia = new JLabel(BUNDLE.getString("Settings.runtimefrommediafile"));
    panelGeneral.add(lblRuntimeFromMedia, "2, 10, right, default");

    chckbxRuntimeFromMf = new JCheckBox("");
    panelGeneral.add(chckbxRuntimeFromMf, "4, 10");

    JSeparator separator = new JSeparator();
    panelGeneral.add(separator, "2, 12, 7, 1");

    JLabel lblTraktTv = new JLabel(BUNDLE.getString("Settings.trakt"));//$NON-NLS-1$
    panelGeneral.add(lblTraktTv, "2, 14");

    chckbxTraktTv = new JCheckBox("");
    panelGeneral.add(chckbxTraktTv, "4, 14, 5, 1");

    JPanel panelMovieDataSources = new JPanel();

    panelMovieDataSources.setBorder(new TitledBorder(null,
        BUNDLE.getString("Settings.movie.datasource"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelMovieDataSources, "2, 4, fill, fill");
    panelMovieDataSources.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(72dlu;default):grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("100px:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, }));

    JScrollPane scrollPane = new JScrollPane();
    panelMovieDataSources.add(scrollPane, "2, 2, 5, 1, fill, fill");

    tableMovieSources = new JTable();
    tableMovieSources.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    scrollPane.setViewportView(tableMovieSources);

    JPanel panelMovieSourcesButtons = new JPanel();
    panelMovieDataSources.add(panelMovieSourcesButtons, "8, 2, fill, top");
    panelMovieSourcesButtons.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JButton btnAdd = new JButton(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    btnAdd.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        File file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.datasource.folderchooser")); //$NON-NLS-1$
        if (file != null && file.exists() && file.isDirectory()) {
          settings.getMovieSettings().addMovieDataSources(file.getAbsolutePath());
        }
      }
    });

    panelMovieSourcesButtons.add(btnAdd, "2, 1, fill, top");

    JButton btnRemove = new JButton(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
    btnRemove.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int row = tableMovieSources.convertRowIndexToModel(tableMovieSources.getSelectedRow());
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
      }
    });
    panelMovieSourcesButtons.add(btnRemove, "2, 3, fill, top");

    JLabel lblAllowMultipleMoviesPerFolder = new JLabel(BUNDLE.getString("Settings.multipleMovies")); //$NON-NLS-1$
    panelMovieDataSources.add(lblAllowMultipleMoviesPerFolder, "2, 4, right, default");

    chckbxMultipleMoviesPerFolder = new JCheckBox("");
    panelMovieDataSources.add(chckbxMultipleMoviesPerFolder, "4, 4");

    JTextPane tpMultipleMoviesHint = new JTextPane();
    TmmFontHelper.changeFont(tpMultipleMoviesHint, 0.833);
    tpMultipleMoviesHint.setBackground(UIManager.getColor("Panel.background"));
    tpMultipleMoviesHint.setText(BUNDLE.getString("Settings.multipleMovies.hint")); //$NON-NLS-1$
    tpMultipleMoviesHint.setEditable(false);
    panelMovieDataSources.add(tpMultipleMoviesHint, "6, 4, 5, 1, fill, fill");

    JSeparator separator_1 = new JSeparator();
    panelMovieDataSources.add(separator_1, "2, 6, 9, 1");

    JLabel lblNfoFormat = new JLabel(BUNDLE.getString("Settings.nfoFormat")); //$NON-NLS-1$
    panelMovieDataSources.add(lblNfoFormat, "2, 8, right, default");

    cbNfoFormat = new JComboBox(MovieConnectors.values());
    panelMovieDataSources.add(cbNfoFormat, "4, 8, 3, 1, fill, default");

    JLabel lblNfoFileNaming = new JLabel(BUNDLE.getString("Settings.nofFileNaming")); //$NON-NLS-1$
    panelMovieDataSources.add(lblNfoFileNaming, "2, 10, right, default");

    cbMovieNfoFilename1 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + ".nfo"); //$NON-NLS-1$
    panelMovieDataSources.add(cbMovieNfoFilename1, "4, 10, 3, 1");

    cbMovieNfoFilename2 = new JCheckBox("movie.nfo");
    panelMovieDataSources.add(cbMovieNfoFilename2, "4, 11, 3, 1");

    JPanel panelBadWords = new JPanel();
    panelBadWords.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.movie.badwords"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelBadWords, "4, 2, 1, 3, fill, fill");
    panelBadWords.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, }));

    JTextPane txtpntBadWordsHint = new JTextPane();
    txtpntBadWordsHint.setBackground(UIManager.getColor("Panel.background"));
    txtpntBadWordsHint.setText(BUNDLE.getString("Settings.movie.badwords.hint")); //$NON-NLS-1$
    TmmFontHelper.changeFont(txtpntBadWordsHint, 0.833);
    panelBadWords.add(txtpntBadWordsHint, "2, 2, 3, 1, fill, default");

    JScrollPane scpBadWords = new JScrollPane();
    panelBadWords.add(scpBadWords, "2, 4, fill, fill");

    listBadWords = new JList();
    scpBadWords.setViewportView(listBadWords);

    JButton btnRemoveBadWord = new JButton(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
    btnRemoveBadWord.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int row = listBadWords.getSelectedIndex();
        if (row != -1) {
          String badWord = MovieModuleManager.MOVIE_SETTINGS.getBadWords().get(row);
          MovieModuleManager.MOVIE_SETTINGS.removeBadWord(badWord);
        }
      }
    });
    panelBadWords.add(btnRemoveBadWord, "4, 4, default, bottom");

    tfAddBadword = new JTextField();
    tfAddBadword.setColumns(10);
    panelBadWords.add(tfAddBadword, "2, 6, fill, default");

    JButton btnAddBadWord = new JButton(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    btnAddBadWord.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (StringUtils.isNotEmpty(tfAddBadword.getText())) {
          MovieModuleManager.MOVIE_SETTINGS.addBadWord(tfAddBadword.getText());
          tfAddBadword.setText("");
        }
      }
    });
    panelBadWords.add(btnAddBadWord, "4, 6");

    initDataBindings();

    // NFO filenames
    List<MovieNfoNaming> movieNfoFilenames = settings.getMovieSettings().getMovieNfoFilenames();
    if (movieNfoFilenames.contains(MovieNfoNaming.FILENAME_NFO)) {
      cbMovieNfoFilename1.setSelected(true);
    }

    if (movieNfoFilenames.contains(MovieNfoNaming.MOVIE_NFO)) {
      cbMovieNfoFilename2.setSelected(true);
    }

    // item listener
    cbMovieNfoFilename1.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });
    cbMovieNfoFilename2.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });

    if (!Globals.isDonator()) {
      chckbxTraktTv.setSelected(false);
      chckbxTraktTv.setEnabled(false);
    }

    // column headings
    tableMovieSources.getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("Settings.source")); //$NON-NLS-1$
  }

  /**
   * check changes of checkboxes
   */
  private void checkChanges() {
    // set NFO filenames
    settings.getMovieSettings().clearMovieNfoFilenames();
    if (cbMovieNfoFilename1.isSelected()) {
      settings.getMovieSettings().addMovieNfoFilename(MovieNfoNaming.FILENAME_NFO);
    }
    if (cbMovieNfoFilename2.isSelected()) {
      settings.getMovieSettings().addMovieNfoFilename(MovieNfoNaming.MOVIE_NFO);
    }
  }

  protected void initDataBindings() {
    BeanProperty<Settings, List<String>> settingsBeanProperty_4 = BeanProperty.create("movieSettings.movieDataSource");
    JTableBinding<String, Settings, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, settings, settingsBeanProperty_4,
        tableMovieSources);
    //
    ObjectProperty<String> stringObjectProperty = ObjectProperty.create();
    jTableBinding.addColumnBinding(stringObjectProperty);
    //
    jTableBinding.bind();
    //
    BeanProperty<Settings, MovieConnectors> settingsBeanProperty_10 = BeanProperty.create("movieSettings.movieConnector");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<Settings, MovieConnectors, JComboBox, Object> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_10, cbNfoFormat, jComboBoxBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_2 = BeanProperty.create("movieSettings.detectMovieMultiDir");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, chckbxMultipleMoviesPerFolder, jCheckBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_3 = BeanProperty.create("movieSettings.buildImageCacheOnImport");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_3, chckbxImageCache, jCheckBoxBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_6 = BeanProperty.create("movieSettings.badWords");
    JListBinding<String, Settings, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, listBadWords);
    jListBinding.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_8 = BeanProperty.create("movieSettings.runtimeFromMediaInfo");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_8, chckbxRuntimeFromMf, jCheckBoxBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_9 = BeanProperty.create("movieSettings.yearColumnVisible");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, chckbxYear, jCheckBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_13 = BeanProperty.create("movieSettings.trailerColumnVisible");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_13, chckbxTrailer, jCheckBoxBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_14 = BeanProperty.create("movieSettings.subtitleColumnVisible");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_14, chckbxSubtitles, jCheckBoxBeanProperty);
    autoBinding_12.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_15 = BeanProperty.create("movieSettings.imageColumnVisible");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_13 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_15, chckbxImages, jCheckBoxBeanProperty);
    autoBinding_13.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_16 = BeanProperty.create("movieSettings.nfoColumnVisible");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_14 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_16, chckbxNfo, jCheckBoxBeanProperty);
    autoBinding_14.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty = BeanProperty.create("movieSettings.syncTrakt");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxTraktTv, jCheckBoxBeanProperty);
    autoBinding.bind();
  }
}
