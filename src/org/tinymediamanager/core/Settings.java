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
package org.tinymediamanager.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.core.ImageCache.CacheType;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.movie.MovieFanartNaming;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.MoviePosterNaming;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowSettings;

import ch.qos.logback.classic.Level;

/**
 * The Class Settings - holding all settings for tmm.
 * 
 * @author Manuel Laggner
 */
@XmlRootElement(name = "tinyMediaManager")
public class Settings extends AbstractModelObject {
  private static final Logger         LOGGER                      = LoggerFactory.getLogger(Settings.class);
  private static Settings             instance;

  /**
   * Constants mainly for events
   */
  private final static String         CONFIG_FILE                 = "config.xml";
  private final static String         TITLE_PREFIX                = "titlePrefix";
  private final static String         PREFIX                      = "prefix";
  private final static String         VIDEO_FILE_TYPE             = "videoFileTypes";
  private final static String         AUDIO_FILE_TYPE             = "audioFileTypes";
  private final static String         SUBTITLE_FILE_TYPE          = "subtitleFileTypes";
  private final static String         FILETYPE                    = "filetype";
  private final static String         PROXY_HOST                  = "proxyHost";
  private final static String         PROXY_PORT                  = "proxyPort";
  private final static String         PROXY_USERNAME              = "proxyUsername";
  private final static String         PROXY_PASSWORD              = "proxyPassword";
  private final static String         CLEAR_CACHE_SHUTDOWN        = "clearCacheShutdown";
  private final static String         LOG_LEVEL                   = "logLevel";
  private final static String         IMAGE_CACHE                 = "imageCache";
  private final static String         IMAGE_CACHE_TYPE            = "imageCacheType";
  private final static String         IMAGE_CACHE_BACKGROUND      = "imageCacheBackground";
  private final static String         LANGUAGE                    = "language";
  private final static String         WOL_DEVICES                 = "wolDevices";

  @XmlElementWrapper(name = TITLE_PREFIX)
  @XmlElement(name = PREFIX)
  private final List<String>          titlePrefix                 = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = VIDEO_FILE_TYPE)
  @XmlElement(name = FILETYPE)
  private final List<String>          videoFileTypes              = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = AUDIO_FILE_TYPE)
  @XmlElement(name = FILETYPE)
  private final List<String>          audioFileTypes              = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = SUBTITLE_FILE_TYPE)
  @XmlElement(name = FILETYPE)
  private final List<String>          subtitleFileTypes           = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = WOL_DEVICES)
  private final List<WolDevice>       wolDevices                  = ObservableCollections.observableList(new ArrayList<WolDevice>());

  @XmlAttribute
  private String                      version                     = "";

  private String                      proxyHost;
  private String                      proxyPort;
  private String                      proxyUsername;
  private String                      proxyPassword;
  private int                         logLevel                    = Level.DEBUG_INT;
  private boolean                     imageCache                  = true;
  private CacheType                   imageCacheType              = CacheType.SMOOTH;
  private boolean                     imageCacheBackground        = false;
  private boolean                     dirty                       = false;
  private boolean                     clearCacheShutdown          = false;
  private MovieSettings               movieSettings               = null;
  private TvShowSettings              tvShowSettings              = null;
  private MovieScraperMetadataConfig  movieScraperMetadataConfig  = null;
  private TvShowScraperMetadataConfig tvShowScraperMetadataConfig = null;
  private WindowConfig                windowConfig                = null;
  // language 2 char - saved to config
  private String                      language;

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
        catch (Exception e) {
          Settings.instance = new Settings();
          Settings.instance.writeDefaultSettings();
        }
      }
      catch (Exception e) {
        LOGGER.error("getInstance", e);
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "tmm.settings", "message.config.loadsettingserror"));
      }

      Settings.instance.clearDirty();

    }
    return Settings.instance;
  }

  /**
   * is our settings file up2date?
   */
  public boolean isCurrentVersion() {
    return ReleaseInfo.getVersion().equals(version);
  }

  /**
   * gets the version of out settings file
   */
  public String getVersion() {
    return version;
  }

  /**
   * sets the current version into settings file
   */
  public void setCurrentVersion() {
    version = ReleaseInfo.getVersion();
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
    if (!titlePrefix.contains(prfx)) {
      titlePrefix.add(prfx);
      firePropertyChange(TITLE_PREFIX, null, titlePrefix);
    }
  }

  /**
   * Removes the title prefix.
   * 
   * @param prfx
   *          the prfx
   */
  public void removeTitlePrefix(String prfx) {
    titlePrefix.remove(prfx);
    firePropertyChange(TITLE_PREFIX, null, titlePrefix);
  }

  /**
   * Gets the title prefix.
   * 
   * @return the title prefix
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
    if (!type.startsWith(".")) {
      type = "." + type;
    }
    if (!videoFileTypes.contains(type)) {
      videoFileTypes.add(type);
      firePropertyChange(VIDEO_FILE_TYPE, null, videoFileTypes);
    }
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
   * Adds the audio file types.
   * 
   * @param type
   *          the type
   */
  public void addAudioFileTypes(String type) {
    if (!type.startsWith(".")) {
      type = "." + type;
    }
    if (!audioFileTypes.contains(type)) {
      audioFileTypes.add(type);
      firePropertyChange(AUDIO_FILE_TYPE, null, audioFileTypes);
    }
  }

  /**
   * Removes the audio file type.
   * 
   * @param type
   *          the type
   */
  public void removeAudioFileType(String type) {
    audioFileTypes.remove(type);
    firePropertyChange(AUDIO_FILE_TYPE, null, audioFileTypes);
  }

  /**
   * Gets the audio file type.
   * 
   * @return the audio file type
   */
  public List<String> getAudioFileType() {
    return audioFileTypes;
  }

  /**
   * Adds the subtitle file types.
   * 
   * @param type
   *          the type
   */
  public void addSubtitleFileTypes(String type) {
    if (!type.startsWith(".")) {
      type = "." + type;
    }
    if (!subtitleFileTypes.contains(type)) {
      subtitleFileTypes.add(type);
      firePropertyChange(SUBTITLE_FILE_TYPE, null, subtitleFileTypes);
    }
  }

  /**
   * Removes the subtitle file type.
   * 
   * @param type
   *          the type
   */
  public void removeSubtitleFileType(String type) {
    subtitleFileTypes.remove(type);
    firePropertyChange(SUBTITLE_FILE_TYPE, null, subtitleFileTypes);
  }

  /**
   * Gets the subtitle file type.
   * 
   * @return the subtitle file type
   */
  public List<String> getSubtitleFileType() {
    return subtitleFileTypes;
  }

  /**
   * Convenience method to get all supported file extensions
   * 
   * @return list
   */
  public List<String> getAllSupportedFileTypes() {
    List<String> list = new ArrayList<String>();
    list.addAll(getAudioFileType());
    list.addAll(getVideoFileType());
    list.addAll(getSubtitleFileType());
    list.add(".nfo");
    return list;
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
    catch (Exception e) {
      LOGGER.error("saveSettings", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "tmm.settings", "message.config.savesettingserror"));
    }
    finally {
      try {
        w.close();
      }
      catch (Exception e) {
        LOGGER.error("saveSettings", e);
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "tmm.settings", "message.config.savesettingserror"));
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
  public void writeDefaultSettings() {
    version = ReleaseInfo.getVersion();

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
    addVideoFileTypes(".disc"); // video stubs
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
    Collections.sort(videoFileTypes);

    addAudioFileTypes(".a52");
    addAudioFileTypes(".aa3");
    addAudioFileTypes(".aac");
    addAudioFileTypes(".ac3");
    addAudioFileTypes(".adt");
    addAudioFileTypes(".adts");
    addAudioFileTypes(".aif");
    addAudioFileTypes(".aiff");
    addAudioFileTypes(".alac");
    addAudioFileTypes(".ape");
    addAudioFileTypes(".at3");
    addAudioFileTypes(".atrac");
    addAudioFileTypes(".au");
    addAudioFileTypes(".dts");
    addAudioFileTypes(".flac");
    addAudioFileTypes(".m4a");
    addAudioFileTypes(".m4b");
    addAudioFileTypes(".m4p");
    addAudioFileTypes(".mid");
    addAudioFileTypes(".midi");
    addAudioFileTypes(".mka");
    addAudioFileTypes(".mp3");
    addAudioFileTypes(".mpa");
    addAudioFileTypes(".oga");
    addAudioFileTypes(".ogg");
    addAudioFileTypes(".pcm");
    addAudioFileTypes(".ra");
    addAudioFileTypes(".ram");
    addAudioFileTypes(".rm");
    addAudioFileTypes(".tta");
    addAudioFileTypes(".wav");
    addAudioFileTypes(".wave");
    addAudioFileTypes(".wma");
    Collections.sort(audioFileTypes);

    // default subtitle files
    addSubtitleFileTypes(".aqt");
    addSubtitleFileTypes(".cvd");
    addSubtitleFileTypes(".dks");
    addSubtitleFileTypes(".jss");
    addSubtitleFileTypes(".sub");
    addSubtitleFileTypes(".ttxt");
    addSubtitleFileTypes(".mpl");
    addSubtitleFileTypes(".pjs");
    addSubtitleFileTypes(".psb");
    addSubtitleFileTypes(".rt");
    addSubtitleFileTypes(".srt");
    addSubtitleFileTypes(".smi");
    addSubtitleFileTypes(".ssf");
    addSubtitleFileTypes(".ssa");
    addSubtitleFileTypes(".svcd");
    addSubtitleFileTypes(".usf");
    addSubtitleFileTypes(".idx");
    addSubtitleFileTypes(".ass");
    addSubtitleFileTypes(".pgs");
    addSubtitleFileTypes(".vobsub");
    Collections.sort(subtitleFileTypes);

    // default title prefix
    addTitlePrefix("A");
    addTitlePrefix("An");
    addTitlePrefix("The");
    addTitlePrefix("Der");
    addTitlePrefix("Die");
    addTitlePrefix("Das");
    addTitlePrefix("Ein");
    addTitlePrefix("Eine");
    Collections.sort(titlePrefix);

    movieSettings.addMovieNfoFilename(MovieNfoNaming.MOVIE_NFO);
    movieSettings.addMoviePosterFilename(MoviePosterNaming.POSTER_JPG);
    movieSettings.addMoviePosterFilename(MoviePosterNaming.POSTER_PNG);
    movieSettings.addMovieFanartFilename(MovieFanartNaming.FANART_JPG);
    movieSettings.addMovieFanartFilename(MovieFanartNaming.FANART_PNG);

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
   * Gets the log level.
   * 
   * @return the log level
   */
  @XmlElement(name = LOG_LEVEL)
  public int getLogLevel() {
    return logLevel;
  }

  /**
   * Sets the log level.
   * 
   * @param newValue
   *          the new log level
   */
  public void setLogLevel(int newValue) {
    int oldValue = this.logLevel;
    this.logLevel = newValue;

    ch.qos.logback.classic.Logger tl = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.tinymediamanager");
    tl.setLevel(Level.toLevel(logLevel));

    firePropertyChange(LOG_LEVEL, oldValue, newValue);
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

  /**
   * Checks if is image cache.
   * 
   * @return true, if is image cache
   */
  public boolean isImageCache() {
    return imageCache;
  }

  /**
   * Sets the image cache.
   * 
   * @param newValue
   *          the new image cache
   */
  public void setImageCache(boolean newValue) {
    boolean oldValue = this.imageCache;
    this.imageCache = newValue;
    firePropertyChange(IMAGE_CACHE, oldValue, newValue);
  }

  /**
   * Gets the image cache type.
   * 
   * @return the image cache type
   */
  public CacheType getImageCacheType() {
    return imageCacheType;
  }

  /**
   * Checks if is image cache background.
   * 
   * @return true, if is image cache background
   */
  public boolean isImageCacheBackground() {
    return imageCacheBackground;
  }

  /**
   * Sets the image cache type.
   * 
   * @param newValue
   *          the new image cache type
   */
  public void setImageCacheType(CacheType newValue) {
    CacheType oldValue = this.imageCacheType;
    this.imageCacheType = newValue;
    firePropertyChange(IMAGE_CACHE_TYPE, oldValue, newValue);
  }

  /**
   * Sets the image cache background.
   * 
   * @param newValue
   *          the new image cache background
   */
  public void setImageCacheBackground(boolean newValue) {
    boolean oldValue = this.imageCacheBackground;
    this.imageCacheBackground = newValue;
    firePropertyChange(IMAGE_CACHE_BACKGROUND, oldValue, newValue);
  }

  /**
   * get Localge.getLanguage() 2 char from settings
   * 
   * @return 2 char string - use "new Locale(getLanguage())"
   */
  @XmlElement(name = LANGUAGE)
  public String getLanguage() {
    if (language == null || language.isEmpty()) {
      return Locale.getDefault().getLanguage();
    }
    return language;
  }

  /**
   * set Localge.getLanguage() 2 char into settings
   * 
   * @param language
   */
  public void setLanguage(String language) {
    String oldValue = this.language;
    this.language = language;
    Locale.setDefault(new Locale(language));
    firePropertyChange(LANGUAGE, oldValue, language);
  }

  public void addWolDevice(WolDevice newDevice) {
    wolDevices.add(newDevice);
    firePropertyChange(WOL_DEVICES, null, wolDevices.size());
  }

  public void removeWolDevice(WolDevice device) {
    wolDevices.remove(device);
    firePropertyChange(WOL_DEVICES, null, wolDevices.size());
  }

  public List<WolDevice> getWolDevices() {
    return wolDevices;
  }
}
