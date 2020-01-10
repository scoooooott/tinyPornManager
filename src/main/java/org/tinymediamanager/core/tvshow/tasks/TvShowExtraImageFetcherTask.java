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
package org.tinymediamanager.core.tvshow.tasks;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.ImageUtils;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;

/**
 * The class TvShowExtraImageFetcherTask. To fetch extrafanarts and extrathumbs
 * 
 * @author Manuel Laggner
 */
public class TvShowExtraImageFetcherTask implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(TvShowExtraImageFetcherTask.class);

  private TvShow              tvShow;
  private MediaFileType       type;

  public TvShowExtraImageFetcherTask(TvShow tvShow, MediaFileType type) {
    this.tvShow = tvShow;
    this.type = type;
  }

  @Override
  public void run() {

    // try/catch block in the root of the thread to log crashes
    try {
      switch (type) {
        case EXTRAFANART:
          downloadExtraFanart();
          break;

        default:
          return;
      }

      // check if tmm has been shut down
      if (Thread.interrupted()) {
        return;
      }

      tvShow.callbackForWrittenArtwork(MediaArtworkType.ALL);
      tvShow.saveToDb();
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed: ", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, tvShow, "message.extraimage.threadcrashed"));
    }
  }

  private void downloadExtraFanart() {
    List<String> fanartUrls = tvShow.getExtraFanartUrls();

    // do not create extrafanarts folder, if no extrafanarts are selected
    if (fanartUrls.isEmpty()) {
      return;
    }

    // create an empty extrafanarts folder
    Path folder = tvShow.getPathNIO().resolve("extrafanart");
    try {
      if (Files.isDirectory(folder)) {
        Utils.deleteDirectorySafely(folder, tvShow.getDataSource());
        tvShow.removeAllMediaFiles(MediaFileType.EXTRAFANART);
      }
      Files.createDirectory(folder);
    }
    catch (IOException e) {
      LOGGER.error("could not create extrafanarts folder: {}", e.getMessage());
      return;
    }

    // fetch and store images
    int i = 1;
    for (String urlAsString : fanartUrls) {
      try {
        String filename = "fanart" + i + "." + FilenameUtils.getExtension(urlAsString);

        Path destFile = ImageUtils.downloadImage(urlAsString, folder, filename);

        MediaFile mf = new MediaFile(destFile, MediaFileType.EXTRAFANART);
        mf.gatherMediaInformation();
        tvShow.addToMediaFiles(mf);

        // build up image cache
        ImageCache.invalidateCachedImage(mf);
        ImageCache.cacheImageSilently(mf);

        i++;
      }
      catch (InterruptedException | InterruptedIOException e) {
        // do not swallow these Exceptions
        Thread.currentThread().interrupt();
      }
      catch (Exception e) {
        LOGGER.warn("problem downloading extrafanart {} - {} ", urlAsString, e.getMessage());
      }
    }
  }
}
