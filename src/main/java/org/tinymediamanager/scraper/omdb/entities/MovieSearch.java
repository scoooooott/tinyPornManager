package org.tinymediamanager.scraper.omdb.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by wjanes on 10.02.2017.
 *
 * @author Wolfgang Janes
 */
public class MovieSearch {
  @SerializedName("Search")
  public List<MovieEntity> search = null;

  @SerializedName("totalResults")
  public String totalResults;

  @SerializedName("Response")
  public String response;
}
