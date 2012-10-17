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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.core.movie.MovieToXbmcNfoConnector.Actor;
import org.tinymediamanager.scraper.MediaMetadata.Genres;

/**
 * The Class MovieToXbmcNfoConnector.
 */
@XmlRootElement(name = "movie")
@XmlSeeAlso(Actor.class)
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

  /** The mpaa certification */
  private String              mpaa;

  /** The certifications */
  private String              certifications;

  /** the credits */
  private String              credits;

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
    xbmc.setThumb(movie.getPosterUrl());
    xbmc.setId(movie.getImdbId());
    xbmc.setStudio(movie.getProductionCompany());

    // certifications
    StringBuilder certifications = new StringBuilder();
    for (MovieCertification certification : movie.getCertifications()) {
      if (!StringUtils.isEmpty(certifications)) {
        certifications.append(" / ");
      }
      certifications.append(certification.getCountry() + ":" + certification.getCertification());

      // MPAA is stored separate
      if ("US".equals(certification.getCountry())) {
        xbmc.setMpaa(certification.getCertification());
      }
    }
    xbmc.setCertifications(certifications.toString());

    // filename and path
    if (movie.getMovieFiles().size() > 0) {
      xbmc.setFilenameandpath(movie.getPath() + File.separator + movie.getMovieFiles().get(0));
    }

    xbmc.setDirector(movie.getDirector());
    xbmc.setCredits(movie.getWriter());
    for (MovieCast cast : movie.getActors()) {
      xbmc.addActor(cast.getName(), cast.getCharacter());
    }

    for (Genres genre : movie.getGenres()) {
      xbmc.addGenre(genre.toString());
    }

    // and marshall it
    String nfoFilename = movie.getPath() + File.separator + NFO_NAME;
    JAXBContext context;
    Writer w = null;
    try {
      context = JAXBContext.newInstance(MovieToXbmcNfoConnector.class, Actor.class);
      Marshaller m = context.createMarshaller();
      m.setProperty("jaxb.encoding", "UTF-8");
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      w = new FileWriter(nfoFilename);
      m.marshal(xbmc, w);

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
        movie.setYear(xbmc.getYear());
        movie.setOverview(xbmc.getPlot());
        movie.setTagline(xbmc.getTagline());
        movie.setRuntime(xbmc.getRuntime());
        movie.setPosterUrl(xbmc.getThumb());
        movie.setImdbId(xbmc.getId());
        movie.setDirector(xbmc.getDirector());
        movie.setWriter(xbmc.getCredits());
        movie.setProductionCompany(xbmc.getStudio());
        if (!StringUtils.isEmpty(xbmc.getMpaa())) {
          movie.addCertification(new MovieCertification("US", xbmc.getMpaa()));
        }

        for (Actor actor : xbmc.getActors()) {
          movie.addToCast(new MovieCast(actor.getName(), actor.getRole()));
        }

        for (String genre : xbmc.getGenres()) {
          Genres genreFound = Genres.getGenre(genre);
          if (genreFound != null) {
            movie.addGenre(genreFound);
          }
        }

        movie.setNfoFilename(nfoFilename);

      }
      catch (FileNotFoundException e) {
        return null;
      }
      catch (IOException e) {
        return null;
      }
    }
    catch (JAXBException e) {
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
   */
  public void addActor(String name, String role) {
    Actor actor = new Actor(name, role);
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

  @XmlElement(name = "studio")
  public String getStudio() {
    return studio;
  }

  public void setStudio(String studio) {
    this.studio = studio;
  }

  @XmlElement(name = "mpaa")
  public String getMpaa() {
    return mpaa;
  }

  public void setMpaa(String mpaa) {
    this.mpaa = mpaa;
  }

  @XmlElement(name = "certification")
  public String getCertifications() {
    return certifications;
  }

  public void setCertifications(String certifications) {
    this.certifications = certifications;
  }

  @XmlElement(name = "credits")
  public String getCredits() {
    return credits;
  }

  public void setCredits(String credits) {
    this.credits = credits;
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
     */
    public Actor(String name, String role) {
      this.name = name;
      this.role = role;
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

  }

}
