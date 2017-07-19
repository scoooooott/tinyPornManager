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
package org.tinymediamanager.ui.tvshows.panels.episode;

import java.awt.Font;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.tvshows.TvShowEpisodeSelectionModel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowEpisodeDetailsPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeDetailsPanel extends JPanel {
  private static final long                 serialVersionUID = -5598009673335010850L;
  private final static Logger               LOGGER           = LoggerFactory.getLogger(TvShowEpisodeDetailsPanel.class);
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle       BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private final TvShowEpisodeSelectionModel selectionModel;

  /** UI components */
  private LinkLabel                         lblPath;
  private JLabel                            lblSeason;
  private JLabel                            lblEpisode;
  private JLabel                            lblAired;
  private JLabel                            lblTags;
  private JLabel                            lblDateAdded;

  /**
   * Instantiates a new tv show episode details panel.
   * 
   * @param model
   *          the selection model
   */
  public TvShowEpisodeDetailsPanel(TvShowEpisodeSelectionModel model) {
    this.selectionModel = model;

    initComponents();
    initDataBindings();

    lblPath.addActionListener(arg0 -> {
      if (!StringUtils.isEmpty(lblPath.getText())) {
        // get the location from the label
        Path path = Paths.get(lblPath.getText());
        try {
          // check whether this location exists
          if (Files.exists(path)) {
            TmmUIHelper.openFile(path);
          }
        }
        catch (Exception ex) {
          LOGGER.error("open filemanager", ex);
          MessageManager.instance
              .pushMessage(new Message(MessageLevel.ERROR, path, "message.erroropenfolder", new String[] { ":", ex.getLocalizedMessage() }));
        }
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("insets 0", "[][10lp][grow]", "[]2lp[]2lp[]2lp[]2lp[]2lp[]"));
    {
      JLabel lblSeasonT = new JLabel(BUNDLE.getString("metatag.season")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblSeasonT, 1.166, Font.BOLD);
      add(lblSeasonT, "cell 0 0");

      lblSeason = new JLabel("");
      TmmFontHelper.changeFont(lblSeason, 1.166);
      add(lblSeason, "cell 2 0");
    }
    {
      JLabel lblEpisodeT = new JLabel(BUNDLE.getString("metatag.episode")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblEpisodeT, 1.166, Font.BOLD);
      add(lblEpisodeT, "cell 0 1");

      lblEpisode = new JLabel("");
      TmmFontHelper.changeFont(lblEpisode, 1.166);
      add(lblEpisode, "cell 2 1");
    }
    {
      JLabel lblAiredT = new JLabel(BUNDLE.getString("metatag.aired")); //$NON-NLS-1$
      lblAiredT.setFont(lblAiredT.getFont().deriveFont(Font.BOLD));
      add(lblAiredT, "cell 0 2");

      lblAired = new JLabel("");
      add(lblAired, "cell 2 2");
    }
    {
      JLabel lblTagsT = new JLabel(BUNDLE.getString("metatag.tags")); //$NON-NLS-1$
      lblTagsT.setFont(lblTagsT.getFont().deriveFont(Font.BOLD));
      add(lblTagsT, "cell 0 3");

      lblTags = new JLabel("");
      add(lblTags, "cell 2 3");
    }
    {
      JLabel lblDateAddedT = new JLabel(BUNDLE.getString("metatag.dateadded")); //$NON-NLS-1$
      lblDateAddedT.setFont(lblDateAddedT.getFont().deriveFont(Font.BOLD));
      add(lblDateAddedT, "cell 0 4");

      lblDateAdded = new JLabel("");
      add(lblDateAdded, "cell 2 4");
    }
    {
      JLabel lblPathT = new JLabel(BUNDLE.getString("metatag.path")); //$NON-NLS-1$
      lblPathT.setFont(lblPathT.getFont().deriveFont(Font.BOLD));
      add(lblPathT, "cell 0 5");

      lblPath = new LinkLabel("");
      add(lblPath, "cell 2 5, growx, wmin 0");
    }
  }

  protected void initDataBindings() {
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty = BeanProperty.create("selectedTvShowEpisode.path");
    BeanProperty<LinkLabel, String> linkLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowEpisodeSelectionModel, String, LinkLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowEpisodeSelectionModelBeanProperty, lblPath, linkLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, Integer> tvShowEpisodeSelectionModelBeanProperty_1 = BeanProperty
        .create("selectedTvShowEpisode.season");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowEpisodeSelectionModel, Integer, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowEpisodeSelectionModelBeanProperty_1, lblSeason, jLabelBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, Integer> tvShowEpisodeSelectionModelBeanProperty_2 = BeanProperty
        .create("selectedTvShowEpisode.episode");
    AutoBinding<TvShowEpisodeSelectionModel, Integer, JLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowEpisodeSelectionModelBeanProperty_2, lblEpisode, jLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_3 = BeanProperty
        .create("selectedTvShowEpisode.firstAiredAsString");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowEpisodeSelectionModelBeanProperty_3, lblAired, jLabelBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_4 = BeanProperty
        .create("selectedTvShowEpisode.tagsAsString");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowEpisodeSelectionModelBeanProperty_4, lblTags, jLabelBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_5 = BeanProperty
        .create("selectedTvShowEpisode.dateAddedAsString");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowEpisodeSelectionModelBeanProperty_5, lblDateAdded, jLabelBeanProperty);
    autoBinding_5.bind();
  }
}
