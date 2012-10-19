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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.movie.MovieCast.CastType;
import org.tinymediamanager.scraper.CastMember;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaArt;
import org.tinymediamanager.scraper.MediaArtifactType;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaMetadata.Genres;
import org.tinymediamanager.scraper.util.CachedUrl;

/**
 * The Class Movie.
 */
@Entity
public class Movie extends AbstractModelObject {

  /** The Constant NFO_FILE. */
  protected final static String    NFO_FILE           = "movie.nfo";

  /** The Constant TITLE. */
  protected final static String    TITLE              = "title";

  /** The Constant ORIGINAL_TITLE. */
  protected final static String    ORIGINAL_TITLE     = "originaltitle";

  /** The Constant RATING. */
  protected final static String    RATING             = "rating";

  /** The Constant YEAR. */
  protected final static String    YEAR               = "year";

  /** The Constant OUTLINE. */
  protected final static String    OUTLINE            = "outline";

  /** The Constant PLOT. */
  protected final static String    PLOT               = "plot";

  /** The Constant TAGLINE. */
  protected final static String    TAGLINE            = "tagline";

  /** The Constant RUNTIME. */
  protected final static String    RUNTIME            = "runtime";

  /** The Constant THUMB. */
  protected final static String    THUMB              = "thumb";

  /** The Constant THUMB_PATH. */
  protected final static String    THUMB_PATH         = "thumbpath";

  /** The Constant ID. */
  protected final static String    ID                 = "id";

  /** The Constant IMDB_ID. */
  protected final static String    IMDB_ID            = "imdbid";

  /** The Constant FILENAME_AND_PATH. */
  protected final static String    FILENAME_AND_PATH  = "filenameandpath";

  /** The Constant PATH. */
  protected final static String    PATH               = "path";

  /** The Constant DIRECTOR. */
  protected final static String    DIRECTOR           = "director";

  /** The Constant WRITER. */
  protected final static String    WRITER             = "writer";

  /** The Constant ACTOR. */
  protected final static String    ACTOR              = "actor";

  /** The Constant Production Company. */
  protected final static String    PRODUCTION_COMPANY = "productionCompany";

  /** The Constant NAME. */
  protected final static String    NAME               = "name";

  /** The Constant ROLE. */
  protected final static String    ROLE               = "role";

  /** The Constant GENRE. */
  protected final static String    GENRE              = "genre";

  /** The Constant CERTIFICATION. */
  protected final static String    CERTIFICATIONS     = "certifications";

  /** The Constant logger. */
  @XmlTransient
  private static final Logger      LOGGER             = Logger.getLogger(Movie.class);

  /** The id. */
  @Id
  @GeneratedValue
  private Long                     id;

  /** The name. */
  private String                   name;

  /** The original name. */
  private String                   originalName;

  /** The year. */
  private String                   year;

  /** The imdb id. */
  private String                   imdbId;

  /** The tmdb id. */
  private int                      tmdbId;

  /** The overview. */
  private String                   overview;

  /** The tagline. */
  private String                   tagline;

  /** The rating. */
  private float                    rating;

  /** The runtime. */
  private int                      runtime;

  /** The fanart url. */
  private String                   fanartUrl;

  /** The fanart. */
  private String                   fanart;

  /** The poster url. */
  private String                   posterUrl;

  /** The poster. */
  private String                   poster;

  /** The path. */
  private String                   path;

  /** The nfo filename. */
  private String                   nfoFilename;

  /** The director. */
  private String                   director;

  /** The writer. */
  private String                   writer;

  /** The production company. */
  private String                   productionCompany;

  /** The certification. */
  // private String certification;

  /** The scraped. */
  private boolean                  scraped;

  /** The movie files. */
  private List<String>             movieFiles         = new ArrayList<String>();

  /** The genres. */
  private List<Genres>             genres             = new ArrayList<Genres>();

  /** The cast. */
  @OneToMany(cascade = CascadeType.ALL)
  private List<MovieCast>          cast               = new ArrayList<MovieCast>();

  /** The cast observable. */
  @Transient
  private List<MovieCast>          castObservable     = ObservableCollections.observableList(cast);

  /** the certifications. */
  @OneToMany(cascade = CascadeType.ALL)
  private List<MovieCertification> certifications     = new ArrayList<MovieCertification>();

  /**
   * Instantiates a new movie.
   */
  public Movie() {
    name = new String();
    originalName = new String();
    year = new String();
    imdbId = new String();
    overview = new String();
    tagline = new String();
    fanartUrl = new String();
    fanart = new String();
    posterUrl = new String();
    poster = new String();
    path = new String();
    nfoFilename = new String();
    setDirector(new String());
    setWriter(new String());
    tmdbId = 0;
    setScraped(false);
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
  public void setObservableCastList() {
    castObservable = ObservableCollections.observableList(cast);
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
   * Adds the to files.
   * 
   * @param newFile
   *          the new file
   */
  public void addToFiles(String newFile) {
    movieFiles.add(newFile);
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
   * Find images.
   */
  private void findImages() {
    // try to find images in movie path

    // poster - movie.tbn
    String poster = path + File.separator + "movie.tbn";
    File imageFile = new File(poster);
    if (imageFile.exists()) {
      LOGGER.debug("found poster " + imageFile.getPath());
      setPoster(poster);
    } else {
      LOGGER.debug("no poster found");
    }

    // fanart - fanart.jpg
    String fanart = path + File.separator + "fanart.jpg";
    imageFile = new File(fanart);
    if (imageFile.exists()) {
      LOGGER.debug("found fanart " + imageFile.getPath());
      setFanart(fanart);
    } else {
      LOGGER.debug("no fanart found");
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
    return fanart;
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
    firePropertyChange("tmdbid", oldValue, newValue);
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
    return poster;
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
   * @return the movie
   */
  public static Movie parseNFO(String path) {
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
      movie = MovieToXbmcNfoConnector.getData(file.getPath());
      if (movie == null) {
        LOGGER.debug("did not find movie informations in nfo");
        continue;
      }

      movie.setPath(path);
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
    setTmdbId(Integer.parseInt(metadata.getTMDBID()));
    setYear(metadata.getYear());
    setRating(metadata.getRating());
    setRuntime(Integer.parseInt(metadata.getRuntime()));
    setTagline(metadata.getTagline());
    setProductionCompany(metadata.getCompany());

    // certifications
    certifications.clear();
    for (Certification certification : metadata.getCertifications()) {
      MovieCertification cert = new MovieCertification(certification.getCountry(), certification.getCertification());
      certifications.add(cert);
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
    castObservable.clear();
    List<CastMember> cast = metadata.getCastMembers();
    String director = new String();
    String writer = new String();
    for (CastMember member : cast) {
      MovieCast castMember = new MovieCast();
      castMember.setName(member.getName());
      castMember.setCharacter(member.getCharacter());
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
    genres.clear();
    for (Genres genre : metadata.getGenres()) {
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
    byte tmp_buffer[] = new byte[4096];
    int n;
    FileOutputStream outputStream = null;
    InputStream is = null;
    CachedUrl url = null;
    String filename = null;
    String oldFilename = null;

    // poster
    if (poster && !StringUtils.isEmpty(getPosterUrl())) {
      try {
        oldFilename = getPoster();
        setPoster("");
        url = new CachedUrl(getPosterUrl());
        filename = this.path + File.separator + "movie.tbn";
        outputStream = new FileOutputStream(filename);
        is = url.getInputStream();
        IOUtils.copy(is, outputStream);
        // while ((n = is.read(tmp_buffer)) > 0) {
        // outputStream.write(tmp_buffer, 0, n);
        // outputStream.flush();
        // }
        outputStream.close();
        is.close();
        setPoster(filename);
      } catch (IOException e) {
        LOGGER.error("writeImages - poster", e);
        setPoster(oldFilename);
      }
    }

    // fanart
    if (fanart && !StringUtils.isEmpty(getFanartUrl())) {
      try {
        oldFilename = getFanart();
        setFanart("");
        url = new CachedUrl(getFanartUrl());
        filename = this.path + File.separator + "fanart.jpg";
        outputStream = new FileOutputStream(filename);
        is = url.getInputStream();
        IOUtils.copy(is, outputStream);
        // while ((n = is.read(tmp_buffer)) > 0) {
        // outputStream.write(tmp_buffer, 0, n);
        // outputStream.flush();
        // }
        outputStream.close();
        is.close();
        setFanart(filename);
      } catch (IOException e) {
        LOGGER.error("writeImages - fanart", e);
        setFanart(oldFilename);
      }
    }
  }

  /**
   * Write nfo.
   */
  public void writeNFO() {
    setNfoFilename(MovieToXbmcNfoConnector.setData(this));
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
  public void saveToDb() {
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
  public List<Genres> getGenres() {
    return genres;
  }

  /**
   * Adds the genre.
   * 
   * @param newValue
   *          the new value
   */
  public void addGenre(Genres newValue) {
    genres.add(newValue);
    firePropertyChange(GENRE, null, newValue);
  }

  /**
   * Removes the genre.
   * 
   * @param genre
   *          the genre
   */
  public void removeGenre(Genres genre) {
    genres.remove(genre);
    firePropertyChange(GENRE, null, genre);
  }

  /**
   * Removes the all genres.
   */
  public void removeAllGenres() {
    genres.clear();
    firePropertyChange(GENRE, null, genres);
  }

  /**
   * Gets the certifications.
   * 
   * @return the certifications
   */
  public List<MovieCertification> getCertifications() {
    return certifications;
  }

  /**
   * Adds the certification.
   * 
   * @param newValue
   *          the new value
   */
  public void addCertification(MovieCertification newValue) {
    this.certifications.add(newValue);
    firePropertyChange(CERTIFICATIONS, null, newValue);
  }

  /**
   * Sets the certifications.
   * 
   * @param newValue
   *          the new certifications
   */
  public void setCertifications(List<MovieCertification> newValue) {
    this.certifications = newValue;
    firePropertyChange(CERTIFICATIONS, null, newValue);
  }

  /**
   * Removes the all certifications.
   */
  public void removeAllCertifications() {
    certifications.clear();
    firePropertyChange(CERTIFICATIONS, null, certifications);
  }

}
