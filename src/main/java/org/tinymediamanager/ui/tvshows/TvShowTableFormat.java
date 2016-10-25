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

import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.table.TmmTableFormat;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;

/**
 * The class TvShowTableFormat is used to define the columns for the TV show tree table
 *
 * @author Manuel Laggner
 */
public class TvShowTableFormat extends TmmTableFormat<TmmTreeNode> {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

  public TvShowTableFormat() {

    /*
     * season count
     */
    Column col = new Column(BUNDLE.getString("metatag.seasons"), "seasons", node -> getSeasons(node), String.class);
    col.setHeaderIcon(IconManager.SEASONS);
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * episode count
     */
    col = new Column(BUNDLE.getString("metatag.episodes"), "episodes", node -> getEpisodes(node), String.class);
    col.setHeaderIcon(IconManager.EPISODES);
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * rating
     */
    col = new Column(BUNDLE.getString("metatag.rating"), "rating", node -> getRating(node), String.class);
    col.setHeaderIcon(IconManager.RATING);
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * video format
     */
    col = new Column(BUNDLE.getString("metatag.format"), "format", node -> getFormat(node), String.class);
    col.setHeaderIcon(IconManager.VIDEO_FORMAT);
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * NFO
     */
    col = new Column(BUNDLE.getString("tmm.nfo"), "nfo", node -> hasNfo(node), ImageIcon.class);
    col.setHeaderIcon(IconManager.NFO);
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * images
     */
    col = new Column(BUNDLE.getString("tmm.images"), "images", node -> hasImages(node), ImageIcon.class);
    col.setHeaderIcon(IconManager.IMAGES);
    col.setColumnResizeable(false);
    addColumn(col);
  }

  private String getSeasons(TmmTreeNode node) {
    Object userObject = node.getUserObject();
    if (userObject instanceof TvShow) {
      return String.valueOf(((TvShow) userObject).getSeasonCount());
    }
    return "";
  }

  private String getEpisodes(TmmTreeNode node) {
    Object userObject = node.getUserObject();
    if (userObject instanceof TvShow) {
      return String.valueOf(((TvShow) userObject).getEpisodeCount());
    }
    if (userObject instanceof TvShowSeason) {
      return String.valueOf(((TvShowSeason) userObject).getEpisodes().size());
    }
    return "";
  }

  private String getRating(TmmTreeNode node) {
    Object userObject = node.getUserObject();
    if (userObject instanceof TvShow || userObject instanceof TvShowEpisode) {
      return String.valueOf(((MediaEntity) userObject).getRating());
    }
    return "";
  }

  private String getFormat(TmmTreeNode node) {
    Object userObject = node.getUserObject();
    if (userObject instanceof TvShowEpisode) {
      return ((TvShowEpisode) userObject).getMediaInfoVideoFormat();
    }
    return "";
  }

  private ImageIcon hasNfo(TmmTreeNode node) {
    Object userObject = node.getUserObject();
    if (userObject instanceof TvShowEpisode) {
      return getCheckIcon(((TvShowEpisode) userObject).getHasNfoFile());
    }
    return null;
  }

  private ImageIcon hasImages(TmmTreeNode node) {
    Object userObject = node.getUserObject();
    if (userObject instanceof TvShow) {
      return getCheckIcon(((TvShow) userObject).getHasNfoFile());
    }
    if (userObject instanceof TvShowEpisode) {
      return getCheckIcon(((TvShowEpisode) userObject).getHasNfoFile());
    }
    return null;
  }
}
