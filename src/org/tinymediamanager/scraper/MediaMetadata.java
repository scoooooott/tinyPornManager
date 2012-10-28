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
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * The Class MediaMetadata.
 */
public class MediaMetadata {

  /**
   * The Enum ArtworkSize.
   */
  public enum ArtworkSize {

    /** The small. */
    SMALL,
    /** The medium. */
    MEDIUM,
    /** The original. */
    ORIGINAL
  }

  /** The Constant ACTOR. */
  public static final int          ACTOR          = 0;

  /** The Constant WRITER. */
  public static final int          WRITER         = 1;

  /** The Constant DIRECTOR. */
  public static final int          DIRECTOR       = 2;

  /** The Constant OTHER. */
  public static final int          OTHER          = 99;

  /** The Constant ALL. */
  public static final int          ALL            = 999;

  /** The store. */
  private Map<MetadataKey, String> store          = new HashMap<MetadataKey, String>();

  /** The cast members. */
  private List<CastMember>         castMembers    = new ArrayList<CastMember>();

  /** The fanart. */
  private List<MediaArt>           fanart         = new ArrayList<MediaArt>();

  /** The genres. */
  private List<MediaGenres>        genres         = new ArrayList<MediaGenres>();

  /** The certifications. */
  private List<Certification>      certifications = new ArrayList<Certification>();

  /**
   * Instantiates a new media metadata.
   */
  public MediaMetadata() {
  }

  /**
   * Gets the aspect ratio.
   * 
   * @return the aspect ratio
   */
  public String getAspectRatio() {
    return (String) get(MetadataKey.ASPECT_RATIO);
  }

  /**
   * Sets the aspect ratio.
   * 
   * @param aspectRatio
   *          the new aspect ratio
   */
  public void setAspectRatio(String aspectRatio) {
    set(MetadataKey.ASPECT_RATIO, aspectRatio);
  }

  /**
   * Gets the company.
   * 
   * @return the company
   */
  public String getCompany() {
    return (String) get(MetadataKey.COMPANY);
  }

  /**
   * Sets the company.
   * 
   * @param company
   *          the new company
   */
  public void setCompany(String company) {
    set(MetadataKey.COMPANY, company);
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
   * Gets the mPAA rating.
   * 
   * @return the mPAA rating
   */
  public String getMPAARating() {
    return (String) get(MetadataKey.MPAA_RATING);
  }

  /**
   * Sets the mPAA rating.
   * 
   * @param rating
   *          the new mPAA rating
   */
  public void setMPAARating(String rating) {
    set(MetadataKey.MPAA_RATING, rating);
  }

  /**
   * Gets the provider data id.
   * 
   * @return the provider data id
   */
  public String getProviderDataId() {
    return get(MetadataKey.MEDIA_PROVIDER_DATA_ID);
  }

  /**
   * Sets the provider data id.
   * 
   * @param providerDataId
   *          the new provider data id
   */
  public void setProviderDataId(String providerDataId) {
    set(MetadataKey.MEDIA_PROVIDER_DATA_ID, providerDataId);
  }

  /**
   * Gets the release date.
   * 
   * @return the release date
   */
  public String getReleaseDate() {
    return (String) get(MetadataKey.RELEASE_DATE);
  }

  /**
   * Sets the release date.
   * 
   * @param releaseDate
   *          the new release date
   */
  public void setReleaseDate(String releaseDate) {
    set(MetadataKey.RELEASE_DATE, releaseDate);
  }

  /**
   * Gets the runtime.
   * 
   * @return the runtime
   */
  public String getRuntime() {
    return (String) get(MetadataKey.RUNNING_TIME);
  }

  /**
   * Sets the runtime.
   * 
   * @param runtime
   *          the new runtime
   */
  public void setRuntime(String runtime) {
    set(MetadataKey.RUNNING_TIME, runtime);
  }

  /**
   * Gets the media title.
   * 
   * @return the media title
   */
  public String getMediaTitle() {
    return (String) get(MetadataKey.MEDIA_TITLE);
  }

  /**
   * Sets the media title.
   * 
   * @param title
   *          the new media title
   */
  public void setMediaTitle(String title) {
    set(MetadataKey.MEDIA_TITLE, title);
  }

  /**
   * Gets the user rating.
   * 
   * @return the user rating
   */
  public String getUserRating() {
    return (String) get(MetadataKey.USER_RATING);
  }

  /**
   * Sets the user rating.
   * 
   * @param userRating
   *          the new user rating
   */
  public void setUserRating(String userRating) {
    set(MetadataKey.USER_RATING, userRating);
  }

  /**
   * Gets the year.
   * 
   * @return the year
   */
  public String getYear() {
    return (String) get(MetadataKey.YEAR);
  }

  /**
   * Sets the year.
   * 
   * @param year
   *          the new year
   */
  public void setYear(String year) {
    set(MetadataKey.YEAR, year);
  }

  /**
   * Gets the original title.
   * 
   * @return the original title
   */
  public String getOriginalTitle() {
    return (String) get(MetadataKey.ORIGINAL_TITLE);
  }

  /**
   * Sets the original title.
   * 
   * @param originalTitle
   *          the new original title
   */
  public void setOriginalTitle(String originalTitle) {
    set(MetadataKey.ORIGINAL_TITLE, originalTitle);
  }

  /**
   * Gets the cast members.
   * 
   * @param type
   *          the type
   * @return the cast members
   */
  public List<CastMember> getCastMembers(int type) {
    if (castMembers == null || type == ALL)
      return castMembers;

    List<CastMember> l = new ArrayList<CastMember>(castMembers.size());
    for (CastMember cm : castMembers) {
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
  public List<MediaArt> getMediaArt(MediaArtifactType type) {
    List<MediaArt> mediaArt = getFanart();
    if (mediaArt == null || type == null)
      return mediaArt;

    // TODO: Cache this information
    List<MediaArt> l = new ArrayList<MediaArt>(mediaArt.size());
    for (MediaArt ma : mediaArt) {
      if (ma.getType() == type)
        l.add(ma);
    }
    return l;

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
  public void addCastMember(CastMember cm) {
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
  public void addMediaArt(MediaArt ma) {
    fanart.add(ma);
  }

  /**
   * Sets the description.
   * 
   * @param plot
   *          the new description
   */
  public void setDescription(String plot) {
    set(MetadataKey.DESCRIPTION, plot);
  }

  /**
   * Gets the description.
   * 
   * @return the description
   */
  public String getDescription() {
    return (String) get(MetadataKey.DESCRIPTION);
  }

  /**
   * Gets the.
   * 
   * @param key
   *          the key
   * @return the string
   */
  public String get(MetadataKey key) {
    return store.get(key);
  }

  /**
   * Sets the.
   * 
   * @param key
   *          the key
   * @param value
   *          the value
   */
  public void set(MetadataKey key, String value) {
    if (value != null) {
      // manipulate some fields
      if (key == MetadataKey.SEASON || key == MetadataKey.EPISODE || key == MetadataKey.DVD_DISC) {
        int n = toInt((String) value);
        value = String.valueOf(n);
      }

      store.put(key, value);
    }
  }

  /**
   * To int.
   * 
   * @param value
   *          the value
   * @return the int
   */
  private int toInt(String value) {
    try {
      return Integer.parseInt(value);
    }
    catch (Exception e) {
      return 0;
    }
  }

  /*
   * public String getProviderDataUrl() { return (String)
   * get(MetadataKey.METADATA_PROVIDER_DATA_URL); } public void
   * setProviderDataUrl(String url) {
   * set(MetadataKey.METADATA_PROVIDER_DATA_URL, url); }
   */

  /**
   * Gets the provider id.
   * 
   * @return the provider id
   */
  public String getProviderId() {
    return (String) get(MetadataKey.METADATA_PROVIDER_ID);
  }

  /**
   * Sets the provider id.
   * 
   * @param id
   *          the new provider id
   */
  public void setProviderId(String id) {
    set(MetadataKey.METADATA_PROVIDER_ID, id);
  }

  /**
   * Gets the cast members.
   * 
   * @return the cast members
   */
  public List<CastMember> getCastMembers() {
    return castMembers;
  }

  /**
   * Gets the fanart.
   * 
   * @return the fanart
   */
  public List<MediaArt> getFanart() {
    return fanart;
  }

  /**
   * Gets the float.
   * 
   * @param key
   *          the key
   * @param defValue
   *          the def value
   * @return the float
   */
  public float getFloat(MetadataKey key, float defValue) {
    String value = getString(key);
    if (value != null) {
      return NumberUtils.toFloat(value, defValue);
    }
    else {
      return defValue;
    }
  }

  /**
   * Gets the int.
   * 
   * @param key
   *          the key
   * @param defValue
   *          the def value
   * @return the int
   */
  public int getInt(MetadataKey key, int defValue) {
    String value = getString(key);
    if (value != null) {
      return NumberUtils.toInt(value, defValue);
    }
    else {
      return defValue;
    }
  }

  /**
   * Gets the string.
   * 
   * @param key
   *          the key
   * @return the string
   */
  public String getString(MetadataKey key) {
    return get(key);
  }

  /**
   * Sets the string.
   * 
   * @param key
   *          the key
   * @param value
   *          the value
   */
  public void setString(MetadataKey key, String value) {
    set(key, value);
  }

  /**
   * Removes the.
   * 
   * @param key
   *          the key
   */
  public void remove(MetadataKey key) {
    store.remove(key);
  }

  /**
   * Contains cast member.
   * 
   * @param cm
   *          the cm
   * @return true, if successful
   */
  private boolean containsCastMember(CastMember cm) {
    boolean found = false;
    if (castMembers != null) {
      for (CastMember m : castMembers) {
        if (m.getType() == cm.getType() && (m.getName() != null && m.getName().equals(cm.getName()))) {
          found = true;
          break;
        }
      }
    }

    return found;
  }

  /**
   * Sets the imdbid.
   * 
   * @param imdbid
   *          the new imdbid
   */
  public void setIMDBID(String imdbid) {
    set(MetadataKey.IMDB_ID, imdbid);
  }

  /**
   * Gets the imdbid.
   * 
   * @return the imdbid
   */
  public String getIMDBID() {
    return get(MetadataKey.IMDB_ID);
  }

  /**
   * Sets the tmdbid.
   * 
   * @param tmdbid
   *          the new tmdbid
   */
  public void setTMDBID(String tmdbid) {
    set(MetadataKey.TMDB_ID, tmdbid);
  }

  /**
   * Gets the tmdbid.
   * 
   * @return the tmdbid
   */
  public String getTMDBID() {
    return get(MetadataKey.TMDB_ID);
  }

  /**
   * Sets the plot.
   * 
   * @param plot
   *          the new plot
   */
  public void setPlot(String plot) {
    set(MetadataKey.PLOT, plot);
  }

  /**
   * Gets the plot.
   * 
   * @return the plot
   */
  public String getPlot() {
    return get(MetadataKey.PLOT);
  }

  /**
   * Sets the outline.
   * 
   * @param outline
   *          the new outline
   */
  public void setOutline(String outline) {
    set(MetadataKey.OUTLINE, outline);
  }

  /**
   * Gets the outline.
   * 
   * @return the outline
   */
  public String getOutline() {
    return get(MetadataKey.OUTLINE);
  }

  /**
   * Sets the rating.
   * 
   * @param rating
   *          the new rating
   */
  public void setRating(float rating) {
    set(MetadataKey.USER_RATING, String.valueOf(rating));
  }

  /**
   * Gets the rating.
   * 
   * @return the rating
   */
  public float getRating() {
    float rating = Float.valueOf(get(MetadataKey.USER_RATING));
    return rating;
  }

  /**
   * Sets the tagline.
   * 
   * @param tagline
   *          the new tagline
   */
  public void setTagline(String tagline) {
    set(MetadataKey.TAGLINE, tagline);
  }

  /**
   * Gets the tagline.
   * 
   * @return the tagline
   */
  public String getTagline() {
    return get(MetadataKey.TAGLINE);
  }

  /**
   * only update if the existing value is null or empty.
   * 
   * @param md
   *          the md
   * @param key
   *          the key
   * @param value
   *          the value
   */
  public static void updateMDValue(MediaMetadata md, MetadataKey key, String value) {
    if (md.get(key) == null && !StringUtils.isEmpty(value)) {
      md.set(key, value);
    }
  }

  public void addCertification(Certification certification) {
    certifications.add(certification);
  }

  public List<Certification> getCertifications() {
    return certifications;
  }

}
