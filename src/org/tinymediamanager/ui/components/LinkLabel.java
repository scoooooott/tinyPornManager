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
package org.tinymediamanager.ui.components;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

/**
 * An extension of JLabel which looks like a link and responds appropriately
 * when clicked. Note that this class will only work with Swing 1.1.1 and later.
 * Note that because of the way this class is implemented, getText() will not
 * return correct values, user <code>getNormalText</code> instead.
 * 
 * @author Manuel Laggner
 */

public class LinkLabel extends JLabel {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * The normal text set by the user.
   */
  private String            text;

  /**
   * Creates a new LinkLabel with the given text.
   * 
   * @param text
   *          the text
   */
  public LinkLabel(String text) {
    super(text);

    if (Desktop.isDesktopSupported()) {
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      enableEvents(MouseEvent.MOUSE_EVENT_MASK);
    }
  }

  /**
   * Sets the text of the label.
   * 
   * @param text
   *          the new text
   */
  @Override
  public void setText(String text) {
    if (Desktop.isDesktopSupported()) {
      super.setText("<html><font color=\"#0000CF\"><u>" + text + "</u></font></html>"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    else {
      super.setText(text);
    }
    this.text = text;
  }

  /**
   * Returns the text set by the user.
   * 
   * @return the normal text
   */
  public String getNormalText() {
    return text;
  }

  /**
   * Processes mouse events and responds to clicks.
   * 
   * @param evt
   *          the evt
   */
  @Override
  protected void processMouseEvent(MouseEvent evt) {
    super.processMouseEvent(evt);
    if (evt.getID() == MouseEvent.MOUSE_CLICKED)
      fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getNormalText()));
  }

  /**
   * Adds an ActionListener to the list of listeners receiving notifications
   * when the label is clicked.
   * 
   * @param listener
   *          the listener
   */
  public void addActionListener(ActionListener listener) {
    listenerList.add(ActionListener.class, listener);
  }

  /**
   * Removes the given ActionListener from the list of listeners receiving
   * notifications when the label is clicked.
   * 
   * @param listener
   *          the listener
   */
  public void removeActionListener(ActionListener listener) {
    listenerList.remove(ActionListener.class, listener);
  }

  /**
   * Fires an ActionEvent to all interested listeners.
   * 
   * @param evt
   *          the evt
   */
  protected void fireActionPerformed(ActionEvent evt) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2) {
      if (listeners[i] == ActionListener.class) {
        ActionListener listener = (ActionListener) listeners[i + 1];
        listener.actionPerformed(evt);
      }
    }
  }
}