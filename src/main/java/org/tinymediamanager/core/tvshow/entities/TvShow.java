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
import static org.tinymediamanager.core.Constants.ADDED_EPISODE;
import static org.tinymediamanager.core.Constants.ADDED_SEASON;
import static org.tinymediamanager.core.Constants.CERTIFICATION;
import static org.tinymediamanager.core.Constants.DATA_SOURCE;
import static org.tinymediamanager.core.Constants.DIRECTOR;
import static org.tinymediamanager.core.Constants.EPISODE_COUNT;
import static org.tinymediamanager.core.Constants.FIRST_AIRED;
import static org.tinymediamanager.core.Constants.FIRST_AIRED_AS_STRING;
import static org.tinymediamanager.core.Constants.GENRE;
import static org.tinymediamanager.core.Constants.GENRES_AS_STRING;
import static org.tinymediamanager.core.Constants.HAS_NFO_FILE;
import static org.tinymediamanager.core.Constants.IMDB;
import static org.tinymediamanager.core.Constants.REMOVED_EPISODE;
import static org.tinymediamanager.core.Constants.RUNTIME;
import static org.tinymediamanager.core.Constants.SEASON_COUNT;
import static org.tinymediamanager.core.Constants.SORT_TITLE;
import static org.tinymediamanager.core.Constants.STATUS;
import static org.tinymediamanager.core.Constants.TAG;
import static org.tinymediamanager.core.Constants.TAGS_AS_STRING;
import static org.tinymediamanager.core.Constants.TITLE_SORTABLE;
import static org.tinymediamanager.core.Constants.TRAKT;
import static org.tinymediamanager.core.Constants.TVDB;
import static org.tinymediamanager.core.Constants.WATCHED;
import static org.tinymediamanager.core.Constants.WRITER;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.TvShowArtworkHelper;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowMediaFileComparator;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.connector.TvShowToXbmcNfoConnector;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.util.StrgUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * The Class TvShow.
 * 
 * @author Manuel Laggner
 */
public class TvShow extends MediaEntity {
  private static final Logger                LOGGER                = LoggerFactory.getLogger(TvShow.class);
  private static final Comparator<MediaFile> MEDIA_FILE_COMPARATOR = new TvShowMediaFileComparator();

  @JsonProperty
  private String                             dataSource            = "";
  @JsonProperty
  private String                             director              = "";
  @JsonProperty
  private String                             writer                = "";
  @JsonProperty
  private int                                runtime               = 0;
  @JsonProperty
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private Date                               firstAired            = null;
  @JsonProperty
  private String                             status                = "";
  @JsonProperty
  private boolean                            watched               = false;
  @JsonProperty
  private String                             sortTitle             = "";
  @JsonProperty
  private Certification                      certification         = Certification.NOT_RATED;

  @JsonProperty
  private List<String>                       genres                = new CopyOnWriteArrayList<>();
  @JsonProperty
  private List<String>                       tags                  = new CopyOnWriteArrayList<>();
  @JsonProperty
  private HashMap<Integer, String>           seasonPosterUrlMap    = new HashMap<>(0);
  @JsonProperty
  private List<TvShowActor>                  actors                = new CopyOnWriteArrayList<>();

  private List<TvShowEpisode>                episodes              = new CopyOnWriteArrayList<>();
  private HashMap<Integer, MediaFile>        seasonPosters         = new HashMap<>(0);
  private List<TvShowSeason>                 seasons               = new CopyOnWriteArrayList<>();
  private List<MediaGenres>                  genresForAccess       = new CopyOnWriteArrayList<>();
  private String                             titleSortable         = "";
  private Date                               lastWatched           = null;

  private PropertyChangeListener             propertyChangeListener;

  /**
   * Instantiates a tv show. To initialize the propertychangesupport after loading
   */
  public TvShow() {
    // register for dirty flag listener
    super();

    // give tag events from episodes up to the TvShowList
    propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("tag".equals(evt.getPropertyName()) && evt.getSource() instanceof TvShowEpisode) {
          firePropertyChange(evt);
        }
      }
    };
  }

  @Override
  protected Comparator<MediaFile> getMediaFileComparator() {
    return MEDIA_FILE_COMPARATOR;
  }

  /**
   * Initialize after loading.
   */
  @Override
  public void initializeAfterLoading() {
    super.initializeAfterLoading();

    // remove empty tag and null values
    Utils.removeEmptyStringsFromList(tags);
    Utils.removeEmptyStringsFromList(genres);

    // load genres
    for (String genre : new ArrayList<>(genres)) {
      addGenre(MediaGenres.getGenre(genre));
    }

    // create season poster map
    Pattern pattern = Pattern.compile("(?i)season([0-9]{1,4})-poster\\..{2,4}");
    for (MediaFile mf : getMediaFiles(MediaFileType.SEASON_POSTER)) {
      if (mf.getFilename().startsWith("season-specials-poster")) {
        seasonPosters.put(0, mf);
      }
      else {
        // parse out the season from the name
        Matcher matcher = pattern.matcher(mf.getFilename());
        if (matcher.matches()) {
          try {
            int season = Integer.parseInt(matcher.group(1));
            seasonPosters.put(season, mf);
          }
          catch (Exception ignored) {
          }
        }
      }
    }

    for (TvShowEpisode episode : episodes) {
      episode.addPropertyChangeListener(propertyChangeListener);
    }
  }

  /**
   * Overwrites all null/empty elements with "other" value (but might be empty also)<br>
   * For lists, check with 'contains' and add.<br>
   * Do NOT merge path, dateAdded, scraped, mediaFiles and other crucial properties!
   */
  public void merge(TvShow other) {
    super.merge(other);

    // get ours, and merge other values
    for (TvShowEpisode ep : episodes) {
      TvShowEpisode otherEP = other.getEpisode(ep.getSeason(), ep.getEpisode());
      ep.merge(otherEP);
    }
    // get others, and simply add
    for (TvShowEpisode otherEp : other.getEpisodes()) {
      TvShowEpisode ourEP = getEpisode(otherEp.getSeason(), otherEp.getEpisode()); // do not do a contains check!
      if (ourEP == null) {
        TvShowEpisode clone = new TvShowEpisode(otherEp);
        clone.setTvShow(this); // yes!
        addEpisode(clone);
      }
    }
  }

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
    return episodes;
  }

  /**
   * Adds the episode.
   * 
   * @param episode
   *          the episode
   */
  public void addEpisode(TvShowEpisode episode) {
    int oldValue = episodes.size();
    episodes.add(episode);
    episode.addPropertyChangeListener(propertyChangeListener);
    addToSeason(episode);

    Utils.sortList(episodes);

    firePropertyChange(ADDED_EPISODE, null, episode);
    firePropertyChange(EPISODE_COUNT, oldValue, episodes.size());
  }

  /**
   * Gets the episode count.
   * 
   * @return the episode count
   */
  public int getEpisodeCount() {
    return episodes.size();
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

  private void removeFromSeason(TvShowEpisode episode) {
    TvShowSeason season = getSeasonForEpisode(episode);
    season.removeEpisode(episode);
  }

  /**
   * Gets the season for episode.
   * 
   * @param episode
   *          the episode
   * @return the season for episode
   */
  public synchronized TvShowSeason getSeasonForEpisode(TvShowEpisode episode) {
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
      int oldValue = seasons.size();
      season = new TvShowSeason(episode.getSeason(), this);
      seasons.add(season);
      firePropertyChange(ADDED_SEASON, null, season);
      firePropertyChange(SEASON_COUNT, oldValue, seasons.size());
    }

    return season;
  }

  public int getSeasonCount() {
    return seasons.size();
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
    int oldValue = episodes.size();
    if (episodes.size() > 0) {
      for (int i = episodes.size() - 1; i >= 0; i--) {
        TvShowEpisode episode = episodes.get(i);
        episodes.remove(episode);
        episode.removePropertyChangeListener(propertyChangeListener);
        TvShowList.getInstance().removeEpisodeFromDb(episode);
      }
    }

    firePropertyChange(EPISODE_COUNT, oldValue, episodes.size());
  }

  /**
   * Removes the episode.
   * 
   * @param episode
   *          the episode
   */
  public void removeEpisode(TvShowEpisode episode) {
    if (episodes.contains(episode)) {
      int oldValue = episodes.size();
      episodes.remove(episode);
      episode.removePropertyChangeListener(propertyChangeListener);
      removeFromSeason(episode);
      TvShowList.getInstance().removeEpisodeFromDb(episode);
      saveToDb();

      firePropertyChange(REMOVED_EPISODE, null, episode);
      firePropertyChange(EPISODE_COUNT, oldValue, episodes.size());
    }
  }

  /**
   * Removes an episode from tmm and deletes it from the data source
   * 
   * @param episode
   *          the episode to be removed
   */
  public void deleteEpisode(TvShowEpisode episode) {
    if (episodes.contains(episode)) {
      int oldValue = episodes.size();
      episode.deleteFilesSafely();
      episodes.remove(episode);
      episode.removePropertyChangeListener(propertyChangeListener);
      removeFromSeason(episode);
      TvShowList.getInstance().removeEpisodeFromDb(episode);
      saveToDb();

      firePropertyChange(REMOVED_EPISODE, null, episode);
      firePropertyChange(EPISODE_COUNT, oldValue, episodes.size());
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
  @JsonSetter
  public void setGenres(List<MediaGenres> genres) {
    // two way sync of genres

    // first, add new ones
    for (MediaGenres genre : genres) {
      if (!this.genresForAccess.contains(genre)) {
        this.genresForAccess.add(genre);
        if (!this.genres.contains(genre.name())) {
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
    // check against null metadata (e.g. aborted request)
    if (metadata == null) {
      LOGGER.error("metadata was null");
      return;
    }

    // check if metadata has at least a name
    if (StringUtils.isEmpty(metadata.getTitle())) {
      LOGGER.warn("wanted to save empty metadata for " + getTitle());
      return;
    }

    // populate ids
    for (Entry<String, Object> entry : metadata.getIds().entrySet()) {
      setId((String) entry.getKey(), entry.getValue().toString());
    }

    if (config.isTitle()) {
      setTitle(metadata.getTitle());
    }

    if (config.isPlot()) {
      setPlot(metadata.getPlot());
    }

    if (config.isYear()) {
      if (metadata.getYear() != 0) {
        setYear(Integer.toString(metadata.getYear()));
      }
      else {
        setYear("");
      }
    }

    if (config.isRating()) {
      setRating(metadata.getRating());
      setVotes(metadata.getVoteCount());
    }

    if (config.isAired()) {
      setFirstAired(metadata.getReleaseDate());
    }

    if (config.isStatus()) {
      setStatus(metadata.getStatus());
    }

    if (config.isRuntime()) {
      setRuntime(metadata.getRuntime());
    }

    if (config.isCast()) {
      setProductionCompany(StringUtils.join(metadata.getProductionCompanies(), ", "));
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
      // TODO write actor images for tv shows
      // writeActorImages();
    }

    if (config.isCertification()) {
      if (metadata.getCertifications().size() > 0) {
        setCertification(metadata.getCertifications().get(0));
      }
    }

    if (config.isGenres()) {
      setGenres(metadata.getGenres());
    }

    // set scraped
    setScraped(true);

    // update DB
    writeNFO();
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
          setArtworkUrl(art.getDefaultUrl(), MediaFileType.POSTER);
          // and download it
          TvShowArtworkHelper.downloadArtwork(this, MediaFileType.POSTER);
          break;
        }
      }

      // fanart
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.BACKGROUND) {
          // set url
          setArtworkUrl(art.getDefaultUrl(), MediaFileType.FANART);
          // and download it
          TvShowArtworkHelper.downloadArtwork(this, MediaFileType.FANART);
          break;
        }
      }

      // banner
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.BANNER) {
          // set url
          setArtworkUrl(art.getDefaultUrl(), MediaFileType.BANNER);
          // and download it
          TvShowArtworkHelper.downloadArtwork(this, MediaFileType.BANNER);
          break;
        }
      }

      // logo
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.LOGO) {
          // set url
          setArtworkUrl(art.getDefaultUrl(), MediaFileType.LOGO);
          // and download it
          TvShowArtworkHelper.downloadArtwork(this, MediaFileType.LOGO);
          break;
        }
      }

      // clearlogo
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.CLEARLOGO) {
          // set url
          setArtworkUrl(art.getDefaultUrl(), MediaFileType.CLEARLOGO);
          // and download it
          TvShowArtworkHelper.downloadArtwork(this, MediaFileType.CLEARLOGO);
          break;
        }
      }

      // clearart
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.CLEARART) {
          // set url
          setArtworkUrl(art.getDefaultUrl(), MediaFileType.CLEARART);
          // and download it
          TvShowArtworkHelper.downloadArtwork(this, MediaFileType.CLEARART);
          break;
        }
      }

      // thumb
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.THUMB) {
          // set url
          setArtworkUrl(art.getDefaultUrl(), MediaFileType.THUMB);
          // and download it
          TvShowArtworkHelper.downloadArtwork(this, MediaFileType.THUMB);
          break;
        }
      }

      // season poster
      HashMap<Integer, String> seasonPosters = new HashMap<>();
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.SEASON && art.getSeason() >= 0) {
          // check if there is already an artwork for this season
          String url = seasonPosters.get(art.getSeason());
          if (StringUtils.isBlank(url)) {
            setSeasonPosterUrl(art.getSeason(), art.getDefaultUrl());
            TvShowArtworkHelper.downloadSeasonPoster(this, art.getSeason());
            seasonPosters.put(art.getSeason(), art.getDefaultUrl());
          }
        }
      }

      // update DB
      saveToDb();
    }
  }

  /**
   * download the specified type of artwork for this TV show
   * 
   * @param type
   *          the chosen artwork type to be downloaded
   */
  public void downloadArtwork(MediaFileType type) {
    TvShowArtworkHelper.downloadArtwork(this, type);
  }

  /**
   * download season poster
   * 
   * @param season
   *          the season to download the poster for
   */
  public void downloadSeasonPoster(int season) {
    TvShowArtworkHelper.downloadSeasonPoster(this, season);
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
    if (!StringUtils.isEmpty(getArtworkFilename(MediaFileType.POSTER)) && !StringUtils.isEmpty(getArtworkFilename(MediaFileType.FANART))) {
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
    return this.getIdAsString(IMDB);
  }

  /**
   * Sets the imdb id.
   * 
   * @param newValue
   *          the new imdb id
   */
  public void setImdbId(String newValue) {
    this.setId(IMDB, newValue);
  }

  /**
   * Gets the tvdb id.
   * 
   * @return the tvdb id
   */
  public String getTvdbId() {
    return this.getIdAsString(TVDB);
  }

  /**
   * Sets the tvdb id.
   * 
   * @param newValue
   *          the new tvdb id
   */
  public void setTvdbId(String newValue) {
    this.setId(TVDB, newValue);
  }

  /**
   * Gets the TraktTV id.
   * 
   * @return the TraktTV id
   */
  public int getTraktId() {
    return this.getIdAsInt(TRAKT);
  }

  /**
   * Sets the TvRage id.
   * 
   * @param newValue
   *          the new TraktTV id
   */
  public void setTraktId(int newValue) {
    this.setId(TRAKT, newValue);
  }

  /**
   * first aired date.
   * 
   * @return the date
   */
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
  public void setFirstAired(String aired) {
    try {
      setFirstAired(StrgUtils.parseDate(aired));
    }
    catch (ParseException e) {
    }
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
   * Gets the tags.
   * 
   * @return the tags
   */
  public List<String> getTags() {
    return this.tags;
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
    // and re-set TV show path to the actor
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
    return this.actors;
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

    // first add the new ones
    for (TvShowActor actor : newActors) {
      if (!actors.contains(actor)) {
        actors.add(actor);
      }
    }

    // second remove unused
    for (int i = actors.size() - 1; i >= 0; i--) {
      TvShowActor actor = actors.get(i);
      if (!newActors.contains(actor)) {
        actors.remove(actor);
      }
    }

    // and re-set TV show path to the actors
    for (TvShowActor actor : actors) {
      if (StringUtils.isBlank(actor.getEntityRoot())) {
        actor.setEntityRoot(getPathNIO());
      }
    }

    firePropertyChange(ACTORS, null, this.getActors());
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
        if (name.toLowerCase(Locale.ROOT).startsWith("."))
          return false;

        // check if filetype is in our settings
        if (name.toLowerCase(Locale.ROOT).endsWith("nfo")) {
          return true;
        }

        return false;
      }
    };

    TvShow tvShow = null;
    File[] nfoFiles = tvShowDirectory.listFiles(filter);
    if (nfoFiles == null) {
      return tvShow;
    }

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
   * get all episodes to scraper (with season or ep number == -1)
   * 
   * @return a list of all episodes to scrape
   */
  public List<TvShowEpisode> getEpisodesToScrape() {
    List<TvShowEpisode> episodes = new ArrayList<>();
    for (TvShowEpisode episode : new ArrayList<>(this.episodes)) {
      if (episode.getFirstAired() != null || (episode.getSeason() > -1 && episode.getEpisode() > -1)) {
        episodes.add(episode);
      }
    }
    return episodes;
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
   * Gets the season poster url.
   * 
   * @param season
   *          the season
   * @return the season poster url
   */
  public String getSeasonPosterUrl(int season) {
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
  public void setSeasonPosterUrl(int season, String url) {
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
    MediaFile poster = seasonPosters.get(season);
    if (poster == null) {
      return "";
    }
    return poster.getFile().getAbsolutePath();
  }

  Dimension getSeasonPosterSize(int season) {
    MediaFile seasonPoster = seasonPosters.get(season);
    if (seasonPoster != null) {
      return new Dimension(seasonPoster.getVideoWidth(), seasonPoster.getVideoHeight());
    }

    return new Dimension(0, 0);
  }

  /**
   * Sets the season poster.
   * 
   * @param season
   *          the season
   * @param file
   *          the file
   */
  public void setSeasonPoster(int season, File file) {
    MediaFile mf = new MediaFile(file, MediaFileType.SEASON_POSTER);
    setSeasonPoster(season, mf);
  }

  /**
   * Sets the season poster.
   * 
   * @param season
   *          the season
   * @param mf
   *          the media file
   */
  public void setSeasonPoster(int season, MediaFile mf) {
    // check if that MF is already in our show
    MediaFile oldMf = seasonPosters.get(season);
    if (oldMf != null && oldMf.equals(mf)) {
      // it is there - do not add it again
      return;
    }

    mf.gatherMediaInformation();
    addToMediaFiles(mf);

    if (seasonPosters.containsKey(season)) {
      seasonPosters.remove(season);
    }
    seasonPosters.put(season, mf);
  }

  void clearSeasonPoster(int season) {
    MediaFile mf = seasonPosters.get(season);
    if (mf != null) {
      removeFromMediaFiles(mf);
    }
    seasonPosters.remove(season);
  }

  /**
   * Gets the media files of all episodes.<br>
   * (without the TV show MFs like poster/banner/...)
   * 
   * @return the media files
   */
  public List<MediaFile> getEpisodesMediaFiles() {
    List<MediaFile> mediaFiles = new ArrayList<>();
    for (TvShowEpisode episode : new ArrayList<>(this.episodes)) {
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
  public List<Path> getImagesToCache() {
    // get files to cache
    List<Path> filesToCache = new ArrayList<>();

    for (MediaFile mf : new ArrayList<>(getMediaFiles())) {
      if (mf.isGraphic()) {
        filesToCache.add(mf.getFileAsPath());
      }
    }

    for (TvShowEpisode episode : new ArrayList<>(this.episodes)) {
      filesToCache.addAll(episode.getImagesToCache());
    }

    return filesToCache;
  }

  @Override
  public synchronized void callbackForWrittenArtwork(MediaArtworkType type) {
  }

  @Override
  public void saveToDb() {
    // update/insert this TV show to the database
    TvShowList.getInstance().persistTvShow(this);
  }

  @Override
  public void deleteFromDb() {
    // remove this TV show from the database
    TvShowList.getInstance().removeTvShow(this);
  }

  public TvShowEpisode getEpisode(int season, int episode) {
    TvShowEpisode ep = null;

    for (TvShowEpisode e : new ArrayList<>(this.episodes)) {
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
  public boolean hasNewlyAddedEpisodes() {
    for (TvShowEpisode episode : new ArrayList<>(this.episodes)) {
      if (episode.isNewlyAdded()) {
        return true;
      }
    }
    return false;
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
    if (!scraped) {
      if (!plot.isEmpty() && !(year.isEmpty() || year.equals("0")) && !(genres == null || genres.size() == 0)
          && !(actors == null || actors.size() == 0)) {
        return true;
      }
    }
    return scraped;
  }

  /**
   * <b>PHYSICALLY</b> deletes a complete TV show by moving it to datasource backup folder<br>
   * DS\.backup\&lt;moviename&gt;
   */
  public boolean deleteFilesSafely() {
    return Utils.deleteDirectorySafely(Paths.get(getPath()), getDataSource());
  }

  /**
   * this is legacy code to prevent data loss due to DB changes
   * 
   * @param studio
   *          the studio
   */
  @Deprecated
  @JsonSetter
  public void setStudio(String studio) {
    setProductionCompany(studio);
  }
}
