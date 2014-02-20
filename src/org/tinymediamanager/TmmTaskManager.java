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
package org.tinymediamanager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.tinymediamanager.TmmThreadPool.TmmThreadFactory;
import org.tinymediamanager.ui.TmmSwingWorker;

/**
 * The class TmmTaskManager. Used to manage all tasks within tmm (except the helper tasks, e.g. scraper sub tasks)
 * 
 * @author Manuel Laggner
 */
public class TmmTaskManager {
  // we have some "named" queues, holding different types of tasks
  // image download and subtitle download are rather small/fast tasks - we only queue them in a queue and provide to abort the complete queue
  private static ThreadPoolExecutor                  imageDownloadExecutor;
  private static ThreadPoolExecutor                  subtitleDownloadExecutor;

  // trailer download are rather big/long running tasks; only x at a time can be run and they are able to be cancelled individually
  private static ThreadPoolExecutor                  downloadExecutor;
  protected static final Queue<TmmSwingWorker<?, ?>> ACTIVE_DOWNLOAD_TASKS = new ConcurrentLinkedQueue<TmmSwingWorker<?, ?>>();

  // main tasks (update datasource, scraping, renaming) are queueable tasks, but only one at a time can run; they can be cancelled individually
  private final static ThreadPoolExecutor            mainTaskExecutor      = new ThreadPoolExecutor(1, 1, // max threads
                                                                               2, TimeUnit.SECONDS, // time to wait before closing idle workers
                                                                               new LinkedBlockingQueue<Runnable>(), // our queue
                                                                               new TmmThreadFactory("main-task"));

  public static void addImageDownloadTask(Runnable task) {
    if (imageDownloadExecutor == null || imageDownloadExecutor.isShutdown()) {
      imageDownloadExecutor = new ThreadPoolExecutor(3, 3, 2, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new TmmThreadFactory(
          "imageDownload-task"));
    }
    imageDownloadExecutor.execute(task);
  }

  public static void addDownloadTask(TmmSwingWorker<?, ?> task) {
    if (downloadExecutor == null) {
      downloadExecutor = new ThreadPoolExecutor(1, 1, 2, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new TmmThreadFactory("download-task"));
    }
    ACTIVE_DOWNLOAD_TASKS.add(task);
    downloadExecutor.execute(task);
  }

  public static void removeDownloadTask(TmmSwingWorker<?, ?> task) {
    if (downloadExecutor != null) {
      downloadExecutor.remove(task);
    }
    ACTIVE_DOWNLOAD_TASKS.remove(task);
  }

  public static Queue<TmmSwingWorker<?, ?>> getActiveDownloadTasks() {
    return ACTIVE_DOWNLOAD_TASKS;
  }
}
