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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.ui.CertificationImageConverter;
import org.tinymediamanager.ui.MediaInfoAudioCodecConverter;
import org.tinymediamanager.ui.MediaInfoVideoCodecConverter;
import org.tinymediamanager.ui.MediaInfoVideoFormatConverter;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.WatchedIconConverter;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.ImageLabel.Position;
import org.tinymediamanager.ui.components.StarRater;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowEpisodeInformationPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeInformationPanel extends JPanel {

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = 2032708149757390567L;

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The split pane vertical. */
  private JSplitPane                  splitPaneVertical;

  /** The panel top. */
  private JPanel                      panelTop;

  /** The panel tv show logos. */
  private JPanel                      panelTvShowLogos;

  /** The panel rating. */
  private StarRater                   panelRatingStars;

  /** The lbl tv show name. */
  private JLabel                      lblTvShowName;

  /** The label rating. */
  private JLabel                      lblRating;

  /** The lbl vote count. */
  private JLabel                      lblVoteCount;

  /** The lbl episode title. */
  private JLabel                      lblEpisodeTitle;

  /** The lbl certification image. */
  private JLabel                      lblCertificationImage;

  /** The lbl tv show background. */
  private ImageLabel                  lblTvShowBackground;

  /** The lbl tv show poster. */
  private ImageLabel                  lblTvShowPoster;

  /** The panel bottom. */
  private JPanel                      panelBottom;

  /** The tp overview. */
  private JTextPane                   tpOverview;

  /** The tv show episode selection model. */
  private TvShowEpisodeSelectionModel tvShowEpisodeSelectionModel;

  /** The panel images. */
  private JPanel                      panelImages;

  /** The panel logos. */
  private JPanel                      panelLogos;

  /** The tabbed pane tv show episode details. */
  private JTabbedPane                 tabbedPaneTvShowEpisodeDetails;

  /** The panel actors. */
  private JPanel                      panelActors;

  /** The panel details. */
  private JPanel                      panelDetails;

  /** The panel media information. */
  private JPanel                      panelMediaInformation;
  private JPanel                      panelMediaFiles;

  /** The lbl media logo resolution. */
  private JLabel                      lblMediaLogoResolution;

  /** The lbl media logo video codec. */
  private JLabel                      lblMediaLogoVideoCodec;

  /** The lbl media logo audio. */
  private JLabel                      lblMediaLogoAudio;

  /** The panel watched. */
  private JPanel                      panelWatched;

  /** The lbl watched. */
  private JLabel                      lblWatched;

  /** The separator. */
  private JSeparator                  separator;

  /**
   * Instantiates a new tv show information panel.
   * 
   * @param tvShowEpisodeSelectionModel
   *          the tv show selection model
   */
  public TvShowEpisodeInformationPanel(TvShowEpisodeSelectionModel tvShowEpisodeSelectionModel) {
    this.tvShowEpisodeSelectionModel = tvShowEpisodeSelectionModel;
    setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("650px:grow"), }, new RowSpec[] { RowSpec.decode("fill:default:grow"), }));

    splitPaneVertical = new JSplitPane();
    splitPaneVertical.setBorder(null);
    splitPaneVertical.setResizeWeight(0.9);
    splitPaneVertical.setContinuousLayout(true);
    splitPaneVertical.setOneTouchExpandable(true);
    splitPaneVertical.setOrientation(JSplitPane.VERTICAL_SPLIT);
    add(splitPaneVertical, "1, 1, fill, fill");

    panelTop = new JPanel();
    panelTop.setBorder(null);
    splitPaneVertical.setTopComponent(panelTop);
    panelTop.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("300px:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { RowSpec.decode("fill:default"),
        RowSpec.decode("default:grow"), }));

    JPanel panelTvShowHeader = new JPanel();
    panelTop.add(panelTvShowHeader, "2, 1, 3, 1, fill, top");
    panelTvShowHeader.setBorder(null);
    panelTvShowHeader.setLayout(new BorderLayout(0, 0));

    JPanel panelMovieTitle = new JPanel();
    panelTvShowHeader.add(panelMovieTitle, BorderLayout.NORTH);
    panelMovieTitle.setLayout(new BorderLayout(0, 0));
    lblTvShowName = new JLabel("");
    panelMovieTitle.add(lblTvShowName);
    lblTvShowName.setFont(new Font("Dialog", Font.BOLD, 16));

    panelWatched = new JPanel();
    panelMovieTitle.add(panelWatched, BorderLayout.EAST);

    lblWatched = new JLabel("");
    panelWatched.add(lblWatched);

    JPanel panelRatingTagline = new JPanel();
    panelTvShowHeader.add(panelRatingTagline, BorderLayout.CENTER);
    panelRatingTagline.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("24px"), FormFactory.DEFAULT_ROWSPEC, }));

    lblRating = new JLabel("");
    panelRatingTagline.add(lblRating, "2, 2, left, center");

    lblVoteCount = new JLabel("");
    panelRatingTagline.add(lblVoteCount, "3, 2, left, center");

    panelRatingStars = new StarRater(5, 2);
    panelRatingTagline.add(panelRatingStars, "1, 2, left, top");
    panelRatingStars.setEnabled(false);

    lblEpisodeTitle = new JLabel();
    panelRatingTagline.add(lblEpisodeTitle, "1, 3, 3, 1, default, center");

    panelTvShowLogos = new JPanel();
    panelTvShowHeader.add(panelTvShowLogos, BorderLayout.EAST);

    lblCertificationImage = new JLabel();
    panelTvShowLogos.add(lblCertificationImage);

    panelImages = new JPanel();
    panelTop.add(panelImages, "1, 2, 4, 1, fill, fill");
    panelImages.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, ColumnSpec.decode("200px"),
        ColumnSpec.decode("25px"), ColumnSpec.decode("400px"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("right:default:grow"), },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, RowSpec.decode("36px"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:261px"), }));

    separator = new JSeparator();
    panelImages.add(separator, "1, 1, 7, 1");

    panelLogos = new JPanel();
    panelImages.add(panelLogos, "5, 2, 3, 1, right, top");
    panelLogos.setOpaque(false);
    panelLogos.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

    lblMediaLogoResolution = new JLabel("");
    panelLogos.add(lblMediaLogoResolution);

    lblMediaLogoVideoCodec = new JLabel("");
    panelLogos.add(lblMediaLogoVideoCodec);

    lblMediaLogoAudio = new JLabel("");
    panelLogos.add(lblMediaLogoAudio);

    lblTvShowPoster = new ImageLabel();
    lblTvShowPoster.setAlternativeText(BUNDLE.getString("image.notfound.poster")); //$NON-NLS-1$
    lblTvShowPoster.setPosition(Position.BOTTOM_LEFT);
    panelImages.add(lblTvShowPoster, "3, 2, 1, 3, fill, fill");

    lblTvShowBackground = new ImageLabel();
    lblTvShowBackground.setAlternativeText(BUNDLE.getString("image.notfound.fanart")); //$NON-NLS-1$
    lblTvShowBackground.setPosition(Position.BOTTOM_LEFT);
    panelImages.add(lblTvShowBackground, "5, 4, fill, fill");

    panelBottom = new JPanel();
    panelBottom.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("496px:grow"), FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));
    splitPaneVertical.setBottomComponent(panelBottom);

    tabbedPaneTvShowEpisodeDetails = new JTabbedPane(JTabbedPane.TOP);
    panelBottom.add(tabbedPaneTvShowEpisodeDetails, "1, 2, fill, fill");

    panelDetails = new TvShowEpisodeDetailsPanel(tvShowEpisodeSelectionModel);
    tabbedPaneTvShowEpisodeDetails.addTab(BUNDLE.getString("metatag.details"), null, panelDetails, null);

    JScrollPane scrollPaneOverview = new JScrollPane();
    tpOverview = new JTextPane();
    tpOverview.setEditable(false);
    scrollPaneOverview.setViewportView(tpOverview);

    JPanel panelOverview = new JPanel();
    panelOverview.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("241px:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
        RowSpec.decode("fill:default:grow"), }));

    tabbedPaneTvShowEpisodeDetails.addTab(BUNDLE.getString("metatag.plot"), null, panelOverview, null); //$NON-NLS-1$
    panelOverview.add(scrollPaneOverview, "1, 2, fill, fill");

    panelActors = new TvShowEpisodeCastPanel(tvShowEpisodeSelectionModel);
    tabbedPaneTvShowEpisodeDetails.addTab(BUNDLE.getString("metatag.cast"), null, panelActors, null); //$NON-NLS-1$

    panelMediaInformation = new TvShowEpisodeMediaInformationPanel(tvShowEpisodeSelectionModel);
    tabbedPaneTvShowEpisodeDetails.addTab(BUNDLE.getString("metatag.mediainformation"), null, panelMediaInformation, null); //$NON-NLS-1$

    panelMediaFiles = new TvShowEpisodeMediaFilesPanel(tvShowEpisodeSelectionModel);
    tabbedPaneTvShowEpisodeDetails.addTab(BUNDLE.getString("metatag.mediafiles"), null, panelMediaFiles, null); //$NON-NLS-1$

    // beansbinding init
    initDataBindings();
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
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_2 = BeanProperty.create("selectedTvShowEpisode.thumb");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imagePath");
    AutoBinding<TvShowEpisodeSelectionModel, String, ImageLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_2, lblTvShowBackground, imageLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_3 = BeanProperty.create("selectedTvShowEpisode.plot");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowEpisodeSelectionModel, String, JTextPane, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_3, tpOverview, jTextPaneBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, Float> tvShowEpisodeSelectionModelBeanProperty_4 = BeanProperty.create("selectedTvShowEpisode.rating");
    BeanProperty<StarRater, Float> starRaterBeanProperty = BeanProperty.create("rating");
    AutoBinding<TvShowEpisodeSelectionModel, Float, StarRater, Float> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_4, panelRatingStars, starRaterBeanProperty);
    autoBinding_4.bind();
    //
    AutoBinding<TvShowEpisodeSelectionModel, Float, JLabel, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_4, lblRating, jLabelBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_5 = BeanProperty
        .create("selectedTvShowEpisode.tvShowSeason.poster");
    AutoBinding<TvShowEpisodeSelectionModel, String, ImageLabel, String> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_5, lblTvShowPoster, imageLabelBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_6 = BeanProperty
        .create("selectedTvShowEpisode.mediaInfoVideoFormat");
    BeanProperty<JLabel, Icon> jLabelBeanProperty_1 = BeanProperty.create("icon");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, Icon> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_6, lblMediaLogoResolution, jLabelBeanProperty_1);
    autoBinding_7.setConverter(new MediaInfoVideoFormatConverter());
    autoBinding_7.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_7 = BeanProperty
        .create("selectedTvShowEpisode.mediaInfoVideoCodec");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, Icon> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_7, lblMediaLogoVideoCodec, jLabelBeanProperty_1);
    autoBinding_8.setConverter(new MediaInfoVideoCodecConverter());
    autoBinding_8.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_8 = BeanProperty
        .create("selectedTvShowEpisode.mediaInfoAudioCodecAndChannels");
    AutoBinding<TvShowEpisodeSelectionModel, String, JLabel, Icon> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_8, lblMediaLogoAudio, jLabelBeanProperty_1);
    autoBinding_9.setConverter(new MediaInfoAudioCodecConverter());
    autoBinding_9.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, Certification> tvShowEpisodeSelectionModelBeanProperty_9 = BeanProperty
        .create("selectedTvShowEpisode.tvShow.certification");
    AutoBinding<TvShowEpisodeSelectionModel, Certification, JLabel, Icon> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_9, lblCertificationImage, jLabelBeanProperty_1);
    autoBinding_10.setConverter(new CertificationImageConverter());
    autoBinding_10.bind();
    //
    BeanProperty<TvShowEpisodeSelectionModel, Boolean> tvShowEpisodeSelectionModelBeanProperty_10 = BeanProperty
        .create("selectedTvShowEpisode.watched");
    AutoBinding<TvShowEpisodeSelectionModel, Boolean, JLabel, Icon> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_10, lblWatched, jLabelBeanProperty_1);
    autoBinding_11.setConverter(new WatchedIconConverter());
    autoBinding_11.bind();
  }
}
