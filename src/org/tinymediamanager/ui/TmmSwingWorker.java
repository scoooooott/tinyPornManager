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

import java.awt.GraphicsEnvironment;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * The Class TmmSwingWorker.
 * 
 * @author Manuel Laggner
 */
public abstract class TmmSwingWorker<T, V> extends SwingWorker<T, V> {
  private JLabel       lblProgressAction = null;
  private JProgressBar progressBar       = null;
  private JButton      btnCancelTask     = null;

  /**
   * Sets the references to the ui elements.
   * 
   * @param label
   *          the label
   * @param bar
   *          the bar
   * @param button
   *          the button
   */
  public void setUIElements(JLabel label, JProgressBar bar, JButton button) {
    lblProgressAction = label;
    progressBar = bar;
    btnCancelTask = button;
  }

  /**
   * Cancel.
   */
  public abstract void cancel();

  /**
   * Start progress bar.
   * 
   * @param description
   *          the description
   */
  protected void startProgressBar(final String description) {
    if (!GraphicsEnvironment.isHeadless()) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          if (lblProgressAction != null && description != null) {
            lblProgressAction.setText(description);
          }
          if (progressBar != null) {
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
          }
          if (btnCancelTask != null) {
            btnCancelTask.setVisible(true);
          }
        }
      });
    }
  }

  /**
   * Start progress bar.
   * 
   * @param description
   *          the description
   */
  protected void startProgressBar(final String description, final int max, final int progress) {
    if (!GraphicsEnvironment.isHeadless()) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          if (lblProgressAction != null && description != null) {
            lblProgressAction.setText(description);
          }
          if (progressBar != null) {
            progressBar.setVisible(true);
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(max);
            progressBar.setValue(progress);
          }
          if (btnCancelTask != null) {
            btnCancelTask.setVisible(true);
          }
        }
      });
    }
  }

  /**
   * Start progress bar.
   * 
   * @param description
   *          the description
   * @param value
   *          the value
   */
  protected void startProgressBar(final String description, final int value) {
    if (!GraphicsEnvironment.isHeadless()) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          if (lblProgressAction != null && description != null) {
            lblProgressAction.setText(description);
          }
          if (progressBar != null) {
            progressBar.setVisible(true);
            progressBar.setValue(value);
          }
          if (btnCancelTask != null) {
            btnCancelTask.setVisible(true);
          }
        }
      });
    }
  }

  /**
   * Stop progress bar.
   */
  protected void stopProgressBar() {
    if (!GraphicsEnvironment.isHeadless()) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          if (lblProgressAction != null) {
            lblProgressAction.setText("");
          }
          if (progressBar != null) {
            progressBar.setIndeterminate(false);
            progressBar.setVisible(false);
          }
          if (btnCancelTask != null) {
            btnCancelTask.setVisible(false);
          }
        }
      });
    }
  }

  public JLabel getProgressActionLabel() {
    return lblProgressAction;
  }

  public JProgressBar getProgressBar() {
    return progressBar;
  }

  public JButton getActionButton() {
    return btnCancelTask;
  }
}
