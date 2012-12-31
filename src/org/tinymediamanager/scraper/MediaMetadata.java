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
import java.util.List;

import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaCastMember.CastType;

/**
 * The Class MediaMetadata.
 */
public class MediaMetadata {

  private String                providerId;

  private String                imdbId            = "";
  private int                   tmdbId            = 0;
  private String                plot              = "";
  private String                title             = "";
  private String                originalTitle     = "";
  private double                rating            = 0.0;
  private int                   voteCount         = 0;
  private int                   runtime           = 0;
  private String                tagline           = "";
  private String                productionCompany = "";
  private String                year              = "";
  private String                releaseDate       = "";

  /** The cast members. */
  private List<MediaCastMember> castMembers       = new ArrayList<MediaCastMember>();

  /** The fanart. */
  private List<MediaArtwork>    fanart            = new ArrayList<MediaArtwork>();

  /** The genres. */
  private List<MediaGenres>     genres            = new ArrayList<MediaGenres>();

  /** The certifications. */
  private List<Certification>   certifications    = new ArrayList<Certification>();

  /** The trailers. */
  private List<MediaTrailer>    trailers          = new ArrayList<MediaTrailer>();

  /**
   * Instantiates a new media metadata.
   */
  public MediaMetadata(String providerId) {
    this.providerId = providerId;
  }

  public String getProviderId() {
    return providerId;
  }

  public int getTmdbId() {
    return tmdbId;
  }

  public String getPlot() {
    return plot;
  }

  public String getTitle() {
    return title;
  }

  public String getOriginalTitle() {
    return originalTitle;
  }

  public double getRating() {
    return rating;
  }

  public int getRuntime() {
    return runtime;
  }

  public String getTagline() {
    return tagline;
  }

  public void setTmdbId(int tmdbId) {
    this.tmdbId = tmdbId;
  }

  public String getImdbId() {
    return imdbId;
  }

  public void setImdbId(String imdbId) {
    this.imdbId = imdbId;
  }

  public void setPlot(String plot) {
    this.plot = plot;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setOriginalTitle(String originalTitle) {
    this.originalTitle = originalTitle;
  }

  public void setRating(double rating) {
    this.rating = rating;
  }

  public int getVoteCount() {
    return voteCount;
  }

  public void setVoteCount(int voteCount) {
    this.voteCount = voteCount;
  }

  public void setRuntime(int runtime) {
    this.runtime = runtime;
  }

  public void setTagline(String tagline) {
    this.tagline = tagline;
  }

  public String getYear() {
    return year;
  }

  public String getReleaseDate() {
    return releaseDate;
  }

  public void setYear(String year) {
    this.year = year;
  }

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

  public String getProductionCompany() {
    return productionCompany;
  }

  public void setProductionCompany(String productionCompany) {
    this.productionCompany = productionCompany;
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

}
