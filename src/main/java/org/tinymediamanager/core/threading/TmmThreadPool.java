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
package org.tinymediamanager.core.threading;

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

/**
 * The Class TmmThreadPool.
 * 
 * @author Myron Boyle, Manuel Laggner
 */
public abstract class TmmThreadPool extends TmmTask {
  private static final Logger       LOGGER  = LoggerFactory.getLogger(TmmThreadPool.class);

  private ThreadPoolExecutor        pool    = null;
  private CompletionService<Object> service = null;

  protected String                  poolname;

  protected TmmThreadPool(String taskName) {
    super(taskName, 0, TaskType.MAIN_TASK);
  }

  /**
   * create new ThreadPool.
   * 
   * @param threads
   *          amount of threads
   * @param name
   *          a name for the logging
   */
  protected void initThreadPool(int threads, String name) {
    this.cancel = false;
    this.poolname = name;
    pool = new ThreadPoolExecutor(threads, threads, // max threads
        2, TimeUnit.SECONDS, // time to wait before closing idle workers
        new LinkedBlockingQueue<>(), // our queue
        new TmmThreadFactory(name) // our thread settings
    );
    pool.allowCoreThreadTimeOut(true);
    this.service = new ExecutorCompletionService<>(pool);
  }

  /**
   * submits a new callable to thread pool.
   * 
   * @param task
   *          the callable
   */
  protected synchronized void submitTask(Callable<Object> task) {
    if (!cancel) {
      workUnits++;
      service.submit(task);
    }
  }

  /**
   * submits a new runnable to thread pool.
   * 
   * @param task
   *          the runnable
   */
  protected synchronized void submitTask(Runnable task) {
    if (!cancel) {
      workUnits++;
      service.submit(task, "");
    }
  }

  /**
   * Wait for completion or cancel.
   */
  protected void waitForCompletionOrCancel() {
    pool.shutdown();
    while (!cancel && !pool.isTerminated() && progressDone < workUnits) {
      try {
        final Future<Object> future = service.take();
        progressDone++;
        callback(future.get());
      }
      catch (InterruptedException e) {
        LOGGER.error("ThreadPool {} interrupted!", poolname);
        Thread.currentThread().interrupt();
      }
      catch (ExecutionException e) {
        LOGGER.error("ThreadPool {}: Error getting result! - {}", poolname, e);
      }
    }
    if (cancel) {
      try {
        LOGGER.info("Abort queue (discarding {} tasks", workUnits - progressDone);
        pool.getQueue().clear();
        pool.awaitTermination(3, TimeUnit.SECONDS);

        // shutdown now can cause a inconsistency because it will call Thread.interrupt which can cause a (sub)thread to crash
        pool.shutdown();
      }
      catch (InterruptedException e) {
        LOGGER.error("ThreadPool {} interrupted in shutdown! - {}", poolname, e);
        Thread.currentThread().interrupt();
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
   * a copy of the default thread factory, just to set the pool name.
   */
  static class TmmThreadFactory implements ThreadFactory {
    final ThreadGroup   group;
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final String        namePrefix;

    TmmThreadFactory(String poolname) {
      SecurityManager s = System.getSecurityManager();
      group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
      namePrefix = "tmmpool-" + poolname + "-T";
    }

    @Override
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
