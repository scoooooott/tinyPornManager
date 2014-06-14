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

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.tvshow.TvShowEpisodeNaming;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowRenamer;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ScrollablePanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class TvShowRenamerSettingsPanel
 * 
 * @author Manuel Laggner
 */
public class TvShowRenamerSettingsPanel extends ScrollablePanel implements HierarchyListener {
  private static final long           serialVersionUID = 5189531235704401313L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());          //$NON-NLS-1$
  private static final String         SPACE            = "<space>";

  private TvShowSettings              settings         = Settings.getInstance().getTvShowSettings();

  private List<String>                separators       = new ArrayList<String>(Arrays.asList(SPACE, ".", "_", "-", " - "));
  private List<String>                spaceReplacement = new ArrayList<String>(Arrays.asList("_", ".", "-"));

  /**
   * UI components
   */
  private JTextPane                   txtpntAsciiHint;
  private JSeparator                  separator_1;
  private JLabel                      lblTvShow;
  private JLabel                      lblEpisodes;
  private JTextPane                   txtpnSeasonHint;
  private JLabel                      lblSeasonFolderName;
  private final ButtonGroup           buttonGroup      = new ButtonGroup();
  private JLabel                      lblSeparator;
  private JLabel                      lblExample;
  private JComboBox                   cbTvShowForPreview;
  private JCheckBox                   chckbxAddSeason;
  private JCheckBox                   chckbxAddShow;
  private JCheckBox                   chckbxAddEpisodeTitle;
  private JRadioButton                rdbtnRawNumber;
  private JRadioButton                rdbtnSeasonEpisode;
  private JRadioButton                rdbtnSxe;
  private JComboBox                   cbSeparator;
  private JTextField                  tfSeasonFoldername;
  private JRadioButton                rdbtn0Sxe;
  private JCheckBox                   chckbxAsciiReplacement;
  private JCheckBox                   chckbxTvShowFolder;
  private JCheckBox                   chckbxYear;

  private ActionListener              renamerActionListener;
  private JLabel                      lblSeparatorHint;
  private JComboBox                   cbSpaceReplacement;
  private JLabel                      lblSpaceReplacementHint;
  private JCheckBox                   chckbxSpaceReplacement;

  public TvShowRenamerSettingsPanel() {
    setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC }));

    // the panel renamer
    renamerActionListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        checkChanges();
        createRenamerExample();
      }
    };

    JPanel panelRenamer = new JPanel();
    panelRenamer.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.renamer"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$

    add(panelRenamer, "2, 2, fill, fill");
    panelRenamer.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.MIN_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("min:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow(3)"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.UNRELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("default:grow"),
        FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("default:grow"), RowSpec.decode("40px"), FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    chckbxAddSeason = new JCheckBox(BUNDLE.getString("Settings.tvshowseasontofilename")); //$NON-NLS-1$
    chckbxAddSeason.addActionListener(renamerActionListener);

    lblTvShow = new JLabel(BUNDLE.getString("metatag.tvshow")); //$NON-NLS-1$
    panelRenamer.add(lblTvShow, "2, 2");

    chckbxTvShowFolder = new JCheckBox(BUNDLE.getString("tvshow.renamer.tvshowfolder")); //$NON-NLS-1$
    chckbxTvShowFolder.addActionListener(renamerActionListener);
    panelRenamer.add(chckbxTvShowFolder, "2, 4, 3, 1");

    chckbxYear = new JCheckBox(BUNDLE.getString("tvshow.renamer.tvshowfolder.year")); //$NON-NLS-1$
    chckbxYear.addActionListener(renamerActionListener);
    panelRenamer.add(chckbxYear, "6, 4, 3, 1");

    separator_1 = new JSeparator();
    panelRenamer.add(separator_1, "2, 6, 7, 1");

    lblEpisodes = new JLabel(BUNDLE.getString("metatag.episodes")); //$NON-NLS-1$
    panelRenamer.add(lblEpisodes, "2, 8");
    panelRenamer.add(chckbxAddSeason, "2, 10, 3, 1");

    rdbtnSeasonEpisode = new JRadioButton("S01E01");
    rdbtnSeasonEpisode.addActionListener(renamerActionListener);
    buttonGroup.add(rdbtnSeasonEpisode);
    panelRenamer.add(rdbtnSeasonEpisode, "6, 10");

    chckbxAddShow = new JCheckBox(BUNDLE.getString("Settings.tvshowtofilename")); //$NON-NLS-1$
    chckbxAddShow.addActionListener(renamerActionListener);
    panelRenamer.add(chckbxAddShow, "2, 12, 3, 1");

    rdbtnSxe = new JRadioButton("1x01");
    rdbtnSxe.addActionListener(renamerActionListener);
    buttonGroup.add(rdbtnSxe);
    panelRenamer.add(rdbtnSxe, "6, 12");

    chckbxAddEpisodeTitle = new JCheckBox(BUNDLE.getString("Settings.tvshowepisodetofilename")); //$NON-NLS-1$
    chckbxAddEpisodeTitle.addActionListener(renamerActionListener);
    panelRenamer.add(chckbxAddEpisodeTitle, "2, 14, 3, 1");

    rdbtn0Sxe = new JRadioButton("01x01");
    rdbtn0Sxe.addActionListener(renamerActionListener);
    buttonGroup.add(rdbtn0Sxe);
    panelRenamer.add(rdbtn0Sxe, "6, 14");

    rdbtnRawNumber = new JRadioButton("101");
    rdbtnRawNumber.addActionListener(renamerActionListener);
    buttonGroup.add(rdbtnRawNumber);
    panelRenamer.add(rdbtnRawNumber, "6, 16");

    lblSeparator = new JLabel(BUNDLE.getString("Settings.separator")); //$NON-NLS-1$
    panelRenamer.add(lblSeparator, "2, 18, right, default");

    cbSeparator = new JComboBox(separators.toArray());
    panelRenamer.add(cbSeparator, "4, 18, fill, default");

    lblSeparatorHint = new JLabel(BUNDLE.getString("Settings.separator.hint")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblSeparatorHint, 0.833);
    panelRenamer.add(lblSeparatorHint, "6, 18, 3, 1, fill, default");

    chckbxSpaceReplacement = new JCheckBox(BUNDLE.getString("Settings.movie.renamer.spacesubstitution")); //$NON-NLS-1$
    chckbxSpaceReplacement.addActionListener(renamerActionListener);
    panelRenamer.add(chckbxSpaceReplacement, "2, 20");

    cbSpaceReplacement = new JComboBox(spaceReplacement.toArray());
    panelRenamer.add(cbSpaceReplacement, "4, 20, fill, default");

    lblSpaceReplacementHint = new JLabel(BUNDLE.getString("Settings.tvshowspacereplacement.hint")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblSpaceReplacementHint, 0.833);
    panelRenamer.add(lblSpaceReplacementHint, "6, 20, 3, 1, fill, default");

    lblSeasonFolderName = new JLabel(BUNDLE.getString("Settings.tvshowseasonfoldername")); //$NON-NLS-1$
    panelRenamer.add(lblSeasonFolderName, "2, 22, right, top");

    tfSeasonFoldername = new JTextField();
    panelRenamer.add(tfSeasonFoldername, "4, 22, fill, top");
    tfSeasonFoldername.getDocument().addDocumentListener(new DocumentListener() {
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

    txtpnSeasonHint = new JTextPane();
    txtpnSeasonHint.setOpaque(false);
    TmmFontHelper.changeFont(txtpnSeasonHint, 0.833);
    txtpnSeasonHint.setText(BUNDLE.getString("Settings.tvshowseasonhint")); //$NON-NLS-1$
    panelRenamer.add(txtpnSeasonHint, "6, 22, 3, 1, fill, fill");

    chckbxAsciiReplacement = new JCheckBox(BUNDLE.getString("Settings.renamer.asciireplacement")); //$NON-NLS-1$
    chckbxAsciiReplacement.addActionListener(renamerActionListener);
    panelRenamer.add(chckbxAsciiReplacement, "2, 24, 3, 1");

    txtpntAsciiHint = new JTextPane();
    txtpntAsciiHint.setText(BUNDLE.getString("Settings.renamer.asciireplacement.hint")); //$NON-NLS-1$
    TmmFontHelper.changeFont(txtpntAsciiHint, 0.833);
    txtpntAsciiHint.setBackground(UIManager.getColor("Panel.background"));
    panelRenamer.add(txtpntAsciiHint, "2, 26, 5, 1, fill, fill");

    JLabel lblExampleT = new JLabel(BUNDLE.getString("Settings.example")); //$NON-NLS-1$
    panelRenamer.add(lblExampleT, "2, 28, right, default");

    cbTvShowForPreview = new JComboBox();
    cbTvShowForPreview.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        createRenamerExample();
      }
    });
    panelRenamer.add(cbTvShowForPreview, "4, 28, 3, 1, fill, default");

    lblExample = new JLabel("");
    panelRenamer.add(lblExample, "2, 30, 7, 1");

    initDataBindings();

    // set radio buttons and white space separator
    String separator = settings.getRenamerSeparator();
    int index = -1;
    if (" ".equals(separator)) {
      index = separators.indexOf(SPACE);
    }
    else {
      index = separators.indexOf(separator);
    }
    if (index >= 0) {
      cbSeparator.setSelectedIndex(index);
    }
    cbSeparator.addActionListener(renamerActionListener);

    String spaceReplacement = settings.getRenamerSpaceReplacement();
    index = this.spaceReplacement.indexOf(spaceReplacement);
    if (index >= 0) {
      cbSpaceReplacement.setSelectedIndex(index);
    }
    cbSpaceReplacement.addActionListener(renamerActionListener);

    switch (settings.getRenamerFormat()) {
      case NUMBER:
        rdbtnRawNumber.setSelected(true);
        break;

      case WITH_SE:
        rdbtnSeasonEpisode.setSelected(true);
        break;

      case WITH_X:
        rdbtnSxe.setSelected(true);
        break;

      case WITH_0X:
        rdbtn0Sxe.setSelected(true);
        break;
    }
  }

  @Override
  public void hierarchyChanged(HierarchyEvent arg0) {
    if (isShowing()) {
      buildAndInstallTvShowArray();
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

  private void createRenamerExample() {
    TvShow tvShow = null;

    if (cbTvShowForPreview.getSelectedItem() instanceof TvShowPreviewContainer) {
      TvShowPreviewContainer container = (TvShowPreviewContainer) cbTvShowForPreview.getSelectedItem();
      tvShow = container.tvShow;
    }

    if (tvShow != null && tvShow.getEpisodes().size() > 0 && tvShow.getEpisodes().get(0) != null) {
      TvShowEpisode episode = tvShow.getEpisodes().get(0);
      String tvShowDir = TvShowRenamer.generateTvShowDir(tvShow);
      String filename = TvShowRenamer.generateFilename(tvShow, episode.getMediaFiles(MediaFileType.VIDEO).get(0));
      String seasonDir = TvShowRenamer.generateSeasonDir(tfSeasonFoldername.getText(), episode);
      lblExample.setText(tvShowDir + File.separator + seasonDir + File.separator + filename);
    }
    else {
      lblExample.setText("");
    }
  }

  private void checkChanges() {
    String separator = (String) cbSeparator.getSelectedItem();
    if (SPACE.equals(separator)) {
      settings.setRenamerSeparator(" ");
    }
    else {
      settings.setRenamerSeparator(separator);
    }

    String spaceReplacement = (String) cbSpaceReplacement.getSelectedItem();
    settings.setRenamerSpaceReplacement(spaceReplacement);

    if (rdbtnRawNumber.isSelected()) {
      settings.setRenamerFormat(TvShowEpisodeNaming.NUMBER);
    }
    else if (rdbtnSeasonEpisode.isSelected()) {
      settings.setRenamerFormat(TvShowEpisodeNaming.WITH_SE);
    }
    else if (rdbtnSxe.isSelected()) {
      settings.setRenamerFormat(TvShowEpisodeNaming.WITH_X);
    }
    else if (rdbtn0Sxe.isSelected()) {
      settings.setRenamerFormat(TvShowEpisodeNaming.WITH_0X);
    }
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

  private class TvShowComparator implements Comparator<TvShow> {
    @Override
    public int compare(TvShow arg0, TvShow arg1) {
      return arg0.getTitle().compareTo(arg1.getTitle());
    }
  }

  protected void initDataBindings() {
    BeanProperty<TvShowSettings, Boolean> settingsBeanProperty = BeanProperty.create("renamerAddSeason");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxAddSeason, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<TvShowSettings, Boolean> settingsBeanProperty_1 = BeanProperty.create("renamerAddShow");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, chckbxAddShow, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<TvShowSettings, Boolean> settingsBeanProperty_2 = BeanProperty.create("renamerAddTitle");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, chckbxAddEpisodeTitle, jCheckBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<TvShowSettings, String> settingsBeanProperty_3 = BeanProperty.create("renamerSeasonFolder");
    BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowSettings, String, JTextField, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_3, tfSeasonFoldername, jTextFieldBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<TvShowSettings, Boolean> settingsBeanProperty_6 = BeanProperty.create("asciiReplacement");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, chckbxAsciiReplacement, jCheckBoxBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<TvShowSettings, Boolean> settingsBeanProperty_7 = BeanProperty.create("renamerTvShowFolder");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_7, chckbxTvShowFolder, jCheckBoxBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<TvShowSettings, Boolean> tvShowSettingsBeanProperty = BeanProperty.create("renamerSpaceSubstitution");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        tvShowSettingsBeanProperty, chckbxSpaceReplacement, jCheckBoxBeanProperty);
    autoBinding_4.bind();
  }
}
