package org.tinymediamanager.ui.components;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.threading.TmmTaskHandle;
import org.tinymediamanager.core.threading.TmmTaskHandle.TaskState;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;

public class TaskListComponent extends JPanel {
  private static final long           serialVersionUID  = -6088880093610800005L;
  private static final int            UPPERMARGIN       = 3;
  private static final int            LEFTMARGIN        = 2;
  private static final int            BOTTOMMARGIN      = 2;
  private static final int            BETWEENTEXTMARGIN = 3;
  private static final ResourceBundle BUNDLE            = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  static final int                    ITEM_WIDTH        = 400;

  private TmmTaskHandle               taskHandle;

  private JLabel                      mainLabel;
  private JLabel                      dynaLabel;
  private JProgressBar                bar;
  private JButton                     closeButton;

  private int                         mainHeight;
  private int                         dynaHeight;
  private int                         buttonWidth;

  public TaskListComponent(TmmTaskHandle handle) {
    setFocusable(true);
    setRequestFocusEnabled(true);
    setLayout(new CustomLayout());
    setBorder(BorderFactory.createEmptyBorder());

    this.taskHandle = handle;

    mainLabel = new JLabel();
    dynaLabel = new JLabel();

    bar = new JProgressBar();

    closeButton = new JButton(new CancelAction());
    closeButton.setBorderPainted(false);
    closeButton.setBorder(BorderFactory.createEmptyBorder());
    closeButton.setOpaque(false);
    closeButton.setContentAreaFilled(false);
    closeButton.setFocusable(false);

    // start figure out height
    mainLabel.setText("XYZ");
    dynaLabel.setText("XYZ");
    mainHeight = Math.max(mainLabel.getPreferredSize().height, closeButton.getPreferredSize().height);
    dynaHeight = dynaLabel.getPreferredSize().height;
    buttonWidth = closeButton.getPreferredSize().width;
    mainLabel.setText(null);
    dynaLabel.setText(null);
    // end figure out height

    add(mainLabel);
    add(bar);
    add(closeButton);
    add(dynaLabel);

    updateTaskInformation();
  }

  public void updateTaskInformation() {
    mainLabel.setText(taskHandle.getTaskName());

    switch (taskHandle.getState()) {
      case CREATED:
      case STARTED:
        if (StringUtils.isNotBlank(taskHandle.getTaskDescription())) {
          dynaLabel.setText(taskHandle.getTaskDescription());
        }
        else {
          dynaLabel.setText(BUNDLE.getString("task.running"));
        }
        break;

      case QUEUED:
        dynaLabel.setText(BUNDLE.getString("task.queued"));
        break;

      case CANCELLED:
        dynaLabel.setText(BUNDLE.getString("task.cancelled"));
        break;

      case FINISHED:
        dynaLabel.setText(BUNDLE.getString("task.finished"));
        break;
    }

    if (taskHandle.getWorkUnits() > 0) {
      bar.setValue(taskHandle.getProgressDone());
      bar.setMaximum(taskHandle.getWorkUnits());
      bar.setIndeterminate(false);
    }
    else if (taskHandle.getState() == TaskState.QUEUED) {
      bar.setIndeterminate(false);
      bar.setValue(0);
      bar.setMaximum(1);
    }
    else {
      bar.setIndeterminate(true);
    }
  }

  TmmTaskHandle getHandle() {
    return taskHandle;
  }

  /**************************************************************************
   * helper classes
   **************************************************************************/
  private class CustomLayout implements LayoutManager {

    @Override
    public void addLayoutComponent(String name, java.awt.Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(java.awt.Container parent) {
      int height = UPPERMARGIN + mainHeight + BETWEENTEXTMARGIN + dynaHeight + BOTTOMMARGIN;
      return new Dimension(ITEM_WIDTH, height);
    }

    @Override
    public void layoutContainer(java.awt.Container parent) {
      int parentWidth = parent.getWidth();
      int offset = parentWidth - buttonWidth - LEFTMARGIN;
      if (closeButton != null) {
        closeButton.setBounds(offset, UPPERMARGIN, buttonWidth, mainHeight);
      }

      // have the bar approx 30 percent of the width
      int barOffset = offset - (ITEM_WIDTH / 3);
      bar.setBounds(barOffset, UPPERMARGIN, offset - barOffset - LEFTMARGIN, mainHeight);
      mainLabel.setBounds(LEFTMARGIN, UPPERMARGIN, barOffset - LEFTMARGIN, mainHeight);
      dynaLabel.setBounds(LEFTMARGIN, mainHeight + UPPERMARGIN + BETWEENTEXTMARGIN, parentWidth - LEFTMARGIN, dynaHeight);
    }

    @Override
    public Dimension minimumLayoutSize(java.awt.Container parent) {
      return preferredLayoutSize(parent);
    }

    @Override
    public void removeLayoutComponent(java.awt.Component comp) {
    }
  }

  private class CancelAction extends AbstractAction {
    private static final long serialVersionUID = -2634569716059018131L;

    public CancelAction() {
      putValue(SMALL_ICON, IconManager.PROCESS_STOP);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
      taskHandle.cancel();
    }
  }
}
