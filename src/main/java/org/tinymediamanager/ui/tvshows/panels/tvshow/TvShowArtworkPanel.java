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
package org.tinymediamanager.ui.tvshows.panels.tvshow;

import static org.tinymediamanager.core.Constants.MEDIA_FILES;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.ui.panels.ImagePanel;
import org.tinymediamanager.ui.tvshows.TvShowSelectionModel;

import net.miginfocom.swing.MigLayout;

/**
 * The class TvShowArtworkPanel. To display all artwork from a TV show in the UI
 * 
 * @author Manuel Laggner
 */
public class TvShowArtworkPanel extends JPanel {
  private static final long     serialVersionUID = -8105505340634141604L;

  private final List<MediaFile> mediaFiles;
  private ImagePanel            imagePanel;

  public TvShowArtworkPanel(final TvShowSelectionModel selectionModel) {
    mediaFiles = new ArrayList<>();

    initComponents();

    PropertyChangeListener propertyChangeListener = propertyChangeEvent -> {
      String property = propertyChangeEvent.getPropertyName();
      Object source = propertyChangeEvent.getSource();

      if (source.getClass() != TvShowSelectionModel.class) {
        return;
      }
      if ("selectedTvShow".equals(property) || MEDIA_FILES.equals(property)) {
        synchronized (mediaFiles) {
          mediaFiles.clear();
          for (MediaFile mediafile : new ArrayList<>(selectionModel.getSelectedTvShow().getMediaFiles())) {
            if (mediafile.isGraphic()) {
              mediaFiles.add(mediafile);
            }
          }
          imagePanel.rebuildPanel();
        }
      }
    };
    selectionModel.addPropertyChangeListener(propertyChangeListener);
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[grow]", "[grow]"));
    {
      imagePanel = new ImagePanel(mediaFiles);

      imagePanel.setMaxWidth(500);
      imagePanel.setMaxHeight(200);
      add(imagePanel, "cell 0 0,grow");
    }
  }
}
