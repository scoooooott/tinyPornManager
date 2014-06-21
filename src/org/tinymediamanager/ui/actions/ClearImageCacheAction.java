/*
 * Copyright 2012 - 2014 Manuel Laggner
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
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The ClearImageCacheAction to clear all cached images
 * 
 * @author Manuel Laggner
 */
public class ClearImageCacheAction extends AbstractAction {
  private static final long           serialVersionUID = -4615019451671427233L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private final static Logger         LOGGER           = LoggerFactory.getLogger(ClearImageCacheAction.class);

  public ClearImageCacheAction() {
    putValue(NAME, BUNDLE.getString("tmm.clearimagecache")); //$NON-NLS-1$
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tmm.clearimagecache")); //$NON-NLS-1$
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    File cache = new File(ImageCache.CACHE_DIR);
    if (cache.exists()) {
      try {
        FileUtils.deleteDirectory(cache);
      }
      catch (Exception e) {
        LOGGER.warn(e.getMessage());
      }
    }
  }
}
