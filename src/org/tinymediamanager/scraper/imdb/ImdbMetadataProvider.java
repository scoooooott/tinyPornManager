/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.scraper.imdb;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaCastMember;
import org.tinymediamanager.scraper.MediaCastMember.CastType;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchOptions.SearchParam;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.MetadataUtil;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.scraper.util.CachedUrl;

/**
 * The Class ImdbMetadataProvider.
 */
public class ImdbMetadataProvider implements IMediaMetadataProvider {

  /** The Constant LOGGER. */
  private static final Logger      LOGGER       = Logger.getLogger(ImdbMetadataProvider.class);

  /** The imdb site. */
  private ImdbSiteDefinition       imdbSite;

  /** The provider info. */
  private static MediaProviderInfo providerInfo = new MediaProviderInfo("imdb", "imdb.com", "Scraper for imdb which is able to scrape movie metadata");

  /**
   * Instantiates a new imdb metadata provider.
   */
  public ImdbMetadataProvider() {
    imdbSite = ImdbSiteDefinition.IMDB_COM;
  }

  /**
   * Instantiates a new imdb metadata provider.
   * 
   * @param site
   *          the site
   */
  public ImdbMetadataProvider(ImdbSiteDefinition site) {
    imdbSite = site;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaMetadataProvider#getInfo()
   */
  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public MediaMetadata getMetadata(MediaScrapeOptions scrapeOptions) throws Exception {
    // check if there is a md in the result
    if (scrapeOptions.getResult() != null && scrapeOptions.getResult().getMetadata() != null) {
      LOGGER.debug("IMDB: getMetadata from cache: " + scrapeOptions.getResult());
      return scrapeOptions.getResult().getMetadata();
    }

    MediaMetadata md = new MediaMetadata(providerInfo.getId());
    String imdbId = "";

    // imdbId from searchResult
    if (scrapeOptions.getResult() != null) {
      imdbId = scrapeOptions.getResult().getIMDBId();
    }

    // imdbid from scraper option
    if (!MetadataUtil.isValidImdbId(imdbId)) {
      imdbId = scrapeOptions.getImdbId();
    }

    if (!MetadataUtil.isValidImdbId(imdbId)) {
      return md;
    }

    LOGGER.debug("IMDB: getMetadata(imdbId): " + imdbId);
    md.setImdbId(imdbId);

    ExecutorCompletionService<Document> compSvcImdb = new ExecutorCompletionService<Document>(Globals.executor);
    ExecutorCompletionService<MediaMetadata> compSvcTmdb = new ExecutorCompletionService<MediaMetadata>(Globals.executor);
    ExecutorCompletionService<List<MediaArtwork>> compSvcTmdbArtwork = new ExecutorCompletionService<List<MediaArtwork>>(Globals.executor);
    ExecutorCompletionService<List<MediaTrailer>> compSvcTmdbTrailer = new ExecutorCompletionService<List<MediaTrailer>>(Globals.executor);

    // worker for imdb request (/combined) (everytime from akas.imdb.com)
    // StringBuilder sb = new StringBuilder(imdbSite.getSite());
    StringBuilder sb = new StringBuilder(ImdbSiteDefinition.IMDB_COM.getSite());
    sb.append("title/");
    sb.append(imdbId);
    sb.append("/combined");
    Callable<Document> worker = new ImdbWorker(sb.toString());
    Future<Document> futureCombined = compSvcImdb.submit(worker);

    // worker for imdb request (/plotsummary) (from chosen site)
    Future<Document> futurePlotsummary = null;
    if (!Globals.settings.isImdbScrapeForeignLanguage()) {
      sb = new StringBuilder(imdbSite.getSite());
      sb.append("title/");
      sb.append(imdbId);
      sb.append("/plotsummary");

      worker = new ImdbWorker(sb.toString());
      futurePlotsummary = compSvcImdb.submit(worker);
    }

    // worker for tmdb request
    Future<MediaMetadata> futureTmdb = null;
    if (Globals.settings.isImdbScrapeForeignLanguage()) {
      Callable<MediaMetadata> worker2 = new TmdbWorker(imdbId);
      // futureTmdb = executor.submit(worker2);
      futureTmdb = compSvcTmdb.submit(worker2);
    }

    // // worker for artwork
    // Callable<List<MediaArtwork>> workerArtwork = new
    // TmdbArtworkWorker(imdbId);
    // Future<List<MediaArtwork>> futureArtwork =
    // compSvcTmdbArtwork.submit(workerArtwork);
    //
    // // worker for fanart
    // Callable<List<MediaTrailer>> workerTrailer = new
    // TmdbTrailerWorker(imdbId);
    // Future<List<MediaTrailer>> futureTrailer =
    // compSvcTmdbTrailer.submit(workerTrailer);

    Document doc;
    doc = futureCombined.get();

    /*
     * title and year have the following structure
     * 
     * <div id="tn15title"><h1>Merida - Legende der Highlands <span>(<a
     * href="/year/2012/">2012</a>) <span class="pro-link">...</span> <span
     * class="title-extra">Brave <i>(original title)</i></span> </span></h1>
     * </div>
     */

    // parse title and year
    Element title = doc.getElementById("tn15title");
    if (title != null) {
      Element element = null;
      // title
      Elements elements = title.getElementsByTag("h1");
      if (elements.size() > 0) {
        element = elements.first();
        String movieTitle = cleanString(element.ownText());
        md.setTitle(movieTitle);
      }

      // year and original title
      elements = title.getElementsByTag("span");
      if (elements.size() > 0) {
        element = elements.first();
        String content = element.text();

        // search year
        Pattern yearPattern = Pattern.compile("\\(([0-9]{4})|/\\)");
        Matcher matcher = yearPattern.matcher(element.text());
        while (matcher.find()) {
          if (matcher.group(1) != null) {
            String movieYear = matcher.group(1);
            md.setYear(movieYear);
            break;
          }
        }

        // original title
        // if (imdbSite == ImdbSiteDefinition.IMDB_COM) {
        // original title = title
        md.setOriginalTitle(md.getTitle());
        // } else {
        // // try to parse the title out of "title-extra"
        // Elements span = element.getElementsByClass("title-extra");
        // if (span.size() > 0) {
        // Element titleExtra = span.first();
        // String originalTitle = titleExtra.ownText();
        // if (!StringUtils.isEmpty(originalTitle)) {
        // md.setOriginalTitle(originalTitle);
        // }
        // }
        // }
      }

    }

    // poster
    Element poster = doc.getElementById("primary-poster");
    if (poster != null) {
      String posterUrl = poster.attr("src");
      posterUrl = posterUrl.replaceAll("SX[0-9]{2,4}_", "SX195_");
      posterUrl = posterUrl.replaceAll("SY[0-9]{2,4}_", "SY195_");
      processMediaArt(md, MediaArtworkType.POSTER, "Poster", posterUrl);
    }

    /*
     * <div class="starbar-meta"> <b>7.4/10</b> &nbsp;&nbsp;<a href="ratings"
     * class="tn15more">52,871 votes</a>&nbsp;&raquo; </div>
     */

    // rating and rating count
    Element ratingElement = doc.getElementById("tn15rating");
    if (ratingElement != null) {
      Elements elements = ratingElement.getElementsByClass("starbar-meta");
      if (elements.size() > 0) {
        Element div = elements.get(0);

        // rating comes in <b> tag
        Elements b = div.getElementsByTag("b");
        if (b.size() == 1) {
          String ratingAsString = b.text();
          Pattern ratingPattern = Pattern.compile("([0-9]\\.[0-9])/10");
          Matcher matcher = ratingPattern.matcher(ratingAsString);
          while (matcher.find()) {
            if (matcher.group(1) != null) {
              float rating = 0;
              try {
                rating = Float.valueOf(matcher.group(1));
              }
              catch (Exception e) {
              }
              md.setRating(rating);
              break;
            }
          }
        }

        // count
        Elements a = div.getElementsByAttributeValue("href", "ratings");
        if (a.size() == 1) {
          String countAsString = a.text().replaceAll("[.,]|votes", "").trim();
          int voteCount = 0;
          try {
            voteCount = Integer.parseInt(countAsString);
          }
          catch (Exception e) {
          }
          md.setVoteCount(voteCount);
        }
      }

      // // top250
      // elements = ratingElement.getElementsByClass("starbar-special");
      // if (elements.size() > 0) {
      // Elements a = elements.get(0).getElementsByTag("a");
      // if(a.size() > 0){
      // Element anchor = a.get(0);
      // Pattern topPattern = Pattern.compile("Top 250: #([0-9]{1,3})");
      // Matcher matcher = topPattern.matcher(anchor.ownText());
      // while(matcher.find()){
      // if(matcher.group(1) != null){
      // int top250 = 0;
      // try{
      // top250 = Integer.parseInt(matcher.group(1));
      // } catch(Exception e){
      // }
      // // TODO
      // }
      // }
      // }
      // }
    }

    // parse all items coming by <div class="info">
    Elements elements = doc.getElementsByClass("info");
    for (Element element : elements) {
      // only parse divs
      if (!"div".equals(element.tag().getName())) {
        continue;
      }

      // elements with h5 are the titles of the values
      Elements h5 = element.getElementsByTag("h5");
      if (h5.size() > 0) {
        Element firstH5 = h5.first();
        String h5Title = firstH5.text();

        /*
         * <div class="info"><h5>Tagline:</h5><div class="info-content"> (7) To
         * Defend Us... <a class="tn15more inline"
         * href="/title/tt0472033/taglines" onClick=
         * "(new Image()).src='/rg/title-tease/taglines/images/b.gif?link=/title/tt0472033/taglines';"
         * >See more</a>&nbsp;&raquo; </div></div>
         */
        // tagline
        // if (h5Title.matches("(?i)" + imdbSite.getTagline() + ".*") &&
        // !Globals.settings.isImdbScrapeForeignLanguage()) {
        if (h5Title.matches("(?i)" + ImdbSiteDefinition.IMDB_COM.getTagline() + ".*") && !Globals.settings.isImdbScrapeForeignLanguage()) {
          Elements div = element.getElementsByClass("info-content");
          if (div.size() > 0) {
            Element taglineElement = div.first();
            String tagline = cleanString(taglineElement.ownText().replaceAll("»", ""));
            md.setTagline(tagline);
          }
        }

        /*
         * <div class="info-content"><a
         * href="/Sections/Genres/Animation/">Animation</a> | <a
         * href="/Sections/Genres/Action/">Action</a> | <a
         * href="/Sections/Genres/Adventure/">Adventure</a> | <a
         * href="/Sections/Genres/Fantasy/">Fantasy</a> | <a
         * href="/Sections/Genres/Mystery/">Mystery</a> | <a
         * href="/Sections/Genres/Sci-Fi/">Sci-Fi</a> | <a
         * href="/Sections/Genres/Thriller/">Thriller</a> <a
         * class="tn15more inline" href="/title/tt0472033/keywords" onClick=
         * "(new Image()).src='/rg/title-tease/keywords/images/b.gif?link=/title/tt0472033/keywords';"
         * > See more</a>&nbsp;&raquo; </div>
         */
        // genres are only scraped from akas.imdb.com
        // if (imdbSite == ImdbSiteDefinition.IMDB_COM) {
        if (h5Title.matches("(?i)" + imdbSite.getGenre() + "(.*)")) {
          Elements div = element.getElementsByClass("info-content");
          if (div.size() > 0) {
            Elements a = div.first().getElementsByTag("a");
            for (Element anchor : a) {
              if (anchor.attr("href").matches("/Sections/Genres/.*")) {
                MediaGenres genre = MediaGenres.getGenre(anchor.ownText());
                if (genre != null && !md.getGenres().contains(genre)) {
                  md.addGenre(genre);
                }
              }
            }
          }
        }
        // }

        /*
         * <div class="info"><h5>Runtime:</h5><div class="info-content">129 min
         * </div></div>
         */
        // runtime
        // if (h5Title.matches("(?i)" + imdbSite.getRuntime() + ".*")) {
        if (h5Title.matches("(?i)" + ImdbSiteDefinition.IMDB_COM.getRuntime() + ".*")) {
          Elements div = element.getElementsByClass("info-content");
          if (div.size() > 0) {
            Element taglineElement = div.first();
            String runtimeAsString = cleanString(taglineElement.ownText().replaceAll("min", ""));
            int runtime = 0;
            try {
              runtime = Integer.parseInt(runtimeAsString);
            }
            catch (Exception e) {
            }
            md.setRuntime(runtime);
          }
        }

        /*
         * <div class="info"> <h5>Writers:</h5> <div class="info-content"> <a
         * href="/name/nm0152312/" onclick=
         * "(new Image()).src='/rg/writerlist/position-1/images/b.gif?link=name/nm0152312/';"
         * >Brenda Chapman</a> (story)<br/> <a href="/name/nm0028764/" onclick=
         * "(new Image()).src='/rg/writerlist/position-2/images/b.gif?link=name/nm0028764/';"
         * >Mark Andrews</a> (screenplay) ...<br/> <a
         * href="fullcredits#writers">(more)</a> </div> </div>
         */
        // writer
        // if (h5Title.matches("(?i)" + imdbSite.getWriter() + ".*")) {
        if (h5Title.matches("(?i)" + ImdbSiteDefinition.IMDB_COM.getWriter() + ".*")) {
          Elements a = element.getElementsByTag("a");
          for (Element anchor : a) {
            if (anchor.attr("href").matches("/name/nm.*")) {
              MediaCastMember cm = new MediaCastMember(CastType.WRITER);
              cm.setName(anchor.ownText());
              md.addCastMember(cm);
            }
          }
        }

        /*
         * <div class="info"><h5>Certification:</h5><div class="info-content"><a
         * href="/search/title?certificates=us:pg">USA:PG</a> <i>(certificate
         * #47489)</i> | <a
         * href="/search/title?certificates=ca:pg">Canada:PG</a>
         * <i>(Ontario)</i> | <a
         * href="/search/title?certificates=au:pg">Australia:PG</a> | <a
         * href="/search/title?certificates=in:u">India:U</a> | <a
         * href="/search/title?certificates=ie:pg">Ireland:PG</a>
         * ...</div></div>
         */
        // certification
        // if (h5Title.matches("(?i)" + imdbSite.getCertification() + ".*")) {
        if (h5Title.matches("(?i)" + ImdbSiteDefinition.IMDB_COM.getCertification() + ".*")) {
          Elements a = element.getElementsByTag("a");
          for (Element anchor : a) {
            // certification for the right country
            if (anchor.attr("href").matches("(?i)/search/title\\?certificates=" + Globals.settings.getCertificationCountry().getAlpha2() + ".*")) {
              Pattern certificationPattern = Pattern.compile(".*:(.*)");
              Matcher matcher = certificationPattern.matcher(anchor.ownText());
              Certification certification = null;
              while (matcher.find()) {
                if (matcher.group(1) != null) {
                  certification = Certification.getCertification(Globals.settings.getCertificationCountry(), matcher.group(1));
                }
              }

              if (certification != null) {
                md.addCertification(certification);
                break;
              }
            }
          }
        }
      }

      /*
       * <div id="director-info" class="info"> <h5>Director:</h5> <div
       * class="info-content"><a href="/name/nm0000416/" onclick=
       * "(new Image()).src='/rg/directorlist/position-1/images/b.gif?link=name/nm0000416/';"
       * >Terry Gilliam</a><br/> </div> </div>
       */
      // director
      if ("director-info".equals(element.id())) {
        Elements a = element.getElementsByTag("a");
        for (Element anchor : a) {
          if (anchor.attr("href").matches("/name/nm.*")) {
            MediaCastMember cm = new MediaCastMember(CastType.DIRECTOR);
            cm.setName(anchor.ownText());
            md.addCastMember(cm);
          }
        }
      }
    }

    /*
     * <table class="cast"> <tr class="odd"><td class="hs"><a
     * href="http://pro.imdb.com/widget/resume_redirect/" onClick=
     * "(new Image()).src='/rg/resume/prosystem/images/b.gif?link=http://pro.imdb.com/widget/resume_redirect/';"
     * ><img src=
     * "http://i.media-imdb.com/images/SF9113d6f5b7cb1533c35313ccd181a6b1/tn15/no_photo.png"
     * width="25" height="31" border="0"></td><td class="nm"><a
     * href="/name/nm0577828/" onclick=
     * "(new Image()).src='/rg/castlist/position-1/images/b.gif?link=/name/nm0577828/';"
     * >Joseph Melito</a></td><td class="ddd"> ... </td><td class="char"><a
     * href="/character/ch0003139/">Young Cole</a></td></tr> <tr
     * class="even"><td class="hs"><a href="/name/nm0000246/" onClick=
     * "(new Image()).src='/rg/title-tease/tinyhead/images/b.gif?link=/name/nm0000246/';"
     * ><img src=
     * "http://ia.media-imdb.com/images/M/MV5BMjA0MjMzMTE5OF5BMl5BanBnXkFtZTcwMzQ2ODE3Mw@@._V1._SY30_SX23_.jpg"
     * width="23" height="32" border="0"></a><br></td><td class="nm"><a
     * href="/name/nm0000246/" onclick=
     * "(new Image()).src='/rg/castlist/position-2/images/b.gif?link=/name/nm0000246/';"
     * >Bruce Willis</a></td><td class="ddd"> ... </td><td class="char"><a
     * href="/character/ch0003139/">James Cole</a></td></tr> <tr class="odd"><td
     * class="hs"><a href="/name/nm0781218/" onClick=
     * "(new Image()).src='/rg/title-tease/tinyhead/images/b.gif?link=/name/nm0781218/';"
     * ><img src=
     * "http://ia.media-imdb.com/images/M/MV5BODI1MTA2MjkxM15BMl5BanBnXkFtZTcwMTcwMDg2Nw@@._V1._SY30_SX23_.jpg"
     * width="23" height="32" border="0"></a><br></td><td class="nm"><a
     * href="/name/nm0781218/" onclick=
     * "(new Image()).src='/rg/castlist/position-3/images/b.gif?link=/name/nm0781218/';"
     * >Jon Seda</a></td><td class="ddd"> ... </td><td class="char"><a
     * href="/character/ch0003143/">Jose</a></td></tr>...</table>
     */
    // cast
    elements = doc.getElementsByClass("cast");
    if (elements.size() > 0) {
      Elements tr = elements.get(0).getElementsByTag("tr");
      for (Element row : tr) {
        Elements td = row.getElementsByTag("td");
        MediaCastMember cm = new MediaCastMember();
        for (Element column : td) {
          // actor thumb
          if (column.hasClass("hs")) {
            Elements img = column.getElementsByTag("img");
            if (img.size() > 0) {
              String thumbUrl = img.get(0).attr("src");
              if (thumbUrl.contains("no_photo.png")) {
                cm.setImageUrl("");
              }
              else {
                thumbUrl = thumbUrl.replaceAll("SX[0-9]{2,4}_", "SX100_");
                thumbUrl = thumbUrl.replaceAll("SY[0-9]{2,4}_", "SY125_");
                cm.setImageUrl(thumbUrl);
              }
            }
          }
          // actor name
          if (column.hasClass("nm")) {
            cm.setName(cleanString(column.text()));
          }
          // character
          if (column.hasClass("char")) {
            cm.setCharacter(cleanString(column.text()));
          }
        }
        if (StringUtils.isNotEmpty(cm.getName()) && StringUtils.isNotEmpty(cm.getCharacter())) {
          cm.setType(CastType.ACTOR);
          md.addCastMember(cm);
        }
      }
    }

    // Production companies
    elements = doc.getElementsByClass("blackcatheader");
    for (Element blackcatheader : elements) {
      // if (blackcatheader.ownText().equals(imdbSite.getProductionCompanies()))
      // {
      if (blackcatheader.ownText().equals(ImdbSiteDefinition.IMDB_COM.getProductionCompanies())) {
        Elements a = blackcatheader.nextElementSibling().getElementsByTag("a");
        StringBuilder productionCompanies = new StringBuilder();
        for (Element anchor : a) {
          if (StringUtils.isNotEmpty(productionCompanies)) {
            productionCompanies.append(", ");
          }
          productionCompanies.append(anchor.ownText());
        }
        md.setProductionCompany(productionCompanies.toString());
        break;
      }
    }

    /*
     * plot from /plotsummary
     */
    // build the url
    if (!Globals.settings.isImdbScrapeForeignLanguage()) {
      doc = null;
      doc = futurePlotsummary.get();

      // imdb.com has another site structure
      if (imdbSite == ImdbSiteDefinition.IMDB_COM) {
        Elements plotpar = doc.getElementsByClass("plotpar");
        if (plotpar.size() > 0) {
          String plot = cleanString(plotpar.get(0).ownText());
          md.setPlot(plot);
        }
      }
      else {
        Element wiki = doc.getElementById("swiki.2.1");
        if (wiki != null) {
          String plot = cleanString(wiki.ownText());
          md.setPlot(plot);
        }
      }

      // title also from chosen site if we are not scraping akas.imdb.com
      if (imdbSite != ImdbSiteDefinition.IMDB_COM) {
        title = doc.getElementById("tn15title");
        if (title != null) {
          Element element = null;
          // title
          elements = title.getElementsByClass("main");
          if (elements.size() > 0) {
            element = elements.first();
            String movieTitle = cleanString(element.ownText());
            md.setTitle(movieTitle);
          }
        }
        md.setTagline("");
      }
    }

    // get data from tmdb?
    if (Globals.settings.isImdbScrapeForeignLanguage()) {
      MediaMetadata tmdbMd = futureTmdb.get();
      if (tmdbMd != null) {
        // title
        md.setTitle(tmdbMd.getTitle());
        // tagline
        md.setTagline(tmdbMd.getTagline());
        // plot
        md.setPlot(tmdbMd.getPlot());
      }
    }

    // // get artwork from TMDB
    // try {
    // List<MediaArtwork> mediaArt = futureArtwork.get();
    // if (mediaArt != null && mediaArt.size() > 0) {
    // md.clearMediaArt();
    // md.addMediaArt(mediaArt);
    //
    // // also store tmdbId
    // if (md.getTmdbId() == 0) {
    // md.setTmdbId(mediaArt.get(0).getTmdbId());
    // }
    // }
    // }
    // catch (Exception e) {
    // }
    //
    // // get trailer from tmdb
    // try {
    // List<MediaTrailer> trailers = futureTrailer.get();
    // if (trailers != null && trailers.size() > 0) {
    // for (MediaTrailer trailer : trailers) {
    // md.addTrailer(trailer);
    // }
    // }
    // }
    // catch (Exception e) {
    // }

    return md;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.tinymediamanager.scraper.IMediaMetadataProvider#search(org.tinymediamanager
   * .scraper.SearchQuery)
   */
  /**
   * Search.
   * 
   * @param query
   *          the query
   * @return the list
   * @throws Exception
   *           the exception
   */
  @Override
  public List<MediaSearchResult> search(MediaSearchOptions query) throws Exception {
    /*
     * IMDb matches seem to come in several "flavours".
     * 
     * Firstly, if there is one exact match it returns the matching IMDb page.
     * 
     * If that fails to produce a unique hit then a list of possible matches are
     * returned categorised as: Popular Titles (Displaying ? Results) Titles
     * (Exact Matches) (Displaying ? Results) Titles (Partial Matches)
     * (Displaying ? Results)
     * 
     * We should check the Exact match section first, then the poplar titles and
     * finally the partial matches.
     * 
     * Note: That even with exact matches there can be more than 1 hit, for
     * example "Star Trek"
     */

    Pattern imdbIdPattern = Pattern.compile("/title/(tt[0-9]{7})/");

    List<MediaSearchResult> result = new ArrayList<MediaSearchResult>();

    String searchTerm = "";

    if (StringUtils.isNotEmpty(query.get(SearchParam.IMDBID))) {
      searchTerm = query.get(SearchParam.IMDBID);
    }

    if (StringUtils.isEmpty(searchTerm)) {
      searchTerm = query.get(SearchParam.QUERY);
    }

    if (StringUtils.isEmpty(searchTerm)) {
      return result;
    }

    StringBuilder sb = new StringBuilder(imdbSite.getSite());
    sb.append("find?q=");
    try {
      // search site was everytime in UTF-8
      // sb.append(URLEncoder.encode(searchTerm,
      // imdbSite.getCharset().displayName()));
      sb.append(URLEncoder.encode(searchTerm, "UTF-8"));
    }
    catch (UnsupportedEncodingException ex) {
      // Failed to encode the movie name for some reason!
      LOGGER.debug("Failed to encode search term: " + searchTerm);
      sb.append(searchTerm);
    }

    sb.append("&s=tt");

    LOGGER.debug("========= BEGIN IMDB Scraper Search for: " + sb.toString());
    Document doc;
    try {
      CachedUrl url = new CachedUrl(sb.toString());

      // doc = Jsoup.parse(url.getInputStream(),
      // imdbSite.getCharset().displayName(), "");
      doc = Jsoup.parse(url.getInputStream(), "UTF-8", "");
    }
    catch (Exception e) {
      LOGGER.debug("tried to fetch search response", e);
      return result;
    }

    // check if it was directly redirected to the site
    Elements elements = doc.getElementsByAttributeValue("rel", "canonical");
    for (Element element : elements) {
      MediaMetadata md = null;
      // we have been redirected to the movie site
      String movieName = null;
      String movieId = null;

      String href = element.attr("href");
      Matcher matcher = imdbIdPattern.matcher(href);
      while (matcher.find()) {
        if (matcher.group(1) != null) {
          movieId = matcher.group(1);
        }
      }

      // get full information
      if (!StringUtils.isEmpty(movieId)) {
        MediaScrapeOptions options = new MediaScrapeOptions();
        options.setImdbId(movieId);
        md = getMetadata(options);
        if (!StringUtils.isEmpty(md.getTitle())) {
          movieName = md.getTitle();
        }
      }

      // if no movie name/id was found - continue
      if (StringUtils.isEmpty(movieName) || StringUtils.isEmpty(movieId)) {
        continue;
      }

      MediaSearchResult sr = new MediaSearchResult(providerInfo.getId());
      sr.setTitle(movieName);
      sr.setIMDBId(movieId);
      sr.setYear(md.getYear());
      sr.setMetadata(md);
      result.add(sr);

      return result;
    }

    // parse results
    // elements = doc.getElementsByClass("result_text");
    elements = doc.getElementsByClass("findResult");
    for (Element tr : elements) {
      // we only want the tr's
      if (!"tr".equalsIgnoreCase(tr.tagName())) {
        continue;
      }

      // find the id / name
      String movieName = "";
      String movieId = "";
      String year = "";
      Elements tds = tr.getElementsByClass("result_text");
      for (Element element : tds) {
        // we only want the td's
        if (!"td".equalsIgnoreCase(element.tagName())) {
          continue;
        }

        // get the name inside the link
        Elements anchors = element.getElementsByTag("a");
        for (Element a : anchors) {
          if (StringUtils.isEmpty(a.text())) {
            continue;
          }

          // movie name
          movieName = a.text();

          // parse id
          String href = a.attr("href");
          Matcher matcher = imdbIdPattern.matcher(href);
          while (matcher.find()) {
            if (matcher.group(1) != null) {
              movieId = matcher.group(1);
            }
          }

          // try to parse out the year
          Pattern yearPattern = Pattern.compile("\\(([0-9]{4})|/\\)");
          matcher = yearPattern.matcher(element.text());
          while (matcher.find()) {
            if (matcher.group(1) != null) {
              year = matcher.group(1);
              break;
            }
          }

          break;
        }
      }

      // if an id/name was found - parse the poster image
      String posterUrl = "";
      tds = tr.getElementsByClass("primary_photo");
      for (Element element : tds) {
        Elements imgs = element.getElementsByTag("img");
        for (Element img : imgs) {
          posterUrl = img.attr("src");
          posterUrl = posterUrl.replaceAll("SX[0-9]{2,4}_", "SY195_");
          posterUrl = posterUrl.replaceAll("CR[0-9]{1,3},[0-9]{1,3},[0-9]{1,3},[0-9]{1,3}_", "");
        }
      }

      // if no movie name/id was found - continue
      if (StringUtils.isEmpty(movieName) || StringUtils.isEmpty(movieId)) {
        continue;
      }

      MediaSearchResult sr = new MediaSearchResult(providerInfo.getId());
      sr.setTitle(movieName);
      sr.setIMDBId(movieId);
      sr.setYear(year);
      sr.setPosterUrl(posterUrl);

      // populate extra args
      MetadataUtil.copySearchQueryToSearchResult(query, sr);

      sr.setScore(MetadataUtil.calculateScore(searchTerm, movieName));

      result.add(sr);

      // only get 20 results
      if (result.size() >= 20) {
        break;
      }
    }

    return result;
  }

  /**
   * Process media art.
   * 
   * @param md
   *          the md
   * @param type
   *          the type
   * @param label
   *          the label
   * @param image
   *          the image
   */
  private void processMediaArt(MediaMetadata md, MediaArtworkType type, String label, String image) {
    MediaArtwork ma = new MediaArtwork();
    ma.setDownloadUrl(image);
    ma.setLabel(label);
    // ma.setProviderId(getInfo().getId());
    ma.setType(type);
    md.addMediaArt(ma);
  }

  /**
   * Clean string.
   * 
   * @param oldString
   *          the old string
   * @return the string
   */
  private String cleanString(String oldString) {
    if (StringUtils.isEmpty(oldString)) {
      return "";
    }
    // remove non breaking spaces
    String newString = oldString.replace(String.valueOf((char) 160), " ");
    // and trim
    return StringUtils.trim(newString);
  }

  /**
   * The Class ImdbWorker.
   */
  private class ImdbWorker implements Callable<Document> {

    /** The url. */
    private String   url;

    /** The doc. */
    private Document doc = null;

    /**
     * Instantiates a new imdb worker.
     * 
     * @param url
     *          the url
     */
    public ImdbWorker(String url) {
      this.url = url;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Document call() throws Exception {
      doc = null;
      try {
        CachedUrl cachedUrl = new CachedUrl(url);
        doc = Jsoup.parse(cachedUrl.getInputStream(), imdbSite.getCharset().displayName(), "");
      }
      catch (Exception e) {
        LOGGER.debug("tried to fetch imdb movie page " + url, e);
      }
      return doc;
    }
  }

  /**
   * The Class TmdbWorker.
   */
  private class TmdbWorker implements Callable<MediaMetadata> {

    /** The imdb id. */
    private String imdbId;

    /**
     * Instantiates a new tmdb worker.
     * 
     * @param imdbId
     *          the imdb id
     */
    public TmdbWorker(String imdbId) {
      this.imdbId = imdbId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public MediaMetadata call() throws Exception {
      try {
        TmdbMetadataProvider tmdb = new TmdbMetadataProvider();
        MediaScrapeOptions options = new MediaScrapeOptions();
        options.setImdbId(imdbId);
        return tmdb.getMetadata(options);
      }
      catch (Exception e) {
        return null;
      }
    }
  }

  // /**
  // * The Class TmdbArtworkWorker.
  // */
  // private class TmdbArtworkWorker implements Callable<List<MediaArtwork>> {
  //
  // /** The imdb id. */
  // private String imdbId;
  //
  // /**
  // * Instantiates a new tmdb artwork worker.
  // *
  // * @param imdbId
  // * the imdb id
  // */
  // public TmdbArtworkWorker(String imdbId) {
  // this.imdbId = imdbId;
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see java.util.concurrent.Callable#call()
  // */
  // @Override
  // public List<MediaArtwork> call() throws Exception {
  // TmdbMetadataProvider tmdbMd = new TmdbMetadataProvider();
  // MediaScrapeOptions options = new MediaScrapeOptions();
  // options.setArtworkType(MediaArtworkType.ALL);
  // options.setImdbId(imdbId);
  // return tmdbMd.getArtwork(options);
  // }
  // }
  //
  // /**
  // * The Class TmdbTrailerWorker.
  // */
  // private class TmdbTrailerWorker implements Callable<List<MediaTrailer>> {
  //
  // /** The imdb id. */
  // private String imdbId;
  //
  // /**
  // * Instantiates a new tmdb artwork worker.
  // *
  // * @param imdbId
  // * the imdb id
  // */
  // public TmdbTrailerWorker(String imdbId) {
  // this.imdbId = imdbId;
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see java.util.concurrent.Callable#call()
  // */
  // @Override
  // public List<MediaTrailer> call() throws Exception {
  // TmdbMetadataProvider tmdbMd = new TmdbMetadataProvider();
  // MediaScrapeOptions options = new MediaScrapeOptions();
  // options.setImdbId(imdbId);
  // return tmdbMd.getTrailers(options);
  // }
  // }
}