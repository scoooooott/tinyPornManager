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

import java.awt.EventQueue;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.ui.MainWindow;

/**
 * The Class TinyMediaManager.
 */
public class TinyMediaManager {

  private static final Logger LOGGER = Logger.getLogger(TinyMediaManager.class);

  /**
   * The main method.
   * 
   * @param args
   *          the arguments
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      private TinyMediaManagerSplash splash;

      public void run() {
        try {

          // Get the native look and feel class name
          String laf = UIManager.getSystemLookAndFeelClassName();

          // Get the look and feel class name
          // String laf = "com.jtattoo.plaf.smart.SmartLookAndFeel";
          // String laf =
          // "de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel";

          // Install the look and feel
          UIManager.setLookAndFeel(laf);

          // // initialize splash screen
          // StartupWorker worker = new StartupWorker();
          // worker.execute();

          // get logger configuration
          // updateProgress("loading logger", 10);
          PropertyConfigurator.configure(TinyMediaManager.class.getResource("log4j.conf"));
          // DOMConfigurator.configure(TinyMediaManager.class.getResource("log4j.xml"));
          LOGGER.debug("starting tinyMediaManager");
          LOGGER.debug("default encoding " + System.getProperty("file.encoding"));

          // initialize database
          // updateProgress("initialize database", 20);
          LOGGER.debug("initialize database");
          Globals.startDatabase();
          LOGGER.debug("database opened");

          // proxy settings
          if (Globals.settings.useProxy()) {
            LOGGER.debug("setting proxy");
            Globals.settings.setProxy();
          }

          // load database
          // updateProgress("loading database", 30);
          MovieList movieList = MovieList.getInstance();
          movieList.loadMoviesFromDatabase();

          // launch application
          // updateProgress("loading ui", 90);
          MainWindow window = new MainWindow("tinyMediaManager " + org.tinymediamanager.ReleaseInfo.getVersion());

          // stopSplash();

        } catch (javax.persistence.PersistenceException e) {
          JOptionPane.showMessageDialog(null, e.getMessage());
        } catch (Exception e) {
          JOptionPane.showMessageDialog(null, e.getMessage());
          LOGGER.error("start of tmm", e);
        }
      }
    });
  }
}
