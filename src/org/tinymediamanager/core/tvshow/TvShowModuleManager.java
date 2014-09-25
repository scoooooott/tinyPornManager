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
package org.tinymediamanager.core.tvshow;

import java.io.File;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.ITmmModule;
import org.tinymediamanager.core.TmmModuleManager;

/**
 * The class TvShowModuleManager. Used to manage the tv show module
 * 
 * @author Manuel Laggner
 */
public class TvShowModuleManager implements ITmmModule {
  private static final String        MODULE_TITLE = "TV show management";
  // private static final String TV_SHOW_DB = "tvshow.odb";
  private static TvShowModuleManager instance;

  private boolean                    enabled;
  private EntityManager              entityManager;

  private TvShowModuleManager() {
    enabled = false;
  }

  public static TvShowModuleManager getInstance() {
    if (instance == null) {
      instance = new TvShowModuleManager();
    }
    return instance;
  }

  @Override
  public String getModuleTitle() {
    return MODULE_TITLE;
  }

  @Override
  public void startUp() throws Exception {
    // // enhance if needed
    // if (System.getProperty("tmmenhancer") != null) {
    // com.objectdb.Enhancer.enhance("org.tinymediamanager.core.entities.*");
    // com.objectdb.Enhancer.enhance("org.tinymediamanager.core.tvshow.entities.*");
    // com.objectdb.Enhancer.enhance("org.tinymediamanager.scraper.MediaTrailer");
    // }
    //
    // // get a connection to the database
    // entityManagerFactory = Persistence.createEntityManagerFactory(TV_SHOW_DB);
    // try {
    // entityManager = entityManagerFactory.createEntityManager();
    // }
    // catch (PersistenceException e) {
    // if (e.getCause().getMessage().contains("does not match db file")) {
    // // happens when there's a recovery file which does not match (cannot be recovered) - just delete and try again
    // FileUtils.deleteQuietly(new File(TV_SHOW_DB + "$"));
    // entityManager = entityManagerFactory.createEntityManager();
    // }
    // else {
    // // unknown
    // throw (e);
    // }
    // }

    // temp solution for a combined DB
    entityManager = TmmModuleManager.getInstance().getEntityManager();

    TvShowList.getInstance().loadTvShowsFromDatabase(entityManager);
    enabled = true;
  }

  @Override
  public void shutDown() throws Exception {
    // EntityManagerFactory entityManagerFactory = entityManager.getEntityManagerFactory();
    // entityManager.close();
    // entityManagerFactory.close();

    enabled = false;

    if (Globals.settings.isDeleteTrashOnExit()) {
      for (String ds : Globals.settings.getTvShowSettings().getTvShowDataSource()) {
        File file = new File(ds + File.separator + Constants.BACKUP_FOLDER);
        FileUtils.deleteQuietly(file);
      }
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  public EntityManager getEntityManager() {
    return entityManager;
  }
}
