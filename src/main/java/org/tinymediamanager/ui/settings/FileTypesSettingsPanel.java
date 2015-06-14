/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ScrollablePanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class FileTypesSettingsPanel extends ScrollablePanel {
  private static final long           serialVersionUID = 9136097757447080369L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private Settings                    settings         = Settings.getInstance();
  private JPanel                      panelVideoFiletypes;
  private JTextField                  tfVideoFiletype;
  private JList                       listVideoFiletypes;
  private JPanel                      panelSubtitleFiletypes;
  private JTextField                  tfSubtitleFiletype;
  private JList                       listSubtitleFiletypes;
  private JList                       listSortPrefixes;
  private JTextField                  tfSortPrefix;
  private JPanel                      panelAudioFiletypes;
  private JList                       listAudioFiletypes;
  private JTextField                  tfAudioFiletype;

  /**
   * Instantiates a new general settings panel.
   */
  public FileTypesSettingsPanel() {
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:max(200px;min)"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(200px;default)"), FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("max(200px;default)"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(200px;default)"), }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("top:default"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("top:default"),
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

    panelVideoFiletypes = new JPanel();
    panelVideoFiletypes.setBorder(new TitledBorder(
        UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("Settings.videofiletypes"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    panelVideoFiletypes.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JScrollPane scrollPaneVideoFiletypes = new JScrollPane();
    panelVideoFiletypes.add(scrollPaneVideoFiletypes, "2, 2, 5, 1, fill, fill");

    listVideoFiletypes = new JList();
    scrollPaneVideoFiletypes.setViewportView(listVideoFiletypes);

    tfVideoFiletype = new JTextField();
    panelVideoFiletypes.add(tfVideoFiletype, "2, 4, fill, default");
    tfVideoFiletype.setColumns(10);

    JButton btnAddVideoFiletype = new JButton(IconManager.LIST_ADD);
    btnAddVideoFiletype.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    btnAddVideoFiletype.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (StringUtils.isNotEmpty(tfVideoFiletype.getText())) {
          Globals.settings.addVideoFileTypes(tfVideoFiletype.getText());
          tfVideoFiletype.setText("");
        }
      }
    });

    panelVideoFiletypes.add(btnAddVideoFiletype, "4, 4");
    add(panelVideoFiletypes, "2, 2, fill, fill");

    JButton btnRemoveVideoFiletype = new JButton(IconManager.LIST_REMOVE);
    btnRemoveVideoFiletype.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
    btnRemoveVideoFiletype.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int row = listVideoFiletypes.getSelectedIndex();
        if (row != -1) {
          String prefix = Globals.settings.getVideoFileType().get(row);
          Globals.settings.removeVideoFileType(prefix);
        }
      }
    });
    panelVideoFiletypes.add(btnRemoveVideoFiletype, "6, 4, default, bottom");

    panelSubtitleFiletypes = new JPanel();
    add(panelSubtitleFiletypes, "4, 2, fill, fill");
    panelSubtitleFiletypes.setBorder(new TitledBorder(
        UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("Settings.extrafiletypes"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    panelSubtitleFiletypes.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
    JScrollPane scrollPaneSubtitleFiletypes = new JScrollPane();
    panelSubtitleFiletypes.add(scrollPaneSubtitleFiletypes, "2, 2, 5, 1, fill, fill");

    listSubtitleFiletypes = new JList();
    scrollPaneSubtitleFiletypes.setViewportView(listSubtitleFiletypes);

    tfSubtitleFiletype = new JTextField();
    panelSubtitleFiletypes.add(tfSubtitleFiletype, "2, 4, fill, default");
    tfSubtitleFiletype.setColumns(10);

    JButton btnAddSubtitleFiletype = new JButton(IconManager.LIST_ADD);
    btnAddSubtitleFiletype.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    btnAddSubtitleFiletype.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (StringUtils.isNotEmpty(tfSubtitleFiletype.getText())) {
          Globals.settings.addSubtitleFileTypes(tfSubtitleFiletype.getText());
          tfSubtitleFiletype.setText("");
        }
      }
    });
    panelSubtitleFiletypes.add(btnAddSubtitleFiletype, "4, 4");

    JButton btnRemoveSubtitleFiletype = new JButton(IconManager.LIST_REMOVE);
    btnRemoveSubtitleFiletype.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
    btnRemoveSubtitleFiletype.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int row = listSubtitleFiletypes.getSelectedIndex();
        if (row != -1) {
          String prefix = Globals.settings.getSubtitleFileType().get(row);
          Globals.settings.removeSubtitleFileType(prefix);
        }
      }
    });
    panelSubtitleFiletypes.add(btnRemoveSubtitleFiletype, "6, 4, default, bottom");

    panelAudioFiletypes = new JPanel();
    add(panelAudioFiletypes, "6, 2, fill, fill");
    panelAudioFiletypes.setBorder(new TitledBorder(
        UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("Settings.audiofiletypes"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    panelAudioFiletypes.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
    JScrollPane scrollPaneAudioFiletypes = new JScrollPane();
    panelAudioFiletypes.add(scrollPaneAudioFiletypes, "2, 2, 5, 1, fill, fill");

    listAudioFiletypes = new JList();
    scrollPaneAudioFiletypes.setViewportView(listAudioFiletypes);

    tfAudioFiletype = new JTextField();
    panelAudioFiletypes.add(tfAudioFiletype, "2, 4, fill, default");
    tfAudioFiletype.setColumns(10);

    JButton btnAddAudioFiletype = new JButton(IconManager.LIST_ADD);
    btnAddAudioFiletype.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    btnAddAudioFiletype.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (StringUtils.isNotEmpty(tfAudioFiletype.getText())) {
          Globals.settings.addAudioFileTypes(tfAudioFiletype.getText());
          tfAudioFiletype.setText("");
        }
      }
    });
    panelAudioFiletypes.add(btnAddAudioFiletype, "4, 4");

    JButton btnRemoveAudioFiletype = new JButton(IconManager.LIST_REMOVE);
    btnRemoveAudioFiletype.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
    btnRemoveAudioFiletype.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int row = listAudioFiletypes.getSelectedIndex();
        if (row != -1) {
          String prefix = Globals.settings.getAudioFileType().get(row);
          Globals.settings.removeAudioFileType(prefix);
        }
      }
    });
    panelAudioFiletypes.add(btnRemoveAudioFiletype, "6, 4, default, bottom");

    JPanel panelSortOptions = new JPanel();
    panelSortOptions.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), BUNDLE.getString("Settings.sorting"),
        TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelSortOptions, "2, 4, 3, 1, fill, fill");
    panelSortOptions.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JScrollPane scrollPaneSortPrefixes = new JScrollPane();
    panelSortOptions.add(scrollPaneSortPrefixes, "2, 2, 5, 1, fill, fill");

    listSortPrefixes = new JList();
    scrollPaneSortPrefixes.setViewportView(listSortPrefixes);

    tfSortPrefix = new JTextField();
    panelSortOptions.add(tfSortPrefix, "2, 4, fill, default");
    tfSortPrefix.setColumns(10);

    JButton btnAddSortPrefix = new JButton(IconManager.LIST_ADD);
    btnAddSortPrefix.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    btnAddSortPrefix.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (StringUtils.isNotEmpty(tfSortPrefix.getText())) {
          Globals.settings.addTitlePrefix(tfSortPrefix.getText());
          tfSortPrefix.setText("");
          MovieList.getInstance().invalidateTitleSortable();
          TvShowList.getInstance().invalidateTitleSortable();
        }
      }
    });
    panelSortOptions.add(btnAddSortPrefix, "4, 4");

    JButton btnRemoveSortPrefix = new JButton(IconManager.LIST_REMOVE);
    btnRemoveSortPrefix.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
    btnRemoveSortPrefix.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int row = listSortPrefixes.getSelectedIndex();
        if (row != -1) {
          String prefix = Globals.settings.getTitlePrefix().get(row);
          Globals.settings.removeTitlePrefix(prefix);
          MovieList.getInstance().invalidateTitleSortable();
          TvShowList.getInstance().invalidateTitleSortable();
        }
      }
    });
    panelSortOptions.add(btnRemoveSortPrefix, "6, 4, default, bottom");

    JTextPane tpSortingHints = new JTextPane();
    TmmFontHelper.changeFont(tpSortingHints, 0.833);
    tpSortingHints.setText(BUNDLE.getString("Settings.sorting.info")); //$NON-NLS-1$
    tpSortingHints.setBackground(UIManager.getColor("Panel.background"));
    panelSortOptions.add(tpSortingHints, "2, 6, 3, 1, fill, fill");

    initDataBindings();
  }

  protected void initDataBindings() {
    BeanProperty<Settings, List<String>> settingsBeanProperty_5 = BeanProperty.create("videoFileType");
    JListBinding<String, Settings, JList> jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_5, listVideoFiletypes);
    jListBinding_1.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_6 = BeanProperty.create("subtitleFileType");
    JListBinding<String, Settings, JList> jListBinding_2 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_6, listSubtitleFiletypes);
    jListBinding_2.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_10 = BeanProperty.create("titlePrefix");
    JListBinding<String, Settings, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_10, listSortPrefixes);
    jListBinding.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_11 = BeanProperty.create("audioFileType");
    JListBinding<String, Settings, JList> jListBinding_3 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_11, listAudioFiletypes);
    jListBinding_3.bind();
  }
}
