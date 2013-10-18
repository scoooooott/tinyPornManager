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
package org.tinymediamanager.core.tvshow;

import static org.tinymediamanager.core.Constants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jdesktop.observablecollections.ObservableCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaEntity;
import org.tinymediamanager.core.MediaEntityImageFetcherTask;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.tvshow.connector.TvShowToXbmcNfoConnector;
import org.tinymediamanager.core.tvshow.tasks.TvShowEpisodeScrapeTask;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaCastMember;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.util.Url;

/**
 * The Class TvShow.
 * 
 * @author Manuel Laggner
 */
@Entity
@Inheritance(strategy = javax.persistence.InheritanceType.JOINED)
public class TvShow extends MediaEntity {
  private static final Logger      LOGGER             = LoggerFactory.getLogger(TvShow.class);

  private String                   dataSource         = "";
  private String                   director           = "";
  private String                   writer             = "";
  private int                      runtime            = 0;
  private int                      votes              = 0;
  private Date                     firstAired         = null;
  private String                   status             = "";
  private String                   studio             = "";
  private boolean                  watched            = false;
  private String                   sortTitle          = "";

  private List<TvShowEpisode>      episodes           = new ArrayList<TvShowEpisode>();
  private List<String>             tags               = new ArrayList<String>();
  private HashMap<Integer, String> seasonPosterUrlMap = new HashMap<Integer, String>();
  private HashMap<Integer, String> seasonPosterMap    = new HashMap<Integer, String>();

  @Transient
  private List<TvShowEpisode>      episodesObservable = ObservableCollections.observableList(episodes);

  @Transient
  private List<TvShowSeason>       seasons            = ObservableCollections.observableList(new ArrayList<TvShowSeason>());

  @OneToMany(cascade = CascadeType.ALL)
  private List<TvShowActor>        actors             = new ArrayList<TvShowActor>();

  @Transient
  private List<TvShowActor>        actorsObservables  = ObservableCollections.observableList(actors);

  private List<String>             genres             = new ArrayList<String>();

  @Transient
  private List<MediaGenres>        genresForAccess    = new ArrayList<MediaGenres>();

  @Transient
  private List<String>             tagsObservable     = ObservableCollections.observableList(tags);

  @OneToMany(cascade = CascadeType.ALL)
  private List<MediaTrailer>       trailer            = new ArrayList<MediaTrailer>();

  @Transient
  private List<MediaTrailer>       trailerObservable  = ObservableCollections.observableList(trailer);

  @Enumerated(EnumType.STRING)
  private Certification            certification      = Certification.NOT_RATED;

  @Transient
  private String                   titleSortable      = "";

  static {
    mediaFileComparator = new TvShowMediaFileComparator();
  }

  /**
   * Instantiates a tv show. To initialize the propertychangesupport after loading
   */
  public TvShow() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#setTitle(java.lang.String)
   */
  @Override
  public void setTitle(String newValue) {
    String oldValue = this.title;
    super.setTitle(newValue);

    oldValue = this.titleSortable;
    titleSortable = "";
    firePropertyChange(TITLE_SORTABLE, oldValue, titleSortable);
  }

  /**
   * Returns the sortable variant of title<br>
   * eg "The Big Bang Theory" -> "Big Bang Theory, The".
   * 
   * @return the title in its sortable format
   */
  public String getTitleSortable() {
    if (StringUtils.isEmpty(titleSortable)) {
      titleSortable = Utils.getSortableName(this.getTitle());
    }
    return titleSortable;
  }

  public void clearTitleSortable() {
    titleSortable = "";
  }

  public String getSortTitle() {
    return sortTitle;
  }

  public void setSortTitle(String newValue) {
    String oldValue = this.sortTitle;
    this.sortTitle = newValue;
    firePropertyChange(SORT_TITLE, oldValue, newValue);
  }

  /**
   * Gets the episodes.
   * 
   * @return the episodes
   */
  public List<TvShowEpisode> getEpisodes() {
    return episodesObservable;
  }

  /**
   * Adds the episode.
   * 
   * @param episode
   *          the episode
   */
  public void addEpisode(TvShowEpisode episode) {
    int oldValue = episodesObservable.size();
    episodesObservable.add(episode);
    addToSeason(episode);

    Collections.sort(episodesObservable);

    firePropertyChange(ADDED_EPISODE, null, episode);
    firePropertyChange(EPISODE_COUNT, oldValue, episodesObservable.size());
  }

  /**
   * Gets the episode count.
   * 
   * @return the episode count
   */
  public int getEpisodeCount() {
    return episodesObservable.size();
  }

  /**
   * Adds the to season.
   * 
   * @param episode
   *          the episode
   */
  private void addToSeason(TvShowEpisode episode) {
    TvShowSeason season = getSeasonForEpisode(episode);
    season.addEpisode(episode);
  }

  /**
   * Gets the season for episode.
   * 
   * @param episode
   *          the episode
   * @return the season for episode
   */
  public TvShowSeason getSeasonForEpisode(TvShowEpisode episode) {
    TvShowSeason season = null;

    // search for an existing season
    for (TvShowSeason s : seasons) {
      if (s.getSeason() == episode.getSeason()) {
        season = s;
        break;
      }
    }

    // no one found - create one
    if (season == null) {
      season = new TvShowSeason(episode.getSeason(), this);
      seasons.add(season);
      firePropertyChange(ADDED_SEASON, null, season);
    }

    return season;
  }

  /**
   * Initialize after loading.
   */
  public void initializeAfterLoading() {
    super.initializeAfterLoading();

    // remove empty tag and null values
    Utils.removeEmptyStringsFromList(tags);
    Utils.removeEmptyStringsFromList(genres);

    actorsObservables = ObservableCollections.observableList(actors);
    episodesObservable = ObservableCollections.observableList(episodes);
    tagsObservable = ObservableCollections.observableList(tags);
    trailerObservable = ObservableCollections.observableList(trailer);

    // load genres
    for (String genre : new ArrayList<String>(genres)) {
      addGenre(MediaGenres.getGenre(genre));
    }

    // create the seasons structure
    for (TvShowEpisode episode : episodes) {
      addToSeason(episode);
    }
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
   * remove all episodes from this tv show.
   */
  public void removeAllEpisodes() {
    int oldValue = episodesObservable.size();
    if (episodesObservable.size() > 0) {
      Globals.entityManager.getTransaction().begin();
      for (int i = episodesObservable.size() - 1; i >= 0; i--) {
        TvShowEpisode episode = episodesObservable.get(i);
        episodesObservable.remove(episode);
        Globals.entityManager.remove(episode);
      }
      Globals.entityManager.getTransaction().commit();
    }

    firePropertyChange(EPISODE_COUNT, oldValue, episodesObservable.size());
  }

  /**
   * Removes the episode.
   * 
   * @param episode
   *          the episode
   */
  public void removeEpisode(TvShowEpisode episode) {
    if (episodesObservable.contains(episode)) {
      int oldValue = episodesObservable.size();

      synchronized (Globals.entityManager) {
        Globals.entityManager.getTransaction().begin();
        episodesObservable.remove(episode);
        Globals.entityManager.remove(episode);
        Globals.entityManager.persist(this);
        Globals.entityManager.getTransaction().commit();
      }

      firePropertyChange(REMOVED_EPISODE, null, episode);
      firePropertyChange(EPISODE_COUNT, oldValue, episodesObservable.size());
    }
  }

  /**
   * Gets the seasons.
   * 
   * @return the seasons
   */
  public List<TvShowSeason> getSeasons() {
    return seasons;
  }

  /**
   * Gets the genres.
   * 
   * @return the genres
   */
  public List<MediaGenres> getGenres() {
    return genresForAccess;
  }

  /**
   * Adds the genre.
   * 
   * @param newValue
   *          the new value
   */
  public void addGenre(MediaGenres newValue) {
    if (!genresForAccess.contains(newValue)) {
      genresForAccess.add(newValue);
      if (!genres.contains(newValue.name())) {
        genres.add(newValue.name());
      }
      firePropertyChange(GENRE, null, newValue);
      firePropertyChange(GENRES_AS_STRING, null, newValue);
    }
  }

  /**
   * Sets the genres.
   * 
   * @param genres
   *          the new genres
   */
  public void setGenres(List<MediaGenres> genres) {
    // two way sync of genres

    // first, add new ones
    for (MediaGenres genre : genres) {
      if (!this.genresForAccess.contains(genre)) {
        this.genresForAccess.add(genre);
        if (!genres.contains(genre.name())) {
          this.genres.add(genre.name());
        }
      }
    }

    // second remove old ones
    for (int i = this.genresForAccess.size() - 1; i >= 0; i--) {
      MediaGenres genre = this.genresForAccess.get(i);
      if (!genres.contains(genre)) {
        this.genresForAccess.remove(genre);
        this.genres.remove(genre.name());
      }
    }

    firePropertyChange(GENRE, null, genres);
    firePropertyChange(GENRES_AS_STRING, null, genres);
  }

  /**
   * Removes the genre.
   * 
   * @param genre
   *          the genre
   */
  public void removeGenre(MediaGenres genre) {
    if (genresForAccess.contains(genre)) {
      genresForAccess.remove(genre);
      genres.remove(genre.name());
      firePropertyChange(GENRE, null, genre);
      firePropertyChange(GENRES_AS_STRING, null, genre);
    }
  }

  /**
   * Gets the genres as string.
   * 
   * @return the genres as string
   */
  public String getGenresAsString() {
    StringBuilder sb = new StringBuilder();
    for (MediaGenres genre : genresForAccess) {
      if (!StringUtils.isEmpty(sb)) {
        sb.append(", ");
      }
      sb.append(genre != null ? genre.getLocalizedName() : "null");
    }
    return sb.toString();
  }

  /**
   * Sets the metadata.
   * 
   * @param metadata
   *          the new metadata
   * @param config
   *          the config
   */
  public void setMetadata(MediaMetadata metadata, TvShowScraperMetadataConfig config) {
    // check if metadata has at least a name
    if (StringUtils.isEmpty(metadata.getTitle())) {
      LOGGER.warn("wanted to save empty metadata for " + getTitle());
      return;
    }

    // populate ids
    for (Entry<String, Object> entry : metadata.getIds().entrySet()) {
      if ("tvdb".equals(entry.getKey()) && entry.getValue() != null) {
        setTvdbId(entry.getValue().toString());
      }
      else {
        setId((String) entry.getKey(), entry.getValue().toString());
      }
    }

    if (config.isTitle()) {
      setTitle(metadata.getTitle());
    }

    if (config.isPlot()) {
      setPlot(metadata.getPlot());
    }

    if (config.isYear()) {
      setYear(metadata.getYear());
    }

    if (config.isRating()) {
      setRating((float) metadata.getRating());
      setVotes(metadata.getVoteCount());
    }

    if (config.isAired()) {
      try {
        setFirstAired(metadata.getFirstAired());
      }
      catch (ParseException e) {

      }
    }

    if (config.isStatus()) {
      setStatus(metadata.getStatus());
    }

    if (config.isRuntime()) {
      setRuntime(metadata.getRuntime());
    }

    if (config.isYear()) {
      setYear(metadata.getYear());
    }

    if (config.isCast()) {
      setStudio(metadata.getStudio());
      List<TvShowActor> actors = new ArrayList<TvShowActor>();
      String director = "";
      String writer = "";

      for (MediaCastMember member : metadata.getCastMembers()) {
        switch (member.getType()) {
          case ACTOR:
            TvShowActor actor = new TvShowActor();
            actor.setName(member.getName());
            actor.setCharacter(member.getCharacter());
            actor.setThumb(member.getImageUrl());
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
      // TODO write actor images for tv shows
      // writeActorImages();
    }

    if (config.isCertification()) {
      setCertification(metadata.getCertifications().get(0));
    }

    if (config.isGenres()) {
      setGenres(metadata.getGenres());
    }

    // set scraped
    setScraped(true);

    // write NFO
    writeNFO();

    // update DB
    saveToDb();
  }

  /**
   * Sets the artwork.
   * 
   * @param artwork
   *          the artwork
   * @param config
   *          the config
   */
  public void setArtwork(List<MediaArtwork> artwork, TvShowScraperMetadataConfig config) {
    if (config.isArtwork()) {
      // poster
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.POSTER) {
          // set url
          setPosterUrl(art.getDefaultUrl());
          // and download it
          writePosterImage();
          break;
        }
      }

      // fanart
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.BACKGROUND) {
          // set url
          setFanartUrl(art.getDefaultUrl());
          // and download it
          writeFanartImage();
          break;
        }
      }

      // banner
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.BANNER) {
          // set url
          setBannerUrl(art.getDefaultUrl());
          // and download it
          writeBannerImage();
          break;
        }
      }

      // season poster
      HashMap<Integer, String> seasonPosters = new HashMap<Integer, String>();
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.SEASON && art.getSeason() >= 0) {
          // check if there is already an artwork for this season
          String url = seasonPosters.get(art.getSeason());
          if (StringUtils.isBlank(url)) {
            setSeasonPosterUrl(art.getSeason(), art.getDefaultUrl());
            writeSeasonPoster(art.getSeason());
            seasonPosters.put(art.getSeason(), art.getDefaultUrl());
          }
        }
      }

      // update DB
      saveToDb();
    }
  }

  /**
   * Write poster image.
   */
  public void writePosterImage() {
    if (StringUtils.isNotEmpty(getPosterUrl())) {
      boolean firstImage = true;
      // create correct filename
      String filename = "poster." + FilenameUtils.getExtension(getPosterUrl());
      // get image in thread
      MediaEntityImageFetcherTask task = new MediaEntityImageFetcherTask(this, getPosterUrl(), MediaArtworkType.POSTER, filename, firstImage);
      Globals.executor.execute(task);
    }
  }

  /**
   * Write fanart image.
   */
  public void writeFanartImage() {
    if (StringUtils.isNotEmpty(getFanartUrl())) {
      boolean firstImage = true;
      // create correct filename
      String filename = "fanart." + FilenameUtils.getExtension(getFanartUrl());
      // get image in thread
      MediaEntityImageFetcherTask task = new MediaEntityImageFetcherTask(this, getFanartUrl(), MediaArtworkType.BACKGROUND, filename, firstImage);
      Globals.executor.execute(task);
    }
  }

  /**
   * Write banner image.
   */
  public void writeBannerImage() {
    if (StringUtils.isNotEmpty(getBannerUrl())) {
      boolean firstImage = true;
      // create correct filename
      String filename = "banner." + FilenameUtils.getExtension(getBannerUrl());
      // get image in thread
      MediaEntityImageFetcherTask task = new MediaEntityImageFetcherTask(this, getBannerUrl(), MediaArtworkType.BANNER, filename, firstImage);
      Globals.executor.execute(task);
    }
  }

  /**
   * Write season poster.
   * 
   * @param season
   *          the season
   */
  public void writeSeasonPoster(int season) {
    String seasonPosterUrl = seasonPosterUrlMap.get(season);

    TvShowSeason tvShowSeason = null;
    // try to get a season instance
    for (TvShowSeason s : seasons) {
      if (s.getSeason() == season) {
        tvShowSeason = s;
        break;
      }
    }

    String filename = "";
    if (season > 0) {
      filename = String.format(path + File.separator + "season%02d-poster." + FilenameUtils.getExtension(seasonPosterUrl), season);
    }
    else {
      filename = path + File.separator + "season-specials-poster." + FilenameUtils.getExtension(seasonPosterUrl);
    }
    SeasonPosterImageFetcher task = new SeasonPosterImageFetcher(filename, tvShowSeason, seasonPosterUrl);
    Globals.executor.execute(task);
  }

  /**
   * Write nfo.
   */
  public void writeNFO() {
    TvShowToXbmcNfoConnector.setData(this);
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
    if (!StringUtils.isEmpty(getPoster()) && !StringUtils.isEmpty(getFanart())) {
      return true;
    }
    return false;
  }

  /**
   * Gets the imdb id.
   * 
   * @return the imdb id
   */
  public String getImdbId() {
    Object obj = ids.get("imdbId");
    if (obj == null) {
      return "";
    }
    return obj.toString();
  }

  /**
   * Sets the imdb id.
   * 
   * @param newValue
   *          the new imdb id
   */
  public void setImdbId(String newValue) {
    String oldValue = getImdbId();
    ids.put("imdbId", newValue);
    firePropertyChange(IMDBID, oldValue, newValue);
  }

  /**
   * Gets the tvdb id.
   * 
   * @return the tvdb id
   */
  public String getTvdbId() {
    Object obj = ids.get("tvdb");
    if (obj == null) {
      return "";
    }
    return obj.toString();
  }

  /**
   * Sets the tvdb id.
   * 
   * @param newValue
   *          the new tvdb id
   */
  public void setTvdbId(String newValue) {
    String oldValue = getTvdbId();
    ids.put("tvdb", newValue);
    firePropertyChange(TVDBID, oldValue, newValue);
  }

  /**
   * Gets the studio.
   * 
   * @return the studio
   */
  public String getStudio() {
    return studio;
  }

  /**
   * Sets the studio.
   * 
   * @param newValue
   *          the new studio
   */
  public void setStudio(String newValue) {
    String oldValue = this.studio;
    this.studio = newValue;
    firePropertyChange(STUDIO, oldValue, newValue);
  }

  /**
   * first aired date.
   * 
   * @return the date
   */
  public Date getFirstAired() {
    return firstAired;
  }

  /**
   * sets the first aired date.
   * 
   * @param newValue
   *          the new first aired
   */
  public void setFirstAired(Date newValue) {
    Date oldValue = this.firstAired;
    this.firstAired = newValue;
    firePropertyChange(FIRST_AIRED, oldValue, newValue);
    firePropertyChange(FIRST_AIRED_AS_STRING, oldValue, newValue);
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
   * @throws ParseException
   *           if string cannot be parsed!
   */
  public void setFirstAired(String aired) throws ParseException {
    setFirstAired(org.tinymediamanager.scraper.util.StrgUtils.parseDate(aired));
  }

  /**
   * Gets the status.
   * 
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Sets the status.
   * 
   * @param newValue
   *          the new status
   */
  public void setStatus(String newValue) {
    String oldValue = this.status;
    this.status = newValue;
    firePropertyChange(STATUS, oldValue, newValue);
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

    for (String tag : tagsObservable) {
      if (tag.equals(newTag)) {
        return;
      }
    }

    tagsObservable.add(newTag);
    firePropertyChange(TAG, null, tagsObservable);
    firePropertyChange(TAGS_AS_STRING, null, newTag);
  }

  /**
   * Removes the from tags.
   * 
   * @param removeTag
   *          the remove tag
   */
  public void removeFromTags(String removeTag) {
    tagsObservable.remove(removeTag);
    firePropertyChange(TAG, null, tagsObservable);
    firePropertyChange(TAGS_AS_STRING, null, removeTag);
  }

  /**
   * Sets the tags.
   * 
   * @param newTags
   *          the new tags
   */
  public void setTags(List<String> newTags) {
    // two way sync of tags

    // first, add new ones
    for (String tag : newTags) {
      if (!this.tagsObservable.contains(tag)) {
        this.tagsObservable.add(tag);
      }
    }

    // second remove old ones
    for (int i = this.tagsObservable.size() - 1; i >= 0; i--) {
      String tag = this.tagsObservable.get(i);
      if (!newTags.contains(tag)) {
        this.tagsObservable.remove(tag);
      }
    }

    firePropertyChange(TAG, null, tagsObservable);
    firePropertyChange(TAGS_AS_STRING, null, tagsObservable);
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
   * Gets the tags.
   * 
   * @return the tags
   */
  public List<String> getTags() {
    return this.tagsObservable;
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
   * Gets the runtime.
   * 
   * @return the runtime
   */
  public int getRuntime() {
    return runtime;
  }

  /**
   * Sets the runtime.
   * 
   * @param newValue
   *          the new runtime
   */
  public void setRuntime(int newValue) {
    int oldValue = this.runtime;
    this.runtime = newValue;
    firePropertyChange(RUNTIME, oldValue, newValue);
  }

  /**
   * Adds the actor.
   * 
   * @param obj
   *          the obj
   */
  public void addActor(TvShowActor obj) {
    actorsObservables.add(obj);
    firePropertyChange(ACTORS, null, this.getActors());
  }

  /**
   * Gets the actors.
   * 
   * @return the actors
   */
  public List<TvShowActor> getActors() {
    return this.actorsObservables;
  }

  /**
   * Removes the actor.
   * 
   * @param obj
   *          the obj
   */
  public void removeActor(TvShowActor obj) {
    actorsObservables.remove(obj);
    firePropertyChange(ACTORS, null, this.getActors());
  }

  /**
   * Sets the actors.
   * 
   * @param newActors
   *          the new actors
   */
  public void setActors(List<TvShowActor> newActors) {
    // two way sync of actors

    // first add the new ones
    for (TvShowActor actor : newActors) {
      if (!actorsObservables.contains(actor)) {
        actorsObservables.add(actor);
      }
    }

    // second remove unused
    for (int i = actorsObservables.size() - 1; i >= 0; i--) {
      TvShowActor actor = actorsObservables.get(i);
      if (!newActors.contains(actor)) {
        actorsObservables.remove(actor);
      }
    }

    firePropertyChange(ACTORS, null, this.getActors());
  }

  /**
   * Gets the trailers.
   * 
   * @return the trailers
   */
  public List<MediaTrailer> getTrailers() {
    return this.trailerObservable;
  }

  /**
   * Adds the trailer.
   * 
   * @param obj
   *          the obj
   */
  public void addTrailer(MediaTrailer obj) {
    trailerObservable.add(obj);
    firePropertyChange(TRAILER, null, trailerObservable);
  }

  /**
   * Removes the all trailers.
   */
  public void removeAllTrailers() {
    trailerObservable.clear();
    firePropertyChange(TRAILER, null, trailerObservable);
  }

  /**
   * Gets the certifications.
   * 
   * @return the certifications
   */
  public Certification getCertification() {
    return certification;
  }

  /**
   * Sets the certifications.
   * 
   * @param newValue
   *          the new certifications
   */
  public void setCertification(Certification newValue) {
    this.certification = newValue;
    firePropertyChange(CERTIFICATION, null, newValue);
  }

  /**
   * Gets the votes.
   * 
   * @return the votes
   */
  public int getVotes() {
    return votes;
  }

  /**
   * Sets the votes.
   * 
   * @param newValue
   *          the new votes
   */
  public void setVotes(int newValue) {
    int oldValue = this.votes;
    this.votes = newValue;
    firePropertyChange(VOTES, oldValue, newValue);
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

  /**
   * Parses the nfo.
   * 
   * @param tvShowDirectory
   *          the tv show directory
   * @return the tv show
   */
  public static TvShow parseNFO(File tvShowDirectory) {
    LOGGER.debug("try to find a nfo for " + tvShowDirectory.getPath());
    // check if there are any NFOs in that directory
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        // do not start with .
        if (name.toLowerCase().startsWith("."))
          return false;

        // check if filetype is in our settings
        if (name.toLowerCase().endsWith("nfo")) {
          return true;
        }

        return false;
      }
    };

    TvShow tvShow = null;
    File[] nfoFiles = tvShowDirectory.listFiles(filter);

    for (File file : nfoFiles) {
      tvShow = TvShowToXbmcNfoConnector.getData(file);
      if (tvShow != null) {
        tvShow.setPath(tvShowDirectory.getPath());
        tvShow.addToMediaFiles(new MediaFile(file, MediaFileType.NFO));
        break;
      }

      LOGGER.debug("did not find tv show informations in nfo");
    }

    return tvShow;
  }

  /**
   * Find images.
   */
  public void findImages() {
    // try to find images in tv show root folder

    // find poster - poster.jpg/png
    findPoster();

    // fanart - fanart.jpg/png
    findFanart();

    // banner - banner.jpg/png
    findBanner();

    // season posters - seasonXX-poster.jpg/png
    findSeasonPosters();
  }

  /**
   * Find poster.
   */
  private void findPoster() {
    boolean found = false;

    File posterFile = new File(path, "poster.jpg");
    if (posterFile.exists()) {
      setPoster(posterFile);
      found = true;
      LOGGER.debug("found poster " + posterFile.getPath());
    }

    if (!found) {
      posterFile = new File(path, "poster.png");
      if (posterFile.exists()) {
        setPoster(posterFile);
        found = true;
        LOGGER.debug("found poster " + posterFile.getPath());
      }
    }

    // still not found anything? try *-poster.*
    if (!found) {
      Pattern pattern = Pattern.compile("(?i).*-poster\\..{2,4}");
      File[] files = new File(path).listFiles();
      for (File file : files) {
        Matcher matcher = pattern.matcher(file.getName());
        if (matcher.matches()) {
          setPoster(file);
          LOGGER.debug("found poster " + file.getPath());
          found = true;
          break;
        }
      }
    }

    // we did not find a poster, try to get it if an url exists
    if (!found && StringUtils.isNotEmpty(posterUrl)) {
      writePosterImage();
      found = true;
      LOGGER.debug("got poster url: " + posterUrl + " ; try to download this");
    }

    if (!found) {
      LOGGER.debug("Sorry, could not find poster.");
    }
  }

  /**
   * Find fanart.
   */
  private void findFanart() {
    boolean found = false;

    File fanartFile = new File(path, "fanart.jpg");
    if (fanartFile.exists()) {
      setFanart(fanartFile);
      found = true;
      LOGGER.debug("found fanart " + fanartFile.getPath());
    }

    if (!found) {
      fanartFile = new File(path, "fanart.png");
      if (fanartFile.exists()) {
        setFanart(fanartFile);
        found = true;
        LOGGER.debug("found fanart " + fanartFile.getPath());
      }
    }

    // still not found anything? try *-fanart.*
    if (!found) {
      Pattern pattern = Pattern.compile("(?i).*-fanart\\..{2,4}");
      File[] files = new File(path).listFiles();
      for (File file : files) {
        Matcher matcher = pattern.matcher(file.getName());
        if (matcher.matches()) {
          setFanart(file);
          LOGGER.debug("found fanart " + file.getPath());
          found = true;
          break;
        }
      }
    }

    // we did not find a fanart, try to get it if an url exists
    if (!found && StringUtils.isNotEmpty(fanartUrl)) {
      writeFanartImage();
      found = true;
      LOGGER.debug("got fanart url: " + fanartUrl + " ; try to download this");
    }

    if (!found) {
      LOGGER.debug("Sorry, could not find fanart.");
    }
  }

  /**
   * Find banner.
   */
  private void findBanner() {
    boolean found = false;

    File bannerFile = new File(path, "banner.jpg");
    if (bannerFile.exists()) {
      setBanner(bannerFile);
      found = true;
      LOGGER.debug("found banner " + bannerFile.getPath());
    }

    if (!found) {
      bannerFile = new File(path, "banner.png");
      if (bannerFile.exists()) {
        setBanner(bannerFile);
        found = true;
        LOGGER.debug("found banner " + bannerFile.getPath());
      }
    }

    // still not found anything? try *-banner.*
    if (!found) {
      Pattern pattern = Pattern.compile("(?i).*-banner\\..{2,4}");
      File[] files = new File(path).listFiles();
      for (File file : files) {
        Matcher matcher = pattern.matcher(file.getName());
        if (matcher.matches()) {
          setBanner(file);
          LOGGER.debug("found banner " + file.getPath());
          found = true;
          break;
        }
      }
    }

    // we did not find a banner, try to get it if an url exists
    if (!found && StringUtils.isNotEmpty(bannerUrl)) {
      writeBannerImage();
      found = true;
      LOGGER.debug("got banner url: " + bannerUrl + " ; try to download this");
    }

    if (!found) {
      LOGGER.debug("Sorry, could not find banner.");
    }
  }

  private void findSeasonPosters() {
    Pattern pattern = Pattern.compile("(?i)season([0-9]{1,2})-poster\\..{2,4}");
    File[] files = new File(path).listFiles();
    for (File file : files) {
      Matcher matcher = pattern.matcher(file.getName());
      if (matcher.matches()) {
        // setBanner(FilenameUtils.getName(file.getName()));
        LOGGER.debug("found season poster " + file.getPath());
        try {
          int season = Integer.parseInt(matcher.group(1));
          setSeasonPoster(season, FilenameUtils.getName(file.getName()));
        }
        catch (Exception e) {
        }
      }
    }
  }

  /**
   * Scrape all episodes.
   */
  public void scrapeAllEpisodes() {
    List<TvShowEpisode> episodes = new ArrayList<TvShowEpisode>();
    for (TvShowEpisode episode : episodesObservable) {
      if (episode.getSeason() > -1 && episode.getEpisode() > -1) {
        episodes.add(episode);
      }
    }

    // scrape episodes in a task
    TvShowEpisodeScrapeTask task = new TvShowEpisodeScrapeTask(episodes);
    Globals.executor.execute(task);
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

  /**
   * Gets the season poster url.
   * 
   * @param season
   *          the season
   * @return the season poster url
   */
  String getSeasonPosterUrl(int season) {
    String url = seasonPosterUrlMap.get(season);
    if (StringUtils.isBlank(url)) {
      return "";
    }
    return url;
  }

  /**
   * Sets the season poster url.
   * 
   * @param season
   *          the season
   * @param url
   *          the url
   */
  void setSeasonPosterUrl(int season, String url) {
    seasonPosterUrlMap.put(season, url);
  }

  /**
   * Gets the season poster.
   * 
   * @param season
   *          the season
   * @return the season poster
   */
  String getSeasonPoster(int season) {
    String poster = seasonPosterMap.get(season);
    if (StringUtils.isBlank(poster)) {
      return "";
    }
    return path + File.separator + poster;
  }

  /**
   * Sets the season poster.
   * 
   * @param season
   *          the season
   * @param path
   *          the path
   */
  void setSeasonPoster(int season, String path) {
    seasonPosterMap.put(season, path);
  }

  /**
   * Gets the media files of all episodes.<br>
   * (without the TV show MFs like poster/banner/...)
   * 
   * @return the media files
   */
  public List<MediaFile> getEpisodesMediaFiles() {
    List<MediaFile> mediaFiles = new ArrayList<MediaFile>();
    for (TvShowEpisode episode : episodes) {
      for (MediaFile mf : episode.getMediaFiles()) {

        if (!mediaFiles.contains(mf)) {
          mediaFiles.add(mf);
        }
      }
    }
    return mediaFiles;
  }

  /**
   * Gets the images to cache.
   * 
   * @return the images to cache
   */
  public List<File> getImagesToCache() {
    // get files to cache
    List<File> filesToCache = new ArrayList<File>();

    for (MediaFile mf : new ArrayList<MediaFile>(getMediaFiles())) {
      if (mf.isGraphic()) {
        filesToCache.add(mf.getFile());
      }
    }

    for (TvShowEpisode episode : episodes) {
      filesToCache.addAll(episode.getImagesToCache());
    }

    return filesToCache;
  }

  /**
   * The Class SeasonPosterImageFetcher.
   * 
   * @author Manuel Laggner
   */
  private class SeasonPosterImageFetcher implements Runnable {

    /** The filename. */
    private String       filename;

    /** The tv show season. */
    private TvShowSeason tvShowSeason;

    /** The url. */
    private String       url;

    /**
     * Instantiates a new season poster image fetcher.
     * 
     * @param filename
     *          the filename
     * @param tvShowSeason
     *          the tv show season
     * @param url
     *          the url
     */
    SeasonPosterImageFetcher(String filename, TvShowSeason tvShowSeason, String url) {
      this.filename = filename;
      this.tvShowSeason = tvShowSeason;
      this.url = url;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      String oldFilename = "";
      try {
        if (tvShowSeason != null) {
          oldFilename = tvShowSeason.getPoster();
          tvShowSeason.setPoster("");
        }

        // debug message
        LOGGER.debug("writing season poster " + filename);

        // fetch and store images
        Url url1 = new Url(url);
        FileOutputStream outputStream = new FileOutputStream(filename);
        InputStream is = url1.getInputStream();
        IOUtils.copy(is, outputStream);
        outputStream.close();
        outputStream.flush();
        try {
          outputStream.getFD().sync(); // wait until file has been completely written
        }
        catch (Exception e) {
          // empty here -> just not let the thread crash
        }
        is.close();

        ImageCache.invalidateCachedImage(filename);
        if (tvShowSeason != null) {
          tvShowSeason.setPoster(FilenameUtils.getName(filename));
        }
      }
      catch (IOException e) {
        LOGGER.debug("fetch image", e);
        // fallback
        if (tvShowSeason != null) {
          tvShowSeason.setPoster(oldFilename);
        }
      }
      catch (Exception e) {
        LOGGER.error("Thread crashed", e);
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, this, "message.scrape.tvshowartworkfailed"));
      }
      finally {
        saveToDb();
      }
    }
  }

  @Override
  public synchronized void callbackForWrittenArtwork(MediaArtworkType type) {
  }

  public TvShowEpisode getEpisode(int season, int episode) {
    TvShowEpisode ep = null;

    for (TvShowEpisode e : new ArrayList<TvShowEpisode>(episodes)) {
      if (e.getSeason() == season && e.getEpisode() == episode) {
        ep = e;
        break;
      }
    }
    return ep;
  }

  /**
   * check if one of the tv shows episode is newly added
   * 
   * @return true/false
   */
  public boolean isNewlyAdded() {
    for (TvShowEpisode episode : episodes) {
      if (episode.isNewlyAdded()) {
        return true;
      }
    }
    return false;
  }
}
