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
package org.tinymediamanager.core.movie.entities;

import static org.tinymediamanager.core.Constants.ACTORS;
import static org.tinymediamanager.core.Constants.CERTIFICATION;
import static org.tinymediamanager.core.Constants.COUNTRY;
import static org.tinymediamanager.core.Constants.DATA_SOURCE;
import static org.tinymediamanager.core.Constants.DIRECTOR;
import static org.tinymediamanager.core.Constants.EDITION;
import static org.tinymediamanager.core.Constants.EDITION_AS_STRING;
import static org.tinymediamanager.core.Constants.GENRE;
import static org.tinymediamanager.core.Constants.GENRES_AS_STRING;
import static org.tinymediamanager.core.Constants.HAS_NFO_FILE;
import static org.tinymediamanager.core.Constants.IMDB;
import static org.tinymediamanager.core.Constants.MEDIA_SOURCE;
import static org.tinymediamanager.core.Constants.MOVIESET;
import static org.tinymediamanager.core.Constants.MOVIESET_TITLE;
import static org.tinymediamanager.core.Constants.PRODUCERS;
import static org.tinymediamanager.core.Constants.RELEASE_DATE;
import static org.tinymediamanager.core.Constants.RELEASE_DATE_AS_STRING;
import static org.tinymediamanager.core.Constants.RUNTIME;
import static org.tinymediamanager.core.Constants.SORT_TITLE;
import static org.tinymediamanager.core.Constants.SPOKEN_LANGUAGES;
import static org.tinymediamanager.core.Constants.TAG;
import static org.tinymediamanager.core.Constants.TAGS_AS_STRING;
import static org.tinymediamanager.core.Constants.TITLE_FOR_UI;
import static org.tinymediamanager.core.Constants.TITLE_SORTABLE;
import static org.tinymediamanager.core.Constants.TMDB;
import static org.tinymediamanager.core.Constants.TOP250;
import static org.tinymediamanager.core.Constants.TRAILER;
import static org.tinymediamanager.core.Constants.TRAKT;
import static org.tinymediamanager.core.Constants.VIDEO_IN_3D;
import static org.tinymediamanager.core.Constants.WATCHED;
import static org.tinymediamanager.core.Constants.WRITER;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieArtworkHelper;
import org.tinymediamanager.core.movie.MovieEdition;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieMediaFileComparator;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.MovieRenamer;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieTrailerQuality;
import org.tinymediamanager.core.movie.MovieTrailerSources;
import org.tinymediamanager.core.movie.connector.MovieConnectors;
import org.tinymediamanager.core.movie.connector.MovieToKodiNfoConnector;
import org.tinymediamanager.core.movie.connector.MovieToMpNfoConnector;
import org.tinymediamanager.core.movie.connector.MovieToXbmcNfoConnector;
import org.tinymediamanager.core.movie.tasks.MovieActorImageFetcher;
import org.tinymediamanager.core.movie.tasks.MovieTrailerDownloadTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMovieSetMetadataProvider;
import org.tinymediamanager.scraper.util.StrgUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * The main class for movies.
 * 
 * @author Manuel Laggner / Myron Boyle
 */
public class Movie extends MediaEntity {
  @XmlTransient
  private static final Logger                   LOGGER                     = LoggerFactory.getLogger(Movie.class);
  private static final Comparator<MediaFile>    MEDIA_FILE_COMPARATOR      = new MovieMediaFileComparator();
  private static final Comparator<MovieTrailer> TRAILER_QUALITY_COMPARATOR = new MovieTrailer.QualityComparator();

  @JsonProperty
  private String                                sortTitle                  = "";
  @JsonProperty
  private String                                tagline                    = "";
  @JsonProperty
  private int                                   runtime                    = 0;
  @JsonProperty
  private String                                director                   = "";
  @JsonProperty
  private String                                writer                     = "";
  @JsonProperty
  private String                                dataSource                 = "";
  @JsonProperty
  private boolean                               watched                    = false;
  @JsonProperty
  private boolean                               isDisc                     = false;
  @JsonProperty
  private String                                spokenLanguages            = "";
  @JsonProperty
  private boolean                               subtitles                  = false;
  @JsonProperty
  private String                                country                    = "";
  @JsonProperty
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private Date                                  releaseDate                = null;
  @JsonProperty
  private boolean                               multiMovieDir              = false;                               // we detected more movies in
                                                                                                                  // same folder
  @JsonProperty
  private int                                   top250                     = 0;
  @JsonProperty
  private MediaSource                           mediaSource                = MediaSource.UNKNOWN;                 // DVD, Bluray, etc
  @JsonProperty
  private boolean                               videoIn3D                  = false;
  @JsonProperty
  private Certification                         certification              = Certification.NOT_RATED;
  @JsonProperty
  private UUID                                  movieSetId;
  @JsonProperty
  private MovieEdition                          edition                    = MovieEdition.NONE;
  @JsonProperty
  private boolean                               stacked                    = false;
  @JsonProperty
  private boolean                               offline                    = false;

  @JsonProperty
  private List<String>                          genres                     = new CopyOnWriteArrayList<>();
  @JsonProperty
  private List<String>                          tags                       = new CopyOnWriteArrayList<>();
  @JsonProperty
  private List<String>                          extraThumbs                = new CopyOnWriteArrayList<>();
  @JsonProperty
  private List<String>                          extraFanarts               = new CopyOnWriteArrayList<>();
  @JsonProperty
  private List<MovieActor>                      actors                     = new CopyOnWriteArrayList<>();
  @JsonProperty
  private List<MovieProducer>                   producers                  = new CopyOnWriteArrayList<>();
  @JsonProperty
  private List<MovieTrailer>                    trailer                    = new CopyOnWriteArrayList<>();

  private MovieSet                              movieSet;
  private String                                titleSortable              = "";
  private Date                                  lastWatched                = null;
  private List<MediaGenres>                     genresForAccess            = new CopyOnWriteArrayList<>();

  /**
   * Instantiates a new movie. To initialize the propertychangesupport after loading
   */
  public Movie() {
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

  public void merge(Movie other) {
    if (other == null) {
      return;
    }
    super.merge(other);

    this.sortTitle = StringUtils.isEmpty(this.sortTitle) ? other.getSortTitle() : this.sortTitle;
    this.tagline = StringUtils.isEmpty(this.tagline) ? other.getTagline() : this.tagline;
    this.director = StringUtils.isEmpty(this.director) ? other.getDirector() : this.director;
    this.writer = StringUtils.isEmpty(this.writer) ? other.getWriter() : this.writer;
    this.spokenLanguages = StringUtils.isEmpty(this.spokenLanguages) ? other.getSpokenLanguages() : this.spokenLanguages;
    this.country = StringUtils.isEmpty(this.country) ? other.getCountry() : this.country;
    this.titleSortable = StringUtils.isEmpty(this.titleSortable) ? other.getTitleSortable() : this.titleSortable;

    this.runtime = this.runtime == 0 ? other.getRuntime() : this.runtime;
    this.top250 = this.top250 == 0 ? other.getTop250() : this.top250;
    this.releaseDate = this.releaseDate == null ? other.getReleaseDate() : this.releaseDate;
    this.movieSet = this.movieSet == null ? other.getMovieSet() : this.movieSet;
    this.mediaSource = this.mediaSource == MediaSource.UNKNOWN ? other.getMediaSource() : this.mediaSource;
    this.certification = this.certification == Certification.NOT_RATED ? other.getCertification() : this.certification;
    this.edition = this.edition == MovieEdition.NONE ? other.getEdition() : this.edition;

    for (MediaGenres genre : other.getGenres()) {
      addGenre(genre); // already checks dupes
    }
    for (MovieActor actor : other.getActors()) {
      if (!this.actors.contains(actor)) {
        this.actors.add(actor);
      }
    }
    for (MovieProducer prod : other.getProducers()) {
      if (!this.producers.contains(prod)) {
        this.producers.add(prod);
      }
    }
    for (MovieTrailer trail : other.getTrailer()) {
      if (!this.trailer.contains(trail)) {
        this.trailer.add(trail);
      }
    }

    for (String key : other.getTags()) {
      if (!this.tags.contains(key)) {
        this.tags.add(key);
      }
    }
    for (String key : other.getExtraThumbs()) {
      if (!this.extraThumbs.contains(key)) {
        this.extraThumbs.add(key);
      }
    }
    for (String key : other.getExtraFanarts()) {
      if (!this.extraFanarts.contains(key)) {
        this.extraFanarts.add(key);
      }
    }
  }

  @Override
  protected Comparator<MediaFile> getMediaFileComparator() {
    return MEDIA_FILE_COMPARATOR;
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
    return scraped || getHasMetadata();
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
   * doe we have basic metadata filled?<br>
   * like plot and year to take another fields into account always produces false positives (there are documentaries out there, which do not have
   * actors or either a producer in the meta data DBs..)
   * 
   * @return true/false
   */
  public Boolean getHasMetadata() {
    if (!plot.isEmpty() && !(year.isEmpty() || year.equals("0"))) {
      return true;
    }
    return false;
  }

  /**
   * Gets the check mark for images.<br>
   * Assumes true, but when PosterFilename is set and we do not have a poster, return false<br>
   * same for fanarts.
   * 
   * @return the checks for images
   */
  public Boolean getHasImages() {
    if (!MovieModuleManager.MOVIE_SETTINGS.getMoviePosterFilenames().isEmpty() && StringUtils.isEmpty(getArtworkFilename(MediaFileType.POSTER))) {
      return false;
    }

    if (!MovieModuleManager.MOVIE_SETTINGS.getMovieFanartFilenames().isEmpty() && StringUtils.isEmpty(getArtworkFilename(MediaFileType.FANART))) {
      return false;
    }

    return true;
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
    String titleForUi = title;
    if (StringUtils.isNotBlank(year)) {
      titleForUi += " (" + year + ")";
    }
    return titleForUi;
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
    // and re-set movie path the actors
    if (StringUtils.isBlank(obj.getEntityRoot())) {
      obj.setEntityRoot(getPathNIO());
    }

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

    // first remove unused
    for (int i = tags.size() - 1; i >= 0; i--) {
      String tag = tags.get(i);
      if (!newTags.contains(tag)) {
        tags.remove(tag);
      }
    }

    // second, add new ones in the right order
    for (int i = 0; i < newTags.size(); i++) {
      String tag = newTags.get(i);
      if (!tags.contains(tag)) {
        try {
          tags.add(i, tag);
        }
        catch (IndexOutOfBoundsException e) {
          tags.add(tag);
        }
      }
      else {
        int indexOldList = tags.indexOf(tag);
        if (i != indexOldList) {
          String oldTag = tags.remove(indexOldList);
          try {
            tags.add(i, oldTag);
          }
          catch (IndexOutOfBoundsException e) {
            tags.add(oldTag);
          }
        }
      }
    }

    Utils.removeEmptyStringsFromList(tags);

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

  /** set subtitles */
  public void setSubtitles(boolean sub) {
    this.subtitles = sub;
  }

  /**
   * Searches for actor images, and matches them to our "actors", updating the thumb url
   * 
   * @deprecated thumbPath is generated dynamic - no need for storage
   */
  @Deprecated
  public void findActorImages() {
    if (MovieModuleManager.MOVIE_SETTINGS.isWriteActorImages()) {
      // get all files from the actors path
      try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(getPathNIO())) {
        for (Path path : directoryStream) {
          if (Utils.isRegularFile(path)) {

            for (MovieActor actor : getActors()) {
              if (StringUtils.isBlank(actor.getThumbPath())) {
                // yay, actor with empty image
                String name = actor.getNameForStorage();
                if (name.equals(path.getFileName().toString())) {
                  actor.setThumbPath(path.toAbsolutePath().toString());
                }
              }
            }

          }
        }
      }
      catch (IOException ex) {
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
    return this.getIdAsString(IMDB);
  }

  /**
   * Gets the tmdb id.
   * 
   * @return the tmdb id
   */
  public int getTmdbId() {
    return this.getIdAsInt(TMDB);
  }

  /**
   * Sets the tmdb id.
   * 
   * @param newValue
   *          the new tmdb id
   */
  public void setTmdbId(int newValue) {
    this.setId(TMDB, newValue);
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
   * Sets the TraktTV id.
   * 
   * @param newValue
   *          the new TraktTV id
   */
  public void setTraktId(int newValue) {
    this.setId(TRAKT, newValue);
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

    for (MediaFile file : new ArrayList<>(getMediaFiles())) {
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
  @JsonSetter
  public void setExtraThumbs(List<String> extraThumbs) {
    this.extraThumbs.clear();
    this.extraThumbs.addAll(extraThumbs);
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
  @JsonSetter
  public void setExtraFanarts(List<String> extraFanarts) {
    this.extraFanarts.clear();
    this.extraFanarts.addAll(extraFanarts);
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
   * Sets the metadata.
   * 
   * @param metadata
   *          the new metadata
   * @param config
   *          the config
   */
  public void setMetadata(MediaMetadata metadata, MovieScraperMetadataConfig config) {
    if (metadata == null) {
      LOGGER.error("metadata was null");
      return;
    }

    // check if metadata has at least a name
    if (StringUtils.isEmpty(metadata.getTitle())) {
      LOGGER.warn("wanted to save empty metadata for " + getTitle());
      return;
    }

    setIds(metadata.getIds());

    // set chosen metadata
    if (config.isTitle()) {
      setTitle(metadata.getTitle());
    }

    if (config.isOriginalTitle()) {
      setOriginalTitle(metadata.getOriginalTitle());
    }

    if (config.isTagline()) {
      setTagline(metadata.getTagline());
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
      setReleaseDate(metadata.getReleaseDate());
    }

    if (config.isRating()) {
      setRating(metadata.getRating());
      setVotes(metadata.getVoteCount());
      setTop250(metadata.getTop250());
    }

    if (config.isRuntime()) {
      setRuntime(metadata.getRuntime());
    }

    setSpokenLanguages(StringUtils.join(metadata.getSpokenLanguages(), ", "));
    setCountry(StringUtils.join(metadata.getCountries(), ", "));

    // certifications
    if (config.isCertification()) {
      if (metadata.getCertifications() != null && metadata.getCertifications().size() > 0) {
        setCertification(metadata.getCertifications().get(0));
      }
    }

    // cast
    if (config.isCast()) {
      setProductionCompany(StringUtils.join(metadata.getProductionCompanies(), ", "));
      List<MovieActor> actors = new ArrayList<>();
      List<MovieProducer> producers = new ArrayList<>();
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

    // tags
    if (config.isTags()) {
      for (String tag : metadata.getTags()) {
        addToTags(tag);
      }
    }

    // set scraped
    setScraped(true);

    // create MovieSet
    if (config.isCollection()) {
      int col = 0;
      try {
        col = (int) metadata.getId(MediaMetadata.TMDB_SET);
      }
      catch (Exception ignored) {
      }
      if (col != 0) {
        MovieSet movieSet = MovieList.getInstance().getMovieSet(metadata.getCollectionName(), col);
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
              options.setLanguage(LocaleUtils.toLocale(MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage().name()));
              options.setCountry(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry());

              MediaMetadata info = mp.getMetadata(options);
              if (info != null && StringUtils.isNotBlank(info.getTitle())) {
                movieSet.setTitle(info.getTitle());
                movieSet.setPlot(info.getPlot());

                if (!info.getMediaArt(MediaArtworkType.POSTER).isEmpty()) {
                  movieSet.setArtworkUrl(info.getMediaArt(MediaArtworkType.POSTER).get(0).getDefaultUrl(), MediaFileType.POSTER);
                }
                if (!info.getMediaArt(MediaArtworkType.BACKGROUND).isEmpty()) {
                  movieSet.setArtworkUrl(info.getMediaArt(MediaArtworkType.BACKGROUND).get(0).getDefaultUrl(), MediaFileType.FANART);
                }
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
          movieSet.saveToDb();
        }
      }
    }

    // update DB
    writeNFO();
    saveToDb();

    // rename the movie if that has been chosen in the settings
    if (MovieModuleManager.MOVIE_SETTINGS.isMovieRenameAfterScrape()) {
      MovieRenamer.renameMovie(this);
    }
  }

  /**
   * Sets the trailers; first one is "inNFO" if not a local one.
   * 
   * @param trailers
   *          the new trailers
   */
  @JsonSetter
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

      // if not yet one has been found; sort by quality descending and take the first one which is lower or equal to the desired quality
      if (preferredTrailer == null) {
        List<MovieTrailer> sortedTrailers = new ArrayList<>(trailers);
        Collections.sort(sortedTrailers, TRAILER_QUALITY_COMPARATOR);
        for (MovieTrailer trailer : sortedTrailers) {
          if (desiredQuality.ordinal() >= MovieTrailerQuality.getMovieTrailerQuality(trailer.getQuality()).ordinal()) {
            trailer.setInNfo(Boolean.TRUE);
            preferredTrailer = trailer;
            break;
          }
        }
      }
    } // end if MovieModuleManager.MOVIE_SETTINGS.isUseTrailerPreference()

    // if not yet one has been found; sort by quality descending and take the first one
    if (preferredTrailer == null && !trailers.isEmpty()) {
      List<MovieTrailer> sortedTrailers = new ArrayList<>(trailers);
      Collections.sort(sortedTrailers, TRAILER_QUALITY_COMPARATOR);
      preferredTrailer = sortedTrailers.get(0);
      preferredTrailer.setInNfo(Boolean.TRUE);
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

    if (MovieModuleManager.MOVIE_SETTINGS.isUseTrailerPreference() && MovieModuleManager.MOVIE_SETTINGS.isAutomaticTrailerDownload()
        && getMediaFiles(MediaFileType.TRAILER).isEmpty() && !trailer.isEmpty()) {
      MovieTrailer trailer = this.trailer.get(0);
      MovieTrailerDownloadTask task = new MovieTrailerDownloadTask(trailer, this);
      TmmTaskManager.getInstance().addDownloadTask(task);
    }

    // persist
    writeNFO();
    saveToDb();
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

    md.setTitle(title);
    md.setOriginalTitle(originalTitle);
    md.setTagline(tagline);
    md.setPlot(plot);
    try {
      md.setYear(Integer.parseInt(year));
    }
    catch (Exception ignored) {
    }
    md.setRating(rating);
    md.setVoteCount(votes);
    md.setRuntime(runtime);
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
  @JsonSetter
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

    // and re-set movie path to the actors
    for (MovieActor actor : actors) {
      if (StringUtils.isBlank(actor.getEntityRoot())) {
        actor.setEntityRoot(getPathNIO());
      }
    }

    // third - rename thumbs if needed
    // NAH - thumb is always dynamic now - so if name doesnt change, nothing to rename
    // actor writing/caching is done somewhere else...

    // if (MovieModuleManager.MOVIE_SETTINGS.isWriteActorImages()) {
    // Path actorDir = getPathNIO().resolve(MovieActor.ACTOR_DIR);
    //
    // for (MovieActor actor : actors) {
    // if (StringUtils.isNotBlank(actor.getThumbPath())) {
    // try {
    // // build expected filename
    // Path actorName = actorDir.resolve(actor.getNameForStorage() + "." + FilenameUtils.getExtension(actor.getThumbPath()));
    // Path oldFile = Paths.get(actor.getThumbPath());
    // Utils.moveFileSafe(oldFile, actorName);
    // }
    // catch (IOException e) {
    // LOGGER.warn("couldn't rename actor thumb (" + actor.getThumbPath() + "): " + e.getMessage());
    // }
    // }
    // }
    // }

    firePropertyChange(ACTORS, null, this.getActors());
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
      String name = mfs.get(0).getFilename();
      if (isStacked()) {
        // when movie IS stacked, remove stacking marker, else keep it!
        name = Utils.cleanStackingMarkers(name);
      }
      return getNfoFilename(nfo, name);
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
   *          the new/desired movie filename (stacking marker should already be set correct here!)
   * @return the nfo filename
   */
  public String getNfoFilename(MovieNfoNaming nfo, String newMovieFilename) {
    String filename = "";
    switch (nfo) {
      case FILENAME_NFO:
        if (isDisc()) {
          // if filename is activated, we generate them accordingly MF(1)
          // but if disc, fixate this
          if (Files.exists(getPathNIO().resolve("VIDEO_TS.ifo")) || Files.exists(getPathNIO().resolve("VIDEO_TS"))) {
            filename = "VIDEO_TS.nfo";
          }
          else if (Files.exists(getPathNIO().resolve("index.bdmv"))) {
            filename = "index.nfo";
          }
          else if (Files.exists(getPathNIO().resolve("BDMV"))) {
            filename = "BDMV.nfo";
          }
        }
        else {
          String movieFilename = FilenameUtils.getBaseName(newMovieFilename);
          filename += movieFilename + ".nfo";
        }
        break;
      case MOVIE_NFO:
        filename += "movie.nfo";
        break;
      case DISC_NFO:
        if (isDisc()) {
          Path dir = getPathNIO().resolve("VIDEO_TS");
          if (Files.isDirectory(dir)) {
            filename = dir.resolve("VIDEO_TS.nfo").toString();
          }
          dir = getPathNIO().resolve("BDMV");
          if (Files.isDirectory(dir)) {
            filename = dir.resolve("index.nfo").toString();
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
      return FilenameUtils.getBaseName(Utils.cleanStackingMarkers(mfs.get(0).getFilename()));
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
    if (MovieModuleManager.MOVIE_SETTINGS.getMovieNfoFilenames().isEmpty()) {
      LOGGER.info("Not writing any NFO file, because NFO filename preferences were empty...");
      return;
    }
    if (MovieModuleManager.MOVIE_SETTINGS.getMovieConnector() == MovieConnectors.MP) {
      MovieToMpNfoConnector.setData(this);
    }
    else if (MovieModuleManager.MOVIE_SETTINGS.getMovieConnector() == MovieConnectors.XBMC) {
      MovieToXbmcNfoConnector.setData(this);
    }
    else {
      MovieToKodiNfoConnector.setData(this);
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
   * @param newGenres
   *          the new genres
   */
  @JsonSetter
  public void setGenres(List<MediaGenres> newGenres) {
    // two way sync of genres

    // first remove old ones
    for (int i = genresForAccess.size() - 1; i >= 0; i--) {
      MediaGenres genre = genresForAccess.get(i);
      if (!newGenres.contains(genre)) {
        genresForAccess.remove(genre);
      }
    }

    // second, add new ones in the right order
    for (int i = 0; i < newGenres.size(); i++) {
      MediaGenres genre = newGenres.get(i);
      if (!genresForAccess.contains(genre)) {
        try {
          genresForAccess.add(i, genre);
        }
        catch (IndexOutOfBoundsException e) {
          genresForAccess.add(genre);
        }
      }
      else {
        int indexOldList = genresForAccess.indexOf(genre);
        if (i != indexOldList) {
          MediaGenres oldGenre = genresForAccess.remove(indexOldList);
          try {
            genresForAccess.add(i, oldGenre);
          }
          catch (IndexOutOfBoundsException e) {
            genresForAccess.add(oldGenre);
          }
        }
      }
    }

    // third, build new genre as string list
    genres.clear();
    for (MediaGenres genre : genresForAccess) {
      genres.add(genre.name());
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
      movieSet.removeMovie(this, true);
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

  public MediaSource getMediaSource() {
    return mediaSource;
  }

  public void setMediaSource(MediaSource newValue) {
    MediaSource oldValue = this.mediaSource;
    this.mediaSource = newValue;
    firePropertyChange(MEDIA_SOURCE, oldValue, newValue);
  }

  /**
   * Gets the images to cache.
   */
  public List<Path> getImagesToCache() {
    // image files
    List<Path> filesToCache = new ArrayList<>();
    for (MediaFile mf : getMediaFiles()) {
      if (mf.isGraphic()) {
        filesToCache.add(mf.getFileAsPath());
      }
    }

    // actor image files
    if (MovieModuleManager.MOVIE_SETTINGS.isWriteActorImages()) {
      for (MovieActor actor : actors) {
        Path imagePath = actor.getStoragePath();
        if (imagePath != null) {
          filesToCache.add(imagePath);
        }
      }
    }

    return filesToCache;
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
  public void setReleaseDate(String dateAsString) {
    try {
      setReleaseDate(StrgUtils.parseDate(dateAsString));
    }
    catch (ParseException e) {
    }
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

  /**
   * get all video files for that movie
   *
   * @return a list of all video files
   */
  public List<MediaFile> getVideoFiles() {
    return getMediaFiles(MediaFileType.VIDEO);
  }

  /**
   * get the first video file for this entity
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
   * gets the basename (without stacking)
   * 
   * @return the video base name (without stacking)
   */
  public String getVideoBasenameWithoutStacking() {
    MediaFile mf = getMediaFiles(MediaFileType.VIDEO).get(0);
    return FilenameUtils.getBaseName(mf.getFilenameWithoutStacking());
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
    // and re-set movie path of the producer
    if (StringUtils.isBlank(obj.getEntityRoot())) {
      obj.setEntityRoot(getPathNIO());
    }

    producers.add(obj);

    firePropertyChange(PRODUCERS, null, producers);
  }

  public void removeProducer(MovieProducer obj) {
    producers.remove(obj);
    firePropertyChange(PRODUCERS, null, producers);
  }

  @JsonSetter
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

    // and re-set movie path to the producers
    for (MovieProducer producer : producers) {
      if (StringUtils.isBlank(producer.getEntityRoot())) {
        producer.setEntityRoot(getPathNIO());
      }
    }

    firePropertyChange(PRODUCERS, null, producers);
  }

  public List<MovieProducer> getProducers() {
    return this.producers;
  }

  /**
   * Is the movie "stacked" (more than one video file)
   * 
   * @return true if the movie is stacked; false otherwise
   */
  public boolean isStacked() {
    return stacked;
  }

  public void setStacked(boolean stacked) {
    this.stacked = stacked;
  }

  /**
   * ok, we might have detected some stacking MFs.<br>
   * But if we only have ONE video file, reset stacking markers in this case<br>
   * eg: "Harry Potter 7 - Part 1" is not stacked<br>
   * CornerCase: what if HP7 has more files...?
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
      return Utils.deleteDirectorySafely(getPathNIO(), getDataSource());
    }
  }

  public MovieEdition getEdition() {
    return edition;
  }

  public String getEditionAsString() {
    return edition.toString();
  }

  public void setOffline(boolean newValue) {
    boolean oldValue = this.offline;
    this.offline = newValue;
    firePropertyChange("offline", oldValue, newValue);
  }

  public boolean isOffline() {
    return offline;
  }

  public void setEdition(MovieEdition newValue) {
    MovieEdition oldValue = this.edition;
    this.edition = newValue;
    firePropertyChange(EDITION, oldValue, newValue);
    firePropertyChange(EDITION_AS_STRING, oldValue, newValue);
  }
}
