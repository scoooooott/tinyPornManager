/*
 * Copyright 2012 - 2016 Manuel Laggner
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
package org.tinymediamanager.ui.components.table;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.lang3.StringUtils;

/**
 * This popup allows to select columns to be shown/hidden in the TmmTable
 *
 * @author Manuel Laggner
 */
public class TmmTableColumnSelectionPopup {

  /**
   * Shows the popup allowing to show/hide columns.
   */
  static void showColumnSelectionPopup(Component c, final TmmTable table) {

    JPopupMenu popup = new JPopupMenu();
    TableColumnModel columnModel = table.getColumnModel();
    if (!(columnModel instanceof TmmTableColumnModel)) {
      return;
    }

    final TmmTableColumnModel tmmTableColumnModel = (TmmTableColumnModel) columnModel;
    List<TableColumn> columns = tmmTableColumnModel.getAllColumns();
    Map<String, Object> displayNameToCheckBox = new HashMap<>();
    List<String> displayNames = new ArrayList<>();

    for (Iterator<TableColumn> it = columns.iterator(); it.hasNext();) {
      final TableColumn etc = it.next();

      String columnName = "";
      if (etc.getHeaderValue() instanceof String) {
        columnName = etc.getHeaderValue().toString();
      }
      else {
        if (etc.getHeaderRenderer() instanceof DefaultTableCellRenderer) {
          columnName = ((DefaultTableCellRenderer) etc.getHeaderRenderer()).getToolTipText();
        }
      }

      // fallback
      if (StringUtils.isBlank(columnName)) {
        columnName = etc.getHeaderValue().toString();
      }

      JCheckBoxMenuItem checkBox = new JCheckBoxMenuItem();
      checkBox.setText(columnName);
      checkBox.setSelected(!tmmTableColumnModel.isColumnHidden(etc));
      // checkBox.setEnabled(etc.isHidingAllowed());

      final JCheckBoxMenuItem checkBoxMenuItem = checkBox;
      checkBox.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
          tmmTableColumnModel.setColumnHidden(etc, !checkBoxMenuItem.isSelected());
          // table.updateColumnSelectionMouseListener();
        }
      });

      if (!displayNames.contains(columnName)) {
        // the expected case
        displayNameToCheckBox.put(columnName, checkBox);
      }
      else {
        // the same display name is used for more columns - fuj
        ArrayList<JCheckBoxMenuItem> al = null;
        Object theFirstOne = displayNameToCheckBox.get(columnName);
        if (theFirstOne instanceof JCheckBoxMenuItem) {
          JCheckBoxMenuItem firstCheckBox = (JCheckBoxMenuItem) theFirstOne;
          al = new ArrayList<>();
          al.add(firstCheckBox);
        }
        else {
          // already a list there
          if (theFirstOne instanceof ArrayList) {
            al = (ArrayList<JCheckBoxMenuItem>) theFirstOne;
          }
          else {
            throw new IllegalStateException("Wrong object theFirstOne is " + theFirstOne);
          }
        }
        al.add(checkBox);
        displayNameToCheckBox.put(columnName, al);
      }
      displayNames.add(columnName);
    }

    // Collections.sort(displayNames, Collator.getInstance());
    int index = 0;
    for (Iterator<String> it = displayNames.iterator(); it.hasNext();) {
      String displayName = it.next();
      Object obj = displayNameToCheckBox.get(displayName);
      JCheckBoxMenuItem checkBox = null;
      if (obj instanceof JCheckBoxMenuItem) {
        checkBox = (JCheckBoxMenuItem) obj;
      }
      else {
        // in case there are duplicate names we store ArrayLists
        // of JCheckBoxes
        if (obj instanceof ArrayList) {
          ArrayList<JCheckBoxMenuItem> al = (ArrayList<JCheckBoxMenuItem>) obj;
          if (index >= al.size()) {
            index = 0;
          }
          checkBox = al.get(index++);
        }
        else {
          throw new IllegalStateException("Wrong object obj is " + obj);
        }
      }
      popup.add(checkBox);
    }

    popup.show(c, 8, 8);
  }

}
