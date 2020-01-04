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

package org.tinymediamanager.core.tvshow.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.w3c.dom.Element;

/**
 * the class TvShowToKodiConnector is used to write a the most recent Kodi compatible NFO file
 *
 * @author Manuel Laggner
 */
public class TvShowToKodiConnector extends TvShowGenericXmlConnector {
  private static final Logger LOGGER = LoggerFactory.getLogger(TvShowToKodiConnector.class);

  public TvShowToKodiConnector(TvShow tvShow) {
    super(tvShow);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  /**
   * write the new rating style<br />
   * <ratings> <rating name="default" max="10" default="true"> <value>5.800000</value> <votes>2100</votes> </rating> <rating name="imdb">
   * <value>8.9</value> <votes>12345</votes> </rating> </ratings>
   */
  @Override
  protected void addRating() {
    Element ratings = document.createElement("ratings");

    for (MediaRating r : tvShow.getRatings().values()) {
      // skip user ratings here
      if (MediaRating.USER.equals(r.getId())) {
        continue;
      }

      Element rating = document.createElement("rating");
      rating.setAttribute("name", r.getId());
      rating.setAttribute("max", String.valueOf(r.getMaxValue()));

      MediaRating mainMediaRating = tvShow.getRating();
      rating.setAttribute("default", r == mainMediaRating ? "true" : "false");

      Element value = document.createElement("value");
      value.setTextContent(Float.toString(r.getRating()));
      rating.appendChild(value);

      Element votes = document.createElement("votes");
      votes.setTextContent(Integer.toString(r.getVotes()));
      rating.appendChild(votes);

      ratings.appendChild(rating);
    }

    root.appendChild(ratings);
  }

  /**
   * votes are now in the ratings tag
   */
  @Override
  protected void addVotes() {
  }

  @Override
  protected void addOwnTags() {

  }
}
