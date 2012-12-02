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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.MovieToXbmcNfoConnector.Actor;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaGenres;

// TODO: Auto-generated Javadoc
/**
 * The Class MovieToXbmcNfoConnector.
 */
@XmlRootElement(name = "movie")
@XmlSeeAlso(Actor.class)
@XmlType(propOrder = { "title", "originaltitle", "rating", "year", "votes", "outline", "plot", "tagline", "runtime", "thumb", "mpaa",
    "certifications", "id", "tmdbId", "filenameandpath", "watched", "playcount", "genres", "studio", "credits", "director", "actors" })
public class MovieToXbmcNfoConnector {

  /** The Constant logger. */
  private static final Logger LOGGER   = Logger.getLogger(MovieToXbmcNfoConnector.class);

  /** The Constant NFO_NAME. */
  private final static String NFO_NAME = "movie.nfo";

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
  private int                 runtime;

  /** The thumb. */
  private String              thumb;

  /** The id. */
  private String              id;

  /** The tmdbid. */
  private int                 tmdbId;

  /** The filenameandpath. */
  private String              filenameandpath;

  /** The director. */
  private String              director;

  /** The sudio. */
  private String              studio;

  /** The actors. */
  @XmlAnyElement(lax = true)
  private List<Actor>         actors;

  /** The genres. */
  @XmlElement(name = "genre")
  private List<String>        genres;

  /** The mpaa certification. */
  private String              mpaa;

  /** The certifications. */
  private String              certifications;

  /** the credits. */
  private String              credits;

  /** The watched. */
  private boolean             watched;

  /** The playcount. */
  private int                 playcount;

  /**
   * Instantiates a new movie to xbmc nfo connector.
   */
  public MovieToXbmcNfoConnector() {
    actors = new ArrayList<MovieToXbmcNfoConnector.Actor>();
    genres = new ArrayList<String>();
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
    xbmc.setRuntime(movie.getRuntime());
    xbmc.setThumb(movie.getPoster());
    xbmc.setId(movie.getImdbId());
    xbmc.setTmdbId(movie.getTmdbId());
    xbmc.setStudio(movie.getProductionCompany());
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
    if (movie.getMovieFiles().size() > 0) {
      xbmc.setFilenameandpath(movie.getPath() + File.separator + movie.getMovieFiles().get(0));
    }

    xbmc.setDirector(movie.getDirector());
    xbmc.setCredits(movie.getWriter());
    for (MovieCast cast : movie.getActors()) {
      xbmc.addActor(cast.getName(), cast.getCharacter(), cast.getThumb());
    }

    for (MediaGenres genre : movie.getGenres()) {
      xbmc.addGenre(genre.toString());
    }

    // and marshall it
    // String nfoFilename = movie.getPath() + File.separator + NFO_NAME;
    String nfoFilename = "";
    for (MovieNfoNaming name : Globals.settings.getMovieNfoFilenames()) {
      JAXBContext context;
      Writer w = null;
      try {
        switch (name) {
          case FILENAME_NFO:
            // nfoFilename = movie.getPath() + File.separator +
            // movie.getMovieFiles().get(0).replaceAll("\\.[A-Za-z0-9]{3,4}$",
            // ".nfo");
            nfoFilename = movie.getPath() + File.separator + FilenameUtils.getBaseName(movie.getMovieFiles().get(0)) + ".nfo";
            break;

          case MOVIE_NFO:
            nfoFilename = movie.getPath() + File.separator + "movie.nfo";
            break;
        }
        context = JAXBContext.newInstance(MovieToXbmcNfoConnector.class, Actor.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        // w = new FileWriter(nfoFilename);
        w = new StringWriter();
        m.marshal(xbmc, w);
        StringBuilder sb = new StringBuilder(w.toString());
        w.close();

        // on windows make windows conform linebreaks
        if (SystemUtils.IS_OS_WINDOWS) {
          sb = new StringBuilder(sb.toString().replaceAll("(?<!\r)\n", "\r\n"));
        }

        w = new FileWriter(nfoFilename);
        String xml = sb.toString();
        IOUtils.write(xml, w);

      }
      catch (JAXBException e) {
        LOGGER.error("setData", e);
      }
      catch (IOException e) {
        LOGGER.error("setData", e);
      }
      finally {
        try {
          w.close();
        }
        catch (Exception e) {
          LOGGER.error("setData", e);
        }
      }
    }

    return nfoFilename;

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
      context = JAXBContext.newInstance(MovieToXbmcNfoConnector.class, Actor.class);
      Unmarshaller um = context.createUnmarshaller();
      try {
        MovieToXbmcNfoConnector xbmc = (MovieToXbmcNfoConnector) um.unmarshal(new FileReader(nfoFilename));
        movie = new Movie();
        movie.setName(xbmc.getTitle());
        movie.setOriginalName(xbmc.getOriginaltitle());
        movie.setRating(xbmc.getRating());
        movie.setVotes(xbmc.getVotes());
        movie.setYear(xbmc.getYear());
        movie.setOverview(xbmc.getPlot());
        movie.setTagline(xbmc.getTagline());
        movie.setRuntime(xbmc.getRuntime());
        if (StringUtils.isNotEmpty(xbmc.getThumb()) && xbmc.getThumb().contains("http://")) {
          movie.setPosterUrl(xbmc.getThumb());
        }
        else {
          movie.setPoster(xbmc.getThumb());
        }

        movie.setImdbId(xbmc.getId());
        movie.setTmdbId(xbmc.getTmdbId());
        movie.setDirector(xbmc.getDirector());
        movie.setWriter(xbmc.getCredits());
        movie.setProductionCompany(xbmc.getStudio());
        if (!StringUtils.isEmpty(xbmc.getMpaa())) {
          movie.setCertification(Certification.findCertification(xbmc.getMpaa()));
        }
        movie.setWatched(xbmc.isWatched());

        for (Object obj : xbmc.getActors()) {
          // every unused XML element will be shown as an actor - we have to
          // test it this way; else the program will crash
          if (obj instanceof Actor) {
            Actor actor = (Actor) obj;
            MovieCast cast = new MovieCast(actor.getName(), actor.getRole());
            cast.setThumb(actor.getThumb());
            movie.addToCast(cast);
          }
        }

        for (String genre : xbmc.getGenres()) {
          MediaGenres genreFound = MediaGenres.getGenre(genre);
          if (genreFound != null) {
            movie.addGenre(genreFound);
          }
        }

        movie.setNfoFilename(nfoFilename);

      }
      catch (FileNotFoundException e) {
        LOGGER.error("setData", e);
        return null;
      }
      catch (IOException e) {
        LOGGER.error("setData", e);
        return null;
      }
    }
    catch (JAXBException e) {
      // LOGGER.error("setData", e);
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
    return actors;
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
  public int getRuntime() {
    return runtime;
  }

  /**
   * Sets the runtime.
   * 
   * @param runtime
   *          the new runtime
   */
  public void setRuntime(int runtime) {
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
  @XmlElement(name = "director")
  public String getDirector() {
    return director;
  }

  /**
   * Sets the director.
   * 
   * @param director
   *          the new director
   */
  public void setDirector(String director) {
    this.director = director;
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
  @XmlElement(name = "credits")
  public String getCredits() {
    return credits;
  }

  /**
   * Sets the credits.
   * 
   * @param credits
   *          the new credits
   */
  public void setCredits(String credits) {
    this.credits = credits;
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

}
