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
package org.tinymediamanager.ui.movies.panels;

import static org.tinymediamanager.core.Constants.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.movies.MovieSelectionModel;
import org.tinymediamanager.ui.panels.ImagePanel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class MovieArtworkPanel. To display all artwork from a movie in the UI
 * 
 * @author Manuel Laggner
 */
public class MovieArtworkPanel extends JPanel {
  private static final long serialVersionUID = -7478111154774646873L;

  public MovieArtworkPanel(final MovieSelectionModel selectionModel) {
    setLayout(
        new FormLayout(new ColumnSpec[] { FormSpecs.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.UNRELATED_GAP_COLSPEC, },
            new RowSpec[] { FormSpecs.PARAGRAPH_GAP_ROWSPEC, RowSpec.decode("fill:50dlu:grow"), FormSpecs.PARAGRAPH_GAP_ROWSPEC, }));
    final List<MediaFile> mediaFiles = new ArrayList<MediaFile>();
    final ImagePanel imagePanel = new ImagePanel(mediaFiles);

    imagePanel.setMaxWidth(400);
    imagePanel.setMaxHeight(200);
    add(imagePanel, "2, 2");

    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String property = propertyChangeEvent.getPropertyName();
        Object source = propertyChangeEvent.getSource();
        if (source instanceof MovieSelectionModel || (source instanceof Movie && MEDIA_FILES.equals(property))) {
          synchronized (mediaFiles) {
            mediaFiles.clear();
            for (MediaFile mediafile : new ArrayList<MediaFile>(selectionModel.getSelectedMovie().getMediaFiles())) {
              if (mediafile.isGraphic()) {
                mediaFiles.add(mediafile);
              }
            }
            imagePanel.rebuildPanel();
          }
        }
      }
    };
    selectionModel.addPropertyChangeListener(propertyChangeListener);
  }
}
