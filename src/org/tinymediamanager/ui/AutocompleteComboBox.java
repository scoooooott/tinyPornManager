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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * The Class AutocompleteComboBox.
 * 
 * @author Manuel Laggner
 */
public class AutocompleteComboBox extends JComboBox implements JComboBox.KeySelectionManager {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 6366300597464784607L;

  /** The search for. */
  private String            searchFor;

  /** The lap. */
  private long              lap;

  /**
   * The Class CBDocument.
   * 
   * @author Manuel Laggner
   */
  public class CBDocument extends PlainDocument {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8263475765974828640L;

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.text.PlainDocument#insertString(int, java.lang.String, javax.swing.text.AttributeSet)
     */
    @Override
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
      if (str == null)
        return;
      super.insertString(offset, str, a);
      if (!isPopupVisible() && str.length() != 0)
        fireActionEvent();
    }
  }

  /**
   * Instantiates a new autocomplete combo box.
   * 
   * @param items
   *          the items
   */
  public AutocompleteComboBox(Object[] items) {
    super(items);
    lap = new java.util.Date().getTime();
    setKeySelectionManager(this);
    JTextField tf;
    if (getEditor() != null) {
      tf = (JTextField) getEditor().getEditorComponent();
      if (tf != null) {
        tf.setDocument(new CBDocument());
        addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            JTextField tf = (JTextField) getEditor().getEditorComponent();
            String text = tf.getText();
            ComboBoxModel aModel = getModel();
            String current;
            for (int i = 0; i < aModel.getSize(); i++) {
              current = aModel.getElementAt(i).toString();
              if (current.toLowerCase().startsWith(text.toLowerCase())) {
                tf.setText(current);
                tf.setSelectionStart(text.length());
                tf.setSelectionEnd(current.length());
                break;
              }
            }
          }
        });
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComboBox.KeySelectionManager#selectionForKey(char, javax.swing.ComboBoxModel)
   */
  @Override
  public int selectionForKey(char aKey, ComboBoxModel aModel) {
    long now = new java.util.Date().getTime();
    if (searchFor != null && aKey == KeyEvent.VK_BACK_SPACE && searchFor.length() > 0) {
      searchFor = searchFor.substring(0, searchFor.length() - 1);
    }
    else {
      // System.out.println(lap);
      // Kam nie hier vorbei.
      if (lap + 1000 < now)
        searchFor = "" + aKey;
      else
        searchFor = searchFor + aKey;
    }
    lap = now;
    String current;
    for (int i = 0; i < aModel.getSize(); i++) {
      current = aModel.getElementAt(i).toString().toLowerCase();
      if (current.toLowerCase().startsWith(searchFor.toLowerCase()))
        return i;
    }
    return -1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComboBox#fireActionEvent()
   */
  @Override
  public void fireActionEvent() {
    super.fireActionEvent();
  }

}
