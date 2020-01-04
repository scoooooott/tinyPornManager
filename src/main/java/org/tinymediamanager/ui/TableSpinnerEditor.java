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
package org.tinymediamanager.ui;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * The TableSpinnerEditor, to provide a spinner for a table column
 * 
 * @author Manuel Laggner
 */
public class TableSpinnerEditor extends DefaultCellEditor {
  private static final long      serialVersionUID = -4249251950819118622L;

  private JSpinner               spinner;
  private JSpinner.DefaultEditor editor;
  private JTextField             textField;
  private boolean                valueSet;

  // Initializes the spinner.
  public TableSpinnerEditor() {
    super(new JTextField());
    spinner = new JSpinner();
    editor = ((JSpinner.DefaultEditor) spinner.getEditor());
    textField = editor.getTextField();
    textField.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent fe) {
        SwingUtilities.invokeLater(() -> {
          if (valueSet) {
            textField.setCaretPosition(1);
          }
        });
      }

      @Override
      public void focusLost(FocusEvent fe) {
        stopCellEditing();
      }
    });
    textField.addActionListener(ae -> stopCellEditing());
  }

  // Prepares the spinner component and returns it.
  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    if (!valueSet) {
      spinner.setValue(value);
    }
    SwingUtilities.invokeLater(() -> textField.requestFocus());
    return spinner;
  }

  @Override
  public boolean isCellEditable(EventObject eo) {
    if (eo instanceof KeyEvent) {
      KeyEvent ke = (KeyEvent) eo;
      textField.setText(String.valueOf(ke.getKeyChar()));
      valueSet = true;
    }
    else {
      valueSet = false;
    }
    return true;
  }

  // Returns the spinners current value.
  @Override
  public Object getCellEditorValue() {
    return spinner.getValue();
  }

  @Override
  public boolean stopCellEditing() {
    try {
      editor.commitEdit();
      spinner.commitEdit();
    }
    catch (java.text.ParseException e) {
      JOptionPane.showMessageDialog(null, "Invalid value, discarding.");
    }
    return super.stopCellEditing();
  }
}
