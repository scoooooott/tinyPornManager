package org.tinymediamanager.scraper.mpdbtv.entities;

import com.google.gson.annotations.SerializedName;

public class Release {

  @SerializedName("country_id")
  public String countryId;

  @SerializedName("country_name")
  public String countryName;

  @SerializedName("country_name_en")
  public String countryNameEn;

  @SerializedName("year")
  public Object year;

  @SerializedName("certification")
  public Object certification;

  @SerializedName("description")
  public Object description;
  
}
