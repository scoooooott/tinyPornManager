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
package org.tinymediamanager.core.tvshow.connector;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.io.FileUtils;
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
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.tvshow.entities.TvShowActor;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.util.ParserUtils;

/**
 * The Class tvShowEpisodeEpisodeToXbmcNfoConnector.
 * 
 * @author Manuel Laggner
 */
@XmlRootElement(name = "episodedetails")
@XmlType(propOrder = { "title", "showtitle", "rating", "votes", "season", "episode", "uniqueid", "displayseason", "displayepisode", "plot", "thumb",
    "mpaa", "tags", "playcount", "lastplayed", "watched", "credits", "director", "aired", "premiered", "studio", "actors", "fileinfo",
    "unsupportedElements" })
public class TvShowEpisodeToXbmcNfoConnector {
  private static final Logger LOGGER         = LoggerFactory.getLogger(TvShowEpisodeToXbmcNfoConnector.class);
  private static JAXBContext  context        = initContext();

  private String              season         = "";
  private String              episode        = "";
  private String              displayseason  = "";
  private String              displayepisode = "";
  private String              uniqueid       = "";
  private String              title          = "";
  private String              showtitle      = "";
  private float               rating         = 0;
  private int                 votes          = 0;
  private String              plot           = "";
  @XmlElement(name = "studio")
  private List<String>        studio         = null;
  private String              mpaa           = "";
  private String              aired          = "";
  private String              premiered      = "";

  @XmlElement
  private int                 playcount      = 0;
  @XmlElement
  private boolean             watched        = false;

  @XmlAnyElement(lax = true)
  private List<Object>        actors;

  @XmlElement
  private Fileinfo            fileinfo;

  @XmlElement(name = "credits")
  private List<String>        credits;

  @XmlElement(name = "director")
  private List<String>        director;

  @XmlElement(name = "tag")
  private List<String>        tags;

  @XmlAnyElement(lax = true)
  private List<Object>        unsupportedElements;

  /** not supported tags, but used to retrain in NFO. */
  @XmlElement
  String                      thumb;

  @XmlElement
  String                      lastplayed;

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
    actors = new ArrayList<>();
    director = new ArrayList<>();
    credits = new ArrayList<>();
    tags = new ArrayList<>();
    unsupportedElements = new ArrayList<>();
  }

  /**
   * Sets the data.
   * 
   * @param tvShowEpisodes
   *          the tv show episodes
   */
  public static void setData(List<TvShowEpisode> tvShowEpisodes) {
    boolean multiEpisode = tvShowEpisodes.size() > 1 ? true : false;

    if (context == null) {
      MessageManager.instance
          .pushMessage(new Message(MessageLevel.ERROR, tvShowEpisodes.get(0), "message.nfo.writeerror", new String[] { ":", "Context is null" }));
      return;
    }

    if (tvShowEpisodes.size() == 0) {
      return;
    }

    TvShowEpisode episode = tvShowEpisodes.get(0);
    MediaFile mf = episode.getBiggestMediaFile(MediaFileType.VIDEO);
    if (mf == null) {
      return; // no video file?
    }
    String nfoFilename = mf.getBasename() + ".nfo";
    if (episode.isDisc()) {
      if (mf.isBlurayFile()) {
        nfoFilename = "BDMV.nfo"; // dunno, but more correct
      }
      if (mf.isDVDFile()) {
        nfoFilename = "VIDEO_TS.nfo";
      }
    }

    File nfoFile = new File(episode.getPath(), nfoFilename);

    // parse out all episodes from the nfo
    List<TvShowEpisodeToXbmcNfoConnector> xbmcConnectors = parseNfo(nfoFile);

    // process all episodes
    StringBuilder outputXml = new StringBuilder();
    for (int i = 0; i < tvShowEpisodes.size(); i++) {
      episode = tvShowEpisodes.get(i);
      List<Object> unsupportedTags = new ArrayList<>();

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
      else {
        // store all unsupported tags
        for (Object obj : xbmc.actors) { // ugly hack for invalid xml structure
          if (!(obj instanceof Actor)) {
            unsupportedTags.add(obj);
          }
        }
      }

      xbmc.setTitle(episode.getTitle());
      xbmc.setShowtitle(episode.getTvShow().getTitle());
      xbmc.setRating(episode.getRating());
      xbmc.setVotes(episode.getVotes());
      xbmc.setSeason(String.valueOf(episode.getSeason()));
      xbmc.setEpisode(String.valueOf(episode.getEpisode()));
      xbmc.setDisplayseason(String.valueOf(episode.getDisplaySeason()));
      xbmc.setDisplayepisode(String.valueOf(episode.getDisplayEpisode()));
      xbmc.setPlot(episode.getPlot());
      xbmc.setAired(episode.getFirstAiredFormatted());
      xbmc.setPremiered(episode.getFirstAiredFormatted());
      if (StringUtils.isNotEmpty(episode.getTvShow().getProductionCompany())) {
        xbmc.studio = Arrays.asList(episode.getTvShow().getProductionCompany().split("\\s*[,\\/]\\s*")); // split on , or / and remove whitespace
                                                                                                         // around
      }

      if (episode.getTvdbId() != null) {
        xbmc.setUniqueid(episode.getTvdbId().toString());
      }
      xbmc.setMpaa(episode.getTvShow().getCertification().getName());
      xbmc.watched = episode.isWatched();
      if (xbmc.watched) {
        xbmc.playcount = 1;
      }

      xbmc.actors.clear();
      // actors for tv show episode (guests)
      for (TvShowActor actor : episode.getGuests()) {
        xbmc.addActor(actor.getName(), actor.getCharacter(), actor.getThumbUrl());
      }

      // write thumb url to multi ep NFOs
      // since the video file contains two or more EPs, we can only store 1 thumb file; in this
      // case we write the thumb url to the NFOs that Kodi can display the proper one
      if (multiEpisode && StringUtils.isNotBlank(episode.getArtworkUrl(MediaFileType.THUMB))) {
        xbmc.thumb = episode.getArtworkUrl(MediaFileType.THUMB);
      }
      else {
        xbmc.thumb = "";
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

      xbmc.tags.clear();
      for (String tag : episode.getTags()) {
        xbmc.tags.add(tag);
      }

      // fileinfo
      Fileinfo info = new Fileinfo();
      for (MediaFile mediaFile : episode.getMediaFiles(MediaFileType.VIDEO)) {
        if (StringUtils.isEmpty(mediaFile.getVideoCodec())) {
          break;
        }

        info.streamdetails.video.codec = mediaFile.getVideoCodec();
        info.streamdetails.video.aspect = String.valueOf(mediaFile.getAspectRatio());
        info.streamdetails.video.width = mediaFile.getVideoWidth();
        info.streamdetails.video.height = mediaFile.getVideoHeight();
        info.streamdetails.video.durationinseconds = mediaFile.getDuration();
        // "Spec": https://github.com/xbmc/xbmc/blob/master/xbmc/guilib/StereoscopicsManager.cpp
        if (mediaFile.getVideo3DFormat().equals(MediaFile.VIDEO_3D_SBS) || mediaFile.getVideo3DFormat().equals(MediaFile.VIDEO_3D_HSBS)) {
          info.streamdetails.video.stereomode = "left_right";
        }
        else if (mediaFile.getVideo3DFormat().equals(MediaFile.VIDEO_3D_TAB) || mediaFile.getVideo3DFormat().equals(MediaFile.VIDEO_3D_HTAB)) {
          info.streamdetails.video.stereomode = "top_bottom"; // maybe?
        }

        for (MediaFileAudioStream as : mediaFile.getAudioStreams()) {
          Audio audio = new Audio();

          if (StringUtils.isNotBlank(as.getCodec())) {
            audio.codec = as.getCodec().replaceAll("-", "_");
          }
          else {
            audio.codec = as.getCodec();
          }
          audio.language = as.getLanguage();
          audio.channels = String.valueOf(as.getChannelsAsInt());
          info.streamdetails.audio.add(audio);
        }
        for (MediaFileSubtitle ss : mediaFile.getSubtitles()) {
          Subtitle sub = new Subtitle();
          sub.language = ss.getLanguage();
          info.streamdetails.subtitle.add(sub);
        }
        break;
      }
      // add external subtitles to NFO
      for (MediaFile mediaFile : episode.getMediaFiles(MediaFileType.SUBTITLE)) {
        for (MediaFileSubtitle ss : mediaFile.getSubtitles()) {
          Subtitle sub = new Subtitle();
          sub.language = ss.getLanguage();
          info.streamdetails.subtitle.add(sub);
        }
      }
      xbmc.fileinfo = info;

      // add all unsupported tags again
      xbmc.unsupportedElements.addAll(unsupportedTags);

      // and marshall it
      try {
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dat = formatter.format(new Date());
        String comment = "<!-- created on " + dat + " - tinyMediaManager " + Globals.settings.getVersion() + " -->\n";
        m.setProperty("com.sun.xml.internal.bind.xmlHeaders", comment);

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
      catch (Exception e) {
        LOGGER.error("setData " + nfoFile.getAbsolutePath(), e.getMessage());
        MessageManager.instance.pushMessage(
            new Message(MessageLevel.ERROR, tvShowEpisodes.get(0), "message.nfo.writeerror", new String[] { ":", e.getLocalizedMessage() }));
      }
    }

    try {
      FileUtils.write(nfoFile, outputXml, "UTF-8");
      for (TvShowEpisode e : tvShowEpisodes) {
        e.removeAllMediaFiles(MediaFileType.NFO);
        e.addToMediaFiles(new MediaFile(nfoFile));
      }
    }
    catch (Exception e) {
      LOGGER.error("setData " + nfoFile.getAbsolutePath(), e.getMessage());
      MessageManager.instance.pushMessage(
          new Message(MessageLevel.ERROR, tvShowEpisodes.get(0), "message.nfo.writeerror", new String[] { ":", e.getLocalizedMessage() }));
    }
  }

  /**
   * Gets the data.
   * 
   * @param nfo
   *          the nfo file
   * @return the data
   */
  public static List<TvShowEpisode> getData(File nfo) {
    // try to parse XML
    List<TvShowEpisode> episodes = new ArrayList<>(1);

    if (context == null) {
      return episodes;
    }

    // parse out all episodes from the nfo
    List<TvShowEpisodeToXbmcNfoConnector> xbmcConnectors = parseNfo(nfo);

    for (TvShowEpisodeToXbmcNfoConnector xbmc : xbmcConnectors) {
      // only continue, if there is a title in the nfo
      if (StringUtils.isEmpty(xbmc.getTitle())) {
        continue;
      }

      TvShowEpisode episode = new TvShowEpisode();
      episode.setTitle(xbmc.getTitle());
      episode.setPlot(xbmc.getPlot());
      episode.setRating(xbmc.getRating());
      episode.setVotes(xbmc.getVotes());

      try {
        episode.setEpisode(Integer.parseInt(xbmc.getEpisode()));
        episode.setSeason(Integer.parseInt(xbmc.getSeason()));
      }
      catch (NumberFormatException e) {
      }

      try {
        episode.setDisplayEpisode(Integer.parseInt(xbmc.getDisplayepisode()));
        episode.setDisplaySeason(Integer.parseInt(xbmc.getDisplayseason()));
      }
      catch (NumberFormatException e) {
      }

      episode.setVotes(xbmc.getVotes());
      episode.setWatched(xbmc.watched);
      if (xbmc.playcount > 0) {
        episode.setWatched(true);
      }

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
      episode.setFirstAired(xbmc.getAired());

      // now there is the complicated part: tv show actors should be on the tv show level
      // episode "guests" should be on the episode level
      // BUT: at this moment there is no information about the tv show, so we parse them all into the episode
      for (Actor actor : xbmc.getActors()) {
        TvShowActor cast = new TvShowActor(actor.getName(), actor.getRole());
        cast.setThumbUrl(actor.getThumb());
        episode.addActor(cast);
      }

      for (String tag : xbmc.tags) {
        episode.addToTags(tag);
      }

      episode.addToMediaFiles(new MediaFile(nfo, MediaFileType.NFO));
      episodes.add(episode);
    }

    return episodes;
  }

  @XmlElement(name = "title")
  public String getTitle() {
    return title;
  }

  @XmlElement(name = "uniqueid")
  public String getUniqueid() {
    return uniqueid;
  }

  public void setUniqueid(String uniqueid) {
    this.uniqueid = uniqueid;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @XmlElement(name = "season")
  public String getSeason() {
    return season;
  }

  @XmlElement(name = "episode")
  public String getEpisode() {
    return episode;
  }

  @XmlElement(name = "showtitle")
  public String getShowtitle() {
    return showtitle;
  }

  public void setSeason(String season) {
    this.season = season;
  }

  public void setEpisode(String episode) {
    this.episode = episode;
  }

  @XmlElement(name = "displayseason")
  public String getDisplayseason() {
    return displayseason;
  }

  @XmlElement(name = "displayepisode")
  public String getDisplayepisode() {
    return displayepisode;
  }

  public void setDisplayseason(String displayseason) {
    this.displayseason = displayseason;
  }

  public void setDisplayepisode(String displayepisode) {
    this.displayepisode = displayepisode;
  }

  @XmlElement(name = "mpaa")
  public String getMpaa() {
    return this.mpaa;
  }

  public void setMpaa(String mpaa) {
    this.mpaa = mpaa;
  }

  public void setShowtitle(String showtitle) {
    this.showtitle = showtitle;
  }

  @XmlElement(name = "rating")
  public float getRating() {
    return rating;
  }

  @XmlElement(name = "votes")
  public int getVotes() {
    return votes;
  }

  public void setVotes(int votes) {
    this.votes = votes;
  }

  @XmlElement(name = "aired")
  public String getAired() {
    return aired;
  }

  public void setAired(String aired) {
    this.aired = aired;
  }

  @XmlElement(name = "premiered")
  public String getPremiered() {
    return premiered;
  }

  public void setPremiered(String premiered) {
    this.premiered = premiered;
  }

  @XmlElement(name = "plot")
  public String getPlot() {
    return plot;
  }

  public void setRating(float rating) {
    this.rating = rating;
  }

  public void setPlot(String plot) {
    this.plot = plot;
  }

  public void addActor(String name, String role, String thumb) {
    Actor actor = new Actor(name, role, thumb);
    actors.add(actor);
  }

  public List<Actor> getActors() {
    // @XmlAnyElement(lax = true) causes all unsupported tags to be in actors;
    // filter Actors out
    List<Actor> pureActors = new ArrayList<>();
    for (Object obj : actors) {
      if (obj instanceof Actor) {
        Actor actor = (Actor) obj;
        pureActors.add(actor);
      }
    }
    return pureActors;
  }

  public List<String> getDirector() {
    return director;
  }

  public void addDirector(String director) {
    this.director.add(director);
  }

  public List<String> getCredits() {
    return credits;
  }

  public void addCredits(String credits) {
    this.credits.add(credits);
  }

  // inner class actor to represent actors
  @XmlRootElement(name = "actor")
  public static class Actor {
    private String name;
    private String role;
    private String thumb;

    public Actor() {
    }

    public Actor(String name, String role, String thumb) {
      this.name = name;
      this.role = role;
      this.thumb = thumb;
    }

    @XmlElement(name = "name")
    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    @XmlElement(name = "role")
    public String getRole() {
      return role;
    }

    public void setRole(String role) {
      this.role = role;
    }

    @XmlElement(name = "thumb")
    public String getThumb() {
      return thumb;
    }

    public void setThumb(String thumb) {
      this.thumb = thumb;
    }
  }

  private static List<TvShowEpisodeToXbmcNfoConnector> parseNfo(File nfoFile) {
    List<TvShowEpisodeToXbmcNfoConnector> xbmcConnectors = new ArrayList<>(1);

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
            TvShowEpisodeToXbmcNfoConnector xbmc = parseNfoPart(sb.toString());
            xbmcConnectors.add(xbmc);
          }
          catch (Exception e) {
            LOGGER.error("failed to parse " + nfoFile.getAbsolutePath(), e);
          }
        }
      }
      catch (IOException e) {
        // MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, nfoFile.getPath(), "message.nfo.readerror"));
      }
    }

    return xbmcConnectors;
  }

  private static TvShowEpisodeToXbmcNfoConnector parseNfoPart(String part) throws Exception {
    Unmarshaller um = context.createUnmarshaller();
    if (um == null) {
      throw new Exception("could not create unmarshaller");
    }
    try {
      Reader in = new StringReader(part);
      return (TvShowEpisodeToXbmcNfoConnector) um.unmarshal(in);
    }
    catch (UnmarshalException e) {
      LOGGER.error("tried to unmarshal; now trying to clean xml stream");
    }
    catch (IllegalArgumentException e) {
      LOGGER.warn("tried to unmarshal; now trying to clean xml stream");
    }

    // clean NFO string and retry
    StringReader in = new StringReader(ParserUtils.cleanNfo(part));
    return (TvShowEpisodeToXbmcNfoConnector) um.unmarshal(in);
  }

  /*
   * inner class holding file informations
   */
  static class Fileinfo {
    @XmlElement
    Streamdetails streamdetails;

    public Fileinfo() {
      streamdetails = new Streamdetails();
    }
  }

  /*
   * inner class holding details of audio and video stream
   */
  static class Streamdetails {
    @XmlElement
    private Video          video;

    @XmlElement
    private List<Audio>    audio;

    @XmlElement
    private List<Subtitle> subtitle;

    public Streamdetails() {
      video = new Video();
      audio = new ArrayList<>();
      subtitle = new ArrayList<>();
    }
  }

  /*
   * inner class holding details of the video stream
   */
  static class Video {
    @XmlElement
    private String codec;

    @XmlElement
    private String aspect;

    @XmlElement
    private int    width;

    @XmlElement
    private int    height;

    @XmlElement
    private int    durationinseconds;

    @XmlElement
    private String stereomode;
  }

  /*
   * inner class holding details of the audio stream
   */
  static class Audio {
    @XmlElement
    private String codec;

    @XmlElement
    private String language;

    @XmlElement
    private String channels;
  }

  /*
   * inner class holding details of the subtitle stream
   */
  static class Subtitle {
    @XmlElement
    private String language;
  }
}
