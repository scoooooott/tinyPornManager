/*
 * Copyright 2012 - 2014 Manuel Laggner
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
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.tinymediamanager.core.threading.TmmTaskHandle;

/**
 * The class PopupPane. To present active tasks in a popup pane
 * 
 * @author Manuel Laggner
 */
public class PopupPane extends JScrollPane {
  private static final long          serialVersionUID = 4151412495928010232L;

  private JPanel                     view;
  private HashSet<TaskListComponent> listComponents;

  public PopupPane() {
    setName("progresspopup");
    setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    setFocusable(true);
    setRequestFocusEnabled(true);

    listComponents = new HashSet<TaskListComponent>();
    view = new JPanel();
    setViewportView(view);

    GridLayout grid = new GridLayout(0, 1);
    grid.setHgap(0);
    grid.setVgap(0);
    view.setLayout(grid);
    view.setBorder(BorderFactory.createEmptyBorder());
  }

  public void addListComponent(TaskListComponent lst) {
    listComponents.add(lst);
    if (view.getComponentCount() > 0) {
      JComponent previous = (JComponent) view.getComponent(view.getComponentCount() - 1);
      previous.setBorder(new BottomLineBorder());
    }
    lst.setBorder(BorderFactory.createEmptyBorder());
    view.add(lst);
    if (listComponents.size() > 5) {
      setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }
    else {
      setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    }
  }

  public void removeListComponent(TmmTaskHandle handle) {
    Iterator<TaskListComponent> it = listComponents.iterator();
    while (it.hasNext()) {
      TaskListComponent comp = (TaskListComponent) it.next();
      if (comp.getHandle() == handle) {
        view.remove(comp);
        it.remove();
        break;
      }
    }
    if (listComponents.size() > 5) {
      setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }
    else {
      setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    }
  }

  @Override
  public Dimension getPreferredSize() {
    int count = view.getComponentCount();
    int height = count > 0 ? view.getComponent(0).getPreferredSize().height : 0;
    int offset = count > 5 ? height * 5 + 5 : (count * height) + 5;
    // 22 is the width of the additional scrollbar
    return new Dimension(count > 5 ? TaskListComponent.ITEM_WIDTH + 22 : TaskListComponent.ITEM_WIDTH + 2, offset);
  }

  /**********************************************************************************************
   * helper classes
   **********************************************************************************************/
  private class BottomLineBorder implements Border {
    private Insets ins = new Insets(0, 0, 1, 0);
    private Color  col = new Color(221, 229, 248);

    public BottomLineBorder() {
    }

    @Override
    public Insets getBorderInsets(Component c) {
      return ins;
    }

    @Override
    public boolean isBorderOpaque() {
      return false;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      Color old = g.getColor();
      g.setColor(col);
      g.drawRect(x, y + height - 2, width, 1);
      g.setColor(old);
    }
  }
}
