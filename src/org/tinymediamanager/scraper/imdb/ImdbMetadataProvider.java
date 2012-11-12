package org.tinymediamanager.scraper.imdb;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.scraper.IHasFindByIMDBID;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.ProviderInfo;
import org.tinymediamanager.scraper.SearchQuery;
import org.tinymediamanager.scraper.util.CachedUrl;

public class ImdbMetadataProvider implements IMediaMetadataProvider, IHasFindByIMDBID {
  private static final Logger                          LOGGER     = Logger.getLogger(ImdbMetadataProvider.class);

  private static final Map<String, ImdbSiteDefinition> IMDB_SITES = new HashMap<String, ImdbSiteDefinition>();

  static {
    IMDB_SITES.put("us", new ImdbSiteDefinition("http://www.imdb.com/", "ISO-8859-1", "Director|Directed by", "Cast", "Release Date", "Runtime",
        "Country", "Company", "Genre", "Quotes", "Plot", "Rated", "Certification", "Original Air Date", "Writer|Writing credits", "Taglines"));

    IMDB_SITES.put("fr", new ImdbSiteDefinition("http://www.imdb.fr/", "ISO-8859-1", "R&#xE9;alisateur|R&#xE9;alis&#xE9; par", "Ensemble",
        "Date de sortie", "Dur&#xE9;e", "Pays", "Soci&#xE9;t&#xE9;", "Genre", "Citation", "Intrigue", "Rated", "Classification", "Date de sortie",
        "Sc&#xE9;naristes|Sc&#xE9;naristes", "Taglines"));

    IMDB_SITES.put("es", new ImdbSiteDefinition("http://www.imdb.es/", "ISO-8859-1", "Director|Dirigida por", "Reparto", "Fecha de Estreno",
        "Duraci&#xF3;n", "Pa&#xED;s", "Compa&#xF1;&#xED;a", "G&#xE9;nero", "Quotes", "Trama", "Rated", "Clasificaci&#xF3;n", "Fecha de Estreno",
        "Escritores|Cr&#xE9;ditos del gui&#xF3;n", "Taglines"));

    IMDB_SITES.put("de", new ImdbSiteDefinition("http://www.imdb.de/", "ISO-8859-1", "Regisseur|Regie", "Besetzung", "Premierendatum", "L&#xE4;nge",
        "Land", "Firma", "Genre", "Quotes", "Handlung", "Rated", "Altersfreigabe", "Premierendatum", "Guionista|Buch", "Taglines"));

    IMDB_SITES.put("it", new ImdbSiteDefinition("http://www.imdb.it/", "ISO-8859-1", "Regista|Registi|Regia di", "Cast", "Data di uscita", "Durata",
        "Nazionalit&#xE0;", "Compagnia", "Genere", "Quotes", "Trama", "Rated", "Certification", "Data di uscita", "Sceneggiatore|Scritto da",
        "Taglines"));

    IMDB_SITES.put("pt", new ImdbSiteDefinition("http://www.imdb.pt/", "ISO-8859-1", "Diretor|Dirigido por", "Elenco", "Data de Lan&#xE7;amento",
        "Dura&#xE7;&#xE3;o", "Pa&#xED;s", "Companhia", "G&#xEA;nero", "Quotes", "Argumento", "Rated", "Certifica&#xE7;&#xE3;o",
        "Data de Lan&#xE7;amento", "Roteirista|Cr&#xE9;ditos como roteirista", "Taglines"));

    // Use this as a workaround for English speakers abroad who get localised
    // versions of imdb.com
    IMDB_SITES.put("labs", new ImdbSiteDefinition("http://akas.imdb.com/", "ISO-8859-1", "Director|Directors|Directed by", "Cast", "Release Date",
        "Runtime", "Country", "Production Co", "Genres", "Quotes", "Storyline", "Rated", "Certification", "Original Air Date",
        "Writer|Writers|Writing credits", "Taglines"));

    // TODO: Leaving this as labs.imdb.com for the time being, but will be
    // updated to www.imdb.com
    IMDB_SITES.put("us2", new ImdbSiteDefinition("http://labs.imdb.com/", "ISO-8859-1", "Director|Directors|Directed by", "Cast", "Release Date",
        "Runtime", "Country", "Production Co", "Genres", "Quotes", "Storyline", "Rated", "Certification", "Original Air Date",
        "Writer|Writers|Writing credits", "Taglines"));

    // Not 100% sure these are correct
    IMDB_SITES.put("it2", new ImdbSiteDefinition("http://www.imdb.it/", "ISO-8859-1", "Regista|Registi|Regia di", "Attori", "Data di uscita",
        "Durata", "Nazionalit&#xE0;", "Compagnia", "Genere", "Quotes", "Trama", "Rated", "Certification", "Data di uscita",
        "Sceneggiatore|Scritto da", "Taglines"));
  }

  private ImdbSiteDefinition                           imdbSite;

  public ImdbMetadataProvider() {
    imdbSite = IMDB_SITES.get("labs");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.tinymediamanager.scraper.IHasFindByIMDBID#getMetadataForIMDBId(java
   * .lang.String)
   */
  @Override
  public MediaMetadata getMetadataForIMDBId(String imdbid) throws Exception {
    // TODO Auto-generated method stub
    return null;
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
    // TODO Auto-generated method stub
    return null;
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

    List<MediaSearchResult> result = new ArrayList<MediaSearchResult>();

    String searchTerm = query.get(SearchQuery.Field.QUERY);

    StringBuilder sb = new StringBuilder(imdbSite.getSite());
    sb.append("find?q=");
    try {
      sb.append(URLEncoder.encode(searchTerm, imdbSite.getCharset().displayName()));
    }
    catch (UnsupportedEncodingException ex) {
      // Failed to encode the movie name for some reason!
      LOGGER.debug("Failed to encode search term: " + searchTerm);
      sb.append(searchTerm);
    }

    // if (StringTools.isValidString(year)) {
    // sb.append("+%28").append(year).append("%29");
    // }

    sb.append(";s=tt;site=aka");

    LOGGER.debug("========= BEGIN IMDB Scraper Search for: " + sb.toString());
    String xml;
    try {
      CachedUrl url = new CachedUrl(sb.toString());
      InputStream is = url.getInputStream();
      StringWriter writer = new StringWriter();
      IOUtils.copy(is, writer);
      xml = writer.toString();
      is.close();
      writer.close();
    }
    catch (Exception e) {
      LOGGER.debug("tried to fetch search response", e);
      return result;
    }

    // Check if this is an exact match (we got a movie page instead of a results
    // list)
    Pattern titleregex = Pattern.compile(Pattern.quote("<link rel=\"canonical\" href=\"" + imdbSite.getSite() + "title/" + "(tt\\d+)/\""));
    Matcher titlematch = titleregex.matcher(xml);
    if (titlematch.find()) {
      LOGGER.debug("IMDb returned one match " + titlematch.group(1));
      // return titlematch.group(1);
    }

    String otherMovieName = HTMLTools.extractTag(HTMLTools.extractTag(xml, ";ttype=ep\">", "\"</a>.</li>"), "<b>", "</b>").toLowerCase();
    String formattedMovieName;
    if (!StringUtils.isEmpty(otherMovieName)) {
      // if (StringTools.isValidString(year) && otherMovieName.endsWith(")") &&
      // otherMovieName.contains("(")) {
      // otherMovieName = otherMovieName.substring(0,
      // otherMovieName.lastIndexOf('(') - 1);
      // formattedMovieName = otherMovieName + "</a> (" + year + ")";
      // }
      // else {
      formattedMovieName = otherMovieName + "</a>";
      // }
    }
    else {
      sb = new StringBuilder();
      try {
        sb.append(URLEncoder.encode(searchTerm, imdbSite.getCharset().displayName()).replace("+", " "));
      }
      catch (UnsupportedEncodingException ex) {
        LOGGER.debug("Failed to encode movie name: " + searchTerm);
        sb.append(searchTerm);
      }
      sb.append("</a>");
      // if (StringTools.isValidString(year)) {
      // sb.append(" (").append(year).append(")");
      // }
      otherMovieName = sb.toString();
      formattedMovieName = otherMovieName;
    }

    // logger.debug(logMessage + "Title search: '" + formattedMovieName +
    // "'"); // XXX DEBUG
    for (String searchResult : HTMLTools.extractTags(xml, "<div class=\"media_strip_thumbs\">", "<div id=\"sidebar\">", ".src='/rg/find-" + "title-",
        "</td>", false)) {
      // logger.debug(logMessage + "Title check  : '" + searchResult + "'");
      // // XXX DEBUG
      if (searchResult.toLowerCase().indexOf(formattedMovieName) != -1) {
        // logger.debug(logMessage + "Title match  : '" + searchResult + "'");
        // // XXX DEBUG
        MediaSearchResult sr = new MediaSearchResult();
        sr.setTitle(HTMLTools.extractTag(searchResult, "/images/b.gif?link=" + "/title/", "/';\">"));
        result.add(sr);
      }
      else {
        for (String otherResult : HTMLTools.extractTags(searchResult, "</';\">", "</p>", "<p class=\"find-aka\">", "</em>", false)) {
          if (otherResult.toLowerCase().indexOf("\"" + otherMovieName + "\"") != -1) {
            // logger.debug(logMessage + "Other Title match: '" + otherResult
            // + "'"); // XXX DEBUG

            MediaSearchResult sr = new MediaSearchResult();
            sr.setTitle(HTMLTools.extractTag(searchResult, "/images/b.gif?link=" + "/title/", "/';\">"));
            result.add(sr);
          }
        }
      }
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

}