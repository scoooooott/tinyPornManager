/*
 * Copyright 2012 - 2015 Manuel Laggner
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
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.converter.ZeroIdConverter;
import org.tinymediamanager.ui.tvshows.TvShowSelectionModel;

import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowDetailsPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowDetailsPanel extends JPanel {
  private static final long           serialVersionUID = -1569492065407109019L;
  private static final Logger         LOGGER           = LoggerFactory.getLogger(TvShowDetailsPanel.class);
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private final TvShowSelectionModel  selectionModel;

  /** UI components */
  private JLabel                      lblGenres;
  private JLabel                      lblCertification;
  private LinkLabel                   lblThetvdbId;
  private LinkLabel                   lblImdbId;
  private LinkLabel                   lblPath;
  private JLabel                      lblPremiered;
  private JLabel                      lblStudio;
  private JLabel                      lblStatus;
  private JLabel                      lblYear;
  private JLabel                      lblTags;
  private LinkLabel                   lblTraktTvId;

  /**
   * Instantiates a new tv show details panel.
   * 
   * @param selectionModel
   *          the selection model
   */
  public TvShowDetailsPanel(TvShowSelectionModel selectionModel) {
    this.selectionModel = selectionModel;

    initComponents();
    initDataBindings();

    lblImdbId.addActionListener(arg0 -> {
      String url = "http://www.imdb.com/title/" + lblImdbId.getText();
      try {
        TmmUIHelper.browseUrl(url);
      }
      catch (Exception e) {
        LOGGER.error("browse to imdbid", e);
        MessageManager.instance
            .pushMessage(new Message(MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":", e.getLocalizedMessage() }));
      }
    });

    lblThetvdbId.addActionListener(arg0 -> {
      String url = "http://thetvdb.com/?tab=series&id=" + lblThetvdbId.getText();
      try {
        TmmUIHelper.browseUrl(url);
      }
      catch (Exception e) {
        LOGGER.error("browse to thetvdb", e);
        MessageManager.instance
            .pushMessage(new Message(MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":", e.getLocalizedMessage() }));
      }
    });

    lblTraktTvId.addActionListener(arg0 -> {
      String url = "https://trakt.tv/shows/" + lblTraktTvId.getText();
      try {
        TmmUIHelper.browseUrl(url);
      }
      catch (Exception e) {
        LOGGER.error("browse to traktid", e);
        MessageManager.instance
            .pushMessage(new Message(Message.MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":", e.getLocalizedMessage() }));
      }
    });
    lblPath.addActionListener(arg0 -> {
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
              .pushMessage(new Message(MessageLevel.ERROR, path, "message.erroropenfolder", new String[] { ":", ex.getLocalizedMessage() }));
        }
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("insets 0", "[][grow][][grow 200]", "[]2lp[]2lp[]2lp[]2lp[]2lp[]2lp[]"));
    {
      JLabel lblPremieredT = new JLabel(BUNDLE.getString("metatag.premiered")); //$NON-NLS-1$
      setBoldLabel(lblPremieredT);
      add(lblPremieredT, "cell 0 0");

      lblPremiered = new JLabel("");
      add(lblPremiered, "cell 1 0");
    }
    {
      JLabel lblYearT = new JLabel(BUNDLE.getString("metatag.year")); //$NON-NLS-1$
      setBoldLabel(lblYearT);
      add(lblYearT, "cell 2 0");

      lblYear = new JLabel("");
      add(lblYear, "cell 3 0");
    }
    {
      JLabel lblStatusT = new JLabel(BUNDLE.getString("metatag.status")); //$NON-NLS-1$
      setBoldLabel(lblStatusT);
      add(lblStatusT, "cell 0 1");

      lblStatus = new JLabel("");
      add(lblStatus, "cell 1 1");
    }
    {
      JLabel lblImdbIdT = new JLabel("IMDB Id");
      setBoldLabel(lblImdbIdT);
      add(lblImdbIdT, "cell 2 1");

      lblImdbId = new LinkLabel("");
      add(lblImdbId, "cell 3 1");
    }
    {
      JLabel lblStudioT = new JLabel(BUNDLE.getString("metatag.studio")); //$NON-NLS-1$
      setBoldLabel(lblStudioT);
      add(lblStudioT, "cell 0 2");

      lblStudio = new JLabel("");
      add(lblStudio, "cell 1 2");
    }
    {
      JLabel lblThetvdbIdT = new JLabel("TheTVDB Id");
      setBoldLabel(lblThetvdbIdT);
      add(lblThetvdbIdT, "cell 2 2");

      lblThetvdbId = new LinkLabel("");
      add(lblThetvdbId, "cell 3 2");
    }
    {
      JLabel lblCertificationT = new JLabel(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
      setBoldLabel(lblCertificationT);
      add(lblCertificationT, "cell 0 3");

      lblCertification = new JLabel("");
      add(lblCertification, "cell 1 3");
    }
    {
      JLabel lblTrakttvIdT = new JLabel(BUNDLE.getString("metatag.trakt")); //$NON-NLS-1$
      setBoldLabel(lblTrakttvIdT);
      add(lblTrakttvIdT, "cell 2 3");

      lblTraktTvId = new LinkLabel("");
      add(lblTraktTvId, "cell 3 3");
    }
    {
      JLabel lblGenresT = new JLabel(BUNDLE.getString("metatag.genre")); //$NON-NLS-1$
      setBoldLabel(lblGenresT);
      add(lblGenresT, "cell 0 4");

      lblGenres = new JLabel("");
      add(lblGenres, "cell 1 4 3 1");
    }
    {
      JLabel lblTagsT = new JLabel(BUNDLE.getString("metatag.tags")); //$NON-NLS-1$
      setBoldLabel(lblTagsT);
      add(lblTagsT, "cell 0 5");

      lblTags = new JLabel("");
      add(lblTags, "cell 1 5 3 1");
    }
    {
      JLabel lblPathT = new JLabel(BUNDLE.getString("metatag.path")); //$NON-NLS-1$
      setBoldLabel(lblPathT);
      add(lblPathT, "cell 0 6");

      lblPath = new LinkLabel("");
      add(lblPath, "cell 1 6 3 1,growx");
    }
  }

  private void setBoldLabel(JLabel label) {
    label.setFont(label.getFont().deriveFont(Font.BOLD));
  }

  protected void initDataBindings() {
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_2 = BeanProperty.create("selectedTvShow.tvdbId");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty_2, lblThetvdbId, jLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_3 = BeanProperty.create("selectedTvShow.imdbId");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty_3, lblImdbId, jLabelBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_4 = BeanProperty.create("selectedTvShow.path");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty_4, lblPath, jLabelBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty = BeanProperty.create("selectedTvShow.certification.name");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty, lblCertification, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_1 = BeanProperty.create("selectedTvShow.genresAsString");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty_1, lblGenres, jLabelBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_6 = BeanProperty.create("selectedTvShow.productionCompany");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty_6, lblStudio, jLabelBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_7 = BeanProperty.create("selectedTvShow.firstAiredAsString");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty_7, lblPremiered, jLabelBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_5 = BeanProperty.create("selectedTvShow.status");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty_5, lblStatus, jLabelBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_8 = BeanProperty.create("selectedTvShow.year");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty_8, lblYear, jLabelBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_9 = BeanProperty.create("selectedTvShow.tagAsString");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty_9, lblTags, jLabelBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<TvShowSelectionModel, Integer> tvShowSelectionModelBeanProperty_10 = BeanProperty.create("selectedTvShow.traktId");
    BeanProperty<LinkLabel, String> linkLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowSelectionModel, Integer, LinkLabel, String> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty_10, lblTraktTvId, linkLabelBeanProperty);
    autoBinding_10.setConverter(new ZeroIdConverter());
    autoBinding_10.bind();
  }
}
