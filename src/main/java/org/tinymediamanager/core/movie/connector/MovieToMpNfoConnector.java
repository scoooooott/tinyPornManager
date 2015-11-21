/*
 * Copyright 2012 - 2015 Manuel Laggner
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
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieHelpers;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieMediaSource;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.connector.MovieToMpNfoConnector.Actor;
import org.tinymediamanager.core.movie.connector.MovieToMpNfoConnector.MovieSets;
import org.tinymediamanager.core.movie.connector.MovieToMpNfoConnector.Producer;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieActor;
import org.tinymediamanager.core.movie.entities.MovieProducer;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.util.ParserUtils;

/**
 * The Class MovieTompNfoConnector.
 * 
 * @author Manuel Laggner
 */
@XmlRootElement(name = "movie")
@XmlSeeAlso({ Actor.class, MovieSets.class, Producer.class })
@XmlType(propOrder = { "title", "originaltitle", "sorttitle", "sets", "rating", "year", "votes", "outline", "plot", "tagline", "runtime", "thumb",
    "fanart", "mpaa", "imdb", "ids", "genres", "studio", "country", "premiered", "credits", "director", "actors", "producers", "watched", "playcount",
    "source" })
public class MovieToMpNfoConnector {

  private static final Logger LOGGER        = LoggerFactory.getLogger(MovieToMpNfoConnector.class);
  private static JAXBContext  context       = initContext();

  public String               title         = "";
  public String               originaltitle = "";
  public String               sorttitle     = "";
  @XmlElementWrapper
  @XmlElement(name = "set", type = MovieSets.class)
  public List<MovieSets>      sets;
  public float                rating        = 0;
  public String               year          = "";
  public int                  votes         = 0;
  public String               outline       = "";
  public String               plot          = "";
  public String               tagline       = "";
  public String               runtime       = "";
  public String               thumb         = "";
  @XmlElementWrapper
  @XmlElement(name = "thumb")
  public List<String>         fanart;
  public String               mpaa          = "";
  public String               imdb          = "";
  @XmlElementWrapper(name = "ids")
  public Map<String, Object>  ids;
  @XmlElementWrapper
  @XmlElement(name = "genre")
  public List<String>         genres;
  public String               studio        = "";
  public String               country       = "";
  public String               premiered     = "";
  public String               credits       = "";
  public String               director      = "";
  @XmlAnyElement(lax = true)
  private List<Object>        actors;
  @XmlAnyElement(lax = true)
  private List<Object>        producers;
  public boolean              watched       = false;
  public int                  playcount     = 0;
  public String               source        = "";

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
    genres = new ArrayList<>();
    fanart = new ArrayList<>();
    sets = new ArrayList<>();
    ids = new HashMap<>();
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

    MovieToMpNfoConnector mp = createInstanceFromMovie(movie);

    // and marshall it
    List<MovieNfoNaming> nfoNames = new ArrayList<MovieNfoNaming>();
    if (movie.isMultiMovieDir()) {
      // Fixate the name regardless of setting
      nfoNames.add(MovieNfoNaming.FILENAME_NFO);
    }
    else {
      nfoNames = MovieModuleManager.MOVIE_SETTINGS.getMovieNfoFilenames();
    }
    writeNfoFiles(movie, mp, nfoNames);
  }

  /**
   * create an instance from the given movie
   * 
   * @param movie
   *          the movie to create the instance for
   * @return the newly created instance
   */
  static MovieToMpNfoConnector createInstanceFromMovie(Movie movie) {
    MovieToMpNfoConnector mp = new MovieToMpNfoConnector();
    // set data
    mp.title = movie.getTitle();
    mp.originaltitle = movie.getOriginalTitle();

    mp.sorttitle = movie.getSortTitle();
    // if sort title is empty, insert the title sortable
    if (StringUtils.isBlank(mp.sorttitle)) {
      mp.sorttitle = movie.getTitleSortable();
    }

    mp.rating = movie.getRating();
    mp.votes = movie.getVotes();
    mp.year = movie.getYear();
    mp.premiered = movie.getReleaseDateFormatted();
    mp.plot = movie.getPlot();

    // outline is only the first 200 characters of the plot
    if (StringUtils.isNotBlank(mp.plot) && mp.plot.length() > 200) {
      int spaceIndex = mp.plot.indexOf(" ", 200);
      if (spaceIndex > 0) {
        mp.outline = mp.plot.substring(0, spaceIndex) + "...";
      }
      else {
        mp.outline = mp.plot;
      }
    }
    else if (StringUtils.isNotBlank(mp.plot)) {
      mp.outline = mp.plot;
    }

    mp.tagline = movie.getTagline();
    mp.runtime = String.valueOf(movie.getRuntime());
    mp.thumb = FilenameUtils.getName(movie.getArtworkFilename(MediaFileType.POSTER));

    // fanarts: is extrafanart is activated - put up to 5 fanarts into the NFO; else take the only fanart
    List<MediaFile> extrafanarts = movie.getMediaFiles(MediaFileType.EXTRAFANART);
    if (extrafanarts.size() > 0) {
      for (int i = 0; i < extrafanarts.size(); i++) {
        MediaFile mf = extrafanarts.get(i);

        File fanart = mf.getFile();
        String fanartPath = new File(movie.getPath()).toURI().relativize(fanart.toURI()).getPath();
        mp.fanart.add(fanartPath);

        if (i == 4) {
          break;
        }
      }
    }
    else {
      mp.fanart.add(FilenameUtils.getName(movie.getArtworkFilename(MediaFileType.FANART)));
    }
    mp.imdb = movie.getImdbId();
    mp.ids.putAll(movie.getIds());
    mp.studio = movie.getProductionCompany();
    mp.country = movie.getCountry();

    mp.watched = movie.isWatched();
    if (mp.watched) {
      mp.playcount = 1;
    }
    else {
      mp.playcount = 0;
    }

    // certification
    if (movie.getCertification() != null) {
      mp.mpaa = movie.getCertification().name();
    }

    if (movie.getMediaSource() != MovieMediaSource.UNKNOWN) {
      mp.source = movie.getMediaSource().name();
    }

    // // filename and path
    // if (movie.getMediaFiles(MediaFileType.VIDEO).size() > 0) {
    // mp.setFilenameandpath(movie.getPath() + File.separator + movie.getMediaFiles(MediaFileType.VIDEO).get(0).getFilename());
    // }

    mp.director = movie.getDirector();
    mp.credits = movie.getWriter();
    for (MovieActor cast : movie.getActors()) {
      mp.addActor(cast.getName(), cast.getCharacter(), cast.getThumbUrl());
    }

    for (MovieProducer producer : movie.getProducers()) {
      mp.addProducer(producer.getName(), producer.getRole(), producer.getThumbUrl());
    }

    for (MediaGenres genre : movie.getGenres()) {
      mp.genres.add(genre.toString());
    }

    // movie set
    if (movie.getMovieSet() != null) {
      MovieSet movieSet = movie.getMovieSet();
      MovieSets set = new MovieSets(movieSet.getTitle(), movieSet.getMovieIndex(movie) + 1);
      mp.sets.add(set);
    }

    return mp;
  }

  static void writeNfoFiles(Movie movie, MovieToMpNfoConnector mp, List<MovieNfoNaming> nfoNames) {
    String nfoFilename = "";
    List<MediaFile> newNfos = new ArrayList<MediaFile>(1);

    for (MovieNfoNaming name : nfoNames) {
      try {
        nfoFilename = movie.getNfoFilename(name);
        if (nfoFilename.isEmpty()) {
          continue;
        }

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
        MediaFile mf = new MediaFile(f);
        mf.gatherMediaInformation(true); // force to update filedate
        newNfos.add(mf);
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
      movie.setTitle(mp.title);
      movie.setOriginalTitle(mp.originaltitle);
      movie.setSortTitle(mp.sorttitle);

      movie.setRating(mp.rating);
      movie.setVotes(mp.votes);
      movie.setYear(mp.year);
      try {
        movie.setReleaseDate(mp.premiered);
      }
      catch (ParseException e) {
      }
      movie.setPlot(mp.plot);
      movie.setTagline(mp.tagline);
      try {
        String rt = mp.runtime.replaceAll("[^0-9]", "");
        movie.setRuntime(Integer.parseInt(rt));
      }
      catch (Exception e) {
        LOGGER.warn("could not parse runtime: " + mp.runtime);
      }

      for (Entry<String, Object> entry : mp.ids.entrySet()) {
        try {
          // reformat old ID styles
          if ("imdbId".equals(entry.getKey())) {
            movie.setId(Constants.IMDB, entry.getValue());
          }
          else if ("tmdbId".equals(entry.getKey())) {
            movie.setId(Constants.TMDB, entry.getValue());
          }
          else {
            movie.setId(entry.getKey(), entry.getValue());
          }
        }
        catch (Exception e) {
          LOGGER.warn("could not set ID: " + entry.getKey() + " ; " + entry.getValue());
        }
      }

      if (StringUtils.isBlank(movie.getImdbId())) {
        movie.setImdbId(mp.imdb);
      }

      movie.setDirector(mp.director);
      movie.setWriter(mp.credits);
      movie.setProductionCompany(mp.studio);
      movie.setCountry(mp.country);

      movie.setWatched(mp.watched);
      if (mp.playcount > 0) {
        movie.setWatched(true);
      }

      if (!StringUtils.isEmpty(mp.mpaa)) {
        movie.setCertification(MovieHelpers.parseCertificationStringForMovieSetupCountry(mp.mpaa));
      }

      if (StringUtils.isNotBlank(mp.source)) {
        try {
          MovieMediaSource source = MovieMediaSource.valueOf(mp.source);
          if (source != null) {
            movie.setMediaSource(source);
          }
        }
        catch (Exception ignored) {
        }
      }

      // movieset
      if (mp.sets != null && !mp.sets.isEmpty()) {
        MovieSets sets = mp.sets.get(0);
        // search for that movieset
        MovieList movieList = MovieList.getInstance();
        MovieSet movieSet = movieList.getMovieSet(sets.name, 0);

        // add movie to movieset
        if (movieSet != null) {
          movie.setMovieSet(movieSet);
          movie.setSortTitle(sets.name + String.format("%02d", sets.order));
        }
      }

      for (Actor actor : mp.getActors()) {
        MovieActor cast = new MovieActor(actor.name, actor.role);
        cast.setThumbUrl(actor.thumb);
        movie.addActor(cast);
      }

      for (Producer producer : mp.getProducers()) {
        MovieProducer cast = new MovieProducer(producer.name, producer.role);
        cast.setThumbUrl(producer.thumb);
        movie.addProducer(cast);
      }

      for (String genre : mp.genres) {
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

  protected static MovieToMpNfoConnector parseNFO(File nfoFile) throws Exception {
    Unmarshaller um = context.createUnmarshaller();
    if (um == null) {
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, nfoFile, "message.nfo.readerror"));
      throw new Exception("could not create unmarshaller");
    }

    Reader in = null;
    MovieToMpNfoConnector mp = null;
    try {
      in = new InputStreamReader(new FileInputStream(nfoFile), "UTF-8");
      mp = (MovieToMpNfoConnector) um.unmarshal(in);
    }
    catch (UnmarshalException e) {
    }
    catch (IllegalArgumentException e) {
    }
    finally {
      if (in != null) {
        in.close();
      }
    }

    if (mp == null) {
      // now trying to parse it via string
      String completeNFO = FileUtils.readFileToString(nfoFile, "UTF-8").trim().replaceFirst("^([\\W]+)<", "<");
      completeNFO = completeNFO.replace("<movie>",
          "<movie xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
      try {
        in = new StringReader(ParserUtils.cleanNfo(completeNFO));
        mp = (MovieToMpNfoConnector) um.unmarshal(in);
      }
      finally {
        if (in != null) {
          in.close();
        }
      }
    }
    return mp;

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

  // inner class actor to represent actors
  @XmlRootElement(name = "actor")
  public static class Actor {
    public String name;
    public String role;
    public String thumb;

    public Actor() {
    }

    public Actor(String name, String role, String thumb) {
      this.name = name;
      this.role = role;
      this.thumb = thumb;
    }
  }

  /*
   * inner class to represent producers
   */
  @XmlRootElement(name = "producer")
  public static class Producer {
    public String name;
    public String role;
    public String thumb;

    public Producer() {
    }

    public Producer(String name, String role, String thumb) {
      this.name = name;
      this.role = role;
      this.thumb = thumb;
    }
  }

  // inner class actor to represent movie sets
  public static class MovieSets {
    @XmlValue
    public String name;
    @XmlAttribute(name = "order")
    public int    order;

    public MovieSets() {

    }

    public MovieSets(String name, int order) {
      this.name = name;
      this.order = order;
    }
  }
}
