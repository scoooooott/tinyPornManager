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
package org.tinymediamanager.core.movie.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieActor;
import org.tinymediamanager.scraper.util.Url;

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
      String actorsDirPath = movie.getPath() + File.separator + MovieActor.ACTOR_DIR;

      // check if actors folder exists
      File actorsDir = new File(actorsDirPath);
      if (!actorsDir.exists()) {
        actorsDir.mkdirs();
      }

      // first check which actors images can be deleted
      FilenameFilter filter = new FilenameFilter() {
        Pattern pattern = Pattern.compile("(?i).*\\.(tbn|png|jpg)");

        public boolean accept(File dir, String name) {
          // do not start with .
          if (name.toLowerCase().startsWith("."))
            return false;

          // check if filetype is in our settings
          Matcher matcher = pattern.matcher(name);
          if (matcher.matches()) {
            return true;
          }

          return false;
        }
      };

      File[] imageFiles = actorsDir.listFiles(filter);
      for (File file : imageFiles) {
        boolean found = false;
        // check if there is an actor for this file
        String name = FilenameUtils.getBaseName(file.getName()).replace("_", " ");
        for (MovieActor actor : movie.getActors()) {
          if (actor.getName().equals(name)) {
            found = true;

            // trick it to get rid of wrong extensions
            if (!FilenameUtils.getExtension(file.getName()).equalsIgnoreCase(FilenameUtils.getExtension(actor.getThumbUrl()))) {
              found = false;
            }
            break;
          }
        }

        // delete image if not found
        if (!found) {
          FileUtils.deleteQuietly(file);
        }
      }

      // second download missing images
      for (MovieActor actor : movie.getActors()) {
        String actorName = actor.getName().replace(" ", "_");

        String providedFiletype = FilenameUtils.getExtension(actor.getThumbUrl());
        File actorImage = new File(actorsDirPath + File.separator + actorName + "." + providedFiletype);
        if (!actorImage.exists() && StringUtils.isNotEmpty(actor.getThumbUrl())) {
          try {
            Url url = new Url(actor.getThumbUrl());
            FileOutputStream outputStream = new FileOutputStream(actorImage);
            InputStream is = url.getInputStream();
            IOUtils.copy(is, outputStream);
            outputStream.flush();
            try {
              outputStream.getFD().sync();
            }
            catch (Exception e) {
            }
            outputStream.close();
            is.close();

            actor.setThumbPath(actorsDirPath + File.separator + actorName + "." + providedFiletype);
          }
          catch (IOException e) {
            LOGGER.warn("Problem getting actor image: " + e.getMessage());
          }
        }

        // set path if it is empty and an image exists
        if (actorImage.exists() && StringUtils.isEmpty(actor.getThumbPath())) {
          actor.setThumbPath(actorsDirPath + File.separator + actorName + "." + providedFiletype);
        }
      }
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed: ", e);
    }
  }
}
