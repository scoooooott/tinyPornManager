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

package org.tinymediamanager.core.tvshow.connector;

import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_BANNER;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_POSTER;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_THUMB;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import org.tinymediamanager.core.MediaAiredStatus;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.tvshow.TvShowHelpers;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.util.MetadataUtil;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * The class TvShowNfoParser is used to parse all types of NFO/XML files
 * 
 * @author Manuel Laggner
 */
public class TvShowNfoParser {
  private static final Logger       LOGGER              = LoggerFactory.getLogger(TvShowNfoParser.class);
  /** ignore the following tags since they originally do not belong to a TV show NFO */
  private static final List<String> IGNORE              = Arrays.asList("set", "epbookmark", "resume");

  private Element                   root;
  private final List<String>        supportedElements   = new ArrayList<>();

  public String                     title               = "";
  public String                     originalTitle       = "";
  public String                     sortTitle           = "";
  public String                     showTitle           = "";
  public int                        year                = -1;
  public String                     plot                = "";
  public int                        runtime             = 0;
  public MediaCertification         certification       = MediaCertification.UNKNOWN;
  public Date                       releaseDate         = null;
  public MediaAiredStatus           status              = MediaAiredStatus.UNKNOWN;
  public boolean                    watched             = false;
  public int                        playcount           = 0;
  public String                     userNote            = "";

  public Map<String, Object>        ids                 = new HashMap<>();
  public Map<String, Rating>        ratings             = new HashMap<>();

  public List<String>               posters             = new ArrayList<>();
  public Map<Integer, String>       seasonTitles        = new HashMap<>();
  public Map<Integer, List<String>> seasonPosters       = new HashMap<>();
  public Map<Integer, List<String>> seasonBanners       = new HashMap<>();
  public Map<Integer, List<String>> seasonThumbs        = new HashMap<>();
  public List<String>               banners             = new ArrayList<>();
  public List<String>               fanarts             = new ArrayList<>();
  public List<MediaGenres>          genres              = new ArrayList<>();
  public List<String>               studios             = new ArrayList<>();
  public List<String>               tags                = new ArrayList<>();
  public List<Person>               actors              = new ArrayList<>();

  public List<String>               unsupportedElements = new ArrayList<>();

  /* some xbmc related tags we parse, but do not use internally */
  public String                     outline             = "";
  public String                     tagline             = "";
  public int                        top250              = 0;
  public String                     trailer             = "";
  public Date                       lastplayed          = null;
  public String                     code                = "";
  public Date                       dateadded           = null;
  public String                     episodeguide        = "";

  /**
   * create a new instance by parsing the document
   *
   * @param document
   *          the document returned by JSOUP.parse()
   */
  private TvShowNfoParser(Document document) {
    // first check if there is a valid root object
    Elements elements = document.select("tvshow");
    if (elements.isEmpty()) {
      return;
    }

    document.outputSettings().prettyPrint(false);

    this.root = elements.get(0);

    // parse all supported fields
    parseTag(TvShowNfoParser::parseTitle);
    parseTag(TvShowNfoParser::parseOriginalTitle);
    parseTag(TvShowNfoParser::parseSortTitle);
    parseTag(TvShowNfoParser::parseShowTitle);
    parseTag(TvShowNfoParser::parseRatingAndVotes);
    parseTag(TvShowNfoParser::parseYear);
    parseTag(TvShowNfoParser::parsePlot);
    parseTag(TvShowNfoParser::parseOutline);
    parseTag(TvShowNfoParser::parseRuntime);
    parseTag(TvShowNfoParser::parseStatus);
    parseTag(TvShowNfoParser::parsePosters);
    parseTag(TvShowNfoParser::parseBanners);
    parseTag(TvShowNfoParser::parseFanarts);
    parseTag(TvShowNfoParser::parseSeasonArtwork);
    parseTag(TvShowNfoParser::parseSeasonNames);
    parseTag(TvShowNfoParser::parseCertification);
    parseTag(TvShowNfoParser::parseIds);
    parseTag(TvShowNfoParser::parseReleaseDate);
    parseTag(TvShowNfoParser::parseWatchedAndPlaycount);
    parseTag(TvShowNfoParser::parseGenres);
    parseTag(TvShowNfoParser::parseStudios);
    parseTag(TvShowNfoParser::parseTags);
    parseTag(TvShowNfoParser::parseActors);
    parseTag(TvShowNfoParser::parseTrailer);

    parseTag(TvShowNfoParser::parseTagline);
    parseTag(TvShowNfoParser::parseTop250);
    parseTag(TvShowNfoParser::parseLastplayed);
    parseTag(TvShowNfoParser::parseCode);
    parseTag(TvShowNfoParser::parseDateadded);
    parseTag(TvShowNfoParser::parseEpisodeguide);
    parseTag(TvShowNfoParser::parseUserNote);

    // MUST BE THE LAST ONE!
    parseTag(TvShowNfoParser::findUnsupportedElements);
  }

  /**
   * parse the tag in a save way
   *
   * @param function
   *          the parsing function to be executed
   */
  private Void parseTag(Function<TvShowNfoParser, Void> function) {
    try {
      function.apply(this);
    }
    catch (Exception e) {
      LOGGER.warn("problem parsing tag (line {}): {}", e.getStackTrace()[0].getLineNumber(), e.getMessage());
    }

    return null;
  }

  /**
   * parse the given file
   * 
   * @param path
   *          the path to the NFO/XML to be parsed
   * @return a new instance of the parser class
   * @throws IOException
   *           any exception if parsing fails
   */
  public static TvShowNfoParser parseNfo(Path path) throws IOException {
    return new TvShowNfoParser(Jsoup.parse(new FileInputStream(path.toFile()), "UTF-8", "", Parser.xmlParser()));
  }

  /**
   * parse the xml content
   *
   * @param content
   *          the content of the NFO/XML to be parsed
   * @return a new instance of the parser class
   */
  public static TvShowNfoParser parseNfo(String content) {
    return new TvShowNfoParser(Jsoup.parse(content, "", Parser.xmlParser()));
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
      originalTitle = element.ownText();
    }

    return null;
  }

  /**
   * the sort title usually comes in the sorttitle tag
   */
  private Void parseSortTitle() {
    supportedElements.add("sorttitle");

    Element element = getSingleElement(root, "sorttitle");
    if (element != null) {
      sortTitle = element.ownText();
    }

    return null;
  }

  /**
   * the show title usually comes in the showTitle tag
   */
  private Void parseShowTitle() {
    supportedElements.add("showtitle");

    Element element = getSingleElement(root, "showtitle");
    if (element != null) {
      showTitle = element.ownText();
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
      r.id = Rating.DEFAULT;
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
          r.maxValue = MetadataUtil.parseInt(ratingChild.attr("max"));
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
                r.votes = MetadataUtil.parseInt(child.ownText());
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
      }
    }

    return null;
  }

  /**
   * parse status in <status>xxx</status>
   */
  private Void parseStatus() {
    supportedElements.add("status");

    Element element = getSingleElement(root, "status");
    if (element != null) {
      status = MediaAiredStatus.findAiredStatus(element.ownText());
    }

    return null;
  }

  /**
   * posters are usually inside <thumb>xxx</thumb> tag with an aspect of "poster"<br />
   * but there are also season poster in this tag
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

        String posterUrl = element.ownText();
        if (StringUtils.isBlank(posterUrl) || !posterUrl.matches("https?://.*")) {
          continue;
        }
        if (element.hasAttr("type") && element.attr("type").equals("season")) {
          // season poster
          // parse out season number
          try {
            Integer season = Integer.parseInt(element.attr("season"));
            List<String> seasonPosterList = seasonPosters.get(season);
            if (seasonPosterList == null) {
              seasonPosterList = new ArrayList<>();
              seasonPosters.put(season, seasonPosterList);
            }
            if (!seasonPosterList.contains(posterUrl)) {
              seasonPosterList.add(posterUrl);
            }
          }
          catch (Exception ignored) {
          }
        }
        else {
          // tv show poster
          if (!posters.contains(posterUrl)) {
            posters.add(posterUrl);
          }
        }
      }
    }

    return null;
  }

  /**
   * banners are usually inside <thumb>xxx</thumb> tag with an aspect of "banner"
   */
  private Void parseBanners() {
    // supportedElements.add("thumb"); //already registered with posters

    // get all thumb elements
    Elements thumbs = root.select(root.tagName() + " > thumb");
    if (!thumbs.isEmpty()) {
      for (Element element : thumbs) {
        // if there is an aspect attribute, it has to be poster
        if (element.hasAttr("aspect") && !element.attr("aspect").equals("banner")) {
          continue;
        }
        if (StringUtils.isNotBlank(element.ownText()) && element.ownText().matches("https?://.*")) {
          banners.add(element.ownText());
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
      String prefix = fanart.attr("url");
      Elements thumbs = fanart.select(fanart.tagName() + " > thumb");
      // thumb children available
      if (!thumbs.isEmpty()) {
        for (Element element : thumbs) {
          if (StringUtils.isNotBlank(element.ownText()) && element.ownText().matches("https?://.*")) {
            fanarts.add(element.ownText());
          }
          else if (StringUtils.isNotBlank(element.ownText()) && prefix.matches("https?://.*")) {
            fanarts.add(prefix + element.ownText());
          }
        }
      }
      // no children - get own text
      else if (StringUtils.isNotBlank(fanart.ownText()) && fanart.ownText().matches("https?://.*")) {
        fanarts.add(fanart.ownText());
      }
      else if (StringUtils.isNotBlank(fanart.ownText()) && prefix.matches("https?://.*")) {
        fanarts.add(prefix + fanart.ownText());
      }
    }

    return null;
  }

  /**
   * posters are usually inside <thumb>xxx</thumb> tag with an type of "season"<br />
   * but there are also season poster in this tag
   */
  private Void parseSeasonArtwork() {
    // supportedElements.add("thumb"); //already registered with posters

    // get all thumb elements
    Elements thumbs = root.select(root.tagName() + " > thumb");
    for (Element element : thumbs) {
      // there has to be the type of season
      if (!element.hasAttr("aspect") || !element.hasAttr("type") || !element.attr("type").equals("season")) {
        continue;
      }

      String artworkUrl = element.ownText();
      if (StringUtils.isBlank(artworkUrl) || !artworkUrl.matches("https?://.*")) {
        continue;
      }

      // parse out season number
      Integer season = null;
      try {
        season = Integer.parseInt(element.attr("season"));
      }
      catch (Exception ignored) {
      }

      if (season == null) {
        continue;
      }

      switch (element.attr("aspect")) {
        case "poster":
          List<String> seasonPosterList = seasonPosters.get(season);
          if (seasonPosterList == null) {
            seasonPosterList = new ArrayList<>();
            seasonPosters.put(season, seasonPosterList);
          }
          if (!seasonPosterList.contains(artworkUrl)) {
            seasonPosterList.add(artworkUrl);
          }
          break;

        case "banner":
          List<String> seasonBannerList = seasonBanners.get(season);
          if (seasonBannerList == null) {
            seasonBannerList = new ArrayList<>();
            seasonBanners.put(season, seasonBannerList);
          }
          if (!seasonBannerList.contains(artworkUrl)) {
            seasonBannerList.add(artworkUrl);
          }
          break;

        case "thumb":
          List<String> seasonThumbList = seasonThumbs.get(season);
          if (seasonThumbList == null) {
            seasonThumbList = new ArrayList<>();
            seasonThumbs.put(season, seasonThumbList);
          }
          if (!seasonThumbList.contains(artworkUrl)) {
            seasonThumbList.add(artworkUrl);
          }
          break;

        default:
          continue;
      }
    }

    return null;
  }

  /**
   * names season come in the form <namedseason number="1">title</namedseason>
   */
  private Void parseSeasonNames() {
    supportedElements.add("namedseason");

    // get all thumb elements
    Elements namedseasons = root.select(root.tagName() + " > namedseason");

    for (Element namedseason : namedseasons) {
      try {
        int season = MetadataUtil.parseInt(namedseason.attr("number"));
        if (StringUtils.isNotBlank(namedseason.ownText())) {
          seasonTitles.put(season, namedseason.ownText());
        }
      }
      catch (Exception e) {
        LOGGER.trace("could not parse named season: {}", e.getMessage());
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
      certification = TvShowHelpers.parseCertificationStringForTvShowSetupCountry(element.ownText());
    }

    return null;
  }

  /**
   * ids can be stored either in the<br />
   * - id tag (imdbID) or<br />
   * - imdb tag (imdbId) or<br />
   * - tmdbId tag (tmdb Id> or<br />
   * - in a special nested tag (tmm store)
   */
  private Void parseIds() {
    supportedElements.add("id");
    supportedElements.add("imdb");
    supportedElements.add("imdbid");
    supportedElements.add("tmdbid");
    supportedElements.add("ids");
    supportedElements.add("uniqueid");

    // id tag
    Element element = getSingleElement(root, "id");
    if (element != null) {
      try {
        ids.put(MediaMetadata.TVDB, MetadataUtil.parseInt(element.ownText()));
      }
      catch (NumberFormatException ignored) {
      }
    }

    // uniqueid tag
    Elements elements = root.select(root.tagName() + " > uniqueid");
    for (Element id : elements) {
      try {
        String key = id.attr("type");
        String value = id.ownText();
        if (StringUtils.isNoneBlank(key, value)) {
          // special handling for TVDB: <uniqueid type="unknown"..
          if ("unknown".equals(key) && ids.get(MediaMetadata.TVDB) == null) {
            try {
              ids.put(MediaMetadata.TVDB, MetadataUtil.parseInt(value));
            }
            catch (Exception e) {
              // store as string
              ids.put(key, value);
            }
          }
          else {
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
      }
      catch (Exception ignored) {
      }
    }

    // imdb id and pattern check
    element = getSingleElement(root, "imdb");
    if (element != null && MetadataUtil.isValidImdbId(element.ownText())) {
      ids.put(MediaMetadata.IMDB, element.ownText());
    }
    element = getSingleElement(root, "imdbid");
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
        element = getSingleElement(root, "playcount");
        if (element != null) {
          playcount = MetadataUtil.parseInt(element.ownText());
        }
      }
      catch (Exception ignored) {
        // nothing to be catched here
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
      // direct/multiple genre tags in show root
      elements = root.select(root.tagName() + " > genre");
    }

    if (elements != null && !elements.isEmpty()) {
      for (Element genre : elements) {
        if (StringUtils.isNotBlank(genre.ownText())) {
          // old style - single tag with delimiter
          String[] split = genre.ownText().split("/");
          for (String sp : split) {
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
        }
      }
      if (StringUtils.isNotBlank(actor.name)) {
        actors.add(actor);
      }
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
      }
    }

    return null;
  }

  /**
   * find episodeguide for xbmc related nfos
   */
  private Void parseEpisodeguide() {
    supportedElements.add("episodeguide");

    Element element = getSingleElement(root, "episodeguide");
    if (element != null) {
      episodeguide = element.children().toString();
    }

    return null;
  }

  /**
   * find and store all unsupported tags
   */
  private Void findUnsupportedElements() {
    // get all children of the root
    for (Element element : root.children()) {
      if (!IGNORE.contains(element.tagName()) && !supportedElements.contains(element.tagName())) {
        String elementText = element.toString().replaceAll(">\\r?\\n\\s*<", "><");
        unsupportedElements.add(elementText);
      }
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
   * morph this instance to a TvShow object
   *
   * @return the TvShow object
   */
  public TvShow toTvShow() {
    TvShow show = new TvShow();
    show.setTitle(title);
    show.setOriginalTitle(originalTitle);
    show.setSortTitle(sortTitle);

    for (Map.Entry<String, TvShowNfoParser.Rating> entry : ratings.entrySet()) {
      TvShowNfoParser.Rating r = entry.getValue();
      show.setRating(new MediaRating(r.id, r.rating, r.votes, r.maxValue));
    }

    // year is initially -1, only take parsed values which are higher than -1
    if (year > -1) {
      show.setYear(year);
    }

    show.setFirstAired(releaseDate);
    if (dateadded != null) {
      // set when in NFO, else use constructor date
      show.setDateAdded(dateadded);
    }
    show.setPlot(plot);
    show.setRuntime(runtime);

    if (!posters.isEmpty()) {
      show.setArtworkUrl(posters.get(0), MediaFileType.POSTER);
    }

    for (Map.Entry<Integer, String> entry : seasonTitles.entrySet()) {
      show.addSeasonTitle(entry.getKey(), entry.getValue());
    }

    for (Map.Entry<Integer, List<String>> entry : seasonPosters.entrySet()) {
      if (!entry.getValue().isEmpty()) {
        show.setSeasonArtworkUrl(entry.getKey(), entry.getValue().get(0), SEASON_POSTER);
      }
    }

    for (Map.Entry<Integer, List<String>> entry : seasonBanners.entrySet()) {
      if (!entry.getValue().isEmpty()) {
        show.setSeasonArtworkUrl(entry.getKey(), entry.getValue().get(0), SEASON_BANNER);
      }
    }

    for (Map.Entry<Integer, List<String>> entry : seasonThumbs.entrySet()) {
      if (!entry.getValue().isEmpty()) {
        show.setSeasonArtworkUrl(entry.getKey(), entry.getValue().get(0), SEASON_THUMB);
      }
    }

    if (!banners.isEmpty()) {
      show.setArtworkUrl(banners.get(0), MediaFileType.BANNER);
    }

    if (!fanarts.isEmpty()) {
      show.setArtworkUrl(fanarts.get(0), MediaFileType.FANART);
    }
    for (Map.Entry<String, Object> entry : ids.entrySet()) {
      show.setId(entry.getKey(), entry.getValue());
    }

    String studio = StringUtils.join(studios, " / ");
    if (studio == null) {
      show.setProductionCompany("");
    }
    else {
      show.setProductionCompany(studio);
    }

    show.setCertification(certification);
    show.setStatus(status);

    for (Person actor : actors) {
      show.addActor(morphPerson(org.tinymediamanager.core.entities.Person.Type.ACTOR, actor));
    }

    for (MediaGenres genre : genres) {
      show.addGenre(genre);
    }

    for (String tag : tags) {
      show.addToTags(tag);
    }

    show.setNote(userNote);

    return show;
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
  public static class Rating {
    public static final String DEFAULT  = "default";
    public static final String USER     = "user";

    public String              id       = "";
    public float               rating   = 0;
    public int                 votes    = 0;
    public int                 maxValue = 10;
  }

  public static class Person {
    public String name    = "";
    public String role    = "";
    public String thumb   = "";
    public String profile = "";
  }
}
