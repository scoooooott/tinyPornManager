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
package org.tinymediamanager.ui.panels;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.IMediaInformation;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.ui.converter.CertificationImageConverter;

import net.miginfocom.swing.MigLayout;

/**
 * The class MediaInformationLogosPanel is used to display all media info related logos
 */
public class MediaInformationLogosPanel extends JPanel {
  private static final long           serialVersionUID            = -3403472105793548302L;
  private static final Logger         LOGGER                      = LoggerFactory.getLogger(MediaInformationLogosPanel.class);

  private static final String         IMAGE_SOURCE                = "/org/tinymediamanager/ui/images/";

  private IMediaInformation           mediaInformationSource;

  private CertificationImageConverter certificationImageConverter = new CertificationImageConverter();

  private JLabel                      lblCertification            = new JLabel();
  private JLabel                      lblVideoFormat              = new JLabel();
  private JLabel                      lblAspectRatio              = new JLabel();
  private JLabel                      lblVideoCodec               = new JLabel();
  private JLabel                      lblVideo3d                  = new JLabel();
  private JLabel                      lblAudioCodec               = new JLabel();
  private JLabel                      lblAudioChannels            = new JLabel();
  private JLabel                      lblContainer                = new JLabel();
  private JLabel                      lblSource                   = new JLabel();

  public MediaInformationLogosPanel() {
    setLayout(new MigLayout("hidemode 3, insets n n n 15lp", "[][][][][][10lp][][][10lp][][]", "[]"));

    add(lblCertification, "cell 0 0, gapright 15lp");

    add(lblVideoFormat, "cell 1 0");
    add(lblAspectRatio, "cell 2 0");
    add(lblVideoCodec, "cell 3 0");
    add(lblVideo3d, "cell 4 0");

    add(lblAudioChannels, "cell 6 0");
    add(lblAudioCodec, "cell 7 0");

    add(lblSource, "cell 9 0");
    // add(lblContainer, "cell 10 0");
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
    setIcon(lblContainer, getContainerIcon());
    setIcon(lblSource, getSourceIcon());
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

  /**
   * get the right icon for the given certification
   * 
   * @return the icon or null
   */
  private Icon getCertificationIcon() {
    Icon icon = certificationImageConverter.convertForward(mediaInformationSource.getCertification());

    if (icon == null || icon == CertificationImageConverter.emptyImage) {
      return null;
    }

    return icon;
  }

  /**
   * get the right icon for the video format
   * 
   * @return the icon or null
   */
  private Icon getVideoFormatIcon() {
    String videoFormat = mediaInformationSource.getMediaInfoVideoFormat();

    // a) return null if the Format is empty
    if (StringUtils.isEmpty(videoFormat)) {
      return null;
    }

    try {
      URL file = this.getClass().getResource(IMAGE_SOURCE + "/video/format/" + videoFormat + ".png");

      if (file == null) {
        file = this.getClass().getResource(IMAGE_SOURCE + "/video/format/" + videoFormat.replaceAll("[pi]", "") + ".png");
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

  /**
   * get the right icon for the aspect ratio
   * 
   * @return the icon or null
   */
  private Icon getAspectRatioIcon() {
    float aspectRatio = mediaInformationSource.getMediaInfoAspectRatio();

    // a) return null if the aspect ratio is zero
    if (aspectRatio == 0) {
      return null;
    }

    try {
      String ratio = String.valueOf(aspectRatio);
      // try to load image
      URL file = this.getClass().getResource(IMAGE_SOURCE + "/aspectratio/" + ratio + ".png");

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

  /**
   * get the right icon for the video codec
   * 
   * @return the icon or null
   */
  private Icon getVideoCodecIcon() {
    String videoCodec = mediaInformationSource.getMediaInfoVideoCodec();

    // a) return null if the Format is empty
    if (StringUtils.isEmpty(videoCodec)) {
      return null;
    }

    try {
      URL file = this.getClass().getResource(IMAGE_SOURCE + "/video/codec/" + videoCodec.toLowerCase() + ".png");
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

  /**
   * get the right icon for the audio codec
   * 
   * @return the icon or null
   */
  private Icon getAudioCodecIcon() {
    String audioCodec = mediaInformationSource.getMediaInfoAudioCodec();

    // a) return null if the codec is empty
    if (StringUtils.isEmpty(audioCodec)) {
      return null;
    }

    try {
      URL file = this.getClass().getResource(IMAGE_SOURCE + "/audio/codec/" + audioCodec.toLowerCase() + ".png");
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

  /**
   * get the right icon for the audio channels
   * 
   * @return the icon or null
   */
  private Icon getAudioChannelsIcon() {
    int audioChannels = mediaInformationSource.getMediaInfoAudioChannels();

    // a) return null if there are no channels
    if (audioChannels == 0) {
      return null;
    }

    try {
      URL file = this.getClass().getResource(IMAGE_SOURCE + "/audio/channels/" + audioChannels + ".png");

      // stereo?
      if (audioChannels == 2) {
        file = this.getClass().getResource(IMAGE_SOURCE + "/audio/channels/2.0.png");
      }

      if (file == null) {
        String channels = audioChannels - 1 + ".1";
        file = this.getClass().getResource(IMAGE_SOURCE + "/audio/channels/" + channels + ".png");
      }

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

  /**
   * get the right icon for 3D
   * 
   * @return the icon or null
   */
  private Icon getVideo3dIcon() {
    // a) return null if the video is not in 3D
    if (!mediaInformationSource.isVideoIn3D()) {
      return null;
    }

    try {
      URL file = this.getClass().getResource(IMAGE_SOURCE + "/video/3d.png");
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

  /**
   * get the right icon for the container
   * 
   * @return the icon or null
   */
  private Icon getContainerIcon() {
    String container = mediaInformationSource.getMediaInfoContainerFormat();

    // a) return null if the container is empty
    if (StringUtils.isEmpty(container)) {
      return null;
    }

    try {
      URL file = this.getClass().getResource(IMAGE_SOURCE + "/container/" + container.toLowerCase() + ".png");
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

  /**
   * get the media source
   * 
   * @return the icon or null
   */
  private Icon getSourceIcon() {
    MediaSource source = mediaInformationSource.getMediaInfoSource();

    // a) return null if the source is empty
    if (source == MediaSource.UNKNOWN) {
      return null;
    }

    try {
      URL file = this.getClass().getResource(IMAGE_SOURCE + "/source/" + source.name().toLowerCase() + ".png");
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
