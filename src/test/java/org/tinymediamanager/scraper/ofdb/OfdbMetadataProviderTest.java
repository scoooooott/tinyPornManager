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

package org.tinymediamanager.scraper.ofdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaRating;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;

public class OfdbMetadataProviderTest {

  @Test
  public void testSearch() {
    IMovieMetadataProvider mp = null;
    List<MediaSearchResult> results = null;
    MediaSearchOptions options = null;

    try {
      options = new MediaSearchOptions(MediaType.MOVIE);
      // Die Piefke Saga
      results = null;
      try {
        mp = new OfdbMetadataProvider();
        options = new MediaSearchOptions(MediaType.MOVIE, "Die Piefke Saga");
        options.setLanguage(Locale.GERMAN);
        results = mp.search(options);
        // did we get a result?
        assertNotNull("Result", results);
        assertEquals("Die Piefke-Saga", results.get(0).getTitle());

        // result count
        assertEquals("Result count", 1, results.size());
      }
      catch (Exception e) {
        e.printStackTrace();
        fail();
      }

      // Lucky # Slevin
      results = null;
      try {
        mp = new OfdbMetadataProvider();
        options = new MediaSearchOptions(MediaType.MOVIE, "Slevin");
        options.setLanguage(Locale.GERMAN);
        results = mp.search(options);
        // did we get a result?
        assertNotNull("Result", results);

        // result count
        assertEquals("Result count", 1, results.size());

        assertEquals("Lucky # Slevin", results.get(0).getTitle());
        assertEquals("Lucky Number Slevin", results.get(0).getOriginalTitle());
        assertEquals(2006, results.get(0).getYear());
      }
      catch (Exception e) {
        e.printStackTrace();
        fail();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testScrape() {
    IMovieMetadataProvider mp = null;
    MediaScrapeOptions options = null;
    MediaMetadata md = null;

    // Merida
    try {
      mp = new OfdbMetadataProvider();
      options = new MediaScrapeOptions(MediaType.MOVIE);
      options.setLanguage(Locale.GERMAN);
      options.setId(mp.getProviderInfo().getId(), "226045");

      md = mp.getMetadata(options);

      assertThat(md.getTitle()).isEqualTo("Merida - Legende der Highlands");
      assertThat(md.getOriginalTitle()).isEqualTo("Brave");
      assertThat(md.getYear()).isEqualTo(2012);
      assertThat(md.getPlot()).startsWith(
          "Merida wächst als Erstgeborene von König Fergus an, der im schottischen Hochland sein Volk, bestehend aus vier Clans, anführt. Fergus hatte, als Merida noch ein Kleinkind war, einen Teil seines linken Beines im Kampf gegen einen riesigen, gefährlichen Bären verloren -");
      assertThat(md.getTagline()).isEmpty();

      assertThat(md.getRatings().size()).isEqualTo(1);
      MediaRating mediaRating = md.getRatings().get(0);
      assertThat(mediaRating.getRating()).isGreaterThan(0);
      assertThat(mediaRating.getVoteCount()).isGreaterThan(0);
      assertThat(mediaRating.getMaxValue()).isEqualTo(10);

      assertThat(md.getCastMembers(MediaCastMember.CastType.ACTOR)).isNotNull();
      assertThat(md.getCastMembers(MediaCastMember.CastType.ACTOR).size()).isEqualTo(11);
      assertThat(md.getCastMembers(MediaCastMember.CastType.ACTOR).get(0).getName()).isEqualTo("Billy Connolly");
      assertThat(md.getCastMembers(MediaCastMember.CastType.ACTOR).get(0).getCharacter()).isEqualTo("Fergus");
      assertThat(md.getCastMembers(MediaCastMember.CastType.DIRECTOR)).isNotNull();
      assertThat(md.getCastMembers(MediaCastMember.CastType.DIRECTOR).size()).isEqualTo(3);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }

    // with imdbid (Bourne Vermächtnis)
    try {
      mp = new OfdbMetadataProvider();

      MediaScrapeOptions scop = new MediaScrapeOptions(MediaType.MOVIE);
      scop.setId(MediaMetadata.IMDB, "tt1194173");
      md = mp.getMetadata(scop);

      assertThat(md.getTitle()).isEqualTo("Das Bourne Vermächtnis");
      assertThat(md.getOriginalTitle()).isEqualTo("The Bourne Legacy");
      assertThat(md.getYear()).isEqualTo(2012);

    }
    catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
}
