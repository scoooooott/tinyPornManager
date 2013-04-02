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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.core.movie.MovieFanartNaming;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.MoviePosterNaming;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieScrapers;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.movie.connector.MovieConnectors;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowScrapers;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.scraper.CountryCode;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.FanartSizes;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.Languages;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.PosterSizes;

/**
 * The Class Settings.
 * 
 * @author Manuel Laggner
 */
@XmlRootElement(name = "tinyMediaManager")
public class Settings extends AbstractModelObject {
  /** The Constant logger. */
  private static final Logger         LOGGER                      = Logger.getLogger(Settings.class);

  /** The instance. */
  private static Settings             instance;

  /** The Constant CONFIG_FILE. */
  private final static String         CONFIG_FILE                 = "config.xml";

  /** The Constant SCRAPER_TMDB_LANGU. */
  private final static String         SCRAPER_LANGU               = "scraperTmdbLanguage";

  /** The Constant TITLE_PREFIX. */
  private final static String         TITLE_PREFIX                = "titlePrefix";

  /** The Constant TITLE_PREFIX. */
  private final static String         PREFIX                      = "prefix";

  /** The Constant VIDEO_FILE_TYPE. */
  private final static String         VIDEO_FILE_TYPE             = "videoFileTypes";

  /** The Constant FILETYPE. */
  private final static String         FILETYPE                    = "filetype";

  /** The Constant PROXY_HOST. */
  private final static String         PROXY_HOST                  = "proxyHost";

  /** The Constant PROXY_PORT. */
  private final static String         PROXY_PORT                  = "proxyPort";

  /** The Constant PROXY_USERNAME. */
  private final static String         PROXY_USERNAME              = "proxyUsername";

  /** The Constant PROXY_PASSWORD. */
  private final static String         PROXY_PASSWORD              = "proxyPassword";

  /** The Constant CERTIFICATION_COUNTRY. */
  private final static String         CERTIFICATION_COUNTRY       = "certificationCountry";

  /** The Constant CLEAR_CACHE_SHUTDOWN. */
  private final static String         CLEAR_CACHE_SHUTDOWN        = "clearCacheShutdown";

  /** The video file types. */
  @XmlElementWrapper(name = TITLE_PREFIX)
  @XmlElement(name = PREFIX)
  private final List<String>          titlePrefix                 = ObservableCollections.observableList(new ArrayList<String>());

  /** The video file types. */
  @XmlElementWrapper(name = VIDEO_FILE_TYPE)
  @XmlElement(name = FILETYPE)
  private final List<String>          videoFileTypes              = ObservableCollections.observableList(new ArrayList<String>());

  /** The proxy host. */
  private String                      proxyHost;

  /** The proxy port. */
  private String                      proxyPort;

  /** The proxy username. */
  private String                      proxyUsername;

  /** The proxy password. */
  private String                      proxyPassword;

  /** The scraper tmdb language. */
  private Languages                   scraperLanguage             = Languages.en;

  /** The country for certification. */
  private CountryCode                 certificationCountry        = CountryCode.US;

  /** The dirty flag. */
  private boolean                     dirty                       = false;

  /** The clear cache on shutdown. */
  private boolean                     clearCacheShutdown          = false;

  /** The movie settings. */
  private MovieSettings               movieSettings               = null;

  /** The tv show settings. */
  private TvShowSettings              tvShowSettings              = null;

  /** The movieScraperMetadata configuration. */
  private MovieScraperMetadataConfig  movieScraperMetadataConfig  = null;

  /** The tvShowScraperMetadata configuration. */
  private TvShowScraperMetadataConfig tvShowScraperMetadataConfig = null;

  /** The window config. */
  private WindowConfig                windowConfig                = null;

  /** The property change listener. */
  private PropertyChangeListener      propertyChangeListener;

  /**
   * Instantiates a new settings.
   */
  private Settings() {
    propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        setDirty();
      }
    };
    addPropertyChangeListener(propertyChangeListener);

    // default values
    movieSettings = new MovieSettings();
    movieSettings.addPropertyChangeListener(propertyChangeListener);
    tvShowSettings = new TvShowSettings();
    tvShowSettings.addPropertyChangeListener(propertyChangeListener);
    movieScraperMetadataConfig = new MovieScraperMetadataConfig();
    movieScraperMetadataConfig.addPropertyChangeListener(propertyChangeListener);
    tvShowScraperMetadataConfig = new TvShowScraperMetadataConfig();
    tvShowScraperMetadataConfig.addPropertyChangeListener(propertyChangeListener);
    windowConfig = new WindowConfig();
    windowConfig.addPropertyChangeListener(propertyChangeListener);
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
          Reader in = new InputStreamReader(new FileInputStream(CONFIG_FILE), "UTF-8");
          Settings.instance = (Settings) um.unmarshal(in);
        }
        catch (FileNotFoundException e) {
          // e.printStackTrace();
          Settings.instance = new Settings();
          Settings.instance.writeDefaultSettings();
        }
        catch (IOException e) {
          // e.printStackTrace();
          Settings.instance = new Settings();
          Settings.instance.writeDefaultSettings();
        }
      }
      catch (JAXBException e) {
        LOGGER.error("getInstance", e);
      }

      Settings.instance.clearDirty();

    }
    return Settings.instance;
  }

  /**
   * Sets the dirty.
   */
  private void setDirty() {
    dirty = true;
  }

  /**
   * Clear dirty.
   */
  private void clearDirty() {
    dirty = false;
  }

  /**
   * Adds a title prefix.
   * 
   * @param prfx
   *          the prefix
   */
  public void addTitlePrefix(String prfx) {
    titlePrefix.add(prfx);
    firePropertyChange(TITLE_PREFIX, null, titlePrefix);
  }

  /**
   * Removes the video file type.
   * 
   * @param prfx
   *          the prfx
   */
  public void removeTitlePrefix(String prfx) {
    titlePrefix.remove(prfx);
    firePropertyChange(TITLE_PREFIX, null, titlePrefix);
  }

  /**
   * Gets the video file type.
   * 
   * @return the video file type
   */
  public List<String> getTitlePrefix() {
    return titlePrefix;
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
    // is there anything to save?
    if (!dirty) {
      return;
    }

    // create JAXB context and instantiate marshaller
    JAXBContext context;
    Writer w = null;
    try {
      context = JAXBContext.newInstance(Settings.class);
      Marshaller m = context.createMarshaller();
      m.setProperty("jaxb.encoding", "UTF-8");
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
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

    }
    catch (JAXBException e) {
      LOGGER.error("saveSettings", e);
    }
    catch (IOException e) {
      LOGGER.error("saveSettings", e);
    }
    finally {
      try {
        w.close();
      }
      catch (Exception e) {
        LOGGER.error("saveSettings", e);
      }
    }

    // set proxy information
    setProxy();

    // clear dirty flag
    clearDirty();
  }

  /**
   * Write default settings.
   */
  private void writeDefaultSettings() {
    // default video file types derived from
    // http://wiki.xbmc.org/index.php?title=Advancedsettings.xml#.3Cvideoextensions.3E
    addVideoFileTypes(".3gp");
    addVideoFileTypes(".asf");
    addVideoFileTypes(".asx");
    addVideoFileTypes(".avc");
    addVideoFileTypes(".avi");
    addVideoFileTypes(".bdmv");
    addVideoFileTypes(".bin");
    addVideoFileTypes(".bivx");
    addVideoFileTypes(".dat");
    addVideoFileTypes(".divx");
    addVideoFileTypes(".dv");
    addVideoFileTypes(".dvr-ms");
    addVideoFileTypes(".fli");
    addVideoFileTypes(".flv");
    addVideoFileTypes(".h264");
    addVideoFileTypes(".img");
    addVideoFileTypes(".iso");
    addVideoFileTypes(".mts");
    addVideoFileTypes(".mt2s");
    addVideoFileTypes(".m2ts");
    addVideoFileTypes(".m2v");
    addVideoFileTypes(".m4v");
    addVideoFileTypes(".mkv");
    addVideoFileTypes(".mov");
    addVideoFileTypes(".mp4");
    addVideoFileTypes(".mpeg");
    addVideoFileTypes(".mpg");
    addVideoFileTypes(".nrg");
    addVideoFileTypes(".nsv");
    addVideoFileTypes(".nuv");
    addVideoFileTypes(".ogm");
    addVideoFileTypes(".pva");
    addVideoFileTypes(".qt");
    addVideoFileTypes(".rm");
    addVideoFileTypes(".rmvb");
    addVideoFileTypes(".strm");
    addVideoFileTypes(".svq3");
    addVideoFileTypes(".ts");
    addVideoFileTypes(".ty");
    addVideoFileTypes(".viv");
    addVideoFileTypes(".vob");
    addVideoFileTypes(".vp3");
    addVideoFileTypes(".wmv");
    addVideoFileTypes(".xvid");

    // default title prefix
    titlePrefix.clear();
    addTitlePrefix("A");
    addTitlePrefix("An");
    addTitlePrefix("The");
    addTitlePrefix("Der");
    addTitlePrefix("Die");
    addTitlePrefix("Das");
    addTitlePrefix("Ein");
    addTitlePrefix("Eine");

    setScraperLanguage(Languages.en);
    setCertificationCountry(CountryCode.US);

    movieSettings.setImageTmdbPosterSize(PosterSizes.w342);
    movieSettings.setImageTmdbFanartSize(FanartSizes.original);
    movieSettings.setMovieConnector(MovieConnectors.XBMC);
    movieSettings.addMovieNfoFilename(MovieNfoNaming.FILENAME_NFO);
    movieSettings.addMoviePosterFilename(MoviePosterNaming.POSTER_JPG);
    movieSettings.addMoviePosterFilename(MoviePosterNaming.POSTER_PNG);
    movieSettings.addMovieFanartFilename(MovieFanartNaming.FANART_JPG);
    movieSettings.addMovieFanartFilename(MovieFanartNaming.FANART_PNG);
    movieSettings.setMovieScraper(MovieScrapers.TMDB);
    movieSettings.setImdbScrapeForeignLanguage(false);

    tvShowSettings.setTvShowScraper(TvShowScrapers.TVDB);

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
   * Sets the proxy.
   */
  public void setProxy() {
    if (useProxy()) {
      System.setProperty("proxyHost", getProxyHost());

      if (StringUtils.isNotEmpty(getProxyPort())) {
        System.setProperty("proxyPort", getProxyPort());
      }

      if (StringUtils.isNotEmpty(getProxyUsername())) {
        System.setProperty("http.proxyUser", getProxyUsername());
        System.setProperty("https.proxyUser", getProxyUsername());
      }
      if (StringUtils.isNotEmpty(getProxyPassword())) {
        System.setProperty("http.proxyPassword", getProxyPassword());
        System.setProperty("https.proxyPassword", getProxyPassword());
      }
      // System.setProperty("java.net.useSystemProxies", "true");
    }
  }

  /**
   * Should we use a proxy.
   * 
   * @return true, if successful
   */
  public boolean useProxy() {
    if (StringUtils.isNotEmpty(getProxyHost())) {
      return true;
    }
    return false;
  }

  /**
   * Gets the scraper language.
   * 
   * @return the scraper language
   */
  @XmlElement(name = SCRAPER_LANGU)
  public Languages getScraperLanguage() {
    return scraperLanguage;
  }

  /**
   * Sets the scraper language.
   * 
   * @param newValue
   *          the new scraper language
   */
  public void setScraperLanguage(Languages newValue) {
    Languages oldValue = this.scraperLanguage;
    this.scraperLanguage = newValue;
    firePropertyChange(SCRAPER_LANGU, oldValue, newValue);
  }

  /**
   * Gets the certification country.
   * 
   * @return the certification country
   */
  @XmlElement(name = CERTIFICATION_COUNTRY)
  public CountryCode getCertificationCountry() {
    return certificationCountry;
  }

  /**
   * Sets the certification country.
   * 
   * @param newValue
   *          the new certification country
   */
  public void setCertificationCountry(CountryCode newValue) {
    CountryCode oldValue = this.certificationCountry;
    certificationCountry = newValue;
    firePropertyChange(CERTIFICATION_COUNTRY, oldValue, newValue);
  }

  /**
   * Checks if is clear cache shutdown.
   * 
   * @return true, if is clear cache shutdown
   */
  public boolean isClearCacheShutdown() {
    return clearCacheShutdown;
  }

  /**
   * Sets the clear cache shutdown.
   * 
   * @param newValue
   *          the new clear cache shutdown
   */
  public void setClearCacheShutdown(boolean newValue) {
    boolean oldValue = this.clearCacheShutdown;
    this.clearCacheShutdown = newValue;
    firePropertyChange(CLEAR_CACHE_SHUTDOWN, oldValue, newValue);
  }

  /**
   * Sets the movie settings.
   * 
   * @param movieSettings
   *          the new movie settings
   */
  public void setMovieSettings(MovieSettings movieSettings) {
    this.movieSettings = movieSettings;
    this.movieSettings.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Gets the movie settings.
   * 
   * @return the movie settings
   */
  public MovieSettings getMovieSettings() {
    return this.movieSettings;
  }

  /**
   * Sets the tv show settings.
   * 
   * @param tvShowSettings
   *          the new tv show settings
   */
  public void setTvShowSettings(TvShowSettings tvShowSettings) {
    this.tvShowSettings = tvShowSettings;
    this.tvShowSettings.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Gets the tv show settings.
   * 
   * @return the tv show settings
   */
  public TvShowSettings getTvShowSettings() {
    return this.tvShowSettings;
  }

  /**
   * Gets the movie scraper metadata config.
   * 
   * @return the movie scraper metadata config
   */
  public MovieScraperMetadataConfig getMovieScraperMetadataConfig() {
    return movieScraperMetadataConfig;
  }

  /**
   * Sets the movie scraper metadata config.
   * 
   * @param scraperMetadataConfig
   *          the new movie scraper metadata config
   */
  public void setMovieScraperMetadataConfig(MovieScraperMetadataConfig scraperMetadataConfig) {
    this.movieScraperMetadataConfig = scraperMetadataConfig;
    this.movieScraperMetadataConfig.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Gets the tv show scraper metadata config.
   * 
   * @return the tv show scraper metadata config
   */
  public TvShowScraperMetadataConfig getTvShowScraperMetadataConfig() {
    return tvShowScraperMetadataConfig;
  }

  /**
   * Sets the tv show scraper metadata config.
   * 
   * @param scraperMetadataConfig
   *          the new tv show scraper metadata config
   */
  public void setTvShowScraperMetadataConfig(TvShowScraperMetadataConfig scraperMetadataConfig) {
    this.tvShowScraperMetadataConfig = scraperMetadataConfig;
    this.tvShowScraperMetadataConfig.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Gets the window config.
   * 
   * @return the window config
   */
  public WindowConfig getWindowConfig() {
    return windowConfig;
  }

  /**
   * Sets the window config.
   * 
   * @param windowConfig
   *          the new window config
   */
  public void setWindowConfig(WindowConfig windowConfig) {
    this.windowConfig = windowConfig;
    this.windowConfig.addPropertyChangeListener(propertyChangeListener);
  }
}
