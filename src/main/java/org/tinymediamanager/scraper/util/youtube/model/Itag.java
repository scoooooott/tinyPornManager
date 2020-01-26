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
package org.tinymediamanager.scraper.util.youtube.model;

import java.util.ArrayList;
import java.util.List;

import org.tinymediamanager.scraper.util.youtube.model.quality.AudioQuality;
import org.tinymediamanager.scraper.util.youtube.model.quality.VideoQuality;

/**
 * YouTube video / audio stream format codes
 *
 * @author Wolfgang Janes
 */
public enum Itag {

  UNKNOWN {
    @Override
    public void setId(int id) {
      this.id = id;
    }
  },

  I_5(VideoQuality.SMALL, AudioQuality.LOW),
  I_6(VideoQuality.SMALL, AudioQuality.LOW),
  I_13(VideoQuality.UNKNOWN, AudioQuality.LOW),
  I_17(VideoQuality.TINY, AudioQuality.LOW),
  I_18(VideoQuality.MEDIUM, AudioQuality.LOW),
  I_22(VideoQuality.HD_720, AudioQuality.MEDIUM),

  I_34(VideoQuality.MEDIUM, AudioQuality.MEDIUM),
  I_35(VideoQuality.LARGE, AudioQuality.MEDIUM),
  I_36(VideoQuality.TINY, AudioQuality.UNKNOWN),
  I_37(VideoQuality.HD_1080, AudioQuality.MEDIUM),
  I_38(VideoQuality.HIGHRES, AudioQuality.MEDIUM),

  I_43(VideoQuality.MEDIUM, AudioQuality.MEDIUM),
  I_44(VideoQuality.LARGE, AudioQuality.MEDIUM),
  I_45(VideoQuality.HD_720, AudioQuality.MEDIUM),
  I_46(VideoQuality.HD_1080, AudioQuality.MEDIUM),

  //3D
  I_82(VideoQuality.MEDIUM, AudioQuality.MEDIUM, true),
  I_83(VideoQuality.LARGE, AudioQuality.MEDIUM, true),
  I_84(VideoQuality.HD_720, AudioQuality.MEDIUM, true),
  I_85(VideoQuality.HD_1080, AudioQuality.MEDIUM, true),
  I_100(VideoQuality.MEDIUM, AudioQuality.MEDIUM, true),
  I_101(VideoQuality.LARGE, AudioQuality.MEDIUM, true),
  I_102(VideoQuality.HD_720, AudioQuality.MEDIUM, true),

  //Apple Live Streaming
  I_91(VideoQuality.TINY, AudioQuality.LOW),
  I_92(VideoQuality.SMALL, AudioQuality.LOW),
  I_93(VideoQuality.MEDIUM, AudioQuality.MEDIUM),
  I_94(VideoQuality.LARGE, AudioQuality.MEDIUM),
  I_95(VideoQuality.HD_720, AudioQuality.HIGH),
  I_96(VideoQuality.HD_1080, AudioQuality.HIGH),
  I_132(VideoQuality.SMALL, AudioQuality.LOW),
  I_151(VideoQuality.TINY, AudioQuality.LOW),

  // DASH MP4 Video
  I_133(VideoQuality.SMALL),
  I_134(VideoQuality.MEDIUM),
  I_135(VideoQuality.LARGE),
  I_136(VideoQuality.HD_720),
  I_137(VideoQuality.HD_1080),
  I_138(VideoQuality.HD_2160),
  I_160(VideoQuality.TINY),
  I_212(VideoQuality.LARGE),
  I_264(VideoQuality.HD_1440),
  I_266(VideoQuality.HD_2160),
  I_298(VideoQuality.HD_720),
  I_299(VideoQuality.HD_1080),

  //DASH MP4 Audio
  I_139(AudioQuality.LOW),
  I_140(AudioQuality.MEDIUM),
  I_141(AudioQuality.HIGH),
  I_256(AudioQuality.UNKNOWN),
  I_325(AudioQuality.UNKNOWN),
  I_328(AudioQuality.UNKNOWN),

  // DASH webm video
  I_167(VideoQuality.MEDIUM),
  I_168(VideoQuality.LARGE),
  I_169(VideoQuality.HD_1080),
  I_170(VideoQuality.HD_1080),
  I_218(VideoQuality.LARGE),
  I_219(VideoQuality.TINY),
  I_242(VideoQuality.SMALL),
  I_243(VideoQuality.MEDIUM),
  I_244(VideoQuality.LARGE),
  I_245(VideoQuality.LARGE),
  I_246(VideoQuality.LARGE),
  I_247(VideoQuality.HD_720),
  I_248(VideoQuality.HD_1080),
  I_271(VideoQuality.HD_1440),
  I_272(VideoQuality.HIGHRES),
  I_278(VideoQuality.TINY),
  I_302(VideoQuality.HD_720),
  I_303(VideoQuality.HD_1080),
  I_308(VideoQuality.HD_1440),
  I_313(VideoQuality.HD_2160),
  I_315(VideoQuality.HD_2160),

  //DASH webm audio
  I_171(AudioQuality.MEDIUM),
  I_172(AudioQuality.HIGH),

  //DASH webm audio with opus inside
  I_249(AudioQuality.LOW),
  I_250(AudioQuality.MEDIUM),
  I_251(AudioQuality.MEDIUM),

  //DASH webm HDR video
  I_330(VideoQuality.TINY),
  I_331(VideoQuality.SMALL),
  I_332(VideoQuality.MEDIUM),
  I_333(VideoQuality.LARGE),
  I_334(VideoQuality.HD_720),
  I_335(VideoQuality.HD_1080),
  I_336(VideoQuality.HD_1440),
  I_337(VideoQuality.HD_2160),

  //AV1 Video only formats
  I_394(VideoQuality.TINY),
  I_395(VideoQuality.SMALL),
  I_396(VideoQuality.MEDIUM),
  I_397(VideoQuality.LARGE),
  I_398(VideoQuality.HD_720),
  I_399(VideoQuality.HD_1080),
  I_400(VideoQuality.HD_1440),
  I_401(VideoQuality.HD_2160),
  I_402(VideoQuality.HD_2880);

  protected int        id;
  private VideoQuality videoQuality;
  private AudioQuality audioQuality;
  private boolean      isVRor3D;

  Itag() {
    this.videoQuality = VideoQuality.UNKNOWN;
    this.audioQuality = AudioQuality.UNKNOWN;
  }

  Itag(VideoQuality videoQuality) {
    this(videoQuality, AudioQuality.NO_AUDIO, false);
  }

  Itag(AudioQuality audioQuality) {
    this(VideoQuality.NO_VIDEO, audioQuality, false);
  }

  Itag(VideoQuality videoQuality, AudioQuality audioQuality) {
    this(videoQuality, audioQuality, false);
  }

  Itag(VideoQuality videoQuality, AudioQuality audioQuality, boolean isVRor3D) {
    setId(Integer.parseInt(name().substring(2)));
    this.videoQuality = videoQuality;
    this.audioQuality = audioQuality;
    this.isVRor3D = isVRor3D;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int id() {
    return id;
  }

  public VideoQuality videoQuality() {
    return videoQuality;
  }

  public AudioQuality audioQuality() {
    return audioQuality;
  }

  public boolean isVideo() {
    return videoQuality != VideoQuality.NO_VIDEO;
  }

  public boolean isAudio() {
    return audioQuality != AudioQuality.NO_AUDIO;
  }

  public boolean isVRor3D() {
    return isVRor3D;
  }

  public static Itag findItag(int number) {
    try {
      return Itag.valueOf("I_" + number);
    }
    catch (ExceptionInInitializerError e) {
      // nothing to do here
    }
    return null;
  }

  public static List<AudioQuality> getAudioQualityList() {
    List<AudioQuality> list = new ArrayList<>();
    list.add(AudioQuality.HIGH);
    list.add(AudioQuality.MEDIUM);
    list.add(AudioQuality.LOW);

    return list;
  }

  public static List<VideoQuality> getVideoQualityList() {
    List<VideoQuality> list = new ArrayList<>();
    list.add(VideoQuality.HIGHRES);
    list.add(VideoQuality.HD_2160);
    list.add(VideoQuality.HD_1440);
    list.add(VideoQuality.HD_1080);
    list.add(VideoQuality.HD_720);
    list.add(VideoQuality.LARGE);
    list.add(VideoQuality.MEDIUM);
    list.add(VideoQuality.SMALL);
    list.add(VideoQuality.TINY);

    return list;
  }

  @Override
  public String toString() {
    return String.valueOf(id);
  }
}
