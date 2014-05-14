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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.entities.MediaFile;

/**
 * The Class ImageCacheTask. Cache a bunch of images in a separate task
 * 
 * @author Manuel Laggner
 */
public class ImageCacheTask implements Runnable {
  private static final Logger LOGGER       = LoggerFactory.getLogger(ImageCacheTask.class);

  private List<File>          filesToCache = new ArrayList<File>();

  public ImageCacheTask(String pathToFile) {
    filesToCache.add(new File(pathToFile));
  }

  public ImageCacheTask(File file) {
    filesToCache.add(file);
  }

  public ImageCacheTask(List<File> files) {
    filesToCache.addAll(files);
  }

  @Override
  public void run() {
    for (File fileToCache : filesToCache) {
      try {
        ImageCache.cacheImage(new MediaFile(fileToCache));
      }
      catch (Exception e) {
        LOGGER.warn("failed to cache file: " + fileToCache.getPath());
      }
    }
  }
}
