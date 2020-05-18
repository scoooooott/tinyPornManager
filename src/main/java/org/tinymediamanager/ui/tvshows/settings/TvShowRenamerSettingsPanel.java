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

package org.tinymediamanager.ui.tvshows.settings;

import static org.tinymediamanager.ui.TmmFontHelper.H3;
import static org.tinymediamanager.ui.TmmFontHelper.L2;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowRenamer;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
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
 * The class TvShowRenamerSettingsPanel
 * 
 * @author Manuel Laggner
 */
public class TvShowRenamerSettingsPanel extends JPanel implements HierarchyListener {
  private static final long                        serialVersionUID  = 5189531235704401313L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle              BUNDLE            = ResourceBundle.getBundle("messages", new UTF8Control());
  private static final Logger                      LOGGER            = LoggerFactory.getLogger(TvShowRenamerSettingsPanel.class);

  private TvShowSettings                           settings          = TvShowModuleManager.SETTINGS;
  private List<String>                             spaceReplacements = new ArrayList<>(Arrays.asList("_", ".", "-"));
  private List<String>                             colonReplacements = new ArrayList<>(Arrays.asList(" ", "-"));
  private EventList<TvShowRenamerExample>          exampleEventList;

  /*
   * UI components
   */
  private JLabel                                   lblExample;
  private JComboBox<TvShowPreviewContainer>        cbTvShowForPreview;
  private EnhancedTextField                        tfSeasonFolderName;
  private JCheckBox                                chckbxAsciiReplacement;
  private JCheckBox                                chckbxShowFoldernameSpaceReplacement;
  private JComboBox                                cbShowFoldernameSpaceReplacement;
  private JCheckBox                                chckbxSeasonFoldernameSpaceReplacement;
  private JComboBox                                cbSeasonFoldernameSpaceReplacement;
  private JCheckBox                                chckbxFilenameSpaceReplacement;
  private JComboBox                                cbFilenameSpaceReplacement;
  private JComboBox<TvShowEpisodePreviewContainer> cbEpisodeForPreview;
  private TmmTable                                 tableExamples;
  private EnhancedTextField                        tfTvShowFolder;
  private EnhancedTextField                        tfEpisodeFilename;
  private JComboBox                                cbColonReplacement;

  public TvShowRenamerSettingsPanel() {

    exampleEventList = GlazedLists
        .threadSafeList(new ObservableElementList<>(new BasicEventList<>(), GlazedLists.beanConnector(TvShowRenamerExample.class)));

    // UI initializations
    initComponents();
    initDataBindings();

    // the panel renamer
    DocumentListener documentListener = new DocumentListener() {
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
    };

    tfTvShowFolder.getDocument().addDocumentListener(documentListener);
    tfSeasonFolderName.getDocument().addDocumentListener(documentListener);
    tfEpisodeFilename.getDocument().addDocumentListener(documentListener);

    cbTvShowForPreview.addActionListener(arg0 -> {
      buildAndInstallEpisodeArray();
      createRenamerExample();
    });

    // show folder name space replacement
    String replacement = settings.getRenamerShowPathnameSpaceReplacement();
    int index = spaceReplacements.indexOf(replacement);
    if (index >= 0) {
      cbShowFoldernameSpaceReplacement.setSelectedIndex(index);
    }

    // season folder name space replacement
    replacement = settings.getRenamerSeasonPathnameSpaceReplacement();
    index = spaceReplacements.indexOf(replacement);
    if (index >= 0) {
      cbSeasonFoldernameSpaceReplacement.setSelectedIndex(index);
    }

    // filename space replacement
    replacement = settings.getRenamerFilenameSpaceReplacement();
    index = spaceReplacements.indexOf(replacement);
    if (index >= 0) {
      cbFilenameSpaceReplacement.setSelectedIndex(index);
    }

    // colon replacement
    String colonReplacement = settings.getRenamerColonReplacement();
    index = this.colonReplacements.indexOf(colonReplacement);
    if (index >= 0) {
      cbColonReplacement.setSelectedIndex(index);
    }

    lblExample.putClientProperty("clipPosition", SwingConstants.LEFT);

    // examples
    exampleEventList.add(new TvShowRenamerExample("${title}"));
    exampleEventList.add(new TvShowRenamerExample("${originalTitle}"));
    exampleEventList.add(new TvShowRenamerExample("${titleSortable}"));
    exampleEventList.add(new TvShowRenamerExample("${seasonNr}"));
    exampleEventList.add(new TvShowRenamerExample("${seasonNr2}"));
    exampleEventList.add(new TvShowRenamerExample("${seasonNrDvd}"));
    exampleEventList.add(new TvShowRenamerExample("${seasonNrDvd2}"));
    exampleEventList.add(new TvShowRenamerExample("${episodeNr}"));
    exampleEventList.add(new TvShowRenamerExample("${episodeNr2}"));
    exampleEventList.add(new TvShowRenamerExample("${episodeNrDvd}"));
    exampleEventList.add(new TvShowRenamerExample("${episodeNrDvd2}"));
    exampleEventList.add(new TvShowRenamerExample("${airedDate}"));
    exampleEventList.add(new TvShowRenamerExample("${year}"));
    exampleEventList.add(new TvShowRenamerExample("${showYear}"));
    exampleEventList.add(new TvShowRenamerExample("${showTitle}"));
    exampleEventList.add(new TvShowRenamerExample("${showOriginalTitle}"));
    exampleEventList.add(new TvShowRenamerExample("${showTitleSortable}"));
    exampleEventList.add(new TvShowRenamerExample("${videoResolution}"));
    exampleEventList.add(new TvShowRenamerExample("${videoFormat}"));
    exampleEventList.add(new TvShowRenamerExample("${videoCodec}"));
    exampleEventList.add(new TvShowRenamerExample("${videoFormat}"));
    exampleEventList.add(new TvShowRenamerExample("${videoBitDepth}"));
    exampleEventList.add(new TvShowRenamerExample("${audioCodec}"));
    exampleEventList.add(new TvShowRenamerExample("${audioCodecList}"));
    exampleEventList.add(new TvShowRenamerExample("${audioCodecsAsString}"));
    exampleEventList.add(new TvShowRenamerExample("${audioChannels}"));
    exampleEventList.add(new TvShowRenamerExample("${audioChannelList}"));
    exampleEventList.add(new TvShowRenamerExample("${audioChannelsAsString}"));
    exampleEventList.add(new TvShowRenamerExample("${audioLanguage}"));
    exampleEventList.add(new TvShowRenamerExample("${audioLanguageList}"));
    exampleEventList.add(new TvShowRenamerExample("${audioLanguagesAsString}"));
    exampleEventList.add(new TvShowRenamerExample("${subtitleLanguageList}"));
    exampleEventList.add(new TvShowRenamerExample("${subtitleLanguagesAsString}"));
    exampleEventList.add(new TvShowRenamerExample("${mediaSource}"));
    exampleEventList.add(new TvShowRenamerExample("${hdr}"));
    exampleEventList.add(new TvShowRenamerExample("${filesize}"));
    exampleEventList.add(new TvShowRenamerExample("${parent}"));
    exampleEventList.add(new TvShowRenamerExample("${showNote}"));
    exampleEventList.add(new TvShowRenamerExample("${note}"));

    // event listener must be at the end
    ActionListener renamerActionListener = arg0 -> {
      checkChanges();
      createRenamerExample();
    };

    chckbxShowFoldernameSpaceReplacement.addActionListener(renamerActionListener);
    chckbxSeasonFoldernameSpaceReplacement.addActionListener(renamerActionListener);
    chckbxFilenameSpaceReplacement.addActionListener(renamerActionListener);
    chckbxAsciiReplacement.addActionListener(renamerActionListener);
    cbEpisodeForPreview.addActionListener(arg0 -> createRenamerExample());
    cbShowFoldernameSpaceReplacement.addActionListener(renamerActionListener);
    cbSeasonFoldernameSpaceReplacement.addActionListener(renamerActionListener);
    cbFilenameSpaceReplacement.addActionListener(renamerActionListener);
    cbColonReplacement.addActionListener(renamerActionListener);

    // force the size of the table
    tableExamples.setPreferredScrollableViewportSize(tableExamples.getPreferredSize());
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[grow]", "[][15lp!][][15lp!][]"));
    {
      JPanel panelPatterns = new JPanel(new MigLayout("insets 0, hidemode 1", "[20lp!][15lp][][300lp,grow]", "[][][][][][][]"));

      JLabel lblPatternsT = new TmmLabel(BUNDLE.getString("Settings.tvshow.renamer.title"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelPatterns, lblPatternsT, true);
      add(collapsiblePanel, "cell 0 0,growx,wmin 0");

      {
        JLabel lblTvShowFolder = new JLabel(BUNDLE.getString("Settings.tvshowfoldername"));
        panelPatterns.add(lblTvShowFolder, "cell 1 0 2 1,alignx right");

        tfTvShowFolder = new EnhancedTextField(IconManager.UNDO_GREY);
        tfTvShowFolder.setIconToolTipText(BUNDLE.getString("Settings.renamer.reverttodefault"));
        tfTvShowFolder.addIconMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            tfTvShowFolder.setText(TvShowSettings.DEFAULT_RENAMER_FOLDER_PATTERN);
          }
        });
        panelPatterns.add(tfTvShowFolder, "cell 3 0 2 1,growx");

        JLabel lblDefault = new JLabel(BUNDLE.getString("Settings.default"));
        panelPatterns.add(lblDefault, "cell 1 1 2 1,alignx right");
        TmmFontHelper.changeFont(lblDefault, L2);

        JTextArea tpDefaultFolderPattern = new ReadOnlyTextArea(TvShowSettings.DEFAULT_RENAMER_FOLDER_PATTERN);
        panelPatterns.add(tpDefaultFolderPattern, "cell 3 1 2 1,growx,wmin 0");
        TmmFontHelper.changeFont(tpDefaultFolderPattern, L2);
      }
      {
        JLabel lblSeasonFolderName = new JLabel(BUNDLE.getString("Settings.tvshowseasonfoldername"));
        panelPatterns.add(lblSeasonFolderName, "cell 1 2 2 1,alignx right");

        tfSeasonFolderName = new EnhancedTextField(IconManager.UNDO_GREY);
        tfSeasonFolderName.setIconToolTipText(BUNDLE.getString("Settings.renamer.reverttodefault"));
        tfSeasonFolderName.addIconMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            tfSeasonFolderName.setText(TvShowSettings.DEFAULT_RENAMER_SEASON_PATTERN);
          }
        });
        panelPatterns.add(tfSeasonFolderName, "cell 3 2 2 1,growx");

        JLabel lblDefault = new JLabel(BUNDLE.getString("Settings.default"));
        panelPatterns.add(lblDefault, "cell 1 3 2 1,alignx right");
        TmmFontHelper.changeFont(lblDefault, L2);

        JTextArea tpDefaultSeasonPattern = new ReadOnlyTextArea(TvShowSettings.DEFAULT_RENAMER_SEASON_PATTERN);
        panelPatterns.add(tpDefaultSeasonPattern, "cell 3 3 2 1,growx,wmin 0");
        TmmFontHelper.changeFont(tpDefaultSeasonPattern, L2);
      }
      {
        JLabel lblEpisodeFileName = new JLabel(BUNDLE.getString("Settings.tvshowfilename"));
        panelPatterns.add(lblEpisodeFileName, "cell 1 4 2 1,alignx right");

        tfEpisodeFilename = new EnhancedTextField(IconManager.UNDO_GREY);
        tfEpisodeFilename.setIconToolTipText(BUNDLE.getString("Settings.renamer.reverttodefault"));
        tfEpisodeFilename.addIconMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            tfEpisodeFilename.setText(TvShowSettings.DEFAULT_RENAMER_FILE_PATTERN);
          }
        });
        panelPatterns.add(tfEpisodeFilename, "cell 3 4 2 1,growx");

        JLabel lblDefault = new JLabel(BUNDLE.getString("Settings.default"));
        panelPatterns.add(lblDefault, "cell 1 5 2 1,alignx right");
        TmmFontHelper.changeFont(lblDefault, L2);

        JTextArea tpDefaultFilePattern = new ReadOnlyTextArea(TvShowSettings.DEFAULT_RENAMER_FILE_PATTERN);
        panelPatterns.add(tpDefaultFilePattern, "cell 3 5 2 1,growx,wmin 0");
        TmmFontHelper.changeFont(tpDefaultFilePattern, L2);
      }
      {
        JLabel lblRenamerHintT = new JLabel(BUNDLE.getString("Settings.tvshow.renamer.hint"));
        panelPatterns.add(lblRenamerHintT, "cell 1 6 3 1");

        JButton btnHelp = new JButton(BUNDLE.getString("tmm.help"));
        btnHelp.addActionListener(e -> {
          String url = StringEscapeUtils.unescapeHtml4("https://gitlab.com/tinyMediaManager/tinyMediaManager/wikis/TV-Show-Settings#renamer");
          try {
            TmmUIHelper.browseUrl(url);
          }
          catch (Exception e1) {
            LOGGER.error("Wiki", e1);
            MessageManager.instance
                .pushMessage(new Message(Message.MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":", e1.getLocalizedMessage() }));
          }
        });
        panelPatterns.add(btnHelp, "cell 1 6 3 1");
      }
    }
    {
      JPanel panelAdvancedOptions = SettingsPanelFactory.createSettingsPanel();

      JLabel lblAdvancedOptions = new TmmLabel(BUNDLE.getString("Settings.advancedoptions"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelAdvancedOptions, lblAdvancedOptions, true);
      add(collapsiblePanel, "cell 0 2,growx");
      {
        chckbxShowFoldernameSpaceReplacement = new JCheckBox(BUNDLE.getString("Settings.renamer.showfolderspacereplacement"));
        chckbxShowFoldernameSpaceReplacement.setToolTipText(BUNDLE.getString("Settings.renamer.folderspacereplacement.hint"));
        panelAdvancedOptions.add(chckbxShowFoldernameSpaceReplacement, "cell 1 0 2 1");

        cbShowFoldernameSpaceReplacement = new JComboBox(spaceReplacements.toArray());
        panelAdvancedOptions.add(cbShowFoldernameSpaceReplacement, "cell 1 0");
      }
      {
        chckbxSeasonFoldernameSpaceReplacement = new JCheckBox(BUNDLE.getString("Settings.renamer.seasonfolderspacereplacement"));
        chckbxSeasonFoldernameSpaceReplacement.setToolTipText(BUNDLE.getString("Settings.renamer.folderspacereplacement.hint"));
        panelAdvancedOptions.add(chckbxSeasonFoldernameSpaceReplacement, "cell 1 1 2 1");

        cbSeasonFoldernameSpaceReplacement = new JComboBox(spaceReplacements.toArray());
        panelAdvancedOptions.add(cbSeasonFoldernameSpaceReplacement, "cell 1 1");
      }
      {
        chckbxFilenameSpaceReplacement = new JCheckBox(BUNDLE.getString("Settings.renamer.spacereplacement"));
        chckbxFilenameSpaceReplacement.setToolTipText(BUNDLE.getString("Settings.renamer.spacereplacement.hint"));
        panelAdvancedOptions.add(chckbxFilenameSpaceReplacement, "cell 1 2 2 1");

        cbFilenameSpaceReplacement = new JComboBox(spaceReplacements.toArray());
        panelAdvancedOptions.add(cbFilenameSpaceReplacement, "cell 1 2");
      }
      {
        JLabel lblColonReplacement = new JLabel(BUNDLE.getString("Settings.renamer.colonreplacement"));
        panelAdvancedOptions.add(lblColonReplacement, "cell 2 3");
        lblColonReplacement.setToolTipText(BUNDLE.getString("Settings.renamer.colonreplacement.hint"));

        cbColonReplacement = new JComboBox(colonReplacements.toArray());
        panelAdvancedOptions.add(cbColonReplacement, "cell 2 3");
      }

      {
        chckbxAsciiReplacement = new JCheckBox(BUNDLE.getString("Settings.renamer.asciireplacement"));
        panelAdvancedOptions.add(chckbxAsciiReplacement, "cell 1 4 2 1");

        JLabel lblAsciiHint = new JLabel(BUNDLE.getString("Settings.renamer.asciireplacement.hint"));
        panelAdvancedOptions.add(lblAsciiHint, "cell 2 5");
        TmmFontHelper.changeFont(lblAsciiHint, L2);
      }
    }
    {
      JPanel panelExample = new JPanel();
      panelExample.setLayout(new MigLayout("hidemode 1, insets 0", "[20lp!][300lp,grow]", ""));

      JLabel lblAdvancedOptions = new TmmLabel(BUNDLE.getString("Settings.example"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelExample, lblAdvancedOptions, true);
      add(collapsiblePanel, "cell 0 4,growx, wmin 0");
      {
        JLabel lblExampleTvShowT = new JLabel(BUNDLE.getString("metatag.tvshow"));
        panelExample.add(lblExampleTvShowT, "cell 1 0");

        cbTvShowForPreview = new JComboBox();
        panelExample.add(cbTvShowForPreview, "cell 1 0,growx,wmin 0");
      }
      {
        JLabel lblExampleEpisodeT = new JLabel(BUNDLE.getString("metatag.episode"));
        panelExample.add(lblExampleEpisodeT, "cell 1 0");

        cbEpisodeForPreview = new JComboBox();
        panelExample.add(cbEpisodeForPreview, "cell 1 0,growx,wmin 0");
      }
      {
        lblExample = new JLabel("");
        panelExample.add(lblExample, "cell 1 1, wmin 0");
        TmmFontHelper.changeFont(lblExample, Font.BOLD);
      }
      {
        DefaultEventTableModel<TvShowRenamerExample> exampleTableModel = new DefaultEventTableModel<>(
            GlazedListsSwing.swingThreadProxyList(exampleEventList), new TvShowRenamerExampleTableFormat());

        tableExamples = new TmmTable(exampleTableModel);
        JScrollPane scrollPane = new JScrollPane(tableExamples);
        tableExamples.configureScrollPane(scrollPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        ScrollingEventDelegator.install(scrollPane);
        panelExample.add(scrollPane, "cell 1 2,grow");
        scrollPane.setViewportView(tableExamples);
        tableExamples.setRowHeight(35);
      }
    }
  }

  @Override
  public void hierarchyChanged(HierarchyEvent arg0) {
    if (isShowing()) {
      buildAndInstallTvShowArray();
      buildAndInstallEpisodeArray();
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

  private void buildAndInstallTvShowArray() {
    cbTvShowForPreview.removeAllItems();
    List<TvShow> allTvShows = new ArrayList<>(TvShowList.getInstance().getTvShows());
    allTvShows.sort(new TvShowComparator());
    for (TvShow tvShow : allTvShows) {
      TvShowPreviewContainer container = new TvShowPreviewContainer();
      container.tvShow = tvShow;
      cbTvShowForPreview.addItem(container);
    }
  }

  private void buildAndInstallEpisodeArray() {
    cbEpisodeForPreview.removeAllItems();
    Object obj = cbTvShowForPreview.getSelectedItem();
    if (obj instanceof TvShowPreviewContainer) {
      TvShowPreviewContainer c = (TvShowPreviewContainer) cbTvShowForPreview.getSelectedItem();
      for (TvShowEpisode episode : c.tvShow.getEpisodes()) {
        TvShowEpisodePreviewContainer container = new TvShowEpisodePreviewContainer();
        container.episode = episode;
        cbEpisodeForPreview.addItem(container);
      }
    }
  }

  private void createRenamerExample() {
    // need to start it async, that binding will transfer changes to settings first
    SwingUtilities.invokeLater(() -> {
      TvShow tvShow = null;
      TvShowEpisode episode = null;

      if (cbTvShowForPreview.getSelectedItem() instanceof TvShowPreviewContainer) {
        TvShowPreviewContainer container = (TvShowPreviewContainer) cbTvShowForPreview.getSelectedItem();
        tvShow = container.tvShow;
      }

      if (cbEpisodeForPreview.getSelectedItem() instanceof TvShowEpisodePreviewContainer) {
        TvShowEpisodePreviewContainer container = (TvShowEpisodePreviewContainer) cbEpisodeForPreview.getSelectedItem();
        episode = container.episode;
      }

      if (tvShow != null && episode != null) {
        String tvShowDir = TvShowRenamer.getTvShowFoldername(tfTvShowFolder.getText(), tvShow);
        MediaFile episodeMf = TvShowRenamer
            .generateEpisodeFilenames(tfEpisodeFilename.getText(), tvShow, episode.getMediaFiles(MediaFileType.VIDEO).get(0)).get(0);

        String newFilenameAndPath = episodeMf.getFile().toString().replace(episode.getTvShow().getPath(), "");
        lblExample.setText(tvShowDir + newFilenameAndPath);

        // create examples
        for (TvShowRenamerExample example : exampleEventList) {
          example.createExample(episode);
        }
        TableColumnResizer.adjustColumnPreferredWidths(tableExamples, 7);
      }
      else {
        lblExample.setText("");
      }
    });
  }

  private void checkChanges() {
    // show folder name space replacement
    String spaceReplacement = (String) cbShowFoldernameSpaceReplacement.getSelectedItem();
    settings.setRenamerShowPathnameSpaceReplacement(spaceReplacement);

    // season folder name space replacement
    spaceReplacement = (String) cbSeasonFoldernameSpaceReplacement.getSelectedItem();
    settings.setRenamerSeasonPathnameSpaceReplacement(spaceReplacement);

    // filename space replacement
    spaceReplacement = (String) cbFilenameSpaceReplacement.getSelectedItem();
    settings.setRenamerFilenameSpaceReplacement(spaceReplacement);

    String colonReplacement = (String) cbColonReplacement.getSelectedItem();
    settings.setRenamerColonReplacement(colonReplacement);
  }

  /*************************************************************
   * helper classes
   *************************************************************/
  private static class TvShowPreviewContainer {
    TvShow tvShow;

    @Override
    public String toString() {
      return tvShow.getTitle();
    }
  }

  private static class TvShowEpisodePreviewContainer {
    TvShowEpisode episode;

    @Override
    public String toString() {
      return episode.getSeason() + "." + episode.getEpisode() + " " + episode.getTitle();
    }
  }

  private static class TvShowComparator implements Comparator<TvShow> {
    @Override
    public int compare(TvShow arg0, TvShow arg1) {
      return arg0.getTitle().compareTo(arg1.getTitle());
    }
  }

  @SuppressWarnings("unused")
  private static class TvShowRenamerExample extends AbstractModelObject {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^\\$\\{(.*?)([\\}\\[;\\.]+.*)");
    private String               token;
    private String               completeToken;
    private String               description;
    private String               example       = "";

    public TvShowRenamerExample(String token) {
      this.token = token;
      this.completeToken = createCompleteToken();
      try {
        this.description = BUNDLE.getString("Settings.tvshow.renamer." + token);
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
        String sourceToken = TvShowRenamer.TOKEN_MAP.get(alias);

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

    private void createExample(TvShowEpisode episode) {
      String oldValue = example;
      if (episode == null) {
        example = "";
      }
      else {
        example = TvShowRenamer.createDestination(token, Collections.singletonList(episode));
      }
      firePropertyChange("example", oldValue, example);
    }
  }

  private static class TvShowRenamerExampleTableFormat implements TableFormat<TvShowRenamerExample> {
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
    public Object getColumnValue(TvShowRenamerExample baseObject, int column) {
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
    BeanProperty<TvShowSettings, Boolean> settingsBeanProperty_6 = BeanProperty.create("asciiReplacement");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, chckbxAsciiReplacement, jCheckBoxBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<TvShowSettings, Boolean> tvShowSettingsBeanProperty = BeanProperty.create("renamerShowPathnameSpaceSubstitution");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty, chckbxShowFoldernameSpaceReplacement, jCheckBoxBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<TvShowSettings, Boolean> tvShowSettingsBeanProperty_7 = BeanProperty.create("renamerSeasonPathnameSpaceSubstitution");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty_7, chckbxSeasonFoldernameSpaceReplacement, jCheckBoxBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<TvShowSettings, Boolean> tvShowSettingsBeanProperty_8 = BeanProperty.create("renamerFilenameSpaceSubstitution");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty_8, chckbxFilenameSpaceReplacement, jCheckBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<TvShowSettings, String> tvShowSettingsBeanProperty_1 = BeanProperty.create("renamerTvShowFoldername");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
    AutoBinding<TvShowSettings, String, JTextField, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty_1, tfTvShowFolder, jTextFieldBeanProperty_1);
    autoBinding.bind();
    //
    BeanProperty<TvShowSettings, String> tvShowSettingsBeanProperty_2 = BeanProperty.create("renamerFilename");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_2 = BeanProperty.create("text");
    AutoBinding<TvShowSettings, String, JTextField, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty_2, tfEpisodeFilename, jTextFieldBeanProperty_2);
    autoBinding_1.bind();
    //
    BeanProperty<TvShowSettings, String> tvShowSettingsBeanProperty_3 = BeanProperty.create("renamerSeasonFoldername");
    BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowSettings, String, JTextField, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty_3, tfSeasonFolderName, jTextFieldBeanProperty);
    autoBinding_2.bind();
  }
}
