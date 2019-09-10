package org.tinymediamanager.scraper.mpdbtv.entities;

import com.google.gson.annotations.SerializedName;

public class Genre {

  @SerializedName("id")
  public Integer id;

  @SerializedName("mediatype")
  public String mediatype;

  @SerializedName("name")
  public String name;
  
}
