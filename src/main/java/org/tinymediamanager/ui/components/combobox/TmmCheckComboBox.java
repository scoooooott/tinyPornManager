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
package org.tinymediamanager.ui.components.combobox;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.WrapLayout;
import org.tinymediamanager.ui.components.FlatButton;

/**
 * the class TmmCheckComboBox extends the JComboBox by a checkbox for multiple selections
 * 
 * @author Manuel Laggner
 *
 * @param <E>
 */
public class TmmCheckComboBox<E> extends JComboBox<TmmCheckComboBoxItem<E>> {
  private static final long                         serialVersionUID = -7796247854176782396L;
  protected static final ResourceBundle             BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  protected List<TmmCheckComboBoxItem<E>>           checkBoxes;
  protected Map<E, Boolean>                         selectedItems;
  protected List<TmmCheckComboBoxSelectionListener> changedListeners = new Vector<>();

  protected Object                                  nullObject       = new Object();
  protected JComponent                              editor           = null;

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
    init();
  }

  /**
   * initializations
   */
  protected void init() {
    setEditor(new CheckBoxEditor());
    setEditable(true);
  }

  @Override
  public void setEditor(ComboBoxEditor anEditor) {
    super.setEditor(anEditor);
    if (anEditor instanceof JComponent) {
      this.editor = (JComponent) anEditor;
    }
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
    if (items == null || items.isEmpty()) {
      return;
    }

    for (E item : items) {
      if (selectedItems.containsKey(item)) {
        selectedItems.put(item, true);
      }
    }

    reset();
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

    // force the JComboBox to re-calculate the size
    if (editor != null) {
      SwingUtilities.invokeLater(() -> {
        editor.firePropertyChange("border", true, false);
        editor.revalidate();
      });
    }
    revalidate();
    repaint();
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

    cb = new TmmCheckComboBoxItem<>(BUNDLE.getString("Button.selectall"));
    cb.setSelected(selectedAll);
    checkBoxes.add(cb);

    cb = new TmmCheckComboBoxItem<>(BUNDLE.getString("Button.selectnone"));
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
    protected final ListCellRenderer              defaultRenderer;
    protected javax.swing.JSeparator              separator;
    protected final List<TmmCheckComboBoxItem<E>> checkBoxes;

    public CheckBoxRenderer(final List<TmmCheckComboBoxItem<E>> items) {
      this.checkBoxes = items;
      separator = new javax.swing.JSeparator(javax.swing.JSeparator.HORIZONTAL);

      // get the default renderer from a JComboBox
      JComboBox box = new JComboBox();
      defaultRenderer = box.getRenderer();
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends TmmCheckComboBoxItem<E>> list, TmmCheckComboBoxItem<E> value, int index,
        boolean isSelected, boolean cellHasFocus) {
      if (index > 0 && index <= checkBoxes.size()) {
        TmmCheckComboBoxItem<E> cb = checkBoxes.get(index - 1);
        if (cb.getUserObject() == nullObject) {
          list.setToolTipText(null);
          return separator;
        }

        if (isSelected) {
          cb.setBackground(UIManager.getColor("ComboBox.selectionBackground"));
          cb.setForeground(UIManager.getColor("ComboBox.selectionForeground"));
          list.setToolTipText(checkBoxes.get(index - 1).getText());
        }
        else {
          cb.setBackground(UIManager.getColor("ComboBox.background"));
          cb.setForeground(UIManager.getColor("ComboBox.foreground"));
        }

        return cb;
      }

      list.setToolTipText(null);
      return defaultRenderer.getListCellRendererComponent(list, BUNDLE.getString("ComboBox.select"), index, isSelected, cellHasFocus);
    }
  }

  protected class CheckBoxEditor extends JPanel implements ComboBoxEditor {
    public CheckBoxEditor() {
      super();

      setLayout(new WrapLayout(FlowLayout.LEFT, 5, 2));
      setOpaque(false);
      setBorder(null);
    }

    @Override
    public Component getEditorComponent() {
      return this;
    }

    @Override
    public void setItem(Object anObject) {
      removeAll();

      List<E> objs = getSelectedItems();
      if (objs.isEmpty()) {
        add(new JLabel(BUNDLE.getString("ComboBox.select")));
      }
      else {
        for (E obj : objs) {
          add(getEditorItem(obj));
        }
      }

      // force the JComboBox to re-calculate the size
      firePropertyChange("border", true, false);
      revalidate();
    }

    protected JComponent getEditorItem(E userObject) {
      return new CheckBoxEditorItem(userObject);
    }

    @Override
    public Object getItem() {
      return getSelectedItems();
    }

    @Override
    public void selectAll() {

    }

    @Override
    public void addActionListener(ActionListener l) {

    }

    @Override
    public void removeActionListener(ActionListener l) {

    }
  }

  protected class CheckBoxEditorItem extends JPanel {

    public CheckBoxEditorItem(E userObject) {
      super();
      putClientProperty("class", "roundedPanel");
      setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
      setBorder(BorderFactory.createEmptyBorder(1, 10, 1, 5));

      JLabel label = new JLabel(userObject.toString());
      label.setBorder(BorderFactory.createEmptyBorder());
      add(label);

      JButton button = new FlatButton(IconManager.DELETE);
      button.setBorder(BorderFactory.createEmptyBorder());
      button.addActionListener(e -> {
        selectedItems.put(userObject, false);
        reset();
      });
      add(button);
    }
  }
}
