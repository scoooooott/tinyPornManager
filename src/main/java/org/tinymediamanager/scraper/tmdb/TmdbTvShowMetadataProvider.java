/*
 * Copyright 2012 - 2015 Manuel Laggner
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
package org.tinymediamanager.scraper.tmdb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaCastMember;
import org.tinymediamanager.scraper.MediaEpisode;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.MetadataUtil;

import com.uwetrottmann.tmdb.Tmdb;
import com.uwetrottmann.tmdb.entities.AppendToResponse;
import com.uwetrottmann.tmdb.entities.CastMember;
import com.uwetrottmann.tmdb.entities.ProductionCompany;
import com.uwetrottmann.tmdb.entities.TvEpisode;
import com.uwetrottmann.tmdb.entities.TvResultsPage;
import com.uwetrottmann.tmdb.entities.TvSeason;
import com.uwetrottmann.tmdb.entities.TvShow;
import com.uwetrottmann.tmdb.entities.TvShowComplete;
import com.uwetrottmann.tmdb.enumerations.AppendToResponseItem;

class TmdbTvShowMetadataProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(TmdbTvShowMetadataProvider.class);

  private Tmdb api;

  public TmdbTvShowMetadataProvider(Tmdb api) {
    this.api = api;
  }

  /**
   * searches a TV show with the given query parameters
   *
   * @param query
   *          the query parameters
   * @return a list of found TV shows
   * @throws Exception
   *           any exception which can be thrown while searching
   */
  List<MediaSearchResult> search(MediaSearchOptions query) throws Exception {
    LOGGER.debug("search() " + query.toString());

    List<MediaSearchResult> resultList = new ArrayList<MediaSearchResult>();

    String searchString = "";

    // check type
    if (query.getMediaType() != MediaType.TV_SHOW) {
      throw new Exception("wrong media type for this scraper");
    }

    if (StringUtils.isEmpty(searchString) && StringUtils.isNotEmpty(query.get(MediaSearchOptions.SearchParam.QUERY))) {
      searchString = query.get(MediaSearchOptions.SearchParam.QUERY);
    }

    if (StringUtils.isEmpty(searchString) && StringUtils.isNotEmpty(query.get(MediaSearchOptions.SearchParam.TITLE))) {
      searchString = query.get(MediaSearchOptions.SearchParam.TITLE);
    }

    if (StringUtils.isEmpty(searchString)) {
      LOGGER.debug("TMDB Scraper: empty searchString");
      return resultList;
    }

    searchString = MetadataUtil.removeNonSearchCharacters(searchString);
    String language = query.get(MediaSearchOptions.SearchParam.LANGUAGE);

    // begin search
    LOGGER.info("========= BEGIN TMDB Scraper Search for: " + searchString);
    TvResultsPage resultsPage = null;
    synchronized (api) {
      TmdbConnectionCounter.trackConnections();
      resultsPage = api.searchService().tv(searchString, 1, language, 0, "phrase");
    }

    if (resultsPage == null || resultsPage.results == null) {
      LOGGER.info("found 0 results");
      return resultList;
    }

    LOGGER.info("found " + resultsPage.results.size() + " results");
    for (TvShow show : resultsPage.results) {
      MediaSearchResult result = new MediaSearchResult(TmdbMetadataProvider.providerInfo.getId());
      result.setMediaType(MediaType.TV_SHOW);
      result.setId(Integer.toString(show.id));
      result.setTitle(show.name);
      result.setOriginalTitle(show.original_name);
      result.setPosterUrl(TmdbMetadataProvider.configuration.images.base_url + "w342" + show.poster_path);

      // parse release date to year
      if (show.first_air_date != null) {
        result.setYear(new SimpleDateFormat("yyyy").format(show.first_air_date));
      }

      // calculate score
      result.setScore(MetadataUtil.calculateScore(searchString, result.getTitle()));

      resultList.add(result);
    }

    return resultList;
  }

  /**
   * Get the episode list for the specified TV show
   * 
   * @param options
   *          the scrape options for getting the episode list
   * @return the episode list
   * @throws Exception
   *           any exception which can be thrown while searching
   */
  List<MediaEpisode> getEpisodeList(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getEpisodeList() " + options.toString());
    List<MediaEpisode> episodes = new ArrayList<MediaEpisode>();

    int tmdbId = 0;
    // tmdbId from searchResult
    if (options.getResult() != null) {
      tmdbId = Integer.parseInt(options.getResult().getId());
    }

    // tmdbId from option
    if (tmdbId == 0) {
      tmdbId = options.getTmdbId();
    }

    // no tmdb id, no search..
    if (tmdbId == 0) {
      return episodes;
    }

    String language = options.getLanguage().name();

    // the API does not provide a complete access to all episodes, so we have to
    // fetch the show summary first and every season afterwards..
    synchronized (api) {
      TmdbConnectionCounter.trackConnections();
      TvShowComplete complete = api.tvService().tv(tmdbId, language, null);
      if (complete != null) {
        for (TvSeason season : ListUtils.nullSafe(complete.seasons)) {
          TmdbConnectionCounter.trackConnections();
          TvSeason fullSeason = api.tvSeasonsService().season(tmdbId, season.season_number, language, null);
          if (fullSeason != null) {
            for (TvEpisode episode : ListUtils.nullSafe(fullSeason.episodes)) {
              MediaEpisode ep = new MediaEpisode(TmdbMetadataProvider.providerInfo.getId());
              ep.episode = episode.episode_number;
              ep.season = episode.season_number;
              ep.title = episode.name;
              ep.rating = episode.vote_average;
              ep.ids.put(TmdbMetadataProvider.providerInfo.getId(), episode.id);
              episodes.add(ep);
            }
          }
        }
      }
    }

    return episodes;
  }

  /**
   * Get the meta data for either the TV show or an episode
   * 
   * @param options
   *          the scrape options
   * @return the meta data
   * @throws Exception
   *           any exception which can be thrown while searching
   */
  MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
    switch (options.getType()) {
      case TV_SHOW:
        return getTvShowMetadata(options);

      case TV_EPISODE:
        return getEpisodeMetadata(options);

      default:
        throw new Exception("unsupported media type");
    }
  }

  private MediaMetadata getTvShowMetadata(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getTvShowMetadata() " + options.toString());
    MediaMetadata md = new MediaMetadata(TmdbMetadataProvider.providerInfo.getId());

    int tmdbId = 0;
    // tmdbId from searchResult
    if (options.getResult() != null) {
      tmdbId = Integer.parseInt(options.getResult().getId());
    }

    // tmdbId from option
    if (tmdbId == 0) {
      tmdbId = options.getTmdbId();
    }

    // no tmdb id, no scrape..
    if (tmdbId == 0) {
      return md;
    }

    String language = options.getLanguage().name();

    // get the data from tmdb
    TvShowComplete complete = null;
    synchronized (api) {
      TmdbConnectionCounter.trackConnections();
      complete = api.tvService().tv(tmdbId, language, new AppendToResponse(AppendToResponseItem.CREDITS));
    }

    if (complete == null) {
      return md;
    }

    md.setId(TmdbMetadataProvider.providerInfo.getId(), tmdbId);
    md.storeMetadata(MediaMetadata.TITLE, complete.name);
    md.storeMetadata(MediaMetadata.ORIGINAL_TITLE, complete.original_name);
    md.storeMetadata(MediaMetadata.RATING, complete.vote_average);
    md.storeMetadata(MediaMetadata.VOTE_COUNT, complete.vote_count);
    if (complete.first_air_date != null) {
      md.storeMetadata(MediaMetadata.RELEASE_DATE, complete.first_air_date);
    }
    md.storeMetadata(MediaMetadata.PLOT, complete.overview);
    md.storeMetadata(MediaMetadata.POSTER_URL, TmdbMetadataProvider.configuration.images.base_url + "w342" + complete.poster_path);

    String productionCompany = "";
    for (ProductionCompany company : ListUtils.nullSafe(complete.production_companies)) {
      if (!productionCompany.isEmpty()) {
        productionCompany += ", ";
      }
      productionCompany += company.name;
    }
    md.storeMetadata(MediaMetadata.PRODUCTION_COMPANY, productionCompany);
    md.storeMetadata(MediaMetadata.STATUS, complete.status);

    if (complete.first_air_date != null) {
      md.storeMetadata(MediaMetadata.YEAR, new SimpleDateFormat("yyyy").format(complete.first_air_date));
    }

    if (complete.credits != null) {
      for (CastMember castMember : ListUtils.nullSafe(complete.credits.cast)) {
        MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.ACTOR);
        cm.setName(castMember.name);
        cm.setName(castMember.character);
        md.addCastMember(cm);
      }
    }

    return md;
  }

  private MediaMetadata getEpisodeMetadata(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getEpisodeMetadata() " + options.toString());
    MediaMetadata md = new MediaMetadata(TmdbMetadataProvider.providerInfo.getId());

    int tmdbId = 0;
    // tmdbId from searchResult
    if (options.getResult() != null) {
      tmdbId = Integer.parseInt(options.getResult().getId());
    }

    // tmdbId from option
    if (tmdbId == 0) {
      tmdbId = options.getTmdbId();
    }

    // no tmdb id, no scrape..
    if (tmdbId == 0) {
      return md;
    }

    // get episode number and season number
    int seasonNr = -1;
    int episodeNr = -1;

    try {
      seasonNr = Integer.parseInt(options.getId(MediaMetadata.SEASON_NR));
      episodeNr = Integer.parseInt(options.getId(MediaMetadata.EPISODE_NR));
    }
    catch (Exception e) {
      LOGGER.warn("error parsing season/episode number");
    }

    // parsed valid episode number/season number?
    if (seasonNr == -1 || episodeNr == -1) {
      return md;
    }

    String language = options.getLanguage().name();

    // get the data from tmdb
    TvEpisode episode = null;
    synchronized (api) {
      TmdbConnectionCounter.trackConnections();
      // get episode via season listing -> improves caching performance
      TvSeason fullSeason = api.tvSeasonsService().season(tmdbId, seasonNr, language, null);
      if (fullSeason != null) {
        for (TvEpisode ep : ListUtils.nullSafe(fullSeason.episodes)) {
          if (ep.season_number == seasonNr && ep.episode_number == episodeNr) {
            episode = ep;
            break;
          }
        }
      }
    }

    if (episode == null) {
      return md;
    }

    md.setId(TmdbMetadataProvider.providerInfo.getId(), episode.id);
    md.storeMetadata(MediaMetadata.TITLE, episode.name);
    md.storeMetadata(MediaMetadata.PLOT, episode.overview);
    md.storeMetadata(MediaMetadata.RATING, episode.vote_average);
    md.storeMetadata(MediaMetadata.VOTE_COUNT, episode.vote_count);

    if (episode.air_date != null) {
      md.storeMetadata(MediaMetadata.RELEASE_DATE, episode.air_date);
    }

    for (CastMember castMember : ListUtils.nullSafe(episode.guest_stars)) {
      MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.ACTOR);
      cm.setName(castMember.name);
      cm.setName(castMember.character);
      md.addCastMember(cm);
    }

    // Thumb
    if (options.getArtworkType() == MediaArtwork.MediaArtworkType.ALL || options.getArtworkType() == MediaArtwork.MediaArtworkType.THUMB) {
      MediaArtwork ma = new MediaArtwork();
      ma.setType(MediaArtwork.MediaArtworkType.THUMB);
      ma.setDefaultUrl(TmdbMetadataProvider.configuration.images.base_url + "original" + episode.still_path);
      md.addMediaArt(ma);
    }

    return md;
  }
}
