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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.MediaEntityImageFetcherTask;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
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

      case EXTRAFANART:
      case EXTRATHUMB:
      case LOGO:
      case CLEARLOGO:
      case BANNER:
      case CLEARART:
      case THUMB:
      case DISCART:
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
            if (!MovieModuleManager.SETTINGS.getMovieFanartFilenames().isEmpty() || force) {
              downloadFanart(movie);
            }
            break;
          case POSTER:
            if (!MovieModuleManager.SETTINGS.getMoviePosterFilenames().isEmpty() || force) {
              downloadPoster(movie);
            }
            break;
          case BANNER:
            if (MovieModuleManager.SETTINGS.isImageBanner() || force) {
              downloadExtraArtwork(movie, mft);
            }
            break;
          case CLEARART:
            if (MovieModuleManager.SETTINGS.isImageClearart() || force) {
              downloadExtraArtwork(movie, mft);
            }
            break;
          case DISCART:
            if (MovieModuleManager.SETTINGS.isImageDiscart() || force) {
              downloadExtraArtwork(movie, mft);
            }
            break;
          case LOGO:
          case CLEARLOGO:
            if (MovieModuleManager.SETTINGS.isImageLogo() || force) {
              downloadExtraArtwork(movie, mft);
            }
            break;
          case THUMB:
            if (MovieModuleManager.SETTINGS.isImageThumb() || force) {
              downloadExtraArtwork(movie, mft);
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
    if (MovieModuleManager.SETTINGS.getMovieFanartFilenames().isEmpty()) {
      return fanartnames;
    }
    if (movie.isMultiMovieDir()) {
      if (MovieModuleManager.SETTINGS.getMovieFanartFilenames().contains(MovieFanartNaming.FILENAME_FANART)) {
        fanartnames.add(MovieFanartNaming.FILENAME_FANART);
      }
      if (MovieModuleManager.SETTINGS.getMovieFanartFilenames().contains(MovieFanartNaming.FILENAME_FANART2)) {
        fanartnames.add(MovieFanartNaming.FILENAME_FANART2);
      }
      if (fanartnames.isEmpty() || !MovieModuleManager.SETTINGS.getMovieFanartFilenames().isEmpty()) {
        fanartnames.add(MovieFanartNaming.FILENAME_FANART);
      }
    }
    else if (movie.isDisc()) {
      fanartnames.add(MovieFanartNaming.FANART);
    }
    else {
      fanartnames = MovieModuleManager.SETTINGS.getMovieFanartFilenames();
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

      String ext = FilenameUtils.getExtension(fanartUrl);
      if (StringUtils.isBlank(ext)) {
        // no extension? fall back to jpg
        ext = "jpg";
      }
      else if ("tbn".equals(ext)) {
        ext = "jpg";
      }
      String filename = baseFilename + "." + ext;

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
    if (MovieModuleManager.SETTINGS.getMoviePosterFilenames().isEmpty()) {
      return posternames;
    }
    if (movie.isMultiMovieDir()) {
      if (MovieModuleManager.SETTINGS.getMoviePosterFilenames().contains(MoviePosterNaming.FILENAME_POSTER)) {
        posternames.add(MoviePosterNaming.FILENAME_POSTER);
      }
      if (MovieModuleManager.SETTINGS.getMoviePosterFilenames().contains(MoviePosterNaming.FILENAME)) {
        posternames.add(MoviePosterNaming.FILENAME);
      }
      if (posternames.isEmpty() && !MovieModuleManager.SETTINGS.getMoviePosterFilenames().isEmpty()) {
        posternames.add(MoviePosterNaming.FILENAME_POSTER);
      }
    }
    else if (movie.isDisc()) {
      if (MovieModuleManager.SETTINGS.getMoviePosterFilenames().contains(MoviePosterNaming.FOLDER)) {
        posternames.add(MoviePosterNaming.FOLDER);
      }
      if (MovieModuleManager.SETTINGS.getMoviePosterFilenames().contains(MoviePosterNaming.POSTER)
          || (posternames.isEmpty() && !MovieModuleManager.SETTINGS.getMoviePosterFilenames().isEmpty())) {
        posternames.add(MoviePosterNaming.POSTER);
      }
    }
    else {
      posternames.addAll(MovieModuleManager.SETTINGS.getMoviePosterFilenames());
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

      String ext = FilenameUtils.getExtension(posterUrl);
      if (StringUtils.isBlank(ext)) {
        // no extension? fall back to jpg
        ext = "jpg";
      }
      else if ("tbn".equals(ext)) {
        ext = "jpg";
      }
      String filename = baseFilename + "." + ext;

      if (++i == 1) {
        firstImage = true;
      }

      // get image in thread
      MediaEntityImageFetcherTask task = new MediaEntityImageFetcherTask(movie, posterUrl, MediaArtworkType.POSTER, filename, firstImage);
      TmmTaskManager.getInstance().addImageDownloadTask(task);
    }
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
      return getBaseFanartFilename(fanart, movie, movie.getVideoBasenameWithoutStacking());
    }
    else {
      return getBaseFanartFilename(fanart, movie, ""); // no video files
    }
  }

  public static String getBaseFanartFilename(MovieFanartNaming fanart, Movie movie, String newMovieFilename) {
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
      case MOVIENAME_FANART:
        filename += movie.getTitle() + "-fanart";
        break;
      default:
        filename = "";
        break;
    }
    return filename;
  }

  /**
   * all XBMC supported poster names. (without path!)
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
      return getBasePosterFilename(poster, movie, movie.getVideoBasenameWithoutStacking());
    }
    else {
      return getBasePosterFilename(poster, movie, ""); // no video files
    }
  }

  public static String getBasePosterFilename(MoviePosterNaming poster, Movie movie, String newMovieFilename) {
    String filename = "";
    String mediafile = newMovieFilename;

    switch (poster) {
      case MOVIENAME_POSTER:
        filename += movie.getTitle();
        break;
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
   * set the found artwork for the given movie
   * 
   * @param movie
   *          the movie to set the artwork for
   * @param artwork
   *          a list of all artworks to be set
   */
  public static void setArtwork(Movie movie, List<MediaArtwork> artwork) {
    // sort artwork once again (langu/rating)
    Collections.sort(artwork, new MediaArtwork.MediaArtworkComparator(MovieModuleManager.SETTINGS.getScraperLanguage().name()));

    // poster
    setBestPoster(movie, artwork);

    // fanart
    setBestFanart(movie, artwork);

    // works now for single & multimovie
    setBestArtwork(movie, artwork, MediaArtworkType.LOGO, MovieModuleManager.SETTINGS.isImageLogo());
    setBestArtwork(movie, artwork, MediaArtworkType.CLEARLOGO, MovieModuleManager.SETTINGS.isImageLogo());
    setBestArtwork(movie, artwork, MediaArtworkType.CLEARART, MovieModuleManager.SETTINGS.isImageClearart());
    setBestArtwork(movie, artwork, MediaArtworkType.BANNER, MovieModuleManager.SETTINGS.isImageBanner());
    setBestArtwork(movie, artwork, MediaArtworkType.THUMB, MovieModuleManager.SETTINGS.isImageThumb());
    setBestArtwork(movie, artwork, MediaArtworkType.DISC, MovieModuleManager.SETTINGS.isImageDiscart());

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
    String preferredLanguage = MovieModuleManager.SETTINGS.getScraperLanguage().name();

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
    String preferredLanguage = MovieModuleManager.SETTINGS.getScraperLanguage().name();

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
