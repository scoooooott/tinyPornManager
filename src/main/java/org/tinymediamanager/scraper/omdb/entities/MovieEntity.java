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

package org.tinymediamanager.scraper.omdb.entities;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class MovieEntity {

  @SerializedName("Title")
  public String            title;
  @SerializedName("Year")
  public String            year;
  @SerializedName("Rated")
  public String            rated;
  @SerializedName("Released")
  public String            released;
  @SerializedName("Runtime")
  public String            runtime;
  @SerializedName("Genre")
  public String            genre;
  @SerializedName("Director")
  public String            director;
  @SerializedName("Writer")
  public String            writer;
  @SerializedName("Actors")
  public String            actors;
  @SerializedName("Plot")
  public String            plot;
  @SerializedName("Language")
  public String            language;
  @SerializedName("Country")
  public String            country;
  @SerializedName("Awards")
  public String            awards;
  @SerializedName("Poster")
  public String            poster;
  @SerializedName("Metascore")
  public String            metascore;
  @SerializedName("imdbRating")
  public String            imdbRating;
  @SerializedName("imdbVotes")
  public String            imdbVotes;
  @SerializedName("imdbID")
  public String            imdbID;
  @SerializedName("Type")
  public String            type;
  @SerializedName("totalSeasons")
  public String            totalSeasons;
  @SerializedName("Response")
  public String            response;
  @SerializedName("Ratings")
  public List<MovieRating> ratings;
}
