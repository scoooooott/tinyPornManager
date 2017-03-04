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

package org.tinymediamanager.core.movie.connector;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.CertificationStyle;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.movie.MovieEdition;
import org.tinymediamanager.core.movie.MovieHelpers;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.connector.MovieToKodiNfoConnector.Actor;
import org.tinymediamanager.core.movie.connector.MovieToKodiNfoConnector.Producer;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieActor;
import org.tinymediamanager.core.movie.entities.MovieProducer;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.movie.entities.MovieTrailer;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.util.ParserUtils;

/**
 * The Class MovieToKodiNfoConnector. This class is the interface between tinyMediaManager and the Kodi style NFO files
 *
 * @author Manuel Laggner
 */
@XmlRootElement(name = "movie")
@XmlSeeAlso({ Actor.class, Producer.class })
@XmlType(propOrder = { "title", "originaltitle", "set", "sorttitle", "rating", "epbookmark", "year", "top250", "votes", "outline", "plot", "tagline",
    "runtime", "thumb", "fanart", "mpaa", "certification", "id", "ids", "tmdbId", "trailer", "country", "premiered", "status", "code", "aired",
    "fileinfo", "watched", "playcount", "genres", "studio", "credits", "director", "tags", "actors", "producers", "resume", "lastplayed", "dateadded",
    "keywords", "poster", "url", "languages", "source", "edition", "unsupportedElements" })
public class MovieToKodiNfoConnector {
  private static final Logger  LOGGER                = LoggerFactory.getLogger(MovieToKodiNfoConnector.class);
  private static final Pattern PATTERN_NFO_MOVIE_TAG = Pattern.compile("<movie.*?>");
  private static JAXBContext   context               = initContext();

  public String                title                 = "";
  public String                originaltitle         = "";

  @XmlJavaTypeAdapter(MovieSetAdapter.class)
  public Set                   set;
  public String                sorttitle             = "";
  public float                 rating                = 0;
  public String                year                  = "";
  public String                top250                = "";
  public int                   votes                 = 0;
  public String                outline               = "";
  public String                plot                  = "";
  public String                tagline               = "";
  public String                runtime               = "";
  public String                thumb                 = "";
  public String                fanart                = "";
  public String                mpaa                  = "";
  public String                certification         = "";
  public String                id                    = "";
  @XmlElementWrapper(name = "ids")
  private Map<String, Object>  ids;
  public int                   tmdbId                = 0;
  public String                trailer               = "";
  public String                country               = "";
  public String                premiered             = "";
  public Fileinfo              fileinfo;
  public boolean               watched               = false;
  public int                   playcount             = 0;
  @XmlElement(name = "genre")
  public List<String>          genres;
  public List<String>          studio;
  public List<String>          credits;
  public List<String>          director;
  @XmlElement(name = "tag")
  private List<String>         tags;
  @XmlAnyElement(lax = true)
  private List<Object>         actors;
  @XmlAnyElement(lax = true)
  private List<Object>         producers;
  public String                languages;
  public String                source;
  public String                edition;

  @XmlAnyElement(lax = true)
  private List<Object>         unsupportedElements;

  /**
   * not supported tags, but used to retrain in NFO.
   */
  public String                epbookmark;
  public String                lastplayed;
  public String                status;
  public String                code;
  public String                aired;
  public Object                resume;
  public String                dateadded;
  public Object                keywords;
  public Object                poster;
  public Object                url;

  // @XmlElement(name = "rotten-tomatoes")
  // private Object rottentomatoes;

  /*
   * inits the context for faster marshalling/unmarshalling
   */
  private static JAXBContext initContext() {
    try {
      return JAXBContext.newInstance(MovieToKodiNfoConnector.class, Actor.class, Set.class);
    }
    catch (JAXBException e) {
      LOGGER.error("Error instantiating JaxB", e);
    }
    return null;
  }

  public MovieToKodiNfoConnector() {
    actors = new ArrayList<>();
    genres = new ArrayList<>();
    tags = new ArrayList<>();
    director = new ArrayList<>();
    credits = new ArrayList<>();
    producers = new ArrayList<>();
    ids = new HashMap<>();
    unsupportedElements = new ArrayList<>();
    fileinfo = new Fileinfo();
    set = new Set();
  }

  /**
   * create the NFO from the given movie using the NFO file name settings
   *
   * @param movie
   *          the movie
   */
  public static void setData(Movie movie) {
    if (context == null) {
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.nfo.writeerror", new String[] { ":", "Context is null" }));
      return;
    }

    // create the instance from movie data
    MovieToKodiNfoConnector kodi = createInstanceFromMovie(movie);

    // and marshall it
    List<MovieNfoNaming> nfonames = new ArrayList<>();
    if (movie.isMultiMovieDir()) {
      // Fixate the name regardless of setting
      nfonames.add(MovieNfoNaming.FILENAME_NFO);
    }
    else {
      nfonames = MovieModuleManager.MOVIE_SETTINGS.getMovieNfoFilenames();
    }
    writeNfoFiles(movie, kodi, nfonames);
  }

  /**
   * create an instance from the given movie
   *
   * @param movie
   *          the movie to create the instance for
   * @return the newly created instance
   */
  static MovieToKodiNfoConnector createInstanceFromMovie(Movie movie) {
    MovieToKodiNfoConnector kodi = null;
    List<Object> unsupportedTags = new ArrayList<>();

    // load existing NFO if possible
    for (MediaFile mf : movie.getMediaFiles(MediaFileType.NFO)) {
      Path file = mf.getFileAsPath();
      if (Files.exists(file)) {
        try {
          kodi = parseNFO(file);
        }
        catch (Exception e) {
          LOGGER.error("failed to parse " + mf.getFilename(), e);
        }
      }
      if (kodi != null) {
        break;
      }
    }

    // create new
    if (kodi == null) {
      kodi = new MovieToKodiNfoConnector();
    }
    else {
      // store all unsupported tags
      for (Object obj : kodi.actors) { // ugly hack for invalid xml structure
        if (!(obj instanceof Producer) && !(obj instanceof Actor)) {
          unsupportedTags.add(obj);
        }
      }
    }

    // set data
    kodi.title = movie.getTitle();
    kodi.originaltitle = movie.getOriginalTitle();
    kodi.rating = movie.getRating();
    kodi.votes = movie.getVotes();
    if (movie.getTop250() == 0) {
      kodi.top250 = "";
    }
    else {
      kodi.top250 = String.valueOf(movie.getTop250());
    }
    kodi.year = movie.getYear();
    kodi.premiered = movie.getReleaseDateFormatted();
    kodi.plot = movie.getPlot();

    // outline is only the first 200 characters of the plot
    if (StringUtils.isNotBlank(kodi.plot) && kodi.plot.length() > 200) {
      int spaceIndex = kodi.plot.indexOf(" ", 200);
      if (spaceIndex > 0) {
        kodi.outline = kodi.plot.substring(0, spaceIndex) + "...";
      }
      else {
        kodi.outline = kodi.plot;
      }
    }
    else if (StringUtils.isNotBlank(kodi.plot)) {
      kodi.outline = kodi.plot;
    }

    kodi.tagline = movie.getTagline();
    kodi.runtime = String.valueOf(movie.getRuntime());

    String artworkUrl = movie.getArtworkUrl(MediaFileType.POSTER);
    if (artworkUrl.matches("https?://.*")) {
      kodi.thumb = artworkUrl;
    }
    else {
      // clean old invalid entries
      kodi.thumb = "";
    }

    artworkUrl = movie.getArtworkUrl(MediaFileType.FANART);
    if (artworkUrl.matches("https?://.*")) {
      kodi.fanart = artworkUrl;
    }
    else {
      // clean old invalid entries
      kodi.fanart = "";
    }

    kodi.id = movie.getImdbId();
    if (movie.getTmdbId() != 0) {
      kodi.tmdbId = movie.getTmdbId();
    }

    kodi.ids.putAll(movie.getIds());

    if (StringUtils.isNotEmpty(movie.getProductionCompany())) {
      kodi.studio = Arrays.asList(movie.getProductionCompany().split("\\s*[,\\/]\\s*")); // split on , or / and remove whitespace around
    }

    kodi.country = movie.getCountry();
    kodi.watched = movie.isWatched();
    if (kodi.watched) {
      kodi.playcount = 1;
    }
    else {
      kodi.playcount = 0;
    }

    kodi.languages = movie.getSpokenLanguages();

    // certifications
    if (movie.getCertification() != null) {
      kodi.certification = CertificationStyle.formatCertification(movie.getCertification(),
          MovieModuleManager.MOVIE_SETTINGS.getMovieCertificationStyle());
      if (MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry() == CountryCode.US) {
        // if we have US certs, write correct "Rated XX" String
        kodi.mpaa = Certification.getMPAAString(movie.getCertification());
      }
      else {
        kodi.mpaa = CertificationStyle.formatCertification(movie.getCertification(), MovieModuleManager.MOVIE_SETTINGS.getMovieCertificationStyle());
      }
    }

    // support of frodo director tags
    kodi.director.clear();
    if (StringUtils.isNotEmpty(movie.getDirector())) {
      String directors[] = movie.getDirector().split(", ");
      for (String director : directors) {
        kodi.director.add(director);
      }
    }

    // support of frodo credits tags
    kodi.credits.clear();
    if (StringUtils.isNotEmpty(movie.getWriter())) {
      String writers[] = movie.getWriter().split(", ");
      for (String writer : writers) {
        kodi.credits.add(writer);
      }
    }

    kodi.actors.clear();
    for (MovieActor cast : new ArrayList<>(movie.getActors())) {
      kodi.addActor(cast.getName(), cast.getCharacter(), cast.getThumbUrl());
    }

    kodi.producers.clear();
    for (MovieProducer producer : new ArrayList<>(movie.getProducers())) {
      kodi.addProducer(producer.getName(), producer.getRole(), producer.getThumbUrl());
    }

    kodi.genres.clear();
    for (MediaGenres genre : new ArrayList<>(movie.getGenres())) {
      kodi.genres.add(genre.toString());
    }

    kodi.trailer = "";
    for (MovieTrailer trailer : new ArrayList<>(movie.getTrailer())) {
      if (trailer.getInNfo() && !trailer.getUrl().startsWith("file")) {
        // parse internet trailer url for nfo (do not add local one)
        kodi.trailer = prepareTrailerForKodi(trailer);
        break;
      }
    }
    // keep trailer already in NFO, remove tag only when empty
    if (kodi.trailer.isEmpty()) {
      kodi.trailer = null;
    }

    kodi.tags.clear();
    for (String tag : new ArrayList<>(movie.getTags())) {
      kodi.tags.add(tag);
    }

    // movie set
    if (movie.getMovieSet() != null) {
      MovieSet movieSet = movie.getMovieSet();
      kodi.set.name = movieSet.getTitle();
      kodi.set.overview = movieSet.getPlot();
    }
    else {
      kodi.set.name = "";
      kodi.set.overview = "";
    }

    kodi.sorttitle = movie.getSortTitle();
    if (movie.getMediaSource() != MediaSource.UNKNOWN) {
      kodi.source = movie.getMediaSource().name();
    }
    if (movie.getEdition() != MovieEdition.NONE) {
      kodi.edition = movie.getEdition().getTitle();
    }

    // fileinfo
    for (MediaFile mediaFile : movie.getMediaFiles(MediaFileType.VIDEO)) {
      if (StringUtils.isEmpty(mediaFile.getVideoCodec())) {
        break;
      }

      kodi.fileinfo.streamdetails.video.codec = mediaFile.getVideoCodec();
      kodi.fileinfo.streamdetails.video.aspect = String.valueOf(mediaFile.getAspectRatio());
      kodi.fileinfo.streamdetails.video.width = mediaFile.getVideoWidth();
      kodi.fileinfo.streamdetails.video.height = mediaFile.getVideoHeight();
      kodi.fileinfo.streamdetails.video.durationinseconds = movie.getRuntimeFromMediaFiles();
      // "Spec": https://github.com/xbmc/xbmc/blob/master/xbmc/guilib/StereoscopicsManager.cpp
      if (mediaFile.getVideo3DFormat().equals(MediaFile.VIDEO_3D_SBS) || mediaFile.getVideo3DFormat().equals(MediaFile.VIDEO_3D_HSBS)) {
        kodi.fileinfo.streamdetails.video.stereomode = "left_right";
      }
      else if (mediaFile.getVideo3DFormat().equals(MediaFile.VIDEO_3D_TAB) || mediaFile.getVideo3DFormat().equals(MediaFile.VIDEO_3D_HTAB)) {
        kodi.fileinfo.streamdetails.video.stereomode = "top_bottom"; // maybe?
      }

      kodi.fileinfo.streamdetails.audio.clear();
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
        kodi.fileinfo.streamdetails.audio.add(audio);
      }

      kodi.fileinfo.streamdetails.subtitle.clear();
      for (MediaFileSubtitle ss : mediaFile.getSubtitles()) {
        Subtitle sub = new Subtitle();
        sub.language = ss.getLanguage();
        kodi.fileinfo.streamdetails.subtitle.add(sub);
      }
      break;
    }
    // add external subtitles to NFO
    for (MediaFile mediaFile : movie.getMediaFiles(MediaFileType.SUBTITLE)) {
      for (MediaFileSubtitle ss : mediaFile.getSubtitles()) {
        Subtitle sub = new Subtitle();
        sub.language = ss.getLanguage();
        kodi.fileinfo.streamdetails.subtitle.add(sub);
      }
    }

    // add all unsupported tags again
    kodi.unsupportedElements.addAll(unsupportedTags);

    return kodi;
  }

  static void writeNfoFiles(Movie movie, MovieToKodiNfoConnector kodi, List<MovieNfoNaming> nfoNames) {
    String nfoFilename = "";
    List<MediaFile> newNfos = new ArrayList<>(1);

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
        m.marshal(kodi, w);
        StringBuilder sb = new StringBuilder(w.toString());
        w.close();

        // on windows make windows conform linebreaks
        if (SystemUtils.IS_OS_WINDOWS) {
          sb = new StringBuilder(sb.toString().replaceAll("(?<!\r)\n", "\r\n"));
        }
        Path f = movie.getPathNIO().resolve(nfoFilename);
        Utils.writeStringToFile(f, sb.toString());
        MediaFile mf = new MediaFile(f);
        mf.gatherMediaInformation(true); // force to update filedate
        newNfos.add(mf);
      }
      catch (Exception e) {
        LOGGER.error("setData " + movie.getPathNIO().resolve(nfoFilename), e);
        MessageManager.instance
            .pushMessage(new Message(MessageLevel.ERROR, movie, "message.nfo.writeerror", new String[] { ":", e.getLocalizedMessage() }));
      }
    }

    if (newNfos.size() > 0) {
      movie.removeAllMediaFiles(MediaFileType.NFO);
      movie.addToMediaFiles(newNfos);
    }

  }

  /**
   * Extract the data out of the NFO file and create a new Movie instance
   *
   * @param nfoFile
   *          the nfo filename
   * @return the newly created Movie instance
   */
  public static Movie getData(Path nfoFile) {
    if (context == null) {
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, nfoFile, "message.nfo.readerror"));
      return null;
    }

    // try to parse XML
    Movie movie = null;
    try {
      MovieToKodiNfoConnector kodi = parseNFO(nfoFile);
      movie = new Movie();
      movie.setTitle(kodi.title);
      movie.setOriginalTitle(kodi.originaltitle);
      movie.setRating(kodi.rating);
      movie.setVotes(kodi.votes);
      movie.setYear(kodi.year);
      if (StringUtils.isNotBlank(kodi.top250)) {
        try {
          movie.setTop250(Integer.parseInt(kodi.top250));
        }
        catch (NumberFormatException e) {
          movie.setTop250(0);
        }
      }
      else {
        movie.setTop250(0);
      }
      movie.setReleaseDate(kodi.premiered);
      movie.setPlot(kodi.plot);
      movie.setTagline(kodi.tagline);
      try {
        String rt = kodi.runtime.replaceAll("[^0-9]", "");
        movie.setRuntime(Integer.parseInt(rt));
      }
      catch (Exception e) {
        LOGGER.warn("could not parse runtime: " + kodi.runtime + "; Movie: " + movie.getPathNIO());
      }

      if (StringUtils.isNotBlank(kodi.thumb)) {
        if (kodi.thumb.matches("https?://.*")) {
          movie.setArtworkUrl(kodi.thumb, MediaFileType.POSTER);
        }
      }

      if (StringUtils.isNotBlank(kodi.fanart)) {
        if (kodi.fanart.matches("https?://.*")) {
          movie.setArtworkUrl(kodi.fanart, MediaFileType.FANART);
        }
      }

      for (Entry<String, Object> entry : kodi.ids.entrySet()) {
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
        movie.setImdbId(kodi.id);
      }
      if (movie.getTmdbId() == 0 && kodi.tmdbId != 0) {
        movie.setTmdbId(kodi.tmdbId);
      }

      // convert director to internal format
      String director = "";
      for (String dir : kodi.director) {
        if (!StringUtils.isEmpty(director)) {
          director += ", ";
        }
        director += dir;
      }
      movie.setDirector(director);

      // convert writer to internal format
      String writer = "";
      for (String wri : kodi.credits) {
        if (StringUtils.isNotEmpty(writer)) {
          writer += ", ";
        }
        writer += wri;
      }
      movie.setWriter(writer);

      String studio = StringUtils.join(kodi.studio, " / ");
      if (studio == null) {
        movie.setProductionCompany("");
      }
      else {
        movie.setProductionCompany(studio);
      }
      movie.setProductionCompany(movie.getProductionCompany().replaceAll("\\s*,\\s*", " / "));

      movie.setCountry(kodi.country);
      if (!StringUtils.isEmpty(kodi.certification)) {
        movie.setCertification(MovieHelpers.parseCertificationStringForMovieSetupCountry(kodi.certification));
      }
      if (!StringUtils.isEmpty(kodi.mpaa) && movie.getCertification() == Certification.NOT_RATED) {
        movie.setCertification(MovieHelpers.parseCertificationStringForMovieSetupCountry(kodi.mpaa));
      }
      movie.setWatched(kodi.watched);
      if (kodi.playcount > 0) {
        movie.setWatched(true);
      }
      movie.setSpokenLanguages(kodi.languages);

      if (StringUtils.isNotBlank(kodi.source)) {
        try {
          MediaSource source = MediaSource.valueOf(kodi.source);
          if (source != null) {
            movie.setMediaSource(source);
          }
        }
        catch (Exception ignored) {
        }
      }

      if (StringUtils.isNotBlank(kodi.edition)) {
        MovieEdition edition = MovieEdition.getMovieEditionFromString(kodi.edition);
        movie.setEdition(edition);
      }

      // movieset
      if (StringUtils.isNotEmpty(kodi.set.name)) {
        // search for that movieset
        MovieList movieList = MovieList.getInstance();
        MovieSet movieSet = movieList.getMovieSet(kodi.set.name, 0);

        // add movie to movieset
        if (movieSet != null) {
          if (StringUtils.isBlank(movieSet.getPlot())) {
            movieSet.setPlot(kodi.set.overview);
          }
          movie.setMovieSet(movieSet);
        }
      }

      movie.setSortTitle(kodi.sorttitle);

      for (Actor actor : kodi.getActors()) {
        MovieActor cast = new MovieActor(actor.name, actor.role);
        cast.setThumbUrl(actor.thumb);
        movie.addActor(cast);
      }

      for (Producer producer : kodi.getProducers()) {
        MovieProducer cast = new MovieProducer(producer.name, producer.role);
        cast.setThumbUrl(producer.thumb);
        movie.addProducer(cast);
      }

      for (String genre : kodi.genres) {
        String[] genres = genre.split("/");
        for (String g : genres) {
          MediaGenres genreFound = MediaGenres.getGenre(g.trim());
          if (genreFound != null) {
            movie.addGenre(genreFound);
          }
        }
      }

      if (StringUtils.isNotEmpty(kodi.trailer)) {
        String urlFromNfo = parseTrailerUrl(kodi.trailer);
        if (!urlFromNfo.startsWith("file")) {
          // only add new MT when not a local file
          MovieTrailer trailer = new MovieTrailer();
          trailer.setName("fromNFO");
          trailer.setProvider("from NFO");
          trailer.setQuality("unknown");
          trailer.setUrl(urlFromNfo);
          trailer.setInNfo(true);
          movie.addTrailer(trailer);
        }
      }

      for (String tag : kodi.tags) {
        movie.addToTags(tag);
      }

    }
    catch (UnmarshalException e) {
      LOGGER.error("getData " + nfoFile, e.getMessage());
      return null;
    }
    catch (Exception e) {
      LOGGER.error("getData " + nfoFile, e);
      return null;
    }

    // only return if a movie name has been found
    if (StringUtils.isEmpty(movie.getTitle())) {
      return null;
    }

    return movie;
  }

  protected static MovieToKodiNfoConnector parseNFO(Path nfoFile) throws Exception {
    Unmarshaller um = context.createUnmarshaller();
    if (um == null) {
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, nfoFile, "message.nfo.readerror"));
      throw new Exception("could not create unmarshaller");
    }

    MovieToKodiNfoConnector kodi = null;
    Reader in = null;
    try {
      in = new InputStreamReader(new FileInputStream(nfoFile.toFile()), "UTF-8");
      kodi = (MovieToKodiNfoConnector) um.unmarshal(in);
    }
    catch (UnmarshalException | IllegalArgumentException e) {
    }
    finally {
      if (in != null) {
        in.close();
      }
    }

    if (kodi == null) {
      // now trying to parse it via string
      String completeNFO = Utils.readFileToString(nfoFile).trim().replaceFirst("^([\\W]+)<", "<");
      Matcher matcher = PATTERN_NFO_MOVIE_TAG.matcher(completeNFO);
      if (matcher.find()) {
        completeNFO = matcher
            .replaceFirst("<movie xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
      }
      try {
        in = new StringReader(ParserUtils.cleanNfo(completeNFO));
        kodi = (MovieToKodiNfoConnector) um.unmarshal(in);
      }
      finally {
        if (in != null) {
          in.close();
        }
      }
    }
    return kodi;
  }

  private void addActor(String name, String role, String thumb) {
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

  private void addProducer(String name, String role, String thumb) {
    Producer producer = new Producer(name, role, thumb);
    producers.add(producer);
  }

  public List<Producer> getProducers() {
    // @XmlAnyElement(lax = true) causes all unsupported tags to be in producers;
    // filter producers out
    List<Producer> pureProducers = new ArrayList<>();
    // for (Object obj : producers) {
    for (Object obj : actors) { // ugly hack for invalid xml structure
      if (obj instanceof Producer) {
        Producer producer = (Producer) obj;
        pureProducers.add(producer);
      }
    }
    return pureProducers;
  }

  private static String prepareTrailerForKodi(MovieTrailer trailer) {
    // youtube trailer are stored in a special notation: plugin://plugin.video.youtube/?action=play_video&videoid=<ID>
    // parse out the ID from the url and store it in the right notation
    Pattern pattern = Pattern.compile("https{0,1}://.*youtube..*/watch\\?v=(.*)$");
    Matcher matcher = pattern.matcher(trailer.getUrl());
    if (matcher.matches()) {
      return "plugin://plugin.video.youtube/?action=play_video&videoid=" + matcher.group(1);
    }

    // other urls are handled by the hd-trailers.net plugin
    pattern = Pattern.compile("https{0,1}://.*(apple.com|yahoo-redir|yahoo.com|youtube.com|moviefone.com|ign.com|hd-trailers.net|aol.com).*");
    matcher = pattern.matcher(trailer.getUrl());
    if (matcher.matches()) {
      try {
        return "plugin://plugin.video.hdtrailers_net/video/" + matcher.group(1) + "/" + URLEncoder.encode(trailer.getUrl(), "UTF-8");
      }
      catch (Exception e) {
        LOGGER.error("failed to escape " + trailer.getUrl());
      }
    }
    // everything else is stored directly
    return trailer.getUrl();
  }

  private static String parseTrailerUrl(String nfoTrailerUrl) {
    // try to parse out youtube trailer plugin
    Pattern pattern = Pattern.compile("plugin://plugin.video.youtube/\\?action=play_video&videoid=(.*)$");
    Matcher matcher = pattern.matcher(nfoTrailerUrl);
    if (matcher.matches()) {
      return "http://www.youtube.com/watch?v=" + matcher.group(1);
    }

    pattern = Pattern.compile("plugin://plugin.video.hdtrailers_net/video/.*\\?/(.*)$");
    matcher = pattern.matcher(nfoTrailerUrl);
    if (matcher.matches()) {
      try {
        return URLDecoder.decode(matcher.group(1), "UTF-8");
      }
      catch (UnsupportedEncodingException e) {
        LOGGER.error("failed to unescape " + nfoTrailerUrl);
      }
    }

    return nfoTrailerUrl;
  }

  /*
   * inner class actor to represent actors
   */
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

  /*
   * inner class holding file informations
   */
  static class Fileinfo {
    public Streamdetails streamdetails;

    public Fileinfo() {
      streamdetails = new Streamdetails();
    }
  }

  /*
   * inner class holding details of audio and video stream
   */
  static class Streamdetails {
    public Video          video;
    public List<Audio>    audio;
    public List<Subtitle> subtitle;

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
    public String codec;
    public String aspect;
    public int    width;
    public int    height;
    public int    durationinseconds;
    public String stereomode;
  }

  /*
   * inner class holding details of the audio stream
   */
  static class Audio {
    public String codec;
    public String language;
    public String channels;
  }

  /*
   * inner class holding details of the subtitle stream
   */
  static class Subtitle {
    public String language;
  }

  static class Set {
    public String name     = "";
    public String overview = "";

    List<String>  mixed;

    @XmlMixed
    public List<String> getMixed() {
      return mixed;
    }

    public void setMixed(List<String> mixed) {
      this.mixed = mixed;
    }
  }

  static class MovieSetAdapter extends XmlAdapter<Set, Set> {

    @Override
    public Set marshal(Set set) throws Exception {
      // write code for marshall
      if (StringUtils.isBlank(set.name)) {
        return null;
      }

      // return "<set><name>" + set.name + "</name><overview>" + set.overview + "</overview></set>";
      return set;
    }

    @Override
    public Set unmarshal(Set v) throws Exception {
      Set movieSet = new Set();

      if (StringUtils.isBlank(v.name) && !v.mixed.isEmpty()) {
        try {
          movieSet.name = v.mixed.get(0);
        }
        catch (Exception ignored) {
        }
      }

      if (StringUtils.isBlank(movieSet.name)) {
        movieSet.name = v.name;
        movieSet.overview = v.overview;
      }

      return movieSet;
    }
  }
}
