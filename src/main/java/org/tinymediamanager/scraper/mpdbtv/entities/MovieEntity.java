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
  public Integer budget;

  @SerializedName("saga_id")
  public Integer sagaId;

  @SerializedName("saga_order")
  public Integer sagaOrder;

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
  public String idTmdb;

  @SerializedName("title")
  public String title;

  @SerializedName("tagline")
  public String tagline;

  @SerializedName("plot")
  public String plot;

  @SerializedName("translations")
  public List<Translation> translations = null;

  @SerializedName("firstRelease")
  public Integer firstRelease;

  @SerializedName("releases")
  public List<Release> releases = null;


  @SerializedName("genres")
  public List<Genre> genres = null;

  @SerializedName("studios")
  public List<Studio> studios = null;

  @SerializedName("directors")
  public List<Director> directors = null;

  @SerializedName("producers")
  public List<Producer> producers = null;

  @SerializedName("actors")
  public List<Actor> actors = null;

  @SerializedName("posters")
  public List<Poster> posters = null;

  @SerializedName("fanarts")
  public List<Fanart> fanarts = null;

  @SerializedName("discarts")
  public List<DiscArt> discarts = null;

  @SerializedName("hdlogos")
  public List<HDLogo> hdlogos = null;

  @SerializedName("hdcleararts")
  public List<HDClearArt> hdcleararts = null;

  @SerializedName("trailers")
  public List<Trailer> trailers = null;
  
}