package org.tinymediamanager.testing;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskHandle.TaskType;
import org.tinymediamanager.core.threading.TmmTaskManager;

/**
 * adds some Fake tasks for testing UI
 * 
 * @author Myron Boyle
 *
 */
public class FakeTmmTaskAction extends AbstractAction {
  private static final long   serialVersionUID = 1L;
  private static final Logger LOGGER           = LoggerFactory.getLogger(FakeTmmTaskAction.class);
  private int                 amount           = 0;
  private int                 workUnits        = 0;
  private String              type             = "";

  /**
   * adds "amount" number of tasks into queue, each with x workunits
   * 
   * @param amount
   * @param workUnits
   */
  public FakeTmmTaskAction(String type, int amount, int workUnits) {
    this.amount = amount;
    this.workUnits = workUnits;
    this.type = type;
    putValue(NAME, "TASK: add " + amount + " " + type + " task (" + workUnits + " workUnits)"); //$NON-NLS-1$
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    for (int i = 1; i <= amount; i++) {
      TmmTask task = new FakeTmmTask(type + i, workUnits, TaskType.MAIN_TASK);
      switch (type) {
        case "download":
          TmmTaskManager.getInstance().addDownloadTask(task);
          break;

        case "image":
          TmmTaskManager.getInstance().addImageDownloadTask(task);
          break;

        case "main":
          // TmmTaskManager.getInstance().addMainTask(task);
          break;

        default:
          TmmTaskManager.getInstance().addUnnamedTask(task);
          break;
      }
      LOGGER.info("added " + type + " task " + i);
    }
  }

}
