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
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.ScraperMetadataConfig;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.MovieCast.CastType;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaCastMember;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.scraper.util.UrlUtil;

import com.omertron.themoviedbapi.model.ArtworkType;

/**
 * The main class for movies.
 */
@Entity
public class Movie extends AbstractModelObject {

  /** The Constant TITLE. */
  protected final static String TITLE                = "title";

  /** The Constant TITLE. */
  protected final static String SORT_TITLE           = "sortTitle";

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

  // /** The Constant MOVIE_FILES. */
  // protected final static String MOVIE_FILES = "movieFiles";

  /** The Constant MEDIA_FILES. */
  protected final static String MEDIA_FILES          = "mediaFiles";

  /** The Constant DATE_ADDED. */
  protected final static String DATE_ADDED           = "dateAdded";

  /** The Constant DATE_ADDED_AS_STRING. */
  protected final static String DATE_ADDED_AS_STRING = "dateAddedAsString";

  /** The Constant WATCHED. */
  protected final static String WATCHED              = "watched";

  /** The Constant TRAILER. */
  protected final static String TRAILER              = "trailer";

  /** The Constant TAG. */
  protected final static String TAG                  = "tag";

  /** The Constant logger. */
  @XmlTransient
  private static final Logger   LOGGER               = Logger.getLogger(Movie.class);

  /** The id. */
  @Id
  @GeneratedValue
  private Long                  id;

  /** The name. */
  private String                name                 = "";

  @Transient
  private String                sortTitle            = "";

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

  /** The trailer. */
  @OneToMany(cascade = CascadeType.ALL)
  private List<MediaTrailer>    trailer              = new ArrayList<MediaTrailer>();

  /** The trailer observable. */
  @Transient
  private List<MediaTrailer>    trailerObservable    = ObservableCollections.observableList(trailer);

  /** The tags. */
  private List<String>          tags                 = new ArrayList<String>();

  /** The tags observable. */
  @Transient
  private List<String>          tagsObservable       = ObservableCollections.observableList(tags);

  /** The duplicate flag. */
  @Transient
  private boolean               duplicate            = false;

  /** The extra thumbs. */
  private List<String>          extraThumbs          = new ArrayList<String>();

  /** The movie set. */
  private MovieSet              movieSet;

  /**
   * Instantiates a new movie. Needed for JAXB
   */
  public Movie() {
  }

  /**
   * Returns the sortable variant of title<br>
   * eg "The Bourne Legacy" -> "Bourne Legacy, The"
   * 
   * @return the title in its sortable format
   */
  public String getSortTitle() {
    if (StringUtils.isEmpty(sortTitle)) {
      sortTitle = Utils.getSortableName(this.getName());
    }
    return sortTitle;
  }

  // /**
  // * Returns the sortable variant of originatltitle<br>
  // * eg "The Bourne Legacy" -> "Bourne Legacy, The"
  // *
  // * @return the originaltitle in its sortable format
  // */
  // public String getOriginalNameSortable() {
  // return Utils.getSortableName(this.getOriginalName());
  // }

  // /**
  // * Returns the common name of title when it is named sortable<br>
  // * eg "Bourne Legacy, The" -> "The Bourne Legacy"
  // *
  // * @return the common title
  // */
  // public String getNameRemoveSortable() {
  // return Utils.removeSortableName(this.getName());
  // }
  //
  // /**
  // * Returns the common name of title when it is named sortable<br>
  // * eg "Bourne Legacy, The" -> "The Bourne Legacy"
  // *
  // * @return the common originaltitle
  // */
  // public String getOriginalNameRemoveSortable() {
  // return Utils.removeSortableName(this.getOriginalName());
  // }

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
    StringBuffer nameForUi = new StringBuffer(name);
    if (year != null && !year.isEmpty()) {
      nameForUi.append(" (");
      nameForUi.append(year);
      nameForUi.append(")");
    }
    return nameForUi.toString();
  }

  /**
   * Sets the observable cast list.
   */
  public void setObservables() {
    castObservable = ObservableCollections.observableList(cast);
    mediaFilesObservable = ObservableCollections.observableList(mediaFiles);
    trailerObservable = ObservableCollections.observableList(trailer);
    tagsObservable = ObservableCollections.observableList(tags);
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
   * Downloads trailer to movie folder (get from NFO), naming
   * <code>&lt;movie&gt;-trailer.ext</code><br>
   * Downloads to .tmp file first and renames after successful download.
   * 
   * @author Myron Boyle
   * @param trailerToDownload
   *          the MediaTrailer object to download
   * @return true/false if successful
   */
  public Boolean downladTtrailer(MediaTrailer trailerToDownload) {
    // get trailer filename from NFO file
    String tfile = this.getNfoFilename().replaceAll("(?i)\\.nfo$", "-trailer.");
    try {
      String ext = UrlUtil.getFileExtension(trailerToDownload.getUrl());
      if (ext.isEmpty()) {
        ext = "unknown";
      }
      // download to temp first
      trailerToDownload.downloadTo(tfile + ext + ".tmp");
      LOGGER.info("Trailer download successfully");
      // TODO: maybe check if there are other trailerfiles (with other
      // extension) and remove
      FileUtils.deleteQuietly(new File(tfile + ext));
      MovieRenamer.moveFile(tfile + ext + ".tmp", tfile + ext);
    }
    catch (IOException e) {
      LOGGER.error("Error downloading trailer", e);
      return false;
    }
    catch (URISyntaxException e) {
      LOGGER.error("Error downloading trailer; url invalid", e);
      return false;
    }
    catch (Exception e) {
      LOGGER.error("Error downloading trailer; rename failed", e);
    }
    return true;
  }

  /**
   * Adds the to tags.
   * 
   * @param newTag
   *          the new tag
   */
  public void addToTags(String newTag) {
    for (String tag : tagsObservable) {
      if (tag.equals(newTag)) {
        return;
      }
    }

    tagsObservable.add(newTag);
    firePropertyChange(TAG, null, tagsObservable);
    firePropertyChange("tagsAsString", null, tagsObservable);
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
    firePropertyChange("tagsAsString", null, tagsObservable);
  }

  /**
   * Clear tags.
   */
  public void clearTags() {
    tagsObservable.clear();
    firePropertyChange(TAG, null, tagsObservable);
    firePropertyChange("tagsAsString", null, tagsObservable);
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
   * Adds the to files.
   * 
   * @param newFile
   *          the new file
   */
  public void addToFiles(String newFile) {
    // movieFiles.add(newFile);
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
    }
    else {
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
    }
    else {
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
    if (StringUtils.isEmpty(filename)) {
      return false;
    }

    for (MediaFile file : mediaFiles) {
      if (filename.compareTo(file.getFilename()) == 0) {
        return true;
      }
    }

    return false;
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

      if (movie != null) {
        movie.setPath(path);
        movie.addToFiles(videoFiles);
        movie.findImages();
        break;
      }

      LOGGER.debug("did not find movie informations in nfo");
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
   * Download extra thumbs.
   * 
   * @param thumbs
   *          the thumbs
   */
  public void downloadExtraThumbs(List<String> thumbs) {
    // init/delete old thumbs
    extraThumbs.clear();

    try {
      String path = getPath() + File.separator + "extrathumbs";
      File folder = new File(path);
      if (folder.exists()) {
        FileUtils.deleteDirectory(folder);
      }

      folder.mkdirs();

      // fetch and store images
      for (int i = 0; i < thumbs.size(); i++) {
        String url = thumbs.get(i);
        CachedUrl cachedUrl = new CachedUrl(url);
        FileOutputStream outputStream = new FileOutputStream(path + File.separator + "thumb" + (i + 1) + ".jpg");
        InputStream is = cachedUrl.getInputStream();
        IOUtils.copy(is, outputStream);
        outputStream.close();
        is.close();
      }
    }
    catch (IOException e) {
      LOGGER.warn("download extrathumbs", e);
    }
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
   * @param md
   *          the new metadata
   */
  public void setMetadata(MediaMetadata md) {
    setMetadata(md, Globals.settings.getScraperMetadataConfig());
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
  public void setMetadata(MediaMetadata metadata, ScraperMetadataConfig config) {
    setImdbId(metadata.getImdbId());
    setTmdbId(metadata.getTmdbId());

    // set chosen metadata
    if (config.isTitle()) {
      setName(metadata.getTitle());
    }

    if (config.isOriginalTitle()) {
      setOriginalName(metadata.getOriginalTitle());
    }

    if (config.isTagline()) {
      setTagline(metadata.getTagline());
    }

    if (config.isPlot()) {
      setOverview(metadata.getPlot());
    }

    if (config.isYear()) {
      setYear(metadata.getYear());
    }

    if (config.isRating()) {
      setRating((float) metadata.getRating());
      setVotes(metadata.getVoteCount());
    }

    if (config.isRuntime()) {
      setRuntime(metadata.getRuntime());
    }

    // certifications
    if (config.isCertification()) {
      if (metadata.getCertifications() != null && metadata.getCertifications().size() > 0) {
        setCertification(metadata.getCertifications().get(0));
      }
    }

    // // poster
    // if (config.isArtwork()) {
    // List<MediaArtwork> art = metadata.getMediaArt(MediaArtworkType.POSTER);
    // if (art.size() > 0) {
    // MediaArtwork poster = art.get(0);
    // setPosterUrl(poster.getDownloadUrl());
    // }
    //
    // // fanart
    // art = metadata.getMediaArt(MediaArtworkType.BACKGROUND);
    // if (art.size() > 0) {
    // MediaArtwork fanart = art.get(0);
    // setFanartUrl(fanart.getDownloadUrl());
    // }
    // }

    // cast
    if (config.isCast()) {
      setProductionCompany(metadata.getProductionCompany());
      removeAllActors();
      List<MediaCastMember> cast = metadata.getCastMembers();
      String director = "";
      String writer = "";
      for (MediaCastMember member : cast) {
        MovieCast castMember = new MovieCast();
        castMember.setName(member.getName());
        castMember.setCharacter(member.getCharacter());
        castMember.setThumb(member.getImageUrl());
        switch (member.getType()) {
          case ACTOR:
            castMember.setType(CastType.ACTOR);
            addToCast(castMember);
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
        }
      }
      setDirector(director);
      setWriter(writer);
    }

    // genres
    if (config.isGenres()) {
      removeAllGenres();
      for (MediaGenres genre : metadata.getGenres()) {
        addGenre(genre);
      }
    }

    // // trailer
    // if (config.isTrailer()) {
    // removeAllTrailers();
    // List<MediaTrailer> trailers = metadata.getTrailers();
    // for (MediaTrailer trailer : trailers) {
    // if (this.trailer.size() == 0) {
    // trailer.setInNfo(Boolean.TRUE);
    // }
    // addTrailer(trailer);
    // }
    //
    // // extra trailer from hdtrailers.net
    // trailers = HDTrailersNet.getTrailers(this);
    // for (MediaTrailer trailer : trailers) {
    // if (this.trailer.size() == 0) {
    // trailer.setInNfo(Boolean.TRUE);
    // }
    // addTrailer(trailer);
    // }
    // }

    // set scraped
    setScraped(true);

    // write NFO
    writeNFO();
    // writeImages(true, true);

    // update DB
    saveToDb();

  }

  /**
   * Sets the trailers.
   * 
   * @param trailers
   *          the new trailers
   */
  public void setTrailers(List<MediaTrailer> trailers) {
    removeAllTrailers();
    for (MediaTrailer trailer : trailers) {
      if (this.trailer.size() == 0) {
        trailer.setInNfo(Boolean.TRUE);
      }
      addTrailer(trailer);
    }

    // persist
    saveToDb();
  }

  /**
   * Gets the metadata.
   * 
   * @return the metadata
   */
  public MediaMetadata getMetadata() {
    MediaMetadata md = new MediaMetadata("");

    md.setImdbId(imdbId);
    md.setTmdbId(tmdbId);
    md.setTitle(name);
    md.setOriginalTitle(originalName);
    md.setTagline(tagline);
    md.setPlot(overview);
    md.setYear(year);
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
   *          the new artwork
   */
  public void setArtwork(MediaMetadata md) {
    setArtwork(md, Globals.settings.getScraperMetadataConfig());
  }

  /**
   * Sets the artwork.
   * 
   * @param md
   *          the md
   * @param config
   *          the config
   */
  public void setArtwork(MediaMetadata md, ScraperMetadataConfig config) {
    setArtwork(md.getMediaArt(MediaArtworkType.ALL), config);
  }

  /**
   * Sets the artwork.
   * 
   * @param artwork
   *          the new artwork
   */
  public void setArtwork(List<MediaArtwork> artwork) {
    setArtwork(artwork, Globals.settings.getScraperMetadataConfig());
  }

  /**
   * Sets the artwork.
   * 
   * @param artwork
   *          the artwork
   * @param config
   *          the config
   */
  public void setArtwork(List<MediaArtwork> artwork, ScraperMetadataConfig config) {
    if (config.isArtwork()) {
      // poster
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.POSTER) {
          setPosterUrl(art.getDownloadUrl());
          break;
        }
      }

      // fanart
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.BACKGROUND) {
          setFanartUrl(art.getDownloadUrl());
          break;
        }
      }

      // download images
      writeImages(true, true);
      // update DB
      saveToDb();
    }
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

    sortTitle = "";
    firePropertyChange(SORT_TITLE, oldValue, newValue);
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
            filename = filename + getMediaFiles().get(0).getFilename().replaceAll("\\.[A-Za-z0-9]{3,4}$", ".tbn");
            break;

          case FILENAME_JPG:
            filename = filename + getMediaFiles().get(0).getFilename().replaceAll("\\.[A-Za-z0-9]{3,4}$", ".jpg");
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
      int i = 0;
      for (MovieFanartNaming name : Globals.settings.getMovieFanartFilenames()) {
        boolean firstImage = false;
        if (++i == 1) {
          firstImage = true;
        }
        filename = this.path + File.separator;
        switch (name) {
          case FILENAME_JPG:
            filename = filename + getMediaFiles().get(0).getFilename().replaceAll("\\.[A-Za-z0-9]{3,4}$", "-fanart.jpg");
            break;

          case FANART_JPG:
            filename = filename + "fanart.jpg";
            break;
        }
        // get image in thread
        MovieImageFetcher task = new MovieImageFetcher(this, getFanartUrl(), ArtworkType.BACKDROP, filename, firstImage);
        Globals.executor.execute(task);
      }
    }
  }

  /**
   * Write nfo.
   */
  public void writeNFO() {
    if (Globals.settings.getMovieConnector() == MovieConnectors.MP) {
      setNfoFilename(MovieToMpNfoConnector.setData(this));
    }
    else {
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
      sb.append(genre != null ? genre.toString() : "null");
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

  /**
   * Gets the date added as string.
   * 
   * @return the date added as string
   */
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

  /**
   * Sets the duplicate.
   */
  public void setDuplicate() {
    this.duplicate = true;
  }

  /**
   * Clear duplicate.
   */
  public void clearDuplicate() {
    this.duplicate = false;
  }

  /**
   * Checks if is duplicate.
   * 
   * @return true, if is duplicate
   */
  public boolean isDuplicate() {
    return this.duplicate;
  }

  /**
   * <p>
   * Uses <code>ReflectionToStringBuilder</code> to generate a
   * <code>toString</code> for the specified object.
   * </p>
   * 
   * @param object
   *          the Object to be output
   * @return the String result
   * @see ReflectionToStringBuilder#toString(Object)
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  /**
   * @return the movieset
   */
  public MovieSet getMovieSet() {
    return movieSet;
  }

  /**
   * @param movieset
   *          the movieset to set
   */
  public void setMovieSet(MovieSet movieset) {
    this.movieSet = movieset;
  }
}
