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
package org.tinymediamanager.core.tvshow;

import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_BANNER;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_POSTER;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_THUMB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.IFileNaming;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.tasks.MediaEntityImageFetcherTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonBannerNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonPosterNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonThumbNaming;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.http.Url;

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
  public static void downloadArtwork(TvShow show, MediaFileType type) {
    String url = show.getArtworkUrl(type);
    if (StringUtils.isBlank(url)) {
      return;
    }

    List<IFileNaming> fileNamings = new ArrayList<>();

    switch (type) {
      case FANART:
        fileNamings.addAll(TvShowModuleManager.SETTINGS.getFanartFilenames());
        break;

      case POSTER:
        fileNamings.addAll(TvShowModuleManager.SETTINGS.getPosterFilenames());
        break;

      case BANNER:
        fileNamings.addAll(TvShowModuleManager.SETTINGS.getBannerFilenames());
        break;

      case LOGO:
        fileNamings.addAll(TvShowModuleManager.SETTINGS.getLogoFilenames());
        break;

      case CLEARLOGO:
        fileNamings.addAll(TvShowModuleManager.SETTINGS.getClearlogoFilenames());
        break;

      case CLEARART:
        fileNamings.addAll(TvShowModuleManager.SETTINGS.getClearartFilenames());
        break;

      case THUMB:
        fileNamings.addAll(TvShowModuleManager.SETTINGS.getThumbFilenames());
        break;

      default:
        return;
    }

    int i = 0;
    for (IFileNaming naming : fileNamings) {
      boolean firstImage = false;
      String filename = naming.getFilename("", Utils.getArtworkExtension(url));

      if (StringUtils.isBlank(filename)) {
        continue;
      }

      if (++i == 1) {
        firstImage = true;
      }

      // get image in thread
      MediaEntityImageFetcherTask task = new MediaEntityImageFetcherTask(show, url, MediaFileType.getMediaArtworkType(type), filename, firstImage);
      TmmTaskManager.getInstance().addImageDownloadTask(task);
    }

    // if that has been a local file, remove it from the artwork urls after we've already started the download(copy) task
    if (url.startsWith("file:")) {
      show.removeArtworkUrl(type);
    }
  }

  /**
   * set & download missing artwork for the given TV show
   *
   * @param tvShow
   *          the TV show to set the artwork for
   * @param artwork
   *          a list of all artworks to be set
   */
  public static void downloadMissingArtwork(TvShow tvShow, List<MediaArtwork> artwork) {
    // sort artwork once again (langu/rating)
    artwork.sort(new MediaArtwork.MediaArtworkComparator(TvShowModuleManager.SETTINGS.getScraperLanguage().name()));

    // poster
    if (tvShow.getMediaFiles(MediaFileType.POSTER).isEmpty()) {
      setBestArtwork(tvShow, artwork, MediaArtworkType.POSTER);
    }

    // fanart
    if (tvShow.getMediaFiles(MediaFileType.FANART).isEmpty()) {
      setBestArtwork(tvShow, artwork, MediaArtworkType.BACKGROUND);
    }

    // logo
    if (tvShow.getMediaFiles(MediaFileType.LOGO).isEmpty()) {
      setBestArtwork(tvShow, artwork, MediaArtworkType.LOGO);
    }

    // clearlogo
    if (tvShow.getMediaFiles(MediaFileType.CLEARLOGO).isEmpty()) {
      setBestArtwork(tvShow, artwork, MediaArtworkType.CLEARLOGO);
    }

    // clearart
    if (tvShow.getMediaFiles(MediaFileType.CLEARART).isEmpty()) {
      setBestArtwork(tvShow, artwork, MediaArtworkType.CLEARART);
    }

    // banner
    if (tvShow.getMediaFiles(MediaFileType.BANNER).isEmpty()) {
      setBestArtwork(tvShow, artwork, MediaArtworkType.BANNER);
    }

    // thumb
    if (tvShow.getMediaFiles(MediaFileType.THUMB).isEmpty()) {
      setBestArtwork(tvShow, artwork, MediaArtworkType.THUMB);
    }

    // discart
    if (tvShow.getMediaFiles(MediaFileType.DISC).isEmpty()) {
      setBestArtwork(tvShow, artwork, MediaArtworkType.DISC);
    }

    for (TvShowSeason season : tvShow.getSeasons()) {
      if (StringUtils.isBlank(season.getArtworkFilename(SEASON_POSTER))) {
        for (MediaArtwork art : artwork) {
          if (art.getSeason() == season.getSeason()) {
            tvShow.setSeasonArtworkUrl(art.getSeason(), art.getDefaultUrl(), SEASON_POSTER);
            downloadSeasonPoster(tvShow, art.getSeason());
          }
        }
      }
      if (StringUtils.isBlank(season.getArtworkFilename(SEASON_BANNER))) {
        for (MediaArtwork art : artwork) {
          if (art.getSeason() == season.getSeason()) {
            tvShow.setSeasonArtworkUrl(art.getSeason(), art.getDefaultUrl(), SEASON_BANNER);
            downloadSeasonBanner(tvShow, art.getSeason());
          }
        }
      }
      if (StringUtils.isBlank(season.getArtworkFilename(SEASON_THUMB))) {
        for (MediaArtwork art : artwork) {
          if (art.getSeason() == season.getSeason()) {
            tvShow.setSeasonArtworkUrl(art.getSeason(), art.getDefaultUrl(), SEASON_THUMB);
            downloadSeasonThumb(tvShow, art.getSeason());
          }
        }
      }
    }

    // update DB
    tvShow.saveToDb();
  }

  /**
   * choose the best artwork for this tv show
   *
   * @param tvShow
   *          our tv show
   * @param artwork
   *          the artwork list
   * @param type
   *          the type to download
   */
  private static void setBestArtwork(TvShow tvShow, List<MediaArtwork> artwork, MediaArtworkType type) {
    for (MediaArtwork art : artwork) {
      if (art.getType() == type && StringUtils.isNotBlank(art.getDefaultUrl())) {
        tvShow.setArtworkUrl(art.getDefaultUrl(), MediaFileType.getMediaFileType(type));
        downloadArtwork(tvShow, MediaFileType.getMediaFileType(type));
        break;
      }
    }
  }

  /**
   * detect if there is missing artwork for the given TV show
   * 
   * @param tvShow
   *          the TV show to check artwork for
   * @return true/false
   */
  public static boolean hasMissingArtwork(TvShow tvShow) {
    if (tvShow.getMediaFiles(MediaFileType.POSTER).isEmpty()) {
      return true;
    }
    if (tvShow.getMediaFiles(MediaFileType.FANART).isEmpty()) {
      return true;
    }
    if (tvShow.getMediaFiles(MediaFileType.BANNER).isEmpty()) {
      return true;
    }
    if (tvShow.getMediaFiles(MediaFileType.DISC).isEmpty()) {
      return true;
    }
    if (tvShow.getMediaFiles(MediaFileType.LOGO).isEmpty()) {
      return true;
    }
    if (tvShow.getMediaFiles(MediaFileType.CLEARLOGO).isEmpty()) {
      return true;
    }
    if (tvShow.getMediaFiles(MediaFileType.CLEARART).isEmpty()) {
      return true;
    }
    if (tvShow.getMediaFiles(MediaFileType.THUMB).isEmpty()) {
      return true;
    }
    for (TvShowSeason season : tvShow.getSeasons()) {
      if (StringUtils.isBlank(season.getArtworkFilename(SEASON_POSTER))) {
        return true;
      }
      if (StringUtils.isBlank(season.getArtworkFilename(SEASON_BANNER))) {
        return true;
      }
      if (StringUtils.isBlank(season.getArtworkFilename(SEASON_THUMB))) {
        return true;
      }
    }

    return false;
  }

  /**
   * detect if there is missing artwork for the given episode
   * 
   * @param episode
   *          the episode to check artwork for
   * @return true/false
   */
  public static boolean hasMissingArtwork(TvShowEpisode episode) {
    return episode.getMediaFiles(MediaFileType.THUMB).isEmpty();
  }

  public static void downloadSeasonArtwork(TvShow show, int season, MediaArtworkType artworkType) {
    switch (artworkType) {
      case SEASON_POSTER:
        downloadSeasonPoster(show, season);
        break;

      case SEASON_BANNER:
        downloadSeasonBanner(show, season);
        break;

      case SEASON_THUMB:
        downloadSeasonThumb(show, season);
        break;

      default:
        return;
    }
  }

  /**
   * Download the season poster
   * 
   * @param show
   *          the TV show
   * @param season
   *          the season to download the poster for
   */
  private static void downloadSeasonPoster(TvShow show, int season) {
    String seasonPosterUrl = show.getSeasonArtworkUrl(season, SEASON_POSTER);

    TvShowSeason tvShowSeason = null;
    // try to get a season instance
    for (TvShowSeason s : show.getSeasons()) {
      if (s.getSeason() == season) {
        tvShowSeason = s;
        break;
      }
    }

    for (TvShowSeasonPosterNaming seasonPosterNaming : TvShowModuleManager.SETTINGS.getSeasonPosterFilenames()) {
      String filename = show.getPathNIO() + File.separator + seasonPosterNaming.getFilename(show, season, Utils.getArtworkExtension(seasonPosterUrl));

      SeasonArtworkImageFetcher task = new SeasonArtworkImageFetcher(show, filename, tvShowSeason, seasonPosterUrl, SEASON_POSTER);
      TmmTaskManager.getInstance().addImageDownloadTask(task);
    }
  }

  /**
   * Download the season banner
   *
   * @param show
   *          the TV show
   * @param season
   *          the season to download the banner for
   */
  private static void downloadSeasonBanner(TvShow show, int season) {
    String seasonBannerUrl = show.getSeasonArtworkUrl(season, SEASON_BANNER);

    TvShowSeason tvShowSeason = null;
    // try to get a season instance
    for (TvShowSeason s : show.getSeasons()) {
      if (s.getSeason() == season) {
        tvShowSeason = s;
        break;
      }
    }

    for (TvShowSeasonBannerNaming seasonBannerNaming : TvShowModuleManager.SETTINGS.getSeasonBannerFilenames()) {
      String filename = show.getPathNIO() + File.separator + seasonBannerNaming.getFilename(show, season, Utils.getArtworkExtension(seasonBannerUrl));

      SeasonArtworkImageFetcher task = new SeasonArtworkImageFetcher(show, filename, tvShowSeason, seasonBannerUrl, SEASON_BANNER);
      TmmTaskManager.getInstance().addImageDownloadTask(task);
    }
  }

  /**
   * Download the season thumb
   *
   * @param show
   *          the TV show
   * @param season
   *          the season to download the thumb for
   */
  private static void downloadSeasonThumb(TvShow show, int season) {
    String seasonThumbUrl = show.getSeasonArtworkUrl(season, SEASON_THUMB);

    TvShowSeason tvShowSeason = null;
    // try to get a season instance
    for (TvShowSeason s : show.getSeasons()) {
      if (s.getSeason() == season) {
        tvShowSeason = s;
        break;
      }
    }

    for (TvShowSeasonThumbNaming seasonThumbNaming : TvShowModuleManager.SETTINGS.getSeasonThumbFilenames()) {
      String filename = show.getPathNIO() + File.separator + seasonThumbNaming.getFilename(show, season, Utils.getArtworkExtension(seasonThumbUrl));

      SeasonArtworkImageFetcher task = new SeasonArtworkImageFetcher(show, filename, tvShowSeason, seasonThumbUrl, SEASON_THUMB);
      TmmTaskManager.getInstance().addImageDownloadTask(task);
    }
  }

  private static class SeasonArtworkImageFetcher implements Runnable {
    private TvShow           tvShow;
    private TvShowSeason     tvShowSeason;
    private MediaArtworkType artworkType;
    private String           filename;
    private String           url;

    SeasonArtworkImageFetcher(TvShow show, String filename, TvShowSeason tvShowSeason, String url, MediaArtworkType type) {
      this.tvShow = show;
      this.filename = filename;
      this.artworkType = type;
      this.tvShowSeason = tvShowSeason;
      this.url = url;
    }

    @Override
    public void run() {
      String oldFilename = "";
      try {
        if (tvShowSeason != null) {
          oldFilename = tvShow.getSeasonArtwork(tvShowSeason.getSeason(), artworkType);
          tvShowSeason.clearArtwork(artworkType);
        }

        LOGGER.debug("writing season artwork " + filename);

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

        ImageCache.invalidateCachedImage(Paths.get(filename));
        if (tvShowSeason != null) {
          tvShowSeason.setArtwork(Paths.get(filename), artworkType);
        }
      }
      catch (IOException e) {
        LOGGER.debug("fetch image", e);
        // fallback
        if (tvShowSeason != null && !oldFilename.isEmpty()) {
          tvShowSeason.setArtwork(Paths.get(oldFilename), artworkType);
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
