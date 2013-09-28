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

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ui.TmmSwingWorker;

/**
 * The Class TmmThreadPool.
 * 
 * @author Myron Boyle
 * @version $Id$
 */
public abstract class TmmThreadPool extends TmmSwingWorker {

  /** The Constant LOGGER. */
  private static final Logger       LOGGER    = LoggerFactory.getLogger(TmmThreadPool.class);

  /** The pool. */
  private ThreadPoolExecutor        pool      = null;

  /** The service. */
  private CompletionService<Object> service   = null;

  /** The cancel. */
  protected boolean                 cancel    = false;

  /** The taskcount. */
  private int                       taskcount = 0;

  /** The taskdone. */
  private int                       taskdone  = 0;

  /** The poolname. */
  private String                    poolname  = "";

  /**
   * create new ThreadPool.
   * 
   * @param threads
   *          amount of threads
   * @param name
   *          a name for the logging
   */
  public void initThreadPool(int threads, String name) {
    this.taskcount = 0;
    this.taskdone = 0;
    this.cancel = false;
    this.poolname = name;
    pool = new ThreadPoolExecutor(threads, threads, // max threads
        2, TimeUnit.SECONDS, // time to wait before closing idle workers
        new LinkedBlockingQueue<Runnable>(), // our queue
        new TmmThreadFactory(name) // our thread settings
    );
    pool.allowCoreThreadTimeOut(true);
    this.service = new ExecutorCompletionService<Object>(pool);
  }

  /**
   * submits a new callable to thread pool.
   * 
   * @param task
   *          the callable
   */
  public void submitTask(Callable<Object> task) {
    if (!cancel) {
      taskcount++;
      service.submit(task);
    }
  }

  /**
   * submits a new runnable to thread pool.
   * 
   * @param task
   *          the runnable
   */
  public void submitTask(Runnable task) {
    if (!cancel) {
      taskcount++;
      service.submit(task, null);
    }
  }

  /**
   * Wait for completion or cancel.
   */
  public void waitForCompletionOrCancel() {
    pool.shutdown();
    while (!cancel && !pool.isTerminated() && taskdone < taskcount) {
      try {
        final Future<Object> future = service.take();
        taskdone++;
        callback(future.get());
      }
      catch (InterruptedException e) {
        LOGGER.error("ThreadPool " + this.poolname + " interrupted!", e);
      }
      catch (ExecutionException e) {
        LOGGER.error("ThreadPool " + this.poolname + ": Error getting result!", e);
      }
    }
    if (cancel) {
      try {
        LOGGER.info("Abort queue (discarding " + (getTaskcount() - getTaskdone()) + " tasks)");
        pool.getQueue().clear();
        pool.awaitTermination(3, TimeUnit.SECONDS);

        // shutdown now can cause a inconsistency because it will call Thread.interrupt which can cause a (sub)thread to crash
        // pool.shutdownNow();
        pool.shutdown();
      }
      catch (InterruptedException e) {
        LOGGER.error("ThreadPool " + this.poolname + " interrupted in shutdown!", e);
      }
    }
  }

  /**
   * callback for result.
   * 
   * @param obj
   *          the result of the finished thread.
   */
  public abstract void callback(Object obj);

  /**
   * returns the amount of submitted tasks.
   * 
   * @return the taskcount
   */
  public int getTaskcount() {
    return taskcount;
  }

  /**
   * returns the amount of executed tasks.
   * 
   * @return the taskdone
   */
  public int getTaskdone() {
    return taskdone;
  }

  /**
   * cancel the pool.
   */
  public void cancelThreadPool() {
    this.cancel = true;
  }

  /**
   * a copy of the default thread factory, just to set the pool name.
   */
  static class TmmThreadFactory implements ThreadFactory {
    // static final AtomicInteger poolNumber = new AtomicInteger(1);
    /** The group. */
    final ThreadGroup   group;

    /** The thread number. */
    final AtomicInteger threadNumber = new AtomicInteger(1);

    /** The name prefix. */
    final String        namePrefix;

    /**
     * Instantiates a new tmm thread factory.
     * 
     * @param poolname
     *          the poolname
     */
    TmmThreadFactory(String poolname) {
      SecurityManager s = System.getSecurityManager();
      group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
      namePrefix = "tmmpool-" + poolname + "-thread-";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    public Thread newThread(Runnable r) {
      Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
      if (t.isDaemon()) {
        t.setDaemon(false);
      }
      if (t.getPriority() != Thread.NORM_PRIORITY) {
        t.setPriority(Thread.NORM_PRIORITY);
      }
      return t;
    }
  }
}
