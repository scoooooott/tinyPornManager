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

import java.awt.Color;
import java.awt.Font;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieRenamer;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.table.TmmTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import net.miginfocom.swing.MigLayout;

/**
 * The class MovieRenamerSettingsPanel.
 */
public class MovieRenamerSettingsPanel extends JPanel implements HierarchyListener {
  private static final long              serialVersionUID           = 5039498266207230875L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle    BUNDLE                     = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieSettings                  settings                   = MovieModuleManager.SETTINGS;
  private List<String>                   separators                 = new ArrayList<>(Arrays.asList("_", ".", "-"));
  private EventList<MovieRenamerExample> exampleEventList           = null;

  /**
   * UI components
   */
  private JTextField                     tfMoviePath;
  private JTextField                     tfMovieFilename;
  private JLabel                         lblExample;
  private JCheckBox                      chckbxAsciiReplacement;

  private JCheckBox                      chckbxSpaceSubstitution;
  private JComboBox                      cbSeparator;
  private JComboBox                      cbMovieForPreview;
  private JCheckBox                      chckbxRemoveOtherNfos;
  private JCheckBox                      chckbxMoviesetSingleMovie;

  private ActionListener                 actionCreateRenamerExample = e -> createRenamerExample();
  private TmmTable                       tableExamples;
  private JLabel                         lblMMDWarning;

  public MovieRenamerSettingsPanel() {
    exampleEventList = GlazedLists
        .threadSafeList(new ObservableElementList<>(new BasicEventList<>(), GlazedLists.beanConnector(MovieRenamerExample.class)));

    // UI initializations
    initComponents();
    initDataBindings();

    // data init
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

    // space separator
    String replacement = settings.getRenamerSpaceReplacement();
    int index = separators.indexOf(replacement);
    if (index >= 0) {
      cbSeparator.setSelectedIndex(index);
    }

    cbMovieForPreview.addActionListener(actionCreateRenamerExample);
    cbSeparator.addActionListener(arg0 -> {
      checkChanges();
      createRenamerExample();
    });

    chckbxMoviesetSingleMovie.addActionListener(actionCreateRenamerExample);
    chckbxAsciiReplacement.addActionListener(actionCreateRenamerExample);
    chckbxSpaceSubstitution.addActionListener(actionCreateRenamerExample);

    // examples
    exampleEventList.add(new MovieRenamerExample("${title}"));
    exampleEventList.add(new MovieRenamerExample("${originalTitle}"));
    exampleEventList.add(new MovieRenamerExample("${title[0]}"));
    exampleEventList.add(new MovieRenamerExample("${title[0,2]}"));
    exampleEventList.add(new MovieRenamerExample("${titleSortable}"));
    exampleEventList.add(new MovieRenamerExample("${year}"));
    exampleEventList.add(new MovieRenamerExample("${movieSet.title}"));
    exampleEventList.add(new MovieRenamerExample("${movieSet.titleSortable}"));
    exampleEventList.add(new MovieRenamerExample("${imdb}"));
    exampleEventList.add(new MovieRenamerExample("${certification}"));
    exampleEventList.add(new MovieRenamerExample("${directors[0].name}"));
    exampleEventList.add(new MovieRenamerExample("${genres[0].name}"));
    exampleEventList.add(new MovieRenamerExample("${tags[0]}"));
    exampleEventList.add(new MovieRenamerExample("${language}"));
    exampleEventList.add(new MovieRenamerExample("${videoResolution}"));
    exampleEventList.add(new MovieRenamerExample("${videoCodec}"));
    exampleEventList.add(new MovieRenamerExample("${videoFormat}"));
    exampleEventList.add(new MovieRenamerExample("${audioCodec}"));
    exampleEventList.add(new MovieRenamerExample("${audioChannels}"));
    exampleEventList.add(new MovieRenamerExample("${mediaSource}"));
    exampleEventList.add(new MovieRenamerExample("${3Dformat}"));
    exampleEventList.add(new MovieRenamerExample("${edition}"));
  }

  private void initComponents() {
    setLayout(new MigLayout("hidemode 1", "[25lp,shrink 0][15lp,shrink 0][][400lp:500lp:500lp][10lp:10lp,grow]",
        "[][][][][][][][5lp][][][][][][20lp][][][][100lp,grow]"));
    {
      final JLabel lblPatternAndOptionsT = new JLabel(BUNDLE.getString("Settings.movie.renamer.title")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblPatternAndOptionsT, 1.16667, Font.BOLD);
      add(lblPatternAndOptionsT, "cell 0 0 4 1");
    }
    {
      JLabel lblMoviePath = new JLabel(BUNDLE.getString("Settings.renamer.folder")); //$NON-NLS-1$
      add(lblMoviePath, "flowx,cell 1 1 2 1,alignx right");

      tfMoviePath = new JTextField();
      add(tfMoviePath, "cell 3 1,growx");

      JLabel lblDefault = new JLabel(BUNDLE.getString("Settings.default")); //$NON-NLS-1$
      add(lblDefault, "flowx,cell 1 2 2 1,alignx right");
      TmmFontHelper.changeFont(lblDefault, 0.833);

      JTextArea tpDefaultFolderPattern = new ReadOnlyTextArea(MovieSettings.DEFAULT_RENAMER_FOLDER_PATTERN);
      add(tpDefaultFolderPattern, "cell 3 2, growx");
      TmmFontHelper.changeFont(tpDefaultFolderPattern, 0.833);
    }
    {
      JLabel lblMovieFilename = new JLabel(BUNDLE.getString("Settings.renamer.file")); //$NON-NLS-1$
      add(lblMovieFilename, "cell 1 3 2 1,alignx right");

      tfMovieFilename = new JTextField();
      add(tfMovieFilename, "cell 3 3, growx");

      JLabel lblDefault = new JLabel(BUNDLE.getString("Settings.default")); //$NON-NLS-1$
      add(lblDefault, "cell 1 4 2 1,alignx right");
      TmmFontHelper.changeFont(lblDefault, 0.833);

      JTextArea tpDefaultFilePattern = new ReadOnlyTextArea(MovieSettings.DEFAULT_RENAMER_FILE_PATTERN);
      add(tpDefaultFilePattern, "cell 3 4, growx");
      TmmFontHelper.changeFont(tpDefaultFilePattern, 0.833);
    }
    {
      JTextArea tpChooseAFolder = new ReadOnlyTextArea(BUNDLE.getString("Settings.movie.renamer.example")); //$NON-NLS-1$
      add(tpChooseAFolder, "cell 2 5 2 1, growx");
      TmmFontHelper.changeFont(tpChooseAFolder, 0.833);
    }
    {
      lblMMDWarning = new JLabel(BUNDLE.getString("Settings.renamer.folder.warning"));
      lblMMDWarning.setForeground(Color.red);
      add(lblMMDWarning, "cell 1 6 3 1,growx");
    }
    {
      chckbxSpaceSubstitution = new JCheckBox(BUNDLE.getString("Settings.movie.renamer.spacesubstitution")); //$NON-NLS-1$
      add(chckbxSpaceSubstitution, "flowx,cell 1 8 3 1");

      cbSeparator = new JComboBox(separators.toArray());
      add(cbSeparator, "cell 1 8 3 1");
    }
    {
      chckbxAsciiReplacement = new JCheckBox(BUNDLE.getString("Settings.renamer.asciireplacement")); //$NON-NLS-1$
      add(chckbxAsciiReplacement, "flowx,cell 1 9 4 1");
    }

    JLabel lblAsciiHint = new JLabel(BUNDLE.getString("Settings.renamer.asciireplacement.hint")); //$NON-NLS-1$
    add(lblAsciiHint, "cell 2 10 3 1,aligny top");
    TmmFontHelper.changeFont(lblAsciiHint, 0.833);
    {
      chckbxMoviesetSingleMovie = new JCheckBox(BUNDLE.getString("Settings.renamer.moviesetsinglemovie")); //$NON-NLS-1$
      add(chckbxMoviesetSingleMovie, "cell 1 11 4 1");
    }
    {
      chckbxRemoveOtherNfos = new JCheckBox(BUNDLE.getString("Settings.renamer.removenfo")); //$NON-NLS-1$
      add(chckbxRemoveOtherNfos, "cell 1 12 3 1");
    }

    {
      final JLabel lblExampleT = new JLabel(BUNDLE.getString("Settings.example")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblExampleT, 1.16667, Font.BOLD);
      add(lblExampleT, "cell 0 14 4 1");
    }
    {
      JLabel lblExampleT = new JLabel(BUNDLE.getString("tmm.movie")); //$NON-NLS-1$
      add(lblExampleT, "flowx,cell 1 15 4 1");

      cbMovieForPreview = new JComboBox();
      add(cbMovieForPreview, "cell 1 15 4 1");

      lblExample = new JLabel("");
      add(lblExample, "cell 1 16 4 1");
      TmmFontHelper.changeFont(lblExample, 0.916, Font.BOLD);

      DefaultEventTableModel<MovieRenamerExample> exampleTableModel = new DefaultEventTableModel<>(
          GlazedListsSwing.swingThreadProxyList(exampleEventList), new MovieRenamerExampleTableFormat());
      tableExamples = new TmmTable(exampleTableModel);
      JScrollPane scrollPaneExamples = new JScrollPane(tableExamples);
      tableExamples.configureScrollPane(scrollPaneExamples);
      add(scrollPaneExamples, "cell 1 17 4 1,grow");
    }
  }

  private void buildAndInstallMovieArray() {
    cbMovieForPreview.removeAllItems();
    List<Movie> allMovies = new ArrayList<>(MovieList.getInstance().getMovies());
    Collections.sort(allMovies, new MovieComparator());
    for (Movie movie : allMovies) {
      MoviePreviewContainer container = new MoviePreviewContainer();
      container.movie = movie;
      cbMovieForPreview.addItem(container);
    }
  }

  private void createRenamerExample() {
    Movie movie = null;

    // empty is valid (although not unique)
    if (!tfMoviePath.getText().isEmpty() && !MovieRenamer.isFolderPatternUnique(tfMoviePath.getText())) {
      lblMMDWarning.setVisible(true);
    }
    else {
      lblMMDWarning.setVisible(false);
    }

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
        // the old folder name
        path = movie.getPathNIO().getFileName().toString();
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

      // create examples
      for (MovieRenamerExample example : exampleEventList) {
        example.createExample(movie);
      }
      try {
        TableColumnResizer.adjustColumnPreferredWidths(tableExamples, 7);
      }
      catch (Exception e) {
      }
    }
    else {
      lblExample.setText(BUNDLE.getString("Settings.movie.renamer.nomovie")); //$NON-NLS-1$
    }
  }

  private void checkChanges() {
    // separator
    String separator = (String) cbSeparator.getSelectedItem();
    settings.setRenamerSpaceReplacement(separator);
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

  @SuppressWarnings("unused")
  private class MovieRenamerExample extends AbstractModelObject {
    private String token;
    private String description;
    private String example = "";

    public MovieRenamerExample(String token) {
      this.token = token;
      try {
        this.description = BUNDLE.getString("Settings.movie.renamer." + token); //$NON-NLS-1$
      }
      catch (Exception e) {
        this.description = "";
      }
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public String getExample() {
      return example;
    }

    public void setExample(String example) {
      this.example = example;
    }

    private void createExample(Movie movie) {
      String oldValue = example;
      if (movie == null) {
        example = "";
      }
      else {
        example = MovieRenamer.createDestination(token, movie, true);
      }
      firePropertyChange("example", oldValue, example);
    }
  }

  private class MovieRenamerExampleTableFormat implements TableFormat<MovieRenamerExample> {
    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return null;

        case 1:
          return BUNDLE.getString("Settings.renamer.token"); //$NON-NLS-1$

        case 2:
          return BUNDLE.getString("Settings.renamer.value"); //$NON-NLS-1$

      }
      return null;
    }

    @Override
    public Object getColumnValue(MovieRenamerExample baseObject, int column) {
      switch (column) {
        case 0:
          return baseObject.token;

        case 1:
          return baseObject.description;

        case 2:
          return baseObject.example;

        default:
          break;
      }
      return null;
    }
  }

  protected void initDataBindings() {
    BeanProperty<MovieSettings, String> settingsBeanProperty_11 = BeanProperty.create("renamerPathname");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_3 = BeanProperty.create("text");
    AutoBinding<MovieSettings, String, JTextField, String> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_11, tfMoviePath, jTextFieldBeanProperty_3);
    autoBinding_10.bind();
    //
    BeanProperty<MovieSettings, String> settingsBeanProperty_12 = BeanProperty.create("renamerFilename");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_4 = BeanProperty.create("text");
    AutoBinding<MovieSettings, String, JTextField, String> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_12, tfMovieFilename, jTextFieldBeanProperty_4);
    autoBinding_11.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty = BeanProperty.create("renamerSpaceSubstitution");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxSpaceSubstitution, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_1 = BeanProperty.create("renamerNfoCleanup");
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
}
