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
package org.tinymediamanager.ui.actions;

import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.TmmOsUtils;
import org.tinymediamanager.core.UTF8Control;

/**
 * The {@link CreateDesktopFileAction} is used the create a .desktop file in ~/.local/share/applications on linux
 * 
 * @author Manuel Laggner
 */
public class CreateDesktopFileAction extends TmmAction {
  private static final long           serialVersionUID = 1668251251156765161L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());
  private static final Logger         LOGGER           = LoggerFactory.getLogger(CreateDesktopFileAction.class);

  public CreateDesktopFileAction() {
    putValue(NAME, BUNDLE.getString("tmm.createdesktopentry"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tmm.createdesktopentry.hint"));
  }

  @Override
  protected void processAction(ActionEvent e) {
    String currentUsersHomeDir = System.getProperty("user.home");
    if (StringUtils.isNotBlank(currentUsersHomeDir)) {
      // build the path to the
      Path desktopFile = Paths.get(currentUsersHomeDir, ".local", "share", "applications", "tinyMediaManager2.desktop");
      if (Files.isWritable(desktopFile.getParent())) {
        TmmOsUtils.createDesktopFileForLinux(desktopFile.toFile());
      }
    }
  }
}
