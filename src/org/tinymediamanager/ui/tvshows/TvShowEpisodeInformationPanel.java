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

import static org.tinymediamanager.core.Constants.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.Box;
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
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.ui.ColumnLayout;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.ImageLabel.Position;
import org.tinymediamanager.ui.components.StarRater;
import org.tinymediamanager.ui.converter.CertificationImageConverter;
import org.tinymediamanager.ui.converter.MediaInfoAudioCodecConverter;
import org.tinymediamanager.ui.converter.MediaInfoVideoCodecConverter;
import org.tinymediamanager.ui.converter.MediaInfoVideoFormatConverter;
import org.tinymediamanager.ui.converter.WatchedIconConverter;

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
  private static final long           serialVersionUID = 2032708149757390567L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** UI components */
  private JSplitPane                  splitPaneVertical;
  private JPanel                      panelTop;
  private JPanel                      panelTvShowLogos;
  private StarRater                   panelRatingStars;
  private JLabel                      lblTvShowName;
  private JLabel                      lblRating;
  private JLabel                      lblVoteCount;
  private JLabel                      lblEpisodeTitle;
  private JLabel                      lblCertificationImage;
  private ImageLabel                  lblEpisodeThumb;
  private ImageLabel                  lblSeasonPoster;
  private JPanel                      panelBottom;
  private JTextPane                   tpOverview;
  private JPanel                      panelLogos;
  private JTabbedPane                 tabbedPaneTvShowEpisodeDetails;
  private JPanel                      panelActors;
  private JPanel                      panelDetails;
  private JPanel                      panelMediaInformation;
  private JPanel                      panelMediaFiles;
  private JLabel                      lblMediaLogoResolution;
  private JLabel                      lblMediaLogoVideoCodec;
  private JLabel                      lblMediaLogoAudio;
  private JPanel                      panelWatched;
  private JLabel                      lblWatched;

  private TvShowEpisodeSelectionModel tvShowEpisodeSelectionModel;
  private JPanel                      panelLeft;
  private JLabel                      lblSeasonPosterSize;
  private JLabel                      lblEpisodeThumbSize;
  private JSeparator                  separator;
  private JLabel                      lblPlot;

  /**
   * Instantiates a new tv show information panel.
   * 
   * @param tvShowEpisodeSelectionModel
   *          the tv show selection model
   */
  public TvShowEpisodeInformationPanel(TvShowEpisodeSelectionModel tvShowEpisodeSelectionModel) {
    this.tvShowEpisodeSelectionModel = tvShowEpisodeSelectionModel;
    setLayout(new FormLayout(
        new ColumnSpec[] { ColumnSpec.decode("100px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("200px:grow(4)"), },
        new RowSpec[] { RowSpec.decode("fill:default:grow"), }));

    panelLeft = new JPanel();
    panelLeft.setLayout(new ColumnLayout());
    add(panelLeft, "1, 1, fill, fill");

    lblSeasonPoster = new ImageLabel(false) {
      private static final long serialVersionUID = -4774846565578766742L;

      @Override
      public Dimension getPreferredSize() {
        if (originalImage != null) {
          return new Dimension(getParent().getWidth(),
              (int) (getParent().getWidth() / (float) originalImage.getWidth() * (float) originalImage.getHeight()));
        }
        return new Dimension(getParent().getWidth(), (int) (getParent().getWidth() / 2d * 3d) + 1);
      }
    };
    panelLeft.add(lblSeasonPoster);
    lblSeasonPoster.setAlternativeText(BUNDLE.getString("image.notfound.poster")); //$NON-NLS-1$
    lblSeasonPoster.setPosition(Position.BOTTOM_LEFT);
    lblSeasonPoster.enableLightbox();

    lblSeasonPosterSize = new JLabel(BUNDLE.getString("mediafiletype.season_poster")); //$NON-NLS-1$
    panelLeft.add(lblSeasonPosterSize);
    panelLeft.add(Box.createVerticalStrut(20));

    lblEpisodeThumb = new ImageLabel(false) {
      private static final long serialVersionUID = -4774846565578766742L;

      @Override
      public Dimension getPreferredSize() {
        if (originalImage != null) {
          return new Dimension(getParent().getWidth(),
              (int) (getParent().getWidth() / (float) originalImage.getWidth() * (float) originalImage.getHeight()));
        }
        return new Dimension(getParent().getWidth(), (int) (getParent().getWidth() / 16d * 9d) + 1);
      }
    };
    panelLeft.add(lblEpisodeThumb);
    lblEpisodeThumb.setAlternativeText(BUNDLE.getString("image.notfound.thumb")); //$NON-NLS-1$
    lblEpisodeThumb.setPosition(Position.BOTTOM_LEFT);
    lblEpisodeThumb.enableLightbox();

    lblEpisodeThumbSize = new JLabel(BUNDLE.getString("mediafiletype.thumb")); //$NON-NLS-1$
    panelLeft.add(lblEpisodeThumbSize);

    splitPaneVertical = new JSplitPane();
    splitPaneVertical.setBorder(null);
    splitPaneVertical.setResizeWeight(0.9);
    splitPaneVertical.setContinuousLayout(true);
    splitPaneVertical.setOneTouchExpandable(true);
    splitPaneVertical.setOrientation(JSplitPane.VERTICAL_SPLIT);
    add(splitPaneVertical, "3, 1, fill, fill");

    panelTop = new JPanel();
    panelTop.setBorder(null);
    splitPaneVertical.setTopComponent(panelTop);
    panelTop.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("200px:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { RowSpec.decode("fill:default"), FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("fill:36px"),
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        RowSpec.decode("top:50px:grow(2)"), }));

    JPanel panelTvShowHeader = new JPanel();
    panelTop.add(panelTvShowHeader, "2, 1, 3, 1, fill, top");
    panelTvShowHeader.setBorder(null);
    panelTvShowHeader.setLayout(new BorderLayout(0, 0));

    JPanel panelMovieTitle = new JPanel();
    panelTvShowHeader.add(panelMovieTitle, BorderLayout.NORTH);
    panelMovieTitle.setLayout(new BorderLayout(0, 0));
    lblTvShowName = new JLabel("");
    panelMovieTitle.add(lblTvShowName);
    TmmFontHelper.changeFont(lblTvShowName, 1.33, Font.BOLD);

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

    panelRatingStars = new StarRater(10, 1);
    panelRatingTagline.add(panelRatingStars, "1, 2, left, top");
    panelRatingStars.setEnabled(false);

    lblEpisodeTitle = new JLabel();
    panelRatingTagline.add(lblEpisodeTitle, "1, 3, 3, 1, default, center");

    panelTvShowLogos = new JPanel();
    panelTvShowHeader.add(panelTvShowLogos, BorderLayout.EAST);

    lblCertificationImage = new JLabel();
    panelTvShowLogos.add(lblCertificationImage);

    separator = new JSeparator();
    panelTop.add(separator, "2, 4, 3, 1");

    panelDetails = new TvShowEpisodeDetailsPanel(tvShowEpisodeSelectionModel);
    panelTop.add(panelDetails, "2, 6, 3, 1");

    panelTop.add(new JSeparator(), "2, 8, 3, 1");

    panelLogos = new JPanel();
    panelTop.add(panelLogos, "2, 10, 3, 1");
    panelLogos.setOpaque(false);
    panelLogos.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

    lblMediaLogoResolution = new JLabel("");
    panelLogos.add(lblMediaLogoResolution);

    lblMediaLogoVideoCodec = new JLabel("");
    panelLogos.add(lblMediaLogoVideoCodec);

    lblMediaLogoAudio = new JLabel("");
    panelLogos.add(lblMediaLogoAudio);

    panelTop.add(new JSeparator(), "2, 12, 3, 1");

    lblPlot = new JLabel(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
    lblPlot.setFont(lblPlot.getFont().deriveFont(Font.BOLD));
    panelTop.add(lblPlot, "2, 14");

    JScrollPane scrollPaneOverview = new JScrollPane();
    scrollPaneOverview.setBorder(null);
    tpOverview = new JTextPane();
    tpOverview.setOpaque(false);
    tpOverview.setEditable(false);
    scrollPaneOverview.setViewportView(tpOverview);

    JPanel panelOverview = new JPanel();
    panelTop.add(panelOverview, "2, 15, 3, 1, fill, fill");
    panelOverview.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
        RowSpec.decode("fill:default:grow"), }));
    panelOverview.add(scrollPaneOverview, "1, 2, fill, fill");

    panelBottom = new JPanel();
    panelBottom.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("200px:grow"), FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));
    splitPaneVertical.setBottomComponent(panelBottom);

    tabbedPaneTvShowEpisodeDetails = new JTabbedPane(JTabbedPane.TOP);
    panelBottom.add(tabbedPaneTvShowEpisodeDetails, "1, 2, fill, fill");

    panelActors = new TvShowEpisodeCastPanel(tvShowEpisodeSelectionModel);
    tabbedPaneTvShowEpisodeDetails.addTab(BUNDLE.getString("metatag.cast"), null, panelActors, null); //$NON-NLS-1$

    panelMediaInformation = new TvShowEpisodeMediaInformationPanel(tvShowEpisodeSelectionModel);
    tabbedPaneTvShowEpisodeDetails.addTab(BUNDLE.getString("metatag.mediainformation"), null, panelMediaInformation, null); //$NON-NLS-1$

    panelMediaFiles = new TvShowEpisodeMediaFilesPanel(tvShowEpisodeSelectionModel);
    tabbedPaneTvShowEpisodeDetails.addTab(BUNDLE.getString("metatag.mediafiles"), null, panelMediaFiles, null); //$NON-NLS-1$

    // beansbinding init
    initDataBindings();

    // manual coded binding
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String property = propertyChangeEvent.getPropertyName();
        Object source = propertyChangeEvent.getSource();
        // react on selection of a movie and change of a movie
        if (source instanceof TvShowEpisodeSelectionModel) {
          TvShowEpisodeSelectionModel model = (TvShowEpisodeSelectionModel) source;
          setSeasonPoster(model.getSelectedTvShowEpisode());
          setEpisodeThumb(model.getSelectedTvShowEpisode());
        }
        if ((source.getClass() == TvShowEpisode.class && THUMB.equals(property))) {
          TvShowEpisode episode = (TvShowEpisode) source;
          setEpisodeThumb(episode);
        }
        if ((source.getClass() == TvShowEpisode.class && SEASON_POSTER.equals(property))) {
          TvShowEpisode episode = (TvShowEpisode) source;
          setSeasonPoster(episode);
        }
      }
    };

    this.tvShowEpisodeSelectionModel.addPropertyChangeListener(propertyChangeListener);
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

  private void setSeasonPoster(TvShowEpisode tvShowEpisode) {
    lblSeasonPoster.clearImage();
    lblSeasonPoster.setImagePath(tvShowEpisode.getTvShowSeason().getPoster());
    Dimension posterSize = tvShowEpisode.getTvShowSeason().getPosterSize();
    if (posterSize.width > 0 && posterSize.height > 0) {
      lblSeasonPosterSize.setText(BUNDLE.getString("mediafiletype.season_poster") + " - " + posterSize.width + "x" + posterSize.height); //$NON-NLS-1$
    }
    else {
      lblSeasonPosterSize.setText(BUNDLE.getString("mediafiletype.season_poster")); //$NON-NLS-1$
    }
  }

  private void setEpisodeThumb(TvShowEpisode tvShowEpisode) {
    lblEpisodeThumb.clearImage();
    lblEpisodeThumb.setImagePath(tvShowEpisode.getThumb());
    Dimension thumbSize = tvShowEpisode.getThumbSize();
    if (thumbSize.width > 0 && thumbSize.height > 0) {
      lblEpisodeThumbSize.setText(BUNDLE.getString("mediafiletype.thumb") + " - " + thumbSize.width + "x" + thumbSize.height); //$NON-NLS-1$
    }
    else {
      lblEpisodeThumbSize.setText(BUNDLE.getString("mediafiletype.thumb")); //$NON-NLS-1$
    }
  }
}
