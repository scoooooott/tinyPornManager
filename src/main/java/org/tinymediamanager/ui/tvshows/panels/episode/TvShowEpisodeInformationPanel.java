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

import static org.tinymediamanager.core.Constants.SEASON_POSTER;
import static org.tinymediamanager.core.Constants.THUMB;

import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.ColumnLayout;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.ImageLabel.Position;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.StarRater;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.panels.MediaInformationLogosPanel;
import org.tinymediamanager.ui.tvshows.TvShowEpisodeSelectionModel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowEpisodeInformationPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeInformationPanel extends JPanel {
  private static final long           serialVersionUID = 2032708149757390567L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

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
  private JPanel                      panelDetails;
  private JLabel                      lblSeasonPosterSize;
  private JLabel                      lblEpisodeThumbSize;
  private JLabel                      lblPlot;

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
      // react on selection of a movie and change of a movie
      if (source instanceof TvShowEpisodeSelectionModel) {
        TvShowEpisodeSelectionModel model = (TvShowEpisodeSelectionModel) source;
        setSeasonPoster(model.getSelectedTvShowEpisode());
        setEpisodeThumb(model.getSelectedTvShowEpisode());
        panelLogos.setMediaInformationSource(model.getSelectedTvShowEpisode());
      }
      if ((source.getClass() == TvShowEpisode.class && THUMB.equals(property))) {
        TvShowEpisode episode = (TvShowEpisode) source;
        setEpisodeThumb(episode);
      }
      if ((source.getClass() == TvShowEpisode.class && SEASON_POSTER.equals(property))) {
        TvShowEpisode episode = (TvShowEpisode) source;
        setSeasonPoster(episode);
      }
    };

    this.tvShowEpisodeSelectionModel.addPropertyChangeListener(propertyChangeListener);
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[100lp:100lp,grow][300lp:300lp,grow 250]", "[grow]"));
    {
      JPanel panelLeft = new JPanel();
      panelLeft.setLayout(new ColumnLayout());
      add(panelLeft, "cell 0 0,grow");

      lblSeasonPoster = new ImageLabel(false, false, true);
      lblSeasonPoster.setDesiredAspectRatio(2 / 3.0f);
      panelLeft.add(lblSeasonPoster);
      lblSeasonPoster.setPosition(Position.BOTTOM_LEFT);
      lblSeasonPoster.enableLightbox();

      lblSeasonPosterSize = new JLabel(BUNDLE.getString("mediafiletype.season_poster")); //$NON-NLS-1$
      panelLeft.add(lblSeasonPosterSize);
      panelLeft.add(Box.createVerticalStrut(20));

      lblEpisodeThumb = new ImageLabel(false, false, true);
      lblEpisodeThumb.setDesiredAspectRatio(16 / 9.0f);
      panelLeft.add(lblEpisodeThumb);
      lblEpisodeThumb.setPosition(Position.BOTTOM_LEFT);
      lblEpisodeThumb.enableLightbox();

      lblEpisodeThumbSize = new JLabel(BUNDLE.getString("mediafiletype.thumb")); //$NON-NLS-1$
      panelLeft.add(lblEpisodeThumbSize);
    }
    {
      JPanel panelRight = new JPanel();
      panelRight.setLayout(new MigLayout("", "[grow]", "[][][shrink 0][][shrink 0][][shrink 0][][shrink 0][][]"));
      add(panelRight, "cell 1 0,grow");

      {
        lblTvShowName = new JLabel("");
        panelRight.add(lblTvShowName, "flowx,cell 0 0,growx,wmin 0");
        TmmFontHelper.changeFont(lblTvShowName, 1.33, Font.BOLD);
      }
      {
        lblEpisodeTitle = new JLabel();
        panelRight.add(lblEpisodeTitle, "cell 0 1,growx, wmin 0");
      }
      {
        panelRight.add(new JSeparator(), "cell 0 2, growx, wmin 0");
      }
      {
        panelDetails = new TvShowEpisodeDetailsPanel(tvShowEpisodeSelectionModel);
        panelRight.add(panelDetails, "cell 0 3,growx");
      }
      {
        panelRight.add(new JSeparator(), "cell 0 4,growx");
      }
      {
        panelRatingStars = new StarRater(10, 1);
        panelRight.add(panelRatingStars, "flowx,cell 0 5");
        panelRatingStars.setEnabled(false);

        lblVoteCount = new JLabel("");
        panelRight.add(lblVoteCount, "cell 0 5");

        lblRating = new JLabel("");
        panelRight.add(lblRating, "cell 0 5");
      }
      {
        panelRight.add(new JSeparator(), "cell 0 6,growx");
      }
      {
        panelLogos = new MediaInformationLogosPanel();
        panelRight.add(panelLogos, "cell 0 7");
      }
      {
        panelRight.add(new JSeparator(), "cell 0 8,growx");
      }
      {
        lblPlot = new TmmLabel(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
        panelRight.add(lblPlot, "cell 0 9");

        JScrollPane scrollPanePlot = new JScrollPane();
        scrollPanePlot.setBorder(null);

        taOverview = new ReadOnlyTextArea();
        scrollPanePlot.setViewportView(taOverview);
        panelRight.add(scrollPanePlot, "cell 0 10,grow");
      }
    }
  }

  private void setSeasonPoster(TvShowEpisode tvShowEpisode) {
    // only reset if there was a real change
    if (tvShowEpisode.getTvShowSeason().getPoster().equals(lblSeasonPoster.getImagePath())) {
      return;
    }

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
    // only reset if there was a real change
    if (tvShowEpisode.getArtworkFilename(MediaFileType.THUMB).equals(lblEpisodeThumb.getImagePath())) {
      return;
    }

    lblEpisodeThumb.clearImage();
    lblEpisodeThumb.setImagePath(tvShowEpisode.getArtworkFilename(MediaFileType.THUMB));
    Dimension thumbSize = tvShowEpisode.getArtworkDimension(MediaFileType.THUMB);
    if (thumbSize.width > 0 && thumbSize.height > 0) {
      lblEpisodeThumbSize.setText(BUNDLE.getString("mediafiletype.thumb") + " - " + thumbSize.width + "x" + thumbSize.height); //$NON-NLS-1$
    }
    else {
      lblEpisodeThumbSize.setText(BUNDLE.getString("mediafiletype.thumb")); //$NON-NLS-1$
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
        .create("selectedTvShowEpisode.rating.rating");
    BeanProperty<StarRater, Float> starRaterBeanProperty = BeanProperty.create("rating");
    AutoBinding<TvShowEpisodeSelectionModel, Float, StarRater, Float> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_4, panelRatingStars, starRaterBeanProperty);
    autoBinding_4.bind();
    //
    AutoBinding<TvShowEpisodeSelectionModel, Float, JLabel, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowEpisodeSelectionModel, tvShowEpisodeSelectionModelBeanProperty_4, lblRating, jLabelBeanProperty);
    autoBinding_5.bind();
    //
  }
}
