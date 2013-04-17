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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

/**
 * The Class TmmSwingWorker.
 * 
 * @author Manuel Laggner
 */
public abstract class TmmSwingWorker extends SwingWorker<Void, Void> {

  /** The label progressAction. */
  protected JLabel       lblProgressAction;

  /** The progress bar. */
  protected JProgressBar progressBar;

  /** The button cancelScraper. */
  protected JButton      btnCancelTask;

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
}
