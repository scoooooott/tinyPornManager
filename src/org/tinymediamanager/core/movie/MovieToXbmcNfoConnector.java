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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.MovieToXbmcNfoConnector.Actor;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaTrailer;

/**
 * The Class MovieToXbmcNfoConnector.
 */
@XmlRootElement(name = "movie")
@XmlSeeAlso(Actor.class)
@XmlType(propOrder = { "title", "originaltitle", "set", "sorttitle", "rating", "year", "votes", "outline", "plot", "tagline", "runtime", "thumb",
    "mpaa", "certifications", "id", "tmdbId", "filenameandpath", "trailer", "fileinfo", "watched", "playcount", "genres", "studio", "credits",
    "director", "tags", "actors" })
public class MovieToXbmcNfoConnector {

  /** The Constant logger. */
  private static final Logger LOGGER = Logger.getLogger(MovieToXbmcNfoConnector.class);

  /** The title. */
  private String              title;

  /** The originaltitle. */
  private String              originaltitle;

  /** The rating. */
  private float               rating;

  /** The votes. */
  private int                 votes;

  /** The year. */
  private String              year;

  /** The outline. */
  private String              outline;

  /** The plot. */
  private String              plot;

  /** The tagline. */
  private String              tagline;

  /** The runtime. */
  private String              runtime;

  /** The thumb. */
  private String              thumb;

  /** The id. */
  private String              id;

  /** The tmdbid. */
  private int                 tmdbId;

  /** The filenameandpath. */
  private String              filenameandpath;

  /** The director. */
  @XmlElement(name = "director")
  private List<String>        director;

  /** The sudio. */
  private String              studio;

  /** The actors. */
  @XmlAnyElement(lax = true)
  private List<Object>        actors;

  /** The genres. */
  @XmlElement(name = "genre")
  private List<String>        genres;

  /** The mpaa certification. */
  private String              mpaa;

  /** The certifications. */
  private String              certifications;

  /** the credits. */
  @XmlElement(name = "credits")
  private List<String>        credits;

  /** The watched. */
  private boolean             watched;

  /** The playcount. */
  private int                 playcount;

  /** The trailer. */
  private String              trailer;

  /** The tags. */
  @XmlElement(name = "tag")
  private List<String>        tags;

  /** The set. */
  private String              set;

  /** The sorttitle. */
  private String              sorttitle;

  // private Fileinfo fileinfo;

  /**
   * Instantiates a new movie to xbmc nfo connector.
   */
  public MovieToXbmcNfoConnector() {
    actors = new ArrayList();
    genres = new ArrayList<String>();
    tags = new ArrayList<String>();
    director = new ArrayList<String>();
    credits = new ArrayList<String>();
  }

  /**
   * Sets the data.
   * 
   * @param movie
   *          the movie
   * @return the string
   */
  public static String setData(Movie movie) {
    MovieToXbmcNfoConnector xbmc = new MovieToXbmcNfoConnector();
    // set data
    xbmc.setTitle(movie.getName());
    xbmc.setOriginaltitle(movie.getOriginalName());
    xbmc.setRating(movie.getRating());
    xbmc.setVotes(movie.getVotes());
    xbmc.setYear(movie.getYear());
    xbmc.setPlot(movie.getOverview());

    // outline is only the first 200 characters of the plot
    int spaceIndex = 0;
    if (!StringUtils.isEmpty(xbmc.getPlot()) && xbmc.getPlot().length() > 200) {
      spaceIndex = xbmc.getPlot().indexOf(" ", 200);
      if (spaceIndex > 0) {
        xbmc.setOutline(xbmc.getPlot().substring(0, spaceIndex));
      }
      else {
        xbmc.setOutline(xbmc.getPlot());
      }
    }
    else if (!StringUtils.isEmpty(xbmc.getPlot())) {
      spaceIndex = xbmc.getPlot().length();
      xbmc.setOutline(xbmc.getPlot().substring(0, spaceIndex));
    }

    xbmc.setTagline(movie.getTagline());
    xbmc.setRuntime(String.valueOf(movie.getRuntime()));
    xbmc.setThumb(movie.getPoster());
    xbmc.setId(movie.getImdbId());
    xbmc.setTmdbId(movie.getTmdbId());

    // only write first studio
    if (StringUtils.isNotEmpty(movie.getProductionCompany())) {
      String[] studio = movie.getProductionCompany().split(", ");
      if (studio.length > 0) {
        xbmc.setStudio(studio[0]);
      }
    }

    xbmc.setWatched(movie.isWatched());
    if (xbmc.isWatched()) {
      xbmc.setPlaycount(1);
    }

    // certifications
    if (movie.getCertification() != null) {
      xbmc.setMpaa(movie.getCertification().toString());
      xbmc.setCertifications(movie.getCertification().toString());
    }

    // filename and path
    if (movie.getMediaFiles().size() > 0) {
      xbmc.setFilenameandpath(movie.getPath() + File.separator + movie.getMediaFiles().get(0).getFilename());
    }

    // support of frodo director tags
    if (StringUtils.isNotEmpty(movie.getDirector())) {
      String directors[] = movie.getDirector().split(", ");
      for (String director : directors) {
        xbmc.addDirector(director);
      }
    }

    // support of frodo credits tags
    if (StringUtils.isNotEmpty(movie.getWriter())) {
      String writers[] = movie.getWriter().split(", ");
      for (String writer : writers) {
        xbmc.addCredits(writer);
      }
    }

    for (MovieCast cast : movie.getActors()) {
      xbmc.addActor(cast.getName(), cast.getCharacter(), cast.getThumb());
    }

    for (MediaGenres genre : movie.getGenres()) {
      xbmc.addGenre(genre.toString());
    }

    for (MediaTrailer trailer : movie.getTrailers()) {
      if (trailer.getInNfo()) {
        xbmc.setTrailer(trailer.getUrl());
        break;
      }
    }

    for (String tag : movie.getTags()) {
      xbmc.addTag(tag);
    }

    // movie set
    if (movie.getMovieSet() != null) {
      MovieSet movieSet = movie.getMovieSet();
      xbmc.setSet(movieSet.getName());
      // xbmc.setSorttitle(movieSet.getName() + (movieSet.getMovieIndex(movie) +
      // 1));
    }

    xbmc.setSorttitle(movie.getSortTitle());

    // // fileinfo
    // for (MediaFile mediaFile : movie.getMediaFiles()) {
    // if (StringUtils.isEmpty(mediaFile.getVideoCodec())) {
    // break;
    // }
    //
    // if (xbmc.getFileinfo() == null) {
    // Fileinfo info = new Fileinfo();
    // info.streamdetails.video.codec = mediaFile.getVideoCodec();
    // info.streamdetails.video.aspect =
    // String.valueOf(mediaFile.getAspectRatio());
    // info.streamdetails.video.width = mediaFile.getVideoWidth();
    // info.streamdetails.video.height = mediaFile.getVideoHeight();
    // info.streamdetails.video.durationinseconds = mediaFile.getDuration();
    //
    // Audio audio = new Audio();
    // audio.codec = mediaFile.getAudioCodec();
    // audio.language = "";
    // audio.channels = mediaFile.getAudioChannels();
    // info.streamdetails.audio.add(audio);
    // xbmc.setFileinfo(info);
    // }
    // }

    // and marshall it
    String nfoFilename = "";
    for (MovieNfoNaming name : Globals.settings.getMovieNfoFilenames()) {
      JAXBContext context;
      try {
        nfoFilename = movie.getNfoFilename(name);
        synchronized (JAXBContext.class) {
          context = JAXBContext.newInstance(MovieToXbmcNfoConnector.class, Actor.class);
        }
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        // w = new FileWriter(nfoFilename);
        Writer w = new StringWriter();
        m.marshal(xbmc, w);
        StringBuilder sb = new StringBuilder(w.toString());
        w.close();

        // on windows make windows conform linebreaks
        if (SystemUtils.IS_OS_WINDOWS) {
          sb = new StringBuilder(sb.toString().replaceAll("(?<!\r)\n", "\r\n"));
        }
        FileUtils.write(new File(nfoFilename), sb, "UTF-8");
      }
      catch (JAXBException e) {
        LOGGER.error("setData", e);
      }
      catch (IOException e) {
        LOGGER.error("setData", e);
      }
    }

    // return only the name w/o path
    return FilenameUtils.getName(nfoFilename);

  }

  /**
   * Gets the data.
   * 
   * @param nfoFilename
   *          the nfo filename
   * @return the data
   */
  public static Movie getData(String nfoFilename) {
    // try to parse XML
    JAXBContext context;
    Movie movie = null;
    try {
      synchronized (JAXBContext.class) {
        context = JAXBContext.newInstance(MovieToXbmcNfoConnector.class, Actor.class);
      }
      Unmarshaller um = context.createUnmarshaller();
      Reader in = new InputStreamReader(new FileInputStream(nfoFilename), "UTF-8");
      MovieToXbmcNfoConnector xbmc = (MovieToXbmcNfoConnector) um.unmarshal(in);
      movie = new Movie();
      movie.setName(xbmc.getTitle());
      movie.setOriginalName(xbmc.getOriginaltitle());
      movie.setRating(xbmc.getRating());
      movie.setVotes(xbmc.getVotes());
      movie.setYear(xbmc.getYear());
      movie.setOverview(xbmc.getPlot());
      movie.setTagline(xbmc.getTagline());
      try {
        String rt = xbmc.getRuntime().replaceAll("[^0-9]", "");
        movie.setRuntime(Integer.parseInt(rt));
      }
      catch (Exception e) {
        LOGGER.warn("could not parse runtime: " + xbmc.getRuntime());
      }
      if (StringUtils.isNotEmpty(xbmc.getThumb()) && xbmc.getThumb().contains("http://")) {
        movie.setPosterUrl(xbmc.getThumb());
      }
      else {
        movie.setPoster(xbmc.getThumb());
      }

      movie.setImdbId(xbmc.getId());
      movie.setTmdbId(xbmc.getTmdbId());

      // convert director to internal format
      String director = "";
      for (String dir : xbmc.getDirector()) {
        if (!StringUtils.isEmpty(director)) {
          director += ", ";
        }
        director += dir;
      }
      movie.setDirector(director);

      // convert writer to internal format
      String writer = "";
      for (String wri : xbmc.getCredits()) {
        if (StringUtils.isNotEmpty(writer)) {
          writer += ", ";
        }
        writer += wri;
      }
      movie.setWriter(writer);

      movie.setProductionCompany(xbmc.getStudio());
      if (!StringUtils.isEmpty(xbmc.getCertifications())) {
        movie.setCertification(Certification.parseCertificationStringForSetupCountry(xbmc.getCertifications()));
      }
      if (!StringUtils.isEmpty(xbmc.getMpaa()) && movie.getCertification() == Certification.NOT_RATED) {
        movie.setCertification(Certification.parseCertificationStringForSetupCountry(xbmc.getMpaa()));
      }
      movie.setWatched(xbmc.isWatched());

      // movieset
      if (StringUtils.isNotEmpty(xbmc.getSet())) {
        // search for that movieset
        MovieList movieList = MovieList.getInstance();
        MovieSet movieSet = movieList.findMovieSet(xbmc.getSet());
        // no one found - create it
        if (movieSet == null) {
          movieSet = new MovieSet(xbmc.getSet());
          movieSet.saveToDb();
          movieList.addMovieSet(movieSet);
          movieList.getMovieSetTreeModel().addMovieSet(movieSet);
        }

        // add movie to movieset
        if (movieSet != null) {
          movie.setMovieSet(movieSet);
        }
      }

      movie.setSortTitle(xbmc.getSorttitle());

      for (Actor actor : xbmc.getActors()) {
        MovieCast cast = new MovieCast(actor.getName(), actor.getRole());
        cast.setThumb(actor.getThumb());
        movie.addToCast(cast);
      }

      for (String genre : xbmc.getGenres()) {
        String[] genres = genre.split("/");
        for (String g : genres) {
          MediaGenres genreFound = MediaGenres.getGenre(g.trim());
          if (genreFound != null) {
            movie.addGenre(genreFound);
          }
        }
      }

      if (StringUtils.isNotEmpty(xbmc.getTrailer())) {
        MediaTrailer trailer = new MediaTrailer();
        trailer.setName("fromNFO");
        trailer.setProvider("from NFO");
        trailer.setQuality("unknown");
        trailer.setUrl(xbmc.getTrailer());
        trailer.setInNfo(true);
        movie.addTrailer(trailer);
      }

      for (String tag : xbmc.getTags()) {
        movie.addToTags(tag);
      }

      // set only the name w/o path
      movie.setNfoFilename(FilenameUtils.getName(nfoFilename));
    }
    catch (FileNotFoundException e) {
      LOGGER.error("setData", e);
      return null;
    }

    catch (Exception e) {
      LOGGER.error("setData", e);
      return null;
    }

    // only return if a movie name has been found
    if (StringUtils.isEmpty(movie.getName())) {
      return null;
    }

    return movie;
  }

  /**
   * Adds the genre.
   * 
   * @param genre
   *          the genre
   */
  public void addGenre(String genre) {
    genres.add(genre);
  }

  /**
   * Gets the genres.
   * 
   * @return the genres
   */
  public List<String> getGenres() {
    return this.genres;
  }

  /**
   * Adds the tag.
   * 
   * @param tag
   *          the tag
   */
  public void addTag(String tag) {
    tags.add(tag);
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
   * Adds the actor.
   * 
   * @param name
   *          the name
   * @param role
   *          the role
   * @param thumb
   *          the thumb
   */
  public void addActor(String name, String role, String thumb) {
    Actor actor = new Actor(name, role, thumb);
    actors.add(actor);
  }

  /**
   * Gets the actors.
   * 
   * @return the actors
   */
  public List<Actor> getActors() {
    // @XmlAnyElement(lax = true) causes all unsupported tags to be in actors;
    // filter Actors out
    List<Actor> pureActors = new ArrayList<Actor>();
    for (Object obj : actors) {
      if (obj instanceof Actor) {
        Actor actor = (Actor) obj;
        pureActors.add(actor);
      }
    }
    return pureActors;
  }

  /**
   * Gets the title.
   * 
   * @return the title
   */
  @XmlElement(name = "title")
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title.
   * 
   * @param title
   *          the new title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Gets the originaltitle.
   * 
   * @return the originaltitle
   */
  @XmlElement(name = "originaltitle")
  public String getOriginaltitle() {
    return originaltitle;
  }

  /**
   * Sets the originaltitle.
   * 
   * @param originaltitle
   *          the new originaltitle
   */
  public void setOriginaltitle(String originaltitle) {
    this.originaltitle = originaltitle;
  }

  /**
   * Gets the rating.
   * 
   * @return the rating
   */
  @XmlElement(name = "rating")
  public float getRating() {
    return rating;
  }

  /**
   * Sets the rating.
   * 
   * @param rating
   *          the new rating
   */
  public void setRating(float rating) {
    this.rating = rating;
  }

  /**
   * Gets the votes.
   * 
   * @return the votes
   */
  @XmlElement(name = "votes")
  public int getVotes() {
    return votes;
  }

  /**
   * Sets the votes.
   * 
   * @param votes
   *          the new votes
   */
  public void setVotes(int votes) {
    this.votes = votes;
  }

  /**
   * Gets the year.
   * 
   * @return the year
   */
  @XmlElement(name = "year")
  public String getYear() {
    return year;
  }

  /**
   * Sets the year.
   * 
   * @param year
   *          the new year
   */
  public void setYear(String year) {
    this.year = year;
  }

  /**
   * Gets the outline.
   * 
   * @return the outline
   */
  @XmlElement(name = "outline")
  public String getOutline() {
    return outline;
  }

  /**
   * Sets the outline.
   * 
   * @param outline
   *          the new outline
   */
  public void setOutline(String outline) {
    this.outline = outline;
  }

  /**
   * Gets the plot.
   * 
   * @return the plot
   */
  @XmlElement(name = "plot")
  public String getPlot() {
    return plot;
  }

  /**
   * Sets the plot.
   * 
   * @param plot
   *          the new plot
   */
  public void setPlot(String plot) {
    this.plot = plot;
  }

  /**
   * Gets the tagline.
   * 
   * @return the tagline
   */
  @XmlElement(name = "tagline")
  public String getTagline() {
    return tagline;
  }

  /**
   * Sets the tagline.
   * 
   * @param tagline
   *          the new tagline
   */
  public void setTagline(String tagline) {
    this.tagline = tagline;
  }

  /**
   * Gets the runtime.
   * 
   * @return the runtime
   */
  @XmlElement(name = "runtime")
  public String getRuntime() {
    return runtime;
  }

  /**
   * Sets the runtime.
   * 
   * @param runtime
   *          the new runtime
   */
  public void setRuntime(String runtime) {
    this.runtime = runtime;
  }

  /**
   * Gets the thumb.
   * 
   * @return the thumb
   */
  @XmlElement(name = "thumb")
  public String getThumb() {
    return thumb;
  }

  /**
   * Sets the thumb.
   * 
   * @param thumb
   *          the new thumb
   */
  public void setThumb(String thumb) {
    this.thumb = thumb;
  }

  /**
   * Gets the id.
   * 
   * @return the id
   */
  @XmlElement(name = "id")
  public String getId() {
    return id;
  }

  /**
   * Sets the id.
   * 
   * @param id
   *          the new id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the filenameandpath.
   * 
   * @return the filenameandpath
   */
  @XmlElement(name = "filenameandpath")
  public String getFilenameandpath() {
    return filenameandpath;
  }

  /**
   * Sets the filenameandpath.
   * 
   * @param filenameandpath
   *          the new filenameandpath
   */
  public void setFilenameandpath(String filenameandpath) {
    this.filenameandpath = filenameandpath;
  }

  /**
   * Gets the director.
   * 
   * @return the director
   */
  public List<String> getDirector() {
    return director;
  }

  /**
   * Sets the director.
   * 
   * @param director
   *          the new director
   */
  public void addDirector(String director) {
    this.director.add(director);
  }

  /**
   * Gets the studio.
   * 
   * @return the studio
   */
  @XmlElement(name = "studio")
  public String getStudio() {
    return studio;
  }

  /**
   * Sets the studio.
   * 
   * @param studio
   *          the new studio
   */
  public void setStudio(String studio) {
    this.studio = studio;
  }

  /**
   * Gets the mpaa.
   * 
   * @return the mpaa
   */
  @XmlElement(name = "mpaa")
  public String getMpaa() {
    return mpaa;
  }

  /**
   * Sets the mpaa.
   * 
   * @param mpaa
   *          the new mpaa
   */
  public void setMpaa(String mpaa) {
    this.mpaa = mpaa;
  }

  /**
   * Gets the certifications.
   * 
   * @return the certifications
   */
  @XmlElement(name = "certification")
  public String getCertifications() {
    return certifications;
  }

  /**
   * Sets the certifications.
   * 
   * @param certifications
   *          the new certifications
   */
  public void setCertifications(String certifications) {
    this.certifications = certifications;
  }

  /**
   * Gets the credits.
   * 
   * @return the credits
   */
  public List<String> getCredits() {
    return credits;
  }

  /**
   * Sets the credits.
   * 
   * @param credits
   *          the new credits
   */
  public void addCredits(String credits) {
    this.credits.add(credits);
  }

  /**
   * Checks if is watched.
   * 
   * @return true, if is watched
   */
  @XmlElement(name = "watched")
  public boolean isWatched() {
    return watched;
  }

  /**
   * Sets the watched.
   * 
   * @param watched
   *          the new watched
   */
  public void setWatched(boolean watched) {
    this.watched = watched;
  }

  /**
   * Gets the playcount.
   * 
   * @return the playcount
   */
  @XmlElement(name = "playcount")
  public int getPlaycount() {
    return playcount;
  }

  /**
   * Sets the playcount.
   * 
   * @param playcount
   *          the new playcount
   */
  public void setPlaycount(int playcount) {
    this.playcount = playcount;
  }

  /**
   * Gets the tmdb id.
   * 
   * @return the tmdb id
   */
  @XmlElement(name = "tmdbid")
  public int getTmdbId() {
    return tmdbId;
  }

  /**
   * Sets the tmdb id.
   * 
   * @param tmdbId
   *          the new tmdb id
   */
  public void setTmdbId(int tmdbId) {
    this.tmdbId = tmdbId;
  }

  /**
   * Gets the trailer.
   * 
   * @return the trailer
   */
  @XmlElement(name = "trailer")
  public String getTrailer() {
    return trailer;
  }

  /**
   * Sets the trailer.
   * 
   * @param trailer
   *          the new trailer
   */
  public void setTrailer(String trailer) {
    this.trailer = trailer;
  }

  /**
   * Gets the sets the.
   * 
   * @return the sets the
   */
  @XmlElement(name = "set")
  public String getSet() {
    return set;
  }

  /**
   * Gets the sorttitle.
   * 
   * @return the sorttitle
   */
  @XmlElement(name = "sorttitle")
  public String getSorttitle() {
    return sorttitle;
  }

  /**
   * Sets the sets the.
   * 
   * @param set
   *          the new sets the
   */
  public void setSet(String set) {
    this.set = set;
  }

  /**
   * Sets the sorttitle.
   * 
   * @param sorttitle
   *          the new sorttitle
   */
  public void setSorttitle(String sorttitle) {
    this.sorttitle = sorttitle;
  }

  // public Fileinfo getFileinfo() {
  // return fileinfo;
  // }
  //
  // public void setFileinfo(Fileinfo fileinfo) {
  // this.fileinfo = fileinfo;
  // }

  // inner class actor to represent actors
  /**
   * The Class Actor.
   */
  @XmlRootElement(name = "actor")
  public static class Actor {

    /** The name. */
    private String name;

    /** The role. */
    private String role;

    /** The thumb. */
    private String thumb;

    /**
     * Instantiates a new actor.
     */
    public Actor() {
    }

    /**
     * Instantiates a new actor.
     * 
     * @param name
     *          the name
     * @param role
     *          the role
     * @param thumb
     *          the thumb
     */
    public Actor(String name, String role, String thumb) {
      this.name = name;
      this.role = role;
      this.thumb = thumb;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    @XmlElement(name = "name")
    public String getName() {
      return name;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *          the new name
     */
    public void setName(String name) {
      this.name = name;
    }

    /**
     * Gets the role.
     * 
     * @return the role
     */
    @XmlElement(name = "role")
    public String getRole() {
      return role;
    }

    /**
     * Sets the role.
     * 
     * @param role
     *          the new role
     */
    public void setRole(String role) {
      this.role = role;
    }

    /**
     * Gets the thumb.
     * 
     * @return the thumb
     */
    @XmlElement(name = "thumb")
    public String getThumb() {
      return thumb;
    }

    /**
     * Sets the thumb.
     * 
     * @param thumb
     *          the new thumb
     */
    public void setThumb(String thumb) {
      this.thumb = thumb;
    }

  }

  // static class Fileinfo {
  // @XmlElement
  // Streamdetails streamdetails;
  //
  // public Fileinfo() {
  // streamdetails = new Streamdetails();
  // }
  // }
  //
  // static class Streamdetails {
  // @XmlElement
  // Video video;
  // @XmlElement
  // List<Audio> audio;
  //
  // public Streamdetails() {
  // video = new Video();
  // audio = new ArrayList<Audio>();
  // }
  // }
  //
  // static class Video {
  // @XmlElement
  // String codec;
  // @XmlElement
  // String aspect;
  // @XmlElement
  // int width;
  // @XmlElement
  // int height;
  // @XmlElement
  // int durationinseconds;
  // }
  //
  // static class Audio {
  // @XmlElement
  // String codec;
  // @XmlElement
  // String language;
  // @XmlElement
  // String channels;
  // }
}
