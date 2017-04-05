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

package org.tinymediamanager.ui.tvshows.settings;

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
import javax.swing.JTextField;
import javax.swing.JTextPane;
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
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.JHintCheckBox;
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
  private static final long               serialVersionUID = 5189531235704401313L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle     BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private TvShowSettings                  settings         = TvShowModuleManager.SETTINGS;
  private List<String>                    spaceReplacement = new ArrayList<>(Arrays.asList("_", ".", "-"));
  private EventList<TvShowRenamerExample> exampleEventList = null;

  /*
   * UI components
   */
  private JLabel                          lblExample;
  private JComboBox                       cbTvShowForPreview;
  private JTextField                      tfSeasonFoldername;
  private JCheckBox                       chckbxAsciiReplacement;
  private JComboBox                       cbSpaceReplacement;
  private JHintCheckBox                   chckbxSpaceReplacement;
  private JComboBox                       cbEpisodeForPreview;
  private TmmTable                        tableExamples;
  private JTextField                      tfTvShowFolder;
  private JTextField                      tfEpisodeFilename;

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
    tfSeasonFoldername.getDocument().addDocumentListener(documentListener);
    tfEpisodeFilename.getDocument().addDocumentListener(documentListener);

    chckbxSpaceReplacement.addActionListener(renamerActionListener);
    chckbxAsciiReplacement.addActionListener(renamerActionListener);

    cbTvShowForPreview.addActionListener(arg0 -> {
      buildAndInstallEpisodeArray();
      createRenamerExample();
    });

    cbEpisodeForPreview.addActionListener(arg0 -> createRenamerExample());
    cbSpaceReplacement.addActionListener(renamerActionListener);

    String spaceReplacement = settings.getRenamerSpaceReplacement();
    int index = this.spaceReplacement.indexOf(spaceReplacement);
    if (index >= 0) {
      cbSpaceReplacement.setSelectedIndex(index);
    }

    // examples
    exampleEventList.add(new TvShowRenamerExample("$T"));
    exampleEventList.add(new TvShowRenamerExample("$1"));
    exampleEventList.add(new TvShowRenamerExample("$2"));
    exampleEventList.add(new TvShowRenamerExample("$3"));
    exampleEventList.add(new TvShowRenamerExample("$4"));
    exampleEventList.add(new TvShowRenamerExample("$E"));
    exampleEventList.add(new TvShowRenamerExample("$D"));
    exampleEventList.add(new TvShowRenamerExample("$Y"));
    exampleEventList.add(new TvShowRenamerExample("$N"));
    exampleEventList.add(new TvShowRenamerExample("$M"));
    exampleEventList.add(new TvShowRenamerExample("$R"));
    exampleEventList.add(new TvShowRenamerExample("$A"));
    exampleEventList.add(new TvShowRenamerExample("$V"));
    exampleEventList.add(new TvShowRenamerExample("$F"));
    exampleEventList.add(new TvShowRenamerExample("$S"));
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp,shrink 0][15lp,shrink 0][][grow]", "[][][][][][][][20lp][][][][20lp][][][][][100lp,grow]"));
    {
      final JLabel lblPatternAndOptionsT = new JLabel(BUNDLE.getString("Settings.tvshow.renamer.title")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblPatternAndOptionsT, 1.16667, Font.BOLD);
      add(lblPatternAndOptionsT, "cell 0 0 8 1");
    }
    {
      JLabel lblTvShowFolder = new JLabel(BUNDLE.getString("Settings.tvshowfoldername")); //$NON-NLS-1$
      add(lblTvShowFolder, "cell 1 1 2 1");

      tfTvShowFolder = new JTextField();
      tfTvShowFolder.setColumns(20);
      add(tfTvShowFolder, "cell 3 1");

      JLabel lblDefault = new JLabel(BUNDLE.getString("Settings.default")); //$NON-NLS-1$
      add(lblDefault, "flowx,cell 1 2 2 1,alignx right,aligny top");
      TmmFontHelper.changeFont(lblDefault, 0.833);

      JLabel lblDefaultFolderPattern = new JLabel(TvShowSettings.DEFAULT_RENAMER_FOLDER_PATTERN);
      add(lblDefaultFolderPattern, "cell 3 2,aligny top");
      TmmFontHelper.changeFont(lblDefaultFolderPattern, 0.833);
    }
    {
      JLabel lblSeasonFolderName = new JLabel(BUNDLE.getString("Settings.tvshowseasonfoldername")); //$NON-NLS-1$
      add(lblSeasonFolderName, "cell 1 3 2 1");

      tfSeasonFoldername = new JTextField();
      tfSeasonFoldername.setColumns(20);
      add(tfSeasonFoldername, "cell 3 3");

      JLabel lblDefault = new JLabel(BUNDLE.getString("Settings.default")); //$NON-NLS-1$
      add(lblDefault, "flowx,cell 1 4 2 1,alignx right,aligny top");
      TmmFontHelper.changeFont(lblDefault, 0.833);

      JLabel lblDefaultSeasonPattern = new JLabel(TvShowSettings.DEFAULT_RENAMER_SEASON_PATTERN);
      add(lblDefaultSeasonPattern, "cell 3 4,aligny top");
      TmmFontHelper.changeFont(lblDefaultSeasonPattern, 0.833);
    }
    {
      JLabel lblEpisodeFileName = new JLabel(BUNDLE.getString("Settings.tvshowfilename"));
      add(lblEpisodeFileName, "cell 1 5 2 1");

      tfEpisodeFilename = new JTextField();
      tfEpisodeFilename.setColumns(20);
      add(tfEpisodeFilename, "cell 3 5");

      JLabel lblDefault = new JLabel(BUNDLE.getString("Settings.default")); //$NON-NLS-1$
      add(lblDefault, "flowx,cell 1 6 2 1,alignx right,aligny top");
      TmmFontHelper.changeFont(lblDefault, 0.833);

      JLabel lblDefaultFilePattern = new JLabel(TvShowSettings.DEFAULT_RENAMER_FILE_PATTERN);
      add(lblDefaultFilePattern, "cell 3 6,aligny top");
      TmmFontHelper.changeFont(lblDefaultFilePattern, 0.833);
    }
    {
      chckbxSpaceReplacement = new JHintCheckBox(BUNDLE.getString("Settings.movie.renamer.spacesubstitution")); //$NON-NLS-1$
      add(chckbxSpaceReplacement, "flowx,cell 1 8 3 1");
      chckbxSpaceReplacement.setHintIcon(IconManager.HINT);
      chckbxSpaceReplacement.setToolTipText(BUNDLE.getString("Settings.tvshowspacereplacement.hint")); //$NON-NLS-1$

      cbSpaceReplacement = new JComboBox(spaceReplacement.toArray());
      add(cbSpaceReplacement, "cell 1 8 3 1");
    }
    {
      chckbxAsciiReplacement = new JCheckBox(BUNDLE.getString("Settings.renamer.asciireplacement")); //$NON-NLS-1$
      add(chckbxAsciiReplacement, "cell 1 9 3 1");

      JTextPane txtpntAsciiHint = new JTextPane();
      add(txtpntAsciiHint, "cell 2 10 2 1");
      txtpntAsciiHint.setText(BUNDLE.getString("Settings.renamer.asciireplacement.hint")); //$NON-NLS-1$
      TmmFontHelper.changeFont(txtpntAsciiHint, 0.833);
      txtpntAsciiHint.setOpaque(false);
    }
    {
      final JLabel lblExampleT = new JLabel(BUNDLE.getString("Settings.example")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblExampleT, 1.16667, Font.BOLD);
      add(lblExampleT, "cell 0 12 5 1");
    }
    {
      JLabel lblExampleTvShowT = new JLabel(BUNDLE.getString("metatag.tvshow"));
      add(lblExampleTvShowT, "cell 1 13 2 1");

      cbTvShowForPreview = new JComboBox();
      add(cbTvShowForPreview, "cell 3 13,growx");
    }
    {
      JLabel lblExampleEpisodeT = new JLabel(BUNDLE.getString("metatag.episode"));
      add(lblExampleEpisodeT, "cell 1 14 2 1");

      cbEpisodeForPreview = new JComboBox();
      add(cbEpisodeForPreview, "cell 3 14,growx");
    }
    {
      lblExample = new JLabel("");
      add(lblExample, "cell 1 15 3 1");
      TmmFontHelper.changeFont(lblExample, Font.BOLD);
    }
    {
      DefaultEventTableModel<TvShowRenamerExample> exampleTableModel = new DefaultEventTableModel<>(
          GlazedListsSwing.swingThreadProxyList(exampleEventList), new TvShowRenamerExampleTableFormat());

      tableExamples = new TmmTable(exampleTableModel);
      JScrollPane scrollPane = new JScrollPane(tableExamples);
      tableExamples.configureScrollPane(scrollPane);
      add(scrollPane, "cell 1 16 3 1,grow");
      scrollPane.setViewportView(tableExamples);
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
    Collections.sort(allTvShows, new TvShowComparator());
    for (TvShow tvShow : allTvShows) {
      TvShowPreviewContainer container = new TvShowPreviewContainer();
      container.tvShow = tvShow;
      cbTvShowForPreview.addItem(container);
    }
  }

  private void buildAndInstallEpisodeArray() {
    cbEpisodeForPreview.removeAllItems();
    Object obj = cbTvShowForPreview.getSelectedItem();
    if (obj != null && obj instanceof TvShowPreviewContainer) {
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
        String seasonDir = TvShowRenamer.getSeasonFoldername(tfSeasonFoldername.getText(), episode.getTvShow(), episode.getSeason());
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
        catch (Exception e) {
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
        example = TvShowRenamer.createDestination(token, Arrays.asList(episode));
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
        tvShowSettingsBeanProperty_3, tfSeasonFoldername, jTextFieldBeanProperty);
    autoBinding_2.bind();
  }
}
