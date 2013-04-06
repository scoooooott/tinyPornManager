/*
 * Copyright 2012 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.scraper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaCastMember.CastType;

/**
 * The Class MediaMetadata.
 * 
 * @author Manuel Laggner
 */
public class MediaMetadata {

  /** The provider id. */
  private String                  providerId;

  /** a hashmap storing other ids. */
  private HashMap<String, Object> ids               = new HashMap<String, Object>();

  /** The plot. */
  private String                  plot              = "";

  /** The title. */
  private String                  title             = "";

  /** The original title. */
  private String                  originalTitle     = "";

  /** The rating. */
  private double                  rating            = 0.0;

  /** The vote count. */
  private int                     voteCount         = 0;

  /** The runtime. */
  private int                     runtime           = 0;

  /** The tagline. */
  private String                  tagline           = "";

  /** The production company. */
  private String                  productionCompany = "";

  /** The year. */
  private String                  year              = "";

  /** The release date. */
  private String                  releaseDate       = "";

  /** The spoken languages. */
  private String                  spokenLanguages   = "";

  /** The cast members. */
  private List<MediaCastMember>   castMembers       = new ArrayList<MediaCastMember>();

  /** The fanart. */
  private List<MediaArtwork>      fanart            = new ArrayList<MediaArtwork>();

  /** The genres. */
  private List<MediaGenres>       genres            = new ArrayList<MediaGenres>();

  /** The certifications. */
  private List<Certification>     certifications    = new ArrayList<Certification>();

  /** The trailers. */
  private List<MediaTrailer>      trailers          = new ArrayList<MediaTrailer>();

  /**
   * Instantiates a new media metadata.
   * 
   * @param providerId
   *          the provider id
   */
  public MediaMetadata(String providerId) {
    this.providerId = providerId;
  }

  /**
   * Gets the provider id.
   * 
   * @return the provider id
   */
  public String getProviderId() {
    return providerId;
  }

  /**
   * Gets the tmdb id.
   * 
   * @return the tmdb id
   */
  public int getTmdbId() {
    try {
      return (Integer) ids.get("tmdbId");
    }
    catch (NumberFormatException e) {
      return 0;
    }
  }

  /**
   * Gets the tmdb id set.
   * 
   * @return the tmdb id set
   */
  public int getTmdbIdSet() {
    try {
      return (Integer) ids.get("tmdbIdSet");
    }
    catch (NumberFormatException e) {
      return 0;
    }
  }

  /**
   * Sets the tmdb id set.
   * 
   * @param tmdbIdSet
   *          the new tmdb id set
   */
  public void setTmdbIdSet(int tmdbIdSet) {
    ids.put("tmdbIdSet", tmdbIdSet);
  }

  /**
   * Gets the plot.
   * 
   * @return the plot
   */
  public String getPlot() {
    return plot;
  }

  /**
   * Gets the title.
   * 
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the original title.
   * 
   * @return the original title
   */
  public String getOriginalTitle() {
    return originalTitle;
  }

  /**
   * Gets the rating.
   * 
   * @return the rating
   */
  public double getRating() {
    return rating;
  }

  /**
   * Gets the runtime.
   * 
   * @return the runtime
   */
  public int getRuntime() {
    return runtime;
  }

  /**
   * Gets the tagline.
   * 
   * @return the tagline
   */
  public String getTagline() {
    return tagline;
  }

  /**
   * Sets the tmdb id.
   * 
   * @param tmdbId
   *          the new tmdb id
   */
  public void setTmdbId(int tmdbId) {
    ids.put("tmdbId", tmdbId);
  }

  /**
   * Gets the imdb id.
   * 
   * @return the imdb id
   */
  public String getImdbId() {
    return (String) ids.get("imdbId");
  }

  /**
   * Sets the imdb id.
   * 
   * @param imdbId
   *          the new imdb id
   */
  public void setImdbId(String imdbId) {
    ids.put("imdbId", imdbId);
  }

  /**
   * Sets the plot.
   * 
   * @param plot
   *          the new plot
   */
  public void setPlot(String plot) {
    this.plot = plot;
  }

  /**
   * Sets the title.
   * 
   * @param title
   *          the new title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Sets the original title.
   * 
   * @param originalTitle
   *          the new original title
   */
  public void setOriginalTitle(String originalTitle) {
    this.originalTitle = originalTitle;
  }

  /**
   * Sets the rating (range 0-10).
   * 
   * @param rating
   *          the new rating
   */
  public void setRating(double rating) {
    this.rating = rating;
  }

  /**
   * Gets the vote count.
   * 
   * @return the vote count
   */
  public int getVoteCount() {
    return voteCount;
  }

  /**
   * Sets the vote count.
   * 
   * @param voteCount
   *          the new vote count
   */
  public void setVoteCount(int voteCount) {
    this.voteCount = voteCount;
  }

  /**
   * Sets the runtime.
   * 
   * @param runtime
   *          the new runtime
   */
  public void setRuntime(int runtime) {
    this.runtime = runtime;
  }

  /**
   * Sets the tagline.
   * 
   * @param tagline
   *          the new tagline
   */
  public void setTagline(String tagline) {
    this.tagline = tagline;
  }

  /**
   * Gets the year.
   * 
   * @return the year
   */
  public String getYear() {
    return year;
  }

  /**
   * Gets the release date.
   * 
   * @return the release date
   */
  public String getReleaseDate() {
    return releaseDate;
  }

  /**
   * Sets the year.
   * 
   * @param year
   *          the new year
   */
  public void setYear(String year) {
    this.year = year;
  }

  /**
   * Sets the release date.
   * 
   * @param releaseDate
   *          the new release date
   */
  public void setReleaseDate(String releaseDate) {
    this.releaseDate = releaseDate;
  }

  /**
   * Gets the genres.
   * 
   * @return the genres
   */
  public List<MediaGenres> getGenres() {
    return genres;
  }

  /**
   * Gets the cast members.
   * 
   * @param type
   *          the type
   * @return the cast members
   */
  public List<MediaCastMember> getCastMembers(CastType type) {
    if (type == CastType.ALL)
      return castMembers;

    // get all castmember for the given type
    List<MediaCastMember> l = new ArrayList<MediaCastMember>(castMembers.size());
    for (MediaCastMember cm : castMembers) {
      if (cm.getType() == type)
        l.add(cm);
    }
    return l;
  }

  /**
   * Gets the media art.
   * 
   * @param type
   *          the type
   * @return the media art
   */
  public List<MediaArtwork> getMediaArt(MediaArtworkType type) {
    List<MediaArtwork> mediaArt = getFanart();
    if (mediaArt == null || type == MediaArtworkType.ALL)
      return mediaArt;

    // get all artwork
    List<MediaArtwork> l = new ArrayList<MediaArtwork>(mediaArt.size());
    for (MediaArtwork ma : mediaArt) {
      if (ma.getType() == type)
        l.add(ma);
    }
    return l;

  }

  /**
   * Gets the production company.
   * 
   * @return the production company
   */
  public String getProductionCompany() {
    return productionCompany;
  }

  /**
   * Sets the production company.
   * 
   * @param productionCompany
   *          the new production company
   */
  public void setProductionCompany(String productionCompany) {
    this.productionCompany = productionCompany;
  }

  /**
   * Sets the spoken languages.
   * 
   * @param spokenLanguages
   *          the new spoken languages
   */
  public void setSpokenLanguages(String spokenLanguages) {
    this.spokenLanguages = spokenLanguages;
  }

  /**
   * Gets the spoken languages.
   * 
   * @return the spoken languages
   */
  public String getSpokenLanguages() {
    return this.spokenLanguages;
  }

  /**
   * Adds the genre.
   * 
   * @param genre
   *          the genre
   */
  public void addGenre(MediaGenres genre) {
    genres.add(genre);
  }

  /**
   * Adds the cast member.
   * 
   * @param cm
   *          the cm
   */
  public void addCastMember(MediaCastMember cm) {
    if (containsCastMember(cm))
      return;
    castMembers.add(cm);

  }

  /**
   * Adds the media art.
   * 
   * @param ma
   *          the ma
   */
  public void addMediaArt(MediaArtwork ma) {
    fanart.add(ma);
  }

  /**
   * Clear media art.
   */
  public void clearMediaArt() {
    fanart.clear();
  }

  /**
   * Adds the media art.
   * 
   * @param art
   *          the art
   */
  public void addMediaArt(List<MediaArtwork> art) {
    fanart.addAll(art);
  }

  /**
   * Gets the cast members.
   * 
   * @return the cast members
   */
  public List<MediaCastMember> getCastMembers() {
    return castMembers;
  }

  /**
   * Gets the fanart.
   * 
   * @return the fanart
   */
  public List<MediaArtwork> getFanart() {
    return fanart;
  }

  /**
   * Contains cast member.
   * 
   * @param cm
   *          the cm
   * @return true, if successful
   */
  private boolean containsCastMember(MediaCastMember cm) {
    boolean found = false;
    if (castMembers != null) {
      for (MediaCastMember m : castMembers) {
        if (m.getType() == cm.getType() && (m.getName() != null && m.getName().equals(cm.getName()))) {
          found = true;
          break;
        }
      }
    }

    return found;
  }

  /**
   * Adds the certification.
   * 
   * @param certification
   *          the certification
   */
  public void addCertification(Certification certification) {
    certifications.add(certification);
  }

  /**
   * Gets the certifications.
   * 
   * @return the certifications
   */
  public List<Certification> getCertifications() {
    return certifications;
  }

  /**
   * Adds the trailer.
   * 
   * @param trailer
   *          the trailer
   */
  public void addTrailer(MediaTrailer trailer) {
    trailers.add(trailer);
  }

  /**
   * Gets the trailers.
   * 
   * @return the trailers
   */
  public List<MediaTrailer> getTrailers() {
    return trailers;
  }

  /**
   * Sets the id.
   * 
   * @param key
   *          the key
   * @param object
   *          the id
   */
  public void setId(String key, Object object) {
    ids.put(key, object);
  }

  /**
   * Gets the id.
   * 
   * @param key
   *          the key
   * @return the id
   */
  public Object getId(String key) {
    return ids.get(key);
  }

  /**
   * Gets the ids.
   * 
   * @return the ids
   */
  public HashMap<String, Object> getIds() {
    return ids;
  }

  /**
   * <p>
   * Uses <code>ReflectionToStringBuilder</code> to generate a <code>toString</code> for the specified object.
   * </p>
   * 
   * @return the String result
   * @see ReflectionToStringBuilder#toString(Object)
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
