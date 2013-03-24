/*
 * Copyright 2012 Manuel Laggner
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.util.CachedUrl;

/**
 * @author Manuel Laggner
 * 
 */
public class MovieImageFetcher implements Runnable {

  private final static Logger LOGGER = Logger.getLogger(MovieImageFetcher.class);

  private Movie               movie;
  private String              url;
  private MediaArtworkType    type;
  private String              filename;
  private boolean             firstImage;

  public MovieImageFetcher(Movie movie, String url, MediaArtworkType type, String filename, boolean firstImage) {
    this.movie = movie;
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
            oldFilename = movie.getPoster();
            movie.setPoster("");
            break;

          case BACKGROUND:
            oldFilename = movie.getFanart();
            movie.setFanart("");
            break;
        }
      }

      // debug message
      LOGGER.debug("writing " + type + " " + filename);

      // fetch and store images
      CachedUrl cachedUrl = new CachedUrl(url);
      FileOutputStream outputStream = new FileOutputStream(filename);
      InputStream is = cachedUrl.getInputStream();
      IOUtils.copy(is, outputStream);
      outputStream.close();
      is.close();

      // set the new image if its the first image
      if (firstImage) {
        LOGGER.debug("set " + type + " " + FilenameUtils.getName(filename));
        switch (type) {
          case POSTER:
            movie.setPoster(FilenameUtils.getName(filename));
            movie.saveToDb();
            break;

          case BACKGROUND:
            movie.setFanart(FilenameUtils.getName(filename));
            movie.saveToDb();
            break;
        }
      }

    }
    catch (IOException e) {
      LOGGER.debug("fetch image", e);
      // fallback
      if (firstImage) {
        switch (type) {
          case POSTER:
            movie.setPoster(oldFilename);
            movie.saveToDb();
            break;

          case BACKGROUND:
            movie.setFanart(oldFilename);
            movie.saveToDb();
            break;
        }
      }
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed", e);
    }
  }
}
