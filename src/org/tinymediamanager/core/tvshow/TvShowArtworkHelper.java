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
package org.tinymediamanager.core.tvshow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaEntityImageFetcherTask;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.util.Url;

/**
 * The class TvShowArtworkHelper . A helper class for managing TV show artwork
 * 
 * @author Manuel Laggner
 */
public class TvShowArtworkHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(TvShowArtworkHelper.class);

  /**
   * Manage downloading of the chosen artwork type
   * 
   * @param show
   *          the TV show for which artwork has to be downloaded
   * @param type
   *          the artwork type to be downloaded
   */
  public void downloadArtwork(TvShow show, MediaFileType type) {
    String url = "";
    String filename = "";

    switch (type) {
      case FANART:
        url = show.getFanartUrl();
        filename = "fanart." + FilenameUtils.getExtension(url);
        break;

      case POSTER:
        url = show.getPosterUrl();
        filename = "poster." + FilenameUtils.getExtension(url);
        break;

      case BANNER:
        url = show.getBannerUrl();
        filename = "banner." + FilenameUtils.getExtension(url);
        break;

      case EXTRAFANART:
      case EXTRATHUMB:
      case LOGO:
      case CLEARART:
      case THUMB:
        url = show.getArtworkUrl(type);
        filename = type.name().toLowerCase() + "." + FilenameUtils.getExtension(url);
        break;

      default:
        break;
    }

    if (StringUtils.isBlank(url) || StringUtils.isBlank(filename)) {
      return;
    }

    // get image in thread
    MediaEntityImageFetcherTask task = new MediaEntityImageFetcherTask(show, url, MediaArtworkType.getMediaArtworkType(type), filename, true);
    TmmTaskManager.getInstance().addImageDownloadTask(task);
  }

  /**
   * Download the season poster
   * 
   * @param show
   *          the TV show
   * @param season
   *          the season to download the poster for
   */
  public void downloadSeasonPoster(TvShow show, int season) {
    String seasonPosterUrl = show.getSeasonPosterUrl(season);

    TvShowSeason tvShowSeason = null;
    // try to get a season instance
    for (TvShowSeason s : show.getSeasons()) {
      if (s.getSeason() == season) {
        tvShowSeason = s;
        break;
      }
    }

    String filename = "";
    if (season > 0) {
      filename = String.format(show.getPath() + File.separator + "season%02d-poster." + FilenameUtils.getExtension(seasonPosterUrl), season);
    }
    else {
      filename = show.getPath() + File.separator + "season-specials-poster." + FilenameUtils.getExtension(seasonPosterUrl);
    }
    SeasonPosterImageFetcher task = new SeasonPosterImageFetcher(show, filename, tvShowSeason, seasonPosterUrl);
    TmmTaskManager.getInstance().addImageDownloadTask(task);
  }

  private class SeasonPosterImageFetcher implements Runnable {
    private TvShow       tvShow;
    private String       filename;
    private TvShowSeason tvShowSeason;
    private String       url;

    SeasonPosterImageFetcher(TvShow show, String filename, TvShowSeason tvShowSeason, String url) {
      this.tvShow = show;
      this.filename = filename;
      this.tvShowSeason = tvShowSeason;
      this.url = url;
    }

    @Override
    public void run() {
      String oldFilename = "";
      try {
        if (tvShowSeason != null) {
          oldFilename = tvShowSeason.getPoster();
          tvShowSeason.clearPoster();
        }

        LOGGER.debug("writing season poster " + filename);

        // fetch and store images
        Url url1 = new Url(url);
        FileOutputStream outputStream = new FileOutputStream(filename);
        InputStream is = url1.getInputStream();
        IOUtils.copy(is, outputStream);
        outputStream.close();
        outputStream.flush();
        try {
          outputStream.getFD().sync(); // wait until file has been completely written
        }
        catch (Exception e) {
          // empty here -> just not let the thread crash
        }
        is.close();

        ImageCache.invalidateCachedImage(filename);
        if (tvShowSeason != null) {
          tvShowSeason.setPoster(new File(filename));
        }
      }
      catch (IOException e) {
        LOGGER.debug("fetch image", e);
        // fallback
        if (tvShowSeason != null) {
          tvShowSeason.setPoster(new File(oldFilename));
        }
      }
      catch (Exception e) {
        LOGGER.error("Thread crashed", e);
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, this, "message.scrape.tvshowartworkfailed"));
      }
      finally {
        tvShow.saveToDb();
      }
    }
  }
}
