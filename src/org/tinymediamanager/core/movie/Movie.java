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

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.movie.MovieCast.CastType;
import org.tinymediamanager.scraper.CastMember;
import org.tinymediamanager.scraper.MediaArt;
import org.tinymediamanager.scraper.MediaArtifactType;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.util.CachedUrl;

@Entity
public class Movie extends AbstractModelObject {

  protected final static String NFO_FILE = "movie.nfo";
  protected final static String TITLE = "title";
  protected final static String ORIGINAL_TITLE = "originaltitle";
  protected final static String RATING = "rating";
  protected final static String YEAR = "year";
  protected final static String OUTLINE = "outline";
  protected final static String PLOT = "plot";
  protected final static String TAGLINE = "tagline";
  protected final static String RUNTIME = "runtime";
  protected final static String THUMB = "thumb";
  protected final static String THUMB_PATH = "thumbpath";
  protected final static String ID = "id";
  protected final static String IMDB_ID = "imdbid";
  protected final static String FILENAME_AND_PATH = "filenameandpath";
  protected final static String PATH = "path";
  protected final static String DIRECTOR = "director";
  protected final static String ACTOR = "actor";
  protected final static String NAME = "name";
  protected final static String ROLE = "role";

  @Id
  @GeneratedValue
  private Long id;
  private String name;
  private String originalName;
  private String year;
  private String imdbId;
  private int tmdbId;
  private String overview;
  private String tagline;
  private float rating;
  private int runtime;
  private String fanartUrl;
  private String fanart;
  private String posterUrl;
  private String poster;
  private String path;
  private String nfoFilename;
  private String director;
  private String writer;

  @Transient
  private boolean scraped;

  private List<String> movieFiles = new ArrayList<String>();

  @OneToMany(cascade = CascadeType.ALL)
  private List<MovieCast> cast = new ArrayList<MovieCast>();

  @Transient
  private List<MovieCast> castObservable = ObservableCollections.observableList(cast);

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

  public String getNfoFilename() {
    return nfoFilename;
  }

  public Boolean getHasNfoFile() {
    if (!StringUtils.isEmpty(nfoFilename)) {
      return true;
    }
    return false;
  }

  public Boolean getHasImages() {
    if (!StringUtils.isEmpty(poster)) {
      return true;
    }
    return false;
  }

  public void setNfoFilename(String newValue) {
    String oldValue = this.nfoFilename;
    this.nfoFilename = newValue;
    firePropertyChange("nfoFilename", oldValue, newValue);
    firePropertyChange("hasNfoFile", false, true);
  }

  public String getNameForUi() {
    String nameForUi = new String(name);
    if (year != null && !year.isEmpty()) {
      nameForUi += " (" + year + ")";
    }
    return nameForUi;
  }

  public void setObservableCastList() {
    castObservable = ObservableCollections.observableList(cast);
  }

  public void addToCast(MovieCast obj) {
    castObservable.add(obj);
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

  public void addToFiles(String newFile) {
    movieFiles.add(newFile);
  }

  public List<String> getMovieFiles() {
    return this.movieFiles;
  }

  private void findImages() {
    // try to find images in movie path

    // poster - movie.tbn
    String poster = path + File.separator + "movie.tbn";
    File imageFile = new File(poster);
    if (imageFile.exists()) {
      setPoster(poster);
    }

    // fanart - fanart.jpg
    String fanart = path + File.separator + "fanart.jpg";
    imageFile = new File(fanart);
    if (imageFile.exists()) {
      setFanart(fanart);
    }
  }

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

  public List<MovieCast> getCast() {
    return this.castObservable;
  }

  public String getDirector() {
    return director;
  }

  public String getFanart() {
    return fanart;
  }

  public String getFanartUrl() {
    return fanartUrl;
  }

  public Long getId() {
    return id;
  }

  public String getImdbId() {
    return imdbId;
  }

  public int getTmdbId() {
    return tmdbId;
  }

  public void setTmdbId(int newValue) {
    int oldValue = this.tmdbId;
    this.tmdbId = newValue;
    firePropertyChange("tmdbid", oldValue, newValue);
  }

  public String getName() {
    return name;
  }

  public String getOriginalName() {
    return originalName;
  }

  public String getOverview() {
    return overview;
  }

  public String getPath() {
    return path;
  }

  public String getPoster() {
    return poster;
  }

  public String getPosterUrl() {
    return posterUrl;
  }

  public float getRating() {
    return rating;
  }

  public int getRuntime() {
    return runtime;
  }

  public String getTagline() {
    return tagline;
  }

  public String getWriter() {
    return writer;
  }

  public String getYear() {
    return year;
  }

  public boolean hasFile(String filename) {
    for (String fileName : movieFiles) {
      if (fileName.compareTo(filename) == 0) {
        return true;
      }
    }
    return false;
  }

  // when loading from Database
  void onLoad() {
  }

  public static Movie parseNFO(String path) {
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
      movie = MovieToXbmcNfoConnector.getData(file.getPath());
      if (movie == null) {
        continue;
      }

      movie.setPath(path);
      movie.findImages();
      break;
    }

    return movie;
  }

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

  public void setFanart(String newValue) {
    String oldValue = this.fanart;
    this.fanart = newValue;
    firePropertyChange("fanart", oldValue, newValue);
  }

  public void setFanartUrl(String newValue) {
    String oldValue = fanartUrl;
    fanartUrl = newValue;
    firePropertyChange("fanartUrl", oldValue, newValue);
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setImdbId(String newValue) {
    String oldValue = imdbId;
    imdbId = newValue;
    firePropertyChange("imdbId", oldValue, newValue);
  }

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

    // write NFO and saving images
    writeNFO();
    writeImages(true, true);

    // update DB
    saveToDb();

  }

  public void removeAllActors() {
    castObservable.clear();
    firePropertyChange("cast", null, this.getCast());
    firePropertyChange("actors", null, this.getCast());
  }

  public void setName(String newValue) {
    String oldValue = name;
    name = newValue;
    firePropertyChange("name", oldValue, newValue);
    firePropertyChange("nameForUi", oldValue, newValue);
  }

  public void setOriginalName(String newValue) {
    String oldValue = originalName;
    originalName = newValue;
    firePropertyChange("originalName", oldValue, newValue);
  }

  public void setOverview(String newValue) {
    String oldValue = overview;
    overview = newValue;
    firePropertyChange("overview", oldValue, newValue);
  }

  public void setPath(String newValue) {
    String oldValue = path;
    path = newValue;
    firePropertyChange("path", oldValue, newValue);
  }

  public void setPoster(String newValue) {
    String oldValue = this.poster;
    this.poster = newValue;
    firePropertyChange("poster", oldValue, newValue);
  }

  public void setPosterUrl(String newValue) {
    String oldValue = posterUrl;
    posterUrl = newValue;
    firePropertyChange("posterUrl", oldValue, newValue);
  }

  public void setRating(float newValue) {
    float oldValue = rating;
    rating = newValue;
    firePropertyChange("rating", oldValue, newValue);
  }

  public void setRuntime(int newValue) {
    int oldValue = this.runtime;
    this.runtime = newValue;
    firePropertyChange("runtime", oldValue, newValue);
  }

  private void setScraped(boolean newValue) {
    this.scraped = newValue;

  }

  public void setTagline(String newValue) {
    String oldValue = this.tagline;
    this.tagline = newValue;
    firePropertyChange("tagline", oldValue, newValue);
  }

  public void setYear(String newValue) {
    String oldValue = year;
    year = newValue;
    firePropertyChange("year", oldValue, newValue);
    firePropertyChange("nameForUi", oldValue, newValue);
  }

  public void writeImages(boolean poster, boolean fanart) {
    byte tmp_buffer[] = new byte[4096];
    int n;
    FileOutputStream outputStream = null;
    InputStream is = null;
    CachedUrl url = null;
    String filename = null;

    // poster
    if (poster) {
      try {
        url = new CachedUrl(getPosterUrl());
        filename = this.path + File.separator + "movie.tbn";
        outputStream = new FileOutputStream(filename);
        is = url.getInputStream(null, true);
        while ((n = is.read(tmp_buffer)) > 0) {
          outputStream.write(tmp_buffer, 0, n);
          outputStream.flush();
        }
        outputStream.close();
        setPoster(filename);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    // fanart
    if (fanart) {
      try {
        url = new CachedUrl(getFanartUrl());
        filename = this.path + File.separator + "fanart.jpg";
        outputStream = new FileOutputStream(filename);
        is = url.getInputStream(null, true);
        while ((n = is.read(tmp_buffer)) > 0) {
          outputStream.write(tmp_buffer, 0, n);
          outputStream.flush();
        }
        outputStream.close();
        setFanart(filename);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public void writeNFO() {
    setNfoFilename(MovieToXbmcNfoConnector.setData(this));
  }

  public void setDirector(String newValue) {
    String oldValue = this.director;
    this.director = newValue;
    firePropertyChange("director", oldValue, newValue);
  }

  public void setWriter(String newValue) {
    String oldValue = this.writer;
    this.writer = newValue;
    firePropertyChange("writer", oldValue, newValue);
  }

  public void saveToDb() {
    // update DB
    Globals.entityManager.getTransaction().begin();
    Globals.entityManager.persist(this);
    Globals.entityManager.getTransaction().commit();
  }

}
