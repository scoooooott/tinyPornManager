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
package org.tinymediamanager.ui.plaf.light;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;

import com.jtattoo.plaf.BaseTableUI;

/**
 * Class TmmLightTableUI
 * 
 * @author Manuel Laggner
 */
public class TmmLightTableUI extends BaseTableUI {
  private static Color SELECTED_BORDER = new Color(78, 107, 126);

  public static ComponentUI createUI(JComponent c) {
    return new TmmLightTableUI();
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
          catch (Exception e) {
          }
        }
        // if the component to render is a JComponent, add our tweaks.
        if (component instanceof JComponent) {
          JComponent jcomponent = (JComponent) component;
          jcomponent.setOpaque(isSelected);
          if (isSelected && !colsNotToDraw.contains(columnAtPoint)) {
            boolean draw = true;
            // draw the new border only if there is no spacing in the existend border
            if (jcomponent.getBorder() instanceof EmptyBorder) {
              EmptyBorder border = (EmptyBorder) jcomponent.getBorder();
              Insets borderInsets = border.getBorderInsets();
              if (borderInsets.left > 1 || borderInsets.right > 1 || borderInsets.top > 1 || borderInsets.bottom > 1) {
                draw = false;
              }
            }
            else if (jcomponent.getBorder() instanceof CompoundBorder) {
              draw = false;
            }
            if (draw) {
              jcomponent.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, SELECTED_BORDER));
            }
          }
        }

        super.paintComponent(graphics, component, container, x, y, w, h, shouldValidate);
      }
    };
  }
}
