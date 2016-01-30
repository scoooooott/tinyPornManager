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
package org.tinymediamanager.ui.panels;

import java.awt.FlowLayout;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.IMediaInformation;
import org.tinymediamanager.ui.converter.CertificationImageConverter;

/**
 * The class MediaInformationLogosPanel is used to display all media info related logos
 */
public class MediaInformationLogosPanel extends JPanel {
  private static final long           serialVersionUID            = -3403472105793548302L;
  private static final Logger         LOGGER                      = LoggerFactory.getLogger(MediaInformationLogosPanel.class);

  private IMediaInformation           mediaInformationSource;

  private CertificationImageConverter certificationImageConverter = new CertificationImageConverter();

  private JLabel                      lblCertification            = new JLabel();
  private JLabel                      lblVideoFormat              = new JLabel();
  private JLabel                      lblAspectRatio              = new JLabel();
  private JLabel                      lblVideoCodec               = new JLabel();
  private JLabel                      lblVideo3d                  = new JLabel();
  private JLabel                      lblAudioCodec               = new JLabel();
  private JLabel                      lblAudioChannels            = new JLabel();

  public MediaInformationLogosPanel() {
    setLayout(new FlowLayout());

    add(lblCertification);
    add(lblVideoFormat);
    add(lblAspectRatio);
    add(lblVideoCodec);
    add(lblVideo3d);
    add(lblAudioCodec);
    add(lblAudioChannels);
  }

  public void setMediaInformationSource(IMediaInformation source) {
    this.mediaInformationSource = source;

    setIcon(lblCertification, getCertificationIcon());
    setIcon(lblVideoFormat, getVideoFormatIcon());
    setIcon(lblAspectRatio, getAspectRatioIcon());
    setIcon(lblVideoCodec, getVideoCodecIcon());
    setIcon(lblVideo3d, getVideo3dIcon());
    setIcon(lblAudioCodec, getAudioCodecIcon());
    setIcon(lblAudioChannels, getAudioChannelsIcon());
  }

  private void setIcon(JLabel label, Icon icon) {
    label.setIcon(icon);
    if (icon != null) {
      label.setVisible(true);
    }
    else {
      label.setVisible(false);
    }
  }

  private Icon getCertificationIcon() {
    Icon icon = certificationImageConverter.convertForward(mediaInformationSource.getCertification());

    if (icon == null || icon == CertificationImageConverter.emptyImage) {
      return null;
    }

    return icon;
  }

  private Icon getVideoFormatIcon() {
    String videoFormat = mediaInformationSource.getMediaInfoVideoFormat();

    // a) return null if the Format is empty
    if (StringUtils.isEmpty(videoFormat)) {
      return null;
    }

    try {
      URL file = null;

      // check 1080p
      if (videoFormat.contains("1080")) {
        // try to load 1080p.png
        file = this.getClass().getResource("/images/mediainfo/video/1080p.png");
      }

      // check 720p
      if (videoFormat.contains("720")) {
        // try to load 720p.png
        file = this.getClass().getResource("/images/mediainfo/video/720p.png");
      }

      // check 576p
      if (videoFormat.contains("576p")) {
        // try to load 576p.png
        file = this.getClass().getResource("/images/mediainfo/video/576p.png");
      }

      // check 540p
      if (videoFormat.contains("540p")) {
        // try to load 540p.png
        file = this.getClass().getResource("/images/mediainfo/video/540p.png");
      }

      // check 480p
      if (videoFormat.contains("480p")) {
        // try to load 480p.png
        file = this.getClass().getResource("/images/mediainfo/video/480p.png");
      }

      // return image
      if (file != null) {
        return new ImageIcon(file);
      }
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
    }

    // we did not get any file: return the empty
    return null;
  }

  private Icon getAspectRatioIcon() {
    float aspectRatio = mediaInformationSource.getMediaInfoAspectRatio();

    // a) return null if the aspect ratio is zero
    if (aspectRatio == 0) {
      return null;
    }

    try {
      String ratio = String.valueOf(aspectRatio);
      // try to load image
      URL file = this.getClass().getResource("/images/mediainfo/aspect/" + ratio + ".png");

      // return image
      if (file != null) {
        return new ImageIcon(file);
      }
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
    }

    // we did not get any file: return the empty
    return null;
  }

  private Icon getVideoCodecIcon() {
    String videoCodec = mediaInformationSource.getMediaInfoVideoCodec();

    // a) return null if the Format is empty
    if (StringUtils.isEmpty(videoCodec)) {
      return null;
    }

    try {
      URL file = this.getClass().getResource("/images/mediainfo/video/" + videoCodec.toLowerCase() + ".png");
      if (file != null) {
        return new ImageIcon(file);
      }
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
    }

    // we did not get any file: return the empty
    return null;
  }

  private Icon getAudioCodecIcon() {
    String audioCodec = mediaInformationSource.getMediaInfoAudioCodec();

    // a) return null if the codec is empty
    if (StringUtils.isEmpty(audioCodec)) {
      return null;
    }

    try {
      URL file = this.getClass().getResource("/images/mediainfo/audio/" + audioCodec.toLowerCase() + ".png");
      if (file != null) {
        return new ImageIcon(file);
      }
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
    }

    // we did not get any file: return the empty
    return null;
  }

  private Icon getAudioChannelsIcon() {
    int audioChannels = mediaInformationSource.getMediaInfoAudioChannels();

    // a) return null if there are no channels
    if (audioChannels == 0) {
      return null;
    }

    try {
      URL file = this.getClass().getResource("/images/mediainfo/audio/" + audioChannels + "ch.png");
      if (file != null) {
        return new ImageIcon(file);
      }
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
    }

    // we did not get any file: return the empty
    return null;
  }

  private Icon getVideo3dIcon() {
    // a) return null if the video is not in 3D
    if (!mediaInformationSource.isVideoIn3D()) {
      return null;
    }

    try {
      URL file = this.getClass().getResource("/images/mediainfo/video/3d.png");
      if (file != null) {
        return new ImageIcon(file);
      }
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
    }

    // we did not get any file: return the empty
    return null;
  }
}
