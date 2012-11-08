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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.SplashScreen;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.ui.MainWindow;

// TODO: Auto-generated Javadoc
/**
 * The Class TinyMediaManager.
 */
public class TinyMediaManager {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = Logger.getLogger(TinyMediaManager.class);

  /**
   * The main method.
   * 
   * @param args
   *          the arguments
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      private SplashScreen splash;

      public void run() {
        try {
          // set look and feel
          setLookAndFeel();

          // init splash
          splash = SplashScreen.getSplashScreen();
          long timeStart = System.currentTimeMillis();

          // // initialize splash screen
          // StartupWorker worker = new StartupWorker();
          // worker.execute();

          // get logger configuration
          updateProgress("loading logger");
          PropertyConfigurator.configure(TinyMediaManager.class.getResource("log4j.conf"));
          // DOMConfigurator.configure(TinyMediaManager.class.getResource("log4j.xml"));
          LOGGER.debug("starting tinyMediaManager");
          LOGGER.debug("default encoding " + System.getProperty("file.encoding"));

          // initialize database
          updateProgress("initialize database");
          LOGGER.debug("initialize database");
          Globals.startDatabase();
          LOGGER.debug("database opened");

          // proxy settings
          if (Globals.settings.useProxy()) {
            LOGGER.debug("setting proxy");
            Globals.settings.setProxy();
          }

          // load database
          updateProgress("loading movies");
          MovieList movieList = MovieList.getInstance();
          movieList.loadMoviesFromDatabase();

          // launch application
          updateProgress("loading ui");
          long timeEnd = System.currentTimeMillis();
          if ((timeEnd - timeStart) > 3000) {
            try {
              Thread.sleep(3000 - (timeEnd - timeStart));
            }
            catch (Exception e) {
            }
          }
          MainWindow window = new MainWindow("tinyMediaManager " + org.tinymediamanager.ReleaseInfo.getVersion());

          // stopSplash();

        }
        catch (javax.persistence.PersistenceException e) {
          JOptionPane.showMessageDialog(null, e.getMessage());
        }
        catch (Exception e) {
          JOptionPane.showMessageDialog(null, e.getMessage());
          LOGGER.error("start of tmm", e);
        }
      }

      /**
       * Update progress on splash screen.
       * 
       * @param text
       *          the text
       */
      private void updateProgress(String text) {
        if (splash != null) {
          Graphics2D g2 = splash.createGraphics();
          if (g2 != null) {
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(20, 200, 480, 300);
            g2.setPaintMode();
            g2.setColor(Color.WHITE);
            g2.drawString(text + "...", 20, 300);
            splash.update();
          }
        }

      }

      /**
       * Sets the look and feel.
       * 
       * @throws Exception
       *           the exception
       */
      private void setLookAndFeel() throws Exception {
        // Get the native look and feel class name
        // String laf = UIManager.getSystemLookAndFeelClassName();

        // Get the look and feel class name
        com.jtattoo.plaf.luna.LunaLookAndFeel.setTheme("Default");
        String laf = "com.jtattoo.plaf.luna.LunaLookAndFeel";

        // Install the look and feel
        UIManager.setLookAndFeel(laf);
      }
    });
  }
}
