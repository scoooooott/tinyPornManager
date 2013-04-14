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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.scraper.MediaGenres;

/**
 * The Class TvShowToXbmcNfoConnector.
 * 
 * @author Manuel Laggner
 */
@XmlRootElement(name = "tvshow")
public class TvShowToXbmcNfoConnector {

  /** The Constant logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(TvShowToXbmcNfoConnector.class);

  /** The id. */
  private String              id;

  /** The title. */
  private String              title;

  /** The rating. */
  private float               rating;

  /** The votes. */
  private int                 votes;

  /** The year. */
  private String              year;

  /** The plot. */
  private String              plot;

  /** The genres. */
  @XmlElement(name = "genre")
  private List<String>        genres;

  /**
   * Instantiates a new tv show to xbmc nfo connector.
   */
  public TvShowToXbmcNfoConnector() {
    genres = new ArrayList<String>();
  }

  /**
   * Sets the data.
   * 
   * @param tvShow
   *          the tv show
   * @return the string
   */
  public static String setData(TvShow tvShow) {
    TvShowToXbmcNfoConnector xbmc = null;
    JAXBContext context = null;
    String nfoFilename = "tvshow.nfo";
    File nfoFile = new File(tvShow.getPath(), nfoFilename);

    // load existing NFO if possible
    if (nfoFile.exists()) {
      try {
        synchronized (JAXBContext.class) {
          context = JAXBContext.newInstance(TvShowToXbmcNfoConnector.class);
        }
        Unmarshaller um = context.createUnmarshaller();
        Reader in = new InputStreamReader(new FileInputStream(nfoFile), "UTF-8");
        xbmc = (TvShowToXbmcNfoConnector) um.unmarshal(in);
      }
      catch (Exception e) {
        LOGGER.error("failed to parse " + nfoFilename, e);
      }
    }

    // create new
    if (xbmc == null) {
      xbmc = new TvShowToXbmcNfoConnector();
    }

    // set data
    xbmc.setId(tvShow.getId("tvdb").toString());
    xbmc.setTitle(tvShow.getTitle());
    xbmc.setRating(tvShow.getRating());
    xbmc.setVotes(tvShow.getVotes());
    xbmc.setPlot(tvShow.getPlot());
    xbmc.setYear(tvShow.getYear());

    xbmc.genres.clear();
    for (MediaGenres genre : tvShow.getGenres()) {
      xbmc.addGenre(genre.toString());
    }

    // and marshall it
    try {

      synchronized (JAXBContext.class) {
        context = JAXBContext.newInstance(TvShowToXbmcNfoConnector.class);
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
      FileUtils.write(nfoFile, sb, "UTF-8");
    }
    catch (JAXBException e) {
      LOGGER.error("setData", e);
    }
    catch (IOException e) {
      LOGGER.error("setData", e);
    }

    // return only the name w/o path
    return nfoFilename;
  }

  public static TvShow getData(String nfoFilename) {
    // try to parse XML
    JAXBContext context;
    TvShow tvShow = null;
    try {
      synchronized (JAXBContext.class) {
        context = JAXBContext.newInstance(TvShowToXbmcNfoConnector.class);
      }
      Unmarshaller um = context.createUnmarshaller();
      Reader in = new InputStreamReader(new FileInputStream(nfoFilename), "UTF-8");
      TvShowToXbmcNfoConnector xbmc = (TvShowToXbmcNfoConnector) um.unmarshal(in);
      tvShow = new TvShow();
      if (StringUtils.isNotBlank(xbmc.getId())) {
        tvShow.setId("tvdb", xbmc.getId());
      }
      tvShow.setTitle(xbmc.getTitle());
      tvShow.setRating(xbmc.getRating());
      tvShow.setVotes(xbmc.getVotes());
      tvShow.setYear(xbmc.getYear());
      tvShow.setPlot(xbmc.getPlot());

      for (String genre : xbmc.getGenres()) {
        String[] genres = genre.split("/");
        for (String g : genres) {
          MediaGenres genreFound = MediaGenres.getGenre(g.trim());
          if (genreFound != null) {
            tvShow.addGenre(genreFound);
          }
        }
      }
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
    if (StringUtils.isEmpty(tvShow.getTitle())) {
      return null;
    }
    return tvShow;
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
   * Sets the plot.
   * 
   * @param plot
   *          the new plot
   */
  public void setPlot(String plot) {
    this.plot = plot;
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
}
