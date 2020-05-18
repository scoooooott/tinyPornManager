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
package org.tinymediamanager.ui.movies.settings;

import static org.tinymediamanager.ui.TmmFontHelper.H3;
import static org.tinymediamanager.ui.TmmFontHelper.L1;
import static org.tinymediamanager.ui.TmmFontHelper.L2;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieRenamer;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.ScrollingEventDelegator;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.EnhancedTextField;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.SettingsPanelFactory;
import org.tinymediamanager.ui.components.TmmLabel;
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
  private static final long              serialVersionUID = 5039498266207230875L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle    BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());
  private static final Logger            LOGGER           = LoggerFactory.getLogger(MovieRenamerSettingsPanel.class);

  private MovieSettings                  settings         = MovieModuleManager.SETTINGS;
  private List<String>                   spaceReplacement = new ArrayList<>(Arrays.asList("_", ".", "-"));
  private List<String>                   colonReplacement = new ArrayList<>(Arrays.asList(" ", "-"));
  private EventList<MovieRenamerExample> exampleEventList;

  /**
   * UI components
   */
  private EnhancedTextField              tfMoviePath;
  private EnhancedTextField              tfMovieFilename;
  private JLabel                         lblExample;
  private JCheckBox                      chckbxAsciiReplacement;

  private JCheckBox                      chckbxFoldernameSpaceReplacement;
  private JComboBox                      cbFoldernameSpaceReplacement;
  private JCheckBox                      chckbxFilenameSpaceReplacement;
  private JComboBox                      cbFilenameSpaceReplacement;
  private JComboBox                      cbMovieForPreview;
  private JCheckBox                      chckbxRemoveOtherNfos;
  private JCheckBox                      chckbxMoviesetSingleMovie;

  private TmmTable                       tableExamples;
  private ReadOnlyTextArea               taWarning;
  private JComboBox                      cbColonReplacement;

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

    // foldername space replacement
    String replacement = settings.getRenamerPathnameSpaceReplacement();
    int index = spaceReplacement.indexOf(replacement);
    if (index >= 0) {
      cbFoldernameSpaceReplacement.setSelectedIndex(index);
    }

    // filename space replacement
    replacement = settings.getRenamerFilenameSpaceReplacement();
    index = spaceReplacement.indexOf(replacement);
    if (index >= 0) {
      cbFilenameSpaceReplacement.setSelectedIndex(index);
    }

    // colon replacement
    replacement = settings.getRenamerColonReplacement();
    index = colonReplacement.indexOf(replacement);
    if (index >= 0) {
      cbColonReplacement.setSelectedIndex(index);
    }

    cbFoldernameSpaceReplacement.addActionListener(arg0 -> {
      checkChanges();
      createRenamerExample();
    });
    cbFilenameSpaceReplacement.addActionListener(arg0 -> {
      checkChanges();
      createRenamerExample();
    });
    cbColonReplacement.addActionListener(arg0 -> {
      checkChanges();
      createRenamerExample();
    });

    lblExample.putClientProperty("clipPosition", SwingConstants.LEFT);

    // examples
    exampleEventList.add(new MovieRenamerExample("${title}"));
    exampleEventList.add(new MovieRenamerExample("${originalTitle}"));
    exampleEventList.add(new MovieRenamerExample("${title[0]}"));
    exampleEventList.add(new MovieRenamerExample("${title;first}"));
    exampleEventList.add(new MovieRenamerExample("${title[0,2]}"));
    exampleEventList.add(new MovieRenamerExample("${titleSortable}"));
    exampleEventList.add(new MovieRenamerExample("${year}"));
    exampleEventList.add(new MovieRenamerExample("${movieSet.title}"));
    exampleEventList.add(new MovieRenamerExample("${movieSet.titleSortable}"));
    exampleEventList.add(new MovieRenamerExample("${rating}"));
    exampleEventList.add(new MovieRenamerExample("${imdb}"));
    exampleEventList.add(new MovieRenamerExample("${certification}"));
    exampleEventList.add(new MovieRenamerExample("${directors[0].name}"));
    exampleEventList.add(new MovieRenamerExample("${genres[0]}"));
    exampleEventList.add(new MovieRenamerExample("${genres[0].name}"));
    exampleEventList.add(new MovieRenamerExample("${genresAsString}"));
    exampleEventList.add(new MovieRenamerExample("${tags[0]}"));
    exampleEventList.add(new MovieRenamerExample("${language}"));
    exampleEventList.add(new MovieRenamerExample("${videoResolution}"));
    exampleEventList.add(new MovieRenamerExample("${videoCodec}"));
    exampleEventList.add(new MovieRenamerExample("${videoFormat}"));
    exampleEventList.add(new MovieRenamerExample("${videoBitDepth}"));
    exampleEventList.add(new MovieRenamerExample("${audioCodec}"));
    exampleEventList.add(new MovieRenamerExample("${audioCodecList}"));
    exampleEventList.add(new MovieRenamerExample("${audioCodecsAsString}"));
    exampleEventList.add(new MovieRenamerExample("${audioChannels}"));
    exampleEventList.add(new MovieRenamerExample("${audioChannelList}"));
    exampleEventList.add(new MovieRenamerExample("${audioChannelsAsString}"));
    exampleEventList.add(new MovieRenamerExample("${audioLanguage}"));
    exampleEventList.add(new MovieRenamerExample("${audioLanguageList}"));
    exampleEventList.add(new MovieRenamerExample("${audioLanguagesAsString}"));
    exampleEventList.add(new MovieRenamerExample("${subtitleLanguageList}"));
    exampleEventList.add(new MovieRenamerExample("${subtitleLanguagesAsString}"));
    exampleEventList.add(new MovieRenamerExample("${mediaSource}"));
    exampleEventList.add(new MovieRenamerExample("${3Dformat}"));
    exampleEventList.add(new MovieRenamerExample("${hdr}"));
    exampleEventList.add(new MovieRenamerExample("${filesize}"));
    exampleEventList.add(new MovieRenamerExample("${edition}"));
    exampleEventList.add(new MovieRenamerExample("${parent}"));
    exampleEventList.add(new MovieRenamerExample("${note}"));

    // event listener must be at the end
    ActionListener actionCreateRenamerExample = e -> createRenamerExample();
    cbMovieForPreview.addActionListener(actionCreateRenamerExample);
    chckbxMoviesetSingleMovie.addActionListener(actionCreateRenamerExample);
    chckbxAsciiReplacement.addActionListener(actionCreateRenamerExample);
    chckbxFilenameSpaceReplacement.addActionListener(actionCreateRenamerExample);
    chckbxFoldernameSpaceReplacement.addActionListener(actionCreateRenamerExample);

    // force the size of the table
    tableExamples.setPreferredScrollableViewportSize(tableExamples.getPreferredSize());
  }

  private void initComponents() {
    setLayout(new MigLayout("hidemode 1", "[grow]", "[][15lp!][][15lp!][]"));
    {
      JPanel panelPatterns = new JPanel(new MigLayout("insets 0, hidemode 1", "[20lp!][15lp][][300lp,grow]", "[][][][][][]"));

      JLabel lblPatternsT = new TmmLabel(BUNDLE.getString("Settings.movie.renamer.title"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelPatterns, lblPatternsT, true);
      add(collapsiblePanel, "cell 0 0,growx, wmin 0");
      {
        JLabel lblMoviePath = new JLabel(BUNDLE.getString("Settings.renamer.folder"));
        panelPatterns.add(lblMoviePath, "cell 1 0 2 1,alignx right");

        tfMoviePath = new EnhancedTextField(IconManager.UNDO_GREY);
        tfMoviePath.setIconToolTipText(BUNDLE.getString("Settings.renamer.reverttodefault"));
        tfMoviePath.addIconMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            tfMoviePath.setText(MovieSettings.DEFAULT_RENAMER_FOLDER_PATTERN);
          }
        });
        panelPatterns.add(tfMoviePath, "cell 3 0,growx");

        JLabel lblDefault = new JLabel(BUNDLE.getString("Settings.default"));
        panelPatterns.add(lblDefault, "cell 1 1 2 1,alignx right");
        TmmFontHelper.changeFont(lblDefault, L2);

        JTextArea tpDefaultFolderPattern = new ReadOnlyTextArea(MovieSettings.DEFAULT_RENAMER_FOLDER_PATTERN);
        panelPatterns.add(tpDefaultFolderPattern, "cell 3 1,growx,wmin 0");
        TmmFontHelper.changeFont(tpDefaultFolderPattern, L2);
      }
      {
        JLabel lblMovieFilename = new JLabel(BUNDLE.getString("Settings.renamer.file"));
        panelPatterns.add(lblMovieFilename, "cell 1 2 2 1,alignx right");

        tfMovieFilename = new EnhancedTextField(IconManager.UNDO_GREY);
        tfMovieFilename.setIconToolTipText(BUNDLE.getString("Settings.renamer.reverttodefault"));
        tfMovieFilename.addIconMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            tfMovieFilename.setText(MovieSettings.DEFAULT_RENAMER_FILE_PATTERN);
          }
        });
        panelPatterns.add(tfMovieFilename, "cell 3 2,growx");

        JLabel lblDefault = new JLabel(BUNDLE.getString("Settings.default"));
        panelPatterns.add(lblDefault, "cell 1 3 2 1,alignx right");
        TmmFontHelper.changeFont(lblDefault, L2);

        JTextArea tpDefaultFilePattern = new ReadOnlyTextArea(MovieSettings.DEFAULT_RENAMER_FILE_PATTERN);
        panelPatterns.add(tpDefaultFilePattern, "cell 3 3,growx,wmin 0");
        TmmFontHelper.changeFont(tpDefaultFilePattern, L2);
      }
      {

        JLabel lblRenamerHintT = new JLabel(BUNDLE.getString("Settings.movie.renamer.example"));
        panelPatterns.add(lblRenamerHintT, "cell 1 4 3 1");

        JButton btnHelp = new JButton(BUNDLE.getString("tmm.help"));
        btnHelp.addActionListener(e -> {
          String url = StringEscapeUtils.unescapeHtml4("https://gitlab.com/tinyMediaManager/tinyMediaManager/wikis/Movie-Settings#renamer");
          try {
            TmmUIHelper.browseUrl(url);
          }
          catch (Exception e1) {
            LOGGER.error("Wiki", e1);
            MessageManager.instance
                .pushMessage(new Message(Message.MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":", e1.getLocalizedMessage() }));
          }
        });
        panelPatterns.add(btnHelp, "cell 1 4 3 1");
      }
      {
        taWarning = new ReadOnlyTextArea();
        taWarning.setForeground(Color.red);
        panelPatterns.add(taWarning, "cell 3 5,growx,wmin 0");
      }
    }
    {
      JPanel panelAdvancedOptions = SettingsPanelFactory.createSettingsPanel();

      JLabel lblAdvancedOptions = new TmmLabel(BUNDLE.getString("Settings.advancedoptions"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelAdvancedOptions, lblAdvancedOptions, true);
      add(collapsiblePanel, "cell 0 2,growx, wmin 0");
      {
        chckbxFoldernameSpaceReplacement = new JCheckBox(BUNDLE.getString("Settings.renamer.folderspacereplacement"));
        chckbxFoldernameSpaceReplacement.setToolTipText(BUNDLE.getString("Settings.renamer.folderspacereplacement.hint"));
        panelAdvancedOptions.add(chckbxFoldernameSpaceReplacement, "cell 1 0 2 1");

        cbFoldernameSpaceReplacement = new JComboBox<>(spaceReplacement.toArray());
        panelAdvancedOptions.add(cbFoldernameSpaceReplacement, "cell 1 0");
      }
      {
        chckbxFilenameSpaceReplacement = new JCheckBox(BUNDLE.getString("Settings.renamer.spacereplacement"));
        chckbxFilenameSpaceReplacement.setToolTipText(BUNDLE.getString("Settings.renamer.spacereplacement.hint"));
        panelAdvancedOptions.add(chckbxFilenameSpaceReplacement, "cell 1 1 2 1");

        cbFilenameSpaceReplacement = new JComboBox<>(spaceReplacement.toArray());
        panelAdvancedOptions.add(cbFilenameSpaceReplacement, "cell 1 1");
      }
      {
        JLabel lblColonReplacement = new JLabel(BUNDLE.getString("Settings.renamer.colonreplacement"));
        panelAdvancedOptions.add(lblColonReplacement, "cell 2 2");
        lblColonReplacement.setToolTipText(BUNDLE.getString("Settings.renamer.colonreplacement.hint"));

        cbColonReplacement = new JComboBox<>(colonReplacement.toArray());
        panelAdvancedOptions.add(cbColonReplacement, "cell 2 2");
      }
      {
        chckbxAsciiReplacement = new JCheckBox(BUNDLE.getString("Settings.renamer.asciireplacement"));
        panelAdvancedOptions.add(chckbxAsciiReplacement, "cell 1 3 2 1");

        JLabel lblAsciiHint = new JLabel(BUNDLE.getString("Settings.renamer.asciireplacement.hint"));
        panelAdvancedOptions.add(lblAsciiHint, "cell 2 4");
        TmmFontHelper.changeFont(lblAsciiHint, L2);
      }
      {
        chckbxMoviesetSingleMovie = new JCheckBox(BUNDLE.getString("Settings.renamer.moviesetsinglemovie"));
        panelAdvancedOptions.add(chckbxMoviesetSingleMovie, "cell 1 5 2 1");
      }
      {
        chckbxRemoveOtherNfos = new JCheckBox(BUNDLE.getString("Settings.renamer.removenfo"));
        panelAdvancedOptions.add(chckbxRemoveOtherNfos, "cell 1 6 2 1");
      }
    }
    {
      JPanel panelExample = new JPanel();
      panelExample.setLayout(new MigLayout("hidemode 1, insets 0", "[20lp!][300lp,grow]", ""));

      JLabel lblExampleHeader = new TmmLabel(BUNDLE.getString("Settings.example"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelExample, lblExampleHeader, true);
      add(collapsiblePanel, "cell 0 4, growx, wmin 0");
      {
        JLabel lblExampleT = new JLabel(BUNDLE.getString("tmm.movie"));
        panelExample.add(lblExampleT, "cell 1 0");

        cbMovieForPreview = new JComboBox();
        panelExample.add(cbMovieForPreview, "cell 1 0");

        lblExample = new TmmLabel("", L1);
        panelExample.add(lblExample, "cell 1 1, wmin 0");

        DefaultEventTableModel<MovieRenamerExample> exampleTableModel = new DefaultEventTableModel<>(
            GlazedListsSwing.swingThreadProxyList(exampleEventList), new MovieRenamerExampleTableFormat());
        tableExamples = new TmmTable(exampleTableModel);
        JScrollPane scrollPaneExamples = new JScrollPane(tableExamples);
        tableExamples.configureScrollPane(scrollPaneExamples);
        scrollPaneExamples.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        ScrollingEventDelegator.install(scrollPaneExamples);
        panelExample.add(scrollPaneExamples, "cell 1 2,grow");
        tableExamples.setRowHeight(35);
      }
    }
  }

  private void buildAndInstallMovieArray() {
    cbMovieForPreview.removeAllItems();
    List<Movie> allMovies = new ArrayList<>(MovieList.getInstance().getMovies());
    allMovies.sort(new MovieComparator());
    for (Movie movie : allMovies) {
      MoviePreviewContainer container = new MoviePreviewContainer();
      container.movie = movie;
      cbMovieForPreview.addItem(container);
    }
  }

  private void createRenamerExample() {
    Movie movie = null;

    String warning = "";
    // empty is valid (although not unique)
    if (!tfMoviePath.getText().isEmpty() && !MovieRenamer.isFolderPatternUnique(tfMoviePath.getText())) {
      warning = BUNDLE.getString("Settings.renamer.folder.warning");
    }
    if (!warning.isEmpty()) {
      taWarning.setVisible(true);
      taWarning.setText(warning);
    }
    else {
      taWarning.setVisible(false);
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
        path = Paths.get(movie.getDataSource(), path).toString();
      }
      else {
        // the old folder name
        path = movie.getPathNIO().toString();
      }

      if (StringUtils.isNotBlank(tfMovieFilename.getText())) {
        List<MediaFile> mediaFiles = movie.getMediaFiles(MediaFileType.VIDEO);
        if (!mediaFiles.isEmpty()) {
          String extension = FilenameUtils.getExtension(mediaFiles.get(0).getFilename());
          filename = MovieRenamer.createDestinationForFilename(tfMovieFilename.getText(), movie) + "." + extension;
        }
      }
      else {
        filename = movie.getMediaFiles(MediaFileType.VIDEO).get(0).getFilename();
      }

      try {
        lblExample.setText(Paths.get(path, filename).toString());
      }
      catch (Exception e) {
        // not changing on errors
      }

      // create examples
      for (MovieRenamerExample example : exampleEventList) {
        example.createExample(movie);
      }
      TableColumnResizer.adjustColumnPreferredWidths(tableExamples, 7);
    }
    else {
      lblExample.setText(BUNDLE.getString("Settings.movie.renamer.nomovie"));
    }
  }

  private void checkChanges() {
    // foldername space replacement
    String replacement = (String) cbFoldernameSpaceReplacement.getSelectedItem();
    settings.setRenamerPathnameSpaceReplacement(replacement);

    // filename space replacement
    replacement = (String) cbFilenameSpaceReplacement.getSelectedItem();
    settings.setRenamerFilenameSpaceReplacement(replacement);

    // colon replacement
    replacement = (String) cbColonReplacement.getSelectedItem();
    settings.setRenamerColonReplacement(replacement);
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
  private static class MoviePreviewContainer {
    Movie movie;

    @Override
    public String toString() {
      return movie.getTitle();
    }
  }

  private static class MovieComparator implements Comparator<Movie> {
    @Override
    public int compare(Movie arg0, Movie arg1) {
      return arg0.getTitle().compareTo(arg1.getTitle());
    }
  }

  @SuppressWarnings("unused")
  private static class MovieRenamerExample extends AbstractModelObject {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^\\$\\{(.*?)([\\}\\[;\\.]+.*)");
    private String               token;
    private String               completeToken;
    private String               description;
    private String               example       = "";

    private MovieRenamerExample(String token) {
      this.token = token;
      this.completeToken = createCompleteToken();
      try {
        this.description = BUNDLE.getString("Settings.movie.renamer." + token);
      }
      catch (Exception e) {
        this.description = "";
      }
    }

    private String createCompleteToken() {
      String result = token;

      Matcher matcher = TOKEN_PATTERN.matcher(token);
      if (matcher.find() && matcher.groupCount() > 1) {
        String alias = matcher.group(1);
        String sourceToken = MovieRenamer.TOKEN_MAP.get(alias);

        if (StringUtils.isNotBlank(sourceToken)) {
          result = "<html>" + token + "<br>${" + sourceToken + matcher.group(2) + "</html>";
        }
      }
      return result;
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

  private static class MovieRenamerExampleTableFormat implements TableFormat<MovieRenamerExample> {
    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return BUNDLE.getString("Settings.renamer.token.name");

        case 1:
          return BUNDLE.getString("Settings.renamer.token");

        case 2:
          return BUNDLE.getString("Settings.renamer.value");

        default:
          return null;
      }
    }

    @Override
    public Object getColumnValue(MovieRenamerExample baseObject, int column) {
      switch (column) {
        case 0:
          return baseObject.completeToken;

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
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty = BeanProperty.create("renamerPathnameSpaceSubstitution");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxFoldernameSpaceReplacement, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_2 = BeanProperty.create("renamerFilenameSpaceSubstitution");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, chckbxFilenameSpaceReplacement, jCheckBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_1 = BeanProperty.create("renamerNfoCleanup");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, chckbxRemoveOtherNfos, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<MovieSettings, Boolean> settingsBeanProperty_5 = BeanProperty.create("renamerCreateMoviesetForSingleMovie");
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
