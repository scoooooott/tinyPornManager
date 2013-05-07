/*
 * Copyright 2012 - 2013 Manuel Laggner
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
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
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.connector.MovieConnectors;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieSettingsPanel extends JPanel {

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = 1L;

  /** The settings. */
  private Settings                    settings         = Settings.getInstance();

  /** The table movie sources. */
  private JTable                      tableMovieSources;

  /** The cb nfo format. */
  private JComboBox                   cbNfoFormat;

  /** The cb movie nfo filename1. */
  private JCheckBox                   cbMovieNfoFilename1;

  /** The cb movie nfo filename2. */
  private JCheckBox                   cbMovieNfoFilename2;

  /** The tf movie path. */
  private JTextField                  tfMoviePath;

  /** The tf movie filename. */
  private JTextField                  tfMovieFilename;

  /** The tf sort prefix. */
  private JTextField                  tfSortPrefix;

  /** The list sort prefixes. */
  private JList                       listSortPrefixes;

  // /** The tf filetype. */
  // private JTextField tfFiletype;
  //
  // /** The list filetypes. */
  // private JList listFiletypes;

  /**
   * Instantiates a new movie settings panel.
   */
  public MovieSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default"),
        FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JPanel panelMovieDataSources = new JPanel();

    panelMovieDataSources.setBorder(new TitledBorder(null,
        BUNDLE.getString("Settings.datasource"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelMovieDataSources, "2, 2, fill, fill");
    panelMovieDataSources.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(72dlu;default)"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(66dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("max(44dlu;default)"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("100px:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JScrollPane scrollPane = new JScrollPane();
    panelMovieDataSources.add(scrollPane, "2, 2, 5, 1, fill, fill");

    tableMovieSources = new JTable();
    scrollPane.setViewportView(tableMovieSources);

    JPanel panelMovieSourcesButtons = new JPanel();
    panelMovieDataSources.add(panelMovieSourcesButtons, "8, 2, default, top");
    panelMovieSourcesButtons.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JButton btnAdd = new JButton(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    btnAdd.addActionListener(new ActionListener() {
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
      public void actionPerformed(ActionEvent arg0) {
        int row = tableMovieSources.convertRowIndexToModel(tableMovieSources.getSelectedRow());
        if (row != -1) { // nothing selected
          String path = Globals.settings.getMovieSettings().getMovieDataSource().get(row);
          String[] choices = { BUNDLE.getString("Button.continue"), BUNDLE.getString("Button.abort") }; //$NON-NLS-1$
          int decision = JOptionPane.showOptionDialog(null, String.format(BUNDLE.getString("Settings.datasource.remove.info"), path),
              BUNDLE.getString("Settings.datasource.remove"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices,
              BUNDLE.getString("Button.abort")); //$NON-NLS-1$
          if (decision == 0) {
            Globals.settings.getMovieSettings().removeMovieDataSources(path);
          }
        }
      }
    });
    panelMovieSourcesButtons.add(btnRemove, "2, 3, fill, top");

    JLabel lblNfoFormat = new JLabel(BUNDLE.getString("Settings.nfoFormat")); //$NON-NLS-1$
    panelMovieDataSources.add(lblNfoFormat, "2, 4, right, default");

    cbNfoFormat = new JComboBox(MovieConnectors.values());
    panelMovieDataSources.add(cbNfoFormat, "4, 4, fill, default");

    JLabel lblNfoFileNaming = new JLabel(BUNDLE.getString("Settings.nofFileNaming")); //$NON-NLS-1$
    panelMovieDataSources.add(lblNfoFileNaming, "2, 6, right, default");

    cbMovieNfoFilename1 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + ".nfo"); //$NON-NLS-1$
    panelMovieDataSources.add(cbMovieNfoFilename1, "4, 6");

    cbMovieNfoFilename2 = new JCheckBox("movie.nfo");
    panelMovieDataSources.add(cbMovieNfoFilename2, "4, 7");

    // JPanel panelFiletypes = new JPanel();
    // panelFiletypes.setBorder(new TitledBorder(
    //        UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("Settings.filetypes"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    // add(panelFiletypes, "4, 2, fill, fill");
    // panelFiletypes.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
    // FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
    // RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
    //
    // JScrollPane scrollPane_1 = new JScrollPane();
    // panelFiletypes.add(scrollPane_1, "2, 2, fill, fill");
    //
    // listFiletypes = new JList();
    // scrollPane_1.setViewportView(listFiletypes);
    //
    //    JButton btnRemoveFiletype = new JButton(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
    // btnRemoveFiletype.addActionListener(new ActionListener() {
    // @Override
    // public void actionPerformed(ActionEvent arg0) {
    // int row = listFiletypes.getSelectedIndex();
    // if (row != -1) {
    // String prefix = Globals.settings.getVideoFileType().get(row);
    // Globals.settings.removeVideoFileType(prefix);
    // }
    // }
    // });
    // panelFiletypes.add(btnRemoveFiletype, "4, 2, default, bottom");
    //
    // tfFiletype = new JTextField();
    // panelFiletypes.add(tfFiletype, "2, 4, fill, default");
    // tfFiletype.setColumns(10);
    //
    //    JButton btnAddFiletype = new JButton(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    // btnAddFiletype.addActionListener(new ActionListener() {
    // @Override
    // public void actionPerformed(ActionEvent e) {
    // if (StringUtils.isNotEmpty(tfFiletype.getText())) {
    // Globals.settings.addVideoFileTypes(tfFiletype.getText());
    // tfFiletype.setText("");
    // }
    // }
    // });
    // panelFiletypes.add(btnAddFiletype, "4, 4");

    JPanel panelSortOptions = new JPanel();
    panelSortOptions.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("Settings.sorting"),
        TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelSortOptions, "4, 2, fill, fill");
    panelSortOptions.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblSortingPrefixes = new JLabel(BUNDLE.getString("Settings.titlePrefix")); //$NON-NLS-1$
    panelSortOptions.add(lblSortingPrefixes, "2, 2");

    JScrollPane scrollPaneSortPrefixes = new JScrollPane();
    panelSortOptions.add(scrollPaneSortPrefixes, "2, 4, fill, fill");

    listSortPrefixes = new JList();
    scrollPaneSortPrefixes.setViewportView(listSortPrefixes);

    JButton btnRemoveSortPrefix = new JButton(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
    btnRemoveSortPrefix.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int row = listSortPrefixes.getSelectedIndex();
        if (row != -1) {
          String prefix = Globals.settings.getTitlePrefix().get(row);
          Globals.settings.removeTitlePrefix(prefix);
        }
      }
    });
    panelSortOptions.add(btnRemoveSortPrefix, "4, 4, default, bottom");

    tfSortPrefix = new JTextField();
    panelSortOptions.add(tfSortPrefix, "2, 6, fill, default");
    tfSortPrefix.setColumns(10);

    JButton btnAddSortPrefix = new JButton(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    btnAddSortPrefix.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (StringUtils.isNotEmpty(tfSortPrefix.getText())) {
          Globals.settings.addTitlePrefix(tfSortPrefix.getText());
          tfSortPrefix.setText("");
        }
      }
    });
    panelSortOptions.add(btnAddSortPrefix, "4, 6");

    JTextPane tpSortingHints = new JTextPane();
    tpSortingHints.setFont(new Font("Dialog", Font.PLAIN, 10));
    tpSortingHints.setText(BUNDLE.getString("Settings.sorting.info")); //$NON-NLS-1$
    tpSortingHints.setBackground(UIManager.getColor("Panel.background"));
    panelSortOptions.add(tpSortingHints, "2, 8, 3, 1, fill, fill");

    // the panel renamer
    JPanel panelRenamer = new JPanel();
    panelRenamer.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.renamer"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelRenamer, "2, 4, fill, fill");
    panelRenamer.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), }));

    JLabel lblMoviePath = new JLabel(BUNDLE.getString("Settings.renamer.folder")); //$NON-NLS-1$
    panelRenamer.add(lblMoviePath, "2, 2, right, default");

    tfMoviePath = new JTextField();
    panelRenamer.add(tfMoviePath, "4, 2, fill, default");
    tfMoviePath.setColumns(10);

    JTextPane txtpntTitle = new JTextPane();
    txtpntTitle.setFont(new Font("Dialog", Font.PLAIN, 10));
    txtpntTitle.setBackground(UIManager.getColor("Panel.background"));
    txtpntTitle.setText(BUNDLE.getString("Settings.renamer.info")); //$NON-NLS-1$
    txtpntTitle.setEditable(false);
    panelRenamer.add(txtpntTitle, "6, 2, 1, 5, fill, fill");

    JLabel lblMovieFilename = new JLabel(BUNDLE.getString("Settings.renamer.file")); //$NON-NLS-1$
    panelRenamer.add(lblMovieFilename, "2, 4, right, fill");

    tfMovieFilename = new JTextField();
    lblMovieFilename.setLabelFor(tfMovieFilename);
    panelRenamer.add(tfMovieFilename, "4, 4, fill, default");
    tfMovieFilename.setColumns(10);

    JTextPane txtrChooseAFolder = new JTextPane();
    txtrChooseAFolder.setFont(new Font("Dialog", Font.PLAIN, 10));
    txtrChooseAFolder.setText(BUNDLE.getString("Settings.renamer.example")); //$NON-NLS-1$
    txtrChooseAFolder.setBackground(UIManager.getColor("Panel.background"));
    panelRenamer.add(txtrChooseAFolder, "2, 6, 3, 1, fill, fill");

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
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });
    cbMovieNfoFilename2.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });

  }

  // check changes of checkboxes
  /**
   * Check changes.
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
    jTableBinding.addColumnBinding(stringObjectProperty).setColumnName("Source");
    //
    jTableBinding.bind();
    //
    BeanProperty<Settings, MovieConnectors> settingsBeanProperty_10 = BeanProperty.create("movieSettings.movieConnector");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<Settings, MovieConnectors, JComboBox, Object> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_10, cbNfoFormat, jComboBoxBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_11 = BeanProperty.create("movieSettings.movieRenamerPathname");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_3 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_11, tfMoviePath, jTextFieldBeanProperty_3);
    autoBinding_10.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_12 = BeanProperty.create("movieSettings.movieRenamerFilename");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_4 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_12, tfMovieFilename, jTextFieldBeanProperty_4);
    autoBinding_11.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty = BeanProperty.create("titlePrefix");
    JListBinding<String, Settings, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings, settingsBeanProperty,
        listSortPrefixes);
    jListBinding.bind();
    // //
    // BeanProperty<Settings, List<String>> settingsBeanProperty_1 = BeanProperty.create("videoFileType");
    // JListBinding<String, Settings, JList> jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
    // settingsBeanProperty_1, listFiletypes);
    // jListBinding_1.bind();
  }
}
