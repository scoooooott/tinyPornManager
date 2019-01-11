/*
 * Copyright 2012 - 2019 Manuel Laggner
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

package org.tinymediamanager.scraper.entities;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * The class MediaRating is used to store the new type of ratings (multiple rating from different sources)
 *
 * @author Manuel Laggner
 * @since 3.0
 */
public class MediaRating {
  private String id        = "";
  private float  rating    = 0;
  private int    voteCount = 0;
  private int    maxValue  = 10;

  public MediaRating(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return StrgUtils.getNonNullString(id);
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

  public int getVoteCount() {
    return voteCount;
  }

  public void setVoteCount(int voteCount) {
    this.voteCount = voteCount;
  }

  public int getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(int maxValue) {
    this.maxValue = maxValue;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + maxValue;
    result = prime * result + Float.floatToIntBits(rating);
    result = prime * result + voteCount;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MediaRating other = (MediaRating) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    if (maxValue != other.maxValue)
      return false;
    if (Float.floatToIntBits(rating) != Float.floatToIntBits(other.rating))
      return false;
    if (voteCount != other.voteCount)
      return false;
    return true;
  }

}
