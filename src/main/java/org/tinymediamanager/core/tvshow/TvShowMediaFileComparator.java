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
package org.tinymediamanager.core.tvshow;

import java.util.Comparator;

import org.tinymediamanager.core.entities.MediaFile;

/**
 * For logical sorting of mediafiles
 * 
 * @author Manuel Laggner
 */
public class TvShowMediaFileComparator implements Comparator<MediaFile> {

  @Override
  public int compare(MediaFile mf1, MediaFile mf2) {
    if (mf1.getType().ordinal() != mf2.getType().ordinal()) {
      return mf1.getType().ordinal() - mf2.getType().ordinal();
    }

    // starting from here we have the same MediaFileType
    // sort according to different criteria based on the type
    switch (mf1.getType()) {
      // sort all video files / season/extra artwork and by name
      case VIDEO:
      case SEASON_POSTER:
      case SEASON_BANNER:
      case SEASON_THUMB:
      case EXTRAFANART:
      case EXTRATHUMB:
        return mf1.getFilename().compareTo(mf2.getFilename());

      // sort the rest by filesize (descending)
      default:
        return Long.compare(mf2.getFilesize(), mf1.getFilesize());
    }
  }
}
