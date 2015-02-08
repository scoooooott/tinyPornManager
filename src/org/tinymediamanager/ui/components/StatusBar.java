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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskHandle;
import org.tinymediamanager.core.threading.TmmTaskHandle.TaskState;
import org.tinymediamanager.core.threading.TmmTaskHandle.TaskType;
import org.tinymediamanager.core.threading.TmmTaskListener;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.ui.IconManager;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class StatusBar. Show active tasks in a status bar
 * 
 * @author Manuel Laggner
 */
public class StatusBar extends JPanel implements TmmTaskListener {
  private static final long                     serialVersionUID = -6375900257553323558L;

  private Map<TmmTaskHandle, TaskListComponent> taskMap;
  private TmmTaskHandle                         activeTask;

  private JProgressBar                          bar;
  private JLabel                                label;
  private JButton                               closeButton;
  // private JWindow popupWindow;
  private JPopupMenu                            popup;
  private PopupPane                             pane;

  private final int                             preferredHeight;

  public StatusBar() {
    taskMap = new HashMap<TmmTaskHandle, TaskListComponent>();
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.DEFAULT_COLSPEC,
        FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.LABEL_COMPONENT_GAP_COLSPEC, }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC }));

    label = new JLabel();
    bar = new JProgressBar();
    bar.addMouseListener(new MListener());
    closeButton = new JButton(IconManager.PROCESS_STOP);
    closeButton.setBorderPainted(false);
    closeButton.setBorder(BorderFactory.createEmptyBorder());
    closeButton.setOpaque(false);
    closeButton.setContentAreaFilled(false);
    closeButton.setAction(new CancelAction());

    // start figure out height
    label.setText("XYZ");
    bar.setString("XYZ");
    preferredHeight = Math.max(Math.max(label.getPreferredSize().height, bar.getPreferredSize().height), closeButton.getPreferredSize().height) + 2;
    bar.setString(null);
    label.setText(null);
    // end figure out height

    pane = new PopupPane();
    pane.getActionMap().put("HidePopup", new AbstractAction() {
      private static final long serialVersionUID = -5945688032617664909L;

      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        // escape pressed - hiding;
        hidePopup();
      }
    });
    pane.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "HidePopup");
    pane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "HidePopup");

    add(label, "3, 1, default, default");
    add(bar, "5, 1");
    add(closeButton, "7, 1");

    label.setVisible(false);
    bar.setVisible(false);
    closeButton.setVisible(false);

    popup = new JPopupMenu();
    popup.setInvoker(bar);
    popup.add(pane);

    TmmTaskManager.getInstance().addTaskListener(this);
  }

  public void showPopup() {
    if (GraphicsEnvironment.isHeadless()) {
      return;
    }
    if (taskMap.size() == 0) {
      // just in case..
      return;
    }

    resizePopup();
    // popupWindow.setVisible(true);
    popup.setVisible(true);
    // pane.requestFocus();
  }

  public void hidePopup() {
    if (GraphicsEnvironment.isHeadless()) {
      return;
    }
    if (popup != null) {
      popup.setVisible(false);
    }
  }

  private void resizePopup() {
    pane.invalidate();
    popup.pack();
    Point point = new Point(0, 0);
    SwingUtilities.convertPointToScreen(point, this);
    Dimension dim = popup.getPreferredSize();
    Rectangle usableRect = getUsableScreenBounds();
    Point loc = new Point(point.x + this.getSize().width - dim.width - 5 * 2, point.y - dim.height - 5);
    // -5 in x coordinate is becuase of the hgap between the separator and button and separator and edge
    if (!usableRect.contains(loc)) {
      loc = new Point(loc.x, point.y + 5 + this.getSize().height);
    }
    // +4 here because of the width of the close button in popup, we
    // want the progress bars to align visually.. but there's separator in status now..
    popup.setLocation(loc);
  }

  private Rectangle getUsableScreenBounds() {
    GraphicsConfiguration gconf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    Rectangle bounds = new Rectangle(gconf.getBounds());

    try {
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      Insets insets = toolkit.getScreenInsets(gconf);
      bounds.y += insets.top;
      bounds.x += insets.left;
      bounds.height -= (insets.top + insets.bottom);
      bounds.width -= (insets.left + insets.right);
    }
    catch (Exception ex) {
    }
    return bounds;
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension retValue;
    retValue = super.getPreferredSize();
    retValue.height = preferredHeight;
    return retValue;
  }

  @Override
  public Dimension getMinimumSize() {
    Dimension retValue;
    retValue = super.getMinimumSize();
    retValue.height = preferredHeight;
    return retValue;
  }

  @Override
  public Dimension getMaximumSize() {
    Dimension retValue;
    retValue = super.getMaximumSize();
    retValue.height = preferredHeight;
    return retValue;
  }

  @Override
  public synchronized void processTaskEvent(final TmmTaskHandle task) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {

        if (task.getState() == TaskState.CREATED || task.getState() == TaskState.QUEUED) {
          createListItem(task);
        }
        else if (task.getState() == TaskState.STARTED) {
          TaskListComponent comp = taskMap.get(task);
          if (comp == null) {
            createListItem(task);
            comp = taskMap.get(task);
          }
          comp.updateTaskInformation();
        }
        else if (task.getState() == TaskState.CANCELLED || task.getState() == TaskState.FINISHED) {
          removeListItem(task);
        }

        // search for a new activetask to be displayed in the statusbar
        if (activeTask == null || activeTask.getState() == TaskState.FINISHED || activeTask.getState() == TaskState.CANCELLED) {
          activeTask = null;
          for (Entry<TmmTaskHandle, TaskListComponent> entry : taskMap.entrySet()) {
            TmmTaskHandle handle = entry.getKey();
            if (handle.getType() == TaskType.MAIN_TASK && handle.getState() == TaskState.STARTED) {
              activeTask = handle;
              break;
            }
          }

          // no active main task found; if there are any BG tasks, display a dummy char to indicate something is working
          if (activeTask == null) {
            for (Entry<TmmTaskHandle, TaskListComponent> entry : taskMap.entrySet()) {
              TmmTaskHandle handle = entry.getKey();
              if (handle.getState() == TaskState.STARTED) {
                activeTask = handle;
                break;
              }
            }
          }
        }

        // hide components if there is nothing to be displayed
        if (activeTask == null) {
          label.setVisible(false);
          closeButton.setVisible(false);
          bar.setVisible(false);
        }
        else {
          // ensure everything is visible
          label.setVisible(true);
          bar.setVisible(true);
          if (activeTask.getType() == TaskType.MAIN_TASK) {
            closeButton.setVisible(true);
          }
          else {
            closeButton.setVisible(false);
          }

          // and update content
          label.setText(activeTask.getTaskName());
          if (activeTask.getWorkUnits() > 0) {
            bar.setIndeterminate(false);
            bar.setMaximum(activeTask.getWorkUnits());
            bar.setValue(activeTask.getProgressDone());
          }
          else {
            bar.setIndeterminate(true);
          }
        }
      }
    });
  }

  private void createListItem(TmmTaskHandle handle) {
    TaskListComponent comp;
    if (taskMap.containsKey(handle)) {
      // happens when we click to display on popup and there is a
      // new handle waiting in the queue.
      comp = taskMap.get(handle);
    }
    else {
      comp = new TaskListComponent(handle);
      taskMap.put(handle, comp);
    }
    pane.addListComponent(comp);

    if (popup.isShowing()) {
      resizePopup();
    }
  }

  private void removeListItem(TmmTaskHandle handle) {
    taskMap.remove(handle);
    pane.removeListComponent(handle);

    if (popup.isShowing() && taskMap.isEmpty()) {
      hidePopup();
    }
    else if (popup.isShowing()) {
      resizePopup();
    }
  }

  /****************************************************************************************
   * helper classes
   ****************************************************************************************/
  private class MListener extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
      if (popup.isShowing()) {
        hidePopup();
      }
      else {
        showPopup();
      }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }

  private class CancelAction extends AbstractAction {
    private static final long serialVersionUID = -3021931797641209077L;

    public CancelAction() {
      putValue(SMALL_ICON, IconManager.PROCESS_STOP);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
      if (activeTask instanceof TmmTask) {
        activeTask.cancel();
      }
    }
  }
}
