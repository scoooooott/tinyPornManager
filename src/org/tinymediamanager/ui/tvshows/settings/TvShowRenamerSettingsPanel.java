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

package org.tinymediamanager.ui.tvshows.settings;

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

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowRenamer;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.JHintCheckBox;
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
 * The class TvShowRenamerSettingsPanel
 * 
 * @author Manuel Laggner
 */
public class TvShowRenamerSettingsPanel extends JPanel implements HierarchyListener {
  private static final long               serialVersionUID = 5189531235704401313L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle     BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private TvShowSettings                  settings         = Settings.getInstance().getTvShowSettings();
  private List<String>                    spaceReplacement = new ArrayList<String>(Arrays.asList("_", ".", "-"));
  private EventList<TvShowRenamerExample> exampleEventList = null;

  /**
   * UI components
   */
  private JTextPane                       txtpntAsciiHint;
  private JLabel                          lblSeasonFolderName;
  private JLabel                          lblExample;
  private JComboBox                       cbTvShowForPreview;
  private JTextField                      tfSeasonFoldername;
  private JCheckBox                       chckbxAsciiReplacement;
  private JComboBox                       cbSpaceReplacement;
  private JHintCheckBox                   chckbxSpaceReplacement;
  private JPanel                          panelExample;
  private JComboBox                       cbEpisodeForPreview;
  private JScrollPane                     scrollPane;
  private JTable                          tableExamples;
  private JLabel                          lblTvShowFolder;
  private JTextField                      tfTvShowFolder;
  private JTextField                      tfEpisodeFilename;
  private JLabel                          lblEpisodeFileName;

  public TvShowRenamerSettingsPanel() {
    setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
            FormFactory.RELATED_GAP_ROWSPEC, }));

    exampleEventList = GlazedLists.threadSafeList(new ObservableElementList<TvShowRenamerExample>(new BasicEventList<TvShowRenamerExample>(),
        GlazedLists.beanConnector(TvShowRenamerExample.class)));
    DefaultEventTableModel<TvShowRenamerExample> exampleTableModel = new DefaultEventTableModel<TvShowRenamerExample>(
        GlazedListsSwing.swingThreadProxyList(exampleEventList), new TvShowRenamerExampleTableFormat());

    // the panel renamer
    ActionListener renamerActionListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        checkChanges();
        createRenamerExample();
      }
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

    JPanel panelRenamer = new JPanel();
    panelRenamer.setBorder(new TitledBorder(null,
        BUNDLE.getString("Settings.tvshow.renamer.title"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$

    add(panelRenamer, "2, 2, fill, fill");
    panelRenamer.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, }));

    lblTvShowFolder = new JLabel(BUNDLE.getString("Settings.tvshowfoldername")); //$NON-NLS-1$
    panelRenamer.add(lblTvShowFolder, "2, 2, right, default");

    tfTvShowFolder = new JTextField();
    panelRenamer.add(tfTvShowFolder, "4, 2, 3, 1, fill, default");
    tfTvShowFolder.getDocument().addDocumentListener(documentListener);

    lblSeasonFolderName = new JLabel(BUNDLE.getString("Settings.tvshowseasonfoldername")); //$NON-NLS-1$
    panelRenamer.add(lblSeasonFolderName, "2, 4, right, default");

    tfSeasonFoldername = new JTextField();
    panelRenamer.add(tfSeasonFoldername, "4, 4, 3, 1, fill, default");
    tfSeasonFoldername.getDocument().addDocumentListener(documentListener);

    lblEpisodeFileName = new JLabel(BUNDLE.getString("Settings.tvshowfilename"));//$NON-NLS-1$
    panelRenamer.add(lblEpisodeFileName, "2, 6, right, default");

    tfEpisodeFilename = new JTextField();
    panelRenamer.add(tfEpisodeFilename, "4, 6, 3, 1, fill, default");
    tfEpisodeFilename.getDocument().addDocumentListener(documentListener);

    chckbxAsciiReplacement = new JCheckBox(BUNDLE.getString("Settings.renamer.asciireplacement")); //$NON-NLS-1$
    chckbxAsciiReplacement.addActionListener(renamerActionListener);

    chckbxSpaceReplacement = new JHintCheckBox(BUNDLE.getString("Settings.movie.renamer.spacesubstitution")); //$NON-NLS-1$
    chckbxSpaceReplacement.setHintIcon(IconManager.HINT);
    chckbxSpaceReplacement.setToolTipText(BUNDLE.getString("Settings.tvshowspacereplacement.hint")); //$NON-NLS-1$
    chckbxSpaceReplacement.addActionListener(renamerActionListener);
    panelRenamer.add(chckbxSpaceReplacement, "2, 10, right, default");

    cbSpaceReplacement = new JComboBox(spaceReplacement.toArray());
    panelRenamer.add(cbSpaceReplacement, "4, 10, fill, default");
    cbSpaceReplacement.addActionListener(renamerActionListener);
    panelRenamer.add(chckbxAsciiReplacement, "2, 12, 9, 1");

    txtpntAsciiHint = new JTextPane();
    txtpntAsciiHint.setText(BUNDLE.getString("Settings.renamer.asciireplacement.hint")); //$NON-NLS-1$
    TmmFontHelper.changeFont(txtpntAsciiHint, 0.833);
    txtpntAsciiHint.setBackground(UIManager.getColor("Panel.background"));
    panelRenamer.add(txtpntAsciiHint, "2, 14, 7, 1, fill, fill");

    panelExample = new JPanel();
    panelExample.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.example"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelExample, "2, 4, fill, fill");
    panelExample.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("50dlu:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, }));

    JLabel lblExampleTvShowT = new JLabel(BUNDLE.getString("metatag.tvshow"));
    panelExample.add(lblExampleTvShowT, "2, 2, right, default");

    cbTvShowForPreview = new JComboBox();
    cbTvShowForPreview.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        buildAndInstallEpisodeArray();
        createRenamerExample();
      }
    });
    panelExample.add(cbTvShowForPreview, "4, 2");

    JLabel lblExampleEpisodeT = new JLabel(BUNDLE.getString("metatag.episode"));
    panelExample.add(lblExampleEpisodeT, "2, 4, right, default");

    cbEpisodeForPreview = new JComboBox();
    cbEpisodeForPreview.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        createRenamerExample();
      }
    });
    panelExample.add(cbEpisodeForPreview, "4, 4, fill, default");

    lblExample = new JLabel("");
    TmmFontHelper.changeFont(lblExample, Font.BOLD);
    panelExample.add(lblExample, "2, 6, 3, 1");

    tableExamples = new ZebraJTable(exampleTableModel);
    scrollPane = ZebraJTable.createStripedJScrollPane(tableExamples);
    scrollPane.setViewportView(tableExamples);
    panelExample.add(scrollPane, "2, 8, 3, 1, fill, fill");

    initDataBindings();

    String spaceReplacement = settings.getRenamerSpaceReplacement();
    int index = this.spaceReplacement.indexOf(spaceReplacement);
    if (index >= 0) {
      cbSpaceReplacement.setSelectedIndex(index);
    }

    // examples
    exampleEventList.add(new TvShowRenamerExample("$T"));
    exampleEventList.add(new TvShowRenamerExample("$1"));
    exampleEventList.add(new TvShowRenamerExample("$2"));
    exampleEventList.add(new TvShowRenamerExample("$E"));
    exampleEventList.add(new TvShowRenamerExample("$Y"));
    exampleEventList.add(new TvShowRenamerExample("$N"));
    exampleEventList.add(new TvShowRenamerExample("$R"));
    exampleEventList.add(new TvShowRenamerExample("$A"));
    exampleEventList.add(new TvShowRenamerExample("$V"));
    exampleEventList.add(new TvShowRenamerExample("$F"));

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
    List<TvShow> allTvShows = new ArrayList<TvShow>(TvShowList.getInstance().getTvShows());
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
      String tvShowDir = TvShowRenamer.generateTvShowDir(tfTvShowFolder.getText(), tvShow);
      String filename = TvShowRenamer.generateFilename(tfEpisodeFilename.getText(), tvShow, episode.getMediaFiles(MediaFileType.VIDEO).get(0));
      String seasonDir = TvShowRenamer.generateSeasonDir(tfSeasonFoldername.getText(), episode);
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
        example = TvShowRenamer.createDestination(token, episode.getTvShow(), Arrays.asList(episode));
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
