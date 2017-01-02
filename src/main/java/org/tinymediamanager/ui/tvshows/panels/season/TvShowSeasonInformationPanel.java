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
package org.tinymediamanager.ui.tvshows.panels.season;

import static org.tinymediamanager.core.Constants.ADDED_EPISODE;
import static org.tinymediamanager.core.Constants.MEDIA_FILES;
import static org.tinymediamanager.core.Constants.POSTER;
import static org.tinymediamanager.core.Constants.REMOVED_EPISODE;

import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.ColumnLayout;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.tvshows.TvShowSeasonSelectionModel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;

/**
 * The Class TvShowInformationPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowSeasonInformationPanel extends JPanel {
  private static final long                     serialVersionUID  = 1911808562993073590L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle           BUNDLE            = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private EventList<TvShowEpisode>              episodeEventList;
  private DefaultEventTableModel<TvShowEpisode> episodeTableModel = null;
  private TvShowSeasonSelectionModel            tvShowSeasonSelectionModel;

  /** UI components */
  private JPanel                                panelTop;
  private ImageLabel                            lblTvShowPoster;
  private JLabel                                lblPosterSize;
  private JPanel                                panelRight;
  private JPanel                                panelLeft;
  private JLabel                                lblTvshowTitle;
  private JLabel                                lblSeasonT;
  private JLabel                                lblSeason;
  private JSeparator                            separator;
  private JLabel                                lblEpisodelistT;
  private JScrollPane                           scrollPaneEpisodes;
  private TmmTable                              tableEpisodes;

  /**
   * Instantiates a new tv show information panel.
   * 
   * @param tvShowSeasonSelectionModel
   *          the tv show selection model
   */
  public TvShowSeasonInformationPanel(TvShowSeasonSelectionModel tvShowSeasonSelectionModel) {
    this.tvShowSeasonSelectionModel = tvShowSeasonSelectionModel;
    episodeEventList = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()),
        GlazedLists.beanConnector(TvShowEpisode.class));

    setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("70dlu:grow"), FormSpecs.UNRELATED_GAP_COLSPEC,
            ColumnSpec.decode("200dlu:grow(2)"), FormSpecs.UNRELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.PARAGRAPH_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), FormSpecs.PARAGRAPH_GAP_ROWSPEC, }));

    panelLeft = new JPanel();
    add(panelLeft, "2, 2, fill, fill");
    panelLeft.setLayout(new ColumnLayout());

    lblTvShowPoster = new ImageLabel(false, false, true);
    lblTvShowPoster.setDesiredAspectRatio(2 / 3.0f);
    panelLeft.add(lblTvShowPoster);
    lblTvShowPoster.enableLightbox();
    lblPosterSize = new JLabel(BUNDLE.getString("mediafiletype.poster")); //$NON-NLS-1$
    panelLeft.add(lblPosterSize);

    panelRight = new JPanel();
    add(panelRight, "4, 2, fill, fill");
    panelRight
        .setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("200px:grow"), }, new RowSpec[] { RowSpec.decode("fill:default:grow"), }));

    panelTop = new JPanel();
    panelTop.setBorder(null);
    panelRight.add(panelTop, "1, 1, fill, fill");
    panelTop.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { RowSpec.decode("fill:default"), FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.PARAGRAPH_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, RowSpec.decode("top:default:grow"), }));

    lblTvshowTitle = new JLabel("");
    TmmFontHelper.changeFont(lblTvshowTitle, 1.33, Font.BOLD);
    panelTop.add(lblTvshowTitle, "2, 1, 3, 1");

    lblSeasonT = new JLabel(BUNDLE.getString("metatag.season")); //$NON-NLS-1$
    TmmFontHelper.changeFont(lblSeasonT, 1.166, Font.BOLD);
    panelTop.add(lblSeasonT, "2, 3");

    lblSeason = new JLabel("");
    TmmFontHelper.changeFont(lblSeason, 1.166, Font.BOLD);
    panelTop.add(lblSeason, "4, 3");

    separator = new JSeparator();
    panelTop.add(separator, "2, 5, 3, 1");

    lblEpisodelistT = new JLabel(BUNDLE.getString("metatag.episodes")); //$NON-NLS-1$
    panelTop.add(lblEpisodelistT, "2, 7, 3, 1");

    episodeTableModel = new DefaultEventTableModel<>(GlazedListsSwing.swingThreadProxyList(episodeEventList), new EpisodeTableFormat());
    tableEpisodes = new TmmTable(episodeTableModel);
    JScrollPane scrollPaneEpisodes = new JScrollPane(tableEpisodes);
    tableEpisodes.configureScrollPane(scrollPaneEpisodes);
    panelTop.add(scrollPaneEpisodes, "2, 9, 3, 1, fill, fill");
    scrollPaneEpisodes.setViewportView(tableEpisodes);

    // manual coded binding
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String property = propertyChangeEvent.getPropertyName();
        Object source = propertyChangeEvent.getSource();
        // react on selection of a movie and change of a tv show
        if (source instanceof TvShowSeasonSelectionModel
            || (source instanceof TvShowSeason && (MEDIA_FILES.equals(propertyChangeEvent.getPropertyName())
                || ADDED_EPISODE.equals(propertyChangeEvent.getPropertyName()) || REMOVED_EPISODE.equals(propertyChangeEvent.getPropertyName())))) {
          TvShowSeason selectedSeason;
          if (source instanceof TvShowSeasonSelectionModel) {
            TvShowSeasonSelectionModel model = (TvShowSeasonSelectionModel) source;
            selectedSeason = model.getSelectedTvShowSeason();
          }
          else {
            selectedSeason = (TvShowSeason) source;
          }
          setPoster(selectedSeason);

          try {
            episodeEventList.getReadWriteLock().writeLock().lock();
            episodeEventList.clear();
            episodeEventList.addAll(selectedSeason.getEpisodes());
          }
          catch (Exception e) {
          }
          finally {
            episodeEventList.getReadWriteLock().writeLock().unlock();
          }
        }
        if ((source.getClass() == TvShowSeason.class && POSTER.equals(property))) {
          TvShowSeason season = (TvShowSeason) source;
          setPoster(season);
        }
      }
    };

    initDataBindings();

    tvShowSeasonSelectionModel.addPropertyChangeListener(propertyChangeListener);
  }

  private void setPoster(TvShowSeason season) {
    lblTvShowPoster.clearImage();
    lblTvShowPoster.setImagePath(season.getPoster());
    Dimension posterSize = season.getPosterSize();
    if (posterSize.width > 0 && posterSize.height > 0) {
      lblPosterSize.setText(BUNDLE.getString("mediafiletype.poster") + " - " + posterSize.width + "x" + posterSize.height); //$NON-NLS-1$
    }
    else {
      lblPosterSize.setText(BUNDLE.getString("mediafiletype.poster")); //$NON-NLS-1$
    }
  }

  protected void initDataBindings() {
    BeanProperty<TvShowSeasonSelectionModel, String> tvShowSeasonSelectionModelBeanProperty = BeanProperty
        .create("selectedTvShowSeason.tvShow.title");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowSeasonSelectionModel, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowSeasonSelectionModel, tvShowSeasonSelectionModelBeanProperty, lblTvshowTitle, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<TvShowSeasonSelectionModel, Integer> tvShowSeasonSelectionModelBeanProperty_1 = BeanProperty.create("selectedTvShowSeason.season");
    AutoBinding<TvShowSeasonSelectionModel, Integer, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ,
        tvShowSeasonSelectionModel, tvShowSeasonSelectionModelBeanProperty_1, lblSeason, jLabelBeanProperty);
    autoBinding_1.bind();

  }

  private static class EpisodeTableFormat implements AdvancedTableFormat<TvShowEpisode> {
    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return BUNDLE.getString("metatag.episode"); //$NON-NLS-1$

        case 1:
          return BUNDLE.getString("metatag.title"); //$NON-NLS-1$

        case 2:
          return BUNDLE.getString("metatag.aired"); //$NON-NLS-1$

      }
      throw new IllegalStateException();
    }

    @Override
    public Object getColumnValue(TvShowEpisode episode, int column) {
      switch (column) {
        case 0:
          return episode.getEpisode();

        case 1:
          return episode.getTitle();

        case 2:
          return episode.getFirstAiredAsString();
      }

      throw new IllegalStateException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnClass(int)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Class getColumnClass(int column) {
      switch (column) {
        case 0:
        case 1:
        case 2:
          return String.class;
      }

      throw new IllegalStateException();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Comparator getColumnComparator(int arg0) {
      return null;
    }
  }
}
