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
package org.tinymediamanager.ui.tvshows.panels.tvshow;

import static org.tinymediamanager.core.Constants.BANNER;
import static org.tinymediamanager.core.Constants.FANART;
import static org.tinymediamanager.core.Constants.MEDIA_FILES;
import static org.tinymediamanager.core.Constants.MEDIA_INFORMATION;
import static org.tinymediamanager.core.Constants.POSTER;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.ui.ColumnLayout;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.StarRater;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.converter.CertificationImageConverter;
import org.tinymediamanager.ui.converter.RatingConverter;
import org.tinymediamanager.ui.converter.VoteCountConverter;
import org.tinymediamanager.ui.converter.ZeroIdConverter;
import org.tinymediamanager.ui.panels.MediaInformationLogosPanel;
import org.tinymediamanager.ui.tvshows.TvShowOtherIdsConverter;
import org.tinymediamanager.ui.tvshows.TvShowSelectionModel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowInformationPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowInformationPanel extends JPanel {
  private static final long           serialVersionUID = 1911808562993073590L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());
  private static final Logger         LOGGER           = LoggerFactory.getLogger(TvShowInformationPanel.class);

  private final TvShowSelectionModel  tvShowSelectionModel;

  private JTextArea                   taGenres;
  private JLabel                      lblCertification;
  private LinkLabel                   lblThetvdbId;
  private LinkLabel                   lblImdbId;
  private LinkLabel                   lblTmdbId;
  private LinkLabel                   lblPath;
  private JLabel                      lblPremiered;
  private JTextArea                   taStudio;
  private JLabel                      lblStatus;
  private JLabel                      lblYear;
  private JTextArea                   taTags;
  private JTextArea                   taOtherIds;
  private JLabel                      lblCountry;
  private JLabel                      lblRuntime;
  private JTextArea                   taNote;
  private StarRater                   panelRatingStars;
  private JLabel                      lblTvShowName;
  private JLabel                      lblRating;
  private JLabel                      lblVoteCount;
  private ImageLabel                  lblTvShowBackground;
  private JLabel                      lblFanartSize;
  private ImageLabel                  lblTvShowPoster;
  private JLabel                      lblPosterSize;
  private ImageLabel                  lblTvShowBanner;
  private JLabel                      lblBannerSize;
  private JTextArea                   taOverview;
  private JSeparator                  sepLogos;
  private MediaInformationLogosPanel  panelLogos;
  private JLabel                      lblOriginalTitle;
  private JScrollPane                 scrollPane;
  private JLabel                      lblCertificationLogo;

  /**
   * Instantiates a new tv show information panel.
   *
   * @param tvShowSelectionModel
   *          the tv show selection model
   */
  public TvShowInformationPanel(TvShowSelectionModel tvShowSelectionModel) {
    this.tvShowSelectionModel = tvShowSelectionModel;

    initComponents();

    // beansbinding init
    initDataBindings();

    // action listeners
    lblImdbId.addActionListener(arg0 -> {
      String url = "https://www.imdb.com/title/" + lblImdbId.getText();
      try {
        TmmUIHelper.browseUrl(url);
      }
      catch (Exception e) {
        LOGGER.error("browse to imdbid", e);
        MessageManager.instance
            .pushMessage(new Message(Message.MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":", e.getLocalizedMessage() }));
      }
    });

    lblThetvdbId.addActionListener(arg0 -> {
      String url = "https://thetvdb.com/?tab=series&id=" + lblThetvdbId.getText();
      try {
        TmmUIHelper.browseUrl(url);
      }
      catch (Exception e) {
        LOGGER.error("browse to thetvdb", e);
        MessageManager.instance
            .pushMessage(new Message(Message.MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":", e.getLocalizedMessage() }));
      }
    });

    lblTmdbId.addActionListener(arg0 -> {
      String url = "https://www.themoviedb.org/tv/" + lblTmdbId.getText();
      try {
        TmmUIHelper.browseUrl(url);
      }
      catch (Exception e) {
        LOGGER.error("browse to tmdb", e);
        MessageManager.instance
            .pushMessage(new Message(Message.MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":", e.getLocalizedMessage() }));
      }
    });

    lblPath.addActionListener(e -> {
      if (StringUtils.isNotBlank(lblPath.getText())) {
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

    // manual coded binding
    PropertyChangeListener propertyChangeListener = propertyChangeEvent -> {
      String property = propertyChangeEvent.getPropertyName();
      Object source = propertyChangeEvent.getSource();
      // react on selection/change of a TV show
      if (source.getClass() != TvShowSelectionModel.class) {
        return;
      }

      TvShowSelectionModel model = (TvShowSelectionModel) source;
      TvShow tvShow = model.getSelectedTvShow();

      if ("selectedTvShow".equals(property) || POSTER.equals(property)) {
        setPoster(tvShow);
      }

      if ("selectedTvShow".equals(property) || FANART.equals(property)) {
        setFanart(tvShow);
      }

      if ("selectedTvShow".equals(property) || BANNER.equals(property)) {
        setBanner(tvShow);
      }

      if ("selectedTvShow".equals(property) || MEDIA_FILES.equals(property) || MEDIA_INFORMATION.equals(property)) {
        panelLogos.setMediaInformationSource(tvShow);
      }

      if ("selectedTvShow".equals(property)) {
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
      }
    };

    tvShowSelectionModel.addPropertyChangeListener(propertyChangeListener);

    // select first entry

  }

  private void initComponents() {
    setLayout(new MigLayout("", "[100lp:100lp,grow][300lp:300lp,grow 250]", "[][grow]"));
    {
      JPanel panelLeft = new JPanel();
      panelLeft.setLayout(new ColumnLayout());
      add(panelLeft, "cell 0 0 1 2,grow");

      lblTvShowPoster = new ImageLabel(false, false, true);
      lblTvShowPoster.setDesiredAspectRatio(2 / 3f);
      lblTvShowPoster.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      panelLeft.add(lblTvShowPoster);
      lblTvShowPoster.enableLightbox();
      lblPosterSize = new JLabel(BUNDLE.getString("mediafiletype.poster"));
      panelLeft.add(lblPosterSize);
      panelLeft.add(Box.createVerticalStrut(20));

      lblTvShowBackground = new ImageLabel(false, false, true);
      lblTvShowBackground.setDesiredAspectRatio(16 / 9f);
      lblTvShowBackground.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      panelLeft.add(lblTvShowBackground);
      lblTvShowBackground.enableLightbox();
      lblFanartSize = new JLabel(BUNDLE.getString("mediafiletype.fanart"));
      panelLeft.add(lblFanartSize);
      panelLeft.add(Box.createVerticalStrut(20));

      lblTvShowBanner = new ImageLabel(false, false, true);
      lblTvShowBanner.setDesiredAspectRatio(25 / 8f);
      lblTvShowBanner.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      panelLeft.add(lblTvShowBanner);
      lblTvShowBanner.enableLightbox();
      lblBannerSize = new JLabel(BUNDLE.getString("mediafiletype.banner"));
      panelLeft.add(lblBannerSize);
    }
    {
      JPanel panelTitle = new JPanel();
      add(panelTitle, "cell 1 0,grow");
      panelTitle.setLayout(new MigLayout("insets 0 0 n n", "[grow]", "[][][shrink 0]"));
      {
        lblTvShowName = new TmmLabel("", 1.33);
        panelTitle.add(lblTvShowName, "cell 0 0,growx,wmin 0");
      }
      {
        lblOriginalTitle = new JLabel("");
        panelTitle.add(lblOriginalTitle, "cell 0 1,growx,wmin 0");
      }
      {
        panelTitle.add(new JSeparator(), "cell 0 2,growx");
      }
    }
    {
      JPanel panelRight = new JPanel();
      panelRight.setLayout(new MigLayout("insets n 0 n n, hidemode 2", "[100lp,grow]", "[][shrink 0][][shrink 0][][shrink 0][][grow][][]"));

      scrollPane = new JScrollPane(panelRight);
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      add(scrollPane, "cell 1 1,grow, wmin 0");
      {
        JPanel panelTopDetails = new JPanel();
        panelRight.add(panelTopDetails, "cell 0 0,growx");
        panelTopDetails.setLayout(new MigLayout("insets 0", "[][][40lp][][grow][]", "[]2lp[]2lp[grow]2lp[]2lp[]2lp[]2lp[]2lp[]"));
        {
          JLabel lblYearT = new TmmLabel(BUNDLE.getString("metatag.year"));
          panelTopDetails.add(lblYearT, "flowy,cell 0 0");

          lblYear = new JLabel("");
          panelTopDetails.add(lblYear, "cell 1 0");
        }
        {
          JLabel lblImdbIdT = new TmmLabel("IMDB ID");
          panelTopDetails.add(lblImdbIdT, "cell 3 0");

          lblImdbId = new LinkLabel("");
          panelTopDetails.add(lblImdbId, "cell 4 0");
        }
        {
          lblCertificationLogo = new JLabel("");
          panelTopDetails.add(lblCertificationLogo, "cell 5 0 1 3, top");
        }
        {
          JLabel lblPremieredT = new TmmLabel(BUNDLE.getString("metatag.premiered"));
          panelTopDetails.add(lblPremieredT, "cell 0 1");

          lblPremiered = new JLabel("");
          panelTopDetails.add(lblPremiered, "cell 1 1");
        }
        {
          JLabel lblThetvdbIdT = new TmmLabel("TheTVDB ID");
          lblThetvdbIdT.setText("TheTVDB ID");
          panelTopDetails.add(lblThetvdbIdT, "cell 3 1");

          lblThetvdbId = new LinkLabel("");
          panelTopDetails.add(lblThetvdbId, "cell 4 1");
        }
        {
          JLabel lblCertificationT = new TmmLabel(BUNDLE.getString("metatag.certification"));
          panelTopDetails.add(lblCertificationT, "cell 0 2");

          lblCertification = new JLabel("");
          panelTopDetails.add(lblCertification, "cell 1 2");
        }
        {
          JLabel lblTmdbIdT = new TmmLabel(BUNDLE.getString("metatag.tmdb"));
          panelTopDetails.add(lblTmdbIdT, "cell 3 2");

          lblTmdbId = new LinkLabel();
          panelTopDetails.add(lblTmdbId, "cell 4 2");
        }
        {
          JLabel lblOtherIdsT = new TmmLabel(BUNDLE.getString("metatag.otherids"));
          panelTopDetails.add(lblOtherIdsT, "cell 3 3");

          taOtherIds = new ReadOnlyTextArea();
          panelTopDetails.add(taOtherIds, "cell 4 3 2 1,growx,wmin 0");
        }
        {
          JLabel lblRuntimeT = new TmmLabel(BUNDLE.getString("metatag.runtime"));
          panelTopDetails.add(lblRuntimeT, "cell 0 3,aligny top");

          lblRuntime = new JLabel("");
          panelTopDetails.add(lblRuntime, "cell 1 3,aligny top");
        }
        {
          JLabel lblGenresT = new TmmLabel(BUNDLE.getString("metatag.genre"));
          panelTopDetails.add(lblGenresT, "cell 0 4");

          taGenres = new ReadOnlyTextArea();
          panelTopDetails.add(taGenres, "cell 1 4 5 1,growx,wmin 0");
        }
        {
          JLabel lblStatusT = new TmmLabel(BUNDLE.getString("metatag.status"));
          panelTopDetails.add(lblStatusT, "cell 0 5");

          lblStatus = new JLabel("");
          panelTopDetails.add(lblStatus, "cell 1 5 4 1");
        }
        {
          JLabel lblStudioT = new TmmLabel(BUNDLE.getString("metatag.studio"));
          panelTopDetails.add(lblStudioT, "cell 0 6,wmin 0");

          taStudio = new ReadOnlyTextArea();
          panelTopDetails.add(taStudio, "cell 1 6 5 1,growx, wmin 0");
        }
        {
          JLabel lblCountryT = new TmmLabel(BUNDLE.getString("metatag.country"));
          panelTopDetails.add(lblCountryT, "cell 0 7");

          lblCountry = new JLabel("");
          panelTopDetails.add(lblCountry, "cell 1 7 5 1, wmin 0");
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
        TmmFontHelper.changeFont(lblPlot, Font.BOLD);

        taOverview = new ReadOnlyTextArea();
        panelRight.add(taOverview, "cell 0 7,growx,wmin 0,aligny top");
      }
      {
        panelRight.add(new JSeparator(), "cell 0 8,growx");
      }
      {
        JPanel panelBottomDetails = new JPanel();
        panelBottomDetails.setLayout(new MigLayout("insets 0", "[][grow]", "[]2lp[]2lp[]"));
        panelRight.add(panelBottomDetails, "cell 0 9,grow");

        {
          JLabel lblTagsT = new TmmLabel(BUNDLE.getString("metatag.tags"));
          panelBottomDetails.add(lblTagsT, "cell 0 0");

          taTags = new ReadOnlyTextArea();
          panelBottomDetails.add(taTags, "cell 1 0,growx,wmin 0");
        }
        {
          JLabel lblPathT = new TmmLabel(BUNDLE.getString("metatag.path"));
          panelBottomDetails.add(lblPathT, "cell 0 1");

          lblPath = new LinkLabel("");
          panelBottomDetails.add(lblPath, "cell 1 1,growx,wmin 0");
        }
        {
          JLabel lblNoteT = new TmmLabel(BUNDLE.getString("metatag.note"));
          panelBottomDetails.add(lblNoteT, "cell 0 2");

          taNote = new ReadOnlyTextArea();
          panelBottomDetails.add(taNote, "cell 1 2,growx,wmin 0");
        }
      }
    }
  }

  private void setPoster(TvShow tvShow) {
    lblTvShowPoster.clearImage();
    lblTvShowPoster.setImagePath(tvShow.getArtworkFilename(MediaFileType.POSTER));
    Dimension posterSize = tvShow.getArtworkDimension(MediaFileType.POSTER);
    if (posterSize.width > 0 && posterSize.height > 0) {
      lblPosterSize.setText(BUNDLE.getString("mediafiletype.poster") + " - " + posterSize.width + "x" + posterSize.height);
    }
    else {
      lblPosterSize.setText(BUNDLE.getString("mediafiletype.poster"));
    }
  }

  private void setFanart(TvShow tvShow) {
    lblTvShowBackground.clearImage();
    lblTvShowBackground.setImagePath(tvShow.getArtworkFilename(MediaFileType.FANART));
    Dimension fanartSize = tvShow.getArtworkDimension(MediaFileType.FANART);
    if (fanartSize.width > 0 && fanartSize.height > 0) {
      lblFanartSize.setText(BUNDLE.getString("mediafiletype.fanart") + " - " + fanartSize.width + "x" + fanartSize.height);
    }
    else {
      lblFanartSize.setText(BUNDLE.getString("mediafiletype.fanart"));
    }
  }

  private void setBanner(TvShow tvShow) {
    lblTvShowBanner.clearImage();
    lblTvShowBanner.setImagePath(tvShow.getArtworkFilename(MediaFileType.BANNER));
    Dimension bannerSize = tvShow.getArtworkDimension(MediaFileType.BANNER);
    if (bannerSize.width > 0 && bannerSize.height > 0) {
      lblBannerSize.setText(BUNDLE.getString("mediafiletype.banner") + " - " + bannerSize.width + "x" + bannerSize.height);
    }
    else {
      lblBannerSize.setText(BUNDLE.getString("mediafiletype.banner"));
    }
  }

  protected void initDataBindings() {
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty = BeanProperty.create("selectedTvShow.title");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowSelectionModel,
        tvShowSelectionModelBeanProperty, lblTvShowName, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_1 = BeanProperty.create("selectedTvShow.plot");
    BeanProperty<JTextArea, String> jTextAreaBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowSelectionModel, String, JTextArea, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowSelectionModel,
        tvShowSelectionModelBeanProperty_1, taOverview, jTextAreaBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<TvShowSelectionModel, Float> tvShowSelectionModelBeanProperty_2 = BeanProperty.create("selectedTvShow.rating.ratingNormalized");
    BeanProperty<StarRater, Float> starRaterBeanProperty = BeanProperty.create("rating");
    AutoBinding<TvShowSelectionModel, Float, StarRater, Float> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowSelectionModel,
        tvShowSelectionModelBeanProperty_2, panelRatingStars, starRaterBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<TvShowSelectionModel, TvShow> tvShowSelectionModelBeanProperty_5 = BeanProperty.create("selectedTvShow");
    AutoBinding<TvShowSelectionModel, TvShow, JLabel, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowSelectionModel,
        tvShowSelectionModelBeanProperty_5, lblRating, jLabelBeanProperty);
    autoBinding_3.setConverter(new RatingConverter<>());
    autoBinding_3.bind();
    //
    BeanProperty<TvShowSelectionModel, Integer> tvShowSelectionModelBeanProperty_3 = BeanProperty.create("selectedTvShow.rating.votes");
    AutoBinding<TvShowSelectionModel, Integer, JLabel, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowSelectionModel,
        tvShowSelectionModelBeanProperty_3, lblVoteCount, jLabelBeanProperty);
    autoBinding_4.setConverter(new VoteCountConverter());
    autoBinding_4.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_4 = BeanProperty.create("selectedTvShow.originalTitle");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowSelectionModel,
        tvShowSelectionModelBeanProperty_4, lblOriginalTitle, jLabelBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<TvShowSelectionModel, Integer> tvShowSelectionModelBeanProperty_6 = BeanProperty.create("selectedTvShow.year");
    AutoBinding<TvShowSelectionModel, Integer, JLabel, String> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowSelectionModel,
        tvShowSelectionModelBeanProperty_6, lblYear, jLabelBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_7 = BeanProperty.create("selectedTvShow.imdbId");
    BeanProperty<LinkLabel, String> linkLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowSelectionModel, String, LinkLabel, String> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowSelectionModel,
        tvShowSelectionModelBeanProperty_7, lblImdbId, linkLabelBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_8 = BeanProperty.create("selectedTvShow.certification.name");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowSelectionModel,
        tvShowSelectionModelBeanProperty_8, lblCertification, jLabelBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_9 = BeanProperty.create("selectedTvShow.tvdbId");
    AutoBinding<TvShowSelectionModel, String, LinkLabel, String> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowSelectionModel,
        tvShowSelectionModelBeanProperty_9, lblThetvdbId, linkLabelBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<TvShowSelectionModel, Integer> tvShowSelectionModelBeanProperty_10 = BeanProperty.create("selectedTvShow.runtime");
    AutoBinding<TvShowSelectionModel, Integer, JLabel, String> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowSelectionModel,
        tvShowSelectionModelBeanProperty_10, lblRuntime, jLabelBeanProperty);
    autoBinding_10.bind();
    //
    BeanProperty<TvShowSelectionModel, Map<String, Object>> tvShowSelectionModelBeanProperty_11 = BeanProperty.create("selectedTvShow.ids");
    AutoBinding<TvShowSelectionModel, Map<String, Object>, JTextArea, String> autoBinding_11 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowSelectionModel, tvShowSelectionModelBeanProperty_11, taOtherIds, jTextAreaBeanProperty);
    autoBinding_11.setConverter(new TvShowOtherIdsConverter());
    autoBinding_11.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_12 = BeanProperty.create("selectedTvShow.status.name");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowSelectionModel,
        tvShowSelectionModelBeanProperty_12, lblStatus, jLabelBeanProperty);
    autoBinding_12.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_13 = BeanProperty.create("selectedTvShow.genresAsString");
    AutoBinding<TvShowSelectionModel, String, JTextArea, String> autoBinding_13 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowSelectionModel, tvShowSelectionModelBeanProperty_13, taGenres, jTextAreaBeanProperty);
    autoBinding_13.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_14 = BeanProperty.create("selectedTvShow.firstAiredAsString");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding_14 = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowSelectionModel,
        tvShowSelectionModelBeanProperty_14, lblPremiered, jLabelBeanProperty);
    autoBinding_14.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_15 = BeanProperty.create("selectedTvShow.productionCompany");
    AutoBinding<TvShowSelectionModel, String, JTextArea, String> autoBinding_15 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowSelectionModel, tvShowSelectionModelBeanProperty_15, taStudio, jTextAreaBeanProperty);
    autoBinding_15.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_16 = BeanProperty.create("selectedTvShow.country");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding_16 = Bindings.createAutoBinding(UpdateStrategy.READ, tvShowSelectionModel,
        tvShowSelectionModelBeanProperty_16, lblCountry, jLabelBeanProperty);
    autoBinding_16.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_17 = BeanProperty.create("selectedTvShow.tagsAsString");
    AutoBinding<TvShowSelectionModel, String, JTextArea, String> autoBinding_17 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowSelectionModel, tvShowSelectionModelBeanProperty_17, taTags, jTextAreaBeanProperty);
    autoBinding_17.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_18 = BeanProperty.create("selectedTvShow.path");
    AutoBinding<TvShowSelectionModel, String, LinkLabel, String> autoBinding_18 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowSelectionModel, tvShowSelectionModelBeanProperty_18, lblPath, linkLabelBeanProperty);
    autoBinding_18.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_19 = BeanProperty.create("selectedTvShow.note");
    AutoBinding<TvShowSelectionModel, String, JTextArea, String> autoBinding_19 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowSelectionModel, tvShowSelectionModelBeanProperty_19, taNote, jTextAreaBeanProperty);
    autoBinding_19.bind();
    //
    BeanProperty<TvShowSelectionModel, MediaCertification> tvShowSelectionModelBeanProperty_20 = BeanProperty.create("selectedTvShow.certification");
    BeanProperty<JLabel, Icon> jLabelBeanProperty_1 = BeanProperty.create("icon");
    AutoBinding<TvShowSelectionModel, MediaCertification, JLabel, Icon> autoBinding_20 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowSelectionModel, tvShowSelectionModelBeanProperty_20, lblCertificationLogo, jLabelBeanProperty_1);
    autoBinding_20.setConverter(new CertificationImageConverter());
    autoBinding_20.bind();
    //
    BeanProperty<TvShowSelectionModel, Integer> tvShowSelectionModelBeanProperty_21 = BeanProperty.create("selectedTvShow.tmdbId");
    AutoBinding<TvShowSelectionModel, Integer, LinkLabel, String> autoBinding_21 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowSelectionModel, tvShowSelectionModelBeanProperty_21, lblTmdbId, linkLabelBeanProperty);
    autoBinding_21.setConverter(new ZeroIdConverter());
    autoBinding_21.bind();
  }
}
