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
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.ui.ColumnLayout;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.StarRater;
import org.tinymediamanager.ui.converter.CertificationImageConverter;

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
  private static final long           serialVersionUID = 1911808562993073590L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** UI components */
  private JSplitPane                  splitPaneVertical;
  private JTabbedPane                 tabbedPaneTvShowDetails;
  private JPanel                      panelTop;
  private JPanel                      panelTvShowLogos;
  private StarRater                   panelRatingStars;
  private JLabel                      lblTvShowName;
  private JLabel                      lblRating;
  private JLabel                      lblVoteCount;
  private JLabel                      lblCertificationImage;
  private ImageLabel                  lblTvShowBackground;
  private JLabel                      lblFanartSize;
  private ImageLabel                  lblTvShowPoster;
  private JLabel                      lblPosterSize;
  private ImageLabel                  lblTvShowBanner;
  private JLabel                      lblBannerSize;
  private JPanel                      panelBottom;
  private JTextPane                   tpOverview;
  private JPanel                      panelMediaInformation;
  private JPanel                      panelRight;
  private JPanel                      panelLeft;
  private JLabel                      lblPlot;
  private JSeparator                  separator;
  private JSeparator                  separator_1;

  private TvShowSelectionModel        tvShowSelectionModel;

  /**
   * Instantiates a new tv show information panel.
   * 
   * @param tvShowSelectionModel
   *          the tv show selection model
   */
  public TvShowInformationPanel(TvShowSelectionModel tvShowSelectionModel) {
    this.tvShowSelectionModel = tvShowSelectionModel;
    setLayout(new FormLayout(
        new ColumnSpec[] { ColumnSpec.decode("100px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("200px:grow(4)"), },
        new RowSpec[] { RowSpec.decode("fill:default:grow"), }));

    panelLeft = new JPanel();
    add(panelLeft, "1, 1, fill, fill");
    panelLeft.setLayout(new ColumnLayout());

    lblTvShowPoster = new ImageLabel(false) {
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
    panelLeft.add(lblTvShowPoster);
    lblTvShowPoster.setAlternativeText(BUNDLE.getString("image.notfound.poster")); //$NON-NLS-1$
    lblTvShowPoster.enableLightbox();
    lblPosterSize = new JLabel(BUNDLE.getString("mediafiletype.poster")); //$NON-NLS-1$
    panelLeft.add(lblPosterSize);
    panelLeft.add(Box.createVerticalStrut(20));

    lblTvShowBackground = new ImageLabel(false) {
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
    panelLeft.add(lblTvShowBackground);
    lblTvShowBackground.setAlternativeText(BUNDLE.getString("image.notfound.fanart"));
    lblTvShowBackground.enableLightbox();
    lblFanartSize = new JLabel(BUNDLE.getString("mediafiletype.fanart")); //$NON-NLS-1$
    panelLeft.add(lblFanartSize);
    panelLeft.add(Box.createVerticalStrut(20));

    lblTvShowBanner = new ImageLabel(false) {
      private static final long serialVersionUID = -4774846565578766742L;

      @Override
      public Dimension getPreferredSize() {
        if (originalImage != null) {
          return new Dimension(getParent().getWidth(),
              (int) (getParent().getWidth() / (float) originalImage.getWidth() * (float) originalImage.getHeight()));
        }
        return new Dimension(getParent().getWidth(), (int) (getParent().getWidth() / 25d * 8d) + 1);
      }
    };
    panelLeft.add(lblTvShowBanner);
    lblTvShowBanner.setAlternativeText(BUNDLE.getString("image.notfound.banner")); //$NON-NLS-1$
    lblTvShowBanner.enableLightbox();
    lblBannerSize = new JLabel(BUNDLE.getString("mediafiletype.banner")); //$NON-NLS-1$
    panelLeft.add(lblBannerSize);

    panelRight = new JPanel();
    add(panelRight, "3, 1, fill, fill");
    panelRight
        .setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("200px:grow"), }, new RowSpec[] { RowSpec.decode("fill:default:grow"), }));

    splitPaneVertical = new JSplitPane();
    panelRight.add(splitPaneVertical, "1, 1, fill, fill");
    splitPaneVertical.setBorder(null);
    splitPaneVertical.setResizeWeight(0.9);
    splitPaneVertical.setContinuousLayout(true);
    splitPaneVertical.setOneTouchExpandable(true);
    splitPaneVertical.setOrientation(JSplitPane.VERTICAL_SPLIT);

    panelTop = new JPanel();
    panelTop.setBorder(null);
    splitPaneVertical.setTopComponent(panelTop);
    panelTop.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("200px:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { RowSpec.decode("fill:default"), FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        RowSpec.decode("top:50px:grow(2)"), }));

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

    panelRatingStars = new StarRater(10, 1);
    panelRatingTagline.add(panelRatingStars, "1, 2, left, top");
    panelRatingStars.setEnabled(false);

    panelTvShowLogos = new JPanel();
    panelTvShowHeader.add(panelTvShowLogos, "2, 1, 1, 2, fill, fill");

    lblCertificationImage = new JLabel();
    panelTvShowLogos.add(lblCertificationImage);

    separator_1 = new JSeparator();
    panelTop.add(separator_1, "2, 4, 3, 1");

    JPanel panelDetails = new TvShowDetailsPanel(tvShowSelectionModel);
    panelTop.add(panelDetails, "2, 6, 3, 1");

    separator = new JSeparator();
    panelTop.add(separator, "2, 8, 3, 1");

    lblPlot = new JLabel(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
    lblPlot.setFont(lblPlot.getFont().deriveFont(Font.BOLD));
    panelTop.add(lblPlot, "2, 10, 3, 1");

    JPanel panelOverview = new JPanel();
    panelTop.add(panelOverview, "2, 11, 3, 1, fill, fill");
    panelOverview.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
        RowSpec.decode("fill:default:grow"), }));

    JScrollPane scrollPaneOverview = new JScrollPane();
    scrollPaneOverview.setBorder(null);
    panelOverview.add(scrollPaneOverview, "1, 2, fill, fill");

    tpOverview = new JTextPane();
    tpOverview.setOpaque(false);
    tpOverview.setEditable(false);
    scrollPaneOverview.setViewportView(tpOverview);

    panelBottom = new JPanel();
    panelBottom.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("200px:grow"), }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
        RowSpec.decode("min:grow"), }));
    splitPaneVertical.setBottomComponent(panelBottom);

    tabbedPaneTvShowDetails = new JTabbedPane(JTabbedPane.TOP);
    panelBottom.add(tabbedPaneTvShowDetails, "1, 2, fill, fill");

    TvShowCastPanel panelCast = new TvShowCastPanel(tvShowSelectionModel);
    tabbedPaneTvShowDetails.addTab(BUNDLE.getString("metatag.cast"), null, panelCast, null); //$NON-NLS-1$

    panelMediaInformation = new TvShowMediaInformationPanel(tvShowSelectionModel);
    tabbedPaneTvShowDetails.addTab(BUNDLE.getString("metatag.mediafiles"), null, panelMediaInformation, null); //$NON-NLS-1$

    // beansbinding init
    initDataBindings();

    // manual coded binding
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String property = propertyChangeEvent.getPropertyName();
        Object source = propertyChangeEvent.getSource();
        // react on selection of a movie and change of a tv show
        if (source instanceof TvShowSelectionModel) {
          TvShowSelectionModel model = (TvShowSelectionModel) source;
          setFanart(model.getSelectedTvShow());
          setPoster(model.getSelectedTvShow());
          setBanner(model.getSelectedTvShow());
        }
        if ((source.getClass() == TvShow.class && FANART.equals(property))) {
          TvShow tvShow = (TvShow) source;
          setFanart(tvShow);
        }
        if ((source.getClass() == TvShow.class && POSTER.equals(property))) {
          TvShow tvShow = (TvShow) source;
          setPoster(tvShow);
        }
        if ((source.getClass() == TvShow.class && BANNER.equals(property))) {
          TvShow tvShow = (TvShow) source;
          setBanner(tvShow);
        }
      }
    };

    tvShowSelectionModel.addPropertyChangeListener(propertyChangeListener);

    // select first entry

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
    BeanProperty<TvShowSelectionModel, Certification> tvShowSelectionModelBeanProperty_8 = BeanProperty.create("selectedTvShow.certification");
    BeanProperty<JLabel, Icon> jLabelBeanProperty_2 = BeanProperty.create("icon");
    AutoBinding<TvShowSelectionModel, Certification, JLabel, Icon> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowSelectionModel, tvShowSelectionModelBeanProperty_8, lblCertificationImage, jLabelBeanProperty_2);
    autoBinding_9.setConverter(new CertificationImageConverter());
    autoBinding_9.bind();
  }

  private void setPoster(TvShow tvShow) {
    lblTvShowPoster.clearImage();
    lblTvShowPoster.setImagePath(tvShow.getPoster());
    Dimension posterSize = tvShow.getPosterSize();
    if (posterSize.width > 0 && posterSize.height > 0) {
      lblPosterSize.setText(BUNDLE.getString("mediafiletype.poster") + " - " + posterSize.width + "x" + posterSize.height); //$NON-NLS-1$
    }
    else {
      lblPosterSize.setText(BUNDLE.getString("mediafiletype.poster")); //$NON-NLS-1$
    }
  }

  private void setFanart(TvShow tvShow) {
    lblTvShowBackground.clearImage();
    lblTvShowBackground.setImagePath(tvShow.getFanart());
    Dimension fanartSize = tvShow.getFanartSize();
    if (fanartSize.width > 0 && fanartSize.height > 0) {
      lblFanartSize.setText(BUNDLE.getString("mediafiletype.fanart") + " - " + fanartSize.width + "x" + fanartSize.height); //$NON-NLS-1$
    }
    else {
      lblFanartSize.setText(BUNDLE.getString("mediafiletype.fanart")); //$NON-NLS-1$
    }
  }

  private void setBanner(TvShow tvShow) {
    lblTvShowBanner.clearImage();
    lblTvShowBanner.setImagePath(tvShow.getBanner());
    Dimension bannerSize = tvShow.getBannerSize();
    if (bannerSize.width > 0 && bannerSize.height > 0) {
      lblBannerSize.setText(BUNDLE.getString("mediafiletype.banner") + " - " + bannerSize.width + "x" + bannerSize.height); //$NON-NLS-1$
    }
    else {
      lblBannerSize.setText(BUNDLE.getString("mediafiletype.banner")); //$NON-NLS-1$
    }
  }
}
