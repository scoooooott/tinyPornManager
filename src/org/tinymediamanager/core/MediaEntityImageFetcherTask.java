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
package org.tinymediamanager.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.util.Url;

/**
 * The Class MediaEntityImageFetcherTask.
 * 
 * @author Manuel Laggner
 */
public class MediaEntityImageFetcherTask implements Runnable {
  private final static Logger LOGGER = LoggerFactory.getLogger(MediaEntityImageFetcherTask.class);

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
            oldFilename = entity.getArtworkFilename(MediaArtworkType.getMediaFileType(type));
            entity.removeAllMediaFiles(MediaArtworkType.getMediaFileType(type));
            break;

          default:
            return;
        }
      }

      // debug message
      LOGGER.debug("writing " + type + " " + filename);

      // check if old and new file are the same (possible if you select it in the imagechooser)
      boolean sameFile = false;
      if (url.startsWith("file:")) {
        String newUrl = url.replace("file:/", "");
        File file = new File(newUrl);
        File destFile = new File(entity.getPath(), filename);
        if (file.equals(destFile)) {
          sameFile = true;
        }
      }

      // fetch and store images
      if (!sameFile) {
        Url url1 = new Url(url);
        File tempFile = new File(entity.getPath(), filename + ".part");
        FileOutputStream outputStream = new FileOutputStream(tempFile);
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

        // check if the file has been downloaded
        if (!tempFile.exists() || tempFile.length() == 0) {
          throw new Exception("0byte file downloaded: " + filename);
        }

        // delete the old one if exisiting
        File destinationFile = new File(entity.getPath(), filename);
        FileUtils.deleteQuietly(destinationFile);

        // move the temp file to the expected filename
        if (!tempFile.renameTo(destinationFile)) {
          throw new Exception("renaming temp file failed: " + filename);
        }
      }

      // has tmm been shut down?
      if (Thread.interrupted()) {
        return;
      }

      // set the new image if its the first image
      if (firstImage) {
        LOGGER.debug("set " + type + " " + FilenameUtils.getName(filename));
        ImageCache.invalidateCachedImage(entity.getPath() + File.separator + filename);
        switch (type) {
          case POSTER:
          case BACKGROUND:
          case BANNER:
          case THUMB:
          case CLEARART:
          case DISC:
          case LOGO:
            entity.setArtwork(new File(entity.getPath(), filename), MediaArtworkType.getMediaFileType(type));
            entity.saveToDb();
            entity.callbackForWrittenArtwork(type);
            break;

          default:
            return;
        }
      }
    }
    catch (InterruptedException e) {
      LOGGER.warn("interrupted image download");
      // remove temp file
      File tempFile = new File(entity.getPath(), filename + ".part");
      if (tempFile.exists()) {
        FileUtils.deleteQuietly(tempFile);
      }
      return;
    }
    catch (Exception e) {
      LOGGER.debug("fetch image", e);
      // remove temp file
      File tempFile = new File(entity.getPath(), filename + ".part");
      if (tempFile.exists()) {
        FileUtils.deleteQuietly(tempFile);
      }
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
            entity.setArtwork(new File(oldFilename), MediaArtworkType.getMediaFileType(type));
            entity.saveToDb();
            entity.callbackForWrittenArtwork(type);
            break;

          default:
            return;
        }
      }

      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "ArtworkDownload", "message.artwork.threadcrashed", new String[] { ":",
          e.getLocalizedMessage() }));
    }
  }
}
