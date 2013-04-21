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

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.ui.components.VerticalTextIcon;
import org.tinymediamanager.ui.dialogs.AboutDialog;
import org.tinymediamanager.ui.dialogs.BugReportDialog;
import org.tinymediamanager.ui.dialogs.FeedbackDialog;
import org.tinymediamanager.ui.movies.MoviePanel;
import org.tinymediamanager.ui.moviesets.MovieSetPanel;
import org.tinymediamanager.ui.settings.SettingsPanel;
import org.tinymediamanager.ui.tvshows.TvShowPanel;

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

  /** The action exit. */
  private final Action                actionExit       = new ExitAction();

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

  /**
   * Create the application.
   * 
   * @param name
   *          the name
   */
  public MainWindow(String name) {
    super(name);
    setName("mainWindow");

    instance = this;

    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    JMenu mnTmm = new JMenu("tinyMediaManager");
    menuBar.add(mnTmm);

    // JMenuItem mntmSettings = mnTmm.add(actionSettings);
    // mntmSettings.setText("Settings");

    JMenuItem mntmFeedback = mnTmm.add(actionFeedback);
    mntmFeedback.setText(BUNDLE.getString("Feedback")); //$NON-NLS-1$

    JMenuItem mntmBugReport = mnTmm.add(actionBugReport);
    mntmBugReport.setText(BUNDLE.getString("BugReport")); //$NON-NLS-1$

    mnTmm.addSeparator();

    JMenuItem mntmExit = mnTmm.add(actionExit);
    mntmExit.setText(BUNDLE.getString("tmm.exit")); //$NON-NLS-1$
    initialize();

    // debug menu
    JMenu debug = new JMenu(BUNDLE.getString("tmm.debug")); //$NON-NLS-1$
    JMenuItem clearDatabase = new JMenuItem(BUNDLE.getString("tmm.cleardatabase")); //$NON-NLS-1$
    debug.add(clearDatabase);
    clearDatabase.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // delete the database
        try {
          Globals.shutdownDatabase();
          File db = new File("tmm.odb");
          if (db.exists()) {
            db.delete();
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          JOptionPane.showMessageDialog(null, BUNDLE.getString("tmm.cleardatabase.info")); //$NON-NLS-1$
        }
        catch (Exception e) {
          JOptionPane.showMessageDialog(null, BUNDLE.getString("tmm.cleardatabase.error")); //$NON-NLS-1$
          // open the tmm folder
          try {
            File path = new File(".");
            // check whether this location exists
            if (path.exists()) {
              Desktop.getDesktop().open(path);
            }
          }
          catch (Exception ex) {
            LOGGER.warn(ex.getMessage());
          }
        }
        System.exit(0);
      }
    });
    JMenuItem clearCache = new JMenuItem(BUNDLE.getString("tmm.clearcache")); //$NON-NLS-1$
    debug.add(clearCache);
    clearCache.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        File cache = new File("cache");
        if (cache.exists()) {
          try {
            FileUtils.deleteDirectory(cache);
          }
          catch (Exception e) {
            LOGGER.warn(e.getMessage());
          }
        }
      }
    });

    menuBar.add(debug);

    mnTmm = new JMenu("?");
    menuBar.add(mnTmm);
    JMenuItem mntmDonate = mnTmm.add(actionDonate);
    mntmDonate.setText(BUNDLE.getString("tmm.donate")); //$NON-NLS-1$
    mnTmm.addSeparator();
    JMenuItem mntmAbout = mnTmm.add(actionAbout);
    mntmAbout.setText(BUNDLE.getString("tmm.about")); //$NON-NLS-1$
    // setVisible(true);

    // Globals.executor.execute(new MyStatusbarThread());
    // use a Future to be able to cancel it
    statusTask.execute();
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
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:max(400dlu;default):grow"), FormFactory.NARROW_LINE_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, }));

    JTabbedPane tabbedPane = VerticalTextIcon.createTabbedPane(JTabbedPane.LEFT);
    tabbedPane.setTabPlacement(JTabbedPane.LEFT);
    getContentPane().add(tabbedPane, "1, 2, fill, fill");

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
    btnCancelTask.setIcon(new ImageIcon(MoviePanel.class.getResource("/org/tinymediamanager/ui/images/Button_Stop.png")));
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
    panelStatusBar.add(lblLoadingImg, "9, 1");

    panelMovies = new MoviePanel();
    VerticalTextIcon.addTab(tabbedPane, BUNDLE.getString("tmm.movies"), panelMovies); //$NON-NLS-1$

    JPanel panelMovieSets = new MovieSetPanel();
    VerticalTextIcon.addTab(tabbedPane, BUNDLE.getString("tmm.moviesets"), panelMovieSets); //$NON-NLS-1$

    JPanel panelTvShows = new TvShowPanel();
    VerticalTextIcon.addTab(tabbedPane, BUNDLE.getString("tmm.tvshows"), panelTvShows); //$NON-NLS-1$

    JPanel panelSettings = new SettingsPanel();
    VerticalTextIcon.addTab(tabbedPane, BUNDLE.getString("tmm.settings"), panelSettings); //$NON-NLS-1$

    // shutdown listener - to clean database connections safely
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        int confirm = 0;
        // if there are some threads running, display exit confirmation
        if (Globals.executor.getActiveCount() > 0) {
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
              File cache = new File("cache");
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
          System.exit(0); // calling the method is a must
        }
      }
    });

  }

  // status bar thread
  /**
   * The Class StatusbarThread.
   * 
   * @author Manuel Laggner
   */
  private class StatusbarThread extends SwingWorker<Void, Void> {

    /** The loading. */
    private final ImageIcon    loading;

    /** The ex. */
    private ThreadPoolExecutor ex = Globals.executor;

    /**
     * Instantiates a new statusbar thread.
     */
    public StatusbarThread() {
      loading = new ImageIcon(MainWindow.class.getResource("/org/tinymediamanager/ui/images/loading.gif"));
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
          if (this.ex.getActiveCount() > 0) {
            if (lblLoadingImg.getIcon() != loading) {
              lblLoadingImg.setIcon(loading);
            }
          }
          else {
            if (lblLoadingImg.getIcon() == loading) {
              lblLoadingImg.setIcon(null);
            }
          }
          String text = String.format(
              "<html><body>" + BUNDLE.getString("status.activethreads") + " [%d/%d]<br>" + BUNDLE.getString("status.queuesize")
                  + " %d </body></html>", this.ex.getActiveCount(), this.ex.getMaximumPoolSize(), this.ex.getQueue().size()); //$NON-NLS-1$
          // LOGGER.debug(text);
          lblLoadingImg.setToolTipText(text);
          Thread.sleep(2000);
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
   * The Class ExitAction.
   * 
   * @author Manuel Laggner
   */
  private class ExitAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new exit action.
     */
    public ExitAction() {
      // putValue(NAME, "SwingAction");
      // putValue(SHORT_DESCRIPTION, "Some short description");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      instance.setVisible(false);
      instance.dispose();
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

  /**
   * The Class AboutAction.
   * 
   * @author Manuel Laggner
   */
  private class AboutAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new about action.
     */
    public AboutAction() {
      // putValue(NAME, "SwingAction");
      // putValue(SHORT_DESCRIPTION, "Some short description");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      Dialog aboutDialog = new AboutDialog();
      aboutDialog.setVisible(true);
    }
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
      instance.activeTask = task;
      instance.activeTask.setUIElements(instance.lblProgressAction, instance.progressBar, instance.btnCancelTask);
      instance.activeTask.execute();
      return true;
    }

    return false;
  }

  /**
   * The Class FeedbackAction.
   * 
   * @author Manuel Laggner
   */
  private class FeedbackAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new feedback action.
     */
    public FeedbackAction() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      JDialog dialog = new FeedbackDialog();
      dialog.pack();
      dialog.setVisible(true);
    }
  }

  /**
   * Gets the frame.
   * 
   * @return the frame
   */
  public static JFrame getFrame() {
    return instance;
  }

  /**
   * The Class BugReportAction.
   * 
   * @author Manuel Laggner
   */
  private class BugReportAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new feedback action.
     */
    public BugReportAction() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      JDialog dialog = new BugReportDialog();
      dialog.pack();
      dialog.setVisible(true);
    }
  }

  /**
   * The Class DonateAction.
   * 
   * @author Manuel Laggner
   */
  private class DonateAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new feedback action.
     */
    public DonateAction() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      try {
        String url = StringEscapeUtils
            .unescapeHtml4("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&amp;business=manuel%2elaggner%40gmail%2ecom&amp;lc=GB&amp;item_name=tinyMediaManager&amp;currency_code=EUR&amp;bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted");
        Desktop.getDesktop().browse(new URI(url));
      }
      catch (Exception e1) {
        LOGGER.error("Donate", e1);
      }
    }
  }
}
