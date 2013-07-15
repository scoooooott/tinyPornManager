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
package org.tinymediamanager.core.movie.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieActor;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.MovieSet;
import org.tinymediamanager.core.movie.connector.MovieToXbmcNfoConnector.Actor;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaTrailer;

/**
 * The Class MovieToXbmcNfoConnector.
 * 
 * @author Manuel Laggner
 */
@XmlRootElement(name = "movie")
@XmlSeeAlso(Actor.class)
@XmlType(propOrder = { "title", "originaltitle", "set", "sorttitle", "rating", "epbookmark", "year", "top250", "votes", "outline", "plot", "tagline",
    "runtime", "thumb", "mpaa", "certifications", "id", "tmdbId", "trailer", "country", "premiered", "status", "code", "aired", "fileinfo",
    "watched", "playcount", "genres", "studio", "credits", "director", "tags", "actors", "resume", "lastplayed", "dateadded" })
public class MovieToXbmcNfoConnector {
  private static final Logger LOGGER         = LoggerFactory.getLogger(MovieToXbmcNfoConnector.class);
  private static JAXBContext  context        = initContext();

  private String              title          = "";
  private String              originaltitle  = "";
  private float               rating         = 0;
  private int                 votes          = 0;
  private String              year           = "";
  private String              outline        = "";
  private String              plot           = "";
  private String              tagline        = "";
  private String              runtime        = "";
  private String              thumb          = "";
  private String              id             = "";
  private int                 tmdbId         = 0;
  private String              studio         = "";
  private String              mpaa           = "";
  private String              certifications = "";
  private boolean             watched        = false;
  private int                 playcount      = 0;
  private String              trailer        = "";
  private String              set            = "";
  private String              sorttitle      = "";
  private Fileinfo            fileinfo;

  @XmlElement(name = "director")
  private List<String>        director;

  @XmlAnyElement(lax = true)
  private List<Object>        actors;

  @XmlElement(name = "genre")
  private List<String>        genres;

  @XmlElement(name = "credits")
  private List<String>        credits;

  @XmlElement(name = "tag")
  private List<String>        tags;

  // /** The filenameandpath. */
  // private String filenameandpath;

  /** not supported tags, but used to retrain in NFO. */
  @XmlElement
  String                      epbookmark;

  @XmlElement
  String                      top250;

  @XmlElement
  String                      lastplayed;

  @XmlElement
  String                      country;

  @XmlElement
  String                      status;

  @XmlElement
  String                      code;

  @XmlElement
  String                      aired;

  @XmlElement
  String                      premiered;

  @XmlElement
  Resume                      resume;

  @XmlElement
  String                      dateadded;

  private static JAXBContext initContext() {
    try {
      return JAXBContext.newInstance(MovieToXbmcNfoConnector.class, Actor.class);
    }
    catch (JAXBException e) {
      LOGGER.error(e.getMessage());
    }
    return null;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
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
    if (context == null) {
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.nfo.writeerror", new String[] { ":", "Context is null" }));
      return "";
    }

    MovieToXbmcNfoConnector xbmc = null;

    // load existing NFO if possible
    for (MovieNfoNaming name : Globals.settings.getMovieSettings().getMovieNfoFilenames()) {
      File file = new File(movie.getPath(), movie.getNfoFilename(name));
      if (file.exists()) {
        try {
          Unmarshaller um = context.createUnmarshaller();
          Reader in = new InputStreamReader(new FileInputStream(file), "UTF-8");
          xbmc = (MovieToXbmcNfoConnector) um.unmarshal(in);
        }
        catch (Exception e) {
          LOGGER.error("failed to parse " + movie.getNfoFilename(name), e);
        }
      }
      if (xbmc != null) {
        break;
      }
    }

    // create new
    if (xbmc == null) {
      xbmc = new MovieToXbmcNfoConnector();
    }

    // set data
    xbmc.setTitle(movie.getTitle());
    xbmc.setOriginaltitle(movie.getOriginalTitle());
    xbmc.setRating(movie.getRating());
    xbmc.setVotes(movie.getVotes());
    xbmc.setYear(movie.getYear());
    xbmc.setPlot(movie.getPlot());

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
    // xbmc.setThumb(movie.getPoster());
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
      xbmc.setMpaa(Certification.generateCertificationStringWithAlternateNames(movie.getCertification()));
      xbmc.setCertifications(Certification.generateCertificationStringWithAlternateNames(movie.getCertification()));
    }

    // // filename and path
    // if (movie.getMediaFiles().size() > 0) {
    // xbmc.setFilenameandpath(movie.getPath() + File.separator +
    // movie.getMediaFiles().get(0).getFilename());
    // }

    // support of frodo director tags
    xbmc.director.clear();
    if (StringUtils.isNotEmpty(movie.getDirector())) {
      String directors[] = movie.getDirector().split(", ");
      for (String director : directors) {
        xbmc.addDirector(director);
      }
    }

    // support of frodo credits tags
    xbmc.credits.clear();
    if (StringUtils.isNotEmpty(movie.getWriter())) {
      String writers[] = movie.getWriter().split(", ");
      for (String writer : writers) {
        xbmc.addCredits(writer);
      }
    }

    xbmc.actors.clear();
    for (MovieActor cast : movie.getActors()) {
      xbmc.addActor(cast.getName(), cast.getCharacter(), cast.getThumb());
    }

    xbmc.genres.clear();
    for (MediaGenres genre : movie.getGenres()) {
      xbmc.addGenre(genre.toString());
    }

    for (MediaTrailer trailer : movie.getTrailers()) {
      if (trailer.getInNfo()) {
        // parse trailer url for nfo
        xbmc.setTrailer(prepareTrailerForXbmc(trailer));
        break;
      }
    }

    xbmc.tags.clear();
    for (String tag : movie.getTags()) {
      xbmc.addTag(tag);
    }

    // movie set
    if (movie.getMovieSet() != null) {
      MovieSet movieSet = movie.getMovieSet();
      xbmc.setSet(movieSet.getTitle());
      // xbmc.setSorttitle(movieSet.getName() + (movieSet.getMovieIndex(movie) +
      // 1));
    }

    xbmc.setSorttitle(movie.getSortTitle());

    // fileinfo
    for (MediaFile mediaFile : movie.getMediaFiles(MediaFileType.VIDEO)) {
      if (StringUtils.isEmpty(mediaFile.getVideoCodec())) {
        break;
      }

      if (xbmc.getFileinfo() == null) {
        Fileinfo info = new Fileinfo();
        info.streamdetails.video.codec = mediaFile.getVideoCodec();
        info.streamdetails.video.aspect = String.valueOf(mediaFile.getAspectRatio());
        info.streamdetails.video.width = mediaFile.getVideoWidth();
        info.streamdetails.video.height = mediaFile.getVideoHeight();
        info.streamdetails.video.durationinseconds = mediaFile.getDuration();

        Audio audio = new Audio();
        audio.codec = mediaFile.getAudioCodec();
        audio.language = "";
        audio.channels = mediaFile.getAudioChannels();
        info.streamdetails.audio.add(audio);
        xbmc.setFileinfo(info);
      }
    }

    // and marshall it
    String nfoFilename = "";
    for (MovieNfoNaming name : Globals.settings.getMovieSettings().getMovieNfoFilenames()) {

      try {
        nfoFilename = movie.getNfoFilename(name);

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
        FileUtils.write(new File(movie.getPath(), nfoFilename), sb, "UTF-8");
      }
      catch (Exception e) {
        LOGGER.error("setData", e.getMessage());
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.nfo.writeerror", new String[] { ":",
            e.getLocalizedMessage() }));
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
    if (context == null) {
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, nfoFilename, "message.nfo.readerror"));
      return null;
    }

    // try to parse XML
    Movie movie = null;
    try {
      Unmarshaller um = context.createUnmarshaller();
      if (um == null) {
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, nfoFilename, "message.nfo.readerror"));
        return null;
      }

      Reader in = new InputStreamReader(new FileInputStream(nfoFilename), "UTF-8");
      MovieToXbmcNfoConnector xbmc = (MovieToXbmcNfoConnector) um.unmarshal(in);
      movie = new Movie();
      movie.setTitle(xbmc.getTitle());
      movie.setOriginalTitle(xbmc.getOriginaltitle());
      movie.setRating(xbmc.getRating());
      movie.setVotes(xbmc.getVotes());
      movie.setYear(xbmc.getYear());
      movie.setPlot(xbmc.getPlot());
      movie.setTagline(xbmc.getTagline());
      try {
        String rt = xbmc.getRuntime().replaceAll("[^0-9]", "");
        movie.setRuntime(Integer.parseInt(rt));
      }
      catch (Exception e) {
        LOGGER.warn("could not parse runtime: " + xbmc.getRuntime());
      }

      if (xbmc.getThumb() != null) {
        if (xbmc.getThumb().contains("http://")) {
          movie.setPosterUrl(xbmc.getThumb());
        }
        // else {
        // movie.setPoster(xbmc.getThumb());
        // }
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
        movie.setCertification(Certification.parseCertificationStringForMovieSetupCountry(xbmc.getCertifications()));
      }
      if (!StringUtils.isEmpty(xbmc.getMpaa()) && movie.getCertification() == Certification.NOT_RATED) {
        movie.setCertification(Certification.parseCertificationStringForMovieSetupCountry(xbmc.getMpaa()));
      }
      movie.setWatched(xbmc.isWatched());

      // movieset
      if (StringUtils.isNotEmpty(xbmc.getSet())) {
        // search for that movieset
        MovieList movieList = MovieList.getInstance();
        MovieSet movieSet = movieList.getMovieSet(xbmc.getSet());
        // // no one found - create it
        // if (movieSet == null) {
        // movieSet = new MovieSet(xbmc.getSet());
        // movieSet.saveToDb();
        // movieList.addMovieSet(movieSet);
        // }

        // add movie to movieset
        if (movieSet != null) {
          movie.setMovieSet(movieSet);
        }
      }

      // be aware of the sorttitle - set an empty string if nothing has been
      // found
      if (StringUtils.isEmpty(xbmc.getSorttitle())) {
        movie.setSortTitle("");
      }
      else {
        movie.setSortTitle(xbmc.getSorttitle());
      }

      for (Actor actor : xbmc.getActors()) {
        MovieActor cast = new MovieActor(actor.getName(), actor.getRole());
        cast.setThumb(actor.getThumb());
        movie.addActor(cast);
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

        trailer.setUrl(parseTrailerUrl(xbmc.getTrailer()));

        trailer.setInNfo(true);
        movie.addTrailer(trailer);
      }

      for (String tag : xbmc.getTags()) {
        movie.addToTags(tag);
      }

      // set only the name w/o path
      movie.setNfoFilename(FilenameUtils.getName(nfoFilename));
    }
    catch (UnmarshalException e) {
      LOGGER.error("getData " + e.getMessage());
      // MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, nfoFilename, "message.nfo.readerror"));
      return null;
    }
    catch (Exception e) {
      LOGGER.error("getData", e);
      // MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, nfoFilename, "message.nfo.readerror"));
      return null;
    }

    // only return if a movie name has been found
    if (StringUtils.isEmpty(movie.getTitle())) {
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

  // /**
  // * Gets the filenameandpath.
  // *
  // * @return the filenameandpath
  // */
  // @XmlElement(name = "filenameandpath")
  // public String getFilenameandpath() {
  // return filenameandpath;
  // }
  //
  // /**
  // * Sets the filenameandpath.
  // *
  // * @param filenameandpath
  // * the new filenameandpath
  // */
  // public void setFilenameandpath(String filenameandpath) {
  // this.filenameandpath = filenameandpath;
  // }

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

  /**
   * Gets the fileinfo.
   * 
   * @return the fileinfo
   */
  public Fileinfo getFileinfo() {
    return fileinfo;
  }

  public void setFileinfo(Fileinfo fileinfo) {
    this.fileinfo = fileinfo;
  }

  private static String prepareTrailerForXbmc(MediaTrailer trailer) {
    // youtube trailer are stored in a special notation: plugin://plugin.video.youtube/?action=play_video&videoid=<ID>
    // parse out the ID from the url and store it in the right notation
    Pattern pattern = Pattern.compile("https{0,1}://.*youtube..*/watch\\?v=(.*)$");
    Matcher matcher = pattern.matcher(trailer.getUrl());
    if (matcher.matches()) {
      return "plugin://plugin.video.youtube/?action=play_video&videoid=" + matcher.group(1);
    }

    // other urls are handled by the hd-trailers.net plugin
    pattern = Pattern.compile("https{0,1}://.*(apple.com|yahoo-redir|yahoo.com|youtube.com|moviefone.com|ign.com|hd-trailers.net|aol.com).*");
    matcher = pattern.matcher(trailer.getUrl());
    if (matcher.matches()) {
      try {
        return "plugin://plugin.video.hdtrailers_net/video/" + matcher.group(1) + "/" + URLEncoder.encode(trailer.getUrl(), "UTF-8");
      }
      catch (Exception e) {
        LOGGER.error("failed to escape " + trailer.getUrl());
      }
    }
    // everything else is stored directly
    return trailer.getUrl();
  }

  private static String parseTrailerUrl(String nfoTrailerUrl) {
    // try to parse out youtube trailer plugin
    Pattern pattern = Pattern.compile("plugin://plugin.video.youtube/?action=play_video&videoid=(.*)$");
    Matcher matcher = pattern.matcher(nfoTrailerUrl);
    if (matcher.matches()) {
      return "http://www.youtube.com/watch?v=" + matcher.group(1);
    }

    pattern = Pattern.compile("plugin://plugin.video.hdtrailers_net/video/.*?/(.*)$");
    matcher = pattern.matcher(nfoTrailerUrl);
    if (matcher.matches()) {
      try {
        return URLDecoder.decode(matcher.group(1), "UTF-8");
      }
      catch (UnsupportedEncodingException e) {
        LOGGER.error("failed to unescape " + nfoTrailerUrl);
      }
    }

    return nfoTrailerUrl;
  }

  // inner class actor to represent actors
  /**
   * The Class Actor.
   * 
   * @author Manuel Laggner
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

  /**
   * The Class Fileinfo.
   * 
   * @author Manuel Laggner
   */
  static class Fileinfo {

    /** The streamdetails. */
    @XmlElement
    Streamdetails streamdetails;

    /**
     * Instantiates a new fileinfo.
     */
    public Fileinfo() {
      streamdetails = new Streamdetails();
    }
  }

  /**
   * The Class Streamdetails.
   * 
   * @author Manuel Laggner
   */
  static class Streamdetails {

    /** The video. */
    @XmlElement
    Video       video;

    /** The audio. */
    @XmlElement
    List<Audio> audio;

    /**
     * Instantiates a new streamdetails.
     */
    public Streamdetails() {
      video = new Video();
      audio = new ArrayList<Audio>();
    }
  }

  /**
   * The Class Video.
   * 
   * @author Manuel Laggner
   */
  static class Video {

    /** The codec. */
    @XmlElement
    String codec;

    /** The aspect. */
    @XmlElement
    String aspect;

    /** The width. */
    @XmlElement
    int    width;

    /** The height. */
    @XmlElement
    int    height;

    /** The durationinseconds. */
    @XmlElement
    int    durationinseconds;
  }

  /**
   * The Class Audio.
   * 
   * @author Manuel Laggner
   */
  static class Audio {

    /** The codec. */
    @XmlElement
    String codec;

    /** The language. */
    @XmlElement
    String language;

    /** The channels. */
    @XmlElement
    String channels;
  }

  /**
   * The Class Resume.
   * 
   * @author Manuel Laggner
   */
  static class Resume {

    /** The position. */
    @XmlElement
    String position;

    /** The total. */
    @XmlElement
    String total;
  }
}
