package org.tinymediamanager.scraper.mpdbtv.entities;

import com.google.gson.annotations.SerializedName;

public class Trailer {

  @SerializedName("id")
  public Integer id;

  @SerializedName("url")
  public String url;

  @SerializedName("type")
  public String type;

  @SerializedName("quality")
  public String quality;


}
