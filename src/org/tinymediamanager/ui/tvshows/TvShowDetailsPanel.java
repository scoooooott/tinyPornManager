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

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = -1569492065407109019L;

  /** The Constant LOGGER. */
  private final static Logger         LOGGER           = LoggerFactory.getLogger(TvShowDetailsPanel.class);

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The selection model. */
  private final TvShowSelectionModel  selectionModel;

  /** The lbl genres. */
  private JLabel                      lblGenres;

  /** The lbl certification. */
  private JLabel                      lblCertification;

  /** The lbl thetvdb id. */
  private LinkLabel                   lblThetvdbId;

  /** The lbl imdb id. */
  private LinkLabel                   lblImdbId;

  /** The lbl path. */
  private LinkLabel                   lblPath;

  /** The lbl premiered. */
  private JLabel                      lblPremiered;

  /** The lbl studio. */
  private JLabel                      lblStudio;

  /** The lbl status. */
  private JLabel                      lblStatus;

  /** The lbl year t. */
  private JLabel                      lblYearT;

  /** The lbl year. */
  private JLabel                      lblYear;

  /**
   * Instantiates a new tv show details panel.
   * 
   * @param selectionModel
   *          the selection model
   */
  public TvShowDetailsPanel(TvShowSelectionModel selectionModel) {
    this.selectionModel = selectionModel;
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("25px"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow(3)"), },
        new RowSpec[] { FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblGenresT = new JLabel(BUNDLE.getString("metatag.genre")); //$NON-NLS-1$
    add(lblGenresT, "2, 2");

    lblGenres = new JLabel("");
    lblGenresT.setLabelFor(lblGenres);
    add(lblGenres, "6, 2, 5, 1");

    JLabel lblCertificationT = new JLabel(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
    add(lblCertificationT, "2, 4");

    lblCertification = new JLabel("");
    lblCertificationT.setLabelFor(lblCertification);
    add(lblCertification, "6, 4");

    JLabel lblStudioT = new JLabel(BUNDLE.getString("metatag.studio")); //$NON-NLS-1$
    add(lblStudioT, "2, 6");

    lblStudio = new JLabel("");
    add(lblStudio, "6, 6, 5, 1");

    JLabel lblPremieredT = new JLabel(BUNDLE.getString("metatag.premiered")); //$NON-NLS-1$
    add(lblPremieredT, "2, 8");

    lblPremiered = new JLabel("");
    add(lblPremiered, "6, 8");

    lblYearT = new JLabel(BUNDLE.getString("metatag.year")); //$NON-NLS-1$
    add(lblYearT, "8, 8");

    lblYear = new JLabel("");
    add(lblYear, "10, 8");

    JLabel lblStatusT = new JLabel(BUNDLE.getString("metatag.status")); //$NON-NLS-1$
    add(lblStatusT, "2, 10");

    lblStatus = new JLabel("");
    add(lblStatus, "6, 10, 5, 1");

    JLabel lblThetvdbIdT = new JLabel("TheTVDB Id");
    add(lblThetvdbIdT, "2, 12");

    lblThetvdbId = new LinkLabel("");
    lblThetvdbIdT.setLabelFor(lblThetvdbId);
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
    add(lblThetvdbId, "6, 12");

    JLabel lblImdbIdT = new JLabel("IMDB Id");
    add(lblImdbIdT, "8, 12");

    lblImdbId = new LinkLabel("");
    lblImdbIdT.setLabelFor(lblImdbId);
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
    add(lblImdbId, "10, 12");

    JLabel lblPathT = new JLabel(BUNDLE.getString("metatag.path")); //$NON-NLS-1$
    add(lblPathT, "2, 14");

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
    add(lblPath, "6, 14, 5, 1");
    initDataBindings();
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
  }
}
