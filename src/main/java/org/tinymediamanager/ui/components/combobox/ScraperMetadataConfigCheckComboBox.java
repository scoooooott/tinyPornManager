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
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.tinymediamanager.core.ScraperMetadataConfig;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.FlatButton;

/**
 * the class ScraperMetadataConfigCheckComboBox is used to display a CheckCombBox for the scraper metadata config
 * 
 * @author Manuel Laggner
 */
public class ScraperMetadataConfigCheckComboBox<E extends ScraperMetadataConfig> extends TmmCheckComboBox<E> {
  private static final long serialVersionUID = 8153649858409237947L;

  public ScraperMetadataConfigCheckComboBox(final List<E> scrapers) {
    super(scrapers);
  }

  public ScraperMetadataConfigCheckComboBox(E[] scrapers) {
    super(scrapers);
  }

  @Override
  protected void setRenderer() {
    setRenderer(new ScraperMetadataConfigRenderer(checkBoxes));
  }

  @Override
  protected void init() {
    super.init();
    setEditor(new ScraperMetadataConfigEditor());
  }

  @Override
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
      cb.setText(obj.getDescription());
      cb.setToolTipText(obj.getToolTip());
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

  private class ScraperMetadataConfigRenderer extends CheckBoxRenderer {
    private ScraperMetadataConfigRenderer(final List<TmmCheckComboBoxItem<E>> items) {
      super(items);
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

  private class ScraperMetadataConfigEditor extends CheckBoxEditor {
    @Override
    protected JComponent getEditorItem(E userObject) {
      return new ScraperMetadataConfigEditorItem(userObject);
    }
  }

  private class ScraperMetadataConfigEditorItem extends JPanel {

    public ScraperMetadataConfigEditorItem(E userObject) {
      super();
      putClientProperty("class", "roundedPanel");
      setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
      setBorder(BorderFactory.createEmptyBorder(1, 10, 1, 5));

      JLabel label = new JLabel(userObject.getDescription());
      label.setToolTipText(userObject.getToolTip());
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
