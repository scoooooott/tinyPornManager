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

import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;

/**
 * various MediaFileTypes
 * 
 * @author Myron Boyle
 */
public enum MediaFileType {

  // ordering of list = ordering of type in GUI ;)
  // do not forget to add new items to generic methods in this class!!!

  // @formatter:off
  VIDEO, 
  TRAILER,
  SAMPLE, // sample != trailer
  @Deprecated
  VIDEO_EXTRA,
  AUDIO,
  SUBTITLE, 
  NFO, 
  POSTER, // gfx
  FANART, // gfx 
  BANNER, // gfx
  CLEARART, // gfx
  DISC, // gfx
  LOGO, // gfx
  CLEARLOGO, // gfx
  THUMB, // gfx
  CHARACTERART, //gfx
  KEYART, // gfx
  SEASON_POSTER, // gfx
  SEASON_BANNER, // gfx
  SEASON_THUMB, // gfx
  EXTRAFANART, // gfx
  EXTRATHUMB, // gfx
  EXTRA,
  GRAPHIC, // NO gfx (since not a searchable type)
  MEDIAINFO, // xxx-mediainfo.xml
  VSMETA, // xxx.ext.vsmeta Synology
  THEME, // "theme" files for some skins, like theme.mp3 (or bg video)
  TEXT, // various text infos, like BDinfo.txt or others...
  DOUBLE_EXT, // the filename startsWith video filename (and added extension), so we keep them...
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

      case SEASON_POSTER:
        return MediaFileType.SEASON_POSTER;

      case SEASON_BANNER:
        return MediaFileType.SEASON_BANNER;

      case SEASON_THUMB:
        return MediaFileType.SEASON_THUMB;

      case THUMB:
        return MediaFileType.THUMB;

      case CLEARART:
        return MediaFileType.CLEARART;

      case LOGO:
        return MediaFileType.LOGO;

      case CLEARLOGO:
        return MediaFileType.CLEARLOGO;

      case CHARACTERART:
        return MediaFileType.CHARACTERART;

      case DISC:
        return MediaFileType.DISC;

      case KEYART:
        return MediaFileType.KEYART;

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
        return MediaArtworkType.SEASON_POSTER;

      case SEASON_BANNER:
        return MediaArtworkType.SEASON_BANNER;

      case SEASON_THUMB:
        return MediaArtworkType.SEASON_THUMB;

      case THUMB:
        return MediaArtworkType.THUMB;

      case CLEARART:
        return MediaArtworkType.CLEARART;

      case LOGO:
        return MediaArtworkType.LOGO;

      case CLEARLOGO:
        return MediaArtworkType.CLEARLOGO;

      case CHARACTERART:
        return MediaArtworkType.CHARACTERART;

      case DISC:
        return MediaArtworkType.DISC;

      case KEYART:
        return MediaArtworkType.KEYART;

      default:
        throw new IllegalStateException();
    }
  }

  /**
   * get all artwork types
   *
   * @return list of MediaFileTypes for artwork
   */
  public static MediaFileType[] getGraphicMediaFileTypes() {
    // @formatter:off
    return new MediaFileType[] {
        MediaFileType.FANART,
        MediaFileType.POSTER,
        MediaFileType.BANNER,
        MediaFileType.CLEARART,
        MediaFileType.DISC,
        MediaFileType.LOGO,
        MediaFileType.CLEARLOGO,
        MediaFileType.THUMB,
        MediaFileType.CHARACTERART,
        MediaFileType.KEYART,
        MediaFileType.SEASON_POSTER,
        MediaFileType.SEASON_BANNER,
        MediaFileType.SEASON_THUMB,
        MediaFileType.EXTRAFANART,
        MediaFileType.EXTRATHUMB
    };
    // @formatter:on
  }

}
