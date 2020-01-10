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
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.SwingConstants;

/**
 * The Class EqualsLayout.
 * 
 * @author Santhosh Kumar - santhosh@in.fiorano.com
 */
public class EqualsLayout implements LayoutManager, SwingConstants {

  /** The gap. */
  private int gap;

  /** The alignment. */
  private int alignment;

  /** The min width. */
  private int minWidth = 0;

  /**
   * Instantiates a new equals layout.
   * 
   * @param alignment
   *          the alignment
   * @param gap
   *          the gap
   */
  public EqualsLayout(int alignment, int gap) {
    setGap(gap);
    setAlignment(alignment);
  }

  /**
   * Instantiates a new equals layout.
   * 
   * @param gap
   *          the gap
   */
  public EqualsLayout(int gap) {
    this(RIGHT, gap);
  }

  /**
   * Gets the alignment.
   * 
   * @return the alignment
   */
  public int getAlignment() {
    return alignment;
  }

  /**
   * Sets the alignment.
   * 
   * @param alignment
   *          the new alignment
   */
  public void setAlignment(int alignment) {
    this.alignment = alignment;
  }

  /**
   * Gets the gap.
   * 
   * @return the gap
   */
  public int getGap() {
    return gap;
  }

  /**
   * Sets the min width.
   * 
   * @param width
   *          the new min width
   */
  public void setMinWidth(int width) {
    this.minWidth = width;
  }

  /**
   * Gets the min width.
   * 
   * @return the min width
   */
  public int getMinWidth() {
    return this.minWidth;
  }

  /**
   * Sets the gap.
   * 
   * @param gap
   *          the new gap
   */
  public void setGap(int gap) {
    this.gap = gap;
  }

  /**
   * Dimensions.
   * 
   * @param children
   *          the children
   * @return the dimension[]
   */
  private Dimension[] dimensions(Component children[]) {
    int maxWidth = 0;
    int maxHeight = 0;
    int visibleCount = 0;
    Dimension componentPreferredSize;

    for (Component aChildren : children) {
      if (aChildren.isVisible()) {
        componentPreferredSize = aChildren.getPreferredSize();
        maxWidth = Math.max(maxWidth, componentPreferredSize.width);
        maxHeight = Math.max(maxHeight, componentPreferredSize.height);
        visibleCount++;
      }
    }

    if (maxWidth < minWidth) {
      maxWidth = minWidth;
    }

    int usedWidth = maxWidth * visibleCount + gap * (visibleCount - 1);
    int usedHeight = maxHeight;
    return new Dimension[] { new Dimension(maxWidth, maxHeight), new Dimension(usedWidth, usedHeight), };
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
   */
  @Override
  public void layoutContainer(Container container) {
    Insets insets = container.getInsets();

    Component[] children = container.getComponents();
    Dimension dim[] = dimensions(children);

    int maxWidth = dim[0].width;
    int maxHeight = dim[0].height;
    int usedWidth = dim[1].width;

    switch (alignment) {
      case LEFT:
      case TOP:
        for (int i = 0, c = children.length; i < c; i++) {
          if (!children[i].isVisible())
            continue;
          children[i].setBounds(insets.left + (maxWidth + gap) * i, insets.top, maxWidth, maxHeight);
        }
        break;
      case RIGHT:
      case BOTTOM:
        for (int i = 0, c = children.length; i < c; i++) {
          if (!children[i].isVisible())
            continue;
          children[i].setBounds(container.getWidth() - insets.right - usedWidth + (maxWidth + gap) * i, insets.top, maxWidth, maxHeight);
        }
        break;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
   */
  @Override
  public Dimension minimumLayoutSize(Container c) {
    return preferredLayoutSize(c);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
   */
  @Override
  public Dimension preferredLayoutSize(Container container) {
    Insets insets = container.getInsets();

    Component[] children = container.getComponents();
    Dimension dim[] = dimensions(children);

    int usedWidth = dim[1].width;
    int usedHeight = dim[1].height;

    return new Dimension(insets.left + usedWidth + insets.right, insets.top + usedHeight + insets.bottom);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
   */
  @Override
  public void addLayoutComponent(String string, Component comp) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
   */
  @Override
  public void removeLayoutComponent(Component c) {
  }
}
