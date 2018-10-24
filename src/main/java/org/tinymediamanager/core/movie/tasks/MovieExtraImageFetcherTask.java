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
package org.tinymediamanager.core.movie.tasks;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.ImageUtils;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.http.Url;

/**
 * The class MovieExtraImageFetcherTask. To fetch extrafanarts and extrathumbs
 * 
 * @author Manuel Laggner
 */
public class MovieExtraImageFetcherTask implements Runnable {
  private final static Logger LOGGER = LoggerFactory.getLogger(MovieExtraImageFetcherTask.class);

  private Movie               movie;
  private MediaFileType       type;

  public MovieExtraImageFetcherTask(Movie movie, MediaFileType type) {
    this.movie = movie;
    this.type = type;
  }

  @Override
  public void run() {
    // try/catch block in the root of the thread to log crashes
    try {
      // just for single movies
      if (!movie.isMultiMovieDir()) {
        switch (type) {
          case EXTRATHUMB:
            downloadExtraThumbs();
            break;

          case EXTRAFANART:
            downloadExtraFanart();
            break;

          default:
            return;
        }
      }
      else {
        LOGGER.info("Movie '" + movie.getTitle() + "' is within a multi-movie-directory - skip downloading of " + type.name() + " images.");
      }

      // check if tmm has been shut down
      if (Thread.interrupted()) {
        return;
      }

      movie.callbackForWrittenArtwork(MediaArtworkType.ALL);
      movie.saveToDb();
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed: ", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.extraimage.threadcrashed"));
    }
  }

  private void downloadExtraFanart() {
    List<String> fanarts = movie.getExtraFanarts();

    // do not create extrafanarts folder, if no extrafanarts are selected
    if (fanarts.size() == 0) {
      return;
    }

    // create an empty extrafanarts folder
    Path folder = movie.getPathNIO().resolve("extrafanart");
    try {
      if (Files.isDirectory(folder)) {
        Utils.deleteDirectoryRecursive(folder);
        movie.removeAllMediaFiles(MediaFileType.EXTRAFANART);
      }
      Files.createDirectory(folder);
    }
    catch (IOException e) {
      LOGGER.error("could not create extrafanarts folder: " + e.getMessage());
      return;
    }

    // fetch and store images
    int i = 1;
    for (String urlAsString : fanarts) {
      long timestamp = System.currentTimeMillis();
      FileOutputStream outputStream = null;
      InputStream is = null;
      try {
        String filename = "fanart" + i + "." + FilenameUtils.getExtension(urlAsString);

        // don't write jpeg -> write jpg
        if (FilenameUtils.getExtension(filename).equalsIgnoreCase("JPEG")) {
          filename = FilenameUtils.getBaseName(filename) + ".jpg";
        }

        // debug message
        LOGGER.debug("writing " + type + " " + filename);
        Path destFile = folder.resolve(filename);
        Path tempFile = null;
        try {
          // create a temp file/folder inside the tmm folder
          Path tempFolder = Paths.get(Constants.TEMP_FOLDER);
          if (!Files.exists(tempFolder)) {
            Files.createDirectory(tempFolder);
          }
          tempFile = tempFolder.resolve(filename + "." + timestamp + ".part"); // multi episode same file
        }
        catch (Exception e) {
          // could not create the temp folder somehow - put the files into the entity dir
          tempFile = folder.resolve(filename + "." + timestamp + ".part"); // multi episode same file
        }

        // fetch and store images
        Url url = new Url(urlAsString);
        outputStream = new FileOutputStream(tempFile.toFile());
        // fetch the images with at max 5 retries
        is = url.getInputStreamWithRetry(5);

        if (is == null) {
          // 404 et all
          IOUtils.closeQuietly(outputStream);
          throw new FileNotFoundException("Error accessing url: " + url.getStatusLine());
        }

        IOUtils.copy(is, outputStream);
        outputStream.flush();
        try {
          outputStream.getFD().sync(); // wait until file has been completely written
          // give it a few milliseconds
          Thread.sleep(150);
        }
        catch (Exception ignored) {
          // empty here -> just not let the thread crash
        }
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(outputStream);

        // check if the file has been downloaded
        if (!Files.exists(tempFile) || Files.size(tempFile) == 0) {
          // cleanup the file
          FileUtils.deleteQuietly(tempFile.toFile());
          throw new Exception("0byte file downloaded: " + filename);
        }

        // move the temp file to the expected filename
        if (!Utils.moveFileSafe(tempFile, destFile)) {
          throw new Exception("renaming temp file failed: " + filename);
        }

        MediaFile mf = new MediaFile(destFile, MediaFileType.EXTRAFANART);
        mf.gatherMediaInformation();
        movie.addToMediaFiles(mf);

        // build up image cache
        if (Settings.getInstance().isImageCache()) {
          try {
            ImageCache.cacheImage(destFile);
          }
          catch (Exception ignored) {
          }
        }

        // has tmm been shut down?
        if (Thread.interrupted()) {
          return;
        }

        i++;
      }
      catch (InterruptedException | InterruptedIOException e) {
        LOGGER.warn("interrupted download extrafanarts");
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(outputStream);

        // leave the loop
        break;
      }
      catch (Exception e) {
        LOGGER.warn("problem downloading extrafanarts: " + e.getMessage());
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(outputStream);
      }
    }
  }

  private void downloadExtraThumbs() {
    List<String> thumbs = movie.getExtraThumbs();

    // do not create extrathumbs folder, if no extrathumbs are selected
    if (thumbs.size() == 0) {
      return;
    }

    Path folder = movie.getPathNIO().resolve("extrathumbs");
    try {
      if (Files.isDirectory(folder)) {
        Utils.deleteDirectoryRecursive(folder);
        movie.removeAllMediaFiles(MediaFileType.EXTRATHUMB);
      }
      Files.createDirectory(folder);
    }
    catch (IOException e) {
      LOGGER.error("could not create extrathumbs folder: " + e.getMessage());
      return;
    }

    // fetch and store images
    int i = 1;
    for (String urlAsString : thumbs) {
      long timestamp = System.currentTimeMillis();
      FileOutputStream outputStream = null;
      InputStream is = null;
      try {
        String filename = "thumb" + i + ".";
        if (MovieModuleManager.SETTINGS.isImageExtraThumbsResize()) {
          filename += "jpg";
        }
        else {
          filename += FilenameUtils.getExtension(urlAsString);
        }

        // don't write jpeg -> write jpg
        if (FilenameUtils.getExtension(filename).equalsIgnoreCase("JPEG")) {
          filename = FilenameUtils.getBaseName(filename) + ".jpg";
        }

        // debug message
        LOGGER.debug("writing " + type + " " + filename);
        Path destFile = folder.resolve(filename);
        Path tempFile = null;
        try {
          // create a temp file/folder inside the tmm folder
          Path tempFolder = Paths.get(Constants.TEMP_FOLDER);
          if (!Files.exists(tempFolder)) {
            Files.createDirectory(tempFolder);
          }
          tempFile = tempFolder.resolve(filename + "." + timestamp + ".part"); // multi episode same file
        }
        catch (Exception e) {
          // could not create the temp folder somehow - put the files into the entity dir
          tempFile = folder.resolve(filename + "." + timestamp + ".part"); // multi episode same file
        }

        Url url = new Url(urlAsString);
        // fetch the images with at max 5 retries
        is = url.getInputStreamWithRetry(5);

        if (is == null) {
          // 404 et all
          IOUtils.closeQuietly(outputStream);
          throw new FileNotFoundException("Error accessing url: " + url.getStatusLine());
        }

        // rescale?
        if (MovieModuleManager.SETTINGS.isImageExtraThumbsResize()) {
          try {
            InputStream oldIs = is;
            is = ImageUtils.scaleImage(IOUtils.toByteArray(oldIs), MovieModuleManager.SETTINGS.getImageExtraThumbsSize());
            IOUtils.closeQuietly(oldIs);
          }
          catch (Exception e) {
            LOGGER.warn("problem with rescaling: " + e.getMessage());
            continue;
          }
        }

        outputStream = new FileOutputStream(tempFile.toFile());

        IOUtils.copy(is, outputStream);
        outputStream.flush();
        try {
          outputStream.getFD().sync();
        }
        catch (Exception ignored) {
          // empty here -> just not let the thread crash
        }
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(outputStream);

        // check if the file has been downloaded
        if (!Files.exists(tempFile) || Files.size(tempFile) == 0) {
          // cleanup the file
          FileUtils.deleteQuietly(tempFile.toFile());
          throw new Exception("0byte file downloaded: " + filename);
        }

        // move the temp file to the expected filename
        if (!Utils.moveFileSafe(tempFile, destFile)) {
          throw new Exception("renaming temp file failed: " + filename);
        }

        MediaFile mf = new MediaFile(destFile, MediaFileType.EXTRATHUMB);
        mf.gatherMediaInformation();
        movie.addToMediaFiles(mf);

        // build up image cache
        if (Settings.getInstance().isImageCache()) {
          try {
            ImageCache.cacheImage(destFile);
          }
          catch (Exception ignored) {
          }
        }

        // has tmm been shut down?
        if (Thread.interrupted()) {
          return;
        }

        i++;
      }
      catch (InterruptedException | InterruptedIOException e) {
        LOGGER.warn("interrupted download extrathumbs");
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(outputStream);

        // leave the loop
        break;
      }
      catch (Exception e) {
        LOGGER.warn("problem downloading extrathumbs: " + e.getMessage());
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(outputStream);
      }
    }
  }
}
