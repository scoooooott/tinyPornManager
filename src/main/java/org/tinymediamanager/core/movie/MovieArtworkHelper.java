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
package org.tinymediamanager.core.movie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.MediaEntityImageFetcherTask;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.filenaming.MovieBannerNaming;
import org.tinymediamanager.core.movie.filenaming.MovieClearartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieClearlogoNaming;
import org.tinymediamanager.core.movie.filenaming.MovieDiscartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieFanartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieLogoNaming;
import org.tinymediamanager.core.movie.filenaming.MoviePosterNaming;
import org.tinymediamanager.core.movie.filenaming.MovieThumbNaming;
import org.tinymediamanager.core.movie.tasks.MovieExtraImageFetcher;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;

/**
 * The class MovieArtworkHelper. A helper class for managing movie artwork
 * 
 * @author Manuel Laggner
 */
public class MovieArtworkHelper {

  /**
   * Manage downloading of the chosen artwork type
   * 
   * @param movie
   *          the movie for which artwork has to be downloaded
   * @param type
   *          the type of artwork to be downloaded
   */
  public static void downloadArtwork(Movie movie, MediaFileType type) {
    switch (type) {
      case FANART:
        downloadFanart(movie);
        break;

      case POSTER:
        downloadPoster(movie);
        break;

      case LOGO:
        downloadLogo(movie);
        break;

      case CLEARLOGO:
        downloadClearlogo(movie);
        break;

      case BANNER:
        downloadBanner(movie);
        break;

      case CLEARART:
        downloadClearart(movie);
        break;

      case THUMB:
        downloadThumb(movie);
        break;

      case DISC:
        downloadDiscart(movie);
        break;

      case EXTRAFANART:
      case EXTRATHUMB:
        downloadExtraArtwork(movie, type);
        break;

      default:
        break;
    }
  }

  /**
   * downloads all missing artworks<br>
   * adheres the user artwork settings
   * 
   * @param movie
   *          for specified movie
   */
  public static void downloadMissingArtwork(Movie movie) {
    downloadMissingArtwork(movie, false);
  }

  /**
   * downloads all missing artworks
   * 
   * @param movie
   *          for specified movie
   * @param force
   *          if forced, we ignore the artwork settings and download all known
   */
  public static void downloadMissingArtwork(Movie movie, boolean force) {
    MediaFileType[] mfts = MediaFileType.getGraphicMediaFileTypes();

    // do for all known graphical MediaFileTypes
    for (MediaFileType mft : mfts) {

      List<MediaFile> mfs = movie.getMediaFiles(mft);
      if (mfs.isEmpty()) {
        // not in our list? get'em!
        switch (mft) {
          case FANART:
            if (!MovieModuleManager.SETTINGS.getFanartFilenames().isEmpty() || force) {
              downloadFanart(movie);
            }
            break;

          case POSTER:
            if (!MovieModuleManager.SETTINGS.getPosterFilenames().isEmpty() || force) {
              downloadPoster(movie);
            }
            break;

          case BANNER:
            if (!MovieModuleManager.SETTINGS.getBannerFilenames().isEmpty() || force) {
              downloadBanner(movie);
            }
            break;

          case CLEARART:
            if (!MovieModuleManager.SETTINGS.getClearartFilenames().isEmpty() || force) {
              downloadClearart(movie);
            }
            break;

          case DISC:
            if (!MovieModuleManager.SETTINGS.getDiscartFilenames().isEmpty() || force) {
              downloadDiscart(movie);
            }
            break;

          case LOGO:
            if (!MovieModuleManager.SETTINGS.getLogoFilenames().isEmpty() || force) {
              downloadLogo(movie);
            }
            break;

          case CLEARLOGO:
            if (!MovieModuleManager.SETTINGS.getClearlogoFilenames().isEmpty() || force) {
              downloadClearlogo(movie);
            }
            break;

          case THUMB:
            if (!MovieModuleManager.SETTINGS.getThumbFilenames().isEmpty() || force) {
              downloadThumb(movie);
            }
            break;

          case EXTRAFANART:
            if (MovieModuleManager.SETTINGS.isImageExtraFanart() || force) {
              downloadExtraArtwork(movie, mft);
            }
            break;

          case EXTRATHUMB:
            if (MovieModuleManager.SETTINGS.isImageExtraThumbs() || force) {
              downloadExtraArtwork(movie, mft);
            }
            break;

          default:
            break;
        }
      }
    }
  }

  /**
   * set & download missing artwork for the given movie
   *
   * @param movie
   *          the movie to set the artwork for
   * @param artwork
   *          a list of all artworks to be set
   */
  public static void downloadMissingArtwork(Movie movie, List<MediaArtwork> artwork) {
    // sort artwork once again (langu/rating)
    Collections.sort(artwork, new MediaArtwork.MediaArtworkComparator(MovieModuleManager.SETTINGS.getScraperLanguage().getLanguage()));

    // poster
    if (movie.getMediaFiles(MediaFileType.POSTER).isEmpty()) {
      setBestPoster(movie, artwork);
    }

    // fanart
    if (movie.getMediaFiles(MediaFileType.FANART).isEmpty()) {
      setBestFanart(movie, artwork);
    }

    // logo
    if (movie.getMediaFiles(MediaFileType.LOGO).isEmpty()) {
      setBestArtwork(movie, artwork, MediaArtworkType.LOGO, !MovieModuleManager.SETTINGS.getLogoFilenames().isEmpty());
    }

    // clearlogo
    if (movie.getMediaFiles(MediaFileType.CLEARLOGO).isEmpty()) {
      setBestArtwork(movie, artwork, MediaArtworkType.CLEARLOGO, !MovieModuleManager.SETTINGS.getClearlogoFilenames().isEmpty());
    }

    // clearart
    if (movie.getMediaFiles(MediaFileType.CLEARART).isEmpty()) {
      setBestArtwork(movie, artwork, MediaArtworkType.CLEARART, !MovieModuleManager.SETTINGS.getClearartFilenames().isEmpty());
    }

    // banner
    if (movie.getMediaFiles(MediaFileType.BANNER).isEmpty()) {
      setBestArtwork(movie, artwork, MediaArtworkType.BANNER, !MovieModuleManager.SETTINGS.getBannerFilenames().isEmpty());
    }

    // thumb
    if (movie.getMediaFiles(MediaFileType.THUMB).isEmpty()) {
      setBestArtwork(movie, artwork, MediaArtworkType.THUMB, !MovieModuleManager.SETTINGS.getThumbFilenames().isEmpty());
    }

    // discart
    if (movie.getMediaFiles(MediaFileType.DISC).isEmpty()) {
      setBestArtwork(movie, artwork, MediaArtworkType.DISC, !MovieModuleManager.SETTINGS.getDiscartFilenames().isEmpty());
    }

    // extrathumbs
    List<String> extrathumbs = new ArrayList<>();
    if (movie.getMediaFiles(MediaFileType.EXTRATHUMB).isEmpty() && MovieModuleManager.SETTINGS.isImageExtraThumbs()
        && MovieModuleManager.SETTINGS.getImageExtraThumbsCount() > 0) {
      for (MediaArtwork art : artwork) {
        // only get artwork in desired resolution
        if (art.getType() == MediaArtworkType.BACKGROUND && art.getSizeOrder() == MovieModuleManager.SETTINGS.getImageFanartSize().getOrder()) {
          extrathumbs.add(art.getDefaultUrl());
          if (extrathumbs.size() >= MovieModuleManager.SETTINGS.getImageExtraThumbsCount()) {
            break;
          }
        }
      }
      movie.setExtraThumbs(extrathumbs);
      if (extrathumbs.size() > 0) {
        if (!movie.isMultiMovieDir()) {
          downloadArtwork(movie, MediaFileType.EXTRATHUMB);
        }
      }
    }

    // extrafanarts
    List<String> extrafanarts = new ArrayList<>();
    if (MovieModuleManager.SETTINGS.isImageExtraFanart() && MovieModuleManager.SETTINGS.getImageExtraFanartCount() > 0) {
      for (MediaArtwork art : artwork) {
        // only get artwork in desired resolution
        if (art.getType() == MediaArtworkType.BACKGROUND && art.getSizeOrder() == MovieModuleManager.SETTINGS.getImageFanartSize().getOrder()) {
          extrafanarts.add(art.getDefaultUrl());
          if (extrafanarts.size() >= MovieModuleManager.SETTINGS.getImageExtraFanartCount()) {
            break;
          }
        }
      }
      movie.setExtraFanarts(extrafanarts);
      if (extrafanarts.size() > 0) {
        if (!movie.isMultiMovieDir()) {
          downloadArtwork(movie, MediaFileType.EXTRAFANART);
        }
      }
    }

    // update DB
    movie.saveToDb();
  }

  /**
   * detect if there is missing artwork for the given movie
   *
   * @param movie
   *          the movie to check artwork for
   * @return true/false
   */
  public static boolean hasMissingArtwork(Movie movie) {
    if (!MovieModuleManager.SETTINGS.getPosterFilenames().isEmpty() && movie.getMediaFiles(MediaFileType.POSTER).isEmpty()) {
      return true;
    }
    if (!MovieModuleManager.SETTINGS.getFanartFilenames().isEmpty() && movie.getMediaFiles(MediaFileType.FANART).isEmpty()) {
      return true;
    }
    if (!MovieModuleManager.SETTINGS.getBannerFilenames().isEmpty() && movie.getMediaFiles(MediaFileType.BANNER).isEmpty()) {
      return true;
    }
    if (!MovieModuleManager.SETTINGS.getDiscartFilenames().isEmpty() && movie.getMediaFiles(MediaFileType.DISC).isEmpty()) {
      return true;
    }
    if (!MovieModuleManager.SETTINGS.getLogoFilenames().isEmpty() && movie.getMediaFiles(MediaFileType.LOGO).isEmpty()) {
      return true;
    }
    if (!MovieModuleManager.SETTINGS.getClearlogoFilenames().isEmpty() && movie.getMediaFiles(MediaFileType.CLEARLOGO).isEmpty()) {
      return true;
    }
    if (!MovieModuleManager.SETTINGS.getClearartFilenames().isEmpty() && movie.getMediaFiles(MediaFileType.CLEARART).isEmpty()) {
      return true;
    }
    if (!MovieModuleManager.SETTINGS.getThumbFilenames().isEmpty() && movie.getMediaFiles(MediaFileType.THUMB).isEmpty()) {
      return true;
    }

    return false;
  }

  /**
   * Fanart format is not empty, so we want at least one ;)<br>
   * Idea is, to check whether the preferred format is set in settings<br>
   * and if not, take some default (since we want fanarts)
   * 
   * @param movie
   *          the movie to get the fanart names for
   * @return List of MovieFanartNaming (can be empty!)
   */
  public static List<MovieFanartNaming> getFanartNamesForMovie(Movie movie) {
    List<MovieFanartNaming> fanartnames = new ArrayList<>();
    if (MovieModuleManager.SETTINGS.getFanartFilenames().isEmpty()) {
      return fanartnames;
    }
    if (movie.isMultiMovieDir()) {
      if (MovieModuleManager.SETTINGS.getFanartFilenames().contains(MovieFanartNaming.FILENAME_FANART)) {
        fanartnames.add(MovieFanartNaming.FILENAME_FANART);
      }
      if (MovieModuleManager.SETTINGS.getFanartFilenames().contains(MovieFanartNaming.FILENAME_FANART2)) {
        fanartnames.add(MovieFanartNaming.FILENAME_FANART2);
      }
      if (fanartnames.isEmpty() || !MovieModuleManager.SETTINGS.getFanartFilenames().isEmpty()) {
        fanartnames.add(MovieFanartNaming.FILENAME_FANART);
      }
    }
    else if (movie.isDisc()) {
      fanartnames.add(MovieFanartNaming.FANART);
    }
    else {
      fanartnames = MovieModuleManager.SETTINGS.getFanartFilenames();
    }
    return fanartnames;
  }

  private static void downloadFanart(Movie movie) {
    String fanartUrl = movie.getArtworkUrl(MediaFileType.FANART);
    if (StringUtils.isBlank(fanartUrl)) {
      return;
    }

    int i = 0;
    for (MovieFanartNaming name : getFanartNamesForMovie(movie)) {
      boolean firstImage = false;
      String baseFilename = getBaseFanartFilename(name, movie);

      if (StringUtils.isBlank(fanartUrl) || StringUtils.isBlank(baseFilename)) {
        continue;
      }

      String filename = baseFilename + "." + Utils.getArtworkExtension(fanartUrl);

      if (++i == 1) {
        firstImage = true;
      }

      // get image in thread
      MediaEntityImageFetcherTask task = new MediaEntityImageFetcherTask(movie, fanartUrl, MediaArtworkType.BACKGROUND, filename, firstImage);
      TmmTaskManager.getInstance().addImageDownloadTask(task);
    }
  }

  /**
   * Poster format is not empty, so we want at least one ;)<br>
   * Idea is, to check whether the preferred format is set in settings<br>
   * and if not, take some default (since we want posters)
   * 
   * @param movie
   *          the movie to get the poster names for
   * @return list of MoviePosterNaming (can be empty!)
   */
  public static List<MoviePosterNaming> getPosterNamesForMovie(Movie movie) {
    List<MoviePosterNaming> posternames = new ArrayList<>();
    if (MovieModuleManager.SETTINGS.getPosterFilenames().isEmpty()) {
      return posternames;
    }
    if (movie.isMultiMovieDir()) {
      if (MovieModuleManager.SETTINGS.getPosterFilenames().contains(MoviePosterNaming.FILENAME_POSTER)) {
        posternames.add(MoviePosterNaming.FILENAME_POSTER);
      }
      if (MovieModuleManager.SETTINGS.getPosterFilenames().contains(MoviePosterNaming.FILENAME)) {
        posternames.add(MoviePosterNaming.FILENAME);
      }
      if (posternames.isEmpty() && !MovieModuleManager.SETTINGS.getPosterFilenames().isEmpty()) {
        posternames.add(MoviePosterNaming.FILENAME_POSTER);
      }
    }
    else if (movie.isDisc()) {
      if (MovieModuleManager.SETTINGS.getPosterFilenames().contains(MoviePosterNaming.FOLDER)) {
        posternames.add(MoviePosterNaming.FOLDER);
      }
      if (MovieModuleManager.SETTINGS.getPosterFilenames().contains(MoviePosterNaming.POSTER)
          || (posternames.isEmpty() && !MovieModuleManager.SETTINGS.getPosterFilenames().isEmpty())) {
        posternames.add(MoviePosterNaming.POSTER);
      }
    }
    else {
      posternames.addAll(MovieModuleManager.SETTINGS.getPosterFilenames());
    }
    return posternames;
  }

  private static void downloadPoster(Movie movie) {
    String posterUrl = movie.getArtworkUrl(MediaFileType.POSTER);
    if (StringUtils.isBlank(posterUrl)) {
      return;
    }

    int i = 0;
    for (MoviePosterNaming name : getPosterNamesForMovie(movie)) {
      boolean firstImage = false;
      String baseFilename = getBasePosterFilename(name, movie);

      if (StringUtils.isBlank(posterUrl) || StringUtils.isBlank(baseFilename)) {
        continue;
      }

      String filename = baseFilename + "." + Utils.getArtworkExtension(posterUrl);
      if (++i == 1) {
        firstImage = true;
      }

      // get image in thread
      MediaEntityImageFetcherTask task = new MediaEntityImageFetcherTask(movie, posterUrl, MediaArtworkType.POSTER, filename, firstImage);
      TmmTaskManager.getInstance().addImageDownloadTask(task);
    }
  }

  private static void downloadBanner(Movie movie) {
    String bannerUrl = movie.getArtworkUrl(MediaFileType.BANNER);
    if (StringUtils.isBlank(bannerUrl)) {
      return;
    }

    int i = 0;
    for (MovieBannerNaming name : getBannerNamesForMovie(movie)) {
      boolean firstImage = false;
      String baseFilename = getBaseBannerFilename(name, movie);

      if (StringUtils.isBlank(bannerUrl) || StringUtils.isBlank(baseFilename)) {
        continue;
      }

      String filename = baseFilename + "." + Utils.getArtworkExtension(bannerUrl);
      if (++i == 1) {
        firstImage = true;
      }

      // get image in thread
      MediaEntityImageFetcherTask task = new MediaEntityImageFetcherTask(movie, bannerUrl, MediaArtworkType.BANNER, filename, firstImage);
      TmmTaskManager.getInstance().addImageDownloadTask(task);
    }
  }

  /**
   * Banner format is not empty, so we want at least one ;)<br>
   * Idea is, to check whether the preferred format is set in settings<br>
   * and if not, take some default (since we want banners)
   *
   * @param movie
   *          the movie to get the banner names for
   * @return list of MovieBannerNaming (can be empty!)
   */
  public static List<MovieBannerNaming> getBannerNamesForMovie(Movie movie) {
    List<MovieBannerNaming> bannernames = new ArrayList<>();
    if (MovieModuleManager.SETTINGS.getBannerFilenames().isEmpty()) {
      return bannernames;
    }

    if (movie.isMultiMovieDir()) {
      if (MovieModuleManager.SETTINGS.getBannerFilenames().contains(MovieBannerNaming.FILENAME_BANNER)) {
        bannernames.add(MovieBannerNaming.FILENAME_BANNER);
      }
      if (bannernames.isEmpty() && !MovieModuleManager.SETTINGS.getBannerFilenames().isEmpty()) {
        bannernames.add(MovieBannerNaming.FILENAME_BANNER);
      }
    }
    else if (movie.isDisc()) {
      if (MovieModuleManager.SETTINGS.getBannerFilenames().contains(MovieBannerNaming.BANNER)) {
        bannernames.add(MovieBannerNaming.BANNER);
      }
      if (bannernames.isEmpty() && !MovieModuleManager.SETTINGS.getBannerFilenames().isEmpty()) {
        bannernames.add(MovieBannerNaming.BANNER);
      }
    }
    else {
      bannernames.addAll(MovieModuleManager.SETTINGS.getBannerFilenames());
    }
    return bannernames;
  }

  private static void downloadClearart(Movie movie) {
    String clearartUrl = movie.getArtworkUrl(MediaFileType.CLEARART);
    if (StringUtils.isBlank(clearartUrl)) {
      return;
    }

    int i = 0;
    for (MovieClearartNaming name : getClearartNamesForMovie(movie)) {
      boolean firstImage = false;
      String baseFilename = getBaseClearartFilename(name, movie);

      if (StringUtils.isBlank(clearartUrl) || StringUtils.isBlank(baseFilename)) {
        continue;
      }

      String filename = baseFilename + "." + Utils.getArtworkExtension(clearartUrl);
      if (++i == 1) {
        firstImage = true;
      }

      // get image in thread
      MediaEntityImageFetcherTask task = new MediaEntityImageFetcherTask(movie, clearartUrl, MediaArtworkType.CLEARART, filename, firstImage);
      TmmTaskManager.getInstance().addImageDownloadTask(task);
    }
  }

  /**
   * Clearart format is not empty, so we want at least one ;)<br>
   * Idea is, to check whether the preferred format is set in settings<br>
   * and if not, take some default (since we want cleararts)
   *
   * @param movie
   *          the movie to get the clearart names for
   * @return list of MovieClearartNaming (can be empty!)
   */
  public static List<MovieClearartNaming> getClearartNamesForMovie(Movie movie) {
    List<MovieClearartNaming> clearartnames = new ArrayList<>();
    if (MovieModuleManager.SETTINGS.getClearartFilenames().isEmpty()) {
      return clearartnames;
    }

    if (movie.isMultiMovieDir()) {
      if (MovieModuleManager.SETTINGS.getClearartFilenames().contains(MovieClearartNaming.FILENAME_CLEARART)) {
        clearartnames.add(MovieClearartNaming.FILENAME_CLEARART);
      }
      if (clearartnames.isEmpty() && !MovieModuleManager.SETTINGS.getClearartFilenames().isEmpty()) {
        clearartnames.add(MovieClearartNaming.FILENAME_CLEARART);
      }
    }
    else if (movie.isDisc()) {
      if (MovieModuleManager.SETTINGS.getClearartFilenames().contains(MovieClearartNaming.CLEARART)) {
        clearartnames.add(MovieClearartNaming.CLEARART);
      }
      if (clearartnames.isEmpty() && !MovieModuleManager.SETTINGS.getClearartFilenames().isEmpty()) {
        clearartnames.add(MovieClearartNaming.CLEARART);
      }
    }
    else {
      clearartnames.addAll(MovieModuleManager.SETTINGS.getClearartFilenames());
    }
    return clearartnames;
  }

  private static void downloadDiscart(Movie movie) {
    String discartUrl = movie.getArtworkUrl(MediaFileType.DISC);
    if (StringUtils.isBlank(discartUrl)) {
      return;
    }

    int i = 0;
    for (MovieDiscartNaming name : getDiscartNamesForMovie(movie)) {
      boolean firstImage = false;
      String baseFilename = getBaseDiscartFilename(name, movie);

      if (StringUtils.isBlank(discartUrl) || StringUtils.isBlank(baseFilename)) {
        continue;
      }

      String filename = baseFilename + "." + Utils.getArtworkExtension(discartUrl);
      if (++i == 1) {
        firstImage = true;
      }

      // get image in thread
      MediaEntityImageFetcherTask task = new MediaEntityImageFetcherTask(movie, discartUrl, MediaArtworkType.DISC, filename, firstImage);
      TmmTaskManager.getInstance().addImageDownloadTask(task);
    }
  }

  /**
   * Discart format is not empty, so we want at least one ;)<br>
   * Idea is, to check whether the preferred format is set in settings<br>
   * and if not, take some default (since we want discarts)
   *
   * @param movie
   *          the movie to get the discart names for
   * @return list of MovieDiscartNaming (can be empty!)
   */
  public static List<MovieDiscartNaming> getDiscartNamesForMovie(Movie movie) {
    List<MovieDiscartNaming> discartnames = new ArrayList<>();
    if (MovieModuleManager.SETTINGS.getDiscartFilenames().isEmpty()) {
      return discartnames;
    }

    if (movie.isMultiMovieDir()) {
      if (MovieModuleManager.SETTINGS.getDiscartFilenames().contains(MovieDiscartNaming.FILENAME_DISC)) {
        discartnames.add(MovieDiscartNaming.FILENAME_DISC);
      }
      if (discartnames.isEmpty() && !MovieModuleManager.SETTINGS.getDiscartFilenames().isEmpty()) {
        discartnames.add(MovieDiscartNaming.FILENAME_DISC);
      }
    }
    else if (movie.isDisc()) {
      if (MovieModuleManager.SETTINGS.getDiscartFilenames().contains(MovieDiscartNaming.DISC)) {
        discartnames.add(MovieDiscartNaming.DISC);
      }
      if (discartnames.isEmpty() && !MovieModuleManager.SETTINGS.getDiscartFilenames().isEmpty()) {
        discartnames.add(MovieDiscartNaming.DISC);
      }
    }
    else {
      discartnames.addAll(MovieModuleManager.SETTINGS.getDiscartFilenames());
    }
    return discartnames;
  }

  private static void downloadLogo(Movie movie) {
    String logoUrl = movie.getArtworkUrl(MediaFileType.LOGO);
    if (StringUtils.isBlank(logoUrl)) {
      return;
    }

    int i = 0;
    for (MovieLogoNaming name : getLogoNamesForMovie(movie)) {
      boolean firstImage = false;
      String baseFilename = getBaseLogoFilename(name, movie);

      if (StringUtils.isBlank(logoUrl) || StringUtils.isBlank(baseFilename)) {
        continue;
      }

      String filename = baseFilename + "." + Utils.getArtworkExtension(logoUrl);
      if (++i == 1) {
        firstImage = true;
      }

      // get image in thread
      MediaEntityImageFetcherTask task = new MediaEntityImageFetcherTask(movie, logoUrl, MediaArtworkType.LOGO, filename, firstImage);
      TmmTaskManager.getInstance().addImageDownloadTask(task);
    }
  }

  /**
   * Logo format is not empty, so we want at least one ;)<br>
   * Idea is, to check whether the preferred format is set in settings<br>
   * and if not, take some default (since we want logos)
   *
   * @param movie
   *          the movie to get the logo names for
   * @return list of MovieLogoNaming (can be empty!)
   */
  public static List<MovieLogoNaming> getLogoNamesForMovie(Movie movie) {
    List<MovieLogoNaming> logonames = new ArrayList<>();
    if (MovieModuleManager.SETTINGS.getLogoFilenames().isEmpty()) {
      return logonames;
    }

    if (movie.isMultiMovieDir()) {
      if (MovieModuleManager.SETTINGS.getLogoFilenames().contains(MovieLogoNaming.FILENAME_LOGO)) {
        logonames.add(MovieLogoNaming.FILENAME_LOGO);
      }
      if (logonames.isEmpty() && !MovieModuleManager.SETTINGS.getLogoFilenames().isEmpty()) {
        logonames.add(MovieLogoNaming.FILENAME_LOGO);
      }
    }
    else if (movie.isDisc()) {
      if (MovieModuleManager.SETTINGS.getLogoFilenames().contains(MovieLogoNaming.LOGO)) {
        logonames.add(MovieLogoNaming.LOGO);
      }
      if (logonames.isEmpty() && !MovieModuleManager.SETTINGS.getLogoFilenames().isEmpty()) {
        logonames.add(MovieLogoNaming.LOGO);
      }
    }
    else {
      logonames.addAll(MovieModuleManager.SETTINGS.getLogoFilenames());
    }
    return logonames;
  }

  private static void downloadClearlogo(Movie movie) {
    String clearlogoUrl = movie.getArtworkUrl(MediaFileType.CLEARLOGO);
    if (StringUtils.isBlank(clearlogoUrl)) {
      return;
    }

    int i = 0;
    for (MovieClearlogoNaming name : getClearlogoNamesForMovie(movie)) {
      boolean firstImage = false;
      String baseFilename = getBaseClearlogoFilename(name, movie);

      if (StringUtils.isBlank(clearlogoUrl) || StringUtils.isBlank(baseFilename)) {
        continue;
      }

      String filename = baseFilename + "." + Utils.getArtworkExtension(clearlogoUrl);
      if (++i == 1) {
        firstImage = true;
      }

      // get image in thread
      MediaEntityImageFetcherTask task = new MediaEntityImageFetcherTask(movie, clearlogoUrl, MediaArtworkType.CLEARLOGO, filename, firstImage);
      TmmTaskManager.getInstance().addImageDownloadTask(task);
    }
  }

  /**
   * Clearlogo format is not empty, so we want at least one ;)<br>
   * Idea is, to check whether the preferred format is set in settings<br>
   * and if not, take some default (since we want clearlogos)
   *
   * @param movie
   *          the movie to get the clearlogo names for
   * @return list of MovieClearlogoNaming (can be empty!)
   */
  public static List<MovieClearlogoNaming> getClearlogoNamesForMovie(Movie movie) {
    List<MovieClearlogoNaming> clearlogonames = new ArrayList<>();
    if (MovieModuleManager.SETTINGS.getClearlogoFilenames().isEmpty()) {
      return clearlogonames;
    }

    if (movie.isMultiMovieDir()) {
      if (MovieModuleManager.SETTINGS.getClearlogoFilenames().contains(MovieClearlogoNaming.FILENAME_CLEARLOGO)) {
        clearlogonames.add(MovieClearlogoNaming.FILENAME_CLEARLOGO);
      }
      if (clearlogonames.isEmpty() && !MovieModuleManager.SETTINGS.getClearlogoFilenames().isEmpty()) {
        clearlogonames.add(MovieClearlogoNaming.FILENAME_CLEARLOGO);
      }
    }
    else if (movie.isDisc()) {
      if (MovieModuleManager.SETTINGS.getClearlogoFilenames().contains(MovieClearlogoNaming.CLEARLOGO)) {
        clearlogonames.add(MovieClearlogoNaming.CLEARLOGO);
      }
      if (clearlogonames.isEmpty() && !MovieModuleManager.SETTINGS.getClearlogoFilenames().isEmpty()) {
        clearlogonames.add(MovieClearlogoNaming.CLEARLOGO);
      }
    }
    else {
      clearlogonames.addAll(MovieModuleManager.SETTINGS.getClearlogoFilenames());
    }
    return clearlogonames;
  }

  private static void downloadThumb(Movie movie) {
    String thumbUrl = movie.getArtworkUrl(MediaFileType.THUMB);
    if (StringUtils.isBlank(thumbUrl)) {
      return;
    }

    int i = 0;
    for (MovieThumbNaming name : getThumbNamesForMovie(movie)) {
      boolean firstImage = false;
      String baseFilename = getBaseThumbFilename(name, movie);

      if (StringUtils.isBlank(thumbUrl) || StringUtils.isBlank(baseFilename)) {
        continue;
      }

      String filename = baseFilename + "." + Utils.getArtworkExtension(thumbUrl);
      if (++i == 1) {
        firstImage = true;
      }

      // get image in thread
      MediaEntityImageFetcherTask task = new MediaEntityImageFetcherTask(movie, thumbUrl, MediaArtworkType.THUMB, filename, firstImage);
      TmmTaskManager.getInstance().addImageDownloadTask(task);
    }
  }

  /**
   * Thumb format is not empty, so we want at least one ;)<br>
   * Idea is, to check whether the preferred format is set in settings<br>
   * and if not, take some default (since we want thumbs)
   *
   * @param movie
   *          the movie to get the thumb names for
   * @return list of MovieThumbNaming (can be empty!)
   */
  public static List<MovieThumbNaming> getThumbNamesForMovie(Movie movie) {
    List<MovieThumbNaming> thumbnames = new ArrayList<>();
    if (MovieModuleManager.SETTINGS.getThumbFilenames().isEmpty()) {
      return thumbnames;
    }

    if (movie.isMultiMovieDir()) {
      if (MovieModuleManager.SETTINGS.getThumbFilenames().contains(MovieThumbNaming.FILENAME_THUMB)) {
        thumbnames.add(MovieThumbNaming.FILENAME_THUMB);
      }
      if (MovieModuleManager.SETTINGS.getThumbFilenames().contains(MovieThumbNaming.FILENAME_LANDSCAPE)) {
        thumbnames.add(MovieThumbNaming.FILENAME_LANDSCAPE);
      }
      if (thumbnames.isEmpty() && !MovieModuleManager.SETTINGS.getThumbFilenames().isEmpty()
          && MovieModuleManager.SETTINGS.getThumbFilenames().contains(MovieThumbNaming.THUMB)) {
        thumbnames.add(MovieThumbNaming.FILENAME_THUMB);
      }
      if (thumbnames.isEmpty() && !MovieModuleManager.SETTINGS.getThumbFilenames().isEmpty()) {
        thumbnames.add(MovieThumbNaming.FILENAME_LANDSCAPE);
      }
    }
    else if (movie.isDisc()) {
      if (MovieModuleManager.SETTINGS.getThumbFilenames().contains(MovieThumbNaming.THUMB)) {
        thumbnames.add(MovieThumbNaming.THUMB);
      }
      if (MovieModuleManager.SETTINGS.getThumbFilenames().contains(MovieThumbNaming.LANDSCAPE)) {
        thumbnames.add(MovieThumbNaming.LANDSCAPE);
      }
      if (thumbnames.isEmpty() && !MovieModuleManager.SETTINGS.getThumbFilenames().isEmpty()
          && MovieModuleManager.SETTINGS.getThumbFilenames().contains(MovieThumbNaming.FILENAME_THUMB)) {
        thumbnames.add(MovieThumbNaming.THUMB);
      }
      if (thumbnames.isEmpty() && !MovieModuleManager.SETTINGS.getThumbFilenames().isEmpty()) {
        thumbnames.add(MovieThumbNaming.LANDSCAPE);
      }
    }
    else {
      thumbnames.addAll(MovieModuleManager.SETTINGS.getThumbFilenames());
    }
    return thumbnames;
  }

  private static void downloadExtraArtwork(Movie movie, MediaFileType type) {
    // get images in thread
    MovieExtraImageFetcher task = new MovieExtraImageFetcher(movie, type);
    TmmTaskManager.getInstance().addImageDownloadTask(task);
  }

  /**
   * all XBMC supported fanart names. (without path!)
   * 
   * @param fanart
   *          the fanart
   * @param movie
   *          the movie
   * @return the fanart filename
   */
  public static String getBaseFanartFilename(MovieFanartNaming fanart, Movie movie) {
    List<MediaFile> mfs = movie.getMediaFiles(MediaFileType.VIDEO);
    if (mfs != null && mfs.size() > 0) {
      return getBaseFanartFilename(fanart, movie.getVideoBasenameWithoutStacking());
    }
    else {
      return getBaseFanartFilename(fanart, ""); // no video files
    }
  }

  public static String getBaseFanartFilename(MovieFanartNaming fanart, String newMovieFilename) {
    String filename = "";
    String mediafile = newMovieFilename;

    switch (fanart) {
      case FANART:
        filename += "fanart";
        break;
      case FILENAME_FANART:
        filename += mediafile.isEmpty() ? "" : mediafile + "-fanart";
        break;
      case FILENAME_FANART2:
        filename += mediafile.isEmpty() ? "" : mediafile + ".fanart";
        break;
      default:
        filename = "";
        break;
    }
    return filename;
  }

  /**
   * all supported poster names. (without path!)
   *
   * @param poster
   *          the poster
   * @param movie
   *          the movie
   * @return the poster filename
   */
  public static String getBasePosterFilename(MoviePosterNaming poster, Movie movie) {
    List<MediaFile> mfs = movie.getMediaFiles(MediaFileType.VIDEO);
    if (mfs != null && mfs.size() > 0) {
      return getBasePosterFilename(poster, movie.getVideoBasenameWithoutStacking());
    }
    else {
      return getBasePosterFilename(poster, ""); // no video files
    }
  }

  public static String getBasePosterFilename(MoviePosterNaming poster, String newMovieFilename) {
    String filename = "";
    String mediafile = newMovieFilename;

    switch (poster) {
      case FILENAME_POSTER:
        filename += mediafile.isEmpty() ? "" : mediafile + "-poster";
        break;
      case FILENAME:
        filename += mediafile.isEmpty() ? "" : mediafile;
        break;
      case MOVIE:
        filename += "movie";
        break;
      case POSTER:
        filename += "poster";
        break;
      case FOLDER:
        filename += "folder";
        break;
      default:
        filename = "";
        break;
    }

    return filename;
  }

  /**
   * all supported banner names. (without path!)
   *
   * @param banner
   *          the banner
   * @param movie
   *          the movie
   * @return the banner filename
   */
  public static String getBaseBannerFilename(MovieBannerNaming banner, Movie movie) {
    List<MediaFile> mfs = movie.getMediaFiles(MediaFileType.VIDEO);
    if (mfs != null && mfs.size() > 0) {
      return getBaseBannerFilename(banner, movie.getVideoBasenameWithoutStacking());
    }
    else {
      return getBaseBannerFilename(banner, ""); // no video files
    }
  }

  public static String getBaseBannerFilename(MovieBannerNaming banner, String newMovieFilename) {
    String filename = "";
    String mediafile = newMovieFilename;

    switch (banner) {
      case FILENAME_BANNER:
        filename += mediafile.isEmpty() ? "" : mediafile + "-banner";
        break;
      case BANNER:
        filename += "banner";
        break;
      default:
        filename = "";
        break;
    }

    return filename;
  }

  /**
   * all supported clearart names. (without path!)
   *
   * @param clearart
   *          the clearart
   * @param movie
   *          the movie
   * @return the clearart filename
   */
  public static String getBaseClearartFilename(MovieClearartNaming clearart, Movie movie) {
    List<MediaFile> mfs = movie.getMediaFiles(MediaFileType.VIDEO);
    if (mfs != null && mfs.size() > 0) {
      return getBaseClearartFilename(clearart, movie.getVideoBasenameWithoutStacking());
    }
    else {
      return getBaseClearartFilename(clearart, ""); // no video files
    }
  }

  public static String getBaseClearartFilename(MovieClearartNaming clearart, String newMovieFilename) {
    String filename = "";
    String mediafile = newMovieFilename;

    switch (clearart) {
      case FILENAME_CLEARART:
        filename += mediafile.isEmpty() ? "" : mediafile + "-clearart";
        break;
      case CLEARART:
        filename += "clearart";
        break;
      default:
        filename = "";
        break;
    }

    return filename;
  }

  /**
   * all supported discart names. (without path!)
   *
   * @param discart
   *          the discart
   * @param movie
   *          the movie
   * @return the discart filename
   */
  public static String getBaseDiscartFilename(MovieDiscartNaming discart, Movie movie) {
    List<MediaFile> mfs = movie.getMediaFiles(MediaFileType.VIDEO);
    if (mfs != null && mfs.size() > 0) {
      return getBaseDiscartFilename(discart, movie.getVideoBasenameWithoutStacking());
    }
    else {
      return getBaseDiscartFilename(discart, ""); // no video files
    }
  }

  public static String getBaseDiscartFilename(MovieDiscartNaming discart, String newMovieFilename) {
    String filename = "";
    String mediafile = newMovieFilename;

    switch (discart) {
      case FILENAME_DISC:
        filename += mediafile.isEmpty() ? "" : mediafile + "-disc";
        break;
      case DISC:
        filename += "disc";
        break;
      default:
        filename = "";
        break;
    }

    return filename;
  }

  /**
   * all supported logo names. (without path!)
   *
   * @param logo
   *          the logo
   * @param movie
   *          the movie
   * @return the logo filename
   */
  public static String getBaseLogoFilename(MovieLogoNaming logo, Movie movie) {
    List<MediaFile> mfs = movie.getMediaFiles(MediaFileType.VIDEO);
    if (mfs != null && mfs.size() > 0) {
      return getBaseLogoFilename(logo, movie.getVideoBasenameWithoutStacking());
    }
    else {
      return getBaseLogoFilename(logo, ""); // no video files
    }
  }

  public static String getBaseLogoFilename(MovieLogoNaming logo, String newMovieFilename) {
    String filename = "";
    String mediafile = newMovieFilename;

    switch (logo) {
      case FILENAME_LOGO:
        filename += mediafile.isEmpty() ? "" : mediafile + "-logo";
        break;
      case LOGO:
        filename += "logo";
        break;
      default:
        filename = "";
        break;
    }

    return filename;
  }

  /**
   * all supported clearlogo names. (without path!)
   *
   * @param clearlogo
   *          the clearlogo
   * @param movie
   *          the movie
   * @return the clearlogo filename
   */
  public static String getBaseClearlogoFilename(MovieClearlogoNaming clearlogo, Movie movie) {
    List<MediaFile> mfs = movie.getMediaFiles(MediaFileType.VIDEO);
    if (mfs != null && mfs.size() > 0) {
      return getBaseClearlogoFilename(clearlogo, movie.getVideoBasenameWithoutStacking());
    }
    else {
      return getBaseClearlogoFilename(clearlogo, ""); // no video files
    }
  }

  public static String getBaseClearlogoFilename(MovieClearlogoNaming clearlogo, String newMovieFilename) {
    String filename = "";
    String mediafile = newMovieFilename;

    switch (clearlogo) {
      case FILENAME_CLEARLOGO:
        filename += mediafile.isEmpty() ? "" : mediafile + "-clearlogo";
        break;
      case CLEARLOGO:
        filename += "clearlogo";
        break;
      default:
        filename = "";
        break;
    }

    return filename;
  }

  /**
   * all supported thumb names. (without path!)
   *
   * @param thumb
   *          the thumb
   * @param movie
   *          the movie
   * @return the thumb filename
   */
  public static String getBaseThumbFilename(MovieThumbNaming thumb, Movie movie) {
    List<MediaFile> mfs = movie.getMediaFiles(MediaFileType.VIDEO);
    if (mfs != null && mfs.size() > 0) {
      return getBaseThumbFilename(thumb, movie.getVideoBasenameWithoutStacking());
    }
    else {
      return getBaseThumbFilename(thumb, ""); // no video files
    }
  }

  public static String getBaseThumbFilename(MovieThumbNaming thumb, String newMovieFilename) {
    String filename = "";
    String mediafile = newMovieFilename;

    switch (thumb) {
      case FILENAME_THUMB:
        filename += mediafile.isEmpty() ? "" : mediafile + "-thumb";
        break;
      case THUMB:
        filename += "thumb";
        break;
      case FILENAME_LANDSCAPE:
        filename += mediafile.isEmpty() ? "" : mediafile + "-landscape";
        break;
      case LANDSCAPE:
        filename += "landscape";
        break;
      default:
        filename = "";
        break;
    }

    return filename;
  }

  /**
   * set the found artwork for the given movie
   * 
   * @param movie
   *          the movie to set the artwork for
   * @param artwork
   *          a list of all artworks to be set
   */
  public static void setArtwork(Movie movie, List<MediaArtwork> artwork) {
    // sort artwork once again (langu/rating)
    Collections.sort(artwork, new MediaArtwork.MediaArtworkComparator(MovieModuleManager.SETTINGS.getScraperLanguage().getLanguage()));

    // poster
    setBestPoster(movie, artwork);

    // fanart
    setBestFanart(movie, artwork);

    // works now for single & multimovie
    setBestArtwork(movie, artwork, MediaArtworkType.LOGO, !MovieModuleManager.SETTINGS.getLogoFilenames().isEmpty());
    setBestArtwork(movie, artwork, MediaArtworkType.CLEARLOGO, !MovieModuleManager.SETTINGS.getClearlogoFilenames().isEmpty());
    setBestArtwork(movie, artwork, MediaArtworkType.CLEARART, !MovieModuleManager.SETTINGS.getClearartFilenames().isEmpty());
    setBestArtwork(movie, artwork, MediaArtworkType.BANNER, !MovieModuleManager.SETTINGS.getBannerFilenames().isEmpty());
    setBestArtwork(movie, artwork, MediaArtworkType.THUMB, !MovieModuleManager.SETTINGS.getThumbFilenames().isEmpty());
    setBestArtwork(movie, artwork, MediaArtworkType.DISC, !MovieModuleManager.SETTINGS.getDiscartFilenames().isEmpty());

    // extrathumbs
    List<String> extrathumbs = new ArrayList<>();
    if (MovieModuleManager.SETTINGS.isImageExtraThumbs() && MovieModuleManager.SETTINGS.getImageExtraThumbsCount() > 0) {
      for (MediaArtwork art : artwork) {
        // only get artwork in desired resolution
        if (art.getType() == MediaArtworkType.BACKGROUND && art.getSizeOrder() == MovieModuleManager.SETTINGS.getImageFanartSize().getOrder()) {
          extrathumbs.add(art.getDefaultUrl());
          if (extrathumbs.size() >= MovieModuleManager.SETTINGS.getImageExtraThumbsCount()) {
            break;
          }
        }
      }
      movie.setExtraThumbs(extrathumbs);
      if (extrathumbs.size() > 0) {
        if (!movie.isMultiMovieDir()) {
          downloadArtwork(movie, MediaFileType.EXTRATHUMB);
        }
      }
    }

    // extrafanarts
    List<String> extrafanarts = new ArrayList<>();
    if (MovieModuleManager.SETTINGS.isImageExtraFanart() && MovieModuleManager.SETTINGS.getImageExtraFanartCount() > 0) {
      for (MediaArtwork art : artwork) {
        // only get artwork in desired resolution
        if (art.getType() == MediaArtworkType.BACKGROUND && art.getSizeOrder() == MovieModuleManager.SETTINGS.getImageFanartSize().getOrder()) {
          extrafanarts.add(art.getDefaultUrl());
          if (extrafanarts.size() >= MovieModuleManager.SETTINGS.getImageExtraFanartCount()) {
            break;
          }
        }
      }
      movie.setExtraFanarts(extrafanarts);
      if (extrafanarts.size() > 0) {
        if (!movie.isMultiMovieDir()) {
          downloadArtwork(movie, MediaFileType.EXTRAFANART);
        }
      }
    }

    // update DB
    movie.saveToDb();
  }

  /*
   * find the "best" poster in the list of artwork, assign it to the movie and download it
   */
  private static void setBestPoster(Movie movie, List<MediaArtwork> artwork) {
    int preferredSizeOrder = MovieModuleManager.SETTINGS.getImagePosterSize().getOrder();
    String preferredLanguage = MovieModuleManager.SETTINGS.getScraperLanguage().getLanguage();

    MediaArtwork foundPoster = null;

    if (MovieModuleManager.SETTINGS.isImageLanguagePriority()) {
      // language has priority over size.
      // first run: find it with the preferred size
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.POSTER && art.getLanguage().equals(preferredLanguage) && art.getSizeOrder() == preferredSizeOrder) {
          foundPoster = art;
          break;
        }
      }

      // second run: try to find a poster in the right language with size +/- 1 (order)
      if (foundPoster == null) {
        int minOrder = preferredSizeOrder - 1;
        int maxOrder = preferredSizeOrder + 1;
        for (MediaArtwork art : artwork) {
          if (art.getType() == MediaArtworkType.POSTER && art.getLanguage().equals(preferredLanguage)
              && (art.getSizeOrder() == minOrder || art.getSizeOrder() == maxOrder)) {
            foundPoster = art;
            break;
          }
        }
      }
    }
    else {
      // size has priority over language
      for (MediaArtwork art : artwork) {
        // only get artwork in desired resolution
        if (art.getType() == MediaArtworkType.POSTER && art.getSizeOrder() == preferredSizeOrder) {
          foundPoster = art;
          break;
        }
      }
    }

    // final run: if there has nothing been found take the first one
    if (foundPoster == null) {
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.POSTER) {
          foundPoster = art;
          break;
        }
      }
    }

    // assign and download the poster
    if (foundPoster != null) {
      movie.setArtworkUrl(foundPoster.getDefaultUrl(), MediaFileType.POSTER);

      // did we get the tmdbid from artwork?
      if (movie.getTmdbId() == 0 && foundPoster.getTmdbId() > 0) {
        movie.setTmdbId(foundPoster.getTmdbId());
      }
      downloadArtwork(movie, MediaFileType.POSTER);
    }
  }

  /*
   * find the "best" fanart in the list of artwork, assign it to the movie and download it
   */
  private static void setBestFanart(Movie movie, List<MediaArtwork> artwork) {
    int preferredSizeOrder = MovieModuleManager.SETTINGS.getImageFanartSize().getOrder();
    String preferredLanguage = MovieModuleManager.SETTINGS.getScraperLanguage().getLanguage();

    MediaArtwork foundfanart = null;

    if (MovieModuleManager.SETTINGS.isImageLanguagePriority()) {
      // language has priority over size.
      // first run: find it with the preferred size
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.BACKGROUND && art.getLanguage().equals(preferredLanguage) && art.getSizeOrder() == preferredSizeOrder) {
          foundfanart = art;
          break;
        }
      }

      // second run: try to find a fanart in the right language with size +/- 1 (order)
      if (foundfanart == null) {
        int minOrder = preferredSizeOrder - 1;
        int maxOrder = preferredSizeOrder + 1;
        for (MediaArtwork art : artwork) {
          if (art.getType() == MediaArtworkType.BACKGROUND && art.getLanguage().equals(preferredLanguage)
              && (art.getSizeOrder() == minOrder || art.getSizeOrder() == maxOrder)) {
            foundfanart = art;
            break;
          }
        }
      }
    }
    else {
      // size has priority over language
      for (MediaArtwork art : artwork) {
        // only get artwork in desired resolution
        if (art.getType() == MediaArtworkType.BACKGROUND && art.getSizeOrder() == preferredSizeOrder) {
          foundfanart = art;
          break;
        }
      }
    }

    // final run: if there has nothing been found take the first one
    if (foundfanart == null) {
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.BACKGROUND) {
          foundfanart = art;
          break;
        }
      }
    }

    // assign and download the fanart
    if (foundfanart != null) {
      movie.setArtworkUrl(foundfanart.getDefaultUrl(), MediaFileType.FANART);

      // did we get the tmdbid from artwork?
      if (movie.getTmdbId() == 0 && foundfanart.getTmdbId() > 0) {
        movie.setTmdbId(foundfanart.getTmdbId());
      }
      downloadArtwork(movie, MediaFileType.FANART);
    }
  }

  /**
   * choose the best artwork for this movie
   * 
   * @param movie
   *          our movie
   * @param artwork
   *          the artwork list
   * @param type
   *          the type to download
   * @param download
   *          indicates, whether to download and add, OR JUST SAVE THE URL for a later download
   */
  private static void setBestArtwork(Movie movie, List<MediaArtwork> artwork, MediaArtworkType type, boolean download) {
    for (MediaArtwork art : artwork) {
      if (art.getType() == type) {
        movie.setArtworkUrl(art.getDefaultUrl(), MediaFileType.getMediaFileType(type));
        if (download) {
          downloadArtwork(movie, MediaFileType.getMediaFileType(type));
        }
        break;
      }
    }
  }
}
