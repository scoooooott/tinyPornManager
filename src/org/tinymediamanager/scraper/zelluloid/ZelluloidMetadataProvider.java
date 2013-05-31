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
package org.tinymediamanager.scraper.zelluloid;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.Certification;
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
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.scraper.util.Url;

/**
 * The Class OfdbMetadataProvider.
 * 
 * @author Myron Boyle (myron0815@gmx.net)
 */
public class ZelluloidMetadataProvider implements IMediaMetadataProvider, IMediaTrailerProvider {

  /** The Constant LOGGER. */
  private static final Logger              LOGGER        = LoggerFactory.getLogger(ZelluloidMetadataProvider.class);

  /** The Constant BASE_URL. */
  private static final String              BASE_URL      = "http://www.zelluloid.de";

  /** The Constant PAGE_ENCODING. */
  private static final String              PAGE_ENCODING = "ISO-8859-1";

  /** The Constant instance. */
  private static ZelluloidMetadataProvider instance;

  /** The provider info. */
  private static MediaProviderInfo         providerInfo  = new MediaProviderInfo("zelluloid", "zelluloid.de",
                                                             "Scraper for german zelluloid.de which is able to scrape movie metadata");

  /**
   * Gets the single instance of OfdbMetadataProvider.
   * 
   * @return single instance of OfdbMetadataProvider
   */
  public static synchronized ZelluloidMetadataProvider getInstance() {
    if (instance == null) {
      instance = new ZelluloidMetadataProvider();
    }
    return instance;
  }

  /**
   * Instantiates a new ofdb metadata provider.
   */
  public ZelluloidMetadataProvider() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaMetadataProvider#getProviderInfo()
   */
  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaMetadataProvider#getMetadata(org. tinymediamanager.scraper.MediaScrapeOptions)
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
    md.setOriginalTitle(options.getResult().getOriginalTitle());

    String id = "";
    if (StringUtils.isEmpty(options.getResult().getId())) {
      id = StrgUtils.substr(options.getResult().getUrl(), "id=(.*?)");
    }
    else {
      id = options.getResult().getId();
    }

    String detailurl = options.getResult().getUrl();
    if (StringUtils.isEmpty(detailurl)) {
      detailurl = BASE_URL + "/filme/index.php3?id=" + id;
    }

    Url url;
    try {
      LOGGER.debug("get details page");
      url = new CachedUrl(detailurl);
      InputStream in = url.getInputStream();
      Document doc = Jsoup.parse(in, PAGE_ENCODING, "");
      in.close();

      // parse plot
      String plot = doc.getElementsByAttributeValue("class", "bigtext").text();
      md.setPlot(plot);
      md.setTagline(plot.length() > 150 ? plot.substring(0, 150) : plot);

      // parse poster
      el = doc.getElementsByAttributeValueStarting("pic", "/images/poster");
      if (el.size() == 1) {
        md.setPosterUrl(BASE_URL + el.get(0).attr("pic"));
      }

      // parse year
      if (StringUtils.isEmpty(md.getYear())) {
        el = doc.getElementsByAttributeValueContaining("href", "az.php3?j=");
        if (el.size() == 1) {
          md.setYear(el.get(0).text());
        }
      }

      // parse cinema release
      el = doc.getElementsByAttributeValueContaining("href", "?v=w");
      if (el.size() > 0) {
        try {
          SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
          Date d = sdf.parse(el.get(0).text());
          sdf = new SimpleDateFormat("yyyy-MM-dd");
          md.setReleaseDate(sdf.format(d));
        }
        catch (Exception e) {
          LOGGER.warn("cannot parse cinema release date: " + el.get(0).text());
        }
      }

      // parse original title
      if (StringUtils.isEmpty(md.getOriginalTitle())) {
        md.setOriginalTitle(StrgUtils.substr(doc.toString(), "Originaltitel: (.*?)\\<"));
      }
      if (StringUtils.isEmpty(md.getOriginalTitle())) {
        md.setOriginalTitle(md.getTitle());
      }

      // parse runtime
      String rt = (StrgUtils.substr(doc.toString(), "ca.&nbsp;(.*?)&nbsp;min"));
      if (!rt.isEmpty()) {
        try {
          md.setRuntime(Integer.valueOf(rt));
        }
        catch (Exception e2) {
          LOGGER.warn("cannot convert runtime: " + rt);
        }
      }

      // parse genres
      el = doc.getElementsByAttributeValueContaining("href", "az.php3?g=");
      for (Element g : el) {
        MediaGenres genre = MediaGenres.getGenre(g.text());
        if (genre != null && !md.getGenres().contains(genre)) {
          md.addGenre(genre);
        }
      }

      // parse cert
      // FSK: ab 12, $230 Mio. Budget
      String fsk = StrgUtils.substr(doc.toString(), "FSK: (.*?)[,<]");
      if (!fsk.isEmpty()) {
        md.addCertification(Certification.findCertification(fsk));
      }

      // parse rating
      Elements ratings = doc.getElementsByAttributeValue("class", "ratingBarTable");
      if (ratings.size() == 2) { // get user rating
        Element e = ratings.get(1);
        // <div>87%</div>
        String r = e.getElementsByTag("div").text().replace("%", "");
        try {
          md.setRating(Double.valueOf(r) / 10); // only 0-10
        }
        catch (Exception e2) {
          LOGGER.warn("cannot convert rating: " + r);
        }
      }

      // details page
      url = new CachedUrl(BASE_URL + "/filme/details.php3?id=" + id);
      in = url.getInputStream();
      doc = Jsoup.parse(in, PAGE_ENCODING, "");
      in.close();

      Element tab = doc.getElementById("ccdetails");
      int header = 0;
      String lastRole = "";
      for (Element tr : tab.getElementsByTag("tr")) {
        if (tr.toString().contains("dyngfx")) { // header gfx
          if (tr.toString().contains("Besetzung")) {
            header = 1;
          }
          else if (tr.toString().contains("Crew")) {
            header = 2;
          }
          else if (tr.toString().contains("Produktion")) {
            header = 3;
          }
          else if (tr.toString().contains("Verleih")) {
            header = 4;
          }
          else if (tr.toString().contains("Alternativtitel")) {
            header = 5;
          }
          continue;
        }
        else {
          // no header gfx, so data
          MediaCastMember mcm = new MediaCastMember();
          el = tr.getElementsByTag("td");
          if (header == 1) {
            // actors
            if (el.size() == 2) {
              mcm.setCharacter(el.get(0).text());
              mcm.setName(el.get(1).getElementsByTag("a").text());
              mcm.setId(StrgUtils.substr(el.get(1).getElementsByTag("a").attr("href"), "id=(\\d+)"));
              mcm.setType(MediaCastMember.CastType.ACTOR);
              // System.out.println("Cast: " + mcm.getCharacter() + " - " +
              // mcm.getName());
              md.addCastMember(mcm);
              // TODO: parse actor detail pages :/
            }
          }
          else if (header == 2) {
            // crew
            if (el.size() == 2) {
              String crewrole = el.get(0).html().trim();
              mcm.setName(el.get(1).getElementsByTag("a").text());
              if (crewrole.equals("&nbsp;")) {
                mcm.setPart(lastRole);
              }
              else {
                mcm.setPart(crewrole);
                lastRole = crewrole;
              }
              if (crewrole.equals("Regie")) {
                mcm.setType(MediaCastMember.CastType.DIRECTOR);
              }
              else if (crewrole.equals("Drehbuch")) {
                mcm.setType(MediaCastMember.CastType.WRITER);
              }
              else {
                mcm.setType(MediaCastMember.CastType.OTHER);
              }
              mcm.setId(StrgUtils.substr(el.get(1).getElementsByTag("a").attr("href"), "id=(\\d+)"));
              // System.out.println("Crew: " + mcm.getPart() + " - " +
              // mcm.getName());
              md.addCastMember(mcm);
            }
          }
          else if (header == 3) {
            // production
            md.setProductionCompany(el.get(0).text());
          }
        }
      }

      // get links page
      url = new CachedUrl(BASE_URL + "/filme/links.php3?id=" + id);
      in = url.getInputStream();
      doc = Jsoup.parse(in, PAGE_ENCODING, "");
      in.close();

      el = doc.getElementsByAttributeValueContaining("href", "german.imdb.com");
      if (el != null && el.size() > 0) {
        String imdb = StrgUtils.substr(el.get(0).attr("href"), "(tt\\d{7})");
        if (imdb.isEmpty()) {
          imdb = "tt" + StrgUtils.substr(el.get(0).attr("href"), "\\?(\\d+)");
        }
        md.setImdbId(imdb);
      }
    }
    catch (IOException e) {
      LOGGER.error("Error parsing " + options.getResult().getUrl());
      throw e;
    }

    return md;
  }

  /**
   * Removes all weird characters from search as well some "stopwords" as der|die|das|the|a.
   * 
   * @param q
   *          the query string to clean
   * @return the string
   */
  private String cleanSearch(String q) {
    q = " " + q.toLowerCase() + " "; // easier regex
    // TODO: doppelte hintereinander funzen so nicht
    q = q.replaceAll("(?i)( a | the | der | die | das |\\(\\d+\\))", " ");
    q = q.replaceAll("[^A-Za-z0-9äöüÄÖÜ ]", " ");
    q = q.replaceAll("  ", "");
    return q.trim();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaMetadataProvider#search(org.tinymediamanager .scraper.MediaSearchOptions)
   */
  @Override
  public List<MediaSearchResult> search(MediaSearchOptions options) throws Exception {
    LOGGER.debug("search() " + options.toString());
    List<MediaSearchResult> resultList = new ArrayList<MediaSearchResult>();
    String searchUrl = "";
    String searchTerm = "";
    String imdb = "";

    // only title search
    if (StringUtils.isNotEmpty(options.get(MediaSearchOptions.SearchParam.TITLE))) {
      searchTerm = cleanSearch(options.get(MediaSearchOptions.SearchParam.TITLE));
      searchUrl = BASE_URL + "/suche/index.php3?qstring=" + URLEncoder.encode(searchTerm, "UTF-8");
      LOGGER.debug("search with title: " + searchTerm);
    }
    else if (StringUtils.isNotEmpty(options.get(MediaSearchOptions.SearchParam.QUERY))) {
      searchTerm = cleanSearch(options.get(MediaSearchOptions.SearchParam.QUERY));
      searchUrl = BASE_URL + "/suche/index.php3?qstring=" + URLEncoder.encode(searchTerm, "UTF-8");
      LOGGER.debug("search for everything: " + searchTerm);
    }
    else {
      LOGGER.debug("empty searchString");
      return resultList;
    }

    searchTerm = MetadataUtil.removeNonSearchCharacters(searchTerm);

    Url url = new CachedUrl(searchUrl);
    InputStream in = url.getInputStream();
    Document doc = Jsoup.parse(in, PAGE_ENCODING, "");
    in.close();

    // only look for movie links
    Elements filme = doc.getElementsByAttributeValueStarting("href", "hit.php");
    LOGGER.debug("found " + filme.size() + " search results");
    if (filme == null || filme.isEmpty()) {
      if (!doc.getElementsByTag("title").text().contains("Suche nach")) {
        // redirected to detail page
        MediaSearchResult msr = new MediaSearchResult(providerInfo.getId());
        Elements el = doc.getElementsByAttributeValueStarting("href", "index.php3?id=");
        if (el.size() > 0) {
          msr.setId(StrgUtils.substr(el.get(0).attr("href"), "id=(\\d+)"));
        }
        msr.setTitle(StrgUtils.substr(doc.getElementsByTag("title").text(), "(.*?)\\|").trim());
        el = doc.getElementsByAttributeValueContaining("href", "az.php3?j=");
        if (el.size() == 1) {
          msr.setYear(el.get(0).text());
        }
        resultList.add(msr);
      }
      return resultList;
    }

    // <a
    // href="hit.php3?hit=d6900d7d9baf66ba77d8e59cc425da9e-movie-7614-17114331-1"
    // class="normLight">Avatar - Aufbruch nach Pandora</B>
    // <nobr>(2009)</nobr><br /><span class="smallLight"
    // style="color:#ccc;">Avatar</span></a>

    // map to merge 2 results :/
    Map<String, MediaSearchResult> res = new HashMap<String, MediaSearchResult>();

    for (Element a : filme) {
      try {
        String id = StrgUtils.substr(a.attr("href"), "-movie-(.*?)-");
        MediaSearchResult sr = new MediaSearchResult(providerInfo.getId());
        if (res.containsKey(id)) {
          LOGGER.debug("dupe found; merging with previous searchresult");
          sr = res.get(id);
        }

        if (StringUtils.isNotEmpty(imdb)) {
          sr.setIMDBId(imdb);
        }
        if (StringUtils.isEmpty(sr.getId())) {
          sr.setId(id);
        }
        if (StringUtils.isEmpty(sr.getTitle())) {
          if (a.html().contains("nobr")) {
            sr.setTitle(a.ownText());
          }
          else {
            sr.setTitle(a.text());
          }
        }
        LOGGER.debug("found movie " + sr.getTitle());
        if (StringUtils.isEmpty(sr.getOriginalTitle())) {
          sr.setOriginalTitle(a.getElementsByTag("span").text());
        }
        if (StringUtils.isEmpty(sr.getYear())) {
          sr.setYear(StrgUtils.substr(a.getElementsByTag("nobr").text(), ".*(\\d{4}).*")); // any
                                                                                           // 4
                                                                                           // digit
        }
        sr.setMediaType(MediaType.MOVIE);
        sr.setUrl(BASE_URL + "/filme/index.php3?id=" + id);
        // sr.setPosterUrl(BASE_URL + "/images" + StrgUtils.substr(a.toString(),
        // "images(.*?)\\&quot"));

        sr.setScore(MetadataUtil.calculateScore(searchTerm, sr.getTitle()));
        // populate extra args
        MetadataUtil.copySearchQueryToSearchResult(options, sr);
        res.put(id, sr);
      }
      catch (Exception e) {
        LOGGER.warn("error parsing movie result: " + e.getMessage());
      }
    }
    for (String r : res.keySet()) {
      resultList.add(res.get(r));
    }
    Collections.sort(resultList);
    Collections.reverse(resultList);
    return resultList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaTrailerProvider#getTrailers(org. tinymediamanager.scraper.MediaScrapeOptions)
   */
  @Override
  public List<MediaTrailer> getTrailers(MediaScrapeOptions options) throws Exception {
    // http://www.zelluloid.de/filme/trailer.php3?id=7614
    return null;
  }
}
