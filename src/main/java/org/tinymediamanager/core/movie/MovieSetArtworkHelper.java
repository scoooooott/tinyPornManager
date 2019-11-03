/*
 * Copyright 2012 - 2019 Manuel Laggner
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
import java.util.regex.Matcher;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaFileHelper;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
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
      MediaFileType.BANNER, MediaFileType.LOGO, MediaFileType.CLEARLOGO, MediaFileType.CLEARART);
  private static final String[]            SUPPORTED_ARTWORK_FILETYPES = { "jpg", "png", "tbn" };

  /**
   * Update the artwork for a given movie set. This should be triggered after every movie set change like creating, adding movies, removing movies
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
   * whenever a movie set is renamed, the artwork in the artwork folder must be renamed too
   * 
   * @param movieSet
   *          the movie set
   */
  public static void renameArtwork(MovieSet movieSet) {
    String artworkFolder = MovieModuleManager.SETTINGS.getMovieSetArtworkFolder();
    if (!MovieModuleManager.SETTINGS.isEnableMovieSetArtworkFolder() || StringUtils.isBlank(artworkFolder)) {
      return;
    }

    List<MediaFile> cleanup = new ArrayList<>();
    for (MediaFile mf : movieSet.getMediaFiles()) {
      if (mf.isGraphic() && mf.getFile().getParent().endsWith(artworkFolder)) {
        try {
          String extension = FilenameUtils.getExtension(mf.getFilename());
          String artworkFileName = MovieRenamer.replaceInvalidCharacters(movieSet.getTitle()) + "-" + mf.getType().name().toLowerCase(Locale.ROOT)
              + "." + extension;
          Path artworkFile = Paths.get(artworkFolder, artworkFileName);
          Utils.moveFileSafe(mf.getFileAsPath(), artworkFile);

          movieSet.addToMediaFiles(new MediaFile(artworkFile));
          cleanup.add(mf);
        }
        catch (Exception e) {
          LOGGER.error("could not rename movie set artwork: {}", e.getMessage());
        }
      }
    }

    for (MediaFile mf : cleanup) {
      movieSet.removeFromMediaFiles(mf);
    }
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

    for (MediaFileType type : SUPPORTED_ARTWORK_TYPES) {
      for (String fileType : SUPPORTED_ARTWORK_FILETYPES) {
        String artworkFileName = MovieRenamer.replaceInvalidCharacters(movieSet.getTitle()) + "-" + type.name().toLowerCase(Locale.ROOT) + "."
            + fileType;
        Path artworkFile = Paths.get(artworkFolder, artworkFileName);
        if (Files.exists(artworkFile)) {
          // add this artwork to the media files
          MediaFile mediaFile = new MediaFile(artworkFile, type);
          mediaFile.gatherMediaInformation();
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
          mediaFile.gatherMediaInformation();
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
   */
  public static void setArtwork(MovieSet movieSet, List<MediaArtwork> artwork) {
    // sort artwork once again (langu/rating)
    artwork.sort(new MediaArtwork.MediaArtworkComparator(MovieModuleManager.SETTINGS.getImageScraperLanguage().getLanguage()));

    // poster
    setBestPoster(movieSet, artwork);

    // fanart
    setBestFanart(movieSet, artwork);

    // works now for single & multimovie
    setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.LOGO);
    setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.CLEARLOGO);
    setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.CLEARART);
    setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.BANNER);
    setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.THUMB);
    setBestArtwork(movieSet, artwork, MediaArtwork.MediaArtworkType.DISC);

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
    if (!MovieModuleManager.SETTINGS.getThumbFilenames().isEmpty() && movieSet.getMediaFiles(MediaFileType.THUMB).isEmpty()) {
      return true;
    }

    return false;
  }

  public static void downloadMissingArtwork(MovieSet movieSet) {
    downloadMissingArtwork(movieSet, false);
  }

  public static void downloadMissingArtwork(MovieSet movieSet, boolean force) {
    MediaFileType[] mfts = MediaFileType.getGraphicMediaFileTypes();

    // do for all known graphical MediaFileTypes
    for (MediaFileType mft : mfts) {

      List<MediaFile> mfs = movieSet.getMediaFiles(mft);
      if (mfs.isEmpty()) {
        boolean download = false;
        // not in our list? get'em!
        switch (mft) {
          case FANART:
            if (!MovieModuleManager.SETTINGS.getFanartFilenames().isEmpty() || force) {
              download = true;
            }
            break;

          case POSTER:
            if (!MovieModuleManager.SETTINGS.getPosterFilenames().isEmpty() || force) {
              download = true;
            }
            break;

          case BANNER:
            if (!MovieModuleManager.SETTINGS.getBannerFilenames().isEmpty() || force) {
              download = true;
            }
            break;

          case CLEARART:
            if (!MovieModuleManager.SETTINGS.getClearartFilenames().isEmpty() || force) {
              download = true;
            }
            break;

          case DISC:
            if (!MovieModuleManager.SETTINGS.getDiscartFilenames().isEmpty() || force) {
              download = true;
            }
            break;

          case LOGO:
            if (!MovieModuleManager.SETTINGS.getLogoFilenames().isEmpty() || force) {
              download = true;
            }
            break;

          case CLEARLOGO:
            if (!MovieModuleManager.SETTINGS.getClearlogoFilenames().isEmpty() || force) {
              download = true;
            }
            break;

          case THUMB:
            if (!MovieModuleManager.SETTINGS.getThumbFilenames().isEmpty() || force) {
              download = true;
            }
            break;

          case EXTRAFANART:
            if (MovieModuleManager.SETTINGS.isImageExtraFanart() || force) {
              download = true;
            }
            break;

          case EXTRATHUMB:
            if (MovieModuleManager.SETTINGS.isImageExtraThumbs() || force) {
              download = true;
            }
            break;

          default:
            break;
        }

        if (download) {
          downloadArtwork(movieSet, mft);
        }
      }
    }
  }

  public static void downloadMissingArtwork(MovieSet movieSet, List<MediaArtwork> artwork) {
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
      Path artworkFolder = Paths.get(this.artworkFolder);

      // check if folder exists
      if (!Files.exists(artworkFolder)) {
        try {
          Files.createDirectories(artworkFolder);
        }
        catch (IOException e) {
          LOGGER.warn("could not create directory: " + artworkFolder, e);
        }
      }

      // write files
      try {
        String filename = MovieRenamer.replaceInvalidCharacters(movieSet.getTitle()) + "-";
        filename += type.name().toLowerCase(Locale.ROOT) + "." + extension;

        Path imageFile = artworkFolder.resolve(filename);
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
