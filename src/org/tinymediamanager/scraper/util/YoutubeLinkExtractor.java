/*
 * Copyright 2012 - 2014 Manuel Laggner
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
package org.tinymediamanager.scraper.util;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.movie.MovieModuleManager;

/**
 * Extract download links/video urls from a youtube url
 * 
 * @author Manuel Laggner
 */
public class YoutubeLinkExtractor {
  private static final Logger LOGGER = LoggerFactory.getLogger(YoutubeLinkExtractor.class);

  private enum VideoQuality {
    p3072, p2304, p1080, p720, p520, p480, p360, p270, p240, p224, p144
  }

  // http://en.wikipedia.org/wiki/YouTube#Quality_and_codecs
  static final Map<Integer, VideoQuality> itagMap = new HashMap<Integer, VideoQuality>();

  static {
    itagMap.put(264, VideoQuality.p1080);
    itagMap.put(248, VideoQuality.p1080);
    itagMap.put(247, VideoQuality.p720);
    itagMap.put(246, VideoQuality.p480);
    itagMap.put(245, VideoQuality.p480);
    itagMap.put(244, VideoQuality.p480);
    itagMap.put(243, VideoQuality.p360);
    itagMap.put(242, VideoQuality.p240);
    itagMap.put(137, VideoQuality.p1080);
    itagMap.put(136, VideoQuality.p720);
    itagMap.put(135, VideoQuality.p480);
    itagMap.put(134, VideoQuality.p360);
    itagMap.put(133, VideoQuality.p240);
    itagMap.put(120, VideoQuality.p720);
    itagMap.put(102, VideoQuality.p720);
    itagMap.put(101, VideoQuality.p360);
    itagMap.put(100, VideoQuality.p360);
    itagMap.put(85, VideoQuality.p1080);
    itagMap.put(84, VideoQuality.p720);
    itagMap.put(83, VideoQuality.p480);
    itagMap.put(82, VideoQuality.p360);
    itagMap.put(46, VideoQuality.p1080);
    itagMap.put(45, VideoQuality.p720);
    itagMap.put(44, VideoQuality.p480);
    itagMap.put(43, VideoQuality.p360);
    itagMap.put(38, VideoQuality.p3072);
    itagMap.put(37, VideoQuality.p1080);
    itagMap.put(36, VideoQuality.p240);
    itagMap.put(35, VideoQuality.p480);
    itagMap.put(34, VideoQuality.p360);
    itagMap.put(22, VideoQuality.p720);
    itagMap.put(18, VideoQuality.p360);
    itagMap.put(17, VideoQuality.p144);
    itagMap.put(6, VideoQuality.p270);
    itagMap.put(5, VideoQuality.p240);
  }

  public static String extractVideoUrl(String url) throws IOException, InterruptedException {
    String id = extractId(url);
    if (StringUtils.isBlank(id)) {
      return "";
    }
    LOGGER.debug("Parsed youtube id: " + id);

    VideoQuality desiredQuality = itagMap.get(extractQuality(url));
    if (desiredQuality == null) {
      // try to pick the quality via settings
      switch (MovieModuleManager.MOVIE_SETTINGS.getTrailerQuality()) {
        case HD_1080:
          desiredQuality = VideoQuality.p1080;
          break;

        case HD_720:
          desiredQuality = VideoQuality.p720;
          break;

        default:
          desiredQuality = VideoQuality.p480;
          break;
      }

    }

    // get the info page
    try {
      Url steamPage = new Url(url);
      StringWriter writer = new StringWriter();
      IOUtils.copy(steamPage.getInputStream(), writer, "UTF-8");
      String streampage = writer.toString();

      List<VideoDownload> downloads = extractHtmlInfo(streampage);
      // return the first; this is either the desired quality or anything similar
      if (!downloads.isEmpty()) {
        // get the desired quality
        for (VideoDownload dl : downloads) {
          if (dl.vq == desiredQuality) {
            return URLDecoder.decode(dl.url.toExternalForm(), "UTF-8");
          }
        }
      }
    }
    catch (MalformedURLException e) {
      throw e;
    }
    catch (Exception e) {
      return "";
    }

    return "";
  }

  /**
   * extracts the youtube id from the given url
   * 
   * @param url
   *          to url to extract the youtube id
   * @return the youtube id (or an empty string if nothing found)
   */
  public static String extractId(String url) {
    {
      Pattern u = Pattern.compile("youtube.com/watch?.*v=([^&]*)");
      Matcher um = u.matcher(url.toString());
      if (um.find()) {
        return um.group(1);
      }
    }

    {
      Pattern u = Pattern.compile("youtube.com/v/([^&]*)");
      Matcher um = u.matcher(url.toString());
      if (um.find()) {
        return um.group(1);
      }
    }

    return "";
  }

  /**
   * extracts the quality id from the given url
   * 
   * @param url
   *          url to extract the quality
   * @return the quality id (or an empty string if nothing found)
   */
  public static int extractQuality(String url) {
    {
      Pattern u = Pattern.compile("youtube.com/watch?.*fmt=([^&]*)");
      Matcher um = u.matcher(url.toString());
      if (um.find()) {
        try {
          return Integer.parseInt(um.group(1));
        }
        catch (NumberFormatException e) {
        }
      }
    }

    {
      Pattern u = Pattern.compile("youtube.com/v/.*fmt=([^&]*)");
      Matcher um = u.matcher(url.toString());
      if (um.find()) {
        try {
          return Integer.parseInt(um.group(1));
        }
        catch (NumberFormatException e) {
        }
      }
    }

    return 0;
  }

  private static List<VideoDownload> extractHtmlInfo(String html) throws Exception {
    List<VideoDownload> sNextVideoURL = new ArrayList<VideoDownload>();
    {
      Pattern age = Pattern.compile("(verify_age)");
      Matcher ageMatch = age.matcher(html);
      if (ageMatch.find())
        return sNextVideoURL;
    }

    {
      Pattern age = Pattern.compile("(unavailable-player)");
      Matcher ageMatch = age.matcher(html);
      if (ageMatch.find())
        return sNextVideoURL;
    }

    {
      Pattern urlencod = Pattern.compile("\"url_encoded_fmt_stream_map\": \"([^\"]*)\"");
      Matcher urlencodMatch = urlencod.matcher(html);
      if (urlencodMatch.find()) {
        String url_encoded_fmt_stream_map;
        url_encoded_fmt_stream_map = urlencodMatch.group(1);

        // normal embedded video, unable to grab age restricted videos
        Pattern encod = Pattern.compile("url=(.*)");
        Matcher encodMatch = encod.matcher(url_encoded_fmt_stream_map);
        if (encodMatch.find()) {
          String sline = encodMatch.group(1);

          sNextVideoURL.addAll(extractUrlEncodedVideos(sline));
        }

        // stream video
        Pattern encodStream = Pattern.compile("stream=(.*)");
        Matcher encodStreamMatch = encodStream.matcher(url_encoded_fmt_stream_map);
        if (encodStreamMatch.find()) {
          String sline = encodStreamMatch.group(1);

          String[] urlStrings = sline.split("stream=");

          for (String urlString : urlStrings) {
            urlString = StringEscapeUtils.unescapeJava(urlString);

            Pattern link = Pattern.compile("(sparams.*)&itag=(\\d+)&.*&conn=rtmpe(.*),");
            Matcher linkMatch = link.matcher(urlString);
            if (linkMatch.find()) {

              String sparams = linkMatch.group(1);
              String itag = linkMatch.group(2);
              String url = linkMatch.group(3);

              url = "http" + url + "?" + sparams;

              url = URLDecoder.decode(url, "UTF-8");

              addVideo(sNextVideoURL, itag, new URL(url));
            }
          }
        }
      }
    }
    // adaptive trailer are kinda useless: die video stream is separated from the audio stream :(
    // {
    // Pattern urlencod = Pattern.compile("\"adaptive_fmts\": \"([^\"]*)\"");
    // Matcher urlencodMatch = urlencod.matcher(html);
    // if (urlencodMatch.find()) {
    // String adaptive_fmts;
    // adaptive_fmts = urlencodMatch.group(1);
    //
    // // normal embedded video, unable to grab age restricted videos
    // Pattern encod = Pattern.compile("url=(.*)");
    // Matcher encodMatch = encod.matcher(adaptive_fmts);
    // if (encodMatch.find()) {
    // String sline = encodMatch.group(1);
    //
    // sNextVideoURL.addAll(extractUrlEncodedVideos(sline));
    // }
    // }
    // }

    return sNextVideoURL;
  }

  private static List<VideoDownload> extractUrlEncodedVideos(String sline) throws Exception {
    List<VideoDownload> sNextVideoURL = new ArrayList<VideoDownload>();
    String[] urlStrings = sline.split("url=");

    for (String urlString : urlStrings) {
      urlString = StringEscapeUtils.unescapeJava(urlString);

      String urlFull = URLDecoder.decode(urlString, "UTF-8");

      // universal request
      {
        String url = null;
        {
          Pattern link = Pattern.compile("([^&,]*)[&,]");
          Matcher linkMatch = link.matcher(urlString);
          if (linkMatch.find()) {
            url = linkMatch.group(1);
            url = URLDecoder.decode(url, "UTF-8");
          }
        }

        String itag = null;
        {
          Pattern link = Pattern.compile("itag=(\\d+)");
          Matcher linkMatch = link.matcher(urlFull);
          if (linkMatch.find()) {
            itag = linkMatch.group(1);
          }
        }

        String sig = null;

        if (sig == null) {
          Pattern link = Pattern.compile("&signature=([^&,]*)");
          Matcher linkMatch = link.matcher(urlFull);
          if (linkMatch.find()) {
            sig = linkMatch.group(1);
          }
        }

        if (sig == null) {
          Pattern link = Pattern.compile("sig=([^&,]*)");
          Matcher linkMatch = link.matcher(urlFull);
          if (linkMatch.find()) {
            sig = linkMatch.group(1);
          }
        }

        if (sig == null) {
          Pattern link = Pattern.compile("[&,]s=([^&,]*)");
          Matcher linkMatch = link.matcher(urlFull);
          if (linkMatch.find()) {
            sig = linkMatch.group(1);
          }
        }

        if (url != null && itag != null && sig != null) {
          try {
            url += "&signature=" + sig;

            addVideo(sNextVideoURL, itag, new URL(url));
            continue;
          }
          catch (MalformedURLException e) {
            // ignore bad urls
          }
        }
      }
    }
    return sNextVideoURL;
  }

  private static void addVideo(List<VideoDownload> sNextVideoURL, String itag, URL url) {
    Integer i = Integer.decode(itag);
    VideoQuality vd = itagMap.get(i);

    sNextVideoURL.add(new VideoDownload(vd, url));
  }

  /*****************************************************************************************
   * helper classes
   ****************************************************************************************/
  static private class VideoDownload {
    public VideoQuality vq;
    public URL          url;

    public VideoDownload(VideoQuality vq, URL u) {
      this.vq = vq;
      this.url = u;
    }
  }
}
