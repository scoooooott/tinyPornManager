package org.tinymediamanager.scraper.util;

import java.io.IOException;
import java.io.InputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tinymediamanager.core.movie.Movie;

public class HDTrailerNet {

  public static void getTrailer(Movie movie) {
    try {
      // FIXME: how to get hd-trailers url?!?
      // english title and guess url (as done in other tools)
      // or get url from google? use english title and take first entry?
      // ex
      // http://www.google.at/search?q=inurl:hd-trailers.net/movie+%22men+in+black+3%22
      Url url = new CachedUrl("http://www.hd-trailers.net/movie/men-in-black-3/");
      InputStream in = url.getInputStream();

      Document doc = Jsoup.parse(in, "UTF-8", "");
      Elements tr = doc.getElementsByAttributeValue("itemprop", "trailer");
      /*
       * <tr style="" itemprop="trailer" itemscope
       * itemtype="http://schema.org/VideoObject"> <td class="bottomTableDate"
       * rowspan="2">2012-03-30</td> <td class="bottomTableName"
       * rowspan="2"><span class="standardTrailerName" itemprop="name">Trailer
       * 2</span> <a href=
       * "http://blog.hd-trailers.net/how-to-download-hd-trailers-from-apple/#workarounds"
       * ><img src="http://static.hd-trailers.net/images/error.png" width="16"
       * height="16" style="border:0px;vertical-align:middle"
       * alt="Apple Direct Download Unavailable"
       * title="Apple Direct Download Unavailable" /></a></td>
       * 
       * <td class="bottomTableResolution"><a href=
       * "http://trailers.apple.com/movies/sony_pictures/meninblack3/meninblack3-tlr2_h480p.mov"
       * rel="lightbox[res480p 852 480]"
       * title="Men in Black 3 - Trailer 2 - 480p">480p</a></td> <td
       * class="bottomTableResolution"><a href=
       * "http://trailers.apple.com/movies/sony_pictures/meninblack3/meninblack3-tlr2_h720p.mov"
       * rel="lightbox[res720p 1280 720]"
       * title="Men in Black 3 - Trailer 2 - 720p">720p</a></td> <td
       * class="bottomTableResolution"><a href=
       * "http://trailers.apple.com/movies/sony_pictures/meninblack3/meninblack3-tlr2_h1080p.mov"
       * rel="lightbox[res1080p 1920 1080]"
       * title="Men in Black 3 - Trailer 2 - 1080p">1080p</a></td> <td
       * class="bottomTableIcon"> <a
       * href="http://trailers.apple.com/trailers/sony_pictures/meninblack3/"
       * target="_blank"> <img
       * src="http://static.hd-trailers.net/images/apple.ico" alt="Apple"
       * height="16px" width="16px"/></a></td> </tr> <tr> <td
       * class="bottomTableFileSize">36 MB</td> <td
       * class="bottomTableFileSize">111 MB</td> <td
       * class="bottomTableFileSize">181 MB</td> <td class="bottomTableEmbed"><a
       * href=
       * "/embed-code.php?movieId=men-in-black-3&amp;source=1&amp;trailerName=Trailer 2&amp;resolutions=480;720;1080"
       * rel="lightbox[embed 600 600]"
       * title="Embed this video on your website">embed</a></td> </tr>
       */
      for (Element t : tr) {
        String date = t.select("td.bottomTableDate").first().text();

        // apple.com urls currently not working (according to hd-trailers)
        String tr0tit = t.select("td.bottomTableResolution > a").get(0).attr("title");
        String tr0url = t.select("td.bottomTableResolution > a").get(0).attr("href");
        System.out.println(date + " | " + tr0tit + " | " + tr0url);

        String tr1tit = t.select("td.bottomTableResolution > a").get(1).attr("title");
        String tr1url = t.select("td.bottomTableResolution > a").get(1).attr("href");
        System.out.println(date + " | " + tr1tit + " | " + tr1url);

        String tr2tit = t.select("td.bottomTableResolution > a").get(2).attr("title");
        String tr2url = t.select("td.bottomTableResolution > a").get(2).attr("href");
        System.out.println(date + " | " + tr2tit + " | " + tr2url);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
    }
  }
}
