/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.tinymediamanager.ui.TmmFontHelper;

import com.jtattoo.plaf.luna.LunaComboBoxUI;

/**
 * The class SmallTextFieldBorder - for a smaller version of a JComboBox
 * 
 * @author Manuel Laggner
 */
public class SmallComboBox extends JComboBox {
  private static final long serialVersionUID = 256568641808640L;

  public SmallComboBox() {
    super();
    // try catch block for safety reasons (and WBPro)
    try {
      init();
    }
    catch (Exception e) {
    }
  }

  public SmallComboBox(Object[] items) {
    super(items);

    // try catch block for safety reasons (and WBPro)
    try {
      init();
    }
    catch (Exception e) {
    }
  }

  private void init() throws Exception {
    setRenderer(new SmallComboBoxRenderer());
    setEditor(new SmallComboBoxEditor());
    setUI(new SmallComboBoxUI());
  }

  class SmallComboBoxRenderer extends JPanel implements ListCellRenderer {
    private static final long serialVersionUID = 7291014994809111069L;

    private JLabel            labelItem        = new JLabel();

    public SmallComboBoxRenderer() {
      setLayout(new GridBagLayout());
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.weightx = 1.0;
      constraints.insets = new Insets(0, 2, 0, 2);

      labelItem.setOpaque(false);
      labelItem.setHorizontalAlignment(JLabel.LEFT);
      TmmFontHelper.changeFont(labelItem, 0.916);

      add(labelItem, constraints);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      if (value != null) {
        labelItem.setText(value.toString());
      }

      Color bg = null;
      Color fg = null;

      bg = UIManager.getColor("List.dropCellBackground");
      fg = UIManager.getColor("List.dropCellForeground");

      if (isSelected) {
        setBackground(bg == null ? list.getSelectionBackground() : bg);
        setForeground(fg == null ? list.getSelectionForeground() : fg);
      }
      else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }

      return this;
    }

  }

  class SmallComboBoxEditor extends BasicComboBoxEditor {
    private JPanel panel     = new JPanel();
    private JLabel labelItem = new JLabel();

    public SmallComboBoxEditor() {
      panel.setLayout(new GridBagLayout());
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.weightx = 1.0;
      constraints.insets = new Insets(0, 2, 0, 2);

      labelItem.setOpaque(false);
      labelItem.setHorizontalAlignment(JLabel.LEFT);
      labelItem.setForeground(Color.WHITE);

      panel.add(labelItem, constraints);
      panel.setBackground(Color.BLUE);
    }

    @Override
    public Component getEditorComponent() {
      return this.panel;
    }

    @Override
    public Object getItem() {
      return null;
    }
  }

  static class SmallComboBoxUI extends LunaComboBoxUI {
    public static ComponentUI createUI(JComponent c) {
      return new SmallComboBoxUI();
    }

    @Override
    public void installUI(JComponent c) {
      super.installUI(c);
      comboBox.setRequestFocusEnabled(true);
      if (comboBox.getEditor() != null) {
        if (comboBox.getEditor().getEditorComponent() instanceof JTextField) {
          ((JTextField) (comboBox.getEditor().getEditorComponent())).setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
        }
      }
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
      Dimension size = super.getPreferredSize(c);
      return new Dimension(size.width + 2, size.height - 2);
    }
  }
}
