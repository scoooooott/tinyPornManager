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

package org.tinymediamanager.core.movie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tinymediamanager.core.movie.MovieRenamer.morphTemplate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
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
import org.tinymediamanager.core.entities.MediaTrailer;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.jmte.NamedDateRenderer;
import org.tinymediamanager.core.jmte.NamedFirstCharacterRenderer;
import org.tinymediamanager.core.jmte.NamedUpperCaseRenderer;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.scraper.DynaEnum;

import com.floreysoft.jmte.Engine;

public class MovieJmteTests {

  private static Map<String, String> TOKEN_MAP = new HashMap<>();
  private Engine                     engine;
  private Map<String, Object>        root;

  @BeforeClass
  public static void init() {
    TOKEN_MAP.putAll(MovieRenamer.TOKEN_MAP);
  }

  @Test
  public void testMoviePatterns() {
    try {
      Movie movie = createMovie();

      engine = Engine.createEngine();
      engine.registerNamedRenderer(new NamedDateRenderer());
      engine.registerNamedRenderer(new NamedUpperCaseRenderer());
      engine.registerNamedRenderer(new NamedFirstCharacterRenderer());
      engine.setModelAdaptor(new MovieRenamer.MovieRenamerModelAdaptor());

      root = new HashMap<>();
      root.put("movie", movie);
      root.put("movieSet", movie.getMovieSet());

      // test single tokens
      compare("${title}", "Aladdin");
      compare("${title[0]}", "A");
      compare("${title;first}", "A");
      compare("${title[0,2]}", "Al");
      compare("${originalTitle}", "Disneys Aladdin");
      compare("${sortTitle}", "Aladdin");
      compare("${year}", "1992");
      compare("${releaseDate}", "1992-11-25");
      compare("${rating}", "7.2");
      compare("${movieSet.title}", "Aladdin Collection");
      compare("${movieSet.title[0]}", "A");
      compare("${movieSet.titleSortable}", "Aladdin Collection");
      compare("${titleSortable}", "Aladdin");
      compare("${imdb}", "tt0103639");
      compare("${certification}", "G");
      compare("${language}", "en");

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
      compare("${edition}", "Director's Cut");
      compare("${edition.name}", "DIRECTORS_CUT");
      compare("${3Dformat}", "3D SBS");

      compare("${genres[0].name}", "Adventure");
      compare("${tags[0]}", "Disney");
      compare("${actors[0].name}", "Scott Weinger");
      compare("${producers[0].name}", "Ron Clements");
      compare("${directors[0].name}", "Ron Clements");
      compare("${writers[0].name}", "Ted Elliott");

      // test raw movie access
      compare("${movie.top250}", "199");
      compare("${movie.videoFiles[0].overallBitRate}", "3500");

      // test combined tokens
      compare("${title} (${year})", "Aladdin (1992)");
      compare("${titleSortable[0]}/${title} (${year})", "A/Aladdin (1992)");

      // tests that should not failure
      compare("${tags[100]}", "");

      // test empty brackets
      compare("{ ${tags[100]} }", "{  }");

      // test conditional output
      compare("${- ,edition,}", "- Director's Cut");
      // compare("${- ,edition[0,2],}", "- Di"); // does not work at the moment in JMTE

      // test parent and space separator expressions
      compare("${parent}", "A" + File.separator + "1992");
      compare("${movie.country}", "US DE");
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

  private Movie createMovie() throws Exception {
    Movie movie = new Movie();
    movie.setDataSource("/media/movies");
    movie.setPath("/media/movies/A/1992/Aladdin");
    movie.setTitle("Aladdin");
    movie.setOriginalTitle("Disneys Aladdin");
    movie.setSortTitle("Aladdin");
    movie.setRating(new MediaRating(MediaRating.NFO, 7.2f, 5987));
    movie.setYear(1992);
    movie.setTop250(199);
    movie.setPlot("Princess Jasmine grows tired of being forced to remain in the...");
    movie.setTagline("Wish granted");
    movie.setRuntime(90);
    movie.setArtworkUrl("http://poster", MediaFileType.POSTER);
    movie.setArtworkUrl("http://fanart", MediaFileType.FANART);
    movie.setImdbId("tt0103639");
    movie.setTmdbId(812);
    movie.setId("trakt", 655);
    movie.setProductionCompany("Walt Disney");
    movie.setCountry("US/DE");
    movie.setCertification(MediaCertification.US_G);

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    movie.setReleaseDate(sdf.parse("1992-11-25"));

    MediaTrailer trailer = new MediaTrailer();
    trailer.setUrl("https://trailer");
    trailer.setInNfo(true);
    movie.addTrailer(trailer);

    MovieSet movieSet = new MovieSet();
    movieSet.setTitle("Aladdin Collection");
    movieSet.setPlot("Aladdin plot");
    movie.setMovieSet(movieSet);

    // ToDo fileinfo
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

    movie.addToMediaFiles(mf);

    movie.setWatched(true);
    movie.setGenres(Arrays.asList(MediaGenres.ADVENTURE, MediaGenres.FAMILY));
    movie.addWriter(new Person(Person.Type.WRITER, "Ted Elliott", "Writer"));
    movie.addWriter(new Person(Person.Type.WRITER, "Terry Rossio", "Writer"));
    movie.addWriter(new Person(Person.Type.WRITER, "Ron Clements", "Writer"));
    movie.addWriter(new Person(Person.Type.WRITER, "John Jusker", "Writer"));
    movie.addDirector(new Person(Person.Type.DIRECTOR, "Ron Clements", "Director"));
    movie.addWriter(new Person(Person.Type.DIRECTOR, "John Jusker", "Director"));
    movie.addToTags("Disney");
    movie.addToTags("Oriental");

    movie.addActor(new Person(Person.Type.ACTOR, "Scott Weinger", "Aladdin 'Al' (voice)"));
    movie.addActor(new Person(Person.Type.ACTOR, "Robin Williams", "Genie (voice)"));

    movie.addProducer(new Person(Person.Type.PRODUCER, "Ron Clements", "Producer"));
    movie.addProducer(new Person(Person.Type.PRODUCER, "Donald W. Ernst", "Producer"));

    movie.setSpokenLanguages("en");
    movie.setMediaSource(MediaSource.BLURAY);
    movie.setEdition(MovieEdition.DIRECTORS_CUT);
    return movie;
  }

  // @Test
  // public void getProperties() throws Exception {
  // printBeanInfo(Movie.class);
  // printBeanInfo(MovieSet.class);
  // printBeanInfo(Person.class);
  // printBeanInfo(Rating.class);
  // printBeanInfo(MediaFile.class);
  // printBeanInfo(MediaFileAudioStream.class);
  // printBeanInfo(MediaFileSubtitle.class);
  // printBeanInfo(MovieTrailer.class);
  // printBeanInfo(MediaSource.class);
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
