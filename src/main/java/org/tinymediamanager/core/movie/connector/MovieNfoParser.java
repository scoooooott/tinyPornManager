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

import static org.tinymediamanager.core.entities.Person.Type.ACTOR;
import static org.tinymediamanager.core.entities.Person.Type.DIRECTOR;
import static org.tinymediamanager.core.entities.Person.Type.PRODUCER;
import static org.tinymediamanager.core.entities.Person.Type.WRITER;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.movie.MovieEdition;
import org.tinymediamanager.core.movie.MovieHelpers;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.movie.entities.MovieTrailer;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.MetadataUtil;
import org.tinymediamanager.scraper.util.ParserUtils;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * The class MovieNfoParser is used to parse all types of NFO/XML files
 * 
 * @author Manuel Laggner
 */
public class MovieNfoParser {
  private Element            root;
  private final List<String> supportedElements   = new ArrayList<>();

  public String              title               = "";
  public String              originaltitle       = "";
  public String              sorttitle           = "";
  public int                 year                = 0;
  public Set                 set                 = null;
  public int                 top250              = 0;
  public String              plot                = "";
  public String              outline             = "";
  public String              tagline             = "";
  public int                 runtime             = 0;
  public Certification       certification       = Certification.NOT_RATED;
  public String              country             = "";
  public Date                releaseDate         = null;
  public boolean             watched             = false;
  public int                 playcount           = 0;
  public String              languages           = "";
  public MediaSource         source              = MediaSource.UNKNOWN;
  public MovieEdition        edition             = MovieEdition.NONE;
  public String              trailer             = "";

  public Map<String, Object> ids                 = new HashMap<>();
  public Map<String, Rating> ratings             = new HashMap<>();

  public List<String>        posters             = new ArrayList<>();
  public List<String>        fanarts             = new ArrayList<>();
  public List<MediaGenres>   genres              = new ArrayList<>();
  public List<String>        studios             = new ArrayList<>();
  public List<String>        tags                = new ArrayList<>();
  public List<Person>        actors              = new ArrayList<>();
  public List<Person>        producers           = new ArrayList<>();
  public List<Person>        directors           = new ArrayList<>();
  public List<Person>        credits             = new ArrayList<>();

  public List<String>        unsupportedElements = new ArrayList<>();

  /* some xbmc related tags we parse, but do not use internally */
  public Fileinfo            fileinfo            = null;
  public String              epbookmark          = "";
  public Date                lastplayed          = null;
  public String              status              = "";
  public String              code                = "";
  public Date                dateadded           = null;

  /**
   * create a new instance by parsing the document
   * 
   * @param document
   *          the document returned by JSOUP.parse()
   */
  private MovieNfoParser(Document document) {
    // first check if there is a valid root object
    Elements elements = document.select("movie");
    if (elements.isEmpty()) {
      return;
    }

    document.outputSettings().prettyPrint(false);

    this.root = elements.get(0);

    // parse all supported fields
    parseTitle();
    parseOriginalTitle();
    parseSorttitle();
    parseRatingAndVotes();
    parseSet();
    parseYear();
    parseTop250();
    parsePlot();
    parseOutline();
    parseTagline();
    parseRuntime();
    parsePosters();
    parseFanarts();
    parseCertification();
    parseIds();
    parseCountry();
    parseReleaseDate();
    parseWatchedAndPlaycount();
    parseGenres();
    parseStudios();
    parseCredits();
    parseDirectors();
    parseTags();
    parseActors();
    parseProducers();
    parseFileinfo();
    parseLanguages();
    parseSource();
    parseEdition();
    parseTrailer();

    parseEpbookmark();
    parseLastplayed();
    parseStatus();
    parseCode();
    parseDateadded();
    findUnsupportedElements();
  }

  /**
   * parse the given file
   * 
   * @param path
   *          the path to the NFO/XML to be parsed
   * @return a new instance of the parser class
   * @throws Exception
   *           any exception if parsing fails
   */
  public static MovieNfoParser parseNfo(Path path) throws Exception {
    return new MovieNfoParser(Jsoup.parse(new FileInputStream(path.toFile()), "UTF-8", "", Parser.xmlParser()));
  }

  /**
   * parse the xml content
   *
   * @param content
   *          the content of the NFO/XML to be parsed
   * @return a new instance of the parser class
   * @throws Exception
   *           any exception if parsing fails
   */
  public static MovieNfoParser parseNfo(String content) throws Exception {
    return new MovieNfoParser(Jsoup.parse(content, "", Parser.xmlParser()));
  }

  /**
   * determines whether this was a valid NFO or not<br />
   * we use several fields which should be filled in a valid NFO for decision
   * 
   * @return true/false
   */
  public boolean isValidNfo() {
    if (StringUtils.isBlank(title)) {
      return false;
    }

    if (year <= 0) {
      return false;
    }

    return true;
  }

  private Element getSingleElement(Element parent, String tag) {
    Elements elements = parent.select(parent.tagName() + " > " + tag);
    if (elements.size() != 1) {
      return null;
    }
    return elements.get(0);
  }

  /**
   * the title usually comes in the title tag
   */
  private void parseTitle() {
    supportedElements.add("title");

    Element element = getSingleElement(root, "title");
    if (element != null) {
      title = element.ownText();
    }
  }

  /**
   * the original title usually comes in the originaltitle tag
   */
  private void parseOriginalTitle() {
    supportedElements.add("originaltitle");

    Element element = getSingleElement(root, "originaltitle");
    if (element != null) {
      originaltitle = element.ownText();
    }
  }

  /**
   * the sorttitle usually comes in the sorttitle tag
   */
  private void parseSorttitle() {
    supportedElements.add("sorttitle");

    Element element = getSingleElement(root, "sorttitle");
    if (element != null) {
      sorttitle = element.ownText();
    }
  }

  /**
   * rating and votes are either in<br />
   * - two separate fields: rating, votes (old style) or<br />
   * - in a nested ratings field (new style)
   */
  private void parseRatingAndVotes() {
    supportedElements.add("rating");
    supportedElements.add("userrating");
    supportedElements.add("ratings");
    supportedElements.add("votes");

    // old style
    // <rating>6.5</rating>
    // <votes>846</votes>
    Element element = getSingleElement(root, "rating");
    if (element != null) {
      Rating r = new Rating();
      r.id = Rating.DEFAULT;
      try {
        r.rating = Float.parseFloat(element.ownText());
      }
      catch (Exception ignored) {
      }
      element = getSingleElement(root, "votes");
      if (element != null) {
        try {
          r.votes = ParserUtils.parseInt(element.ownText()); // replace thousands separator
        }
        catch (Exception ignored) {
        }
      }
      if (r.rating > 0) {
        ratings.put(r.id, r);
      }
    }

    // user rating
    // <userrating>8</userrating>
    element = getSingleElement(root, "userrating");
    if (element != null) {
      try {
        Rating r = new Rating();
        r.id = Rating.USER;
        r.rating = Float.parseFloat(element.ownText());
        if (r.rating > 0) {
          ratings.put(r.id, r);
        }
      }
      catch (Exception ignored) {
      }
    }

    // new style
    // <ratings>
    // <rating name="default" max="10" default="true"> <value>5.800000</value> <votes>2100</votes> </rating>
    // <rating name="imdb"> <value>8.9</value> <votes>12345</votes> </rating>
    // </ratings>
    element = getSingleElement(root, "ratings");
    if (element != null) {
      for (Element ratingChild : element.select(element.tagName() + " > rating")) {
        Rating r = new Rating();
        // name
        r.id = ratingChild.attr("name");

        // maxvalue
        try {
          r.maxValue = ParserUtils.parseInt(ratingChild.attr("max"));
        }
        catch (NumberFormatException ignored) {
        }

        for (Element child : ratingChild.children()) {
          // value & votes
          switch (child.tagName()) {
            case "value":
              try {
                r.rating = Float.parseFloat(child.ownText());
              }
              catch (NumberFormatException ignored) {
              }
              break;

            case "votes":
              try {
                r.votes = ParserUtils.parseInt(child.ownText());
              }
              catch (Exception ignored) {
              }
              break;
          }
        }

        if (StringUtils.isNotBlank(r.id) && r.rating > 0) {
          ratings.put(r.id, r);
        }
      }
    }
  }

  /**
   * the movie set comes in a few different flavors<br />
   * - old kodi/xbmc style is simply a set tag with the set name<br />
   * - new kodi style is a set tag with a name and overview child<br />
   * - old mediaportal style is a sets tag with a set child<br />
   * - new mediaportal style is a set tag with the set name
   */
  private void parseSet() {
    supportedElements.add("sets");
    supportedElements.add("set");

    // old mp style:
    // <sets> <set order="1">set name</set> </sets>
    Element element = getSingleElement(root, "sets");
    if (element != null) {
      element = getSingleElement(element, "set");
      if (element != null && StringUtils.isNotBlank(element.ownText())) {
        set = new Set();
        set.name = element.ownText();
      }
    }
    else {
      element = getSingleElement(root, "set");
      // new kodi style
      // <set> <name>set name</name><overview>set overview</overview></set>
      if (element != null && !element.children().isEmpty()) {
        Set tmp = new Set();
        for (Element child : element.children()) {
          switch (child.tagName()) {
            case "name":
              tmp.name = child.ownText();
              break;

            case "overview":
              tmp.overview = child.ownText();
              break;
          }
        }
        if (StringUtils.isNotBlank(tmp.name)) {
          set = tmp;
        }
      }
      // old kodi/xbmc or new mediaportal style
      // <set>set name</set>
      else if (element != null && StringUtils.isNotBlank(element.ownText())) {
        set = new Set();
        set.name = element.ownText();
        set.overview = "";
      }
    }
  }

  /**
   * the year usually comes in the year tag as an integer
   */
  private void parseYear() {
    supportedElements.add("year");

    Element element = getSingleElement(root, "year");
    if (element != null) {
      try {
        year = ParserUtils.parseInt(element.ownText());
      }
      catch (Exception ignored) {
      }
    }
  }

  /**
   * the top250 usually comes in the top250 tag as an integer (or empty)
   */
  private void parseTop250() {
    supportedElements.add("top250");

    Element element = getSingleElement(root, "top250");
    if (element != null) {
      try {
        top250 = ParserUtils.parseInt(element.ownText());
      }
      catch (Exception ignored) {
      }
    }
  }

  /**
   * the plot usually comes in the plot tag as an integer (or empty)
   */
  private void parsePlot() {
    supportedElements.add("plot");

    Element element = getSingleElement(root, "plot");
    if (element != null) {
      plot = element.ownText();
    }
  }

  /**
   * the outline usually comes in the outline tag as an integer (or empty)
   */
  private void parseOutline() {
    supportedElements.add("outline");

    Element element = getSingleElement(root, "outline");
    if (element != null) {
      outline = element.ownText();
    }
  }

  /**
   * the tagline usually comes in the tagline tag as an integer (or empty)
   */
  private void parseTagline() {
    supportedElements.add("tagline");

    Element element = getSingleElement(root, "tagline");
    if (element != null) {
      tagline = element.ownText();
    }
  }

  /**
   * the runtime usually comes in the runtime tag as an integer
   */
  private void parseRuntime() {
    supportedElements.add("runtime");

    Element element = getSingleElement(root, "runtime");
    if (element != null) {
      try {
        runtime = ParserUtils.parseInt(element.ownText());
      }
      catch (Exception ignored) {
      }
    }
  }

  /**
   * posters can come in several different forms<br />
   * - kodi usually puts it into thumb tags (multiple; in newer versions with an aspect attribute)<br />
   * - mediaportal puts it also in thumb tags (single)
   */
  private void parsePosters() {
    supportedElements.add("thumb");

    // get all thumb elements
    Elements thumbs = root.select(root.tagName() + " > thumb");
    if (!thumbs.isEmpty()) {
      for (Element element : thumbs) {
        // if there is an aspect attribute, it has to be poster
        if (element.hasAttr("aspect") && !element.attr("aspect").equals("poster")) {
          continue;
        }
        if (StringUtils.isNotBlank(element.ownText()) && element.ownText().matches("https?://.*")) {
          posters.add(element.ownText());
        }
      }
    }
  }

  /**
   * fanarts can come in several different forms<br />
   * - xbmc had it in one single fanart tag (no nested tags)<br />
   * - kodi usually puts it into a fanart tag (in newer versions a nested thumb tag)<br />
   * - mediaportal puts it also into a fanart tag (with nested thumb tags)
   */
  private void parseFanarts() {
    supportedElements.add("fanart");

    // get all thumb elements
    Element fanart = getSingleElement(root, "fanart");
    if (fanart != null) {
      Elements thumbs = fanart.select(fanart.tagName() + " > thumb");
      // thumb children available
      if (!thumbs.isEmpty()) {
        for (Element element : thumbs) {
          if (StringUtils.isNotBlank(element.ownText()) && element.ownText().matches("https?://.*")) {
            fanarts.add(element.ownText());
          }
        }
      }
      // no children - get own text
      else if (StringUtils.isNotBlank(fanart.ownText()) && fanart.ownText().matches("https?://.*")) {
        fanarts.add(fanart.ownText());
      }
    }
  }

  /**
   * certification will come in the certification or mpaa tag<br />
   * - kodi has both tags filled, but certification has a much more clear format<br />
   * - mediaportal has only mpaa filled
   */
  private void parseCertification() {
    supportedElements.add("certification");
    supportedElements.add("mpaa");

    Element element = getSingleElement(root, "certification");
    if (element == null) {
      element = getSingleElement(root, "mpaa");
    }
    if (element != null) {
      certification = MovieHelpers.parseCertificationStringForMovieSetupCountry(element.ownText());
    }
  }

  /**
   * ids can be stored either in the<br />
   * - id tag (imdbID) or<br />
   * - imdb tag (imdbId) or<br />
   * - tmdbId tag (tmdb Id> or<br />
   * - in a special nested tag (tmm store)
   */
  private void parseIds() {
    supportedElements.add("id");
    supportedElements.add("imdb");
    supportedElements.add("tmdbid");
    supportedElements.add("ids");

    // id tag & check against imdb pattern (otherwise we cannot say for which provider this id is)
    Element element = getSingleElement(root, "id");
    if (element != null && MetadataUtil.isValidImdbId(element.ownText())) {
      ids.put(MediaMetadata.IMDB, element.ownText());
    }
    element = getSingleElement(root, "imdb");
    if (element != null && MetadataUtil.isValidImdbId(element.ownText())) {
      ids.put(MediaMetadata.IMDB, element.ownText());
    }
    // tmdbId tag
    element = getSingleElement(root, "tmdbId");
    if (element != null) {
      try {
        ids.put(MediaMetadata.TMDB, ParserUtils.parseInt(element.ownText()));
      }
      catch (NumberFormatException ignored) {
      }
    }
    // iterate over our internal id store (old JAXB style)
    element = getSingleElement(root, "ids");
    if (element != null) {
      Elements children = element.select(element.tagName() + " > entry");
      for (Element entry : children) {
        Element key = getSingleElement(entry, "key");
        Element value = getSingleElement(entry, "value");
        if (StringUtils.isNoneBlank(key.ownText(), value.ownText())) {
          // check whether the id is an integer
          try {
            ids.put(key.ownText(), ParserUtils.parseInt(value.ownText()));
          }
          catch (Exception e) {
            // store as string
            ids.put(key.ownText(), value.ownText());
          }
        }
      }
    }
    // iterate over our internal id store (new style)
    element = getSingleElement(root, "ids");
    if (element != null) {
      Elements children = element.children();
      for (Element entry : children) {
        if (StringUtils.isNoneBlank(entry.tagName(), entry.ownText())) {
          // check whether the id is an integer
          try {
            ids.put(entry.tagName(), ParserUtils.parseInt(entry.ownText()));
          }
          catch (Exception e) {
            // store as string
            ids.put(entry.tagName(), entry.ownText());
          }
        }
      }
    }
  }

  /**
   * the country usually comes in the country tag
   */
  private void parseCountry() {
    supportedElements.add("country");

    Element element = getSingleElement(root, "country");
    if (element != null) {
      country = element.ownText();
    }
  }

  /**
   * the release date is usually in the premiered tag
   */
  private void parseReleaseDate() {
    supportedElements.add("premiered");
    supportedElements.add("aired");

    Element element = getSingleElement(root, "premiered");
    if (element != null) {
      // parse a date object out of the string
      try {
        Date date = StrgUtils.parseDate(element.ownText());
        if (date != null) {
          releaseDate = date;
        }
      }
      catch (ParseException ignored) {
      }
    }
    // also look if there is an aired date
    if (releaseDate == null) {
      element = getSingleElement(root, "aired");
      if (element != null) {
        // parse a date object out of the string
        try {
          Date date = StrgUtils.parseDate(element.ownText());
          if (date != null) {
            releaseDate = date;
          }
        }
        catch (ParseException ignored) {
        }
      }
    }
  }

  /**
   * parse the watched flag (watched tag) and playcount (playcount tag) together
   */
  private void parseWatchedAndPlaycount() {
    supportedElements.add("watched");
    supportedElements.add("playcount");

    Element element = getSingleElement(root, "watched");
    if (element != null) {
      try {
        watched = Boolean.parseBoolean(element.ownText());
      }
      catch (Exception ignored) {
      }
    }

    element = getSingleElement(root, "playcount");
    if (element != null) {
      try {
        playcount = ParserUtils.parseInt(element.ownText());
        if (playcount > 0 && watched == false) {
          watched = true;
        }
      }
      catch (Exception ignored) {
      }
    }
  }

  /**
   * parse the genres tags<br />
   * - kodi has multiple genre tags<br />
   * - mediaportal as a nested genres tag
   */
  private void parseGenres() {
    supportedElements.add("genres");
    supportedElements.add("genre");

    Elements elements = null;
    Element element = getSingleElement(root, "genres");
    if (element != null) {
      // nested genre tags
      elements = element.select(element.tagName() + " > genre");
    }
    else {
      // direct/multiple genre tags in movie root
      elements = root.select(root.tagName() + " > genre");
    }

    if (elements != null && !elements.isEmpty()) {
      for (Element genre : elements) {
        if (StringUtils.isNotBlank(genre.ownText())) {
          genres.add(MediaGenres.getGenre(genre.ownText()));
        }
      }
    }
  }

  /**
   * studios come in two different flavors<br />
   * - kodi has multiple studio tags<br />
   * - mediaportal has all studios (comma separated) in one studio tag
   */
  private void parseStudios() {
    supportedElements.add("studio");

    Elements elements = root.select(root.tagName() + " > studio");
    // if there is exactly one studio tag, split the studios at the comma
    if (elements.size() == 1) {
      try {
        studios.addAll(Arrays.asList(elements.get(0).ownText().split("\\s*[,\\/]\\s*"))); // split on , or / and remove whitespace around)
      }
      catch (Exception ignored) {
      }
    }
    else {
      for (Element element : elements) {
        if (StringUtils.isNotBlank(element.ownText())) {
          studios.add(element.ownText());
        }
      }
    }
  }

  /**
   * credits come in two different flavors<br />
   * - kodi has multiple credits tags<br />
   * - mediaportal has all credits (comma separated) in one credits tag
   */
  private void parseCredits() {
    supportedElements.add("credits");

    Elements elements = root.select(root.tagName() + " > credits");
    // if there is exactly one credits tag, split the credits at the comma
    if (elements.size() == 1) {
      try {
        // split on , or / and remove whitespace around)
        List<String> creditsNames = Arrays.asList(elements.get(0).ownText().split("\\s*[,\\/]\\s*"));
        for (String credit : creditsNames) {
          Person person = new Person();
          person.name = credit;
          credits.add(person);
        }
      }
      catch (Exception ignored) {
      }
    }
    else {
      for (Element element : elements) {
        if (StringUtils.isNotBlank(element.ownText())) {
          Person person = new Person();
          person.name = element.ownText();
          credits.add(person);
        }
      }
    }
  }

  /**
   * directors come in two different flavors<br />
   * - kodi has multiple director tags<br />
   * - mediaportal has all directors (comma separated) in one director tag
   */
  private void parseDirectors() {
    supportedElements.add("director");

    Elements elements = root.select(root.tagName() + " > director");
    // if there is exactly one director tag, split the directors at the comma
    if (elements.size() == 1) {
      try {
        // split on , or / and remove whitespace around)
        List<String> directorNames = Arrays.asList(elements.get(0).ownText().split("\\s*[,\\/]\\s*"));
        for (String director : directorNames) {
          Person person = new Person();
          person.name = director;
          directors.add(person);
        }
      }
      catch (Exception ignored) {
      }
    }
    else {
      for (Element element : elements) {
        if (StringUtils.isNotBlank(element.ownText())) {
          if (StringUtils.isNotBlank(element.ownText())) {
            Person person = new Person();
            person.name = element.ownText();
            directors.add(person);
          }
        }
      }
    }
  }

  /**
   * tags usually come in a tag tag
   */
  private void parseTags() {
    supportedElements.add("tag");

    Elements elements = root.select(root.tagName() + " > tag");
    for (Element element : elements) {
      if (StringUtils.isNotBlank(element.ownText())) {
        tags.add(element.ownText());
      }
    }
  }

  /**
   * actors usually come as multiple actor tags in the root with three child tags:<br />
   * - name<br />
   * - role<br />
   * - thumb
   */
  private void parseActors() {
    supportedElements.add("actor");

    Elements elements = root.select(root.tagName() + " > actor");
    for (Element element : elements) {
      Person actor = new Person();
      for (Element child : element.children()) {
        switch (child.tagName()) {
          case "name":
            actor.name = child.ownText();
            break;

          case "role":
            actor.role = child.ownText();
            break;

          case "thumb":
            actor.thumb = child.ownText();
            break;
        }
      }
      if (StringUtils.isNotBlank(actor.name)) {
        actors.add(actor);
      }
    }
  }

  /**
   * producers usually come as multiple producer tags in the root with three child tags:<br />
   * - name<br />
   * - role<br />
   * - thumb
   */
  private void parseProducers() {
    supportedElements.add("producer");

    Elements elements = root.select(root.tagName() + " > producer");
    for (Element element : elements) {
      Person producer = new Person();
      for (Element child : element.children()) {
        switch (child.tagName().toLowerCase(Locale.ROOT)) {
          case "name":
            producer.name = child.ownText();
            break;

          case "role":
            producer.role = child.ownText();
            break;

          case "thumb":
            producer.thumb = child.ownText();
            break;
        }
      }
      if (StringUtils.isNotBlank(producer.name)) {
        producers.add(producer);
      }
    }
  }

  /**
   * parse file information.
   */
  private void parseFileinfo() {
    supportedElements.add("fileinfo");

    Element element = getSingleElement(root, "fileinfo");
    if (element != null) {
      // there is a fileinfo tag available - look if there is also a streamdetails tag
      element = getSingleElement(element, "streamdetails");
      if (element != null) {
        // available; parse out everything
        fileinfo = new Fileinfo();

        for (Element child : element.children()) {
          switch (child.tagName().toLowerCase(Locale.ROOT)) {
            case "video":
              Video video = parseVideo(child);
              if (video != null) {
                fileinfo.videos.add(video);
              }
              break;

            case "audio":
              Audio audio = parseAudio(child);
              if (audio != null) {
                fileinfo.audios.add(audio);
              }
              break;

            case "subtitle":
              Subtitle subtitle = parseSubtitle(child);
              if (subtitle != null) {
                fileinfo.subtitles.add(subtitle);
              }
              break;
          }
        }
      }
    }
  }

  private Video parseVideo(Element element) {
    Video video = new Video();
    for (Element child : element.children()) {
      switch (child.tagName().toLowerCase(Locale.ROOT)) {
        case "codec":
          video.codec = child.ownText();
          break;

        case "aspect":
          try {
            video.aspect = Float.parseFloat(child.ownText());
          }
          catch (NumberFormatException ignored) {
          }
          break;

        case "width":
          try {
            video.width = ParserUtils.parseInt(child.ownText());
          }
          catch (NumberFormatException ignored) {
          }
          break;

        case "height":
          try {
            video.height = ParserUtils.parseInt(child.ownText());
          }
          catch (NumberFormatException ignored) {
          }
          break;

        case "durationinseconds":
          try {
            video.durationinseconds = ParserUtils.parseInt(child.ownText());
          }
          catch (NumberFormatException ignored) {
          }
          break;

        case "stereomode":
          video.stereomode = child.ownText();
          break;
      }
    }

    // if there is at least the codec, we return the created object
    if (StringUtils.isNotBlank(video.codec)) {
      return video;
    }

    return null;
  }

  private Audio parseAudio(Element element) {
    Audio audio = new Audio();
    for (Element child : element.children()) {
      switch (child.tagName().toLowerCase(Locale.ROOT)) {
        case "codec":
          audio.codec = child.ownText();
          break;

        case "language":
          audio.language = child.ownText();
          break;

        case "channels":
          try {
            audio.channels = ParserUtils.parseInt(child.ownText());
          }
          catch (NumberFormatException ignored) {
          }
      }
    }

    // if there is at least the codec, we return the created object
    if (StringUtils.isNotBlank(audio.codec)) {
      return audio;
    }
    return null;
  }

  private Subtitle parseSubtitle(Element element) {
    Subtitle subtitle = new Subtitle();
    for (Element child : element.children()) {
      switch (child.tagName().toLowerCase(Locale.ROOT)) {
        case "language":
          subtitle.language = child.ownText();
          break;
      }
    }

    // if there is at least the codec, we return the created object
    if (StringUtils.isNotBlank(subtitle.language)) {
      return subtitle;
    }
    return null;
  }

  /**
   * spoken languages are usually stored in the languages tag
   */
  private void parseLanguages() {
    supportedElements.add("languages");

    Element element = getSingleElement(root, "languages");
    if (element != null) {
      languages = element.ownText();
    }

    // if the languages are in MediaPortal style, parse them and prepare them for tmm
    if (StringUtils.isNotBlank(languages)) {
      List<String> languages = new ArrayList<>();
      for (String langu : this.languages.split("\\|")) {
        langu = langu.trim();
        String languIso = LanguageUtils.getIso2LanguageFromLocalizedString(langu);
        if (StringUtils.isNotBlank(languIso)) {
          languages.add(languIso);
        }
        else {
          languages.add(langu);
        }
      }
      this.languages = StringUtils.join(languages.toArray(), ", ");
    }
  }

  /**
   * the media source is usually in the source tag
   */
  private void parseSource() {
    supportedElements.add("source");

    Element element = getSingleElement(root, "source");
    if (element != null) {
      try {
        source = MediaSource.getMediaSource(element.ownText());
      }
      catch (Exception ignored) {
      }
    }
  }

  /**
   * the edition is usually in the edition tag
   */
  private void parseEdition() {
    supportedElements.add("edition");

    Element element = getSingleElement(root, "edition");
    if (element != null) {
      try {
        edition = MovieEdition.getMovieEditionFromString(element.ownText());
      }
      catch (Exception ignored) {
      }
    }
  }

  /**
   * a trailer is usually in the trailer tag
   */
  private void parseTrailer() {
    supportedElements.add("trailer");

    Element element = getSingleElement(root, "trailer");
    if (element != null) {
      // the trailer can come as a plain http link or prepared for kodi

      // try to parse out youtube trailer plugin
      Pattern pattern = Pattern.compile("plugin://plugin.video.youtube/\\?action=play_video&videoid=(.*)$");
      Matcher matcher = pattern.matcher(element.ownText());
      if (matcher.matches()) {
        trailer = "http://www.youtube.com/watch?v=" + matcher.group(1);
      }
      else {
        pattern = Pattern.compile("plugin://plugin.video.hdtrailers_net/video/.*\\?/(.*)$");
        matcher = pattern.matcher(element.ownText());
        if (matcher.matches()) {
          try {
            trailer = URLDecoder.decode(matcher.group(1), "UTF-8");
          }
          catch (UnsupportedEncodingException ignored) {
          }
        }
      }

      // pure http link
      if (StringUtils.isNotBlank(element.ownText()) && element.ownText().matches("https?://.*")) {
        trailer = element.ownText();
      }
    }
  }

  /**
   * find epbookmark for xbmc related nfos
   */
  private void parseEpbookmark() {
    supportedElements.add("epbookmark");

    Element element = getSingleElement(root, "epbookmark");
    if (element != null) {
      epbookmark = element.ownText();
    }
  }

  /**
   * find lastplayed for xbmc related nfos
   */
  private void parseLastplayed() {
    supportedElements.add("lastplayed");

    Element element = getSingleElement(root, "lastplayed");
    if (element != null) {
      // parse a date object out of the string
      try {
        Date date = StrgUtils.parseDate(element.ownText());
        if (date != null) {
          lastplayed = date;
        }
      }
      catch (ParseException ignored) {
      }
    }
  }

  /**
   * find status for xbmc related nfos
   */
  private void parseStatus() {
    supportedElements.add("status");

    Element element = getSingleElement(root, "status");
    if (element != null) {
      status = element.ownText();
    }
  }

  /**
   * find code for xbmc related nfos
   */
  private void parseCode() {
    supportedElements.add("code");

    Element element = getSingleElement(root, "code");
    if (element != null) {
      code = element.ownText();
    }
  }

  /**
   * find dateadded for xbmc related nfos
   */
  private void parseDateadded() {
    supportedElements.add("dateadded");

    Element element = getSingleElement(root, "dateadded");
    if (element != null) {
      // parse a date object out of the string
      try {
        Date date = StrgUtils.parseDate(element.ownText());
        if (date != null) {
          dateadded = date;
        }
      }
      catch (ParseException ignored) {
      }
    }
  }

  /**
   * find and store all unsupported tags
   */
  private void findUnsupportedElements() {
    // get all children of the root
    for (Element element : root.children()) {
      if (!supportedElements.contains(element.tagName())) {
        String elementText = element.toString().replaceAll(">\\r?\\n\\s*<", "><");
        unsupportedElements.add(elementText);
      }
    }
  }

  /**
   * morph this instance to a movie object
   *
   * @return the movie Object
   */
  public Movie toMovie() {
    Movie movie = new Movie();
    movie.setTitle(title);
    movie.setOriginalTitle(originaltitle);

    // legacy
    Rating rating = ratings.get(Rating.DEFAULT);
    if (rating != null) {
      movie.setRating(rating.rating);
      movie.setVotes(rating.votes);
    }
    movie.setYear(year);
    movie.setTop250(top250);
    movie.setReleaseDate(releaseDate);
    movie.setPlot(plot);
    movie.setTagline(tagline);
    movie.setRuntime(runtime);

    if (!posters.isEmpty()) {
      movie.setArtworkUrl(posters.get(0), MediaFileType.POSTER);
    }

    if (!fanarts.isEmpty()) {
      movie.setArtworkUrl(fanarts.get(0), MediaFileType.FANART);
    }

    for (Map.Entry<String, Object> entry : ids.entrySet()) {
      movie.setId(entry.getKey(), entry.getValue());
    }

    String studio = StringUtils.join(studios, " / ");
    if (studio == null) {
      movie.setProductionCompany("");
    }
    else {
      movie.setProductionCompany(studio);
    }

    movie.setCountry(country);
    movie.setCertification(certification);

    movie.setWatched(watched);
    if (playcount > 0) {
      movie.setWatched(true);
    }
    movie.setSpokenLanguages(languages);
    movie.setMediaSource(source);
    movie.setEdition(edition);

    // movieset
    if (set != null && StringUtils.isNotEmpty(set.name)) {
      // search for that movieset
      MovieList movieList = MovieList.getInstance();
      MovieSet movieSet = movieList.getMovieSet(set.name, 0);

      // add movie to movieset
      if (movieSet != null) {
        if (StringUtils.isBlank(movieSet.getPlot())) {
          movieSet.setPlot(set.overview);
        }
        movie.setMovieSet(movieSet);
      }
    }

    movie.setSortTitle(sorttitle);

    for (Person actor : actors) {
      org.tinymediamanager.core.entities.Person cast = new org.tinymediamanager.core.entities.Person(ACTOR, actor.name, actor.role);
      cast.setThumbUrl(actor.thumb);
      movie.addActor(cast);
    }

    for (Person producer : producers) {
      org.tinymediamanager.core.entities.Person cast = new org.tinymediamanager.core.entities.Person(PRODUCER, producer.name, producer.role);
      cast.setThumbUrl(producer.thumb);
      movie.addProducer(cast);
    }

    for (Person director : directors) {
      org.tinymediamanager.core.entities.Person cast = new org.tinymediamanager.core.entities.Person(DIRECTOR, director.name, "Director");
      movie.addDirector(cast);
    }

    for (Person writer : credits) {
      org.tinymediamanager.core.entities.Person cast = new org.tinymediamanager.core.entities.Person(WRITER, writer.name, "Writer");
      movie.addWriter(cast);
    }

    for (MediaGenres genre : genres) {
      movie.addGenre(genre);
    }

    if (StringUtils.isNotEmpty(trailer)) {
      if (!trailer.startsWith("file")) {
        // only add new MT when not a local file
        MovieTrailer trailer = new MovieTrailer();
        trailer.setName("fromNFO");
        trailer.setProvider("from NFO");
        trailer.setQuality("unknown");
        trailer.setUrl(this.trailer);
        trailer.setInNfo(true);
        movie.addTrailer(trailer);
      }
    }

    for (String tag : tags) {
      movie.addToTags(tag);
    }

    return movie;
  }

  /*
   * entity classes
   */
  public static class Set {
    public String name     = "";
    public String overview = "";
  }

  public static class Rating {
    public static final String DEFAULT  = "default";
    public static final String USER     = "user";

    public String              id       = "";
    public float               rating   = 0;
    public int                 votes    = 0;
    public int                 maxValue = 10;
  }

  public static class Person {
    public String name  = "";
    public String role  = "";
    public String thumb = "";
  }

  public static class Fileinfo {
    public List<Video>    videos    = new ArrayList<>();
    public List<Audio>    audios    = new ArrayList<>();
    public List<Subtitle> subtitles = new ArrayList<>();
  }

  public static class Video {
    public String codec      = "";
    public float  aspect     = 0f;
    public int    width      = 0;
    public int    height     = 0;
    public int    durationinseconds;
    public String stereomode = "";
  }

  public static class Audio {
    public String codec    = "";
    public String language = "";
    public int    channels = 0;
  }

  public static class Subtitle {
    public String language;
  }
}
