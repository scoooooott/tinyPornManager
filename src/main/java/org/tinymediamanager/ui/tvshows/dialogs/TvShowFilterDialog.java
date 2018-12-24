/*
 * Copyright 2012 - 2018 Manuel Laggner
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

package org.tinymediamanager.ui.tvshows.dialogs;

import java.awt.BorderLayout;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.tinymediamanager.Globals;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;
import org.tinymediamanager.ui.components.treetable.TmmTreeTable;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.tvshows.ITvShowUIFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowAudioCodecFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowCastFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowDatasourceFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowEmptyFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowFrameRateFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowGenreFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowMediaSourceFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowMissingArtworkFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowMissingEpisodesFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowMissingMetadataFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowMissingSubtitlesFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowNewEpisodesFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowStudioFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowTagFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowVideoCodecFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowVideoFormatFilter;
import org.tinymediamanager.ui.tvshows.filters.TvShowWatchedFilter;

import net.miginfocom.swing.MigLayout;

public class TvShowFilterDialog extends TmmDialog {
  private static final long             serialVersionUID = 2298540526328945319L;
  /** @wbp.nls.resourceBundle messages */
  protected static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private static final float            FONT_SIZE        = Math.round(Globals.settings.getFontSize() * 0.916);

  private TmmTreeTable                  treeTable;
  private final JPanel                  panelFilter;

  public TvShowFilterDialog(TmmTreeTable treeTable) {
    super(BUNDLE.getString("movieextendedsearch.options"), "tvShowFilter");
    setModalityType(ModalityType.MODELESS);

    this.treeTable = treeTable;

    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new MigLayout("", "[][10lp][100lp, grow]", "[][200lp:n,grow][][][]"));

      JLabel lblFilterBy = new TmmLabel(BUNDLE.getString("movieextendedsearch.filterby")); //$NON-NLS-1$
      setComponentFont(lblFilterBy);
      panelContent.add(lblFilterBy, "cell 0 0,growx,aligny top");

      panelFilter = new JPanel(new MigLayout("", "[][][100lp:n,grow]", "[]"));

      JScrollPane scrollPane = new JScrollPane(panelFilter);
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      panelContent.add(scrollPane, "cell 0 1 3 1,grow");

      addFilter(new TvShowNewEpisodesFilter());
      addFilter(new TvShowWatchedFilter());
      addFilter(new TvShowGenreFilter());
      addFilter(new TvShowStudioFilter());
      addFilter(new TvShowCastFilter());
      addFilter(new TvShowTagFilter());
      addFilter(new TvShowVideoFormatFilter());
      addFilter(new TvShowVideoCodecFilter());
      addFilter(new TvShowFrameRateFilter());
      addFilter(new TvShowAudioCodecFilter());
      addFilter(new TvShowMediaSourceFilter());
      addFilter(new TvShowDatasourceFilter());
      addFilter(new TvShowMissingMetadataFilter());
      addFilter(new TvShowMissingArtworkFilter());
      addFilter(new TvShowMissingSubtitlesFilter());
      addFilter(new TvShowMissingEpisodesFilter());
      addFilter(new TvShowEmptyFilter());
    }
  }

  /**
   * add a new filter to the panel and selection model
   *
   * @param filter
   *          the filter to be added
   */
  private void addFilter(ITvShowUIFilter<TmmTreeNode> filter) {
    panelFilter.add(filter.getCheckBox(), "");
    panelFilter.add(filter.getLabel(), "right");

    if (filter.getFilterComponent() != null) {
      panelFilter.add(filter.getFilterComponent(), "wmin 100, grow, wrap");
    }
    else {
      panelFilter.add(Box.createGlue(), "wrap");
    }

    treeTable.addFilter(filter);
  }

  private void setComponentFont(JComponent comp) {
    comp.setFont(comp.getFont().deriveFont(FONT_SIZE));
  }

  @Override
  protected void initBottomPanel() {
    // no bottom line needed
  }

  @Override
  public void dispose() {
    // do not dispose (singleton), but save the size/position
    TmmWindowSaver.getInstance().saveSettings(this);
  }

}
