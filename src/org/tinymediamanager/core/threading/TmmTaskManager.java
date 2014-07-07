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
package org.tinymediamanager.core.threading;

import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.tinymediamanager.core.threading.TmmTaskHandle.TaskState;
import org.tinymediamanager.core.threading.TmmThreadPool.TmmThreadFactory;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The class TmmTaskManager. Used to manage all tasks within tmm (except the helper tasks, e.g. scraper sub tasks)
 * 
 * @author Manuel Laggner
 */
public class TmmTaskManager implements TmmTaskListener {
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private final static TmmTaskManager instance         = new TmmTaskManager();
  private final Set<TmmTaskListener>  taskListener     = new CopyOnWriteArraySet<TmmTaskListener>();
  private final Set<TmmTaskHandle>    runningTasks     = new CopyOnWriteArraySet<TmmTaskHandle>();

  // we have some "named" queues, holding different types of tasks
  // image download/subtitle download are rather small/fast tasks - we only queue them in a queue and provide to abort the complete queue
  private ThreadPoolExecutor          imageDownloadExecutor;

  // this is a queue which holds "other" tasks
  private ThreadPoolExecutor          unnamedTaskExecutor;

  // trailer download are rather big/long running tasks; only x at a time can be run and they are able to be cancelled individually
  private ThreadPoolExecutor          downloadExecutor;

  // main tasks (update datasource, scraping, renaming) are queueable tasks, but only one at a time can run; they can be cancelled individually
  private final ThreadPoolExecutor    mainTaskExecutor = createMainTaskQueue();

  // fake task handles to manage queues
  private TmmTaskHandle               imageQueueHandle;
  private TmmTaskHandle               unnamedQueueHandle;

  private TmmTaskManager() {
    imageQueueHandle = new ImageQueueTaskHandle();
    unnamedQueueHandle = new UnnamedQueueTaskHandle();
  }

  public static TmmTaskManager getInstance() {
    return instance;
  }

  public void addTaskListener(TmmTaskListener listener) {
    taskListener.add(listener);
  }

  public void removeTaskListener(TmmTaskListener listener) {
    taskListener.remove(listener);
  }

  private ThreadPoolExecutor createImageDownloadExecutor() {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 3, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new TmmThreadFactory(
        "image-download-task")) {
      @Override
      protected void beforeExecute(Thread d, Runnable r) {
        super.beforeExecute(d, r);
        if (imageQueueHandle != null) {
          processTaskEvent(imageQueueHandle);
        }
      }

      @Override
      protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (imageQueueHandle != null) {
          processTaskEvent(imageQueueHandle);
        }
      }
    };
    executor.allowCoreThreadTimeOut(true);
    return executor;
  }

  private ThreadPoolExecutor createUnnamedTaskExecutor() {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 3, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new TmmThreadFactory(
        "unnamed-task")) {
      @Override
      protected void beforeExecute(Thread d, Runnable r) {
        super.beforeExecute(d, r);
        if (unnamedQueueHandle != null) {
          processTaskEvent(unnamedQueueHandle);
        }
      }

      @Override
      protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (unnamedQueueHandle != null) {
          processTaskEvent(unnamedQueueHandle);
        }
      }
    };
    executor.allowCoreThreadTimeOut(true);
    return executor;
  }

  /**
   * add a image download task to the queue
   * 
   * @param task
   *          the task to be added
   */
  public void addImageDownloadTask(Runnable task) {
    if (imageDownloadExecutor == null || imageDownloadExecutor.isShutdown()) {
      imageDownloadExecutor = createImageDownloadExecutor();
    }
    imageDownloadExecutor.execute(task);
  }

  /**
   * add a tasks which does not fit in the named queues (like caching or TV show episode scraping task)
   * 
   * @param task
   *          the task to be added
   */
  public void addUnnamedTask(Runnable task) {
    if (unnamedTaskExecutor == null || unnamedTaskExecutor.isShutdown()) {
      unnamedTaskExecutor = createUnnamedTaskExecutor();
    }
    if (task instanceof TmmTask) {
      TmmTask t = (TmmTask) task;
      t.addListener(this);
      t.setState(TaskState.QUEUED);
    }
    unnamedTaskExecutor.execute(task);
  }

  /**
   * add a download task to the queue
   * 
   * @param task
   *          the task to be added
   */
  public void addDownloadTask(TmmTask task) {
    if (downloadExecutor == null) {
      downloadExecutor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new TmmThreadFactory("download-task"));
      downloadExecutor.allowCoreThreadTimeOut(true);
    }
    task.addListener(this);
    task.setState(TaskState.QUEUED);
    downloadExecutor.execute(task);
  }

  /**
   * get the count of all open and running unnamed tasks
   * 
   * @return the count of all running and open unnamed tasks
   */
  public int getUnnamedOpenTaskCount() {
    if (unnamedTaskExecutor == null) {
      return 0;
    }
    return unnamedTaskExecutor.getQueue().size() + unnamedTaskExecutor.getActiveCount();
  }

  /**
   * cancel all open and running image downloads
   */
  public void cancelImageDownloads() {
    if (imageDownloadExecutor != null) {
      imageDownloadExecutor.shutdownNow();
    }
  }

  /**
   * cancel all open and running unnamed tasks
   */
  public void cancelUnnamedTasks() {
    if (unnamedTaskExecutor != null) {
      unnamedTaskExecutor.shutdownNow();
    }
  }

  /**
   * add a new task to the the main task queue
   * 
   * @param newTask
   *          the task to be added
   * @return true if there is alreday a main task running
   */
  public boolean addMainTask(TmmThreadPool newTask) {
    boolean result = false;
    newTask.addListener(this);
    newTask.setState(TaskState.QUEUED);
    mainTaskExecutor.execute(newTask);
    return result;
  }

  private ThreadPoolExecutor createMainTaskQueue() {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, // max threads
        1, TimeUnit.SECONDS, // time to wait before closing idle workers
        new LinkedBlockingQueue<Runnable>(), // our queue
        new TmmThreadFactory("main-task"));
    executor.allowCoreThreadTimeOut(true);
    return executor;
  }

  /**
   * shut down all threads
   */
  public void shutdown() {
    if (imageDownloadExecutor != null) {
      imageDownloadExecutor.shutdown();
    }
    if (unnamedTaskExecutor != null) {
      unnamedTaskExecutor.shutdown();
    }
    if (downloadExecutor != null) {
      downloadExecutor.shutdown();
    }
    if (mainTaskExecutor != null) {
      mainTaskExecutor.shutdown();
    }
    for (TmmTaskHandle task : runningTasks) {
      task.cancel();
    }
  }

  /**
   * hard shutdown of all tasks after a max of 4 secs waiting
   */
  public void shutdownNow() {
    if (poolRunning()) {
      // give the threads 4 seconds to finish
      try {
        Thread.sleep(4000);
      }
      catch (Exception e) {
      }
    }

    // check if all finished
    if (imageDownloadExecutor != null && !imageDownloadExecutor.isTerminated()) {
      imageDownloadExecutor.shutdownNow();
    }
    if (unnamedTaskExecutor != null && !unnamedTaskExecutor.isTerminated()) {
      unnamedTaskExecutor.shutdownNow();
    }
    if (downloadExecutor != null && !downloadExecutor.isTerminated()) {
      downloadExecutor.shutdownNow();
    }
    if (mainTaskExecutor != null && !mainTaskExecutor.isTerminated()) {
      mainTaskExecutor.shutdownNow();
    }
  }

  /**
   * is a TMM thread pool running?!
   */
  public boolean poolRunning() {
    if (checkForThreadAlive("tmmpool")) {
      return true;
    }
    return false;
  }

  /**
   * Look for a text in name of running threads to check if some threads have not shut down yet
   * 
   * @param contains
   *          the String to look for in thread name
   * @return true if a running thread's name contains given String
   */
  private boolean checkForThreadAlive(String contains) {
    for (Thread t : getAllThreads()) {
      if (t.isAlive() && getThreadName(t).contains(contains)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get all threads of our own threadgroup (threads started by our webapplication).
   * 
   * @return all threads
   */
  private Thread[] getAllThreads() {
    ThreadGroup root = Thread.currentThread().getThreadGroup();

    int nAlloc = root.activeCount();
    int n = 0;
    Thread[] threads;
    do {
      nAlloc *= 2;
      threads = new Thread[nAlloc];
      n = root.enumerate(threads, true);
    } while (n == nAlloc);

    return java.util.Arrays.copyOf(threads, n);
  }

  /**
   * Get thread's name in lowercase.
   * 
   * @param t
   *          the thread
   * @return the thread name
   */
  private String getThreadName(Thread t) {
    return (t != null && !isEmpty(t.getName())) ? t.getName().toLowerCase() : "";
  }

  private static boolean isEmpty(CharSequence cs) {
    return cs == null || cs.length() == 0;
  }

  @Override
  public void processTaskEvent(TmmTaskHandle task) {
    if (task.getState() == TaskState.STARTED) {
      runningTasks.add(task);
    }
    if (task.getState() == TaskState.FINISHED) {
      runningTasks.remove(task);
    }
    for (TmmTaskListener listener : taskListener) {
      listener.processTaskEvent(task);
    }
  }

  /*************************************************************************
   * helper classes
   *************************************************************************/
  private class ImageQueueTaskHandle implements TmmTaskHandle {
    @Override
    public String getTaskName() {
      return BUNDLE.getString("task.imagedownloads");
    }

    @Override
    public int getWorkUnits() {
      return 0;
    }

    @Override
    public int getProgressDone() {
      return 0;
    }

    @Override
    public String getTaskDescription() {
      return getOpenTasks() + " " + BUNDLE.getString("task.remaining");
    }

    private int getOpenTasks() {
      int openTasks = 0;
      if (imageDownloadExecutor != null) {
        openTasks = imageDownloadExecutor.getQueue().size() + imageDownloadExecutor.getActiveCount();
      }
      return openTasks;
    }

    @Override
    public TaskState getState() {
      if (imageQueueHandle != null && getOpenTasks() > 0) {
        return TaskState.STARTED;
      }
      return TaskState.FINISHED;
    }

    @Override
    public TaskType getType() {
      return TaskType.BACKGROUND_TASK;
    }

    @Override
    public void cancel() {
      cancelImageDownloads();
    }
  }

  private class UnnamedQueueTaskHandle implements TmmTaskHandle {
    @Override
    public String getTaskName() {
      return BUNDLE.getString("task.othertasks");
    }

    @Override
    public int getWorkUnits() {
      return 0;
    }

    @Override
    public int getProgressDone() {
      return 0;
    }

    @Override
    public String getTaskDescription() {
      return getOpenTasks() + " " + BUNDLE.getString("task.remaining");
    }

    private int getOpenTasks() {
      int openTasks = 0;
      if (unnamedTaskExecutor != null) {
        openTasks = unnamedTaskExecutor.getQueue().size() + unnamedTaskExecutor.getActiveCount();
      }
      return openTasks;
    }

    @Override
    public TaskState getState() {
      if (unnamedTaskExecutor != null && getOpenTasks() > 0) {
        return TaskState.STARTED;
      }
      return TaskState.FINISHED;
    }

    @Override
    public TaskType getType() {
      return TaskType.BACKGROUND_TASK;
    }

    @Override
    public void cancel() {
      cancelImageDownloads();
    }
  }
}
