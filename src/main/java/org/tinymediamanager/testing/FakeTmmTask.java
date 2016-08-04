package org.tinymediamanager.testing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.threading.TmmTask;

/**
 * Fake Task, to test GUI
 * 
 * @author Myron Boyle
 *
 */
public class FakeTmmTask extends TmmTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(FakeTmmTask.class);

  protected FakeTmmTask(String taskName, int workUnits, TaskType type) {
    super(taskName, workUnits, type);
  }

  @Override
  protected void doInBackground() {
    for (int i = 1; i <= workUnits; i++) {
      LOGGER.info(taskName + ": " + i + " of " + workUnits);
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {
        LOGGER.error("Thread interrupted!");
      }
      publishState(i);
      if (cancel) {
        LOGGER.info(taskName + " cancelled!");
        break;
      }
    }
  }

}
