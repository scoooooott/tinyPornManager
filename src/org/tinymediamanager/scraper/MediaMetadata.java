package org.tinymediamanager.scraper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class MediaMetadata {

  public enum ArtworkSize {
    SMALL, MEDIUM, ORIGINAL
  }

  public static final int          ACTOR       = 0;
  public static final int          WRITER      = 1;
  public static final int          DIRECTOR    = 2;
  public static final int          OTHER       = 99;
  public static final int          ALL         = 999;

  private Map<MetadataKey, String> store       = new HashMap<MetadataKey, String>();
  private List<CastMember>         castMembers = new ArrayList<CastMember>();
  private List<MediaArt>           fanart      = new ArrayList<MediaArt>();
  private List<String>             genres      = new ArrayList<String>();

  public MediaMetadata() {
  }

  public String getAspectRatio() {
    return (String) get(MetadataKey.ASPECT_RATIO);
  }

  public void setAspectRatio(String aspectRatio) {
    set(MetadataKey.ASPECT_RATIO, aspectRatio);
  }

  public String getCompany() {
    return (String) get(MetadataKey.COMPANY);
  }

  public void setCompany(String company) {
    set(MetadataKey.COMPANY, company);
  }

  public List<String> getGenres() {
    return genres;
  }

  public String getMPAARating() {
    return (String) get(MetadataKey.MPAA_RATING);
  }

  public void setMPAARating(String rating) {
    set(MetadataKey.MPAA_RATING, rating);
  }

  public String getProviderDataId() {
    return get(MetadataKey.MEDIA_PROVIDER_DATA_ID);
  }

  public void setProviderDataId(String providerDataId) {
    set(MetadataKey.MEDIA_PROVIDER_DATA_ID, providerDataId);
  }

  public String getReleaseDate() {
    return (String) get(MetadataKey.RELEASE_DATE);
  }

  public void setReleaseDate(String releaseDate) {
    set(MetadataKey.RELEASE_DATE, releaseDate);
  }

  public String getRuntime() {
    return (String) get(MetadataKey.RUNNING_TIME);
  }

  public void setRuntime(String runtime) {
    set(MetadataKey.RUNNING_TIME, runtime);
  }

  public String getMediaTitle() {
    return (String) get(MetadataKey.MEDIA_TITLE);
  }

  public void setMediaTitle(String title) {
    set(MetadataKey.MEDIA_TITLE, title);
  }

  public String getUserRating() {
    return (String) get(MetadataKey.USER_RATING);
  }

  public void setUserRating(String userRating) {
    set(MetadataKey.USER_RATING, userRating);
  }

  public String getYear() {
    return (String) get(MetadataKey.YEAR);
  }

  public void setYear(String year) {
    set(MetadataKey.YEAR, year);
  }

  public String getOriginalTitle() {
    return (String) get(MetadataKey.ORIGINAL_TITLE);
  }

  public void setOriginalTitle(String originalTitle) {
    set(MetadataKey.ORIGINAL_TITLE, originalTitle);
  }

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

  public void addGenre(String genre) {
    if (genre == null || genre.trim().length() == 0)
      return;
    genres.add(genre);

  }

  public void addCastMember(CastMember cm) {
    if (containsCastMember(cm))
      return;
    castMembers.add(cm);

  }

  public void addMediaArt(MediaArt ma) {
    fanart.add(ma);

  }

  public void setDescription(String plot) {
    set(MetadataKey.DESCRIPTION, plot);
  }

  public String getDescription() {
    return (String) get(MetadataKey.DESCRIPTION);
  }

  public String get(MetadataKey key) {
    return store.get(key);
  }

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

  public String getProviderId() {
    return (String) get(MetadataKey.METADATA_PROVIDER_ID);
  }

  public void setProviderId(String id) {
    set(MetadataKey.METADATA_PROVIDER_ID, id);
  }

  public List<CastMember> getCastMembers() {
    return castMembers;
  }

  public List<MediaArt> getFanart() {
    return fanart;
  }

  public float getFloat(MetadataKey key, float defValue) {
    String value = getString(key);
    if (value != null) {
      return NumberUtils.toFloat(value, defValue);
    }
    else {
      return defValue;
    }
  }

  public int getInt(MetadataKey key, int defValue) {
    String value = getString(key);
    if (value != null) {
      return NumberUtils.toInt(value, defValue);
    }
    else {
      return defValue;
    }
  }

  public String getString(MetadataKey key) {
    return get(key);
  }

  public void setString(MetadataKey key, String value) {
    set(key, value);
  }

  public void remove(MetadataKey key) {
    store.remove(key);
  }

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

  public void setIMDBID(String imdbid) {
    set(MetadataKey.IMDB_ID, imdbid);
  }

  public String getIMDBID() {
    return get(MetadataKey.IMDB_ID);
  }

  public void setTMDBID(String tmdbid) {
    set(MetadataKey.TMDB_ID, tmdbid);
  }

  public String getTMDBID() {
    return get(MetadataKey.TMDB_ID);
  }

  public void setPlot(String plot) {
    set(MetadataKey.PLOT, plot);
  }

  public String getPlot() {
    return get(MetadataKey.PLOT);
  }

  public void setOutline(String outline) {
    set(MetadataKey.OUTLINE, outline);
  }

  public String getOutline() {
    return get(MetadataKey.OUTLINE);
  }

  public void setRating(float rating) {
    set(MetadataKey.USER_RATING, String.valueOf(rating));
  }

  public float getRating() {
    float rating = Float.valueOf(get(MetadataKey.USER_RATING));
    return rating;
  }

  public void setTagline(String tagline) {
    set(MetadataKey.TAGLINE, tagline);
  }

  public String getTagline() {
    return get(MetadataKey.TAGLINE);
  }

  /**
   * only update if the existing value is null or empty
   * 
   * @param md
   * @param key
   * @param value
   */
  public static void updateMDValue(MediaMetadata md, MetadataKey key, String value) {
    if (md.get(key) == null && !StringUtils.isEmpty(value)) {
      md.set(key, value);
    }
  }

}
