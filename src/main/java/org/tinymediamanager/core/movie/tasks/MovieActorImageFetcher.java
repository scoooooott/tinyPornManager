/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieActor;
import org.tinymediamanager.scraper.util.UrlUtil;

/**
 * The Class MovieActorImageFetcher.
 * 
 * @author Manuel Laggner
 */
public class MovieActorImageFetcher implements Runnable {

  private final static Logger LOGGER = LoggerFactory.getLogger(MovieActorImageFetcher.class);

  private Movie               movie;

  public MovieActorImageFetcher(Movie movie) {
    this.movie = movie;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    // try/catch block in the root of the thread to log crashes
    try {

      // check if actors folder exists
      Path actorsDir = movie.getPathNIO().resolve(MovieActor.ACTOR_DIR);
      if (!Files.isDirectory(actorsDir)) {
        Files.createDirectory(actorsDir);
      }

      // first check which actors images can be deleted
      try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(actorsDir)) {
        for (Path path : directoryStream) {
          if (Utils.isRegularFile(path) && path.getFileName().toString().matches("(?i).*\\.(tbn|png|jpg)")
              && !path.getFileName().toString().startsWith(".")) {
            boolean found = false;
            // check if there is an actor for this file
            String actorImage = FilenameUtils.getBaseName(path.getFileName().toString()).replace("_", " ");
            for (MovieActor actor : movie.getActors()) {
              if (actor.getName().equals(actorImage)) {
                found = true;

                // trick it to get rid of wrong extensions
                if (!FilenameUtils.getExtension(path.getFileName().toString()).equalsIgnoreCase(UrlUtil.getExtension(actor.getThumbUrl()))) {
                  found = false;
                }
                break;
              }
            }
            // delete image if not found
            if (!found) {
              Utils.deleteFileWithBackup(path, movie.getDataSource());
            }
          }
        }
      }
      catch (IOException ex) {
      }

      // second download missing images
      for (MovieActor actor : movie.getActors()) {
        Path actorImage = actor.getStoragePath();

        if (actorImage != null && StringUtils.isNotEmpty(actor.getThumbUrl()) && !Files.exists(actorImage)) {
          Path cache = ImageCache.getCachedFile(actor.getThumbUrl());
          if (cache != null) {
            Utils.copyFileSafe(cache, actorImage);
          }
        }
        else {
          LOGGER.warn("Cannot download actor image " + actor);
        }
      }

    }
    catch (Exception e) {
      LOGGER.error("Thread crashed: ", e);
    }
  }
}
