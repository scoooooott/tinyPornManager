/*
 * Copyright 2012 - 2014 Manuel Laggner
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
package org.tinymediamanager.core.movie;

import java.util.regex.Pattern;

/**
 * The enum MovieMediaSource - to represent all possible media sources for movies
 * 
 * @author Manuel Laggner
 */
public enum MovieMediaSource {
  //@formatter:off
  // the well known and XBMC/Kodi compatible sources
  TV("TV"), 
  VHS("VHS"), 
  DVD("DVD"), 
  HDDVD("HDDVD"), 
  BLURAY("Bluray"), 
  // other sources
  CAM("Cam"),
  TS("Telesync"),
  DVDSCR("DVD Screener"),
  WEBRIP("Webrip"),
  // and our fallback
  UNKNOWN("Unknown");  // @formatter:on

  // tokens taken from http://en.wikipedia.org/wiki/Pirated_movie_release_types
  private static Pattern blurayPattern = Pattern
                                           .compile("[ .\\-_/\\\\\\[\\(](bluray|blueray|bdrip|brrip|dbrip|bd25|bd50|bdmv|hdrip)[ .\\-_/\\\\\\]\\)]?");
  private static Pattern hddvdPattern  = Pattern.compile("[ .\\-_/\\\\\\[\\(](hddvd|hddvdrip)[ .\\-_/\\\\\\]\\)]?");
  private static Pattern dvdPattern    = Pattern.compile("[ .\\-_/\\\\\\[\\(](dvd|video_ts|dvdrip|dvdr|r5)[ .\\-_/\\\\\\]\\)]?");
  private static Pattern tvPattern     = Pattern.compile("[ .\\-_/\\\\\\[\\(](hdtv|pdtv|dsr|dtv|hdtvrip|tvrip|dvbrip)[ .\\-_/\\\\\\]\\)]?");
  private static Pattern vhsPattern    = Pattern.compile("[ .\\-_/\\\\\\[\\(](vhs)[ .\\-_/\\\\\\]\\)]?");
  private static Pattern camPattern    = Pattern.compile("[ .\\-_/\\\\\\[\\(](cam)[ .\\-_/\\\\\\]\\)]?");
  private static Pattern tsPattern     = Pattern.compile("[ .\\-_/\\\\\\[\\(](ts|telesync|hdts)[ .\\-_/\\\\\\]\\)]?");
  private static Pattern dvdscrPattern = Pattern.compile("[ .\\-_/\\\\\\[\\(](dvdscr)[ .\\-_/\\\\\\]\\)]?");
  private static Pattern webripPattern = Pattern.compile("[ .\\-_/\\\\\\[\\(](webrip)[ .\\-_/\\\\\\]\\)]?");

  private String         title;

  private MovieMediaSource(String title) {
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
  public static MovieMediaSource parseMediaSource(String filename) {
    String fn = filename.toLowerCase();
    // http://wiki.xbmc.org/index.php?title=Media_flags#Media_source

    if (blurayPattern.matcher(fn).find()) {
      return MovieMediaSource.BLURAY; // yes!
    }
    else if (dvdPattern.matcher(fn).find()) {
      return MovieMediaSource.DVD;
    }
    else if (hddvdPattern.matcher(fn).find()) {
      return MovieMediaSource.HDDVD;
    }
    else if (tsPattern.matcher(fn).find()) {
      return MovieMediaSource.TS;
    }
    else if (dvdscrPattern.matcher(fn).find()) {
      return MovieMediaSource.DVDSCR;
    }
    else if (tvPattern.matcher(fn).find()) {
      return MovieMediaSource.TV;
    }
    else if (camPattern.matcher(fn).find()) {
      return MovieMediaSource.CAM;
    }
    else if (webripPattern.matcher(fn).find()) {
      return MovieMediaSource.WEBRIP;
    }
    else if (vhsPattern.matcher(fn).find()) {
      return MovieMediaSource.VHS;
    }

    return MovieMediaSource.UNKNOWN;
  }
}
