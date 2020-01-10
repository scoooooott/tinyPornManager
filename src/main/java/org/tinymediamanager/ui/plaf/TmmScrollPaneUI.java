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

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseScrollPaneUI;

public class TmmScrollPaneUI extends BaseScrollPaneUI {

  public static ComponentUI createUI(JComponent c) {
    return new TmmScrollPaneUI();
  }

  @Override
  protected void installDefaults(JScrollPane scrollpane) {
    LookAndFeel.installColorsAndFont(scrollpane, "ScrollPane.background", "ScrollPane.foreground", "ScrollPane.font");

    Border vpBorder = scrollpane.getViewportBorder();
    if ((vpBorder == null) || (vpBorder instanceof UIResource)) {
      vpBorder = UIManager.getBorder("ScrollPane.viewportBorder");
      scrollpane.setViewportBorder(vpBorder);
    }
    LookAndFeel.installProperty(scrollpane, "opaque", Boolean.TRUE);

    Object roundPane = scrollpane.getClientProperty("roundScrollPane");
    if (roundPane != null && "true".equals(roundPane.toString())) {
      LookAndFeel.installColorsAndFont(scrollpane, "ScrollPane.foreground", "ScrollPane.background", "ScrollPane.font");
      LookAndFeel.installBorder(scrollpane, "ScrollPane.border");
      scrollpane.getViewport().setOpaque(false);
      scrollpane.setBackground(AbstractLookAndFeel.getControlColorLight());

      scrollpane.getHorizontalScrollBar().putClientProperty("swapColors", "true");
      scrollpane.getHorizontalScrollBar().updateUI();
      scrollpane.getHorizontalScrollBar().setBorder(new EmptyBorder(0, 1, 0, 1));
      scrollpane.getHorizontalScrollBar().setOpaque(false);
      scrollpane.getVerticalScrollBar().putClientProperty("swapColors", "true");
      scrollpane.getVerticalScrollBar().updateUI();
      scrollpane.getVerticalScrollBar().setBorder(new EmptyBorder(0, 0, 1, 0));
      scrollpane.getVerticalScrollBar().setOpaque(false);

      if (scrollpane.getViewport().getView() instanceof JTable) {
        scrollpane.setColumnHeader(new JViewport());
        scrollpane.getColumnHeader().setOpaque(false);
        JTable table = (JTable) scrollpane.getViewport().getView();
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.getTableHeader().setOpaque(false);

        TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
        ((DefaultTableCellRenderer) renderer).setBorder(null);
        Component component = renderer.getTableCellRendererComponent(table, "", false, false, -1, 0);
        ((JComponent) component).setOpaque(false);
        for (int i = 0; i < table.getColumnCount(); i++) {
          table.getColumnModel().getColumn(i).setHeaderRenderer(new HeaderRenderer());
          table.getColumnModel().getColumn(i).setCellRenderer(new BorderTableCellRenderer(0, 5, 0, 0));
        }
      }

      scrollpane.getViewport().setOpaque(false);

      if (scrollpane.getViewport().getView() instanceof JComponent) {
        ((JComponent) scrollpane.getViewport().getView()).setOpaque(false);
      }

      if (scrollpane.getViewport().getView() instanceof JList) {
        ((JList) scrollpane.getViewport().getView()).setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        if ((((JList) scrollpane.getViewport().getView()).getCellRenderer()) != null) {
          ((DefaultListCellRenderer) ((JList) scrollpane.getViewport().getView()).getCellRenderer()).setOpaque(false);
        }
      }
    }
  }

  private static class HeaderRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 7963585655106103416L;
    private static Border     defaultBorder    = BorderFactory.createMatteBorder(0, 0, 1, 0, AbstractLookAndFeel.getGridColor());

    public HeaderRenderer() {
      setHorizontalAlignment(SwingConstants.CENTER);
      setOpaque(false);

      // This call is needed because DefaultTableCellRenderer calls setBorder()
      // in its constructor, which is executed after updateUI()
      setBorder(defaultBorder);
      setOpaque(false);
    }

    @Override
    public void updateUI() {
      super.updateUI();
      setBorder(defaultBorder);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
      JTableHeader h = table != null ? table.getTableHeader() : null;

      if (h != null) {
        setEnabled(h.isEnabled());
        setComponentOrientation(h.getComponentOrientation());

        setForeground(h.getForeground());
        setBackground(h.getBackground());
        setFont(h.getFont());
      }
      else {
        /* Use sensible values instead of random leftover values from the last call */
        setEnabled(true);
        setComponentOrientation(ComponentOrientation.UNKNOWN);

        setForeground(UIManager.getColor("TableHeader.foreground"));
        setBackground(UIManager.getColor("TableHeader.background"));
        setFont(UIManager.getFont("TableHeader.font"));
      }

      setValue(value);

      return this;
    }
  }

  private static class BorderTableCellRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = -6545791732880295743L;
    private Border            border;

    /**
     * create the CellRenderer with the default inset (2 px left)
     */
    public BorderTableCellRenderer() {
      border = BorderFactory.createEmptyBorder(0, 2, 0, 0);
    }

    public BorderTableCellRenderer(int top, int left, int bottom, int right) {
      border = BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    public BorderTableCellRenderer(Insets insets) {
      border = BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      setForeground(table.getForeground());
      if (isSelected) {
        setBackground(table.getSelectionBackground());
        setForeground(table.getSelectionForeground());
      }
      else {
        setBackground(table.getBackground());

      }

      // left margin
      Component comp = super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
      Border defaultBorder = ((JComponent) comp).getBorder();
      this.setBorder(BorderFactory.createCompoundBorder(defaultBorder, border));

      if (value != null) {
        setValue(value.toString());
      }
      else {
        setValue("");
      }

      return this;
    }
  }
}
