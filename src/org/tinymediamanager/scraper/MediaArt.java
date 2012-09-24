package org.tinymediamanager.scraper;

import java.io.IOException;
import java.io.InputStream;

import org.tinymediamanager.scraper.util.CachedUrl;

public class MediaArt {
  private static final long serialVersionUID = 1L;
  private String            downloadUrl;
  private String            providerId;
  private MediaArtifactType type;
  private String            label;
  private int               season;

  public MediaArt() {
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  public MediaArtifactType getType() {
    return type;
  }

  public void setType(MediaArtifactType type) {
    this.type = type;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public int getSeason() {
    return season;
  }

  public void setSeason(int season) {
    this.season = season;
  }

  public InputStream getImageIS() {
    CachedUrl url;
    try {
      url = new CachedUrl(getDownloadUrl());
      return url.getInputStream(null, true);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();

    }
    return null;

  }

}
