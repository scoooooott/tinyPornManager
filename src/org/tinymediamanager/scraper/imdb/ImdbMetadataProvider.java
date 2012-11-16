package org.tinymediamanager.scraper.imdb;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import org.tinymediamanager.scraper.util.CachedUrl;

public class ImdbMetadataProvider implements IMediaMetadataProvider, IHasFindByIMDBID {
  private static final Logger LOGGER = Logger.getLogger(ImdbMetadataProvider.class);

  // private static final Map<String, ImdbSiteDefinition> IMDB_SITES = new
  // HashMap<String, ImdbSiteDefinition>();

  // static {
  // IMDB_SITES.put("us", new ImdbSiteDefinition("http://www.imdb.com/",
  // "ISO-8859-1", "Director|Directed by", "Cast", "Release Date", "Runtime",
  // "Country", "Company", "Genre",
  // "Quotes", "Plot", "Rated", "Certification", "Original Air Date",
  // "Writer|Writing credits", "Taglines"));
  //
  // IMDB_SITES.put("fr", new ImdbSiteDefinition("http://www.imdb.fr/",
  // "ISO-8859-1", "R&#xE9;alisateur|R&#xE9;alis&#xE9; par", "Ensemble",
  // "Date de sortie", "Dur&#xE9;e", "Pays",
  // "Soci&#xE9;t&#xE9;", "Genre", "Citation", "Intrigue", "Rated",
  // "Classification", "Date de sortie", "Sc&#xE9;naristes|Sc&#xE9;naristes",
  // "Taglines"));
  //
  // IMDB_SITES.put("es", new ImdbSiteDefinition("http://www.imdb.es/",
  // "ISO-8859-1", "Director|Dirigida por", "Reparto", "Fecha de Estreno",
  // "Duraci&#xF3;n", "Pa&#xED;s",
  // "Compa&#xF1;&#xED;a", "G&#xE9;nero", "Quotes", "Trama", "Rated",
  // "Clasificaci&#xF3;n", "Fecha de Estreno",
  // "Escritores|Cr&#xE9;ditos del gui&#xF3;n", "Taglines"));
  //
  // IMDB_SITES.put("de", new ImdbSiteDefinition("http://www.imdb.de/",
  // "ISO-8859-1", "Regisseur|Regie", "Besetzung", "Premierendatum",
  // "L&#xE4;nge", "Land", "Firma", "Genre",
  // "Quotes", "Handlung", "Rated", "Altersfreigabe", "Premierendatum",
  // "Guionista|Buch", "Taglines"));
  //
  // IMDB_SITES.put("it", new ImdbSiteDefinition("http://www.imdb.it/",
  // "ISO-8859-1", "Regista|Registi|Regia di", "Cast", "Data di uscita",
  // "Durata", "Nazionalit&#xE0;",
  // "Compagnia", "Genere", "Quotes", "Trama", "Rated", "Certification",
  // "Data di uscita", "Sceneggiatore|Scritto da", "Taglines"));
  //
  // IMDB_SITES.put("pt", new ImdbSiteDefinition("http://www.imdb.pt/",
  // "ISO-8859-1", "Diretor|Dirigido por", "Elenco", "Data de Lan&#xE7;amento",
  // "Dura&#xE7;&#xE3;o", "Pa&#xED;s",
  // "Companhia", "G&#xEA;nero", "Quotes", "Argumento", "Rated",
  // "Certifica&#xE7;&#xE3;o", "Data de Lan&#xE7;amento",
  // "Roteirista|Cr&#xE9;ditos como roteirista", "Taglines"));
  //
  // // Use this as a workaround for English speakers abroad who get localised
  // // versions of imdb.com
  // IMDB_SITES.put("labs", new ImdbSiteDefinition("http://akas.imdb.com/",
  // "ISO-8859-1", "Director|Directors|Directed by", "Cast", "Release Date",
  // "Runtime", "Country",
  // "Production Co", "Genres", "Quotes", "Storyline", "Rated", "Certification",
  // "Original Air Date", "Writer|Writers|Writing credits", "Taglines"));
  //
  // // TODO: Leaving this as labs.imdb.com for the time being, but will be
  // // updated to www.imdb.com
  // IMDB_SITES.put("us2", new ImdbSiteDefinition("http://labs.imdb.com/",
  // "ISO-8859-1", "Director|Directors|Directed by", "Cast", "Release Date",
  // "Runtime", "Country",
  // "Production Co", "Genres", "Quotes", "Storyline", "Rated", "Certification",
  // "Original Air Date", "Writer|Writers|Writing credits", "Taglines"));
  //
  // // Not 100% sure these are correct
  // IMDB_SITES.put("it2", new ImdbSiteDefinition("http://www.imdb.it/",
  // "ISO-8859-1", "Regista|Registi|Regia di", "Attori", "Data di uscita",
  // "Durata", "Nazionalit&#xE0;",
  // "Compagnia", "Genere", "Quotes", "Trama", "Rated", "Certification",
  // "Data di uscita", "Sceneggiatore|Scritto da", "Taglines"));
  // }

  private ImdbSiteDefinition  imdbSite;

  public ImdbMetadataProvider() {
    imdbSite = ImdbSiteDefinition.IMDB_COM;
  }

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
  @Override
  public MediaMetadata getMetadataForIMDBId(String imdbId) throws Exception {
    LOGGER.debug("IMDB: getMetadata(imdbId): " + imdbId);

    MediaMetadata md = new MediaMetadata();
    md.setIMDBID(imdbId);

    // build the url
    StringBuilder sb = new StringBuilder(imdbSite.getSite());
    sb.append("title/");
    sb.append(imdbId);
    sb.append("/combined");

    Document doc;
    try {
      CachedUrl url = new CachedUrl(sb.toString());
      doc = Jsoup.parse(url.getInputStream(), imdbSite.getCharset().displayName(), "");
    } catch (Exception e) {
      LOGGER.debug("tried to fetch imdb movie page", e);
      return md;
    }

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
        if (h5Title.matches("(?i)" + imdbSite.getTagline() + ".*")) {
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
      }
    }

    /*
     * plot from /plotsummary
     */
    // build the url
    sb = new StringBuilder(imdbSite.getSite());
    sb.append("title/");
    sb.append(imdbId);
    sb.append("/plotsummary");

    doc = null;
    try {
      CachedUrl url = new CachedUrl(sb.toString());
      doc = Jsoup.parse(url.getInputStream(), imdbSite.getCharset().displayName(), "");
    } catch (Exception e) {
      LOGGER.debug("tried to fetch imdb plot page", e);
      return md;
    }

    Elements plotpar = doc.getElementsByClass("plotpar");
    if (plotpar.size() > 0) {
      String plot = cleanString(plotpar.get(0).ownText());
      md.setPlot(plot);
    }

    // TODO plot from http://www.imdb.de/title/<imdbid>/plotsummary

    // TODO genres for other imdbsites

    return md;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.tinymediamanager.scraper.IHasFindByIMDBID#searchByImdbId(java.lang.
   * String)
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
  @Override
  public MediaMetadata getMetaData(MediaSearchResult result) throws Exception {
    if (result.getMetadata() != null) {
      LOGGER.debug("IMDB: getMetadata(result) from cache: " + result);
      return result.getMetadata();
    } else {
      LOGGER.debug("IMDB: getMetadata(result): " + result);
      String imdbId = result.getIMDBId();
      return getMetadataForIMDBId(imdbId);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.tinymediamanager.scraper.IMediaMetadataProvider#search(org.tinymediamanager
   * .scraper.SearchQuery)
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

  private String cleanString(String oldString) {
    if (StringUtils.isEmpty(oldString)) {
      return "";
    }
    // remove non breaking spaces
    String newString = oldString.replace(String.valueOf((char) 160), " ");
    // and trim
    return StringUtils.trim(newString);
  }

}