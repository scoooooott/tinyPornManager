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

import static org.tinymediamanager.core.Constants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
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
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaEntity;
import org.tinymediamanager.core.MediaEntityImageFetcher;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.MovieCast.CastType;
import org.tinymediamanager.core.movie.connector.MovieConnectors;
import org.tinymediamanager.core.movie.connector.MovieToMpNfoConnector;
import org.tinymediamanager.core.movie.connector.MovieToXbmcNfoConnector;
import org.tinymediamanager.core.movie.tasks.MovieActorImageFetcher;
import org.tinymediamanager.core.movie.tasks.MovieExtraImageFetcher;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaCastMember;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.scraper.util.ParserUtils;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.scraper.util.UrlUtil;

/**
 * The main class for movies.
 * 
 * @author Manuel Laggner / Myron Boyle
 */
@Entity
@Inheritance(strategy = javax.persistence.InheritanceType.JOINED)
public class Movie extends MediaEntity {
  /** The Constant logger. */
  @XmlTransient
  private static final Logger LOGGER               = Logger.getLogger(Movie.class);

  /** The title sortable. */
  @Transient
  private String              titleSortable        = "";

  /** The sorttitle. */
  private String              sortTitle            = "";

  /** The imdb id. */
  private String              imdbId               = "";

  /** The tmdb id. */
  private int                 tmdbId               = 0;

  /** The tagline. */
  private String              tagline              = "";

  /** The votes. */
  private int                 votes                = 0;

  /** The runtime. */
  private int                 runtime              = 0;

  /** The nfo filename. */
  private String              nfoFilename          = "";

  /** The director. */
  private String              director             = "";

  /** The writer. */
  private String              writer               = "";

  /** The certification. */
  private Certification       certification        = Certification.NOT_RATED;

  /** The data source. */
  private String              dataSource           = "";

  /** The watched. */
  private boolean             watched              = false;

  /** The new genres based on an enum like class. */
  private List<String>        genres               = new ArrayList<String>();

  /** The genres2 for access. */
  @Transient
  private List<MediaGenres>   genresForAccess      = new ArrayList<MediaGenres>();

  /** The cast. */
  @OneToMany(cascade = CascadeType.ALL)
  private List<MovieCast>     cast                 = new ArrayList<MovieCast>();

  /** The cast observable. */
  @Transient
  private List<MovieCast>     castObservable       = ObservableCollections.observableList(cast);

  /** The media files. */
  @OneToMany(cascade = CascadeType.ALL)
  private List<MediaFile>     mediaFiles           = new ArrayList<MediaFile>();

  /** The media files observable. */
  @Transient
  private List<MediaFile>     mediaFilesObservable = ObservableCollections.observableList(mediaFiles);

  /** The trailer. */
  @OneToMany(cascade = CascadeType.ALL)
  private List<MediaTrailer>  trailer              = new ArrayList<MediaTrailer>();

  /** The trailer observable. */
  @Transient
  private List<MediaTrailer>  trailerObservable    = ObservableCollections.observableList(trailer);

  /** The tags. */
  private List<String>        tags                 = new ArrayList<String>();

  /** The tags observable. */
  @Transient
  private List<String>        tagsObservable       = ObservableCollections.observableList(tags);

  /** The extra thumbs. */
  private List<String>        extraThumbs          = new ArrayList<String>();

  /** The extra fanarts. */
  private List<String>        extraFanarts         = new ArrayList<String>();

  /** The movie set. */
  private MovieSet            movieSet;

  /** is this a disc movie folder (video_ts / bdmv)?. */
  private boolean             isDisc               = false;

  /** The spoken languages. */
  private String              spokenLanguages      = "";

  /**
   * Instantiates a new movie. Needed for JAXB
   */
  public Movie() {
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
      setSortTitle(movieSet.getName() + (movieSet.getMovieIndex(this) + 1));
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

  /**
   * Gets the nfo filename.
   * 
   * @return the nfo filename
   */
  public String getNfoFilename() {
    if (!StringUtils.isEmpty(nfoFilename)) {
      return path + File.separator + nfoFilename;
    }
    else {
      return nfoFilename;
    }
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
    if (!StringUtils.isEmpty(poster) && !StringUtils.isEmpty(fanart)) {
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
    firePropertyChange(NFO_FILENAME, oldValue, newValue);
    firePropertyChange(HAS_NFO_FILE, false, true);
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
   * Sets the observable cast list.
   */
  private void setObservables() {
    castObservable = ObservableCollections.observableList(cast);
    mediaFilesObservable = ObservableCollections.observableList(mediaFiles);
    trailerObservable = ObservableCollections.observableList(trailer);
    tagsObservable = ObservableCollections.observableList(tags);
  }

  /**
   * Initialize after loading.
   */
  public void initializeAfterLoading() {
    // set observables
    setObservables();

    // load genres
    for (String genre : genres) {
      addGenre(MediaGenres.getGenre(genre));
    }
  }

  /**
   * Adds the to cast.
   * 
   * @param obj
   *          the obj
   */
  public void addToCast(MovieCast obj) {
    castObservable.add(obj);
    firePropertyChange(CAST, null, this.getCast());

    switch (obj.getType()) {
      case ACTOR:
        firePropertyChange(ACTORS, null, this.getCast());
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
   * Downloads trailer to movie folder (get from NFO), naming <code>&lt;movie&gt;-trailer.ext</code><br>
   * Downloads to .tmp file first and renames after successful download.
   * 
   * @param trailerToDownload
   *          the MediaTrailer object to download
   * @return true/false if successful
   * @author Myron Boyle
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
   * Adds a media file.
   * 
   * @param path
   *          the path of the media file (needs no to be the same as movie path)
   * @param newFile
   *          the new file
   */
  public void addToFiles(String path, String newFile) {
    MediaFile mediaFile = new MediaFile(path, newFile);
    mediaFile.gatherMediaInformation();
    addToMediaFiles(mediaFile);
  }

  /**
   * Adds the list of media files.
   * 
   * @param videoFiles
   *          the video files
   */
  public void addToFiles(File[] videoFiles) {
    for (File file : videoFiles) {
      // check if that file exists for that movie
      if (!hasFile(file.getName())) {
        // create new movie file
        addToFiles(file.getParent(), file.getName());
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

    // actor images
    if (Globals.settings.isWriteActorImages()) {
      findActorImages();
    }
  }

  /**
   * Find local trailers.
   */
  public void addLocalTrailers() {
    LOGGER.debug("try to find local/downloaded trailers");

    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.matches("(?i)^sample\\..{2,4}") || name.matches("(?i).*-trailer\\..{2,4}");
      }
    };

    File[] trailerFiles = new File(path).listFiles(filter);
    for (File file : trailerFiles) {
      MediaTrailer mt = new MediaTrailer();
      mt.setName(file.getName());
      mt.setProvider("downloaded");
      mt.setQuality("unknown");
      mt.setInNfo(false);
      mt.setUrl(file.toURI().toString());
      addTrailer(mt);
    }

  }

  /**
   * checks movie folder for poster and sets it.
   * 
   * @param name
   *          the filename within movie folder
   * @return true/false if found (and set)
   */
  private boolean findAndSetPoster(String name) {
    File p = new File(name);
    if (p.exists()) {
      setPoster(p.getName());
      LOGGER.debug("found poster " + p.getPath());
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Find poster.
   */
  private void findPoster() {
    boolean found = false;

    MoviePosterNaming[] all = MoviePosterNaming.values();
    for (MoviePosterNaming variant : all) {
      if (!found) {
        found = findAndSetPoster(getPosterFilename(variant));
      }
    }

    // still not found anything? try *-poster.*
    if (!found) {
      Pattern pattern = Pattern.compile("(?i).*-poster\\..{2,4}");
      File[] files = new File(path).listFiles();
      for (File file : files) {
        Matcher matcher = pattern.matcher(file.getName());
        if (matcher.matches()) {
          setPoster(FilenameUtils.getName(file.getName()));
          LOGGER.debug("found poster " + file.getPath());
          found = true;
        }
      }
    }

    // we did not find a poster, try to get it if an url exists
    if (!found && StringUtils.isNotEmpty(posterUrl)) {
      writeImages(true, false);
      found = true;
      LOGGER.debug("got poster url: " + posterUrl + " ; try to download this");
    }

    if (!found) {
      LOGGER.debug("Sorry, could not find poster.");
    }
  }

  /**
   * checks movie folder for fanart and sets it.
   * 
   * @param name
   *          the filename within movie folder
   * @return true/false if found (and set)
   */
  private boolean findAndSetFanart(String name) {
    File p = new File(name);
    if (p.exists()) {
      setFanart(p.getName());
      LOGGER.debug("found fanart " + p.getPath());
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Find fanart.
   */
  private void findFanart() {
    boolean found = false;

    MovieFanartNaming[] all = MovieFanartNaming.values();
    for (MovieFanartNaming variant : all) {
      if (!found) {
        found = findAndSetFanart(getFanartFilename(variant));
      }
    }

    // still not found anything? try *-fanart.*
    if (!found) {
      Pattern pattern = Pattern.compile("(?i).*-fanart\\..{2,4}");
      File[] files = new File(path).listFiles();
      for (File file : files) {
        Matcher matcher = pattern.matcher(file.getName());
        if (matcher.matches()) {
          setFanart(FilenameUtils.getName(file.getName()));
          LOGGER.debug("found fanart " + file.getPath());
          found = true;
        }
      }
    }

    // we did not find a poster, try to get it if an url exists
    if (!found && StringUtils.isNotEmpty(fanartUrl)) {
      writeImages(false, true);
      found = true;
      LOGGER.debug("got fanart url: " + fanartUrl + " ; try to download this");
    }

    if (!found) {
      LOGGER.debug("Sorry, could not find fanart.");
    }
  }

  /**
   * Find actor images.
   */
  private void findActorImages() {
    String actorsDirPath = getPath() + File.separator + MovieCast.ACTOR_DIR;

    // second download missing images
    for (MovieCast actor : getActors()) {
      String actorName = actor.getName().replace(" ", "_");
      File actorImage = new File(actorsDirPath + File.separator + actorName + ".tbn");
      // set path if it is empty and an image exists
      if (actorImage.exists() && StringUtils.isEmpty(actor.getThumbPath())) {
        actor.setThumbPath(MovieCast.ACTOR_DIR + File.separator + actorName + ".tbn");
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
  @Override
  public String getFanart() {
    if (!StringUtils.isEmpty(fanart)) {
      return path + File.separator + fanart;
    }
    else {
      return fanart;
    }
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
    firePropertyChange(TMDBID, oldValue, newValue);
  }

  /**
   * Gets the poster.
   * 
   * @return the poster
   */
  @Override
  public String getPoster() {
    if (!StringUtils.isEmpty(poster)) {
      return path + File.separator + poster;
    }
    else {
      return poster;
    }
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

        // check if filetype is in our settings
        if (name.toLowerCase().endsWith("nfo")) {
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

      // no known NFO format? try to find a imdb number in it... (if <100kb ;)
      if (movie == null) {
        if (FileUtils.sizeOf(file) < 100000) {
          try {
            String imdb = FileUtils.readFileToString(file);
            imdb = StrgUtils.substr(imdb, ".*(tt\\d{7}).*");
            if (!imdb.isEmpty()) {
              LOGGER.debug("Found IMDB id: " + imdb);
              movie = new Movie();
              movie.setImdbId(imdb);
              movie.setTitle(ParserUtils.detectCleanMoviename(directory.getName()));
            }
          }
          catch (IOException e) {
            LOGGER.warn("couldn't read NFO " + file.getName());
          }
        }
      }

      if (movie != null) {
        movie.setPath(path);
        movie.addToFiles(videoFiles);
        movie.findImages();
        movie.addLocalTrailers();
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
    firePropertyChange(FANART, oldValue, newValue);
    firePropertyChange(HAS_IMAGES, false, true);
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

    // do not create extrathumbs folder, if no extrathumbs are selected
    if (thumbs.size() == 0) {
      return;
    }

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
        String providedFiletype = FilenameUtils.getExtension(url);

        FileOutputStream outputStream = null;
        InputStream is = null;
        if (Globals.settings.isImageExtraThumbsResize() && Globals.settings.getImageExtraThumbsSize() > 0) {
          outputStream = new FileOutputStream(path + File.separator + "thumb" + (i + 1) + ".jpg");
          try {
            is = ImageCache.scaleImage(url, Globals.settings.getImageExtraThumbsSize());
          }
          catch (Exception e) {
            LOGGER.warn("problem with rescaling: " + e.getMessage());
            continue;
          }
        }
        else {
          outputStream = new FileOutputStream(path + File.separator + "thumb" + (i + 1) + "." + providedFiletype);
          CachedUrl cachedUrl = new CachedUrl(url);
          is = cachedUrl.getInputStream();
        }

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
   * Download extra thumbs.
   * 
   * @param fanarts
   *          the fanarts
   */
  public void downloadExtraFanarts(List<String> fanarts) {
    // init/delete old fanarts
    extraFanarts.clear();

    // do not create extrafanarts folder, if no extrafanarts are selected
    if (fanarts.size() == 0) {
      return;
    }

    try {
      String path = getPath() + File.separator + "extrafanart";
      File folder = new File(path);
      if (folder.exists()) {
        FileUtils.deleteDirectory(folder);
      }

      folder.mkdirs();

      // fetch and store images
      for (int i = 0; i < fanarts.size(); i++) {
        String url = fanarts.get(i);
        String providedFiletype = FilenameUtils.getExtension(url);
        CachedUrl cachedUrl = new CachedUrl(url);
        FileOutputStream outputStream = new FileOutputStream(path + File.separator + "fanart" + (i + 1) + "." + providedFiletype);
        InputStream is = cachedUrl.getInputStream();
        IOUtils.copy(is, outputStream);
        outputStream.close();
        is.close();
      }
    }
    catch (IOException e) {
      LOGGER.warn("download extrafanarts", e);
    }
  }

  /**
   * Write extra images.
   * 
   * @param extrathumbs
   *          the extrathumbs
   * @param extrafanart
   *          the extrafanart
   */
  public void writeExtraImages(boolean extrathumbs, boolean extrafanart) {
    // get images in thread
    MovieExtraImageFetcher task = new MovieExtraImageFetcher(this, extrafanart, extrathumbs);
    Globals.executor.execute(task);
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
    setMetadata(md, Globals.settings.getMovieScraperMetadataConfig());
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
    // check if metadata has at least a name
    if (StringUtils.isEmpty(metadata.getTitle())) {
      LOGGER.warn("wanted to save empty metadata for " + getTitle());
      return;
    }

    setImdbId(metadata.getImdbId());
    setTmdbId(metadata.getTmdbId());

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
      setYear(metadata.getYear());
    }

    if (config.isRating()) {
      setRating((float) metadata.getRating());
      setVotes(metadata.getVoteCount());
    }

    if (config.isRuntime()) {
      setRuntime(metadata.getRuntime());
    }

    setSpokenLanguages(metadata.getSpokenLanguages());

    // certifications
    if (config.isCertification()) {
      if (metadata.getCertifications() != null && metadata.getCertifications().size() > 0) {
        setCertification(metadata.getCertifications().get(0));
      }
    }

    // cast
    if (config.isCast()) {
      setProductionCompany(metadata.getProductionCompany());
      List<MediaCastMember> cast = metadata.getCastMembers();
      List<MovieCast> actors = new ArrayList<MovieCast>();
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
            // addToCast(castMember);
            actors.add(castMember);
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
      writeActorImages();
    }

    // genres
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
    md.setTitle(title);
    md.setOriginalTitle(originalTitle);
    md.setTagline(tagline);
    md.setPlot(plot);
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
    setArtwork(md, Globals.settings.getMovieScraperMetadataConfig());
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
   *          the new artwork
   */
  public void setArtwork(List<MediaArtwork> artwork) {
    setArtwork(artwork, Globals.settings.getMovieScraperMetadataConfig());
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
      // poster
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.POSTER) {
          setPosterUrl(art.getDefaultUrl());

          LOGGER.debug(art.getSmallestArtwork());
          LOGGER.debug(art.getBiggestArtwork());

          // did we get the tmdbid from artwork?
          if (tmdbId == 0 && art.getTmdbId() > 0) {
            setTmdbId(art.getTmdbId());
          }
          break;
        }
      }

      // fanart
      for (MediaArtwork art : artwork) {
        if (art.getType() == MediaArtworkType.BACKGROUND) {
          setFanartUrl(art.getDefaultUrl());

          LOGGER.debug(art.getSmallestArtwork());
          LOGGER.debug(art.getBiggestArtwork());

          // did we get the tmdbid from artwork?
          if (tmdbId == 0 && art.getTmdbId() > 0) {
            setTmdbId(art.getTmdbId());
          }
          break;
        }
      }

      // extrathumbs
      List<String> extrathumbs = new ArrayList<String>();
      if (Globals.settings.isImageExtraThumbs() && Globals.settings.getImageExtraThumbsCount() > 0) {
        for (MediaArtwork art : artwork) {
          if (art.getType() == MediaArtworkType.BACKGROUND) {
            extrathumbs.add(art.getDefaultUrl());
            if (extrathumbs.size() >= Globals.settings.getImageExtraThumbsCount()) {
              break;
            }
          }
        }
        setExtraThumbs(extrathumbs);
      }

      // extrafanarts
      List<String> extrafanarts = new ArrayList<String>();
      if (Globals.settings.isImageExtraFanart() && Globals.settings.getImageExtraFanartCount() > 0) {
        for (MediaArtwork art : artwork) {
          if (art.getType() == MediaArtworkType.BACKGROUND) {
            extrafanarts.add(art.getDefaultUrl());
            if (extrafanarts.size() >= Globals.settings.getImageExtraFanartCount()) {
              break;
            }
          }
        }
        setExtraFanarts(extrafanarts);
      }

      // download extra images
      if (extrathumbs.size() > 0 || extrafanarts.size() > 0) {
        writeExtraImages(true, true);
      }

      // download images
      writeImages(true, true);
      // update DB
      saveToDb();
    }
  }

  /**
   * Sets the actors.
   * 
   * @param newCast
   *          the new actors
   */
  public void setActors(List<MovieCast> newCast) {
    // two way sync of cast

    // first add the new ones
    for (MovieCast cast : newCast) {
      if (!castObservable.contains(cast)) {
        castObservable.add(cast);
      }
    }

    // second remove unused
    for (int i = castObservable.size() - 1; i >= 0; i--) {
      MovieCast cast = castObservable.get(i);
      if (!newCast.contains(cast)) {
        castObservable.remove(cast);
      }
    }

    firePropertyChange("cast", null, this.getCast());
    firePropertyChange("actors", null, this.getCast());
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
   * Sets the poster.
   * 
   * @param newValue
   *          the new poster
   */
  @Override
  public void setPoster(String newValue) {
    String oldValue = this.poster;
    this.poster = newValue;
    firePropertyChange("poster", oldValue, newValue);
    firePropertyChange("hasImages", false, true);
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
   * all XBMC supported poster names.
   * 
   * @param poster
   *          the poster
   * @return the poster filename
   */
  public String getPosterFilename(MoviePosterNaming poster) {
    String filename = path + File.separator;
    String mediafile = FilenameUtils.getBaseName(getMediaFiles().get(0).getFilename());

    switch (poster) {
      case MOVIENAME_POSTER_PNG:
        filename += getTitle() + ".png";
        break;
      case MOVIENAME_POSTER_JPG:
        filename += getTitle() + ".jpg";
        break;
      case MOVIENAME_POSTER_TBN:
        filename += getTitle() + ".tbn";
        break;
      case FILENAME_POSTER_PNG:
        filename += mediafile + "-poster.png";
        break;
      case FILENAME_POSTER_JPG:
        filename += mediafile + "-poster.jpg";
        break;
      case FILENAME_POSTER_TBN:
        filename += mediafile + "-poster.tbn";
        break;
      case FILENAME_PNG:
        filename += mediafile + ".png";
        break;
      case FILENAME_JPG:
        filename += mediafile + ".jpg";
        break;
      case FILENAME_TBN:
        filename += mediafile + ".tbn";
        break;
      case MOVIE_PNG:
        filename += "movie.png";
        break;
      case MOVIE_JPG:
        filename += "movie.jpg";
        break;
      case MOVIE_TBN:
        filename += "movie.tbn";
        break;
      case POSTER_PNG:
        filename += "poster.png";
        break;
      case POSTER_JPG:
        filename += "poster.jpg";
        break;
      case POSTER_TBN:
        filename += "poster.tbn";
        break;
      case FOLDER_PNG:
        filename += "folder.png";
        break;
      case FOLDER_JPG:
        filename += "folder.jpg";
        break;
      case FOLDER_TBN:
        filename += "folder.tbn";
        break;
      default:
        filename = "";
        break;
    }
    return filename;
  }

  /**
   * all XBMC supported fanart names.
   * 
   * @param fanart
   *          the fanart
   * @return the fanart filename
   */
  public String getFanartFilename(MovieFanartNaming fanart) {
    String filename = path + File.separator;
    String mediafile = FilenameUtils.getBaseName(getMediaFiles().get(0).getFilename());

    switch (fanart) {
      case FANART_PNG:
        filename += "fanart.png";
        break;
      case FANART_JPG:
        filename += "fanart.jpg";
        break;
      case FANART_TBN:
        filename += "fanart.tbn";
        break;
      case FILENAME_FANART_PNG:
        filename += mediafile + "-fanart.png";
        break;
      case FILENAME_FANART_JPG:
        filename += mediafile + "-fanart.jpg";
        break;
      case FILENAME_FANART_TBN:
        filename += mediafile + "-fanart.tbn";
        break;
      case MOVIENAME_FANART_PNG:
        filename += getTitle() + "-fanart.png";
        break;
      case MOVIENAME_FANART_JPG:
        filename += getTitle() + "-fanart.jpg";
        break;
      case MOVIENAME_FANART_TBN:
        filename += getTitle() + "-fanart.tbn";
        break;
      default:
        filename = "";
        break;
    }
    return filename;
  }

  /**
   * all XBMC supported NFO names.
   * 
   * @param nfo
   *          the nfo
   * @return the nfo filename
   */
  public String getNfoFilename(MovieNfoNaming nfo) {
    String filename = path + File.separator;
    String mediafile = FilenameUtils.getBaseName(getMediaFiles().get(0).getFilename());

    switch (nfo) {
      case FILENAME_NFO:
        filename += mediafile + ".nfo";
        break;
      case MOVIE_NFO:
        filename += "movie.nfo";
        break;
      default:
        filename = "";
        break;
    }
    return filename;
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
    String filename = null;

    // poster
    if (poster && !StringUtils.isEmpty(getPosterUrl())) {
      // try {
      int i = 0;
      for (MoviePosterNaming name : Globals.settings.getMoviePosterFilenames()) {
        boolean firstImage = false;
        filename = getPosterFilename(name);

        // only store .png as png and .jpg as jpg
        String generatedFiletype = FilenameUtils.getExtension(filename);
        String providedFiletype = FilenameUtils.getExtension(getPosterUrl());
        if (!generatedFiletype.equals(providedFiletype)) {
          continue;
        }

        if (++i == 1) {
          firstImage = true;
        }

        // get image in thread
        MediaEntityImageFetcher task = new MediaEntityImageFetcher(this, getPosterUrl(), MediaArtworkType.POSTER, filename, firstImage);
        Globals.executor.execute(task);
      }
    }

    // fanart
    if (fanart && !StringUtils.isEmpty(getFanartUrl())) {
      int i = 0;
      for (MovieFanartNaming name : Globals.settings.getMovieFanartFilenames()) {
        boolean firstImage = false;
        filename = getFanartFilename(name);

        // only store .png as png and .jpg as jpg
        String generatedFiletype = FilenameUtils.getExtension(filename);
        String providedFiletype = FilenameUtils.getExtension(getFanartUrl());
        if (!generatedFiletype.equals(providedFiletype)) {
          continue;
        }

        if (++i == 1) {
          firstImage = true;
        }

        // get image in thread
        MediaEntityImageFetcher task = new MediaEntityImageFetcher(this, getFanartUrl(), MediaArtworkType.BACKGROUND, filename, firstImage);
        Globals.executor.execute(task);
      }
    }
  }

  /**
   * Write actor images.
   */
  public void writeActorImages() {
    // check if actor images shall be written
    if (!Globals.settings.isWriteActorImages()) {
      return;
    }

    MovieActorImageFetcher task = new MovieActorImageFetcher(this);
    Globals.executor.execute(task);
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
      sb.append(genre != null ? genre.toString() : "null");
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

    // remove movieset-sorttitle
    if (oldValue != null && newValue == null) {
      setSortTitle("");
    }

    firePropertyChange(MOVIESET, oldValue, newValue);
  }

  /**
   * Removes the from movie set.
   */
  public void removeFromMovieSet() {
    if (movieSet != null) {
      movieSet.removeMovie(this);
    }
    setMovieSet(null);
    setSortTitle("");
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
   * 
   * @return the media info video format
   */
  public String getMediaInfoVideoFormat() {
    if (mediaFiles.size() > 0) {
      MediaFile mediaFile = mediaFiles.get(0);
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
    if (mediaFiles.size() > 0) {
      MediaFile mediaFile = mediaFiles.get(0);
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
    if (mediaFiles.size() > 0) {
      MediaFile mediaFile = mediaFiles.get(0);
      return mediaFile.getAudioCodec() + "_" + mediaFile.getAudioChannels();
    }

    return "";
  }

  /**
   * Sets the spoken languages.
   * 
   * @param newValue
   *          the new spoken languages
   */
  public void setSpokenLanguages(String newValue) {
    String oldValue = this.spokenLanguages;
    this.spokenLanguages = newValue;
    firePropertyChange(SPOKEN_LANGUAGES, oldValue, newValue);
  }

  /**
   * Gets the spoken languages.
   * 
   * @return the spoken languages
   */
  public String getSpokenLanguages() {
    return this.spokenLanguages;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#getBanner()
   */
  @Override
  public String getBanner() {
    // TODO implement banners for movies
    return "";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.MediaEntity#setBanner(java.lang.String)
   */
  @Override
  public void setBanner(String banner) {
    // TODO implement banners for movies
  }

}
