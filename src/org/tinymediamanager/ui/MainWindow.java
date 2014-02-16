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
package org.tinymediamanager.ui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.Timer;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.LaunchUtil;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.UpdaterTask;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.WolDevice;
import org.tinymediamanager.ui.actions.AboutAction;
import org.tinymediamanager.ui.actions.BugReportAction;
import org.tinymediamanager.ui.actions.ClearImageCacheAction;
import org.tinymediamanager.ui.actions.ClearUrlCacheAction;
import org.tinymediamanager.ui.actions.DonateAction;
import org.tinymediamanager.ui.actions.ExitAction;
import org.tinymediamanager.ui.actions.FeedbackAction;
import org.tinymediamanager.ui.actions.RebuildImageCacheAction;
import org.tinymediamanager.ui.actions.SettingsAction;
import org.tinymediamanager.ui.components.LightBoxPanel;
import org.tinymediamanager.ui.components.TextFieldPopupMenu;
import org.tinymediamanager.ui.components.VerticalTextIcon;
import org.tinymediamanager.ui.dialogs.LogDialog;
import org.tinymediamanager.ui.dialogs.MessageSummaryDialog;
import org.tinymediamanager.ui.movies.MoviePanel;
import org.tinymediamanager.ui.moviesets.MovieSetPanel;
import org.tinymediamanager.ui.tvshows.TvShowPanel;

import ch.swingfx.twinkle.NotificationBuilder;
import ch.swingfx.twinkle.window.Positions;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MainWindow.
 * 
 * @author Manuel Laggner
 */
public class MainWindow extends JFrame {

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  /** The logger. */
  private final static Logger         LOGGER           = LoggerFactory.getLogger(MainWindow.class);

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = 1L;

  /** The action about. */
  private final Action                actionAbout      = new AboutAction();

  /** The action feedback. */
  private final Action                actionFeedback   = new FeedbackAction();

  /** The action bug report. */
  private final Action                actionBugReport  = new BugReportAction();

  /** The action donate. */
  private final Action                actionDonate     = new DonateAction();

  /** The instance. */
  private static MainWindow           instance;

  /** The panel movies. */
  private JPanel                      panelMovies;
  private JPanel                      panelMovieSets;
  private JPanel                      panelTvShows;

  /** The panel status bar. */
  private JPanel                      panelStatusBar;

  /** The lbl loading img. */
  private JLabel                      lblLoadingImg;

  /** The label progressAction. */
  private JLabel                      lblProgressAction;

  /** The progress bar. */
  private JProgressBar                progressBar;

  /** The button cancelScraper. */
  private JButton                     btnCancelTask;

  /** The active task. */
  private TmmSwingWorker              activeTask;

  /** The status task. */
  private StatusbarThread             statusTask       = new StatusbarThread();
  private List<String>                messagesList;

  private JPopupMenu                  taskPopup;
  private LightBoxPanel               lightBoxPanel;

  private JDialog                     settingsDialog;

  /**
   * Create the application.
   * 
   * @param name
   *          the name
   */
  public MainWindow(String name) {
    super(name);
    setName("mainWindow");
    setMinimumSize(new Dimension(1000, 700));

    instance = this;
    lightBoxPanel = new LightBoxPanel();

    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    JMenu mnTmm = new JMenu("tinyMediaManager");
    menuBar.add(mnTmm);

    mnTmm.add(new SettingsAction());
    mnTmm.addSeparator();
    mnTmm.add(new ExitAction());
    initialize();

    // tools menu
    JMenu tools = new JMenu(BUNDLE.getString("tmm.tools")); //$NON-NLS-1$
    JMenuItem clearDatabase = new JMenuItem(BUNDLE.getString("tmm.cleardatabase")); //$NON-NLS-1$
    tools.add(clearDatabase);
    clearDatabase.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        // display warning popup
        int answer = JOptionPane.showConfirmDialog(MainWindow.this, BUNDLE.getString("tmm.cleardatabase.hint"),
            BUNDLE.getString("tmm.cleardatabase"), JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.OK_OPTION) {
          return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // delete the database
        try {
          Globals.shutdownDatabase();
          File db = new File(Constants.DB);
          if (db.exists()) {
            db.delete();
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          JOptionPane.showMessageDialog(null, BUNDLE.getString("tmm.cleardatabase.info")); //$NON-NLS-1$
        }
        catch (Exception e) {
          JOptionPane.showMessageDialog(null, BUNDLE.getString("tmm.cleardatabase.error")); //$NON-NLS-1$
          // open the tmm folder
          File path = new File(".");
          try {
            // check whether this location exists
            if (path.exists()) {
              TmmUIHelper.openFile(path);
            }
          }
          catch (Exception ex) {
            LOGGER.warn(ex.getMessage());
            MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, path, "message.erroropenfolder", new String[] { ":",
                ex.getLocalizedMessage() }));
          }
        }
        System.exit(0);
      }
    });

    JMenu cache = new JMenu(BUNDLE.getString("tmm.cache")); //$NON-NLS-1$
    tools.add(cache);

    JMenuItem clearUrlCache = new JMenuItem(new ClearUrlCacheAction());
    cache.add(clearUrlCache);
    cache.addSeparator();
    JMenuItem clearImageCache = new JMenuItem(new ClearImageCacheAction());
    cache.add(clearImageCache);

    JMenuItem rebuildImageCache = new JMenuItem(new RebuildImageCacheAction());
    cache.add(rebuildImageCache);

    JMenuItem tmmFolder = new JMenuItem(BUNDLE.getString("tmm.gotoinstalldir")); //$NON-NLS-1$
    tools.add(tmmFolder);
    tmmFolder.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        File path = new File(System.getProperty("user.dir"));
        try {
          // check whether this location exists
          if (path.exists()) {
            TmmUIHelper.openFile(path);
          }
        }
        catch (Exception ex) {
          LOGGER.error("open filemanager", ex);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, path, "message.erroropenfolder", new String[] { ":",
              ex.getLocalizedMessage() }));
        }
      }
    });

    JMenuItem tmmLogs = new JMenuItem(BUNDLE.getString("tmm.errorlogs")); //$NON-NLS-1$
    tools.add(tmmLogs);
    tmmLogs.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        JDialog logDialog = new LogDialog();
        logDialog.setLocationRelativeTo(MainWindow.getActiveInstance());
        logDialog.setVisible(true);
      }
    });

    tools.addSeparator();
    final JMenu menuWakeOnLan = new JMenu(BUNDLE.getString("tmm.wakeonlan")); //$NON-NLS-1$
    menuWakeOnLan.addMenuListener(new MenuListener() {
      @Override
      public void menuCanceled(MenuEvent arg0) {
      }

      @Override
      public void menuDeselected(MenuEvent arg0) {
      }

      @Override
      public void menuSelected(MenuEvent arg0) {
        menuWakeOnLan.removeAll();
        for (final WolDevice device : Globals.settings.getWolDevices()) {
          JMenuItem item = new JMenuItem(device.getName());
          item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
              Utils.sendWakeOnLanPacket(device.getMacAddress());
            }
          });
          menuWakeOnLan.add(item);
        }
      }
    });
    tools.add(menuWakeOnLan);

    menuBar.add(tools);

    mnTmm = new JMenu(BUNDLE.getString("tmm.contact")); //$NON-NLS-1$
    JMenuItem mntmFeedback = mnTmm.add(actionFeedback);
    mntmFeedback.setText(BUNDLE.getString("Feedback")); //$NON-NLS-1$

    JMenuItem mntmBugReport = mnTmm.add(actionBugReport);
    mntmBugReport.setText(BUNDLE.getString("BugReport")); //$NON-NLS-1$
    menuBar.add(mnTmm);

    mnTmm = new JMenu("?");
    menuBar.add(mnTmm);
    JMenuItem mntmAbout = mnTmm.add(actionAbout);
    mntmAbout.setText(BUNDLE.getString("tmm.about")); //$NON-NLS-1$

    menuBar.add(Box.createGlue());

    //    mnTmm = new JMenu(BUNDLE.getString("tmm.donate")); //$NON-NLS-1$
    JButton btnDonate = new JButton(actionDonate);
    btnDonate.setBorderPainted(false);
    btnDonate.setFocusPainted(false);
    btnDonate.setContentAreaFilled(false);
    menuBar.add(btnDonate);
    // JMenuItem mntmDonate = mnTmm.add(actionDonate);
    //    mntmDonate.setText(BUNDLE.getString("tmm.donate")); //$NON-NLS-1$

    // Globals.executor.execute(new MyStatusbarThread());
    // use a Future to be able to cancel it
    statusTask.execute();
    checkForUpdate();
  }

  private void checkForUpdate() {
    try {
      final SwingWorker<Boolean, Void> updateWorker = new UpdaterTask();

      updateWorker.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          if (StateValue.DONE == updateWorker.getState()) {
            try {
              boolean update = updateWorker.get();
              LOGGER.debug("update result was: " + update);
              if (update) {
                int answer = JOptionPane.showConfirmDialog(null, BUNDLE.getString("tmm.update.message"), BUNDLE.getString("tmm.update.title"),
                    JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.OK_OPTION) {
                  LOGGER.info("Updating...");

                  // spawn getdown and exit TMM
                  ProcessBuilder pb = new ProcessBuilder(LaunchUtil.getJVMPath(), "-Djava.net.preferIPv4Stack=true", "-jar", "getdown.jar", ".");
                  pb.directory(new File("").getAbsoluteFile()); // set working directory (current TMM dir)
                  closeTmmAndStart(pb);

                }
              }
            }
            catch (Exception e) {
              LOGGER.error("Update task failed!" + e.getMessage());
            }
          }
        }
      });

      // update task start a few secs after GUI...
      Timer timer = new Timer(5000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          updateWorker.execute();
        }
      });
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
    setIconImage(Globals.logo);
    setBounds(5, 5, 1100, 727);
    // do nothing, we have our own windowClosing() listener
    // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    getContentPane().setLayout(
        new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), ColumnSpec.decode("1dlu"), }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:max(500px;default):grow"), FormFactory.NARROW_LINE_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, }));

    JLayeredPane content = new JLayeredPane();
    content.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("right:270px"), }, new RowSpec[] { RowSpec.decode("fill:max(500px;default):grow"), }));
    getContentPane().add(content, "1, 2, fill, fill");

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow") }, new RowSpec[] { RowSpec
        .decode("fill:max(500px;default):grow") }));
    content.add(mainPanel, "1, 1, 3, 1, fill, fill");
    content.setLayer(mainPanel, 1);

    JTabbedPane tabbedPane = VerticalTextIcon.createTabbedPane(JTabbedPane.LEFT);
    tabbedPane.setTabPlacement(JTabbedPane.LEFT);
    mainPanel.add(tabbedPane, "1, 1, fill, fill");
    // getContentPane().add(tabbedPane, "1, 2, fill, fill");

    panelStatusBar = new JPanel();
    getContentPane().add(panelStatusBar, "1, 4");
    panelStatusBar.setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.DEFAULT_COLSPEC,
            FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
            FormFactory.DEFAULT_COLSPEC, FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
            FormFactory.LABEL_COMPONENT_GAP_COLSPEC, }, new RowSpec[] { RowSpec.decode("20px"), }));

    lblProgressAction = new JLabel("");
    panelStatusBar.add(lblProgressAction, "3, 1, default, default");

    progressBar = new JProgressBar();
    panelStatusBar.add(progressBar, "5, 1");

    btnCancelTask = new JButton("");
    panelStatusBar.add(btnCancelTask, "7, 1");
    btnCancelTask.setVisible(false);
    btnCancelTask.setContentAreaFilled(false);
    btnCancelTask.setBorderPainted(false);
    btnCancelTask.setBorder(null);
    btnCancelTask.setMargin(new Insets(0, 0, 0, 0));
    btnCancelTask.setIcon(IconManager.PROCESS_STOP);
    btnCancelTask.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        if (activeTask != null && !activeTask.isDone()) {
          activeTask.cancel();
        }
      }
    });
    progressBar.setVisible(false);

    lblLoadingImg = new JLabel("");
    // lblLoadingImg.addMouseListener(new MouseAdapter() {
    // @Override
    // public void mouseClicked(MouseEvent arg0) {
    // createTaskPopup(arg0);
    // }
    // });
    panelStatusBar.add(lblLoadingImg, "9, 1");

    panelMovies = new MoviePanel();
    VerticalTextIcon.addTab(tabbedPane, BUNDLE.getString("tmm.movies"), panelMovies); //$NON-NLS-1$

    panelMovieSets = new MovieSetPanel();
    VerticalTextIcon.addTab(tabbedPane, BUNDLE.getString("tmm.moviesets"), panelMovieSets); //$NON-NLS-1$

    panelTvShows = new TvShowPanel();
    VerticalTextIcon.addTab(tabbedPane, BUNDLE.getString("tmm.tvshows"), panelTvShows); //$NON-NLS-1$

    // shutdown listener - to clean database connections safely
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        closeTmm();
      }
    });
    MessageManager.instance.addListener(new UIMessageListener());

    // mouse event listener for context menu
    Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
      @Override
      public void eventDispatched(AWTEvent arg0) {
        if (arg0 instanceof MouseEvent && MouseEvent.MOUSE_RELEASED == arg0.getID() && arg0.getSource() instanceof JTextComponent) {
          MouseEvent me = (MouseEvent) arg0;
          JTextComponent tc = (JTextComponent) arg0.getSource();
          if (me.isPopupTrigger() && tc.getComponentPopupMenu() == null) {
            TextFieldPopupMenu.buildCutCopyPaste().show(tc, me.getX(), me.getY());
          }
        }
      }
    }, AWTEvent.MOUSE_EVENT_MASK);
  }

  public void closeTmm() {
    closeTmmAndStart(null);
  }

  private void closeTmmAndStart(ProcessBuilder pb) {
    int confirm = 0;
    // if there are some threads running, display exit confirmation
    if (Globals.poolRunning()) {
      confirm = JOptionPane.showOptionDialog(null, BUNDLE.getString("tmm.exit.runningtasks"), BUNDLE.getString("tmm.exit.confirmation"),
          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null); //$NON-NLS-1$
    }
    if (confirm == JOptionPane.YES_OPTION) {
      LOGGER.info("bye bye");
      try {
        // send shutdown signal
        Globals.executor.shutdown();
        // cancel our status task (send interrupt())
        statusTask.cancel(true);
        // save unsaved settings
        Globals.settings.saveSettings();
        // close database connection
        Globals.shutdownDatabase();
        // clear cache directory
        if (Globals.settings.isClearCacheShutdown()) {
          File cache = new File("cache" + File.separator + "url");
          if (cache.exists()) {
            FileUtils.deleteDirectory(cache);
          }
        }
      }
      catch (Exception ex) {
        LOGGER.warn(ex.getMessage());
      }
      dispose();
      try {
        // wait a bit for threads to finish (if any)
        Globals.executor.awaitTermination(2, TimeUnit.SECONDS);
        // hard kill
        Globals.executor.shutdownNow();
      }
      catch (InterruptedException e1) {
        LOGGER.debug("Global thread shutdown");
      }

      // spawn our process
      if (pb != null) {
        try {
          LOGGER.info("Going to execute: " + pb.command());
          Process p = pb.start();
        }
        catch (IOException e) {
          LOGGER.error("Cannot spawn process:", e);
        }
      }

      System.exit(0); // calling the method is a must
    }
  }

  /**
   * Gets the active instance.
   * 
   * @return the active instance
   */
  public static MainWindow getActiveInstance() {
    return instance;
  }

  // status bar thread
  /**
   * The Class StatusbarThread.
   * 
   * @author Manuel Laggner
   */
  private class StatusbarThread extends SwingWorker<Void, Void> {
    /** The ex. */
    private ThreadPoolExecutor ex = Globals.executor;

    /**
     * Instantiates a new statusbar thread.
     */
    public StatusbarThread() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    public Void doInBackground() {
      Thread.currentThread().setName("statusBar thread");
      try {
        while (!Thread.interrupted()) {
          if (Globals.poolRunning() || (activeTask != null && !activeTask.isDone())) {
            if (lblLoadingImg.getIcon() != IconManager.LOADING) {
              lblLoadingImg.setIcon(IconManager.LOADING);
            }
          }
          else if (lblLoadingImg.getIcon() == IconManager.LOADING) {
            lblLoadingImg.setIcon(null);
          }

          // lblLoadingImg.setIcon(loading);

          // if a main task is finished and a message collector is alive -> show it with the messages collected
          if (messagesList != null && activeTask != null && (activeTask.isDone() || activeTask.isCancelled())) {
            if (messagesList.size() > 0) {
              MessageSummaryDialog dialog = new MessageSummaryDialog(messagesList);
              dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
              dialog.setVisible(true);
            }
            messagesList = null;
          }

          String text = String.format(
              "<html><body>" + BUNDLE.getString("status.activethreads") + " [%d/%d]<br>" + BUNDLE.getString("status.queuesize")
                  + " %d </body></html>", this.ex.getActiveCount(), this.ex.getMaximumPoolSize(), this.ex.getQueue().size()); //$NON-NLS-1$
          // LOGGER.debug(text);
          lblLoadingImg.setToolTipText(text);
          Thread.sleep(1000);
        }
      }
      catch (InterruptedException e) {
        // called on cancel(), so don't log it
        // LOGGER.debug("statusBar thread shutdown");
      }
      return null;
    }
  }

  /**
   * Gets the movie panel.
   * 
   * @return the movie panel
   */
  public MoviePanel getMoviePanel() {
    return (MoviePanel) panelMovies;
  }

  public MovieSetPanel getMovieSetPanel() {
    return (MovieSetPanel) panelMovieSets;
  }

  public TvShowPanel getTvShowPanel() {
    return (TvShowPanel) panelTvShows;
  }

  /**
   * Executes a "main" task. A "main" task is a task which can't be parallelized
   * 
   * @param task
   *          the task
   * @return true, if successful
   */
  public static boolean executeMainTask(TmmSwingWorker task) {
    if (instance == null) {
      return false;
    }
    if (instance.activeTask == null || instance.activeTask.isDone()) {
      instance.messagesList = new ArrayList<String>();
      instance.activeTask = task;
      instance.activeTask.setUIElements(instance.lblProgressAction, instance.progressBar, instance.btnCancelTask);
      instance.activeTask.execute();
      return true;
    }

    return false;
  }

  /**
   * Gets the frame.
   * 
   * @return the frame
   */
  public static JFrame getFrame() {
    return instance;
  }

  public void addMessage(String title, String message) {
    // JPanel msg = new NotificationMessage(title, message);
    // messagePanel.add(msg);
    NotificationBuilder builder = new NotificationBuilder().withMessage(message).withTitle(title).withStyle(new TmmNotificationStyle())
        .withPosition(Positions.SOUTH_EAST);
    builder.showNotification();
  }

  public void addMessage(MessageLevel level, String title, String message) {
    // JPanel msg = new NotificationMessage(level, title, message);
    // messagePanel.add(msg);
    NotificationBuilder builder = new NotificationBuilder().withMessage(message).withTitle(title).withStyle(new TmmNotificationStyle())
        .withPosition(Positions.SOUTH_EAST).withIcon(IconManager.ERROR);
    builder.showNotification();

    if (messagesList != null) {
      messagesList.add(message + ": " + title);
    }
  }

  private void createTaskPopup(MouseEvent arg0) {
    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(5, 1));

    do {
      JPanel subPanel = new JPanel();
      subPanel.setLayout(new BorderLayout());
      JButton btnCancel = new JButton(IconManager.PROCESS_STOP);
      btnCancel.setContentAreaFilled(false);
      btnCancel.setBorderPainted(false);
      btnCancel.setBorder(null);
      btnCancel.setMargin(new Insets(0, 2, 0, 2));

      subPanel.add(btnCancel, BorderLayout.EAST);
      subPanel.add(new JLabel("Task status " + panel.getComponentCount()), BorderLayout.CENTER);
      panel.add(subPanel);

    } while (panel.getComponentCount() < 5);

    int x = -panel.getPreferredSize().width - 5;
    int y = -panel.getPreferredSize().height - 5;

    taskPopup = new JPopupMenu();
    taskPopup.setLayout(new BorderLayout());
    taskPopup.add(panel, BorderLayout.CENTER);
    taskPopup.show(lblLoadingImg, x, y);
  }

  public void createLightbox(String pathToFile, String urlToFile) {
    lightBoxPanel.setImageLocation(pathToFile, urlToFile);
    lightBoxPanel.showLightBox(instance);
  }
}
