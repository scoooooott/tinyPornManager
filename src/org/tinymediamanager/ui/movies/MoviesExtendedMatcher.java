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
package org.tinymediamanager.ui.movies;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieActor;
import org.tinymediamanager.core.movie.entities.MovieProducer;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaGenres;

import ca.odell.glazedlists.matchers.Matcher;

/**
 * The Class MoviesExtendedMatcher.
 * 
 * @author Manuel Laggner
 */
public class MoviesExtendedMatcher implements Matcher<Movie> {
  public enum SearchOptions {
    DUPLICATES, WATCHED, GENRE, CERTIFICATION, CAST, TAG, MOVIESET, VIDEO_FORMAT, VIDEO_CODEC, AUDIO_CODEC, DATASOURCE, MISSING_METADATA,
    MISSING_ARTWORK, MISSING_SUBTITLES, NEW_MOVIES
  }

  private HashMap<SearchOptions, Object> searchOptions;

  /**
   * Instantiates a new movies extended matcher.
   * 
   * @param searchOptions
   *          the search options
   */
  public MoviesExtendedMatcher(HashMap<SearchOptions, Object> searchOptions) {
    this.searchOptions = searchOptions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ca.odell.glazedlists.matchers.Matcher#matches(java.lang.Object)
   */
  @Override
  public boolean matches(Movie movie) {
    // not null
    if (movie == null) {
      return false;
    }

    // check duplicates
    if (searchOptions.containsKey(SearchOptions.DUPLICATES)) {
      if (!movie.isDuplicate()) {
        return false;
      }
    }

    // check against watched flag
    if (searchOptions.containsKey(SearchOptions.WATCHED)) {
      boolean watched = (Boolean) searchOptions.get(SearchOptions.WATCHED);
      boolean result = !(movie.isWatched() ^ watched);
      if (result == false) {
        return false;
      }
    }

    // check against genre
    if (searchOptions.containsKey(SearchOptions.GENRE)) {
      MediaGenres genre = (MediaGenres) searchOptions.get(SearchOptions.GENRE);
      if (!movie.getGenres().contains(genre)) {
        return false;
      }
    }

    // check against certification
    if (searchOptions.containsKey(SearchOptions.CERTIFICATION)) {
      Certification cert = (Certification) searchOptions.get(SearchOptions.CERTIFICATION);
      if (cert != movie.getCertification()) {
        return false;
      }
    }

    // check against cast member
    if (searchOptions.containsKey(SearchOptions.CAST)) {
      String castSearch = (String) searchOptions.get(SearchOptions.CAST);
      if (!containsCast(movie, castSearch)) {
        return false;
      }
    }

    // check against tag
    if (searchOptions.containsKey(SearchOptions.TAG)) {
      String tag = (String) searchOptions.get(SearchOptions.TAG);
      if (!containsTag(movie, tag)) {
        return false;
      }
    }

    // check against MOVIESET
    if (searchOptions.containsKey(SearchOptions.MOVIESET)) {
      Boolean isInSet = (Boolean) searchOptions.get(SearchOptions.MOVIESET);
      if ((movie.getMovieSet() != null) != isInSet) {
        return false;
      }
    }

    // check against video format
    if (searchOptions.containsKey(SearchOptions.VIDEO_FORMAT)) {
      String videoFormat = (String) searchOptions.get(SearchOptions.VIDEO_FORMAT);
      if (videoFormat == MediaFile.VIDEO_FORMAT_HD || videoFormat == MediaFile.VIDEO_FORMAT_SD) {
        if (videoFormat == MediaFile.VIDEO_FORMAT_HD && !isVideoHD(movie.getMediaInfoVideoFormat())) {
          return false;
        }
        if (videoFormat == MediaFile.VIDEO_FORMAT_SD && isVideoHD(movie.getMediaInfoVideoFormat())) {
          return false;
        }
      }
      else {
        if (videoFormat != movie.getMediaInfoVideoFormat()) {
          return false;
        }
      }
    }

    // check against video codec
    if (searchOptions.containsKey(SearchOptions.VIDEO_CODEC)) {
      String videoCodec = (String) searchOptions.get(SearchOptions.VIDEO_CODEC);
      if (!videoCodec.equals(movie.getMediaInfoVideoCodec())) {
        return false;
      }
    }

    // check against audio codec
    if (searchOptions.containsKey(SearchOptions.AUDIO_CODEC)) {
      String audioCodec = (String) searchOptions.get(SearchOptions.AUDIO_CODEC);
      if (!containsAudioCodec(movie, audioCodec)) {
        return false;
      }
    }

    // check against datasource
    if (searchOptions.containsKey(SearchOptions.DATASOURCE)) {
      String datasource = (String) searchOptions.get(SearchOptions.DATASOURCE);
      if (!new File(datasource).equals(new File(movie.getDataSource()))) {
        return false;
      }
    }

    // check against missing metadata
    if (searchOptions.containsKey(SearchOptions.MISSING_METADATA)) {
      if (movie.isScraped()) {
        return false;
      }
    }

    // check against missing artwork
    if (searchOptions.containsKey(SearchOptions.MISSING_ARTWORK)) {
      if (movie.getHasImages()) {
        return false;
      }
    }

    // check against missing subtitles
    if (searchOptions.containsKey(SearchOptions.MISSING_SUBTITLES)) {
      if (movie.hasSubtitles()) {
        return false;
      }
    }

    // check against new movies
    if (searchOptions.containsKey(SearchOptions.NEW_MOVIES)) {
      if (!movie.isNewlyAdded()) {
        return false;
      }
    }

    return true;
  }

  private boolean isVideoHD(String videoFormat) {
    if (videoFormat == MediaFile.VIDEO_FORMAT_720P) {
      return true;
    }
    if (videoFormat == MediaFile.VIDEO_FORMAT_1080P) {
      return true;
    }
    if (videoFormat == MediaFile.VIDEO_FORMAT_4K) {
      return true;
    }
    if (videoFormat == MediaFile.VIDEO_FORMAT_8K) {
      return true;
    }
    return false;
  }

  private boolean containsAudioCodec(Movie movie, String codec) {
    List<MediaFile> videoFiles = movie.getMediaFiles(MediaFileType.VIDEO);

    if (videoFiles.size() == 0) {
      return false;
    }

    MediaFile mf = videoFiles.get(0);
    for (MediaFileAudioStream stream : mf.getAudioStreams()) {
      if (codec.equals(stream.getCodec())) {
        return true;
      }
    }

    return false;
  }

  private boolean containsTag(Movie movie, String tag) {
    for (String tagInMovie : movie.getTags()) {
      if (tagInMovie.equals(tag)) {
        return true;
      }
    }

    return false;
  }

  private boolean containsCast(Movie movie, String name) {
    if (StringUtils.isNotEmpty(name)) {
      Pattern pattern = Pattern.compile("(?i)" + Pattern.quote(name));
      java.util.regex.Matcher matcher = null;

      // director
      if (StringUtils.isNotEmpty(movie.getDirector())) {
        matcher = pattern.matcher(movie.getDirector());
        if (matcher.find()) {
          return true;
        }
      }

      // writer
      if (StringUtils.isNotEmpty(movie.getWriter())) {
        matcher = pattern.matcher(movie.getWriter());
        if (matcher.find()) {
          return true;
        }
      }

      // actors
      for (MovieActor cast : movie.getActors()) {
        if (StringUtils.isNotEmpty(cast.getName())) {
          matcher = pattern.matcher(cast.getName());
          if (matcher.find()) {
            return true;
          }
        }
      }

      // producers
      for (MovieProducer producer : movie.getProducers()) {
        if (StringUtils.isNotEmpty(producer.getName())) {
          matcher = pattern.matcher(producer.getName());
          if (matcher.find()) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
