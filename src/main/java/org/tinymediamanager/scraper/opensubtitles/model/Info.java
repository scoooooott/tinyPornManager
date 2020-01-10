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
package org.tinymediamanager.scraper.opensubtitles.model;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author Myron Boyle
 */
public class Info {
  private double               seconds;
  private String               status;
  private ArrayList<MovieInfo> movieInfo = new ArrayList<>();

  public Info(Map<String, Object> response) throws Exception {
    this.seconds = (Double) response.get("seconds");
    this.status = (String) response.get("status");

    Object[] data = (Object[]) response.get("data");
    if (data != null) {
      for (int i = 0; i < data.length; i++) {
        movieInfo.add(new MovieInfo(data[i]));
      }
    }
  }

  public ArrayList<MovieInfo> getMovieInfo() {
    return movieInfo;
  }

  public double getSeconds() {
    return seconds;
  }

  public String getStatus() {
    return status;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public static class MovieInfo {
    public String id               = "";
    public String movieKind        = "";
    public String movieTitle       = "";
    public String movieReleaseName = "";
    public String subFormat        = "";
    public String subDownloadLink  = "";
    public Float  subRating        = 0f;
    public String zipDownloadLink  = "";
    public String season           = "";
    public String episode          = "";

    @SuppressWarnings("unchecked")
    public MovieInfo(Object data) throws Exception {
      Map<String, Object> values = (Map<String, Object>) data;
      this.id = (String) values.get("IDSubtitleFile");
      this.movieKind = (String) values.get("MovieKind");
      this.movieTitle = (String) values.get("MovieName");
      this.movieReleaseName = (String) values.get("MovieReleaseName");
      this.subFormat = (String) values.get("SubFormat");
      this.subDownloadLink = (String) values.get("SubDownloadLink");
      this.subRating = Float.parseFloat((String) values.get("SubRating"));
      this.zipDownloadLink = (String) values.get("ZipDownloadLink");
      this.season = (String) values.get("SeriesSeason");
      this.episode = (String) values.get("SeriesEpisode");
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
  }
}
