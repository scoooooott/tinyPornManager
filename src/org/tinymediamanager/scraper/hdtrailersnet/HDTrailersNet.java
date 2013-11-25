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
package org.tinymediamanager.scraper.hdtrailersnet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.scraper.util.Url;

/**
 * The Class HDTrailersNet.
 * 
 * @author Myron Boyle
 */
public class HDTrailersNet implements IMediaTrailerProvider {
  private static final Logger      LOGGER       = LoggerFactory.getLogger(HDTrailersNet.class);
  private static MediaProviderInfo providerInfo = new MediaProviderInfo("hdtrailersnet", "hd-trailers.net",
                                                    "Scraper for hd-trailers.net which is able to scrape trailers");

  /**
   * Instantiates a new hD trailers net.
   */
  public HDTrailersNet() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaTrailerProvider#getTrailers(org. tinymediamanager.scraper.MediaScrapeOptions)
   */
  @Override
  public List<MediaTrailer> getTrailers(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getTrailers() " + options.toString());
    List<MediaTrailer> trailers = new ArrayList<MediaTrailer>();
    MediaMetadata md = options.getMetadata();

    if (md == null || StringUtils.isEmpty(md.getStringValue(MediaMetadata.ORIGINAL_TITLE))) {
      LOGGER.warn("no originalTitle served");
      return trailers;
    }

    String ot = md.getStringValue(MediaMetadata.ORIGINAL_TITLE);

    // check if the original title is not empty
    if (StringUtils.isEmpty(ot)) {
      return trailers;
    }

    try {
      // best guess
      String search = "http://www.hd-trailers.net/movie/" + ot.replaceAll("[^a-zA-Z0-9]", "-").replaceAll("--", "-").toLowerCase() + "/";
      LOGGER.debug("Guessed HD-Trailers Url: " + search);

      Url url = new CachedUrl(search);
      InputStream in = url.getInputStream();
      Document doc = Jsoup.parse(in, "UTF-8", "");
      Elements tr = doc.getElementsByAttributeValue("itemprop", "trailer");
      /*
       * <tr style="" itemprop="trailer" itemscope itemtype="http://schema.org/VideoObject"> <td class="bottomTableDate" rowspan="2">2012-03-30</td>
       * <td class="bottomTableName" rowspan="2"><span class="standardTrailerName" itemprop="name">Trailer 2</span> <a href=
       * "http://blog.hd-trailers.net/how-to-download-hd-trailers-from-apple/#workarounds" ><img src="http://static.hd-trailers.net/images/error.png"
       * width="16" height="16" style="border:0px;vertical-align:middle" alt="Apple Direct Download Unavailable"
       * title="Apple Direct Download Unavailable" /></a></td>
       * 
       * <td class="bottomTableResolution"><a href= "http://trailers.apple.com/movies/sony_pictures/meninblack3/meninblack3-tlr2_h480p.mov"
       * rel="lightbox[res480p 852 480]" title="Men in Black 3 - Trailer 2 - 480p">480p</a></td> <td class="bottomTableResolution"><a href=
       * "http://trailers.apple.com/movies/sony_pictures/meninblack3/meninblack3-tlr2_h720p.mov" rel="lightbox[res720p 1280 720]"
       * title="Men in Black 3 - Trailer 2 - 720p">720p</a></td> <td class="bottomTableResolution"><a href=
       * "http://trailers.apple.com/movies/sony_pictures/meninblack3/meninblack3-tlr2_h1080p.mov" rel="lightbox[res1080p 1920 1080]"
       * title="Men in Black 3 - Trailer 2 - 1080p">1080p</a></td> <td class="bottomTableIcon"> <a
       * href="http://trailers.apple.com/trailers/sony_pictures/meninblack3/" target="_blank"> <img
       * src="http://static.hd-trailers.net/images/apple.ico" alt="Apple" height="16px" width="16px"/></a></td> </tr> <tr> <td
       * class="bottomTableFileSize">36 MB</td> <td class="bottomTableFileSize">111 MB</td> <td class="bottomTableFileSize">181 MB</td> <td
       * class="bottomTableEmbed"><a href=
       * "/embed-code.php?movieId=men-in-black-3&amp;source=1&amp;trailerName=Trailer 2&amp;resolutions=480;720;1080" rel="lightbox[embed 600 600]"
       * title="Embed this video on your website">embed</a></td> </tr>
       */
      for (Element t : tr) {
        try {
          String date = t.select("td.bottomTableDate").first().text();
          String title = t.select("td.bottomTableName > span").first().text();

          // apple.com urls currently not working (according to hd-trailers)
          String tr0qual = t.select("td.bottomTableResolution > a").get(0).text();
          String tr0url = t.select("td.bottomTableResolution > a").get(0).attr("href");
          MediaTrailer trailer = new MediaTrailer();
          trailer.setName(title + " (" + date + ")");
          trailer.setDate(date);
          trailer.setUrl(tr0url);
          trailer.setQuality(tr0qual);
          trailer.setProvider(getProviderFromUrl(tr0url));
          LOGGER.debug(trailer.toString());
          trailers.add(trailer);

          String tr1qual = t.select("td.bottomTableResolution > a").get(1).text();
          String tr1url = t.select("td.bottomTableResolution > a").get(1).attr("href");
          trailer = new MediaTrailer();
          trailer.setName(title + " (" + date + ")");
          trailer.setDate(date);
          trailer.setUrl(tr1url);
          trailer.setQuality(tr1qual);
          trailer.setProvider(getProviderFromUrl(tr1url));
          LOGGER.debug(trailer.toString());
          trailers.add(trailer);

          String tr2qual = t.select("td.bottomTableResolution > a").get(2).text();
          String tr2url = t.select("td.bottomTableResolution > a").get(2).attr("href");
          trailer = new MediaTrailer();
          trailer.setName(title + " (" + date + ")");
          trailer.setDate(date);
          trailer.setUrl(tr2url);
          trailer.setQuality(tr2qual);
          trailer.setProvider(getProviderFromUrl(tr2url));
          LOGGER.debug(trailer.toString());
          trailers.add(trailer);
        }
        catch (IndexOutOfBoundsException i) {
          // ignore parse errors per line
          LOGGER.warn("Error parsing HD-Trailers line. Possible missing quality.");
        }
      }
    }
    catch (IOException e) {
      LOGGER.error("cannot parse HD-Trailers movie: " + ot, e);
    }
    finally {
    }
    return trailers;
  }

  /**
   * Returns the "Source" for this trailer by parsing the URL.
   * 
   * @param url
   *          the url
   * @return the provider from url
   */
  private static String getProviderFromUrl(String url) {
    url = url.toLowerCase();
    String source = "unknown";
    if (url.contains("youtube.com")) {
      source = "youtube";
    }
    else if (url.contains("apple.com")) {
      source = "apple";
    }
    else if (url.contains("aol.com")) {
      source = "aol";
    }
    else if (url.contains("yahoo.com")) {
      source = "yahoo";
    }
    else if (url.contains("hd-trailers.net")) {
      source = "hdtrailers";
    }
    else if (url.contains("moviefone.com")) {
      source = "moviefone";
    }
    else if (url.contains("mtv.com")) {
      source = "mtv";
    }
    else if (url.contains("ign.com")) {
      source = "ign";
    }
    return source;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaTrailerProvider#getProviderInfo()
   */
  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }
}
