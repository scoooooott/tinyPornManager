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
package org.tinymediamanager.core.movie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.MediaEntityImageFetcherTask;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.tasks.MovieExtraImageFetcher;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;

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

  private static void downloadFanart(Movie movie) {
    String fanartUrl = movie.getArtworkUrl(MediaFileType.FANART);
    if (StringUtils.isBlank(fanartUrl)) {
      return;
    }

    int i = 0;
    List<MovieFanartNaming> fanartnames = new ArrayList<MovieFanartNaming>();
    if (movie.isMultiMovieDir()) {
      // Fixate the name regardless of setting
      fanartnames.add(MovieFanartNaming.FILENAME_FANART_JPG);
      fanartnames.add(MovieFanartNaming.FILENAME_FANART_PNG);
    }
    else if (movie.isDisc()) {
      // override fanart naming for disc files
      fanartnames.add(MovieFanartNaming.FANART_JPG);
      fanartnames.add(MovieFanartNaming.FANART_PNG);
    }
    else {
      fanartnames = MovieModuleManager.MOVIE_SETTINGS.getMovieFanartFilenames();
    }
    for (MovieFanartNaming name : fanartnames) {
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

      if (++i == 1) {
        firstImage = true;
      }

      // get image in thread
      MediaEntityImageFetcherTask task = new MediaEntityImageFetcherTask(movie, fanartUrl, MediaArtworkType.BACKGROUND, filename, firstImage);
      TmmTaskManager.getInstance().addImageDownloadTask(task);
    }
  }

  private static void downloadPoster(Movie movie) {
    String posterUrl = movie.getArtworkUrl(MediaFileType.POSTER);

    int i = 0;
    List<MoviePosterNaming> posternames = new ArrayList<MoviePosterNaming>();
    if (movie.isMultiMovieDir()) {
      // Fixate the name regardless of setting
      posternames.add(MoviePosterNaming.FILENAME_POSTER_JPG);
      posternames.add(MoviePosterNaming.FILENAME_POSTER_PNG);
    }
    else if (movie.isDisc()) {
      // override poster naming for disc files - allowed is poster.jpg/png or folder.jpg/png;
      // crosscheck with settings
      if (MovieModuleManager.MOVIE_SETTINGS.getMoviePosterFilenames().contains(MoviePosterNaming.FOLDER_JPG)) {
        posternames.add(MoviePosterNaming.FOLDER_JPG);
        posternames.add(MoviePosterNaming.FOLDER_PNG);
      }

      if (MovieModuleManager.MOVIE_SETTINGS.getMoviePosterFilenames().contains(MoviePosterNaming.POSTER_JPG) || posternames.isEmpty()) {
        posternames.add(MoviePosterNaming.POSTER_JPG);
        posternames.add(MoviePosterNaming.POSTER_PNG);
      }
    }
    else {
      posternames = MovieModuleManager.MOVIE_SETTINGS.getMoviePosterFilenames();
    }
    for (MoviePosterNaming name : posternames) {
      boolean firstImage = false;
      String filename = getPosterFilename(name, movie);

      // only store .png as png and .jpg as jpg
      String generatedFiletype = FilenameUtils.getExtension(filename);
      String providedFiletype = FilenameUtils.getExtension(posterUrl);
      if (!generatedFiletype.equals(providedFiletype)) {
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
      return getFanartFilename(fanart, movie, mfs.get(0).getFilename());
    }
    else {
      return getFanartFilename(fanart, movie, ""); // no video files
    }
  }

  public static String getFanartFilename(MovieFanartNaming fanart, Movie movie, String newMovieFilename) {
    String filename = "";
    String mediafile = Utils.cleanStackingMarkers(FilenameUtils.getBaseName(newMovieFilename));

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
      return getPosterFilename(poster, movie, mfs.get(0).getFilename());
    }
    else {
      return getPosterFilename(poster, movie, ""); // no video files
    }
  }

  public static String getPosterFilename(MoviePosterNaming poster, Movie movie, String newMovieFilename) {
    String filename = "";
    String mediafile = Utils.cleanStackingMarkers(FilenameUtils.getBaseName(newMovieFilename));

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
   * @param artwork
   */
  public static void setArtwork(Movie movie, List<MediaArtwork> artwork) {
    // sort artwork once again (langu/rating)
    Collections.sort(artwork, new MediaArtwork.MediaArtworkComparator(MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage()));

    // poster
    setBestPoster(movie, artwork);

    // fanart
    setBestFanart(movie, artwork);

    if (!movie.isMultiMovieDir()) {
      // logo
      if (MovieModuleManager.MOVIE_SETTINGS.isImageLogo()) {
        setBestArtwork(movie, artwork, MediaArtworkType.LOGO);
      }
      // clearart
      if (MovieModuleManager.MOVIE_SETTINGS.isImageClearart()) {
        setBestArtwork(movie, artwork, MediaArtworkType.CLEARART);
      }
      // banner
      if (MovieModuleManager.MOVIE_SETTINGS.isImageBanner()) {
        setBestArtwork(movie, artwork, MediaArtworkType.BANNER);
      }
      // thumb
      if (MovieModuleManager.MOVIE_SETTINGS.isImageThumb()) {
        setBestArtwork(movie, artwork, MediaArtworkType.THUMB);
      }
      // disc art
      if (MovieModuleManager.MOVIE_SETTINGS.isImageDiscart()) {
        setBestArtwork(movie, artwork, MediaArtworkType.DISC);
      }

      // extrathumbs
      List<String> extrathumbs = new ArrayList<String>();
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
          downloadArtwork(movie, MediaFileType.EXTRATHUMB);
        }
      }

      // extrafanarts
      List<String> extrafanarts = new ArrayList<String>();
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
    String preferredLanguage = MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage().name();

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
      movie.setPosterUrl(foundPoster.getDefaultUrl());

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
    String preferredLanguage = MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage().name();

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
      movie.setFanartUrl(foundfanart.getDefaultUrl());

      // did we get the tmdbid from artwork?
      if (movie.getTmdbId() == 0 && foundfanart.getTmdbId() > 0) {
        movie.setTmdbId(foundfanart.getTmdbId());
      }
      downloadArtwork(movie, MediaFileType.FANART);
    }
  }

  /*
   * choose the best artwork for this movie
   */
  private static void setBestArtwork(Movie movie, List<MediaArtwork> artwork, MediaArtworkType type) {
    for (MediaArtwork art : artwork) {
      if (art.getType() == type) {
        movie.setArtworkUrl(art.getDefaultUrl(), MediaFileType.getMediaFileType(type));
        downloadArtwork(movie, MediaFileType.getMediaFileType(type));
        break;
      }
    }
  }
}
