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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.ComponentUI;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseTableUI;

/**
 * Class TmmTableUI
 *
 * @author Manuel Laggner
 */
public class TmmTableUI extends BaseTableUI {

  public static ComponentUI createUI(JComponent c) {
    return new TmmTableUI();
  }

  @Override
  public void installUI(JComponent c) {
    super.installUI(c);

    table.remove(rendererPane);
    rendererPane = createCustomCellRendererPane();
    table.add(rendererPane);
  }

  /**
   * Creates a custom {@link CellRendererPane} that sets the renderer component to be non-opaque if the associated row isn't selected. This custom
   * {@code CellRendererPane} is needed because a table UI delegate has no prepare renderer like {@link JTable} has.
   */
  private CellRendererPane createCustomCellRendererPane() {
    return new CellRendererPane() {
      private static final long serialVersionUID = 7146435127995900923L;

      @SuppressWarnings({ "unchecked", "rawtypes" })
      @Override
      public void paintComponent(Graphics graphics, Component component, Container container, int x, int y, int w, int h, boolean shouldValidate) {
        // figure out what row we're rendering a cell for.
        Point point = new Point(x, y);
        int rowAtPoint = table.rowAtPoint(point);
        int columnAtPoint = table.columnAtPoint(point);

        boolean isSelected = table.isRowSelected(rowAtPoint);

        // look if there are any non drawable borders defined
        Object prop = table.getClientProperty("borderNotToDraw");
        List<Integer> colsNotToDraw = new ArrayList<>();
        if (prop != null && prop instanceof List<?>) {
          try {
            colsNotToDraw.addAll((List) prop);
          }
          catch (Exception ignored) {
          }
        }

        // if the component to render is a JComponent, add our tweaks.
        if (component instanceof JComponent) {
          JComponent jcomponent = (JComponent) component;
          jcomponent.setOpaque(isSelected);

          if (isSelected && !colsNotToDraw.contains(columnAtPoint)) {
            jcomponent.setBorder(BorderFactory.createCompoundBorder(new RightSideBorder(AbstractLookAndFeel.getTheme().getSelectedGridColor()),
                jcomponent.getBorder()));
          }
        }

        super.paintComponent(graphics, component, container, x, y, w, h, shouldValidate);
      }
    };
  }

  private static class RightSideBorder extends AbstractBorder {

    private final Color color;
    private final int   thickness = 1;

    public RightSideBorder(Color color) {
      this.color = color;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      Graphics2D g2d = (Graphics2D) g.create();
      g2d.setColor(this.color);
      g2d.setStroke(new BasicStroke(thickness));
      g2d.drawLine(width - 1, 0, width - 1, height - 1);
      g2d.dispose();
    }
  }
}
