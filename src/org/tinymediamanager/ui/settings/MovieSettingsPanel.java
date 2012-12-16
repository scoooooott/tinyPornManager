/*
 * Copyright 2012 Manuel Laggner
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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ObjectProperty;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieConnectors;
import org.tinymediamanager.core.movie.MovieNfoNaming;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

// TODO: Auto-generated Javadoc
/**
 * The Class MovieSettingsPanel.
 */
public class MovieSettingsPanel extends JPanel {

  /** The settings. */
  private Settings   settings = Settings.getInstance();

  /** The table movie sources. */
  private JTable     tableMovieSources;

  /** The cb nfo format. */
  private JComboBox  cbNfoFormat;

  /** The cb movie nfo filename1. */
  private JCheckBox  cbMovieNfoFilename1;

  /** The cb movie nfo filename2. */
  private JCheckBox  cbMovieNfoFilename2;

  /** The tf movie path. */
  private JTextField tfMoviePath;

  /** The tf movie filename. */
  private JTextField tfMovieFilename;

  /**
   * Instantiates a new movie settings panel.
   */
  public MovieSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JPanel panelMovieDataSources = new JPanel();

    panelMovieDataSources.setBorder(new TitledBorder(null, "Data Sources", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    add(panelMovieDataSources, "2, 2, fill, top");
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
    panelMovieDataSources.add(panelMovieSourcesButtons, "8, 2");
    panelMovieSourcesButtons
        .setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JButton btnAdd = new JButton("Add");
    btnAdd.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
          settings.addMovieDataSources(fileChooser.getSelectedFile().getAbsolutePath());
        }
      }
    });

    panelMovieSourcesButtons.add(btnAdd, "2, 2, fill, top");

    JButton btnRemove = new JButton("Remove");
    btnRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        int row = tableMovieSources.convertRowIndexToModel(tableMovieSources.getSelectedRow());
        String path = Globals.settings.getMovieDataSource().get(row);
        String[] choices = { "Continue", "Abort" };
        int decision = JOptionPane.showOptionDialog(null, "If you remove " + path
            + " from your data sources, all movies inside this path will also be removed. Continue?", "Remove datasource", JOptionPane.YES_NO_OPTION,
            JOptionPane.PLAIN_MESSAGE, null, choices, "Abort");
        if (decision == 0) {
          Globals.settings.removeMovieDataSources(path);
        }
      }
    });
    panelMovieSourcesButtons.add(btnRemove, "2, 4, fill, top");

    JLabel lblNfoFormat = new JLabel("NFO format");
    panelMovieDataSources.add(lblNfoFormat, "2, 4, right, default");

    cbNfoFormat = new JComboBox(MovieConnectors.values());
    panelMovieDataSources.add(cbNfoFormat, "4, 4, fill, default");

    JLabel lblNfoFileNaming = new JLabel("NFO file naming");
    panelMovieDataSources.add(lblNfoFileNaming, "2, 6");

    cbMovieNfoFilename1 = new JCheckBox("<filename>.nfo");
    panelMovieDataSources.add(cbMovieNfoFilename1, "4, 6");

    cbMovieNfoFilename2 = new JCheckBox("movie.nfo");
    panelMovieDataSources.add(cbMovieNfoFilename2, "4, 7");

    // the panel renamer
    JPanel panelRenamer = new JPanel();
    panelRenamer.setBorder(new TitledBorder(null, "Renamer", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    add(panelRenamer, "2, 4, fill, top");
    panelRenamer.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), }));

    JLabel lblMoviePath = new JLabel("Folder name");
    panelRenamer.add(lblMoviePath, "2, 2, right, default");

    tfMoviePath = new JTextField();
    panelRenamer.add(tfMoviePath, "4, 2, fill, default");
    tfMoviePath.setColumns(10);

    JTextPane txtpntTitle = new JTextPane();
    txtpntTitle.setFont(new Font("Dialog", Font.PLAIN, 10));
    txtpntTitle.setBackground(UIManager.getColor("Panel.background"));
    txtpntTitle.setText("Available pattern:\n$T = Title\n$O = OriginalTitle\n$1 = first letter of the title\n$Y = Year");
    txtpntTitle.setEditable(false);
    panelRenamer.add(txtpntTitle, "6, 2, 1, 5, fill, fill");

    JLabel lblMovieFilename = new JLabel("File name");
    panelRenamer.add(lblMovieFilename, "2, 4, right, fill");

    tfMovieFilename = new JTextField();
    lblMovieFilename.setLabelFor(tfMovieFilename);
    panelRenamer.add(tfMovieFilename, "4, 4, fill, default");
    tfMovieFilename.setColumns(10);

    JTextPane txtrChooseAFolder = new JTextPane();
    txtrChooseAFolder.setFont(new Font("Dialog", Font.PLAIN, 10));
    txtrChooseAFolder
        .setText("Choose a folder and file renaming pattern.\nExample:\nDatasource = /media/movies\nFolder name = $1/$T [$Y]\nFile name = $T\n\nResult:\nFolder name = /media/movies/A/Aladdin [1992]/\nFile name = Aladdin.avi");
    txtrChooseAFolder.setBackground(UIManager.getColor("Panel.background"));
    panelRenamer.add(txtrChooseAFolder, "2, 6, 3, 1, fill, fill");

    initDataBindings();

    // NFO filenames
    List<MovieNfoNaming> movieNfoFilenames = settings.getMovieNfoFilenames();
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

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    BeanProperty<Settings, List<String>> settingsBeanProperty_4 = BeanProperty.create("movieDataSource");
    JTableBinding<String, Settings, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, settings, settingsBeanProperty_4,
        tableMovieSources);
    //
    ObjectProperty<String> stringObjectProperty = ObjectProperty.create();
    jTableBinding.addColumnBinding(stringObjectProperty).setColumnName("Source");
    //
    jTableBinding.bind();
    //
    BeanProperty<Settings, MovieConnectors> settingsBeanProperty_10 = BeanProperty.create("movieConnector");
    AutoBinding<Settings, MovieConnectors, JComboBox, Object> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_10, cbNfoFormat, jComboBoxBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_11 = BeanProperty.create("movieRenamerPathname");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_3 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_11, tfMoviePath, jTextFieldBeanProperty_3);
    autoBinding_10.bind();
    //
    BeanProperty<Settings, String> settingsBeanProperty_12 = BeanProperty.create("movieRenamerFilename");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_4 = BeanProperty.create("text");
    AutoBinding<Settings, String, JTextField, String> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_12, tfMovieFilename, jTextFieldBeanProperty_4);
    autoBinding_11.bind();
    //
  }

  // check changes of checkboxes
  /**
   * Check changes.
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
  }
}
