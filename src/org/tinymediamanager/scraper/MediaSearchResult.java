package org.tinymediamanager.scraper;

import java.util.HashMap;
import java.util.Map;

import org.tinymediamanager.scraper.util.StringUtils;

public class MediaSearchResult implements Comparable<MediaSearchResult> {
  private static final long   serialVersionUID = 2L;

  private String              providerId;
  private String              url;
  private String              title;
  private String              year;
  private String              originalTitle;
  private String              id;
  private float               score;

  private Map<String, String> extraArgs        = new HashMap<String, String>();
  private String              imdbId;
  private MediaMetadata       metadata         = null;

  private MediaType           type;

  public MediaSearchResult() {
  }

  public String getOriginalTitle() {
    return originalTitle;
  }

  public void setOriginalTitle(String originalTitle) {
    this.originalTitle = originalTitle;
  }

  public MediaSearchResult(String providerId, MediaType type, float score) {
    this.providerId = providerId;
    this.type = type;
    this.score = score;
  }

  public MediaSearchResult(String providerId, String id, String title, String year, float score) {
    super();
    this.providerId = providerId;
    this.id = id;
    this.title = title;
    this.year = year;
    this.score = score;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public float getScore() {
    return score;
  }

  public void setScore(float score) {
    this.score = score;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void addExtraArg(String key, String value) {
    this.extraArgs.put(key, value);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("MediaSearchResult [extraArgs=");
    builder.append(StringUtils.mapToString(extraArgs));
    builder.append(", id=");
    builder.append(id);
    builder.append(", imdbId=");
    builder.append(imdbId);
    builder.append(", metadata=");
    builder.append(metadata);
    builder.append(", providerId=");
    builder.append(providerId);
    builder.append(", score=");
    builder.append(score);
    builder.append(", title=");
    builder.append(title);
    builder.append(", originalTitle=");
    builder.append(originalTitle);
    builder.append(", type=");
    builder.append(type);
    builder.append(", url=");
    builder.append(url);
    builder.append(", year=");
    builder.append(year);
    builder.append("]");
    return builder.toString();
  }

  public MediaType getMediaType() {
    return type;
  }

  public void setMediaType(MediaType type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getIMDBId() {
    return imdbId;
  }

  public void setIMDBId(String imdbid) {
    this.imdbId = imdbid;
  }

  public Map<String, String> getExtra() {
    return extraArgs;
  }

  public MediaMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(MediaMetadata md) {
    this.metadata = md;
  }

  @Override
  public int compareTo(MediaSearchResult arg0) {
    if (getScore() < arg0.getScore()) {
      return -1;
    }
    else if (getScore() == arg0.getScore()) {
      return 0;
    }
    else {
      return 1;
    }

  }
}
