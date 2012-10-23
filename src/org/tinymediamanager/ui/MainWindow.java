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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.tinymediamanager.Globals;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MainWindow.
 */
public class MainWindow {

  /** The frame. */
  private JFrame frame;

  /**
   * Create the application.
   */
  public MainWindow() {
    initialize();
    frame.setVisible(true);
  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize() {
    frame = new JFrame("tinyMediaManager " + org.tinymediamanager.ReleaseInfo.getVersion());
    frame.setBounds(5, 5, 1100, 700);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().setLayout(
        new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
            RowSpec.decode("fill:default:grow"), }));

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setUI(new TmmTabbedPaneUI());
    tabbedPane.setTabPlacement(JTabbedPane.LEFT);
    frame.getContentPane().add(tabbedPane, "2, 2, fill, fill");

    JPanel panelMovies = new MoviePanel();// new JPanel();
    tabbedPane.addTab("", new ImageIcon(MainWindow.class.getResource("/org/tinymediamanager/ui/images/show_reel.png")), panelMovies, null);

    JPanel panelSettings = new SettingsPanel();// JPanel();
    tabbedPane.addTab("", new ImageIcon(MainWindow.class.getResource("/org/tinymediamanager/ui/images/Action-configure-icon.png")), panelSettings, null);

    // shutdown listener - to clean database connections safetly
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        try {
          Globals.shutdownDatabase();
        } catch (Exception ex) {
        }
        frame.dispose();
        System.exit(0); // calling the method is a must
      }
    });
  }

}
