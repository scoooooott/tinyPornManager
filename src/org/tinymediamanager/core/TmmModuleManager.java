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
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class TmmModuleManager. Used to manage all modules inside tmm
 * 
 * @author Manuel Laggner
 */
public class TmmModuleManager {
  private static final Logger     LOGGER = LoggerFactory.getLogger(TmmModuleManager.class);
  private static final String     TMM_DB = "tmm.odb";
  private static TmmModuleManager instance;

  private EntityManager           entityManager;
  private Set<ITmmModule>         modules;

  private TmmModuleManager() {
    modules = new LinkedHashSet<ITmmModule>();
  }

  public static TmmModuleManager getInstance() {
    if (instance == null) {
      instance = new TmmModuleManager();
    }
    return instance;
  }

  public void registerModule(ITmmModule module) {
    modules.add(module);
  }

  public void enableModule(ITmmModule module) throws Exception {
    if (!modules.contains(module)) {
      throw new Exception("module " + module.getModuleTitle() + " not registered");
    }

    module.startUp();
  }

  public void disableModule(ITmmModule module) throws Exception {
    if (!modules.contains(module)) {
      throw new Exception("module " + module.getModuleTitle() + " not registered");
    }

    module.shutDown();
  }

  /**
   * start up tmm - do initialization code here
   */
  public void startUp() {
    // enhance if needed
    if (System.getProperty("tmmenhancer") != null) {
      com.objectdb.Enhancer.enhance("org.tinymediamanager.core.entities.*");
      com.objectdb.Enhancer.enhance("org.tinymediamanager.core.movie.entities.*");
      com.objectdb.Enhancer.enhance("org.tinymediamanager.core.tvshow.entities.*");
      com.objectdb.Enhancer.enhance("org.tinymediamanager.scraper.MediaTrailer");
    }

    // get a connection to the database
    EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(TMM_DB);
    try {
      entityManager = entityManagerFactory.createEntityManager();
    }
    catch (PersistenceException e) {
      if (e.getCause().getMessage().contains("does not match db file")) {
        // happens when there's a recovery file which does not match (cannot be recovered) - just delete and try again
        FileUtils.deleteQuietly(new File(TMM_DB + "$"));
        entityManager = entityManagerFactory.createEntityManager();
      }
      else {
        // unknown
        throw (e);
      }
    }
  }

  /**
   * shutdown tmm - forces all registered modules to shut down
   */
  public void shutDown() {
    for (ITmmModule module : modules) {
      if (module.isEnabled()) {
        try {
          module.shutDown();
        }
        catch (Exception e) {
          LOGGER.error("problem shutting down " + module.getModuleTitle() + ": " + e.getMessage());
        }
      }
    }

    EntityManagerFactory emf = entityManager.getEntityManagerFactory();
    entityManager.close();
    emf.close();
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }
}
