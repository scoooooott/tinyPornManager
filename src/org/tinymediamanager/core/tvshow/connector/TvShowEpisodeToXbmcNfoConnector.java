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
@XmlType(propOrder = { "title", "showtitle", "rating", "season", "episode", "plot", "actors" })
public class TvShowEpisodeToXbmcNfoConnector {

  /** The Constant logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(TvShowEpisodeToXbmcNfoConnector.class);

  /** The season. */
  private String              season;

  /** The episode. */
  private String              episode;

  /** The title. */
  private String              title;

  /** The showtitle. */
  private String              showtitle;

  /** The rating. */
  private float               rating;

  /** The plot. */
  private String              plot;

  /** The actors. */
  @XmlAnyElement(lax = true)
  private List<Object>        actors;

  /**
   * Instantiates a new tv show episode to xbmc nfo connector.
   */
  public TvShowEpisodeToXbmcNfoConnector() {
    actors = new ArrayList<Object>();
  }

  /**
   * Sets the data.
   * 
   * @param tvShowEpisode
   *          the tv show
   * @return the string
   */
  public static String setData(List<TvShowEpisode> tvShowEpisodes) {
    List<TvShowEpisodeToXbmcNfoConnector> xbmcConnectors = new ArrayList<TvShowEpisodeToXbmcNfoConnector>();
    JAXBContext context = null;

    // tv show episode NFO is a bit weird. There can be stored multiple
    // episodes inside one XML (in a non valid manner); so we have
    // to read the NFO, split it into some smaller NFOs and parse them
    TvShowEpisode episode = tvShowEpisodes.get(0);
    String nfoFilename = FilenameUtils.getBaseName(episode.getMediaFiles(MediaFileType.VIDEO).get(0).getFilename()) + ".nfo";
    File nfoFile = new File(episode.getPath(), nfoFilename);

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
            synchronized (JAXBContext.class) {
              context = JAXBContext.newInstance(TvShowEpisodeToXbmcNfoConnector.class, Actor.class);
            }
            Unmarshaller um = context.createUnmarshaller();
            Reader in = new StringReader(sb.toString());
            TvShowEpisodeToXbmcNfoConnector xbmc = (TvShowEpisodeToXbmcNfoConnector) um.unmarshal(in);
            xbmcConnectors.add(xbmc);
          }
          catch (Exception e) {
            LOGGER.error("failed to parse " + nfoFilename, e);
          }

        }
      }
      catch (IOException e) {
      }
    }

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
      xbmc.actors.clear();
      // TODO actors for tv shows? guests?
      // for (TvShowActor actor : episode.getActors()) {
      // xbmc.addActor(actor.getName(), actor.getCharacter(), actor.getThumb());
      // }
      for (TvShowActor actor : episode.getTvShow().getActors()) {
        xbmc.addActor(actor.getName(), actor.getCharacter(), actor.getThumb());
      }

      // and marshall it
      try {

        synchronized (JAXBContext.class) {
          context = JAXBContext.newInstance(TvShowEpisodeToXbmcNfoConnector.class, Actor.class);
        }
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        Writer w = new StringWriter();
        m.marshal(xbmc, w);
        StringBuilder sb = new StringBuilder(w.toString());
        w.close();

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

    // // load existing NFO if possible
    // if (nfoFile.exists()) {
    // try {
    // synchronized (JAXBContext.class) {
    // context = JAXBContext.newInstance(tvShowEpisodeEpisodeToXbmcNfoConnector.class, Actor.class);
    // }
    // Unmarshaller um = context.createUnmarshaller();
    // Reader in = new InputStreamReader(new FileInputStream(nfoFile), "UTF-8");
    // xbmc = (tvShowEpisodeEpisodeToXbmcNfoConnector) um.unmarshal(in);
    // }
    // catch (Exception e) {
    // LOGGER.error("failed to parse " + nfoFilename, e);
    // }
    // }
    //
    // // create new
    // if (xbmc == null) {
    // xbmc = new tvShowEpisodeEpisodeToXbmcNfoConnector();
    // }
    //
    // // set data
    // xbmc.setTitle(tvShowEpisode.getTitle());
    // xbmc.setRating(tvShowEpisode.getRating());
    // xbmc.setVotes(tvShowEpisode.getVotes());
    // xbmc.setPlot(tvShowEpisode.getPlot());
    //
    // xbmc.actors.clear();
    // for (tvShowEpisodeActor actor : tvShowEpisode.getActors()) {
    // xbmc.addActor(actor.getName(), actor.getCharacter(), actor.getThumb());
    // }
    //
    // // and marshall it
    // try {
    //
    // synchronized (JAXBContext.class) {
    // context = JAXBContext.newInstance(tvShowEpisodeEpisodeToXbmcNfoConnector.class, Actor.class);
    // }
    // Marshaller m = context.createMarshaller();
    // m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
    // m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    //
    // Writer w = new StringWriter();
    // m.marshal(xbmc, w);
    // StringBuilder sb = new StringBuilder(w.toString());
    // w.close();
    //
    // // on windows make windows conform linebreaks
    // if (SystemUtils.IS_OS_WINDOWS) {
    // sb = new StringBuilder(sb.toString().replaceAll("(?<!\r)\n", "\r\n"));
    // }
    // FileUtils.write(nfoFile, sb, "UTF-8");
    // }
    // catch (JAXBException e) {
    // LOGGER.error("setData", e);
    // }
    // catch (IOException e) {
    // LOGGER.error("setData", e);
    // }
    //
    // // return only the name w/o path
    // return nfoFilename;
    return nfoFilename;
  }

  /**
   * Gets the data.
   * 
   * @param nfoFilename
   *          the nfo filename
   * @return the data
   */
  public static TvShowEpisode getData(String nfoFilename) {
    // // try to parse XML
    // JAXBContext context;
    TvShowEpisode tvShowEpisode = null;
    // try {
    // synchronized (JAXBContext.class) {
    // context = JAXBContext.newInstance(tvShowEpisodeEpisodeToXbmcNfoConnector.class, Actor.class);
    // }
    // Unmarshaller um = context.createUnmarshaller();
    // Reader in = new InputStreamReader(new FileInputStream(nfoFilename), "UTF-8");
    // tvShowEpisodeEpisodeToXbmcNfoConnector xbmc = (tvShowEpisodeEpisodeToXbmcNfoConnector) um.unmarshal(in);
    // tvShowEpisode = new tvShowEpisode();
    //
    // tvShowEpisode.setTitle(xbmc.getTitle());
    // tvShowEpisode.setRating(xbmc.getRating());
    // tvShowEpisode.setVotes(xbmc.getVotes());
    // tvShowEpisode.setPlot(xbmc.getPlot());
    //
    // for (Actor actor : xbmc.getActors()) {
    // tvShowEpisodeActor tvShowEpisodeActor = new tvShowEpisodeActor(actor.getName(), actor.getRole());
    // tvShowEpisodeActor.setThumb(actor.getThumb());
    // tvShowEpisode.addActor(tvShowEpisodeActor);
    // }
    // }
    // catch (FileNotFoundException e) {
    // LOGGER.error("setData", e);
    // return null;
    // }
    //
    // catch (Exception e) {
    // LOGGER.error("setData", e);
    // return null;
    // }
    //
    // // only return if a movie name has been found
    // if (StringUtils.isEmpty(tvShowEpisode.getTitle())) {
    // return null;
    // }
    return tvShowEpisode;
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
}
