/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.ui.movies;

import static org.tinymediamanager.core.Constants.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.LinkLabel;
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
 * The Class MovieMediaInformationPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieMediaFilesPanel extends JPanel {
  private static final long           serialVersionUID = 3181909355114738346L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private final static Logger         LOGGER           = LoggerFactory.getLogger(MovieMediaFilesPanel.class);

  private MovieSelectionModel         movieSelectionModel;

  private JLabel                      lblFilesT;
  private LinkLabel                   lblMoviePath;
  private JLabel                      lblDateAddedT;
  private JLabel                      lblDateAdded;
  private JLabel                      lblMoviePathT;
  // private JButton btnPlay;

  /** The media file event list. */
  private EventList<MediaFile>        mediaFileEventList;
  private MediaFilesPanel             panelMediaFiles;

  /**
   * Instantiates a new movie media information panel.
   * 
   * @param model
   *          the model
   */
  public MovieMediaFilesPanel(MovieSelectionModel model) {
    this.movieSelectionModel = model;
    mediaFileEventList = new ObservableElementList<MediaFile>(GlazedLists.threadSafeList(new BasicEventList<MediaFile>()),
        GlazedLists.beanConnector(MediaFile.class));

    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("200px:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
        RowSpec.decode("default:grow"), FormFactory.NARROW_LINE_GAP_ROWSPEC, }));

    lblDateAddedT = new JLabel(BUNDLE.getString("metatag.dateadded")); //$NON-NLS-1$
    add(lblDateAddedT, "2, 2");

    lblDateAdded = new JLabel("");
    add(lblDateAdded, "4, 2");

    // btnPlay = new JButton("Play");
    // btnPlay.addActionListener(new ActionListener() {
    // public void actionPerformed(ActionEvent arg0) {
    // try {
    // Desktop.getDesktop().open(movieSelectionModel.getSelectedMovie().getMediaFiles(MediaFileType.VIDEO).get(0).getFile());
    // }
    // catch (Exception e) {
    //
    // }
    // }
    // if (!StringUtils.isEmpty(lblMoviePath.getNormalText())) {
    // // get the location from the label
    // StringBuilder movieFile = new
    // StringBuilder(lblMoviePath.getNormalText());
    // movieFile.append(File.separator);
    // movieFile.append(movieSelectionModel.getSelectedMovie().getMediaFiles().get(0).getFilename());
    // File f = new File(movieFile.toString());
    //
    // try {
    // if (f.exists()) {
    //
    // String vlcF = f.getAbsolutePath();
    // // F I X M E: german umlauts do not decode correctly; Bug in
    // // libDvdNav? so workaround;
    // if (vlcF.matches(".*[äöüÄÖÜ].*")) {
    // LOGGER.debug("VLC: workaround: german umlauts found - use system player");
    // Desktop.getDesktop().open(f);
    // }
    // else {
    // try {
    //
    // if (!vlcF.startsWith("/")) {
    // // add the missing 3rd / if not start with one (eg windows)
    // vlcF = "/" + vlcF;
    // }
    // String mrl = new FileMrl().file(vlcF).value();
    //
    // final EmbeddedMediaPlayerComponent mediaPlayerComponent = new
    // EmbeddedMediaPlayerComponent();
    // JFrame frame = new JFrame("player");
    // frame.setLocation(100, 100);
    // frame.setSize(1050, 600);
    // frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
    // frame.setVisible(true);
    // frame.setContentPane(mediaPlayerComponent);
    // // mrl = mrl.replace("file://", ""); // does not work either
    //
    // LOGGER.debug("VLC: playing " + mrl);
    // Boolean ok = mediaPlayerComponent.getMediaPlayer().playMedia(mrl);
    // if (!ok) {
    // LOGGER.error("VLC: couldn't create player window!");
    // }
    // }
    // catch (RuntimeException e) {
    // LOGGER.warn("VLC: has not been initialized on startup - use system player");
    // Desktop.getDesktop().open(f);
    // }
    // catch (NoClassDefFoundError e) {
    // LOGGER.warn("VLC: has not been initialized on startup - use system player");
    // Desktop.getDesktop().open(f);
    // }
    //
    // } // end else
    // } // end exists
    // } // end try
    // catch (IOException e) {
    // LOGGER.error("Error opening file", e);
    // }
    // } // end isEmpty
    // }
    // });
    // add(btnPlay, "10, 2");

    lblMoviePathT = new JLabel(BUNDLE.getString("metatag.path")); //$NON-NLS-1$
    add(lblMoviePathT, "2, 4");

    lblMoviePath = new LinkLabel("");
    lblMoviePath.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (!StringUtils.isEmpty(lblMoviePath.getNormalText())) {
          File path = new File(lblMoviePath.getNormalText());
          try {
            // get the location from the label
            // check whether this location exists
            if (path.exists()) {
              TmmUIHelper.openFile(path);
            }
          }
          catch (Exception ex) {
            LOGGER.error("open filemanager", ex);
            MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, path, "message.erroropenfolder", new String[] { ":",
                ex.getLocalizedMessage() }));
          }
        }
      }
    });
    lblMoviePathT.setLabelFor(lblMoviePath);
    lblMoviePathT.setLabelFor(lblMoviePath);
    add(lblMoviePath, "4, 4");

    lblFilesT = new JLabel(BUNDLE.getString("metatag.files")); //$NON-NLS-1$
    add(lblFilesT, "2, 6, default, top");

    panelMediaFiles = new MediaFilesPanel(mediaFileEventList);
    add(panelMediaFiles, "4, 6, 1, 1, fill, fill");

    initDataBindings();

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String property = propertyChangeEvent.getPropertyName();
        Object source = propertyChangeEvent.getSource();
        // react on selection of a movie and change of media files
        if ((source.getClass() == MovieSelectionModel.class && "selectedMovie".equals(property))
            || (source.getClass() == Movie.class && MEDIA_FILES.equals(property))) {
          // this does sometimes not work. simply wrap it
          try {
            mediaFileEventList.getReadWriteLock().writeLock().lock();
            mediaFileEventList.clear();
            mediaFileEventList.addAll(movieSelectionModel.getSelectedMovie().getMediaFiles());
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

    movieSelectionModel.addPropertyChangeListener(propertyChangeListener);
  }

  protected void initDataBindings() {
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty = BeanProperty.create("selectedMovie.dateAdded.date");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, Integer, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty, lblDateAdded, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty_2 = BeanProperty.create("selectedMovie.dateAdded.day");
    AutoBinding<MovieSelectionModel, Integer, JLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_2, lblDateAdded, jLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_3 = BeanProperty.create("selectedMovie.dateAddedAsString");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_3, lblDateAdded, jLabelBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_13 = BeanProperty.create("selectedMovie.path");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_19 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_13, lblMoviePath, jLabelBeanProperty);
    autoBinding_19.bind();
  }
}
