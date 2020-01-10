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
package org.tinymediamanager.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;

import org.tinymediamanager.ui.components.ImageLabel;

/**
 * Class ColumnLayout - used to layout all components in a single column with a fixed width
 * 
 * @author Manuel Laggner
 */
public class ColumnLayout implements LayoutManager2 {
  private static Dimension MIN_SIZE = new Dimension(1, 1);
  private static Dimension MAX_SIZE = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

  @Override
  public void layoutContainer(Container parent) {
    Component components[] = parent.getComponents();

    // without children there is no layouting needed
    if (components.length == 0)
      return;

    // do a layouting based on the parent's size
    int y = 0;
    Dimension maxSize = preferredLayoutSize(parent);
    int width = (int) (maxSize.width * 0.98);

    for (Component component : components) {
      Dimension preferredSize = component.getPreferredSize();
      int height = preferredSize.height;
      if (component instanceof ImageLabel) {
        height = (int) (preferredSize.getHeight() * width * 1.0 / preferredSize.getWidth());
      }
      component.setBounds(0, y, width, height);
      y += height;
    }
  }

  @Override
  public Dimension minimumLayoutSize(Container parent) {
    return MIN_SIZE;
  }

  @Override
  public Dimension preferredLayoutSize(Container parent) {
    Component components[] = parent.getComponents();

    // calculate the complete height
    int maxWidth = parent.getWidth();
    int maxHeight = parent.getHeight();

    if (maxWidth == 0 || maxHeight == 0) {
      return MIN_SIZE;
    }

    int width = maxWidth;
    int height;

    // max 10 runs to prevent endless loops
    int counter = 0;

    while (true) {
      counter++;
      height = 0;

      for (Component component : components) {
        Dimension preferredSize = component.getPreferredSize();
        if (component instanceof ImageLabel) {
          int proportionalHeight = (int) (preferredSize.getHeight() * width * 1.0 / preferredSize.getWidth());
          height += proportionalHeight;
        }
        else {
          height += preferredSize.height;
        }
      }

      if (width < 1 || height < maxHeight || counter > 10) {
        break;
      }

      width = (int) (width * 0.95);
    }

    return new Dimension(width, height);
  }

  @Override
  public void removeLayoutComponent(Component comp) {
  }

  @Override
  public void addLayoutComponent(Component comp, Object constraints) {
  }

  @Override
  public void addLayoutComponent(String name, Component comp) {
  }

  @Override
  public float getLayoutAlignmentX(Container target) {
    return 0;
  }

  @Override
  public float getLayoutAlignmentY(Container target) {
    return 0;
  }

  @Override
  public void invalidateLayout(Container target) {
  }

  @Override
  public Dimension maximumLayoutSize(Container target) {
    return MAX_SIZE;
  }
}
