/*
 * Copyright 2012 - 2016 Manuel Laggner
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
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaCastMember.CastType;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.entities.MediaTrailer;

/**
 * The Class MediaMetadata. This is the main class to transport meta data.
 * 
 * @author Manuel Laggner
 * @since 2.0
 */
public class MediaMetadata {
  // some well known ids
  public static final String      IMDB                 = "imdb";
  public static final String      TMDB                 = "tmdb";
  public static final String      TVDB                 = "tvdb";
  public static final String      TMDB_SET             = "tmdbSet";

  // some meta ids for TV show scraping
  public static final String      EPISODE_NR           = "episodeNr";
  public static final String      SEASON_NR            = "seasonNr";
  public static final String      EPISODE_NR_DVD       = "dvdEpisodeNr";
  public static final String      SEASON_NR_DVD        = "dvdSeasonNr";

  private final String            providerId;

  // this map contains all set ids
  private HashMap<String, Object> ids                  = new HashMap<>();

  // general media entity
  private String                  title                = "";
  private String                  originalTitle        = "";
  private int                     year                 = 0;
  private Date                    releaseDate          = null;
  private String                  plot                 = "";
  private String                  tagline              = "";
  private int                     runtime              = 0;
  private float                   rating               = 0.0f;
  private int                     voteCount            = 0;

  // movie
  private String                  collectionName       = "";
  private int                     top250               = 0;

  // tv show
  private int                     episodeNumber        = -1;
  private int                     seasonNumber         = -1;
  private int                     dvdEpisodeNumber     = -1;
  private int                     dvdSeasonNumber      = -1;
  private int                     displayEpisodeNumber = -1;
  private int                     displaySeasonNumber  = -1;
  private int                     absoluteNumber       = -1;
  private String                  status               = "";

  // multi value
  private List<MediaCastMember>   castMembers          = new ArrayList<>();
  private List<MediaArtwork>      artwork              = new ArrayList<>();
  private List<MediaGenres>       genres               = new ArrayList<>();
  private List<Certification>     certifications       = new ArrayList<>();
  private List<String>            productionCompanies  = new ArrayList<>();
  private List<String>            spokenLanguages      = new ArrayList<>();
  private List<String>            countries            = new ArrayList<>();
  private List<MediaTrailer>      trailers             = new ArrayList<>();
  private List<MediaMetadata>     subItems             = new ArrayList<>();
  private List<String>            tags                 = new ArrayList<>();

  private HashMap<String, Object> extraData            = new HashMap<>();

  /**
   * Instantiates a new media metadata for the given provider.
   * 
   * @param providerId
   *          the provider id
   */
  public MediaMetadata(String providerId) {
    this.providerId = providerId;
  }

  /**
   * merges all entries from other MD into ours, IF VALUES ARE EMPTY<br>
   * <b>needs testing!</b>
   * 
   * @param md
   *          other MediaMetadata
   */

  public void mergeFrom(MediaMetadata md) {
    Map<String, Object> delta = md.getIds();
    delta.keySet().removeAll(ids.keySet()); // remove all remote ones, which we have in our map

    ids.putAll(delta); // so no dupe on adding while not overwriting

    title = merge(title, md.getTitle());
    originalTitle = merge(originalTitle, md.getOriginalTitle());
    year = merge(year, md.getYear());
    releaseDate = merge(releaseDate, md.getReleaseDate());
    plot = merge(plot, md.getPlot());
    tagline = merge(tagline, md.getTagline());
    runtime = merge(runtime, md.getRuntime());
    rating = merge(rating, md.getRating());
    voteCount = merge(voteCount, md.getVoteCount());
    collectionName = merge(collectionName, md.getCollectionName());
    top250 = merge(top250, md.getTop250());
    episodeNumber = merge(episodeNumber, md.getEpisodeNumber());
    seasonNumber = merge(seasonNumber, md.getSeasonNumber());
    dvdEpisodeNumber = merge(dvdEpisodeNumber, md.getDvdEpisodeNumber());
    dvdSeasonNumber = merge(dvdSeasonNumber, md.getDvdSeasonNumber());
    absoluteNumber = merge(absoluteNumber, md.getAbsoluteNumber());
    status = merge(status, md.getStatus());

    castMembers.removeAll(md.getCastMembers()); // remove all local ones, which we have in other array
    castMembers.addAll(md.getCastMembers()); // so no dupe on adding all ;)

    artwork.removeAll(md.getFanart());
    artwork.addAll(md.getFanart());

    genres.removeAll(md.getGenres());
    genres.addAll(md.getGenres());

    certifications.removeAll(md.getCertifications());
    certifications.addAll(md.getCertifications());

    productionCompanies.removeAll(md.getProductionCompanies());
    productionCompanies.addAll(md.getProductionCompanies());

    spokenLanguages.removeAll(md.getSpokenLanguages());
    spokenLanguages.addAll(md.getSpokenLanguages());

    countries.removeAll(md.getCountries());
    countries.addAll(md.getCountries());

    trailers.removeAll(md.getTrailers());
    trailers.addAll(md.getTrailers());

    subItems.removeAll(md.getSubItems());
    subItems.addAll(md.getSubItems());

    tags.removeAll(md.getTags());
    tags.addAll(md.getTags());

    delta = md.getExtraData();
    delta.keySet().removeAll(extraData.keySet());
    extraData.putAll(delta);
  }

  private String merge(String val1, String val2) {
    return StringUtils.isBlank(val1) ? val2 : val1;
  }

  private int merge(int val1, int val2) {
    return val1 <= 0 ? val2 : val1;
  }

  private Date merge(Date val1, Date val2) {
    return val1 == null ? val2 : val1;
  }

  private float merge(float val1, float val2) {
    return val1 <= 0 ? val2 : val1;
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
    if (type == CastType.ALL) {
      return castMembers;
    }

    // get all castmember for the given type
    List<MediaCastMember> l = new ArrayList<>(castMembers.size());
    for (MediaCastMember cm : castMembers) {
      if (cm.getType() == type) {
        l.add(cm);
      }
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
    if (mediaArt == null || type == MediaArtworkType.ALL) {
      return mediaArt;
    }

    // get all artwork
    List<MediaArtwork> l = new ArrayList<>(mediaArt.size());
    for (MediaArtwork ma : mediaArt) {
      if (ma.getType() == type) {
        l.add(ma);
      }
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
    if (containsCastMember(cm)) {
      return;
    }
    castMembers.add(cm);

  }

  /**
   * Adds the media art.
   * 
   * @param ma
   *          the ma
   */
  public void addMediaArt(MediaArtwork ma) {
    artwork.add(ma);
  }

  /**
   * Clear media art.
   */
  public void clearMediaArt() {
    artwork.clear();
  }

  /**
   * Adds the media art.
   * 
   * @param art
   *          the art
   */
  public void addMediaArt(List<MediaArtwork> art) {
    artwork.addAll(art);
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
    return artwork;
  }

  /**
   * Add a sub item
   *
   * @param item
   *          the subitem to be added
   */
  public void addSubItem(MediaMetadata item) {
    subItems.add(item);
  }

  /**
   * Get all subitems
   * 
   * @return a list of all sub items
   */
  public List<MediaMetadata> getSubItems() {
    return subItems;
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
   * Get all production companies
   * 
   * @return a list of all production companies
   */
  public List<String> getProductionCompanies() {
    return productionCompanies;
  }

  /**
   * Set the production companies
   * 
   * @param productionCompanies
   *          set the given list of production companies
   */
  public void setProductionCompanies(List<String> productionCompanies) {
    this.productionCompanies = productionCompanies;
  }

  /**
   * Add a production company
   * 
   * @param productionCompany
   *          add the given production company if it is not yet present
   */
  public void addProductionCompany(String productionCompany) {
    if (!productionCompanies.contains(productionCompany)) {
      productionCompanies.add(productionCompany);
    }
  }

  /**
   * Removes the given production company
   * 
   * @param productionCompany
   *          the production company to be removed
   */
  public void removeProductionCompany(String productionCompany) {
    productionCompanies.remove(productionCompany);
  }

  /**
   * Get a list of all spoken languages (2 digit: ISO 639-1)
   * 
   * @return a list of all spoken languages
   */
  public List<String> getSpokenLanguages() {
    return spokenLanguages;
  }

  /**
   * Set the spoken languages (2 digit: ISO 639-1)
   * 
   * @param spokenLanguages
   *          the spoken languages to be set
   */
  public void setSpokenLanguages(List<String> spokenLanguages) {
    this.spokenLanguages = spokenLanguages;
  }

  /**
   * Adds the given language if it is not present (2 digit: ISO 639-1)
   * 
   * @param language
   *          the language to be set
   */
  public void addSpokenLanguage(String language) {
    if (!spokenLanguages.contains(language)) {
      spokenLanguages.add(language);
    }
  }

  /**
   * Removes the given language
   * 
   * @param language
   *          the language to be removed
   */
  public void removeSpokenLanguage(String language) {
    spokenLanguages.remove(language);
  }

  /**
   * Get the list of all countries
   * 
   * @return a list of all countries
   */
  public List<String> getCountries() {
    return countries;
  }

  /**
   * Set the countries
   * 
   * @param countries
   *          the countries to be set
   */
  public void setCountries(List<String> countries) {
    this.countries = countries;
  }

  /**
   * Add the country if it is not present
   * 
   * @param country
   *          the country to be added
   */
  public void addCountry(String country) {
    if (!countries.contains(country)) {
      countries.add(country);
    }
  }

  /**
   * Remove the given country
   * 
   * @param country
   *          the country to be removed
   */
  public void removeCountry(String country) {
    countries.remove(country);
  }

  /**
   * Get the title
   * 
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set the title
   * 
   * @param title
   *          the title to be set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Get the original title
   * 
   * @return the original title
   */
  public String getOriginalTitle() {
    return originalTitle;
  }

  /**
   * Set the original title
   * 
   * @param originalTitle
   *          the origial title to be set
   */
  public void setOriginalTitle(String originalTitle) {
    this.originalTitle = originalTitle;
  }

  /**
   * Get the year
   * 
   * @return the year
   */
  public int getYear() {
    return year;
  }

  /**
   * Set the year
   * 
   * @param year
   *          the year to be set
   */
  public void setYear(int year) {
    this.year = year;
  }

  /**
   * Get the release date
   * 
   * @return the release date
   */
  public Date getReleaseDate() {
    return releaseDate;
  }

  /**
   * Set the release date
   * 
   * @param releaseDate
   *          the release date to be set
   */
  public void setReleaseDate(Date releaseDate) {
    this.releaseDate = releaseDate;
  }

  /**
   * Get the plot
   * 
   * @return the plot
   */
  public String getPlot() {
    return plot;
  }

  /**
   * Set the plot
   * 
   * @param plot
   *          the plot to be set
   */
  public void setPlot(String plot) {
    this.plot = plot;
  }

  /**
   * Get the tagline
   * 
   * @return the tagline
   */
  public String getTagline() {
    return tagline;
  }

  /**
   * Set the tagline
   * 
   * @param tagline
   *          the tagline to be set
   */
  public void setTagline(String tagline) {
    this.tagline = tagline;
  }

  /**
   * Get the collection name
   * 
   * @return the collection name
   */
  public String getCollectionName() {
    return collectionName;
  }

  /**
   * Set the collection name
   * 
   * @param collectionName
   *          the collection name to be set
   */
  public void setCollectionName(String collectionName) {
    this.collectionName = collectionName;
  }

  /**
   * Get the runtime in minutes
   * 
   * @return the runtime in minutes
   */
  public int getRuntime() {
    return runtime;
  }

  /**
   * Set the runtime in minutes (full minutes)
   * 
   * @param runtime
   *          the runtime in minutes to be set
   */
  public void setRuntime(int runtime) {
    this.runtime = runtime;
  }

  /**
   * Get the rating (0 ... 10.0)
   * 
   * @return the rating
   */
  public float getRating() {
    return rating;
  }

  /**
   * Set the rating. The values are valid from 0 to 10.0
   * 
   * @param rating
   *          the rating to be set
   */
  public void setRating(float rating) {
    this.rating = rating;
  }

  /**
   * Get the vote count
   * 
   * @return the vote count
   */
  public int getVoteCount() {
    return voteCount;
  }

  /**
   * Set the vote count
   * 
   * @param voteCount
   *          the vote count to be set
   */
  public void setVoteCount(int voteCount) {
    this.voteCount = voteCount;
  }

  /**
   * Get the place in the top 250 or 0 if not set
   * 
   * @return the place in top 250 or 0
   */
  public int getTop250() {
    return top250;
  }

  /**
   * Set the place in the top 250
   * 
   * @param top250
   *          the place to be set
   */
  public void setTop250(int top250) {
    this.top250 = top250;
  }

  /**
   * Get the episode number (or -1 if not set)
   * 
   * @return the episode number (or -1 if not set)
   */
  public int getEpisodeNumber() {
    return episodeNumber;
  }

  /**
   * Set the episode number
   * 
   * @param episodeNumber
   *          the episode number to be set
   */
  public void setEpisodeNumber(int episodeNumber) {
    this.episodeNumber = episodeNumber;
  }

  /**
   * Get the season number (or -1 if not set)
   * 
   * @return the season number (or -1 if not set)
   */
  public int getSeasonNumber() {
    return seasonNumber;
  }

  /**
   * Set the season number
   * 
   * @param seasonNumber
   *          the season number to be set
   */
  public void setSeasonNumber(int seasonNumber) {
    this.seasonNumber = seasonNumber;
  }

  /**
   * Get the DVD episode number (or -1 if not set)
   * 
   * @return the DVD episode number (or -1 if not set)
   */
  public int getDvdEpisodeNumber() {
    return dvdEpisodeNumber;
  }

  /**
   * Set the DVD episode number
   * 
   * @param dvdEpisodeNumber
   *          the DVD episode number to be set
   */
  public void setDvdEpisodeNumber(int dvdEpisodeNumber) {
    this.dvdEpisodeNumber = dvdEpisodeNumber;
  }

  /**
   * Get the DVD season number (or -1 if not set)
   * 
   * @return the DVD season number (or -1 if not set)
   */
  public int getDvdSeasonNumber() {
    return dvdSeasonNumber;
  }

  /**
   * Set the DVD season number
   * 
   * @param dvdSeasonNumber
   *          the DVD season number to be set
   */
  public void setDvdSeasonNumber(int dvdSeasonNumber) {
    this.dvdSeasonNumber = dvdSeasonNumber;
  }

  /**
   * Get the display-episode number (or -1 if not set)
   * 
   * @return the display-episode number (or -1 if not set)
   */
  public int getDisplayEpisodeNumber() {
    return displayEpisodeNumber;
  }

  /**
   * Set the display-episode number
   * 
   * @param displayEpisodeNumber
   *          the display-episode number to be set
   */
  public void setDisplayEpisodeNumber(int displayEpisodeNumber) {
    this.displayEpisodeNumber = displayEpisodeNumber;
  }

  /**
   * Get the display-season number (or -1 if not set)
   * 
   * @return the display-season number (or -1 if not set)
   */
  public int getDisplaySeasonNumber() {
    return displaySeasonNumber;
  }

  /**
   * Set the display-season number
   * 
   * @param displaySeasonNumber
   *          the display-season number to be set
   */
  public void setDisplaySeasonNumber(int displaySeasonNumber) {
    this.displaySeasonNumber = displaySeasonNumber;
  }

  /**
   * Get the absolute number (or -1 if not set)
   * 
   * @return the absolute number (or -1 if not set)
   */
  public int getAbsoluteNumber() {
    return absoluteNumber;
  }

  /**
   * Set the absolute number
   * 
   * @param absoluteNumber
   *          the absolute number to be set
   */
  public void setAbsoluteNumber(int absoluteNumber) {
    this.absoluteNumber = absoluteNumber;
  }

  /**
   * Get the airing status
   * 
   * @return the airing status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Set the airing status
   * 
   * @param status
   *          the airing status to be set
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Get all extra data. Handy key/value store to pass extra data inside a scraper
   * 
   * @return the key/value store
   */
  public Map<String, Object> getExtraData() {
    return extraData;
  }

  /**
   * Add an extra data. Handy key/value store to pass extra data inside a scraper
   * 
   * @param key
   *          the key
   * @param value
   *          the value
   */
  public void addExtraData(String key, Object value) {
    extraData.put(key, value);
  }

  /**
   * Get an extra data. Handy key/value store to pass extra data inside a scraper
   * 
   * @param key
   *          the key
   * @return the value or null
   */
  public Object getExtraData(String key) {
    return extraData.get(key);
  }

  /**
   * Get the tags
   * 
   * @return a list containing all tags
   */
  public List<String> getTags() {
    return tags;
  }

  /**
   * Set tags
   * 
   * @param tags
   *          the tags to be set
   */
  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  /**
   * Add a new tag
   * 
   * @param tag
   *          the tag
   */
  public void addTag(String tag) {
    if (tag != null && !tags.contains(tag)) {
      tags.add(tag);
    }
  }

  /**
   * Remove the given tag
   * 
   * @param tag
   *          the tag to be removed
   */
  public void removeTag(String tag) {
    tags.remove(tag);
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
