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
import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.ui.ImageLabel;
import org.tinymediamanager.ui.StarRater;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * 
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeInformationPanel extends JPanel {

  private static final long           serialVersionUID = 2032708149757390567L;

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

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

  private TvShowEpisodeSelectionModel tvShowEpisodeSelectionModel;

  /** The panel details. */
  private JPanel                      panelDetails;

  /** The lbl ttvdbid t. */
  private JLabel                      lblTtvdbidT;

  /** The lbl imdbid t. */
  private JLabel                      lblImdbidT;

  /** The lbl ttvdb id. */
  private JLabel                      lblTtvdbId;

  /** The lbl imdb id. */
  private JLabel                      lblImdbId;

  /**
   * Instantiates a new tv show information panel.
   * 
   * @param tvShowEpisodeSelectionModel
   *          the tv show selection model
   */
  public TvShowEpisodeInformationPanel(TvShowEpisodeSelectionModel tvShowEpisodeSelectionModel) {
    this.tvShowEpisodeSelectionModel = tvShowEpisodeSelectionModel;
    setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("336px:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
        RowSpec.decode("265px:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    panelTop = new JPanel();
    panelTop.setBorder(null);
    add(panelTop, "1, 2, fill, fill");
    panelTop.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("300px:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { RowSpec.decode("fill:default"),
        RowSpec.decode("top:pref:grow"), }));

    JPanel panelMovieHeader = new JPanel();
    panelTop.add(panelMovieHeader, "2, 1, 3, 1, fill, top");
    panelMovieHeader.setBorder(null);
    panelMovieHeader.setLayout(new BorderLayout(0, 0));

    JPanel panelMovieTitle = new JPanel();
    panelMovieHeader.add(panelMovieTitle, BorderLayout.NORTH);
    panelMovieTitle.setLayout(new BorderLayout(0, 0));
    lblTvShowName = new JLabel("");
    // panelMovieHeader.add(lblMovieName, BorderLayout.NORTH);
    panelMovieTitle.add(lblTvShowName);
    lblTvShowName.setFont(new Font("Dialog", Font.BOLD, 16));

    JPanel panelRatingTagline = new JPanel();
    panelMovieHeader.add(panelRatingTagline, BorderLayout.CENTER);
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
    panelMovieHeader.add(panelTvShowLogos, BorderLayout.EAST);

    lblCertificationImage = new JLabel();
    panelTvShowLogos.add(lblCertificationImage);

    JLayeredPane layeredPaneImages = new JLayeredPane();
    panelTop.add(layeredPaneImages, "1, 2, 4, 1, fill, fill");
    layeredPaneImages.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("max(10px;default)"), ColumnSpec.decode("left:120px"),
        ColumnSpec.decode("default:grow"), }, new RowSpec[] { RowSpec.decode("max(10px;default)"), RowSpec.decode("top:180px"),
        RowSpec.decode("fill:default:grow"), }));

    lblTvShowBackground = new ImageLabel(false, true);
    layeredPaneImages.add(lblTvShowBackground, "1, 1, 3, 3, fill, fill");

    lblTvShowPoster = new ImageLabel();
    layeredPaneImages.setLayer(lblTvShowPoster, 1);
    layeredPaneImages.add(lblTvShowPoster, "2, 2, fill, fill");

    panelBottom = new JPanel();
    panelBottom.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
        FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));
    add(panelBottom, "1, 4, fill, bottom");

    JScrollPane scrollPaneOverview = new JScrollPane();
    panelBottom.add(scrollPaneOverview, "1, 2, fill, fill");

    tpOverview = new JTextPane();
    scrollPaneOverview.setViewportView(tpOverview);

    panelDetails = new JPanel();
    panelBottom.add(panelDetails, "3, 2, fill, fill");
    panelDetails.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(30dlu;default)"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(30dlu;default)"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("top:max(50dlu;default)"), }));

    lblTtvdbidT = new JLabel("ttvdbId");
    panelDetails.add(lblTtvdbidT, "2, 2");

    lblTtvdbId = new JLabel("");
    panelDetails.add(lblTtvdbId, "4, 2");

    lblImdbidT = new JLabel("imdbId");
    panelDetails.add(lblImdbidT, "6, 2");

    lblImdbId = new JLabel("");
    panelDetails.add(lblImdbId, "8, 2");

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
    BeanProperty<TvShowEpisodeSelectionModel, String> tvShowEpisodeSelectionModelBeanProperty_2 = BeanProperty.create("selectedTvShowEpisode.fanart");
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
  }
}
