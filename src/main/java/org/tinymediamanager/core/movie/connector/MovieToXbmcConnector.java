/*
 * Copyright 2012 - 2020 Manuel Laggner
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

package org.tinymediamanager.core.movie.connector;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileHelper;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.entities.MediaTrailer;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.w3c.dom.Element;

/**
 * the class MovieToXbmcConnector is used to write a classic XBMC/Kodi compatible NFO file
 *
 * @author Manuel Laggner
 */
public class MovieToXbmcConnector extends MovieGenericXmlConnector {
  private static final Logger LOGGER = LoggerFactory.getLogger(MovieToXbmcConnector.class);

  public MovieToXbmcConnector(Movie movie) {
    super(movie);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void addOwnTags() {
    addEpbookmark();
    addTop250();
    addLastplayed();
    addStatusAndCode();
    addFileinfo();
  }

  @Override
  protected void addTrailer() {
    Element trailer = document.createElement("trailer");
    for (MediaTrailer mediaTrailer : new ArrayList<>(movie.getTrailer())) {
      if (mediaTrailer.getInNfo() && !mediaTrailer.getUrl().startsWith("file")) {
        trailer.setTextContent(prepareTrailerForXbmc(mediaTrailer));
        break;
      }
    }
    root.appendChild(trailer);
  }

  private String prepareTrailerForXbmc(MediaTrailer trailer) {
    // youtube trailer are stored in a special notation: plugin://plugin.video.youtube/?action=play_video&videoid=<ID>
    // parse out the ID from the url and store it in the right notation
    Pattern pattern = Pattern.compile("https{0,1}://.*youtube..*/watch\\?v=(.*)$");
    Matcher matcher = pattern.matcher(trailer.getUrl());
    if (matcher.matches()) {
      return "plugin://plugin.video.youtube/?action=play_video&videoid=" + matcher.group(1);
    }

    // other urls are handled by the hd-trailers.net plugin
    pattern = Pattern.compile("https{0,1}://.*(apple.com|yahoo-redir|yahoo.com|youtube.com|moviefone.com|ign.com|hd-trailers.net|aol.com).*");
    matcher = pattern.matcher(trailer.getUrl());
    if (matcher.matches()) {
      try {
        return "plugin://plugin.video.hdtrailers_net/video/" + matcher.group(1) + "/" + URLEncoder.encode(trailer.getUrl(), "UTF-8");
      }
      catch (Exception e) {
        LOGGER.error("failed to escape " + trailer.getUrl());
      }
    }
    // everything else is stored directly
    return trailer.getUrl();
  }

  /**
   * add the <epbookmark>xxx</epbookmark> just before the <year>xxx</year>
   */
  private void addEpbookmark() {
    Element epbookmark = document.createElement("epbookmark");

    Element year = getSingleElementByTag("year");
    if (year != null) {
      if (parser != null) {
        epbookmark.setTextContent(parser.epbookmark);
      }
      root.insertBefore(epbookmark, year);
    }
  }

  /**
   * add the <top250>xxx</top250> just before the <set>xxx</set>
   */
  private void addTop250() {
    Element top250 = document.createElement("top250");
    top250.setTextContent(Integer.toString(movie.getTop250()));
    Element set = getSingleElementByTag("set");
    if (set != null) {
      root.insertBefore(top250, set);
    }
  }

  /**
   * add the <lastplayed>xxx</lastplayed> just before the <genre>xxx</genre>
   */
  private void addLastplayed() {
    Element lastplayed = document.createElement("lastplayed");

    Element genre = getSingleElementByTag("genre");
    if (genre != null) {
      if (parser != null && parser.lastplayed != null) {
        lastplayed.setTextContent(new SimpleDateFormat("yyyy-MM-dd").format(parser.lastplayed));
      }
      root.insertBefore(lastplayed, genre);
    }
  }

  /**
   * add the <status>xxx</status> and <code>xxx</code> just before the <premiered>xxx</premiered>
   */
  private void addStatusAndCode() {
    Element status = document.createElement("status");
    Element code = document.createElement("code");

    Element premiered = getSingleElementByTag("premiered");
    if (premiered != null) {
      if (parser != null) {
        status.setTextContent(parser.status);
        code.setTextContent(parser.code);
      }
      root.insertBefore(status, premiered);
      root.insertBefore(code, premiered);
    }
  }

  /**
   * add the <fileinfo>xx</fileinfo> tag with mediainfo data
   */
  private void addFileinfo() {
    Element fileinfo = document.createElement("fileinfo");
    Element streamdetails = document.createElement("streamdetails");

    MediaFile vid = movie.getMainVideoFile();
    if (vid != null) {
      {
        Element video = document.createElement("video");

        Element codec = document.createElement("codec");
        // workaround for h265/hevc since Kodi just "knows" hevc
        // https://forum.kodi.tv/showthread.php?tid=354886&pid=2955329#pid2955329
        if ("h265".equalsIgnoreCase(vid.getVideoCodec())) {
          codec.setTextContent("HEVC");
        }
        else {
          codec.setTextContent(vid.getVideoCodec());
        }
        video.appendChild(codec);

        Element aspect = document.createElement("aspect");
        aspect.setTextContent(Float.toString(vid.getAspectRatio()));
        video.appendChild(aspect);

        Element width = document.createElement("width");
        width.setTextContent(Integer.toString(vid.getVideoWidth()));
        video.appendChild(width);

        Element height = document.createElement("height");
        height.setTextContent(Integer.toString(vid.getVideoHeight()));
        video.appendChild(height);

        // does not work reliable for disc style movies, MediaInfo and even Kodi write weird values in there
        if (!movie.isDisc() && !movie.getMainVideoFile().getExtension().equalsIgnoreCase("iso")) {
          Element durationinseconds = document.createElement("durationinseconds");
          durationinseconds.setTextContent(Integer.toString(movie.getRuntimeFromMediaFiles()));
          video.appendChild(durationinseconds);
        }

        Element stereomode = document.createElement("stereomode");
        // "Spec": https://github.com/xbmc/xbmc/blob/master/xbmc/guilib/StereoscopicsManager.cpp
        if (vid.getVideo3DFormat().equals(MediaFileHelper.VIDEO_3D_SBS) || vid.getVideo3DFormat().equals(MediaFileHelper.VIDEO_3D_HSBS)) {
          stereomode.setTextContent("left_right");
        }
        else if (vid.getVideo3DFormat().equals(MediaFileHelper.VIDEO_3D_TAB) || vid.getVideo3DFormat().equals(MediaFileHelper.VIDEO_3D_HTAB)) {
          stereomode.setTextContent("top_bottom"); // maybe?
        }
        video.appendChild(stereomode);

        streamdetails.appendChild(video);
      }

      for (MediaFileAudioStream audioStream : vid.getAudioStreams()) {
        Element audio = document.createElement("audio");

        Element codec = document.createElement("codec");
        codec.setTextContent(audioStream.getCodec());
        audio.appendChild(codec);

        Element language = document.createElement("language");
        language.setTextContent(audioStream.getLanguage());
        audio.appendChild(language);

        Element channels = document.createElement("channels");
        channels.setTextContent(Integer.toString(audioStream.getAudioChannels()));
        audio.appendChild(channels);

        streamdetails.appendChild(audio);
      }

      // also include external audio files if set
      if (MovieModuleManager.SETTINGS.isIncludeExternalAudioStreams()) {
        for (MediaFile audioFile : movie.getMediaFiles(MediaFileType.AUDIO)) {
          for (MediaFileAudioStream audioStream : vid.getAudioStreams()) {
            Element audio = document.createElement("audio");

            Element codec = document.createElement("codec");
            codec.setTextContent(audioStream.getCodec());
            audio.appendChild(codec);

            Element language = document.createElement("language");
            language.setTextContent(audioStream.getLanguage());
            audio.appendChild(language);

            Element channels = document.createElement("channels");
            channels.setTextContent(Integer.toString(audioStream.getAudioChannels()));
            audio.appendChild(channels);

            streamdetails.appendChild(audio);
          }
        }
      }

      for (MediaFileSubtitle sub : vid.getSubtitles()) {
        Element subtitle = document.createElement("subtitle");

        Element language = document.createElement("language");
        language.setTextContent(sub.getLanguage());
        subtitle.appendChild(language);

        streamdetails.appendChild(subtitle);
      }
    }

    // add external subtitles to NFO
    for (MediaFile mediaFile : movie.getMediaFiles(MediaFileType.SUBTITLE)) {
      for (MediaFileSubtitle sub : mediaFile.getSubtitles()) {
        Element subtitle = document.createElement("subtitle");

        Element language = document.createElement("language");
        language.setTextContent(sub.getLanguage());
        subtitle.appendChild(language);

        streamdetails.appendChild(subtitle);
      }
    }

    fileinfo.appendChild(streamdetails);
    root.appendChild(fileinfo);
  }
}
