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
package org.tinymediamanager.core.tasks;

import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaTrailer;

/**
 * A task for downloading trailers
 *
 * @author Manuel Laggner
 */
public class TrailerDownloadTask extends DownloadTask {

  public TrailerDownloadTask(MediaTrailer trailer, MediaEntity mediaEntity, String filename) {
    super(trailer.getUrl(), mediaEntity.getPathNIO().resolve(filename), mediaEntity, MediaFileType.TRAILER);

    if ("apple".equalsIgnoreCase(trailer.getProvider())) {
      setSpecialUserAgent("QuickTime");
    }
  }
}
