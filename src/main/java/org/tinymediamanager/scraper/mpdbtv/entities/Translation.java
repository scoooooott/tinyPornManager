package org.tinymediamanager.scraper.mpdbtv.entities;

import com.google.gson.annotations.SerializedName;

class Translation {

  @SerializedName("language_id")
  public String languageId;

  @SerializedName("language")
  public String language;

  @SerializedName("title")
  public String title;

  @SerializedName("tagline")
  public Object tagline;

  @SerializedName("plot")
  public Object plot;

}
