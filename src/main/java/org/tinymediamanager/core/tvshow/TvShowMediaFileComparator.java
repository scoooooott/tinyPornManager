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
package org.tinymediamanager.core.tvshow;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.tinymediamanager.core.entities.MediaFile;

/**
 * For logical sorting of mediafiles
 * 
 * @author Manuel Laggner
 */
public class TvShowMediaFileComparator implements Comparator<MediaFile> {

  private List<String> discImageTypes = Arrays.asList("iso", "bin", "dat", "img", "nrg", "disc");

  @Override
  public int compare(MediaFile mf1, MediaFile mf2) {
    if (mf1.getType().ordinal() != mf2.getType().ordinal()) {
      return mf1.getType().ordinal() - mf2.getType().ordinal();
    }

    // sort disc image types to the end
    if (discImageTypes.contains(mf1.getExtension()) && !discImageTypes.contains(mf2.getExtension())) {
      return 1;
    }

    if (!discImageTypes.contains(mf1.getExtension()) && discImageTypes.contains(mf2.getExtension())) {
      return -1;
    }

    return mf1.getFilename().compareTo(mf2.getFilename());
  }
}
