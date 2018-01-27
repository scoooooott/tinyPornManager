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
package org.tinymediamanager.ui.tvshows;

import java.awt.FontMetrics;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.Rating;
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
    FontMetrics fontMetrics = getFontMetrics();

    /*
     * season count
     */
    Column col = new Column(BUNDLE.getString("metatag.seasons"), "seasons", node -> getSeasons(node), String.class);
    col.setHeaderIcon(IconManager.SEASONS);
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("99") * 1.2f));
    addColumn(col);

    /*
     * episode count
     */
    col = new Column(BUNDLE.getString("metatag.episodes"), "episodes", node -> getEpisodes(node), String.class);
    col.setHeaderIcon(IconManager.EPISODES);
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("99") * 1.2f));
    addColumn(col);

    /*
     * rating
     */
    col = new Column(BUNDLE.getString("metatag.rating"), "rating", node -> getRating(node), String.class);
    col.setHeaderIcon(IconManager.RATING);
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("99.9") * 1.2f));
    addColumn(col);

    /*
     * video format
     */
    col = new Column(BUNDLE.getString("metatag.format"), "format", node -> getFormat(node), String.class);
    col.setHeaderIcon(IconManager.VIDEO_FORMAT);
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("1080p") * 1.2f));
    addColumn(col);

    /*
     * main video file size
     */
    col = new Column(BUNDLE.getString("metatag.size"), "fileSize", node -> getFileSize(node), String.class);
    col.setHeaderIcon(IconManager.FILE_SIZE);
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("50000M") * 1.2f));
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

    /*
     * watched
     */
    col = new Column(BUNDLE.getString("metatag.watched"), "watched", node -> isWatched(node), ImageIcon.class);
    col.setHeaderIcon(IconManager.WATCHED);
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
      if (((TvShowSeason) userObject).getEpisodes().size() > 0) {
        return String.valueOf(((TvShowSeason) userObject).getEpisodes().size());
      }
    }
    return "";
  }

  private String getRating(TmmTreeNode node) {
    Object userObject = node.getUserObject();
    if (userObject instanceof TvShow || userObject instanceof TvShowEpisode) {
      Rating rating = ((MediaEntity) userObject).getRating();
      if (rating != null && rating.getRating() > 0) {
        return String.valueOf(rating.getRating());
      }
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

  private String getFileSize(TmmTreeNode node) {
    Object userObject = node.getUserObject();
    if (userObject instanceof TvShowEpisode) {
      long size = 0;
      for (MediaFile mf : ((TvShowEpisode) userObject).getMediaFiles(MediaFileType.VIDEO)) {
        size += mf.getFilesize();
      }

      return (int) (size / (1024.0 * 1024.0)) + " M";
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
      return getCheckIcon(((TvShow) userObject).getHasImages());
    }
    if (userObject instanceof TvShowEpisode) {
      return getCheckIcon(((TvShowEpisode) userObject).getHasImages());
    }
    return null;
  }

  private ImageIcon isWatched(TmmTreeNode node) {
    Object userObject = node.getUserObject();
    if (userObject instanceof TvShow) {
      return getCheckIcon(((TvShow) userObject).isWatched());
    }
    if (userObject instanceof TvShowSeason) {
      return getCheckIcon(((TvShowSeason) userObject).isWatched());
    }
    if (userObject instanceof TvShowEpisode) {
      return getCheckIcon(((TvShowEpisode) userObject).isWatched());
    }
    return null;
  }
}
