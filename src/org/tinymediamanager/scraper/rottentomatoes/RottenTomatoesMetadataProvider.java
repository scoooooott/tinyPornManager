package org.tinymediamanager.scraper.rottentomatoes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.MediaCastMember;
import org.tinymediamanager.scraper.MediaCastMember.CastType;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.MetadataUtil;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.thirdparty.RingBuffer;

import com.omertron.rottentomatoesapi.RottenTomatoesApi;
import com.omertron.rottentomatoesapi.RottenTomatoesException;
import com.omertron.rottentomatoesapi.model.RTCast;
import com.omertron.rottentomatoesapi.model.RTMovie;
import com.omertron.rottentomatoesapi.model.RTPerson;

/**
 * The class RottenTomatoesMetadataProvider. A scraper for Rotten tomatoes
 * 
 * @author Manuel Laggner
 */
public class RottenTomatoesMetadataProvider implements IMediaMetadataProvider {
  private static final Logger            LOGGER            = LoggerFactory.getLogger(RottenTomatoesMetadataProvider.class);
  private static final RingBuffer<Long>  connectionCounter = new RingBuffer<Long>(5);
  private static final String            apiKey            = "heryy75wnpvbkd772vrrbvhx";
  private static final MediaProviderInfo providerInfo      = new MediaProviderInfo(Constants.ROTTENTOMATOESID, "rottentomatoes.com",
                                                               "Scraper for themoviedb.org which is able to scrape movie metadata, artwork and trailers");
  private static RottenTomatoesApi       api;

  public RottenTomatoesMetadataProvider() throws Exception {
    // create a new instance of the tmdb api
    if (api == null) {
      try {
        api = new RottenTomatoesApi(apiKey);
      }
      catch (Exception e) {
        LOGGER.error("RottenTomatoesMetadataProvider", e);

        // remove cached request
        clearRottenTomatoesCache();
        throw e;
      }
    }
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getMetadata() " + options.toString());
    // check if there is a md in the result
    if (options.getResult() != null && options.getResult().getMediaMetadata() != null) {
      LOGGER.debug("RottenTomatoes: getMetadata from cache: " + options.getResult());
      return options.getResult().getMediaMetadata();
    }

    // get ids to scrape
    MediaMetadata md = new MediaMetadata(providerInfo.getId());

    int rottenId = 0;

    // rottenId from searchResult
    if (options.getResult() != null) {
      rottenId = Integer.parseInt(options.getResult().getId());
    }

    // rottenId from option
    if (rottenId == 0) {
      try {
        rottenId = Integer.parseInt(options.getId(providerInfo.getId()));
      }
      catch (Exception e) {
      }
    }

    // imdbId from option
    String imdbId = options.getImdbId();
    if (rottenId == 0 && !Utils.isValidImdbId(imdbId)) {
      LOGGER.warn("not possible to scrape from RottenTomatoes - no rottenId/imdbId found");
      return md;
    }

    // scrape
    LOGGER.debug("RottenTomatoes: getMetadata: rottenId = " + rottenId + "; imdbId = " + imdbId);
    RTMovie movie = null;
    synchronized (api) {
      trackConnections();
      if (rottenId == 0 && Utils.isValidImdbId(imdbId)) {
        try {
          movie = api.getMoviesAlias("imdb", imdbId);
        }
        catch (RottenTomatoesException e) {
          LOGGER.warn("problem getting data vom RottenTomatoes: " + e.getMessage());

          // remove cached request
          clearRottenTomatoesCache();
        }
      }
      if (movie == null && rottenId != 0) {
        try {
          movie = api.getDetailedInfo(rottenId);
        }
        catch (RottenTomatoesException e) {
          LOGGER.warn("problem getting data vom RottenTomatoes: " + e.getMessage());

          // remove cached request
          clearRottenTomatoesCache();
        }
      }

      if (movie == null) {
        LOGGER.warn("no result found");
        return md;
      }
    }

    md.setId(providerInfo.getId(), movie.getId());
    for (Entry<String, String> entry : movie.getAlternateIds().entrySet()) {
      if ("imdb".equals(entry.getKey())) {
        md.setId(MediaMetadata.IMDBID, "tt" + entry.getValue());
      }
    }
    md.storeMetadata(MediaMetadata.PLOT, movie.getSynopsis());
    md.storeMetadata(MediaMetadata.TITLE, movie.getTitle());
    md.storeMetadata(MediaMetadata.YEAR, movie.getYear());
    md.storeMetadata(MediaMetadata.PRODUCTION_COMPANY, movie.getStudio());

    // rating
    for (Entry<String, String> entry : movie.getRatings().entrySet()) {
      if ("audience_score".equals(entry.getKey())) {
        try {
          md.storeMetadata(MediaMetadata.RATING, Integer.parseInt(entry.getValue()) / 10d);
        }
        catch (Exception e) {
        }
      }
    }

    md.storeMetadata(MediaMetadata.RUNTIME, movie.getRuntime());

    // genres
    for (String genre : movie.getGenres()) {
      if ("Science Fiction & Fantasy".equals(genre)) {
        md.addGenre(MediaGenres.FANTASY);
        md.addGenre(MediaGenres.SCIENCE_FICTION);
      }
      else {
        MediaGenres g = MediaGenres.getGenre(genre);
        if (g != null) {
          md.addGenre(g);
        }
      }
    }

    // cast
    for (RTCast rtCast : movie.getCast()) {
      MediaCastMember cm = new MediaCastMember();
      cm.setName(rtCast.getCastName());
      cm.setType(CastType.ACTOR);

      String roles = "";
      for (String character : rtCast.getCharacters()) {
        if (StringUtils.isNotBlank(roles)) {
          roles += ", ";
        }
        roles += character;
      }
      cm.setCharacter(roles);

      md.addCastMember(cm);
    }

    // directors
    for (RTPerson person : movie.getDirectors()) {
      MediaCastMember cm = new MediaCastMember();
      cm.setType(CastType.DIRECTOR);
      cm.setName(person.getName());

      md.addCastMember(cm);
    }

    return md;
  }

  @Override
  public List<MediaSearchResult> search(MediaSearchOptions query) throws Exception {
    // check type
    if (query.getMediaType() == MediaType.MOVIE) {
      return searchMovies(query);
    }

    throw new Exception("wrong media type for this scraper");
  }

  public List<MediaSearchResult> searchMovies(MediaSearchOptions query) throws Exception {
    LOGGER.debug("search() " + query.toString());
    List<MediaSearchResult> resultList = new ArrayList<MediaSearchResult>();
    String searchString = "";

    if (StringUtils.isEmpty(searchString) && StringUtils.isNotEmpty(query.get(MediaSearchOptions.SearchParam.QUERY))) {
      searchString = query.get(MediaSearchOptions.SearchParam.QUERY);
    }

    if (StringUtils.isEmpty(searchString)) {
      LOGGER.debug("RT Scraper: empty searchString");
      return resultList;
    }

    searchString = MetadataUtil.removeNonSearchCharacters(searchString);

    // begin search
    LOGGER.info("========= BEGIN RT Scraper Search for: " + searchString);

    List<RTMovie> moviesFound = new ArrayList<RTMovie>();
    String imdbId = "";
    synchronized (api) {
      // 1. try with IMDBid
      if (StringUtils.isNotEmpty(query.get(MediaSearchOptions.SearchParam.IMDBID))) {
        imdbId = query.get(MediaSearchOptions.SearchParam.IMDBID);
        trackConnections();
        try {
          moviesFound.addAll(api.getMoviesSearch(imdbId, 30, 1));
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data vom rt: " + e.getMessage());
          // remove cached request
          clearRottenTomatoesCache();
        }
      }
      // 2. try to search with searchString
      if (moviesFound.isEmpty()) {
        trackConnections();
        try {
          moviesFound.addAll(api.getMoviesSearch(searchString, 30, 1));
        }
        catch (Exception e) {
          LOGGER.warn("problem getting data vom rt: " + e.getMessage());
          // remove cached request
          clearRottenTomatoesCache();
        }
      }
    }

    if (moviesFound != null) {
      LOGGER.info("found " + moviesFound.size() + " results");
    }

    if (moviesFound == null || moviesFound.size() == 0) {
      return resultList;
    }

    for (RTMovie movie : moviesFound) {
      if (movie == null) {
        continue;
      }

      MediaSearchResult sr = new MediaSearchResult(providerInfo.getId());
      sr.setId(Integer.toString(movie.getId()));
      sr.setTitle(movie.getTitle());

      // alternate IDs
      if (movie.getAlternateIds() != null) {
        sr.setIMDBId(movie.getAlternateIds().get("imdb"));
      }

      // poster
      if (movie.getArtwork() != null) {
        String posterUrl = movie.getArtwork().get("detailed");
        if (StringUtils.isBlank(posterUrl)) {
          posterUrl = movie.getArtwork().get("original");
        }
        if (StringUtils.isNotBlank(posterUrl)) {
          sr.setPosterUrl(posterUrl.replace("_tmb.", "_det."));
        }
      }

      sr.setYear(Integer.toString(movie.getYear()));
      // parse release date to year
      if (StringUtils.isBlank(sr.getYear()) && movie.getReleaseDates() != null) {
        String releaseDate = movie.getReleaseDates().get("theater");
        if (StringUtils.isBlank(releaseDate)) {
          releaseDate = movie.getReleaseDates().get("dvd");
        }
        if (releaseDate != null && releaseDate.length() > 3) {
          sr.setYear(releaseDate.substring(0, 4));
        }
      }

      // populate extra args
      MetadataUtil.copySearchQueryToSearchResult(query, sr);

      if (imdbId.equals(sr.getIMDBId())) {
        // perfect match
        sr.setScore(1);
      }
      else {
        // compare score based on names
        sr.setScore(MetadataUtil.calculateScore(searchString, movie.getTitle()));
      }

      resultList.add(sr);
    }

    Collections.sort(resultList);
    Collections.reverse(resultList);

    return resultList;
  }

  /*
   * 5 calls per 1 second
   */
  private void trackConnections() {
    Long currentTime = System.currentTimeMillis();
    if (connectionCounter.count() == connectionCounter.maxSize()) {
      Long oldestConnection = connectionCounter.getTailItem();
      if (oldestConnection > (currentTime - 1000)) {
        LOGGER.debug("connection limit reached, throttling " + connectionCounter);
        try {
          Thread.sleep(1100 - (currentTime - oldestConnection));
        }
        catch (InterruptedException e) {
          LOGGER.warn(e.getMessage());
        }
      }
    }

    currentTime = System.currentTimeMillis();
    connectionCounter.add(currentTime);
  }

  private void clearRottenTomatoesCache() {
    CachedUrl.cleanupCacheForSpecificHost("api.rottentomatoes.com");
  }
}
