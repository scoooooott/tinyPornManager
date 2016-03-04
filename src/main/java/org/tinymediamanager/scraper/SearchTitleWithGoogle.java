package org.tinymediamanager.scraper;

import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
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
   * Does a fallback search with google, returning the first 10 results...<br>
   * <br>
   * You have to <br>
   * 1) check, if url starts with your desired destination page (aka filter results)<br>
   * 2) get the ID from url (if you work with it)
   * 
   * @param site
   *          the base hostname like "zelluloid.de"
   * @param mpi
   * @param options
   * @return MediaSearchResult, but NO id filled. Scraper MUST work with url-only!
   */
  public List<MediaSearchResult> search(String site, MediaProviderInfo mpi, MediaSearchOptions options) {
    LOGGER.debug("SearchTitleWithGoogle() " + options.toString());
    List<MediaSearchResult> resultList = new ArrayList<MediaSearchResult>();

    String searchUrl = "";
    String searchTerm = "";

    try {
      if (StringUtils.isNotEmpty(options.get(MediaSearchOptions.SearchParam.QUERY))) {
        if (!site.startsWith("http")) {
          site = "http://" + site;
        }
        site = new URL(site).getHost();
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
      if (doc == null) {
        return resultList;
      }

      Elements res = doc.getElementsByClass("r");
      for (Element el : res) {
        Element a = el.getElementsByTag("a").first();
        MediaSearchResult sr = new MediaSearchResult(mpi.getId());
        String gurl = a.attr("href");
        if (gurl.contains("url?q=")) {
          // google manipulated tracking url
          URL tmp = new URL("http://google.com/" + gurl);
          String[] params = tmp.getQuery().split("[\\?&]");
          for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            if (name.equals("q")) {
              gurl = value;
            }
          }
        }
        sr.setUrl(URLDecoder.decode(gurl, "UTF-8"));
        // sr.setId(mpi.getId()); // we have no clue about ID!!
        sr.setTitle(a.text().replaceAll(site, "(via Google)"));
        resultList.add(sr);
      }
    }
    catch (Exception e) {
      LOGGER.error("failed to search for " + searchTerm + ": " + e.getMessage());
    }

    return resultList;
  }

}
