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
package org.tinymediamanager.ui.tvshows.panels.episode;

import static org.tinymediamanager.core.Constants.MEDIA_FILES;
import static org.tinymediamanager.core.Constants.MEDIA_INFORMATION;
import static org.tinymediamanager.core.Constants.POSTER;
import static org.tinymediamanager.core.Constants.SEASON_POSTER;
import static org.tinymediamanager.core.Constants.THUMB;

import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.ui.ColumnLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.FlatButton;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.StarRater;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.converter.RatingConverter;
import org.tinymediamanager.ui.converter.VoteCountConverter;
import org.tinymediamanager.ui.panels.MediaInformationLogosPanel;
import org.tinymediamanager.ui.tvshows.TvShowEpisodeSelectionModel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowEpisodeInformationPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeInformationPanel extends JPanel {
  private static final Logger         LOGGER           = LoggerFactory.getLogger(TvShowEpisodeInformationPanel.class);
  private static final long           serialVersionUID = 2032708149757390567L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private TvShowSettings              settings         = TvShowModuleManager.SETTINGS;
  private TvShowEpisodeSelectionModel tvShowEpisodeSelectionModel;

  /** UI components */
  private StarRater                   panelRatingStars;
  private JLabel                      lblTvShowName;
  private JLabel                      lblRating;
  private JLabel                      lblVoteCount;
  private JLabel                      lblEpisodeTitle;
  private ImageLabel                  lblEpisodeThumb;
  private ImageLabel                  lblSeasonPoster;
  private JTextArea                   taOverview;
  private MediaInformationLogosPanel  panelLogos;
  private JSeparator                  sepLogos;
  private JLabel                      lblSeasonPosterSize;
  private JLabel                      lblEpisodeThumbSize;
  private JLabel                      lblOriginalTitle;
  private JButton                     btnPlay;
  private JLabel                      lblSeason;
  private JLabel                      lblEpisode;
  private JLabel                      lblAired;
  private JTextArea                   taTags;
  private LinkLabel                   lblPath;
  private JLabel                      lblNote;

  /**
   * Instantiates a new tv show information panel.
   * 
   * @param tvShowEpisodeSelectionModel
   *          the tv show selection model
   */
  public TvShowEpisodeInformationPanel(TvShowEpisodeSelectionModel tvShowEpisodeSelectionModel) {
    this.tvShowEpisodeSelectionModel = tvShowEpisodeSelectionModel;

    initComponents();
    initDataBindings();

    // manual coded binding
    PropertyChangeListener propertyChangeListener = propertyChangeEvent -> {
      String property = propertyChangeEvent.getPropertyName();
      Object source = propertyChangeEvent.getSource();
      // react on selection/change of an episode
      if (source.getClass() != TvShowEpisodeSelectionModel.class) {
        return;
      }

      TvShowEpisodeSelectionModel model = (TvShowEpisodeSelectionModel) source;
      TvShowEpisode episode = model.getSelectedTvShowEpisode();

      if ("selectedTvShowEpisode".equals(property) || POSTER.equals(property) || SEASON_POSTER.equals(property)) {
        setSeasonPoster(episode);
      }

      if ("selectedTvShowEpisode".equals(property) || THUMB.equals(property)) {
        setEpisodeThumb(episode);
      }

      if ("selectedTvShowEpisode".equals(property) || MEDIA_FILES.equals(property) || MEDIA_INFORMATION.equals(property)) {
        panelLogos.setMediaInformationSource(episode);
      }
    };

    this.tvShowEpisodeSelectionModel.addPropertyChangeListener(propertyChangeListener);

    btnPlay.addActionListener(e -> {
      MediaFile mf = this.tvShowEpisodeSelectionModel.getSelectedTvShowEpisode().getMainVideoFile();
      if (StringUtils.isNotBlank(mf.getFilename())) {
        try {
          TmmUIHelper.openFile(mf.getFileAsPath());
        }
        catch (Exception ex) {
          LOGGER.error("open file", e);
          MessageManager.instance
              .pushMessage(new Message(Message.MessageLevel.ERROR, mf, "message.erroropenfile", new String[] { ":", ex.getLocalizedMessage() }));
        }
      }
    });

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
              .pushMessage(new Message(Message.MessageLevel.ERROR, path, "message.erroropenfolder", new String[] { ":", ex.getLocalizedMessage() }));
        }
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[100lp:100lp,grow][300lp:300lp,grow 250]", "[][grow]"));

    {
      JPanel panelLeft = new JPanel();
      panelLeft.setLayout(new ColumnLayout());
      add(panelLeft, "cell 0 0 1 2,grow");

      lblSeasonPoster = new ImageLabel(false, false, true);
      lblSeasonPoster.setDesiredAspectRatio(2 / 3.0f);
      panelLeft.add(lblSeasonPoster);
      lblSeasonPoster.enableLightbox();

      lblSeasonPosterSize = new JLabel(BUNDLE.getString("mediafiletype.season_poster"));
      panelLeft.add(lblSeasonPosterSize);
      panelLeft.add(Box.createVerticalStrut(20));

      lblEpisodeThumb = new ImageLabel(false, false, true);
      lblEpisodeThumb.setDesiredAspectRatio(16 / 9.0f);
      panelLeft.add(lblEpisodeThumb);
      lblEpisodeThumb.enableLightbox();

      lblEpisodeThumbSize = new JLabel(BUNDLE.getString("mediafiletype.thumb"));
      panelLeft.add(lblEpisodeThumbSize);
    }
    {
      JPanel panelTitle = new JPanel();
      add(panelTitle, "cell 1 0,growx");
      panelTitle.setLayout(new MigLayout("insets 0 0 n n", "[grow][]", "[][][][shrink 0]"));

      {
        lblTvShowName = new TmmLabel("", 1.33);
        panelTitle.add(lblTvShowName, "flowx,cell 0 0,growx,wmin 0");
      }
      {
        btnPlay = new FlatButton(IconManager.PLAY_LARGE);
        panelTitle.add(btnPlay, "cell 1 0 1 4,aligny top");
      }
      {
        lblEpisodeTitle = new TmmLabel("", 1.16);
        panelTitle.add(lblEpisodeTitle, "cell 0 1,growx,wmin 0");
      }
      {
        lblOriginalTitle = new JLabel("");
        panelTitle.add(lblOriginalTitle, "cell 0 2,growx,wmin 0");
      }
      {
        panelTitle.add(new JSeparator(), "cell 0 3 2 1,growx");
      }
    }
    {
      JPanel panelRight = new JPanel();
      panelRight.setLayout(new MigLayout("insets n 0 n n, hidemode 2", "[grow]", "[][shrink 0][][shrink 0][][shrink 0][][][][]"));
      add(panelRight, "cell 1 1,grow");
      {
        JPanel panelTopDetails = new JPanel();
        panelTopDetails.setLayout(new MigLayout("insets 0", "[][10lp][grow]", "[]2lp[][]"));

        panelRight.add(panelTopDetails, "cell 0 0,grow");
        {
          JLabel lblSeasonT = new JLabel(BUNDLE.getString("metatag.season"));
          TmmFontHelper.changeFont(lblSeasonT, 1.166, Font.BOLD);
          panelTopDetails.add(lblSeasonT, "cell 0 0");

          lblSeason = new JLabel("");
          TmmFontHelper.changeFont(lblSeason, 1.166);
          panelTopDetails.add(lblSeason, "cell 2 0");
        }
        {
          JLabel lblEpisodeT = new JLabel(BUNDLE.getString("metatag.episode"));
          TmmFontHelper.changeFont(lblEpisodeT, 1.166, Font.BOLD);
          panelTopDetails.add(lblEpisodeT, "cell 0 1");

          lblEpisode = new JLabel("");
          TmmFontHelper.changeFont(lblEpisode, 1.166);
          panelTopDetails.add(lblEpisode, "cell 2 1");
        }
        JLabel lblAiredT = new TmmLabel(BUNDLE.getString("metatag.aired"));
        panelTopDetails.add(lblAiredT, "cell 0 2");
        {

          lblAired = new JLabel("");
          panelTopDetails.add(lblAired, "cell 2 2");
        }
      }
      {
        panelRight.add(new JSeparator(), "cell 0 1,growx");
      }
      {
        panelRatingStars = new StarRater(10, 1);
        panelRight.add(panelRatingStars, "flowx,cell 0 2,aligny center");
        panelRatingStars.setEnabled(false);

        lblRating = new JLabel("");
        panelRight.add(lblRating, "cell 0 2,aligny center");

        lblVoteCount = new JLabel("");
        panelRight.add(lblVoteCount, "cell 0 2,aligny center");
      }
      {
        sepLogos = new JSeparator();
        panelRight.add(sepLogos, "cell 0 3,growx");
      }
      {
        panelLogos = new MediaInformationLogosPanel();
        panelRight.add(panelLogos, "cell 0 4,wmin 0");
      }
      {
        panelRight.add(new JSeparator(), "cell 0 5,growx");
      }
      {
        JLabel lblPlot = new TmmLabel(BUNDLE.getString("metatag.plot"));
        panelRight.add(lblPlot, "cell 0 6");

        taOverview = new ReadOnlyTextArea();
        panelRight.add(taOverview, "cell 0 7,growx,wmin 0,aligny top");
      }
      {
        panelRight.add(new JSeparator(), "cell 0 8,growx");
      }
      {
        JPanel panelBottomDetails = new JPanel();
        panelRight.add(panelBottomDetails, "cell 0 9,grow");
        panelBottomDetails.setLayout(new MigLayout("insets 0", "[][10lp][200lp,grow]", "[]2lp[]2lp[]"));
        {
          {
            JLabel lblTagsT = new TmmLabel(BUNDLE.getString("metatag.tags"));
            panelBottomDetails.add(lblTagsT, "cell 0 0");

            taTags = new ReadOnlyTextArea();
            panelBottomDetails.add(taTags, "cell 2 0,growx,wmin 0");
          }
          {
            JLabel lblPathT = new TmmLabel(BUNDLE.getString("metatag.path"));
            panelBottomDetails.add(lblPathT, "cell 0 1");

            lblPath = new LinkLabel("");
            panelBottomDetails.add(lblPath, "cell 2 1,growx,wmin 0");
          }
          {
            JLabel lblNoteT = new TmmLabel(BUNDLE.getString("metatag.note"));
            panelBottomDetails.add(lblNoteT, "cell 0 2");

            lblNote = new JLabel("");
            panelBottomDetails.add(lblNote, "cell 2 2,growx,wmin 0");
          }
        }
      }
    }
  }

  private void setSeasonPoster(TvShowEpisode tvShowEpisode) {
    lblSeasonPoster.clearImage();
    lblSeasonPoster.setImagePath(tvShowEpisode.getTvShowSeason().getArtworkFilename(MediaArtwork.MediaArtworkType.SEASON_POSTER));
    Dimension posterSize = tvShowEpisode.getTvShowSeason().getArtworkSize(MediaArtwork.MediaArtworkType.SEASON_POSTER);
    if (posterSize.width > 0 && posterSize.height > 0) {
      lblSeasonPosterSize.setText(BUNDLE.getString("mediafiletype.season_poster") + " - " + posterSize.width + "x" + posterSize.height);
    }
    else {
      lblSeasonPosterSize.setText(BUNDLE.getString("mediafiletype.season_poster"));
    }
  }

  private void setEpisodeThumb(TvShowEpisode tvShowEpisode) {
    lblEpisodeThumb.clearImage();
    lblEpisodeThumb.setImagePath(tvShowEpisode.getArtworkFilename(MediaFileType.THUMB));
    Dimension thumbSize = tvShowEpisode.getArtworkDimension(MediaFileType.THUMB);
    if (thumbSize.width > 0 && thumbSize.height > 0) {
      lblEpisodeThumbSize.setText(BUNDLE.getString("mediafiletype.thumb") + " - " + thumbSize.width + "x" + thumbSize.height);
    }
    else {
      lblEpisodeThumbSize.setText(BUNDLE.getString("mediafiletype.thumb"));
    }
  }

  protected void initDataBindings() {
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty = BeanProperty
        .create("selectedTvShowEpisode.tvShow.title");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty, lblTvShowName, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_1 = BeanProperty
        .create("selectedTvShowEpisode.titleForUi");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_1, lblEpisodeTitle, jLabelBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_3 = BeanProperty.create("selectedTvShowEpisode.plot");
    BeanProperty<JTextArea, String> JTextAreaBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowEpisodeSelectionModel, String, JTextArea, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_3, taOverview, JTextAreaBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, Float> tvShowEpisodeSelectionModelBeanProperty_4 = BeanProperty
        .create("selectedTvShowEpisode.rating.ratingNormalized");
    BeanProperty<StarRater, Float> starRaterBeanProperty = BeanProperty.create("rating");
    AutoBinding<TvShowEpisodeSelectionModel, Float, StarRater, Float> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_4, panelRatingStars, starRaterBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, MediaEntity> tvShowEpisodeSelectionModelBeanProperty_6 = BeanProperty.create("selectedTvShowEpisode");
    AutoBinding<TvShowEpisodeSelectionModel, MediaEntity, JLabel, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_6, lblRating, jLabelBeanProperty);
    autoBinding_5.setConverter(new RatingConverter<>());
    autoBinding_5.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_2 = BeanProperty
        .create("selectedTvShowEpisode.originalTitle");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_2, lblOriginalTitle, jLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, Integer> tvShowEpisodeSelectionModelBeanProperty_5 = BeanProperty
        .create("selectedTvShowEpisode.rating.votes");
    AutoBinding<TvShowEpisodeSelectionModel, Integer, JLabel, String> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_5, lblVoteCount, jLabelBeanProperty);
    autoBinding_6.setConverter(new VoteCountConverter());
    autoBinding_6.bind();
    //
    BeanProperty<TvShowSettings, Boolean> tvShowSettingsBeanProperty = BeanProperty.create("showLogosPanel");
    BeanProperty<JSeparator, Boolean> jSeparatorBeanProperty = BeanProperty.create("visible");
    AutoBinding<TvShowSettings, Boolean, JSeparator, Boolean> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ, settings,
        tvShowSettingsBeanProperty, sepLogos, jSeparatorBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<MediaInformationLogosPanel, Boolean> mediaInformationLogosPanelBeanProperty = BeanProperty.create("visible");
    AutoBinding<TvShowSettings, Boolean, MediaInformationLogosPanel, Boolean> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ,
        settings, tvShowSettingsBeanProperty, panelLogos, mediaInformationLogosPanelBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, Integer> tvShowEpisodeSelectionModelBeanProperty_7 = BeanProperty
        .create("selectedTvShowEpisode.season");
    AutoBinding<TvShowEpisodeSelectionModel, Integer, JLabel, String> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_7, lblSeason, jLabelBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, Integer> tvShowEpisodeSelectionModelBeanProperty_8 = BeanProperty
        .create("selectedTvShowEpisode.episode");
    AutoBinding<TvShowEpisodeSelectionModel, Integer, JLabel, String> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_8, lblEpisode, jLabelBeanProperty);
    autoBinding_10.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_9 = BeanProperty
        .create("selectedTvShowEpisode.firstAiredAsString");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, String> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_9, lblAired, jLabelBeanProperty);
    autoBinding_11.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_10 = BeanProperty
        .create("selectedTvShowEpisode.tagsAsString");
    BeanProperty<JTextArea, String> jTextAreaBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowEpisodeSelectionModel, String, JTextArea, String> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_10, taTags, jTextAreaBeanProperty);
    autoBinding_12.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_11 = BeanProperty.create("selectedTvShowEpisode.path");
    BeanProperty<LinkLabel, String> linkLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowEpisodeSelectionModel, String, LinkLabel, String> autoBinding_13 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_11, lblPath, linkLabelBeanProperty);
    autoBinding_13.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_12 = BeanProperty.create("selectedTvShowEpisode.note");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, String> autoBinding_14 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_12, lblNote, jLabelBeanProperty);
    autoBinding_14.bind();
  }
}
