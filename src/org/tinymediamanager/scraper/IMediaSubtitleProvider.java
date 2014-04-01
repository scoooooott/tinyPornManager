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
package org.tinymediamanager.scraper;

import java.util.List;

import org.tinymediamanager.core.entities.MediaFile;

/**
 * The Interface IMediaSubtitleProvider.
 * 
 * @author Myron Boyle
 */
public interface IMediaSubtitleProvider {

  /**
   * Gets the info.
   * 
   * @return the provider info containing metadata of the provider
   */
  public MediaProviderInfo getProviderInfo();

  /**
   * searches for subtitles for MediaFile
   * 
   * @param mf
   *          the MediaFile
   * @return the MediaSearchResults
   * @throws Exception
   *           the exception
   */
  public List<MediaSearchResult> search(MediaFile mf) throws Exception;

  /**
   * download subtitle
   * 
   * @param hash
   *          the SubDB file hash (MSR.getId())
   * @param language
   *          2char language string (2-char locale)
   */
  public void download(String hash, String language);
}
