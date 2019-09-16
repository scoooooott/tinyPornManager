package org.tinymediamanager.scraper.mpdbtv.entities;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class HDLogo {

  @SerializedName("id")
  public Integer id;

  @SerializedName("user_id")
  public Integer userId;

  @SerializedName("filename")
  public String filename;

  @SerializedName("width")
  public Integer width;

  @SerializedName("height")
  public Integer height;

  @SerializedName("image_type")
  public Integer imageType;

  @SerializedName("languages")
  public List<Languages> languages = null;

  @SerializedName("rating")
  public double rating;

  @SerializedName("votes")
  public Integer votes;

  @SerializedName("type")
  public String type;

  @SerializedName("original")
  public String original;

  @SerializedName("preview")
  public String preview;

  @SerializedName("thumbnail")
  public String thumbnail;

}
