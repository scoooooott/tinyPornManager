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
package org.tinymediamanager.core.movie;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaFileHelper;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.ScraperMetadataConfig;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.tasks.MediaFileInformationFetcherTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.util.UrlUtil;

/**
 * The class MovieSetArtworkHelper. A helper class for managing movie set artwork
 *
 * @author Manuel Laggner
 */
public class MovieSetArtworkHelper {
  private static final Logger              LOGGER                      = LoggerFactory.getLogger(MovieSetArtworkHelper.class);

  private static final List<MediaFileType> SUPPORTED_ARTWORK_TYPES     = Arrays.asList(MediaFileType.POSTER, MediaFileType.FANART,
      MediaFileType.BANNER, MediaFileType.LOGO, MediaFileType.CLEARLOGO, MediaFileType.CLEARART, MediaFileType.THUMB, MediaFileType.DISC);
  private static final String[]            SUPPORTED_ARTWORK_FILETYPES = { "jpg", "png", "tbn" };

  private MovieSetArtworkHelper() {
    // hide default constructor for utility classes
  }

  /**
   * Update the artwork for a given {@link MovieSet}. This should be triggered after every {@link MovieSet} change like creating, adding movies,
   * removing movies
   * 
   * @param movieSet
   *          the movie set to update the artwork for
   */
  public static void updateArtwork(MovieSet movieSet) {
    // find artwork in the artwork dir
    findArtworkInArtworkFolder(movieSet);

    // find artwork in the movie dirs
    for (Movie movie : new ArrayList<>(movieSet.getMovies())) {
      findArtworkInMovieFolder(movieSet, movie);
    }
  }

  /**
   * cleanup the artwork for the given {@link MovieSet}. Move the artwork to the specified (settings) artwork folder or movie folder
   *
   * @param movieSet
   *          the {@link MovieSet} to do the cleanup for
   */
  public static void cleanupArtwork(MovieSet movieSet) {
    Path artworkFolder = getArtworkFolder();

    List<MediaFile> cleanup = new ArrayList<>();
    Set<MediaFile> needed = new TreeSet<>();

    // we can have 0..n different media files for every type (1 in every artwork folder type and 1 in every movie folder)
    // we will give any available image in our specified artwork folder priority over the ones from the movie files
    for (MediaFileType type : SUPPORTED_ARTWORK_TYPES) {
      List<MediaFile> mediaFiles = movieSet.getMediaFiles(type);
      cleanup.addAll(mediaFiles);

      // remove all 0 size files & not existing files
      mediaFiles = mediaFiles.stream().filter(mf -> {
        if (mf.getFilesize() == 0) {
          return false;
        }
        if (!mf.getFile().toFile().exists()) {
          return false;
        }
        return true;
      }).collect(Collectors.toList());

      if (mediaFiles.isEmpty()) {
        continue;
      }

      // search in the preferred artwork folder
      MediaFile artworkFile = findArtworkInPreferredArtworkFolder(movieSet, mediaFiles);
      if (artworkFile == null) {
        // search in the alternate artwork folder
        artworkFile = findArtworkInAlternateArtworkFolder(movieSet, mediaFiles);
      }
      if (artworkFile == null) {
        // search in movie folders
        artworkFile = findArtworkInMovieFolder(mediaFiles);
      }

      // now we _should_ have at least one artwork file; now distribute that to all other places (if needed)
      if (artworkFile != null) {
        // copy to the movie set folder
        if (artworkFolder != null) {
          // clone mf
          MediaFile newFile = new MediaFile(artworkFile);
          newFile.setFile(createArtworkPathInArtworkFolder(movieSet, type, artworkFile.getExtension()));
          boolean ok = MovieRenamer.copyFile(artworkFile.getFileAsPath(), newFile.getFileAsPath());
          if (ok) {
            needed.add(newFile);
          }
          else {
            // not copied/exception... keep it for now...
            needed.add(artworkFile);
          }
        }
        // copy to each movie folder
        if (MovieModuleManager.SETTINGS.isEnableMovieSetArtworkMovieFolder()) {
          String filename = "movieset-" + type.toString().toLowerCase(Locale.ROOT) + "." + artworkFile.getExtension();
          for (Movie movie : movieSet.getMovies()) {
            try {
              if (!movie.isMultiMovieDir()) {
                // clone mf
                MediaFile newFile = new MediaFile(artworkFile);
                newFile.setFile(movie.getPathNIO().resolve(filename));
                boolean ok = MovieRenamer.copyFile(artworkFile.getFileAsPath(), newFile.getFileAsPath());
                if (ok) {
                  needed.add(newFile);
                }
                else {
                  // not copied/exception... keep it for now...
                  needed.add(artworkFile);
                }
              }
            }
            catch (Exception e) {
              LOGGER.warn("could not write files", e);
            }
          }
        }
      }
    }

    // re-create the image cache on all new files
    for (MediaFile mf : needed) {
      ImageCache.cacheImageSilently(mf.getFile());
    }

    // and assign it to the movie set
    cleanup.forEach(movieSet::removeFromMediaFiles);
    movieSet.addToMediaFiles(new ArrayList<>(needed));

    // now remove all unnecessary ones
    for (int i = cleanup.size() - 1; i >= 0; i--) {
      MediaFile cl = cleanup.get(i);

      // cleanup files which are not needed
      if (!needed.contains(cl)) {
        LOGGER.debug("Deleting {}", cl.getFileAsPath());
        Utils.deleteFileSafely(cl.getFileAsPath());
        // also cleanup the cache for deleted mfs
        ImageCache.invalidateCachedImage(cl);

        // also remove emtpy folders
        try {
          if ((artworkFolder != null && !artworkFolder.equals(cl.getFile().getParent())) && Utils.isFolderEmpty(cl.getFile().getParent())) {
            LOGGER.debug("Deleting empty Directory {}", cl.getFileAsPath().getParent());
            Files.delete(cl.getFileAsPath().getParent()); // do not use recursive her
          }
        }
        catch (IOException e) {
          LOGGER.warn("could not search for empty dir: {}", e.getMessage());
        }
      }
    }

    movieSet.saveToDb();
  }

  /**
   * get a {@link Path} to the artwork folder
   * 
   * @return the {@link Path} to the artwork folder or null
   */
  private static Path getArtworkFolder() {
    String artworkFolder = MovieModuleManager.SETTINGS.getMovieSetArtworkFolder();
    if (!MovieModuleManager.SETTINGS.isEnableMovieSetArtworkFolder() || StringUtils.isBlank(artworkFolder)) {
      return null;
    }
    return Paths.get(artworkFolder);
  }

  /**
   * Create the path to the artwork file path inside the artwork folder. If the artwork folder is not activated this may return null
   * 
   * @param movieSet
   *          the movie set to create the file path for
   * @param type
   *          the artwork type
   * @param extension
   *          the extension of the artwork file
   * @return a {@link Path} to the artwork file
   */
  private static Path createArtworkPathInArtworkFolder(MovieSet movieSet, MediaFileType type, String extension) {
    Path artworkFolder = getArtworkFolder();
    if (artworkFolder == null) {
      return null;
    }

    String movieSetName = MovieRenamer.replaceInvalidCharacters(movieSet.getTitle());

    // also remove illegal separators
    movieSetName = MovieRenamer.replacePathSeparators(movieSetName);

    if (MovieModuleManager.SETTINGS.isMovieSetArtworkFolderStyleKodi()) {
      // <artwork folder>/<movie set name>/<type>.ext style
      return Paths.get(artworkFolder.toString(), movieSetName, type.toString().toLowerCase(Locale.ROOT) + "." + extension);
    }
    else {
      // <artwork folder>/<movie set name>-<type>.ext style
      return Paths.get(artworkFolder.toString(),
          MovieRenamer.replaceInvalidCharacters(movieSet.getTitle()) + "-" + type.name().toLowerCase(Locale.ROOT) + "." + extension);
    }
  }

  /**
   * find the artwork from the preferred artwork folder
   * 
   * @param movieSet
   *          the {@link MovieSet} to find the artwork for
   * @param mediaFiles
   *          a list of all available {@link MediaFile}s for this artwork type
   * @return the {@link MediaFile} in the preferred artwork folder or null
   */
  private static MediaFile findArtworkInPreferredArtworkFolder(MovieSet movieSet, List<MediaFile> mediaFiles) {
    Path artworkFolder = getArtworkFolder();
    if (artworkFolder == null) {
      return null;
    }

    for (MediaFile mediaFile : mediaFiles) {
      // a) is the artwork in the preferred artwork folder?
      if (MovieModuleManager.SETTINGS.isMovieSetArtworkFolderStyleKodi()) {
        String movieSetName = MovieRenamer.replaceInvalidCharacters(movieSet.getTitle());

        // also remove illegal separators
        movieSetName = MovieRenamer.replacePathSeparators(movieSetName);

        if (mediaFile.getFileAsPath().startsWith(artworkFolder.resolve(movieSetName))) {
          return mediaFile;
        }
      }
      else {
        if (mediaFile.getFileAsPath().startsWith(artworkFolder)) {
          return mediaFile;
        }
      }
    }

    return null;
  }

  /**
   * find the artwork from the alternate artwork folder
   *
   * @param movieSet
   *          the {@link MovieSet} to find the artwork for
   * @param mediaFiles
   *          a list of all available {@link MediaFile}s for this artwork type
   * @return the {@link MediaFile} in the alternate artwork folder or null
   */
  private static MediaFile findArtworkInAlternateArtworkFolder(MovieSet movieSet, List<MediaFile> mediaFiles) {
    if (!MovieModuleManager.SETTINGS.isEnableMovieSetArtworkFolder() || StringUtils.isBlank(MovieModuleManager.SETTINGS.getMovieSetArtworkFolder())) {
      return null;
    }

    Path artworkFolder = Paths.get(MovieModuleManager.SETTINGS.getMovieSetArtworkFolder());

    for (MediaFile mediaFile : mediaFiles) {
      // a) is the artwork in the preferred artwork folder?
      if (MovieModuleManager.SETTINGS.isMovieSetArtworkFolderStyleKodi()) {
        if (mediaFile.getFileAsPath().startsWith(artworkFolder)) {
          return mediaFile;
        }
      }
      else {
        String movieSetName = MovieRenamer.replaceInvalidCharacters(movieSet.getTitle());

        // also remove illegal separators
        movieSetName = MovieRenamer.replacePathSeparators(movieSetName);

        if (mediaFile.getFileAsPath().startsWith(artworkFolder.resolve(movieSetName))) {
          return mediaFile;
        }
      }
    }

    return null;
  }

  private static MediaFile findArtworkInMovieFolder(List<MediaFile> mediaFiles) {
    Path artworkFolder = getArtworkFolder();

    for (MediaFile mediaFile : mediaFiles) {
      if (artworkFolder == null || !mediaFile.getFileAsPath().startsWith(artworkFolder)) {
        return mediaFile;
      }
    }

    return null;
  }

  /**
   * find and assign movie set artwork in the artwork folder
   * 
   * @param movieSet
   *          the movie set to search artwork for
   */
  private static void findArtworkInArtworkFolder(MovieSet movieSet) {
    String artworkFolder = MovieModuleManager.SETTINGS.getMovieSetArtworkFolder();
    if (!MovieModuleManager.SETTINGS.isEnableMovieSetArtworkFolder() || StringUtils.isBlank(artworkFolder)) {
      return;
    }

    // here we have 2 kinds of file names in the movie set artwork folder:
    // a) the movie set artwork automator style: <artwork folder>/<movie set name>-<artwork type>.ext
    // b) Artwork Beef style: <artwork folder>/<movie set name>/<artwork type>.ext

    // a)
    for (MediaFileType type : SUPPORTED_ARTWORK_TYPES) {
      for (String fileType : SUPPORTED_ARTWORK_FILETYPES) {
        String movieSetName = MovieRenamer.replaceInvalidCharacters(movieSet.getTitle());

        // also remove illegal separators
        movieSetName = MovieRenamer.replacePathSeparators(movieSetName);

        String artworkFileName = movieSetName + "-" + type.name().toLowerCase(Locale.ROOT) + "." + fileType;
        Path artworkFile = Paths.get(artworkFolder, artworkFileName);
        if (Files.exists(artworkFile)) {
          // add this artwork to the media files
          MediaFile mediaFile = new MediaFile(artworkFile, type);
          TmmTaskManager.getInstance().addUnnamedTask(new MediaFileInformationFetcherTask(mediaFile, movieSet, false));
          movieSet.addToMediaFiles(mediaFile);
        }
      }
    }

    // b)
    for (MediaFileType type : SUPPORTED_ARTWORK_TYPES) {
      for (String fileType : SUPPORTED_ARTWORK_FILETYPES) {
        String movieSetName = MovieRenamer.replaceInvalidCharacters(movieSet.getTitle());

        // also remove illegal separators
        movieSetName = MovieRenamer.replacePathSeparators(movieSetName);

        String artworkFileName = type.name().toLowerCase(Locale.ROOT) + "." + fileType;
        Path artworkFile = Paths.get(artworkFolder, movieSetName, artworkFileName);
        if (Files.exists(artworkFile)) {
          // add this artwork to the media files
          MediaFile mediaFile = new MediaFile(artworkFile, type);
          TmmTaskManager.getInstance().addUnnamedTask(new MediaFileInformationFetcherTask(mediaFile, movieSet, false));
          movieSet.addToMediaFiles(mediaFile);
        }
      }
    }
  }

  /**
   * find and assign movie set artwork in the movie folder
   * 
   * @param movieSet
   *          the movie set to set the artwork for
   * @param movie
   *          the movie to look for the movie set artwork
   */
  private static void findArtworkInMovieFolder(MovieSet movieSet, Movie movie) {
    for (MediaFileType type : SUPPORTED_ARTWORK_TYPES) {
      // only if there is not yet any artwork assigned
      if (!movieSet.getMediaFiles(type).isEmpty()) {
        continue;
      }

      for (String fileType : SUPPORTED_ARTWORK_FILETYPES) {
        String artworkFileName = "movieset-" + type.name().toLowerCase(Locale.ROOT) + "." + fileType;
        Path artworkFile = movie.getPathNIO().resolve(artworkFileName);
        if (Files.exists(artworkFile)) {
          // add this artwork to the media files
          MediaFile mediaFile = new MediaFile(artworkFile, type);
          TmmTaskManager.getInstance().addUnnamedTask(new MediaFileInformationFetcherTask(mediaFile, movieSet, false));
          movieSet.addToMediaFiles(mediaFile);
        }
      }
    }
  }

  /**
   * set the found artwork for the given movie
   *
   * @param movieSet
   *          the movie set to set the artwork for
   * @param artwork
   *          a list of all artworks to be set
   * @param config
   *          the config which artwork to set
   */
  public static void setArtwork(MovieSet movieSet, List<MediaArtwork> artwork, List<MovieSetScraperMetadataConfig> config) {
    if (!ScraperMetadataConfig.containsAnyArtwork(config)) {
      return;
    }

    // sort artwork once again (langu/rating)
    artwork.sort(new MediaArtwork.MediaArtworkComparator(MovieModuleManager.SETTINGS.getImageScraperLanguage().getLanguage()));

    // poster
    if (config.contains(MovieSetScraperMetadataConfig.POSTER)) {
      setBestPoster(movieSet, artwork);
    }

    // fanart
    if (config.contains(MovieSetScraperMetadataConfig.FANART)) {
      setBestFanart(movieSet, artwork);
    }

    // works now for single & multimovie
    if (config.contains(MovieSetScraperMetadataConfig.LOGO)) {
      setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.LOGO);
    }
    if (config.contains(MovieSetScraperMetadataConfig.CLEARLOGO)) {
      setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.CLEARLOGO);
    }
    if (config.contains(MovieSetScraperMetadataConfig.CLEARART)) {
      setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.CLEARART);
    }
    if (config.contains(MovieSetScraperMetadataConfig.BANNER)) {
      setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.BANNER);
    }
    if (config.contains(MovieSetScraperMetadataConfig.THUMB)) {
      setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.THUMB);
    }
    if (config.contains(MovieSetScraperMetadataConfig.DISCART)) {
      setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.DISC);
    }

    // update DB
    movieSet.saveToDb();
  }

  /*
   * find the "best" poster in the list of artwork, assign it to the movie and download it
   */
  private static void setBestPoster(MovieSet movieSet, List<MediaArtwork> artwork) {
    int preferredSizeOrder = MovieModuleManager.SETTINGS.getImagePosterSize().getOrder();
    String preferredLanguage = MovieModuleManager.SETTINGS.getImageScraperLanguage().getLanguage();

    MediaArtwork foundPoster = null;

    if (MovieModuleManager.SETTINGS.isImageLanguagePriority()) {
      // language has priority over size.
      // first run: find it with the preferred size
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtwork.MediaArtworkType.POSTER && art.getLanguage().equals(preferredLanguage)
            && art.getSizeOrder() == preferredSizeOrder) {
          foundPoster = art;
          break;
        }
      }

      // second run: try to find a poster in the right language with size +/- 1 (order)
      if (foundPoster == null) {
        int minOrder = preferredSizeOrder - 1;
        int maxOrder = preferredSizeOrder + 1;
        for (MediaArtwork art : artwork) {
          if (art.getType() == MediaArtwork.MediaArtworkType.POSTER && art.getLanguage().equals(preferredLanguage)
              && (art.getSizeOrder() == minOrder || art.getSizeOrder() == maxOrder)) {
            foundPoster = art;
            break;
          }
        }
      }
    }

    if (foundPoster == null) {
      // nothing found or language has no preference
      for (MediaArtwork art : artwork) {
        // only get artwork in desired resolution
        if (art.getType() == MediaArtwork.MediaArtworkType.POSTER && art.getSizeOrder() == preferredSizeOrder) {
          foundPoster = art;
          break;
        }
      }
    }

    // final run: if there has nothing been found take the first one
    if (foundPoster == null) {
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtwork.MediaArtworkType.POSTER) {
          foundPoster = art;
          break;
        }
      }
    }

    // assign and download the poster
    if (foundPoster != null) {
      movieSet.setArtworkUrl(foundPoster.getDefaultUrl(), MediaFileType.POSTER);
    }
  }

  /*
   * find the "best" fanart in the list of artwork, assign it to the movie set and download it
   */
  private static void setBestFanart(MovieSet movieSet, List<MediaArtwork> artwork) {
    int preferredSizeOrder = MovieModuleManager.SETTINGS.getImageFanartSize().getOrder();
    String preferredLanguage = MovieModuleManager.SETTINGS.getImageScraperLanguage().getLanguage();

    MediaArtwork foundfanart = null;

    if (MovieModuleManager.SETTINGS.isImageLanguagePriority()) {
      // language has priority over size.
      // first run: find it with the preferred size
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtwork.MediaArtworkType.BACKGROUND && art.getLanguage().equals(preferredLanguage)
            && art.getSizeOrder() == preferredSizeOrder) {
          foundfanart = art;
          break;
        }
      }

      // second run: try to find a fanart in the right language with size +/- 1 (order)
      if (foundfanart == null) {
        int minOrder = preferredSizeOrder - 1;
        int maxOrder = preferredSizeOrder + 1;
        for (MediaArtwork art : artwork) {
          if (art.getType() == MediaArtwork.MediaArtworkType.BACKGROUND && art.getLanguage().equals(preferredLanguage)
              && (art.getSizeOrder() == minOrder || art.getSizeOrder() == maxOrder)) {
            foundfanart = art;
            break;
          }
        }
      }
    }

    if (foundfanart == null) {
      // nothing found or language has no preference
      for (MediaArtwork art : artwork) {
        // only get artwork in desired resolution
        if (art.getType() == MediaArtwork.MediaArtworkType.BACKGROUND && art.getSizeOrder() == preferredSizeOrder) {
          foundfanart = art;
          break;
        }
      }
    }

    // final run: if there has nothing been found take the first one
    if (foundfanart == null) {
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtwork.MediaArtworkType.BACKGROUND) {
          foundfanart = art;
          break;
        }
      }
    }

    // assign and download the fanart
    if (foundfanart != null) {
      movieSet.setArtworkUrl(foundfanart.getDefaultUrl(), MediaFileType.FANART);
    }
  }

  /**
   * choose the best artwork for this movieSet
   *
   * @param movieSet
   *          our movie set
   * @param artwork
   *          the artwork list
   * @param type
   *          the type to download
   */
  private static void setBestArtwork(MovieSet movieSet, List<MediaArtwork> artwork, MediaArtwork.MediaArtworkType type) {
    for (MediaArtwork art : artwork) {
      if (art.getType() == type) {
        movieSet.setArtworkUrl(art.getDefaultUrl(), MediaFileType.getMediaFileType(type));
        break;
      }
    }
  }

  /**
   * Manage downloading of the chosen artwork type
   *
   * @param movieSet
   *          the movie for which artwork has to be downloaded
   * @param type
   *          the type of artwork to be downloaded
   */
  public static void downloadArtwork(MovieSet movieSet, MediaFileType type) {
    String url = movieSet.getArtworkUrl(type);
    if (StringUtils.isBlank(url)) {
      return;
    }

    // get image in thread
    MovieSetImageFetcherTask task = new MovieSetImageFetcherTask(movieSet, url, type);
    TmmTaskManager.getInstance().addImageDownloadTask(task);
  }

  /**
   * (re)write the artwork to the movie folders of the given movies
   * 
   * @param movieSet
   *          the parent movie set
   * @param movies
   *          the movies to write the artwork to
   */
  public static void writeImagesToMovieFolder(MovieSet movieSet, List<Movie> movies) {
    if (!MovieModuleManager.SETTINGS.isEnableMovieSetArtworkMovieFolder()) {
      return;
    }

    for (MediaFileType type : SUPPORTED_ARTWORK_TYPES) {
      String url = movieSet.getArtworkUrl(type);
      if (StringUtils.isBlank(url)) {
        continue;
      }

      // get image in thread
      MovieSetImageFetcherTask task = new MovieSetImageFetcherTask(movieSet, url, type, movies);
      TmmTaskManager.getInstance().addImageDownloadTask(task);
    }
  }

  /**
   * strip out the movie set artwork from a movie folder
   *
   * @param movie
   *          the movie to strip out the movie set artwork
   */
  public static void cleanMovieSetArtworkInMovieFolder(Movie movie) {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(movie.getPathNIO())) {
      for (Path entry : stream) {
        Matcher matcher = MediaFileHelper.MOVIESET_ARTWORK_PATTERN.matcher(entry.getFileName().toString());
        if (matcher.find()) {
          Utils.deleteFileSafely(entry);
        }
      }
    }
    catch (Exception e) {
      LOGGER.error("remove movie set artwork: {}", e.getMessage());
    }
  }

  /**
   * remove the whole artwork for the given {@link MovieSet}
   *
   * @param movieSet
   *          the movie set to remove the artwork for
   */
  public static void removeMovieSetArtwork(MovieSet movieSet) {
    for (MediaFile mediaFile : movieSet.getMediaFiles()) {
      if (!mediaFile.isGraphic()) {
        continue;
      }
      Utils.deleteFileSafely(mediaFile.getFile());
    }

    // and also remove any empty subfolders from the artwork folder
    if (!MovieModuleManager.SETTINGS.isEnableMovieSetArtworkFolder() || StringUtils.isBlank(MovieModuleManager.SETTINGS.getMovieSetArtworkFolder())) {
      return;
    }

    try {
      Utils.deleteEmptyDirectoryRecursive(Paths.get(MovieModuleManager.SETTINGS.getMovieSetArtworkFolder()));
    }
    catch (Exception e) {
      LOGGER.warn("could not clean empty subfolders: {}", e.getMessage());
    }
  }

  public static boolean hasMissingArtwork(MovieSet movieSet) {
    if (!MovieModuleManager.SETTINGS.getPosterFilenames().isEmpty() && movieSet.getMediaFiles(MediaFileType.POSTER).isEmpty()) {
      return true;
    }
    if (!MovieModuleManager.SETTINGS.getFanartFilenames().isEmpty() && movieSet.getMediaFiles(MediaFileType.FANART).isEmpty()) {
      return true;
    }
    if (!MovieModuleManager.SETTINGS.getBannerFilenames().isEmpty() && movieSet.getMediaFiles(MediaFileType.BANNER).isEmpty()) {
      return true;
    }
    if (!MovieModuleManager.SETTINGS.getDiscartFilenames().isEmpty() && movieSet.getMediaFiles(MediaFileType.DISC).isEmpty()) {
      return true;
    }
    if (!MovieModuleManager.SETTINGS.getLogoFilenames().isEmpty() && movieSet.getMediaFiles(MediaFileType.LOGO).isEmpty()) {
      return true;
    }
    if (!MovieModuleManager.SETTINGS.getClearlogoFilenames().isEmpty() && movieSet.getMediaFiles(MediaFileType.CLEARLOGO).isEmpty()) {
      return true;
    }
    if (!MovieModuleManager.SETTINGS.getClearartFilenames().isEmpty() && movieSet.getMediaFiles(MediaFileType.CLEARART).isEmpty()) {
      return true;
    }
    return !MovieModuleManager.SETTINGS.getThumbFilenames().isEmpty() && movieSet.getMediaFiles(MediaFileType.THUMB).isEmpty();
  }

  /**
   * get the missing artwork for the given movie set
   * 
   * @param movieSet
   *          the movie set to get the artwork for
   * @param artwork
   *          a list with available artwork
   */
  public static void getMissingArtwork(MovieSet movieSet, List<MediaArtwork> artwork) {
    // sort artwork once again (langu/rating)
    artwork.sort(new MediaArtwork.MediaArtworkComparator(MovieModuleManager.SETTINGS.getScraperLanguage().getLanguage()));

    // poster
    if (movieSet.getMediaFiles(MediaFileType.POSTER).isEmpty()) {
      setBestPoster(movieSet, artwork);
    }

    // fanart
    if (movieSet.getMediaFiles(MediaFileType.FANART).isEmpty()) {
      setBestFanart(movieSet, artwork);
    }

    // logo
    if (movieSet.getMediaFiles(MediaFileType.LOGO).isEmpty()) {
      setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.LOGO);
    }

    // clearlogo
    if (movieSet.getMediaFiles(MediaFileType.CLEARLOGO).isEmpty()) {
      setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.CLEARLOGO);
    }

    // clearart
    if (movieSet.getMediaFiles(MediaFileType.CLEARART).isEmpty()) {
      setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.CLEARART);
    }

    // banner
    if (movieSet.getMediaFiles(MediaFileType.BANNER).isEmpty()) {
      setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.BANNER);
    }

    // thumb
    if (movieSet.getMediaFiles(MediaFileType.THUMB).isEmpty()) {
      setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.THUMB);
    }

    // discart
    if (movieSet.getMediaFiles(MediaFileType.DISC).isEmpty()) {
      setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.DISC);
    }

    // update DB
    movieSet.saveToDb();
  }

  private static class MovieSetImageFetcherTask implements Runnable {
    private MovieSet        movieSet;
    private String          urlToArtwork;
    private MediaFileType   type;
    private boolean         writeToArtworkFolder;
    private String          artworkFolder;
    private boolean         artworkStyleKodi;
    private boolean         writeToMovieFolder;
    private List<MediaFile> writtenArtworkFiles;
    private List<Movie>     movies;

    /**
     * This constructor is needed to write a kind of artwork to the configured locations (or cache dir if nothing specified)
     *
     * @param movieSet
     *          the movie set
     * @param url
     *          the url to the artwork
     * @param type
     *          the artwork type
     */
    private MovieSetImageFetcherTask(MovieSet movieSet, String url, MediaFileType type) {
      this.movieSet = movieSet;
      this.urlToArtwork = url;
      this.type = type;
      this.writtenArtworkFiles = new ArrayList<>();
      this.movies = new ArrayList<>(movieSet.getMovies());

      this.writeToMovieFolder = MovieModuleManager.SETTINGS.isEnableMovieSetArtworkMovieFolder();
      this.artworkFolder = MovieModuleManager.SETTINGS.getMovieSetArtworkFolder();
      this.writeToArtworkFolder = MovieModuleManager.SETTINGS.isEnableMovieSetArtworkFolder() && StringUtils.isNotBlank(artworkFolder);
      if (MovieModuleManager.SETTINGS.isMovieSetArtworkFolderStyleKodi()) {
        // Kodi/Artwork Beef style
        this.artworkStyleKodi = true;
      }
      else {
        // Movie Set Srtwork Automator style
        this.artworkStyleKodi = false;
      }
    }

    /**
     * This constructor is needed to (re)write a kind of artwork to the given list of movies
     * 
     * @param movieSet
     *          the movie set
     * @param url
     *          the url to the artwork
     * @param type
     *          the artwork type
     * @param movies
     *          the list of movies to write the artwork to
     */
    private MovieSetImageFetcherTask(MovieSet movieSet, String url, MediaFileType type, List<Movie> movies) {
      this.movieSet = movieSet;
      this.urlToArtwork = url;
      this.type = type;
      this.writtenArtworkFiles = new ArrayList<>();
      this.movies = new ArrayList<>(movies);

      this.writeToMovieFolder = MovieModuleManager.SETTINGS.isEnableMovieSetArtworkMovieFolder();
      this.artworkFolder = "";
      this.writeToArtworkFolder = false;
    }

    @Override
    public void run() {
      // first, fetch image
      try {
        byte[] bytes = UrlUtil.getByteArrayFromUrl(urlToArtwork);

        String extension = FilenameUtils.getExtension(urlToArtwork);

        // and then write it to the desired files
        movieSet.removeAllMediaFiles(type);
        if (writeToArtworkFolder) {
          writeImageToArtworkFolder(bytes, extension);
        }
        if (writeToMovieFolder) {
          writeImageToMovieFolders(bytes, extension);
        }
        if (!writeToArtworkFolder && !writeToMovieFolder) {
          // at least cache it
          writeImageToCacheFolder(bytes);
        }

        // add all written media files to the movie set
        movieSet.addToMediaFiles(writtenArtworkFiles);
        movieSet.saveToDb();
      }
      catch (InterruptedException | InterruptedIOException e) {
        // do not swallow these Exceptions
        Thread.currentThread().interrupt();
      }
      catch (Exception e) {
        LOGGER.error("fetch image: {} - {}", urlToArtwork, e.getMessage());
      }
    }

    private void writeImageToArtworkFolder(byte[] bytes, String extension) {
      String movieSetName = MovieRenamer.replaceInvalidCharacters(movieSet.getTitle());

      // also remove path separators
      movieSetName = MovieRenamer.replacePathSeparators(movieSetName);

      Path artworkFolderPath;
      if (artworkStyleKodi) {
        // <artwork folder>/<movie set name>/<type>.ext style
        artworkFolderPath = Paths.get(artworkFolder, movieSetName);
      }
      else {
        // <artwork folder>/<movie set name>-<type>.ext style
        artworkFolderPath = Paths.get(artworkFolder);
      }

      // check if folder exists
      if (!Files.exists(artworkFolderPath)) {
        try {
          Files.createDirectories(artworkFolderPath);
        }
        catch (IOException e) {
          LOGGER.warn("could not create directory: " + artworkFolderPath, e);
        }
      }

      // write files
      try {
        String filename = "";
        if (!artworkStyleKodi) {
          // <movie set name>-<type>.ext style
          filename = movieSetName + "-";
        }
        filename += type.name().toLowerCase(Locale.ROOT) + "." + extension;

        Path imageFile = artworkFolderPath.resolve(filename);
        writeImage(bytes, imageFile);

        MediaFile artwork = new MediaFile(imageFile, type);
        artwork.gatherMediaInformation();
        writtenArtworkFiles.add(artwork);

        ImageCache.invalidateCachedImage(artwork);
        ImageCache.cacheImageSilently(artwork);
      }
      catch (Exception e) {
        LOGGER.warn("could not write file", e);
      }
    }

    private void writeImageToMovieFolders(byte[] bytes, String extension) {
      // check for empty strings or movies
      if (movies.isEmpty()) {
        return;
      }

      String filename = "movieset-" + type.name().toLowerCase(Locale.ROOT) + "." + extension;

      // write image for all movies
      for (Movie movie : movies) {
        try {
          if (!movie.isMultiMovieDir()) {
            Path imageFile = movie.getPathNIO().resolve(filename);
            writeImage(bytes, imageFile);

            MediaFile artwork = new MediaFile(imageFile, type);
            artwork.gatherMediaInformation();
            writtenArtworkFiles.add(artwork);

            ImageCache.invalidateCachedImage(artwork);
            ImageCache.cacheImageSilently(artwork);
          }
        }
        catch (Exception e) {
          LOGGER.warn("could not write files", e);
        }
      }
    }

    private void writeImageToCacheFolder(byte[] bytes) {
      String filename = ImageCache.getMD5WithSubfolder(urlToArtwork);

      try {
        writeImage(bytes, ImageCache.getCacheDir().resolve(filename + ".jpg"));
      }
      catch (Exception e) {
        LOGGER.warn("error in image fetcher", e);
      }
    }

    private void writeImage(byte[] bytes, Path pathAndFilename) throws IOException {
      FileOutputStream outputStream = new FileOutputStream(pathAndFilename.toFile());
      InputStream is = new ByteArrayInputStream(bytes);
      IOUtils.copy(is, outputStream);
      outputStream.flush();
      try {
        outputStream.getFD().sync(); // wait until file has been completely written
      }
      catch (Exception e) {
        // empty here -> just not let the thread crash
      }
      outputStream.close();
      is.close();
    }
  }
}
