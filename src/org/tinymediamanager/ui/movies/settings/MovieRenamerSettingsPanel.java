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
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieRenamer;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ZebraJTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class MovieRenamerSettingsPanel.
 */
public class MovieRenamerSettingsPanel extends JPanel implements HierarchyListener {
  private static final long              serialVersionUID           = 5039498266207230875L;
  private static final ResourceBundle    BUNDLE                     = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieSettings                  settings                   = Settings.getInstance().getMovieSettings();
  private List<String>                   separators                 = new ArrayList<String>(Arrays.asList("_", ".", "-"));
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

  private ActionListener                 actionCreateRenamerExample = new ActionListener() {
                                                                      @Override
                                                                      public void actionPerformed(ActionEvent e) {
                                                                        createRenamerExample();
                                                                      }
                                                                    };
  private JScrollPane                    scrollPane;
  private JTable                         tableExamples;
  private JPanel                         panelExample;

  public MovieRenamerSettingsPanel() {
    setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"),
            FormFactory.RELATED_GAP_ROWSPEC, }));
    // the panel renamer
    JPanel panelRenamer = new JPanel();
    panelRenamer.setBorder(new TitledBorder(null,
        BUNDLE.getString("Settings.movie.renamer.title"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelRenamer, "2, 2, fill, fill");
    panelRenamer.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.UNRELATED_GAP_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.UNRELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow(3)"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, }));

    chckbxAsciiReplacement = new JCheckBox(BUNDLE.getString("Settings.renamer.asciireplacement")); //$NON-NLS-1$
    chckbxAsciiReplacement.addActionListener(actionCreateRenamerExample);

    chckbxMoviesetSingleMovie = new JCheckBox(BUNDLE.getString("Settings.renamer.moviesetsinglemovie")); //$NON-NLS-1$
    chckbxMoviesetSingleMovie.addActionListener(actionCreateRenamerExample);

    chckbxSpaceSubstitution = new JCheckBox(BUNDLE.getString("Settings.movie.renamer.spacesubstitution")); //$NON-NLS-1$
    chckbxSpaceSubstitution.addActionListener(actionCreateRenamerExample);

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

    JLabel lblMoviePath = new JLabel(BUNDLE.getString("Settings.renamer.folder")); //$NON-NLS-1$
    panelRenamer.add(lblMoviePath, "2, 4, right, default");
    panelRenamer.add(tfMoviePath, "4, 4, fill, default");
    tfMoviePath.setColumns(10);
    panelRenamer.add(chckbxSpaceSubstitution, "8, 4");

    cbSeparator = new JComboBox(separators.toArray());
    panelRenamer.add(cbSeparator, "10, 4, fill, default");
    cbSeparator.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        checkChanges();
        createRenamerExample();
      }
    });

    JLabel lblMovieFilename = new JLabel(BUNDLE.getString("Settings.renamer.file")); //$NON-NLS-1$
    panelRenamer.add(lblMovieFilename, "2, 6, right, fill");

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
    panelRenamer.add(tfMovieFilename, "4, 6, fill, default");
    tfMovieFilename.setColumns(10);
    lblMovieFilename.setLabelFor(tfMovieFilename);
    panelRenamer.add(chckbxMoviesetSingleMovie, "8, 6, 5, 1, fill, default");

    JTextPane txtrChooseAFolder = new JTextPane();
    TmmFontHelper.changeFont(txtrChooseAFolder, 0.833);
    txtrChooseAFolder.setText(BUNDLE.getString("Settings.movie.renamer.example")); //$NON-NLS-1$
    txtrChooseAFolder.setBackground(UIManager.getColor("Panel.background"));
    panelRenamer.add(txtrChooseAFolder, "2, 8, 3, 3, fill, top");
    panelRenamer.add(chckbxAsciiReplacement, "8, 8, 5, 1");

    JTextPane txtpntAsciiHint = new JTextPane();
    txtpntAsciiHint.setText(BUNDLE.getString("Settings.renamer.asciireplacement.hint")); //$NON-NLS-1$
    TmmFontHelper.changeFont(txtpntAsciiHint, 0.833);
    txtpntAsciiHint.setBackground(UIManager.getColor("Panel.background"));
    panelRenamer.add(txtpntAsciiHint, "8, 10, 5, 1, fill, fill");

    chckbxRemoveOtherNfos = new JCheckBox(BUNDLE.getString("Settings.renamer.removenfo")); //$NON-NLS-1$
    panelRenamer.add(chckbxRemoveOtherNfos, "8, 12, 5, 1");

    exampleEventList = GlazedLists.threadSafeList(new ObservableElementList<MovieRenamerExample>(new BasicEventList<MovieRenamerExample>(),
        GlazedLists.beanConnector(MovieRenamerExample.class)));
    DefaultEventTableModel<MovieRenamerExample> exampleTableModel = new DefaultEventTableModel<MovieRenamerExample>(
        GlazedListsSwing.swingThreadProxyList(exampleEventList), new MovieRenamerExampleTableFormat());

    panelExample = new JPanel();
    panelExample.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.example"), TitledBorder.LEADING, TitledBorder.TOP, null, null));//$NON-NLS-1$
    add(panelExample, "2, 4, fill, fill");
    panelExample.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("50dlu:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, }));

    JLabel lblExampleT = new JLabel(BUNDLE.getString("tmm.movie")); //$NON-NLS-1$
    panelExample.add(lblExampleT, "2, 2");

    cbMovieForPreview = new JComboBox();
    panelExample.add(cbMovieForPreview, "4, 2");

    lblExample = new JLabel("");
    panelExample.add(lblExample, "2, 4, 3, 1");
    TmmFontHelper.changeFont(lblExample, 0.916, Font.BOLD);
    tableExamples = new ZebraJTable(exampleTableModel);
    scrollPane = ZebraJTable.createStripedJScrollPane(tableExamples);
    scrollPane.setViewportView(tableExamples);
    panelExample.add(scrollPane, "2, 6, 3, 1, fill, fill");

    JTextPane txtpntTitle = new JTextPane();
    panelExample.add(txtpntTitle, "2, 8, 3, 1");
    TmmFontHelper.changeFont(txtpntTitle, 0.833);
    txtpntTitle.setBackground(UIManager.getColor("Panel.background"));
    txtpntTitle.setText(BUNDLE.getString("Settings.movie.renamer.optional")); //$NON-NLS-1$
    txtpntTitle.setEditable(false);
    cbMovieForPreview.addActionListener(actionCreateRenamerExample);

    initDataBindings();

    // space separator
    String replacement = settings.getMovieRenamerSpaceReplacement();
    int index = separators.indexOf(replacement);
    if (index >= 0) {
      cbSeparator.setSelectedIndex(index);
    }

    // examples
    exampleEventList.add(new MovieRenamerExample("$T"));
    exampleEventList.add(new MovieRenamerExample("$O"));
    exampleEventList.add(new MovieRenamerExample("$1"));
    exampleEventList.add(new MovieRenamerExample("$E"));
    exampleEventList.add(new MovieRenamerExample("$2"));
    exampleEventList.add(new MovieRenamerExample("$Y"));
    exampleEventList.add(new MovieRenamerExample("$M"));
    exampleEventList.add(new MovieRenamerExample("$N"));
    exampleEventList.add(new MovieRenamerExample("$I"));
    exampleEventList.add(new MovieRenamerExample("$C"));
    exampleEventList.add(new MovieRenamerExample("$L"));
    exampleEventList.add(new MovieRenamerExample("$R"));
    exampleEventList.add(new MovieRenamerExample("$A"));
    exampleEventList.add(new MovieRenamerExample("$V"));
    exampleEventList.add(new MovieRenamerExample("$F"));
    exampleEventList.add(new MovieRenamerExample("$S"));
    exampleEventList.add(new MovieRenamerExample("$3"));
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
}
