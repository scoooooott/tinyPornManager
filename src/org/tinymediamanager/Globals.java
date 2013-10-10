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

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.apache.commons.io.FileUtils;
import org.tinymediamanager.TmmThreadPool.TmmThreadFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.ui.MainWindow;

/**
 * The Class Globals.
 * 
 * @author Manuel Laggner
 */
public class Globals {

  /** The settings. */
  public static final Settings           settings = Settings.getInstance();

  /** The emf. */
  private static EntityManagerFactory    emf;

  /** The entity manager. */
  public static EntityManager            entityManager;

  // public static final ExecutorService executor2 = Executors.newFixedThreadPool(10);
  // see source of newFixedThreadPool
  // see weird logic: http://www.kimchy.org/juc-executorservice-gotcha/
  /** The Constant executor. */
  public static final ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, // max threads
                                                      2, TimeUnit.SECONDS, // time to wait before closing idle workers
                                                      new LinkedBlockingQueue<Runnable>(), // our queue
                                                      new TmmThreadFactory("global"));

  /**
   * Start database.
   * 
   * @throws Exception
   *           the exception
   */
  public static void startDatabase() throws Exception {
    emf = Persistence.createEntityManagerFactory(Constants.DB);
    try {
      entityManager = emf.createEntityManager();
    }
    catch (PersistenceException e) {
      // happens when there's a recovery file which does not match (cannot be recovered) - just delete and try again
      FileUtils.deleteQuietly(new File(Constants.DB + "$"));
      entityManager = emf.createEntityManager();
    }
  }

  /**
   * Shutdown database.
   * 
   * @throws Exception
   *           the exception
   */
  public static void shutdownDatabase() throws Exception {
    entityManager.close();
    emf.close();
  }

  /** The logo. */
  public final static Image logo = Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/org/tinymediamanager/ui/images/tmm.png"));

  /**
   * is a TMM thread pool running?!
   */
  public static boolean poolRunning() {
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
  public static boolean checkForThreadAlive(String contains) {
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
  private static Thread[] getAllThreads() {
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
  private static String getThreadName(Thread t) {
    return (t != null && !isEmpty(t.getName())) ? t.getName().toLowerCase() : "";
  }

  private static boolean isEmpty(CharSequence cs) {
    return cs == null || cs.length() == 0;
  }
}
