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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.EmptyFileException;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.threading.TmmThreadPool;

/**
 * The Class ImageCacheTask. Cache a bunch of images in a separate task
 * 
 * @author Manuel Laggner
 */
public class ImageCacheTask extends TmmThreadPool {
  private static final Logger         LOGGER       = LoggerFactory.getLogger(ImageCacheTask.class);
  private static final ResourceBundle BUNDLE       = ResourceBundle.getBundle("messages", new UTF8Control());

  private List<MediaFile>             filesToCache = new ArrayList<>();

  @Override
  public void callback(Object obj) {
    publishState(progressDone);
  }

  public ImageCacheTask(List<MediaFile> files) {
    super(BUNDLE.getString("tmm.rebuildimagecache"));
    filesToCache.addAll(files);
  }

  @Override
  protected void doInBackground() {
    // distribute the work over all available cores
    int threadCount = Runtime.getRuntime().availableProcessors() - 1;
    if (threadCount < 2) {
      threadCount = 2;
    }

    initThreadPool(threadCount, "imageCache");

    for (MediaFile fileToCache : filesToCache) {
      if (cancel) {
        return;
      }
      submitTask(new CacheTask(fileToCache));
    }
    waitForCompletionOrCancel();
  }

  private class CacheTask implements Callable<Object> {
    private final MediaFile fileToCache;

    CacheTask(MediaFile fileToCache) {
      this.fileToCache = fileToCache;
    }

    @Override
    public Object call() {
      try {
        // sleep 50ms to let the system calm down from a previous task
        Thread.sleep(50);
        ImageCache.cacheImage(fileToCache);
      }
      catch (EmptyFileException e) {
        LOGGER.warn("failed to cache file (file is empty): {}", fileToCache);
      }
      catch (FileNotFoundException e) {
        LOGGER.warn("file '{}' has not been found", fileToCache.getFilename());
      }
      catch (Exception e) {
        LOGGER.warn("failed to cache file: {} - {}", fileToCache.getFile(), e.getMessage());
      }
      return null;
    }
  }
}
