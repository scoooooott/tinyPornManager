package org.tinymediamanager.scraper.imdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.tinymediamanager.scraper.MediaArt;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.SearchQuery;

public class ImdbMetadataProviderTest {

  @Test
  public void testSearch() {
    ImdbMetadataProvider mp = null;
    List<MediaSearchResult> results = null;

    /*
     * test on akas.imdb.com - "9"
     */
    results = null;
    try {
      mp = new ImdbMetadataProvider(ImdbSiteDefinition.IMDB_COM);
      results = mp.search(new SearchQuery(MediaType.MOVIE, SearchQuery.Field.QUERY, "9"));
    } catch (Exception e) {
    }

    // did we get a result?
    assertNotNull("Result", results);

    // result count
    assertEquals("Result count", 5, results.size());

    // check first result (9 - 2009 - tt0472033)
    MediaSearchResult result = results.get(0);
    checkSearchResult("9", "2009", "tt0472033", result);

    // check second result (9 - 2005 - tt0443424)
    result = results.get(1);
    checkSearchResult("9", "2005", "tt0443424", result);

    // check third result (9 - 2002 - tt0342012)
    result = results.get(2);
    checkSearchResult("9", "2002", "tt0342012", result);

    // check fourth result (9 - 2009 - tt1430606)
    result = results.get(3);
    checkSearchResult("9", "2009", "tt1430606", result);

    // check fifth result (9 - 1996 - tt0191312)
    result = results.get(4);
    checkSearchResult("9", "1996", "tt0191312", result);

    /*
     * test on akas.imdb.com - "Inglorious Basterds"
     */
    results = null;
    try {
      mp = new ImdbMetadataProvider(ImdbSiteDefinition.IMDB_COM);
      results = mp.search(new SearchQuery(MediaType.MOVIE, SearchQuery.Field.QUERY, "Inglorious Basterds"));
    } catch (Exception e) {
    }

    // did we get a result?
    assertNotNull("Result", results);

    // result count
    assertEquals("Result count", 10, results.size());

    // check first result (Inglourious Basterds - 2009 - tt0361748)
    result = results.get(0);
    checkSearchResult("Inglourious Basterds", "2009", "tt0361748", result);

    // check second result (Quel maledetto treno blindato - 1978 - tt0076584)
    result = results.get(1);
    checkSearchResult("Quel maledetto treno blindato", "1978", "tt0076584", result);

    // check third result (Eroi dell'inferno - 1987 - tt0342012)
    result = results.get(2);
    checkSearchResult("Eroi dell'inferno", "1987", "tt0422182", result);

    // check 4. result (Inglourious Basterds: Done in 60 seconds - 2010 -
    // tt1430606)
    result = results.get(3);
    checkSearchResult("Inglourious Basterds: Done in 60 seconds", "2010", "tt1837578", result);

    // check 5. result (Inglourious Basterds: Movie Special - 2009 -
    // tt1515156)
    result = results.get(4);
    checkSearchResult("Inglourious Basterds: Movie Special", "2009", "tt1515156", result);

    // check 6. result (Quelli del Maledetto Treno Blindato - Making of
    // 'Inglorious Bastards' - 2007 -
    // tt1028564)
    result = results.get(5);
    checkSearchResult("Quelli del Maledetto Treno Blindato - Making of 'Inglorious Bastards'", "2007", "tt1028564", result);

    // check 7. result (Merah Putih - 2009 - tt1438496)
    result = results.get(6);
    checkSearchResult("Merah Putih", "2009", "tt1438496", result);

    // check 8. result (Joshikyôei hanrangun - 2007 - tt1134826)
    result = results.get(7);
    checkSearchResult("Joshikyôei hanrangun", "2007", "tt1134826", result);

    // check 9. result (Inglorious Bumblers - 2009 - tt1529278)
    result = results.get(8);
    checkSearchResult("Inglorious Bumblers", "2009", "tt1529278", result);

    // check 10. result (Inglorious Relations - 2008 - tt1378268)
    result = results.get(9);
    checkSearchResult("Inglorious Relations", "2008", "tt1378268", result);
  }

  private void checkSearchResult(String title, String year, String imdbId, MediaSearchResult result) {
    // title
    assertEquals("title", title, result.getTitle());
    // year
    assertEquals("year", year, result.getYear());
    // imdbId
    assertEquals("imdbId", imdbId, result.getIMDBId());
  }

  @Test
  public void testScrape() {
    ImdbMetadataProvider mp = null;
    MediaSearchResult sr = null;
    MediaMetadata md = null;

    /*
     * scrape akas.imdb.com - 9 - tt0472033
     */
    mp = new ImdbMetadataProvider(ImdbSiteDefinition.IMDB_COM);
    sr = new MediaSearchResult();
    sr.setIMDBId("tt0472033");
    sr.setTitle("9");
    sr.setYear("2009");

    md = null;
    try {
      md = mp.getMetaData(sr);
    } catch (Exception e) {
    }

    // did we get metadata?
    assertNotNull("MediaMetadata", md);

    // check moviedetails
    checkMovieDetails("9", "2009", "9", 7.0, 63365, "(7) To Defend Us...", md);

    // check poster
    checkMoviePoster("http://ia.media-imdb.com/images/M/MV5BMTY2ODE1MTgxMV5BMl5BanBnXkFtZTcwNTM1NTM2Mg@@._V1._SX195_SY195_.jpg", md);

    /*
     * scrape akas.imdb.com - 12 Monkeys - tt0114746
     */
    mp = new ImdbMetadataProvider(ImdbSiteDefinition.IMDB_COM);
    sr = new MediaSearchResult();
    sr.setIMDBId("tt0114746");
    sr.setTitle("Twelve Monkeys");
    sr.setYear("1995");

    md = null;
    try {
      md = mp.getMetaData(sr);
    } catch (Exception e) {
    }

    // did we get metadata?
    assertNotNull("MediaMetadata", md);

    // check moviedetails
    checkMovieDetails("Twelve Monkeys", "1995", "Twelve Monkeys", 8.1, 262821, "The future is history.", md);

    // check poster
    checkMoviePoster("http://ia.media-imdb.com/images/M/MV5BMTQ4OTM3NzkyN15BMl5BanBnXkFtZTcwMzIwMzgyMQ@@._V1._SX195_SY195_.jpg", md);

    /*
     * scrape akas.imdb.com - Brave - tt1217209
     */
    mp = new ImdbMetadataProvider(ImdbSiteDefinition.IMDB_COM);
    sr = new MediaSearchResult();
    sr.setIMDBId("tt1217209");
    sr.setTitle("Brave");
    sr.setYear("2012");

    md = null;
    try {
      md = mp.getMetaData(sr);
    } catch (Exception e) {
    }

    // did we get metadata?
    assertNotNull("MediaMetadata", md);

    // check moviedetails
    checkMovieDetails("Brave", "2012", "Brave", 7.4, 52871, "Change your fate.", md);

    // check poster
    checkMoviePoster("http://ia.media-imdb.com/images/M/MV5BMzgwODk3ODA1NF5BMl5BanBnXkFtZTcwNjU3NjQ0Nw@@._V1._SX195_SY195_.jpg", md);

  }

  private void checkMovieDetails(String title, String year, String originalTitle, double rating, int voteCount, String tagline, MediaMetadata md) {
    // title
    assertEquals(title, md.getMediaTitle());
    // year
    assertEquals("year", year, md.getYear());
    // original title
    assertEquals("originalTitle", originalTitle, md.getOriginalTitle());
    // rating
    assertEquals("rating", rating, md.getUserRating(), 0.01);
    // count (only check if parsed cout count is smaller than the given
    // votecount)
    if (voteCount > md.getVoteCount()) {
      assertEquals("count", voteCount, md.getVoteCount());
    }
    // tagline
    assertEquals("tagline", tagline, md.getTagline());
  }

  private void checkMoviePoster(String url, MediaMetadata md) {
    // check poster
    List<MediaArt> mediaArt = md.getFanart();
    assertEquals("fanart count", 1, mediaArt.size());
    MediaArt art = mediaArt.get(0);
    assertEquals("poster", url, art.getDownloadUrl());
  }
}
