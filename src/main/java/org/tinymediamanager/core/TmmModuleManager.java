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
package org.tinymediamanager.core;

import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;

/**
 * The class TmmModuleManager. Used to manage all modules inside tmm
 * 
 * @author Manuel Laggner
 */
public class TmmModuleManager {
  private static final Logger     LOGGER = LoggerFactory.getLogger(TmmModuleManager.class);
  private static TmmModuleManager instance;

  private Set<ITmmModule>         modules;

  private TmmModuleManager() {
    modules = new LinkedHashSet<>();
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

  public Set<ITmmModule> getModules() {
    return modules;
  }

  /**
   * start up tmm - do initialization code here
   */
  public void startUp() {

  }

  /**
   * shutdown tmm - forces all registered modules to shut down
   */
  public void shutDown() {
    // shutdown modules
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

    // do cleanup tasks
  }

  /**
   * initialize databases of all modules
   */
  public void initializeDatabase() {
    for (ITmmModule module : modules) {
      try {
        if (module.isEnabled()) {
          module.shutDown();
        }
        module.initializeDatabase();
      }
      catch (Exception e) {
        LOGGER.error("problem shutting down " + module.getModuleTitle() + ": " + e.getMessage());
      }
    }
  }

  /**
   * trigger saveSettings for tmm and all modules
   */
  public void saveSettings() {
    Globals.settings.saveSettings();

    for (ITmmModule module : modules) {
      try {
        if (module.isEnabled()) {
          module.saveSettings();
        }
      }
      catch (Exception e) {
        LOGGER.error("saving settings " + module.getModuleTitle() + ": " + e.getMessage());
      }
    }
  }
}
