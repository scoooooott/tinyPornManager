/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
import org.tinymediamanager.scraper.http.ProxySettings;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * The Class Settings - holding all settings for tmm.
 * 
 * @author Manuel Laggner
 */
@XmlRootElement(name = "tinyMediaManager")
public class Settings extends AbstractSettings {
  private static final Logger   LOGGER                 = LoggerFactory.getLogger(Settings.class);

  public final static String    DEFAULT_CONFIG_FOLDER  = "data";

  private String                settingsFolder         = DEFAULT_CONFIG_FOLDER;
  private static Settings       instance;

  /**
   * Constants mainly for events
   */
  private final static String   CONFIG_FILE            = "tmm.xml";
  private final static String   TITLE_PREFIX           = "titlePrefix";
  private final static String   PREFIX                 = "prefix";
  private final static String   VIDEO_FILE_TYPE        = "videoFileTypes";
  private final static String   AUDIO_FILE_TYPE        = "audioFileTypes";
  private final static String   SUBTITLE_FILE_TYPE     = "subtitleFileTypes";
  private final static String   FILETYPE               = "filetype";
  private final static String   PROXY_HOST             = "proxyHost";
  private final static String   PROXY_PORT             = "proxyPort";
  private final static String   PROXY_USERNAME         = "proxyUsername";
  private final static String   PROXY_PASSWORD         = "proxyPassword";
  private final static String   IMAGE_CACHE            = "imageCache";
  private final static String   IMAGE_CACHE_TYPE       = "imageCacheType";
  private final static String   LANGUAGE               = "language";
  private final static String   WOL_DEVICES            = "wolDevices";
  private final static String   ENABLE_ANALYTICS       = "enableAnalytics";

  private final static String   UPNP_SHARE_LIBRARY     = "upnpShareLibrary";
  private final static String   UPNP_PLAY_ON_REMOTE    = "upnpRemotePlay";

  @XmlElementWrapper(name = TITLE_PREFIX)
  @XmlElement(name = PREFIX)
  private final List<String>    titlePrefix            = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = VIDEO_FILE_TYPE)
  @XmlElement(name = FILETYPE)
  private final List<String>    videoFileTypes         = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = AUDIO_FILE_TYPE)
  @XmlElement(name = FILETYPE)
  private final List<String>    audioFileTypes         = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = SUBTITLE_FILE_TYPE)
  @XmlElement(name = FILETYPE)
  private final List<String>    subtitleFileTypes      = ObservableCollections.observableList(new ArrayList<String>());

  @XmlElementWrapper(name = WOL_DEVICES)
  private final List<WolDevice> wolDevices             = ObservableCollections.observableList(new ArrayList<WolDevice>());

  @XmlAttribute
  private String                version                = "";

  private String                proxyHost;
  private String                proxyPort;
  private String                proxyUsername;
  private String                proxyPassword;

  private String                traktAccessToken       = "";
  private String                traktRefreshToken      = "";

  private static int            DEFAULT_KODI_HTTP_PORT = 8080;
  static {
    if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")) {
      DEFAULT_KODI_HTTP_PORT = 80;
    }
  }
  private String    kodiHost               = "";
  private int       kodiHttpPort           = DEFAULT_KODI_HTTP_PORT;
  private int       kodiTcpPort            = 9090;
  private String    kodiUsername           = "";
  private String    kodiPassword           = "";

  private boolean   imageCache             = true;
  private CacheType imageCacheType         = CacheType.SMOOTH;

  // language 2 char - saved to config
  private String    language;
  private String    mediaPlayer            = "";

  private int       fontSize               = 12;
  private String    fontFamily             = "Dialog";

  private boolean   storeWindowPreferences = true;

  private boolean   deleteTrashOnExit      = false;
  private boolean   enableAnalytics        = false;

  private boolean   upnpShareLibrary       = false;
  private boolean   upnpRemotePlay         = false;

  /**
   * Instantiates a new settings.
   */
  public Settings() {
    addPropertyChangeListener(evt -> setDirty());
  }

  @Override
  protected String getConfigFilename() {
    return CONFIG_FILE;
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void writeDefaultSettings() {
    version = ReleaseInfo.getVersion();

    // default video file types derived from
    // http://wiki.xbmc.org/index.php?title=Advancedsettings.xml#.3Cvideoextensions.3E
    videoFileTypes.clear();
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

    audioFileTypes.clear();
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
    subtitleFileTypes.clear();
    addSubtitleFileTypes(".aqt");
    addSubtitleFileTypes(".cvd");
    addSubtitleFileTypes(".dks");
    addSubtitleFileTypes(".jss");
    addSubtitleFileTypes(".sub");
    addSubtitleFileTypes(".sup");
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
    // addSubtitleFileTypes(".idx"); // not a subtitle! just index for .sub
    addSubtitleFileTypes(".ass");
    addSubtitleFileTypes(".pgs");
    addSubtitleFileTypes(".vobsub");
    Collections.sort(subtitleFileTypes);

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
    addTitlePrefix("Le");
    addTitlePrefix("La");
    addTitlePrefix("Les");
    addTitlePrefix("L'");
    addTitlePrefix("L´");
    addTitlePrefix("L`");
    addTitlePrefix("Un");
    addTitlePrefix("Une");
    addTitlePrefix("Des");
    addTitlePrefix("Du");
    addTitlePrefix("D'");
    addTitlePrefix("D´");
    addTitlePrefix("D`");
    Collections.sort(titlePrefix);

    setProxyFromSystem();

    saveSettings();
  }

  /**
   * Gets the single instance of Settings.
   * 
   * @return single instance of Settings
   */
  public static synchronized Settings getInstance() {
    return getInstance(DEFAULT_CONFIG_FOLDER);
  }

  /**
   * Override our settings folder (defaults to "data")<br>
   * <b>Should only be used for unit testing et all!</b><br>
   *
   * @return single instance of Settings
   */
  public synchronized static Settings getInstance(String folder) {
    if (instance == null) {
      instance = (Settings) getInstance(folder, CONFIG_FILE, Settings.class);
    }
    return instance;
  }

  /**
   * is our settings file up to date?
   */
  public boolean isCurrentVersion() {
    return StrgUtils.compareVersion(version, ReleaseInfo.getVersion()) == 0;
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
    setDirty();
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
    if (!type.startsWith(".")) {
      type = "." + type;
    }
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
    List<String> list = new ArrayList<>();
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

      w = new FileWriter(new File(settingsFolder, CONFIG_FILE));
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
  @XmlJavaTypeAdapter(EncryptedStringXmlAdapter.class)
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
    newValue = StringEscapeUtils.escapeXml10(newValue);
    String oldValue = this.proxyPassword;
    this.proxyPassword = newValue;
    firePropertyChange(PROXY_PASSWORD, oldValue, newValue);
  }

  /**
   * Sets the proxy from system settings, if empty
   */
  public void setProxyFromSystem() {
    String val = "";

    String[] proxyEnvs = { "http.proxyHost", "https.proxyHost", "proxyHost", "socksProxyHost" };
    for (String pe : proxyEnvs) {
      if (StringUtils.isEmpty(getProxyHost())) {
        val = System.getProperty(pe, "");
        if (!val.isEmpty()) {
          setProxyHost(val);
        }
      }
    }

    String[] proxyPortEnvs = { "http.proxyPort", "https.proxyPort", "proxyPort", "socksProxyPort" };
    for (String ppe : proxyPortEnvs) {
      if (StringUtils.isEmpty(getProxyPort())) {
        val = System.getProperty(ppe, "");
        if (!val.isEmpty()) {
          setProxyPort(val);
        }
      }
    }
  }

  /**
   * Sets the TMM proxy.
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
    try {
      ProxySettings.setProxySettings(getProxyHost(), getProxyPort() == null ? 0 : Integer.parseInt(getProxyPort().trim()), getProxyUsername(),
          getProxyPassword());
    }
    catch (NumberFormatException e) {
      LOGGER.error("could not parse proxy port: " + e.getMessage());
    }
  }

  /**
   * Should we use a proxy.
   * 
   * @return true, if successful
   */
  public boolean useProxy() {
    if (StringUtils.isNotBlank(getProxyHost())) {
      return true;
    }
    return false;
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
   * is our library shared via UPNP?
   *
   * @return
   */
  public boolean isUpnpShareLibrary() {
    return upnpShareLibrary;
  }

  /**
   * share library via UPNP?
   *
   * @param upnpShareLibrary
   */
  public void setUpnpShareLibrary(boolean upnpShareLibrary) {
    boolean old = this.upnpShareLibrary;
    this.upnpShareLibrary = upnpShareLibrary;
    firePropertyChange(UPNP_SHARE_LIBRARY, old, upnpShareLibrary);
  }

  /**
   * should we search for rendering devices like Kodi, TVs, et all?
   *
   * @return
   */
  public boolean isUpnpRemotePlay() {
    return upnpRemotePlay;
  }

  /**
   * should we search for rendering devices like Kodi, TVs, et all?
   *
   * @param upnpRemotePlay
   */
  public void setUpnpRemotePlay(boolean upnpRemotePlay) {
    boolean old = this.upnpRemotePlay;
    this.upnpRemotePlay = upnpRemotePlay;
    firePropertyChange(UPNP_PLAY_ON_REMOTE, old, upnpRemotePlay);
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
   * set Locale.toString() 5 char into settings
   * 
   * @param language
   *          the language to be set
   */
  public void setLanguage(String language) {
    String oldValue = this.language;
    this.language = language;
    Locale.setDefault(Utils.getLocaleFromLanguage(language));
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

  @XmlJavaTypeAdapter(EncryptedStringXmlAdapter.class)
  public String getTraktAccessToken() {
    return traktAccessToken;
  }

  public void setTraktAccessToken(String newValue) {
    String oldValue = this.traktAccessToken;
    this.traktAccessToken = newValue.trim();
    firePropertyChange("traktAccessToken", oldValue, newValue);
  }

  @XmlJavaTypeAdapter(EncryptedStringXmlAdapter.class)
  public String getTraktRefreshToken() {
    return traktRefreshToken;
  }

  public void setTraktRefreshToken(String newValue) {
    String oldValue = this.traktRefreshToken;
    this.traktRefreshToken = newValue;
    firePropertyChange("traktRefreshToken", oldValue, newValue);
  }

  public String getKodiHost() {
    return kodiHost;
  }

  public void setKodiHost(String newValue) {
    String oldValue = this.kodiHost;
    this.kodiHost = newValue;
    firePropertyChange("kodiHost", oldValue, newValue);
  }

  /**
   * gets saved Kodi HTTP port, or default
   * 
   * @return
   */
  public int getKodiHttpPort() {
    return kodiHttpPort == 0 ? DEFAULT_KODI_HTTP_PORT : kodiHttpPort;
  }

  public void setKodiHttpPort(int kodiHttpPort) {
    int oldValue = this.kodiHttpPort;
    this.kodiHttpPort = kodiHttpPort;
    firePropertyChange("kodiHttpPort", oldValue, kodiHttpPort);
  }

  /**
   * gets saved Kodi TCP port, or default
   * 
   * @return
   */
  public int getKodiTcpPort() {
    return kodiTcpPort == 0 ? 9090 : kodiTcpPort;
  }

  public void setKodiTcpPort(int kodiTcpPort) {
    int oldValue = this.kodiHttpPort;
    this.kodiTcpPort = kodiTcpPort;
    firePropertyChange("kodiTcpPort", oldValue, kodiTcpPort);
  }

  public String getKodiUsername() {
    return kodiUsername;
  }

  public void setKodiUsername(String newValue) {
    String oldValue = this.kodiUsername;
    this.kodiUsername = newValue;
    firePropertyChange("kodiUsername", oldValue, newValue);
  }

  @XmlJavaTypeAdapter(EncryptedStringXmlAdapter.class)
  public String getKodiPassword() {
    return kodiPassword;
  }

  public void setKodiPassword(String newValue) {
    String oldValue = this.kodiPassword;
    this.kodiPassword = newValue;
    firePropertyChange("kodiPassword", oldValue, newValue);
  }

  public void setMediaPlayer(String newValue) {
    String oldValue = mediaPlayer;
    mediaPlayer = newValue;
    firePropertyChange("mediaPlayer", oldValue, newValue);
  }

  public String getMediaPlayer() {
    return mediaPlayer;
  }

  public void setFontSize(int newValue) {
    int oldValue = this.fontSize;
    this.fontSize = newValue;
    firePropertyChange("fontSize", oldValue, newValue);
  }

  public int getFontSize() {
    return this.fontSize;
  }

  public void setFontFamily(String newValue) {
    String oldValue = this.fontFamily;
    this.fontFamily = newValue;
    firePropertyChange("fontFamily", oldValue, newValue);
  }

  public String getFontFamily() {
    return this.fontFamily;
  }

  public void setDeleteTrashOnExit(boolean newValue) {
    boolean oldValue = deleteTrashOnExit;
    deleteTrashOnExit = newValue;
    firePropertyChange("deleteTrashOnExit", oldValue, newValue);
  }

  public boolean isDeleteTrashOnExit() {
    return deleteTrashOnExit;
  }

  public boolean isEnableAnalytics() {
    return enableAnalytics;
  }

  public void setEnableAnalytics(boolean newValue) {
    boolean oldValue = this.enableAnalytics;
    this.enableAnalytics = newValue;
    firePropertyChange(ENABLE_ANALYTICS, oldValue, newValue);
  }

  public void setStoreWindowPreferences(boolean newValue) {
    boolean oldValue = storeWindowPreferences;
    storeWindowPreferences = newValue;
    firePropertyChange("storeWindowPreferences", oldValue, newValue);
  }

  public boolean isStoreWindowPreferences() {
    return storeWindowPreferences;
  }
}
