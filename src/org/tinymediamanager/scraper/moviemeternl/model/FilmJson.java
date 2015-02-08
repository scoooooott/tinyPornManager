/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Myron Boyle
 */
public class FilmJson {

  @JsonProperty
  private int            id;
  @JsonProperty
  private String         url;
  @JsonProperty
  private int            year;
  @JsonProperty
  private String         imdb;
  @JsonProperty
  private String         title;
  @JsonProperty
  private String         display_title;
  @JsonProperty
  private String         alternative_title;
  @JsonProperty
  private String         plot;
  @JsonProperty
  private double         duration;
  @JsonProperty
  private int            votes_count;
  @JsonProperty
  private String         average;
  @JsonProperty
  private MMPosters      posters;
  @JsonProperty
  private List<String>   countries;
  @JsonProperty
  private List<String>   genres;
  @JsonProperty
  private List<MMActors> actors;
  @JsonProperty
  private List<String>   directors;

  public static class MMPosters {
    @JsonProperty
    private String thumb;
    @JsonProperty
    private String small;
    @JsonProperty
    private String regular;
    @JsonProperty
    private String large;

    public String getThumb() {
      return thumb;
    }

    public String getSmall() {
      return small;
    }

    public String getRegular() {
      return regular;
    }

    public String getLarge() {
      return large;
    }
  }

  public static class MMActors {
    @JsonProperty
    private String name;
    @JsonProperty
    private String voice;

    public String getName() {
      return name;
    }

    public String getVoice() {
      return voice;
    }
  }

  public int getId() {
    return id;
  }

  public String getUrl() {
    return url;
  }

  public int getYear() {
    return year;
  }

  public String getImdb() {
    return imdb;
  }

  public String getTitle() {
    return title;
  }

  public String getDisplay_title() {
    return display_title;
  }

  public String getAlternative_title() {
    return alternative_title;
  }

  public String getPlot() {
    return plot;
  }

  public double getDuration() {
    return duration;
  }

  public int getVotes_count() {
    return votes_count;
  }

  public String getAverage() {
    return average;
  }

  public MMPosters getPosters() {
    return posters;
  }

  public List<String> getCountries() {
    return countries;
  }

  public List<String> getGenres() {
    return genres;
  }

  public List<MMActors> getActors() {
    return actors;
  }

  public List<String> getDirectors() {
    return directors;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
