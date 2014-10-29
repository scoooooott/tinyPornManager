/*
 * Copyright 2012 - 2014 Manuel Laggner
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
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.MediaCastMember;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchOptions.SearchParam;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.MetadataUtil;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.scraper.util.ParserUtils;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.scraper.util.Url;

/**
 * The Class OfdbMetadataProvider. A meta data provider for the site ofdb.de
 * 
 * @author Myron Boyle (myron0815@gmx.net)
 */
public class OfdbMetadataProvider implements IMediaMetadataProvider, IMediaTrailerProvider {
  private static final Logger         LOGGER       = LoggerFactory.getLogger(OfdbMetadataProvider.class);
  private static final String         BASE_URL     = "http://www.ofdb.de";

  private static OfdbMetadataProvider instance;
  private static MediaProviderInfo    providerInfo = new MediaProviderInfo(Constants.OFDBID, "ofdb.de",
                                                       "Scraper for german ofdb.de which is able to scrape movie metadata");

  public static synchronized OfdbMetadataProvider getInstance() {
    if (instance == null) {
      instance = new OfdbMetadataProvider();
    }
    return instance;
  }

  public OfdbMetadataProvider() {
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

    // if we did not have a prior search, fake one
    if (options.getResult() == null && StringUtils.isNotBlank(options.getId(Constants.IMDBID))) {
      MediaSearchOptions searchOptions = new MediaSearchOptions(MediaType.MOVIE);
      searchOptions.set(SearchParam.IMDBID, options.getId(Constants.IMDBID));
      try {
        List<MediaSearchResult> results = search(searchOptions);
        if (results != null && !results.isEmpty()) {
          options.setResult(results.get(0));
        }
      }
      catch (Exception e) {
        LOGGER.warn("failed IMDB search: " + e.getMessage());
      }
    }

    // we can only work further if we got a search result on ofdb.de
    if (options.getResult() == null) {
      throw new Exception("Scrape with ofdb.de without prior search is not supported");
    }

    MediaMetadata md = new MediaMetadata(providerInfo.getId());
    // generic Elements used all over
    Elements el = null;
    // preset values from searchresult (if we have them)
    md.storeMetadata(MediaMetadata.ORIGINAL_TITLE, Utils.removeSortableName(options.getResult().getOriginalTitle()));
    md.storeMetadata(MediaMetadata.TITLE, Utils.removeSortableName(options.getResult().getTitle()));
    md.storeMetadata(MediaMetadata.YEAR, options.getResult().getYear());

    String ofdbId = StrgUtils.substr(options.getResult().getUrl(), "film\\/(\\d+),");

    Url url;
    try {
      LOGGER.debug("get details page");
      url = new CachedUrl(options.getResult().getUrl());
      InputStream in = url.getInputStream();
      Document doc = Jsoup.parse(in, "UTF-8", "");
      in.close();

      // parse details

      // IMDB ID "http://www.imdb.com/Title?1194173"
      el = doc.getElementsByAttributeValueContaining("href", "imdb.com");
      if (!el.isEmpty()) {
        md.setId(MediaMetadata.IMDBID, "tt" + StrgUtils.substr(el.first().attr("href"), "\\?(\\d+)"));
      }

      // Title Year
      if (StringUtils.isEmpty(md.getStringValue(MediaMetadata.YEAR)) || StringUtils.isEmpty(md.getStringValue(MediaMetadata.TITLE))) {
        // <meta property="og:title" content="Bourne Vermächtnis, Das (2012)" />
        el = doc.getElementsByAttributeValue("property", "og:title");
        if (!el.isEmpty()) {
          String[] ty = ParserUtils.parseTitle(el.first().attr("content"));
          md.storeMetadata(MediaMetadata.TITLE, ty[0]);
          md.storeMetadata(MediaMetadata.YEAR, ty[1]);
        }
      }
      // another year position
      if (StringUtils.isEmpty(md.getStringValue(MediaMetadata.YEAR))) {
        // <a href="view.php?page=blaettern&Kat=Jahr&Text=2012">2012</a>
        el = doc.getElementsByAttributeValueContaining("href", "Kat=Jahr");
        md.storeMetadata(MediaMetadata.YEAR, el.first().text());
      }

      // Genre: <a href="view.php?page=genre&Genre=Action">Action</a>
      el = doc.getElementsByAttributeValueContaining("href", "page=genre");
      for (Element g : el) {
        md.addGenre(getTmmGenre(g.text()));
      }

      // rating
      // <br>Note: 8.93 &nbsp;&#149;&nbsp;&nbsp;Stimmen: 2959
      // &nbsp;&#149;&nbsp;&nbsp;Platz: 1 &nbsp;&#149;&nbsp;&nbsp;Ihre Note:
      // --<br>
      String r = StrgUtils.substr(doc.body().toString(), "Note: (.*?) &nbsp");
      if (!r.isEmpty()) {
        try {
          double rating = Double.parseDouble(r);
          md.storeMetadata(MediaMetadata.RATING, rating);
        }
        catch (Exception e) {
          LOGGER.debug("could not parse rating");
        }
      }

      // get PlotLink; open url and parse
      // <a href="plot/22523,31360,Die-Bourne-Identität"><b>[mehr]</b></a>
      LOGGER.debug("parse plot");
      el = doc.getElementsByAttributeValueMatching("href", "plot\\/\\d+,");
      if (!el.isEmpty()) {
        String plotUrl = BASE_URL + "/" + el.first().attr("href");
        try {
          url = new CachedUrl(plotUrl);
          in = url.getInputStream();
          Document plot = Jsoup.parse(in, "UTF-8", "");
          in.close();
          Elements block = plot.getElementsByClass("Blocksatz"); // first
                                                                 // Blocksatz
                                                                 // is plot
          String p = block.first().text(); // remove all html stuff
          p = p.substring(p.indexOf("Mal gelesen") + 12); // remove "header"
          // LOGGER.info(p);
          md.storeMetadata(MediaMetadata.PLOT, p);
          md.storeMetadata(MediaMetadata.TAGLINE, p.length() > 150 ? p.substring(0, 150) : p);
        }
        catch (Exception e) {
          LOGGER.error("failed to get plot page: " + e.getMessage());

          // clear cache
          CachedUrl.removeCachedFileForUrl(plotUrl);
        }
      }

      // http://www.ofdb.de/view.php?page=film_detail&fid=226745
      LOGGER.debug("parse actor detail");
      String movieDetail = BASE_URL + "/view.php?page=film_detail&fid=" + ofdbId;
      doc = null;
      try {
        url = new CachedUrl(movieDetail);
        in = url.getInputStream();
        doc = Jsoup.parse(in, "UTF-8", "");
        in.close();
      }
      catch (Exception e) {
        LOGGER.error("failed to get detail page: " + e.getMessage());

        // clear cache
        CachedUrl.removeCachedFileForUrl(movieDetail);
      }

      if (doc != null) {
        el = doc.getElementsByAttributeValue("valign", "middle");
        // <tr valign="middle">
        // <td nowrap><a href="view.php?page=person&id=7689"><img
        // src="thumbnail.php?cover=images%2Fperson%2F7%2F7689.jpg&size=6"
        // alt="Jeremy Renner" border="0" width="36"></a>&nbsp;&nbsp;</td>
        // <td nowrap><font face="Arial,Helvetica,sans-serif" size="2"
        // class="Daten"><a href="view.php?page=person&id=7689"><b>Jeremy
        // Renner</b></a></font></td>
        // <td nowrap>&nbsp;&nbsp;</td>
        // <td><font face="Arial,Helvetica,sans-serif" size="2" class="Normal">...
        // Aaron Cross</font></td>
        // </tr>
        int i = 0;
        for (Element a : el) {
          String act = a.toString();
          String aname = StrgUtils.substr(act, "<b>(.*?)</b>");
          if (!aname.isEmpty()) {
            MediaCastMember cm = new MediaCastMember();
            cm.setName(aname);
            String id = StrgUtils.substr(act, "id=(.*?)[^\"]\">");
            if (!id.isEmpty()) {
              cm.setId(id);
              // thumb
              // http://www.ofdb.de/thumbnail.php?cover=images%2Fperson%2F7%2F7689.jpg&size=6
              // fullsize ;) http://www.ofdb.de/images/person/7/7689.jpg
              String imgurl = URLDecoder.decode(StrgUtils.substr(act, "images%2Fperson%2F(.*?)&amp;size"), "UTF-8");
              if (!imgurl.isEmpty()) {
                imgurl = BASE_URL + "/images/person/" + imgurl;
              }
              cm.setImageUrl(imgurl);
            }
            String arole = StrgUtils.substr(act, "\\.\\.\\. (.*?)</font>");
            cm.setCharacter(arole);
            cm.setType(MediaCastMember.CastType.ACTOR);
            if (i == 0) {
              cm.setType(MediaCastMember.CastType.DIRECTOR);
            }
            i++;
            md.addCastMember(cm);
          }
        }
      }
    }
    catch (Exception e) {
      LOGGER.error("Error parsing " + options.getResult().getUrl());

      // clear cache
      CachedUrl.removeCachedFileForUrl(options.getResult().getUrl());

      throw e;
    }

    return md;
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
    else if (genre.equals("Abenteuer"))            { g = MediaGenres.ADVENTURE; }
    else if (genre.equals("Action"))               { g = MediaGenres.ACTION; }
    else if (genre.equals("Amateur"))              { g = MediaGenres.INDIE; }
    else if (genre.equals("Animation"))            { g = MediaGenres.ANIMATION; }
    else if (genre.equals("Anime"))                { g = MediaGenres.ANIMATION; }
    else if (genre.equals("Biographie"))           { g = MediaGenres.BIOGRAPHY; }
    else if (genre.equals("Dokumentation"))        { g = MediaGenres.DOCUMENTARY; }
    else if (genre.equals("Drama"))                { g = MediaGenres.DRAMA; }
    else if (genre.equals("Eastern"))              { g = MediaGenres.EASTERN; }
    else if (genre.equals("Erotik"))               { g = MediaGenres.EROTIC; }
    else if (genre.equals("Essayfilm"))            { g = MediaGenres.INDIE; }
    else if (genre.equals("Experimentalfilm"))     { g = MediaGenres.INDIE; }
    else if (genre.equals("Fantasy"))              { g = MediaGenres.FANTASY; }
    else if (genre.equals("Grusel"))               { g = MediaGenres.HORROR; }
    else if (genre.equals("Hardcore"))             { g = MediaGenres.EROTIC; }
    else if (genre.equals("Heimatfilm"))           { g = MediaGenres.TV_MOVIE; }
    else if (genre.equals("Historienfilm"))        { g = MediaGenres.HISTORY; }
    else if (genre.equals("Horror"))               { g = MediaGenres.HORROR; }
    else if (genre.equals("Kampfsport"))           { g = MediaGenres.SPORT; }
    else if (genre.equals("Katastrophen"))         { g = MediaGenres.DISASTER; }
    else if (genre.equals("Kinder-/Familienfilm")) { g = MediaGenres.FAMILY; }
    else if (genre.equals("Komödie"))              { g = MediaGenres.COMEDY; }
    else if (genre.equals("Krieg"))                { g = MediaGenres.WAR; }
    else if (genre.equals("Krimi"))                { g = MediaGenres.CRIME; }
    else if (genre.equals("Kurzfilm"))             { g = MediaGenres.SHORT; }
    else if (genre.equals("Liebe/Romantik"))       { g = MediaGenres.ROMANCE; }
    else if (genre.equals("Mondo"))                { g = MediaGenres.DOCUMENTARY; }
    else if (genre.equals("Musikfilm"))            { g = MediaGenres.MUSIC; }
    else if (genre.equals("Mystery"))              { g = MediaGenres.MYSTERY; }
    else if (genre.equals("Science-Fiction"))      { g = MediaGenres.SCIENCE_FICTION; }
    else if (genre.equals("Serial"))               { g = MediaGenres.SERIES; }
    else if (genre.equals("Sex"))                  { g = MediaGenres.EROTIC; }
    else if (genre.equals("Splatter"))             { g = MediaGenres.HORROR; }
    else if (genre.equals("Sportfilm"))            { g = MediaGenres.SPORT; }
    else if (genre.equals("Stummfilm"))            { g = MediaGenres.SILENT_MOVIE; }
    else if (genre.equals("TV-Film"))              { g = MediaGenres.TV_MOVIE; }
    else if (genre.equals("TV-Mini-Serie"))        { g = MediaGenres.SERIES; }
    else if (genre.equals("TV-Pilotfilm"))         { g = MediaGenres.TV_MOVIE; }
    else if (genre.equals("TV-Serie"))             { g = MediaGenres.SERIES; }
    else if (genre.equals("Thriller"))             { g = MediaGenres.THRILLER; }
    else if (genre.equals("Tierfilm"))             { g = MediaGenres.ANIMAL; }
    else if (genre.equals("Webserie"))             { g = MediaGenres.SERIES; }
    else if (genre.equals("Western"))              { g = MediaGenres.WESTERN; }
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
    List<MediaSearchResult> resultList = new ArrayList<MediaSearchResult>();
    String searchString = "";
    String searchQuery = "";
    String imdb = "";
    Elements filme = null;
    String myear = options.get(MediaSearchOptions.SearchParam.YEAR);

    /*
     * Kat = All | Titel | Person | DTitel | OTitel | Regie | Darsteller | Song | Rolle | EAN| IMDb | Google
     * http://www.ofdb.de//view.php?page=suchergebnis &Kat=xxxxxxxxx&SText=yyyyyyyyyyy
     */
    // 1. search with imdbId
    if (StringUtils.isNotEmpty(options.get(MediaSearchOptions.SearchParam.IMDBID)) && (filme == null || filme.isEmpty())) {
      try {
        imdb = options.get(MediaSearchOptions.SearchParam.IMDBID);
        searchString = BASE_URL + "/view.php?page=suchergebnis&Kat=IMDb&SText=" + imdb;
        LOGGER.debug("search with imdbId: " + imdb);

        Url url = new CachedUrl(searchString);
        InputStream in = url.getInputStream();
        Document doc = Jsoup.parse(in, "UTF-8", "");
        in.close();
        // only look for movie links
        filme = doc.getElementsByAttributeValueMatching("href", "film\\/\\d+,");
        LOGGER.debug("found " + filme.size() + " search results");
      }
      catch (Exception e) {
        LOGGER.error("failed to search for imdb Id " + imdb + ": " + e.getMessage());

        // clear cache
        CachedUrl.removeCachedFileForUrl(searchString);
      }
    }

    // 2. search for search string
    if (StringUtils.isNotEmpty(options.get(MediaSearchOptions.SearchParam.QUERY)) && (filme == null || filme.isEmpty())) {
      try {
        String query = options.get(MediaSearchOptions.SearchParam.QUERY);
        searchQuery = query;
        query = MetadataUtil.removeNonSearchCharacters(query);
        searchString = BASE_URL + "/view.php?page=suchergebnis&Kat=All&SText=" + URLEncoder.encode(cleanSearch(query), "UTF-8");
        LOGGER.debug("search for everything: " + query);

        Url url = new CachedUrl(searchString);
        InputStream in = url.getInputStream();
        Document doc = Jsoup.parse(in, "UTF-8", "");
        in.close();
        // only look for movie links
        filme = doc.getElementsByAttributeValueMatching("href", "film\\/\\d+,");
        LOGGER.debug("found " + filme.size() + " search results");
      }
      catch (Exception e) {
        LOGGER.error("failed to search for " + searchQuery + ": " + e.getMessage());

        // clear cache
        CachedUrl.removeCachedFileForUrl(searchString);
      }
    }

    // 3. search for title
    if (StringUtils.isNotEmpty(options.get(MediaSearchOptions.SearchParam.TITLE)) && (filme == null || filme.isEmpty())) {
      try {
        String title = options.get(MediaSearchOptions.SearchParam.TITLE);
        searchQuery = title;
        title = MetadataUtil.removeNonSearchCharacters(title);
        searchString = BASE_URL + "/view.php?page=suchergebnis&Kat=Titel&SText=" + URLEncoder.encode(cleanSearch(title), "UTF-8");
        LOGGER.debug("search with title: " + title);

        Url url = new CachedUrl(searchString);
        InputStream in = url.getInputStream();
        Document doc = Jsoup.parse(in, "UTF-8", "");
        in.close();
        // only look for movie links
        filme = doc.getElementsByAttributeValueMatching("href", "film\\/\\d+,");
        LOGGER.debug("found " + filme.size() + " search results");
      }
      catch (Exception e) {
        LOGGER.error("failed to search for " + searchQuery + ": " + e.getMessage());

        // clear cache
        CachedUrl.removeCachedFileForUrl(searchString);
      }
    }

    if (filme == null || filme.isEmpty()) {
      LOGGER.debug("nothing found :(");
      return resultList;
    }

    // <a href="film/22523,Die-Bourne-Identität"
    // onmouseover="Tip('<img src=&quot;images/film/22/22523.jpg&quot; width=&quot;120&quot; height=&quot;170&quot;>',SHADOW,true)">Bourne
    // Identität, Die<font size="1"> / Bourne Identity, The</font> (2002)</a>
    HashSet<String> foundResultUrls = new HashSet<String>();
    for (Element a : filme) {
      try {
        MediaSearchResult sr = new MediaSearchResult(providerInfo.getId());
        if (StringUtils.isNotEmpty(imdb)) {
          sr.setIMDBId(imdb);
        }
        sr.setId(StrgUtils.substr(a.toString(), "film\\/(\\d+),")); // OFDB ID
        sr.setTitle(StringEscapeUtils.unescapeHtml4(StrgUtils.substr(a.toString(), ">(.*?)<font")));
        LOGGER.debug("found movie " + sr.getTitle());
        sr.setOriginalTitle(StringEscapeUtils.unescapeHtml4(StrgUtils.substr(a.toString(), "> / (.*?)</font")));
        sr.setYear(StrgUtils.substr(a.toString(), "font> \\((.*?)\\)<\\/a"));
        sr.setMediaType(MediaType.MOVIE);
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

        // populate extra args
        MetadataUtil.copySearchQueryToSearchResult(options, sr);

        if (imdb.equals(sr.getIMDBId())) {
          // perfect match
          sr.setScore(1);
        }
        else {
          // compare score based on names
          float score = MetadataUtil.calculateScore(searchQuery, sr.getTitle());

          if (myear != null && !myear.isEmpty() && !myear.equals("0") && !myear.equals(sr.getYear())) {
            LOGGER.debug("parsed year does not match search result year - downgrading score by 0.01");
            score = score - 0.01f;
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
    List<MediaTrailer> trailers = new ArrayList<MediaTrailer>();
    if (!Utils.isValidImdbId(options.getImdbId())) {
      LOGGER.debug("IMDB id not found");
      return trailers;
    }
    /*
     * function getTrailerData(ci) { switch (ci) { case 'http://de.clip-1.filmtrailer.com/9507_31566_a_1.flv?log_var=72|491100001-1|-' : return
     * '<b>Trailer 1</b><br><i>(small)</i><br><br>&raquo; 160px<br><br>Download:<br>&raquo; <a href=
     * "http://de.clip-1.filmtrailer.com/9507_31566_a_1.wmv?log_var=72|491100001-1|-" >wmv</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_31566_a_2.flv?log_var=72|491100001-1|-' : return '<b>Trailer 1</b><br><i>(medium)</i><br><br>&raquo;
     * 240px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_31566_a_2.wmv?log_var=72|491100001-1|-" >wmv</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_31566_a_3.flv?log_var=72|491100001-1|-' : return '<b>Trailer 1</b><br><i>(large)</i><br><br>&raquo;
     * 320px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_31566_a_3.wmv?log_var=72|491100001-1|-" >wmv</a><br>&raquo;
     * <a href= "http://de.clip-1.filmtrailer.com/9507_31566_a_3.mp4?log_var=72|491100001-1|-" >mp4</a><br>&raquo; <a href=
     * "http://de.clip-1.filmtrailer.com/9507_31566_a_3.webm?log_var=72|491100001-1|-" >webm</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_31566_a_4.flv?log_var=72|491100001-1|-' : return '<b>Trailer 1</b><br><i>(xlarge)</i><br><br>&raquo;
     * 400px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_31566_a_4.wmv?log_var=72|491100001-1|-" >wmv</a><br>&raquo;
     * <a href= "http://de.clip-1.filmtrailer.com/9507_31566_a_4.mp4?log_var=72|491100001-1|-" >mp4</a><br>&raquo; <a href=
     * "http://de.clip-1.filmtrailer.com/9507_31566_a_4.webm?log_var=72|491100001-1|-" >webm</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_31566_a_5.flv?log_var=72|491100001-1|-' : return '<b>Trailer 1</b><br><i>(xxlarge)</i><br><br>&raquo;
     * 640px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_31566_a_5.wmv?log_var=72|491100001-1|-" >wmv</a><br>&raquo;
     * <a href= "http://de.clip-1.filmtrailer.com/9507_31566_a_5.mp4?log_var=72|491100001-1|-" >mp4</a><br>&raquo; <a href=
     * "http://de.clip-1.filmtrailer.com/9507_31566_a_5.webm?log_var=72|491100001-1|-" >webm</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_39003_a_1.flv?log_var=72|491100001-1|-' : return '<b>Trailer 2</b><br><i>(small)</i><br><br>&raquo;
     * 160px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_39003_a_1.wmv?log_var=72|491100001-1|-" >wmv</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_39003_a_2.flv?log_var=72|491100001-1|-' : return '<b>Trailer 2</b><br><i>(medium)</i><br><br>&raquo;
     * 240px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_39003_a_2.wmv?log_var=72|491100001-1|-" >wmv</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_39003_a_3.flv?log_var=72|491100001-1|-' : return '<b>Trailer 2</b><br><i>(large)</i><br><br>&raquo;
     * 320px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_39003_a_3.wmv?log_var=72|491100001-1|-" >wmv</a><br>&raquo;
     * <a href= "http://de.clip-1.filmtrailer.com/9507_39003_a_3.mp4?log_var=72|491100001-1|-" >mp4</a><br>&raquo; <a href=
     * "http://de.clip-1.filmtrailer.com/9507_39003_a_3.webm?log_var=72|491100001-1|-" >webm</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_39003_a_4.flv?log_var=72|491100001-1|-' : return '<b>Trailer 2</b><br><i>(xlarge)</i><br><br>&raquo;
     * 400px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_39003_a_4.wmv?log_var=72|491100001-1|-" >wmv</a><br>&raquo;
     * <a href= "http://de.clip-1.filmtrailer.com/9507_39003_a_4.mp4?log_var=72|491100001-1|-" >mp4</a><br>&raquo; <a href=
     * "http://de.clip-1.filmtrailer.com/9507_39003_a_4.webm?log_var=72|491100001-1|-" >webm</a><br>'; case
     * 'http://de.clip-1.filmtrailer.com/9507_39003_a_5.flv?log_var=72|491100001-1|-' : return '<b>Trailer 2</b><br><i>(xxlarge)</i><br><br>&raquo;
     * 640px<br><br>Download:<br>&raquo; <a href= "http://de.clip-1.filmtrailer.com/9507_39003_a_5.wmv?log_var=72|491100001-1|-" >wmv</a><br>&raquo;
     * <a href= "http://de.clip-1.filmtrailer.com/9507_39003_a_5.mp4?log_var=72|491100001-1|-" >mp4</a><br>&raquo; <a href=
     * "http://de.clip-1.filmtrailer.com/9507_39003_a_5.webm?log_var=72|491100001-1|-" >webm</a><br>'; } }
     */
    Url url = null;
    String searchString = BASE_URL + "/view.php?page=suchergebnis&Kat=IMDb&SText=" + options.getImdbId();
    try {
      // search with IMDB
      url = new CachedUrl(searchString);
      InputStream in = url.getInputStream();
      Document doc = Jsoup.parse(in, "UTF-8", "");
      in.close();
      Elements filme = doc.getElementsByAttributeValueMatching("href", "film\\/\\d+,");
      if (filme == null || filme.isEmpty()) {
        LOGGER.debug("found no search results");
        return trailers;
      }
      LOGGER.debug("found " + filme.size() + " search results"); // hopefully only one

      LOGGER.debug("get (trailer) details page");
      url = new CachedUrl(BASE_URL + "/" + StrgUtils.substr(filme.first().toString(), "href=\\\"(.*?)\\\""));
      in = url.getInputStream();
      doc = Jsoup.parse(in, "UTF-8", "");
      in.close();

      Pattern regex = Pattern.compile("return '(.*?)';");
      Matcher m = regex.matcher(doc.toString());
      while (m.find()) {
        String s = m.group(1);
        /*
         * <b>Trailer 1</b><br><i>(xxlarge)</i><br><br>&raquo; 640px<br><br>Download:<br>&raquo; <a href=
         * "http://de.clip-1.filmtrailer.com/9507_31566_a_5.wmv?log_var=72|491100001-1|-" >wmv</a><br>&raquo; <a href=
         * "http://de.clip-1.filmtrailer.com/9507_31566_a_5.mp4?log_var=72|491100001-1|-" >mp4</a><br>&raquo; <a href=
         * "http://de.clip-1.filmtrailer.com/9507_31566_a_5.webm?log_var=72|491100001-1|-" >webm</a><br>
         */
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
    }
    catch (Exception e) {
      if (url != null) {
        LOGGER.error("Error parsing {}", url.toString());
      }
      else {
        LOGGER.error("Error parsing {}", searchString);
      }

      // clear cache
      CachedUrl.removeCachedFileForUrl(searchString);

      throw e;
    }
    return trailers;
  }
}
