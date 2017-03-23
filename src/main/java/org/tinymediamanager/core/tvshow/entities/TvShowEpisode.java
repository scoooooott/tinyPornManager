/*
 * Copyright 2012 - 2017 Manuel Laggner
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
import static org.tinymediamanager.core.Constants.DIRECTOR;
import static org.tinymediamanager.core.Constants.DISPLAY_EPISODE;
import static org.tinymediamanager.core.Constants.DISPLAY_SEASON;
import static org.tinymediamanager.core.Constants.DVD_EPISODE;
import static org.tinymediamanager.core.Constants.DVD_ORDER;
import static org.tinymediamanager.core.Constants.DVD_SEASON;
import static org.tinymediamanager.core.Constants.EPISODE;
import static org.tinymediamanager.core.Constants.FIRST_AIRED;
import static org.tinymediamanager.core.Constants.FIRST_AIRED_AS_STRING;
import static org.tinymediamanager.core.Constants.MEDIA_SOURCE;
import static org.tinymediamanager.core.Constants.SEASON;
import static org.tinymediamanager.core.Constants.SEASON_POSTER;
import static org.tinymediamanager.core.Constants.TAG;
import static org.tinymediamanager.core.Constants.TAGS_AS_STRING;
import static org.tinymediamanager.core.Constants.TITLE_FOR_UI;
import static org.tinymediamanager.core.Constants.TVDB;
import static org.tinymediamanager.core.Constants.TV_SHOW;
import static org.tinymediamanager.core.Constants.WATCHED;
import static org.tinymediamanager.core.Constants.WRITER;

import java.io.File;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.tinymediamanager.core.MediaEntityImageFetcherTask;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowMediaFileComparator;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.connector.TvShowEpisodeToXbmcNfoConnector;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.util.StrgUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * The Class TvShowEpisode.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisode extends MediaEntity implements Comparable<TvShowEpisode> {
  private static final Logger                LOGGER                = LoggerFactory.getLogger(TvShowEpisode.class);
  private static final Comparator<MediaFile> MEDIA_FILE_COMPARATOR = new TvShowMediaFileComparator();

  @JsonProperty
  private int                                episode               = -1;
  @JsonProperty
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
  private String                             director              = "";
  @JsonProperty
  private String                             writer                = "";
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
  private List<TvShowActor>                  actors                = new CopyOnWriteArrayList<>();
  @JsonProperty
  private List<String>                       tags                  = new CopyOnWriteArrayList<>();

  private TvShow                             tvShow                = null;
  private Date                               lastWatched           = null;

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
   */

  public void merge(TvShowEpisode other) {
    if (other == null) {
      return;
    }
    super.merge(other);

    this.episode = this.episode < 0 ? other.getEpisode() : this.episode;
    this.season = this.season < 0 ? other.getSeason() : this.season;
    this.dvdSeason = this.dvdSeason < 0 ? other.getDvdSeason() : this.dvdSeason;
    this.dvdEpisode = this.dvdEpisode < 0 ? other.getDvdEpisode() : this.dvdEpisode;
    this.displaySeason = this.displaySeason < 0 ? other.getDisplaySeason() : this.displaySeason;
    this.displayEpisode = this.displayEpisode < 0 ? other.getDisplayEpisode() : this.displayEpisode;

    this.firstAired = this.firstAired == null ? other.getFirstAired() : this.firstAired;
    this.mediaSource = this.mediaSource == MediaSource.UNKNOWN ? other.getMediaSource() : MediaSource.UNKNOWN;

    this.director = StringUtils.isEmpty(this.director) ? other.getDirector() : this.director;
    this.writer = StringUtils.isEmpty(this.writer) ? other.getWriter() : this.writer;

    for (String key : other.getTags()) {
      if (!this.tags.contains(key)) {
        this.tags.add(key);
      }
    }
    for (TvShowActor actor : other.getActors()) {
      if (!this.actors.contains(actor)) {
        this.actors.add(actor);
      }
    }
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
    path = new String(source.path);
    title = new String(source.title);
    originalTitle = new String(source.originalTitle);
    year = new String(source.year);
    plot = new String(source.plot);
    rating = source.rating;

    for (Entry<MediaFileType, String> entry : source.artworkUrlMap.entrySet()) {
      artworkUrlMap.put(entry.getKey(), new String(entry.getValue()));
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

    director = source.director;
    writer = source.writer;
    disc = source.disc;
    watched = source.watched;
    votes = source.votes;
    subtitles = source.subtitles;
    actors.addAll(source.actors);
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
    super.setTitle(newValue);
    firePropertyChange(TITLE_FOR_UI, "", newValue);
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
    if (StringUtils.isNotEmpty(thumbUrl)) {
      boolean firstImage = true;
      // create correct filename

      MediaFile mf = getMediaFiles(MediaFileType.VIDEO).get(0);
      String filename;
      String basename = FilenameUtils.getBaseName(mf.getFilename());

      switch (TvShowModuleManager.SETTINGS.getTvShowEpisodeThumbFilename()) {
        case FILENAME_THUMB_POSTFIX:
          filename = basename + "-thumb." + FilenameUtils.getExtension(thumbUrl);
          break;

        case FILENAME_THUMB:
          filename = basename + "." + FilenameUtils.getExtension(thumbUrl);
          break;

        case FILENAME_THUMB_TBN:
          filename = basename + ".tbn";
          break;

        default:
          filename = "";
          break;
      }
      if (isDisc()) {
        filename = "thumb." + FilenameUtils.getExtension(thumbUrl); // DVD/BluRay fixate to thumb.ext
      }

      if (StringUtils.isBlank(thumbUrl) || StringUtils.isBlank(filename)) {
        return;
      }

      // get image in thread
      MediaEntityImageFetcherTask task = new MediaEntityImageFetcherTask(this, thumbUrl, MediaArtworkType.THUMB, filename, firstImage);
      TmmTaskManager.getInstance().addImageDownloadTask(task);
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
    setRating(metadata.getRating());
    setVotes(metadata.getVoteCount());

    List<TvShowActor> actors = new ArrayList<>();
    String director = "";
    String writer = "";
    for (MediaCastMember member : metadata.getCastMembers()) {
      switch (member.getType()) {
        case ACTOR:
          TvShowActor actor = new TvShowActor();
          actor.setName(member.getName());
          actor.setCharacter(member.getCharacter());
          actor.setThumbUrl(member.getImageUrl());
          actors.add(actor);
          break;

        case DIRECTOR:
          if (!StringUtils.isEmpty(director)) {
            director += ", ";
          }
          director += member.getName();
          break;

        case WRITER:
          if (!StringUtils.isEmpty(writer)) {
            writer += ", ";
          }
          writer += member.getName();
          break;

        default:
          break;
      }
    }
    setActors(actors);
    setDirector(director);
    setWriter(writer);

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

    TvShowEpisodeToXbmcNfoConnector.setData(episodesInNfo);
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
   * Gets the writer.
   * 
   * @return the writer
   */
  public String getWriter() {
    return writer;
  }

  /**
   * Sets the director.
   * 
   * @param newValue
   *          the new director
   */
  public void setDirector(String newValue) {
    String oldValue = this.director;
    this.director = newValue;
    firePropertyChange(DIRECTOR, oldValue, newValue);
  }

  /**
   * Sets the writer.
   * 
   * @param newValue
   *          the new writer
   */
  public void setWriter(String newValue) {
    String oldValue = this.writer;
    this.writer = newValue;
    firePropertyChange(WRITER, oldValue, newValue);
  }

  /**
   * Gets the director.
   * 
   * @return the director
   */
  public String getDirector() {
    return director;
  }

  /**
   * Adds the actor.
   * 
   * @param obj
   *          the obj
   */
  public void addActor(TvShowActor obj) {
    // and re-set episode path to the actor
    if (StringUtils.isBlank(obj.getEntityRoot())) {
      obj.setEntityRoot(getPathNIO());
    }

    actors.add(obj);
    firePropertyChange(ACTORS, null, this.getActors());
  }

  /**
   * Gets the actors.
   * 
   * @return the actors
   */
  public List<TvShowActor> getActors() {
    List<TvShowActor> allActors = new ArrayList<>();
    if (tvShow != null) {
      allActors.addAll(tvShow.getActors());
    }
    allActors.addAll(actors);
    return allActors;
  }

  public List<TvShowActor> getGuests() {
    List<TvShowActor> allActors = new ArrayList<>();
    allActors.addAll(actors);
    return allActors;
  }

  /**
   * Removes the actor.
   * 
   * @param obj
   *          the obj
   */
  public void removeActor(TvShowActor obj) {
    actors.remove(obj);

    firePropertyChange(ACTORS, null, this.getActors());
  }

  /**
   * Sets the actors.
   * 
   * @param newActors
   *          the new actors
   */
  @JsonSetter
  public void setActors(List<TvShowActor> newActors) {
    // two way sync of actors
    List<TvShowActor> tvShowActors = new ArrayList<>();

    // tvShow is null while loading
    if (getTvShow() != null) {
      tvShowActors.addAll(getTvShow().getActors());
    }

    // first add the new ones
    for (TvShowActor actor : newActors) {
      if (!tvShowActors.contains(actor) && !actors.contains(actor)) {
        actors.add(actor);
      }
    }

    // second remove unused
    for (int i = actors.size() - 1; i >= 0; i--) {
      TvShowActor actor = actors.get(i);
      if (!newActors.contains(actor) || tvShowActors.contains(actor)) {
        actors.remove(actor);
      }
    }

    // and re-set episode path to the actors
    for (TvShowActor actor : actors) {
      if (StringUtils.isBlank(actor.getEntityRoot())) {
        actor.setEntityRoot(getPathNIO());
      }
    }

    firePropertyChange(ACTORS, null, this.getActors());
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
   * Parses the nfo.
   * 
   * @param episodeFile
   *          the episode file
   * @return the list
   */
  public static List<TvShowEpisode> parseNFO(File episodeFile) {
    List<TvShowEpisode> episodes = new ArrayList<>(1);
    String filename = episodeFile.getParent() + File.separator + FilenameUtils.getBaseName(episodeFile.getName()) + ".nfo";
    episodes.addAll(TvShowEpisodeToXbmcNfoConnector.getData(new File(filename)));
    return episodes;
  }

  /**
   * Parses the nfo.
   * 
   * @param episodeFile
   *          the episode mediafile
   * @return the list
   */
  public static List<TvShowEpisode> parseNFO(MediaFile episodeFile) {
    List<TvShowEpisode> episodes = new ArrayList<>(1);
    episodes.addAll(TvShowEpisodeToXbmcNfoConnector.getData(episodeFile.getFile()));
    return episodes;
  }

  /**
   * Gets the media info video format (i.e. 720p).
   * 
   * @return the media info video format
   */
  public String getMediaInfoVideoFormat() {
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (videos.size() > 0) {
      MediaFile mediaFile = videos.get(0);
      return mediaFile.getVideoFormat();
    }

    return "";
  }

  /**
   * Gets the media info video codec (i.e. divx)
   * 
   * @return the media info video codec
   */
  public String getMediaInfoVideoCodec() {
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (videos.size() > 0) {
      MediaFile mediaFile = videos.get(0);
      return mediaFile.getVideoCodec();
    }

    return "";
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
    return null;
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
   * Event to trigger a season poster changed for the UI
   */
  void setPosterChanged() {
    firePropertyChange(SEASON_POSTER, null, "");
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

    // first, add new ones
    for (String tag : newTags) {
      if (!this.tags.contains(tag)) {
        this.tags.add(tag);
      }
    }

    // second remove old ones
    for (int i = this.tags.size() - 1; i >= 0; i--) {
      String tag = this.tags.get(i);
      if (!newTags.contains(tag)) {
        this.tags.remove(tag);
      }
    }

    firePropertyChange(TAG, null, newTags);
    firePropertyChange(TAGS_AS_STRING, null, newTags);
  }

  /**
   * Gets the tag as string.
   * 
   * @return the tag as string
   */
  public String getTagAsString() {
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
}
