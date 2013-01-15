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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.MovieToMpNfoConnector.Actor;
import org.tinymediamanager.core.movie.MovieToMpNfoConnector.MovieSets;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaGenres;

// TODO: Auto-generated Javadoc
/**
 * The Class MovieTompNfoConnector.
 */
@XmlRootElement(name = "movie")
@XmlSeeAlso({ Actor.class, MovieSets.class })
@XmlType(propOrder = { "title", "originaltitle", "sets", "rating", "year", "votes", "outline", "plot", "tagline", "runtime", "thumb", "fanart",
    "mpaa", "id", "filenameandpath", "genres", "studio", "credits", "director", "actors" })
public class MovieToMpNfoConnector {

  /** The Constant logger. */
  private static final Logger LOGGER = Logger.getLogger(MovieToMpNfoConnector.class);

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

  /** The fanarts. */
  @XmlElementWrapper(name = "fanart")
  @XmlElement(name = "thumb")
  private List<String>        fanart;

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
  @XmlElementWrapper(name = "genres")
  @XmlElement(name = "genre")
  private List<String>        genres;

  /** The mpaa certification. */
  private String              mpaa;

  /** the credits. */
  private String              credits;

  /** The sets. */
  private List<MovieSets>     sets;

  /**
   * Instantiates a new movie to mp nfo connector.
   */
  public MovieToMpNfoConnector() {
    actors = new ArrayList<MovieToMpNfoConnector.Actor>();
    genres = new ArrayList<String>();
    fanart = new ArrayList<String>();
    sets = new ArrayList<MovieSets>();
  }

  /**
   * Sets the data.
   * 
   * @param movie
   *          the movie
   * @return the string
   */
  public static String setData(Movie movie) {
    MovieToMpNfoConnector mp = new MovieToMpNfoConnector();
    // set data
    mp.setTitle(movie.getName());
    mp.setOriginaltitle(movie.getOriginalName());
    mp.setRating(movie.getRating());
    mp.setVotes(movie.getVotes());
    mp.setYear(movie.getYear());
    mp.setPlot(movie.getOverview());

    // outline is only the first 200 characters of the plot
    int spaceIndex = 0;
    if (!StringUtils.isEmpty(mp.getPlot()) && mp.getPlot().length() > 200) {
      spaceIndex = mp.getPlot().indexOf(" ", 200);
      if (spaceIndex > 0) {
        mp.setOutline(mp.getPlot().substring(0, spaceIndex));
      }
      else {
        mp.setOutline(mp.getPlot());
      }
    }
    else if (!StringUtils.isEmpty(mp.getPlot())) {
      spaceIndex = mp.getPlot().length();
      mp.setOutline(mp.getPlot().substring(0, spaceIndex));
    }

    mp.setTagline(movie.getTagline());
    mp.setRuntime(movie.getRuntime());
    mp.setThumb(movie.getPoster());
    mp.addFanart(movie.getFanart());
    mp.setId(movie.getImdbId());
    mp.setStudio(movie.getProductionCompany());

    // certification
    if (movie.getCertification() != null) {
      mp.setMpaa(movie.getCertification().toString());
    }

    // filename and path
    if (movie.getMediaFiles().size() > 0) {
      mp.setFilenameandpath(movie.getPath() + File.separator + movie.getMediaFiles().get(0).getFilename());
    }

    mp.setDirector(movie.getDirector());
    mp.setCredits(movie.getWriter());
    for (MovieCast cast : movie.getActors()) {
      mp.addActor(cast.getName(), cast.getCharacter(), cast.getThumb());
    }

    for (MediaGenres genre : movie.getGenres()) {
      mp.addGenre(genre.toString());
    }

    // movie set
    if (movie.getMovieSet() != null) {
      MovieSet movieSet = movie.getMovieSet();
      MovieSets set = new MovieSets(movieSet.getName(), movieSet.getMovieIndex(movie) + 1);
      mp.addSet(set);
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
            nfoFilename = movie.getPath() + File.separator + FilenameUtils.getBaseName(movie.getMediaFiles().get(0).getFilename()) + ".nfo";
            break;

          case MOVIE_NFO:
            nfoFilename = movie.getPath() + File.separator + "movie.nfo";
            break;
        }
        context = JAXBContext.newInstance(MovieToMpNfoConnector.class, Actor.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        // w = new FileWriter(nfoFilename);
        w = new StringWriter();
        m.marshal(mp, w);
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
      context = JAXBContext.newInstance(MovieToMpNfoConnector.class, Actor.class);
      Unmarshaller um = context.createUnmarshaller();
      try {
        MovieToMpNfoConnector mp = (MovieToMpNfoConnector) um.unmarshal(new FileReader(nfoFilename));
        movie = new Movie();
        movie.setName(mp.getTitle());
        movie.setOriginalName(mp.getOriginaltitle());
        movie.setRating(mp.getRating());
        movie.setVotes(mp.getVotes());
        movie.setYear(mp.getYear());
        movie.setOverview(mp.getPlot());
        movie.setTagline(mp.getTagline());
        movie.setRuntime(mp.getRuntime());
        movie.setPoster(mp.getThumb());

        if (mp.getFanart() != null && mp.getFanart().size() > 0) {
          movie.setFanart(mp.getFanart().get(0));
        }

        movie.setImdbId(mp.getId());
        movie.setDirector(mp.getDirector());
        movie.setWriter(mp.getCredits());
        movie.setProductionCompany(mp.getStudio());
        if (!StringUtils.isEmpty(mp.getMpaa())) {
          movie.setCertification(Certification.findCertification(mp.getMpaa()));
        }

        for (Object obj : mp.getActors()) {
          // every unused XML element will be shown as an actor - we have to
          // test it this way; else the program will crash
          if (obj instanceof Actor) {
            Actor actor = (Actor) obj;
            MovieCast cast = new MovieCast(actor.getName(), actor.getRole());
            cast.setThumb(actor.getThumb());
            movie.addToCast(cast);
          }
        }

        for (String genre : mp.getGenres()) {
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
    }
    catch (Exception e) {
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
   * Gets the fanart.
   * 
   * @return the fanart
   */
  public List<String> getFanart() {
    return fanart;
  }

  /**
   * Adds the fanart.
   * 
   * @param fanart
   *          the fanart
   */
  public void addFanart(String fanart) {
    this.fanart.add(fanart);
  }

  /**
   * Gets the id.
   * 
   * @return the id
   */
  @XmlElement(name = "imdb")
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
   * Gets the sets.
   * 
   * @return the sets
   */
  @XmlElementWrapper
  @XmlElement(name = "set", type = MovieSets.class)
  public List<MovieSets> getSets() {
    return this.sets;
  }

  /**
   * Adds the set.
   * 
   * @param set
   *          the set
   */
  public void addSet(MovieSets set) {
    this.sets.add(set);
  }

  /**
   * Sets the sets.
   * 
   * @param sets
   *          the new sets
   */
  public void setSets(List<MovieSets> sets) {
    this.sets = sets;
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

  // inner class actor to represent movie sets
  /**
   * The Class MovieSets.
   */
  public static class MovieSets {

    /** The name. */
    private String name;

    /** The order. */
    private int    order;

    /**
     * Instantiates a new movie sets.
     */
    public MovieSets() {

    }

    /**
     * Instantiates a new movie sets.
     * 
     * @param name
     *          the name
     * @param order
     *          the order
     */
    public MovieSets(String name, int order) {
      this.name = name;
      this.order = order;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    @XmlValue
    public String getName() {
      return name;
    }

    /**
     * Gets the order.
     * 
     * @return the order
     */
    @XmlAttribute(name = "order")
    public int getOrder() {
      return order;
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
     * Sets the order.
     * 
     * @param order
     *          the new order
     */
    public void setOrder(int order) {
      this.order = order;
    }

  }
}
