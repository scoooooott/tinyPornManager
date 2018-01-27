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
package org.tinymediamanager.ui.tvshows.panels.season;

import static org.tinymediamanager.core.Constants.ADDED_EPISODE;
import static org.tinymediamanager.core.Constants.MEDIA_FILES;
import static org.tinymediamanager.core.Constants.POSTER;
import static org.tinymediamanager.core.Constants.REMOVED_EPISODE;

import java.awt.Dimension;
import java.awt.Font;
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
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.tvshows.TvShowSeasonSelectionModel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowInformationPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowSeasonInformationPanel extends JPanel {
  private static final long                     serialVersionUID = 1911808562993073590L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle           BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private EventList<TvShowEpisode>              episodeEventList;
  private DefaultEventTableModel<TvShowEpisode> episodeTableModel;
  private TvShowSeasonSelectionModel            tvShowSeasonSelectionModel;
  private ImageLabel                            lblTvShowPoster;
  private JLabel                                lblPosterSize;
  private JLabel                                lblTvshowTitle;
  private JLabel                                lblSeason;
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
    episodeTableModel = new DefaultEventTableModel<>(GlazedListsSwing.swingThreadProxyList(episodeEventList), new EpisodeTableFormat());

    initComponents();
    initDataBindings();

    // manual coded binding
    PropertyChangeListener propertyChangeListener = propertyChangeEvent -> {
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
          tableEpisodes.adjustColumnPreferredWidths(6);
        }
        catch (Exception ignored) {
        }
        finally {
          episodeEventList.getReadWriteLock().writeLock().unlock();
        }
      }
      if ((source instanceof TvShowSeason && POSTER.equals(property))) {
        TvShowSeason season = (TvShowSeason) source;
        setPoster(season);
      }
    };
    tvShowSeasonSelectionModel.addPropertyChangeListener(propertyChangeListener);
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[100lp:100lp,grow][300lp:300lp,grow 250]", "[grow]"));

    {
      JPanel panelLeft = new JPanel();
      add(panelLeft, "cell 0 0,grow");
      panelLeft.setLayout(new ColumnLayout());

      lblTvShowPoster = new ImageLabel(false, false, true);
      lblTvShowPoster.setDesiredAspectRatio(2 / 3.0f);
      panelLeft.add(lblTvShowPoster);
      lblTvShowPoster.enableLightbox();
      lblPosterSize = new JLabel(BUNDLE.getString("mediafiletype.poster")); //$NON-NLS-1$
      panelLeft.add(lblPosterSize);
    }
    {
      JPanel panelRight = new JPanel();
      add(panelRight, "cell 1 0,grow");
      panelRight.setLayout(new MigLayout("", "[][323px,grow]", "[][][shrink 0][][286px,grow]"));
      {
        lblTvshowTitle = new TmmLabel("", 1.33);
        panelRight.add(lblTvshowTitle, "cell 0 0 2 1");
      }
      {
        JLabel lblSeasonT = new TmmLabel(BUNDLE.getString("metatag.season"));
        panelRight.add(lblSeasonT, "cell 0 1");
        TmmFontHelper.changeFont(lblSeasonT, 1.166, Font.BOLD);

        lblSeason = new JLabel("");
        panelRight.add(lblSeason, "cell 1 1");
        TmmFontHelper.changeFont(lblSeason, 1.166, Font.BOLD);
      }
      {
        panelRight.add(new JSeparator(), "cell 0 2 2 1,growx");
      }
      {
        JLabel lblEpisodelistT = new TmmLabel(BUNDLE.getString("metatag.episodes"));
        panelRight.add(lblEpisodelistT, "cell 0 3 2 1");
        tableEpisodes = new TmmTable(episodeTableModel);
        JScrollPane scrollPaneEpisodes = new JScrollPane(tableEpisodes);
        panelRight.add(scrollPaneEpisodes, "cell 0 4 2 1,grow");
        tableEpisodes.configureScrollPane(scrollPaneEpisodes);
        scrollPaneEpisodes.setViewportView(tableEpisodes);
      }
    }
  }

  private void setPoster(TvShowSeason season) {
    // only reset if there was a real change
    if (season.getPoster().equals(lblTvShowPoster.getImagePath())) {
      return;
    }

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
