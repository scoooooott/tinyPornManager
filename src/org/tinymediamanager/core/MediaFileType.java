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

package org.tinymediamanager.core;

import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;

/**
 * various MediaFileTypes
 * 
 * @author Myron Boyle
 */
public enum MediaFileType {

  // ordering of list = ordering of type in GUI ;)

  // @formatter:off
  VIDEO, 
  VIDEO_EXTRA, // bonus/extra videos
  TRAILER, 
  SAMPLE, // sample != trailer
  AUDIO, 
  SUBTITLE, 
  NFO, 
  POSTER, 
  FANART, 
  BANNER,
  CLEARART,
  DISCART,
  LOGO,
  THUMB,   
  SEASON_POSTER,
  EXTRAFANART, 
  EXTRATHUMB,  
  GRAPHIC,
  TEXT, // various text infos, like BDinfo.txt or others...
  UNKNOWN;
  // @formatter:on

  /**
   * get the corresponding media file type for the artwork type
   *
   * @param artworkType
   *          the artwork type
   * @return the media file type
   */
  public static MediaFileType getMediaFileType(MediaArtworkType artworkType) {
    switch (artworkType) {
      case BACKGROUND:
        return MediaFileType.FANART;

      case BANNER:
        return MediaFileType.BANNER;

      case POSTER:
        return MediaFileType.POSTER;

      case SEASON:
        return MediaFileType.SEASON_POSTER;

      case THUMB:
        return MediaFileType.THUMB;

      case CLEARART:
        return MediaFileType.CLEARART;

      case LOGO:
        return MediaFileType.LOGO;

      case DISC:
        return MediaFileType.DISCART;

      default:
        return MediaFileType.GRAPHIC;
    }
  }

  /**
   * get the corresponding artwork type for the file type
   *
   * @param fileType
   *          the file type
   * @return the artwork type
   */
  public static MediaArtworkType getMediaArtworkType(MediaFileType fileType) {
    switch (fileType) {
      case FANART:
        return MediaArtworkType.BACKGROUND;

      case BANNER:
        return MediaArtworkType.BANNER;

      case POSTER:
        return MediaArtworkType.POSTER;

      case SEASON_POSTER:
        return MediaArtworkType.SEASON;

      case THUMB:
        return MediaArtworkType.THUMB;

      case CLEARART:
        return MediaArtworkType.CLEARART;

      case LOGO:
        return MediaArtworkType.LOGO;

      case DISCART:
        return MediaArtworkType.DISC;

      default:
        throw new IllegalStateException();
    }
  }
}
