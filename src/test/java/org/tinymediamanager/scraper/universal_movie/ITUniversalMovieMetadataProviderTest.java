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

package org.tinymediamanager.scraper.universal_movie;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviders;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.interfaces.IMovieMetadataProvider;

public class ITUniversalMovieMetadataProviderTest {

  @BeforeClass
  public static void setUp() {
    // load all classpath plugins
    MediaProviders.loadMediaProviders();
  }

  @Test
  public void testSearchTmdb() {
    callSearch("tmdb", "Harry Potter");
  }

  @Test
  public void testSearchImdb() {
    callSearch("imdb", "Harry Potter");
  }

  @Test
  public void testSearchOmdbApi() {
    callSearch("omdbapi", "Harry Potter");
  }

  @Test
  public void testSearchMovieMeter() {
    callSearch("moviemeter", "Harry Potter");
  }

  @Test
  public void testSearchTrakt() {
    callSearch("trakt", "Harry Potter");
  }

  private void callSearch(String providerId, String searchString) {
    try {
      IMovieMetadataProvider mp = new UniversalMovieMetadataProvider();

      mp.getProviderInfo().getConfig().setValue("search", providerId);

      MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
      options.setSearchQuery(searchString);
      options.setLanguage(MediaLanguages.en);
      List<MediaSearchResult> results = new ArrayList<>(mp.search(options));

      // did we get a result?
      assertThat(results).isNotEmpty();

      // are our results from the chosen provider?
      for (MediaSearchResult result : results) {
        // check this via the ID which must exist for the given scraper
        // except.. omdbapi does not have an own id - use imdb id here
        if ("omdbapi".equals(providerId)) {
          assertThat(result.getIdAsString("imdb")).isNotBlank();
        }
        else {
          assertThat(result.getIdAsString(providerId)).isNotBlank();
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testScrapeFromTmdbTwelveMonkeys() {
    try {
      IMovieMetadataProvider mp = new UniversalMovieMetadataProvider();

      prepareTmdbConfig(mp);

      MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
      options.setId("tmdb", "63");
      options.setLanguage(MediaLanguages.de);

      MediaMetadata mediaMetadata = mp.getMetadata(options);

      assertThat(mediaMetadata.getIds()).containsValues("tt0114746", 63);
      assertThat(mediaMetadata.getTitle()).isEqualTo("12 Monkeys");
      assertThat(mediaMetadata.getOriginalTitle()).isEqualTo("Twelve Monkeys");
      assertThat(mediaMetadata.getTagline()).isNotEmpty();
      assertThat(mediaMetadata.getYear()).isEqualTo(1995);
      assertThat(mediaMetadata.getReleaseDate()).isNotNull();
      assertThat(mediaMetadata.getRuntime()).isGreaterThan(0);
      assertThat(mediaMetadata.getPlot()).isNotEmpty();
      assertThat(mediaMetadata.getRatings()).isNotEmpty();
      assertThat(mediaMetadata.getTop250()).isEqualTo(0); // no top250 available at TMDB
      assertThat(mediaMetadata.getGenres()).isNotEmpty();
      assertThat(mediaMetadata.getCertifications()).isNotEmpty();
      assertThat(mediaMetadata.getProductionCompanies()).isNotEmpty();
      assertThat(mediaMetadata.getCastMembers()).isNotEmpty();
      assertThat(mediaMetadata.getSpokenLanguages()).isNotEmpty();
      assertThat(mediaMetadata.getCountries()).isNotEmpty();
      assertThat(mediaMetadata.getMediaArt()).isNotEmpty();
      assertThat(mediaMetadata.getTags()).isNotEmpty();
      assertThat(mediaMetadata.getCollectionName()).isEmpty(); // no collection available for this movie
    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testScrapeFromTmdbDespicableMe() {
    try {
      IMovieMetadataProvider mp = new UniversalMovieMetadataProvider();

      prepareTmdbConfig(mp);

      MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
      options.setId("tmdb", "20352");
      options.setLanguage(MediaLanguages.en);

      MediaMetadata mediaMetadata = mp.getMetadata(options);

      assertThat(mediaMetadata.getIds()).containsValues("tt1323594", 20352, 86066); // 86066 is the collection id
      assertThat(mediaMetadata.getTitle()).isEqualTo("Despicable Me");
      assertThat(mediaMetadata.getOriginalTitle()).isEqualTo("Despicable Me");
      assertThat(mediaMetadata.getTagline()).isNotEmpty();
      assertThat(mediaMetadata.getYear()).isEqualTo(2010);
      assertThat(mediaMetadata.getReleaseDate()).isNotNull();
      assertThat(mediaMetadata.getRuntime()).isGreaterThan(0);
      assertThat(mediaMetadata.getPlot()).isNotEmpty();
      assertThat(mediaMetadata.getRatings()).isNotEmpty();
      assertThat(mediaMetadata.getTop250()).isEqualTo(0); // no top250 available at TMDB
      assertThat(mediaMetadata.getGenres()).isNotEmpty();
      assertThat(mediaMetadata.getCertifications()).isNotEmpty();
      assertThat(mediaMetadata.getProductionCompanies()).isNotEmpty();
      assertThat(mediaMetadata.getCastMembers()).isNotEmpty();
      assertThat(mediaMetadata.getSpokenLanguages()).isNotEmpty();
      assertThat(mediaMetadata.getCountries()).isNotEmpty();
      assertThat(mediaMetadata.getMediaArt()).isNotEmpty();
      assertThat(mediaMetadata.getTags()).isNotEmpty();
      assertThat(mediaMetadata.getCollectionName()).isNotEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  private void prepareTmdbConfig(IMovieMetadataProvider mp) {
    mp.getProviderInfo().getConfig().setValue("title", "tmdb");
    mp.getProviderInfo().getConfig().setValue("originalTitle", "tmdb");
    mp.getProviderInfo().getConfig().setValue("tagline", "tmdb");
    mp.getProviderInfo().getConfig().setValue("year", "tmdb");
    mp.getProviderInfo().getConfig().setValue("releaseDate", "tmdb");
    mp.getProviderInfo().getConfig().setValue("plot", "tmdb");
    mp.getProviderInfo().getConfig().setValue("runtime", "tmdb");
    mp.getProviderInfo().getConfig().setValue("ratings", "tmdb");
    mp.getProviderInfo().getConfig().setValue("top250", "tmdb");
    mp.getProviderInfo().getConfig().setValue("genres", "tmdb");
    mp.getProviderInfo().getConfig().setValue("certifications", "tmdb");
    mp.getProviderInfo().getConfig().setValue("productionCompanies", "tmdb");
    mp.getProviderInfo().getConfig().setValue("castMembers", "tmdb");
    mp.getProviderInfo().getConfig().setValue("spokenLanguages", "tmdb");
    mp.getProviderInfo().getConfig().setValue("countries", "tmdb");
    mp.getProviderInfo().getConfig().setValue("mediaArt", "tmdb");
    mp.getProviderInfo().getConfig().setValue("tags", "tmdb");
    mp.getProviderInfo().getConfig().setValue("collectionName", "tmdb");
  }

  @Test
  public void testScrapeFromTmdbImdbTwelveMonkeys() {
    try {
      IMovieMetadataProvider mp = new UniversalMovieMetadataProvider();

      mp.getProviderInfo().getConfig().setValue("title", "tmdb");
      mp.getProviderInfo().getConfig().setValue("originalTitle", "tmdb");
      mp.getProviderInfo().getConfig().setValue("tagline", "tmdb");
      mp.getProviderInfo().getConfig().setValue("year", "tmdb");
      mp.getProviderInfo().getConfig().setValue("releaseDate", "tmdb");
      mp.getProviderInfo().getConfig().setValue("plot", "tmdb");
      mp.getProviderInfo().getConfig().setValue("runtime", "imdb");
      mp.getProviderInfo().getConfig().setValue("ratings", "imdb");
      mp.getProviderInfo().getConfig().setValue("top250", "imdb");
      mp.getProviderInfo().getConfig().setValue("genres", "imdb");
      mp.getProviderInfo().getConfig().setValue("certifications", "imdb");
      mp.getProviderInfo().getConfig().setValue("productionCompanies", "imdb");
      mp.getProviderInfo().getConfig().setValue("castMembers", "tmdb");
      mp.getProviderInfo().getConfig().setValue("spokenLanguages", "tmdb");
      mp.getProviderInfo().getConfig().setValue("countries", "tmdb");
      mp.getProviderInfo().getConfig().setValue("mediaArt", "tmdb");
      mp.getProviderInfo().getConfig().setValue("tags", "tmdb");
      mp.getProviderInfo().getConfig().setValue("collectionName", "tmdb");

      MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
      options.setId("tmdb", "63");
      options.setLanguage(MediaLanguages.en);

      MediaMetadata mediaMetadata = mp.getMetadata(options);

      assertThat(mediaMetadata.getIds()).containsValues("tt0114746", 63);
      assertThat(mediaMetadata.getTitle()).isEqualTo("12 Monkeys");
      assertThat(mediaMetadata.getOriginalTitle()).isEqualTo("Twelve Monkeys");
      assertThat(mediaMetadata.getTagline()).isNotEmpty();
      assertThat(mediaMetadata.getYear()).isEqualTo(1995);
      assertThat(mediaMetadata.getReleaseDate()).isNotNull();
      assertThat(mediaMetadata.getRuntime()).isGreaterThan(0);
      assertThat(mediaMetadata.getPlot()).isNotEmpty();
      assertThat(mediaMetadata.getRatings()).isNotEmpty();
      // assertThat(mediaMetadata.getTop250()).isGreaterThan(0); // at the moment it is #243
      assertThat(mediaMetadata.getGenres()).isNotEmpty();
      assertThat(mediaMetadata.getCertifications()).isNotEmpty();
      assertThat(mediaMetadata.getProductionCompanies()).isNotEmpty();
      assertThat(mediaMetadata.getCastMembers()).isNotEmpty();
      assertThat(mediaMetadata.getSpokenLanguages()).isNotEmpty();
      assertThat(mediaMetadata.getCountries()).isNotEmpty();
      assertThat(mediaMetadata.getMediaArt()).isNotEmpty();
      assertThat(mediaMetadata.getTags()).isNotEmpty();
      assertThat(mediaMetadata.getCollectionName()).isEmpty(); // no collection available for this movie
    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testScrapeFromTmdbImdbDespicableMe() {
    // movie #2: Despicable Me
    try {
      IMovieMetadataProvider mp = new UniversalMovieMetadataProvider();

      mp.getProviderInfo().getConfig().setValue("title", "tmdb");
      mp.getProviderInfo().getConfig().setValue("originalTitle", "tmdb");
      mp.getProviderInfo().getConfig().setValue("tagline", "tmdb");
      mp.getProviderInfo().getConfig().setValue("year", "imdb");
      mp.getProviderInfo().getConfig().setValue("releaseDate", "imdb");
      mp.getProviderInfo().getConfig().setValue("plot", "tmdb");
      mp.getProviderInfo().getConfig().setValue("runtime", "imdb");
      mp.getProviderInfo().getConfig().setValue("ratings", "imdb");
      mp.getProviderInfo().getConfig().setValue("top250", "imdb");
      mp.getProviderInfo().getConfig().setValue("genres", "imdb");
      mp.getProviderInfo().getConfig().setValue("certifications", "imdb");
      mp.getProviderInfo().getConfig().setValue("productionCompanies", "imdb");
      mp.getProviderInfo().getConfig().setValue("castMembers", "imdb");
      mp.getProviderInfo().getConfig().setValue("spokenLanguages", "tmdb");
      mp.getProviderInfo().getConfig().setValue("countries", "tmdb");
      mp.getProviderInfo().getConfig().setValue("mediaArt", "tmdb");
      mp.getProviderInfo().getConfig().setValue("tags", "tmdb");
      mp.getProviderInfo().getConfig().setValue("collectionName", "tmdb");

      MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
      options.setId("tmdb", "20352");
      options.setLanguage(MediaLanguages.en);

      MediaMetadata mediaMetadata = mp.getMetadata(options);

      assertThat(mediaMetadata.getIds()).containsValues("tt1323594", 20352, 86066); // 86066 is the collection id
      assertThat(mediaMetadata.getTitle()).isEqualTo("Despicable Me");
      assertThat(mediaMetadata.getOriginalTitle()).isEqualTo("Despicable Me");
      assertThat(mediaMetadata.getTagline()).isNotEmpty();
      assertThat(mediaMetadata.getYear()).isEqualTo(2010);
      assertThat(mediaMetadata.getReleaseDate()).isNotNull();
      assertThat(mediaMetadata.getRuntime()).isGreaterThan(0);
      assertThat(mediaMetadata.getPlot()).isNotEmpty();
      assertThat(mediaMetadata.getRatings()).isNotEmpty();
      assertThat(mediaMetadata.getTop250()).isEqualTo(0); // not in top250
      assertThat(mediaMetadata.getGenres()).isNotEmpty();
      assertThat(mediaMetadata.getCertifications()).isNotEmpty();
      assertThat(mediaMetadata.getProductionCompanies()).isNotEmpty();
      assertThat(mediaMetadata.getCastMembers()).isNotEmpty();
      assertThat(mediaMetadata.getSpokenLanguages()).isNotEmpty();
      assertThat(mediaMetadata.getCountries()).isNotEmpty();
      assertThat(mediaMetadata.getMediaArt()).isNotEmpty();
      assertThat(mediaMetadata.getTags()).isNotEmpty();
      assertThat(mediaMetadata.getCollectionName()).isNotEmpty();
    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testScrapeFromTmdbImdbOmdbapiTwelveMonkeys() {
    try {
      IMovieMetadataProvider mp = new UniversalMovieMetadataProvider();

      mp.getProviderInfo().getConfig().setValue("title", "tmdb");
      mp.getProviderInfo().getConfig().setValue("originalTitle", "tmdb");
      mp.getProviderInfo().getConfig().setValue("tagline", "tmdb");
      mp.getProviderInfo().getConfig().setValue("year", "omdbapi");
      mp.getProviderInfo().getConfig().setValue("releaseDate", "omdbapi");
      mp.getProviderInfo().getConfig().setValue("plot", "tmdb");
      mp.getProviderInfo().getConfig().setValue("runtime", "imdb");
      mp.getProviderInfo().getConfig().setValue("ratings", "omdbapi");
      mp.getProviderInfo().getConfig().setValue("top250", "imdb");
      mp.getProviderInfo().getConfig().setValue("genres", "imdb");
      mp.getProviderInfo().getConfig().setValue("certifications", "omdbapi");
      mp.getProviderInfo().getConfig().setValue("productionCompanies", "imdb");
      mp.getProviderInfo().getConfig().setValue("castMembers", "tmdb");
      mp.getProviderInfo().getConfig().setValue("spokenLanguages", "tmdb");
      mp.getProviderInfo().getConfig().setValue("countries", "tmdb");
      mp.getProviderInfo().getConfig().setValue("mediaArt", "tmdb");
      mp.getProviderInfo().getConfig().setValue("tags", "tmdb");
      mp.getProviderInfo().getConfig().setValue("collectionName", "tmdb");

      MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
      options.setId("tmdb", "63");
      options.setLanguage(MediaLanguages.de);

      MediaMetadata mediaMetadata = mp.getMetadata(options);

      assertThat(mediaMetadata.getIds()).containsValues("tt0114746", 63);
      assertThat(mediaMetadata.getTitle()).isEqualTo("12 Monkeys");
      assertThat(mediaMetadata.getOriginalTitle()).isEqualTo("Twelve Monkeys");
      assertThat(mediaMetadata.getTagline()).isNotEmpty();
      assertThat(mediaMetadata.getYear()).isEqualTo(1995);
      assertThat(mediaMetadata.getReleaseDate()).isNotNull();
      assertThat(mediaMetadata.getRuntime()).isGreaterThan(0);
      assertThat(mediaMetadata.getPlot()).isNotEmpty();
      assertThat(mediaMetadata.getRatings()).isNotEmpty();
      // assertThat(mediaMetadata.getTop250()).isGreaterThan(0); // at the moment it is #243
      assertThat(mediaMetadata.getGenres()).isNotEmpty();
      assertThat(mediaMetadata.getCertifications()).isNotEmpty();
      assertThat(mediaMetadata.getProductionCompanies()).isNotEmpty();
      assertThat(mediaMetadata.getCastMembers()).isNotEmpty();
      assertThat(mediaMetadata.getSpokenLanguages()).isNotEmpty();
      assertThat(mediaMetadata.getCountries()).isNotEmpty();
      assertThat(mediaMetadata.getMediaArt()).isNotEmpty();
      assertThat(mediaMetadata.getTags()).isNotEmpty();
      assertThat(mediaMetadata.getCollectionName()).isEmpty(); // no collection available for this movie
    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testScrapeFromMovieMeterImdbTwelveMonkeys() {
    try {
      IMovieMetadataProvider mp = new UniversalMovieMetadataProvider();

      mp.getProviderInfo().getConfig().setValue("title", "moviemeter");
      mp.getProviderInfo().getConfig().setValue("originalTitle", "imdb");
      mp.getProviderInfo().getConfig().setValue("tagline", "imdb");
      mp.getProviderInfo().getConfig().setValue("year", "imdb");
      mp.getProviderInfo().getConfig().setValue("releaseDate", "imdb");
      mp.getProviderInfo().getConfig().setValue("plot", "moviemeter");
      mp.getProviderInfo().getConfig().setValue("runtime", "imdb");
      mp.getProviderInfo().getConfig().setValue("ratings", "imdb");
      mp.getProviderInfo().getConfig().setValue("top250", "imdb");
      mp.getProviderInfo().getConfig().setValue("genres", "imdb");
      mp.getProviderInfo().getConfig().setValue("certifications", "imdb");
      mp.getProviderInfo().getConfig().setValue("productionCompanies", "imdb");
      mp.getProviderInfo().getConfig().setValue("castMembers", "imdb");
      mp.getProviderInfo().getConfig().setValue("spokenLanguages", "imdb");
      mp.getProviderInfo().getConfig().setValue("countries", "imdb");
      mp.getProviderInfo().getConfig().setValue("mediaArt", "imdb");
      mp.getProviderInfo().getConfig().setValue("tags", "imdb");
      mp.getProviderInfo().getConfig().setValue("collectionName", "imdb");

      MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
      options.setId("tmdb", "63");
      options.setLanguage(MediaLanguages.nl);

      MediaMetadata mediaMetadata = mp.getMetadata(options);

      assertThat(mediaMetadata.getIds()).containsValues("tt0114746", 185);
      assertThat(mediaMetadata.getTitle()).isEqualTo("Twelve Monkeys");
      assertThat(mediaMetadata.getOriginalTitle()).isEqualTo("Twelve Monkeys");
      assertThat(mediaMetadata.getTagline()).isNotEmpty();
      assertThat(mediaMetadata.getYear()).isEqualTo(1995);
      assertThat(mediaMetadata.getReleaseDate()).isNotNull();
      assertThat(mediaMetadata.getRuntime()).isGreaterThan(0);
      assertThat(mediaMetadata.getPlot()).isNotEmpty();
      assertThat(mediaMetadata.getRatings()).isNotEmpty();
      // assertThat(mediaMetadata.getTop250()).isGreaterThan(0); // at the moment it is #243
      assertThat(mediaMetadata.getGenres()).isNotEmpty();
      assertThat(mediaMetadata.getCertifications()).isNotEmpty();
      assertThat(mediaMetadata.getProductionCompanies()).isNotEmpty();
      assertThat(mediaMetadata.getCastMembers()).isNotEmpty();
      assertThat(mediaMetadata.getSpokenLanguages()).isNotEmpty();
      assertThat(mediaMetadata.getCountries()).isNotEmpty();
      assertThat(mediaMetadata.getMediaArt()).isNotEmpty();
      assertThat(mediaMetadata.getTags()).isEmpty(); // this movie has no tag
      assertThat(mediaMetadata.getCollectionName()).isEmpty(); // no collection available for this movie
    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testScrapeFromTmdbTraktTwelveMonkeys() {
    try {
      IMovieMetadataProvider mp = new UniversalMovieMetadataProvider();

      mp.getProviderInfo().getConfig().setValue("title", "trakt");
      mp.getProviderInfo().getConfig().setValue("originalTitle", "tmdb");
      mp.getProviderInfo().getConfig().setValue("tagline", "tmdb");
      mp.getProviderInfo().getConfig().setValue("year", "tmdb");
      mp.getProviderInfo().getConfig().setValue("releaseDate", "tmdb");
      mp.getProviderInfo().getConfig().setValue("plot", "tmdb");
      mp.getProviderInfo().getConfig().setValue("runtime", "tmdb");
      mp.getProviderInfo().getConfig().setValue("ratings", "trakt");
      mp.getProviderInfo().getConfig().setValue("top250", "-");
      mp.getProviderInfo().getConfig().setValue("genres", "trakt");
      mp.getProviderInfo().getConfig().setValue("certifications", "tmdb");
      mp.getProviderInfo().getConfig().setValue("productionCompanies", "tmdb");
      mp.getProviderInfo().getConfig().setValue("castMembers", "tmdb");
      mp.getProviderInfo().getConfig().setValue("spokenLanguages", "tmdb");
      mp.getProviderInfo().getConfig().setValue("countries", "tmdb");
      mp.getProviderInfo().getConfig().setValue("mediaArt", "tmdb");
      mp.getProviderInfo().getConfig().setValue("tags", "tmdb");
      mp.getProviderInfo().getConfig().setValue("collectionName", "tmdb");

      MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
      options.setId("imdb", "tt0114746");
      options.setLanguage(MediaLanguages.de);

      MediaMetadata mediaMetadata = mp.getMetadata(options);

      assertThat(mediaMetadata.getIds()).containsValues("tt0114746", 63, 34);
      assertThat(mediaMetadata.getTitle()).isEqualTo("12 Monkeys");
      assertThat(mediaMetadata.getOriginalTitle()).isEqualTo("Twelve Monkeys");
      assertThat(mediaMetadata.getTagline()).isNotEmpty();
      assertThat(mediaMetadata.getYear()).isEqualTo(1995);
      assertThat(mediaMetadata.getReleaseDate()).isNotNull();
      assertThat(mediaMetadata.getRuntime()).isGreaterThan(0);
      assertThat(mediaMetadata.getPlot()).isNotEmpty();
      assertThat(mediaMetadata.getRatings()).isNotEmpty();
      assertThat(mediaMetadata.getTop250()).isEqualTo(0); // not getting scraped
      assertThat(mediaMetadata.getGenres()).isNotEmpty();
      assertThat(mediaMetadata.getCertifications()).isNotEmpty();
      assertThat(mediaMetadata.getProductionCompanies()).isNotEmpty();
      assertThat(mediaMetadata.getCastMembers()).isNotEmpty();
      assertThat(mediaMetadata.getSpokenLanguages()).isNotEmpty();
      assertThat(mediaMetadata.getCountries()).isNotEmpty();
      assertThat(mediaMetadata.getMediaArt()).isNotEmpty();
      assertThat(mediaMetadata.getTags()).isNotEmpty();
      assertThat(mediaMetadata.getCollectionName()).isEmpty(); // no collection available for this movie
    }
    catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }
}
