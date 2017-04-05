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
package org.tinymediamanager.core;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class ImageCacheTask. Cache a bunch of images in a separate task
 * 
 * @author Manuel Laggner
 */
public class ImageCacheTask extends TmmTask {
  private static final Logger         LOGGER       = LoggerFactory.getLogger(ImageCacheTask.class);
  private static final ResourceBundle BUNDLE       = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private List<Path>                  filesToCache = new ArrayList<>();

  public ImageCacheTask(String pathToFile) {
    super(BUNDLE.getString("tmm.rebuildimagecache"), 1, TaskType.BACKGROUND_TASK);
    filesToCache.add(Paths.get(pathToFile));
  }

  @Deprecated
  public ImageCacheTask(File file) {
    super(BUNDLE.getString("tmm.rebuildimagecache"), 1, TaskType.BACKGROUND_TASK);
    filesToCache.add(file.toPath());
  }

  public ImageCacheTask(Path file) {
    super(BUNDLE.getString("tmm.rebuildimagecache"), 1, TaskType.BACKGROUND_TASK);
    filesToCache.add(file);
  }

  public ImageCacheTask(List<Path> files) {
    super(BUNDLE.getString("tmm.rebuildimagecache"), files.size(), TaskType.BACKGROUND_TASK);
    filesToCache.addAll(files);
  }

  @Override
  protected void doInBackground() {
    int i = 0;
    for (Path fileToCache : filesToCache) {
      try {
        if (cancel) {
          return;
        }

        publishState(++i);
        ImageCache.cacheImage(fileToCache);
      }
      catch (EmptyFileException e) {
        LOGGER.warn("failed to cache file (file is empty): " + fileToCache);
      }
      catch (Exception e) {
        LOGGER.warn("failed to cache file: " + fileToCache);
      }
    }
  }
}
