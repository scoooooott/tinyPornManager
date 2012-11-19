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
import org.tinymediamanager.scraper.CastMember;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.IHasFindByIMDBID;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.MediaArt;
import org.tinymediamanager.scraper.MediaArtifactType;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.MetadataUtil;
import org.tinymediamanager.scraper.ProviderInfo;
import org.tinymediamanager.scraper.SearchQuery;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.scraper.util.CachedUrl;

// TODO: Auto-generated Javadoc
/**
 * The Class ImdbMetadataProvider.
 */
public class ImdbMetadataProvider implements IMediaMetadataProvider, IHasFindByIMDBID {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = Logger.getLogger(ImdbMetadataProvider.class);

  /** The imdb site. */
  private ImdbSiteDefinition  imdbSite;

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
   * @see
   * org.tinymediamanager.scraper.IHasFindByIMDBID#getMetadataForIMDBId(java
   * .lang.String)
   */
  /**
   * Gets the metadata for imdb id.
   * 
   * @param imdbId
   *          the imdb id
   * @return the metadata for imdb id
   * @throws Exception
   *           the exception
   */
  @Override
  public MediaMetadata getMetadataForIMDBId(String imdbId) throws Exception {
    LOGGER.debug("IMDB: getMetadata(imdbId): " + imdbId);

    MediaMetadata md = new MediaMetadata();
    md.setIMDBID(imdbId);

    ExecutorCompletionService<Document> compSvcImdb = new ExecutorCompletionService<Document>(Globals.executor);
    ExecutorCompletionService<MediaMetadata> compSvcTmdb = new ExecutorCompletionService<MediaMetadata>(Globals.executor);
    ExecutorCompletionService<List<MediaArt>> compSvcTmdbArtwork = new ExecutorCompletionService<List<MediaArt>>(Globals.executor);

    // worker for imdb request (/combined)
    StringBuilder sb = new StringBuilder(imdbSite.getSite());
    sb.append("title/");
    sb.append(imdbId);
    sb.append("/combined");
    Callable<Document> worker = new ImdbWorker(sb.toString());
    // Future<Document> futureCombined = executor.submit(worker);
    Future<Document> futureCombined = compSvcImdb.submit(worker);

    // worker for imdb request (/plotsummary)
    Future<Document> futurePlotsummary = null;
    if (!Globals.settings.isImdbScrapeForeignLanguage()) {
      sb = new StringBuilder(imdbSite.getSite());
      sb.append("title/");
      sb.append(imdbId);
      sb.append("/plotsummary");

      worker = new ImdbWorker(sb.toString());
      // futurePlotsummary = executor.submit(worker);
      futurePlotsummary = compSvcImdb.submit(worker);
    }

    // worker for tmdb request
    Future<MediaMetadata> futureTmdb = null;
    if (Globals.settings.isImdbScrapeForeignLanguage()) {
      Callable<MediaMetadata> worker2 = new TmdbWorker(imdbId);
      // futureTmdb = executor.submit(worker2);
      futureTmdb = compSvcTmdb.submit(worker2);
    }

    // worker for artwork
    Callable<List<MediaArt>> workerArtwork = new TmdbArtworkWorker(imdbId);
    Future<List<MediaArt>> futureArtwork = compSvcTmdbArtwork.submit(workerArtwork);

    Document doc;
    doc = futureCombined.get();
    // try {
    // CachedUrl url = new CachedUrl(sb.toString());
    // doc = Jsoup.parse(url.getInputStream(),
    // imdbSite.getCharset().displayName(), "");
    // } catch (Exception e) {
    // LOGGER.debug("tried to fetch imdb movie page", e);
    // return md;
    // }

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
        md.setMediaTitle(movieTitle);
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
        if (imdbSite == ImdbSiteDefinition.IMDB_COM) {
          // original title = title
          md.setOriginalTitle(md.getMediaTitle());
        } else {
          // try to parse the title out of "title-extra"
          Elements span = element.getElementsByClass("title-extra");
          if (span.size() > 0) {
            Element titleExtra = span.first();
            String originalTitle = titleExtra.ownText();
            if (!StringUtils.isEmpty(originalTitle)) {
              md.setOriginalTitle(originalTitle);
            }
          }
        }
      }

    }

    // poster
    Element poster = doc.getElementById("primary-poster");
    if (poster != null) {
      String posterUrl = poster.attr("src");
      posterUrl = posterUrl.replaceAll("SX[0-9]{2,4}_", "SX195_");
      posterUrl = posterUrl.replaceAll("SY[0-9]{2,4}_", "SY195_");
      processMediaArt(md, MediaArtifactType.POSTER, "Poster", posterUrl);
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
              } catch (Exception e) {
              }
              md.setUserRating(rating);
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
          } catch (Exception e) {
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
        if (h5Title.matches("(?i)" + imdbSite.getTagline() + ".*") && !Globals.settings.isImdbScrapeForeignLanguage()) {
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
        if (imdbSite == ImdbSiteDefinition.IMDB_COM) {
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
        }

        /*
         * <div class="info"><h5>Runtime:</h5><div class="info-content">129 min
         * </div></div>
         */
        // runtime
        if (h5Title.matches("(?i)" + imdbSite.getRuntime() + ".*")) {
          Elements div = element.getElementsByClass("info-content");
          if (div.size() > 0) {
            Element taglineElement = div.first();
            String runtimeAsString = cleanString(taglineElement.ownText().replaceAll("min", ""));
            int runtime = 0;
            try {
              runtime = Integer.parseInt(runtimeAsString);
            } catch (Exception e) {
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
        if (h5Title.matches("(?i)" + imdbSite.getWriter() + ".*")) {
          Elements a = element.getElementsByTag("a");
          for (Element anchor : a) {
            if (anchor.attr("href").matches("/name/nm.*")) {
              CastMember cm = new CastMember(CastMember.WRITER);
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
        if (h5Title.matches("(?i)" + imdbSite.getCertification() + ".*")) {
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
            CastMember cm = new CastMember(CastMember.DIRECTOR);
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
        CastMember cm = new CastMember();
        for (Element column : td) {
          // actor thumb
          if (column.hasClass("hs")) {
            Elements img = column.getElementsByTag("img");
            if (img.size() > 0) {
              String thumbUrl = img.get(0).attr("src");
              if (thumbUrl.contains("no_photo.png")) {
                cm.setImageUrl("");
              } else {
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
          cm.setType(CastMember.ACTOR);
          md.addCastMember(cm);
        }
      }
    }

    // Production companies
    elements = doc.getElementsByClass("blackcatheader");
    for (Element blackcatheader : elements) {
      if (blackcatheader.ownText().equals(imdbSite.getProductionCompanies())) {
        Elements a = blackcatheader.nextElementSibling().getElementsByTag("a");
        StringBuilder productionCompanies = new StringBuilder();
        for (Element anchor : a) {
          if (StringUtils.isNotEmpty(productionCompanies)) {
            productionCompanies.append(", ");
          }
          productionCompanies.append(anchor.ownText());
        }
        md.setCompany(productionCompanies.toString());
        break;
      }
    }

    /*
     * plot from /plotsummary
     */
    // build the url
    if (!Globals.settings.isImdbScrapeForeignLanguage()) {
      // sb = new StringBuilder(imdbSite.getSite());
      // sb.append(imdbSite.getSite());
      // sb.append("title/");
      // sb.append(imdbId);
      // sb.append("/plotsummary");
      //
      // doc = null;
      // try {
      // CachedUrl url = new CachedUrl(sb.toString());
      // doc = Jsoup.parse(url.getInputStream(),
      // imdbSite.getCharset().displayName(), "");
      // } catch (Exception e) {
      // LOGGER.debug("tried to fetch imdb plot page", e);
      // return md;
      // }
      doc = null;
      doc = futurePlotsummary.get();

      Elements plotpar = doc.getElementsByClass("plotpar");
      if (plotpar.size() > 0) {
        String plot = cleanString(plotpar.get(0).ownText());
        md.setPlot(plot);
      }
    }

    // get data from tmdb?
    if (Globals.settings.isImdbScrapeForeignLanguage()) {
      // TmdbMetadataProvider tmdb = TmdbMetadataProvider.getInstance();
      // MediaMetadata tmdbMd = tmdb.getMetadataForIMDBId(imdbId);
      MediaMetadata tmdbMd = futureTmdb.get();
      if (tmdbMd != null) {
        // title
        md.setMediaTitle(tmdbMd.getMediaTitle());
        // tagline
        md.setTagline(tmdbMd.getTagline());
        // plot
        md.setPlot(tmdbMd.getPlot());
      }
    }

    // get Artwork from TMDB
    try {
      // TmdbMetadataProvider tmdbMd = TmdbMetadataProvider.getInstance();
      // List<MediaArt> mediaArt = tmdbMd.getMediaArt(md.getIMDBID());
      List<MediaArt> mediaArt = futureArtwork.get();
      if (mediaArt != null && mediaArt.size() > 0) {
        md.clearMediaArt();
        md.addMediaArt(mediaArt);

        // also store tmdbId
        if ("0".equals(md.getTMDBID())) {
          md.setTMDBID(String.valueOf(mediaArt.get(0).getTmdbId()));
        }
      }
    } catch (Exception e) {
    }

    return md;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.tinymediamanager.scraper.IHasFindByIMDBID#searchByImdbId(java.lang.
   * String)
   */
  /**
   * Search by imdb id.
   * 
   * @param imdbid
   *          the imdbid
   * @return the media search result
   * @throws Exception
   *           the exception
   */
  @Override
  public MediaSearchResult searchByImdbId(String imdbid) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaMetadataProvider#getInfo()
   */
  /**
   * Gets the info.
   * 
   * @return the info
   */
  @Override
  public ProviderInfo getInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaMetadataProvider#getMetaData(org.
   * tinymediamanager.scraper.MediaSearchResult)
   */
  /**
   * Gets the meta data.
   * 
   * @param result
   *          the result
   * @return the meta data
   * @throws Exception
   *           the exception
   */
  @Override
  public MediaMetadata getMetaData(MediaSearchResult result) throws Exception {
    MediaMetadata md = null;
    if (result.getMetadata() != null) {
      LOGGER.debug("IMDB: getMetadata(result) from cache: " + result);
      md = result.getMetadata();
    } else {
      LOGGER.debug("IMDB: getMetadata(result): " + result);
      String imdbId = result.getIMDBId();
      md = getMetadataForIMDBId(imdbId);
    }

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
  public List<MediaSearchResult> search(SearchQuery query) throws Exception {
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

    String searchTerm = query.get(SearchQuery.Field.QUERY);

    StringBuilder sb = new StringBuilder(imdbSite.getSite());
    sb.append("find?q=");
    try {
      sb.append(URLEncoder.encode(searchTerm, imdbSite.getCharset().displayName()));
    } catch (UnsupportedEncodingException ex) {
      // Failed to encode the movie name for some reason!
      LOGGER.debug("Failed to encode search term: " + searchTerm);
      sb.append(searchTerm);
    }

    sb.append(";s=tt;site=aka");

    LOGGER.debug("========= BEGIN IMDB Scraper Search for: " + sb.toString());
    Document doc;
    try {
      CachedUrl url = new CachedUrl(sb.toString());
      doc = Jsoup.parse(url.getInputStream(), imdbSite.getCharset().displayName(), "");
    } catch (Exception e) {
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
        md = getMetadataForIMDBId(movieId);
        if (!StringUtils.isEmpty(md.getMediaTitle())) {
          movieName = md.getMediaTitle();
        }
      }

      // if no movie name/id was found - continue
      if (StringUtils.isEmpty(movieName) || StringUtils.isEmpty(movieId)) {
        continue;
      }

      MediaSearchResult sr = new MediaSearchResult();
      sr.setTitle(movieName);
      sr.setIMDBId(movieId);
      sr.setYear(md.getYear());
      sr.setMetadata(md);
      result.add(sr);

      return result;
    }

    // parse results
    elements = doc.getElementsByAttributeValue("valign", "top");
    for (Element element : elements) {
      // we only want the td's
      if (!"td".equalsIgnoreCase(element.tagName())) {
        continue;
      }

      // get the name inside the link
      String movieName = null;
      String movieId = null;
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

        break;
      }

      // if no movie name/id was found - continue
      if (StringUtils.isEmpty(movieName) || StringUtils.isEmpty(movieId)) {
        continue;
      }

      MediaSearchResult sr = new MediaSearchResult();
      sr.setTitle(movieName);
      sr.setIMDBId(movieId);

      // try to parse out the year
      Pattern yearPattern = Pattern.compile("\\(([0-9]{4})|/\\)");
      Matcher matcher = yearPattern.matcher(element.text());
      while (matcher.find()) {
        if (matcher.group(1) != null) {
          sr.setYear(matcher.group(1));
          break;
        }
      }

      // populate extra args
      MetadataUtil.copySearchQueryToSearchResult(query, sr);

      sr.setScore(MetadataUtil.calculateScore(searchTerm, movieName));

      result.add(sr);
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.tinymediamanager.scraper.IMediaMetadataProvider#getSupportedSearchTypes
   * ()
   */
  /**
   * Gets the supported search types.
   * 
   * @return the supported search types
   */
  @Override
  public MediaType[] getSupportedSearchTypes() {
    // TODO Auto-generated method stub
    return null;
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
  private void processMediaArt(MediaMetadata md, MediaArtifactType type, String label, String image) {
    MediaArt ma = new MediaArt();
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

  private class ImdbWorker implements Callable<Document> {
    private String   url;
    private Document doc = null;

    public ImdbWorker(String url) {
      this.url = url;
    }

    @Override
    public Document call() throws Exception {
      doc = null;
      try {
        CachedUrl cachedUrl = new CachedUrl(url);
        doc = Jsoup.parse(cachedUrl.getInputStream(), imdbSite.getCharset().displayName(), "");
      } catch (Exception e) {
        LOGGER.debug("tried to fetch imdb movie page " + url, e);
      }
      return doc;
    }
  }

  private class TmdbWorker implements Callable<MediaMetadata> {
    private String imdbId;

    public TmdbWorker(String imdbId) {
      this.imdbId = imdbId;
    }

    @Override
    public MediaMetadata call() throws Exception {
      TmdbMetadataProvider tmdb = TmdbMetadataProvider.getInstance();
      return tmdb.getMetadataForIMDBId(imdbId);
    }
  }

  private class TmdbArtworkWorker implements Callable<List<MediaArt>> {
    private String imdbId;

    public TmdbArtworkWorker(String imdbId) {
      this.imdbId = imdbId;
    }

    @Override
    public List<MediaArt> call() throws Exception {
      TmdbMetadataProvider tmdbMd = TmdbMetadataProvider.getInstance();
      return tmdbMd.getMediaArt(imdbId);
    }
  }

}