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

public class HDLogo {

  @SerializedName("id")
  public Integer         id;

  @SerializedName("user_id")
  public Integer         userId;

  @SerializedName("filename")
  public String          filename;

  @SerializedName("width")
  public Integer         width;

  @SerializedName("height")
  public Integer         height;

  @SerializedName("image_type")
  public Integer         imageType;

  @SerializedName("languages")
  public List<Languages> languages = null;

  @SerializedName("rating")
  public double          rating;

  @SerializedName("votes")
  public Integer         votes;

  @SerializedName("type")
  public String          type;

  @SerializedName("original")
  public String          original;

  @SerializedName("preview")
  public String          preview;

  @SerializedName("thumbnail")
  public String          thumbnail;

}
