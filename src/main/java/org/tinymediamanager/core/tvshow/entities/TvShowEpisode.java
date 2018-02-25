/*
 * Copyright 2012 - 2018 Manuel Laggner
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.IMediaInformation;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.entities.Rating;
import org.tinymediamanager.core.tasks.MediaEntityImageFetcherTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowMediaFileComparator;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.connector.ITvShowEpisodeConnector;
import org.tinymediamanager.core.tvshow.connector.TvShowEpisodeToKodiConnector;
import org.tinymediamanager.core.tvshow.connector.TvShowEpisodeToXbmcConnector;
import org.tinymediamanager.core.tvshow.filenaming.TvShowEpisodeNfoNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowEpisodeThumbNaming;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaRating;
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
  private boolean                            subtitles             = false;
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
    // the reference to the tv show and the media files are the only things we don't copy
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
    subtitles = source.subtitles;

    for (Person actor : source.getActors()) {
      actors.add(new Person(actor));
    }
    for (Person director : source.getDirectors()) {
      directors.add(new Person(director));
    }
    for (Person writer : source.getWriters()) {
      writers.add(new Person(writer));
    }
    for (Rating rating : source.getRatings().values()) {
      ratings.put(rating.getId(), new Rating(rating));
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
    return SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(firstAired);
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
    catch (ParseException e) {
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
  public Rating getRating() {
    Rating rating = null;

    // the user rating
    if (TvShowModuleManager.SETTINGS.getPreferPersonalRating()) {
      rating = ratings.get(Rating.USER);
    }

    // the default rating
    if (rating == null) {
      rating = ratings.get(TvShowModuleManager.SETTINGS.getPreferredRating());
    }

    // then the default one (either NFO or DEFAULT)
    if (rating == null) {
      rating = ratings.get(Rating.NFO);
    }
    if (rating == null) {
      rating = ratings.get(Rating.DEFAULT);
    }

    // is there any rating?
    if (rating == null && !ratings.isEmpty()) {
      for (Rating r : ratings.values()) {
        rating = r;
        break;
      }
    }

    // last but not least a non null value
    if (rating == null) {
      rating = new Rating();
    }

    return rating;
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
   * Write thumb image.
   */
  public void writeThumbImage() {
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
  }

  /**
   * Sets the metadata.
   * 
   * @param metadata
   *          the new metadata
   */
  public void setMetadata(MediaMetadata metadata) {
    // check against null metadata (e.g. aborted request)
    if (metadata == null) {
      LOGGER.error("metadata was null");
      return;
    }

    boolean writeNewThumb = false;

    setTitle(metadata.getTitle());
    setPlot(metadata.getPlot());
    setIds(metadata.getIds());

    setAiredSeason(metadata.getSeasonNumber());
    setAiredEpisode(metadata.getEpisodeNumber());
    setDvdSeason(metadata.getDvdSeasonNumber());
    setDvdEpisode(metadata.getDvdEpisodeNumber());
    setFirstAired(metadata.getReleaseDate());
    setDisplaySeason(metadata.getDisplaySeasonNumber());
    setDisplayEpisode(metadata.getDisplayEpisodeNumber());

    clearRatings();
    for (MediaRating mediaRating : metadata.getRatings()) {
      setRating(new Rating(mediaRating));
    }

    List<Person> actors = new ArrayList<>();
    List<Person> directors = new ArrayList<>();
    List<Person> writers = new ArrayList<>();

    for (MediaCastMember member : metadata.getCastMembers()) {
      switch (member.getType()) {
        case ACTOR:
          actors.add(new Person(member));
          break;

        case DIRECTOR:
          directors.add(new Person(member));
          break;

        case WRITER:
          writers.add(new Person(member));
          break;

        default:
          break;
      }
    }
    setActors(actors);
    setDirectors(directors);
    setWriters(writers);

    for (MediaArtwork ma : metadata.getMediaArt(MediaArtworkType.THUMB)) {
      setArtworkUrl(ma.getDefaultUrl(), MediaFileType.THUMB);
      writeNewThumb = true;
      break;
    }

    // update DB
    writeNFO();
    saveToDb();

    // should we write a new thumb?
    if (writeNewThumb) {
      writeThumbImage();
    }
  }

  /**
   * Write nfo.
   */
  public void writeNFO() {
    List<TvShowEpisode> episodesInNfo = new ArrayList<>(1);

    LOGGER.debug("write nfo: " + getTvShow().getTitle() + " S" + getSeason() + "E" + getEpisode());
    // worst case: multi episode in multiple files
    // e.g. warehouse13.s01e01e02.Part1.avi/warehouse13.s01e01e02.Part2.avi
    for (MediaFile mf : getMediaFiles(MediaFileType.VIDEO)) {
      List<TvShowEpisode> eps = new ArrayList<>(TvShowList.getInstance().getTvEpisodesByFile(tvShow, mf.getFile()));
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
      connector.write(Arrays.asList(TvShowEpisodeNfoNaming.FILENAME));
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
    if (nfos != null && nfos.size() > 0) {
      return true;
    }
    return false;
  }

  /**
   * Gets the checks for images.
   * 
   * @return the checks for images
   */
  public Boolean getHasImages() {
    if (StringUtils.isNotEmpty(getArtworkFilename(MediaFileType.THUMB))) {
      return true;
    }
    return false;
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
    if (videos.size() > 0) {
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
   * get the first video file for this episode
   *
   * @return the first video file
   */
  public MediaFile getFirstVideoFile() {
    List<MediaFile> videoFiles = getVideoFiles();
    if (!videoFiles.isEmpty()) {
      return videoFiles.get(0);
    }

    // just return a dummy MF to prevent NPE
    return new MediaFile();
  }

  /**
   * Gets the images to cache.
   * 
   * @return the images to cache
   */
  public List<Path> getImagesToCache() {
    // get files to cache
    List<Path> filesToCache = new ArrayList<>();

    for (MediaFile mf : new ArrayList<>(getMediaFiles())) {
      if (mf.isGraphic()) {
        filesToCache.add(mf.getFileAsPath());
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
    if (videoFiles.size() > 0) {
      MediaFile videoFile = videoFiles.get(0);
      mediaFilesWithAudioStreams.add(videoFile);
    }

    // get all extra audio streams
    for (MediaFile audioFile : getMediaFiles(MediaFileType.AUDIO)) {
      mediaFilesWithAudioStreams.add(audioFile);
    }

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

  public boolean hasSubtitles() {
    if (this.subtitles) {
      return true; // can be set in GUI
    }

    if (getMediaFiles(MediaFileType.SUBTITLE).size() > 0) {
      return true;
    }

    for (MediaFile mf : getMediaFiles(MediaFileType.VIDEO)) {
      if (mf.hasSubtitles()) {
        return true;
      }
    }

    return false;
  }

  public void setSubtitles(boolean sub) {
    this.subtitles = sub;
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
  public Certification getCertification() {
    return getTvShow().getCertification();
  }

  @Override
  public String getMediaInfoVideoResolution() {
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (videos.size() > 0) {
      MediaFile mediaFile = videos.get(0);
      return mediaFile.getVideoResolution();
    }

    return "";
  }

  @Override
  public String getMediaInfoVideoFormat() {
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (videos.size() > 0) {
      MediaFile mediaFile = videos.get(0);
      return mediaFile.getVideoFormat();
    }

    return "";
  }

  @Override
  public String getMediaInfoVideoCodec() {
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (videos.size() > 0) {
      MediaFile mediaFile = videos.get(0);
      return mediaFile.getVideoCodec();
    }

    return "";
  }

  @Override
  public float getMediaInfoAspectRatio() {
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (videos.size() > 0) {
      MediaFile mediaFile = videos.get(0);
      return mediaFile.getAspectRatio();
    }

    return 0;
  }

  @Override
  public String getMediaInfoAudioCodec() {
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (videos.size() > 0) {
      MediaFile mediaFile = videos.get(0);
      return mediaFile.getAudioCodec();
    }

    return "";
  }

  @Override
  public double getMediaInfoFrameRate() {
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (videos.size() > 0) {
      MediaFile mediaFile = videos.get(0);
      return mediaFile.getFrameRate();
    }

    return 0;
  }

  @Override
  public int getMediaInfoAudioChannels() {
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (videos.size() > 0) {
      MediaFile mediaFile = videos.get(0);
      try {
        String channels = mediaFile.getAudioChannels().replace("ch", "");
        return Integer.parseInt(channels);
      }
      catch (NumberFormatException ignored) {
      }
    }

    return 0;
  }

  @Override
  public String getMediaInfoContainerFormat() {
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (videos.size() > 0) {
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
  public boolean isVideoIn3D() {
    String video3DFormat = "";
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (videos.size() > 0) {
      MediaFile mediaFile = videos.get(0);
      video3DFormat = mediaFile.getVideo3DFormat();
    }

    return StringUtils.isNotBlank(video3DFormat);
  }

  public boolean isDummy() {
    return dummy || getMediaFiles().isEmpty();
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
}
