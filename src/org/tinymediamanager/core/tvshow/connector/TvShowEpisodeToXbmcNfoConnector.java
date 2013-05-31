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
package org.tinymediamanager.core.tvshow.connector;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.tvshow.TvShowActor;
import org.tinymediamanager.core.tvshow.TvShowEpisode;

/**
 * The Class tvShowEpisodeEpisodeToXbmcNfoConnector.
 * 
 * @author Manuel Laggner
 */
@XmlRootElement(name = "episodedetails")
@XmlType(propOrder = { "title", "showtitle", "rating", "votes", "season", "episode", "uniqueid", "plot", "thumb", "mpaa", "playcount", "lastplayed",
    "credits", "director", "aired", "premiered", "studio", "actors" })
public class TvShowEpisodeToXbmcNfoConnector {

  /** The Constant logger. */
  private static final Logger LOGGER    = LoggerFactory.getLogger(TvShowEpisodeToXbmcNfoConnector.class);

  /** The season. */
  private String              season    = "";

  /** The episode. */
  private String              episode   = "";

  /** The uniqueid. */
  private String              uniqueid  = "";

  /** The title. */
  private String              title     = "";

  /** The showtitle. */
  private String              showtitle = "";

  /** The rating. */
  private float               rating    = 0;

  /** The votes. */
  private int                 votes     = 0;

  /** The plot. */
  private String              plot      = "";

  /** The studio. */
  private String              studio    = "";

  /** The mpaa. */
  private String              mpaa      = "";

  /** The actors. */
  @XmlAnyElement(lax = true)
  private List<Object>        actors;

  /** the credits. */
  @XmlElement(name = "credits")
  private List<String>        credits;

  /** The director. */
  @XmlElement(name = "director")
  private List<String>        director;

  /** The aired. */
  private String              aired     = "";

  /** The premiered. */
  private String              premiered = "";

  /** not supported tags, but used to retrain in NFO. */

  @XmlElement
  String                      thumb;

  /** The playcount. */
  @XmlElement
  String                      playcount;

  /** The lastplayed. */
  @XmlElement
  String                      lastplayed;

  private static JAXBContext  context   = initContext();

  private static JAXBContext initContext() {
    try {
      return JAXBContext.newInstance(TvShowEpisodeToXbmcNfoConnector.class, Actor.class);
    }
    catch (JAXBException e) {
      LOGGER.error(e.getMessage());
    }
    return null;
  }

  /**
   * Instantiates a new tv show episode to xbmc nfo connector.
   */
  public TvShowEpisodeToXbmcNfoConnector() {
    actors = new ArrayList<Object>();
    director = new ArrayList<String>();
    credits = new ArrayList<String>();
  }

  /**
   * Sets the data.
   * 
   * @param tvShowEpisodes
   *          the tv show episodes
   * @return the string
   */
  public static String setData(List<TvShowEpisode> tvShowEpisodes) {
    if (context == null) {
      return "";
    }

    TvShowEpisode episode = tvShowEpisodes.get(0);
    String nfoFilename = FilenameUtils.getBaseName(episode.getMediaFiles(MediaFileType.VIDEO).get(0).getFilename()) + ".nfo";
    File nfoFile = new File(episode.getPath(), nfoFilename);

    // parse out all episodes from the nfo
    List<TvShowEpisodeToXbmcNfoConnector> xbmcConnectors = parseNfo(nfoFile);

    // process all episodes
    StringBuilder outputXml = new StringBuilder();
    for (int i = 0; i < tvShowEpisodes.size(); i++) {
      episode = tvShowEpisodes.get(i);

      // look in all parsed NFOs for this episode
      TvShowEpisodeToXbmcNfoConnector xbmc = null;
      for (TvShowEpisodeToXbmcNfoConnector con : xbmcConnectors) {
        if (String.valueOf(episode.getEpisode()).equals(con.episode) && String.valueOf(episode.getSeason()).equals(con.season)) {
          xbmc = con;
          break;
        }
      }

      if (xbmc == null) {
        // create a new connector
        xbmc = new TvShowEpisodeToXbmcNfoConnector();
      }

      xbmc.setTitle(episode.getTitle());
      xbmc.setShowtitle(episode.getTvShow().getTitle());
      xbmc.setRating(episode.getRating());
      xbmc.setSeason(String.valueOf(episode.getSeason()));
      xbmc.setEpisode(String.valueOf(episode.getEpisode()));
      xbmc.setPlot(episode.getPlot());
      xbmc.setAired(episode.getFirstAiredFormatted());
      xbmc.setPremiered(episode.getFirstAiredFormatted());
      xbmc.setStudio(episode.getTvShow().getStudio());
      if (episode.getId("tvdb") != null) {
        xbmc.setUniqueid(episode.getId("tvdb").toString());
      }
      xbmc.setMpaa(episode.getTvShow().getCertification().getName());

      xbmc.actors.clear();
      // actors for tv show episode (guests?)
      for (TvShowActor actor : episode.getActors()) {
        xbmc.addActor(actor.getName(), actor.getCharacter(), actor.getThumb());
      }

      // actors for tv show
      for (TvShowActor actor : episode.getTvShow().getActors()) {
        xbmc.addActor(actor.getName(), actor.getCharacter(), actor.getThumb());
      }

      // support of frodo director tags
      xbmc.director.clear();
      if (StringUtils.isNotEmpty(episode.getDirector())) {
        String directors[] = episode.getDirector().split(", ");
        for (String director : directors) {
          xbmc.addDirector(director);
        }
      }

      // support of frodo credits tags
      xbmc.credits.clear();
      if (StringUtils.isNotEmpty(episode.getWriter())) {
        String writers[] = episode.getWriter().split(", ");
        for (String writer : writers) {
          xbmc.addCredits(writer);
        }
      }

      // and marshall it
      try {
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        Writer w = new StringWriter();
        m.marshal(xbmc, w);
        StringBuilder sb = new StringBuilder(w.toString());
        w.close();

        // strip out <?xml..> on all xmls except the first
        if (i > 0) {
          sb = new StringBuilder(sb.toString().replaceAll("<\\?xml.*\\?>", ""));
        }

        // on windows make windows conform linebreaks
        if (SystemUtils.IS_OS_WINDOWS) {
          sb = new StringBuilder(sb.toString().replaceAll("(?<!\r)\n", "\r\n"));
        }

        outputXml.append(sb);

      }
      catch (JAXBException e) {
        LOGGER.error("setData", e);
      }
      catch (IOException e) {
        LOGGER.error("setData", e);
      }
    }

    try {
      FileUtils.write(nfoFile, outputXml, "UTF-8");
    }
    catch (IOException e) {
      LOGGER.error("setData", e);
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
  public static List<TvShowEpisode> getData(String nfoFilename) {
    // try to parse XML
    List<TvShowEpisode> episodes = new ArrayList<TvShowEpisode>(1);

    if (context == null) {
      return episodes;
    }

    // parse out all episodes from the nfo
    File nfoFile = new File(nfoFilename);
    List<TvShowEpisodeToXbmcNfoConnector> xbmcConnectors = parseNfo(nfoFile);

    for (TvShowEpisodeToXbmcNfoConnector xbmc : xbmcConnectors) {
      // only continue, if there is a title in the nfo
      if (StringUtils.isEmpty(xbmc.getTitle())) {
        continue;
      }

      TvShowEpisode episode = new TvShowEpisode();
      episode.setTitle(xbmc.getTitle());
      episode.setPlot(xbmc.getPlot());
      episode.setRating(xbmc.getRating());

      try {
        episode.setEpisode(Integer.valueOf(xbmc.getEpisode()));
        episode.setSeason(Integer.valueOf(xbmc.getSeason()));
      }
      catch (NumberFormatException e) {
      }

      episode.setVotes(xbmc.getVotes());

      // convert director to internal format
      String director = "";
      for (String dir : xbmc.getDirector()) {
        if (!StringUtils.isEmpty(director)) {
          director += ", ";
        }
        director += dir;
      }
      episode.setDirector(director);

      // convert writer to internal format
      String writer = "";
      for (String wri : xbmc.getCredits()) {
        if (StringUtils.isNotEmpty(writer)) {
          writer += ", ";
        }
        writer += wri;
      }
      episode.setWriter(writer);

      try {
        episode.setFirstAired(xbmc.getAired());
      }
      catch (ParseException e) {
      }

      // now there is the complicated part: tv show actors should be on the tv show level
      // episode "guests" should be on the episode level
      // BUT: at this moment there is no information about the tv show, so we parse them all into the episode
      for (Actor actor : xbmc.getActors()) {
        TvShowActor cast = new TvShowActor(actor.getName(), actor.getRole());
        cast.setThumb(actor.getThumb());
        episode.addActor(cast);
      }

      // set only the name w/o path
      episode.setNfoFilename(FilenameUtils.getName(nfoFilename));

      episodes.add(episode);
    }

    return episodes;
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
   * Gets the uniqueid.
   * 
   * @return the uniqueid
   */
  @XmlElement(name = "uniqueid")
  public String getUniqueid() {
    return uniqueid;
  }

  /**
   * Sets the uniqueid.
   * 
   * @param uniqueid
   *          the new uniqueid
   */
  public void setUniqueid(String uniqueid) {
    this.uniqueid = uniqueid;
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
   * Gets the season.
   * 
   * @return the season
   */
  @XmlElement(name = "season")
  public String getSeason() {
    return season;
  }

  /**
   * Gets the episode.
   * 
   * @return the episode
   */
  @XmlElement(name = "episode")
  public String getEpisode() {
    return episode;
  }

  /**
   * Gets the showtitle.
   * 
   * @return the showtitle
   */
  @XmlElement(name = "showtitle")
  public String getShowtitle() {
    return showtitle;
  }

  /**
   * Sets the season.
   * 
   * @param season
   *          the new season
   */
  public void setSeason(String season) {
    this.season = season;
  }

  /**
   * Sets the episode.
   * 
   * @param episode
   *          the new episode
   */
  public void setEpisode(String episode) {
    this.episode = episode;
  }

  /**
   * Gets the mpaa.
   * 
   * @return the mpaa
   */
  @XmlElement(name = "mpaa")
  public String getMpaa() {
    return this.mpaa;
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
   * Sets the showtitle.
   * 
   * @param showtitle
   *          the new showtitle
   */
  public void setShowtitle(String showtitle) {
    this.showtitle = showtitle;
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
   * Gets the aired.
   * 
   * @return the aired
   */
  @XmlElement(name = "aired")
  public String getAired() {
    return aired;
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
   * Sets the aired.
   * 
   * @param aired
   *          the new aired
   */
  public void setAired(String aired) {
    this.aired = aired;
  }

  /**
   * Gets the premiered.
   * 
   * @return the premiered
   */
  @XmlElement(name = "premiered")
  public String getPremiered() {
    return premiered;
  }

  /**
   * Sets the premiered.
   * 
   * @param premiered
   *          the new premiered
   */
  public void setPremiered(String premiered) {
    this.premiered = premiered;
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
   * Sets the rating.
   * 
   * @param rating
   *          the new rating
   */
  public void setRating(float rating) {
    this.rating = rating;
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

  private static List<TvShowEpisodeToXbmcNfoConnector> parseNfo(File nfoFile) {
    List<TvShowEpisodeToXbmcNfoConnector> xbmcConnectors = new ArrayList<TvShowEpisodeToXbmcNfoConnector>(1);

    // tv show episode NFO is a bit weird. There can be stored multiple
    // episodes inside one XML (in a non valid manner); so we have
    // to read the NFO, split it into some smaller NFOs and parse them
    if (nfoFile.exists()) {
      String completeNFO;
      try {
        completeNFO = FileUtils.readFileToString(nfoFile, "UTF-8");
        Pattern pattern = Pattern.compile("<\\?xml.*\\?>");
        Matcher matcher = pattern.matcher(completeNFO);
        String xmlHeader = "";
        if (matcher.find()) {
          xmlHeader = matcher.group();
        }

        pattern = Pattern.compile("<episodedetails>.+?<\\/episodedetails>", Pattern.DOTALL);
        matcher = pattern.matcher(completeNFO);
        while (matcher.find()) {
          StringBuilder sb = new StringBuilder(xmlHeader);
          sb.append(matcher.group());

          // read out each episode
          try {
            Unmarshaller um = context.createUnmarshaller();
            Reader in = new StringReader(sb.toString());
            TvShowEpisodeToXbmcNfoConnector xbmc = (TvShowEpisodeToXbmcNfoConnector) um.unmarshal(in);
            xbmcConnectors.add(xbmc);
          }
          catch (Exception e) {
            LOGGER.error("failed to parse " + nfoFile.getName(), e);
          }

        }
      }
      catch (IOException e) {
      }
    }

    return xbmcConnectors;
  }
}
