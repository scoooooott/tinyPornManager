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
package org.tinymediamanager.core;

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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.FanartSizes;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.Languages;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.PosterSizes;

/**
 * The Class Settings.
 */
@XmlRootElement(name = "tinyMediaManager")
public class Settings extends AbstractModelObject {

  /** The Constant logger. */
  private static final Logger LOGGER             = Logger.getLogger(Settings.class);

  /** The instance. */
  private static Settings     instance;

  /** The Constant CONFIG_FILE. */
  private final static String CONFIG_FILE        = "config.xml";

  /** The Constant MOVIE_DATA_SOURCE. */
  private final static String MOVIE_DATA_SOURCE  = "movieDataSource";

  /** The Constant PATH. */
  private final static String PATH               = "path";

  /** The Constant VIDEO_FILE_TYPE. */
  private final static String VIDEO_FILE_TYPE    = "videoFileTypes";

  /** The Constant FILETYPE. */
  private final static String FILETYPE           = "filetype";

  /** The Constant PROXY_HOST. */
  private final static String PROXY_HOST         = "proxyHost";

  /** The Constant PROXY_PORT. */
  private final static String PROXY_PORT         = "proxyPort";

  /** The Constant PROXY_USERNAME. */
  private final static String PROXY_USERNAME     = "proxyUsername";

  /** The Constant PROXY_PASSWORD. */
  private final static String PROXY_PASSWORD     = "proxyPassword";

  /** The Constant SCRAPER_TMDB_LANGU. */
  private final static String SCRAPER_TMDB_LANGU = "scraperTmdbLanguage";

  /** The Constant IMAGE_TMDB_LANGU. */
  private final static String IMAGE_TMDB_LANGU   = "imageTmdbLanguage";

  /** The Constant IMAGE_TMDB_POSTER. */
  private final static String IMAGE_TMDB_POSTER  = "imageTmdbPosterSize";

  /** The Constant IMAGE_TMDB_FANART. */
  private final static String IMAGE_TMDB_FANART  = "imageTmdbFanartSize";

  /** The video file types. */
  @XmlElementWrapper(name = VIDEO_FILE_TYPE)
  @XmlElement(name = FILETYPE)
  private final List<String>  videoFileTypes     = new ArrayList<String>();

  /** The movie data sources. */
  @XmlElementWrapper(name = MOVIE_DATA_SOURCE)
  @XmlElement(name = PATH)
  private final List<String>  movieDataSources   = ObservableCollections.observableList(new ArrayList<String>());

  /** The proxy host. */
  private String              proxyHost;

  /** The proxy port. */
  private String              proxyPort;

  /** The proxy username. */
  private String              proxyUsername;

  /** The proxy password. */
  private String              proxyPassword;

  /** The scraper tmdb language. */
  private Languages           scraperTmdbLanguage;

  /** The image tmdb langugage. */
  private Languages           imageTmdbLangugage;

  /** The image tmdb poster size. */
  private PosterSizes         imageTmdbPosterSize;

  /** The image tmdb fanart size. */
  private FanartSizes         imageTmdbFanartSize;

  /**
   * Instantiates a new settings.
   */
  private Settings() {
  }

  /**
   * Gets the single instance of Settings.
   * 
   * @return single instance of Settings
   */
  public static Settings getInstance() {
    if (Settings.instance == null) {
      // try to parse XML
      JAXBContext context;
      try {
        context = JAXBContext.newInstance(Settings.class);
        Unmarshaller um = context.createUnmarshaller();
        try {
          Settings.instance = (Settings) um.unmarshal(new FileReader(CONFIG_FILE));
        } catch (FileNotFoundException e) {
          // e.printStackTrace();
          Settings.instance = new Settings();
          Settings.instance.writeDefaultSettings();
        } catch (IOException e) {
          // e.printStackTrace();
          Settings.instance = new Settings();
          Settings.instance.writeDefaultSettings();
        }
      } catch (JAXBException e) {
        LOGGER.error("getInstance", e);
      }

    }
    return Settings.instance;
  }

  /**
   * Adds the movie data sources.
   * 
   * @param path
   *          the path
   */
  public void addMovieDataSources(String path) {
    movieDataSources.add(path);
    firePropertyChange(MOVIE_DATA_SOURCE, null, movieDataSources);

  }

  /**
   * Removes the movie data sources.
   * 
   * @param path
   *          the path
   */
  public void removeMovieDataSources(String path) {
    movieDataSources.remove(path);
    firePropertyChange(MOVIE_DATA_SOURCE, null, movieDataSources);
  }

  /**
   * Gets the movie data source.
   * 
   * @return the movie data source
   */
  public List<String> getMovieDataSource() {
    return movieDataSources;
  }

  /**
   * Adds the video file types.
   * 
   * @param type
   *          the type
   */
  public void addVideoFileTypes(String type) {
    videoFileTypes.add(type);
    firePropertyChange(VIDEO_FILE_TYPE, null, videoFileTypes);
  }

  /**
   * Removes the video file type.
   * 
   * @param type
   *          the type
   */
  public void removeVideoFileType(String type) {
    videoFileTypes.remove(type);
    firePropertyChange(VIDEO_FILE_TYPE, null, videoFileTypes);
  }

  /**
   * Gets the video file type.
   * 
   * @return the video file type
   */
  public List<String> getVideoFileType() {
    return videoFileTypes;
  }

  /**
   * Save settings.
   */
  public void saveSettings() {
    // create JAXB context and instantiate marshaller
    JAXBContext context;
    Writer w = null;
    try {
      context = JAXBContext.newInstance(Settings.class);
      Marshaller m = context.createMarshaller();
      m.setProperty("jaxb.encoding", "Unicode");
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      // w = new FileWriter(CONFIG_FILE);
      w = new StringWriter();
      m.marshal(this, w);
      StringBuilder sb = new StringBuilder(w.toString());
      w.close();

      // on windows make windows conform linebreaks
      if (SystemUtils.IS_OS_WINDOWS) {
        sb = new StringBuilder(sb.toString().replaceAll("(?<!\r)\n", "\r\n"));
      }

      w = new FileWriter(CONFIG_FILE);
      String xml = sb.toString();
      IOUtils.write(xml, w);

    } catch (JAXBException e) {
      LOGGER.error("saveSettings", e);
    } catch (IOException e) {
      LOGGER.error("saveSettings", e);
    } finally {
      try {
        w.close();
      } catch (Exception e) {
        LOGGER.error("saveSettings", e);
      }
    }

    // set proxy information
    setProxy();
  }

  /**
   * Write default settings.
   */
  private void writeDefaultSettings() {
    // default video file types
    addVideoFileTypes(".mpg");
    addVideoFileTypes(".avi");
    addVideoFileTypes(".mp4");
    addVideoFileTypes(".mkv");

    setScraperTmdbLanguage(Languages.en);
    setImageTmdbLangugage(Languages.en);
    setImageTmdbPosterSize(PosterSizes.w342);
    setImageTmdbFanartSize(FanartSizes.original);

    saveSettings();
  }

  /**
   * Gets the proxy host.
   * 
   * @return the proxy host
   */
  @XmlElement(name = PROXY_HOST)
  public String getProxyHost() {
    return proxyHost;
  }

  /**
   * Sets the proxy host.
   * 
   * @param newValue
   *          the new proxy host
   */
  public void setProxyHost(String newValue) {
    String oldValue = this.proxyHost;
    this.proxyHost = newValue;
    firePropertyChange(PROXY_HOST, oldValue, newValue);
  }

  /**
   * Gets the proxy port.
   * 
   * @return the proxy port
   */
  @XmlElement(name = PROXY_PORT)
  public String getProxyPort() {
    return proxyPort;
  }

  /**
   * Sets the proxy port.
   * 
   * @param newValue
   *          the new proxy port
   */
  public void setProxyPort(String newValue) {
    String oldValue = this.proxyPort;
    this.proxyPort = newValue;
    firePropertyChange(PROXY_PORT, oldValue, newValue);
  }

  /**
   * Gets the proxy username.
   * 
   * @return the proxy username
   */
  @XmlElement(name = PROXY_USERNAME)
  public String getProxyUsername() {
    return proxyUsername;
  }

  /**
   * Sets the proxy username.
   * 
   * @param newValue
   *          the new proxy username
   */
  public void setProxyUsername(String newValue) {
    String oldValue = this.proxyUsername;
    this.proxyUsername = newValue;
    firePropertyChange(PROXY_USERNAME, oldValue, newValue);
  }

  /**
   * Gets the proxy password.
   * 
   * @return the proxy password
   */
  @XmlElement(name = PROXY_PASSWORD)
  public String getProxyPassword() {
    return StringEscapeUtils.unescapeXml(proxyPassword);
  }

  /**
   * Sets the proxy password.
   * 
   * @param newValue
   *          the new proxy password
   */
  public void setProxyPassword(String newValue) {
    newValue = StringEscapeUtils.escapeXml(newValue);
    String oldValue = this.proxyPassword;
    this.proxyPassword = newValue;
    firePropertyChange(PROXY_PASSWORD, oldValue, newValue);
  }

  /**
   * Gets the image tmdb langugage.
   * 
   * @return the image tmdb langugage
   */
  @XmlElement(name = IMAGE_TMDB_LANGU)
  public Languages getImageTmdbLangugage() {
    return imageTmdbLangugage;
  }

  /**
   * Sets the image tmdb langugage.
   * 
   * @param newValue
   *          the new image tmdb langugage
   */
  public void setImageTmdbLangugage(Languages newValue) {
    Languages oldValue = this.imageTmdbLangugage;
    this.imageTmdbLangugage = newValue;
    firePropertyChange(IMAGE_TMDB_LANGU, oldValue, newValue);
  }

  /**
   * Sets the proxy.
   */
  public void setProxy() {
    if (useProxy()) {
      System.setProperty("proxyPort", getProxyPort());
      System.setProperty("proxyHost", getProxyHost());
      System.setProperty("http.proxyUser", getProxyUsername());
      System.setProperty("http.proxyPassword", getProxyPassword());
      System.setProperty("https.proxyUser", getProxyUsername());
      System.setProperty("https.proxyPassword", getProxyPassword());
      // System.setProperty("java.net.useSystemProxies", "true");
    }
  }

  /**
   * Should we use a proxy
   */
  public boolean useProxy() {
    if (!StringUtils.isEmpty(getProxyHost()) && !StringUtils.isEmpty(getProxyPort())) {
      return true;
    }
    return false;
  }

  /**
   * Gets the image tmdb poster size.
   * 
   * @return the image tmdb poster size
   */
  @XmlElement(name = IMAGE_TMDB_POSTER)
  public TmdbMetadataProvider.PosterSizes getImageTmdbPosterSize() {
    return imageTmdbPosterSize;
  }

  /**
   * Sets the image tmdb poster size.
   * 
   * @param newValue
   *          the new image tmdb poster size
   */
  public void setImageTmdbPosterSize(TmdbMetadataProvider.PosterSizes newValue) {
    TmdbMetadataProvider.PosterSizes oldValue = this.imageTmdbPosterSize;
    this.imageTmdbPosterSize = newValue;
    firePropertyChange(IMAGE_TMDB_POSTER, oldValue, newValue);
  }

  /**
   * Gets the image tmdb fanart size.
   * 
   * @return the image tmdb fanart size
   */
  @XmlElement(name = IMAGE_TMDB_FANART)
  public FanartSizes getImageTmdbFanartSize() {
    return imageTmdbFanartSize;
  }

  /**
   * Sets the image tmdb fanart size.
   * 
   * @param newValue
   *          the new image tmdb fanart size
   */
  public void setImageTmdbFanartSize(FanartSizes newValue) {
    FanartSizes oldValue = this.imageTmdbFanartSize;
    this.imageTmdbFanartSize = newValue;
    firePropertyChange(IMAGE_TMDB_FANART, oldValue, newValue);
  }

  /**
   * Gets the scraper tmdb language.
   * 
   * @return the scraper tmdb language
   */
  @XmlElement(name = SCRAPER_TMDB_LANGU)
  public Languages getScraperTmdbLanguage() {
    return scraperTmdbLanguage;
  }

  /**
   * Sets the scraper tmdb language.
   * 
   * @param newValue
   *          the new scraper tmdb language
   */
  public void setScraperTmdbLanguage(Languages newValue) {
    Languages oldValue = this.scraperTmdbLanguage;
    this.scraperTmdbLanguage = newValue;
    firePropertyChange(SCRAPER_TMDB_LANGU, oldValue, newValue);
  }
}
