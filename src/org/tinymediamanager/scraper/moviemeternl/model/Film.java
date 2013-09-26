/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.scraper.moviemeternl.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import redstone.xmlrpc.XmlRpcStruct;

/**
 * @author Myron Boyle
 */
public class Film {
  private String filmId;
  private String url;
  private String title;
  private String alternative_title;
  private String year;
  private String average;
  private String votes_count;
  private String similarity;

  public Film(XmlRpcStruct struct) {
    this.filmId = struct.getString("filmId");
    this.url = struct.getString("url");
    this.title = struct.getString("title");
    this.alternative_title = struct.getString("alternative_title");
    this.year = struct.getString("year");
    this.average = struct.getString("average");
    this.votes_count = struct.getString("votes_count");
    this.similarity = struct.getString("similarity");
  }

  public String getFilmId() {
    return this.filmId;
  }

  public String getUrl() {
    return this.url;
  }

  public String getTitle() {
    return this.title;
  }

  public String getAlternative_title() {
    return this.alternative_title;
  }

  public String getYear() {
    return this.year;
  }

  public String getAverage() {
    return this.average;
  }

  public String getVotes_count() {
    return this.votes_count;
  }

  public String getSimilarity() {
    return this.similarity;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
