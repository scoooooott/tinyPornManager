/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.core.movie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.movie.MovieCast.CastType;
import org.tinymediamanager.scraper.CastMember;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaArt;
import org.tinymediamanager.scraper.MediaArtifactType;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.util.CachedUrl;

import com.moviejukebox.themoviedb.model.ArtworkType;

/**
 * The main class for movies.
 */
@Entity
public class Movie extends AbstractModelObject {

  /** The Constant TITLE. */
  protected final static String TITLE                = "title";

  /** The Constant ORIGINAL_TITLE. */
  protected final static String ORIGINAL_TITLE       = "originaltitle";

  /** The Constant RATING. */
  protected final static String RATING               = "rating";

  /** The Constant VOTES. */
  protected final static String VOTES                = "votes";

  /** The Constant YEAR. */
  protected final static String YEAR                 = "year";

  /** The Constant OUTLINE. */
  protected final static String OUTLINE              = "outline";

  /** The Constant PLOT. */
  protected final static String PLOT                 = "plot";

  /** The Constant TAGLINE. */
  protected final static String TAGLINE              = "tagline";

  /** The Constant RUNTIME. */
  protected final static String RUNTIME              = "runtime";

  /** The Constant THUMB. */
  protected final static String THUMB                = "thumb";

  /** The Constant THUMB_PATH. */
  protected final static String THUMB_PATH           = "thumbpath";

  /** The Constant ID. */
  protected final static String ID                   = "id";

  /** The Constant IMDB_ID. */
  protected final static String IMDB_ID              = "imdbid";

  /** The Constant FILENAME_AND_PATH. */
  protected final static String FILENAME_AND_PATH    = "filenameandpath";

  /** The Constant PATH. */
  protected final static String PATH                 = "path";

  /** The Constant DIRECTOR. */
  protected final static String DIRECTOR             = "director";

  /** The Constant WRITER. */
  protected final static String WRITER               = "writer";

  /** The Constant ACTOR. */
  protected final static String ACTOR                = "actor";

  /** The Constant Production Company. */
  protected final static String PRODUCTION_COMPANY   = "productionCompany";

  /** The Constant NAME. */
  protected final static String NAME                 = "name";

  /** The Constant ROLE. */
  protected final static String ROLE                 = "role";

  /** The Constant GENRE. */
  protected final static String GENRE                = "genre";

  /** The Constant CERTIFICATION. */
  protected final static String CERTIFICATION        = "certification";

  /** The Constant DATA_SOURCE. */
  protected final static String DATA_SOURCE          = "dataSource";

  /** The Constant MOVIE_FILES. */
  protected final static String MOVIE_FILES          = "movieFiles";

  /** The Constant MEDIA_FILES. */
  protected final static String MEDIA_FILES          = "mediaFiles";

  /** The Constant DATE_ADDED. */
  protected final static String DATE_ADDED           = "dateAdded";

  /** The Constant DATE_ADDED_AS_STRING. */
  protected final static String DATE_ADDED_AS_STRING = "dateAddedAsString";

  /** The Constant WATCHED. */
  protected final static String WATCHED              = "watched";

  /** The Constant logger. */
  @XmlTransient
  private static final Logger   LOGGER               = Logger.getLogger(Movie.class);

  /** The id. */
  @Id
  @GeneratedValue
  private Long                  id;

  /** The name. */
  private String                name                 = "";

  /** The original name. */
  private String                originalName         = "";

  /** The year. */
  private String                year                 = "";

  /** The imdb id. */
  private String                imdbId               = "";

  /** The tmdb id. */
  private int                   tmdbId               = 0;

  /** The overview. */
  private String                overview             = "";

  /** The tagline. */
  private String                tagline              = "";

  /** The rating. */
  private float                 rating               = 0f;

  /** The votes. */
  private int                   votes                = 0;

  /** The runtime. */
  private int                   runtime              = 0;

  /** The fanart url. */
  private String                fanartUrl            = "";

  /** The fanart. */
  private String                fanart               = "";

  /** The poster url. */
  private String                posterUrl            = "";

  /** The poster. */
  private String                poster               = "";

  /** The path. */
  private String                path                 = "";

  /** The nfo filename. */
  private String                nfoFilename          = "";

  /** The director. */
  private String                director             = "";

  /** The writer. */
  private String                writer               = "";

  /** The production company. */
  private String                productionCompany    = "";

  /** The certification. */
  private Certification         certification        = Certification.NOT_RATED;

  /** The scraped. */
  private boolean               scraped              = false;

  /** The data source. */
  private String                dataSource           = "";

  /** The date added. */
  private Date                  dateAdded            = new Date();

  /** The watched. */
  private boolean               watched              = false;

  /** The movie files. */
  private List<String>          movieFiles           = new ArrayList<String>();

  /** The genres. */
  private List<MediaGenres>     genres               = new ArrayList<MediaGenres>();

  /** The cast. */
  @OneToMany(cascade = CascadeType.ALL)
  private List<MovieCast>       cast                 = new ArrayList<MovieCast>();

  /** The cast observable. */
  @Transient
  private List<MovieCast>       castObservable       = ObservableCollections.observableList(cast);

  /** The media files. */
  @OneToMany(cascade = CascadeType.ALL)
  private List<MediaFile>       mediaFiles           = new ArrayList<MediaFile>();

  /** The media files observable. */
  @Transient
  private List<MediaFile>       mediaFilesObservable = ObservableCollections.observableList(mediaFiles);

  /**
   * Instantiates a new movie.
   */
  public Movie() {
  }

  /**
   * checks if this movie has been scraped.
   * 
   * @return isScraped
   */
  public boolean isScraped() {
    return scraped;
  }

  /**
   * Gets the nfo filename.
   * 
   * @return the nfo filename
   */
  public String getNfoFilename() {
    return nfoFilename;
  }

  /**
   * Gets the checks for nfo file.
   * 
   * @return the checks for nfo file
   */
  public Boolean getHasNfoFile() {
    if (!StringUtils.isEmpty(nfoFilename)) {
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
    if (!StringUtils.isEmpty(poster)) {
      return true;
    }
    return false;
  }

  /**
   * Sets the nfo filename.
   * 
   * @param newValue
   *          the new nfo filename
   */
  public void setNfoFilename(String newValue) {
    String oldValue = this.nfoFilename;
    this.nfoFilename = newValue;
    firePropertyChange("nfoFilename", oldValue, newValue);
    firePropertyChange("hasNfoFile", false, true);
  }

  /**
   * Gets the name for ui.
   * 
   * @return the name for ui
   */
  public String getNameForUi() {
    String nameForUi = new String(name);
    if (year != null && !year.isEmpty()) {
      nameForUi += " (" + year + ")";
    }
    return nameForUi;
  }

  /**
   * Sets the observable cast list.
   */
  public void setObservables() {
    castObservable = ObservableCollections.observableList(cast);
    mediaFilesObservable = ObservableCollections.observableList(mediaFiles);
  }

  /**
   * Adds the to cast.
   * 
   * @param obj
   *          the obj
   */
  public void addToCast(MovieCast obj) {
    castObservable.add(obj);
    firePropertyChange("cast", null, this.getCast());

    switch (obj.getType()) {
      case ACTOR:
        firePropertyChange("actors", null, this.getCast());
        break;
    }

  }

  /**
   * Adds the to media files.
   * 
   * @param obj
   *          the obj
   */
  public void addToMediaFiles(MediaFile obj) {
    mediaFilesObservable.add(obj);
    firePropertyChange(MEDIA_FILES, null, this.getMediaFiles());
  }

  /**
   * Gets the media files.
   * 
   * @return the media files
   */
  public List<MediaFile> getMediaFiles() {
    return this.mediaFilesObservable;
  }

  /**
   * Removes the from media files.
   * 
   * @param obj
   *          the obj
   */
  public void removeFromMediaFiles(MediaFile obj) {
    mediaFilesObservable.remove(obj);
    firePropertyChange(MEDIA_FILES, null, this.getMediaFiles());
  }

  /**
   * Adds the to files.
   * 
   * @param newFile
   *          the new file
   */
  public void addToFiles(String newFile) {
    movieFiles.add(newFile);
    addToMediaFiles(new MediaFile(getPath(), newFile));
  }

  /**
   * Adds the to files.
   * 
   * @param videoFiles
   *          the video files
   */
  public void addToFiles(File[] videoFiles) {
    for (File file : videoFiles) {
      // check if that file exists for that movie
      if (!hasFile(file.getName())) {
        // create new movie file
        addToFiles(file.getName());
      }
    }
  }

  /**
   * Sets the movie files.
   * 
   * @param newValue
   *          the new movie files
   */
  public void setMovieFiles(List<String> newValue) {
    this.movieFiles = newValue;
    firePropertyChange(MOVIE_FILES, null, newValue);
  }

  /**
   * Gets the movie files.
   * 
   * @return the movie files
   */
  public List<String> getMovieFiles() {
    return this.movieFiles;
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
   * Find images.
   */
  public void findImages() {
    // try to find images in movie path

    // find poster
    findPoster();

    // fanart - fanart.jpg
    findFanart();

  }

  /**
   * Find poster.
   */
  private void findPoster() {
    String movieFileName = null;

    if (getMediaFiles().size() > 0) {
      MediaFile mediaFile = getMediaFiles().get(0);
      movieFileName = mediaFile.getFilename();
    }

    // <movie filename>.jpg
    if (!StringUtils.isEmpty(movieFileName)) {
      String poster = path + File.separator + FilenameUtils.getBaseName(movieFileName) + ".jpg";
      File imageFile = new File(poster);
      if (imageFile.exists()) {
        setPoster(FilenameUtils.getName(poster));
        LOGGER.debug("found poster " + imageFile.getPath());
        return;
      }
    }

    // <movie filename>.tbn
    if (!StringUtils.isEmpty(movieFileName)) {
      String poster = path + File.separator + FilenameUtils.getBaseName(movieFileName) + ".tbn";
      File imageFile = new File(poster);
      if (imageFile.exists()) {
        setPoster(FilenameUtils.getName(poster));
        LOGGER.debug("found poster " + imageFile.getPath());
        return;
      }
    }

    // movie.jpg
    {
      String poster = path + File.separator + "movie.jpg";
      File imageFile = new File(poster);
      if (imageFile.exists()) {
        setPoster(FilenameUtils.getName(poster));
        LOGGER.debug("found poster " + imageFile.getPath());
        return;
      }
    }

    // movie.tbn
    {
      String poster = path + File.separator + "movie.tbn";
      File imageFile = new File(poster);
      if (imageFile.exists()) {
        setPoster(FilenameUtils.getName(poster));
        LOGGER.debug("found poster " + imageFile.getPath());
        return;
      }
    }

    // poster.jpg
    {
      String poster = path + File.separator + "poster.jpg";
      File imageFile = new File(poster);
      if (imageFile.exists()) {
        setPoster(FilenameUtils.getName(poster));
        LOGGER.debug("found poster " + imageFile.getPath());
        return;
      }
    }

    // poster.tbn
    {
      String poster = path + File.separator + "poster.tbn";
      File imageFile = new File(poster);
      if (imageFile.exists()) {
        setPoster(FilenameUtils.getName(poster));
        LOGGER.debug("found poster " + imageFile.getPath());
        return;
      }
    }

    // movie.jpg
    {
      String poster = path + File.separator + "movie.jpg";
      File imageFile = new File(poster);
      if (imageFile.exists()) {
        setPoster(FilenameUtils.getName(poster));
        LOGGER.debug("found poster " + imageFile.getPath());
        return;
      }
    }

    // folder.jpg
    {
      String poster = path + File.separator + "folder.jpg";
      File imageFile = new File(poster);
      if (imageFile.exists()) {
        setPoster(FilenameUtils.getName(poster));
        LOGGER.debug("found poster " + imageFile.getPath());
        return;
      }
    }
  }

  /**
   * Find fanart.
   */
  private void findFanart() {
    String movieFileName = null;

    if (getMediaFiles().size() > 0) {
      MediaFile mediaFile = getMediaFiles().get(0);
      movieFileName = mediaFile.getFilename();
    }

    // <movie filename>-fanart.jpg
    if (!StringUtils.isEmpty(movieFileName)) {
      String fanart = path + File.separator + FilenameUtils.getBaseName(movieFileName) + "-fanart.jpg";
      File imageFile = new File(fanart);
      if (imageFile.exists()) {
        setFanart(FilenameUtils.getName(fanart));
        LOGGER.debug("found fanart " + imageFile.getPath());
        return;
      }
    }

    // fanart.jpg
    {
      String fanart = path + File.separator + "fanart.jpg";
      File imageFile = new File(fanart);
      if (imageFile.exists()) {
        setFanart(FilenameUtils.getName(fanart));
        LOGGER.debug("found fanart " + imageFile.getPath());
        return;
      }
    }
  }

  /**
   * Gets the actors.
   * 
   * @return the actors
   */
  public List<MovieCast> getActors() {
    List<MovieCast> actors = getCast();

    for (int i = 0; i < actors.size(); i++) {
      MovieCast cast = actors.get(i);
      if (cast.getType() != CastType.ACTOR) {
        actors.remove(cast);
      }
    }
    return actors;
  }

  /**
   * Gets the cast.
   * 
   * @return the cast
   */
  public List<MovieCast> getCast() {
    return this.castObservable;
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
   * Gets the fanart.
   * 
   * @return the fanart
   */
  public String getFanart() {
    if (!StringUtils.isEmpty(fanart)) {
      return path + File.separator + fanart;
    } else {
      return fanart;
    }
  }

  /**
   * Gets the fanart url.
   * 
   * @return the fanart url
   */
  public String getFanartUrl() {
    return fanartUrl;
  }

  /**
   * Gets the id.
   * 
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * Gets the imdb id.
   * 
   * @return the imdb id
   */
  public String getImdbId() {
    return imdbId;
  }

  /**
   * Gets the tmdb id.
   * 
   * @return the tmdb id
   */
  public int getTmdbId() {
    return tmdbId;
  }

  /**
   * Sets the tmdb id.
   * 
   * @param newValue
   *          the new tmdb id
   */
  public void setTmdbId(int newValue) {
    int oldValue = this.tmdbId;
    this.tmdbId = newValue;
    firePropertyChange("tmdbId", oldValue, newValue);
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the original name.
   * 
   * @return the original name
   */
  public String getOriginalName() {
    return originalName;
  }

  /**
   * Gets the overview.
   * 
   * @return the overview
   */
  public String getOverview() {
    return overview;
  }

  /**
   * Gets the path.
   * 
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * Gets the poster.
   * 
   * @return the poster
   */
  public String getPoster() {
    if (!StringUtils.isEmpty(poster)) {
      return path + File.separator + poster;
    } else {
      return poster;
    }
  }

  /**
   * Gets the poster url.
   * 
   * @return the poster url
   */
  public String getPosterUrl() {
    return posterUrl;
  }

  /**
   * Gets the rating.
   * 
   * @return the rating
   */
  public float getRating() {
    return rating;
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
   * Gets the runtime.
   * 
   * @return the runtime
   */
  public int getRuntime() {
    return runtime;
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
   * Gets the year.
   * 
   * @return the year
   */
  public String getYear() {
    return year;
  }

  /**
   * Checks for file.
   * 
   * @param filename
   *          the filename
   * @return true, if successful
   */
  public boolean hasFile(String filename) {
    for (String fileName : movieFiles) {
      if (fileName.compareTo(filename) == 0) {
        return true;
      }
    }
    return false;
  }

  // when loading from Database
  /**
   * On load.
   */
  void onLoad() {
  }

  /**
   * Parses the nfo.
   * 
   * @param path
   *          the path
   * @param videoFiles
   *          the video files
   * @return the movie
   */
  public static Movie parseNFO(String path, File[] videoFiles) {
    LOGGER.debug("try to find a nfo for " + path);
    // check if there are any NFOs in that directory
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        // do not start with .
        if (name.toLowerCase().startsWith("."))
          return false;

        // check if filetype is in our settigns
        if (name.toLowerCase().endsWith("nfo") || name.toLowerCase().endsWith("NFO")) {
          return true;
        }

        return false;
      }
    };

    Movie movie = null;

    File directory = new File(path);
    File[] nfoFiles = directory.listFiles(filter);
    for (File file : nfoFiles) {
      LOGGER.debug("parsing nfo" + file.getPath());
      switch (Globals.settings.getMovieConnector()) {
        case XBMC:
          movie = MovieToXbmcNfoConnector.getData(file.getPath());
          break;

        case MP:
          movie = MovieToMpNfoConnector.getData(file.getPath());
          break;
      }

      if (movie == null) {
        LOGGER.debug("did not find movie informations in nfo");
        continue;
      }

      movie.setPath(path);
      movie.addToFiles(videoFiles);
      movie.findImages();
      break;
    }

    return movie;
  }

  /**
   * Removes the from cast.
   * 
   * @param obj
   *          the obj
   */
  public void removeFromCast(MovieCast obj) {
    castObservable.remove(obj);
    firePropertyChange("cast", null, this.getCast());

    switch (obj.getType()) {
      case ACTOR:
        firePropertyChange("actors", null, this.getCast());
        break;

    // case DIRECTOR:
    // firePropertyChange("director", null, this.getCast());
    // break;
    //
    // case WRITER:
    // firePropertyChange("writer", null, this.getCast());
    // break;
    }

  }

  /**
   * Sets the fanart.
   * 
   * @param newValue
   *          the new fanart
   */
  public void setFanart(String newValue) {
    String oldValue = this.fanart;
    this.fanart = newValue;
    firePropertyChange("fanart", oldValue, newValue);
    firePropertyChange("hasImages", false, true);
  }

  /**
   * Sets the fanart url.
   * 
   * @param newValue
   *          the new fanart url
   */
  public void setFanartUrl(String newValue) {
    String oldValue = fanartUrl;
    fanartUrl = newValue;
    firePropertyChange("fanartUrl", oldValue, newValue);
  }

  /**
   * Sets the id.
   * 
   * @param id
   *          the new id
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Sets the imdb id.
   * 
   * @param newValue
   *          the new imdb id
   */
  public void setImdbId(String newValue) {
    String oldValue = imdbId;
    imdbId = newValue;
    firePropertyChange("imdbId", oldValue, newValue);
  }

  /**
   * Sets the metadata.
   * 
   * @param metadata
   *          the new metadata
   */
  /**
   * @param metadata
   */
  public void setMetadata(MediaMetadata metadata) {
    setName(metadata.getMediaTitle());
    setOriginalName(metadata.getOriginalTitle());
    setOverview(metadata.getPlot());
    setImdbId(metadata.getIMDBID());
    if (!StringUtils.isEmpty(metadata.getTMDBID())) {
      try {
        setTmdbId(Integer.parseInt(metadata.getTMDBID()));
      } catch (Exception e) {
        setTmdbId(0);
      }
    }
    setYear(metadata.getYear());
    setRating(metadata.getUserRating());
    setVotes(metadata.getVoteCount());
    setRuntime(metadata.getRuntime());

    setTagline(metadata.getTagline());
    setProductionCompany(metadata.getCompany());

    // certifications
    for (Certification certification : metadata.getCertifications()) {
      setCertification(certification);
      break;
    }

    // poster
    List<MediaArt> art = metadata.getMediaArt(MediaArtifactType.POSTER);
    if (art.size() > 0) {
      MediaArt poster = art.get(0);
      setPosterUrl(poster.getDownloadUrl());
    }

    // fanart
    art = metadata.getMediaArt(MediaArtifactType.BACKGROUND);
    if (art.size() > 0) {
      MediaArt fanart = art.get(0);
      setFanartUrl(fanart.getDownloadUrl());
    }

    // cast
    removeAllActors();
    List<CastMember> cast = metadata.getCastMembers();
    String director = new String();
    String writer = new String();
    for (CastMember member : cast) {
      MovieCast castMember = new MovieCast();
      castMember.setName(member.getName());
      castMember.setCharacter(member.getCharacter());
      castMember.setThumb(member.getImageUrl());
      switch (member.getType()) {
        case CastMember.ACTOR:
          castMember.setType(CastType.ACTOR);
          addToCast(castMember);
          break;
        case CastMember.DIRECTOR:
          if (!StringUtils.isEmpty(director)) {
            director += ", ";
          }
          director += member.getName();
          break;
        case CastMember.WRITER:
          if (!StringUtils.isEmpty(writer)) {
            writer += ", ";
          }
          writer += member.getName();
          break;
      }
    }
    setDirector(director);
    setWriter(writer);

    // genres
    removeAllGenres();
    for (MediaGenres genre : metadata.getGenres()) {
      addGenre(genre);
    }

    // set scraped
    setScraped(true);

    // write NFO and saving images
    writeNFO();
    writeImages(true, true);

    // update DB
    saveToDb();

  }

  /**
   * Removes the all actors.
   */
  public void removeAllActors() {
    castObservable.clear();
    firePropertyChange("cast", null, this.getCast());
    firePropertyChange("actors", null, this.getCast());
  }

  /**
   * Sets the name.
   * 
   * @param newValue
   *          the new name
   */
  public void setName(String newValue) {
    String oldValue = name;
    name = newValue;
    firePropertyChange("name", oldValue, newValue);
    firePropertyChange("nameForUi", oldValue, newValue);
  }

  /**
   * Sets the original name.
   * 
   * @param newValue
   *          the new original name
   */
  public void setOriginalName(String newValue) {
    String oldValue = originalName;
    originalName = newValue;
    firePropertyChange("originalName", oldValue, newValue);
  }

  /**
   * Sets the overview.
   * 
   * @param newValue
   *          the new overview
   */
  public void setOverview(String newValue) {
    String oldValue = overview;
    overview = newValue;
    firePropertyChange("overview", oldValue, newValue);
  }

  /**
   * Sets the path.
   * 
   * @param newValue
   *          the new path
   */
  public void setPath(String newValue) {
    String oldValue = path;
    path = newValue;
    firePropertyChange("path", oldValue, newValue);
  }

  /**
   * Sets the poster.
   * 
   * @param newValue
   *          the new poster
   */
  public void setPoster(String newValue) {
    String oldValue = this.poster;
    this.poster = newValue;
    firePropertyChange("poster", oldValue, newValue);
    firePropertyChange("hasImages", false, true);
  }

  /**
   * Sets the poster url.
   * 
   * @param newValue
   *          the new poster url
   */
  public void setPosterUrl(String newValue) {
    String oldValue = posterUrl;
    posterUrl = newValue;
    firePropertyChange("posterUrl", oldValue, newValue);
  }

  /**
   * Sets the rating.
   * 
   * @param newValue
   *          the new rating
   */
  public void setRating(float newValue) {
    float oldValue = rating;
    rating = newValue;
    firePropertyChange("rating", oldValue, newValue);
    firePropertyChange("hasRating", false, true);
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
    firePropertyChange("runtime", oldValue, newValue);
  }

  /**
   * Sets the scraped.
   * 
   * @param newValue
   *          the new scraped
   */
  private void setScraped(boolean newValue) {
    this.scraped = newValue;
    firePropertyChange("scraped", false, newValue);
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
  public void setYear(String newValue) {
    String oldValue = year;
    year = newValue;
    firePropertyChange("year", oldValue, newValue);
    firePropertyChange("nameForUi", oldValue, newValue);
  }

  /**
   * Write images.
   * 
   * @param poster
   *          the poster
   * @param fanart
   *          the fanart
   */
  public void writeImages(boolean poster, boolean fanart) {
    FileOutputStream outputStream = null;
    InputStream is = null;
    CachedUrl url = null;
    String filename = null;
    String oldFilename = null;

    // poster
    if (poster && !StringUtils.isEmpty(getPosterUrl())) {
      // try {
      int i = 0;
      for (MoviePosterNaming name : Globals.settings.getMoviePosterFilenames()) {
        boolean firstImage = false;
        if (++i == 1) {
          firstImage = true;
        }

        filename = this.path + File.separator;
        switch (name) {
          case FILENAME_TBN:
            filename = filename + getMovieFiles().get(0).replaceAll("\\.[A-Za-z0-9]{3,4}$", ".tbn");
            break;

          case FILENAME_JPG:
            filename = filename + getMovieFiles().get(0).replaceAll("\\.[A-Za-z0-9]{3,4}$", ".jpg");
            break;

          case MOVIE_JPG:
            filename = filename + "movie.jpg";
            break;

          case MOVIE_TBN:
            filename = filename + "movie.tbn";
            break;

          case POSTER_JPG:
            filename = filename + "poster.jpg";
            break;

          case POSTER_TBN:
            filename = filename + "poster.tbn";
            break;

          case FOLDER_JPG:
            filename = filename + "folder.jpg";
            break;
        }

        // get image in thread
        MovieImageFetcher task = new MovieImageFetcher(this, getPosterUrl(), ArtworkType.POSTER, filename, firstImage);
        Globals.executor.execute(task);
      }
    }

    // fanart
    if (fanart && !StringUtils.isEmpty(getFanartUrl())) {
      // try {
      int i = 0;
      for (MovieFanartNaming name : Globals.settings.getMovieFanartFilenames()) {
        boolean firstImage = false;
        if (++i == 1) {
          firstImage = true;
          // oldFilename = getFanart();
          // setFanart("");
        }
        // url = new CachedUrl(getFanartUrl());
        // filename = this.path + File.separator + "fanart.jpg";
        filename = this.path + File.separator;
        switch (name) {
          case FILENAME_JPG:
            filename = filename + getMovieFiles().get(0).replaceAll("\\.[A-Za-z0-9]{3,4}$", "-fanart.jpg");
            break;

          case FANART_JPG:
            filename = filename + "fanart.jpg";
            break;
        }
        // get image in thread
        MovieImageFetcher task = new MovieImageFetcher(this, getFanartUrl(), ArtworkType.BACKDROP, filename, firstImage);
        Globals.executor.execute(task);
        // LOGGER.debug("writing fanart " + filename);
        // outputStream = new FileOutputStream(filename);
        // is = url.getInputStream();
        // IOUtils.copy(is, outputStream);
        // outputStream.close();
        // is.close();
        // if (i == 1) {
        // LOGGER.debug("set poster " + FilenameUtils.getName(filename));
        // setFanart(FilenameUtils.getName(filename));
        // }
      }
      // }
      // catch (IOException e) {
      // LOGGER.error("writeImages - fanart", e);
      // setFanart(oldFilename);
      // }
    }
  }

  /**
   * Write nfo.
   */
  public void writeNFO() {
    if (Globals.settings.getMovieConnector() == MovieConnectors.MP) {
      setNfoFilename(MovieToMpNfoConnector.setData(this));
    } else {
      setNfoFilename(MovieToXbmcNfoConnector.setData(this));
    }
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
   * Gets the production company.
   * 
   * @return the production company
   */
  public String getProductionCompany() {
    return productionCompany;
  }

  /**
   * Sets the production company.
   * 
   * @param newValue
   *          the new production company
   */
  public void setProductionCompany(String newValue) {
    String oldValue = this.productionCompany;
    this.productionCompany = newValue;
    firePropertyChange(PRODUCTION_COMPANY, oldValue, newValue);
  }

  /**
   * Save to db.
   */
  public synchronized void saveToDb() {
    // update DB
    Globals.entityManager.getTransaction().begin();
    Globals.entityManager.persist(this);
    Globals.entityManager.getTransaction().commit();
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
   * Adds the genre.
   * 
   * @param newValue
   *          the new value
   */
  public void addGenre(MediaGenres newValue) {
    genres.add(newValue);
    firePropertyChange(GENRE, null, newValue);
    firePropertyChange("genresAsString", null, newValue);
  }

  /**
   * Removes the genre.
   * 
   * @param genre
   *          the genre
   */
  public void removeGenre(MediaGenres genre) {
    genres.remove(genre);
    firePropertyChange(GENRE, null, genre);
    firePropertyChange("genresAsString", null, genre);
  }

  /**
   * Removes the all genres.
   */
  public void removeAllGenres() {
    genres.clear();
    firePropertyChange(GENRE, null, genres);
    firePropertyChange("genresAsString", null, genres);
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
    if (rating > 0) {
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
    for (MediaGenres genre : genres) {
      if (!StringUtils.isEmpty(sb)) {
        sb.append(", ");
      }
      sb.append(genre.toString());
    }
    return sb.toString();
  }

  /**
   * Gets the date added.
   * 
   * @return the date added
   */
  public Date getDateAdded() {
    return dateAdded;
  }

  public String getDateAddedAsString() {
    if (dateAdded == null) {
      return "";
    }
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
    return sdf.format(dateAdded);
  }

  /**
   * Sets the date added.
   * 
   * @param newValue
   *          the new date added
   */
  public void setDateAdded(Date newValue) {
    Date oldValue = this.dateAdded;
    this.dateAdded = newValue;
    firePropertyChange(DATE_ADDED, oldValue, newValue);
    firePropertyChange(DATE_ADDED_AS_STRING, oldValue, newValue);
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
}
