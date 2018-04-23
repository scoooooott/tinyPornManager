/*
 * Copyright 2012 - 2018 Manuel Laggner
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
import static org.assertj.core.api.Assertions.entry;

import java.nio.file.Paths;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.tinymediamanager.core.entities.Rating;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaGenres;

public class MovieNfoParserTest {

  @Test
  public void testMovieParser() {
    // prepared NFOs with test data
    testKodi1();
    testKodi2();

    testMediaPortal();

    // live NFOs from different Kodi versions
    testKodi14_2();
    testKodi15_2();
    testKodi16_1();
    testKodi17_0();
  }

  private void testKodi17_0() {
    // Kodi version 17.0
    try {
      MovieNfoParser parser = MovieNfoParser.parseNfo(Paths.get("target/test-classes/movie_nfo/kodi17.0.nfo"));

      assertThat(parser).isNotNull();
      assertThat(parser.title).isNotEmpty();
      assertThat(parser.originaltitle).isNotEmpty();
      assertThat(parser.sorttitle).isEmpty();

      assertThat(parser.ratings).hasSize(1);
      assertThat(parser.ratings.get(Rating.DEFAULT).id).isEqualTo(Rating.DEFAULT);
      assertThat(parser.ratings.get(Rating.DEFAULT).rating).isEqualTo(7.4f);
      assertThat(parser.ratings.get(Rating.DEFAULT).votes).isEqualTo(4990);
      assertThat(parser.ratings.get(Rating.DEFAULT).maxValue).isEqualTo(10);

      assertThat(parser.set).isNotNull();
      assertThat(parser.set.name).isNotEmpty();
      assertThat(parser.year).isEqualTo(2005);
      assertThat(parser.top250).isEqualTo(0);
      assertThat(parser.plot).isNotEmpty();
      assertThat(parser.outline).isEmpty();
      assertThat(parser.tagline).isNotEmpty();
      assertThat(parser.runtime).isGreaterThan(0);
      assertThat(parser.posters).hasSize(23);
      for (String poster : parser.posters) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.fanarts).hasSize(19);
      for (String fanart : parser.fanarts) {
        assertThat(fanart).isNotEmpty();
      }
      assertThat(parser.certification).isEqualTo(Certification.US_PG13);
      assertThat(parser.ids).contains(entry("imdb", "tt0372784"));
      assertThat(parser.ids).contains(entry("tmdb", 272));
      assertThat(parser.country).isNotEmpty();
      assertThat(parser.releaseDate).isEqualTo("2005-06-14");
      assertThat(parser.watched).isEqualTo(false);
      assertThat(parser.playcount).isEqualTo(0);
      assertThat(parser.genres).contains(MediaGenres.ACTION, MediaGenres.CRIME, MediaGenres.DRAMA);
      assertThat(parser.studios).hasSize(1);
      for (String studio : parser.studios) {
        assertThat(studio).isNotEmpty();
      }
      assertThat(parser.credits).hasSize(2);
      assertThat(parser.tags).hasSize(0);

      assertThat(parser.actors).hasSize(113);
      assertThat(parser.actors.get(0).thumb).isNotEmpty();
      for (MovieNfoParser.Person actor : parser.actors) {
        assertThat(actor.name).isNotEmpty();
        assertThat(actor.role).isNotEmpty();
        assertThat(actor.thumb).isNotNull();
      }
      assertThat(parser.producers).hasSize(0);
      for (MovieNfoParser.Person credit : parser.credits) {
        assertThat(credit.name).isNotEmpty();
      }
      assertThat(parser.directors).hasSize(1);
      for (MovieNfoParser.Person director : parser.directors) {
        assertThat(director.name).isNotEmpty();
      }

      assertThat(parser.fileinfo).isNull();
      assertThat(parser.unsupportedElements).hasSize(1);
      assertThat(parser.unsupportedElements.get(0)).isEqualTo("<resume><position>0.000000</position><total>0.000000</total></resume>");
      assertThat(parser.trailer).isNotEmpty();

      // xbmc tags
      assertThat(parser.epbookmark).isEmpty();
      assertThat(parser.lastplayed).isNull();
      assertThat(parser.dateadded).isNotNull();
    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }
  }

  private void testKodi16_1() {
    // Kodi version 16.1
    try {
      MovieNfoParser parser = MovieNfoParser.parseNfo(Paths.get("target/test-classes/movie_nfo/kodi16.1.nfo"));

      assertThat(parser).isNotNull();
      assertThat(parser.title).isNotEmpty();
      assertThat(parser.originaltitle).isNotEmpty();
      assertThat(parser.sorttitle).isEmpty();

      assertThat(parser.ratings).hasSize(1);
      assertThat(parser.ratings.get(Rating.NFO).id).isEqualTo(Rating.NFO);
      assertThat(parser.ratings.get(Rating.NFO).rating).isEqualTo(7.4f);
      assertThat(parser.ratings.get(Rating.NFO).votes).isEqualTo(4949);
      assertThat(parser.ratings.get(Rating.NFO).maxValue).isEqualTo(10);

      assertThat(parser.set).isNotNull();
      assertThat(parser.set.name).isNotEmpty();
      assertThat(parser.year).isEqualTo(2005);
      assertThat(parser.top250).isEqualTo(0);
      assertThat(parser.plot).isNotEmpty();
      assertThat(parser.outline).isEmpty();
      assertThat(parser.tagline).isNotEmpty();
      assertThat(parser.runtime).isGreaterThan(0);
      assertThat(parser.posters).hasSize(23);
      for (String poster : parser.posters) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.fanarts).hasSize(19);
      for (String fanart : parser.fanarts) {
        assertThat(fanart).isNotEmpty();
      }
      assertThat(parser.certification).isEqualTo(Certification.US_PG13);
      assertThat(parser.ids).contains(entry("imdb", "tt0372784"));
      assertThat(parser.country).isNotEmpty();
      assertThat(parser.releaseDate).isNull();
      assertThat(parser.watched).isEqualTo(false);
      assertThat(parser.playcount).isEqualTo(0);
      assertThat(parser.genres).contains(MediaGenres.ACTION, MediaGenres.CRIME, MediaGenres.DRAMA);
      assertThat(parser.studios).hasSize(1);
      for (String studio : parser.studios) {
        assertThat(studio).isNotEmpty();
      }
      assertThat(parser.credits).hasSize(2);
      assertThat(parser.tags).hasSize(0);

      assertThat(parser.actors).hasSize(113);
      assertThat(parser.actors.get(0).thumb).isNotEmpty();
      for (MovieNfoParser.Person actor : parser.actors) {
        assertThat(actor.name).isNotEmpty();
        assertThat(actor.role).isNotEmpty();
        assertThat(actor.thumb).isNotNull();
      }
      assertThat(parser.producers).hasSize(0);
      for (MovieNfoParser.Person credit : parser.credits) {
        assertThat(credit.name).isNotEmpty();
      }
      assertThat(parser.directors).hasSize(1);
      for (MovieNfoParser.Person director : parser.directors) {
        assertThat(director.name).isNotEmpty();
      }

      assertThat(parser.fileinfo).isNull();
      assertThat(parser.unsupportedElements).hasSize(1);
      assertThat(parser.unsupportedElements.get(0)).isEqualTo("<resume><position>0.000000</position><total>0.000000</total></resume>");
      assertThat(parser.trailer).isNotEmpty();

      // xbmc tags
      assertThat(parser.epbookmark).isNotEmpty();
      assertThat(parser.lastplayed).isNull();
      assertThat(parser.dateadded).isNotNull();
    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }
  }

  private void testKodi15_2() {
    // Kodi version 15.2
    try {
      MovieNfoParser parser = MovieNfoParser.parseNfo(Paths.get("target/test-classes/movie_nfo/kodi15.2.nfo"));

      assertThat(parser).isNotNull();
      assertThat(parser.title).isNotEmpty();
      assertThat(parser.originaltitle).isNotEmpty();
      assertThat(parser.sorttitle).isEmpty();

      assertThat(parser.ratings).hasSize(1);
      assertThat(parser.ratings.get(Rating.NFO).id).isEqualTo(Rating.NFO);
      assertThat(parser.ratings.get(Rating.NFO).rating).isEqualTo(7.4f);
      assertThat(parser.ratings.get(Rating.NFO).votes).isEqualTo(4949);
      assertThat(parser.ratings.get(Rating.NFO).maxValue).isEqualTo(10);

      assertThat(parser.set).isNotNull();
      assertThat(parser.set.name).isNotEmpty();
      assertThat(parser.year).isEqualTo(2005);
      assertThat(parser.top250).isEqualTo(0);
      assertThat(parser.plot).isNotEmpty();
      assertThat(parser.outline).isEmpty();
      assertThat(parser.tagline).isNotEmpty();
      assertThat(parser.runtime).isGreaterThan(0);
      assertThat(parser.posters).hasSize(23);
      for (String poster : parser.posters) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.fanarts).hasSize(19);
      for (String fanart : parser.fanarts) {
        assertThat(fanart).isNotEmpty();
      }
      assertThat(parser.certification).isEqualTo(Certification.US_PG13);
      assertThat(parser.ids).contains(entry("imdb", "tt0372784"));
      assertThat(parser.country).isNotEmpty();
      assertThat(parser.releaseDate).isNull();
      assertThat(parser.watched).isEqualTo(false);
      assertThat(parser.playcount).isEqualTo(0);
      assertThat(parser.genres).contains(MediaGenres.ACTION, MediaGenres.CRIME, MediaGenres.DRAMA);
      assertThat(parser.studios).hasSize(1);
      for (String studio : parser.studios) {
        assertThat(studio).isNotEmpty();
      }
      assertThat(parser.credits).hasSize(2);
      assertThat(parser.tags).hasSize(0);

      assertThat(parser.actors).hasSize(113);
      assertThat(parser.actors.get(0).thumb).isNotEmpty();
      for (MovieNfoParser.Person actor : parser.actors) {
        assertThat(actor.name).isNotEmpty();
        assertThat(actor.role).isNotEmpty();
        assertThat(actor.thumb).isNotNull();
      }
      assertThat(parser.producers).hasSize(0);
      for (MovieNfoParser.Person credit : parser.credits) {
        assertThat(credit.name).isNotEmpty();
      }
      assertThat(parser.directors).hasSize(1);
      for (MovieNfoParser.Person director : parser.directors) {
        assertThat(director.name).isNotEmpty();
      }

      assertThat(parser.fileinfo).isNull();
      assertThat(parser.unsupportedElements).hasSize(1);
      assertThat(parser.unsupportedElements.get(0)).isEqualTo("<resume><position>0.000000</position><total>0.000000</total></resume>");
      assertThat(parser.trailer).isNotEmpty();

      // xbmc tags
      assertThat(parser.epbookmark).isNotEmpty();
      assertThat(parser.lastplayed).isNull();
      assertThat(parser.dateadded).isNotNull();
    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }
  }

  private void testKodi14_2() {
    // Kodi version 14.2
    try {
      MovieNfoParser parser = MovieNfoParser.parseNfo(Paths.get("target/test-classes/movie_nfo/kodi14.2.nfo"));

      assertThat(parser).isNotNull();
      assertThat(parser.title).isNotEmpty();
      assertThat(parser.originaltitle).isNotEmpty();
      assertThat(parser.sorttitle).isEmpty();

      assertThat(parser.ratings).hasSize(1);
      assertThat(parser.ratings.get(Rating.NFO).id).isEqualTo(Rating.NFO);
      assertThat(parser.ratings.get(Rating.NFO).rating).isEqualTo(7.4f);
      assertThat(parser.ratings.get(Rating.NFO).votes).isEqualTo(4949);
      assertThat(parser.ratings.get(Rating.NFO).maxValue).isEqualTo(10);

      assertThat(parser.set).isNotNull();
      assertThat(parser.set.name).isNotEmpty();
      assertThat(parser.year).isEqualTo(2005);
      assertThat(parser.top250).isEqualTo(0);
      assertThat(parser.plot).isNotEmpty();
      assertThat(parser.outline).isEmpty();
      assertThat(parser.tagline).isNotEmpty();
      assertThat(parser.runtime).isGreaterThan(0);
      assertThat(parser.posters).hasSize(23);
      for (String poster : parser.posters) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.fanarts).hasSize(19);
      for (String fanart : parser.fanarts) {
        assertThat(fanart).isNotEmpty();
      }
      assertThat(parser.certification).isEqualTo(Certification.US_PG13);
      assertThat(parser.ids).contains(entry("imdb", "tt0372784"));
      assertThat(parser.country).isNotEmpty();
      assertThat(parser.releaseDate).isNull();
      assertThat(parser.watched).isEqualTo(false);
      assertThat(parser.playcount).isEqualTo(0);
      assertThat(parser.genres).contains(MediaGenres.ACTION, MediaGenres.CRIME, MediaGenres.DRAMA);
      assertThat(parser.studios).hasSize(1);
      for (String studio : parser.studios) {
        assertThat(studio).isNotEmpty();
      }
      assertThat(parser.credits).hasSize(2);
      for (MovieNfoParser.Person credit : parser.credits) {
        assertThat(credit.name).isNotEmpty();
      }
      assertThat(parser.directors).hasSize(1);
      for (MovieNfoParser.Person director : parser.directors) {
        assertThat(director.name).isNotEmpty();
      }
      assertThat(parser.tags).hasSize(0);

      assertThat(parser.actors).hasSize(113);
      assertThat(parser.actors.get(0).thumb).isNotEmpty();
      for (MovieNfoParser.Person actor : parser.actors) {
        assertThat(actor.name).isNotEmpty();
        assertThat(actor.role).isNotEmpty();
        assertThat(actor.thumb).isNotNull();
      }
      assertThat(parser.producers).hasSize(0);

      assertThat(parser.fileinfo).isNull();
      assertThat(parser.unsupportedElements).hasSize(1);
      assertThat(parser.unsupportedElements.get(0)).isEqualTo("<resume><position>0.000000</position><total>0.000000</total></resume>");
      assertThat(parser.trailer).isNotEmpty();

      // xbmc tags
      assertThat(parser.epbookmark).isNotEmpty();
      assertThat(parser.lastplayed).isNull();
      assertThat(parser.dateadded).isNotNull();
    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }
  }

  private void testMediaPortal() {
    // MediaPortal
    try {
      MovieNfoParser parser = MovieNfoParser.parseNfo(Paths.get("target/test-classes/movie_nfo/mediaportal.nfo"));

      assertThat(parser).isNotNull();
      assertThat(parser.title).isNotEmpty();
      assertThat(parser.originaltitle).isNotEmpty();
      assertThat(parser.sorttitle).isNotEmpty();

      assertThat(parser.ratings).hasSize(1);
      assertThat(parser.ratings.get(Rating.NFO).id).isEqualTo(Rating.NFO);
      assertThat(parser.ratings.get(Rating.NFO).rating).isEqualTo(6.5f);
      assertThat(parser.ratings.get(Rating.NFO).votes).isEqualTo(846);
      assertThat(parser.ratings.get(Rating.NFO).maxValue).isEqualTo(10);

      assertThat(parser.set).isNotNull();
      assertThat(parser.set.name).isNotEmpty();
      assertThat(parser.set.overview).isEmpty();
      assertThat(parser.year).isEqualTo(2009);
      assertThat(parser.top250).isEqualTo(0);
      assertThat(parser.plot).isNotEmpty();
      assertThat(parser.outline).isNotEmpty();
      assertThat(parser.tagline).isNotEmpty();
      assertThat(parser.runtime).isGreaterThan(0);
      assertThat(parser.posters).isEmpty();
      assertThat(parser.fanarts).isEmpty();
      assertThat(parser.certification).isEqualTo(Certification.DE_FSK18);
      assertThat(parser.ids).isNotEmpty();
      assertThat(parser.ids).contains(entry("imdb", "tt0472033"), entry("tmdb", 12244), entry("trakt", 7146));
      assertThat(parser.country).isNotEmpty();
      assertThat(parser.releaseDate).hasSameTimeAs("2009-08-19");
      assertThat(parser.watched).isEqualTo(false);
      assertThat(parser.playcount).isEqualTo(0);
      assertThat(parser.genres).contains(MediaGenres.ANIMATION, MediaGenres.ACTION, MediaGenres.SCIENCE_FICTION, MediaGenres.ADVENTURE,
          MediaGenres.THRILLER);
      assertThat(parser.studios).hasSize(5);
      for (String studio : parser.studios) {
        assertThat(studio).isNotEmpty();
      }
      assertThat(parser.credits).hasSize(16);
      for (MovieNfoParser.Person credit : parser.credits) {
        assertThat(credit.name).isNotEmpty();
      }
      assertThat(parser.directors).hasSize(3);
      for (MovieNfoParser.Person director : parser.directors) {
        assertThat(director.name).isNotEmpty();
      }
      assertThat(parser.tags).isEmpty();
      assertThat(parser.actors).hasSize(9);
      for (MovieNfoParser.Person actor : parser.actors) {
        assertThat(actor.name).isNotEmpty();
        assertThat(actor.role).isNotEmpty();
        assertThat(actor.thumb).isNotEmpty();
      }
      assertThat(parser.producers).hasSize(9);
      assertThat(parser.producers.get(0).thumb).isNotEmpty();
      for (MovieNfoParser.Person producer : parser.producers) {
        assertThat(producer.name).isNotEmpty();
        assertThat(producer.role).isNotEmpty();
      }
      assertThat(parser.fileinfo).isNull();
      assertThat(parser.unsupportedElements).isEmpty();
      assertThat(parser.trailer).isNotEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }
  }

  private void testKodi1() {
    // Kodi 1
    try {
      MovieNfoParser parser = MovieNfoParser.parseNfo(Paths.get("target/test-classes/movie_nfo/kodi.nfo"));

      assertThat(parser).isNotNull();
      assertThat(parser.title).isNotEmpty();
      assertThat(parser.originaltitle).isNotEmpty();
      assertThat(parser.sorttitle).isNotEmpty();

      assertThat(parser.ratings).hasSize(1);
      assertThat(parser.ratings.get(Rating.NFO).id).isEqualTo(Rating.NFO);
      assertThat(parser.ratings.get(Rating.NFO).rating).isEqualTo(6.5f);
      assertThat(parser.ratings.get(Rating.NFO).votes).isEqualTo(3998);
      assertThat(parser.ratings.get(Rating.NFO).maxValue).isEqualTo(10);

      assertThat(parser.set).isNotNull();
      assertThat(parser.set.name).isNotEmpty();
      assertThat(parser.set.overview).isEmpty();
      assertThat(parser.year).isEqualTo(2009);
      assertThat(parser.top250).isEqualTo(250);
      assertThat(parser.plot).isNotEmpty();
      assertThat(parser.outline).isNotEmpty();
      assertThat(parser.tagline).isNotEmpty();
      assertThat(parser.runtime).isGreaterThan(0);
      assertThat(parser.posters).hasSize(1);
      for (String poster : parser.posters) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.fanarts).hasSize(1);
      for (String fanart : parser.fanarts) {
        assertThat(fanart).isNotEmpty();
      }
      assertThat(parser.certification).isEqualTo(Certification.US_PG13);
      assertThat(parser.ids).contains(entry("imdb", "tt0472033"), entry("tmdb", 12244), entry("trakt", 7146));
      assertThat(parser.country).isNotEmpty();
      assertThat(parser.releaseDate).hasSameTimeAs("2009-08-19");
      assertThat(parser.watched).isEqualTo(true);
      assertThat(parser.playcount).isEqualTo(1);
      assertThat(parser.genres).contains(MediaGenres.ANIMATION, MediaGenres.ACTION, MediaGenres.SCIENCE_FICTION, MediaGenres.ADVENTURE,
          MediaGenres.THRILLER);
      assertThat(parser.studios).hasSize(5);
      for (String studio : parser.studios) {
        assertThat(studio).isNotEmpty();
      }
      assertThat(parser.credits).hasSize(16);
      for (MovieNfoParser.Person credit : parser.credits) {
        assertThat(credit.name).isNotEmpty();
      }
      assertThat(parser.directors).hasSize(1);
      for (MovieNfoParser.Person director : parser.directors) {
        assertThat(director.name).isNotEmpty();
      }
      assertThat(parser.tags).hasSize(1);
      for (String tag : parser.tags) {
        assertThat(tag).isNotEmpty();
      }
      assertThat(parser.actors).hasSize(9);
      for (MovieNfoParser.Person actor : parser.actors) {
        assertThat(actor.name).isNotEmpty();
        assertThat(actor.role).isNotEmpty();
        assertThat(actor.thumb).isNotEmpty();
      }
      assertThat(parser.producers).hasSize(9);
      assertThat(parser.producers.get(0).thumb).isNotEmpty();
      for (MovieNfoParser.Person producer : parser.producers) {
        assertThat(producer.name).isNotEmpty();
        assertThat(producer.role).isNotEmpty();
      }
      assertThat(parser.unsupportedElements).hasSize(1);
      assertThat(parser.unsupportedElements.get(0)).isEqualTo("<resume><position>754.000000</position><total>6217.000000</total></resume>");
      assertThat(parser.trailer).isNotEmpty();

      // xbmc tags
      assertThat(parser.epbookmark).isNotEmpty();
      assertThat(parser.status).isNotEmpty();
      assertThat(parser.code).isNotEmpty();
      assertThat(parser.lastplayed).isNotNull();
      assertThat(parser.dateadded).isNotNull();
      assertThat(parser.fileinfo).isNotNull(); // not null, but empty
      assertThat(parser.fileinfo.videos).isEmpty();
      assertThat(parser.fileinfo.audios).isEmpty();
      assertThat(parser.fileinfo.subtitles).isEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }
  }

  private void testKodi2() {
    // Kodi 2
    try {
      MovieNfoParser parser = MovieNfoParser.parseNfo(Paths.get("target/test-classes/movie_nfo/kodi2.nfo"));

      assertThat(parser).isNotNull();
      assertThat(parser.title).isNotEmpty();
      assertThat(parser.originaltitle).isNotEmpty();
      assertThat(parser.sorttitle).isNotEmpty();

      assertThat(parser.ratings).hasSize(4);
      assertThat(parser.ratings.get(Rating.NFO).id).isEqualTo(Rating.NFO);
      assertThat(parser.ratings.get(Rating.NFO).rating).isEqualTo(6.5f);
      assertThat(parser.ratings.get(Rating.NFO).votes).isEqualTo(846);
      assertThat(parser.ratings.get(Rating.NFO).maxValue).isEqualTo(10);
      assertThat(parser.ratings.get(Rating.DEFAULT).id).isEqualTo(Rating.DEFAULT);
      assertThat(parser.ratings.get(Rating.DEFAULT).rating).isEqualTo(5.8f);
      assertThat(parser.ratings.get(Rating.DEFAULT).votes).isEqualTo(2100);
      assertThat(parser.ratings.get(Rating.DEFAULT).maxValue).isEqualTo(10);
      assertThat(parser.ratings.get("imdb").id).isEqualTo("imdb");
      assertThat(parser.ratings.get("imdb").rating).isEqualTo(8.9f);
      assertThat(parser.ratings.get("imdb").votes).isEqualTo(12345);
      assertThat(parser.ratings.get("imdb").maxValue).isEqualTo(10);
      assertThat(parser.ratings.get("metascore").id).isEqualTo("metascore");
      assertThat(parser.ratings.get("metascore").rating).isEqualTo(67f);
      assertThat(parser.ratings.get("metascore").votes).isEqualTo(5);
      assertThat(parser.ratings.get("metascore").maxValue).isEqualTo(100);

      assertThat(parser.set).isNotNull();
      assertThat(parser.set.name).isNotEmpty();
      assertThat(parser.set.overview).isNotEmpty();
      assertThat(parser.year).isEqualTo(2009);
      assertThat(parser.top250).isEqualTo(250);
      assertThat(parser.plot).isNotEmpty();
      assertThat(parser.outline).isNotEmpty();
      assertThat(parser.tagline).isNotEmpty();
      assertThat(parser.runtime).isGreaterThan(0);
      assertThat(parser.posters).hasSize(9);
      for (String poster : parser.posters) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.fanarts).hasSize(10);
      for (String fanart : parser.fanarts) {
        assertThat(fanart).isNotEmpty();
      }
      assertThat(parser.certification).isEqualTo(Certification.DE_FSK12);
      assertThat(parser.ids).contains(entry("imdb", "tt0472033"), entry("tmdb", 12244), entry("trakt", 7146));
      assertThat(parser.country).isNotEmpty();
      assertThat(parser.releaseDate).hasSameTimeAs("2009-08-19");
      assertThat(parser.watched).isEqualTo(true);
      assertThat(parser.playcount).isEqualTo(6);
      assertThat(parser.genres).contains(MediaGenres.ANIMATION, MediaGenres.ACTION, MediaGenres.SCIENCE_FICTION, MediaGenres.ADVENTURE,
          MediaGenres.THRILLER);
      assertThat(parser.studios).hasSize(5);
      assertThat(parser.credits).hasSize(16);
      for (MovieNfoParser.Person credit : parser.credits) {
        assertThat(credit.name).isNotEmpty();
      }
      for (String studio : parser.studios) {
        assertThat(studio).isNotEmpty();
      }
      assertThat(parser.directors).hasSize(2);
      for (MovieNfoParser.Person director : parser.directors) {
        assertThat(director.name).isNotEmpty();
      }
      assertThat(parser.tags).hasSize(2);
      for (String tag : parser.tags) {
        assertThat(tag).isNotEmpty();
      }
      assertThat(parser.actors).hasSize(9);
      for (MovieNfoParser.Person actor : parser.actors) {
        assertThat(actor.name).isNotEmpty();
        assertThat(actor.role).isNotEmpty();
        assertThat(actor.thumb).isNotEmpty();
      }
      assertThat(parser.producers).hasSize(9);
      assertThat(parser.producers.get(0).thumb).isNotEmpty();
      for (MovieNfoParser.Person producer : parser.producers) {
        assertThat(producer.name).isNotEmpty();
        assertThat(producer.role).isNotEmpty();
      }
      assertThat(parser.fileinfo).isNotNull();
      assertThat(parser.fileinfo.videos).hasSize(1);
      for (MovieNfoParser.Video video : parser.fileinfo.videos) {
        assertThat(video.codec).isNotEmpty();
        assertThat(video.aspect).isGreaterThan(0);
        assertThat(video.height).isGreaterThan(0);
        assertThat(video.width).isGreaterThan(0);
        assertThat(video.durationinseconds).isGreaterThan(0);
        assertThat(video.stereomode).isNotEmpty();
      }
      assertThat(parser.fileinfo.audios).hasSize(4);
      for (MovieNfoParser.Audio audio : parser.fileinfo.audios) {
        assertThat(audio.codec).isNotEmpty();
        assertThat(audio.language).isNotEmpty();
        assertThat(audio.channels).isGreaterThan(0);
      }
      assertThat(parser.fileinfo.subtitles).hasSize(14);
      for (MovieNfoParser.Subtitle subtitle : parser.fileinfo.subtitles) {
        assertThat(subtitle.language).isNotEmpty();
      }
      assertThat(parser.unsupportedElements).isEmpty();
      assertThat(parser.trailer).isNotEmpty();

    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }
  }
}
