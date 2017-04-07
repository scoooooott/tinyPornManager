/*
 * Copyright 2012 - 2017 Manuel Laggner
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

package org.tinymediamanager.core.tvshow;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.jmte.NamedNumberRenderer;
import org.tinymediamanager.core.jmte.TmmModelAdaptor;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaGenres;

import com.floreysoft.jmte.Engine;

public class TvShowJmteTests {
  private static Map<String, String> TOKEN_MAP = new HashMap<>();
  private Engine                     engine;
  private Map<String, Object>        root;

  @BeforeClass
  public static void init() {
    // TV show tags
    TOKEN_MAP.put("showTitle", "tvShow.title");
    TOKEN_MAP.put("showTitleSortable", "tvShow.titleSortable");
    TOKEN_MAP.put("showYear", "tvShow.year");

    // episode tags
    TOKEN_MAP.put("episodeNr", "episode.episode");
    TOKEN_MAP.put("episodeNr2", "episode.episode;number(%02d)");
    TOKEN_MAP.put("episodeNrDvd", "episode.dvdEpisode");
    TOKEN_MAP.put("seasonNr", "episode.season");
    TOKEN_MAP.put("seasonNr2", "episode.season;number(%02d)");
    TOKEN_MAP.put("seasonNrDvd", "episode.dvdSeason");
    TOKEN_MAP.put("title", "episode.title");
    TOKEN_MAP.put("year", "episode.year");
    // TOKEN_MAP.put("airedDate", "episode.firstAired");

    TOKEN_MAP.put("videoCodec", "episode.mediaInfoVideoCodec");
    TOKEN_MAP.put("videoFormat", "episode.mediaInfoVideoFormat");
    TOKEN_MAP.put("videoResolution", "episode.mediaInfoVideoResolution");
    TOKEN_MAP.put("audioCodec", "episode.mediaInfoAudioCodec");
    TOKEN_MAP.put("audioChannels", "episode.mediaInfoAudioChannels");
    TOKEN_MAP.put("3Dformat", "episode.video3DFormat");

    TOKEN_MAP.put("mediaSource", "episode.mediaSource");
  }

  @Test
  public void testTvshowPatterns() {
    try {
      TvShow tvShow = createTvShow();

      engine = Engine.createEngine();
      engine.setModelAdaptor(new TmmModelAdaptor());
      root = new HashMap<>();
      root.put("tvShow", tvShow);

      // test single tokens
      compare("${showTitle}", "21 Jump Street");
      compare("${showTitleSortable}", "21 Jump Street");
      compare("${showYear}", "1987");
    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }
  }

  @Test
  public void testEpisodePatterns() {
    try {
      TvShowEpisode episode = createEpisode();

      engine = Engine.createEngine();
      engine.setModelAdaptor(new TmmModelAdaptor());
      engine.registerNamedRenderer(new NamedNumberRenderer());
      root = new HashMap<>();
      root.put("episode", episode);
      root.put("tvShow", episode.getTvShow());

      // test single tokens from the TV show
      compare("${showTitle}", "The 4400");
      compare("${showTitleSortable}", "4400, The");
      compare("${showYear}", "1987");

      // test single tokens from the episode
      compare("${episodeNr}", "3");
      compare("${episodeNr2}", "03");
      compare("${episodeNrDvd}", "5");
      compare("${seasonNr}", "1");
      compare("${seasonNr2}", "01");
      compare("${seasonNrDvd}", "1");
      compare("${title}", "Don't Pet the Teacher");
      compare("${year}", "1987");
      // compare("${airedDate}", "1987-04-26");

      compare("${videoResolution}", "1280x720");
      compare("${videoFormat}", "720p");
      compare("${videoCodec}", "h264");
      compare("${audioCodec}", "AC3");
      compare("${audioChannels}", "6");

      compare("${mediaSource}", "Bluray");
      compare("${mediaSource.name}", "BLURAY");
    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }
  }

  private void compare(String template, String expectedValue) {
    String actualValue = engine.transform(morphTemplate(template), root);
    assertThat(actualValue).isEqualTo(expectedValue);
  }

  private String morphTemplate(String template) {
    String morphedTemplate = template;
    for (Map.Entry<String, String> entry : TOKEN_MAP.entrySet()) {
      Pattern pattern = Pattern.compile("\\$\\{" + entry.getKey() + "([^a-zA-Z0-9])", Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(template);
      while (matcher.find()) {
        morphedTemplate = morphedTemplate.replace(matcher.group(), "${" + entry.getValue() + matcher.group(1));
      }
    }
    return morphedTemplate;
  }

  private TvShow createTvShow() throws Exception {
    TvShow tvShow = new TvShow();
    tvShow.setPath("/media/tvshows/21 Jump Street");
    tvShow.setTitle("The 4400");
    tvShow.setYear("1987");
    tvShow.setRating(7.4f);
    tvShow.setVotes(8);
    tvShow.setCertification(Certification.US_TVPG);
    tvShow.setGenres(Arrays.asList(MediaGenres.ACTION, MediaGenres.ADVENTURE, MediaGenres.DRAMA));
    tvShow.setTvdbId("77585");
    tvShow.setFirstAired("1987-04-12");
    tvShow.setProductionCompany("FOX (US)");
    return tvShow;
  }

  private TvShowEpisode createEpisode() throws Exception {
    TvShowEpisode episode = new TvShowEpisode();
    episode.setTvShow(createTvShow());

    episode.setSeason(1);
    episode.setEpisode(3);
    episode.setDvdSeason(1);
    episode.setDvdEpisode(5);
    episode.setTitle("Don't Pet the Teacher");
    episode.setYear("1987");
    episode.setFirstAired("1987-04-26");
    episode.setMediaSource(MediaSource.BLURAY);

    MediaFile mf = new MediaFile();
    mf.setType(MediaFileType.VIDEO);
    mf.setFilename("Aladdin.mkv");
    mf.setVideoCodec("h264");
    mf.setVideoHeight(720);
    mf.setVideoWidth(1280);
    mf.setDuration(3600);
    mf.setOverallBitRate(3500);
    mf.setVideo3DFormat(MediaFile.VIDEO_3D_SBS);

    MediaFileAudioStream audio = new MediaFileAudioStream();
    audio.setCodec("AC3");
    audio.setLanguage("en");
    audio.setChannels("6");
    mf.setAudioStreams(Arrays.asList(audio));

    MediaFileSubtitle sub = new MediaFileSubtitle();
    sub.setLanguage("de");
    mf.addSubtitle(sub);

    episode.addToMediaFiles(mf);

    return episode;
  }
}
