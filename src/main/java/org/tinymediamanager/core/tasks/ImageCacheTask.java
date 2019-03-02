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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.EmptyFileException;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class ImageCacheTask. Cache a bunch of images in a separate task
 * 
 * @author Manuel Laggner
 */
public class ImageCacheTask extends TmmThreadPool {
  private static final Logger         LOGGER       = LoggerFactory.getLogger(ImageCacheTask.class);
  private static final ResourceBundle BUNDLE       = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private List<Path>                  filesToCache = new ArrayList<>();

  public ImageCacheTask(String pathToFile) {
    super(BUNDLE.getString("tmm.rebuildimagecache"));
    filesToCache.add(Paths.get(pathToFile));
  }

  @Override
  public void callback(Object obj) {
    publishState(progressDone);
  }

  public ImageCacheTask(Path file) {
    super(BUNDLE.getString("tmm.rebuildimagecache"));
    filesToCache.add(file);
  }

  public ImageCacheTask(List<Path> files) {
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

    int i = 0;
    for (Path fileToCache : filesToCache) {
      if (cancel) {
        return;
      }
      submitTask(new CacheTask(fileToCache));
    }
    waitForCompletionOrCancel();
  }

  private class CacheTask implements Callable<Object> {
    private final Path fileToCache;

    public CacheTask(Path fileToCache) {
      this.fileToCache = fileToCache;
    }

    @Override
    public Object call() {
      try {
        ImageCache.cacheImage(fileToCache);
      }
      catch (EmptyFileException e) {
        LOGGER.warn("failed to cache file (file is empty): " + fileToCache);
      }
      catch (FileNotFoundException e) {
        LOGGER.warn("failed to cache file (file not found): " + fileToCache);
      }
      catch (Exception e) {
        LOGGER.warn("failed to cache file: " + fileToCache, e);
      }
      return null;
    }
  }
}
