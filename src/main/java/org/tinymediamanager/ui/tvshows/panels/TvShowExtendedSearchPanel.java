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
package org.tinymediamanager.ui.tvshows.panels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;
import org.tinymediamanager.ui.components.treetable.TmmTreeTable;
import org.tinymediamanager.ui.panels.RoundedPanel;
import org.tinymediamanager.ui.tvshows.ITvShowUIFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowAudioCodecFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowCastFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowDatasourceFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowEmptyFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowGenreFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowMediaSourceFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowMissingArtworkFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowMissingMetadataFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowMissingSubtitlesFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowNewEpisodesFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowTagFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowVideoCodecFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowVideoFormatFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowWatchedFilter;

import net.miginfocom.swing.MigLayout;

/**
 * @author Manuel Laggner
 * 
 */
public class TvShowExtendedSearchPanel extends RoundedPanel {
  private static final long           serialVersionUID = 5003714573168481816L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private TmmTreeTable                treeTable;
  private JPanel                      panelFilter;

  public TvShowExtendedSearchPanel(TmmTreeTable treeTable) {
    super();
    setOpaque(false);
    arcs = new Dimension(10, 10);

    this.treeTable = treeTable;

    // add a dummy mouse listener to prevent clicking through
    addMouseListener(new MouseAdapter() {
    });
    setLayout(new MigLayout("", "[]", "[][][20lp]"));

    JLabel lblFilterBy = new JLabel(BUNDLE.getString("movieextendedsearch.filterby")); //$NON-NLS-1$
    add(lblFilterBy, "cell 0 0,growx,aligny top");

    createFilterPanel();
    add(panelFilter, "cell 0 1,grow");
  }

  private void createFilterPanel() {
    panelFilter = new JPanel();
    GridBagLayout gbl_panelFilter = new GridBagLayout();
    gbl_panelFilter.columnWidths = new int[] { 0 };
    gbl_panelFilter.rowHeights = new int[] { 0 };
    gbl_panelFilter.columnWeights = new double[] { Double.MIN_VALUE };
    gbl_panelFilter.rowWeights = new double[] { Double.MIN_VALUE };
    panelFilter.setLayout(gbl_panelFilter);

    addFilter(new TvShowNewEpisodesFilter());
    addFilter(new TvShowWatchedFilter());
    addFilter(new TvShowGenreFilter());
    addFilter(new TvShowCastFilter());
    addFilter(new TvShowTagFilter());
    addFilter(new TvShowVideoFormatFilter());
    addFilter(new TvShowVideoCodecFilter());
    addFilter(new TvShowAudioCodecFilter());
    addFilter(new TvShowMediaSourceFilter());
    addFilter(new TvShowDatasourceFilter());
    addFilter(new TvShowMissingMetadataFilter());
    addFilter(new TvShowMissingArtworkFilter());
    addFilter(new TvShowMissingSubtitlesFilter());
    addFilter(new TvShowEmptyFilter());
  }

  /**
   * add a new filter to the panel
   * 
   * @param filter
   *          the filter to be added
   */
  private void addFilter(ITvShowUIFilter<TmmTreeNode> filter) {
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.gridx = 0;
    gbc.ipadx = 2;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.LINE_START;
    panelFilter.add(filter.getCheckBox(), gbc);

    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.LINE_END;
    panelFilter.add(filter.getLabel(), gbc);

    gbc.gridx = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.LINE_START;

    if (filter.getFilterComponent() != null) {
      panelFilter.add(filter.getFilterComponent(), gbc);
    }
    else {
      panelFilter.add(Box.createGlue(), gbc);
    }

    treeTable.addFilter(filter);
  }
}
