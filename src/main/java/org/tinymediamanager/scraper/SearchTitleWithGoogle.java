package org.tinymediamanager.scraper;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.http.CachedUrl;
import org.tinymediamanager.scraper.http.Url;

public class SearchTitleWithGoogle {
  private static final Logger LOGGER        = LoggerFactory.getLogger(SearchTitleWithGoogle.class);
  private static final String PAGE_ENCODING = "UTF-8";

  /**
   * does a fallback search with google, returning the first 10 results...
   * 
   * @param site
   *          the base hostname like "zelluloid.de"
   * @param mpi
   * @param options
   * @return
   */
  public List<MediaSearchResult> search(String site, MediaProviderInfo mpi, MediaSearchOptions options) {
    LOGGER.debug("SearchTitleWithGoogle() " + options.toString());
    List<MediaSearchResult> resultList = new ArrayList<MediaSearchResult>();

    String searchUrl = "";
    String searchTerm = "";

    try {
      if (StringUtils.isNotEmpty(options.get(MediaSearchOptions.SearchParam.QUERY))) {
        searchTerm = options.get(MediaSearchOptions.SearchParam.QUERY);
        String lang = options.get(MediaSearchOptions.SearchParam.LANGUAGE);
        searchUrl = "https://www.google." + lang + "/search?q=" + URLEncoder.encode("site:" + site + " " + searchTerm, "UTF-8");
        LOGGER.debug("search for : " + searchTerm + " (" + searchUrl + ")");
      }
      else {
        LOGGER.debug("empty searchString");
        return resultList;
      }
    }
    catch (Exception e) {
      LOGGER.warn("error searching " + e.getMessage());
      return resultList;
    }

    Document doc = null;
    try {
      Url url = new CachedUrl(searchUrl);
      InputStream in = url.getInputStream();
      doc = Jsoup.parse(in, PAGE_ENCODING, "");
      in.close();
    }
    catch (Exception e) {
      LOGGER.error("failed to search for " + searchTerm + ": " + e.getMessage());
    }
    if (doc == null) {
      return resultList;
    }

    Elements res = doc.getElementsByClass("r");
    for (Element el : res) {
      Element a = el.getElementsByTag("a").first();
      MediaSearchResult sr = new MediaSearchResult(mpi.getId());
      sr.setId(mpi.getId());
      sr.setUrl(a.attr("href"));
      sr.setTitle(a.text());
      resultList.add(sr);
    }

    return resultList;
  }

}
