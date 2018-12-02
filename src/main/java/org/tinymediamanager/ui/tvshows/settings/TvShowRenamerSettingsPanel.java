/*
 * Copyright 2012 - 2018 Manuel Laggner
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowRenamer;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.CollapsiblePanel;
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
  private static final long                        serialVersionUID = 5189531235704401313L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle              BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private TvShowSettings                           settings         = TvShowModuleManager.SETTINGS;
  private List<String>                             spaceReplacement = new ArrayList<>(Arrays.asList("_", ".", "-"));
  private List<String>                             colonReplacement = new ArrayList<>(Arrays.asList("", "-"));
  private EventList<TvShowRenamerExample>          exampleEventList;

  /*
   * UI components
   */
  private JLabel                                   lblExample;
  private JComboBox<TvShowPreviewContainer>        cbTvShowForPreview;
  private JTextField                               tfSeasonFolderName;
  private JCheckBox                                chckbxAsciiReplacement;
  private JComboBox<String>                        cbSpaceReplacement;
  private JCheckBox                                chckbxSpaceReplacement;
  private JComboBox<TvShowEpisodePreviewContainer> cbEpisodeForPreview;
  private TmmTable                                 tableExamples;
  private JTextField                               tfTvShowFolder;
  private JTextField                               tfEpisodeFilename;
  private JCheckBox                                chckbxSpecialSeason;
  private JComboBox                                cbColonReplacement;

  public TvShowRenamerSettingsPanel() {

    exampleEventList = GlazedLists
        .threadSafeList(new ObservableElementList<>(new BasicEventList<>(), GlazedLists.beanConnector(TvShowRenamerExample.class)));

    // UI initializations
    initComponents();
    initDataBindings();

    // the panel renamer
    ActionListener renamerActionListener = arg0 -> {
      checkChanges();
      createRenamerExample();
    };

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

    chckbxSpaceReplacement.addActionListener(renamerActionListener);
    chckbxAsciiReplacement.addActionListener(renamerActionListener);

    cbTvShowForPreview.addActionListener(arg0 -> {
      buildAndInstallEpisodeArray();
      createRenamerExample();
    });

    cbEpisodeForPreview.addActionListener(arg0 -> createRenamerExample());
    cbSpaceReplacement.addActionListener(renamerActionListener);
    cbColonReplacement.addActionListener(renamerActionListener);

    // space replacement
    String spaceReplacement = settings.getRenamerSpaceReplacement();
    int index = this.spaceReplacement.indexOf(spaceReplacement);
    if (index >= 0) {
      cbSpaceReplacement.setSelectedIndex(index);
    }

    // colon replacement
    String colonReplacement = settings.getRenamerColonReplacement();
    index = this.colonReplacement.indexOf(colonReplacement);
    if (index >= 0) {
      cbColonReplacement.setSelectedIndex(index);
    }

    lblExample.putClientProperty("clipPosition", SwingConstants.LEFT);

    // examples
    exampleEventList.add(new TvShowRenamerExample("${title}"));
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
    exampleEventList.add(new TvShowRenamerExample("${showTitleSortable}"));
    exampleEventList.add(new TvShowRenamerExample("${videoResolution}"));
    exampleEventList.add(new TvShowRenamerExample("${videoFormat}"));
    exampleEventList.add(new TvShowRenamerExample("${videoCodec}"));
    exampleEventList.add(new TvShowRenamerExample("${videoFormat}"));
    exampleEventList.add(new TvShowRenamerExample("${audioCodec}"));
    exampleEventList.add(new TvShowRenamerExample("${audioChannels}"));
    exampleEventList.add(new TvShowRenamerExample("${audioLanguage}"));
    exampleEventList.add(new TvShowRenamerExample("${mediaSource}"));
    exampleEventList.add(new TvShowRenamerExample("${hdr}"));
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[grow]", "[][15lp!][][15lp!][]"));
    {
      JPanel panelPatterns = new JPanel(new MigLayout("insets 0, hidemode 1", "[20lp!][15lp][][300lp,grow]", "[][][][][][]"));

      JLabel lblPatternsT = new TmmLabel(BUNDLE.getString("Settings.tvshow.renamer.title"), H3); //$NON-NLS-1$
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelPatterns, lblPatternsT, true);
      add(collapsiblePanel, "cell 0 0,growx,wmin 0");

      {
        JLabel lblTvShowFolder = new JLabel(BUNDLE.getString("Settings.tvshowfoldername")); //$NON-NLS-1$
        panelPatterns.add(lblTvShowFolder, "cell 1 0 2 1,alignx right");

        tfTvShowFolder = new JTextField();
        panelPatterns.add(tfTvShowFolder, "cell 3 0 2 1,growx");

        JLabel lblDefault = new JLabel(BUNDLE.getString("Settings.default")); //$NON-NLS-1$
        panelPatterns.add(lblDefault, "cell 1 1 2 1,alignx right");
        TmmFontHelper.changeFont(lblDefault, L2);

        JTextArea tpDefaultFolderPattern = new ReadOnlyTextArea(TvShowSettings.DEFAULT_RENAMER_FOLDER_PATTERN);
        panelPatterns.add(tpDefaultFolderPattern, "cell 3 1 2 1,growx,wmin 0");
        TmmFontHelper.changeFont(tpDefaultFolderPattern, L2);
      }
      {
        JLabel lblSeasonFolderName = new JLabel(BUNDLE.getString("Settings.tvshowseasonfoldername")); //$NON-NLS-1$
        panelPatterns.add(lblSeasonFolderName, "cell 1 2 2 1,alignx right");

        tfSeasonFolderName = new JTextField();
        panelPatterns.add(tfSeasonFolderName, "cell 3 2 2 1,growx");

        JLabel lblDefault = new JLabel(BUNDLE.getString("Settings.default")); //$NON-NLS-1$
        panelPatterns.add(lblDefault, "cell 1 3 2 1,alignx right");
        TmmFontHelper.changeFont(lblDefault, L2);

        JTextArea tpDefaultSeasonPattern = new ReadOnlyTextArea(TvShowSettings.DEFAULT_RENAMER_SEASON_PATTERN);
        panelPatterns.add(tpDefaultSeasonPattern, "cell 3 3 2 1,growx,wmin 0");
        TmmFontHelper.changeFont(tpDefaultSeasonPattern, L2);
      }
      {
        JLabel lblEpisodeFileName = new JLabel(BUNDLE.getString("Settings.tvshowfilename"));
        panelPatterns.add(lblEpisodeFileName, "cell 1 4 2 1,alignx right");

        tfEpisodeFilename = new JTextField();
        panelPatterns.add(tfEpisodeFilename, "cell 3 4 2 1,growx");

        JLabel lblDefault = new JLabel(BUNDLE.getString("Settings.default")); //$NON-NLS-1$
        panelPatterns.add(lblDefault, "cell 1 5 2 1,alignx right");
        TmmFontHelper.changeFont(lblDefault, L2);

        JTextArea tpDefaultFilePattern = new ReadOnlyTextArea(TvShowSettings.DEFAULT_RENAMER_FILE_PATTERN);
        panelPatterns.add(tpDefaultFilePattern, "cell 3 5 2 1,growx,wmin 0");
        TmmFontHelper.changeFont(tpDefaultFilePattern, L2);
      }
    }
    {
      JPanel panelAdvancedOptions = SettingsPanelFactory.createSettingsPanel();

      JLabel lblAdvancedOptions = new TmmLabel(BUNDLE.getString("Settings.advancedoptions"), H3); //$NON-NLS-1$
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelAdvancedOptions, lblAdvancedOptions, true);
      add(collapsiblePanel, "cell 0 2,growx");
      {
        chckbxSpecialSeason = new JCheckBox(BUNDLE.getString("tvshow.renamer.specialseason")); //$NON-NLS-1$
        panelAdvancedOptions.add(chckbxSpecialSeason, "cell 1 0 2 1");
      }
      {
        chckbxSpaceReplacement = new JCheckBox(BUNDLE.getString("Settings.renamer.spacereplacement")); //$NON-NLS-1$
        chckbxSpaceReplacement.setToolTipText(BUNDLE.getString("Settings.renamer.spacereplacement.hint")); //$NON-NLS-1$
        panelAdvancedOptions.add(chckbxSpaceReplacement, "cell 1 1 2 1");

        cbSpaceReplacement = new JComboBox(spaceReplacement.toArray());
        panelAdvancedOptions.add(cbSpaceReplacement, "cell 1 1");
      }
      {
        JLabel lblColonReplacement = new JLabel(BUNDLE.getString("Settings.renamer.colonreplacement")); //$NON-NLS-1$
        panelAdvancedOptions.add(lblColonReplacement, "cell 2 2");
        lblColonReplacement.setToolTipText(BUNDLE.getString("Settings.renamer.colonreplacement.hint"));

        cbColonReplacement = new JComboBox(colonReplacement.toArray());
        panelAdvancedOptions.add(cbColonReplacement, "cell 2 2");
      }

      {
        chckbxAsciiReplacement = new JCheckBox(BUNDLE.getString("Settings.renamer.asciireplacement"));
        panelAdvancedOptions.add(chckbxAsciiReplacement, "cell 1 3 2 1");

        JLabel lblAsciiHint = new JLabel(BUNDLE.getString("Settings.renamer.asciireplacement.hint")); //$NON-NLS-1$
        panelAdvancedOptions.add(lblAsciiHint, "cell 2 4");
        TmmFontHelper.changeFont(lblAsciiHint, L2);
      }
    }
    {
      JPanel panelExample = SettingsPanelFactory.createSettingsPanel();

      JLabel lblAdvancedOptions = new TmmLabel(BUNDLE.getString("Settings.example"), H3); //$NON-NLS-1$
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelExample, lblAdvancedOptions, true);
      add(collapsiblePanel, "cell 0 4,growx, wmin 0");
      {
        JLabel lblExampleTvShowT = new JLabel(BUNDLE.getString("metatag.tvshow"));
        panelExample.add(lblExampleTvShowT, "cell 1 0 2 1");

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
        panelExample.add(lblExample, "cell 1 1 2 1, wmin 0");
        TmmFontHelper.changeFont(lblExample, Font.BOLD);
      }
      {
        DefaultEventTableModel<TvShowRenamerExample> exampleTableModel = new DefaultEventTableModel<>(
            GlazedListsSwing.swingThreadProxyList(exampleEventList), new TvShowRenamerExampleTableFormat());

        tableExamples = new TmmTable(exampleTableModel);
        JScrollPane scrollPane = new JScrollPane(tableExamples);
        tableExamples.configureScrollPane(scrollPane);
        panelExample.add(scrollPane, "cell 1 2 2 1,grow");
        scrollPane.setViewportView(tableExamples);
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
        String filename = TvShowRenamer
            .generateEpisodeFilenames(tfEpisodeFilename.getText(), tvShow, episode.getMediaFiles(MediaFileType.VIDEO).get(0)).get(0).getFilename();
        String seasonDir = TvShowRenamer.getSeasonFoldername(tfSeasonFolderName.getText(), episode.getTvShow(), episode);
        if (StringUtils.isBlank(seasonDir)) {
          lblExample.setText(tvShowDir + File.separator + filename);
        }
        else {
          lblExample.setText(tvShowDir + File.separator + seasonDir + File.separator + filename);
        }
        // create examples
        for (TvShowRenamerExample example : exampleEventList) {
          example.createExample(episode);
        }
        try {
          TableColumnResizer.adjustColumnPreferredWidths(tableExamples, 7);
        }
        catch (Exception ignored) {
        }
      }
      else {
        lblExample.setText("");
      }
    });
  }

  private void checkChanges() {
    String spaceReplacement = (String) cbSpaceReplacement.getSelectedItem();
    settings.setRenamerSpaceReplacement(spaceReplacement);

    String colonReplacement = (String) cbColonReplacement.getSelectedItem();
    settings.setRenamerColonReplacement(colonReplacement);
  }

  /*************************************************************
   * helper classes
   *************************************************************/
  private class TvShowPreviewContainer {
    TvShow tvShow;

    @Override
    public String toString() {
      return tvShow.getTitle();
    }
  }

  private class TvShowEpisodePreviewContainer {
    TvShowEpisode episode;

    @Override
    public String toString() {
      return episode.getSeason() + "." + episode.getEpisode() + " " + episode.getTitle();
    }
  }

  private class TvShowComparator implements Comparator<TvShow> {
    @Override
    public int compare(TvShow arg0, TvShow arg1) {
      return arg0.getTitle().compareTo(arg1.getTitle());
    }
  }

  @SuppressWarnings("unused")
  private class TvShowRenamerExample extends AbstractModelObject {
    private String token;
    private String description;
    private String example = "";

    public TvShowRenamerExample(String token) {
      this.token = token;
      try {
        this.description = BUNDLE.getString("Settings.tvshow.renamer." + token); //$NON-NLS-1$
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

  private class TvShowRenamerExampleTableFormat implements TableFormat<TvShowRenamerExample> {
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
    public Object getColumnValue(TvShowRenamerExample baseObject, int column) {
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
    BeanProperty<TvShowSettings, Boolean> settingsBeanProperty_6 = BeanProperty.create("asciiReplacement");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, chckbxAsciiReplacement, jCheckBoxBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<TvShowSettings, Boolean> tvShowSettingsBeanProperty = BeanProperty.create("renamerSpaceSubstitution");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty, chckbxSpaceReplacement, jCheckBoxBeanProperty);
    autoBinding_4.bind();
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
    //
    BeanProperty<TvShowSettings, Boolean> tvShowSettingsBeanProperty_4 = BeanProperty.create("specialSeason");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty_4, chckbxSpecialSeason, jCheckBoxBeanProperty);
    autoBinding_3.bind();
  }
}
