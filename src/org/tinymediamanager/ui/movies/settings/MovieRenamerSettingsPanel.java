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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieRenamer;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ScrollablePanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class MovieRenamerSettingsPanel.
 */
public class MovieRenamerSettingsPanel extends ScrollablePanel implements HierarchyListener {
  private static final long           serialVersionUID           = 5039498266207230875L;
  private static final ResourceBundle BUNDLE                     = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieSettings               settings                   = Settings.getInstance().getMovieSettings();
  private List<String>                separators                 = new ArrayList<String>(Arrays.asList("_", ".", "-"));

  /**
   * UI components
   */
  private JTextField                  tfMoviePath;
  private JTextField                  tfMovieFilename;
  private JLabel                      lblExample;
  private JCheckBox                   chckbxAsciiReplacement;

  private JCheckBox                   chckbxSpaceSubstitution;
  private JComboBox                   cbSeparator;
  private JComboBox                   cbMovieForPreview;
  private JCheckBox                   chckbxRemoveOtherNfos;
  private JCheckBox                   chckbxMoviesetSingleMovie;

  private ActionListener              actionCreateRenamerExample = new ActionListener() {
                                                                   @Override
                                                                   public void actionPerformed(ActionEvent e) {
                                                                     createRenamerExample();
                                                                   }
                                                                 };

  public MovieRenamerSettingsPanel() {
    setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"),
            FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
    // the panel renamer
    JPanel panelRenamer = new JPanel();
    panelRenamer.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.renamer"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelRenamer, "2, 2, fill, fill");
    panelRenamer.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("default:grow"),
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblMoviePath = new JLabel(BUNDLE.getString("Settings.renamer.folder")); //$NON-NLS-1$
    panelRenamer.add(lblMoviePath, "2, 2, right, default");

    tfMoviePath = new JTextField();
    tfMoviePath.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent arg0) {
        createRenamerExample();
      }

      @Override
      public void insertUpdate(DocumentEvent arg0) {
        createRenamerExample();
      }

      @Override
      public void changedUpdate(DocumentEvent arg0) {
        createRenamerExample();
      }
    });
    panelRenamer.add(tfMoviePath, "4, 2, 3, 1, fill, default");
    tfMoviePath.setColumns(10);

    JTextPane txtpntTitle = new JTextPane();
    txtpntTitle.setFont(new Font("Dialog", Font.PLAIN, 10));
    txtpntTitle.setBackground(UIManager.getColor("Panel.background"));
    txtpntTitle.setText(BUNDLE.getString("Settings.movie.renamer.info")); //$NON-NLS-1$
    txtpntTitle.setEditable(false);
    panelRenamer.add(txtpntTitle, "10, 2, 1, 16, fill, fill");

    JLabel lblMovieFilename = new JLabel(BUNDLE.getString("Settings.renamer.file")); //$NON-NLS-1$
    panelRenamer.add(lblMovieFilename, "2, 4, right, fill");

    tfMovieFilename = new JTextField();
    tfMovieFilename.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent arg0) {
        createRenamerExample();
      }

      @Override
      public void insertUpdate(DocumentEvent arg0) {
        createRenamerExample();
      }

      @Override
      public void changedUpdate(DocumentEvent arg0) {
        createRenamerExample();
      }
    });
    lblMovieFilename.setLabelFor(tfMovieFilename);
    panelRenamer.add(tfMovieFilename, "4, 4, 3, 1, fill, default");
    tfMovieFilename.setColumns(10);

    chckbxSpaceSubstitution = new JCheckBox(BUNDLE.getString("Settings.movie.renamer.spacesubstitution")); //$NON-NLS-1$
    chckbxSpaceSubstitution.addActionListener(actionCreateRenamerExample);
    panelRenamer.add(chckbxSpaceSubstitution, "4, 6");

    cbSeparator = new JComboBox(separators.toArray());
    panelRenamer.add(cbSeparator, "6, 6, fill, default");
    cbSeparator.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        checkChanges();
        createRenamerExample();
      }
    });

    chckbxMoviesetSingleMovie = new JCheckBox(BUNDLE.getString("Settings.renamer.moviesetsinglemovie")); //$NON-NLS-1$
    chckbxMoviesetSingleMovie.addActionListener(actionCreateRenamerExample);
    panelRenamer.add(chckbxMoviesetSingleMovie, "4, 8, 5, 1, fill, default");

    chckbxAsciiReplacement = new JCheckBox(BUNDLE.getString("Settings.renamer.asciireplacement")); //$NON-NLS-1$
    chckbxAsciiReplacement.addActionListener(actionCreateRenamerExample);
    panelRenamer.add(chckbxAsciiReplacement, "4, 10, 5, 1");

    JTextPane txtpntAsciiHint = new JTextPane();
    txtpntAsciiHint.setText(BUNDLE.getString("Settings.renamer.asciireplacement.hint")); //$NON-NLS-1$
    txtpntAsciiHint.setFont(new Font("Dialog", Font.PLAIN, 10));
    txtpntAsciiHint.setBackground(UIManager.getColor("Panel.background"));
    panelRenamer.add(txtpntAsciiHint, "4, 12, 5, 1, fill, fill");

    JTextPane txtrChooseAFolder = new JTextPane();
    txtrChooseAFolder.setFont(new Font("Dialog", Font.PLAIN, 10));
    txtrChooseAFolder.setText(BUNDLE.getString("Settings.movie.renamer.example")); //$NON-NLS-1$
    txtrChooseAFolder.setBackground(UIManager.getColor("Panel.background"));
    panelRenamer.add(txtrChooseAFolder, "2, 14, 3, 1, fill, bottom");

    JLabel lblExampleT = new JLabel(BUNDLE.getString("Settings.example")); //$NON-NLS-1$
    panelRenamer.add(lblExampleT, "2, 16");

    cbMovieForPreview = new JComboBox();
    cbMovieForPreview.addActionListener(actionCreateRenamerExample);
    panelRenamer.add(cbMovieForPreview, "4, 16, 5, 1, fill, default");

    lblExample = new JLabel("");
    lblExample.setFont(lblExample.getFont().deriveFont(11f));
    panelRenamer.add(lblExample, "2, 18, 9, 1");

    JSeparator separator = new JSeparator();
    panelRenamer.add(separator, "1, 20, 10, 1");

    JLabel lblCleanupOptions = new JLabel(BUNDLE.getString("Settings.cleanupoptions")); //$NON-NLS-1$
    panelRenamer.add(lblCleanupOptions, "2, 22, 3, 1");

    chckbxRemoveOtherNfos = new JCheckBox(BUNDLE.getString("Settings.renamer.removenfo")); //$NON-NLS-1$
    panelRenamer.add(chckbxRemoveOtherNfos, "4, 24, 5, 1");

    initDataBindings();

    // space separator
    String replacement = settings.getMovieRenamerSpaceReplacement();
    int index = separators.indexOf(replacement);
    if (index >= 0) {
      cbSeparator.setSelectedIndex(index);
    }
  }

  private void buildAndInstallMovieArray() {
    cbMovieForPreview.removeAllItems();
    List<Movie> allMovies = new ArrayList<Movie>(MovieList.getInstance().getMovies());
    Collections.sort(allMovies, new MovieComparator());
    for (Movie movie : allMovies) {
      MoviePreviewContainer container = new MoviePreviewContainer();
      container.movie = movie;
      cbMovieForPreview.addItem(container);
    }
  }

  private void createRenamerExample() {
    Movie movie = null;
    if (cbMovieForPreview.getSelectedItem() instanceof MoviePreviewContainer) {
      MoviePreviewContainer container = (MoviePreviewContainer) cbMovieForPreview.getSelectedItem();
      movie = container.movie;
    }

    if (movie != null) {
      String path = "";
      String filename = "";
      if (StringUtils.isNotBlank(tfMoviePath.getText())) {
        path = MovieRenamer.createDestinationForFoldername(tfMoviePath.getText(), movie);
      }
      else {
        path = movie.getPath();
      }

      if (StringUtils.isNotBlank(tfMovieFilename.getText())) {
        List<MediaFile> mediaFiles = movie.getMediaFiles(MediaFileType.VIDEO);
        if (mediaFiles.size() > 0) {
          String extension = FilenameUtils.getExtension(mediaFiles.get(0).getFilename());
          filename = MovieRenamer.createDestinationForFilename(tfMovieFilename.getText(), movie) + "." + extension;
        }
      }
      else {
        filename = movie.getMediaFiles(MediaFileType.VIDEO).get(0).getFilename();
      }

      lblExample.setText(movie.getDataSource() + File.separator + path + File.separator + filename);
    }
    else {
      lblExample.setText(BUNDLE.getString("Settings.movie.renamer.nomovie")); //$NON-NLS-1$
    }
  }

  private void checkChanges() {
    // separator
    String separator = (String) cbSeparator.getSelectedItem();
    settings.setMovieRenamerSpaceReplacement(separator);
  }

  @Override
  public void hierarchyChanged(HierarchyEvent arg0) {
    if (isShowing()) {
      buildAndInstallMovieArray();
    }
  }

  @Override
  public void addNotify() {
    super.addNotify();
    addHierarchyListener(this);
  }

  @Override
  public void removeNotify() {
    removeHierarchyListener(this);
    super.removeNotify();
  }

  protected void initDataBindings() {
    BeanProperty<MovieSettings, String> settingsBeanProperty_11 = BeanProperty.create("movieRenamerPathname");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_3 = BeanProperty.create("text");
    AutoBinding<MovieSettings, String, JTextField, String> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_11, tfMoviePath, jTextFieldBeanProperty_3);
    autoBinding_10.bind();
    //
    BeanProperty<MovieSettings, String> settingsBeanProperty_12 = BeanProperty.create("movieRenamerFilename");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_4 = BeanProperty.create("text");
    AutoBinding<MovieSettings, String, JTextField, String> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_12, tfMovieFilename, jTextFieldBeanProperty_4);
    autoBinding_11.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty = BeanProperty.create("movieRenamerSpaceSubstitution");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxSpaceSubstitution, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_1 = BeanProperty.create("movieRenamerNfoCleanup");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, chckbxRemoveOtherNfos, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_5 = BeanProperty.create("movieRenamerCreateMoviesetForSingleMovie");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_5, chckbxMoviesetSingleMovie, jCheckBoxBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_7 = BeanProperty.create("asciiReplacement");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_7, chckbxAsciiReplacement, jCheckBoxBeanProperty);
    autoBinding_5.bind();
  }

  /*****************************************************************************
   * helper classes
   *****************************************************************************/
  private class MoviePreviewContainer {
    Movie movie;

    @Override
    public String toString() {
      return movie.getTitle();
    }
  }

  private class MovieComparator implements Comparator<Movie> {
    @Override
    public int compare(Movie arg0, Movie arg1) {
      return arg0.getTitle().compareTo(arg1.getTitle());
    }
  }
}
