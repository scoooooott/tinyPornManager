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
package org.tinymediamanager.core.tasks;

import java.io.InterruptedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.ImageUtils;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;

/**
 * The Class MediaEntityImageFetcherTask.
 * 
 * @author Manuel Laggner
 */
public class MediaEntityImageFetcherTask implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(MediaEntityImageFetcherTask.class);

  private MediaEntity         entity;
  private String              url;
  private MediaArtworkType    type;
  private String              filename;
  private boolean             firstImage;

  public MediaEntityImageFetcherTask(MediaEntity entity, String url, MediaArtworkType type, String filename, boolean firstImage) {
    this.entity = entity;
    this.url = url;
    this.type = type;
    this.filename = filename;
    this.firstImage = firstImage;
  }

  @Override
  public void run() {
    if (StringUtils.isBlank(filename)) {
      return;
    }

    String oldFilename = null;
    try {
      // store old filename at the first image
      if (firstImage) {
        switch (type) {
          case POSTER:
          case BACKGROUND:
          case BANNER:
          case THUMB:
          case CLEARART:
          case DISC:
          case LOGO:
          case CLEARLOGO:
          case CHARACTERART:
          case KEYART:
            oldFilename = entity.getArtworkFilename(MediaFileType.getMediaFileType(type));
            entity.removeAllMediaFiles(MediaFileType.getMediaFileType(type));
            break;

          default:
            return;
        }
      }

      // debug message
      LOGGER.debug("writing {} - {}", type, filename);
      Path destFile = ImageUtils.downloadImage(url, entity.getPathNIO(), filename);

      // set the new image if its the first image
      if (firstImage) {
        LOGGER.debug("set {} - {}", type, FilenameUtils.getName(filename));
        ImageCache.invalidateCachedImage(entity.getPathNIO().resolve(filename));
        switch (type) {
          case POSTER:
          case BACKGROUND:
          case BANNER:
          case THUMB:
          case CLEARART:
          case DISC:
          case LOGO:
          case CLEARLOGO:
          case CHARACTERART:
          case KEYART:
            entity.setArtwork(destFile, MediaFileType.getMediaFileType(type));
            entity.callbackForWrittenArtwork(type);
            entity.saveToDb();

            // build up image cache
            ImageCache.cacheImageSilently(destFile);
            break;

          default:
            return;
        }
      }
      else {
        MediaFile artwork = new MediaFile(destFile, MediaFileType.getMediaFileType(type));
        artwork.gatherMediaInformation();
        entity.addToMediaFiles(artwork);
      }
    }
    catch (InterruptedException | InterruptedIOException e) {
      // do not swallow these Exceptions
      Thread.currentThread().interrupt();
    }
    catch (Exception e) {
      LOGGER.error("fetch image {} - {}", url, e.getMessage());

      // fallback
      if (firstImage && StringUtils.isNotBlank(oldFilename)) {
        switch (type) {
          case POSTER:
          case BACKGROUND:
          case BANNER:
          case THUMB:
          case CLEARART:
          case DISC:
          case LOGO:
          case CLEARLOGO:
          case CHARACTERART:
          case KEYART:
            Path oldFile = Paths.get(oldFilename);
            entity.setArtwork(oldFile, MediaFileType.getMediaFileType(type));
            entity.callbackForWrittenArtwork(type);
            entity.saveToDb();

            // build up image cache
            ImageCache.cacheImageSilently(oldFile);
            break;

          default:
            return;
        }
      }

      MessageManager.instance.pushMessage(
          new Message(MessageLevel.ERROR, "ArtworkDownload", "message.artwork.threadcrashed", new String[] { ":", e.getLocalizedMessage() }));
    }
  }
}
