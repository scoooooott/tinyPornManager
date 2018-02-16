/*
 * Copyright 2012 - 2018 Manuel Laggner
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

package org.tinymediamanager.core.entities;

import org.tinymediamanager.scraper.entities.MediaRating;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class Rating. This class represents _one_ rating for an MediaEntity
 *
 * @author Manuel Laggner
 */
public class Rating {
  public static final String NFO      = "NFO";
  public static final String DEFAULT  = "default";
  public static final String USER     = "user";

  @JsonProperty
  private String             id       = "";
  @JsonProperty
  private float              rating   = 0;
  @JsonProperty
  private int                votes    = 0;
  @JsonProperty
  private int                maxValue = 10;

  /**
   * JSON constructor - please do not use
   */
  public Rating() {
  }

  public Rating(MediaRating mediaRating) {
    this.id = mediaRating.getId();
    this.rating = mediaRating.getRating();
    this.votes = mediaRating.getVoteCount();
    this.maxValue = mediaRating.getMaxValue();
  }

  /**
   * copy constructor
   * 
   * @param source
   *          the source rating
   */
  public Rating(Rating source) {
    this(source.id, source.rating, source.votes, source.maxValue);
  }

  public Rating(String id, float rating) {
    this.id = id;
    this.rating = rating;
  }

  public Rating(String id, float rating, int votes) {
    this.id = id;
    this.rating = rating;
    this.votes = votes;
  }

  public Rating(String id, float rating, int votes, int maxValue) {
    this.id = id;
    this.rating = rating;
    this.votes = votes;
    this.maxValue = maxValue;
  }

  public Rating(String id, double rating, int votes, int maxValue) {
    this.id = id;
    this.rating = (float) rating;
    this.votes = votes;
    this.maxValue = maxValue;
  }

  public String getId() {
    return id;
  }

  public float getRating() {
    return rating;
  }

  public void setRating(float rating) {
    this.rating = rating;
  }

  public int getVotes() {
    return votes;
  }

  public void setVotes(int votes) {
    this.votes = votes;
  }

  public int getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(int maxValue) {
    this.maxValue = maxValue;
  }
}
