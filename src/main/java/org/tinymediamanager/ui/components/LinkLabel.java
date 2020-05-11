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
package org.tinymediamanager.ui.components;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.UIManager;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.ui.UIConstants;

/**
 * An extension of JLabel which looks like a link and responds appropriately when clicked. Note that this class will only work with Swing 1.1.1 and
 * later. Note that because of the way this class is implemented, getText() will not return correct values, user <code>getNormalText</code> instead.
 * 
 * @author Manuel Laggner
 */

public class LinkLabel extends ReadOnlyTextArea {
  private static final long serialVersionUID = 3762584745632060187L;

  protected String          link;
  protected ActionListener  activeListener   = null;

  /**
   * Creates a new LinkLabel with the given text.
   * 
   * @param text
   *          the text
   */
  public LinkLabel(String text) {
    super(text);
    setLink(text);
  }

  /**
   * Creates a new LinkLabel with the given text.
   */
  public LinkLabel() {
    this(null);
  }

  @Override
  public void updateUI() {
    super.updateUI();

    Font font = UIManager.getFont("Label.font");
    Map attributes = font.getAttributes();
    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
    attributes.put(TextAttribute.FOREGROUND, UIConstants.LINK_COLOR);
    setFont(font.deriveFont(attributes));

    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    enableEvents(MouseEvent.MOUSE_EVENT_MASK);
  }

  @Override
  public void setText(String text) {
    super.setText(text);
    setLink(text);
  }

  /**
   * set the link target (useful if the text and link differs)
   * 
   * @param link
   *          the link target
   */
  public void setLink(String link) {
    this.link = link;
  }

  /**
   * get the link target
   * 
   * @return the link target
   */
  public String getLink() {
    return link;
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
    if ((evt.getID() == MouseEvent.MOUSE_CLICKED || evt.getID() == MouseEvent.MOUSE_PRESSED) && StringUtils.isNotBlank(getLink()))
      fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getLink()));
  }

  /**
   * Adds an ActionListener to the list of listeners receiving notifications when the label is clicked.
   * 
   * @param listener
   *          the listener
   */
  public void addActionListener(ActionListener listener) {
    // remove any previous set listener
    if (activeListener != null) {
      removeActionListener(activeListener);
    }
    listenerList.add(ActionListener.class, listener);
    activeListener = listener;
  }

  /**
   * Removes the given ActionListener from the list of listeners receiving notifications when the label is clicked.
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
