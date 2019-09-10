package org.tinymediamanager.scraper.mpdbtv.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MovieEntity {


  @SerializedName("id")
  public Integer id;

  @SerializedName("original_title")
  public String originalTitle;

  @SerializedName("runtime")
  public Integer runtime;

  @SerializedName("budget")
  public Object budget;

  @SerializedName("saga_id")
  public Object sagaId;

  @SerializedName("saga_order")
  public Object sagaOrder;

  @SerializedName("views")
  public Integer views;

  @SerializedName("created_at")
  public String createdAt;

  @SerializedName("updated")
  public String updated;

  @SerializedName("rating")
  public Double rating;

  @SerializedName("rating_votes")
  public Integer ratingVotes;

  @SerializedName("created_by")
  public Integer createdBy;

  @SerializedName("status")
  public String status;

  @SerializedName("pendingMod")
  public Integer pendingMod;

  @SerializedName("id_allocine")
  public String idAllocine;

  @SerializedName("id_imdb")
  public String idImdb;

  @SerializedName("id_tmdb")
  public Object idTmdb;

  @SerializedName("title")
  public String title;

  @SerializedName("tagline")
  public Object tagline;

  @SerializedName("plot")
  public String plot;

  @SerializedName("translations")
  public List<Translation> translations = null;

  @SerializedName("firstRelease")
  public Integer firstRelease;

  @SerializedName("releases")
  public List<Release> releases = null;

  @SerializedName("countries")
  public List<Object> countries = null;

  @SerializedName("genres")
  public List<Genre> genres = null;

  @SerializedName("studios")
  public List<Studio> studios = null;

  @SerializedName("saga")
  public Object saga;

  @SerializedName("sorttitle")
  public Object sorttitle;

  @SerializedName("directors")
  public List<Director> directors = null;

  @SerializedName("producers")
  public List<Producer> producers = null;

  @SerializedName("actors")
  public List<Actor> actors = null;

  @SerializedName("posters")
  public List<Poster> posters = null;

  @SerializedName("fanarts")
  public List<Object> fanarts = null;

  @SerializedName("discarts")
  public List<Object> discarts = null;

  @SerializedName("hdlogos")
  public List<Object> hdlogos = null;

  @SerializedName("hdcleararts")
  public List<Object> hdcleararts = null;

  @SerializedName("trailers")
  public List<Object> trailers = null;
  
}