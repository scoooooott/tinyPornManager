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

package org.tinymediamanager.ui.tvshows.panels.episode;

import static org.tinymediamanager.core.Constants.MEDIA_FILES;
import static org.tinymediamanager.core.Constants.MEDIA_INFORMATION;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.panels.MediaFilesPanel;
import org.tinymediamanager.ui.tvshows.TvShowEpisodeSelectionModel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowEpisodeMediaInformationPanel.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeMediaInformationPanel extends JPanel {
  private static final long           serialVersionUID = 2513029074142934502L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private TvShowEpisodeSelectionModel selectionModel;
  private EventList<MediaFile>        mediaFileEventList;

  private JLabel                      lblRuntime;
  private JCheckBox                   chckbxWatched;
  private JPanel                      panelVideoStreamDetails;
  private JLabel                      lblVideoCodec;
  private JLabel                      lblVideoResolution;
  private JLabel                      lblVideoBitrate;
  private JLabel                      lblVideoBitDepth;
  private JPanel                      panelAudioStreamT;
  private JPanel                      panelAudioStreamDetails;
  private JPanel                      panelSubtitle;
  private JLabel                      lblSourceT;
  private JLabel                      lblSource;
  private MediaFilesPanel             panelMediaFiles;

  public TvShowEpisodeMediaInformationPanel(TvShowEpisodeSelectionModel model) {
    this.selectionModel = model;
    mediaFileEventList = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(MediaFile.class));

    initComponents();

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = propertyChangeEvent -> {
      String property = propertyChangeEvent.getPropertyName();
      Object source = propertyChangeEvent.getSource();
      // react on selection of a movie and change of media files
      if ((source.getClass() == TvShowEpisodeSelectionModel.class && "selectedTvShowEpisode".equals(property))
          || MEDIA_INFORMATION.equals(property)) {
        fillVideoStreamDetails();
        buildAudioStreamDetails();
        buildSubtitleStreamDetails();
      }
      if ((source.getClass() == TvShowEpisodeSelectionModel.class && "selectedTvShowEpisode".equals(property))
          || (source.getClass() == TvShowEpisode.class && MEDIA_FILES.equals(property))) {
        // this does sometimes not work. simply wrap it
        try {
          mediaFileEventList.getReadWriteLock().writeLock().lock();
          mediaFileEventList.clear();
          mediaFileEventList.addAll(selectionModel.getSelectedTvShowEpisode().getMediaFiles());
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
    setLayout(new MigLayout("", "[][][][][][][grow]", "[][][][][][shrink 0][80lp,grow]"));
    {
      JLabel lblRuntimeT = new JLabel(BUNDLE.getString("metatag.runtime")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblRuntimeT, Font.BOLD);
      add(lblRuntimeT, "cell 0 0");

      lblRuntime = new JLabel("");
      add(lblRuntime, "cell 2 0");
    }
    {
      JLabel lblWatchedT = new JLabel(BUNDLE.getString("metatag.watched")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblWatchedT, Font.BOLD);
      add(lblWatchedT, "cell 4 0");

      chckbxWatched = new JCheckBox("");
      add(chckbxWatched, "cell 6 0");
    }
    {
      lblSourceT = new JLabel(BUNDLE.getString("metatag.source")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblSourceT, Font.BOLD);
      add(lblSourceT, "cell 0 1");

      lblSource = new JLabel("");
      add(lblSource, "cell 2 1 3 1");
    }
    {
      JLabel lblVideoT = new JLabel(BUNDLE.getString("metatag.video")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblVideoT, Font.BOLD);
      add(lblVideoT, "cell 0 2");

      JLabel lblEpisodeT = new JLabel(BUNDLE.getString("metatag.episode")); //$NON-NLS-1$
      add(lblEpisodeT, "cell 2 2");

      panelVideoStreamDetails = new JPanel();
      panelVideoStreamDetails.setLayout(new GridLayout(1, 4, 0, 25));
      add(panelVideoStreamDetails, "cell 4 2 3 1,growx");

      lblVideoCodec = new JLabel("");
      panelVideoStreamDetails.add(lblVideoCodec);

      lblVideoResolution = new JLabel("");
      panelVideoStreamDetails.add(lblVideoResolution);

      lblVideoBitrate = new JLabel("");
      panelVideoStreamDetails.add(lblVideoBitrate);

      lblVideoBitDepth = new JLabel("");
      panelVideoStreamDetails.add(lblVideoBitDepth);
    }
    {
      JLabel lblAudioT = new JLabel(BUNDLE.getString("metatag.audio")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblAudioT, Font.BOLD);
      add(lblAudioT, "cell 0 3");

      panelAudioStreamT = new JPanel();
      panelAudioStreamT.setLayout(new GridLayout(0, 1));
      add(panelAudioStreamT, "cell 2 3");

      panelAudioStreamDetails = new JPanel();
      panelAudioStreamDetails.setLayout(new GridLayout(0, 4));
      add(panelAudioStreamDetails, "cell 4 3 3 1");
    }
    {
      JLabel lblSubtitle = new JLabel(BUNDLE.getString("metatag.subtitles")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblSubtitle, Font.BOLD);
      add(lblSubtitle, "cell 0 4");

      JScrollPane scrollPane = new JScrollPane();
      scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      add(scrollPane, "cell 2 4 3 1,growy");

      panelSubtitle = new JPanel();
      scrollPane.setViewportView(panelSubtitle);
      panelSubtitle.setLayout(new GridBagLayout());
    }
    {
      JSeparator separator = new JSeparator();
      add(separator, "cell 0 5 7 1,growx");
    }
    {
      panelMediaFiles = new MediaFilesPanel(mediaFileEventList) {
        private static final long serialVersionUID = 8834986141071361388L;

        @Override
        public MediaEntity getMediaEntity() {
          return selectionModel.getSelectedTvShowEpisode();
        }
      };
      add(panelMediaFiles, "cell 0 6 7 1,grow");
    }
  }

  private void fillVideoStreamDetails() {
    List<MediaFile> mediaFiles = selectionModel.getSelectedTvShowEpisode().getMediaFiles(MediaFileType.VIDEO);
    if (mediaFiles.size() == 0) {
      return;
    }

    int runtime = selectionModel.getSelectedTvShowEpisode().getRuntimeFromMediaFiles();
    if (runtime == 0) {
      lblRuntime.setText("");
    }
    else {
      long h = TimeUnit.SECONDS.toHours(runtime);
      long m = TimeUnit.SECONDS.toMinutes(runtime - TimeUnit.HOURS.toSeconds(h));
      long s = TimeUnit.SECONDS.toSeconds(runtime - TimeUnit.HOURS.toSeconds(h) - TimeUnit.MINUTES.toSeconds(m));
      if (s > 30) {
        m += 1; // round seconds
      }
      lblRuntime.setText(h + "h " + String.format("%02d", m) + "m");
    }

    MediaFile mediaFile = selectionModel.getSelectedTvShowEpisode().getBiggestMediaFile(MediaFileType.VIDEO);
    chckbxWatched.setSelected(selectionModel.getSelectedTvShowEpisode().isWatched());
    if (mediaFile != null) {
      lblVideoCodec.setText(mediaFile.getVideoCodec());
      lblVideoResolution.setText(mediaFile.getVideoResolution());
      lblVideoBitrate.setText(mediaFile.getBiteRateInKbps());
      lblVideoBitDepth.setText(mediaFile.getBitDepthString());
    }
    lblSource.setText(selectionModel.getSelectedTvShowEpisode().getMediaSource().toString());
  }

  private void buildAudioStreamDetails() {
    panelAudioStreamT.removeAll();
    panelAudioStreamDetails.removeAll();

    List<MediaFile> mediaFiles = selectionModel.getSelectedTvShowEpisode().getMediaFilesContainingAudioStreams();

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
    panelAudioStreamDetails.revalidate();
    panelAudioStreamT.revalidate();
  }

  private void buildSubtitleStreamDetails() {
    panelSubtitle.removeAll();

    List<MediaFile> mediaFiles = selectionModel.getSelectedTvShowEpisode().getMediaFilesContainingSubtitles();
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
}
