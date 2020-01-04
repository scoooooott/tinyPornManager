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
package org.tinymediamanager.scraper.util.youtube.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * This is the MediaDetails class that holds the Information for the given Youtube ID
 *
 * @author Wolfgang Janes
 */
public class MediaDetails {

  private String  videoId;
  private String  title;
  private int     lengthSeconds;
  private String  channelId;
  private boolean isOwnerViewing;
  private String  shortDescription;
  private Boolean isCrawlable;
  private double  averageRating;
  private String  author;
  private int     viewCount;

  public MediaDetails(String videoId) {

    this.videoId = videoId;
    this.title = videoId;

  }

  public void setDetails(JsonNode mediaDetails) {

    this.videoId = mediaDetails.get("videoId").asText();
    this.title = mediaDetails.get("title").asText();
    this.lengthSeconds = mediaDetails.get("lengthSeconds").asInt();
    this.channelId = mediaDetails.get("channelId").asText();
    this.isOwnerViewing = mediaDetails.get("isOwnerViewing").asBoolean();
    this.shortDescription = mediaDetails.get("shortDescription").asText();
    this.isCrawlable = mediaDetails.get("isCrawlable").asBoolean();
    this.averageRating = mediaDetails.get("averageRating").asDouble();
    this.author = mediaDetails.get("author").asText();
    this.viewCount = mediaDetails.get("viewCount").asInt();

  }

  public String getVideoId() {
    return videoId;
  }

  public String getTitle() {
    return title;
  }

  public int getLengthSeconds() {
    return lengthSeconds;
  }

  public String getChannelId() {
    return channelId;
  }

  public boolean isOwnerViewing() {
    return isOwnerViewing;
  }

  public String getShortDescription() {
    return shortDescription;
  }

  public Boolean getCrawlable() {
    return isCrawlable;
  }

  public double getAverageRating() {
    return averageRating;
  }

  public String getAuthor() {
    return author;
  }

  public int getViewCount() {
    return viewCount;
  }
}
