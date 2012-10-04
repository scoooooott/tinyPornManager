package org.tinymediamanager.scraper.tmdb;

import org.tinymediamanager.scraper.MediaArtifactType;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.FanartSizes;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.PosterSizes;

public class TmdbArtwork {

  private MediaArtifactType type;
  private String baseUrl;
  private String filePath;

  public TmdbArtwork(MediaArtifactType type, String baseUrl, String filePath) {
    this.type = type;
    this.baseUrl = baseUrl;
    this.filePath = filePath;
  }

  public MediaArtifactType getType() {
    return type;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public String getFilePath() {
    return filePath;
  }

  public String getUrlForSmallArtwork() {
    String url = null;

    switch (type) {
      case POSTER:
        url = baseUrl + "w154" + filePath;
        break;

      case BACKGROUND:
        url = baseUrl + "w300" + filePath;
        break;
    }

    return url;

  }

  public String getUrlForMediumArtwork() {
    String url = null;

    switch (type) {
      case POSTER:
        url = baseUrl + "w342" + filePath;
        break;

      case BACKGROUND:
        url = baseUrl + "w780" + filePath;
        break;
    }

    return url;

  }

  public String getUrlForOriginalArtwork() {
    String url = baseUrl + "original" + filePath;
    return url;

  }

  public String getUrlForSpecialArtwork(String size) {
    String url = baseUrl + size + filePath;
    return url;
  }

  public String getUrlForSpecialArtwork(PosterSizes size) {
    return getUrlForSpecialArtwork(size.name());
  }

  public String getUrlForSpecialArtwork(FanartSizes size) {
    return getUrlForSpecialArtwork(size.name());
  }
}
