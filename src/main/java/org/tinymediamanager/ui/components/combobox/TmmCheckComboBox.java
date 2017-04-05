/*
 * Copyright 2012 - 2017 Manuel Laggner
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
package org.tinymediamanager.ui.components.combobox;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.tinymediamanager.ui.UTF8Control;

/**
 * the class TmmCheckComboBox extends the JComboBox by a checkbox for multiple selections
 * 
 * @author Manuel Laggner
 *
 * @param <E>
 */
public class TmmCheckComboBox<E> extends JComboBox<TmmCheckComboBoxItem<E>> {
  private static final long                         serialVersionUID = -7796247854176782396L;
  protected static final ResourceBundle             BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  protected List<TmmCheckComboBoxItem<E>>           checkBoxes;
  protected Map<E, Boolean>                         selectedItems;
  protected List<TmmCheckComboBoxSelectionListener> changedListeners = new Vector<>();

  protected Object                                  nullObject       = new Object();

  /**
   * create the CheckComboBox without any items
   */
  public TmmCheckComboBox() {
    this(new HashSet<>(), false);
  }

  /**
   * create the CheckComboBox with the given items
   * 
   * @param items
   *          a list of initial items
   */
  public TmmCheckComboBox(final List<E> items) {
    this(new LinkedHashSet<>(items), false);
  }

  /**
   * create the CheckComboBox with the given items
   * 
   * @param items
   *          a set os initial items
   */
  public TmmCheckComboBox(final Set<E> items) {
    this(items, false);
  }

  /**
   * create the CheckComboBox with the given items
   * 
   * @param items
   *          an array of initial items
   */
  public TmmCheckComboBox(final E[] items) {
    this(new LinkedHashSet<>(Arrays.asList(items)), false);
  }

  /**
   * set new items to the CheckComboBox
   * 
   * @param items
   *          the items to be set
   */
  public void setItems(List<E> items) {
    resetObjs(new LinkedHashSet<>(items), false);
  }

  protected TmmCheckComboBox(final Set<E> objs, boolean selected) {
    resetObjs(objs, selected);
  }

  /**
   * adds a new CheckComboBoxSelectionChangedListener
   * 
   * @param listener
   *          the new listener
   */
  public void addSelectionChangedListener(TmmCheckComboBoxSelectionListener listener) {
    if (listener == null) {
      return;
    }
    changedListeners.add(listener);
  }

  /**
   * remove the given CheckComboBoxSelectionChangedListener
   * 
   * @param listener
   *          the listener to be removed
   */
  public void removeSelectionChangedListener(TmmCheckComboBoxSelectionListener listener) {
    changedListeners.remove(listener);
  }

  protected void resetObjs(final Set<E> items, boolean selected) {
    selectedItems = new LinkedHashMap<>();
    for (E item : items) {
      selectedItems.put(item, selected);
    }
    reset();
  }

  /**
   * get an array of all checked items
   * 
   * @return an array of all checked items
   */
  public List<E> getSelectedItems() {
    Set<E> ret = new LinkedHashSet<>();
    for (Map.Entry<E, Boolean> entry : selectedItems.entrySet()) {
      E obj = entry.getKey();
      Boolean selected = entry.getValue();

      if (selected) {
        ret.add(obj);
      }
    }

    return new ArrayList<>(ret);
  }

  /**
   * set selected items
   * 
   * @param items
   *          the items to be set as selected
   */
  public void setSelectedItems(Collection<E> items) {
    if (items == null) {
      return;
    }

    for (E item : items) {
      if (selectedItems.containsKey(item)) {
        selectedItems.put(item, true);
      }
    }

    reset();
    repaint();
  }

  /**
   * set selected items
   * 
   * @param items
   *          the items to be set as selected
   */
  public void setSelectedItems(E[] items) {
    setSelectedItems(Arrays.asList(items));
  }

  protected void reset() {
    removeAllItems();

    initCheckBoxes();

    this.addItem(new TmmCheckComboBoxItem<>(""));
    for (TmmCheckComboBoxItem<E> checkBox : checkBoxes) {
      this.addItem(checkBox);
    }

    setRenderer();
    addActionListener(this);
  }

  /**
   * set the right renderer for this check combo box
   */
  protected void setRenderer() {
    setRenderer(new CheckBoxRenderer(checkBoxes));
  }

  protected void initCheckBoxes() {
    checkBoxes = new Vector<>();

    boolean selectedAll = true;
    boolean selectedNone = true;

    TmmCheckComboBoxItem<E> cb;
    for (Map.Entry<E, Boolean> entry : selectedItems.entrySet()) {
      E obj = entry.getKey();
      Boolean selected = entry.getValue();

      if (selected) {
        selectedNone = false;
      }
      else {
        selectedAll = false;
      }

      cb = new TmmCheckComboBoxItem<>(obj);
      cb.setSelected(selected);
      checkBoxes.add(cb);
    }

    cb = new TmmCheckComboBoxItem<>(BUNDLE.getString("Button.selectall")); //$NON-NLS-1$
    cb.setSelected(selectedAll);
    checkBoxes.add(cb);

    cb = new TmmCheckComboBoxItem<>(BUNDLE.getString("Button.selectnone")); //$NON-NLS-1$
    cb.setSelected(selectedNone);
    checkBoxes.add(cb);
  }

  protected void checkBoxSelectionChanged(int index) {
    int n = checkBoxes.size();
    if (index < 0 || index >= n)
      return;

    // Set selectedObj = getSelected();
    if (index < n - 2) {
      TmmCheckComboBoxItem<E> cb = checkBoxes.get(index);
      if (cb.getUserObject() == nullObject) {
        return;
      }

      if (cb.isSelected()) {
        cb.setSelected(false);
        selectedItems.put(cb.getUserObject(), false);

        // Select all
        checkBoxes.get(n - 2).setSelected(false);
        // select none
        checkBoxes.get(n - 1).setSelected(getSelectedItems() == null);
      }
      else {
        cb.setSelected(true);
        selectedItems.put(cb.getUserObject(), true);

        List<E> sobjs = getSelectedItems();
        // Select all
        checkBoxes.get(n - 2).setSelected(sobjs != null && sobjs.size() == n - 2);
        // select none
        checkBoxes.get(n - 1).setSelected(false);
      }
    }
    else if (index == n - 2) {
      for (E obj : selectedItems.keySet()) {
        if (obj != nullObject) {
          selectedItems.put(obj, true);
        }
      }

      for (int i = 0; i < n - 1; i++) {
        if (checkBoxes.get(i) != nullObject)
          checkBoxes.get(i).setSelected(true);
      }
      checkBoxes.get(n - 1).setSelected(false);
    }
    else { // if (index==n-1)
      for (E obj : selectedItems.keySet()) {
        selectedItems.put(obj, false);
      }

      for (int i = 0; i < n - 1; i++) {
        checkBoxes.get(i).setSelected(false);
      }
      checkBoxes.get(n - 1).setSelected(true);
    }

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    int sel = getSelectedIndex();

    if (sel == 0) {
      getUI().setPopupVisible(this, false);
    }
    else if (sel > 0) {
      checkBoxSelectionChanged(sel - 1);
      for (TmmCheckComboBoxSelectionListener listener : changedListeners) {
        listener.selectionChanged(sel - 1);
      }
    }

    this.setSelectedIndex(-1); // clear selection
  }

  @Override
  public void setPopupVisible(boolean flag) {
    // leave empty
  }

  /*
   * helper classes
   */
  /**
   * checkbox renderer for combobox
   */
  protected class CheckBoxRenderer implements ListCellRenderer<TmmCheckComboBoxItem<E>> {
    protected final DefaultListCellRenderer       defaultRenderer = new DefaultListCellRenderer();
    protected javax.swing.JSeparator              separator;
    protected final List<TmmCheckComboBoxItem<E>> checkBoxes;

    public CheckBoxRenderer(final List<TmmCheckComboBoxItem<E>> items) {
      this.checkBoxes = items;
      separator = new javax.swing.JSeparator(javax.swing.JSeparator.HORIZONTAL);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends TmmCheckComboBoxItem<E>> list, TmmCheckComboBoxItem<E> value, int index,
        boolean isSelected, boolean cellHasFocus) {
      if (index > 0 && index <= checkBoxes.size()) {
        TmmCheckComboBoxItem<E> cb = checkBoxes.get(index - 1);
        if (cb.getUserObject() == nullObject) {
          return separator;
        }

        if (isSelected) {
          cb.setBackground(UIManager.getColor("ComboBox.selectionBackground"));
          cb.setForeground(UIManager.getColor("ComboBox.selectionForeground"));
        }
        else {
          cb.setBackground(UIManager.getColor("ComboBox.background"));
          cb.setForeground(UIManager.getColor("ComboBox.foreground"));
        }

        return cb;
      }

      String str;
      List<E> objs = getSelectedItems();
      Vector<String> strs = new Vector<>();
      if (objs.isEmpty()) {
        str = BUNDLE.getString("ComboBox.select"); //$NON-NLS-1$
      }
      else {
        for (Object obj : objs) {
          strs.add(obj.toString());
        }
        str = strs.toString();
      }
      return defaultRenderer.getListCellRendererComponent(list, str, index, isSelected, cellHasFocus);
    }
  }
}
