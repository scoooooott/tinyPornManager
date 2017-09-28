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
package org.tinymediamanager.scraper.ofdb;

import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.UnsupportedMediaTypeException;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.entities.MediaRating;
import org.tinymediamanager.scraper.entities.MediaTrailer;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.http.Url;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;
import org.tinymediamanager.scraper.mediaprovider.IMovieTrailerProvider;
import org.tinymediamanager.scraper.util.MetadataUtil;
import org.tinymediamanager.scraper.util.StrgUtils;

import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * The Class OfdbMetadataProvider. A meta data provider for the site ofdb.de
 * 
 * @author Myron Boyle (myron0815@gmx.net)
 */
@PluginImplementation
public class OfdbMetadataProvider implements IMovieMetadataProvider, IMovieTrailerProvider {
  private static final Logger      LOGGER       = LoggerFactory.getLogger(OfdbMetadataProvider.class);
  private static final String      BASE_URL     = "http://www.ofdb.de";

  private static MediaProviderInfo providerInfo = createMediaProviderInfo();

  public OfdbMetadataProvider() {
  }

  private static MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo("ofdb", "Online Filmdatenbank (OFDb.de)",
        "<html><h3>Online Filmdatenbank (OFDb)</h3><br />A german movie database driven by the community.<br /><br />Available languages: DE</html>",
        OfdbMetadataProvider.class.getResource("/ofdb_de.png"));
    providerInfo.setVersion(OfdbMetadataProvider.class);
    return providerInfo;
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  /*
   * <meta property="og:title" content="Bourne Vermaächtnis, Das (2012)" /> <meta property="og:type" content="movie" /> <meta property="og:url"
   * content="http://www.ofdb.de/film/226745,Das-Bourne-Vermächtnis" /> <meta property="og:image" content="http://img.ofdb.de/film/226/226745.jpg" />
   * <meta property="og:site_name" content="OFDb" /> <meta property="fb:app_id" content="198140443538429" /> <script
   * src="http://www.ofdb.de/jscripts/vn/immer_oben.js" type="text/javascript"></script>
   */
  @Override
  public MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getMetadata() " + options.toString());

    if (options.getType() != MediaType.MOVIE) {
      throw new UnsupportedMediaTypeException(options.getType());
    }

    // we have 3 entry points here
    // a) getMetadata has been called with an ofdbId
    // b) getMetadata has been called with an imdbId
    // c) getMetadata has been called from a previous search

    String detailUrl = "";

    // case a)
    String id = "";
    if (options.getResult() != null) {
      id = options.getResult().getId();
    }
    if (StringUtils.isBlank(id)) {
      id = options.getIdAsString(getProviderInfo().getId());
    }
    if (StringUtils.isNotBlank(id)) {
      detailUrl = "http://www.ofdb.de/view.php?page=film&fid=" + id;
    }

    // case b)
    if (options.getResult() == null && StringUtils.isNotBlank(options.getIdAsString(MediaMetadata.IMDB))) {
      MediaSearchOptions searchOptions = new MediaSearchOptions(MediaType.MOVIE);
      searchOptions.setImdbId(options.getIdAsString(MediaMetadata.IMDB));
      try {
        List<MediaSearchResult> results = search(searchOptions);
        if (results != null && !results.isEmpty()) {
          options.setResult(results.get(0));
          detailUrl = options.getResult().getUrl();
        }
      }
      catch (Exception e) {
        LOGGER.warn("failed IMDB search: " + e.getMessage());
      }
    }

    // case c)
    if (options.getResult() != null) {
      detailUrl = options.getResult().getUrl();
    }

    // we can only work further if we got a search result on ofdb.de
    if (StringUtils.isBlank(detailUrl)) {
      throw new Exception("We did not get any useful movie url");
    }

    MediaMetadata md = new MediaMetadata(providerInfo.getId());
    // generic Elements used all over
    Elements el = null;
    String ofdbId = StrgUtils.substr(detailUrl, "film\\/(\\d+),");
    if (StringUtils.isBlank(ofdbId)) {
      ofdbId = StrgUtils.substr(detailUrl, "fid=(\\d+)");
    }

    Url url;
    try {
      LOGGER.trace("get details page");
      url = new Url(detailUrl);
      InputStream in = url.getInputStream();
      Document doc = Jsoup.parse(in, "UTF-8", "");
      in.close();

      if (doc.getAllElements().size() < 10) {
        throw new Exception("meh - we did not receive a valid web page");
      }

      // parse details

      // IMDB ID "http://www.imdb.com/Title?1194173"
      el = doc.getElementsByAttributeValueContaining("href", "imdb.com");
      if (!el.isEmpty()) {
        md.setId(MediaMetadata.IMDB, "tt" + StrgUtils.substr(el.first().attr("href"), "\\?(\\d+)"));
      }

      // title / year
      // <meta property="og:title" content="Bourne Vermächtnis, Das (2012)" />
      el = doc.getElementsByAttributeValue("property", "og:title");
      if (!el.isEmpty()) {
        String[] ty = parseTitle(el.first().attr("content"));
        md.setTitle(StrgUtils.removeCommonSortableName(ty[0]));
        try {
          md.setYear(Integer.parseInt(ty[1]));
        }
        catch (Exception ignored) {
        }
      }
      // another year position
      if (md.getYear() == 0) {
        // <a href="view.php?page=blaettern&Kat=Jahr&Text=2012">2012</a>
        el = doc.getElementsByAttributeValueContaining("href", "Kat=Jahr");
        try {
          md.setYear(Integer.parseInt(el.first().text()));
        }
        catch (Exception ignored) {
        }
      }

      // original title (has to be searched with a regexp)
      // <tr valign="top">
      // <td nowrap=""><font class="Normal" face="Arial,Helvetica,sans-serif"
      // size="2">Originaltitel:</font></td>
      // <td>&nbsp;&nbsp;</td>
      // <td width="99%"><font class="Daten" face="Arial,Helvetica,sans-serif"
      // size="2"><b>Brave</b></font></td>
      // </tr>
      String originalTitle = StrgUtils.substr(doc.body().html(), "(?s)Originaltitel.*?<b>(.*?)</b>");
      if (!originalTitle.isEmpty()) {
        md.setOriginalTitle(StrgUtils.removeCommonSortableName(originalTitle));
      }

      // Genre: <a href="view.php?page=genre&Genre=Action">Action</a>
      el = doc.getElementsByAttributeValueContaining("href", "page=genre");
      for (Element g : el) {
        md.addGenre(getTmmGenre(g.text()));
      }

      // rating
      // <div itemtype="http://schema.org/AggregateRating" itemscope="" itemprop="aggregateRating">
      // Note: <span itemprop="ratingValue">8.74</span><meta itemprop="worstRating" content="1"><meta itemprop="bestRating" content="10">
      // &nbsp;•&nbsp;&nbsp;Stimmen: <span itemprop="ratingCount">2187</span>
      // &nbsp;•&nbsp;&nbsp;Platz: 19 &nbsp;•&nbsp;&nbsp;Ihre Note: --</div>
      try {
        MediaRating rating = new MediaRating("odfb");
        el = doc.getElementsByAttributeValue("itemprop", "ratingValue");
        if (!el.isEmpty()) {
          String r = el.text();
          if (!r.isEmpty()) {
            rating.setRating(Float.parseFloat(r));
            rating.setMaxValue(10);
          }
        }
        el = doc.getElementsByAttributeValue("itemprop", "ratingCount");
        if (!el.isEmpty()) {
          String r = el.text();
          if (!r.isEmpty()) {
            rating.setVoteCount(Integer.parseInt(r));
          }
        }
        md.addRating(rating);
      }
      catch (Exception e) {
        LOGGER.debug("could not parse rating");
      }

      // get PlotLink; open url and parse
      // <a href="plot/22523,31360,Die-Bourne-Identität"><b>[mehr]</b></a>
      LOGGER.trace("parse plot");
      el = doc.getElementsByAttributeValueMatching("href", "plot\\/\\d+,");
      if (!el.isEmpty()) {
        String plotUrl = BASE_URL + "/" + el.first().attr("href");
        try {
          url = new Url(plotUrl);
          in = url.getInputStream();
          Document plot = Jsoup.parse(in, "UTF-8", "");
          in.close();
          Elements block = plot.getElementsByClass("Blocksatz"); // first
          // Blocksatz
          // is plot
          String p = block.first().text(); // remove all html stuff
          p = p.substring(p.indexOf("Mal gelesen") + 12); // remove "header"
          md.setPlot(p);
        }
        catch (Exception e) {
          LOGGER.error("failed to get plot page: " + e.getMessage());
        }
      }

      // http://www.ofdb.de/view.php?page=film_detail&fid=226745
      LOGGER.debug("parse actor detail");
      String movieDetail = BASE_URL + "/view.php?page=film_detail&fid=" + ofdbId;
      doc = null;
      try {
        url = new Url(movieDetail);
        in = url.getInputStream();
        doc = Jsoup.parse(in, "UTF-8", "");
        in.close();
      }
      catch (Exception e) {
        LOGGER.error("failed to get detail page: " + e.getMessage());
      }

      if (doc != null) {
        parseCast(doc.getElementsContainingOwnText("Regie"), MediaCastMember.CastType.DIRECTOR, md);
        parseCast(doc.getElementsContainingOwnText("Darsteller"), MediaCastMember.CastType.ACTOR, md);
        parseCast(doc.getElementsContainingOwnText("Stimme/Sprecher"), MediaCastMember.CastType.ACTOR, md);
        parseCast(doc.getElementsContainingOwnText("Synchronstimme (deutsch)"), MediaCastMember.CastType.ACTOR, md);
        parseCast(doc.getElementsContainingOwnText("Drehbuchautor(in)"), MediaCastMember.CastType.WRITER, md);
        parseCast(doc.getElementsContainingOwnText("Produzent(in)"), MediaCastMember.CastType.PRODUCER, md);
      }
    }
    catch (Exception e) {
      LOGGER.error("Error parsing " + detailUrl);
      throw e;
    }

    return md;
  }

  // parse actors
  // find the header
  // go up until TR table row
  // get next TR for casts entries
  private void parseCast(Elements el, MediaCastMember.CastType type, MediaMetadata md) {
    if (el != null && !el.isEmpty()) {
      Element castEl = null;
      for (Element element : el) {
        if (!element.tagName().equals("option")) { // we get more, just do not take the optionbox
          castEl = element;
        }
      }
      if (castEl == null) {
        LOGGER.debug("meh, no " + type.name() + " found");
        return;
      }
      // walk up to table TR...
      while (!((castEl == null) || (castEl.tagName().equalsIgnoreCase("tr")))) {
        castEl = castEl.parent();
      }
      // ... and take the next table row ^^
      Element tr = castEl.nextElementSibling();

      if (tr != null) {
        for (Element a : tr.getElementsByAttributeValue("valign", "middle")) {
          String act = a.toString();
          String aname = StrgUtils.substr(act, "alt=\"(.*?)\"");
          if (!aname.isEmpty()) {
            MediaCastMember cm = new MediaCastMember();
            cm.setName(aname);
            String id = StrgUtils.substr(act, "id=(.*?)[^\"]\">");
            if (!id.isEmpty()) {
              cm.setId(id);
              // thumb
              // http://www.ofdb.de/thumbnail.php?cover=images%2Fperson%2F7%2F7689.jpg&size=6
              // fullsize ;) http://www.ofdb.de/images/person/7/7689.jpg
              try {
                String imgurl = URLDecoder.decode(StrgUtils.substr(act, "images%2Fperson%2F(.*?)&amp;size"), "UTF-8");
                if (!imgurl.isEmpty()) {
                  imgurl = BASE_URL + "/images/person/" + imgurl;
                }
                cm.setImageUrl(imgurl);
              }
              catch (Exception e) {
              }
            }
            String arole = StrgUtils.substr(act, "\\.\\.\\. (.*?)</font>").replaceAll("<[^>]*>", "");
            cm.setCharacter(arole);
            cm.setType(type);
            md.addCastMember(cm);
          }
        }
      }
    }
  }

  /*
   * Maps scraper Genres to internal TMM genres
   */
  private MediaGenres getTmmGenre(String genre) {
    MediaGenres g = null;
    if (genre.isEmpty()) {
      return g;
    }
    // @formatter:off
    else if (genre.equals("Abenteuer")) {
      g = MediaGenres.ADVENTURE;
    }
    else if (genre.equals("Action")) {
      g = MediaGenres.ACTION;
    }
    else if (genre.equals("Amateur")) {
      g = MediaGenres.INDIE;
    }
    else if (genre.equals("Animation")) {
      g = MediaGenres.ANIMATION;
    }
    else if (genre.equals("Anime")) {
      g = MediaGenres.ANIMATION;
    }
    else if (genre.equals("Biographie")) {
      g = MediaGenres.BIOGRAPHY;
    }
    else if (genre.equals("Dokumentation")) {
      g = MediaGenres.DOCUMENTARY;
    }
    else if (genre.equals("Drama")) {
      g = MediaGenres.DRAMA;
    }
    else if (genre.equals("Eastern")) {
      g = MediaGenres.EASTERN;
    }
    else if (genre.equals("Erotik")) {
      g = MediaGenres.EROTIC;
    }
    else if (genre.equals("Essayfilm")) {
      g = MediaGenres.INDIE;
    }
    else if (genre.equals("Experimentalfilm")) {
      g = MediaGenres.INDIE;
    }
    else if (genre.equals("Fantasy")) {
      g = MediaGenres.FANTASY;
    }
    else if (genre.equals("Grusel")) {
      g = MediaGenres.HORROR;
    }
    else if (genre.equals("Hardcore")) {
      g = MediaGenres.EROTIC;
    }
    else if (genre.equals("Heimatfilm")) {
      g = MediaGenres.TV_MOVIE;
    }
    else if (genre.equals("Historienfilm")) {
      g = MediaGenres.HISTORY;
    }
    else if (genre.equals("Horror")) {
      g = MediaGenres.HORROR;
    }
    else if (genre.equals("Kampfsport")) {
      g = MediaGenres.SPORT;
    }
    else if (genre.equals("Katastrophen")) {
      g = MediaGenres.DISASTER;
    }
    else if (genre.equals("Kinder-/Familienfilm")) {
      g = MediaGenres.FAMILY;
    }
    else if (genre.equals("Komödie")) {
      g = MediaGenres.COMEDY;
    }
    else if (genre.equals("Krieg")) {
      g = MediaGenres.WAR;
    }
    else if (genre.equals("Krimi")) {
      g = MediaGenres.CRIME;
    }
    else if (genre.equals("Kurzfilm")) {
      g = MediaGenres.SHORT;
    }
    else if (genre.equals("Liebe/Romantik")) {
      g = MediaGenres.ROMANCE;
    }
    else if (genre.equals("Mondo")) {
      g = MediaGenres.DOCUMENTARY;
    }
    else if (genre.equals("Musikfilm")) {
      g = MediaGenres.MUSIC;
    }
    else if (genre.equals("Mystery")) {
      g = MediaGenres.MYSTERY;
    }
    else if (genre.equals("Science-Fiction")) {
      g = MediaGenres.SCIENCE_FICTION;
    }
    else if (genre.equals("Serial")) {
      g = MediaGenres.SERIES;
    }
    else if (genre.equals("Sex")) {
      g = MediaGenres.EROTIC;
    }
    else if (genre.equals("Splatter")) {
      g = MediaGenres.HORROR;
    }
    else if (genre.equals("Sportfilm")) {
      g = MediaGenres.SPORT;
    }
    else if (genre.equals("Stummfilm")) {
      g = MediaGenres.SILENT_MOVIE;
    }
    else if (genre.equals("TV-Film")) {
      g = MediaGenres.TV_MOVIE;
    }
    else if (genre.equals("TV-Mini-Serie")) {
      g = MediaGenres.SERIES;
    }
    else if (genre.equals("TV-Pilotfilm")) {
      g = MediaGenres.TV_MOVIE;
    }
    else if (genre.equals("TV-Serie")) {
      g = MediaGenres.SERIES;
    }
    else if (genre.equals("Thriller")) {
      g = MediaGenres.THRILLER;
    }
    else if (genre.equals("Tierfilm")) {
      g = MediaGenres.ANIMAL;
    }
    else if (genre.equals("Webserie")) {
      g = MediaGenres.SERIES;
    }
    else if (genre.equals("Western")) {
      g = MediaGenres.WESTERN;
    }
    // @formatter:on
    if (g == null) {
      g = MediaGenres.getGenre(genre);
    }
    return g;
  }

  /*
   * Removes all weird characters from search as well some "stopwords" as der|die|das|the|a
   */
  private String cleanSearch(String q) {
    q = " " + q + " "; // easier regex
    // TODO: doppelte hintereinander funzen so nicht
    q = q.replaceAll("(?i)( a | the | der | die | das |\\(\\d+\\))", " ");
    q = q.replaceAll("[^A-Za-z0-9äöüÄÖÜ ]", " ");
    q = q.replaceAll("  ", "");
    return q.trim();
  }

  @Override
  public List<MediaSearchResult> search(MediaSearchOptions options) throws Exception {
    LOGGER.debug("search() " + options.toString());

    if (options.getMediaType() != MediaType.MOVIE) {
      throw new UnsupportedMediaTypeException(options.getMediaType());
    }

    List<MediaSearchResult> resultList = new ArrayList<>();
    String searchString = "";
    String searchQuery = "";
    String imdb = "";
    Elements filme = null;
    int myear = options.getYear();

    /*
     * Kat = All | Titel | Person | DTitel | OTitel | Regie | Darsteller | Song | Rolle | EAN| IMDb | Google
     * http://www.ofdb.de//view.php?page=suchergebnis &Kat=xxxxxxxxx&SText=yyyyyyyyyyy
     */
    // 1. search with imdbId
    if (StringUtils.isNotEmpty(options.getImdbId()) && (filme == null || filme.isEmpty())) {
      try {
        imdb = options.getImdbId();
        searchString = BASE_URL + "/view.php?page=suchergebnis&Kat=IMDb&SText=" + imdb;
        LOGGER.debug("search with imdbId: " + imdb);

        Url url = new Url(searchString);
        InputStream in = url.getInputStream();
        Document doc = Jsoup.parse(in, "UTF-8", "");
        in.close();
        // only look for movie links
        filme = doc.getElementsByAttributeValueMatching("href", "film\\/\\d+,");
        LOGGER.debug("found " + filme.size() + " search results");
      }
      catch (Exception e) {
        LOGGER.error("failed to search for imdb Id " + imdb + ": " + e.getMessage());
      }
    }

    // 2. search for search string
    if (StringUtils.isNotEmpty(options.getQuery()) && (filme == null || filme.isEmpty())) {
      try {
        String query = options.getQuery();
        searchQuery = query;
        query = MetadataUtil.removeNonSearchCharacters(query);
        searchString = BASE_URL + "/view.php?page=suchergebnis&Kat=All&SText=" + URLEncoder.encode(cleanSearch(query), "UTF-8");
        LOGGER.debug("search for everything: " + query);

        Url url = new Url(searchString);
        InputStream in = url.getInputStream();
        Document doc = Jsoup.parse(in, "UTF-8", "");
        in.close();
        // only look for movie links
        filme = doc.getElementsByAttributeValueMatching("href", "film\\/\\d+,");
        LOGGER.debug("found " + filme.size() + " search results");
      }
      catch (Exception e) {
        LOGGER.error("failed to search for " + searchQuery + ": " + e.getMessage());
      }
    }

    if (filme == null || filme.isEmpty()) {
      LOGGER.debug("nothing found :(");
      return resultList;
    }

    // <a href="film/22523,Die-Bourne-Identität"
    // onmouseover="Tip('<img src=&quot;images/film/22/22523.jpg&quot;
    // width=&quot;120&quot; height=&quot;170&quot;>',SHADOW,true)">Bourne
    // Identität, Die<font size="1"> / Bourne Identity, The</font> (2002)</a>
    HashSet<String> foundResultUrls = new HashSet<>();
    for (Element a : filme) {
      try {
        MediaSearchResult sr = new MediaSearchResult(providerInfo.getId(), MediaType.MOVIE);
        if (StringUtils.isNotEmpty(imdb)) {
          sr.setIMDBId(imdb);
        }
        sr.setId(StrgUtils.substr(a.toString(), "film\\/(\\d+),")); // OFDB ID
        sr.setTitle(StringEscapeUtils.unescapeHtml4(StrgUtils.removeCommonSortableName(StrgUtils.substr(a.toString(), ".*>(.*?)(\\[.*?\\])?<font"))));
        LOGGER.debug("found movie " + sr.getTitle());
        sr.setOriginalTitle(StringEscapeUtils.unescapeHtml4(StrgUtils.removeCommonSortableName(StrgUtils.substr(a.toString(), ".*> / (.*?)</font"))));
        try {
          sr.setYear(Integer.parseInt(StrgUtils.substr(a.toString(), "font> \\((.*?)\\)<\\/a")));
        }
        catch (Exception ignored) {
        }

        sr.setUrl(BASE_URL + "/" + StrgUtils.substr(a.toString(), "href=\\\"(.*?)\\\""));
        sr.setPosterUrl(BASE_URL + "/images" + StrgUtils.substr(a.toString(), "images(.*?)\\&quot"));

        // check if it has at least a title and url
        if (StringUtils.isBlank(sr.getTitle()) || StringUtils.isBlank(sr.getUrl())) {
          continue;
        }

        // OFDB could provide linke twice - check if that has been already added
        if (foundResultUrls.contains(sr.getUrl())) {
          continue;
        }
        foundResultUrls.add(sr.getUrl());

        if (imdb.equals(sr.getIMDBId())) {
          // perfect match
          sr.setScore(1);
        }
        else {
          // compare score based on names
          float score = MetadataUtil.calculateScore(searchQuery, sr.getTitle());

          if (yearDiffers(myear, sr.getYear())) {
            float diff = (float) Math.abs(myear - sr.getYear()) / 100;
            LOGGER.debug("parsed year does not match search result year - downgrading score by " + diff);
            score -= diff;
          }
          sr.setScore(score);

        }
        resultList.add(sr);
      }
      catch (Exception e) {
        LOGGER.warn("error parsing movie result: " + e.getMessage());
      }
    }
    Collections.sort(resultList);
    Collections.reverse(resultList);

    return resultList;
  }

  @Override
  public List<MediaTrailer> getTrailers(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getTrailers() " + options.toString());
    List<MediaTrailer> trailers = new ArrayList<>();
    if (!MetadataUtil.isValidImdbId(options.getImdbId())) {
      LOGGER.debug("IMDB id not found");
      return trailers;
    }
    /*
     * function getTrailerData(ci) { switch (ci) { case 'http://de.clip-1.filmtrailer.com/9507_31566_a_1.flv?log_var=72|491100001 -1|-' : return
     * '<b>Trailer 1</b><br><i>(small)</i><br><br>&raquo; 160px<br><br>Download:<br>&raquo; <a href=
     * "http://de.clip-1.filmtrailer.com/9507_31566_a_1.wmv?log_var=72|491100001-1|-" >wmv</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_31566_a_2.flv?log_var=72|491100001 -1|-' : return '<b>Trailer 1</b><br><i>(medium)</i><br><br>&raquo;
     * 240px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_31566_a_2.wmv?log_var=72|491100001-1|-" >wmv</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_31566_a_3.flv?log_var=72|491100001 -1|-' : return '<b>Trailer 1</b><br><i>(large)</i><br><br>&raquo;
     * 320px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_31566_a_3.wmv?log_var=72|491100001-1|-" >wmv</a><br>&raquo;
     * <a href= "http://de.clip-1.filmtrailer.com/9507_31566_a_3.mp4?log_var=72|491100001-1|-" >mp4</a><br>&raquo; <a href=
     * "http://de.clip-1.filmtrailer.com/9507_31566_a_3.webm?log_var=72|491100001-1|-" >webm</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_31566_a_4.flv?log_var=72|491100001 -1|-' : return '<b>Trailer 1</b><br><i>(xlarge)</i><br><br>&raquo;
     * 400px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_31566_a_4.wmv?log_var=72|491100001-1|-" >wmv</a><br>&raquo;
     * <a href= "http://de.clip-1.filmtrailer.com/9507_31566_a_4.mp4?log_var=72|491100001-1|-" >mp4</a><br>&raquo; <a href=
     * "http://de.clip-1.filmtrailer.com/9507_31566_a_4.webm?log_var=72|491100001-1|-" >webm</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_31566_a_5.flv?log_var=72|491100001 -1|-' : return '<b>Trailer 1</b><br><i>(xxlarge)</i><br><br>&raquo;
     * 640px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_31566_a_5.wmv?log_var=72|491100001-1|-" >wmv</a><br>&raquo;
     * <a href= "http://de.clip-1.filmtrailer.com/9507_31566_a_5.mp4?log_var=72|491100001-1|-" >mp4</a><br>&raquo; <a href=
     * "http://de.clip-1.filmtrailer.com/9507_31566_a_5.webm?log_var=72|491100001-1|-" >webm</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_39003_a_1.flv?log_var=72|491100001 -1|-' : return '<b>Trailer 2</b><br><i>(small)</i><br><br>&raquo;
     * 160px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_39003_a_1.wmv?log_var=72|491100001-1|-" >wmv</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_39003_a_2.flv?log_var=72|491100001 -1|-' : return '<b>Trailer 2</b><br><i>(medium)</i><br><br>&raquo;
     * 240px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_39003_a_2.wmv?log_var=72|491100001-1|-" >wmv</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_39003_a_3.flv?log_var=72|491100001 -1|-' : return '<b>Trailer 2</b><br><i>(large)</i><br><br>&raquo;
     * 320px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_39003_a_3.wmv?log_var=72|491100001-1|-" >wmv</a><br>&raquo;
     * <a href= "http://de.clip-1.filmtrailer.com/9507_39003_a_3.mp4?log_var=72|491100001-1|-" >mp4</a><br>&raquo; <a href=
     * "http://de.clip-1.filmtrailer.com/9507_39003_a_3.webm?log_var=72|491100001-1|-" >webm</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_39003_a_4.flv?log_var=72|491100001 -1|-' : return '<b>Trailer 2</b><br><i>(xlarge)</i><br><br>&raquo;
     * 400px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_39003_a_4.wmv?log_var=72|491100001-1|-" >wmv</a><br>&raquo;
     * <a href= "http://de.clip-1.filmtrailer.com/9507_39003_a_4.mp4?log_var=72|491100001-1|-" >mp4</a><br>&raquo; <a href=
     * "http://de.clip-1.filmtrailer.com/9507_39003_a_4.webm?log_var=72|491100001-1|-" >webm</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_39003_a_5.flv?log_var=72|491100001 -1|-' : return '<b>Trailer 2</b><br><i>(xxlarge)</i><br><br>&raquo;
     * 640px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_39003_a_5.wmv?log_var=72|491100001-1|-" >wmv</a><br>&raquo;
     * <a href= "http://de.clip-1.filmtrailer.com/9507_39003_a_5.mp4?log_var=72|491100001-1|-" >mp4</a><br>&raquo; <a href=
     * "http://de.clip-1.filmtrailer.com/9507_39003_a_5.webm?log_var=72|491100001-1|-" >webm</a><br>'; } }
     */
    Url url = null;
    String searchString = BASE_URL + "/view.php?page=suchergebnis&Kat=IMDb&SText=" + options.getImdbId();
    try {
      // search with IMDB
      url = new Url(searchString);
      InputStream in = url.getInputStream();
      Document doc = Jsoup.parse(in, "UTF-8", "");
      in.close();
      Elements filme = doc.getElementsByAttributeValueMatching("href", "film\\/\\d+,");
      if (filme == null || filme.isEmpty()) {
        LOGGER.debug("found no search results");
        return trailers;
      }
      LOGGER.debug("found " + filme.size() + " search results"); // hopefully
                                                                 // only one

      LOGGER.debug("get (trailer) details page");
      url = new Url(BASE_URL + "/" + StrgUtils.substr(filme.first().toString(), "href=\\\"(.*?)\\\""));
      in = url.getInputStream();
      doc = Jsoup.parse(in, "UTF-8", "");
      in.close();

      // OLD STYLE
      // <b>Trailer 1</b><br><i>(xxlarge)</i><br><br>&raquo; 640px<br><br>Download:<br>&raquo; <a href=
      // "http://de.clip-1.filmtrailer.com/9507_31566_a_5.wmv?log_var=72|491100001-1|-" >wmv</a><br>&raquo; <a href=
      // "http://de.clip-1.filmtrailer.com/9507_31566_a_5.mp4?log_var=72|491100001-1|-" >mp4</a><br>&raquo; <a href=
      // "http://de.clip-1.filmtrailer.com/9507_31566_a_5.webm?log_var=72|491100001-1|-" >webm</a><br>
      Pattern regex = Pattern.compile("return '(.*?)';");
      Matcher m = regex.matcher(doc.toString());
      while (m.find()) {
        String s = m.group(1);
        String tname = StrgUtils.substr(s, "<b>(.*?)</b>");
        String tpix = StrgUtils.substr(s, "raquo; (.*?)x<br>");
        // String tqual = StrgUtils.substr(s, "<i>\\((.*?)\\)</i>");

        // url + format
        Pattern lr = Pattern.compile("<a href=\"(.*?)\">(.*?)</a>");
        Matcher lm = lr.matcher(s);
        while (lm.find()) {
          String turl = lm.group(1);
          // String tformat = lm.group(2);
          MediaTrailer trailer = new MediaTrailer();
          trailer.setName(tname);
          // trailer.setQuality(tpix + " (" + tformat + ")");
          trailer.setQuality(tpix);
          trailer.setProvider("filmtrailer");
          trailer.setUrl(turl);
          LOGGER.debug(trailer.toString());
          trailers.add(trailer);
        }
      }

      // NEW STYLE (additional!)
      // <div class="clips" id="clips2" style="display: none;">
      // <img src="images/flag_de.gif" align="left" vspace="3" width="18" height="12">&nbsp;
      // <img src="images/trailer_6.gif" align="top" vspace="1" width="16" height="16" alt="freigegeben ab 6 Jahren">&nbsp;
      // <i>Trailer 1:</i>
      // <a href="http://de.clip-1.filmtrailer.com/2845_6584_a_1.flv?log_var=67|491100001-1|-">&nbsp;small&nbsp;</a> &nbsp;
      // <a href="http://de.clip-1.filmtrailer.com/2845_6584_a_2.flv?log_var=67|491100001-1|-">&nbsp;medium&nbsp;</a> &nbsp;
      // <a href="http://de.clip-1.filmtrailer.com/2845_6584_a_3.flv?log_var=67|491100001-1|-">&nbsp;large&nbsp;</a> &nbsp;
      // <a href="http://de.clip-1.filmtrailer.com/2845_6584_a_4.flv?log_var=67|491100001-1|-">&nbsp;xlarge&nbsp;</a> &nbsp;
      // <a href="http://de.clip-1.filmtrailer.com/2845_6584_a_5.flv?log_var=67|491100001-1|-">&nbsp;xxlarge&nbsp;</a> &nbsp;
      // <br>
      // <img src="images/flag_de.gif" align="left" vspace="3" width="18" height="12">&nbsp;
      // <img src="images/trailer_6.gif" align="top" vspace="1" width="16" height="16" alt="freigegeben ab 6 Jahren">&nbsp;
      // <i>Trailer 2:</i>
      // <a href="http://de.clip-1.filmtrailer.com/2845_8244_a_1.flv?log_var=67|491100001-1|-">&nbsp;small&nbsp;</a> &nbsp;
      // <a href="http://de.clip-1.filmtrailer.com/2845_8244_a_2.flv?log_var=67|491100001-1|-">&nbsp;medium&nbsp;</a> &nbsp;
      // <a href="http://de.clip-1.filmtrailer.com/2845_8244_a_3.flv?log_var=67|491100001-1|-">&nbsp;large&nbsp;</a> &nbsp;
      // <a href="http://de.clip-1.filmtrailer.com/2845_8244_a_4.flv?log_var=67|491100001-1|-">&nbsp;xlarge&nbsp;</a> &nbsp;
      // <a href="http://de.clip-1.filmtrailer.com/2845_8244_a_5.flv?log_var=67|491100001-1|-">&nbsp;xxlarge&nbsp;</a> &nbsp;
      // <br>
      // <img src="images/flag_de.gif" align="left" vspace="3" width="18" height="12">&nbsp;
      // <img src="images/trailer_6.gif" align="top" vspace="1" width="16" height="16" alt="freigegeben ab 6 Jahren">&nbsp;
      // <i>Trailer 3:</i>
      // <a href="http://de.clip-1.filmtrailer.com/2845_14749_a_1.flv?log_var=67|491100001-1|-">&nbsp;small&nbsp;</a> &nbsp;
      // <a href="http://de.clip-1.filmtrailer.com/2845_14749_a_2.flv?log_var=67|491100001-1|-">&nbsp;medium&nbsp;</a> &nbsp;
      // <a href="http://de.clip-1.filmtrailer.com/2845_14749_a_3.flv?log_var=67|491100001-1|-">&nbsp;large&nbsp;</a> &nbsp;
      // <a href="http://de.clip-1.filmtrailer.com/2845_14749_a_4.flv?log_var=67|491100001-1|-">&nbsp;xlarge&nbsp;</a> &nbsp;
      // <a href="http://de.clip-1.filmtrailer.com/2845_14749_a_5.flv?log_var=67|491100001-1|-">&nbsp;xxlarge&nbsp;</a> &nbsp;
      // <br>
      // <br>
      // </div>

      // new style size
      // 1 = 160 x 90 = small
      // 2 = 240 x 136 = medium
      // 3 = 320 x 180 = large
      // 4 = 400 x 226 = xlarge
      // 5 = 640 x 360 = xxlarge

      ;

      regex = Pattern.compile("<i>(.*?)</i>(.*?)<br>", Pattern.DOTALL); // get them as single trailer line
      m = regex.matcher(doc.getElementsByClass("clips").html());
      while (m.find()) {
        // LOGGER.info(doc.getElementsByClass("clips").html());
        // parse each line with 5 qualities
        String tname = m.group(1).trim();
        tname = tname.replaceFirst(":$", ""); // replace ending colon

        String urls = m.group(2);
        // url + format
        Pattern lr = Pattern.compile("<a href=\"(.*?)\">(.*?)</a>");
        Matcher lm = lr.matcher(urls);
        while (lm.find()) {
          String turl = lm.group(1);
          String tpix = "";
          String tformat = lm.group(2).replaceAll("&nbsp;", "").trim();
          switch (tformat) {
            case "small":
              tpix = "90p";
              break;

            case "medium":
              tpix = "136p";
              break;

            case "large":
              tpix = "180p";
              break;

            case "xlarge":
              tpix = "226p";
              break;

            case "xxlarge":
              tpix = "360p";
              break;

            default:
              break;
          }
          MediaTrailer trailer = new MediaTrailer();
          trailer.setName(tname);
          // trailer.setQuality(tpix + " (" + tformat + ")");
          trailer.setQuality(tpix);
          trailer.setProvider("filmtrailer");
          trailer.setUrl(turl);
          LOGGER.debug(trailer.toString());
          trailers.add(trailer);
        }
      }
    }
    catch (Exception e) {
      if (url != null) {
        LOGGER.error("Error parsing {}", url.toString());
      }
      else {
        LOGGER.error("Error parsing {}", searchString);
      }

      throw e;
    }
    return trailers;
  }

  /**
   * return a 2 element array. 0 = title; 1=date
   * 
   * parses the title in the format Title YEAR or Title (YEAR)
   * 
   * @param title
   *          the title
   * @return the string[]
   */
  private String[] parseTitle(String title) {
    String v[] = { "", "" };
    if (title == null)
      return v;

    Pattern p = Pattern.compile("(.*)\\s+\\(?([0-9]{4})\\)?", Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(title);
    if (m.find()) {
      v[0] = m.group(1);
      v[1] = m.group(2);
    }
    else {
      v[0] = title;
    }
    return v;
  }

  /**
   * Is i1 != i2 (when >0)
   */
  private boolean yearDiffers(Integer i1, Integer i2) {
    return i1 != null && i1 != 0 && i2 != null && i2 != 0 && i1 != i2;
  }
}
