/*
 * Copyright 2012 - 2016 Manuel Laggner
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

import java.beans.PropertyChangeListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.panels.MediaFilesPanel;
import org.tinymediamanager.ui.tvshows.TvShowSelectionModel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowMediaInformationPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowMediaInformationPanel extends JPanel {
  private static final long           serialVersionUID = 1610264727610254912L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());   //$NON-NLS-1$
  private final static Logger         LOGGER           = LoggerFactory.getLogger(TvShowMediaInformationPanel.class);

  private TvShowSelectionModel        selectionModel;
  private EventList<MediaFile>        mediaFileEventList;
  private MediaFilesPanel             panelMediaFiles;

  private LinkLabel                   lblTvShowPath;
  private JLabel                      lblDateAdded;
  private JCheckBox                   cbWatched;

  public TvShowMediaInformationPanel(TvShowSelectionModel model) {
    this.selectionModel = model;
    mediaFileEventList = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(MediaFile.class));

    initComponents();
    initDataBindings();

    lblTvShowPath.addActionListener(arg0 -> {
      if (!StringUtils.isEmpty(lblTvShowPath.getNormalText())) {
        // get the location from the label
        Path path = Paths.get(lblTvShowPath.getNormalText());
        try {
          // check whether this location exists
          if (Files.exists(path)) {
            TmmUIHelper.openFile(path);
          }
        }
        catch (Exception ex) {
          LOGGER.error("open filemanager", ex);
          MessageManager.instance
              .pushMessage(new Message(MessageLevel.ERROR, path, "message.erroropenfolder", new String[] { ":", ex.getLocalizedMessage() }));
        }
      }
    });

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = propertyChangeEvent -> {
      String property = propertyChangeEvent.getPropertyName();
      Object source = propertyChangeEvent.getSource();
      // react on selection of a tv show and change of media files
      if ((source.getClass() == TvShowSelectionModel.class && "selectedTvShow".equals(property))
          || (source.getClass() == TvShowEpisode.class && "mediaFiles".equals(property))) {
        try {
          mediaFileEventList.getReadWriteLock().writeLock().lock();
          mediaFileEventList.clear();
          mediaFileEventList.addAll(selectionModel.getSelectedTvShow().getMediaFiles());
        }
        catch (Exception ignored) {
        }
        finally {
          mediaFileEventList.getReadWriteLock().writeLock().unlock();
        }
        panelMediaFiles.adjustColumns();
      }
    };

    selectionModel.addPropertyChangeListener(propertyChangeListener);
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[][80lp:n][43px][grow]", "[21px][14px][80lp,grow]"));
    {
      JLabel lblDateAddedT = new JLabel(BUNDLE.getString("metatag.dateadded")); //$NON-NLS-1$
      add(lblDateAddedT, "cell 0 0");

      lblDateAdded = new JLabel("");
      add(lblDateAdded, "cell 1 0");
    }
    {
      JLabel lblWatchedT = new JLabel(BUNDLE.getString("metatag.watched")); //$NON-NLS-1$
      add(lblWatchedT, "cell 2 0");

      cbWatched = new JCheckBox("");
      cbWatched.setEnabled(false);
      add(cbWatched, "cell 3 0");
    }
    {
      JLabel lblTvShowPathT = new JLabel(BUNDLE.getString("metatag.path")); //$NON-NLS-1$
      add(lblTvShowPathT, "cell 0 1");

      lblTvShowPath = new LinkLabel("");
      add(lblTvShowPath, "cell 1 1 3 1");
    }
    {
      panelMediaFiles = new MediaFilesPanel(mediaFileEventList);
      add(panelMediaFiles, "cell 0 2 4 1,grow");
    }
  }

  protected void initDataBindings() {
    BeanProperty<TvShowSelectionModel, Integer> tvShowSelectionModelBeanProperty = BeanProperty.create("selectedTvShow.dateAdded.date");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<TvShowSelectionModel, Integer, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty, lblDateAdded, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<TvShowSelectionModel, Boolean> tvShowSelectionModelBeanProperty_1 = BeanProperty.create("selectedTvShow.watched");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<TvShowSelectionModel, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty_1, cbWatched, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<TvShowSelectionModel, Integer> tvShowSelectionModelBeanProperty_2 = BeanProperty.create("selectedTvShow.dateAdded.day");
    AutoBinding<TvShowSelectionModel, Integer, JLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty_2, lblDateAdded, jLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_3 = BeanProperty.create("selectedTvShow.dateAddedAsString");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty_3, lblDateAdded, jLabelBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<TvShowSelectionModel, String> tvShowSelectionModelBeanProperty_13 = BeanProperty.create("selectedTvShow.path");
    AutoBinding<TvShowSelectionModel, String, JLabel, String> autoBinding_19 = Bindings.createAutoBinding(UpdateStrategy.READ, selectionModel,
        tvShowSelectionModelBeanProperty_13, lblTvShowPath, jLabelBeanProperty);
    autoBinding_19.bind();
  }
}
