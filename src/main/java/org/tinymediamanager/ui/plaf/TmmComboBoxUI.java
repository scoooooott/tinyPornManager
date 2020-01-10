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
package org.tinymediamanager.ui.plaf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseComboBoxUI;
import com.jtattoo.plaf.NoFocusButton;

/**
 * @author Manuel Laggner
 */
public class TmmComboBoxUI extends BaseComboBoxUI {

  public static ComponentUI createUI(JComponent c) {
    return new TmmComboBoxUI();
  }

  @Override
  public JButton createArrowButton() {
    return new ArrowButton();
  }

  @Override
  public void installUI(JComponent c) {
    super.installUI(c);
    if (comboBox.getEditor() != null) {
      if (comboBox.getEditor().getEditorComponent() instanceof JTextField) {
        ((JTextField) (comboBox.getEditor().getEditorComponent())).setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
      }
      else if (comboBox.getEditor().getEditorComponent() instanceof JLabel) {
        ((JLabel) (comboBox.getEditor().getEditorComponent())).setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
      }
    }
  }

  @Override
  protected void installDefaults() {
    super.installDefaults();
    squareButton = false;
  }

  @Override
  public Dimension getPreferredSize(JComponent c) {
    Dimension size = super.getPreferredSize(c);
    return new Dimension(size.width + 6, size.height); // +6 to do not crop the content in the editor
  }

  @SuppressWarnings({ "unchecked" })
  @Override
  public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
    @SuppressWarnings("rawtypes")
    ListCellRenderer renderer = comboBox.getRenderer();
    Component c;

    if (hasFocus && !isPopupVisible(comboBox)) {
      c = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, true, false);
    }
    else {
      c = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, false, false);
      c.setBackground(UIManager.getColor("ComboBox.background"));
    }

    c.setFont(comboBox.getFont());
    if (comboBox.isEnabled()) {
      c.setForeground(comboBox.getForeground());
      c.setBackground(comboBox.getBackground());
    }
    else {
      c.setForeground(UIManager.getColor("ComboBox.disabledForeground", c.getLocale()));
      c.setBackground(UIManager.getColor("ComboBox.disabledBackground", c.getLocale()));
    }

    // Fix for 4238829: should lay out the JPanel.
    boolean shouldValidate = false;
    if (c instanceof JPanel) {
      shouldValidate = true;
    }

    int x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;
    if (padding != null) {
      x = bounds.x + padding.left;
      y = bounds.y + padding.top;
      w = bounds.width - (padding.left + padding.right);
      h = bounds.height - (padding.top + padding.bottom);
    }

    currentValuePane.paintComponent(g, c, comboBox, x, y, w, h, shouldValidate);
  }

  @Override
  public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
    Color t = g.getColor();
    if (comboBox.isEnabled()) {
      g.setColor(UIManager.getColor("ComboBox.background", comboBox.getLocale()));
    }
    else {
      g.setColor(UIManager.getColor("ComboBox.disabledBackground", comboBox.getLocale()));
    }
    g.fillRect(0, 0, comboBox.getWidth(), comboBox.getHeight());
    g.setColor(t);
  }

  @Override
  protected void setButtonBorder() {
  }

  private class ArrowButton extends NoFocusButton {
    private static final long serialVersionUID = -2765755741007665606L;

    public ArrowButton() {
      super();
      setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    @Override
    public void paint(Graphics g) {
      int w = getWidth();
      int h = getHeight();

      g.setColor(AbstractLookAndFeel.getButtonForegroundColor());

      int[] xPoints = { w / 2 + 5, w / 2 - 5, w / 2 };
      int[] yPoints = { h / 2 - 1, h / 2 - 1, h / 2 + 4 };
      g.fillPolygon(xPoints, yPoints, xPoints.length);
    }
  }
}
