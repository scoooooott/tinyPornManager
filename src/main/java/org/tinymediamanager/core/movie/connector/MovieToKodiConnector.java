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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileHelper;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.MediaTrailer;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.MediaMetadata;
import org.w3c.dom.Element;

/**
 * the class MovieToKodiConnector is used to write a the most recent Kodi compatible NFO file
 *
 * @author Manuel Laggner
 */
public class MovieToKodiConnector extends MovieGenericXmlConnector {
  private static final Logger LOGGER = LoggerFactory.getLogger(MovieToKodiConnector.class);

  public MovieToKodiConnector(Movie movie) {
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

  /**
   * the new set style<br />
   * <set><name>xxx</name><overview>xxx</overview></set>
   */
  @Override
  protected void addSet() {
    Element set = document.createElement("set");

    if (movie.getMovieSet() != null) {
      Element name = document.createElement("name");
      name.setTextContent(movie.getMovieSet().getTitle());
      set.appendChild(name);

      Element overview = document.createElement("overview");
      overview.setTextContent(movie.getMovieSet().getPlot());
      set.appendChild(overview);
    }
    root.appendChild(set);
  }

  /**
   * the new thumb style<br />
   * <thumb aspect="poster">xxx</thumb>
   */
  @Override
  protected void addThumb() {
    Element thumb = document.createElement("thumb");

    String posterUrl = movie.getArtworkUrl(MediaFileType.POSTER);
    if (StringUtils.isNotBlank(posterUrl)) {
      thumb.setAttribute("aspect", "poster");
      thumb.setTextContent(posterUrl);
    }

    root.appendChild(thumb);
  }

  /**
   * the new fanart style<br />
   * <fanart><thumb>xxx</thumb></fanart>
   */
  @Override
  protected void addFanart() {
    Element fanart = document.createElement("fanart");

    String fanarUrl = movie.getArtworkUrl(MediaFileType.FANART);
    if (StringUtils.isNotBlank(fanarUrl)) {
      Element thumb = document.createElement("thumb");
      thumb.setTextContent(fanarUrl);
      fanart.appendChild(thumb);
    }

    root.appendChild(fanart);
  }

  @Override
  protected void addTrailer() {
    Element trailer = document.createElement("trailer");

    // only add a trailer if there is no physical trailer due to a bug in kodi
    // https://forum.kodi.tv/showthread.php?tid=348759&pid=2900477#pid2900477
    if (movie.getMediaFiles(MediaFileType.TRAILER).isEmpty()) {
      for (MediaTrailer mediaTrailer : new ArrayList<>(movie.getTrailer())) {
        if (mediaTrailer.getInNfo() && !mediaTrailer.getUrl().startsWith("file")) {
          trailer.setTextContent(prepareTrailerForKodi(mediaTrailer));
          break;
        }
      }
    }
    root.appendChild(trailer);
  }

  private String prepareTrailerForKodi(MediaTrailer trailer) {
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
   * write the new rating style<br />
   * <ratings> <rating name="default" max="10" default="true"> <value>5.800000</value> <votes>2100</votes> </rating> <rating name="imdb">
   * <value>8.9</value> <votes>12345</votes> </rating> </ratings>
   */
  @Override
  protected void addRating() {
    Element ratings = document.createElement("ratings");

    for (MediaRating r : movie.getRatings().values()) {
      // skip user ratings here
      if (MediaRating.USER.equals(r.getId())) {
        continue;
      }

      Element rating = document.createElement("rating");

      // Kodi needs themoviedb instead of tmdb
      if (MediaMetadata.TMDB.equals(r.getId())) {
        rating.setAttribute("name", "themoviedb");
      }
      else {
        rating.setAttribute("name", r.getId());
      }
      rating.setAttribute("max", String.valueOf(r.getMaxValue()));

      MediaRating mainMediaRating = movie.getRating();
      rating.setAttribute("default", r == mainMediaRating ? "true" : "false");

      Element value = document.createElement("value");
      value.setTextContent(Float.toString(r.getRating()));
      rating.appendChild(value);

      Element votes = document.createElement("votes");
      votes.setTextContent(Integer.toString(r.getVotes()));
      rating.appendChild(votes);

      ratings.appendChild(rating);
    }

    root.appendChild(ratings);
  }

  /**
   * votes are now in the ratings tag
   */
  @Override
  protected void addVotes() {
  }

  /**
   * add the <epbookmark>xxx</epbookmark> just before the <year>xxx</year>
   */
  private void addEpbookmark() {
    Element epbookmark = document.createElement("epbookmark");

    Element year = getSingleElementByTag("year");
    if (parser != null) {
      epbookmark.setTextContent(parser.epbookmark);
    }
    root.insertBefore(epbookmark, year);
  }

  /**
   * add the <top250>xxx</top250> just before the <set>xxx</set>
   */
  private void addTop250() {
    Element top250 = document.createElement("top250");
    top250.setTextContent(Integer.toString(movie.getTop250()));
    Element set = getSingleElementByTag("set");
    root.insertBefore(top250, set);
  }

  /**
   * add the <lastplayed>xxx</lastplayed> just before the <genre>xxx</genre>
   */
  private void addLastplayed() {
    Element lastplayed = document.createElement("lastplayed");

    Element genre = getSingleElementByTag("genre");
    if (parser != null && parser.lastplayed != null) {
      lastplayed.setTextContent(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(parser.lastplayed));
    }
    root.insertBefore(lastplayed, genre);
  }

  /**
   * add the <status>xxx</status> and <code>xxx</code> just before the <premiered>xxx</premiered>
   */
  private void addStatusAndCode() {
    Element status = document.createElement("status");
    Element code = document.createElement("code");

    Element premiered = getSingleElementByTag("premiered");
    if (parser != null) {
      status.setTextContent(parser.status);
      code.setTextContent(parser.code);
    }
    root.insertBefore(status, premiered);
    root.insertBefore(code, premiered);
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
        switch (vid.getVideo3DFormat()) {
          case MediaFileHelper.VIDEO_3D_SBS:
          case MediaFileHelper.VIDEO_3D_HSBS:
            stereomode.setTextContent("left_right");
            break;

          case MediaFileHelper.VIDEO_3D_TAB:
          case MediaFileHelper.VIDEO_3D_HTAB:
            stereomode.setTextContent("top_bottom");
            break;

          default:
            break;
        }
        video.appendChild(stereomode);

        streamdetails.appendChild(video);
      }

      for (MediaFileAudioStream audioStream : vid.getAudioStreams()) {
        Element audio = document.createElement("audio");

        Element codec = document.createElement("codec");
        codec.setTextContent(audioStream.getCodec().replaceAll("-", "_"));
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
