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
package org.tinymediamanager.core;

import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

/**
 * The enum MovieMediaSource - to represent all possible media sources for movies
 * 
 * @author Manuel Laggner
 */
public enum MediaSource {
  //@formatter:off
  // the well known and XBMC/Kodi compatible sources
  TV("TV"), 
  VHS("VHS"), 
  DVD("DVD"), 
  HDDVD("HDDVD"), 
  BLURAY("Blu-ray"),
  // other sources
  HDRIP("HDRip"),
  CAM("Cam"),
  TS("Telesync"),
  TC("Telecine"),
  DVDSCR("DVD Screener"),
  R5("R5"),
  WEBRIP("Webrip"),
  WEB_DL("Web-DL"),
  STREAM("Stream"),
  // and our fallback
  UNKNOWN("Unknown");  // @formatter:on

  private static final String START_TOKEN   = "[ .\\-_/\\\\\\[\\(]";
  private static final String END_TOKEN     = "([ .\\-_/\\\\\\]\\)]|$)";

  // tokens taken from http://en.wikipedia.org/wiki/Pirated_movie_release_types
  private static Pattern      blurayPattern = Pattern
      .compile(START_TOKEN + "(bluray|blueray|bdrip|brrip|dbrip|bd25|bd50|bdmv|blu\\-ray)" + END_TOKEN);
  private static Pattern      hdripPattern  = Pattern.compile(START_TOKEN + "(hdrip)" + END_TOKEN);
  private static Pattern      hddvdPattern  = Pattern.compile(START_TOKEN + "(hddvd|hddvdrip)" + END_TOKEN);
  private static Pattern      dvdPattern    = Pattern.compile(START_TOKEN + "(dvd|video_ts|dvdrip|dvdr|r5)" + END_TOKEN);
  private static Pattern      tvPattern     = Pattern.compile(START_TOKEN + "(hdtv|pdtv|dsr|dtv|hdtvrip|tvrip|dvbrip)" + END_TOKEN);
  private static Pattern      vhsPattern    = Pattern.compile(START_TOKEN + "(vhs)" + END_TOKEN);
  private static Pattern      camPattern    = Pattern.compile(START_TOKEN + "(cam)" + END_TOKEN);
  private static Pattern      tsPattern     = Pattern.compile(START_TOKEN + "(ts|telesync|hdts|ht\\-ts)" + END_TOKEN);
  private static Pattern      tcPattern     = Pattern.compile(START_TOKEN + "(tc|telecine|hdtc|ht\\-tc)" + END_TOKEN);
  private static Pattern      dvdscrPattern = Pattern.compile(START_TOKEN + "(dvdscr)" + END_TOKEN);
  private static Pattern      r5Pattern     = Pattern.compile(START_TOKEN + "(r5)" + END_TOKEN);
  private static Pattern      webripPattern = Pattern.compile(START_TOKEN + "(webrip)" + END_TOKEN);
  private static Pattern      webdlPattern  = Pattern.compile(START_TOKEN + "(web-dl|webdl)" + END_TOKEN);

  private String              title;

  MediaSource(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return title;
  }

  /**
   * returns the MediaSource if found in file name
   * 
   * @param filename
   *          the filename
   * @return Bluray | HDDVD | TV | DVD | VHS
   */
  public static MediaSource parseMediaSource(String filename) {
    String fn = filename.toLowerCase(Locale.ROOT);
    String ext = FilenameUtils.getExtension(fn);
    // http://wiki.xbmc.org/index.php?title=Media_flags#Media_source

    if (blurayPattern.matcher(fn).find()) {
      return MediaSource.BLURAY; // yes!
    }
    else if (hdripPattern.matcher(fn).find()) {
      return MediaSource.HDRIP;
    }
    else if (dvdPattern.matcher(fn).find()) {
      return MediaSource.DVD;
    }
    else if (hddvdPattern.matcher(fn).find()) {
      return MediaSource.HDDVD;
    }
    else if (tsPattern.matcher(fn).find()) {
      return MediaSource.TS;
    }
    else if (dvdscrPattern.matcher(fn).find()) {
      return MediaSource.DVDSCR;
    }
    else if (tvPattern.matcher(fn).find()) {
      return MediaSource.TV;
    }
    else if (camPattern.matcher(fn).find()) {
      return MediaSource.CAM;
    }
    else if (webripPattern.matcher(fn).find()) {
      return MediaSource.WEBRIP;
    }
    else if (webdlPattern.matcher(fn).find()) {
      return MediaSource.WEB_DL;
    }
    else if (vhsPattern.matcher(fn).find()) {
      return MediaSource.VHS;
    }
    else if (tcPattern.matcher(fn).find()) {
      return MediaSource.TC;
    }
    else if (r5Pattern.matcher(fn).find()) {
      return MediaSource.R5;
    }

    if (ext.equals("strm")) {
      return MediaSource.STREAM;
    }

    return MediaSource.UNKNOWN;
  }
}
