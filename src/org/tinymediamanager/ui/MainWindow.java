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
package org.tinymediamanager.ui;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.commons.io.FileUtils;
import org.tinymediamanager.Globals;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MainWindow.
 */
public class MainWindow extends JFrame {
  private final Action      actionExit = new ExitAction();

  private static MainWindow instance;

  // /** The frame. */
  // private JFrame frame;

  /**
   * Create the application.
   */
  public MainWindow(String name) {
    super(name);

    instance = this;

    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    JMenu mnTmm = new JMenu("tinyMediaManager");
    menuBar.add(mnTmm);

    JMenuItem mntmExit = mnTmm.add(actionExit);
    mntmExit.setText("Exit");
    initialize();
    // setVisible(true);
  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize() {
    // set the logo
    setIconImage(Globals.logo);
    setBounds(5, 5, 1100, 700);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    getContentPane().setLayout(
        new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
            RowSpec.decode("fill:default:grow"), }));

    JTabbedPane tabbedPane = new JTabbedPane();
    // tabbedPane.setUI(new TmmTabbedPaneUI());
    // tabbedPane.setTabPlacement(JTabbedPane.LEFT);
    getContentPane().add(tabbedPane, "1, 2, fill, fill");

    JPanel panelMovies = new MoviePanel();// new JPanel();
    // tabbedPane.addTab("", new
    // ImageIcon(MainWindow.class.getResource("/org/tinymediamanager/ui/images/show_reel.png")),
    // panelMovies, null);
    tabbedPane.addTab("Movies", panelMovies);

    JPanel panelSettings = new SettingsPanel();// JPanel();
    // tabbedPane.addTab("", new
    // ImageIcon(MainWindow.class.getResource("/org/tinymediamanager/ui/images/Action-configure-icon.png")),
    // panelSettings,
    // null);
    tabbedPane.addTab("Settings", panelSettings);

    // shutdown listener - to clean database connections safetly
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        try {
          // close database connection
          Globals.shutdownDatabase();
          // clear cache directory
          File cache = new File("cache");
          if (cache.exists()) {
            FileUtils.deleteDirectory(cache);
          }

        }
        catch (Exception ex) {
        }
        dispose();
        System.exit(0); // calling the method is a must
      }
    });
  }

  private class ExitAction extends AbstractAction {
    public ExitAction() {
      putValue(NAME, "SwingAction");
      putValue(SHORT_DESCRIPTION, "Some short description");
    }

    public void actionPerformed(ActionEvent e) {
      instance.setVisible(false);
      instance.dispose();
    }
  }

  public static JFrame getFrame() {
    return instance;
  }

}
