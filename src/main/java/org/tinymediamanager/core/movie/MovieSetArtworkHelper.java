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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.http.Url;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The class MovieSetArtworkHelper. A helper class for managing movie set artwork
 *
 * @author Manuel Laggner
 */
public class MovieSetArtworkHelper {
  private static final List<MediaFileType> SUPPORTED_ARTWORK_TYPES     = Arrays.asList(MediaFileType.POSTER, MediaFileType.FANART,
      MediaFileType.BANNER, MediaFileType.LOGO, MediaFileType.CLEARART);
  private static final String[]            SUPPORTED_ARTWORK_FILETYPES = { "jpg", "png", "tbn" };

  private static final Logger LOGGER = LoggerFactory.getLogger(MovieSetArtworkHelper.class);

  /**
   * find and assign movie set artwork in the artwork folder
   * 
   * @param movieSet
   *          the movie set to search artwork for
   */
  public static void findArtworkInArtworkFolder(MovieSet movieSet) {
    String artworkFolder = MovieModuleManager.MOVIE_SETTINGS.getMovieSetArtworkFolder();
    if (!MovieModuleManager.MOVIE_SETTINGS.isEnableMovieSetArtworkFolder() || StringUtils.isBlank(artworkFolder)) {
      return;
    }

    // performance trick: look if the desired file is in the image cache
    for (MediaFileType type : SUPPORTED_ARTWORK_TYPES) {
      for (String fileType : SUPPORTED_ARTWORK_FILETYPES) {
        String artworkFileName = MovieRenamer.replaceInvalidCharacters(movieSet.getTitle()) + "-" + type.name().toLowerCase() + "." + fileType;
        File artworkFile = new File(artworkFolder, artworkFileName);
        if (artworkFile.exists()) {
          // add this artwork to the media files
          MediaFile mediaFile = new MediaFile(artworkFile, type);
          mediaFile.gatherMediaInformation();
          movieSet.addToMediaFiles(mediaFile);
        }
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
    if (!MovieModuleManager.MOVIE_SETTINGS.isEnableMovieSetArtworkMovieFolder()) {
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

      this.writeToMovieFolder = MovieModuleManager.MOVIE_SETTINGS.isEnableMovieSetArtworkMovieFolder();
      this.artworkFolder = MovieModuleManager.MOVIE_SETTINGS.getMovieSetArtworkFolder();
      this.writeToArtworkFolder = MovieModuleManager.MOVIE_SETTINGS.isEnableMovieSetArtworkFolder() && StringUtils.isNotBlank(artworkFolder);
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

      this.writeToMovieFolder = MovieModuleManager.MOVIE_SETTINGS.isEnableMovieSetArtworkMovieFolder();
      this.artworkFolder = "";
      this.writeToArtworkFolder = false;
    }

    @Override
    public void run() {
      // first, fetch image
      try {
        Url url = new Url(urlToArtwork);
        InputStream is = url.getInputStream();
        byte[] bytes = IOUtils.toByteArray(is);
        is.close();

        // and then write it to the desired files
        movieSet.removeAllMediaFiles(type);
        if (writeToArtworkFolder) {
          writeImageToArtworkFolder(bytes);
        }
        if (writeToMovieFolder) {
          writeImageToMovieFolders(bytes);
        }
        if (!writeToArtworkFolder && !writeToMovieFolder) {
          // at least cache it
          writeImageToCacheFolder(bytes);
        }

        // add all written media files to the movie set
        movieSet.addToMediaFiles(writtenArtworkFiles);
      }
      catch (Exception e) {
        if (e instanceof InterruptedException) {
          // only warning
          LOGGER.warn("interrupted image download");
        }
        else {
          LOGGER.error("fetch image", e);
        }
      }
    }

    private void writeImageToArtworkFolder(byte[] bytes) {
      File artworkFolder = new File(this.artworkFolder);

      // check if folder exists
      if (!artworkFolder.exists()) {
        artworkFolder.mkdirs();
      }

      // write files
      try {
        String providedFiletype = FilenameUtils.getExtension(urlToArtwork);
        writeImage(bytes, artworkFolder.getPath() + File.separator + MovieRenamer.replaceInvalidCharacters(movieSet.getTitle()) + "-"
            + type.name().toLowerCase() + "." + providedFiletype);
      }
      catch (Exception e) {
        LOGGER.warn("could not write file", e);
      }
    }

    private void writeImageToMovieFolders(byte[] bytes) {
      // check for empty strings or movies
      if (movies.isEmpty()) {
        return;
      }

      String filename = "movieset-" + type.name().toLowerCase() + ".jpg";

      // write image for all movies
      for (Movie movie : movies) {
        try {
          if (!movie.isMultiMovieDir()) {
            writeImage(bytes, movie.getPath() + File.separator + filename);
          }
        }
        catch (Exception e) {
          LOGGER.warn("could not write files", e);
        }
      }
    }

    private void writeImageToCacheFolder(byte[] bytes) {
      String filename = ImageCache.getCachedFileName(urlToArtwork);

      try {
        writeImage(bytes, ImageCache.getCacheDir() + File.separator + filename + ".jpg");
      }
      catch (Exception e) {
        LOGGER.warn("error in image fetcher", e);
      }
    }

    private void writeImage(byte[] bytes, String pathAndFilename) throws IOException, InterruptedException {
      FileOutputStream outputStream = new FileOutputStream(pathAndFilename);
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

      ImageCache.invalidateCachedImage(pathAndFilename);

      MediaFile artwork = new MediaFile(new File(pathAndFilename), type);
      artwork.gatherMediaInformation();
      writtenArtworkFiles.add(artwork);
    }
  }
}
