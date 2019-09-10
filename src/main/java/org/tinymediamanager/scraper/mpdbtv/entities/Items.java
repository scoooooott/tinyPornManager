package org.tinymediamanager.scraper.mpdbtv.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Items {

  @SerializedName("item")
  List<SearchEntity> item;

}
