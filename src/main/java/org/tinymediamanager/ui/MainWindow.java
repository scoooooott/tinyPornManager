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
package org.tinymediamanager.ui;

import static org.tinymediamanager.TinyMediaManager.shutdownLogger;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker.StateValue;
import javax.swing.Timer;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ITmmModule;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.tasks.UpdaterTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.thirdparty.MediaInfo;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.components.TextFieldPopupMenu;
import org.tinymediamanager.ui.components.TmmSplitPane;
import org.tinymediamanager.ui.components.toolbar.ToolbarPanel;
import org.tinymediamanager.ui.dialogs.UpdateDialog;
import org.tinymediamanager.ui.images.LogoCircle;
import org.tinymediamanager.ui.movies.MovieUIModule;
import org.tinymediamanager.ui.moviesets.MovieSetUIModule;
import org.tinymediamanager.ui.panels.StatusBarPanel;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

import com.sun.jna.Platform;

import net.miginfocom.swing.MigLayout;

/**
 * The Class MainWindow.
 * 
 * @author Manuel Laggner
 */
public class MainWindow extends JFrame {
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());
  private static final Logger         LOGGER           = LoggerFactory.getLogger(MainWindow.class);
  private static final long           serialVersionUID = 1L;

  public static final List<Image>     LOGOS            = createLogos();
  private static MainWindow           instance;

  private ToolbarPanel                toolbarPanel;
  private JTabbedPane                 tabbedPane;
  private JPanel                      detailPanel;
  private JSplitPane                  splitPane;
  private JPanel                      panelStatusBar;

  /**
   * Create the application.
   * 
   * @param name
   *          the name
   */
  public MainWindow(String name) {
    super(name);
    setName("mainWindow");
    setMinimumSize(new Dimension(1050, 700));

    instance = this;

    initialize();

    if (Boolean.parseBoolean(System.getProperty("tmm.noupdate")) != true) {
      checkForUpdate();
    }
  }

  /**
   * load all predefined logo sizes
   * 
   * @return a list of all predefined logos
   */
  private static List<Image> createLogos() {
    List<Image> logos = new ArrayList<>();

    logos.add(new LogoCircle(48).getImage());
    logos.add(new LogoCircle(64).getImage());
    logos.add(new LogoCircle(96).getImage());
    logos.add(new LogoCircle(128).getImage());
    logos.add(new LogoCircle(256).getImage());

    return logos;
  }

  private void checkForUpdate() {
    try {
      final UpdaterTask updateWorker = new UpdaterTask();

      updateWorker.addPropertyChangeListener(evt -> {
        if ("state".equals(evt.getPropertyName()) && evt.getNewValue() == StateValue.DONE) {
          try {
            boolean update = updateWorker.get();
            LOGGER.debug("update result was: " + update);
            if (update) {

              // we might need this somewhen...
              if (updateWorker.isForcedUpdate()) {
                LOGGER.info("Updating (forced)...");
                closeTmmAndStart(Utils.getPBforTMMupdate());
                return;
              }

              // show whatsnewdialog with the option to update
              if (StringUtils.isNotBlank(updateWorker.getChangelog())) {
                UpdateDialog dialog = new UpdateDialog(updateWorker.getChangelog());
                dialog.setVisible(true);
              }
              else {
                // do the update without changelog popup
                Object[] options = { BUNDLE.getString("Button.yes"), BUNDLE.getString("Button.no") };
                int answer = JOptionPane.showOptionDialog(null, BUNDLE.getString("tmm.update.message"), BUNDLE.getString("tmm.update.title"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
                if (answer == JOptionPane.YES_OPTION) {
                  LOGGER.info("Updating...");

                  // spawn getdown and exit TMM
                  closeTmmAndStart(Utils.getPBforTMMupdate());
                }
              }
            }
          }
          catch (Exception e) {
            LOGGER.error("Update task failed!" + e.getMessage());
          }
        }
      });

      // update task start a few secs after GUI...
      Timer timer = new Timer(5000, e -> updateWorker.execute());
      timer.setRepeats(false);
      timer.start();
    }
    catch (Exception e) {
      LOGGER.error("Update task failed!" + e.getMessage());
    }
  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize() {
    // set the logo
    setIconImages(LOGOS);
    setBounds(5, 5, 1100, 727);
    // do nothing, we have our own windowClosing() listener
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    toolbarPanel = new ToolbarPanel();
    getContentPane().add(toolbarPanel, BorderLayout.NORTH);

    JPanel rootPanel = new JPanel();
    rootPanel.putClientProperty("class", "rootPanel");
    rootPanel.setLayout(new MigLayout("insets 0", "[900lp:n,grow]", "[300lp:400lp,grow,shrink 0]0[shrink 0]"));

    // to draw the shadow beneath the toolbar, encapsulate the panel
    JLayer<JComponent> rootLayer = new JLayer<>(rootPanel, new ShadowLayerUI()); // $hide$ - do not parse this in wbpro
    getContentPane().add(rootLayer, BorderLayout.CENTER);

    splitPane = new TmmSplitPane();
    rootPanel.add(splitPane, "cell 0 0, grow");

    tabbedPane = new MainTabbedPane() {
      private static final long serialVersionUID = 9041548865608767661L;

      @Override
      public void updateUI() {
        putClientProperty("rightBorder", "half");
        putClientProperty("bottomBorder", Boolean.FALSE);
        super.updateUI();
      }
    };
    splitPane.setLeftComponent(tabbedPane);

    detailPanel = new JPanel();
    detailPanel.setOpaque(false);
    detailPanel.setLayout(new CardLayout(0, 0));
    splitPane.setRightComponent(detailPanel);

    panelStatusBar = new StatusBarPanel();
    rootPanel.add(panelStatusBar, "cell 0 1,grow");

    addModule(MovieUIModule.getInstance());
    toolbarPanel.setUIModule(MovieUIModule.getInstance());
    addModule(MovieSetUIModule.getInstance());
    addModule(TvShowUIModule.getInstance());

    ChangeListener changeListener = changeEvent -> {
      JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
      if (sourceTabbedPane.getSelectedComponent() instanceof ITmmTabItem) {
        ITmmTabItem activeTab = (ITmmTabItem) sourceTabbedPane.getSelectedComponent();
        toolbarPanel.setUIModule(activeTab.getUIModule());
        CardLayout cl = (CardLayout) detailPanel.getLayout();
        cl.show(detailPanel, activeTab.getUIModule().getModuleId());
      }
    };
    tabbedPane.addChangeListener(changeListener);

    // shutdown listener - to clean database connections safely
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        closeTmm();
      }
    });

    MessageManager.instance.addListener(TmmUIMessageCollector.instance);

    // mouse event listener for context menu
    Toolkit.getDefaultToolkit().addAWTEventListener(arg0 -> {
      if (arg0 instanceof MouseEvent && ((MouseEvent) arg0).isPopupTrigger() && arg0.getSource() instanceof JTextComponent) {
        MouseEvent me = (MouseEvent) arg0;
        JTextComponent tc = (JTextComponent) arg0.getSource();
        if (me.isPopupTrigger() && tc.getComponentPopupMenu() == null) {
          TextFieldPopupMenu.buildCutCopyPaste().show(tc, me.getX(), me.getY());
        }
      }
    }, AWTEvent.MOUSE_EVENT_MASK);

    // inform user that MI could not be loaded
    if (Platform.isLinux() && StringUtils.isBlank(MediaInfo.version())) {
      SwingUtilities.invokeLater(() -> {
        JOptionPane.showMessageDialog(MainWindow.this, BUNDLE.getString("mediainfo.failed.linux"));
      });
    }

    // inform user that something happened while loading the modules
    for (ITmmModule module : TmmModuleManager.getInstance().getModules()) {
      if (!module.getStartupMessages().isEmpty()) {
        for (String message : module.getStartupMessages()) {
          SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(MainWindow.this, message); // $NON-NLS-1$
          });
        }
      }
    }
  }

  private void addModule(ITmmUIModule module) {
    tabbedPane.addTab(module.getTabTitle(), module.getTabPanel());
    detailPanel.add(module.getDetailPanel(), module.getModuleId());
  }

  public void closeTmm() {
    closeTmmAndStart(null);
  }

  public void closeTmmAndStart(ProcessBuilder pb) {
    int confirm = JOptionPane.YES_OPTION;
    // if there are some threads running, display exit confirmation
    if (TmmTaskManager.getInstance().poolRunning()) {
      Object[] options = { BUNDLE.getString("Button.yes"), BUNDLE.getString("Button.no") };
      confirm = JOptionPane.showOptionDialog(null, BUNDLE.getString("tmm.exit.runningtasks"), BUNDLE.getString("tmm.exit.confirmation"),
          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null); // $NON-NLS-1$
    }
    if (confirm == JOptionPane.YES_OPTION) {
      LOGGER.info("bye bye");
      try {
        // send shutdown signal
        TmmTaskManager.getInstance().shutdown();
        // save unsaved settings
        TmmModuleManager.getInstance().saveSettings();
        // hard kill
        TmmTaskManager.getInstance().shutdownNow();
        // close database connection
        TmmModuleManager.getInstance().shutDown();
      }
      catch (Exception ex) {
        LOGGER.warn("", ex);
      }
      dispose();

      // spawn our process
      if (pb != null) {
        try {
          LOGGER.info("Going to execute: " + pb.command());
          pb.start();
        }
        catch (IOException e) {
          LOGGER.error("Cannot spawn process:", e);
        }
      }
      shutdownLogger();
      System.exit(0); // calling the method is a must
    }
  }

  JSplitPane getSplitPane() {
    return splitPane;
  }

  /**
   * Gets the active instance.
   * 
   * @return the active instance
   */
  public static MainWindow getActiveInstance() {
    return instance;
  }

  /**
   * Gets the frame.
   * 
   * @return the frame
   */
  public static JFrame getFrame() {
    return instance;
  }

  public void createLightbox(String pathToFile, String urlToFile) {
    LightBox.showLightBox(instance, pathToFile, urlToFile);
  }
}
