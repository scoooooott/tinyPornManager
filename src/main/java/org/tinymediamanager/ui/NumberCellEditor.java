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
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

/**
 * This cell editor is used for entering numbers only
 *
 * @author Manuel Laggner
 */
public class NumberCellEditor extends DefaultCellEditor {
  private int maxIntegerDigits;
  private int maxFractionDigits;

  public NumberCellEditor(int maxIntegerDigits, int maxFractionDigits) {
    super(new JFormattedTextField());
    this.maxIntegerDigits = maxIntegerDigits;
    this.maxFractionDigits = maxFractionDigits;
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    JFormattedTextField editor = (JFormattedTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);

    if (value instanceof Number) {
      Locale myLocale = Locale.getDefault();

      NumberFormat numberFormat = NumberFormat.getInstance(myLocale);
      numberFormat.setMinimumFractionDigits(0);
      numberFormat.setMaximumFractionDigits(maxFractionDigits);
      numberFormat.setMinimumIntegerDigits(1);
      numberFormat.setMaximumIntegerDigits(maxIntegerDigits);

      editor.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(numberFormat)));

      editor.setHorizontalAlignment(SwingConstants.RIGHT);
      editor.setValue(value);
    }
    return editor;
  }

  @Override
  public boolean stopCellEditing() {
    try {
      // try to get the value
      this.getCellEditorValue();
      return super.stopCellEditing();
    }
    catch (Exception ex) {
      return false;
    }

  }

  @Override
  public Object getCellEditorValue() {
    // get content of textField
    String str = (String) super.getCellEditorValue();
    if (str == null) {
      return null;
    }

    if (str.length() == 0) {
      return null;
    }

    // try to parse a number
    try {
      ParsePosition pos = new ParsePosition(0);
      Number n = NumberFormat.getInstance().parse(str, pos);
      if (pos.getIndex() != str.length()) {
        throw new ParseException("parsing incomplete", pos.getIndex());
      }

      // return an instance of column class
      if (maxFractionDigits > 0) {
        return n.floatValue();
      }
      else {
        return n.intValue();
      }

    }
    catch (ParseException pex) {
      throw new RuntimeException(pex);
    }
  }
}
