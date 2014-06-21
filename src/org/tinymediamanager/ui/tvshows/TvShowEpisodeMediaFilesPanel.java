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
package org.tinymediamanager.ui.tvshows;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.components.MediaFilesPanel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowEpisodeMediaInformationPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeMediaFilesPanel extends JPanel {
  private static final long           serialVersionUID = 6409916197348303643L;
  private MediaFilesPanel             panelMediaFiles;

  private EventList<MediaFile>        mediaFileEventList;
  private TvShowEpisodeSelectionModel selectionModel;

  /**
   * Instantiates a new tv show media information panel.
   * 
   * @param model
   *          the model
   */
  public TvShowEpisodeMediaFilesPanel(TvShowEpisodeSelectionModel model) {
    this.selectionModel = model;
    mediaFileEventList = new ObservableElementList<MediaFile>(GlazedLists.threadSafeList(new BasicEventList<MediaFile>()),
        GlazedLists.beanConnector(MediaFile.class));

    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.NARROW_LINE_GAP_ROWSPEC, }));

    panelMediaFiles = new MediaFilesPanel(mediaFileEventList);
    add(panelMediaFiles, "2, 2, 9, 1, fill, fill");

    initDataBindings();

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String property = propertyChangeEvent.getPropertyName();
        Object source = propertyChangeEvent.getSource();
        // react on selection of a movie and change of media files
        if ((source.getClass() == TvShowEpisodeSelectionModel.class && "selectedTvShowEpisode".equals(property))
            || (source.getClass() == TvShowEpisode.class && "mediaFiles".equals(property))) {
          try {
            mediaFileEventList.getReadWriteLock().writeLock().lock();
            mediaFileEventList.clear();
            mediaFileEventList.addAll(selectionModel.getSelectedTvShowEpisode().getMediaFiles());
          }
          catch (Exception e) {
          }
          finally {
            mediaFileEventList.getReadWriteLock().writeLock().unlock();
          }
          try {
            panelMediaFiles.adjustColumns();
          }
          catch (Exception e) {
          }
        }
      }
    };

    selectionModel.addPropertyChangeListener(propertyChangeListener);
  }

  protected void initDataBindings() {
  }
}
