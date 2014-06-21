/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowDetailsPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowDetailsPanel extends JPanel {
  private static final long           serialVersionUID = -1569492065407109019L;
  private static final Logger         LOGGER           = LoggerFactory.getLogger(TvShowDetailsPanel.class);
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

  /**
   * Instantiates a new tv show details panel.
   * 
   * @param selectionModel
   *          the selection model
   */
  public TvShowDetailsPanel(TvShowSelectionModel selectionModel) {
    this.selectionModel = selectionModel;
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, ColumnSpec.decode("25px"), ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, ColumnSpec.decode("25px"), ColumnSpec.decode("default:grow(2)"), },
        new RowSpec[] { FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblPremieredT = new JLabel(BUNDLE.getString("metatag.premiered")); //$NON-NLS-1$
    setBoldLabel(lblPremieredT);
    add(lblPremieredT, "1, 2");

    lblPremiered = new JLabel("");
    add(lblPremiered, "3, 2");

    JLabel lblYearT = new JLabel(BUNDLE.getString("metatag.year")); //$NON-NLS-1$
    setBoldLabel(lblYearT);
    add(lblYearT, "5, 2");

    lblYear = new JLabel("");
    add(lblYear, "7, 2");

    JLabel lblStatusT = new JLabel(BUNDLE.getString("metatag.status")); //$NON-NLS-1$
    setBoldLabel(lblStatusT);
    add(lblStatusT, "1, 4");

    lblStatus = new JLabel("");
    add(lblStatus, "3, 4");

    JLabel lblImdbIdT = new JLabel("IMDB Id");
    setBoldLabel(lblImdbIdT);
    add(lblImdbIdT, "5, 4");

    lblImdbId = new LinkLabel("");
    lblImdbId.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String url = "http://www.imdb.com/title/" + lblImdbId.getNormalText();
        try {
          TmmUIHelper.browseUrl(url);
        }
        catch (Exception e) {
          LOGGER.error("browse to imdbid", e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":",
              e.getLocalizedMessage() }));
        }
      }
    });
    add(lblImdbId, "7, 4");
    lblImdbIdT.setLabelFor(lblImdbId);

    JLabel lblStudioT = new JLabel(BUNDLE.getString("metatag.studio")); //$NON-NLS-1$
    setBoldLabel(lblStudioT);
    add(lblStudioT, "1, 6");

    lblStudio = new JLabel("");
    add(lblStudio, "3, 6");

    JLabel lblThetvdbIdT = new JLabel("TheTVDB Id");
    setBoldLabel(lblThetvdbIdT);
    add(lblThetvdbIdT, "5, 6");

    lblThetvdbId = new LinkLabel("");
    lblThetvdbId.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String url = "http://thetvdb.com/?tab=series&id=" + lblThetvdbId.getNormalText();
        try {
          TmmUIHelper.browseUrl(url);
        }
        catch (Exception e) {
          LOGGER.error("browse to thetvdb", e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":",
              e.getLocalizedMessage() }));
        }
      }
    });
    add(lblThetvdbId, "7, 6");
    lblThetvdbIdT.setLabelFor(lblThetvdbId);

    JLabel lblCertificationT = new JLabel(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
    setBoldLabel(lblCertificationT);
    add(lblCertificationT, "1, 8");
    lblCertificationT.setLabelFor(lblCertification);

    lblCertification = new JLabel("");
    add(lblCertification, "3, 8, 5, 1");

    JLabel lblGenresT = new JLabel(BUNDLE.getString("metatag.genre")); //$NON-NLS-1$
    setBoldLabel(lblGenresT);
    add(lblGenresT, "1, 10");
    lblGenresT.setLabelFor(lblGenres);

    lblGenres = new JLabel("");
    add(lblGenres, "3, 10, 5, 1");

    JLabel lblTagsT = new JLabel(BUNDLE.getString("metatag.tags")); //$NON-NLS-1$
    setBoldLabel(lblTagsT);
    add(lblTagsT, "1, 12");
    lblGenresT.setLabelFor(lblTags);

    lblTags = new JLabel("");
    add(lblTags, "3, 12, 5, 1");

    JLabel lblPathT = new JLabel(BUNDLE.getString("metatag.path")); //$NON-NLS-1$
    setBoldLabel(lblPathT);
    add(lblPathT, "1, 14");

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
    add(lblPath, "3, 14, 5, 1");
    initDataBindings();
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
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_6 = BeanProperty.create("selectedTvShow.studio");
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
  }
}
