/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.util.CachedUrl;

/**
 * The Class MediaEntityImageFetcherTask.
 * 
 * @author Manuel Laggner
 */
public class MediaEntityImageFetcherTask implements Runnable {

  /** The Constant LOGGER. */
  private final static Logger LOGGER = LoggerFactory.getLogger(MediaEntityImageFetcherTask.class);

  /** The entity. */
  private MediaEntity         entity;

  /** The url. */
  private String              url;

  /** The type. */
  private MediaArtworkType    type;

  /** The filename. */
  private String              filename;

  /** The first image. */
  private boolean             firstImage;

  /**
   * Instantiates a new media entity image fetcher.
   * 
   * @param entity
   *          the entity
   * @param url
   *          the url
   * @param type
   *          the type
   * @param filename
   *          the filename
   * @param firstImage
   *          the first image
   */
  public MediaEntityImageFetcherTask(MediaEntity entity, String url, MediaArtworkType type, String filename, boolean firstImage) {
    this.entity = entity;
    this.url = url;
    this.type = type;
    this.filename = filename;
    this.firstImage = firstImage;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    String oldFilename = null;
    try {
      // store old filename at the first image
      if (firstImage) {
        switch (type) {
          case POSTER:
            oldFilename = entity.getPoster();
            entity.clearPoster();
            break;

          case BACKGROUND:
            oldFilename = entity.getFanart();
            entity.clearFanart();
            break;

          case BANNER:
            oldFilename = entity.getBanner();
            entity.clearBanner();
            break;

          case THUMB:
            oldFilename = entity.getThumb();
            entity.clearThumb();
            break;

          default:
            return;
        }
      }

      // debug message
      LOGGER.debug("writing " + type + " " + filename);

      // fetch and store images
      CachedUrl cachedUrl = new CachedUrl(url);
      FileOutputStream outputStream = new FileOutputStream(new File(entity.getPath(), filename));
      InputStream is = cachedUrl.getInputStream();
      IOUtils.copy(is, outputStream);
      outputStream.close();
      is.close();

      // set the new image if its the first image
      if (firstImage) {
        LOGGER.debug("set " + type + " " + FilenameUtils.getName(filename));
        ImageCache.invalidateCachedImage(entity.getPath() + File.separator + filename);
        switch (type) {
          case POSTER:
            entity.setPoster(new File(entity.getPath(), filename));
            entity.saveToDb();
            break;

          case BACKGROUND:
            entity.setFanart(new File(entity.getPath(), filename));
            entity.saveToDb();
            break;

          case BANNER:
            entity.setBanner(new File(entity.getPath(), filename));
            entity.saveToDb();
            break;

          case THUMB:
            entity.setThumb(new File(entity.getPath(), filename));
            entity.saveToDb();
            break;

          default:
            return;
        }

        entity.callbackForWrittenArtwork(type);
      }

    }
    catch (IOException e) {
      LOGGER.debug("fetch image", e);
      // fallback
      if (firstImage) {
        switch (type) {
          case POSTER:
            entity.setPoster(new File(oldFilename));
            entity.saveToDb();
            break;

          case BACKGROUND:
            entity.setFanart(new File(oldFilename));
            entity.saveToDb();
            break;

          case BANNER:
            entity.setBanner(new File(oldFilename));
            entity.saveToDb();
            break;

          case THUMB:
            entity.setThumb(new File(oldFilename));
            entity.saveToDb();
            break;

          default:
            return;
        }
      }
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "ArtworkDownload", "message.artwork.threadcrashed", new String[] { ":",
          e.getLocalizedMessage() }));
    }
  }
}
