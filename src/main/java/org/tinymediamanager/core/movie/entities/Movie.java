/*
 * Copyright 2012 - 2015 Manuel Laggner
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
package org.tinymediamanager.core.movie.entities;

import static org.tinymediamanager.core.Constants.*;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import org.tinymediamanager.core.movie.MovieArtworkHelper;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieMediaFileComparator;
import org.tinymediamanager.core.movie.MovieMediaSource;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieTrailerQuality;
import org.tinymediamanager.core.movie.MovieTrailerSources;
import org.tinymediamanager.core.movie.connector.MovieConnectors;
import org.tinymediamanager.core.movie.connector.MovieToMpNfoConnector;
import org.tinymediamanager.core.movie.connector.MovieToXbmcNfoConnector;
import org.tinymediamanager.core.movie.tasks.MovieActorImageFetcher;
import org.tinymediamanager.core.movie.tasks.MovieTrailerDownloadTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaCastMember;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.mediaprovider.IMovieSetMetadataProvider;
import org.tinymediamanager.scraper.util.StrgUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The main class for movies.
 * 
 * @author Manuel Laggner / Myron Boyle
 */
public class Movie extends MediaEntity {
  @XmlTransient
  private static final Logger LOGGER          = LoggerFactory.getLogger(Movie.class);

  @JsonProperty
  private String              sortTitle       = "";
  @JsonProperty
  private String              tagline         = "";
  @JsonProperty
  private int                 votes           = 0;
  @JsonProperty
  private int                 runtime         = 0;
  @JsonProperty
  private String              director        = "";
  @JsonProperty
  private String              writer          = "";
  @JsonProperty
  private String              dataSource      = "";
  @JsonProperty
  private boolean             watched         = false;
  @JsonProperty
  private boolean             isDisc          = false;
  @JsonProperty
  private String              spokenLanguages = "";
  @JsonProperty
  private boolean             subtitles       = false;
  @JsonProperty
  private String              country         = "";
  @JsonProperty
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private Date                releaseDate     = null;
  @JsonProperty
  private boolean             multiMovieDir   = false;                               // we detected more movies in same folder
  @JsonProperty
  private int                 top250          = 0;
  @JsonProperty
  private MovieMediaSource    mediaSource     = MovieMediaSource.UNKNOWN;            // DVD, Bluray, etc
  @JsonProperty
  private boolean             videoIn3D       = false;
  @JsonProperty
  private Certification       certification   = Certification.NOT_RATED;
  @JsonProperty
  private UUID                movieSetId;

  @JsonProperty
  private List<String>        genres          = new ArrayList<String>(1);
  @JsonProperty
  private List<String>        tags            = new ArrayList<String>(0);
  @JsonProperty
  private List<String>        extraThumbs     = new ArrayList<String>(0);
  @JsonProperty
  private List<String>        extraFanarts    = new ArrayList<String>(0);
  @JsonProperty
  private List<MovieActor>    actors          = new ArrayList<MovieActor>();
  @JsonProperty
  private List<MovieProducer> producers       = new ArrayList<MovieProducer>(0);
  @JsonProperty
  private List<MovieTrailer>  trailer         = new ArrayList<MovieTrailer>(0);

  private MovieSet            movieSet;
  private String              titleSortable   = "";
  private boolean             newlyAdded      = false;
  private Date                lastWatched     = null;
  private List<MediaGenres>   genresForAccess = new ArrayList<MediaGenres>(0);

  static {
    mediaFileComparator = new MovieMediaFileComparator();
  }

  /**
   * Instantiates a new movie. To initialize the propertychangesupport after loading
   */
  public Movie() {
    // register for dirty flag listener
    super();
  }

  /**
   * checks if this movie has been scraped.<br>
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
   * Gets the sort title.
   * 
   * @return the sort title
   */
  public String getSortTitle() {
    return sortTitle;
  }

  /**
   * Sets the sort title.
   * 
   * @param newValue
   *          the new sort title
   */
  public void setSortTitle(String newValue) {
    String oldValue = this.sortTitle;
    this.sortTitle = newValue;
    firePropertyChange(SORT_TITLE, oldValue, newValue);
  }

  /**
   * Sets the sort title from movie set.
   */
  public void setSortTitleFromMovieSet() {
    if (movieSet != null) {
      int index = movieSet.getMovieIndex(this) + 1;
      setSortTitle(movieSet.getTitle() + String.format("%02d", index));
    }
  }

  /**
   * Returns the sortable variant of title<br>
   * eg "The Bourne Legacy" -> "Bourne Legacy, The".
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

  /**
   * Gets the checks for nfo file.
   * 
   * @return the checks for nfo file
   */
  public Boolean getHasNfoFile() {
    List<MediaFile> mf = getMediaFiles(MediaFileType.NFO);
    if (mf != null && mf.size() > 0) {
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
   * Gets the checks for trailer.
   * 
   * @return the checks for trailer
   */
  public Boolean getHasTrailer() {
    if (trailer != null && trailer.size() > 0) {
      return true;
    }

    // check if there is a mediafile (trailer)
    if (!getMediaFiles(MediaFileType.TRAILER).isEmpty()) {
      return true;
    }

    return false;
  }

  /**
   * Gets the title for ui.
   * 
   * @return the title for ui
   */
  public String getTitleForUi() {
    StringBuffer titleForUi = new StringBuffer(title);
    if (year != null && !year.isEmpty()) {
      titleForUi.append(" (");
      titleForUi.append(year);
      titleForUi.append(")");
    }
    return titleForUi.toString();
  }

  /**
   * Initialize after loading.
   */
  public void initializeAfterLoading() {
    super.initializeAfterLoading();

    // remove empty tag and null values
    Utils.removeEmptyStringsFromList(tags);
    Utils.removeEmptyStringsFromList(genres);

    // load genres
    for (String genre : new ArrayList<String>(genres)) {
      addGenre(MediaGenres.getGenre(genre));
    }

    // link with movie set
    if (movieSetId != null) {
      movieSet = MovieList.getInstance().lookupMovieSet(movieSetId);
    }
  }

  /**
   * Adds the actor.
   * 
   * @param obj
   *          the obj
   */
  public void addActor(MovieActor obj) {
    actors.add(obj);
    firePropertyChange(ACTORS, null, this.getActors());
  }

  /**
   * Gets the trailers
   * 
   * @return the trailers
   */
  public List<MovieTrailer> getTrailer() {
    return this.trailer;
  }

  /**
   * Adds the trailer.
   * 
   * @param obj
   *          the obj
   */
  public void addTrailer(MovieTrailer obj) {
    trailer.add(obj);
    firePropertyChange(TRAILER, null, trailer);
  }

  /**
   * Removes the all trailers.
   */
  public void removeAllTrailers() {
    trailer.clear();
    firePropertyChange(TRAILER, null, trailer);
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
    firePropertyChange(TAG, null, tags);
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
    firePropertyChange(TAG, null, tags);
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

    Utils.removeEmptyStringsFromList(tags);

    firePropertyChange(TAG, null, tags);
    firePropertyChange(TAGS_AS_STRING, null, tags);
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
   * Gets the tags.
   * 
   * @return the tags
   */
  public List<String> getTags() {
    return this.tags;
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

  /** has movie local (or any mediafile inline) subtitles? */
  public boolean hasSubtitles() {
    if (this.subtitles) {
      return true; // local ones found
    }

    for (MediaFile mf : getMediaFiles(MediaFileType.VIDEO)) {
      if (mf.hasSubtitles()) {
        return true;
      }
    }

    return false;
  }

  /** set subtitles */
  public void setSubtitles(boolean sub) {
    this.subtitles = sub;
  }

  /**
   * Find actor images.
   */
  public void findActorImages() {
    if (MovieModuleManager.MOVIE_SETTINGS.isWriteActorImages()) {
      // get all files from the actors path
      File[] actorImages = new File(getPath(), MovieActor.ACTOR_DIR).listFiles();
      if (actorImages != null && actorImages.length > 0) {
        // search all local actor images
        for (MovieActor actor : getActors()) {
          if (StringUtils.isNotBlank(actor.getThumbPath())) {
            continue;
          }

          String actorName = actor.getName().replace(" ", "_");

          Pattern pattern = Pattern.compile("(?i)" + Pattern.quote(actorName) + "\\.(tbn|jpg|png)");
          for (File file : actorImages) {
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.matches()) {
              actor.setThumbPath(file.getAbsolutePath());
            }
          }
        }
      }
    }
  }

  /**
   * Gets the actors.
   * 
   * @return the actors
   */
  public List<MovieActor> getActors() {
    return this.actors;
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
   * Gets the imdb id.
   * 
   * @return the imdb id
   */
  public String getImdbId() {
    Object obj = ids.get(IMDB);
    if (obj == null || !Utils.isValidImdbId(obj.toString())) {
      return "";
    }
    return obj.toString();
  }

  /**
   * Gets the tmdb id.
   * 
   * @return the tmdb id
   */
  public int getTmdbId() {
    int id = 0;
    try {
      id = Integer.valueOf(String.valueOf(ids.get(TMDB)));
    }
    catch (Exception e) {
      return 0;
    }
    return id;
  }

  /**
   * Sets the tmdb id.
   * 
   * @param newValue
   *          the new tmdb id
   */
  public void setTmdbId(int newValue) {
    int oldValue = getTmdbId();
    ids.put(TMDB, newValue);
    firePropertyChange("tmdbId", oldValue, newValue);
  }

  /**
   * Gets the TraktTV id.
   * 
   * @return the TraktTV id
   */
  public int getTraktId() {
    int id = 0;
    try {
      id = Integer.valueOf(String.valueOf(ids.get(TRAKT)));
    }
    catch (Exception e) {
      return 0;
    }
    return id;
  }

  /**
   * Sets the TraktTV id.
   * 
   * @param newValue
   *          the new TraktTV id
   */
  public void setTraktId(int newValue) {
    int oldValue = getTraktId();
    ids.put(TRAKT, newValue);
    firePropertyChange("traktId", oldValue, newValue);
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
   * Gets the runtime in minutes
   * 
   * @return the runtime
   */
  public int getRuntime() {
    int runtimeFromMi = getRuntimeFromMediaFilesInMinutes();
    if (MovieModuleManager.MOVIE_SETTINGS.isRuntimeFromMediaInfo() && runtimeFromMi > 0) {
      return runtimeFromMi;
    }
    return runtime == 0 ? runtimeFromMi : runtime;
  }

  /**
   * Gets the tagline.
   * 
   * @return the tagline
   */
  public String getTagline() {
    return tagline;
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
   * Checks for file.
   * 
   * @param filename
   *          the filename
   * @return true, if successful
   */
  public boolean hasFile(String filename) {
    if (StringUtils.isEmpty(filename)) {
      return false;
    }

    for (MediaFile file : new ArrayList<MediaFile>(getMediaFiles())) {
      if (filename.compareTo(file.getFilename()) == 0) {
        return true;
      }
    }

    return false;
  }

  /**
   * Removes the actor.
   * 
   * @param obj
   *          the obj
   */
  public void removeActor(MovieActor obj) {
    actors.remove(obj);
    firePropertyChange(ACTORS, null, this.getActors());
  }

  /**
   * Gets the extra thumbs.
   * 
   * @return the extra thumbs
   */
  public List<String> getExtraThumbs() {
    return extraThumbs;
  }

  /**
   * Sets the extra thumbs.
   * 
   * @param extraThumbs
   *          the new extra thumbs
   */
  public void setExtraThumbs(List<String> extraThumbs) {
    this.extraThumbs = extraThumbs;
  }

  /**
   * Gets the extra fanarts.
   * 
   * @return the extra fanarts
   */
  public List<String> getExtraFanarts() {
    return extraFanarts;
  }

  /**
   * Sets the extra fanarts.
   * 
   * @param extraFanarts
   *          the new extra fanarts
   */
  public void setExtraFanarts(List<String> extraFanarts) {
    this.extraFanarts = extraFanarts;
  }

  /**
   * Sets the imdb id.
   * 
   * @param newValue
   *          the new imdb id
   */
  public void setImdbId(String newValue) {
    if (!Utils.isValidImdbId(newValue)) {
      newValue = "";
    }
    String oldValue = getImdbId();
    ids.put(IMDB, newValue);
    firePropertyChange("imdbId", oldValue, newValue);
  }

  /**
   * Sets the metadata.
   * 
   * @param metadata
   *          the new metadata
   * @param config
   *          the config
   */
  /**
   * @param metadata
   */
  public void setMetadata(MediaMetadata metadata, MovieScraperMetadataConfig config) {
    if (metadata == null) {
      LOGGER.error("metadata was null");
      return;
    }

    // check if metadata has at least a name
    if (StringUtils.isEmpty(metadata.getStringValue(MediaMetadata.TITLE))) {
      LOGGER.warn("wanted to save empty metadata for " + getTitle());
      return;
    }

    setIds(metadata.getIds());

    // set chosen metadata
    if (config.isTitle()) {
      setTitle(metadata.getStringValue(MediaMetadata.TITLE));
    }

    if (config.isOriginalTitle()) {
      setOriginalTitle(metadata.getStringValue(MediaMetadata.ORIGINAL_TITLE));
    }

    if (config.isTagline()) {
      setTagline(metadata.getStringValue(MediaMetadata.TAGLINE));
    }

    if (config.isPlot()) {
      setPlot(metadata.getStringValue(MediaMetadata.PLOT));
    }

    if (config.isYear()) {
      setYear(metadata.getStringValue(MediaMetadata.YEAR));
      setReleaseDate(metadata.getDateValue(MediaMetadata.RELEASE_DATE));
    }

    if (config.isRating()) {
      setRating(metadata.getFloatValue(MediaMetadata.RATING));
      setVotes(metadata.getIntegerValue(MediaMetadata.VOTE_COUNT));
      setTop250(metadata.getIntegerValue(MediaMetadata.TOP_250));
    }

    if (config.isRuntime()) {
      setRuntime(metadata.getIntegerValue(MediaMetadata.RUNTIME));
    }

    setSpokenLanguages(metadata.getStringValue(MediaMetadata.SPOKEN_LANGUAGES));
    setCountry(metadata.getStringValue(MediaMetadata.COUNTRY));

    // certifications
    if (config.isCertification()) {
      if (metadata.getCertifications() != null && metadata.getCertifications().size() > 0) {
        setCertification(metadata.getCertifications().get(0));
      }
    }

    // cast
    if (config.isCast()) {
      setProductionCompany(metadata.getStringValue(MediaMetadata.PRODUCTION_COMPANY));
      List<MovieActor> actors = new ArrayList<MovieActor>();
      List<MovieProducer> producers = new ArrayList<MovieProducer>();
      String director = "";
      String writer = "";
      for (MediaCastMember member : metadata.getCastMembers()) {
        switch (member.getType()) {
          case ACTOR:
            MovieActor actor = new MovieActor();
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

          case PRODUCER:
            MovieProducer producer = new MovieProducer();
            producer.setName(member.getName());
            producer.setRole(member.getPart());
            producer.setThumbUrl(member.getImageUrl());
            producers.add(producer);
            break;

          default:
            break;
        }
      }
      setActors(actors);
      setDirector(director);
      setWriter(writer);
      setProducers(producers);
      writeActorImages();
    }

    // genres
    if (config.isGenres()) {
      setGenres(metadata.getGenres());
    }

    // set scraped
    setScraped(true);

    // create MovieSet
    if (config.isCollection()) {
      int col = metadata.getIntegerValue(MediaMetadata.TMDB_SET);
      if (col != 0) {
        MovieSet movieSet = MovieList.getInstance().getMovieSet(metadata.getStringValue(MediaMetadata.COLLECTION_NAME), col);
        if (movieSet != null && movieSet.getTmdbId() == 0) {
          movieSet.setTmdbId(col);
          // get movieset metadata
          try {
            List<MediaScraper> sets = MediaScraper.getMediaScrapers(ScraperType.MOVIE_SET);
            if (sets != null && sets.size() > 0) {
              MediaScraper first = sets.get(0); // just get first
              IMovieSetMetadataProvider mp = ((IMovieSetMetadataProvider) first.getMediaProvider());
              MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE_SET);
              options.setTmdbId(col);
              options.setLanguage(MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage());
              options.setCountry(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry());

              MediaMetadata info = mp.getMetadata(options);
              if (info != null && StringUtils.isNotBlank(info.getStringValue(MediaMetadata.TITLE))) {
                movieSet.setTitle(info.getStringValue(MediaMetadata.TITLE));
                movieSet.setPlot(info.getStringValue(MediaMetadata.PLOT));
                movieSet.setArtworkUrl(info.getStringValue(MediaMetadata.POSTER_URL), MediaFileType.POSTER);
                movieSet.setArtworkUrl(info.getStringValue(MediaMetadata.BACKGROUND_URL), MediaFileType.FANART);
              }
            }
          }
          catch (Exception e) {
          }
        }

        // add movie to movieset
        if (movieSet != null) {
          // first remove from "old" movieset
          setMovieSet(null);

          // add to new movieset
          // movieSet.addMovie(this);
          setMovieSet(movieSet);
          movieSet.insertMovie(this);
          movieSet.updateMovieSorttitle();
        }
      }
    }

    // update DB
    saveToDb();
    writeNFO();
  }

  /**
   * Sets the trailers; first one is "inNFO" if not a local one.
   * 
   * @param trailers
   *          the new trailers
   */
  public void setTrailers(List<MovieTrailer> trailers) {
    MovieTrailer preferredTrailer = null;
    removeAllTrailers();

    // set preferred trailer
    if (MovieModuleManager.MOVIE_SETTINGS.isUseTrailerPreference()) {
      MovieTrailerQuality desiredQuality = MovieModuleManager.MOVIE_SETTINGS.getTrailerQuality();
      MovieTrailerSources desiredSource = MovieModuleManager.MOVIE_SETTINGS.getTrailerSource();

      // search for quality and provider
      for (MovieTrailer trailer : trailers) {
        if (desiredQuality.containsQuality(trailer.getQuality()) && desiredSource.containsSource(trailer.getProvider())) {
          trailer.setInNfo(Boolean.TRUE);
          preferredTrailer = trailer;
          break;
        }
      }

      // search for quality
      if (preferredTrailer == null) {
        for (MovieTrailer trailer : trailers) {
          if (desiredQuality.containsQuality(trailer.getQuality())) {
            trailer.setInNfo(Boolean.TRUE);
            preferredTrailer = trailer;
            break;
          }
        }
      }
    }

    // add trailers
    if (preferredTrailer != null) {
      addTrailer(preferredTrailer);
    }
    for (MovieTrailer trailer : trailers) {
      // preferred trailer has already been added
      if (preferredTrailer != null && preferredTrailer == trailer) {
        continue;
      }

      // if still no preferred trailer has been set, then mark the first one
      if (preferredTrailer == null && this.trailer.size() == 0 && !trailer.getUrl().startsWith("file")) {
        trailer.setInNfo(Boolean.TRUE);
      }

      addTrailer(trailer);
    }

    if (MovieModuleManager.MOVIE_SETTINGS.isAutomaticTrailerDownload() && getMediaFiles(MediaFileType.TRAILER).isEmpty() && !trailer.isEmpty()) {
      MovieTrailer trailer = this.trailer.get(0);
      MovieTrailerDownloadTask task = new MovieTrailerDownloadTask(trailer, this);
      TmmTaskManager.getInstance().addDownloadTask(task);
    }

    // persist
    saveToDb();
    writeNFO();
  }

  /**
   * Gets the metadata.
   * 
   * @return the metadata
   */
  public MediaMetadata getMetadata() {
    MediaMetadata md = new MediaMetadata("");

    for (Entry<String, Object> entry : ids.entrySet()) {
      md.setId(entry.getKey(), entry.getValue());
    }

    md.storeMetadata(MediaMetadata.TITLE, title);
    md.storeMetadata(MediaMetadata.ORIGINAL_TITLE, originalTitle);
    md.storeMetadata(MediaMetadata.TAGLINE, tagline);
    md.storeMetadata(MediaMetadata.PLOT, plot);
    md.storeMetadata(MediaMetadata.YEAR, year);
    md.storeMetadata(MediaMetadata.RATING, rating);
    md.storeMetadata(MediaMetadata.VOTE_COUNT, votes);
    md.storeMetadata(MediaMetadata.RUNTIME, runtime);
    md.addCertification(certification);

    return md;
  }

  /**
   * Sets the artwork.
   * 
   * @param md
   *          the md
   * @param config
   *          the config
   */
  public void setArtwork(MediaMetadata md, MovieScraperMetadataConfig config) {
    setArtwork(md.getMediaArt(MediaArtworkType.ALL), config);
  }

  /**
   * Sets the artwork.
   * 
   * @param artwork
   *          the artwork
   * @param config
   *          the config
   */
  public void setArtwork(List<MediaArtwork> artwork, MovieScraperMetadataConfig config) {
    if (config.isArtwork()) {
      MovieArtworkHelper.setArtwork(this, artwork);
    }
  }

  /**
   * Sets the actors.
   * 
   * @param newActors
   *          the new actors
   */
  public void setActors(List<MovieActor> newActors) {
    // two way sync of actors

    // first remove unused
    for (int i = actors.size() - 1; i >= 0; i--) {
      MovieActor actor = actors.get(i);
      if (!newActors.contains(actor)) {
        actors.remove(actor);
      }
    }

    // second add the new ones
    for (int i = 0; i < newActors.size(); i++) {
      MovieActor actor = newActors.get(i);
      if (!actors.contains(actor)) {
        try {
          actors.add(i, actor);
        }
        catch (IndexOutOfBoundsException e) {
          actors.add(actor);
        }
      }
      else {
        int indexOldList = actors.indexOf(actor);
        if (i != indexOldList) {
          MovieActor oldActor = actors.remove(indexOldList);
          try {
            actors.add(i, oldActor);
          }
          catch (IndexOutOfBoundsException e) {
            actors.add(oldActor);
          }
        }
      }
    }

    // third - rename thumbs if needed
    if (MovieModuleManager.MOVIE_SETTINGS.isWriteActorImages()) {
      for (MovieActor actor : actors) {
        if (StringUtils.isNotBlank(actor.getThumbPath())) {
          // build expected filename
          String actorName = getPath() + File.separator + MovieActor.ACTOR_DIR + File.separator + actor.getName().replace(" ", "_") + "."
              + FilenameUtils.getExtension(actor.getThumbPath());
          // check if equal
          if (!actorName.equals(actor.getThumbPath())) {
            // rename
            try {
              FileUtils.moveFile(new File(actor.getThumbPath()), new File(actorName));
            }
            catch (IOException e) {
              LOGGER.warn("couldn't rename actor thumb (" + actor.getThumbPath() + "): " + e.getMessage());
            }
          }
        }
      }
    }

    firePropertyChange(ACTORS, null, this.getActors());
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

    firePropertyChange(TITLE_FOR_UI, oldValue, newValue);

    oldValue = this.titleSortable;
    titleSortable = "";
    firePropertyChange(TITLE_SORTABLE, oldValue, titleSortable);
  }

  /**
   * Sets the runtime in minutes
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
   * Sets the tagline.
   * 
   * @param newValue
   *          the new tagline
   */
  public void setTagline(String newValue) {
    String oldValue = this.tagline;
    this.tagline = newValue;
    firePropertyChange("tagline", oldValue, newValue);
  }

  /**
   * Sets the year.
   * 
   * @param newValue
   *          the new year
   */
  @Override
  public void setYear(String newValue) {
    String oldValue = year;
    super.setYear(newValue);

    firePropertyChange(TITLE_FOR_UI, oldValue, newValue);
  }

  /**
   * all XBMC supported NFO names. (without path!)
   * 
   * @param nfo
   *          the nfo
   * @return the nfo filename
   */
  public String getNfoFilename(MovieNfoNaming nfo) {
    List<MediaFile> mfs = getMediaFiles(MediaFileType.VIDEO);
    if (mfs != null && mfs.size() > 0) {
      return getNfoFilename(nfo, mfs.get(0).getFilename());
    }
    else {
      return getNfoFilename(nfo, ""); // no video files
    }
  }

  /**
   * all XBMC supported NFO names. (without path!)
   * 
   * @param nfo
   *          the nfo filenaming
   * @param newMovieFilename
   *          the new/desired movie filename
   * @return the nfo filename
   */
  public String getNfoFilename(MovieNfoNaming nfo, String newMovieFilename) {
    String filename = "";
    switch (nfo) {
      case FILENAME_NFO:
        if (isDisc()) {
          // if filename is activated, we generate them accordingly MF(1)
          // but if disc, fixtate this
          if (new File(path, "VIDEO_TS.ifo").exists() || new File(path, "VIDEO_TS").exists()) {
            filename = "VIDEO_TS.nfo";
          }
          else if (new File(path, "index.bdmv").exists()) {
            filename = "index.nfo";
          }
          else if (new File(path, "BDMV").exists()) {
            filename = "BDMV.nfo";
          }
        }
        else {
          String movieFilename = FilenameUtils.getBaseName(newMovieFilename);
          filename += movieFilename.isEmpty() ? "" : Utils.cleanStackingMarkers(movieFilename) + ".nfo"; // w/o stacking information
        }
        break;
      case MOVIE_NFO:
        filename += "movie.nfo";
        break;
      case DISC_NFO:
        if (isDisc()) {
          File dir = new File(path, "VIDEO_TS");
          if (dir.exists() && dir.isDirectory()) {
            filename = "VIDEO_TS" + File.separator + "VIDEO_TS.nfo";
          }
          dir = new File(path, "BDMV");
          if (dir.exists() && dir.isDirectory()) {
            filename = "BDMV" + File.separator + "index.nfo";
          }
        }
        break;
      default:
        filename = "";
        break;
    }
    // LOGGER.trace("getNfoFilename: '" + newMovieFilename + "' / " + nfo + " -> '" + filename + "'");
    return filename;
  }

  /**
   * get trailer name (w/o extension)<br>
   * &lt;moviefile&gt;-trailer.ext
   * 
   * @return the trailer basename
   */
  public String getTrailerBasename() {
    List<MediaFile> mfs = getMediaFiles(MediaFileType.VIDEO);
    if (mfs != null && mfs.size() > 0) {
      return Utils.cleanStackingMarkers(mfs.get(0).getBasename());
    }
    return null;
  }

  /**
   * download the specified type of artwork for this movie
   *
   * @param type
   *          the chosen artwork type to be downloaded
   */
  public void downloadArtwork(MediaFileType type) {
    MovieArtworkHelper.downloadArtwork(this, type);
  }

  /**
   * Write actor images.
   */
  public void writeActorImages() {
    // check if actor images shall be written
    if (!MovieModuleManager.MOVIE_SETTINGS.isWriteActorImages() || isMultiMovieDir()) {
      return;
    }

    MovieActorImageFetcher task = new MovieActorImageFetcher(this);
    TmmTaskManager.getInstance().addImageDownloadTask(task);
  }

  /**
   * Write nfo.
   */
  public void writeNFO() {
    if (MovieModuleManager.MOVIE_SETTINGS.getMovieConnector() == MovieConnectors.MP) {
      MovieToMpNfoConnector.setData(this);
    }
    else {
      MovieToXbmcNfoConnector.setData(this);
    }
    firePropertyChange(HAS_NFO_FILE, false, true);
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
   * Gets the checks for rating.
   * 
   * @return the checks for rating
   */
  public boolean getHasRating() {
    if (rating > 0 || scraped) {
      return true;
    }
    return false;
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
   * Checks if this movie is in a folder with other movies and not in an own folder<br>
   * so disable everything except renaming
   * 
   * @return true, if in datasource root
   */
  public boolean isMultiMovieDir() {
    return multiMovieDir;
  }

  /**
   * Sets the flag, that the movie is not in an own folder<br>
   * so disable everything except renaming
   * 
   * @param multiDir
   *          true/false
   */
  public void setMultiMovieDir(boolean multiDir) {
    this.multiMovieDir = multiDir;
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
   * Gets the movie set.
   * 
   * @return the movieset
   */
  public MovieSet getMovieSet() {
    return movieSet;
  }

  /**
   * Sets the movie set.
   * 
   * @param newValue
   *          the new movie set
   */
  public void setMovieSet(MovieSet newValue) {
    MovieSet oldValue = this.movieSet;
    this.movieSet = newValue;

    if (newValue == null) {
      movieSetId = null;
    }
    else {
      movieSetId = newValue.getDbId();
    }

    // remove movieset-sorttitle
    if (oldValue != null && newValue == null) {
      setSortTitle("");
    }

    firePropertyChange(MOVIESET, oldValue, newValue);
    firePropertyChange(MOVIESET_TITLE, oldValue, newValue);
  }

  public void movieSetTitleChanged() {
    firePropertyChange(MOVIESET_TITLE, null, "");
  }

  public String getMovieSetTitle() {
    if (movieSet != null) {
      return movieSet.getTitle();
    }
    return "";
  }

  /**
   * Removes the from movie set.
   */
  public void removeFromMovieSet() {
    if (movieSet != null) {
      movieSet.removeMovie(this);
    }
    setMovieSet(null);
  }

  /**
   * is this a disc movie folder (video_ts / bdmv)?.
   * 
   * @return true, if is disc
   */
  public boolean isDisc() {
    return isDisc;
  }

  /**
   * is this a disc movie folder (video_ts / bdmv)?.
   * 
   * @param isDisc
   *          the new disc
   */
  public void setDisc(boolean isDisc) {
    this.isDisc = isDisc;
  }

  /**
   * has this movie been newlay added in our list?!
   * 
   * @return true/false
   */
  public boolean isNewlyAdded() {
    return this.newlyAdded;
  }

  /**
   * has this movie been newlay added in our list?!
   * 
   * @param newlyAdded
   *          true/false
   */
  public void setNewlyAdded(boolean newlyAdded) {
    this.newlyAdded = newlyAdded;
  }

  /**
   * Gets the media info video format (i.e. 720p).
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
   */
  public String getMediaInfoVideoCodec() {
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (videos.size() > 0) {
      MediaFile mediaFile = videos.get(0);
      return mediaFile.getVideoCodec();
    }

    return "";
  }

  public int getMediaInfoVideoBitrate() {
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (videos.size() > 0) {
      MediaFile mediaFile = videos.get(0);
      return mediaFile.getOverallBitRate();
    }

    return 0;
  }

  /**
   * Gets the media info audio codec (i.e mp3) and channels (i.e. 6 at 5.1 sound)
   */
  public String getMediaInfoAudioCodecAndChannels() {
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (videos.size() > 0) {
      MediaFile mediaFile = videos.get(0);
      return mediaFile.getAudioCodec() + "_" + mediaFile.getAudioChannels();
    }

    return "";
  }

  public String getMediaInfoVideoResolution() {

    return "";
  }

  public void setSpokenLanguages(String newValue) {
    String oldValue = this.spokenLanguages;
    this.spokenLanguages = newValue;
    firePropertyChange(SPOKEN_LANGUAGES, oldValue, newValue);
  }

  public String getSpokenLanguages() {
    return this.spokenLanguages;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String newValue) {
    String oldValue = this.country;
    this.country = newValue;
    firePropertyChange(COUNTRY, oldValue, newValue);
  }

  public MovieMediaSource getMediaSource() {
    return mediaSource;
  }

  public void setMediaSource(MovieMediaSource newValue) {
    MovieMediaSource oldValue = this.mediaSource;
    this.mediaSource = newValue;
    firePropertyChange(MEDIA_SOURCE, oldValue, newValue);
  }

  /**
   * Gets the images to cache.
   */
  public List<File> getImagesToCache() {
    // get files to cache
    List<File> filesToCache = new ArrayList<File>();
    for (MediaFile mf : new ArrayList<MediaFile>(getMediaFiles())) {
      if (mf.isGraphic()) {
        filesToCache.add(mf.getFile());
      }
    }

    return filesToCache;
  }

  public List<MediaFile> getMediaFilesContainingAudioStreams() {
    List<MediaFile> mediaFilesWithAudioStreams = new ArrayList<MediaFile>(1);

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
    List<MediaFile> mediaFilesWithSubtitles = new ArrayList<MediaFile>(1);

    // look in the first media file if it has subtitles
    List<MediaFile> videoFiles = getMediaFiles(MediaFileType.VIDEO);
    if (videoFiles.size() > 0) {
      MediaFile videoFile = videoFiles.get(0);
      if (videoFile.hasSubtitles()) {
        mediaFilesWithSubtitles.add(videoFile);
      }
    }

    // look for all other types
    for (MediaFile mediaFile : getMediaFiles(MediaFileType.SUBTITLE)) {
      if (mediaFile.hasSubtitles()) {
        mediaFilesWithSubtitles.add(mediaFile);
      }
    }

    return mediaFilesWithSubtitles;
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

  public Date getReleaseDate() {
    return releaseDate;
  }

  @JsonIgnore
  public void setReleaseDate(Date newValue) {
    Date oldValue = this.releaseDate;
    this.releaseDate = newValue;
    firePropertyChange(RELEASE_DATE, oldValue, newValue);
    firePropertyChange(RELEASE_DATE_AS_STRING, oldValue, newValue);
  }

  /**
   * release date as yyyy-mm-dd<br>
   * https://xkcd.com/1179/ :P
   */
  public String getReleaseDateFormatted() {
    if (this.releaseDate == null) {
      return "";
    }
    return new SimpleDateFormat("yyyy-MM-dd").format(this.releaseDate);
  }

  /**
   * Gets the first aired as a string, formatted in the system locale.
   */
  public String getReleaseDateAsString() {
    if (this.releaseDate == null) {
      return "";
    }
    return SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(releaseDate);
  }

  /**
   * convenient method to set the release date (parsed from string).
   */
  public void setReleaseDate(String dateAsString) throws ParseException {
    setReleaseDate(StrgUtils.parseDate(dateAsString));
  }

  public Date getLastWatched() {
    return lastWatched;
  }

  public void setLastWatched(Date lastWatched) {
    this.lastWatched = lastWatched;
  }

  @Override
  public void saveToDb() {
    // update/insert this movie to the database
    MovieList.getInstance().persistMovie(this);
  }

  @Override
  public void deleteFromDb() {
    // remove this movie from the database
    MovieList.getInstance().removeMovieFromDb(this);
  }

  @Override
  public synchronized void callbackForWrittenArtwork(MediaArtworkType type) {
    if (MovieModuleManager.MOVIE_SETTINGS.getMovieConnector() == MovieConnectors.MP) {
      writeNFO();
    }
  }

  public List<MediaFile> getVideoFiles() {
    return getMediaFiles(MediaFileType.VIDEO);
  }

  public int getTop250() {
    return top250;
  }

  public void setVideoIn3D(boolean newValue) {
    boolean oldValue = this.videoIn3D;
    this.videoIn3D = newValue;
    firePropertyChange(VIDEO_IN_3D, oldValue, newValue);
  }

  public boolean isVideoIn3D() {
    String video3DFormat = "";
    List<MediaFile> videos = getMediaFiles(MediaFileType.VIDEO);
    if (videos.size() > 0) {
      MediaFile mediaFile = videos.get(0);
      video3DFormat = mediaFile.getVideo3DFormat();
    }

    return videoIn3D || StringUtils.isNotBlank(video3DFormat);
  }

  public void setTop250(int newValue) {
    int oldValue = this.top250;
    this.top250 = newValue;
    firePropertyChange(TOP250, oldValue, newValue);
  }

  public void addProducer(MovieProducer obj) {
    producers.add(obj);
    firePropertyChange(PRODUCERS, null, producers);
  }

  public void removeProducer(MovieProducer obj) {
    producers.remove(obj);
    firePropertyChange(PRODUCERS, null, producers);
  }

  public void setProducers(List<MovieProducer> newProducers) {
    // two way sync of producers
    // first remove unused
    for (int i = producers.size() - 1; i >= 0; i--) {
      MovieProducer producer = producers.get(i);
      if (!newProducers.contains(producer)) {
        producers.remove(producer);
      }
    }

    // second add the new ones
    for (int i = 0; i < newProducers.size(); i++) {
      MovieProducer producer = newProducers.get(i);
      if (!producers.contains(producer)) {
        // new producer
        try {
          producers.add(i, producer);
        }
        catch (IndexOutOfBoundsException e) {
          producers.add(producer);
        }
      }
      else {
        int indexOldList = producers.indexOf(producer);
        if (i != indexOldList) {
          MovieProducer oldProducer = producers.remove(indexOldList);
          try {
            producers.add(i, oldProducer);
          }
          catch (IndexOutOfBoundsException e) {
            producers.add(oldProducer);
          }
        }
      }
    }

    firePropertyChange(PRODUCERS, null, producers);
  }

  public List<MovieProducer> getProducers() {
    return this.producers;
  }

  /**
   * <b>PHYSICALLY</b> deletes a complete Movie by moving it to datasource backup folder<br>
   * DS\.backup\&lt;moviename&gt;
   */
  public boolean deleteFilesSafely() {
    // backup
    if (isMultiMovieDir()) {
      boolean ok = true;
      for (MediaFile mf : getMediaFiles()) {
        if (!mf.deleteSafely(getDataSource())) {
          ok = false;
        }
      }
      return ok;
    }
    else {
      return Utils.deleteDirectorySafely(new File(getPath()), getDataSource());
    }
  }
}
