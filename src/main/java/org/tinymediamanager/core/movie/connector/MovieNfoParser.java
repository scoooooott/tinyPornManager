/*
 * Copyright 2012 - 2020 Manuel Laggner
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.MediaTrailer;
import org.tinymediamanager.core.movie.MovieEdition;
import org.tinymediamanager.core.movie.MovieHelpers;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.MetadataUtil;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * The class MovieNfoParser is used to parse all types of NFO/XML files
 * 
 * @author Manuel Laggner
 */
public class MovieNfoParser {
  private static final Logger LOGGER              = LoggerFactory.getLogger(MovieNfoParser.class);

  private Element             root;
  private final List<String>  supportedElements   = new ArrayList<>();

  public String               title               = "";
  public String               originaltitle       = "";
  public String               sorttitle           = "";
  public int                  year                = -1;
  public Set                  set                 = null;
  public int                  top250              = 0;
  public String               plot                = "";
  public String               outline             = "";
  public String               tagline             = "";
  public int                  runtime             = 0;
  public MediaCertification   certification       = MediaCertification.UNKNOWN;
  public Date                 releaseDate         = null;
  public boolean              watched             = false;
  public int                  playcount           = 0;
  public String               languages           = "";
  public MediaSource          source              = MediaSource.UNKNOWN;
  public MovieEdition         edition             = MovieEdition.NONE;
  public String               trailer             = "";
  public String               originalFilename    = "";
  public String               userNote            = "";

  public Map<String, Object>  ids                 = new HashMap<>();
  public Map<String, Rating>  ratings             = new HashMap<>();

  public List<String>         posters             = new ArrayList<>();
  public List<String>         fanarts             = new ArrayList<>();
  public List<MediaGenres>    genres              = new ArrayList<>();
  public List<String>         countries           = new ArrayList<>();
  public List<String>         studios             = new ArrayList<>();
  public List<String>         tags                = new ArrayList<>();
  public List<Person>         actors              = new ArrayList<>();
  public List<Person>         producers           = new ArrayList<>();
  public List<Person>         directors           = new ArrayList<>();
  public List<Person>         credits             = new ArrayList<>();

  public List<String>         unsupportedElements = new ArrayList<>();

  /* some xbmc related tags we parse, but do not use internally */
  public Fileinfo             fileinfo            = null;
  public String               epbookmark          = "";
  public Date                 lastplayed          = null;
  public String               status              = "";
  public String               code                = "";
  public Date                 dateadded           = null;

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
    parseTag(MovieNfoParser::parseTitle);
    parseTag(MovieNfoParser::parseOriginalTitle);
    parseTag(MovieNfoParser::parseSorttitle);
    parseTag(MovieNfoParser::parseRatingAndVotes);
    parseTag(MovieNfoParser::parseSet);
    parseTag(MovieNfoParser::parseYear);
    parseTag(MovieNfoParser::parseTop250);
    parseTag(MovieNfoParser::parsePlot);
    parseTag(MovieNfoParser::parseOutline);
    parseTag(MovieNfoParser::parseTagline);
    parseTag(MovieNfoParser::parseRuntime);
    parseTag(MovieNfoParser::parsePosters);
    parseTag(MovieNfoParser::parseFanarts);
    parseTag(MovieNfoParser::parseCertification);
    parseTag(MovieNfoParser::parseIds);
    parseTag(MovieNfoParser::parseCountry);
    parseTag(MovieNfoParser::parseReleaseDate);
    parseTag(MovieNfoParser::parseWatchedAndPlaycount);
    parseTag(MovieNfoParser::parseGenres);
    parseTag(MovieNfoParser::parseStudios);
    parseTag(MovieNfoParser::parseCredits);
    parseTag(MovieNfoParser::parseDirectors);
    parseTag(MovieNfoParser::parseTags);
    parseTag(MovieNfoParser::parseActors);
    parseTag(MovieNfoParser::parseProducers);
    parseTag(MovieNfoParser::parseFileinfo);
    parseTag(MovieNfoParser::parseLanguages);
    parseTag(MovieNfoParser::parseSource);
    parseTag(MovieNfoParser::parseEdition);
    parseTag(MovieNfoParser::parseTrailer);

    parseTag(MovieNfoParser::parseEpbookmark);
    parseTag(MovieNfoParser::parseLastplayed);
    parseTag(MovieNfoParser::parseStatus);
    parseTag(MovieNfoParser::parseCode);
    parseTag(MovieNfoParser::parseDateadded);
    parseTag(MovieNfoParser::parseOriginalFilename);
    parseTag(MovieNfoParser::parseUserNote);

    // MUST BE THE LAST ONE!
    parseTag(MovieNfoParser::findUnsupportedElements);
  }

  /**
   * parse the tag in a save way
   *
   * @param function
   *          the parsing function to be executed
   */
  private void parseTag(Function<MovieNfoParser, Void> function) {
    try {
      function.apply(this);
    }
    catch (Exception e) {
      LOGGER.warn("problem parsing tag (line {}): {}", e.getStackTrace()[0].getLineNumber(), e.getMessage());
    }
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
  public static MovieNfoParser parseNfo(String content) {
    return new MovieNfoParser(Jsoup.parse(content, "", Parser.xmlParser()));
  }

  /**
   * determines whether this was a valid NFO or not<br />
   * we use several fields which should be filled in a valid NFO for decision
   * 
   * @return true/false
   */
  public boolean isValidNfo() {
    // we're happy if at least the title could be parsed
    return StringUtils.isNotBlank(title);
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
  private Void parseTitle() {
    supportedElements.add("title");

    Element element = getSingleElement(root, "title");
    if (element != null) {
      title = element.ownText();
    }

    return null;
  }

  /**
   * the original title usually comes in the originaltitle tag
   */
  private Void parseOriginalTitle() {
    supportedElements.add("originaltitle");

    Element element = getSingleElement(root, "originaltitle");
    if (element != null) {
      originaltitle = element.ownText();
    }

    return null;
  }

  /**
   * the sorttitle usually comes in the sorttitle tag
   */
  private Void parseSorttitle() {
    supportedElements.add("sorttitle");

    Element element = getSingleElement(root, "sorttitle");
    if (element != null) {
      sorttitle = element.ownText();
    }

    return null;
  }

  /**
   * rating and votes are either in<br />
   * - two separate fields: rating, votes (old style) or<br />
   * - in a nested ratings field (new style)
   */
  private Void parseRatingAndVotes() {
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
      r.id = MediaRating.NFO;
      try {
        r.rating = Float.parseFloat(element.ownText());
      }
      catch (Exception ignored) {
      }
      element = getSingleElement(root, "votes");
      if (element != null) {
        try {
          r.votes = MetadataUtil.parseInt(element.ownText()); // replace thousands separator
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
        r.id = MediaRating.USER;
        r.rating = Float.parseFloat(element.ownText());
        if (r.rating > 0) {
          ratings.put(r.id, r);
        }
      }
      catch (Exception ignored) {
        // just ignore
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

        // Kodi writes tmdb votes as "themoviedb"
        if ("themoviedb".equals(r.id)) {
          r.id = MediaMetadata.TMDB;
        }

        // maxvalue
        try {
          r.maxValue = MetadataUtil.parseInt(ratingChild.attr("max"));
        }
        catch (NumberFormatException ignored) {
          // just ignore
        }

        for (Element child : ratingChild.children()) {
          // value & votes
          switch (child.tagName()) {
            case "value":
              try {
                r.rating = Float.parseFloat(child.ownText());
              }
              catch (NumberFormatException ignored) {
                // just ignore
              }
              break;

            case "votes":
              try {
                r.votes = MetadataUtil.parseInt(child.ownText());
              }
              catch (Exception ignored) {
                // just ignore
              }
              break;

            default:
              break;
          }
        }

        if (StringUtils.isNotBlank(r.id) && r.rating > 0) {
          ratings.put(r.id, r);
        }
      }
    }

    return null;
  }

  /**
   * the movie set comes in a few different flavors<br />
   * - old kodi/xbmc style is simply a set tag with the set name<br />
   * - new kodi style is a set tag with a name and overview child<br />
   * - old mediaportal style is a sets tag with a set child<br />
   * - new mediaportal style is a set tag with the set name
   */
  private Void parseSet() {
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

      if (element == null) {
        // okay, there is no single <set> tag; maybe it is a mp NFO format with multiple sets; just pick the first one
        element = root.select(root.tagName() + " > " + "set").first();
      }

      // new kodi style
      // <set> <name>set name</name><overview>set overview</overview></set>
      if (element != null && !element.children().isEmpty()) {
        Set tmp = new Set();
        for (Element child : element.children()) {
          switch (child.tagName()) {
            case "name":
            case "setname":
              tmp.name = child.ownText();
              break;

            case "overview":
            case "setdescription":
              tmp.overview = child.ownText();
              break;

            default:
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

    return null;
  }

  /**
   * the year usually comes in the year tag as an integer
   */
  private Void parseYear() {
    supportedElements.add("year");

    Element element = getSingleElement(root, "year");
    if (element != null) {
      try {
        year = MetadataUtil.parseInt(element.ownText());
      }
      catch (Exception ignored) {
        // just ignore
      }
    }

    return null;
  }

  /**
   * the top250 usually comes in the top250 tag as an integer (or empty)
   */
  private Void parseTop250() {
    supportedElements.add("top250");

    Element element = getSingleElement(root, "top250");
    if (element != null) {
      try {
        top250 = MetadataUtil.parseInt(element.ownText());
      }
      catch (Exception ignored) {
        // just ignore
      }
    }

    return null;
  }

  /**
   * the plot usually comes in the plot tag as an integer (or empty)
   */
  private Void parsePlot() {
    supportedElements.add("plot");

    Element element = getSingleElement(root, "plot");
    if (element != null) {
      plot = element.wholeText();
    }

    return null;
  }

  /**
   * the outline usually comes in the outline tag as an integer (or empty)
   */
  private Void parseOutline() {
    supportedElements.add("outline");

    Element element = getSingleElement(root, "outline");
    if (element != null) {
      outline = element.wholeText();
    }

    return null;
  }

  /**
   * the tagline usually comes in the tagline tag as an integer (or empty)
   */
  private Void parseTagline() {
    supportedElements.add("tagline");

    Element element = getSingleElement(root, "tagline");
    if (element != null) {
      tagline = element.wholeText();
    }

    return null;
  }

  /**
   * the runtime usually comes in the runtime tag as an integer
   */
  private Void parseRuntime() {
    supportedElements.add("runtime");

    Element element = getSingleElement(root, "runtime");
    if (element != null) {
      try {
        runtime = MetadataUtil.parseInt(element.ownText());
      }
      catch (Exception ignored) {
        // just ignore
      }
    }

    return null;
  }

  /**
   * posters can come in several different forms<br />
   * - kodi usually puts it into thumb tags (multiple; in newer versions with an aspect attribute)<br />
   * - mediaportal puts it also in thumb tags (single)
   */
  private Void parsePosters() {
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

    return null;
  }

  /**
   * fanarts can come in several different forms<br />
   * - xbmc had it in one single fanart tag (no nested tags)<br />
   * - kodi usually puts it into a fanart tag (in newer versions a nested thumb tag)<br />
   * - mediaportal puts it also into a fanart tag (with nested thumb tags)
   */
  private Void parseFanarts() {
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

    return null;
  }

  /**
   * certification will come in the certification or mpaa tag<br />
   * - kodi has both tags filled, but certification has a much more clear format<br />
   * - mediaportal has only mpaa filled
   */
  private Void parseCertification() {
    supportedElements.add("certification");
    supportedElements.add("mpaa");

    Element element = getSingleElement(root, "certification");
    if (element == null || StringUtils.isBlank(element.ownText())) {
      element = getSingleElement(root, "mpaa");
    }
    if (element != null) {
      certification = MovieHelpers.parseCertificationStringForMovieSetupCountry(element.ownText());
    }

    return null;
  }

  /**
   * ids can be stored either in the<br />
   * - id tag (imdbID) or<br />
   * - imdb tag (imdbId) or<br />
   * - tmdbid tag (tmdb Id> or<br />
   * - uniqueid tag (new kodi style multiple ids) or<br />
   * - in a special nested tag (tmm store)
   */
  private Void parseIds() {
    supportedElements.add("id");
    supportedElements.add("imdb");
    supportedElements.add("tmdbid");
    supportedElements.add("ids");
    supportedElements.add("tmdbcollectionid"); // add the lowercase variant to supported elements, since we have an LC contains check
    supportedElements.add("tmdbCollectionId"); // but write the camelCase
    supportedElements.add("uniqueid");

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
        ids.put(MediaMetadata.TMDB, MetadataUtil.parseInt(element.ownText()));
      }
      catch (NumberFormatException ignored) {
        // just ignore
      }
    }

    // uniqueid tag
    Elements elements = root.select(root.tagName() + " > uniqueid");
    for (Element id : elements) {
      try {
        String key = id.attr("type");
        String value = id.ownText();
        if (StringUtils.isNoneBlank(key, value)) {
          // check whether the id is an integer
          try {
            ids.put(key, MetadataUtil.parseInt(value));
          }
          catch (Exception e) {
            // store as string
            ids.put(key, value);
          }
        }
      }
      catch (Exception ignored) {
        // just ignore
      }
    }

    // iterate over our internal id store (old JAXB style)
    element = getSingleElement(root, "ids");
    if (element != null) {
      Elements children = element.select(element.tagName() + " > entry");
      for (Element entry : children) {
        Element key = getSingleElement(entry, "key");
        Element value = getSingleElement(entry, "value");

        if (key == null || value == null) {
          continue;
        }

        if (StringUtils.isNoneBlank(key.ownText(), value.ownText())) {
          // check whether the id is an integer
          try {
            ids.put(key.ownText(), MetadataUtil.parseInt(value.ownText()));
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
            ids.put(entry.tagName(), MetadataUtil.parseInt(entry.ownText()));
          }
          catch (Exception e) {
            // store as string
            ids.put(entry.tagName(), entry.ownText());
          }
        }
      }
    }

    // the tmdb collection id
    element = getSingleElement(root, "tmdbCollectionId");
    if (element != null) {
      try {
        ids.put(MediaMetadata.TMDB_SET, MetadataUtil.parseInt(element.ownText()));
      }
      catch (NumberFormatException ignored) {
        // just ignore
      }
    }

    return null;
  }

  /**
   * countries come in two different flavors<br />
   * - multiple <country></country> tags (new style)<br />
   * - one <country></country> tag with multiple comma separated values
   */
  private Void parseCountry() {
    supportedElements.add("country");

    Elements elements = root.select(root.tagName() + " > country");
    // if there is exactly one country tag, split the countries at the comma
    if (elements.size() == 1) {
      try {
        countries.addAll(split(elements.get(0).ownText()));
      }
      catch (Exception ignored) {
        // nothing to do here
      }
    }
    else {
      for (Element element : elements) {
        if (StringUtils.isNotBlank(element.ownText())) {
          countries.add(element.ownText());
        }
      }
    }
    return null;
  }

  /**
   * the release date is usually in the premiered tag
   */
  private Void parseReleaseDate() {
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

    return null;
  }

  /**
   * parse the watched flag (watched tag) and playcount (playcount tag) together
   */
  private Void parseWatchedAndPlaycount() {
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
        playcount = MetadataUtil.parseInt(element.ownText());
        if (playcount > 0 && !watched) {
          watched = true;
        }
      }
      catch (Exception ignored) {
      }
    }

    return null;
  }

  /**
   * parse the genres tags<br />
   * - kodi has multiple genre tags<br />
   * - mediaportal as a nested genres tag
   */
  private Void parseGenres() {
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
          // old style - single tag with delimiter
          for (String sp : split(genre.ownText())) {
            genres.add(MediaGenres.getGenre(sp.trim()));
          }
        }
      }
    }

    return null;
  }

  /**
   * studios come in two different flavors<br />
   * - kodi has multiple studio tags<br />
   * - mediaportal has all studios (comma separated) in one studio tag
   */
  private Void parseStudios() {
    supportedElements.add("studio");

    Elements elements = root.select(root.tagName() + " > studio");
    // if there is exactly one studio tag, split the studios at the comma
    if (elements.size() == 1) {
      try {
        studios.addAll(split(elements.get(0).ownText()));
      }
      catch (Exception ignored) {
        // just ignore
      }
    }
    else {
      for (Element element : elements) {
        if (StringUtils.isNotBlank(element.ownText())) {
          studios.add(element.ownText());
        }
      }
    }

    return null;
  }

  /**
   * credits come in two different flavors<br />
   * - kodi has multiple credits tags<br />
   * - mediaportal has all credits (comma separated) in one credits tag
   */
  private Void parseCredits() {
    supportedElements.add("credits");

    Elements elements = root.select(root.tagName() + " > credits");
    // if there is exactly one credits tag, split the credits at the comma
    if (elements.size() == 1) {
      try {
        for (String credit : split(elements.get(0).ownText())) {
          Person person = new Person();
          person.name = credit;
          credits.add(person);
        }
      }
      catch (Exception ignored) {
        // just ignore
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

    return null;
  }

  /**
   * directors come in two different flavors<br />
   * - kodi has multiple director tags<br />
   * - mediaportal has all directors (comma separated) in one director tag
   */
  private Void parseDirectors() {
    supportedElements.add("director");

    Elements elements = root.select(root.tagName() + " > director");
    // if there is exactly one director tag, split the directors at the comma
    if (elements.size() == 1) {
      try {
        for (String director : split(elements.get(0).ownText())) {
          Person person = new Person();
          person.name = director;
          directors.add(person);
        }
      }
      catch (Exception ignored) {
        // just ignore
      }
    }
    else {
      for (Element element : elements) {
        if (StringUtils.isNotBlank(element.ownText())) {
          Person person = new Person();
          person.name = element.ownText();
          directors.add(person);
        }
      }
    }

    return null;
  }

  /**
   * tags usually come in a tag tag
   */
  private Void parseTags() {
    supportedElements.add("tag");

    Elements elements = root.select(root.tagName() + " > tag");
    for (Element element : elements) {
      if (StringUtils.isNotBlank(element.ownText())) {
        tags.add(element.ownText());
      }
    }

    return null;
  }

  /**
   * actors usually come as multiple actor tags in the root with three child tags:<br />
   * - name<br />
   * - role<br />
   * - thumb
   */
  private Void parseActors() {
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

          case "profile":
            actor.profile = child.ownText();
            break;

          default:
            break;
        }
      }
      if (StringUtils.isNotBlank(actor.name)) {
        actors.add(actor);
      }
    }

    return null;
  }

  /**
   * producers usually come as multiple producer tags in the root with three child tags:<br />
   * - name<br />
   * - role<br />
   * - thumb
   */
  private Void parseProducers() {
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

          default:
            break;
        }
      }
      if (StringUtils.isNotBlank(producer.name)) {
        producers.add(producer);
      }
    }

    return null;
  }

  /**
   * parse file information.
   */
  private Void parseFileinfo() {
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

            default:
              break;
          }
        }
      }
    }

    return null;
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
            // just ignore
          }
          break;

        case "width":
          try {
            video.width = MetadataUtil.parseInt(child.ownText());
          }
          catch (NumberFormatException ignored) {
            // just ignore
          }
          break;

        case "height":
          try {
            video.height = MetadataUtil.parseInt(child.ownText());
          }
          catch (NumberFormatException ignored) {
            // just ignore
          }
          break;

        case "durationinseconds":
          try {
            video.durationinseconds = MetadataUtil.parseInt(child.ownText());
          }
          catch (NumberFormatException ignored) {
            // just ignore
          }
          break;

        case "stereomode":
          video.stereomode = child.ownText();
          break;

        default:
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
            audio.channels = MetadataUtil.parseInt(child.ownText());
          }
          catch (NumberFormatException ignored) {
            // just ignore
          }
          break;

        default:
          break;
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

        default:
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
  private Void parseLanguages() {
    supportedElements.add("languages");
    supportedElements.add("language");

    Element element = getSingleElement(root, "languages");
    if (element == null) {
      element = getSingleElement(root, "language");
    }
    if (element != null) {
      languages = element.ownText();
    }

    // if the languages are in MediaPortal style, parse them and prepare them for tmm
    if (StringUtils.isNotBlank(languages)) {
      List<String> languages = new ArrayList<>();
      for (String langu : split(this.languages)) {
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

    return null;
  }

  /**
   * the media source is usually in the source tag
   */
  private Void parseSource() {
    supportedElements.add("source");

    Element element = getSingleElement(root, "source");
    if (element != null) {
      try {
        source = MediaSource.getMediaSource(element.ownText());
      }
      catch (Exception ignored) {
        // just ignore
      }
    }

    return null;
  }

  /**
   * the edition is usually in the edition tag
   */
  private Void parseEdition() {
    supportedElements.add("edition");

    Element element = getSingleElement(root, "edition");
    if (element != null) {
      try {
        edition = MovieEdition.getMovieEditionFromString(element.ownText());
      }
      catch (Exception ignored) {
        // just ignore
      }
    }

    return null;
  }

  /**
   * the original filename is usually in the original_filename tag
   */
  private Void parseOriginalFilename() {
    supportedElements.add("original_filename");

    Element element = getSingleElement(root, "original_filename");
    if (element != null) {
      originalFilename = element.ownText();
    }
    return null;
  }

  /**
   * the user note is usually in the user_note tag
   */
  private Void parseUserNote() {
    supportedElements.add("user_note");

    Element element = getSingleElement(root, "user_note");
    if (element != null) {
      userNote = element.ownText();
    }
    return null;
  }

  /**
   * a trailer is usually in the trailer tag
   */
  private Void parseTrailer() {
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
            // just ignore
          }
        }
      }

      // pure http link
      if (StringUtils.isNotBlank(element.ownText()) && element.ownText().matches("https?://.*")) {
        trailer = element.ownText();
      }
    }

    return null;
  }

  /**
   * find epbookmark for xbmc related nfos
   */
  private Void parseEpbookmark() {
    supportedElements.add("epbookmark");

    Element element = getSingleElement(root, "epbookmark");
    if (element != null) {
      epbookmark = element.ownText();
    }

    return null;
  }

  /**
   * find lastplayed for xbmc related nfos
   */
  private Void parseLastplayed() {
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

    return null;
  }

  /**
   * find status for xbmc related nfos
   */
  private Void parseStatus() {
    supportedElements.add("status");

    Element element = getSingleElement(root, "status");
    if (element != null) {
      status = element.ownText();
    }

    return null;
  }

  /**
   * find code for xbmc related nfos
   */
  private Void parseCode() {
    supportedElements.add("code");

    Element element = getSingleElement(root, "code");
    if (element != null) {
      code = element.ownText();
    }

    return null;
  }

  /**
   * find dateadded for xbmc related nfos
   */
  private Void parseDateadded() {
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
        // just ignore
      }
    }

    return null;
  }

  /**
   * find and store all unsupported tags
   */
  private Void findUnsupportedElements() {
    // get all children of the root
    for (Element element : root.children()) {
      if (!supportedElements.contains(element.tagName().toLowerCase(Locale.ROOT))) {
        String elementText = element.toString().replaceAll(">\\r?\\n\\s*<", "><");
        unsupportedElements.add(elementText);
      }
    }

    return null;
  }

  /**
   * splits the given String into substrings by all known/supported separators
   * 
   * @param source
   *          the source String to split
   * @return an array with all parts
   */
  public static List<String> split(String source) {
    List<String> result = new ArrayList<>();

    for (String string : source.split("\\s*[;,\\/|]\\s*")) {
      if (StringUtils.isNotBlank(string)) {
        result.add(string);
      }
    }

    return result;
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

    for (Map.Entry<String, Rating> entry : ratings.entrySet()) {
      Rating r = entry.getValue();
      movie.setRating(new MediaRating(r.id, r.rating, r.votes, r.maxValue));
    }

    // year is initially -1, only take parsed values which are higher than -1
    if (year > -1) {
      movie.setYear(year);
    }

    movie.setTop250(top250);
    movie.setReleaseDate(releaseDate);
    if (dateadded != null) {
      // set when in NFO, else use constructor date
      movie.setDateAdded(dateadded);
    }
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

    String country = StringUtils.join(countries, "/");
    if (country == null) {
      movie.setCountry("");
    }
    else {
      movie.setCountry(country);
    }
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

      // movie set id
      int tmdbSetId = 0;
      if (ids.get("tmdbSet") != null) {
        try {
          tmdbSetId = Integer.parseInt(ids.get("tmdbSet").toString());
        }
        catch (Exception ignored) {
          // just ignore
        }
      }

      MovieSet movieSet = movieList.getMovieSet(set.name, tmdbSetId);

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
      movie.addActor(morphPerson(ACTOR, actor));
    }

    for (Person producer : producers) {
      movie.addProducer(morphPerson(PRODUCER, producer));
    }

    for (Person director : directors) {
      if (StringUtils.isBlank(director.role)) {
        director.role = "Director";
      }
      movie.addDirector(morphPerson(DIRECTOR, director));
    }

    for (Person writer : credits) {
      if (StringUtils.isBlank(writer.role)) {
        writer.role = "Writer";
      }
      movie.addWriter(morphPerson(WRITER, writer));
    }

    for (MediaGenres genre : genres) {
      movie.addGenre(genre);
    }

    if (StringUtils.isNotEmpty(trailer)) {
      if (!trailer.startsWith("file")) {
        // only add new MT when not a local file
        MediaTrailer trailer = new MediaTrailer();
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

    movie.setOriginalFilename(originalFilename);
    movie.setNote(userNote);

    return movie;
  }

  private org.tinymediamanager.core.entities.Person morphPerson(org.tinymediamanager.core.entities.Person.Type type, Person nfoPerson) {
    org.tinymediamanager.core.entities.Person person = new org.tinymediamanager.core.entities.Person(type);

    person.setName(nfoPerson.name);
    person.setRole(nfoPerson.role);
    person.setThumbUrl(nfoPerson.thumb);
    person.setProfileUrl(nfoPerson.profile);

    return person;
  }

  /*
   * entity classes
   */
  public static class Set {
    public String name     = "";
    public String overview = "";
  }

  public static class Rating {
    public String id       = "";
    public float  rating   = 0;
    public int    votes    = 0;
    public int    maxValue = 10;
  }

  public static class Person {
    public String name    = "";
    public String role    = "";
    public String thumb   = "";
    public String profile = "";
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
