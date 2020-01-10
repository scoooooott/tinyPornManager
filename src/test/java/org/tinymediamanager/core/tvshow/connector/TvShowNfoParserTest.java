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

package org.tinymediamanager.core.tvshow.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.nio.file.Paths;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.tinymediamanager.core.MediaAiredStatus;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.entities.MediaGenres;

public class TvShowNfoParserTest {

  @Test
  public void testNfoParserKodi142() {
    testKodi14_2();
  }

  @Test
  public void testNfoParserKodi152() {
    testKodi15_2();
  }

  @Test
  public void testNfoParserKodi161() {
    testKodi16_1();
  }

  @Test
  public void testNfoParserKodi170() {
    testKodi17_0();
  }

  private void testKodi17_0() {
    // Kodi version 17.0
    try {
      TvShowNfoParser parser = TvShowNfoParser.parseNfo(Paths.get("target/test-classes/tvshow_nfo/kodi17.0.nfo"));

      assertThat(parser).isNotNull();
      assertThat(parser.title).isNotEmpty();
      assertThat(parser.showTitle).isNotEmpty();

      assertThat(parser.ratings).hasSize(1);
      assertThat(parser.ratings.get(TvShowNfoParser.Rating.DEFAULT).id).isEqualTo(TvShowNfoParser.Rating.DEFAULT);
      assertThat(parser.ratings.get(TvShowNfoParser.Rating.DEFAULT).rating).isEqualTo(7.4f);
      assertThat(parser.ratings.get(TvShowNfoParser.Rating.DEFAULT).votes).isEqualTo(8);
      assertThat(parser.ratings.get(TvShowNfoParser.Rating.DEFAULT).maxValue).isEqualTo(10);

      assertThat(parser.year).isEqualTo(1987);
      assertThat(parser.top250).isEqualTo(0);
      assertThat(parser.plot).isNotEmpty();
      assertThat(parser.outline).isEmpty();
      assertThat(parser.tagline).isEmpty();
      assertThat(parser.runtime).isGreaterThan(0);
      assertThat(parser.posters).hasSize(8);
      for (String poster : parser.posters) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(0)).hasSize(2);
      for (String poster : parser.seasonPosters.get(0)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(1)).hasSize(7);
      for (String poster : parser.seasonPosters.get(1)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(2)).hasSize(3);
      for (String poster : parser.seasonPosters.get(2)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(3)).hasSize(4);
      for (String poster : parser.seasonPosters.get(3)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(4)).hasSize(4);
      for (String poster : parser.seasonPosters.get(4)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(5)).hasSize(2);
      for (String poster : parser.seasonPosters.get(5)) {
        assertThat(poster).isNotEmpty();
      }

      assertThat(parser.banners).hasSize(3);
      for (String banner : parser.banners) {
        assertThat(banner).isNotEmpty();
      }

      assertThat(parser.fanarts).hasSize(13);
      for (String fanart : parser.fanarts) {
        assertThat(fanart).isNotEmpty();
      }
      assertThat(parser.certification).isEqualTo(MediaCertification.US_TVPG);
      assertThat(parser.ids).contains(entry("tvdb", 77585));
      assertThat(parser.releaseDate).isEqualTo("1987-04-12");
      assertThat(parser.status).isEqualTo(MediaAiredStatus.ENDED);
      assertThat(parser.watched).isEqualTo(false);
      assertThat(parser.playcount).isEqualTo(0);
      assertThat(parser.genres).contains(MediaGenres.ACTION, MediaGenres.ADVENTURE, MediaGenres.DRAMA);
      assertThat(parser.studios).hasSize(1);
      for (String studio : parser.studios) {
        assertThat(studio).isNotEmpty();
      }

      assertThat(parser.actors).hasSize(10);
      assertThat(parser.actors.get(0).thumb).isNotEmpty();
      for (TvShowNfoParser.Person actor : parser.actors) {
        assertThat(actor.name).isNotEmpty();
        assertThat(actor.role).isNotEmpty();
        assertThat(actor.thumb).isNotNull();
      }

      assertThat(parser.unsupportedElements).hasSize(4); // season, episode, displayseason, displayepisode
      assertThat(parser.trailer).isEmpty();

      // xbmc tags
      assertThat(parser.episodeguide).isNotEmpty();
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
      TvShowNfoParser parser = TvShowNfoParser.parseNfo(Paths.get("target/test-classes/tvshow_nfo/kodi16.1.nfo"));

      assertThat(parser).isNotNull();
      assertThat(parser.title).isNotEmpty();
      assertThat(parser.showTitle).isNotEmpty();

      assertThat(parser.ratings).hasSize(1);
      assertThat(parser.ratings.get(TvShowNfoParser.Rating.DEFAULT).id).isEqualTo(TvShowNfoParser.Rating.DEFAULT);
      assertThat(parser.ratings.get(TvShowNfoParser.Rating.DEFAULT).rating).isEqualTo(7.4f);
      assertThat(parser.ratings.get(TvShowNfoParser.Rating.DEFAULT).votes).isEqualTo(8);
      assertThat(parser.ratings.get(TvShowNfoParser.Rating.DEFAULT).maxValue).isEqualTo(10);

      assertThat(parser.year).isEqualTo(1987);
      assertThat(parser.top250).isEqualTo(0);
      assertThat(parser.plot).isNotEmpty();
      assertThat(parser.outline).isEmpty();
      assertThat(parser.tagline).isEmpty();
      assertThat(parser.runtime).isGreaterThan(0);
      assertThat(parser.posters).hasSize(8);
      for (String poster : parser.posters) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(0)).hasSize(2);
      for (String poster : parser.seasonPosters.get(0)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(1)).hasSize(7);
      for (String poster : parser.seasonPosters.get(1)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(2)).hasSize(3);
      for (String poster : parser.seasonPosters.get(2)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(3)).hasSize(4);
      for (String poster : parser.seasonPosters.get(3)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(4)).hasSize(4);
      for (String poster : parser.seasonPosters.get(4)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(5)).hasSize(2);
      for (String poster : parser.seasonPosters.get(5)) {
        assertThat(poster).isNotEmpty();
      }

      assertThat(parser.banners).hasSize(3);
      for (String banner : parser.banners) {
        assertThat(banner).isNotEmpty();
      }

      assertThat(parser.fanarts).hasSize(13);
      for (String fanart : parser.fanarts) {
        assertThat(fanart).isNotEmpty();
      }
      assertThat(parser.certification).isEqualTo(MediaCertification.US_TVPG);
      assertThat(parser.ids).contains(entry("tvdb", 77585));
      assertThat(parser.releaseDate).isEqualTo("1987-04-12");
      assertThat(parser.status).isEqualTo(MediaAiredStatus.ENDED);
      assertThat(parser.watched).isEqualTo(false);
      assertThat(parser.playcount).isEqualTo(0);
      assertThat(parser.genres).contains(MediaGenres.ACTION, MediaGenres.ADVENTURE, MediaGenres.DRAMA);
      assertThat(parser.studios).hasSize(1);
      for (String studio : parser.studios) {
        assertThat(studio).isNotEmpty();
      }

      assertThat(parser.actors).hasSize(10);
      assertThat(parser.actors.get(0).thumb).isNotEmpty();
      for (TvShowNfoParser.Person actor : parser.actors) {
        assertThat(actor.name).isNotEmpty();
        assertThat(actor.role).isNotEmpty();
        assertThat(actor.thumb).isNotNull();
      }

      assertThat(parser.unsupportedElements).hasSize(4); // season, episode, displayseason, displayepisode
      assertThat(parser.trailer).isEmpty();

      // xbmc tags
      assertThat(parser.episodeguide).isNotEmpty();
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
      TvShowNfoParser parser = TvShowNfoParser.parseNfo(Paths.get("target/test-classes/tvshow_nfo/kodi15.2.nfo"));

      assertThat(parser).isNotNull();
      assertThat(parser.title).isNotEmpty();
      assertThat(parser.showTitle).isNotEmpty();

      assertThat(parser.ratings).hasSize(1);
      assertThat(parser.ratings.get(TvShowNfoParser.Rating.DEFAULT).id).isEqualTo(TvShowNfoParser.Rating.DEFAULT);
      assertThat(parser.ratings.get(TvShowNfoParser.Rating.DEFAULT).rating).isEqualTo(7.4f);
      assertThat(parser.ratings.get(TvShowNfoParser.Rating.DEFAULT).votes).isEqualTo(8);
      assertThat(parser.ratings.get(TvShowNfoParser.Rating.DEFAULT).maxValue).isEqualTo(10);

      assertThat(parser.year).isEqualTo(1987);
      assertThat(parser.top250).isEqualTo(0);
      assertThat(parser.plot).isNotEmpty();
      assertThat(parser.outline).isEmpty();
      assertThat(parser.tagline).isEmpty();
      assertThat(parser.runtime).isGreaterThan(0);
      assertThat(parser.posters).hasSize(8);
      for (String poster : parser.posters) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(0)).hasSize(2);
      for (String poster : parser.seasonPosters.get(0)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(1)).hasSize(7);
      for (String poster : parser.seasonPosters.get(1)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(2)).hasSize(3);
      for (String poster : parser.seasonPosters.get(2)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(3)).hasSize(4);
      for (String poster : parser.seasonPosters.get(3)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(4)).hasSize(4);
      for (String poster : parser.seasonPosters.get(4)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(5)).hasSize(2);
      for (String poster : parser.seasonPosters.get(5)) {
        assertThat(poster).isNotEmpty();
      }

      assertThat(parser.banners).hasSize(3);
      for (String banner : parser.banners) {
        assertThat(banner).isNotEmpty();
      }

      assertThat(parser.fanarts).hasSize(13);
      for (String fanart : parser.fanarts) {
        assertThat(fanart).isNotEmpty();
      }
      assertThat(parser.certification).isEqualTo(MediaCertification.US_TVPG);
      assertThat(parser.ids).contains(entry("tvdb", 77585));
      assertThat(parser.releaseDate).isEqualTo("1987-04-12");
      assertThat(parser.status).isEqualTo(MediaAiredStatus.ENDED);
      assertThat(parser.watched).isEqualTo(false);
      assertThat(parser.playcount).isEqualTo(0);
      assertThat(parser.genres).contains(MediaGenres.ACTION, MediaGenres.ADVENTURE, MediaGenres.DRAMA);
      assertThat(parser.studios).hasSize(1);
      for (String studio : parser.studios) {
        assertThat(studio).isNotEmpty();
      }

      assertThat(parser.actors).hasSize(10);
      assertThat(parser.actors.get(0).thumb).isNotEmpty();
      for (TvShowNfoParser.Person actor : parser.actors) {
        assertThat(actor.name).isNotEmpty();
        assertThat(actor.role).isNotEmpty();
        assertThat(actor.thumb).isNotNull();
      }

      assertThat(parser.unsupportedElements).hasSize(4); // season, episode, displayseason, displayepisode
      assertThat(parser.trailer).isEmpty();

      // xbmc tags
      assertThat(parser.episodeguide).isNotEmpty();
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
      TvShowNfoParser parser = TvShowNfoParser.parseNfo(Paths.get("target/test-classes/tvshow_nfo/kodi14.2.nfo"));

      assertThat(parser).isNotNull();
      assertThat(parser.title).isNotEmpty();
      assertThat(parser.showTitle).isNotEmpty();

      assertThat(parser.ratings).hasSize(1);
      assertThat(parser.ratings.get(TvShowNfoParser.Rating.DEFAULT).id).isEqualTo(TvShowNfoParser.Rating.DEFAULT);
      assertThat(parser.ratings.get(TvShowNfoParser.Rating.DEFAULT).rating).isEqualTo(7.4f);
      assertThat(parser.ratings.get(TvShowNfoParser.Rating.DEFAULT).votes).isEqualTo(8);
      assertThat(parser.ratings.get(TvShowNfoParser.Rating.DEFAULT).maxValue).isEqualTo(10);

      assertThat(parser.year).isEqualTo(1987);
      assertThat(parser.top250).isEqualTo(0);
      assertThat(parser.plot).isNotEmpty();
      assertThat(parser.outline).isEmpty();
      assertThat(parser.tagline).isEmpty();
      assertThat(parser.runtime).isGreaterThan(0);
      assertThat(parser.posters).hasSize(8);
      for (String poster : parser.posters) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(0)).hasSize(2);
      for (String poster : parser.seasonPosters.get(0)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(1)).hasSize(7);
      for (String poster : parser.seasonPosters.get(1)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(2)).hasSize(3);
      for (String poster : parser.seasonPosters.get(2)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(3)).hasSize(4);
      for (String poster : parser.seasonPosters.get(3)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(4)).hasSize(4);
      for (String poster : parser.seasonPosters.get(4)) {
        assertThat(poster).isNotEmpty();
      }
      assertThat(parser.seasonPosters.get(5)).hasSize(2);
      for (String poster : parser.seasonPosters.get(5)) {
        assertThat(poster).isNotEmpty();
      }

      assertThat(parser.banners).hasSize(3);
      for (String banner : parser.banners) {
        assertThat(banner).isNotEmpty();
      }

      assertThat(parser.fanarts).hasSize(13);
      for (String fanart : parser.fanarts) {
        assertThat(fanart).isNotEmpty();
      }
      assertThat(parser.certification).isEqualTo(MediaCertification.US_TVPG);
      assertThat(parser.ids).contains(entry("tvdb", 77585));
      assertThat(parser.releaseDate).isEqualTo("1987-04-12");
      assertThat(parser.status).isEqualTo(MediaAiredStatus.ENDED);
      assertThat(parser.watched).isEqualTo(false);
      assertThat(parser.playcount).isEqualTo(0);
      assertThat(parser.genres).contains(MediaGenres.ACTION, MediaGenres.ADVENTURE, MediaGenres.DRAMA);
      assertThat(parser.studios).hasSize(1);
      for (String studio : parser.studios) {
        assertThat(studio).isNotEmpty();
      }

      assertThat(parser.actors).hasSize(10);
      assertThat(parser.actors.get(0).thumb).isNotEmpty();
      for (TvShowNfoParser.Person actor : parser.actors) {
        assertThat(actor.name).isNotEmpty();
        assertThat(actor.role).isNotEmpty();
        assertThat(actor.thumb).isNotNull();
      }

      assertThat(parser.unsupportedElements).hasSize(4); // season, episode, displayseason, displayepisode
      assertThat(parser.trailer).isEmpty();

      // xbmc tags
      assertThat(parser.episodeguide).isNotEmpty();
      assertThat(parser.lastplayed).isNull();
      assertThat(parser.dateadded).isNotNull();
    }
    catch (Exception e) {
      e.printStackTrace();
      Assertions.fail(e.getMessage());
    }
  }
}
