/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.ui.tvshows;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.LinkLabel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowEpisodeDetailsPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeDetailsPanel extends JPanel {

  /** The Constant serialVersionUID. */
  private static final long                 serialVersionUID = -5598009673335010850L;

  /** The Constant LOGGER. */
  private final static Logger               LOGGER           = LoggerFactory.getLogger(TvShowEpisodeDetailsPanel.class);

  /** The Constant BUNDLE. */
  private static final ResourceBundle       BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The selection model. */
  private final TvShowEpisodeSelectionModel selectionModel;

  /** The lbl path. */
  private LinkLabel                         lblPath;

  /** The lbl season. */
  private JLabel                            lblSeason;

  /** The lbl episode. */
  private JLabel                            lblEpisode;

  /** The lbl aired. */
  private JLabel                            lblAired;
  private JButton                           btnPlay;

  /**
   * Instantiates a new tv show episode details panel.
   * 
   * @param model
   *          the selection model
   */
  public TvShowEpisodeDetailsPanel(TvShowEpisodeSelectionModel model) {
    this.selectionModel = model;
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("25px"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("55px"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblSeasonT = new JLabel(BUNDLE.getString("metatag.season")); //$NON-NLS-1$
    lblSeasonT.setFont(new Font("Dialog", Font.PLAIN, 14));
    add(lblSeasonT, "2, 2");

    lblSeason = new JLabel("");
    lblSeason.setFont(new Font("Dialog", Font.PLAIN, 14));
    add(lblSeason, "6, 2");

    btnPlay = new JButton("");
    btnPlay.setIcon(new ImageIcon(TvShowEpisodeDetailsPanel.class.getResource("/org/tinymediamanager/ui/images/Play.png")));
    btnPlay.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        MediaFile mf = selectionModel.getSelectedTvShowEpisode().getMediaFiles(MediaFileType.VIDEO).get(0);
        try {
          TmmUIHelper.openFile(mf.getFile());
        }
        catch (Exception e) {
          LOGGER.error("open file", e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, mf, "message.erroropenfile", new String[] { ":",
              e.getLocalizedMessage() }));
        }
      }
    });
    add(btnPlay, "8, 2, 1, 5");

    JLabel lblEpisodeT = new JLabel(BUNDLE.getString("metatag.episode")); //$NON-NLS-1$
    lblEpisodeT.setFont(new Font("Dialog", Font.PLAIN, 14));
    add(lblEpisodeT, "2, 4");

    lblEpisode = new JLabel("");
    lblEpisode.setFont(new Font("Dialog", Font.PLAIN, 14));
    add(lblEpisode, "6, 4");

    JLabel lblAiredT = new JLabel(BUNDLE.getString("metatag.aired")); //$NON-NLS-1$
    add(lblAiredT, "2, 6");

    lblAired = new JLabel("");
    add(lblAired, "6, 6");

    JLabel lblPathT = new JLabel(BUNDLE.getString("metatag.path")); //$NON-NLS-1$
    add(lblPathT, "2, 8");

    lblPath = new LinkLabel("");
    lblPathT.setLabelFor(lblPath);
    lblPath.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (!StringUtils.isEmpty(lblPath.getNormalText())) {
          // get the location from the label
          File path = new File(lblPath.getNormalText());
          try {
            // check whether this location exists
            if (path.exists()) {
              TmmUIHelper.openFile(path);
            }
          }
          catch (Exception ex) {
            LOGGER.error("open filemanager", ex);
            MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, path, "message.erroropenfolder", new String[] { ":",
                ex.getLocalizedMessage() }));
          }
        }
      }
    });
    add(lblPath, "6, 8, 3, 1");
    initDataBindings();
  }

  /**
   * Inits the data bindings.
   */
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
  }
}
