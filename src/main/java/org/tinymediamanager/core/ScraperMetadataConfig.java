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

public interface ScraperMetadataConfig {
  public enum Type {
    METADATA,
    CAST,
    ARTWORK
  }

  Type getType();

  String getDescription();

  String getToolTip();

  default boolean isMetaData() {
    return getType() == Type.METADATA;
  }

  default boolean isArtwork() {
    return getType() == Type.ARTWORK;
  }

  default boolean isCast() {
    return getType() == Type.CAST;
  }

  /**
   * check if there is at least one metadata field set
   *
   * @param config
   *          a {@link List} of all set fields
   * @return true/false
   */
  static boolean containsAnyMetadata(List<? extends ScraperMetadataConfig> config) {
    for (ScraperMetadataConfig field : config) {
      if (field.isMetaData()) {
        return true;
      }
    }
    return false;
  }

  /**
   * check if there is at least one artwork field set
   *
   * @param config
   *          a {@link List} of all set fields
   * @return true/false
   */
  static boolean containsAnyArtwork(List<? extends ScraperMetadataConfig> config) {
    for (ScraperMetadataConfig field : config) {
      if (field.isArtwork()) {
        return true;
      }
    }
    return false;
  }

  /**
   * check if there is at least one cast field set
   *
   * @param config
   *          a {@link List} of all set fields
   * @return true/false
   */
  static boolean containsAnyCast(List<? extends ScraperMetadataConfig> config) {
    for (ScraperMetadataConfig field : config) {
      if (field.isCast()) {
        return true;
      }
    }
    return false;
  }
}
