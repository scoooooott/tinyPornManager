/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import static org.tinymediamanager.core.Constants.MEDIA_FILES;
import static org.tinymediamanager.core.Constants.MEDIA_INFORMATION;
import static org.tinymediamanager.core.Constants.MEDIA_SOURCE;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.movies.MovieSelectionModel;
import org.tinymediamanager.ui.panels.MediaFilesPanel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import net.miginfocom.swing.MigLayout;

/**
 * @author Manuel Laggner
 * 
 */
public class MovieMediaInformationPanel extends JPanel {
  private static final long           serialVersionUID = 2513029074142934502L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());  //$NON-NLS-1$
  private final static Logger         LOGGER           = LoggerFactory.getLogger(MovieMediaInformationPanel.class);

  private MovieSelectionModel         movieSelectionModel;
  private EventList<MediaFile>        mediaFileEventList;

  private JLabel                      lblRuntime;
  private JCheckBox                   chckbxWatched;
  private JLabel                      lblVideoCodec;
  private JLabel                      lblVideoResolution;
  private JLabel                      lblVideoBitrate;
  private JLabel                      lblVideoBitDepth;
  private JPanel                      panelAudioStreamT;
  private JPanel                      panelAudioStreamDetails;
  private JPanel                      panelSubtitle;
  private JLabel                      lblSource;
  private LinkLabel                   lblMoviePath;
  private JLabel                      lblDateAdded;
  private MediaFilesPanel             panelMediaFiles;

  /**
   * Instantiates a new movie media information panel.
   * 
   * @param model
   *          the model
   */
  public MovieMediaInformationPanel(MovieSelectionModel model) {
    this.movieSelectionModel = model;
    mediaFileEventList = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(MediaFile.class));

    initComponents();
    initDataBindings();

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = propertyChangeEvent -> {
      String property = propertyChangeEvent.getPropertyName();
      Object source = propertyChangeEvent.getSource();
      // react on selection of a movie and change of media files
      if ((source.getClass() == MovieSelectionModel.class && "selectedMovie".equals(property)) || MEDIA_INFORMATION.equals(property)
          || MEDIA_SOURCE.equals(property)) {
        fillVideoStreamDetails();
        buildAudioStreamDetails();
        buildSubtitleStreamDetails();
      }
      if ((source.getClass() == MovieSelectionModel.class && "selectedMovie".equals(property))
          || (source.getClass() == Movie.class && MEDIA_FILES.equals(property))) {
        // this does sometimes not work. simply wrap it
        try {
          mediaFileEventList.getReadWriteLock().writeLock().lock();
          mediaFileEventList.clear();
          mediaFileEventList.addAll(movieSelectionModel.getSelectedMovie().getMediaFiles());
        }
        catch (Exception ignored) {
        }
        finally {
          mediaFileEventList.getReadWriteLock().writeLock().unlock();
        }
        panelMediaFiles.adjustColumns();
      }
    };

    movieSelectionModel.addPropertyChangeListener(propertyChangeListener);
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[][][][grow]", "[][][][][::100lp][shrink 0][][][80lp,grow]"));
    {
      JLabel lblRuntimeT = new JLabel(BUNDLE.getString("metatag.runtime")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblRuntimeT, Font.BOLD);
      add(lblRuntimeT, "cell 0 0");

      lblRuntime = new JLabel("");
      add(lblRuntime, "cell 1 0");
    }
    {
      JLabel lblWatchedT = new JLabel(BUNDLE.getString("metatag.watched")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblWatchedT, Font.BOLD);
      add(lblWatchedT, "cell 2 0");

      chckbxWatched = new JCheckBox("");
      chckbxWatched.setEnabled(false);
      add(chckbxWatched, "cell 3 0");
    }
    {
      JLabel lblSourceT = new JLabel(BUNDLE.getString("metatag.source")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblSourceT, Font.BOLD);
      add(lblSourceT, "cell 0 1");

      lblSource = new JLabel("");
      add(lblSource, "cell 1 1 2 1");
    }
    {
      JLabel lblVideoT = new JLabel(BUNDLE.getString("metatag.video")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblVideoT, Font.BOLD);
      add(lblVideoT, "cell 0 2");

      JLabel lblMovieT = new JLabel(BUNDLE.getString("metatag.movie")); //$NON-NLS-1$
      add(lblMovieT, "cell 1 2");

      JPanel panelVideoStreamDetails = new JPanel();
      panelVideoStreamDetails.setLayout(new GridLayout(1, 4, 0, 25));
      add(panelVideoStreamDetails, "cell 2 2 2 1,growx");

      lblVideoCodec = new JLabel("");
      panelVideoStreamDetails.add(lblVideoCodec);

      lblVideoResolution = new JLabel("");
      panelVideoStreamDetails.add(lblVideoResolution);

      lblVideoBitrate = new JLabel("");
      panelVideoStreamDetails.add(lblVideoBitrate);

      lblVideoBitDepth = new JLabel("");
      panelVideoStreamDetails.add(lblVideoBitDepth);

      // to create the same spacing as in audio
      panelVideoStreamDetails.add(new JLabel(""));
    }
    {
      JLabel lblAudioT = new JLabel(BUNDLE.getString("metatag.audio")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblAudioT, Font.BOLD);
      add(lblAudioT, "cell 0 3");

      panelAudioStreamT = new JPanel();
      panelAudioStreamT.setLayout(new GridLayout(0, 1));
      add(panelAudioStreamT, "cell 1 3");

      panelAudioStreamDetails = new JPanel();
      panelAudioStreamDetails.setLayout(new GridLayout(0, 4));
      add(panelAudioStreamDetails, "cell 2 3 2 1,growx");
    }
    {
      JLabel lblSubtitle = new JLabel(BUNDLE.getString("metatag.subtitles")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblSubtitle, Font.BOLD);
      add(lblSubtitle, "cell 0 4,aligny top");

      JScrollPane scrollPane = new JScrollPane();
      scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      add(scrollPane, "cell 1 4 3 1,growy");

      panelSubtitle = new JPanel();
      scrollPane.setViewportView(panelSubtitle);
      panelSubtitle.setLayout(new GridBagLayout());
    }
    {
      add(new JSeparator(), "cell 0 5 4 1,growx");
    }
    {
      JLabel lblDateAddedT = new JLabel(BUNDLE.getString("metatag.dateadded")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblDateAddedT, Font.BOLD);
      add(lblDateAddedT, "cell 0 6");

      lblDateAdded = new JLabel("");
      add(lblDateAdded, "cell 1 6 3 1");
    }
    {
      JLabel lblMoviePathT = new JLabel(BUNDLE.getString("metatag.path")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblMoviePathT, Font.BOLD);
      add(lblMoviePathT, "cell 0 7");

      lblMoviePath = new LinkLabel("");
      lblMoviePath.addActionListener(new LinkLabelListener());
      lblMoviePathT.setLabelFor(lblMoviePath);
      add(lblMoviePath, "cell 1 7 3 1");
    }
    {
      panelMediaFiles = new MediaFilesPanel(mediaFileEventList) {
        private static final long serialVersionUID = -8712860341504013403L;

        @Override
        public MediaEntity getMediaEntity() {
          return movieSelectionModel.getSelectedMovie();
        }
      };
      add(panelMediaFiles, "cell 0 8 4 1,grow");
    }
  }

  private void fillVideoStreamDetails() {
    List<MediaFile> mediaFiles = movieSelectionModel.getSelectedMovie().getMediaFiles(MediaFileType.VIDEO);

    if (mediaFiles.size() == 0) {
      return;
    }

    MediaFile mediaFile = mediaFiles.get(0);

    int runtime = 0;
    for (MediaFile mf : mediaFiles) {
      runtime += mf.getDuration();
    }

    if (runtime == 0) {
      lblRuntime.setText("");
    }
    else {
      int minutes = (int) (runtime / 60) % 60;
      int hours = (int) (runtime / (60 * 60)) % 24;
      lblRuntime.setText(hours + "h " + String.format("%02d", minutes) + "m");
    }

    chckbxWatched.setSelected(movieSelectionModel.getSelectedMovie().isWatched());

    lblVideoCodec.setText(mediaFile.getVideoCodec());
    lblVideoResolution.setText(mediaFile.getVideoResolution());
    lblVideoBitrate.setText(mediaFile.getBiteRateInKbps());
    lblVideoBitDepth.setText(mediaFile.getBitDepthString());
    lblSource.setText(movieSelectionModel.getSelectedMovie().getMediaSource().toString());
  }

  private void buildAudioStreamDetails() {
    panelAudioStreamT.removeAll();
    panelAudioStreamDetails.removeAll();

    List<MediaFile> mediaFiles = movieSelectionModel.getSelectedMovie().getMediaFilesContainingAudioStreams();

    for (MediaFile mediaFile : mediaFiles) {
      for (int i = 0; i < mediaFile.getAudioStreams().size(); i++) {
        MediaFileAudioStream audioStream = mediaFile.getAudioStreams().get(i);

        if (mediaFile.getType() == MediaFileType.VIDEO) {
          panelAudioStreamT.add(new JLabel(BUNDLE.getString("metatag.internal"))); //$NON-NLS-1$
        }
        else {
          panelAudioStreamT.add(new JLabel(BUNDLE.getString("metatag.external"))); //$NON-NLS-1$
        }

        panelAudioStreamDetails.add(new JLabel(audioStream.getCodec()));
        panelAudioStreamDetails.add(new JLabel(audioStream.getChannels()));
        panelAudioStreamDetails.add(new JLabel(audioStream.getBitrateInKbps()));
        panelAudioStreamDetails.add(new JLabel(audioStream.getLanguage()));
      }
    }
  }

  private void buildSubtitleStreamDetails() {
    panelSubtitle.removeAll();

    List<MediaFile> mediaFiles = movieSelectionModel.getSelectedMovie().getMediaFilesContainingSubtitles();
    int row = 0;
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.LINE_START;

    Insets defaultInsets = constraints.insets;
    Insets rightInsets = new Insets(0, 50, 0, 50);

    for (MediaFile mediaFile : mediaFiles) {
      for (int i = 0; i < mediaFile.getSubtitles().size(); i++) {
        MediaFileSubtitle subtitle = mediaFile.getSubtitles().get(i);
        constraints.gridy = row;
        constraints.insets = defaultInsets;

        if (mediaFile.getType() == MediaFileType.VIDEO) {
          constraints.gridx = 0;
          panelSubtitle.add(new JLabel(BUNDLE.getString("metatag.internal")), constraints); //$NON-NLS-1$

          constraints.gridx = 1;
          constraints.insets = rightInsets;
          String info = subtitle.getLanguage() + (subtitle.isForced() ? " forced" : "") + " (" + subtitle.getCodec() + ")";
          panelSubtitle.add(new JLabel(info), constraints);
        }
        else {
          constraints.gridx = 0;
          panelSubtitle.add(new JLabel(BUNDLE.getString("metatag.external")), constraints); //$NON-NLS-1$

          constraints.gridx = 1;
          constraints.insets = rightInsets;
          panelSubtitle.add(new JLabel(mediaFile.getFilename()), constraints);
        }

        row++;
      }
    }
  }

  /*
   * helper classes
   */
  private class LinkLabelListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent arg0) {
      if (StringUtils.isNotBlank(lblMoviePath.getText())) {
        Path path = Paths.get(lblMoviePath.getText());
        try {
          // get the location from the label
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
    }
  }

  protected void initDataBindings() {
    BeanProperty<MovieSelectionModel, Boolean> movieSelectionModelBeanProperty = BeanProperty.create("selectedMovie.watched");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieSelectionModel, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty, chckbxWatched, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_1 = BeanProperty.create("selectedMovie.dateAddedAsString");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_1, lblDateAdded, jLabelBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_2 = BeanProperty.create("selectedMovie.path");
    BeanProperty<LinkLabel, String> linkLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, String, LinkLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_2, lblMoviePath, linkLabelBeanProperty);
    autoBinding_2.bind();
  }
}
