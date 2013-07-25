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
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieActor;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.MovieSet;
import org.tinymediamanager.core.movie.connector.MovieToMpNfoConnector.Actor;
import org.tinymediamanager.core.movie.connector.MovieToMpNfoConnector.MovieSets;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaGenres;

/**
 * The Class MovieTompNfoConnector.
 * 
 * @author Manuel Laggner
 */
@XmlRootElement(name = "movie")
@XmlSeeAlso({ Actor.class, MovieSets.class })
@XmlType(propOrder = { "title", "originaltitle", "sorttitle", "sets", "rating", "year", "votes", "outline", "plot", "tagline", "runtime", "thumb",
    "fanart", "mpaa", "id", "genres", "studio", "credits", "director", "actors" })
public class MovieToMpNfoConnector {

  /** The Constant logger. */
  private static final Logger LOGGER        = LoggerFactory.getLogger(MovieToMpNfoConnector.class);

  /** The title. */
  private String              title         = "";

  /** The originaltitle. */
  private String              originaltitle = "";

  private String              sorttitle     = "";

  /** The rating. */
  private float               rating        = 0;

  /** The votes. */
  private int                 votes         = 0;

  /** The year. */
  private String              year          = "";

  /** The outline. */
  private String              outline       = "";

  /** The plot. */
  private String              plot          = "";

  /** The tagline. */
  private String              tagline       = "";

  /** The runtime. */
  private String              runtime       = "";

  /** The thumb. */
  private String              thumb         = "";
  /** The fanarts. */

  @XmlElementWrapper(name = "fanart")
  @XmlElement(name = "thumb")
  private List<String>        fanart;

  /** The id. */
  private String              id            = "";

  /** The director. */
  private String              director      = "";

  /** The sudio. */
  private String              studio        = "";

  /** The actors. */
  @XmlAnyElement(lax = true)
  private List<Object>        actors;

  /** The genres. */
  @XmlElementWrapper(name = "genres")
  @XmlElement(name = "genre")
  private List<String>        genres;

  /** The mpaa certification. */
  private String              mpaa          = "";

  /** the credits. */
  private String              credits       = "";

  /** The sets. */
  private List<MovieSets>     sets;

  private static JAXBContext  context       = initContext();

  private static JAXBContext initContext() {
    try {
      return JAXBContext.newInstance(MovieToMpNfoConnector.class, Actor.class);
    }
    catch (JAXBException e) {
      LOGGER.error(e.getMessage());
    }
    return null;
  }

  /**
   * Instantiates a new movie to mp nfo connector.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public MovieToMpNfoConnector() {
    actors = new ArrayList();
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
    if (context == null) {
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.nfo.writeerror", new String[] { ":", "Context is null" }));
      return "";
    }

    MovieToMpNfoConnector mp = new MovieToMpNfoConnector();
    // set data
    mp.setTitle(movie.getTitle());
    mp.setOriginaltitle(movie.getOriginalTitle());

    mp.setSorttitle(movie.getSortTitle());
    // if sort title is empty, insert the title sortable
    if (StringUtils.isBlank(mp.getSorttitle())) {
      mp.setSorttitle(movie.getTitleSortable());
    }

    mp.setRating(movie.getRating());
    mp.setVotes(movie.getVotes());
    mp.setYear(movie.getYear());
    mp.setPlot(movie.getPlot());

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
    mp.setRuntime(String.valueOf(movie.getRuntime()));
    mp.setThumb(FilenameUtils.getName(movie.getPoster()));
    mp.addFanart(FilenameUtils.getName(movie.getFanart()));
    mp.setId(movie.getImdbId());
    mp.setStudio(movie.getProductionCompany());

    // certification
    if (movie.getCertification() != null) {
      mp.setMpaa(movie.getCertification().name());
    }

    // // filename and path
    // if (movie.getMediaFiles(MediaFileType.VIDEO).size() > 0) {
    // mp.setFilenameandpath(movie.getPath() + File.separator + movie.getMediaFiles(MediaFileType.VIDEO).get(0).getFilename());
    // }

    mp.setDirector(movie.getDirector());
    mp.setCredits(movie.getWriter());
    for (MovieActor cast : movie.getActors()) {
      mp.addActor(cast.getName(), cast.getCharacter(), cast.getThumb());
    }

    for (MediaGenres genre : movie.getGenres()) {
      mp.addGenre(genre.toString());
    }

    // movie set
    if (movie.getMovieSet() != null) {
      MovieSet movieSet = movie.getMovieSet();
      MovieSets set = new MovieSets(movieSet.getTitle(), movieSet.getMovieIndex(movie) + 1);
      mp.addSet(set);
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
        m.marshal(mp, w);
        StringBuilder sb = new StringBuilder(w.toString());
        w.close();

        // on windows make windows conform linebreaks
        if (SystemUtils.IS_OS_WINDOWS) {
          sb = new StringBuilder(sb.toString().replaceAll("(?<!\r)\n", "\r\n"));
        }
        FileUtils.write(new File(movie.getPath(), nfoFilename), sb, "UTF-8");
      }
      catch (Exception e) {
        LOGGER.error("setData", e);
        MessageManager.instance
            .pushMessage(new Message(MessageLevel.ERROR, movie, "message.nfo.writeerror", new String[] { e.getLocalizedMessage() }));
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
      MovieToMpNfoConnector mp = (MovieToMpNfoConnector) um.unmarshal(in);
      movie = new Movie();
      movie.setTitle(mp.getTitle());
      movie.setSortTitle(mp.getSorttitle());
      movie.setOriginalTitle(mp.getOriginaltitle());
      movie.setRating(mp.getRating());
      movie.setVotes(mp.getVotes());
      movie.setYear(mp.getYear());
      movie.setPlot(mp.getPlot());
      movie.setTagline(mp.getTagline());
      try {
        String rt = mp.getRuntime().replaceAll("[^0-9]", "");
        movie.setRuntime(Integer.parseInt(rt));
      }
      catch (Exception e) {
        LOGGER.warn("could not parse runtime: " + mp.getRuntime());
      }

      movie.setImdbId(mp.getId());
      movie.setDirector(mp.getDirector());
      movie.setWriter(mp.getCredits());
      movie.setProductionCompany(mp.getStudio());
      if (!StringUtils.isEmpty(mp.getMpaa())) {
        movie.setCertification(Certification.parseCertificationStringForMovieSetupCountry(mp.getMpaa()));
      }

      // movieset
      if (mp.getSets() != null && mp.getSets().size() > 0) {
        MovieSets sets = mp.getSets().get(0);
        // search for that movieset
        MovieList movieList = MovieList.getInstance();
        MovieSet movieSet = movieList.getMovieSet(sets.getName());

        // add movie to movieset
        if (movieSet != null) {
          movie.setMovieSet(movieSet);
          movie.setSortTitle(sets.getName() + String.format("%02d", sets.getOrder()));
        }
      }

      for (Actor actor : mp.getActors()) {
        MovieActor cast = new MovieActor(actor.getName(), actor.getRole());
        cast.setThumb(actor.getThumb());
        movie.addActor(cast);
      }

      for (String genre : mp.getGenres()) {
        String[] genres = genre.split("/");
        for (String g : genres) {
          MediaGenres genreFound = MediaGenres.getGenre(g.trim());
          if (genreFound != null) {
            movie.addGenre(genreFound);
          }
        }
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

  @XmlElement(name = "sorttitle")
  public String getSorttitle() {
    return sorttitle;
  }

  public void setSorttitle(String sorttitle) {
    this.sorttitle = sorttitle;
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

  @XmlElement(name = "thumb")
  public String getThumb() {
    return thumb;
  }

  public void setThumb(String thumb) {
    this.thumb = thumb;
  }

  public List<String> getFanart() {
    return fanart;
  }

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

  // inner class actor to represent movie sets
  /**
   * The Class MovieSets.
   * 
   * @author Manuel Laggner
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
