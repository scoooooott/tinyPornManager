package org.tinymediamanager.scraper.mpdbtv.entities;

import com.google.gson.annotations.SerializedName;

public class SearchEntity {

  @SerializedName("id")
  public String id;

  @SerializedName("original_title")
  public String original_title;

  @SerializedName("title")
  public String title;

  @SerializedName("year")
  public int year;

  @SerializedName("id_allocine")
  public String id_allocine;

  @SerializedName("id_imdb")
  public String id_imdb;

  @SerializedName("url")
  public String url;

  @SerializedName("posterUrl")
  public String posterUrl;


}
