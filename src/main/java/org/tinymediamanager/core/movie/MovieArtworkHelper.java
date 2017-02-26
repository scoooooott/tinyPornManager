/*
 * Copyright 2012 - 2017 Manuel Laggner
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
            if (!MovieModuleManager.MOVIE_SETTINGS.getMovieFanartFilenames().isEmpty() || force) {
              downloadFanart(movie);
            }
            break;
          case POSTER:
            if (!MovieModuleManager.MOVIE_SETTINGS.getMoviePosterFilenames().isEmpty() || force) {
              downloadPoster(movie);
            }
            break;
          case BANNER:
            if (MovieModuleManager.MOVIE_SETTINGS.isImageBanner() || force) {
              downloadExtraArtwork(movie, mft);
            }
            break;
          case CLEARART:
            if (MovieModuleManager.MOVIE_SETTINGS.isImageClearart() || force) {
              downloadExtraArtwork(movie, mft);
            }
            break;
          case DISCART:
            if (MovieModuleManager.MOVIE_SETTINGS.isImageDiscart() || force) {
              downloadExtraArtwork(movie, mft);
            }
            break;
          case LOGO:
          case CLEARLOGO:
            if (MovieModuleManager.MOVIE_SETTINGS.isImageLogo() || force) {
              downloadExtraArtwork(movie, mft);
            }
            break;
          case THUMB:
            if (MovieModuleManager.MOVIE_SETTINGS.isImageThumb() || force) {
              downloadExtraArtwork(movie, mft);
            }
            break;
          case EXTRAFANART:
            if (MovieModuleManager.MOVIE_SETTINGS.isImageExtraFanart() || force) {
              downloadExtraArtwork(movie, mft);
            }
            break;
          case EXTRATHUMB:
            if (MovieModuleManager.MOVIE_SETTINGS.isImageExtraThumbs() || force) {
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
    Collections.sort(artwork, new MediaArtwork.MediaArtworkComparator(MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage().getLanguage()));

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
      setBestArtwork(movie, artwork, MediaArtworkType.LOGO, MovieModuleManager.MOVIE_SETTINGS.isImageLogo());
    }

    // clearlogo
    if (movie.getMediaFiles(MediaFileType.CLEARLOGO).isEmpty()) {
      setBestArtwork(movie, artwork, MediaArtworkType.CLEARLOGO, MovieModuleManager.MOVIE_SETTINGS.isImageLogo());
    }

    // clearart
    if (movie.getMediaFiles(MediaFileType.CLEARART).isEmpty()) {
      setBestArtwork(movie, artwork, MediaArtworkType.CLEARART, MovieModuleManager.MOVIE_SETTINGS.isImageClearart());
    }

    // banner
    if (movie.getMediaFiles(MediaFileType.BANNER).isEmpty()) {
      setBestArtwork(movie, artwork, MediaArtworkType.BANNER, MovieModuleManager.MOVIE_SETTINGS.isImageBanner());
    }

    // thumb
    if (movie.getMediaFiles(MediaFileType.THUMB).isEmpty()) {
      setBestArtwork(movie, artwork, MediaArtworkType.THUMB, MovieModuleManager.MOVIE_SETTINGS.isImageThumb());
    }

    // discart
    if (movie.getMediaFiles(MediaFileType.DISCART).isEmpty()) {
      setBestArtwork(movie, artwork, MediaArtworkType.DISC, MovieModuleManager.MOVIE_SETTINGS.isImageDiscart());
    }

    // extrathumbs
    List<String> extrathumbs = new ArrayList<>();
    if (movie.getMediaFiles(MediaFileType.EXTRATHUMB).isEmpty() && MovieModuleManager.MOVIE_SETTINGS.isImageExtraThumbs()
        && MovieModuleManager.MOVIE_SETTINGS.getImageExtraThumbsCount() > 0) {
      for (MediaArtwork art : artwork) {
        // only get artwork in desired resolution
        if (art.getType() == MediaArtworkType.BACKGROUND && art.getSizeOrder() == MovieModuleManager.MOVIE_SETTINGS.getImageFanartSize().getOrder()) {
          extrathumbs.add(art.getDefaultUrl());
          if (extrathumbs.size() >= MovieModuleManager.MOVIE_SETTINGS.getImageExtraThumbsCount()) {
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
    if (MovieModuleManager.MOVIE_SETTINGS.isImageExtraFanart() && MovieModuleManager.MOVIE_SETTINGS.getImageExtraFanartCount() > 0) {
      for (MediaArtwork art : artwork) {
        // only get artwork in desired resolution
        if (art.getType() == MediaArtworkType.BACKGROUND && art.getSizeOrder() == MovieModuleManager.MOVIE_SETTINGS.getImageFanartSize().getOrder()) {
          extrafanarts.add(art.getDefaultUrl());
          if (extrafanarts.size() >= MovieModuleManager.MOVIE_SETTINGS.getImageExtraFanartCount()) {
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
    if (!MovieModuleManager.MOVIE_SETTINGS.getMoviePosterFilenames().isEmpty() && movie.getMediaFiles(MediaFileType.POSTER).isEmpty()) {
      return true;
    }
    if (!MovieModuleManager.MOVIE_SETTINGS.getMovieFanartFilenames().isEmpty() && movie.getMediaFiles(MediaFileType.FANART).isEmpty()) {
      return true;
    }
    if (MovieModuleManager.MOVIE_SETTINGS.isImageBanner() && movie.getMediaFiles(MediaFileType.BANNER).isEmpty()) {
      return true;
    }
    if (MovieModuleManager.MOVIE_SETTINGS.isImageDiscart() && movie.getMediaFiles(MediaFileType.DISCART).isEmpty()) {
      return true;
    }
    if (MovieModuleManager.MOVIE_SETTINGS.isImageLogo() && movie.getMediaFiles(MediaFileType.LOGO).isEmpty()) {
      return true;
    }
    if (MovieModuleManager.MOVIE_SETTINGS.isImageLogo() && movie.getMediaFiles(MediaFileType.CLEARLOGO).isEmpty()) {
      return true;
    }
    if (MovieModuleManager.MOVIE_SETTINGS.isImageClearart() && movie.getMediaFiles(MediaFileType.CLEARART).isEmpty()) {
      return true;
    }
    if (MovieModuleManager.MOVIE_SETTINGS.isImageThumb() && movie.getMediaFiles(MediaFileType.THUMB).isEmpty()) {
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
   * @return List of MovieFanartNaming (can be empty!)
   */
  public static List<MovieFanartNaming> getFanartNamesForMovie(Movie movie) {
    List<MovieFanartNaming> fanartnames = new ArrayList<>();
    if (MovieModuleManager.MOVIE_SETTINGS.getMovieFanartFilenames().isEmpty()) {
      return fanartnames;
    }
    if (movie.isMultiMovieDir()) {
      if (MovieModuleManager.MOVIE_SETTINGS.getMovieFanartFilenames().contains(MovieFanartNaming.FILENAME_FANART_JPG)) {
        fanartnames.add(MovieFanartNaming.FILENAME_FANART_JPG);
        fanartnames.add(MovieFanartNaming.FILENAME_FANART_PNG);
      }
      if (MovieModuleManager.MOVIE_SETTINGS.getMovieFanartFilenames().contains(MovieFanartNaming.FILENAME_FANART2_JPG)) {
        fanartnames.add(MovieFanartNaming.FILENAME_FANART2_JPG);
        fanartnames.add(MovieFanartNaming.FILENAME_FANART2_PNG);
      }
      if (fanartnames.isEmpty()) {
        fanartnames.add(MovieFanartNaming.FILENAME_FANART_JPG);
        fanartnames.add(MovieFanartNaming.FILENAME_FANART_PNG);
      }
    }
    else if (movie.isDisc()) {
      fanartnames.add(MovieFanartNaming.FANART_JPG);
      fanartnames.add(MovieFanartNaming.FANART_PNG);
    }
    else {
      fanartnames = MovieModuleManager.MOVIE_SETTINGS.getMovieFanartFilenames();
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
      String filename = getFanartFilename(name, movie);

      // only store .png as png and .jpg as jpg; .tbn will be stored as .jpg
      String generatedFiletype = FilenameUtils.getExtension(filename);
      String providedFiletype = FilenameUtils.getExtension(fanartUrl);
      if ("tbn".equals(providedFiletype)) {
        providedFiletype = "jpg";
      }
      if (!generatedFiletype.equals(providedFiletype)) {
        continue;
      }

      if (StringUtils.isBlank(fanartUrl) || StringUtils.isBlank(filename)) {
        continue;
      }

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
   * @return list of MoviePosterNaming (can be empty!)
   */
  public static List<MoviePosterNaming> getPosterNamesForMovie(Movie movie) {
    List<MoviePosterNaming> posternames = new ArrayList<>();
    if (MovieModuleManager.MOVIE_SETTINGS.getMoviePosterFilenames().isEmpty()) {
      return posternames;
    }
    if (movie.isMultiMovieDir()) {
      if (MovieModuleManager.MOVIE_SETTINGS.getMoviePosterFilenames().contains(MoviePosterNaming.FILENAME_POSTER_JPG)) {
        posternames.add(MoviePosterNaming.FILENAME_POSTER_JPG);
        posternames.add(MoviePosterNaming.FILENAME_POSTER_PNG);
      }
      if (MovieModuleManager.MOVIE_SETTINGS.getMoviePosterFilenames().contains(MoviePosterNaming.FILENAME_JPG)) {
        posternames.add(MoviePosterNaming.FILENAME_JPG);
        posternames.add(MoviePosterNaming.FILENAME_PNG);
      }
      if (posternames.isEmpty()) {
        posternames.add(MoviePosterNaming.FILENAME_POSTER_JPG);
        posternames.add(MoviePosterNaming.FILENAME_POSTER_PNG);
      }
    }
    else if (movie.isDisc()) {
      if (MovieModuleManager.MOVIE_SETTINGS.getMoviePosterFilenames().contains(MoviePosterNaming.FOLDER_JPG)) {
        posternames.add(MoviePosterNaming.FOLDER_JPG);
        posternames.add(MoviePosterNaming.FOLDER_PNG);
      }
      if (MovieModuleManager.MOVIE_SETTINGS.getMoviePosterFilenames().contains(MoviePosterNaming.POSTER_JPG) || posternames.isEmpty()) {
        posternames.add(MoviePosterNaming.POSTER_JPG);
        posternames.add(MoviePosterNaming.POSTER_PNG);
      }
      if (posternames.isEmpty()) {
        posternames.add(MoviePosterNaming.FOLDER_JPG);
        posternames.add(MoviePosterNaming.FOLDER_PNG);
      }
    }
    else {
      posternames = MovieModuleManager.MOVIE_SETTINGS.getMoviePosterFilenames();
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
      String filename = getPosterFilename(name, movie);

      // only store .png as png and .jpg as jpg
      String generatedFiletype = FilenameUtils.getExtension(filename);
      String providedFiletype = FilenameUtils.getExtension(posterUrl);
      if (!generatedFiletype.equals(providedFiletype)) {
        continue;
      }

      if (StringUtils.isBlank(posterUrl) || StringUtils.isBlank(filename)) {
        continue;
      }

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
  public static String getFanartFilename(MovieFanartNaming fanart, Movie movie) {
    List<MediaFile> mfs = movie.getMediaFiles(MediaFileType.VIDEO);
    if (mfs != null && mfs.size() > 0) {
      return getFanartFilename(fanart, movie, movie.getVideoBasenameWithoutStacking());
    }
    else {
      return getFanartFilename(fanart, movie, ""); // no video files
    }
  }

  public static String getFanartFilename(MovieFanartNaming fanart, Movie movie, String newMovieFilename) {
    String filename = "";
    String mediafile = newMovieFilename;

    switch (fanart) {
      case FANART_PNG:
        filename += "fanart.png";
        break;
      case FANART_JPG:
        filename += "fanart.jpg";
        break;
      case FANART_TBN:
        filename += "fanart.tbn";
        break;
      case FILENAME_FANART_PNG:
        filename += mediafile.isEmpty() ? "" : mediafile + "-fanart.png";
        break;
      case FILENAME_FANART_JPG:
        filename += mediafile.isEmpty() ? "" : mediafile + "-fanart.jpg";
        break;
      case FILENAME_FANART2_PNG:
        filename += mediafile.isEmpty() ? "" : mediafile + ".fanart.png";
        break;
      case FILENAME_FANART2_JPG:
        filename += mediafile.isEmpty() ? "" : mediafile + ".fanart.jpg";
        break;
      case FILENAME_FANART_TBN:
        filename += mediafile.isEmpty() ? "" : mediafile + "-fanart.tbn";
        break;
      case MOVIENAME_FANART_PNG:
        filename += movie.getTitle() + "-fanart.png";
        break;
      case MOVIENAME_FANART_JPG:
        filename += movie.getTitle() + "-fanart.jpg";
        break;
      case MOVIENAME_FANART_TBN:
        filename += movie.getTitle() + "-fanart.tbn";
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
  public static String getPosterFilename(MoviePosterNaming poster, Movie movie) {
    List<MediaFile> mfs = movie.getMediaFiles(MediaFileType.VIDEO);
    if (mfs != null && mfs.size() > 0) {
      return getPosterFilename(poster, movie, movie.getVideoBasenameWithoutStacking());
    }
    else {
      return getPosterFilename(poster, movie, ""); // no video files
    }
  }

  public static String getPosterFilename(MoviePosterNaming poster, Movie movie, String newMovieFilename) {
    String filename = "";
    String mediafile = newMovieFilename;

    switch (poster) {
      case MOVIENAME_POSTER_PNG:
        filename += movie.getTitle() + ".png";
        break;
      case MOVIENAME_POSTER_JPG:
        filename += movie.getTitle() + ".jpg";
        break;
      case MOVIENAME_POSTER_TBN:
        filename += movie.getTitle() + ".tbn";
        break;
      case FILENAME_POSTER_PNG:
        filename += mediafile.isEmpty() ? "" : mediafile + "-poster.png";
        break;
      case FILENAME_POSTER_JPG:
        filename += mediafile.isEmpty() ? "" : mediafile + "-poster.jpg";
        break;
      case FILENAME_POSTER_TBN:
        filename += mediafile.isEmpty() ? "" : mediafile + "-poster.tbn";
        break;
      case FILENAME_PNG:
        filename += mediafile.isEmpty() ? "" : mediafile + ".png";
        break;
      case FILENAME_JPG:
        filename += mediafile.isEmpty() ? "" : mediafile + ".jpg";
        break;
      case FILENAME_TBN:
        filename += mediafile.isEmpty() ? "" : mediafile + ".tbn";
        break;
      case MOVIE_PNG:
        filename += "movie.png";
        break;
      case MOVIE_JPG:
        filename += "movie.jpg";
        break;
      case MOVIE_TBN:
        filename += "movie.tbn";
        break;
      case POSTER_PNG:
        filename += "poster.png";
        break;
      case POSTER_JPG:
        filename += "poster.jpg";
        break;
      case POSTER_TBN:
        filename += "poster.tbn";
        break;
      case FOLDER_PNG:
        filename += "folder.png";
        break;
      case FOLDER_JPG:
        filename += "folder.jpg";
        break;
      case FOLDER_TBN:
        filename += "folder.tbn";
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
    Collections.sort(artwork, new MediaArtwork.MediaArtworkComparator(MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage().getLanguage()));

    // poster
    setBestPoster(movie, artwork);

    // fanart
    setBestFanart(movie, artwork);

    // works now for single & multimovie
    setBestArtwork(movie, artwork, MediaArtworkType.LOGO, MovieModuleManager.MOVIE_SETTINGS.isImageLogo());
    setBestArtwork(movie, artwork, MediaArtworkType.CLEARLOGO, MovieModuleManager.MOVIE_SETTINGS.isImageLogo());
    setBestArtwork(movie, artwork, MediaArtworkType.CLEARART, MovieModuleManager.MOVIE_SETTINGS.isImageClearart());
    setBestArtwork(movie, artwork, MediaArtworkType.BANNER, MovieModuleManager.MOVIE_SETTINGS.isImageBanner());
    setBestArtwork(movie, artwork, MediaArtworkType.THUMB, MovieModuleManager.MOVIE_SETTINGS.isImageThumb());
    setBestArtwork(movie, artwork, MediaArtworkType.DISC, MovieModuleManager.MOVIE_SETTINGS.isImageDiscart());

    // extrathumbs
    List<String> extrathumbs = new ArrayList<>();
    if (MovieModuleManager.MOVIE_SETTINGS.isImageExtraThumbs() && MovieModuleManager.MOVIE_SETTINGS.getImageExtraThumbsCount() > 0) {
      for (MediaArtwork art : artwork) {
        // only get artwork in desired resolution
        if (art.getType() == MediaArtworkType.BACKGROUND && art.getSizeOrder() == MovieModuleManager.MOVIE_SETTINGS.getImageFanartSize().getOrder()) {
          extrathumbs.add(art.getDefaultUrl());
          if (extrathumbs.size() >= MovieModuleManager.MOVIE_SETTINGS.getImageExtraThumbsCount()) {
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
    if (MovieModuleManager.MOVIE_SETTINGS.isImageExtraFanart() && MovieModuleManager.MOVIE_SETTINGS.getImageExtraFanartCount() > 0) {
      for (MediaArtwork art : artwork) {
        // only get artwork in desired resolution
        if (art.getType() == MediaArtworkType.BACKGROUND && art.getSizeOrder() == MovieModuleManager.MOVIE_SETTINGS.getImageFanartSize().getOrder()) {
          extrafanarts.add(art.getDefaultUrl());
          if (extrafanarts.size() >= MovieModuleManager.MOVIE_SETTINGS.getImageExtraFanartCount()) {
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
    int preferredSizeOrder = MovieModuleManager.MOVIE_SETTINGS.getImagePosterSize().getOrder();
    String preferredLanguage = MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage().getLanguage();

    MediaArtwork foundPoster = null;

    if (MovieModuleManager.MOVIE_SETTINGS.isImageLanguagePriority()) {
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
    int preferredSizeOrder = MovieModuleManager.MOVIE_SETTINGS.getImageFanartSize().getOrder();
    String preferredLanguage = MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage().getLanguage();

    MediaArtwork foundfanart = null;

    if (MovieModuleManager.MOVIE_SETTINGS.isImageLanguagePriority()) {
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
      if (art.getType() == type && StringUtils.isNotBlank(art.getDefaultUrl())) {
        movie.setArtworkUrl(art.getDefaultUrl(), MediaFileType.getMediaFileType(type));
        if (download) {
          downloadArtwork(movie, MediaFileType.getMediaFileType(type));
        }
        break;
      }
    }
  }
}
