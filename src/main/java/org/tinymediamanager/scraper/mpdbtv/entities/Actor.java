package org.tinymediamanager.scraper.mpdbtv.entities;

import com.google.gson.annotations.SerializedName;

public class Actor {

  @SerializedName("id")
  public Integer id;

  @SerializedName("name")
  public String name;

  @SerializedName("departement_id")

  public Integer departementId;
  @SerializedName("departement")

  public String departement;
  @SerializedName("role")

  public String role;
  @SerializedName("language_id")

  public String languageId;
  @SerializedName("thumb")

  public String thumb;

}
