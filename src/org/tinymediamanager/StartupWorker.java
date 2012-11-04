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

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.ui.MainWindow;

/**
 * @author manuel
 * 
 */
public class StartupWorker extends SwingWorker<String, Void> {
  private static final Logger LOGGER = Logger.getLogger(StartupWorker.class);

  @Override
  /**
   * all methods called in this function
   * are done in the background and will not
   * block the GUI
   */
  protected String doInBackground() throws Exception {
    TinyMediaManagerSplash splash = TinyMediaManagerSplash.getInstance();
    TinyMediaManagerSplash.splash();

    Thread.sleep(1000);

    // get logger configuration
    splash.setProgress("loading logger", 10);
    PropertyConfigurator.configure(TinyMediaManager.class.getResource("log4j.conf"));
    LOGGER.debug("starting tinyMediaManager");
    LOGGER.debug("default encoding " + System.getProperty("file.encoding"));

    Thread.sleep(1000);

    // initialize database
    splash.setProgress("initialize database", 20);
    LOGGER.debug("initialize database");
    Globals.startDatabase();
    LOGGER.debug("database opened");

    Thread.sleep(1000);

    // proxy settings
    if (Globals.settings.useProxy()) {
      LOGGER.debug("setting proxy");
      Globals.settings.setProxy();
    }

    // load database
    splash.setProgress("loading database", 30);
    MovieList movieList = MovieList.getInstance();
    movieList.loadMoviesFromDatabase();

    // launch application
    splash.setProgress("loading ui", 90);
    MainWindow window = new MainWindow("tinyMediaManager " + org.tinymediamanager.ReleaseInfo.getVersion());

    return "";
  }

  protected void done() {
    // close splash screen
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        TinyMediaManagerSplash.disposeSplash();
      }
    });

    // // show main window
    // SwingUtilities.invokeLater(new Runnable() {
    // public void run() {
    // new MainWindow(); // start main gui
    // }
    // });
  }
}
