/*
 * Copyright 2012 - 2013 Manuel Laggner
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaCastMember.CastType;

/**
 * The Class MediaMetadata.
 * 
 * @author Manuel Laggner
 */
public class MediaMetadata {
  public static final String      PROVIDER_ID         = "providerId";
  public static final String      IMDBID              = Constants.IMDBID;
  public static final String      TMDBID              = Constants.TMDBID;
  public static final String      TMDBID_SET          = "tmdbIdSet";
  public static final String      COLLECTION_NAME     = "collectionName";
  public static final String      TITLE               = "title";
  public static final String      ORIGINAL_TITLE      = "originalTitle";
  public static final String      PLOT                = "plot";
  public static final String      RATING              = "rating";
  public static final String      VOTE_COUNT          = "voteCount";
  public static final String      TOP_250             = "top250";
  public static final String      RUNTIME             = "runtime";
  public static final String      TAGLINE             = "tagline";
  public static final String      PRODUCTION_COMPANY  = "productionCompany";
  public static final String      YEAR                = "year";
  public static final String      RELEASE_DATE        = "releaseDate";
  public static final String      SPOKEN_LANGUAGES    = "spokenLanguages";
  public static final String      COUNTRY             = "country";
  public static final String      POSTER_URL          = "posterUrl";
  public static final String      STATUS              = "status";

  // TV
  public static final String      EPISODE_NR          = "episodeNr";
  public static final String      SEASON_NR           = "seasonNr";
  public static final String      EPISODE_NR_DVD      = "dvdEpisodeNr";
  public static final String      SEASON_NR_DVD       = "dvdSeasonNr";
  public static final String      EPISODE_NR_COMBINED = "combinedEpisodeNr";
  public static final String      SEASON_NR_COMBINED  = "combinedSeasonNr";
  public static final String      ABSOLUTE_NR         = "absoluteNr";

  public static Date              INITIAL_DATE        = new Date(0);

  private List<MediaCastMember>   castMembers         = new ArrayList<MediaCastMember>();
  private List<MediaArtwork>      fanart              = new ArrayList<MediaArtwork>();
  private List<MediaGenres>       genres              = new ArrayList<MediaGenres>();
  private List<Certification>     certifications      = new ArrayList<Certification>();
  private List<MediaTrailer>      trailers            = new ArrayList<MediaTrailer>();

  /**
   * new infrastructure
   */
  private HashMap<String, Object> ids                 = new HashMap<String, Object>();
  private HashMap<String, Object> metadata            = new HashMap<String, Object>();

  /**
   * Instantiates a new media metadata for the given provider.
   * 
   * @param providerId
   *          the provider id
   */
  public MediaMetadata(String providerId) {
    storeMetadata(PROVIDER_ID, providerId);
  }

  /**
   * Gets the provider id.
   * 
   * @return the provider id
   */
  public String getProviderId() {
    return getStringValue(PROVIDER_ID);
  }

  /**
   * Stores a metadata in the internal map. Do not store IDs here. Use the ID map
   * 
   * @param key
   *          the key
   * @param value
   *          the metadata
   */
  public void storeMetadata(String key, Object value) {
    metadata.put(key, value);
  }

  /**
   * Gets the String value for a given key
   * 
   * @param key
   *          the key
   * @return value the value
   */
  public String getStringValue(String key) {
    Object data = metadata.get(key);
    if (data != null) {
      return String.valueOf(data);
    }
    return "";
  }

  /**
   * Gets the Integer value for a given key. Integer are passed right thru, whilst other type are casted to an Integer
   * 
   * @param key
   *          the key
   * @return value the value
   */
  public Integer getIntegerValue(String key) {
    Object data = metadata.get(key);
    if (data != null && data instanceof Integer) {
      // return the int
      return (Integer) data;
    }
    else if (data != null) {
      // try to parse out the int
      try {
        return Integer.parseInt(String.valueOf(data));
      }
      catch (Exception e) {
      }
    }

    return 0;
  }

  /**
   * Gets the Float value for a given key. Float are passed right thru, whilst other type are casted to an Float
   * 
   * @param key
   *          the key
   * @return value the value
   */
  public Float getFloatValue(String key) {
    Object data = metadata.get(key);
    if (data != null && data instanceof Float) {
      // return the float
      return (Float) data;
    }
    else if (data != null) {
      // try to parse out the float
      try {
        return Float.parseFloat(String.valueOf(data));
      }
      catch (Exception e) {
      }
    }

    return 0f;
  }

  /**
   * Gets the Double value for a given key. Double are passed right thru, whilst other type are casted to an Double
   * 
   * @param key
   *          the key
   * @return value the value
   */
  public Double getDoubleValue(String key) {
    Object data = metadata.get(key);
    if (data != null && data instanceof Double) {
      // return the float
      return (Double) data;
    }
    else if (data != null) {
      // try to parse out the float
      try {
        return Double.parseDouble(String.valueOf(data));
      }
      catch (Exception e) {
      }
    }

    return 0d;
  }

  /**
   * Gets the Date value for a given key. Date are passed right thru, whilst other type are returned with an initial value
   * 
   * @param key
   *          the key
   * @return value the value
   */
  public Date getDateValue(String key) {
    Object data = metadata.get(key);
    if (data != null && data instanceof Date) {
      return (Date) data;
    }
    return INITIAL_DATE;
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
   * Adds the genre.
   * 
   * @param genre
   *          the genre
   */
  public void addGenre(MediaGenres genre) {
    if (genre != null && !genres.contains(genre)) {
      genres.add(genre);
    }
  }

  /**
   * Adds the cast member.
   * 
   * @param cm
   *          the cast member
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
   * Adds the trailer. To use only when scraping the metadata also provides the trailers
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
   * Sets an ID.
   * 
   * @param key
   *          the ID-key
   * @param object
   *          the id
   */
  public void setId(String key, Object object) {
    ids.put(key, object);
  }

  /**
   * Gets an ID.
   * 
   * @param key
   *          the ID-key
   * @return the id
   */
  public Object getId(String key) {
    Object id = ids.get(key);
    if (id == null) {
      return "";
    }
    return id;
  }

  /**
   * Gets all IDs.
   * 
   * @return the IDs
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
