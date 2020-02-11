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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * the class {@link ScrollingEventDelegator} is used to delegate the scroll event to to parent
 * 
 * @author Manuel Laggner
 */
public final class ScrollingEventDelegator {

  private ScrollingEventDelegator() {
    // hide constructor for utilits classes
  }

  /**
   * Passes mouse wheel events to the parent component if this component cannot scroll further in the given direction.
   * <p>
   * This is the behavior of most web browsers and similar programs that need to handle nested scrollable components.
   */
  public static void install(JScrollPane pane) {
    pane.addMouseWheelListener(new Listener(pane));
  }

  private static class Listener implements MouseWheelListener {

    private final JScrollPane scrollPane;
    private boolean           inHandler;

    Listener(JScrollPane scrollPane) {
      this.scrollPane = scrollPane;
      scrollPane.setWheelScrollingEnabled(false);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
      if (!inHandler) {
        inHandler = true;
        try {
          handleMoved(e);
        }
        finally {
          inHandler = false;
        }
      }
    }

    private void handleMoved(MouseWheelEvent e) {
      JScrollPane currentPane = currentPane(e);
      if (currentPane == null || currentPane == scrollPane || e.isControlDown() || e.isAltDown()) {
        dispatchDefault(scrollPane, e);
      }
      else {
        dispatchDefault(currentPane, (MouseWheelEvent) SwingUtilities.convertMouseEvent(scrollPane, e, currentPane));
      }
    }

    private static void dispatchDefault(JScrollPane comp, MouseWheelEvent e) {
      if (comp.isWheelScrollingEnabled()) {
        comp.dispatchEvent(e);
      }
      else {
        comp.setWheelScrollingEnabled(true);
        comp.dispatchEvent(e);
        comp.setWheelScrollingEnabled(false);
      }
    }

    private JScrollPane currentPane(MouseWheelEvent e) {
      Current current = current(scrollPane);
      if (current == null) {
        return null;
      }

      long validUntil = current.validUntil;
      current.validUntil = e.getWhen() + 1000;

      if (e.getWhen() < validUntil) {
        return current.pane;
      }

      for (Component comp = scrollPane; comp != null; comp = comp.getParent()) {
        if (comp instanceof JScrollPane) {
          JScrollPane otherPane = (JScrollPane) comp;
          if (canScrollFurther(otherPane, e)) {
            current.pane = otherPane;
            return current.pane;
          }
        }
      }

      current.pane = null;
      return null;
    }

    private static boolean canScrollFurther(JScrollPane pane, MouseWheelEvent e) {
      // See BasicScrollPaneUI
      JScrollBar bar = pane.getVerticalScrollBar();
      if (bar == null || !bar.isVisible() || e.isShiftDown()) {
        bar = pane.getHorizontalScrollBar();
        if (bar == null || !bar.isVisible()) {
          return false;
        }
      }

      if (e.getWheelRotation() < 0) {
        return bar.getValue() != 0;
      }
      else {
        int limit = bar.getMaximum() - bar.getVisibleAmount();
        return bar.getValue() != limit;
      }
    }

    private static Current current(Component component) {
      if (component.getParent() == null) {
        return null;
      }

      Component top = component;
      while (top.getParent() != null) {
        top = top.getParent();
      }

      for (MouseWheelListener listener : top.getMouseWheelListeners()) {
        if (listener instanceof Current) {
          return (Current) listener;
        }
      }

      Current current = new Current();
      top.addMouseWheelListener(current);
      return current;
    }
  }

  /**
   * The "currently active scroll pane" needs to remembered once per top-level window.
   * <p>
   * Since a Component does not provide a storage for arbitrary data, this data is stored in a no-op listener.
   */
  private static class Current implements MouseWheelListener {
    private JScrollPane pane;
    private long        validUntil;

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
      // Do nothing.
    }
  }
}
