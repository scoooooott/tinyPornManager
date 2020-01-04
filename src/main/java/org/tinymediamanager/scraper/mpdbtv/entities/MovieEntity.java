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
package org.tinymediamanager.scraper.mpdbtv.entities;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class MovieEntity {

  @SerializedName("id")
  public Integer           id;

  @SerializedName("original_title")
  public String            originalTitle;

  @SerializedName("runtime")
  public Integer           runtime;

  @SerializedName("budget")
  public Integer           budget;

  @SerializedName("saga_id")
  public Integer           sagaId;

  @SerializedName("saga_order")
  public Integer           sagaOrder;

  @SerializedName("views")
  public Integer           views;

  @SerializedName("created_at")
  public String            createdAt;

  @SerializedName("updated")
  public String            updated;

  @SerializedName("rating")
  public Double            rating;

  @SerializedName("rating_votes")
  public Integer           ratingVotes;

  @SerializedName("created_by")
  public Integer           createdBy;

  @SerializedName("status")
  public String            status;

  @SerializedName("pendingMod")
  public Integer           pendingMod;

  @SerializedName("id_allocine")
  public String            idAllocine;

  @SerializedName("id_imdb")
  public String            idImdb;

  @SerializedName("id_tmdb")
  public String            idTmdb;

  @SerializedName("title")
  public String            title;

  @SerializedName("tagline")
  public String            tagline;

  @SerializedName("plot")
  public String            plot;

  @SerializedName("translations")
  public List<Translation> translations = null;

  @SerializedName("firstRelease")
  public Integer           firstRelease;

  @SerializedName("releases")
  public List<Release>     releases     = null;

  @SerializedName("genres")
  public List<Genre>       genres       = null;

  @SerializedName("studios")
  public List<Studio>      studios      = null;

  @SerializedName("directors")
  public List<Director>    directors    = null;

  @SerializedName("producers")
  public List<Producer>    producers    = null;

  @SerializedName("actors")
  public List<Actor>       actors       = null;

  @SerializedName("posters")
  public List<Poster>      posters      = null;

  @SerializedName("fanarts")
  public List<Fanart>      fanarts      = null;

  @SerializedName("discarts")
  public List<DiscArt>     discarts     = null;

  @SerializedName("hdlogos")
  public List<HDLogo>      hdlogos      = null;

  @SerializedName("hdcleararts")
  public List<HDClearArt>  hdcleararts  = null;

  @SerializedName("trailers")
  public List<Trailer>     trailers     = null;

}
