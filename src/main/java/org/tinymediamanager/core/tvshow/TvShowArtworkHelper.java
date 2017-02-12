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
package org.tinymediamanager.core.tvshow;

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
import org.tinymediamanager.core.MediaEntityImageFetcherTask;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonPosterNaming;
import org.tinymediamanager.scraper.entities.MediaArtwork;
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
  }

  public static void downloadMissingArtwork(TvShow show) {
    MediaFileType[] mfts = MediaFileType.getGraphicMediaFileTypes();

    // do for all known graphical MediaFileTypes
    for (MediaFileType mft : mfts) {

      // special logix for season posters
      if (mft == MediaFileType.SEASON_POSTER) {
        for (TvShowSeason season : show.getSeasons()) {
          if (StringUtils.isBlank(season.getPoster())) {
            downloadSeasonPoster(show, season.getSeason());
          }
        }
        continue;
      }

      List<MediaFile> mfs = show.getMediaFiles(mft);
      if (mfs.isEmpty()) {
        boolean download = false;
        // not in our list? get'em!
        switch (mft) {
          case FANART:
            if (!TvShowModuleManager.SETTINGS.getFanartFilenames().isEmpty()) {
              download = true;
            }
            break;

          case POSTER:
            if (!TvShowModuleManager.SETTINGS.getPosterFilenames().isEmpty()) {
              download = true;
            }
            break;

          case BANNER:
            if (!TvShowModuleManager.SETTINGS.getBannerFilenames().isEmpty()) {
              download = true;
            }
            break;

          case CLEARART:
            if (!TvShowModuleManager.SETTINGS.getClearartFilenames().isEmpty()) {
              download = true;
            }
            break;

          case THUMB:
            if (!TvShowModuleManager.SETTINGS.getThumbFilenames().isEmpty()) {
              download = true;
            }
            break;

          case LOGO:
            if (!TvShowModuleManager.SETTINGS.getLogoFilenames().isEmpty()) {
              download = true;
            }
            break;

          case CLEARLOGO:
            if (!TvShowModuleManager.SETTINGS.getClearlogoFilenames().isEmpty()) {
              download = true;
            }
            break;

          default:
            break;
        }

        if (download) {
          downloadArtwork(show, mft);
        }
      }
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
      setBestArtwork(tvShow, artwork, MediaArtwork.MediaArtworkType.POSTER);
    }

    // fanart
    if (tvShow.getMediaFiles(MediaFileType.FANART).isEmpty()) {
      setBestArtwork(tvShow, artwork, MediaArtwork.MediaArtworkType.BACKGROUND);
    }

    // logo
    if (tvShow.getMediaFiles(MediaFileType.LOGO).isEmpty()) {
      setBestArtwork(tvShow, artwork, MediaArtwork.MediaArtworkType.LOGO);
    }

    // clearlogo
    if (tvShow.getMediaFiles(MediaFileType.CLEARLOGO).isEmpty()) {
      setBestArtwork(tvShow, artwork, MediaArtwork.MediaArtworkType.CLEARLOGO);
    }

    // clearart
    if (tvShow.getMediaFiles(MediaFileType.CLEARART).isEmpty()) {
      setBestArtwork(tvShow, artwork, MediaArtwork.MediaArtworkType.CLEARART);
    }

    // banner
    if (tvShow.getMediaFiles(MediaFileType.BANNER).isEmpty()) {
      setBestArtwork(tvShow, artwork, MediaArtwork.MediaArtworkType.BANNER);
    }

    // thumb
    if (tvShow.getMediaFiles(MediaFileType.THUMB).isEmpty()) {
      setBestArtwork(tvShow, artwork, MediaArtwork.MediaArtworkType.THUMB);
    }

    // discart
    if (tvShow.getMediaFiles(MediaFileType.DISC).isEmpty()) {
      setBestArtwork(tvShow, artwork, MediaArtwork.MediaArtworkType.DISC);
    }

    for (TvShowSeason season : tvShow.getSeasons()) {
      if (StringUtils.isBlank(season.getPoster())) {
        for (MediaArtwork art : artwork) {
          if (art.getSeason() == season.getSeason()) {
            tvShow.setSeasonPosterUrl(art.getSeason(), art.getDefaultUrl());
            downloadSeasonPoster(tvShow, art.getSeason());
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
  private static void setBestArtwork(TvShow tvShow, List<MediaArtwork> artwork, MediaArtwork.MediaArtworkType type) {
    for (MediaArtwork art : artwork) {
      if (art.getType() == type) {
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
      if (StringUtils.isBlank(season.getPoster())) {
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
    if (episode.getMediaFiles(MediaFileType.THUMB).isEmpty()) {
      return true;
    }

    return false;
  }

  /**
   * Download the season poster
   * 
   * @param show
   *          the TV show
   * @param season
   *          the season to download the poster for
   */
  public static void downloadSeasonPoster(TvShow show, int season) {
    String seasonPosterUrl = show.getSeasonPosterUrl(season);

    TvShowSeason tvShowSeason = null;
    // try to get a season instance
    for (TvShowSeason s : show.getSeasons()) {
      if (s.getSeason() == season) {
        tvShowSeason = s;
        break;
      }
    }

    for (TvShowSeasonPosterNaming seasonPosterNaming : TvShowModuleManager.SETTINGS.getSeasonPosterFilenames()) {
      String filename = "";
      switch (seasonPosterNaming) {
        case SEASON_POSTER:
          if (season > 0) {
            filename = String.format("season%02d-poster", season);
          }
          else {
            filename = "season-specials-poster";
          }
          break;

        case SEASON_FOLDER:
          String seasonFoldername = TvShowRenamer.getSeasonFoldername(show, season);
          if (StringUtils.isNotBlank(seasonFoldername)) {
            filename = seasonFoldername + File.separator;
          }
          filename += String.format("season%02d", season);
          break;
      }

      filename = show.getPath() + File.separator + filename + "." + Utils.getArtworkExtension(seasonPosterUrl);

      SeasonPosterImageFetcher task = new SeasonPosterImageFetcher(show, filename, tvShowSeason, seasonPosterUrl);
      TmmTaskManager.getInstance().addImageDownloadTask(task);
    }
  }

  private static class SeasonPosterImageFetcher implements Runnable {
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

        ImageCache.invalidateCachedImage(Paths.get(filename));
        if (tvShowSeason != null) {
          tvShowSeason.setPoster(new File(filename));
        }
      }
      catch (IOException e) {
        LOGGER.debug("fetch image", e);
        // fallback
        if (tvShowSeason != null && !oldFilename.isEmpty()) {
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
