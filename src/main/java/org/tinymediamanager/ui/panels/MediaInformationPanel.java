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
package org.tinymediamanager.ui.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.components.table.TmmTableFormat;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import net.miginfocom.swing.MigLayout;

/**
 * The class {@link MediaInformationPanel} is used to display generic data from media entities
 * 
 * @author Manuel Laggner
 */
abstract public class MediaInformationPanel extends JPanel {
  private static final long                 serialVersionUID = 2513029074142934502L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle       BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());
  private static final Logger               LOGGER           = LoggerFactory.getLogger(MediaInformationPanel.class);

  protected EventList<MediaFile>            mediaFileEventList;
  protected EventList<AudioStreamContainer> audioStreamEventList;
  protected EventList<SubtitleContainer>    subtitleEventList;

  protected JLabel                          lblRuntime;
  protected JCheckBox                       chckbxWatched;
  protected JLabel                          lblVideoCodec;
  protected JLabel                          lblVideoResolution;
  protected JLabel                          lblVideoBitrate;
  protected JLabel                          lblVideoBitDepth;
  protected JLabel                          lblFrameRate;
  protected JLabel                          lblSource;
  protected LinkLabel                       lblPath;
  protected JLabel                          lblDateAdded;
  protected JLabel                          lblOriginalFilename;
  protected JLabel                          lblHdrFormat;

  protected MediaFilesPanel                 panelMediaFiles;

  public MediaInformationPanel() {
    mediaFileEventList = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(MediaFile.class));
    audioStreamEventList = GlazedLists.threadSafeList(new BasicEventList<>());
    subtitleEventList = GlazedLists.threadSafeList(new BasicEventList<>());

    initComponents();
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[][][100lp][20lp][][grow]", "[][][][][][][][][5lp!][50lp:75lp,grow][5lp!][50lp:75lp,grow][][][200lp,grow 300]"));
    {
      JLabel lblMoviePathT = new TmmLabel(BUNDLE.getString("metatag.path"));
      add(lblMoviePathT, "cell 0 0");

      lblPath = new LinkLabel("");
      lblPath.addActionListener(new LinkLabelListener());
      add(lblPath, "cell 1 0 5 1,growx,wmin 0");
    }
    {
      JLabel lblDateAddedT = new TmmLabel(BUNDLE.getString("metatag.dateadded"));
      add(lblDateAddedT, "cell 0 1");

      lblDateAdded = new JLabel("");
      add(lblDateAdded, "cell 1 1");
    }
    {
      JLabel lblWatchedT = new TmmLabel(BUNDLE.getString("metatag.watched"));
      add(lblWatchedT, "flowx,cell 4 1 2 1");

      chckbxWatched = new JCheckBox("");
      chckbxWatched.setEnabled(false);
      add(chckbxWatched, "cell 4 1 2 1");
    }
    {
      JLabel lblOriginalFilenameT = new TmmLabel(BUNDLE.getString("metatag.originalfile"));
      add(lblOriginalFilenameT, "cell 0 2");

      lblOriginalFilename = new JLabel("");
      add(lblOriginalFilename, "cell 1 2 5 1,growx,wmin 0");
    }
    {
      add(new JSeparator(), "cell 0 3 6 1,growx");
    }
    {
      JLabel lblVideoT = new TmmLabel(BUNDLE.getString("metatag.video"));
      add(lblVideoT, "cell 0 4");

      JLabel lblSourceT = new TmmLabel(BUNDLE.getString("metatag.source"));
      add(lblSourceT, "cell 1 4");

      lblSource = new JLabel("");
      add(lblSource, "cell 2 4");

      JLabel lblHdrFormatT = new TmmLabel(BUNDLE.getString("metatag.hdrformat"));
      add(lblHdrFormatT, "cell 4 4");

      lblHdrFormat = new JLabel("");
      add(lblHdrFormat, "cell 5 4");

      JLabel lblRuntimeT = new TmmLabel(BUNDLE.getString("metatag.runtime"));
      add(lblRuntimeT, "cell 1 5");

      lblRuntime = new JLabel("");
      add(lblRuntime, "cell 2 5");

      JLabel lblCodecT = new TmmLabel(BUNDLE.getString("metatag.videocodec"));
      add(lblCodecT, "cell 1 6");

      lblVideoCodec = new JLabel("");
      add(lblVideoCodec, "cell 2 6");

      JLabel lblFrameRateT = new TmmLabel(BUNDLE.getString("metatag.framerate"));
      add(lblFrameRateT, "cell 4 6");

      lblFrameRate = new JLabel("");
      add(lblFrameRate, "cell 5 6");

      JLabel lblResolutionT = new TmmLabel(BUNDLE.getString("metatag.resolution"));
      add(lblResolutionT, "cell 1 7");

      lblVideoResolution = new JLabel("");
      add(lblVideoResolution, "cell 2 7");

      JLabel lblVideoBitrateT = new TmmLabel(BUNDLE.getString("metatag.videobitrate"));
      add(lblVideoBitrateT, "cell 4 7");

      lblVideoBitrate = new JLabel("");
      add(lblVideoBitrate, "cell 5 7");

      JLabel lblVideoBitDepthT = new TmmLabel(BUNDLE.getString("metatag.videobitdepth"));
      add(lblVideoBitDepthT, "cell 4 5");

      lblVideoBitDepth = new JLabel("");
      add(lblVideoBitDepth, "cell 5 5");
    }
    {
      JLabel lblAudioT = new TmmLabel(BUNDLE.getString("metatag.audio"));
      add(lblAudioT, "cell 0 9,aligny top");

      TmmTable tableAudioStreams = new TmmTable(
          new DefaultEventTableModel<>(GlazedListsSwing.swingThreadProxyList(audioStreamEventList), new AudioStreamTableFormat()));
      tableAudioStreams.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      JScrollPane scrollPane = new JScrollPane(tableAudioStreams);
      tableAudioStreams.configureScrollPane(scrollPane);
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      add(scrollPane, "cell 1 9 5 1,growx");
    }
    {
      JLabel lblSubtitle = new TmmLabel(BUNDLE.getString("metatag.subtitles"));
      add(lblSubtitle, "cell 0 11,aligny top");

      TmmTable tableSubtitles = new TmmTable(
          new DefaultEventTableModel<>(GlazedListsSwing.swingThreadProxyList(subtitleEventList), new SubtitleTableFormat()));
      tableSubtitles.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      JScrollPane scrollPane = new JScrollPane(tableSubtitles);
      tableSubtitles.configureScrollPane(scrollPane);
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      add(scrollPane, "cell 1 11 5 1,growx");
    }
    {
      add(new JSeparator(), "cell 0 12 6 1,growx");
    }
    {
      JLabel lblMediaFilesT = new TmmLabel(BUNDLE.getString("metatag.mediafiles"));
      add(lblMediaFilesT, "cell 0 13 2 1");
    }
    {
      panelMediaFiles = new MediaFilesPanel(mediaFileEventList) {
        private static final long serialVersionUID = -8712860341504013403L;

        @Override
        public MediaEntity getMediaEntity() {
          return MediaInformationPanel.this.getMediaEntity();
        }
      };
      add(panelMediaFiles, "cell 0 14 6 1,grow");
    }
  }

  abstract protected MediaEntity getMediaEntity();

  abstract protected void fillVideoStreamDetails();

  abstract protected void buildAudioStreamDetails();

  abstract protected void buildSubtitleStreamDetails();

  /*
   * helper classes
   */
  protected class LinkLabelListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent arg0) {
      if (StringUtils.isNotBlank(lblPath.getText())) {
        Path path = Paths.get(lblPath.getText());
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

  protected static class AudioStreamContainer {
    public String               source;
    public MediaFileAudioStream audioStream;

    public AudioStreamContainer() {
    }
  }

  protected static class AudioStreamTableFormat extends TmmTableFormat<AudioStreamContainer> {
    AudioStreamTableFormat() {
      Comparator<String> stringComparator = new StringComparator();

      /*
       * source
       */
      Column col = new Column(BUNDLE.getString("metatag.source"), "source", container -> container.source, String.class);
      col.setColumnComparator(stringComparator);
      addColumn(col);

      /*
       * audio codec
       */
      col = new Column(BUNDLE.getString("metatag.codec"), "codec", container -> container.audioStream.getCodec(), String.class);
      col.setColumnComparator(stringComparator);
      addColumn(col);

      /*
       * channels
       */
      col = new Column(BUNDLE.getString("metatag.channels"), "channels", container -> {
        int channels = container.audioStream.getAudioChannels();

        if (channels > 0) {
          return channels + "ch";
        }

        return "";
      }, String.class);
      col.setColumnComparator(stringComparator);
      addColumn(col);

      /*
       * bitrate
       */
      col = new Column(BUNDLE.getString("metatag.bitrate"), "bitrate", container -> container.audioStream.getBitrateInKbps(), String.class);
      col.setColumnComparator(stringComparator);
      addColumn(col);

      /*
       * language
       */
      col = new Column(BUNDLE.getString("metatag.language"), "language", container -> container.audioStream.getLanguage(), String.class);
      col.setColumnComparator(stringComparator);
      addColumn(col);
    }
  }

  protected static class SubtitleContainer {
    public String            source;
    public MediaFileSubtitle subtitle;

    public SubtitleContainer() {
    }
  }

  protected static class SubtitleTableFormat extends TmmTableFormat<SubtitleContainer> {
    SubtitleTableFormat() {
      Comparator<String> stringComparator = new StringComparator();

      /*
       * source
       */
      Column col = new Column(BUNDLE.getString("metatag.source"), "source", container -> container.source, String.class);
      col.setColumnComparator(stringComparator);
      addColumn(col);

      /*
       * language
       */
      col = new Column(BUNDLE.getString("metatag.language"), "language", container -> {
        String language = container.subtitle.getLanguage();

        if (container.subtitle.isForced()) {
          language += " forced";
        }

        if (StringUtils.isNotBlank(container.subtitle.getCodec())) {
          language += " (" + container.subtitle.getCodec() + ")";
        }

        return language;
      }, String.class);
      col.setColumnComparator(stringComparator);
      addColumn(col);
    }
  }
}
