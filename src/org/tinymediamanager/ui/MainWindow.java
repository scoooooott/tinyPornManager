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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
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
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.ImageCacheTask;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieSet;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.ui.components.NotificationMessage;
import org.tinymediamanager.ui.components.VerticalTextIcon;
import org.tinymediamanager.ui.dialogs.AboutDialog;
import org.tinymediamanager.ui.dialogs.BugReportDialog;
import org.tinymediamanager.ui.dialogs.FeedbackDialog;
import org.tinymediamanager.ui.dialogs.LogDialog;
import org.tinymediamanager.ui.dialogs.MessageSummaryDialog;
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
  private List<String>                messagesList;

  private JPanel                      messagePanel;

  /**
   * Create the application.
   * 
   * @param name
   *          the name
   */
  public MainWindow(String name) {
    super(name);
    setName("mainWindow");
    setMinimumSize(new Dimension(1100, 700));

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
    debug.add(cache);

    JMenuItem clearUrlCache = new JMenuItem(BUNDLE.getString("tmm.clearurlcache")); //$NON-NLS-1$
    cache.add(clearUrlCache);
    clearUrlCache.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        File cache = new File(CachedUrl.CACHE_DIR);
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
    cache.addSeparator();
    JMenuItem clearImageCache = new JMenuItem(BUNDLE.getString("tmm.clearimagecache")); //$NON-NLS-1$
    cache.add(clearImageCache);
    clearImageCache.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        File cache = new File(ImageCache.CACHE_DIR);
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

    JMenuItem rebuildImageCache = new JMenuItem(BUNDLE.getString("tmm.rebuildimagecache")); //$NON-NLS-1$
    cache.add(rebuildImageCache);
    rebuildImageCache.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        if (!Globals.settings.isImageCache()) {
          JOptionPane.showMessageDialog(null, "Image cache is not activated!");
          return;
        }

        List<File> imageFiles = new ArrayList<File>();
        // movie list
        List<Movie> movies = new ArrayList<Movie>(MovieList.getInstance().getMovies());
        for (Movie movie : movies) {
          imageFiles.addAll(movie.getImagesToCache());
        }

        // moviesets
        List<MovieSet> movieSets = new ArrayList<MovieSet>(MovieList.getInstance().getMovieSetList());
        for (MovieSet movieSet : movieSets) {
          imageFiles.addAll(movieSet.getImagesToCache());
        }

        // tv dhows
        List<TvShow> tvShows = new ArrayList<TvShow>(TvShowList.getInstance().getTvShows());
        for (TvShow tvShow : tvShows) {
          imageFiles.addAll(tvShow.getImagesToCache());
        }

        ImageCacheTask task = new ImageCacheTask(imageFiles);
        Globals.executor.execute(task);
      }
    });

    JMenuItem tmmFolder = new JMenuItem(BUNDLE.getString("tmm.gotoinstalldir")); //$NON-NLS-1$
    debug.add(tmmFolder);
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
    debug.add(tmmLogs);
    tmmLogs.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        JDialog logDialog = new LogDialog();
        logDialog.setLocationRelativeTo(MainWindow.getActiveInstance());
        logDialog.setVisible(true);
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

    messagePanel = new JPanel();
    messagePanel.setOpaque(false);
    messagePanel.setLayout(new FlowLayout());

    content.add(messagePanel, "3, 1, right, fill");
    content.setLayer(messagePanel, 2);

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
        closeTmm();
      }
    });
    MessageManager.instance.addListener(new UIMessageListener());
  }

  private void closeTmm() {
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
          if (Globals.poolRunning() || (activeTask != null && !activeTask.isDone())) {
            if (lblLoadingImg.getIcon() != loading) {
              lblLoadingImg.setIcon(loading);
            }
          }
          else if (lblLoadingImg.getIcon() == loading) {
            lblLoadingImg.setIcon(null);
          }

          // if a main task is finished and a message collector is alive -> show it with the messages collected
          if (messagesList != null && activeTask != null && (activeTask.isDone() || activeTask.isCancelled())) {
            MessageSummaryDialog dialog = new MessageSummaryDialog(messagesList);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
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
      TmmWindowSaver.getInstance().saveSettings(MainWindow.this);
      closeTmm();
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
      aboutDialog.setLocationRelativeTo(MainWindow.getActiveInstance());
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
      instance.messagesList = new ArrayList<String>();
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
      dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
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
      dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
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
      String url = StringEscapeUtils
          .unescapeHtml4("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&amp;business=manuel%2elaggner%40gmail%2ecom&amp;lc=GB&amp;item_name=tinyMediaManager&amp;currency_code=EUR&amp;bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted");
      try {
        TmmUIHelper.browseUrl(url);
      }
      catch (Exception e1) {
        LOGGER.error("Donate", e1);
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, url, "message.erroropenurl",
            new String[] { ":", e1.getLocalizedMessage() }));
      }
    }
  }

  public void addMessage(String title, String message) {
    JPanel msg = new NotificationMessage(title, message);
    messagePanel.add(msg);
  }

  public void addMessage(MessageLevel level, String title, String message) {
    JPanel msg = new NotificationMessage(level, title, message);
    messagePanel.add(msg);

    if (messagesList != null) {
      messagesList.add(message + ": " + title);
    }
  }

  public void removeMessage(JComponent comp) {
    messagePanel.remove(comp);
    messagePanel.revalidate();
  }
}
