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
package org.tinymediamanager.scraper;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * The class SubtitleSearchResult.
 * 
 * @author Manuel Laggner
 * @since 2.0
 */
public class SubtitleSearchResult implements Comparable<SubtitleSearchResult> {
  private String providerId;
  private String id          = "";
  private String title       = "";
  private String releaseName = "";
  private String url         = "";
  private float  score       = 0f;
  private float  rating      = 0f;

  public SubtitleSearchResult(String providerId) {
    this.providerId = providerId;
  }

  public SubtitleSearchResult(String providerId, float score) {
    this.providerId = providerId;
    this.score = score;
  }

  /**
   * Get the id of this search result
   * 
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Set the id of this search result
   * 
   * @param id
   *          the search result id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get the release name of this subtitle
   * 
   * @return the release name
   */
  public String getReleaseName() {
    return releaseName;
  }

  /**
   * Set the release name of this subtitle
   * 
   * @param releaseName
   *          the release name
   */
  public void setReleaseName(String releaseName) {
    this.releaseName = StrgUtils.getNonNullString(releaseName);
  }

  /**
   * Get the provider id
   * 
   * @return the provider id
   */
  public String getProviderId() {
    return providerId;
  }

  /**
   * Set the provider id
   * 
   * @param providerId
   *          the provider id
   */
  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  /**
   * Get the title of this search result
   * 
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set the title of this search result
   * 
   * @param title
   *          the title
   */
  public void setTitle(String title) {
    this.title = StrgUtils.getNonNullString(title);
  }

  /**
   * Get the score of this search result. 1.0 is perfect match
   * 
   * @return the score
   */
  public float getScore() {
    return score;
  }

  /**
   * Set the score of this result
   * 
   * @param score
   *          the result
   */
  public void setScore(float score) {
    this.score = score;
  }

  /**
   * Set the score of this result (nullsafe)
   *
   * @param score
   *          the result
   */
  public void setScore(Float score) {
    if (score != null) {
      setScore(score.floatValue());
    }
  }

  /**
   * Get the url to this search result
   * 
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Set the url to this search result
   * 
   * @param url
   *          the url
   */
  public void setUrl(String url) {
    this.url = StrgUtils.getNonNullString(url);
  }

  /**
   * Get the rating of this search result
   *
   * @return the rating
   */
  public float getRating() {
    return rating;
  }

  /**
   * Set the rating to this search result
   *
   * @param rating
   *          the rating
   */
  public void setRating(float rating) {
    this.rating = rating;
  }

  @Override
  public int compareTo(SubtitleSearchResult arg0) {
    if (getScore() < arg0.getScore()) {
      return -1;
    }
    else if (getScore() == arg0.getScore() && getRating() < arg0.getRating()) {
      return -1;
    }
    else if (getScore() == arg0.getScore() && getRating() == arg0.getRating()) {
      return 0;
    }
    else {
      return 1;
    }
  }

  /**
   * <p>
   * Uses <code>ReflectionToStringBuilder</code> to generate a <code>toString</code> for the specified object.
   * </p>
   * 
   * @return the String result
   * @see ReflectionToStringBuilder#toString(Object)
   */
  @Override
  public String toString() {
    return (new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)).toString();
  }
}
