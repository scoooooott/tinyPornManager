package org.tinymediamanager.scraper.imdb;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MetadataUtil;
import org.tinymediamanager.scraper.SearchQuery;
import org.xml.sax.SAXException;

public class ImdbSearchResultParser {
  private static final Logger              LOGGER              = Logger.getLogger(ImdbSearchResultParser.class);

  private static String                    POPULAR_TITLE_MATCH = "Popular Titles";
  private static String                    PARTIAL_TITLE_MATCH = "Titles (Approx Matches)";
  private static String                    EXACT_TITLE_MATCH   = "Titles (Exact Matches)";
  private static String                    END_OF_LIST_MATCH   = "Suggestions For Improving Your Results";

  private static final int                 POPULAR_MATCHES     = 1;
  private static final int                 EXACT_MATCHES       = 2;
  private static final int                 PARTIAL_MATCHES     = 3;
  private static final int                 STARTING            = 88;
  private static final int                 ENDED               = 99;
  private int                              state               = STARTING;

  private static final int                 TITLE_READ_TITLE    = 1;
  private static final int                 TITLE_READ_YEAR     = 2;
  private static final int                 TITLE_DONE          = 3;
  private int                              aState              = TITLE_DONE;

  private String                           charBuffer          = null;
  private MediaSearchResult                curResult           = null;

  private List<IMetadataSearchResult>      results             = new ArrayList<IMetadataSearchResult>();

  private Comparator<MetadataSearchResult> sorter              = new Comparator<IMetadataSearchResult>() {

                                                                 public int compare(IMetadataSearchResult o1, IMetadataSearchResult o2) {
                                                                   if (o1.getScore() > o2.getScore())
                                                                     return -1;
                                                                   if (o1.getScore() < o2.getScore())
                                                                     return 1;
                                                                   return 0;
                                                                 }

                                                               };
  private String                           searchTitle;

  private SearchQuery                      query               = null;

  public ImdbSearchResultParser(SearchQuery query, String url, String searchTitle) {
    super(url);
    this.searchTitle = searchTitle;
    this.query = query;
  }

  public List<IMetadataSearchResult> getResults() {
    Collections.sort(results, sorter);
    return results;
  }

  @Override
  public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
    if (state == ENDED)
      return;

    String elName = localName.toLowerCase();
    if ("img".equals(elName)) {
      // reset the title states when we encounter images
      aState = TITLE_DONE;
    }

    if ("a".equals(elName) && state != STARTING) {
      String href = atts.getValue("href");
      LOGGER.debug("Starting, found an A tag: href: " + href);
      if (href != null && href.indexOf("/title/") != -1) {
        aState = TITLE_READ_TITLE;

        // create the IVIdeoResult
        curResult = new MediaSearchResult(IMDBMetaDataProvider.PROVIDER_ID, query.getMediaType(), 0.0f);

        // set the imdb title url
        String imdbId = IMDBUtils.parseIMDBID(href);
        LOGGER.debug("Setting IMDB ID: " + imdbId + " from href: " + href);
        curResult.setId(imdbId);
        curResult.setUrl(IMDBUtils.createDetailUrl(imdbId));
      }
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (state == ENDED)
      return;

    charBuffer = getCharacters(ch, start, length);
    if (charBuffer == null || charBuffer.length() == 0)
      return;

    if (POPULAR_TITLE_MATCH.equals(charBuffer)) {
      LOGGER.debug("Starting Popular Match Titles");
      state = POPULAR_MATCHES;
    } else if (EXACT_TITLE_MATCH.equals(charBuffer)) {
      LOGGER.debug("Starting Exact Match Titles");
      state = EXACT_MATCHES;
    } else if (PARTIAL_TITLE_MATCH.equals(charBuffer)) {
      LOGGER.debug("Starting Partial Match Titles");
      state = PARTIAL_MATCHES;
    } else if (END_OF_LIST_MATCH.equals(charBuffer)) {
      state = ENDED;
      return;
    }

    if (state == STARTING || state == ENDED)
      return;

    if (aState == TITLE_READ_TITLE) {
      LOGGER.debug("IMDB Title: " + charBuffer);
      curResult.setTitle(Utils.unquote(charBuffer));
      // set the state to READ_YEAR
      aState = TITLE_READ_YEAR;
    } else if (aState == TITLE_READ_YEAR) {
      // year should look like this... (NNNN)
      Pattern p = Pattern.compile("\\(([0-9]+)([\\/]*[A-Z]*)*\\)");
      try {
        Matcher m = p.matcher(charBuffer);
        if (m.find()) {
          curResult.setYear(m.group(1));
        } else {
          LOGGER.error("Could not parse Year from: " + charBuffer);
        }
      } catch (Exception e) {
        LOGGER.error("Error Parsing Year from CharBuffer:[" + charBuffer + "]", e);
      }
      // reset the title reading state... we're done this entry.
      aState = TITLE_DONE;

      String tmpStr = curResult.getTitle();
      curResult.setScore(MetadataUtil.calculateScore(searchTitle, tmpStr));

      // add the result.
      LOGGER.debug("Adding Result: " + tmpStr);
      results.add(curResult);
      curResult = null;
    }
  }

  @Override
  public void skippedEntity(String name) throws SAXException {
    LOGGER.debug("IMDB: Character Entity: " + name);
  }
}
