package org.tinymediamanager.scraper.mpdbtv.entities;

import com.google.gson.annotations.SerializedName;

public class Languages {

  @SerializedName("id")
  public String id;

  @SerializedName("language")
  public String language;

  @SerializedName("country_id")
  public String country_id;

  @SerializedName("country")
  public String country;

  @SerializedName("country_en")
  public String country_en;

}
