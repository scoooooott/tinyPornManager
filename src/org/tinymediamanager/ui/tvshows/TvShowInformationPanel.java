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

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.ui.CertificationImageConverter;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.StarRater;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowInformationPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowInformationPanel extends JPanel {

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = 1911808562993073590L;

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The split pane vertical. */
  private JSplitPane                  splitPaneVertical;

  /** The tabbed pane tv show details. */
  private JTabbedPane                 tabbedPaneTvShowDetails;

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

  /** The lbl certification image. */
  private JLabel                      lblCertificationImage;

  /** The lbl tv show background. */
  private ImageLabel                  lblTvShowBackground;

  /** The lbl tv show poster. */
  private ImageLabel                  lblTvShowPoster;

  /** The lbl tv show banner. */
  private ImageLabel                  lblTvShowBanner;

  /** The panel bottom. */
  private JPanel                      panelBottom;

  /** The tp overview. */
  private JTextPane                   tpOverview;

  /** The tv show selection model. */
  private TvShowSelectionModel        tvShowSelectionModel;

  /** The panel media information. */
  private JPanel                      panelMediaInformation;

  /**
   * Instantiates a new tv show information panel.
   * 
   * @param tvShowSelectionModel
   *          the tv show selection model
   */
  public TvShowInformationPanel(TvShowSelectionModel tvShowSelectionModel) {
    this.tvShowSelectionModel = tvShowSelectionModel;
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
        RowSpec.decode("top:pref:grow"), }));

    JPanel panelTvShowHeader = new JPanel();
    panelTop.add(panelTvShowHeader, "2, 1, 3, 1, fill, top");
    panelTvShowHeader.setBorder(null);
    panelTvShowHeader.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("min:grow"), FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
        FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JPanel panelTvShowTitle = new JPanel();
    panelTvShowHeader.add(panelTvShowTitle, "1, 1, fill, top");
    panelTvShowTitle.setLayout(new BorderLayout(0, 0));
    lblTvShowName = new JLabel("");
    panelTvShowTitle.add(lblTvShowName);
    lblTvShowName.setFont(new Font("Dialog", Font.BOLD, 16));

    JPanel panelRatingTagline = new JPanel();
    panelTvShowHeader.add(panelRatingTagline, "1, 2, fill, top");
    panelRatingTagline.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("24px"), }));

    lblRating = new JLabel("");
    panelRatingTagline.add(lblRating, "2, 2, left, center");

    lblVoteCount = new JLabel("");
    panelRatingTagline.add(lblVoteCount, "3, 2, left, center");

    panelRatingStars = new StarRater(5, 2);
    panelRatingTagline.add(panelRatingStars, "1, 2, left, top");
    panelRatingStars.setEnabled(false);

    panelTvShowLogos = new JPanel();
    panelTvShowHeader.add(panelTvShowLogos, "2, 1, 1, 2, fill, fill");

    lblCertificationImage = new JLabel();
    panelTvShowLogos.add(lblCertificationImage);

    JLayeredPane layeredPaneImages = new JLayeredPane();
    panelTop.add(layeredPaneImages, "1, 2, 4, 1, fill, fill");
    layeredPaneImages.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("max(10px;default)"), ColumnSpec.decode("left:120px:grow"),
        ColumnSpec.decode("default:grow(8)"), ColumnSpec.decode("400px"), }, new RowSpec[] { RowSpec.decode("max(10px;default)"),
        RowSpec.decode("top:100px"), RowSpec.decode("80px:grow"), RowSpec.decode("fill:80px:grow(3)"), }));

    lblTvShowBackground = new ImageLabel(false, true);
    layeredPaneImages.add(lblTvShowBackground, "1, 1, 4, 4, fill, fill");

    lblTvShowPoster = new ImageLabel();
    layeredPaneImages.setLayer(lblTvShowPoster, 1);
    layeredPaneImages.add(lblTvShowPoster, "2, 2, 1, 2, fill, fill");

    JPanel panelGenres = new TvShowGenresPanel(tvShowSelectionModel);
    layeredPaneImages.setLayer(panelGenres, 2);
    layeredPaneImages.add(panelGenres, "2, 3, 3, 2, right, bottom");

    lblTvShowBanner = new ImageLabel();
    layeredPaneImages.setLayer(lblTvShowBanner, 2);
    layeredPaneImages.add(lblTvShowBanner, "4, 1, 1, 2, fill, fill");

    panelBottom = new JPanel();
    panelBottom.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("496px:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
        RowSpec.decode("min:grow"), }));
    splitPaneVertical.setBottomComponent(panelBottom);

    tabbedPaneTvShowDetails = new JTabbedPane(JTabbedPane.TOP);
    panelBottom.add(tabbedPaneTvShowDetails, "1, 2, fill, fill");

    JPanel panelDetails = new TvShowDetailsPanel(tvShowSelectionModel);
    tabbedPaneTvShowDetails.addTab(BUNDLE.getString("metatag.details"), null, panelDetails, null); //$NON-NLS-1$

    JPanel panelOverview = new JPanel();
    panelOverview.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("241px:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
        RowSpec.decode("fill:default:grow"), }));

    tabbedPaneTvShowDetails.addTab(BUNDLE.getString("metatag.plot"), null, panelOverview, null); //$NON-NLS-1$

    JScrollPane scrollPaneOverview = new JScrollPane();
    panelOverview.add(scrollPaneOverview, "1, 2, fill, fill");

    tpOverview = new JTextPane();
    tpOverview.setEditable(false);
    scrollPaneOverview.setViewportView(tpOverview);

    TvShowCastPanel panelCast = new TvShowCastPanel(tvShowSelectionModel);
    panelCast.init();
    tabbedPaneTvShowDetails.addTab(BUNDLE.getString("metatag.cast"), null, panelCast, null); //$NON-NLS-1$

    panelMediaInformation = new TvShowMediaInformationPanel(tvShowSelectionModel);
    tabbedPaneTvShowDetails.addTab(BUNDLE.getString("metatag.mediainformation"), null, panelMediaInformation, null); //$NON-NLS-1$

    // beansbinding init
    initDataBindings();
  }

  protected void initDataBindings() {
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty = BeanProperty.create("selectedTvShow.title");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowSelectionModel,
        tvShowSelectionModelBeanProperty, lblTvShowName, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_1 = BeanProperty.create("selectedTvShow.plot");
    BeanProperty<JTextPane, String> jTextPaneBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowSelectionModel, String, JTextPane, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowSelectionModel, tvShowSelectionModelBeanProperty_1, tpOverview, jTextPaneBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<TvShowSelectionModel, Float> tvShowSelectionModelBeanProperty_2 = BeanProperty.create("selectedTvShow.rating");
    BeanProperty<StarRater, Float> starRaterBeanProperty = BeanProperty.create("rating");
    AutoBinding<TvShowSelectionModel, Float, StarRater, Float> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowSelectionModel,
        tvShowSelectionModelBeanProperty_2, panelRatingStars, starRaterBeanProperty);
    autoBinding_2.bind();
    //
    AutoBinding<TvShowSelectionModel, Float, JLabel, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowSelectionModel,
        tvShowSelectionModelBeanProperty_2, lblRating, jLabelBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_5 = BeanProperty.create("selectedTvShow.fanart");
    BeanProperty<ImageLabel, String> imageLabelBeanProperty = BeanProperty.create("imagePath");
    AutoBinding<TvShowSelectionModel, String, ImageLabel, String> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowSelectionModel, tvShowSelectionModelBeanProperty_5, lblTvShowBackground, imageLabelBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_6 = BeanProperty.create("selectedTvShow.poster");
    AutoBinding<TvShowSelectionModel, String, ImageLabel, String> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowSelectionModel, tvShowSelectionModelBeanProperty_6, lblTvShowPoster, imageLabelBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_7 = BeanProperty.create("selectedTvShow.banner");
    AutoBinding<TvShowSelectionModel, String, ImageLabel, String> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowSelectionModel, tvShowSelectionModelBeanProperty_7, lblTvShowBanner, imageLabelBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<TvShowSelectionModel, Certification> tvShowSelectionModelBeanProperty_8 = BeanProperty.create("selectedTvShow.certification");
    BeanProperty<JLabel, Icon> jLabelBeanProperty_2 = BeanProperty.create("icon");
    AutoBinding<TvShowSelectionModel, Certification, JLabel, Icon> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowSelectionModel, tvShowSelectionModelBeanProperty_8, lblCertificationImage, jLabelBeanProperty_2);
    autoBinding_9.setConverter(new CertificationImageConverter());
    autoBinding_9.bind();
  }
}
