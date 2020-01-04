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

package org.tinymediamanager.core.tasks;

import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaEntityExporter;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.threading.TmmThreadPool;

/**
 * the class {@link ExportTask} is used to export media entities asynchronously
 *
 * @author Manuel Laggner
 */
public class ExportTask extends TmmThreadPool {
  private static final Logger         LOGGER = LoggerFactory.getLogger(ExportTask.class);

  private MediaEntityExporter         exporter;
  private List<? extends MediaEntity> entities;
  private Path                        exportPath;

  public ExportTask(String taskName, MediaEntityExporter exporter, List<? extends MediaEntity> entities, Path exportPath) {
    super(taskName);
    this.exporter = exporter;
    this.entities = entities;
    this.exportPath = exportPath;
  }

  @Override
  protected void doInBackground() {
    start();
    try {
      exporter.export(entities, exportPath);
    }
    catch (Exception e) {
      LOGGER.error("could not export template: {}", e.getMessage());
    }
  }

  @Override
  public void cancel() {
    // cancel the export too
    exporter.cancel();
    super.cancel();
  }

  @Override
  public void callback(Object obj) {
    // we cannot do any progress here since there is no response of progress from jmte
  }
}
