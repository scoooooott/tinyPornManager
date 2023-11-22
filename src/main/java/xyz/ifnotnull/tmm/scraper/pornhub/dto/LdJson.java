package xyz.ifnotnull.tmm.scraper.pornhub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class LdJson {

  private String vkey;

  @JsonProperty("@context")
  private String context;

  @JsonProperty("@type")
  private String type;

  private String name;
  private String duration;
  private String thumbnailUrl;
  private String uploadDate;
  private String description;
  private String author;

  @JsonProperty("interactionStatistic")
  private List<InteractionCounter> interactionStatistic;

  // getters and setters

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDuration() {
    return duration;
  }

  public void setDuration(String duration) {
    this.duration = duration;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public String getUploadDate() {
    return uploadDate;
  }

  public void setUploadDate(String uploadDate) {
    this.uploadDate = uploadDate;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public List<InteractionCounter> getInteractionStatistic() {
    return interactionStatistic;
  }

  public void setInteractionStatistic(List<InteractionCounter> interactionStatistic) {
    this.interactionStatistic = interactionStatistic;
  }

  // Inner class for InteractionCounter
  public static class InteractionCounter {
    @JsonProperty("@type")
    private String type;

    private String interactionType;
    private String userInteractionCount;

    // getters and setters

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getInteractionType() {
      return interactionType;
    }

    public void setInteractionType(String interactionType) {
      this.interactionType = interactionType;
    }

    public String getUserInteractionCount() {
      return userInteractionCount;
    }

    public void setUserInteractionCount(String userInteractionCount) {
      this.userInteractionCount = userInteractionCount;
    }
  }
}
