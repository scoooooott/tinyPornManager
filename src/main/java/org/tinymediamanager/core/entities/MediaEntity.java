/*
 * Copyright 2012 - 2020 Manuel Laggner
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
package org.tinymediamanager.core.entities;

import static org.tinymediamanager.core.Constants.BANNER;
import static org.tinymediamanager.core.Constants.CHARACTERART;
import static org.tinymediamanager.core.Constants.CLEARART;
import static org.tinymediamanager.core.Constants.CLEARLOGO;
import static org.tinymediamanager.core.Constants.DATA_SOURCE;
import static org.tinymediamanager.core.Constants.DATE_ADDED;
import static org.tinymediamanager.core.Constants.DATE_ADDED_AS_STRING;
import static org.tinymediamanager.core.Constants.DISC;
import static org.tinymediamanager.core.Constants.FANART;
import static org.tinymediamanager.core.Constants.HAS_IMAGES;
import static org.tinymediamanager.core.Constants.KEYART;
import static org.tinymediamanager.core.Constants.LOGO;
import static org.tinymediamanager.core.Constants.MEDIA_FILES;
import static org.tinymediamanager.core.Constants.MEDIA_INFORMATION;
import static org.tinymediamanager.core.Constants.NEWLY_ADDED;
import static org.tinymediamanager.core.Constants.ORIGINAL_FILENAME;
import static org.tinymediamanager.core.Constants.ORIGINAL_TITLE;
import static org.tinymediamanager.core.Constants.PATH;
import static org.tinymediamanager.core.Constants.PLOT;
import static org.tinymediamanager.core.Constants.POSTER;
import static org.tinymediamanager.core.Constants.PRODUCTION_COMPANY;
import static org.tinymediamanager.core.Constants.RATING;
import static org.tinymediamanager.core.Constants.SCRAPED;
import static org.tinymediamanager.core.Constants.THUMB;
import static org.tinymediamanager.core.Constants.TITLE;
import static org.tinymediamanager.core.Constants.YEAR;

import java.awt.Dimension;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.TmmDateFormat;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class MediaEntity. The base class for all entities
 * 
 * @author Manuel Laggner
 */
public abstract class MediaEntity extends AbstractModelObject {
  private static final Logger          LOGGER            = LoggerFactory.getLogger(MediaEntity.class);
  /** The id for the database. */
  protected UUID                       dbId              = UUID.randomUUID();

  @JsonProperty
  protected String                     dataSource        = "";

  /** The ids to store the ID from several metadataproviders. */
  @JsonProperty
  protected Map<String, Object>        ids               = new ConcurrentHashMap<>(0);

  @JsonProperty
  protected String                     title             = "";
  @JsonProperty
  protected String                     originalTitle     = "";
  @JsonProperty
  protected int                        year              = 0;
  @JsonProperty
  protected String                     plot              = "";
  @JsonProperty
  protected String                     path              = "";
  @JsonProperty
  protected Date                       dateAdded         = new Date();
  @JsonProperty
  protected String                     productionCompany = "";
  @JsonProperty
  protected boolean                    scraped           = false;
  @JsonProperty
  protected String                     note              = "";

  @JsonProperty
  protected Map<String, MediaRating>   ratings           = new ConcurrentHashMap<>(0);
  @JsonProperty
  private List<MediaFile>              mediaFiles        = new ArrayList<>();
  @JsonProperty
  protected Map<MediaFileType, String> artworkUrlMap     = new HashMap<>();

  protected boolean                    newlyAdded        = false;
  protected boolean                    duplicate         = false;
  protected ReadWriteLock              readWriteLock     = new ReentrantReadWriteLock();

  @JsonProperty
  protected String                     originalFilename  = "";

  public MediaEntity() {
  }

  /**
   * get the main file for this entity
   * 
   * @return the main file
   */
  abstract public MediaFile getMainFile();

  /**
   * Overwrites all null/empty elements with "other" value (but might be empty also)<br>
   * For lists, check with 'contains' and add.<br>
   * Do NOT merge path, dateAdded, scraped, mediaFiles and other crucial properties!
   * 
   * @param other
   *          the media entity to merge in
   */
  public void merge(MediaEntity other) {
    merge(other, false);
  }

  /**
   * Overwrites all elements with "other" value<br>
   * Do NOT merge path, dateAdded, scraped, mediaFiles and other crucial properties!
   *
   * @param other
   *          the media entity to merge in
   */
  public void forceMerge(MediaEntity other) {
    merge(other, true);
  }

  protected void merge(MediaEntity other, boolean force) {
    if (other == null) {
      return;
    }

    setTitle(StringUtils.isEmpty(title) || force ? other.title : title);
    setOriginalTitle(StringUtils.isEmpty(originalTitle) || force ? other.originalTitle : originalTitle);
    setYear(year == 0 || force ? other.year : year);
    setPlot(StringUtils.isEmpty(plot) || force ? other.plot : plot);
    setProductionCompany(StringUtils.isEmpty(productionCompany) || force ? other.productionCompany : productionCompany);
    setOriginalFilename(StringUtils.isEmpty(originalFilename) || force ? other.originalFilename : originalFilename);

    // when force is set, clear the lists/maps and add all other values
    if (force) {
      ids.clear();
      ratings.clear();

      artworkUrlMap.clear();
    }

    setRatings(other.ratings);

    for (String key : other.getIds().keySet()) {
      if (!ids.containsKey(key)) {
        ids.put(key, other.getId(key));
      }
    }
    for (MediaFileType key : other.getArtworkUrls().keySet()) {
      if (!artworkUrlMap.containsKey(key)) {
        artworkUrlMap.put(key, other.getArtworkUrl(key));
      }
    }
  }

  /**
   * Initialize after loading from database.
   */
  public void initializeAfterLoading() {
    sortMediaFiles();
  }

  protected void sortMediaFiles() {
    Comparator<MediaFile> mediaFileComparator = getMediaFileComparator();
    if (mediaFileComparator != null) {
      mediaFiles.sort(mediaFileComparator);
    }
    else {
      Collections.sort(mediaFiles);
    }
  }

  /**
   * get the INTERNAL ID of this object. Do not confuse it with the IDs from the metadata provider!
   * 
   * @return internal id
   */
  public UUID getDbId() {
    return dbId;
  }

  public void setDbId(UUID id) {
    this.dbId = id;
  }

  /**
   * Gets the data source.
   *
   * @return the data source
   */
  public String getDataSource() {
    return dataSource;
  }

  /**
   * Sets the data source.
   *
   * @param newValue
   *          the new data source
   */
  public void setDataSource(String newValue) {
    String oldValue = this.dataSource;
    this.dataSource = newValue;
    firePropertyChange(DATA_SOURCE, oldValue, newValue);
  }

  /**
   * get all ID for this object. These are the IDs from the various scraper
   * 
   * @return a map of all IDs
   */
  public Map<String, Object> getIds() {
    return ids;
  }

  /**
   * get the title of this entity
   * 
   * @return the title or an empty string
   */
  public String getTitle() {
    return title;
  }

  /**
   * get the original title of this entity
   * 
   * @return the original title or an empty string
   */
  public String getOriginalTitle() {
    return originalTitle;
  }

  /**
   * get the plot of this entity
   * 
   * @return the plot or an empty string
   */
  public String getPlot() {
    return plot;
  }

  /**
   * needed as getter<br>
   * better use {@link #getPathNIO()}<br>
   * 
   * @return
   */
  public String getPath() {
    return this.path;
  }

  public String getOriginalFilename() {
    return this.originalFilename;
  }

  /**
   * @return filesystem path or NULL
   */
  public Path getPathNIO() {
    if (StringUtils.isBlank(path)) {
      return null;
    }
    return Paths.get(path).toAbsolutePath();
  }

  /**
   * get the parent relative to the data source as string
   *
   * @return a string which represents the parent relative to the data source
   */
  public String getParent() {
    Path path = getPathNIO();
    if (path == null) {
      return "";
    }

    Path parent = Paths.get(this.dataSource).toAbsolutePath().relativize(path.getParent());

    return parent.toString();
  }

  /**
   * Gets the dimension of the (first) artwork of the given type
   * 
   * @param type
   *          the artwork type
   * @return the dimension of the artwork or a zero dimension if no artwork has been found
   */
  public Dimension getArtworkDimension(MediaFileType type) {
    List<MediaFile> artworks = getMediaFiles(type);
    if (!artworks.isEmpty()) {
      MediaFile mediaFile = artworks.get(0);
      return new Dimension(mediaFile.getVideoWidth(), mediaFile.getVideoHeight());
    }
    return new Dimension(0, 0);
  }

  /**
   * Gets the file name of the (first) artwork of the given type
   * 
   * @param type
   *          the artwork type
   * @return the file name of the artwork or an empty string if nothing has been found
   */
  public String getArtworkFilename(MediaFileType type) {
    List<MediaFile> artworks = getMediaFiles(type);
    if (!artworks.isEmpty()) {
      return artworks.get(0).getFile().toString();
    }
    return "";
  }

  public Map<String, MediaRating> getRatings() {
    return ratings;
  }

  public MediaRating getRating(String id) {
    return ratings.getOrDefault(id, new MediaRating());
  }

  /**
   * get the "main" rating
   * 
   * @return the main (preferred) rating
   */
  public MediaRating getRating() {
    MediaRating mediaRating = ratings.get(MediaRating.USER);

    // then the default one (either NFO or DEFAULT)
    if (mediaRating == null) {
      mediaRating = ratings.get(MediaRating.NFO);
    }
    if (mediaRating == null) {
      mediaRating = ratings.get(MediaRating.DEFAULT);
    }

    // is there any rating?
    if (mediaRating == null && !ratings.isEmpty()) {
      mediaRating = ratings.values().iterator().next();
    }

    // last but not least a non null value
    if (mediaRating == null) {
      mediaRating = new MediaRating();
    }

    return mediaRating;
  }

  public int getYear() {
    return year;
  }

  public void setIds(Map<String, Object> ids) {
    for (Entry<String, Object> entry : ids.entrySet()) {
      if (StringUtils.isNotBlank(entry.getKey()) && entry.getValue() != null) {
        setId(entry.getKey(), entry.getValue());
        firePropertyChange(entry.getKey(), null, entry.getValue());
      }
    }
  }

  public void setTitle(String newValue) {
    String oldValue = title;
    title = newValue == null ? "" : newValue.trim();
    firePropertyChange(TITLE, oldValue, newValue);
  }

  public void setOriginalTitle(String newValue) {
    String oldValue = originalTitle;
    originalTitle = newValue == null ? "" : newValue.trim();
    firePropertyChange(ORIGINAL_TITLE, oldValue, newValue);
  }

  public void setPlot(String newValue) {
    String oldValue = plot;
    plot = newValue == null ? "" : newValue.trim();
    firePropertyChange(PLOT, oldValue, newValue);
  }

  public void setPath(String newValue) {
    String oldValue = path;
    path = newValue;
    firePropertyChange(PATH, oldValue, newValue);
  }

  public void setOriginalFilename(String newValue) {
    String oldValue = originalFilename;
    originalFilename = newValue;
    firePropertyChange(ORIGINAL_FILENAME, oldValue, newValue);
  }

  public void removeRating(String id) {
    MediaRating removedMediaRating = ratings.remove(id);
    if (removedMediaRating != null) {
      firePropertyChange(RATING, null, ratings);
    }
  }

  public void clearRatings() {
    ratings.clear();
    firePropertyChange(RATING, null, ratings);
  }

  public void setRatings(Map<String, MediaRating> newRatings) {
    // preserve the user rating here
    MediaRating userMediaRating = ratings.get(MediaRating.USER);

    ratings.clear();
    for (Entry<String, MediaRating> entry : newRatings.entrySet()) {
      setRating(entry.getValue());
    }

    if (userMediaRating != null && !newRatings.containsKey(MediaRating.USER)) {
      setRating(userMediaRating);
    }
  }

  public void setRating(MediaRating mediaRating) {
    if (mediaRating != null && StringUtils.isNotBlank(mediaRating.getId())) {
      ratings.put(mediaRating.getId(), mediaRating);
      firePropertyChange(RATING, null, mediaRating);
    }
  }

  public void setYear(int newValue) {
    int oldValue = year;
    year = newValue;
    firePropertyChange(YEAR, oldValue, newValue);
  }

  public void setArtworkUrl(String url, MediaFileType type) {
    String oldValue = getArtworkUrl(type);

    switch (type) {
      case POSTER:
      case FANART:
      case BANNER:
      case THUMB:
      case CLEARART:
      case DISC:
      case LOGO:
      case CLEARLOGO:
      case CHARACTERART:
      case KEYART:
        if (StringUtils.isBlank(url)) {
          artworkUrlMap.remove(type);
        }
        else {
          artworkUrlMap.put(type, url);
        }
        break;
      default:
        return;
    }

    firePropertyChange(type.name().toLowerCase(Locale.ROOT) + "Url", oldValue, url);
  }

  /**
   * get the artwork url for the desired type
   * 
   * @param type
   *          the artwork type
   * @return the url to the artwork type or an empty string
   */
  public String getArtworkUrl(MediaFileType type) {
    String url = artworkUrlMap.get(type);
    return url == null ? "" : url;
  }

  /**
   * removed the artwork url for the desired type
   * 
   * @param type
   *          the artwork type
   */
  public void removeArtworkUrl(MediaFileType type) {
    artworkUrlMap.remove(type);
  }

  /**
   * get all artwork urls
   * 
   * @return a map containing all urls
   */
  public Map<MediaFileType, String> getArtworkUrls() {
    return artworkUrlMap;
  }

  public void setArtwork(Path file, MediaFileType type) {
    List<MediaFile> images = getMediaFiles(type);
    MediaFile mediaFile = null;
    if (!images.isEmpty()) {
      mediaFile = images.get(0);
      mediaFile.setFile(file);
      mediaFile.gatherMediaInformation(true);
    }
    else {
      mediaFile = new MediaFile(file, type);
      mediaFile.gatherMediaInformation();
      addToMediaFiles(mediaFile);
    }

    firePropertyChange(MEDIA_INFORMATION, false, true);
  }

  /**
   * Get a map of all primary artworks. If there are multiple media files for one artwork type, only the first is returned in the map
   * 
   * @return a map of all found artworks
   */
  public Map<MediaFileType, MediaFile> getArtworkMap() {
    Map<MediaFileType, MediaFile> artworkMap = new HashMap<>();
    List<MediaFile> mfs = getMediaFiles();
    for (MediaFile mf : mfs) {
      if (!mf.isGraphic()) {
        continue;
      }
      if (!artworkMap.containsKey(mf.getType())) {
        artworkMap.put(mf.getType(), mf);
      }
    }
    return artworkMap;
  }

  public Date getDateAdded() {
    return dateAdded;
  }

  public Date getDateAddedForUi() {
    Date date = null;

    switch (Settings.getInstance().getDateField()) {
      case FILE_CREATION_DATE:
        MediaFile mainMediaFile = getMainFile();
        if (mainMediaFile != null) {
          date = mainMediaFile.getDateCreated();
        }
        break;

      case FILE_LAST_MODIFIED_DATE:
        mainMediaFile = getMainFile();
        if (mainMediaFile != null) {
          date = mainMediaFile.getDateLastModified();
        }
        break;

      default:
        date = dateAdded;
        break;

    }

    // sanity check - must not be null
    if (date == null) {
      date = dateAdded;
    }

    return date;
  }

  public String getDateAddedAsString() {
    Date date = getDateAddedForUi();
    if (date == null) {
      return "";
    }

    return TmmDateFormat.MEDIUM_DATE_SHORT_TIME_FORMAT.format(dateAdded);
  }

  public void setDateAdded(Date newValue) {
    Date oldValue = this.dateAdded;
    this.dateAdded = newValue;
    firePropertyChange(DATE_ADDED, oldValue, newValue);
    firePropertyChange(DATE_ADDED_AS_STRING, oldValue, newValue);
  }

  public String getProductionCompany() {
    return productionCompany;
  }

  public void setProductionCompany(String newValue) {
    String oldValue = this.productionCompany;
    this.productionCompany = newValue;
    firePropertyChange(PRODUCTION_COMPANY, oldValue, newValue);
  }

  protected void setScraped(boolean newValue) {
    this.scraped = newValue;
    firePropertyChange(SCRAPED, false, newValue);
  }

  public boolean isScraped() {
    return scraped;
  }

  public void setNote(String newValue) {
    String oldValue = this.note;
    this.note = newValue;
    firePropertyChange("note", oldValue, newValue);
  }

  public String getNote() {
    return this.note;
  }

  public void setDuplicate() {
    this.duplicate = true;
  }

  public void clearDuplicate() {
    this.duplicate = false;
  }

  public boolean isDuplicate() {
    return this.duplicate;
  }

  /**
   * set the given ID; if the value is zero/"" or null, the key is removed from the existing keys
   *
   * @param key
   *          the ID-key
   * @param value
   *          the ID-value
   */
  public void setId(String key, Object value) {
    // remove ID, if empty/0/null
    // if we only skipped it, the existing entry will stay although someone changed it to empty.
    String v = String.valueOf(value);
    if ("".equals(v) || "0".equals(v) || "null".equals(v)) {
      ids.remove(key);
    }
    else {
      ids.put(key, value);
    }
    firePropertyChange(key, null, value);

    // fire special events for our well known IDs
    if (Constants.TMDB.equals(key) || Constants.IMDB.equals(key) || Constants.TVDB.equals(key) || Constants.TRAKT.equals(key)) {
      firePropertyChange(key + "Id", null, value);
    }
  }

  /**
   * remove the given ID
   * 
   * @param key
   *          the ID-key
   */
  public void removeId(String key) {
    Object obj = ids.remove(key);
    if (obj != null) {
      firePropertyChange(key, obj, null);
    }
  }

  /**
   * get the given id
   *
   * @param key
   *          the ID-key
   * @return
   */
  public Object getId(String key) {
    return ids.get(key);
  }

  /**
   * any ID as String or empty
   * 
   * @return the ID-value as String or an empty string
   */
  public String getIdAsString(String key) {
    Object obj = ids.get(key);
    if (obj == null) {
      return "";
    }
    return String.valueOf(obj);
  }

  /**
   * any ID as int or 0
   * 
   * @return the ID-value as int or an empty string
   */
  public int getIdAsInt(String key) {
    Object obj = ids.get(key);
    if (obj == null) {
      return 0;
    }
    if (obj instanceof Integer) {
      return (Integer) obj;
    }

    if (obj instanceof String) {
      try {
        return Integer.parseInt((String) obj);
      }
      catch (Exception e) {
        LOGGER.trace("could not parse int: {}", e.getMessage());
      }
    }

    return 0;
  }

  public void addToMediaFiles(MediaFile mediaFile) {
    readWriteLock.writeLock().lock();
    // only store the MF if it is not in the list or if the type has been changed
    if (mediaFiles.contains(mediaFile)) {
      int i = mediaFiles.indexOf(mediaFile);
      if (i >= 0) {
        MediaFile oldMf = mediaFiles.get(i);
        if (oldMf.getType() != mediaFile.getType()) {
          mediaFiles.remove(i);
        }
      }
    }
    if (!mediaFiles.contains(mediaFile)) {
      mediaFiles.add(mediaFile);
    }

    sortMediaFiles();
    readWriteLock.writeLock().unlock();

    firePropertyChange(MEDIA_FILES, null, mediaFiles);
    fireAddedEventForMediaFile(mediaFile);
  }

  public void addToMediaFiles(List<MediaFile> mediaFiles) {
    for (MediaFile mediaFile : mediaFiles) {
      addToMediaFiles(mediaFile);
    }
  }

  protected void fireAddedEventForMediaFile(MediaFile mediaFile) {
    switch (mediaFile.getType()) {
      case FANART:
        firePropertyChange(FANART, null, mediaFile.getPath());
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case POSTER:
        firePropertyChange(POSTER, null, mediaFile.getPath());
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case BANNER:
        firePropertyChange(BANNER, null, mediaFile.getPath());
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case THUMB:
        firePropertyChange(THUMB, null, mediaFile.getPath());
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case CLEARART:
        firePropertyChange(CLEARART, null, mediaFile.getPath());
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case DISC:
        firePropertyChange(DISC, null, mediaFile.getPath());
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case LOGO:
        firePropertyChange(LOGO, null, mediaFile.getPath());
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case CLEARLOGO:
        firePropertyChange(CLEARLOGO, null, mediaFile.getPath());
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case CHARACTERART:
        firePropertyChange(CHARACTERART, null, mediaFile.getPath());
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case KEYART:
        firePropertyChange(KEYART, null, mediaFile.getPath());
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      default:
        break;
    }
  }

  protected void fireRemoveEventForMediaFile(MediaFile mediaFile) {
    switch (mediaFile.getType()) {
      case FANART:
        firePropertyChange(FANART, null, "");
        firePropertyChange(HAS_IMAGES, true, false);
        break;

      case POSTER:
        firePropertyChange(POSTER, null, "");
        firePropertyChange(HAS_IMAGES, true, false);
        break;

      case BANNER:
        firePropertyChange(BANNER, null, "");
        firePropertyChange(HAS_IMAGES, true, false);
        break;

      case THUMB:
        firePropertyChange(THUMB, null, "");
        firePropertyChange(HAS_IMAGES, true, false);
        break;

      case CLEARART:
        firePropertyChange(CLEARART, null, "");
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case DISC:
        firePropertyChange(DISC, null, "");
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case LOGO:
        firePropertyChange(LOGO, null, "");
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case CLEARLOGO:
        firePropertyChange(CLEARLOGO, null, "");
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case CHARACTERART:
        firePropertyChange(CHARACTERART, null, "");
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      case KEYART:
        firePropertyChange(KEYART, null, "");
        firePropertyChange(HAS_IMAGES, false, true);
        break;

      default:
        break;
    }
  }

  public List<MediaFile> getMediaFiles() {
    List<MediaFile> mf = new ArrayList<>();
    readWriteLock.readLock().lock();
    try {
      mf.addAll(mediaFiles);
    }
    finally {
      readWriteLock.readLock().unlock();
    }
    return mf;
  }

  public boolean hasMediaFiles() {
    return !mediaFiles.isEmpty();
  }

  /**
   * gets all MediaFiles from specific type<br>
   * <b>Can be one or multiple types!</b>
   * 
   * @param types
   *          1-N types
   * @return list of MF (may be empty, but never null)
   */
  public List<MediaFile> getMediaFiles(MediaFileType... types) {
    List<MediaFile> mf = new ArrayList<>();
    readWriteLock.readLock().lock();
    for (MediaFile mediaFile : mediaFiles) {
      boolean match = false;
      for (MediaFileType type : types) {
        if (mediaFile.getType().equals(type)) {
          match = true;
        }
      }
      if (match) {
        mf.add(mediaFile);
      }
    }
    readWriteLock.readLock().unlock();
    return mf;
  }

  /**
   * gets the BIGGEST MediaFile of type(s)<br>
   * useful for getting the right MF for displaying mediaInformation
   * 
   * @return biggest MF
   */
  public MediaFile getBiggestMediaFile(MediaFileType... types) {
    MediaFile mf = null;

    readWriteLock.readLock().lock();
    for (MediaFile mediaFile : mediaFiles) {
      for (MediaFileType type : types) {
        if (mediaFile.getType().equals(type)) {
          if (mf == null || mediaFile.getFilesize() >= mf.getFilesize()) {
            mf = mediaFile;
          }
        }
      }
    }
    readWriteLock.readLock().unlock();
    return mf;
  }

  /**
   * From all MediaFiles of specified type, get the newest one (according to MI filedate)
   * 
   * @param types
   *          the MediaFileTypes to get the MediaFile for
   * @return NULL or MF
   */
  public MediaFile getNewestMediaFilesOfType(MediaFileType... types) {
    MediaFile mf = null;
    readWriteLock.readLock().lock();
    for (MediaFile mediaFile : mediaFiles) {
      for (MediaFileType type : types) {
        if (mediaFile.getType().equals(type)) {
          if (mf == null || mediaFile.getFiledate() >= mf.getFiledate()) {
            // get the latter one
            mf = new MediaFile(mediaFile);
          }
        }
      }
    }
    readWriteLock.readLock().unlock();
    return mf;
  }

  /**
   * gets all MediaFiles EXCEPT from specific type<br>
   * <b>Can be one or multiple types!</b>
   * 
   * @param types
   *          1-N types
   * @return list of MF (may be empty, but never null)
   */
  public List<MediaFile> getMediaFilesExceptType(MediaFileType... types) {
    List<MediaFile> mf = new ArrayList<>();
    readWriteLock.readLock().lock();
    for (MediaFile mediaFile : mediaFiles) {
      boolean match = false;
      for (MediaFileType type : types) {
        if (mediaFile.getType().equals(type)) {
          match = true;
        }
      }
      if (!match) {
        mf.add(mediaFile);
      }
    }
    readWriteLock.readLock().unlock();
    return mf;
  }

  public void removeAllMediaFiles() {
    List<MediaFile> changedMediafiles = new ArrayList<>(mediaFiles);
    readWriteLock.writeLock().lock();
    for (int i = mediaFiles.size() - 1; i >= 0; i--) {
      mediaFiles.remove(i);
    }
    readWriteLock.writeLock().unlock();
    for (MediaFile mediaFile : changedMediafiles) {
      fireRemoveEventForMediaFile(mediaFile);
    }
  }

  public void removeFromMediaFiles(MediaFile mediaFile) {
    readWriteLock.writeLock().lock();
    try {
      mediaFiles.remove(mediaFile);
    }
    finally {
      readWriteLock.writeLock().unlock();
    }

    firePropertyChange(MEDIA_FILES, null, mediaFiles);
    fireRemoveEventForMediaFile(mediaFile);
  }

  public void removeAllMediaFilesExceptType(MediaFileType type) {
    List<MediaFile> changedMediafiles = new ArrayList<>();

    readWriteLock.writeLock().lock();
    for (int i = mediaFiles.size() - 1; i >= 0; i--) {
      MediaFile mediaFile = mediaFiles.get(i);
      if (!mediaFile.getType().equals(type)) {
        mediaFiles.remove(i);
        changedMediafiles.add(mediaFile);
      }
    }
    readWriteLock.writeLock().unlock();
    for (MediaFile mediaFile : changedMediafiles) {
      fireRemoveEventForMediaFile(mediaFile);
    }
  }

  public void removeAllMediaFiles(MediaFileType type) {
    List<MediaFile> changedMediafiles = new ArrayList<>();

    readWriteLock.writeLock().lock();
    for (int i = mediaFiles.size() - 1; i >= 0; i--) {
      MediaFile mediaFile = mediaFiles.get(i);
      if (mediaFile.getType().equals(type)) {
        mediaFiles.remove(i);
        changedMediafiles.add(mediaFile);
      }
    }
    readWriteLock.writeLock().unlock();
    for (MediaFile mediaFile : changedMediafiles) {
      fireRemoveEventForMediaFile(mediaFile);
    }
  }

  /**
   * <b>PHYSICALLY</b> deletes all {@link MediaFile}s of the given type
   *
   * @param type
   *          the {@link MediaFileType} for all {@link MediaFile}s to delete
   */
  public void deleteMediaFiles(MediaFileType type) {
    getMediaFiles(type).forEach(mediaFile -> {
      mediaFile.deleteSafely(getDataSource());
      removeFromMediaFiles(mediaFile);
    });
  }

  public void updateMediaFilePath(Path oldPath, Path newPath) {
    readWriteLock.readLock().lock();
    List<MediaFile> mfs = new ArrayList<>(this.mediaFiles);
    readWriteLock.readLock().unlock();
    for (MediaFile mf : mfs) {
      // invalidate image cache
      if (mf.isGraphic()) {
        ImageCache.invalidateCachedImage(mf);
      }

      mf.replacePathForRenamedFolder(oldPath, newPath);
    }
  }

  public void gatherMediaFileInformation(boolean force) {
    readWriteLock.readLock().lock();
    List<MediaFile> mfs = new ArrayList<>(this.mediaFiles);
    readWriteLock.readLock().unlock();
    for (MediaFile mediaFile : mfs) {
      mediaFile.gatherMediaInformation(force);
    }

    firePropertyChange(MEDIA_INFORMATION, false, true);
  }

  public void fireEventForChangedMediaInformation() {
    firePropertyChange(MEDIA_INFORMATION, false, true);
  }

  public boolean isNewlyAdded() {
    return this.newlyAdded;
  }

  public void setNewlyAdded(boolean newValue) {
    boolean oldValue = this.newlyAdded;
    this.newlyAdded = newValue;
    firePropertyChange(NEWLY_ADDED, oldValue, newValue);
  }

  public void callbackForGatheredMediainformation(MediaFile mediaFile) {
    // empty - to be used in subclasses
  }

  abstract public void saveToDb();

  abstract public void deleteFromDb();

  abstract public void callbackForWrittenArtwork(MediaArtworkType type);

  abstract protected Comparator<MediaFile> getMediaFileComparator();
}
