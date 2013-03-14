/*
 * Copyright 2012 Manuel Laggner
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.tinymediamanager.core.Settings;
import org.tinymediamanager.ui.MainWindow;

/**
 * The Class Globals.
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
  public static final ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, // max threads
                                                      5, TimeUnit.SECONDS, // time to wait before closing idlo
                                                      new LinkedBlockingQueue<Runnable>() // our queue
                                                  );

  /**
   * Start database.
   * 
   * @throws Exception
   *           the exception
   */
  public static void startDatabase() throws Exception {
    emf = Persistence.createEntityManagerFactory("tmm.odb");
    entityManager = emf.createEntityManager();
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

  /** The logo */
  public final static Image logo = Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/org/tinymediamanager/ui/images/tmm.png"));
}
