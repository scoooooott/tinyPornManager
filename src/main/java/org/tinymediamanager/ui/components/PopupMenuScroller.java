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

package org.tinymediamanager.ui.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.tinymediamanager.ui.IconManager;

public class PopupMenuScroller {

  private JPopupMenu               menu;
  private Component[]              menuItems;
  private MenuScrollItem           upItem;
  private MenuScrollItem           downItem;
  private final MenuScrollListener menuListener     = new MenuScrollListener();
  private int                      scrollCount;
  private int                      interval;
  private int                      topFixedCount;
  private int                      bottomFixedCount;
  private int                      firstIndex       = 0;
  private int                      keepVisibleIndex = -1;

  /**
   * Registers a popup menu to be scrolled with the default number of items to display at a time and the default scrolling interval.
   *
   * @param menu
   *          the popup menu
   * @return the MenuScroller
   */
  public static PopupMenuScroller setScrollerFor(JPopupMenu menu) {
    return new PopupMenuScroller(menu);
  }

  /**
   * Registers a popup menu to be scrolled with the default number of items to display at a time and the specified scrolling interval.
   *
   * @param menu
   *          the popup menu
   * @param scrollCount
   *          the number of items to display at a time
   * @return the MenuScroller
   * @throws IllegalArgumentException
   *           if scrollCount is 0 or negative
   */
  public static PopupMenuScroller setScrollerFor(JPopupMenu menu, int scrollCount) {
    return new PopupMenuScroller(menu, scrollCount);
  }

  /**
   * Registers a popup menu to be scrolled, with the specified number of items to display at a time and the specified scrolling interval.
   *
   * @param menu
   *          the popup menu
   * @param scrollCount
   *          the number of items to be displayed at a time
   * @param interval
   *          the scroll interval, in milliseconds
   * @return the MenuScroller
   * @throws IllegalArgumentException
   *           if scrollCount or interval is 0 or negative
   */
  public static PopupMenuScroller setScrollerFor(JPopupMenu menu, int scrollCount, int interval) {
    return new PopupMenuScroller(menu, scrollCount, interval);
  }

  /**
   * Registers a popup menu to be scrolled, with the specified number of items to display in the scrolling region, the specified scrolling interval,
   * and the specified numbers of items fixed at the top and bottom of the popup menu.
   *
   * @param menu
   *          the popup menu
   * @param scrollCount
   *          the number of items to display in the scrolling portion
   * @param interval
   *          the scroll interval, in milliseconds
   * @param topFixedCount
   *          the number of items to fix at the top. May be 0
   * @param bottomFixedCount
   *          the number of items to fix at the bottom. May be 0
   * @throws IllegalArgumentException
   *           if scrollCount or interval is 0 or negative or if topFixedCount or bottomFixedCount is negative
   * @return the MenuScroller
   */
  public static PopupMenuScroller setScrollerFor(JPopupMenu menu, int scrollCount, int interval, int topFixedCount, int bottomFixedCount) {
    return new PopupMenuScroller(menu, scrollCount, interval, topFixedCount, bottomFixedCount);
  }

  /**
   * Constructs a <code>MenuScroller</code> that scrolls a popup menu with the default number of items to display at a time, and default scrolling
   * interval.
   *
   * @param menu
   *          the popup menu
   */
  public PopupMenuScroller(JPopupMenu menu) {
    this(menu, 15);
  }

  /**
   * Constructs a <code>MenuScroller</code> that scrolls a popup menu with the specified number of items to display at a time, and default scrolling
   * interval.
   *
   * @param menu
   *          the popup menu
   * @param scrollCount
   *          the number of items to display at a time
   * @throws IllegalArgumentException
   *           if scrollCount is 0 or negative
   */
  public PopupMenuScroller(JPopupMenu menu, int scrollCount) {
    this(menu, scrollCount, 150);
  }

  /**
   * Constructs a <code>MenuScroller</code> that scrolls a popup menu with the specified number of items to display at a time, and specified scrolling
   * interval.
   *
   * @param menu
   *          the popup menu
   * @param scrollCount
   *          the number of items to display at a time
   * @param interval
   *          the scroll interval, in milliseconds
   * @throws IllegalArgumentException
   *           if scrollCount or interval is 0 or negative
   */
  public PopupMenuScroller(JPopupMenu menu, int scrollCount, int interval) {
    this(menu, scrollCount, interval, 0, 0);
  }

  /**
   * Constructs a <code>MenuScroller</code> that scrolls a popup menu with the specified number of items to display in the scrolling region, the
   * specified scrolling interval, and the specified numbers of items fixed at the top and bottom of the popup menu.
   *
   * @param menu
   *          the popup menu
   * @param scrollCount
   *          the number of items to display in the scrolling portion
   * @param interval
   *          the scroll interval, in milliseconds
   * @param topFixedCount
   *          the number of items to fix at the top. May be 0
   * @param bottomFixedCount
   *          the number of items to fix at the bottom. May be 0
   * @throws IllegalArgumentException
   *           if scrollCount or interval is 0 or negative or if topFixedCount or bottomFixedCount is negative
   */
  public PopupMenuScroller(JPopupMenu menu, int scrollCount, int interval, int topFixedCount, int bottomFixedCount) {
    if (scrollCount <= 0 || interval <= 0) {
      throw new IllegalArgumentException("scrollCount and interval must be greater than 0");
    }
    if (topFixedCount < 0 || bottomFixedCount < 0) {
      throw new IllegalArgumentException("topFixedCount and bottomFixedCount cannot be negative");
    }

    upItem = new MenuScrollItem(IconManager.CARET_UP, -1);
    downItem = new MenuScrollItem(IconManager.CARET_DOWN, +1);
    setScrollCount(scrollCount);
    setInterval(interval);
    setTopFixedCount(topFixedCount);
    setBottomFixedCount(bottomFixedCount);

    this.menu = menu;
    menu.addPopupMenuListener(menuListener);
  }

  /**
   * Returns the scroll interval in milliseconds
   *
   * @return the scroll interval in milliseconds
   */
  public int getInterval() {
    return interval;
  }

  /**
   * Sets the scroll interval in milliseconds
   *
   * @param interval
   *          the scroll interval in milliseconds
   * @throws IllegalArgumentException
   *           if interval is 0 or negative
   */
  public void setInterval(int interval) {
    if (interval <= 0) {
      throw new IllegalArgumentException("interval must be greater than 0");
    }
    upItem.setInterval(interval);
    downItem.setInterval(interval);
    this.interval = interval;
  }

  /**
   * Returns the number of items in the scrolling portion of the menu.
   *
   * @return the number of items to display at a time
   */
  public int getscrollCount() {
    return scrollCount;
  }

  /**
   * Sets the number of items in the scrolling portion of the menu.
   *
   * @param scrollCount
   *          the number of items to display at a time
   * @throws IllegalArgumentException
   *           if scrollCount is 0 or negative
   */
  public void setScrollCount(int scrollCount) {
    if (scrollCount <= 0) {
      throw new IllegalArgumentException("scrollCount must be greater than 0");
    }
    this.scrollCount = scrollCount;
    MenuSelectionManager.defaultManager().clearSelectedPath();
  }

  /**
   * Returns the number of items fixed at the top of the menu or popup menu.
   *
   * @return the number of items
   */
  public int getTopFixedCount() {
    return topFixedCount;
  }

  /**
   * Sets the number of items to fix at the top of the menu or popup menu.
   *
   * @param topFixedCount
   *          the number of items
   */
  public void setTopFixedCount(int topFixedCount) {
    if (firstIndex <= topFixedCount) {
      firstIndex = topFixedCount;
    }
    else {
      firstIndex += (topFixedCount - this.topFixedCount);
    }
    this.topFixedCount = topFixedCount;
  }

  /**
   * Returns the number of items fixed at the bottom of the menu or popup menu.
   *
   * @return the number of items
   */
  public int getBottomFixedCount() {
    return bottomFixedCount;
  }

  /**
   * Sets the number of items to fix at the bottom of the menu or popup menu.
   *
   * @param bottomFixedCount
   *          the number of items
   */
  public void setBottomFixedCount(int bottomFixedCount) {
    this.bottomFixedCount = bottomFixedCount;
  }

  /**
   * Scrolls the specified item into view each time the menu is opened. Call this method with <code>null</code> to restore the default behavior, which
   * is to show the menu as it last appeared.
   *
   * @param item
   *          the item to keep visible
   * @see #keepVisible(int)
   */
  public void keepVisible(JMenuItem item) {
    if (item == null) {
      keepVisibleIndex = -1;
    }
    else {
      int index = menu.getComponentIndex(item);
      keepVisibleIndex = index;
    }
  }

  /**
   * Scrolls the item at the specified index into view each time the menu is opened. Call this method with <code>-1</code> to restore the default
   * behavior, which is to show the menu as it last appeared.
   *
   * @param index
   *          the index of the item to keep visible
   * @see #keepVisible(javax.swing.JMenuItem)
   */
  public void keepVisible(int index) {
    keepVisibleIndex = index;
  }

  /**
   * Removes this MenuScroller from the associated menu and restores the default behavior of the menu.
   */
  public void dispose() {
    if (menu != null) {
      menu.removePopupMenuListener(menuListener);
      menu = null;
    }
  }

  /**
   * Ensures that the <code>dispose</code> method of this MenuScroller is called when there are no more refrences to it.
   * 
   * @see PopupMenuScroller#dispose()
   */
  @Override
  public void finalize() {
    dispose();
  }

  private void refreshMenu() {
    if (menuItems != null && menuItems.length > 0) {
      firstIndex = Math.max(topFixedCount, firstIndex);
      firstIndex = Math.min(menuItems.length - bottomFixedCount - scrollCount, firstIndex);

      upItem.setEnabled(firstIndex > topFixedCount);
      downItem.setEnabled(firstIndex + scrollCount < menuItems.length - bottomFixedCount);

      menu.removeAll();
      for (int i = 0; i < topFixedCount; i++) {
        menu.add(menuItems[i]);
      }
      if (topFixedCount > 0) {
        menu.addSeparator();
      }

      menu.add(upItem);
      for (int i = firstIndex; i < scrollCount + firstIndex; i++) {
        menu.add(menuItems[i]);
      }
      menu.add(downItem);

      if (bottomFixedCount > 0) {
        menu.addSeparator();
      }
      for (int i = menuItems.length - bottomFixedCount; i < menuItems.length; i++) {
        menu.add(menuItems[i]);
      }

      JComponent parent = (JComponent) upItem.getParent();
      parent.revalidate();
      parent.repaint();
    }
  }

  private class MenuScrollListener implements PopupMenuListener {

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      setMenuItems();
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      restoreMenuItems();
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
      restoreMenuItems();
    }

    private void setMenuItems() {
      menuItems = menu.getComponents();
      if (keepVisibleIndex >= topFixedCount && keepVisibleIndex <= menuItems.length - bottomFixedCount
          && (keepVisibleIndex > firstIndex + scrollCount || keepVisibleIndex < firstIndex)) {
        firstIndex = Math.min(firstIndex, keepVisibleIndex);
        firstIndex = Math.max(firstIndex, keepVisibleIndex - scrollCount + 1);
      }
      if (menuItems.length > topFixedCount + scrollCount + bottomFixedCount) {
        refreshMenu();
      }
    }

    private void restoreMenuItems() {
      menu.removeAll();
      for (Component component : menuItems) {
        menu.add(component);
      }
    }
  }

  private class MenuScrollTimer extends Timer {

    MenuScrollTimer(final int increment, int interval) {
      super(interval, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          firstIndex += increment;
          refreshMenu();
        }
      });
    }
  }

  private class MenuScrollItem extends JMenuItem implements ChangeListener {

    private MenuScrollTimer timer;

    MenuScrollItem(ImageIcon icon, int increment) {
      setIcon(icon);
      setDisabledIcon(icon);
      setHorizontalAlignment(JMenuItem.CENTER);
      timer = new MenuScrollTimer(increment, interval);
      addChangeListener(this);
    }

    public void setInterval(int interval) {
      timer.setDelay(interval);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
      if (isArmed() && !timer.isRunning()) {
        timer.start();
      }
      if (!isArmed() && timer.isRunning()) {
        timer.stop();
      }
    }
  }
}
