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
package org.tinymediamanager.scraper.ofdb;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
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
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.MediaCastMember;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.MetadataUtil;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.scraper.util.ParserUtils;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.scraper.util.Url;

/**
 * The Class OfdbMetadataProvider.
 * 
 * @author Myron Boyle (myron0815@gmx.net)
 */
public class OfdbMetadataProvider implements IMediaMetadataProvider, IMediaTrailerProvider {

  /** The Constant LOGGER. */
  private static final Logger         LOGGER       = LoggerFactory.getLogger(OfdbMetadataProvider.class);

  private static final String         BASE_URL     = "http://www.ofdb.de";

  /** The Constant instance. */
  private static OfdbMetadataProvider instance;

  /** The provider info. */
  private static MediaProviderInfo    providerInfo = new MediaProviderInfo("ofdb", "ofdb.de",
                                                       "Scraper for german ofdb.de which is able to scrape movie metadata");

  /**
   * Gets the single instance of OfdbMetadataProvider.
   * 
   * @return single instance of OfdbMetadataProvider
   */
  public static synchronized OfdbMetadataProvider getInstance() {
    if (instance == null) {
      instance = new OfdbMetadataProvider();
    }
    return instance;
  }

  /**
   * Instantiates a new ofdb metadata provider.
   */
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

    MediaMetadata md = new MediaMetadata(providerInfo.getId());
    // generic Elements used all over
    Elements el = null;
    // preset values from searchresult (if we have them)
    md.setOriginalTitle(Utils.removeSortableName(options.getResult().getOriginalTitle()));
    md.setTitle(Utils.removeSortableName(options.getResult().getTitle()));
    md.setYear(options.getResult().getYear());

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
        md.setImdbId("tt" + StrgUtils.substr(el.first().attr("href"), "\\?(\\d+)"));
      }

      // Title Year
      if (StringUtils.isEmpty(md.getYear()) || StringUtils.isEmpty(md.getTitle())) {
        // <meta property="og:title" content="Bourne Vermächtnis, Das (2012)" />
        el = doc.getElementsByAttributeValue("property", "og:title");
        if (!el.isEmpty()) {
          String[] ty = ParserUtils.parseTitle(el.first().attr("content"));
          md.setTitle(ty[0]);
          md.setYear(ty[1]);
        }
      }
      // another year position
      if (StringUtils.isEmpty(md.getYear())) {
        // <a href="view.php?page=blaettern&Kat=Jahr&Text=2012">2012</a>
        el = doc.getElementsByAttributeValueContaining("href", "Kat=Jahr");
        md.setYear(el.first().text());
      }

      // Genre: <a href="view.php?page=genre&Genre=Action">Action</a>
      el = doc.getElementsByAttributeValueContaining("href", "page=genre");
      for (Element g : el) {
        MediaGenres genre = MediaGenres.getGenre(g.text());
        if (genre != null && !md.getGenres().contains(genre)) {
          md.addGenre(genre);
        }
      }

      // rating
      // <br>Note: 8.93 &nbsp;&#149;&nbsp;&nbsp;Stimmen: 2959
      // &nbsp;&#149;&nbsp;&nbsp;Platz: 1 &nbsp;&#149;&nbsp;&nbsp;Ihre Note:
      // --<br>
      String r = StrgUtils.substr(doc.body().toString(), "Note: (.*?) &nbsp");
      if (!r.isEmpty()) {
        try {
          double rating = Double.parseDouble(r);
          md.setRating(rating);
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
        md.setPlot(p);
        md.setTagline(p.length() > 150 ? p.substring(0, 150) : p);
      }

      // http://www.ofdb.de/view.php?page=film_detail&fid=226745
      LOGGER.debug("parse actor detail");
      String movieDetail = BASE_URL + "/view.php?page=film_detail&fid=" + ofdbId;
      url = new CachedUrl(movieDetail);
      in = url.getInputStream();
      doc = Jsoup.parse(in, "UTF-8", "");
      in.close();

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
    catch (IOException e) {
      LOGGER.error("Error parsing " + options.getResult().getUrl());
      throw e;
    }

    return md;
  }

  /**
   * Removes all weird characters from search as well some "stopwords" as der|die|das|the|a
   * 
   * @param q
   *          the query string to clean
   * @return
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
    String imdb = "";

    /*
     * Kat = All | Titel | Person | DTitel | OTitel | Regie | Darsteller | Song | Rolle | EAN| IMDb | Google
     * http://www.ofdb.de//view.php?page=suchergebnis &Kat=xxxxxxxxx&SText=yyyyyyyyyyy
     */
    // detect the search preference (1. imdb, 2. title, 3. all)
    if (StringUtils.isNotEmpty(options.get(MediaSearchOptions.SearchParam.IMDBID))) {
      imdb = options.get(MediaSearchOptions.SearchParam.IMDBID);
      searchString = BASE_URL + "/view.php?page=suchergebnis&Kat=IMDb&SText=" + imdb;
      LOGGER.debug("search with imdbId: " + imdb);
    }
    else if (StringUtils.isNotEmpty(options.get(MediaSearchOptions.SearchParam.TITLE))) {
      String title = options.get(MediaSearchOptions.SearchParam.TITLE);
      title = MetadataUtil.removeNonSearchCharacters(title);
      searchString = BASE_URL + "/view.php?page=suchergebnis&Kat=Titel&SText=" + URLEncoder.encode(cleanSearch(title), "UTF-8");
      LOGGER.debug("search with title: " + title);
    }
    else if (StringUtils.isNotEmpty(options.get(MediaSearchOptions.SearchParam.QUERY))) {
      String query = options.get(MediaSearchOptions.SearchParam.QUERY);
      query = MetadataUtil.removeNonSearchCharacters(query);
      searchString = BASE_URL + "/view.php?page=suchergebnis&Kat=All&SText=" + URLEncoder.encode(cleanSearch(query), "UTF-8");
      LOGGER.debug("search for everything: " + query);
    }
    else {
      LOGGER.debug("empty searchString");
      return resultList;
    }

    Url url = new CachedUrl(searchString);
    InputStream in = url.getInputStream();
    Document doc = Jsoup.parse(in, "UTF-8", "");
    in.close();
    // only look for movie links
    Elements filme = doc.getElementsByAttributeValueMatching("href", "film\\/\\d+,");
    LOGGER.debug("found " + filme.size() + " search results");
    if (filme == null || filme.isEmpty()) {
      return resultList;
    }

    // <a href="film/22523,Die-Bourne-Identität"
    // onmouseover="Tip('<img src=&quot;images/film/22/22523.jpg&quot; width=&quot;120&quot; height=&quot;170&quot;>',SHADOW,true)">Bourne
    // Identität, Die<font size="1"> / Bourne Identity, The</font> (2002)</a>

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
        // populate extra args
        MetadataUtil.copySearchQueryToSearchResult(options, sr);
        resultList.add(sr);
      }
      catch (Exception e) {
        LOGGER.warn("error parsing movie result: " + e.getMessage());
      }
    }

    return resultList;
  }

  @Override
  public List<MediaTrailer> getTrailers(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getTrailers() " + options.toString());
    List<MediaTrailer> trailers = new ArrayList<MediaTrailer>();
    if (options.getImdbId().isEmpty()) {
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
    try {
      // search with IMDB
      String searchString = BASE_URL + "/view.php?page=suchergebnis&Kat=IMDb&SText=" + options.getImdbId();
      url = new CachedUrl(searchString);
      InputStream in = url.getInputStream();
      Document doc = Jsoup.parse(in, "UTF-8", "");
      in.close();
      Elements filme = doc.getElementsByAttributeValueMatching("href", "film\\/\\d+,");
      LOGGER.debug("found " + filme.size() + " search results"); // hopefully
                                                                 // only one
      if (filme == null || filme.isEmpty()) {
        return trailers;
      }

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
          String tformat = lm.group(2);
          MediaTrailer trailer = new MediaTrailer();
          trailer.setName(tname);
          trailer.setQuality(tpix + " (" + tformat + ")");
          trailer.setProvider("filmtrailer");
          trailer.setUrl(turl);
          LOGGER.debug(trailer.toString());
          trailers.add(trailer);
        }
      }
    }
    catch (IOException e) {
      LOGGER.error("Error parsing " + url.toString());
      throw e;
    }
    return trailers;
  }
}
