/*
 * Copyright 2012 - 2020 Manuel Laggner
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
import java.util.Date;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.TmmDateFormat;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;
import org.tinymediamanager.ui.components.treetable.TmmTreeTableFormat;
import org.tinymediamanager.ui.renderer.DateTableCellRenderer;
import org.tinymediamanager.ui.renderer.RightAlignTableCellRenderer;

/**
 * The class TvShowTableFormat is used to define the columns for the TV show tree table
 *
 * @author Manuel Laggner
 */
public class TvShowTableFormat extends TmmTreeTableFormat<TmmTreeNode> {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

  public TvShowTableFormat() {
    FontMetrics fontMetrics = getFontMetrics();

    /*
     * season count
     */
    Column col = new Column(BUNDLE.getString("metatag.seasons"), "seasons", this::getSeasons, String.class);
    col.setHeaderIcon(IconManager.SEASONS);
    col.setCellRenderer(new RightAlignTableCellRenderer());
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("99") * 1.2f));
    addColumn(col);

    /*
     * episode count
     */
    col = new Column(BUNDLE.getString("metatag.episodes"), "episodes", this::getEpisodes, String.class);
    col.setHeaderIcon(IconManager.EPISODES);
    col.setCellRenderer(new RightAlignTableCellRenderer());
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("999") * 1.2f));
    addColumn(col);

    /*
     * rating
     */
    col = new Column(BUNDLE.getString("metatag.rating"), "rating", this::getRating, String.class);
    col.setHeaderIcon(IconManager.RATING);
    col.setCellRenderer(new RightAlignTableCellRenderer());
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("99.9") * 1.2f));
    addColumn(col);

    /*
     * aired
     */
    col = new Column(BUNDLE.getString("metatag.aired"), "aired", this::getAiredDate, Date.class);
    col.setHeaderIcon(IconManager.DATE_ADDED);
    col.setCellRenderer(new DateTableCellRenderer());
    col.setColumnResizeable(false);
    try {
      Date date = StrgUtils.parseDate("2012-12-12");
      col.setMinWidth((int) (fontMetrics.stringWidth(TmmDateFormat.MEDIUM_DATE_FORMAT.format(date)) * 1.2f));
    }
    catch (Exception ignored) {
    }
    addColumn(col);

    /*
     * video format (hidden per default)
     */
    col = new Column(BUNDLE.getString("metatag.format"), "format", this::getFormat, String.class);
    col.setHeaderIcon(IconManager.VIDEO_FORMAT);
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("1080p") * 1.2f));
    addColumn(col);

    /*
     * main video file size (hidden per default)
     */
    col = new Column(BUNDLE.getString("metatag.size"), "fileSize", this::getFileSize, String.class);
    col.setHeaderIcon(IconManager.FILE_SIZE);
    col.setCellRenderer(new RightAlignTableCellRenderer());
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("50000M") * 1.2f));
    addColumn(col);

    /*
     * NFO
     */
    col = new Column(BUNDLE.getString("tmm.nfo"), "nfo", this::hasNfo, ImageIcon.class);
    col.setHeaderIcon(IconManager.NFO);
    col.setColumnResizeable(false);
    col.setColumnTooltip(this::hasNfoTooltip);
    addColumn(col);

    /*
     * images
     */
    col = new Column(BUNDLE.getString("tmm.images"), "images", this::hasImages, ImageIcon.class);
    col.setHeaderIcon(IconManager.IMAGES);
    col.setColumnResizeable(false);
    col.setColumnTooltip(this::hasImageTooltip);
    addColumn(col);

    /*
     * subtitles
     */
    col = new Column(BUNDLE.getString("tmm.subtitles"), "subtitles", this::hasSubtitles, ImageIcon.class);
    col.setHeaderIcon(IconManager.SUBTITLES);
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * watched
     */
    col = new Column(BUNDLE.getString("metatag.watched"), "watched", this::isWatched, ImageIcon.class);
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
      if (!((TvShowSeason) userObject).getEpisodes().isEmpty()) {
        return String.valueOf(((TvShowSeason) userObject).getEpisodes().size());
      }
    }
    return "";
  }

  private String getRating(TmmTreeNode node) {
    Object userObject = node.getUserObject();
    if (userObject instanceof TvShow || userObject instanceof TvShowEpisode) {
      MediaRating mediaRating = ((MediaEntity) userObject).getRating();
      if (mediaRating != null && mediaRating.getRating() > 0) {
        return String.valueOf(mediaRating.getRating());
      }
    }
    return "";
  }

  private Date getAiredDate(TmmTreeNode node) {
    Object userObject = node.getUserObject();
    if (userObject instanceof TvShow) {
      Date airedDate = ((TvShow) userObject).getFirstAired();
      if (airedDate != null) {
        return airedDate;
      }
    }
    if (userObject instanceof TvShowSeason) {
      Date airedDate = ((TvShowSeason) userObject).getFirstAired();
      if (airedDate != null) {
        return airedDate;
      }
    }
    if (userObject instanceof TvShowEpisode) {
      Date airedDate = ((TvShowEpisode) userObject).getFirstAired();
      if (airedDate != null) {
        return airedDate;
      }
    }
    return null;
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

      return (int) (size / (1000.0 * 1000.0)) + " M";
    }
    return "";
  }

  private ImageIcon hasNfo(TmmTreeNode node) {
    Object userObject = node.getUserObject();
    if (userObject instanceof TvShow) {
      TvShow tvShow = (TvShow) userObject;
      // if we untick to write episode file NFOs, we do not need to check for tree "problems"...
      if (TvShowModuleManager.SETTINGS.getEpisodeNfoFilenames().isEmpty()) {
        return getCheckIcon(tvShow.getHasNfoFile());
      }
      return getTriStateIcon(TRI_STATE.getState(tvShow.getHasNfoFile(), tvShow.getHasEpisodeNfoFiles()));
    }
    else if (userObject instanceof TvShowSeason) {
      TvShowSeason season = ((TvShowSeason) userObject);
      return getCheckIcon(season.getHasEpisodeNfoFiles());
    }
    else if (userObject instanceof TvShowEpisode) {
      TvShowEpisode episode = ((TvShowEpisode) userObject);
      return getCheckIcon(episode.getHasNfoFile());
    }
    return null;
  }

  private ImageIcon hasImages(TmmTreeNode node) {
    Object userObject = node.getUserObject();
    if (userObject instanceof TvShow) {
      TvShow tvShow = (TvShow) userObject;
      return getTriStateIcon(TRI_STATE.getState(tvShow.getHasImages(), tvShow.getHasSeasonAndEpisodeImages()));
    }
    else if (userObject instanceof TvShowSeason) {
      TvShowSeason season = ((TvShowSeason) userObject);
      return getTriStateIcon(TRI_STATE.getState(season.getHasImages(), season.getHasEpisodeImages()));
    }
    else if (userObject instanceof TvShowEpisode) {
      TvShowEpisode episode = ((TvShowEpisode) userObject);
      return getCheckIcon(episode.getHasImages());
    }
    return null;
  }

  private ImageIcon hasSubtitles(TmmTreeNode node) {
    Object userObject = node.getUserObject();
    if (userObject instanceof TvShow) {
      TvShow tvShow = (TvShow) userObject;
      return getCheckIcon(tvShow.hasEpisodeSubtitles());
    }
    else if (userObject instanceof TvShowSeason) {
      TvShowSeason season = ((TvShowSeason) userObject);
      return getCheckIcon(season.hasEpisodeSubtitles());
    }
    else if (userObject instanceof TvShowEpisode) {
      TvShowEpisode episode = ((TvShowEpisode) userObject);
      return getCheckIcon(episode.getHasSubtitles());
    }
    return null;
  }

  private ImageIcon isWatched(TmmTreeNode node) {
    Object userObject = node.getUserObject();
    if (userObject instanceof TvShow) {
      return getCheckIcon(((TvShow) userObject).isWatched());
    }
    else if (userObject instanceof TvShowSeason) {
      return getCheckIcon(((TvShowSeason) userObject).isWatched());
    }
    else if (userObject instanceof TvShowEpisode) {
      return getCheckIcon(((TvShowEpisode) userObject).isWatched());
    }
    return null;
  }

  private String hasNfoTooltip(TmmTreeNode node) {
    if (node.getUserObject() instanceof TvShow) {
      ImageIcon nfoIcon = hasNfo(node);
      if (nfoIcon == IconManager.TABLE_PROBLEM) {
        return BUNDLE.getString("tvshow.tree.nfo.problem");
      }
    }
    return null;
  }

  private String hasImageTooltip(TmmTreeNode node) {
    if (node.getUserObject() instanceof TvShow) {
      ImageIcon nfoIcon = hasImages(node);
      if (nfoIcon == IconManager.TABLE_PROBLEM) {
        return BUNDLE.getString("tvshow.tree.tvshow.image.problem");
      }
    }
    else if (node.getUserObject() instanceof TvShowSeason) {
      ImageIcon nfoIcon = hasImages(node);
      if (nfoIcon == IconManager.TABLE_PROBLEM) {
        return BUNDLE.getString("tvshow.tree.season.image.problem");
      }
    }
    return null;
  }
}
