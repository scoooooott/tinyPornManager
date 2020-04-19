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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.MediaFileHelper;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.MediaTrailer;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.movie.MovieEdition;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.movie.filenaming.MovieNfoNaming;
import org.tinymediamanager.scraper.MediaMetadata;

public class MovieToNfoConnectorTest extends BasicTest {

  @BeforeClass
  public static void setup() {
    deleteSettingsFolder();
    Settings.getInstance(getSettingsFolder());
  }

  @Test
  public void testXbmcNfo() {
    FileUtils.deleteQuietly(new File(getSettingsFolder(), "xbmc_nfo"));
    try {
      Files.createDirectories(Paths.get(getSettingsFolder(), "xbmc_nfo"));
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    try {
      Movie movie = createMovie("xbmc_nfo");

      // write it
      List<MovieNfoNaming> nfoNames = Collections.singletonList(MovieNfoNaming.MOVIE_NFO);
      MovieToXbmcConnector connector = new MovieToXbmcConnector(movie);
      connector.write(nfoNames);

      Path nfoFile = Paths.get(getSettingsFolder(), "xbmc_nfo/movie.nfo");
      assertThat(Files.exists(nfoFile)).isTrue();

      // unmarshal it
      MovieNfoParser movieNfoParser = MovieNfoParser.parseNfo(nfoFile);
      Movie newMovie = movieNfoParser.toMovie();
      compareMovies(movie, newMovie);
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testKodiNfo() {
    FileUtils.deleteQuietly(new File(getSettingsFolder(), "kodi_nfo"));
    try {
      Files.createDirectories(Paths.get(getSettingsFolder(), "kodi_nfo"));
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    try {
      Movie movie = createMovie("kodi_nfo");

      // also add a second rating
      movie.setRating(new MediaRating(MediaMetadata.TMDB, 7.7f, 56987));

      // write it
      List<MovieNfoNaming> nfoNames = Collections.singletonList(MovieNfoNaming.MOVIE_NFO);
      MovieToKodiConnector connector = new MovieToKodiConnector(movie);
      connector.write(nfoNames);

      Path nfoFile = Paths.get(getSettingsFolder(), "kodi_nfo/movie.nfo");
      assertThat(Files.exists(nfoFile)).isTrue();

      // unmarshal it
      MovieNfoParser movieNfoParser = MovieNfoParser.parseNfo(nfoFile);
      Movie newMovie = movieNfoParser.toMovie();
      compareMovies(movie, newMovie);
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testMediaPortalNfo() {
    FileUtils.deleteQuietly(new File(getSettingsFolder(), "mp_nfo"));
    try {
      Files.createDirectories(Paths.get(getSettingsFolder(), "mp_nfo"));
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    try {
      Movie movie = createMovie("mp_nfo");

      // MP is not supporting top250 - strip it out
      movie.setTop250(0);

      // write it
      List<MovieNfoNaming> nfoNames = Collections.singletonList(MovieNfoNaming.FILENAME_NFO);
      MovieToMpLegacyConnector connector = new MovieToMpLegacyConnector(movie);
      connector.write(nfoNames);

      Path nfoFile = Paths.get(getSettingsFolder(), "mp_nfo/Aladdin.nfo");
      assertThat(Files.exists(nfoFile)).isTrue();

      // unmarshal it
      MovieNfoParser movieNfoParser = MovieNfoParser.parseNfo(nfoFile);
      Movie newMovie = movieNfoParser.toMovie();
      compareMovies(movie, newMovie);
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  private void compareMovies(Movie movie, Movie newMovie) {
    assertThat(newMovie.getTitle()).isEqualTo(movie.getTitle());
    assertThat(newMovie.getOriginalTitle()).isEqualTo(movie.getOriginalTitle());
    assertThat(newMovie.getSortTitle()).isEqualTo(movie.getSortTitle());
    assertThat(newMovie.getRating().getRating()).isEqualTo(movie.getRating().getRating());
    assertThat(newMovie.getRating().getVotes()).isEqualTo(movie.getRating().getVotes());
    assertThat(newMovie.getYear()).isEqualTo(movie.getYear());
    assertThat(newMovie.getTop250()).isEqualTo(movie.getTop250());
    assertThat(newMovie.getPlot()).isEqualTo(movie.getPlot());
    assertThat(newMovie.getTagline()).isEqualTo(movie.getTagline());
    assertThat(newMovie.getRuntime()).isEqualTo(movie.getRuntime());
    assertThat(newMovie.getArtworkUrl(MediaFileType.POSTER)).isEqualTo(movie.getArtworkUrl(MediaFileType.POSTER));
    assertThat(newMovie.getArtworkUrl(MediaFileType.FANART)).isEqualTo(movie.getArtworkUrl(MediaFileType.FANART));
    assertThat(newMovie.getImdbId()).isEqualTo(movie.getImdbId());
    assertThat(newMovie.getTmdbId()).isEqualTo(movie.getTmdbId());
    assertThat(newMovie.getProductionCompany()).isEqualTo(movie.getProductionCompany());
    assertThat(newMovie.getCountry()).isEqualTo(movie.getCountry());
    assertThat(newMovie.getCertification()).isEqualTo(movie.getCertification());
    assertThat(newMovie.getTrailer().size()).isEqualTo(movie.getTrailer().size());
    if (!newMovie.getTrailer().isEmpty()) {
      assertThat(newMovie.getTrailer().get(0).getUrl()).isEqualTo(movie.getTrailer().get(0).getUrl());
    }
    assertThat(newMovie.getIds().size()).isEqualTo(movie.getIds().size());
    assertThat(newMovie.getId("trakt")).isEqualTo(movie.getId("trakt"));
    assertThat(newMovie.getReleaseDate()).isEqualTo(movie.getReleaseDate());
    assertThat(newMovie.isWatched()).isEqualTo(movie.isWatched());
    assertThat(newMovie.getGenres().size()).isEqualTo(movie.getGenres().size());
    assertThat(newMovie.getGenres().get(0)).isEqualTo(movie.getGenres().get(0));
    assertThat(newMovie.getWriters()).isEqualTo(movie.getWriters());
    assertThat(newMovie.getDirectors()).isEqualTo(movie.getDirectors());
    assertThat(newMovie.getTags().size()).isEqualTo(movie.getTags().size());
    if (!newMovie.getTags().isEmpty()) {
      assertThat(newMovie.getTags().get(0)).isEqualTo(movie.getTags().get(0));
    }
    assertThat(newMovie.getActors().size()).isEqualTo(movie.getActors().size());
    assertThat(newMovie.getActors().get(0)).isEqualTo(movie.getActors().get(0));
    assertThat(newMovie.getProducers().size()).isEqualTo(movie.getProducers().size());
    assertThat(newMovie.getProducers().get(0)).isEqualTo(movie.getProducers().get(0));
    assertThat(newMovie.getSpokenLanguages()).isEqualTo(movie.getSpokenLanguages());
    assertThat(newMovie.getMediaSource()).isEqualTo(movie.getMediaSource());
    assertThat(newMovie.getEdition()).isEqualTo(movie.getEdition());
  }

  private Movie createMovie(String path) throws Exception {
    Movie movie = new Movie();
    movie.setPath(Paths.get(getSettingsFolder(), path).toString());
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
    movie.setCountry("US");
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
    mf.setVideo3DFormat(MediaFileHelper.VIDEO_3D_SBS);

    MediaFileAudioStream audio = new MediaFileAudioStream();
    audio.setCodec("AC3");
    audio.setLanguage("en");
    audio.setAudioChannels(6);
    mf.setAudioStreams(Collections.singletonList(audio));

    MediaFileSubtitle sub = new MediaFileSubtitle();
    sub.setLanguage("de");
    mf.addSubtitle(sub);

    movie.addToMediaFiles(mf);

    movie.setWatched(true);
    movie.addGenre(MediaGenres.ADVENTURE);
    movie.addGenre(MediaGenres.FAMILY);
    movie.addWriter(new Person(Person.Type.WRITER, "Ted Elliott", "Writer"));
    movie.addWriter(new Person(Person.Type.WRITER, "Terry Rossio", "Writer"));
    movie.addWriter(new Person(Person.Type.WRITER, "Ron Clements", "Writer"));
    movie.addWriter(new Person(Person.Type.WRITER, "John Jusker", "Writer"));
    movie.addDirector(new Person(Person.Type.DIRECTOR, "Ron Clements", "Director"));
    movie.addWriter(new Person(Person.Type.DIRECTOR, "John Jusker", "Director"));
    movie.addToTags("Disney");
    movie.addToTags("Oriental");

    movie.addActor(new Person(Person.Type.ACTOR, "Scott Weinger", "Aladdin 'Al' (voice)",
        "https://image.tmdb.org/t/p/w640/rlZpPoORiJzStzIuAyrPOlLhnaL.jpg", "https://www.themoviedb.org/person/15827"));
    movie.addActor(new Person(Person.Type.ACTOR, "Robin Williams", "Genie (voice)"));

    movie.addProducer(new Person(Person.Type.PRODUCER, "Ron Clements", "Producer"));
    movie.addProducer(new Person(Person.Type.PRODUCER, "Donald W. Ernst", "Producer"));

    movie.setSpokenLanguages("de, fr, Englirsch");
    movie.setMediaSource(MediaSource.BLURAY);
    movie.setEdition(MovieEdition.DIRECTORS_CUT);
    return movie;
  }

  @Test
  public void parseNFO() throws Exception {
    Path dir = Paths.get("target/test-classes/testmovies/MovieSets/");

    System.out.println("is valid? " + MovieConnectors.isValidNFO(dir.resolve("MSold.nfo")));
    MovieNfoParser movieNfoParser = MovieNfoParser.parseNfo(dir.resolve("MSold.nfo"));
    Movie nfo = nfo = movieNfoParser.toMovie();
    System.out.println(nfo.getMovieSet().getTitle() + " - " + nfo.getMovieSet());

    System.out.println("is valid? " + MovieConnectors.isValidNFO(dir.resolve("MSnew.nfo")));
    movieNfoParser = MovieNfoParser.parseNfo(dir.resolve("MSnew.nfo"));
    nfo = movieNfoParser.toMovie();
    System.out.println(nfo.getMovieSet().getTitle() + " - " + nfo.getMovieSet());

    System.out.println("is valid? " + MovieConnectors.isValidNFO(dir.resolve("MSmixed.nfo")));
    // NO - mixed NOT supported
    // nfo = MovieToKodiNfoConnector.getData(dir.resolve("MSmixed.nfo"));
    // System.out.println(nfo.getMovieSet().getTitle() + " - " + nfo.getMovieSet());
  }
}
