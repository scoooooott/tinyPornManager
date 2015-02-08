/*
 * Copyright 2012 - 2015 Manuel Laggner
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

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
  static final Map<Integer, VideoQuality> itagMap                = new HashMap<Integer, VideoQuality>();

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

  private static Pattern                  patternAge             = Pattern.compile("(verify_age)");
  private static Pattern                  patternUnavailable     = Pattern.compile("(unavailable-player)");
  private static Pattern                  patternUrlencod        = Pattern.compile("\"url_encoded_fmt_stream_map\":\"([^\"]*)\"");
  private static Pattern                  patternUrl             = Pattern.compile("url=(.*)");
  private static Pattern                  patternStream          = Pattern.compile("stream=(.*)");
  private static Pattern                  patternLink            = Pattern.compile("(sparams.*)&itag=(\\d+)&.*&conn=rtmpe(.*),");
  private static Pattern                  patternDecryptFunction = Pattern.compile("signature=(\\w+?)\\([^)]\\)");
  private static Pattern                  patternSubfunction     = Pattern.compile("([a-zA-Z]*?)[.]?(\\w+?)\\([^)]*?\\)");
  private static Pattern                  playerUrlPattern       = Pattern.compile("\\\"assets\\\":\\{.*?\\\"js\\\":\\\"(.*?)\\\"");

  private String                          youtubeUrl;
  private String                          id;
  private String                          jsonConfiguration;
  private String                          playerJavascript;

  public YoutubeLinkExtractor(String youtubeUrl) {
    this.youtubeUrl = youtubeUrl;
  }

  public String extractVideoUrl() throws IOException, InterruptedException {
    id = extractId(youtubeUrl);
    if (StringUtils.isBlank(id)) {
      return "";
    }
    LOGGER.debug("Parsed youtube id: " + id);

    VideoQuality desiredQuality = itagMap.get(extractQuality(youtubeUrl));
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
      Url jsonConfigUrl = new Url(youtubeUrl + "&spf=prefetch");
      StringWriter writer = new StringWriter();
      IOUtils.copy(jsonConfigUrl.getInputStream(), writer, "UTF-8");
      jsonConfiguration = writer.toString();

      List<VideoDownload> downloads = extractJsonInfo();
      // return the first; this is either the desired quality or anything similar
      if (!downloads.isEmpty()) {
        // get the desired quality
        for (VideoDownload dl : downloads) {
          if (dl.vq == desiredQuality) {
            return URLDecoder.decode(dl.url.toExternalForm(), "UTF-8");
          }
        }

        // still not found any useful link.. try to get the best one
        for (VideoDownload dl : downloads) {
          if (dl.vq.ordinal() >= desiredQuality.ordinal()) {
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

  private List<VideoDownload> extractJsonInfo() throws Exception {
    List<VideoDownload> sNextVideoURL = new ArrayList<VideoDownload>();
    {
      Matcher matcher = patternAge.matcher(jsonConfiguration);
      if (matcher.find())
        return sNextVideoURL;
    }
    {
      Matcher matcher = patternUnavailable.matcher(jsonConfiguration);
      if (matcher.find())
        return sNextVideoURL;
    }

    {
      Matcher matcher = patternUrlencod.matcher(jsonConfiguration);
      if (matcher.find()) {
        String url_encoded_fmt_stream_map;
        url_encoded_fmt_stream_map = matcher.group(1);

        // normal embedded video, unable to grab age restricted videos
        Matcher encodMatch = patternUrl.matcher(url_encoded_fmt_stream_map);
        if (encodMatch.find()) {
          String sline = encodMatch.group(1);

          sNextVideoURL.addAll(extractUrlEncodedVideos(sline, id));
        }

        // stream video

        Matcher encodStreamMatch = patternStream.matcher(url_encoded_fmt_stream_map);
        if (encodStreamMatch.find()) {
          String sline = encodStreamMatch.group(1);

          String[] urlStrings = sline.split("stream=");

          for (String urlString : urlStrings) {
            urlString = StringEscapeUtils.unescapeJava(urlString);
            Matcher linkMatch = patternLink.matcher(urlString);
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

    Collections.sort(sNextVideoURL, new VideoUrlComparator());

    return sNextVideoURL;
  }

  private List<VideoDownload> extractUrlEncodedVideos(String sline, String id) throws Exception {
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
            sig = decryptSignature(sig);
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

  private void addVideo(List<VideoDownload> sNextVideoURL, String itag, URL url) {
    Integer i = Integer.decode(itag);
    VideoQuality vd = itagMap.get(i);

    sNextVideoURL.add(new VideoDownload(vd, url));
  }

  private String decryptSignature(String encryptedSignature) throws Exception {
    // first extract the player url and download the js player
    Matcher matcher = playerUrlPattern.matcher(jsonConfiguration);
    if (matcher.find()) {
      // only download the player javascript the first time
      if (StringUtils.isBlank(playerJavascript)) {
        Url jsPlayer = new Url("https:" + matcher.group(1).replaceAll("\\\\", ""));
        StringWriter writer = new StringWriter();
        IOUtils.copy(jsPlayer.getInputStream(), writer, "UTF-8");
        playerJavascript = writer.toString();
      }
      if (StringUtils.isBlank(playerJavascript)) {
        return "";
      }

      // here comes the magic: extract the decrypt JS functions and translate them to Java :)
      matcher = patternDecryptFunction.matcher(playerJavascript);
      if (matcher.find()) {
        String decryptFunction = matcher.group(1);

        // extract relevant JS code
        String javaScript = extractJavascriptCode(playerJavascript, decryptFunction);

        // create a script engine manager
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        engine.eval(javaScript);
        Invocable inv = (Invocable) engine;

        // invoke the function to decrypt the signature
        String result = (String) inv.invokeFunction(decryptFunction, encryptedSignature);

        return result;
      }
    }
    return "";
  }

  private String extractJavascriptCode(String fullSource, String functionName) {
    // get function body
    String functionSource = getMethodBody(fullSource, functionName);

    // and extract all subfunctions
    if (StringUtils.isNotBlank(functionSource)) {
      List<JSObjectMethod> subfunctions = getSubfunctions(functionSource);
      for (JSObjectMethod function : subfunctions) {
        // remove string functions
        if (function.method.equals("split") || function.method.equals("join")) {
          continue;
        }
        // look if the object already have been found
        if (function.object != null) {
          if (functionSource.contains(function.object + "={")) {
            // the whole object has already been added -> continue
            continue;
          }
          // extract the whole object
          Pattern pattern = Pattern.compile("(" + function.object + "=\\{.*?\\});");
          Matcher matcher = pattern.matcher(fullSource);
          if (matcher.find()) {
            functionSource += matcher.group(1);
          }
        }
        else {
          functionSource += getMethodBody(fullSource, function.method);
        }
      }
    }

    return functionSource;
  }

  private String getMethodBody(String fullSource, String functionName) {
    Pattern pattern = Pattern.compile("(function " + functionName + "\\([^)]+?\\)\\{[^}]+?\\})");
    Matcher matcher = pattern.matcher(fullSource);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return "";
  }

  private List<JSObjectMethod> getSubfunctions(String functionSource) {
    boolean first = true;
    List<JSObjectMethod> subfunctions = new ArrayList<JSObjectMethod>();

    // attempt to find all functions which have been called in this function
    Matcher matcher = patternSubfunction.matcher(functionSource);
    while (matcher.find()) {
      // the first result is the function name itself
      if (first) {
        first = false;
        continue;
      }
      subfunctions.add(new JSObjectMethod(matcher.group(1), matcher.group(2)));
    }

    return subfunctions;
  }

  /*****************************************************************************************
   * helper classes
   ****************************************************************************************/
  private class VideoDownload {
    public VideoQuality vq;
    public URL          url;

    public VideoDownload(VideoQuality vq, URL u) {
      this.vq = vq;
      this.url = u;
    }
  }

  private class JSObjectMethod {
    String object;
    String method;

    public JSObjectMethod(String object, String method) {
      this.object = object;
      this.method = method;
    }
  }

  private class VideoUrlComparator implements Comparator<VideoDownload> {
    @Override
    public int compare(VideoDownload o1, VideoDownload o2) {
      if (o1.vq.ordinal() == o2.vq.ordinal()) {
        return 0;
      }
      if (o1.vq.ordinal() > o2.vq.ordinal()) {
        return 1;
      }
      return -1;
    }
  }
}
