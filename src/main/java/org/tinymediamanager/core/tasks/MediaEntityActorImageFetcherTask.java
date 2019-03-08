/*
 * Copyright 2012 - 2019 Manuel Laggner
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.scraper.http.Url;
import org.tinymediamanager.scraper.util.UrlUtil;

/**
 * The class MediaEntityActorImageFetcherTask.
 * 
 * @author Manuel Laggner
 */
public abstract class MediaEntityActorImageFetcherTask implements Runnable {

  private final static Logger LOGGER = LoggerFactory.getLogger(MediaEntityActorImageFetcherTask.class);

  protected MediaEntity       mediaEntity;
  protected Set<Person>       persons;

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
      }

      // second - download images
      for (Person person : persons) {
        try {
          downloadPersonImage(person);
        }
        catch (InterruptedIOException e) {
          LOGGER.info("artwork download aborted");
          throw e;
        }
        catch (Exception e) {
          LOGGER.warn("Problem downloading actor artwork: {}", e.getMessage());
        }
      }
    }
    catch (InterruptedIOException e) {
      // re-interrupt the thread
      Thread.currentThread().interrupt();
    }
    catch (Exception e) {
      // log any other exception
      LOGGER.error("Thread crashed: ", e);
    }
  }

  private void downloadPersonImage(Person person) throws IOException {
    String actorImageFilename = person.getNameForStorage();
    if (StringUtils.isBlank(actorImageFilename)) {
      return;
    }

    Path tempFile = null;
    Path actorImage = Paths.get(mediaEntity.getPath(), Person.ACTOR_DIR, actorImageFilename);

    if (StringUtils.isNotEmpty(person.getThumbUrl())) {
      Path cache = ImageCache.getCachedFile(person.getThumbUrl());
      if (cache != null) {
        LOGGER.debug("using cached version of: {}", person.getThumbUrl());
        Utils.copyFileSafe(cache, actorImage, true);
        // last but not least clean/rebuild the image cache for the new file
        ImageCache.invalidateCachedImage(actorImage);
        ImageCache.cacheImageSilently(actorImage);
      }
      else {
        // no cache file found - directly download it
        try {
          // create a temp file/folder inside the tmm folder
          Path tempFolder = Paths.get(Constants.TEMP_FOLDER);
          if (!Files.exists(tempFolder)) {
            Files.createDirectory(tempFolder);
          }
          tempFile = tempFolder.resolve(actorImageFilename + ".part");
        }
        catch (Exception e) {
          // could not create the temp folder somehow - put the files into the actor folder
          tempFile = Paths.get(mediaEntity.getPath(), Person.ACTOR_DIR, actorImageFilename + ".part");
        }

        Url url = new Url(person.getThumbUrl());

        try (FileOutputStream outputStream = new FileOutputStream(tempFile.toFile());
            // fetch the images with at max 5 retries
            InputStream is = url.getInputStreamWithRetry(5)) {

          if (url.isFault() || is == null) {
            // 404 et all
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
        }

        // check if the file has been downloaded
        if (!Files.exists(tempFile) || Files.size(tempFile) == 0) {
          // cleanup the file
          FileUtils.deleteQuietly(tempFile.toFile());
          throw new IOException("0byte file downloaded: " + actorImageFilename);
        }

        // delete new destination if existing
        Utils.deleteFileSafely(actorImage);

        // move the temp file to the expected filename
        if (!Utils.moveFileSafe(tempFile, actorImage)) {
          throw new IOException("renaming temp file failed: " + actorImageFilename);
        }

        // last but not least clean/rebuild the image cache for the new file
        ImageCache.invalidateCachedImage(actorImage);
        ImageCache.cacheImageSilently(actorImage);
      }
    }

    // remove temp file
    if (tempFile != null && Files.exists(tempFile)) {
      Utils.deleteFileSafely(tempFile);
    }

  }
}
