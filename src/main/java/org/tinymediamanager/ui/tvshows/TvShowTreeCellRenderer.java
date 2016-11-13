/*
 * Copyright 2012 - 2016 Manuel Laggner
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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.UIManager;

import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.components.treetable.TmmTreeTableCellRenderer;

/**
 * The class TvShowTreeCellRenderer. Just for modifying the color of dummy seasons/episodes
 * 
 * @author Manuel Laggner
 */
public class TvShowTreeCellRenderer extends TmmTreeTableCellRenderer {
  private Color colorDummy;

  public TvShowTreeCellRenderer() {
    this.colorDummy = UIManager.getColor("Label.foreground");
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    if (value instanceof TvShowTreeDataProvider.TvShowSeasonTreeNode) {
      TvShowSeason season = (TvShowSeason) ((TvShowTreeDataProvider.TvShowSeasonTreeNode) value).getUserObject();
      if (season.isDummy()) {
        renderer.setForeground(colorDummy);
      }
    }
    else if (value instanceof TvShowTreeDataProvider.TvShowEpisodeTreeNode) {
      TvShowEpisode episode = (TvShowEpisode) ((TvShowTreeDataProvider.TvShowEpisodeTreeNode) value).getUserObject();
      if (episode.isDummy()) {
        renderer.setForeground(colorDummy);
      }
    }

    return renderer;
  }
}
