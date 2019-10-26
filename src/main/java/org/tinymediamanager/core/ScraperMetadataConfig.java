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
