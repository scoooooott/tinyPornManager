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
package org.tinymediamanager.core;

import java.util.List;

import org.tinymediamanager.core.entities.MediaFile;

/**
 * The interface IMediaInformation is used to provide an interface to common media related infos
 * 
 * @author Manuel Laggner
 */
public interface IMediaInformation {

  /**
   * gets the certification for this video file
   * 
   * @return the certification
   */
  MediaCertification getCertification();

  /**
   * gets the main video file
   *
   * @return a MediaFile representing the main video file or an empty MediaFile
   */
  MediaFile getMainVideoFile();

  /**
   * get the accumulated file size of all video files
   * 
   * @return the accumulated file size of all video files
   */
  long getVideoFilesize();

  /**
   * gets the video format (e.g. 720p).
   * 
   * @return the video format
   */
  String getMediaInfoVideoFormat();

  /**
   * Returns the bit depth of video (eg 8 / 10)
   * 
   * @return
   */
  int getMediaInfoVideoBitDepth();

  /**
   * get the video resolution (e.g. 1920x1080)
   *
   * @return the video resolution
   */
  String getMediaInfoVideoResolution();

  /**
   * gets the aspect ratio (e.g. 1.33).
   * 
   * @return the aspect ratio
   */
  float getMediaInfoAspectRatio();

  /**
   * gets the media info video codec (e.g. divx)
   * 
   * @return the video codec
   */
  String getMediaInfoVideoCodec();

  /**
   * get the media info frame rate (e.g. 25.0)
   *
   * @return the frame rate
   */
  double getMediaInfoFrameRate();

  /**
   * is the video in 3D
   * 
   * @return true if the video is in 3D
   */
  boolean isVideoIn3D();

  /**
   * is the video in HighDynamicRange (HDR)
   * 
   * @return true if the video is HDR
   */
  String getVideoHDRFormat();

  /**
   * gets the audio codec (e.g. mp3) from the main audio stream
   * 
   * @return the audio codec
   */
  String getMediaInfoAudioCodec();

  /**
   * gets the audio codec (e.g. mp3) from all audio streams as List
   *
   * @return the audio codecs as List
   */
  List<String> getMediaInfoAudioCodecList();

  /**
   * gets the audio channels (e.g, 6 at 5.1 sound) from the main audio stream
   * 
   * @return the audio channels with a trailing ch
   */
  String getMediaInfoAudioChannels();

  /**
   * gets the audio channels (e.g, 6 at 5.1 sound) from all audio streams as List
   *
   * @return the audio channels from all streams with a trailing ch as List
   */
  List<String> getMediaInfoAudioChannelList();

  /**
   * gets the audio language (e.g, de) from the main audio stream
   *
   * @return the audio language
   */
  String getMediaInfoAudioLanguage();

  /**
   * gets the audio language (e.g, de) from the all audio streams as List
   *
   * @return the audio language from all streams
   */
  List<String> getMediaInfoAudioLanguageList();

  /**
   * gets all subtitle languages (e.g, de) from the subtitle streams as List
   *
   * @return the subtitle languages from all streams
   */
  List<String> getMediaInfoSubtitleLanguageList();

  /**
   * gets the container format
   * 
   * @return the container format
   */
  String getMediaInfoContainerFormat();

  /**
   * gets the media source
   * 
   * @return the media source
   */
  MediaSource getMediaInfoSource();
}
