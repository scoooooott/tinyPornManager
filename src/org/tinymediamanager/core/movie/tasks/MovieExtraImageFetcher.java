/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.util.Url;

/**
 * The class MovieExtraImageFetcher. To fetch extrafanarts and extrathumbs
 * 
 * @author Manuel Laggner
 */
public class MovieExtraImageFetcher implements Runnable {
  private final static Logger LOGGER = LoggerFactory.getLogger(MovieExtraImageFetcher.class);

  private Movie               movie;
  private MediaFileType       type;

  public MovieExtraImageFetcher(Movie movie, MediaFileType type) {
    this.movie = movie;
    this.type = type;
  }

  @Override
  public void run() {
    // try/catch block in the root of the thread to log crashes
    try {
      if (!movie.isMultiMovieDir()) {
        switch (type) {
          case LOGO:
          case BANNER:
          case CLEARART:
          case THUMB:
          case DISCART:
            // download logo
            downloadArtwork(type);
            break;

          case EXTRATHUMB:
            // download extrathumbs
            downloadExtraThumbs();
            break;

          case EXTRAFANART:
            // download extrafanart
            downloadExtraFanart();
            break;

          default:
            return;
        }

        // check if tmm has been shut down
        if (Thread.interrupted()) {
          return;
        }

        movie.saveToDb();
        movie.callbackForWrittenArtwork(MediaArtworkType.ALL);
      }
      else {
        LOGGER.info("Movie '" + movie.getTitle() + "' is within a multi-movie-directory - skip downloading of additional images.");
      }
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed: ", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.extraimage.threadcrashed"));
    }
  }

  private void downloadArtwork(MediaFileType type) {
    String artworkUrl = movie.getArtworkUrl(type);
    if (StringUtils.isBlank(artworkUrl)) {
      return;
    }

    try {
      // we are lucky and have chosen our enums wisely
      String filename = type.name().toLowerCase() + "." + FilenameUtils.getExtension(artworkUrl);
      movie.removeAllMediaFiles(type);

      // debug message
      LOGGER.debug("writing " + type + " " + filename);

      // fetch and store images
      Url url1 = new Url(artworkUrl);
      File file = new File(movie.getPath(), filename);
      FileOutputStream outputStream = new FileOutputStream(file);
      InputStream is = url1.getInputStream();
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

      // has tmm been shut down?
      if (Thread.interrupted()) {
        return;
      }

      movie.setArtwork(file, type);
      movie.saveToDb();
      movie.callbackForWrittenArtwork(MediaArtworkType.getMediaArtworkType(type));
    }
    catch (InterruptedException e) {
      LOGGER.warn("interrupted download " + type.name());
    }
    catch (IOException e) {
      LOGGER.warn("download " + type.name(), e);
    }
  }

  private void downloadExtraFanart() {
    List<String> fanarts = movie.getExtraFanarts();

    // do not create extrafanarts folder, if no extrafanarts are selected
    if (fanarts.size() == 0) {
      return;
    }

    try {
      String path = movie.getPath() + File.separator + "extrafanart";
      File folder = new File(path);
      if (folder.exists()) {
        FileUtils.deleteDirectory(folder);
        movie.removeAllMediaFiles(MediaFileType.EXTRAFANART);
      }

      folder.mkdirs();

      // fetch and store images
      for (int i = 0; i < fanarts.size(); i++) {
        String urlAsString = fanarts.get(i);
        String providedFiletype = FilenameUtils.getExtension(urlAsString);
        Url url = new Url(urlAsString);
        File file = new File(path, "fanart" + (i + 1) + "." + providedFiletype);
        FileOutputStream outputStream = new FileOutputStream(file);
        InputStream is = url.getInputStream();
        IOUtils.copy(is, outputStream);
        outputStream.flush();
        try {
          outputStream.getFD().sync();
        }
        catch (Exception e) {
          // empty here -> just not let the thread crash
        }
        outputStream.close();

        is.close();
        MediaFile mf = new MediaFile(file, MediaFileType.EXTRAFANART);
        mf.gatherMediaInformation();
        movie.addToMediaFiles(mf);
      }
    }
    catch (InterruptedException e) {
      LOGGER.warn("interrupted download extrafanarts");
    }
    catch (IOException e) {
      LOGGER.warn("download extrafanarts", e);
    }
  }

  private void downloadExtraThumbs() {
    List<String> thumbs = movie.getExtraThumbs();

    // do not create extrathumbs folder, if no extrathumbs are selected
    if (thumbs.size() == 0) {
      return;
    }

    try {
      String path = movie.getPath() + File.separator + "extrathumbs";
      File folder = new File(path);
      if (folder.exists()) {
        FileUtils.deleteDirectory(folder);
        movie.removeAllMediaFiles(MediaFileType.THUMB);
      }

      folder.mkdirs();

      // fetch and store images
      for (int i = 0; i < thumbs.size(); i++) {
        String url = thumbs.get(i);
        String providedFiletype = FilenameUtils.getExtension(url);

        FileOutputStream outputStream = null;
        InputStream is = null;
        File file = null;
        if (Globals.settings.getMovieSettings().isImageExtraThumbsResize() && Globals.settings.getMovieSettings().getImageExtraThumbsSize() > 0) {
          file = new File(path, "thumb" + (i + 1) + ".jpg");
          outputStream = new FileOutputStream(file);
          try {
            is = ImageCache.scaleImage(url, Globals.settings.getMovieSettings().getImageExtraThumbsSize());
          }
          catch (Exception e) {
            LOGGER.warn("problem with rescaling: " + e.getMessage());
            continue;
          }
        }
        else {
          file = new File(path, "thumb" + (i + 1) + "." + providedFiletype);
          outputStream = new FileOutputStream(file);
          Url url1 = new Url(url);
          is = url1.getInputStream();
        }

        IOUtils.copy(is, outputStream);
        outputStream.flush();
        try {
          outputStream.getFD().sync();
        }
        catch (Exception e) {
          // empty here -> just not let the thread crash
        }
        outputStream.close();
        is.close();

        MediaFile mf = new MediaFile(file, MediaFileType.THUMB);
        mf.gatherMediaInformation();
        movie.addToMediaFiles(mf);
      }
    }
    catch (IOException e) {
      LOGGER.warn("download extrathumbs", e);
    }
    catch (Exception e) {
      LOGGER.error(e.getMessage());
    }
  }
}
