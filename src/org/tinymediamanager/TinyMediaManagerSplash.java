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

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * @author manuel
 * 
 */
public class TinyMediaManagerSplash extends JWindow {

  private JLabel                        lblProcessDesc;
  private JProgressBar                  progressBar;
  private static TinyMediaManagerSplash instance;

  private TinyMediaManagerSplash(JFrame parent) {
    super(parent);
    // setResizable(false);
    // setUndecorated(true);
    setAlwaysOnTop(true);
    setLocationRelativeTo(null);
    getContentPane().setLayout(
        new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { RowSpec.decode("fill:default:grow"),
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

    JLayeredPane layeredPane = new JLayeredPane();
    getContentPane().add(layeredPane, "1, 1, 5, 5, fill, fill");
    layeredPane.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("fill:default"), FormFactory.RELATED_GAP_ROWSPEC, }));

    JLabel lblSplash = new JLabel("");
    lblSplash.setIcon(new ImageIcon(TinyMediaManagerSplash.class.getResource("/org/tinymediamanager/ui/images/splashscreen.png")));
    layeredPane.add(lblSplash, "1, 1, 5, 6");

    progressBar = new JProgressBar();
    layeredPane.setLayer(progressBar, 1);
    layeredPane.add(progressBar, "3, 5");

    lblProcessDesc = new JLabel("");
    lblProcessDesc.setForeground(Color.WHITE);
    layeredPane.setLayer(lblProcessDesc, 1);
    layeredPane.add(lblProcessDesc, "3, 3");
    this.pack();
  }

  public void setProgress(String description, int percentage) {
    final String text = description;
    final int value = percentage;

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        lblProcessDesc.setText(text);
        progressBar.setValue(value);
      }
    });

  }

  public static void splash() {
    if (instance == null) {
      JFrame f = new JFrame();
      instance = new TinyMediaManagerSplash(f);
      instance.pack();
      instance.setVisible(true);
      instance.setLocationRelativeTo(null);
    }
  }

  public static void disposeSplash() {
    if (instance != null) {
      instance.getOwner().dispose();
      instance = null;
    }
  }

  /**
   * Invokes the main method of the provided class name.
   * 
   * @param args
   *          the command line arguments
   */
  public static void invokeMain(String className, String[] args) {
    try {
      Class.forName(className).getMethod("main", new Class[] { String[].class }) //$NON-NLS-1$
          .invoke(null, new Object[] { args });
    }
    catch (Exception e) {
      InternalError error = new InternalError("Failed to invoke main method"); //$NON-NLS-1$
      error.initCause(e);
      throw error;
    }
  }

  public static TinyMediaManagerSplash getInstance() {
    return instance;
  }

  public static void setInstance(TinyMediaManagerSplash instance) {
    TinyMediaManagerSplash.instance = instance;
  }

}
