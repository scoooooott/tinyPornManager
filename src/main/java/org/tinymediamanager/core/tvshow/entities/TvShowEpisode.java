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
package org.tinymediamanager.core.tvshow.entities;

import static org.tinymediamanager.core.Constants.ACTORS;
import static org.tinymediamanager.core.Constants.AIRED_EPISODE;
import static org.tinymediamanager.core.Constants.AIRED_SEASON;
import static org.tinymediamanager.core.Constants.DIRECTORS;
import static org.tinymediamanager.core.Constants.DIRECTORS_AS_STRING;
import static org.tinymediamanager.core.Constants.DISPLAY_EPISODE;
import static org.tinymediamanager.core.Constants.DISPLAY_SEASON;
import static org.tinymediamanager.core.Constants.DVD_EPISODE;
import static org.tinymediamanager.core.Constants.DVD_ORDER;
import static org.tinymediamanager.core.Constants.DVD_SEASON;
import static org.tinymediamanager.core.Constants.EPISODE;
import static org.tinymediamanager.core.Constants.FIRST_AIRED;
import static org.tinymediamanager.core.Constants.FIRST_AIRED_AS_STRING;
import static org.tinymediamanager.core.Constants.HAS_NFO_FILE;
import static org.tinymediamanager.core.Constants.MEDIA_SOURCE;
import static org.tinymediamanager.core.Constants.SEASON;
import static org.tinymediamanager.core.Constants.SEASON_BANNER;
import static org.tinymediamanager.core.Constants.SEASON_POSTER;
import static org.tinymediamanager.core.Constants.SEASON_THUMB;
import static org.tinymediamanager.core.Constants.TAG;
import static org.tinymediamanager.core.Constants.TAGS_AS_STRING;
import static org.tinymediamanager.core.Constants.TITLE_FOR_UI;
import static org.tinymediamanager.core.Constants.TITLE_SORTABLE;
import static org.tinymediamanager.core.Constants.TVDB;
import static org.tinymediamanager.core.Constants.TV_SHOW;
import static org.tinymediamanager.core.Constants.WATCHED;
import static org.tinymediamanager.core.Constants.WRITERS;
import static org.tinymediamanager.core.Constants.WRITERS_AS_STRING;

import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.IMediaInformation;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.ScraperMetadataConfig;
import org.tinymediamanager.core.TmmDateFormat;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.tasks.MediaEntityImageFetcherTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.TvShowEpisodeScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowMediaFileComparator;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowRenamer;
import org.tinymediamanager.core.tvshow.connector.ITvShowEpisodeConnector;
import org.tinymediamanager.core.tvshow.connector.TvShowEpisodeToKodiConnector;
import org.tinymediamanager.core.tvshow.connector.TvShowEpisodeToXbmcConnector;
import org.tinymediamanager.core.tvshow.filenaming.TvShowEpisodeNfoNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowEpisodeThumbNaming;
import org.tinymediamanager.core.tvshow.tasks.TvShowActorImageFetcherTask;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.StrgUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * The Class TvShowEpisode.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisode extends MediaEntity implements Comparable<TvShowEpisode>, IMediaInformation {
  private static final Logger                LOGGER                = LoggerFactory.getLogger(TvShowEpisode.class);
  private static final Comparator<MediaFile> MEDIA_FILE_COMPARATOR = new TvShowMediaFileComparator();

  @JsonProperty
  private int                                episode               = -1;
  @JsonProperty
  @JsonInclude(JsonInclude.Include.ALWAYS)
  private int                                season                = -1;
  @JsonProperty
  private int                                dvdSeason             = -1;
  @JsonProperty
  private int                                dvdEpisode            = -1;
  @JsonProperty
  private int                                displaySeason         = -1;
  @JsonProperty
  private int                                displayEpisode        = -1;
  @JsonProperty
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private Date                               firstAired            = null;
  @JsonProperty
  private boolean                            disc                  = false;
  @JsonProperty
  private boolean                            multiEpisode          = false;
  @JsonProperty
  private boolean                            watched               = false;
  @JsonProperty
  private boolean                            isDvdOrder            = false;
  @JsonProperty
  private UUID                               tvShowId              = null;
  @JsonProperty
  private MediaSource                        mediaSource           = MediaSource.UNKNOWN;                         // DVD, Bluray, etc
  @JsonProperty
  private boolean                            stacked               = false;

  @JsonProperty
  private List<Person>                       actors                = new CopyOnWriteArrayList<>();
  @JsonProperty
  private List<Person>                       directors             = new CopyOnWriteArrayList<>();
  @JsonProperty
  private List<Person>                       writers               = new CopyOnWriteArrayList<>();
  @JsonProperty
  private List<String>                       tags                  = new CopyOnWriteArrayList<>();

  private TvShow                             tvShow                = null;
  private String                             titleSortable         = "";
  private Date                               lastWatched           = null;
  private boolean                            dummy                 = false;

  /**
   * Instantiates a new tv show episode. To initialize the propertychangesupport after loading
   */
  public TvShowEpisode() {
    // register for dirty flag listener
    super();
  }

  /**
   * Overwrites all null/empty elements with "other" value (but might be also empty)<br>
   * For lists, check with 'contains' and add.<br>
   * Do NOT merge path, dateAdded, scraped, mediaFiles and other crucial properties!
   *
   * @param other
   *          the other episode to merge in
   */
  public void merge(TvShowEpisode other) {
    merge(other, false);
  }

  /**
   * Overwrites all elements with "other" value<br>
   * Do NOT merge path, dateAdded, scraped, mediaFiles and other crucial properties!
   *
   * @param other
   *          the other episode to merge in
   */
  public void forceMerge(TvShowEpisode other) {
    merge(other, true);
  }

  void merge(TvShowEpisode other, boolean force) {
    if (other == null) {
      return;
    }
    super.merge(other, force);

    setEpisode(episode < 0 || force ? other.episode : episode);
    setSeason(season < 0 || force ? other.season : season);
    setDvdEpisode(dvdEpisode < 0 || force ? other.dvdEpisode : dvdEpisode);
    setDvdSeason(dvdSeason < 0 || force ? other.dvdSeason : dvdSeason);
    setDisplayEpisode(displayEpisode < 0 || force ? other.displayEpisode : displayEpisode);
    setDisplaySeason(displaySeason < 0 || force ? other.displaySeason : displaySeason);
    setFirstAired(firstAired == null || force ? other.firstAired : firstAired);
    setWatched(!watched || force ? other.watched : watched);
    setMediaSource(mediaSource == MediaSource.UNKNOWN || force ? other.mediaSource : mediaSource);

    if (force) {
      tags.clear();
      actors.clear();
      directors.clear();
      writers.clear();
    }

    setTags(other.tags);
    setActors(other.actors);
    setDirectors(other.directors);
    setWriters(other.writers);
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

  @Override
  protected Comparator<MediaFile> getMediaFileComparator() {
    return MEDIA_FILE_COMPARATOR;
  }

  /**
   * (re)sets the path (when renaming tv show/season folder).<br>
   * Exchanges the beginning path from oldPath with newPath<br>
   */
  public void replacePathForRenamedFolder(Path oldPath, Path newPath) {
    String p = getPathNIO().toAbsolutePath().toString();
    p = p.replace(oldPath.toAbsolutePath().toString(), newPath.toAbsolutePath().toString());
    setPath(p);
  }

  /**
   * create a deep copy of this episode
   * 
   * @param source
   *          the source episode
   */
  public TvShowEpisode(TvShowEpisode source) {
    // the reference to the tv show and the media files are the only things we don't
    // copy
    tvShow = source.tvShow;

    // clone media files
    for (MediaFile mf : source.getMediaFiles()) {
      addToMediaFiles(new MediaFile(mf));
    }

    // clone the rest
    path = source.path;
    title = source.title;
    originalTitle = source.originalTitle;
    year = source.year;
    plot = source.plot;

    for (Entry<MediaFileType, String> entry : source.artworkUrlMap.entrySet()) {
      artworkUrlMap.put(entry.getKey(), entry.getValue());
    }

    dateAdded = new Date(source.dateAdded.getTime());
    scraped = source.scraped;
    ids.putAll(source.ids);

    episode = source.episode;
    season = source.season;
    dvdEpisode = source.dvdEpisode;
    dvdSeason = source.dvdSeason;
    isDvdOrder = source.isDvdOrder;

    if (source.firstAired != null) {
      firstAired = new Date(source.firstAired.getTime());
    }

    disc = source.disc;
    watched = source.watched;

    for (Person actor : source.getActors()) {
      actors.add(new Person(actor));
    }
    for (Person director : source.getDirectors()) {
      directors.add(new Person(director));
    }
    for (Person writer : source.getWriters()) {
      writers.add(new Person(writer));
    }
    for (MediaRating mediaRating : source.getRatings().values()) {
      ratings.put(mediaRating.getId(), new MediaRating(mediaRating));
    }
  }

  /**
   * Returns the sortable variant of title<br>
   * eg "The Luminous Fish Effect" -> "Luminous Fish Effect, The".
   *
   * @return the title in its sortable format
   */
  public String getTitleSortable() {
    if (StringUtils.isBlank(titleSortable)) {
      titleSortable = Utils.getSortableName(getTitle());
    }
    return titleSortable;
  }

  public void clearTitleSortable() {
    titleSortable = "";
  }

  public Date getFirstAired() {
    return firstAired;
  }

  @JsonIgnore
  public void setFirstAired(Date newValue) {
    Date oldValue = this.firstAired;
    this.firstAired = newValue;
    firePropertyChange(FIRST_AIRED, oldValue, newValue);
    firePropertyChange(FIRST_AIRED_AS_STRING, oldValue, newValue);

    // also set the year
    if (firstAired != null) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(firstAired);
      setYear(calendar.get(Calendar.YEAR));
    }
  }

  public TvShowSeason getTvShowSeason() {
    if (tvShow == null) {
      return null;
    }
    return tvShow.getSeasonForEpisode(this);
  }

  public void setTvShowSeason() {
    // dummy for beansbinding
  }

  /**
   * Is this episode in a disc folder structure?.
   * 
   * @return true/false
   */
  public boolean isDisc() {
    return disc;
  }

  /**
   * This episode is in a disc folder structure.
   * 
   * @param disc
   *          true/false
   */
  public void setDisc(boolean disc) {
    this.disc = disc;
  }

  /**
   * is this Episode a MultiEpisode? (same files added to another episode?)
   * 
   * @return true/false
   */
  public boolean isMultiEpisode() {
    return multiEpisode;
  }

  public void setMultiEpisode(boolean multiEpisode) {
    this.multiEpisode = multiEpisode;
  }

  /**
   * first aired date as yyyy-mm-dd<br>
   * https://xkcd.com/1179/ :P
   * 
   * @return the date or empty string
   */
  public String getFirstAiredFormatted() {
    if (this.firstAired == null) {
      return "";
    }
    return new SimpleDateFormat("yyyy-MM-dd").format(this.firstAired);
  }

  /**
   * Gets the first aired as a string, formatted in the system locale.
   * 
   * @return the first aired as string
   */
  public String getFirstAiredAsString() {
    if (this.firstAired == null) {
      return "";
    }
    return TmmDateFormat.MEDIUM_DATE_FORMAT.format(firstAired);
  }

  /**
   * convenient method to set the first aired date (parsed from string).
   * 
   * @param aired
   *          the new first aired
   */
  public void setFirstAired(String aired) {
    try {
      setFirstAired(StrgUtils.parseDate(aired));
    }
    catch (ParseException ignored) {
    }
  }

  public TvShow getTvShow() {
    return tvShow;
  }

  public void setTvShow(TvShow newValue) {
    TvShow oldValue = this.tvShow;
    this.tvShow = newValue;
    this.tvShowId = tvShow.getDbId();
    firePropertyChange(TV_SHOW, oldValue, newValue);
  }

  public UUID getTvShowDbId() {
    return tvShowId;
  }

  @Override
  public String getDataSource() {
    return tvShow.getDataSource();
  }

  public int getEpisode() {
    if (isDvdOrder) {
      return dvdEpisode;
    }
    return episode;
  }

  public int getSeason() {
    if (isDvdOrder) {
      return dvdSeason;
    }
    return season;
  }

  public void setEpisode(int newValue) {
    if (isDvdOrder) {
      setDvdEpisode(newValue);
    }
    else {
      setAiredEpisode(newValue);
    }

    firePropertyChange(TITLE_FOR_UI, "", newValue);
  }

  public void setAiredEpisode(int newValue) {
    int oldValue = this.episode;
    this.episode = newValue;
    if (!isDvdOrder) {
      firePropertyChange(EPISODE, oldValue, newValue);
    }
    firePropertyChange(AIRED_EPISODE, oldValue, newValue);
  }

  public int getAiredEpisode() {
    return this.episode;
  }

  @Override
  public void setTitle(String newValue) {
    String oldValue = this.title;
    super.setTitle(newValue);
    firePropertyChange(TITLE_FOR_UI, oldValue, newValue);

    oldValue = this.titleSortable;
    titleSortable = "";
    firePropertyChange(TITLE_SORTABLE, oldValue, titleSortable);
  }

  public void setSeason(int newValue) {
    if (isDvdOrder) {
      setDvdSeason(newValue);
    }
    else {
      setAiredSeason(newValue);
    }

    firePropertyChange(TITLE_FOR_UI, "", newValue);
  }

  public void setAiredSeason(int newValue) {
    int oldValue = this.season;
    this.season = newValue;
    if (!isDvdOrder) {
      firePropertyChange(SEASON, oldValue, newValue);
    }
    firePropertyChange(AIRED_SEASON, oldValue, newValue);
  }

  public int getAiredSeason() {
    return this.season;
  }

  /**
   * get the "main" rating
   *
   * @return the main (preferred) rating
   */
  @Override
  public MediaRating getRating() {
    MediaRating mediaRating = null;

    // the user rating
    if (TvShowModuleManager.SETTINGS.getPreferPersonalRating()) {
      mediaRating = ratings.get(MediaRating.USER);
    }

    // the default rating
    if (mediaRating == null) {
      mediaRating = ratings.get(TvShowModuleManager.SETTINGS.getPreferredRating());
    }

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

  /**
   * Gets the title for ui.
   * 
   * @return the title for ui
   */
  public String getTitleForUi() {
    StringBuilder titleForUi = new StringBuilder();
    int episode = getEpisode();
    int season = getSeason();
    if (episode > 0 && season > 0) {
      titleForUi.append(String.format("S%02dE%02d - ", season, episode));
    }
    titleForUi.append(title);
    return titleForUi.toString();
  }

  /**
   * Initialize after loading.
   */
  public void initializeAfterLoading() {
    super.initializeAfterLoading();

    // remove empty tag and null values
    Utils.removeEmptyStringsFromList(tags);
  }

  /**
   * download the specified type of artwork for this episode
   *
   * @param type
   *          the chosen artwork type to be downloaded
   */
  public void downloadArtwork(MediaFileType type) {
    switch (type) {
      case THUMB:
        writeThumbImage();
        break;

      default:
        break;
    }
  }

  /**
   * Write thumb image.
   */
  private void writeThumbImage() {
    String thumbUrl = getArtworkUrl(MediaFileType.THUMB);
    if (StringUtils.isNotBlank(thumbUrl)) {
      boolean firstImage = false;

      // create correct filename
      MediaFile mf = getMediaFiles(MediaFileType.VIDEO).get(0);
      String basename = FilenameUtils.getBaseName(mf.getFilename());

      int i = 0;
      for (TvShowEpisodeThumbNaming thumbNaming : TvShowModuleManager.SETTINGS.getEpisodeThumbFilenames()) {
        String filename = thumbNaming.getFilename(basename, Utils.getArtworkExtension(thumbUrl));
        if (StringUtils.isBlank(filename)) {
          continue;
        }
        if (isDisc()) {
          filename = "thumb." + FilenameUtils.getExtension(thumbUrl); // DVD/BluRay fixate to thumb.ext
        }

        if (++i == 1) {
          firstImage = true;
        }

        // get image in thread
        MediaEntityImageFetcherTask task = new MediaEntityImageFetcherTask(this, thumbUrl, MediaArtworkType.THUMB, filename, firstImage);
        TmmTaskManager.getInstance().addImageDownloadTask(task);
      }
    }

    // if that has been a local file, remove it from the artwork urls after we've
    // already started the download(copy) task
    if (thumbUrl.startsWith("file:")) {
      removeArtworkUrl(MediaFileType.THUMB);
    }
  }

  /**
   * Sets the metadata.
   * 
   * @param metadata
   *          the new metadata
   */
  public void setMetadata(MediaMetadata metadata, List<TvShowEpisodeScraperMetadataConfig> config) {
    // check against null metadata (e.g. aborted request)
    if (metadata == null) {
      LOGGER.error("metadata was null");
      return;
    }

    boolean writeNewThumb = false;

    // populate ids

    // here we have two flavors:
    // a) we did a search, so all existing ids should be different to to new ones -> remove old ones
    // b) we did just a scrape (probably with another scraper). we should have at least one id in the episode which matches the ids from the metadata
    // ->
    // merge ids

    // search for existing ids
    boolean matchFound = false;
    for (Map.Entry<String, Object> entry : metadata.getIds().entrySet()) {
      if (entry.getValue() != null && entry.getValue().equals(getId(entry.getKey()))) {
        matchFound = true;
        break;
      }
    }

    if (!matchFound) {
      // clear the old ids to set only the new ones
      ids.clear();
    }

    setIds(metadata.getIds());

    if (config.contains(TvShowEpisodeScraperMetadataConfig.TITLE)) {
      // Capitalize first letter of original title if setting is set!
      if (TvShowModuleManager.SETTINGS.getCapitalWordsInTitles()) {
        setTitle(WordUtils.capitalize(metadata.getTitle()));
      }
      else {
        setTitle(metadata.getTitle());
      }
    }

    if (config.contains(TvShowEpisodeScraperMetadataConfig.ORIGINAL_TITLE)) {
      // Capitalize first letter of original title if setting is set!
      if (TvShowModuleManager.SETTINGS.getCapitalWordsInTitles()) {
        setOriginalTitle(WordUtils.capitalize(metadata.getOriginalTitle()));
      }
      else {
        setOriginalTitle(metadata.getOriginalTitle());
      }
    }

    if (config.contains(TvShowEpisodeScraperMetadataConfig.PLOT)) {
      setPlot(metadata.getPlot());
    }

    if (config.contains(TvShowEpisodeScraperMetadataConfig.AIRED_SEASON_EPISODE)) {
      setAiredSeason(metadata.getSeasonNumber());
      setAiredEpisode(metadata.getEpisodeNumber());
    }

    if (config.contains(TvShowEpisodeScraperMetadataConfig.DVD_SEASON_EPISODE)) {
      setDvdSeason(metadata.getDvdSeasonNumber());
      setDvdEpisode(metadata.getDvdEpisodeNumber());
    }

    if (config.contains(TvShowEpisodeScraperMetadataConfig.DISPLAY_SEASON_EPISODE)) {
      setDisplaySeason(metadata.getDisplaySeasonNumber());
      setDisplayEpisode(metadata.getDisplayEpisodeNumber());
    }

    if (config.contains(TvShowEpisodeScraperMetadataConfig.AIRED)) {
      setFirstAired(metadata.getReleaseDate());
    }

    if (config.contains(TvShowEpisodeScraperMetadataConfig.RATING)) {
      Map<String, MediaRating> newRatings = new HashMap<>();
      for (MediaRating mediaRating : metadata.getRatings()) {
        newRatings.put(mediaRating.getId(), mediaRating);
      }
      setRatings(newRatings);
    }

    if (config.contains(TvShowEpisodeScraperMetadataConfig.TAGS)) {
      setTags(metadata.getTags());
    }

    if (ScraperMetadataConfig.containsAnyCast(config)) {
      if (config.contains(TvShowEpisodeScraperMetadataConfig.ACTORS)) {
        setActors(metadata.getCastMembers(Person.Type.ACTOR));
        writeActorImages();
      }

      if (config.contains(TvShowEpisodeScraperMetadataConfig.DIRECTORS)) {
        setDirectors(metadata.getCastMembers(Person.Type.DIRECTOR));
      }

      if (config.contains(TvShowEpisodeScraperMetadataConfig.WRITERS)) {
        setWriters(metadata.getCastMembers(Person.Type.WRITER));
      }
    }

    if (config.contains(TvShowEpisodeScraperMetadataConfig.THUMB)) {
      List<MediaArtwork> mas = metadata.getMediaArt(MediaArtworkType.THUMB);
      if (!mas.isEmpty()) {
        setArtworkUrl(mas.get(0).getDefaultUrl(), MediaFileType.THUMB);
        writeNewThumb = true;
      }
    }

    // update DB
    writeNFO();
    saveToDb();

    // rename the episode if that has been chosen in the settings
    if (TvShowModuleManager.SETTINGS.isRenameAfterScrape()) {
      TvShowRenamer.renameEpisode(this);
    }

    // should we write a new thumb?
    if (writeNewThumb) {
      writeThumbImage();
    }
  }

  /**
   * Write nfo.
   */
  public void writeNFO() {
    List<TvShowEpisodeNfoNaming> nfoNamings = TvShowModuleManager.SETTINGS.getEpisodeNfoFilenames();
    if (nfoNamings.isEmpty()) {
      return;
    }

    List<TvShowEpisode> episodesInNfo = new ArrayList<>(1);

    LOGGER.debug("write nfo: " + getTvShow().getTitle() + " S" + getSeason() + "E" + getEpisode());
    // worst case: multi episode in multiple files
    // e.g. warehouse13.s01e01e02.Part1.avi/warehouse13.s01e01e02.Part2.avi
    for (MediaFile mf : getMediaFiles(MediaFileType.VIDEO)) {
      List<TvShowEpisode> eps = new ArrayList<>(TvShowList.getTvEpisodesByFile(tvShow, mf.getFile()));
      for (TvShowEpisode ep : eps) {
        if (!episodesInNfo.contains(ep)) {
          episodesInNfo.add(ep);
        }
      }
    }

    ITvShowEpisodeConnector connector = null;

    switch (TvShowModuleManager.SETTINGS.getTvShowConnector()) {
      case KODI:
        connector = new TvShowEpisodeToKodiConnector(episodesInNfo);
        break;

      case XBMC:
      default:
        connector = new TvShowEpisodeToXbmcConnector(episodesInNfo);
        break;
    }

    if (connector != null) {
      connector.write(Collections.singletonList(TvShowEpisodeNfoNaming.FILENAME));
    }

    firePropertyChange(HAS_NFO_FILE, false, true);
  }

  /**
   * Gets the checks for nfo file.
   * 
   * @return the checks for nfo file
   */
  public Boolean getHasNfoFile() {
    List<MediaFile> nfos = getMediaFiles(MediaFileType.NFO);
    return nfos != null && !nfos.isEmpty();
  }

  /**
   * Gets the check mark for images. What to be checked is configurable
   * 
   * @return the checks for images
   */
  public Boolean getHasImages() {
    for (MediaArtworkType type : TvShowModuleManager.SETTINGS.getEpisodeCheckImages()) {
      if (StringUtils.isBlank(getArtworkFilename(MediaFileType.getMediaFileType(type)))) {
        return false;
      }
    }
    return true;
  }

  /**
   * add an actor.
   * 
   * @param newActor
   *          the actor to be added
   */
  public void addActor(Person newActor) {
    actors.add(newActor);
    firePropertyChange(ACTORS, null, this.getActors());
  }

  /**
   * remove the given actor.
   *
   * @param actor
   *          the actor to be removed
   */
  public void removeActor(Person actor) {
    actors.remove(actor);
    firePropertyChange(ACTORS, null, this.getActors());
  }

  /**
   * get the actors. These are the main actors of the TV show inclusive the guests of this episode
   *
   * @return the actors of this episode
   */
  public List<Person> getActors() {
    List<Person> allActors = new ArrayList<>();
    if (tvShow != null) {
      allActors.addAll(tvShow.getActors());
    }
    allActors.addAll(actors);
    return allActors;
  }

  /**
   * get all guests in this episode
   * 
   * @return a list of all guests
   */
  public List<Person> getGuests() {
    return actors;
  }

  /**
   * Sets the actors.
   * 
   * @param newActors
   *          the new actors
   */
  @JsonSetter
  public void setActors(List<Person> newActors) {
    // do not add actors which are in the TV show itself
    // tvShow is null while loading
    if (getTvShow() != null) {
      for (Person actor : getTvShow().getActors()) {
        newActors.remove(actor);
      }
    }

    // two way sync of actors
    ListUtils.mergeLists(actors, newActors);
    firePropertyChange(ACTORS, null, this.getActors());
  }

  /**
   * add a director
   *
   * @param director
   *          the director to be added
   */
  public void addDirector(Person director) {
    directors.add(director);
    firePropertyChange(DIRECTORS, null, this.getDirectors());
    firePropertyChange(DIRECTORS_AS_STRING, null, this.getDirectorsAsString());
  }

  /**
   * remove the given director.
   *
   * @param director
   *          the director to be removed
   */
  public void removeDirector(Person director) {
    directors.remove(director);
    firePropertyChange(DIRECTORS, null, this.getDirectors());
    firePropertyChange(DIRECTORS_AS_STRING, null, this.getDirectorsAsString());
  }

  /**
   * Sets the directors.
   *
   * @param newDirectors
   *          the new directors
   */
  @JsonSetter
  public void setDirectors(List<Person> newDirectors) {
    // two way sync of directors
    ListUtils.mergeLists(directors, newDirectors);

    firePropertyChange(DIRECTORS, null, this.getDirectors());
    firePropertyChange(DIRECTORS_AS_STRING, null, this.getDirectorsAsString());
  }

  /**
   * get the directors.
   *
   * @return the directors
   */
  public List<Person> getDirectors() {
    return directors;
  }

  /**
   * get the directors as string
   *
   * @return a string containing all directors; separated by ,
   */
  public String getDirectorsAsString() {
    List<String> directorNames = new ArrayList<>();
    for (Person director : directors) {
      directorNames.add(director.getName());
    }
    return StringUtils.join(directorNames, ", ");
  }

  /**
   * add a writer
   *
   * @param writer
   *          the writer to be added
   */
  public void addWriter(Person writer) {
    writers.add(writer);
    firePropertyChange(WRITERS, null, this.getWriters());
    firePropertyChange(WRITERS_AS_STRING, null, this.getWritersAsString());
  }

  /**
   * remove the given writer.
   *
   * @param writer
   *          the writer to be removed
   */
  public void removeWriter(Person writer) {
    writers.remove(writer);
    firePropertyChange(WRITERS, null, this.getWriters());
    firePropertyChange(WRITERS_AS_STRING, null, this.getWritersAsString());
  }

  /**
   * Sets the writers.
   *
   * @param newWriters
   *          the new writers
   */
  @JsonSetter
  public void setWriters(List<Person> newWriters) {
    // two way sync of writers
    ListUtils.mergeLists(writers, newWriters);

    firePropertyChange(WRITERS, null, this.getWriters());
    firePropertyChange(WRITERS_AS_STRING, null, this.getWritersAsString());
  }

  /**
   * Gets the writers.
   *
   * @return the writers
   */
  public List<Person> getWriters() {
    return writers;
  }

  /**
   * get the writers as string
   *
   * @return a string containing all writers; separated by ,
   */
  public String getWritersAsString() {
    List<String> writerNames = new ArrayList<>();
    for (Person writer : writers) {
      writerNames.add(writer.getName());
    }
    return StringUtils.join(writerNames, ", ");
  }

  /**
   * Checks if is watched.
   * 
   * @return true, if is watched
   */
  public boolean isWatched() {
    return watched;
  }

  /**
   * Sets the watched.
   * 
   * @param newValue
   *          the new watched
   */
  public void setWatched(boolean newValue) {
    boolean oldValue = this.watched;
    this.watched = newValue;
    firePropertyChange(WATCHED, oldValue, newValue);
  }

  public Date getLastWatched() {
    return lastWatched;
  }

  public void setLastWatched(Date lastWatched) {
    this.lastWatched = lastWatched;
  }

  /**
   * Gets the media info audio codec (i.e mp3) and channels (i.e. 6 at 5.1 sound)
   * 
   * @return the media info audio codec
   */
  public String getMediaInfoAudioCodecAndChannels() {
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (!videos.isEmpty()) {
      MediaFile mediaFile = videos.get(0);
      return mediaFile.getAudioCodec() + "_" + mediaFile.getAudioChannels();
    }

    return "";
  }

  /**
   * get all video files for that episode
   *
   * @return a list of all video files
   */
  public List<MediaFile> getVideoFiles() {
    return getMediaFiles(MediaFileType.VIDEO);
  }

  /**
   * Gets the images to cache.
   * 
   * @return the images to cache
   */
  public List<MediaFile> getImagesToCache() {
    // get files to cache
    List<MediaFile> filesToCache = new ArrayList<>();

    for (MediaFile mf : new ArrayList<>(getMediaFiles())) {
      if (mf.isGraphic()) {
        filesToCache.add(mf);
      }
    }

    return filesToCache;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(TvShowEpisode otherTvShowEpisode) {
    if (getTvShow() != otherTvShowEpisode.getTvShow()) {
      return getTvShow().getTitle().compareTo(otherTvShowEpisode.getTvShow().getTitle());
    }

    if (getSeason() != otherTvShowEpisode.getSeason()) {
      return getSeason() - otherTvShowEpisode.getSeason();
    }

    if (getEpisode() != otherTvShowEpisode.getEpisode()) {
      return getEpisode() - otherTvShowEpisode.getEpisode();
    }

    // still nothing found? wtf - maybe some of those -1/-1 eps
    String filename1 = "";
    try {
      filename1 = getMediaFiles(MediaFileType.VIDEO).get(0).getFilename();
    }
    catch (Exception ignored) {
    }

    String filename2 = "";
    try {
      filename2 = otherTvShowEpisode.getMediaFiles(MediaFileType.VIDEO).get(0).getFilename();
    }
    catch (Exception ignored) {
    }
    return filename1.compareTo(filename2);
  }

  public List<MediaFile> getMediaFilesContainingAudioStreams() {
    List<MediaFile> mediaFilesWithAudioStreams = new ArrayList<>(1);

    // get the audio streams from the first video file
    List<MediaFile> videoFiles = getMediaFiles(MediaFileType.VIDEO);
    if (!videoFiles.isEmpty()) {
      MediaFile videoFile = videoFiles.get(0);
      mediaFilesWithAudioStreams.add(videoFile);
    }

    // get all extra audio streams
    mediaFilesWithAudioStreams.addAll(getMediaFiles(MediaFileType.AUDIO));

    return mediaFilesWithAudioStreams;
  }

  public List<MediaFile> getMediaFilesContainingSubtitles() {
    List<MediaFile> mediaFilesWithSubtitles = new ArrayList<>(1);

    for (MediaFile mediaFile : getMediaFiles(MediaFileType.VIDEO, MediaFileType.SUBTITLE)) {
      if (mediaFile.hasSubtitles()) {
        mediaFilesWithSubtitles.add(mediaFile);
      }
    }

    return mediaFilesWithSubtitles;
  }

  public boolean getHasSubtitles() {
    if (!getMediaFiles(MediaFileType.SUBTITLE).isEmpty()) {
      return true;
    }

    for (MediaFile mf : getMediaFiles(MediaFileType.VIDEO)) {
      if (mf.hasSubtitles()) {
        return true;
      }
    }

    return false;
  }

  public int getRuntimeFromMediaFiles() {
    int runtime = 0;
    for (MediaFile mf : getMediaFiles(MediaFileType.VIDEO)) {
      runtime += mf.getDuration();
    }
    return runtime;
  }

  public int getRuntimeFromMediaFilesInMinutes() {
    return getRuntimeFromMediaFiles() / 60;
  }

  @Override
  public synchronized void callbackForWrittenArtwork(MediaArtworkType type) {
  }

  @Override
  public void saveToDb() {
    // update/insert this episode to the database
    TvShowList.getInstance().persistEpisode(this);
  }

  @Override
  public void deleteFromDb() {
    // delete this episode from the database
    TvShowList.getInstance().removeEpisodeFromDb(this);
  }

  /**
   * Event to trigger a season artwork changed for the UI
   */
  void setSeasonArtworkChanged(MediaArtworkType type) {
    switch (type) {
      case SEASON_POSTER:
        firePropertyChange(SEASON_POSTER, null, "");
        break;

      case SEASON_BANNER:
        firePropertyChange(SEASON_BANNER, null, "");
        break;

      case SEASON_THUMB:
        firePropertyChange(SEASON_THUMB, null, "");
        break;

      default:
        break;
    }
  }

  /**
   * checks if this TV show has been scraped.<br>
   * On a fresh DB, just reading local files, everything is again "unscraped". <br>
   * detect minimum of filled values as "scraped"
   * 
   * @return isScraped
   */
  @Override
  public boolean isScraped() {
    if (!scraped && !plot.isEmpty() && firstAired != null && getSeason() > -1 && getEpisode() > -1) {
      return true;
    }
    return scraped;
  }

  /**
   * Adds the to tags.
   * 
   * @param newTag
   *          the new tag
   */
  public void addToTags(String newTag) {
    if (StringUtils.isBlank(newTag)) {
      return;
    }

    for (String tag : tags) {
      if (tag.equals(newTag)) {
        return;
      }
    }

    tags.add(newTag);
    firePropertyChange(TAG, null, newTag);
    firePropertyChange(TAGS_AS_STRING, null, newTag);
  }

  /**
   * Removes the from tags.
   * 
   * @param removeTag
   *          the remove tag
   */
  public void removeFromTags(String removeTag) {
    tags.remove(removeTag);
    firePropertyChange(TAG, null, removeTag);
    firePropertyChange(TAGS_AS_STRING, null, removeTag);
  }

  /**
   * Sets the tags.
   * 
   * @param newTags
   *          the new tags
   */
  @JsonSetter
  public void setTags(List<String> newTags) {
    // two way sync of tags
    ListUtils.mergeLists(tags, newTags);

    firePropertyChange(TAG, null, newTags);
    firePropertyChange(TAGS_AS_STRING, null, newTags);
  }

  /**
   * Gets the tag as string.
   * 
   * @return the tag as string
   */
  public String getTagsAsString() {
    StringBuilder sb = new StringBuilder();
    for (String tag : tags) {
      if (!StringUtils.isEmpty(sb)) {
        sb.append(", ");
      }
      sb.append(tag);
    }
    return sb.toString();
  }

  /**
   * Gets the tvdb id.
   * 
   * @return the tvdb id
   */
  public String getTvdbId() {
    Object obj = ids.get(TVDB);
    if (obj == null) {
      return "";
    }
    return obj.toString();
  }

  public List<String> getTags() {
    return this.tags;
  }

  public int getDvdSeason() {
    return dvdSeason;
  }

  public void setDvdSeason(int newValue) {
    int oldValue = this.dvdSeason;
    this.dvdSeason = newValue;
    if (isDvdOrder) {
      firePropertyChange(SEASON, oldValue, newValue);
    }
    firePropertyChange(DVD_SEASON, oldValue, newValue);
  }

  public int getDvdEpisode() {
    return dvdEpisode;
  }

  public void setDvdEpisode(int newValue) {
    int oldValue = this.dvdEpisode;
    this.dvdEpisode = newValue;
    if (isDvdOrder) {
      firePropertyChange(EPISODE, oldValue, newValue);
    }
    firePropertyChange(DVD_EPISODE, oldValue, newValue);
  }

  public void setDisplaySeason(int newValue) {
    int oldValue = this.displaySeason;
    this.displaySeason = newValue;
    firePropertyChange(DISPLAY_SEASON, oldValue, newValue);
  }

  public int getDisplaySeason() {
    return displaySeason;
  }

  public void setDisplayEpisode(int newValue) {
    int oldValue = this.displayEpisode;
    this.displayEpisode = newValue;
    firePropertyChange(DISPLAY_EPISODE, oldValue, newValue);
  }

  public int getDisplayEpisode() {
    return displayEpisode;
  }

  /**
   * is this episode in DVD order?
   * 
   * @return episode in DVD order
   */
  public boolean isDvdOrder() {
    return isDvdOrder;
  }

  public void setDvdOrder(boolean newValue) {
    boolean oldValue = this.isDvdOrder;
    this.isDvdOrder = newValue;
    firePropertyChange(DVD_ORDER, oldValue, newValue);
  }

  public MediaSource getMediaSource() {
    return mediaSource;
  }

  public void setMediaSource(MediaSource newValue) {
    MediaSource oldValue = this.mediaSource;
    this.mediaSource = newValue;
    firePropertyChange(MEDIA_SOURCE, oldValue, newValue);
  }

  /**
   * gets the basename (without stacking)
   *
   * @return the video base name (without stacking)
   */
  public String getVideoBasenameWithoutStacking() {
    MediaFile mf = getMediaFiles(MediaFileType.VIDEO).get(0);
    return FilenameUtils.getBaseName(mf.getFilenameWithoutStacking());
  }

  /**
   * <b>PHYSICALLY</b> deletes a complete episode by moving it to datasource backup folder<br>
   * DS\.backup\&lt;moviename&gt;
   */
  public boolean deleteFilesSafely() {
    boolean result = true;

    List<MediaFile> mediaFiles = getMediaFiles();
    for (MediaFile mf : mediaFiles) {
      if (!mf.deleteSafely(tvShow.getDataSource())) {
        result = false;
      }
    }

    return result;
  }

  @Override
  public MediaCertification getCertification() {
    // we do not have a dedicated certification for the episode
    return null;
  }

  @Override
  public MediaFile getMainVideoFile() {
    MediaFile vid = null;

    if (stacked) {
      // search the first stacked media file (e.g. CD1)
      vid = getMediaFiles(MediaFileType.VIDEO).stream().min(Comparator.comparingInt(MediaFile::getStacking)).orElse(new MediaFile());
    }
    else {
      // get the biggest one
      vid = getBiggestMediaFile(MediaFileType.VIDEO);
    }

    if (vid != null) {
      return vid;
    }

    // cannot happen - movie MUST always have a video file
    return new MediaFile();
  }

  @Override
  public MediaFile getMainFile() {
    return getMainVideoFile();
  }

  @Override
  public String getMediaInfoVideoResolution() {
    return getMainVideoFile().getVideoResolution();
  }

  @Override
  public String getMediaInfoVideoFormat() {
    return getMainVideoFile().getVideoFormat();
  }

  @Override
  public String getMediaInfoVideoCodec() {
    return getMainVideoFile().getVideoCodec();
  }

  @Override
  public float getMediaInfoAspectRatio() {
    return getMainVideoFile().getAspectRatio();
  }

  @Override
  public String getMediaInfoAudioCodec() {
    return getMainVideoFile().getAudioCodec();
  }

  @Override
  public List<String> getMediaInfoAudioCodecList() {
    return getMainVideoFile().getAudioCodecList();
  }

  @Override
  public double getMediaInfoFrameRate() {
    return getMainVideoFile().getFrameRate();
  }

  @Override
  public String getMediaInfoAudioChannels() {
    return getMainVideoFile().getAudioChannels();
  }

  @Override
  public List<String> getMediaInfoAudioChannelList() {
    return getMainVideoFile().getAudioChannelsList();
  }

  @Override
  public String getMediaInfoAudioLanguage() {
    return getMainVideoFile().getAudioLanguage();
  }

  @Override
  public int getMediaInfoVideoBitDepth() {
    return getMainVideoFile().getBitDepth();
  }

  @Override
  public List<String> getMediaInfoAudioLanguageList() {
    return getMainVideoFile().getAudioLanguagesList();
  }

  @Override
  public List<String> getMediaInfoSubtitleLanguageList() {
    return getMainVideoFile().getSubtitleLanguagesList();
  }

  @Override
  public String getMediaInfoContainerFormat() {
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (!videos.isEmpty()) {
      MediaFile mediaFile = videos.get(0);
      return mediaFile.getContainerFormat();
    }

    return "";
  }

  @Override
  public MediaSource getMediaInfoSource() {
    return getMediaSource();
  }

  @Override
  public String getVideoHDRFormat() {
    return getMainVideoFile().getHdrFormat();
  }

  @Override
  public boolean isVideoIn3D() {
    String video3DFormat = "";
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (!videos.isEmpty()) {
      MediaFile mediaFile = videos.get(0);
      video3DFormat = mediaFile.getVideo3DFormat();
    }

    return StringUtils.isNotBlank(video3DFormat);
  }

  @Override
  public long getVideoFilesize() {
    long filesize = 0;
    for (MediaFile mf : getMediaFiles(MediaFileType.VIDEO)) {
      filesize += mf.getFilesize();
    }
    return filesize;
  }

  public boolean isDummy() {
    return dummy || !hasMediaFiles();
  }

  public void setDummy(boolean dummy) {
    this.dummy = dummy;
  }

  public String getNfoFilename(TvShowEpisodeNfoNaming nfoNaming) {
    List<MediaFile> mfs = getMediaFiles(MediaFileType.VIDEO);
    MediaFile firstMediaFile = mfs.get(0);

    String baseName = "";
    if (isDisc()) {
      if (firstMediaFile.isBlurayFile()) {
        baseName = "BDMV.nfo"; // dunno, but more correct
      }
      if (firstMediaFile.isDVDFile()) {
        baseName = "VIDEO_TS.nfo";
      }
      if (firstMediaFile.isHdDVDFile()) {
        baseName = "HVDVD_TS.nfo";
      }
    }
    else {
      baseName = firstMediaFile.getBasename();
    }
    return nfoNaming.getFilename(baseName, "nfo");
  }

  /**
   * Is the epsiode "stacked" (more than one video file)
   *
   * @return true if the episode is stacked; false otherwise
   */
  public boolean isStacked() {
    return stacked;
  }

  public void setStacked(boolean stacked) {
    this.stacked = stacked;
  }

  /**
   * get the runtime. Just a wrapper to tvShow.getRuntime() until we support separate runtimes for episodes
   *
   * @return the runtime in minutes
   */
  public int getRuntime() {
    return tvShow.getRuntime();
  }

  /**
   * return the TV shows production company if no one is filled for this episode
   *
   * @return the production company
   */
  @Override
  public String getProductionCompany() {
    if (StringUtils.isNotBlank(productionCompany)) {
      return productionCompany;
    }
    if (tvShow != null) {
      return tvShow.getProductionCompany();
    }
    return "";
  }

  /**
   * Write actor images.
   */
  public void writeActorImages() {
    // check if actor images shall be written
    if (!TvShowModuleManager.SETTINGS.isWriteActorImages()) {
      return;
    }

    TvShowActorImageFetcherTask task = new TvShowActorImageFetcherTask(this);
    TmmTaskManager.getInstance().addImageDownloadTask(task);
  }

  /**
   * ok, we might have detected some stacking MFs.<br>
   * But if we only have ONE video file, reset stacking markers in this case<br>
   */
  public void reEvaluateStacking() {
    List<MediaFile> mfs = getMediaFiles(MediaFileType.VIDEO);
    if (mfs.size() > 1 && !isDisc()) {
      // ok, more video files means stacking (if not a disc folder)
      this.setStacked(true);
      for (MediaFile mf : getMediaFiles(MediaFileType.VIDEO, MediaFileType.AUDIO, MediaFileType.SUBTITLE)) {
        mf.detectStackingInformation();
      }
    }
    else {
      // only ONE video? remove any stacking markers from MFs
      this.setStacked(false);
      for (MediaFile mf : getMediaFiles(MediaFileType.VIDEO, MediaFileType.AUDIO, MediaFileType.SUBTITLE)) {
        mf.removeStackingInformation();
      }
    }
  }

  @Override
  protected void fireAddedEventForMediaFile(MediaFile mediaFile) {
    super.fireAddedEventForMediaFile(mediaFile);

    // episode related media file types
    if (mediaFile.getType() == MediaFileType.SUBTITLE) {
      firePropertyChange("hasSubtitle", false, true);
    }
  }

  @Override
  protected void fireRemoveEventForMediaFile(MediaFile mediaFile) {
    super.fireRemoveEventForMediaFile(mediaFile);

    // episode related media file types
    if (mediaFile.getType() == MediaFileType.SUBTITLE) {
      firePropertyChange("hasSubtitle", true, false);
    }
  }
}
