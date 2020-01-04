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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.ImageUtils;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.scraper.util.UrlUtil;

/**
 * The class MediaEntityActorImageFetcherTask.
 * 
 * @author Manuel Laggner
 */
public abstract class MediaEntityActorImageFetcherTask implements Runnable {

  private static final Logger LOGGER  = LoggerFactory.getLogger(MediaEntityActorImageFetcherTask.class);

  protected MediaEntity       mediaEntity;
  protected Set<Person>       persons;
  protected boolean           cleanup = true;

  protected abstract Logger getLogger();

  @Override
  public void run() {
    // try/catch block in the root of the thread to log crashes
    try {

      // check if actors folder exists
      Path actorsDir = mediaEntity.getPathNIO().resolve(Person.ACTOR_DIR);
      if (!Files.isDirectory(actorsDir)) {
        Files.createDirectory(actorsDir);
      }

      // first - check which actors images can be deleted (images for actors which are not in this ME)
      // only if the actors folder is not a symbolic link
      if (cleanup && !Files.isSymbolicLink(actorsDir)) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(actorsDir)) {
          for (Path path : directoryStream) {
            // has tmm been shut down?
            if (Thread.interrupted()) {
              return;
            }

            if (Utils.isRegularFile(path) && path.getFileName().toString().matches("(?i).*\\.(tbn|png|jpg)")
                && !path.getFileName().toString().startsWith(".")) {
              boolean found = false;
              // check if there is an actor for this file
              String actorImage = FilenameUtils.getBaseName(path.getFileName().toString()).replace("_", " ");
              for (Person actor : persons) {
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
                Utils.deleteFileWithBackup(path, mediaEntity.getDataSource());
              }
            }
          }
        }
        catch (IOException ignored) {
          // just ignore here
        }
      }

      // second - download images
      for (Person person : persons) {
        try {
          downloadPersonImage(person);
        }
        catch (InterruptedException | InterruptedIOException e) {
          LOGGER.info("artwork download aborted");
          throw e;
        }
        catch (Exception e) {
          LOGGER.warn("Problem downloading actor artwork: {}", e.getMessage());
        }
      }
    }
    catch (InterruptedException | InterruptedIOException e) {
      // re-interrupt the thread
      Thread.currentThread().interrupt();
    }
    catch (Exception e) {
      // log any other exception
      LOGGER.error("Thread crashed: ", e);
    }
  }

  private void downloadPersonImage(Person person) throws InterruptedException, IOException {
    String actorImageFilename = person.getNameForStorage();
    if (StringUtils.isBlank(actorImageFilename)) {
      return;
    }

    Path actorImage = Paths.get(mediaEntity.getPath(), Person.ACTOR_DIR, actorImageFilename);

    if (StringUtils.isNotEmpty(person.getThumbUrl())) {
      Path cache = ImageCache.getCachedFile(person.getThumbUrl());
      if (cache != null) {
        LOGGER.debug("using cached version of: {}", person.getThumbUrl());
        Utils.copyFileSafe(cache, actorImage, true);
        // last but not least clean/rebuild the image cache for the new file
        ImageCache.cacheImageSilently(actorImage);
      }
      else {
        // no cache file found - directly download it
        ImageUtils.downloadImage(person.getThumbUrl(), actorImage.getParent(), actorImageFilename);

        // last but not least clean/rebuild the image cache for the new file
        ImageCache.invalidateCachedImage(actorImage);
        ImageCache.cacheImageSilently(actorImage);
      }
    }
  }
}
