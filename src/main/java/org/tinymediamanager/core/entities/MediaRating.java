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

package org.tinymediamanager.core.entities;

import org.tinymediamanager.scraper.util.StrgUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class Rating. This class represents _one_ rating for an MediaEntity
 *
 * @author Manuel Laggner
 */
public class MediaRating {
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
  public MediaRating() {
  }

  /**
   * copy constructor
   * 
   * @param source
   *          the source rating
   */
  public MediaRating(MediaRating source) {
    this(source.id, source.rating, source.votes, source.maxValue);
  }

  public MediaRating(String id) {
    this.id = StrgUtils.getNonNullString(id);
  }

  public MediaRating(String id, float rating) {
    this(id);
    this.rating = rating;
  }

  public MediaRating(String id, float rating, int votes) {
    this(id, rating);
    this.votes = votes;
  }

  public MediaRating(String id, float rating, int votes, int maxValue) {
    this(id, rating, votes);
    if (maxValue > 0) {
      this.maxValue = maxValue;
    }
  }

  public MediaRating(String id, double rating, int votes, int maxValue) {
    this(id, (float) rating, votes);
    if (maxValue > 0) {
      this.maxValue = maxValue;
    }
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

  public void setRating(double rating) {
    this.rating = (float) rating;
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

  /**
   * get the rating normalized (0...10)
   *
   * @return the normalized rating
   */
  public float getRatingNormalized() {
    if (maxValue != 0) {
      return rating / maxValue * 10;
    }
    return 0;
  }

  /**
   * set the normalized rating; also sets the maxvalue to 10!
   *
   * @param rating
   *          the rating between 0 and 10
   */
  public void setRatingNormalized(float rating) {
    if (rating < 0 || rating > 10) {
      return;
    }
    this.rating = rating;
    this.maxValue = 10;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    return prime * result + ((id == null) ? 0 : id.hashCode());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    MediaRating other = (MediaRating) obj;
    if (id == null) {
      return other.id == null;
    }
    else {
      return id.equals(other.id);
    }
  }
}
