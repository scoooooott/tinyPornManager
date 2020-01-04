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

package org.tinymediamanager.core.tvshow;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.MediaFileHelper;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.jmte.NamedArrayRenderer;
import org.tinymediamanager.core.jmte.NamedDateRenderer;
import org.tinymediamanager.core.jmte.NamedNumberRenderer;
import org.tinymediamanager.core.jmte.NamedUpperCaseRenderer;
import org.tinymediamanager.core.jmte.ZeroNumberRenderer;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.DynaEnum;

import com.floreysoft.jmte.Engine;

public class TvShowJmteTests {
  private Engine              engine;
  private Map<String, Object> root;

  @Test
  public void testTvshowPatterns() {
    try {
      TvShow tvShow = createTvShow();

      engine = Engine.createEngine();
      engine.registerRenderer(Number.class, new ZeroNumberRenderer());
      engine.registerNamedRenderer(new NamedDateRenderer());
      engine.registerNamedRenderer(new NamedNumberRenderer());
      engine.registerNamedRenderer(new NamedUpperCaseRenderer());
      engine.registerNamedRenderer(new TvShowRenamer.TvShowNamedFirstCharacterRenderer());
      engine.registerNamedRenderer(new NamedArrayRenderer());
      engine.setModelAdaptor(new TvShowRenamer.TvShowRenamerModelAdaptor());
      root = new HashMap<>();
      root.put("tvShow", tvShow);

      // test single tokens
      compare("${showTitle}", "The 4400");
      compare("${showTitleSortable}", "4400, The");
      compare("${showYear}", "1987");

      // test combined tokens
      compare("${showTitle} (${showYear})", "The 4400 (1987)");

      // test empty brackets
      compare("{ ${showTitle[100]} }", "{  }");

      // direct access
      compare("${tvShow.year}/${tvShow.title}", "1987/The 4400");
      compare("${tvShow.year}/${showTitle[0,2]}", "1987/Th");

      // test parent and space separator expressions
      compare("${parent}", "#" + File.separator + "1987");
      compare("${tvShow.productionCompany}", "FOX (US) HBO");
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
      engine.registerRenderer(Number.class, new ZeroNumberRenderer());
      engine.registerNamedRenderer(new NamedDateRenderer());
      engine.registerNamedRenderer(new NamedNumberRenderer());
      engine.registerNamedRenderer(new NamedUpperCaseRenderer());
      engine.registerNamedRenderer(new TvShowRenamer.TvShowNamedFirstCharacterRenderer());
      engine.registerNamedRenderer(new NamedArrayRenderer());
      engine.setModelAdaptor(new TvShowRenamer.TvShowRenamerModelAdaptor());
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
      compare("${airedDate}", "1987-04-26");

      compare("${videoResolution}", "1280x720");
      compare("${videoFormat}", "720p");
      compare("${videoCodec}", "h264");
      compare("${audioCodec}", "AC3");
      compare("${audioCodecList[1]}", "MP3");
      compare("${audioCodecList[2]}", "");
      compare("${audioChannels}", "6ch");
      compare("${audioChannelList[1]}", "2ch");
      compare("${audioChannelList[2]}", "");
      compare("${audioLanguage}", "en");
      compare("${audioLanguageList[1]}", "de");
      compare("${audioLanguageList[1];upper}", "DE");
      compare("${audioLanguageList[2]}", "");

      compare("${mediaSource}", "Blu-ray");
      compare("${mediaSource.name}", "BLURAY");

      // test combined tokens
      compare("${showTitle} - S${seasonNr2}E${episodeNr2} - ${title}", "The 4400 - S01E03 - Don't Pet the Teacher");

      // test empty brackets
      compare("{ ${showTitle[100]} }", "{  }");

      // test direct access
      compare("${episode.firstAired;date(yyyy - MM - dd)} - ${episode.title}", "1987 - 04 - 26 - Don't Pet the Teacher");
      compare("S${episode.season}E${episodeNr} - ${title[0,2]}", "S1E3 - Do");
    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }
  }

  private void compare(String template, String expectedValue) {
    String actualValue = engine.transform(TvShowRenamer.morphTemplate(template), root);
    assertThat(actualValue).isEqualTo(expectedValue);
  }

  private TvShow createTvShow() throws Exception {
    TvShow tvShow = new TvShow();
    tvShow.setDataSource("/media/tvshows/");
    tvShow.setPath("/media/tvshows/#/1987/21 Jump Street");
    tvShow.setTitle("The 4400");
    tvShow.setYear(1987);
    tvShow.setRating(new MediaRating(MediaRating.NFO, 7.4f, 8));
    tvShow.setCertification(MediaCertification.US_TVPG);
    tvShow.setGenres(Arrays.asList(MediaGenres.ACTION, MediaGenres.ADVENTURE, MediaGenres.DRAMA));
    tvShow.setTvdbId("77585");
    tvShow.setFirstAired("1987-04-12");
    tvShow.setProductionCompany("FOX (US)/HBO");
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
    episode.setYear(1987);
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
    mf.setVideo3DFormat(MediaFileHelper.VIDEO_3D_SBS);

    ArrayList<MediaFileAudioStream> audl = new ArrayList<>();
    MediaFileAudioStream audio = new MediaFileAudioStream();
    audio.setCodec("AC3");
    audio.setLanguage("en");
    audio.setAudioChannels(6);
    audl.add(audio);

    audio = new MediaFileAudioStream();
    audio.setCodec("MP3");
    audio.setLanguage("de");
    audio.setAudioChannels(2);
    audl.add(audio);

    mf.setAudioStreams(audl);

    MediaFileSubtitle sub = new MediaFileSubtitle();
    sub.setLanguage("de");
    mf.addSubtitle(sub);

    episode.addToMediaFiles(mf);

    return episode;
  }

  // @Test
  // public void getProperties() throws Exception {
  // printBeanInfo(TvShow.class);
  // printBeanInfo(TvShowSeason.class);
  // printBeanInfo(TvShowEpisode.class);
  // }
  //
  // private void printBeanInfo(Class clazz) throws Exception {
  // System.out.println("\n\n" + clazz.getName() + "\n");
  //
  // // access properties as Map
  // BeanInfo info = Introspector.getBeanInfo(clazz);
  // PropertyDescriptor[] pds = info.getPropertyDescriptors();
  //
  // for (PropertyDescriptor descriptor : pds) {
  // if ("class".equals(descriptor.getDisplayName())) {
  // continue;
  // }
  //
  // if ("declaringClass".equals(descriptor.getDisplayName())) {
  // continue;
  // }
  //
  // if (descriptor.getReadMethod() != null) {
  // final Type type = descriptor.getReadMethod().getGenericReturnType();
  // if (type instanceof ParameterizedTypeImpl) {
  // ParameterizedType pt = (ParameterizedTypeImpl) type;
  //
  // String typeAsString;
  // Class rawTypeClass = (Class) pt.getRawType();
  // typeAsString = rawTypeClass.getSimpleName() + "\\<";
  //
  // int index = 0;
  // for (Type arg : pt.getActualTypeArguments()) {
  // Class argClass = (Class) arg;
  // typeAsString += getTypeName(argClass);
  //
  // index++;
  //
  // if (index < pt.getActualTypeArguments().length) {
  // typeAsString += ",";
  // }
  // }
  // typeAsString += "\\>";
  // System.out.println("|" + typeAsString + "|" + descriptor.getDisplayName() + "|");
  // }
  // else {
  // System.out.println("|" + getTypeName(descriptor.getReadMethod().getReturnType()) + "|" + descriptor.getDisplayName() + "|");
  // }
  // }
  // }
  // }

  private String getTypeName(Class clazz) {
    String typeAsString;

    Class returnType = clazz;
    if (returnType.isEnum()) {
      typeAsString = "String";
    }
    else if (DynaEnum.class.isAssignableFrom(returnType)) {
      typeAsString = "String";
    }
    else {
      typeAsString = returnType.getSimpleName();
    }
    return typeAsString;
  }
}
