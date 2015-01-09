/*
 * Copyright 2012 - 2014 Manuel Laggner
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
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.connector.MovieToMpNfoConnector.Actor;
import org.tinymediamanager.core.movie.connector.MovieToMpNfoConnector.MovieSets;
import org.tinymediamanager.core.movie.connector.MovieToMpNfoConnector.Producer;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieActor;
import org.tinymediamanager.core.movie.entities.MovieProducer;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaGenres;

/**
 * The Class MovieTompNfoConnector.
 * 
 * @author Manuel Laggner
 */
@XmlRootElement(name = "movie")
@XmlSeeAlso({ Actor.class, MovieSets.class, Producer.class })
@XmlType(propOrder = { "title", "originaltitle", "sorttitle", "sets", "rating", "year", "votes", "outline", "plot", "tagline", "runtime", "thumb",
    "fanart", "mpaa", "id", "ids", "genres", "studio", "country", "premiered", "credits", "director", "actors", "producers" })
public class MovieToMpNfoConnector {

  private static final Logger LOGGER        = LoggerFactory.getLogger(MovieToMpNfoConnector.class);
  private static JAXBContext  context       = initContext();

  private String              id            = "";
  private String              title         = "";
  private String              originaltitle = "";
  private String              sorttitle     = "";
  private float               rating        = 0;
  private int                 votes         = 0;
  private String              year          = "";
  private String              outline       = "";
  private String              plot          = "";
  private String              tagline       = "";
  private String              runtime       = "";
  private String              thumb         = "";
  private String              director      = "";
  private String              studio        = "";
  private String              mpaa          = "";
  private String              credits       = "";
  private String              country       = "";

  @XmlElement
  private String              premiered     = "";

  @XmlElementWrapper(name = "fanart")
  @XmlElement(name = "thumb")
  private List<String>        fanart;

  @XmlAnyElement(lax = true)
  private List<Object>        actors;

  @XmlAnyElement(lax = true)
  private List<Object>        producers;

  @XmlElementWrapper(name = "genres")
  @XmlElement(name = "genre")
  private List<String>        genres;

  private List<MovieSets>     sets;

  @XmlElementWrapper(name = "ids")
  private Map<String, Object> ids;

  private static JAXBContext initContext() {
    try {
      return JAXBContext.newInstance(MovieToMpNfoConnector.class, Actor.class);
    }
    catch (JAXBException e) {
      LOGGER.error("Error instantiating JaxB", e);
    }
    return null;
  }

  /**
   * Instantiates a new movie to mp nfo connector.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public MovieToMpNfoConnector() {
    actors = new ArrayList();
    producers = new ArrayList();
    genres = new ArrayList<String>();
    fanart = new ArrayList<String>();
    sets = new ArrayList<MovieSets>();
    ids = new HashMap<String, Object>();
  }

  /**
   * Sets the data.
   * 
   * @param movie
   *          the movie
   */
  public static void setData(Movie movie) {
    if (context == null) {
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.nfo.writeerror", new String[] { ":", "Context is null" }));
      return;
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
    mp.premiered = movie.getReleaseDateFormatted();
    mp.setPlot(movie.getPlot());

    // outline is only the first 200 characters of the plot
    if (StringUtils.isNotBlank(mp.getPlot()) && mp.getPlot().length() > 200) {
      int spaceIndex = mp.getPlot().indexOf(" ", 200);
      if (spaceIndex > 0) {
        mp.setOutline(mp.getPlot().substring(0, spaceIndex) + "...");
      }
      else {
        mp.setOutline(mp.getPlot());
      }
    }
    else if (StringUtils.isNotBlank(mp.getPlot())) {
      mp.setOutline(mp.getPlot());
    }

    mp.setTagline(movie.getTagline());
    mp.setRuntime(String.valueOf(movie.getRuntime()));
    mp.setThumb(FilenameUtils.getName(movie.getPoster()));

    // fanarts: is extrafanart is activated - put up to 5 fanarts into the NFO; else take the only fanart
    List<MediaFile> extrafanarts = movie.getMediaFiles(MediaFileType.EXTRAFANART);
    if (extrafanarts.size() > 0) {
      for (int i = 0; i < extrafanarts.size(); i++) {
        MediaFile mf = extrafanarts.get(i);

        File fanart = mf.getFile();
        String fanartPath = new File(movie.getPath()).toURI().relativize(fanart.toURI()).getPath();
        mp.addFanart(fanartPath);

        if (i == 4) {
          break;
        }
      }
    }
    else {
      mp.addFanart(FilenameUtils.getName(movie.getFanart()));
    }
    mp.setId(movie.getImdbId());
    mp.ids.putAll(movie.getIds());
    mp.setStudio(movie.getProductionCompany());
    mp.setCountry(movie.getCountry());

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
      mp.addActor(cast.getName(), cast.getCharacter(), cast.getThumbUrl());
    }

    for (MovieProducer producer : movie.getProducers()) {
      mp.addProducer(producer.getName(), producer.getRole(), producer.getThumbUrl());
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
    List<MediaFile> newNfos = new ArrayList<MediaFile>(1);

    List<MovieNfoNaming> nfonames = new ArrayList<MovieNfoNaming>();
    if (movie.isMultiMovieDir()) {
      // Fixate the name regardless of setting
      nfonames.add(MovieNfoNaming.FILENAME_NFO);
    }
    else {
      nfonames = MovieModuleManager.MOVIE_SETTINGS.getMovieNfoFilenames();
      if (movie.isDisc()) {
        nfonames.add(MovieNfoNaming.DISC_NFO); // add additionally the NFO at disc style location
      }
    }
    for (MovieNfoNaming name : nfonames) {

      try {
        nfoFilename = movie.getNfoFilename(name);

        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dat = formatter.format(new Date());
        String comment = "<!-- created on " + dat + " - tinyMediaManager " + Globals.settings.getVersion() + " -->\n";
        m.setProperty("com.sun.xml.internal.bind.xmlHeaders", comment);

        // w = new FileWriter(nfoFilename);
        Writer w = new StringWriter();
        m.marshal(mp, w);
        StringBuilder sb = new StringBuilder(w.toString());
        w.close();

        // on windows make windows conform linebreaks
        if (SystemUtils.IS_OS_WINDOWS) {
          sb = new StringBuilder(sb.toString().replaceAll("(?<!\r)\n", "\r\n"));
        }
        File f = new File(movie.getPath(), nfoFilename);
        FileUtils.write(f, sb, "UTF-8");
        newNfos.add(new MediaFile(f));
      }
      catch (Exception e) {
        LOGGER.error("setData " + movie.getPath() + File.separator + nfoFilename, e);
        MessageManager.instance
            .pushMessage(new Message(MessageLevel.ERROR, movie, "message.nfo.writeerror", new String[] { e.getLocalizedMessage() }));
      }
    }

    if (newNfos.size() > 0) {
      movie.removeAllMediaFiles(MediaFileType.NFO);
      movie.addToMediaFiles(newNfos);
    }
  }

  /**
   * Gets the data.
   * 
   * @param nfoFilename
   *          the nfo filename
   * @return the data
   */
  public static Movie getData(File nfoFilename) {
    if (context == null) {
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, nfoFilename, "message.nfo.readerror"));
      return null;
    }

    // try to parse XML
    Movie movie = null;
    try {
      MovieToMpNfoConnector mp = parseNFO(nfoFilename);
      movie = new Movie();
      movie.setTitle(mp.getTitle());
      movie.setSortTitle(mp.getSorttitle());
      movie.setOriginalTitle(mp.getOriginaltitle());
      movie.setRating(mp.getRating());
      movie.setVotes(mp.getVotes());
      movie.setYear(mp.getYear());
      try {
        movie.setReleaseDate(mp.premiered);
      }
      catch (ParseException e) {
      }
      movie.setPlot(mp.getPlot());
      movie.setTagline(mp.getTagline());
      try {
        String rt = mp.getRuntime().replaceAll("[^0-9]", "");
        movie.setRuntime(Integer.parseInt(rt));
      }
      catch (Exception e) {
        LOGGER.warn("could not parse runtime: " + mp.getRuntime());
      }

      for (Entry<String, Object> entry : mp.ids.entrySet()) {
        try {
          movie.setId(entry.getKey(), entry.getValue());
        }
        catch (Exception e) {
          LOGGER.warn("could not set ID: " + entry.getKey() + " ; " + entry.getValue());
        }
      }

      if (StringUtils.isBlank(movie.getImdbId())) {
        movie.setImdbId(mp.id);
      }

      movie.setDirector(mp.getDirector());
      movie.setWriter(mp.getCredits());
      movie.setProductionCompany(mp.getStudio());
      movie.setCountry(mp.getCountry());
      if (!StringUtils.isEmpty(mp.getMpaa())) {
        movie.setCertification(Certification.parseCertificationStringForMovieSetupCountry(mp.getMpaa()));
      }

      // movieset
      if (mp.getSets() != null && mp.getSets().size() > 0) {
        MovieSets sets = mp.getSets().get(0);
        // search for that movieset
        MovieList movieList = MovieList.getInstance();
        MovieSet movieSet = movieList.getMovieSet(sets.getName(), 0);

        // add movie to movieset
        if (movieSet != null) {
          movie.setMovieSet(movieSet);
          movie.setSortTitle(sets.getName() + String.format("%02d", sets.getOrder()));
        }
      }

      for (Actor actor : mp.getActors()) {
        MovieActor cast = new MovieActor(actor.getName(), actor.getRole());
        cast.setThumbUrl(actor.getThumb());
        movie.addActor(cast);
      }

      for (Producer producer : mp.getProducers()) {
        MovieProducer cast = new MovieProducer(producer.name, producer.role);
        cast.setThumbUrl(producer.thumb);
        movie.addProducer(cast);
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

    }
    catch (UnmarshalException e) {
      LOGGER.error("getData " + nfoFilename.getAbsolutePath(), e.getMessage());
      // MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, nfoFilename, "message.nfo.readerror"));
      return null;
    }
    catch (Exception e) {
      LOGGER.error("getData " + nfoFilename.getAbsolutePath(), e);
      // MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, nfoFilename, "message.nfo.readerror"));
      return null;
    }

    // only return if a movie name has been found
    if (StringUtils.isEmpty(movie.getTitle())) {
      return null;
    }

    return movie;
  }

  private static MovieToMpNfoConnector parseNFO(File nfoFile) throws Exception {
    Unmarshaller um = context.createUnmarshaller();
    if (um == null) {
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, nfoFile, "message.nfo.readerror"));
      throw new Exception("could not create unmarshaller");
    }

    try {
      Reader in = new InputStreamReader(new FileInputStream(nfoFile), "UTF-8");
      return (MovieToMpNfoConnector) um.unmarshal(in);
    }
    catch (UnmarshalException e) {
      LOGGER.error("tried to unmarshal; now trying to clean xml stream");
    }

    // now trying to parse it via string
    String completeNFO = FileUtils.readFileToString(nfoFile, "UTF-8").trim().replaceFirst("^([\\W]+)<", "<");
    completeNFO = completeNFO.replace("<movie>",
        "<movie xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
    Reader in = new StringReader(completeNFO);
    return (MovieToMpNfoConnector) um.unmarshal(in);
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

  private void addProducer(String name, String role, String thumb) {
    Producer producer = new Producer(name, role, thumb);
    producers.add(producer);
  }

  public List<Producer> getProducers() {
    // @XmlAnyElement(lax = true) causes all unsupported tags to be in producers;
    // filter producers out
    List<Producer> pureProducers = new ArrayList<Producer>();
    // for (Object obj : producers) {
    for (Object obj : actors) { // ugly hack for invalid xml structure
      if (obj instanceof Producer) {
        Producer producer = (Producer) obj;
        pureProducers.add(producer);
      }
    }
    return pureProducers;
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

  @XmlElement(name = "country")
  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
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

  /*
   * inner class to represent producers
   */
  @XmlRootElement(name = "producer")
  public static class Producer {
    @XmlElement
    private String name;

    @XmlElement
    private String role;

    @XmlElement
    private String thumb;

    public Producer() {
    }

    public Producer(String name, String role, String thumb) {
      this.name = name;
      this.role = role;
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
