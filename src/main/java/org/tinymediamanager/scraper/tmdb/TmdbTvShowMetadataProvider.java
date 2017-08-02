/*
 * Copyright 2012 - 2016 Manuel Laggner
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

import static org.tinymediamanager.scraper.MediaMetadata.IMDB;
import static org.tinymediamanager.scraper.MediaMetadata.TVDB;
import static org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider.providerInfo;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaEpisode;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.MetadataUtil;
import org.tinymediamanager.scraper.util.TvUtils;

import com.uwetrottmann.tmdb2.Tmdb;
import com.uwetrottmann.tmdb2.entities.AppendToResponse;
import com.uwetrottmann.tmdb2.entities.BaseCompany;
import com.uwetrottmann.tmdb2.entities.BaseTvShow;
import com.uwetrottmann.tmdb2.entities.CastMember;
import com.uwetrottmann.tmdb2.entities.ContentRating;
import com.uwetrottmann.tmdb2.entities.TvEpisode;
import com.uwetrottmann.tmdb2.entities.TvSeason;
import com.uwetrottmann.tmdb2.entities.TvShow;
import com.uwetrottmann.tmdb2.entities.TvShowResultsPage;
import com.uwetrottmann.tmdb2.enumerations.AppendToResponseItem;

class TmdbTvShowMetadataProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(TmdbTvShowMetadataProvider.class);

  private final Tmdb          api;

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

    List<MediaSearchResult> resultList = new ArrayList<>();

    String searchString = "";

    // check type
    if (query.getMediaType() != MediaType.TV_SHOW) {
      throw new Exception("wrong media type for this scraper");
    }

    if (StringUtils.isEmpty(searchString) && StringUtils.isNotEmpty(query.getQuery())) {
      searchString = query.getQuery();
    }

    if (StringUtils.isEmpty(searchString)) {
      LOGGER.debug("TMDB Scraper: empty searchString");
      return resultList;
    }

    searchString = MetadataUtil.removeNonSearchCharacters(searchString);
    String language = query.getLanguage().getLanguage();
    if (StringUtils.isNotBlank(query.getLanguage().getCountry())) {
      language += "-" + query.getLanguage().getCountry();
    }

    // begin search
    LOGGER.info("========= BEGIN TMDB Scraper Search for: " + searchString);
    synchronized (api) {
      TmdbConnectionCounter.trackConnections();
      try {
        TvShowResultsPage resultsPage = api.searchService().tv(searchString, 1, language, null, "phrase").execute().body();
        if (resultsPage != null) {
          for (BaseTvShow show : ListUtils.nullSafe(resultsPage.results)) {
            // tmdb provides title = originalTitle if no title in the requested language has been found,
            // so get the title in a alternative language
            if (show.name.equals(show.original_name) && !show.original_language.equals(query.getLanguage().getLanguage())) {
              String languageFallback = MediaLanguages.get(providerInfo.getConfig().getValue("titleFallbackLanguage")).name().replace("_", "-");

              TmdbConnectionCounter.trackConnections();
              TvShow showFallback = api.tvService().tv(show.id, languageFallback).execute().body();

              if (showFallback != null && !StringUtils.isNotBlank(showFallback.name)) {
                show.name = showFallback.name;
              }
            }

            MediaSearchResult result = new MediaSearchResult(TmdbMetadataProvider.providerInfo.getId(), MediaType.TV_SHOW);
            result.setId(Integer.toString(show.id));
            result.setTitle(show.name);
            result.setOriginalTitle(show.original_name);
            result.setPosterUrl(TmdbMetadataProvider.configuration.images.base_url + "w342" + show.poster_path);

            // parse release date to year
            if (show.first_air_date != null) {
              Calendar calendar = Calendar.getInstance();
              calendar.setTime(show.first_air_date);
              result.setYear(calendar.get(Calendar.YEAR));
            }

            // calculate score
            result.setScore(MetadataUtil.calculateScore(searchString, result.getTitle()));

            resultList.add(result);
          }
        }
      }
      catch (Exception e) {
        LOGGER.debug("failed to search: " + e.getMessage());
      }
    }

    LOGGER.info("found " + resultList.size() + " results");
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
    List<MediaEpisode> episodes = new ArrayList<>();

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

    String language = options.getLanguage().getLanguage();
    if (StringUtils.isNotBlank(options.getLanguage().getCountry())) {
      language += "-" + options.getLanguage().getCountry();
    }

    // the API does not provide a complete access to all episodes, so we have to
    // fetch the show summary first and every season afterwards..
    synchronized (api) {
      TmdbConnectionCounter.trackConnections();
      try {
        TvShow complete = api.tvService().tv(tmdbId, language, null).execute().body();
        if (complete != null) {
          for (TvSeason season : ListUtils.nullSafe(complete.seasons)) {
            List<MediaEpisode> seasonEpisodes = new ArrayList<>();
            boolean emptyTextFound = false;

            TmdbConnectionCounter.trackConnections();
            TvSeason fullSeason = api.tvSeasonsService().season(tmdbId, season.season_number, language, null).execute().body();
            if (fullSeason != null) {
              for (TvEpisode episode : ListUtils.nullSafe(fullSeason.episodes)) {
                MediaEpisode ep = new MediaEpisode(TmdbMetadataProvider.providerInfo.getId());
                ep.episode = episode.episode_number;
                ep.season = episode.season_number;
                ep.title = episode.name;
                ep.plot = episode.overview;

                if (StringUtils.isBlank(episode.name) || StringUtils.isBlank(episode.overview)) {
                  emptyTextFound = true;
                }

                if (episode.vote_average != null) {
                  ep.rating = episode.vote_average.floatValue();
                }
                if (episode.air_date != null) {
                  Format formatter = new SimpleDateFormat("yyyy-MM-dd");
                  ep.firstAired = formatter.format(episode.air_date);
                }
                ep.voteCount = episode.vote_count;
                ep.ids.put(TmdbMetadataProvider.providerInfo.getId(), episode.id);
                seasonEpisodes.add(ep);
              }
            }

            // if there is at least one empty text found, rescrape the season with the fallback language
            if (emptyTextFound) {
              String languageFallback = MediaLanguages.get(providerInfo.getConfig().getValue("titleFallbackLanguage")).name().replace("_", "-");

              TmdbConnectionCounter.trackConnections();
              TvSeason fullSeasonFallback = api.tvSeasonsService().season(tmdbId, season.season_number, languageFallback, null).execute().body();
              if (fullSeasonFallback != null) {
                for (MediaEpisode ep : seasonEpisodes) {
                  if (StringUtils.isNotBlank(ep.title) && StringUtils.isNotBlank(ep.plot)) {
                    continue;
                  }

                  // search for the episode in the fallback language
                  for (TvEpisode episode : ListUtils.nullSafe(fullSeasonFallback.episodes)) {
                    if (ep.season == episode.season_number && ep.episode == episode.episode_number) {
                      if (StringUtils.isBlank(ep.title)) {
                        ep.title = episode.name;
                      }
                      if (StringUtils.isBlank(ep.plot)) {
                        ep.plot = episode.overview;
                      }
                      break;
                    }
                  }
                }
              }
            }

            episodes.addAll(seasonEpisodes);
          }
        }
      }
      catch (Exception e) {
        LOGGER.debug("failed to get episode list: " + e.getMessage());
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

    String language = options.getLanguage().getLanguage();
    if (StringUtils.isNotBlank(options.getLanguage().getCountry())) {
      language += "-" + options.getLanguage().getCountry();
    }
    // get the data from tmdb
    TvShow complete = null;
    synchronized (api) {
      TmdbConnectionCounter.trackConnections();
      try {
        complete = api.tvService()
            .tv(tmdbId, language,
                new AppendToResponse(AppendToResponseItem.CREDITS, AppendToResponseItem.EXTERNAL_IDS, AppendToResponseItem.CONTENT_RATINGS))
            .execute().body();
      }
      catch (Exception e) {
        LOGGER.debug("failed to get meta data: " + e.getMessage());
      }
    }

    if (complete == null) {
      return md;
    }

    md.setId(TmdbMetadataProvider.providerInfo.getId(), tmdbId);
    md.setTitle(complete.name);
    md.setOriginalTitle(complete.original_name);
    md.setRating(complete.vote_average);
    md.setVoteCount(complete.vote_count);
    md.setReleaseDate(complete.first_air_date);
    md.setPlot(complete.overview);

    // Poster
    if (StringUtils.isNotBlank(complete.poster_path)) {
      MediaArtwork ma = new MediaArtwork(TmdbMetadataProvider.providerInfo.getId(), MediaArtwork.MediaArtworkType.POSTER);
      ma.setPreviewUrl(TmdbMetadataProvider.configuration.images.base_url + "w185" + complete.poster_path);
      ma.setDefaultUrl(TmdbMetadataProvider.configuration.images.base_url + "w342" + complete.poster_path);
      ma.setLanguage(options.getLanguage().getLanguage());
      ma.setTmdbId(complete.id);
      md.addMediaArt(ma);
    }

    for (BaseCompany company : ListUtils.nullSafe(complete.production_companies)) {
      md.addProductionCompany(company.name.trim());
    }
    md.setStatus(complete.status);

    if (complete.first_air_date != null) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(complete.first_air_date);
      md.setYear(calendar.get(Calendar.YEAR));
    }

    if (complete.credits != null) {
      for (CastMember castMember : ListUtils.nullSafe(complete.credits.cast)) {
        MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.ACTOR);
        cm.setName(castMember.name);
        cm.setName(castMember.character);
        md.addCastMember(cm);
      }
    }

    // external IDs
    if (complete.external_ids != null) {
      if (complete.external_ids.tvdb_id != null && complete.external_ids.tvdb_id > 0) {
        md.setId(TVDB, complete.external_ids.tvdb_id);
      }
      if (StringUtils.isNotBlank(complete.external_ids.imdb_id)) {
        md.setId(IMDB, complete.external_ids.imdb_id);
      }
      if (complete.external_ids.tvrage_id != null && complete.external_ids.tvrage_id > 0) {
        md.setId("tvrage", complete.external_ids.tvrage_id);
      }
    }

    // content ratings
    if (complete.content_ratings != null) {
      for (ContentRating country : ListUtils.nullSafe(complete.content_ratings.results)) {
        if (options.getCountry() == null || options.getCountry().getAlpha2().compareToIgnoreCase(country.iso_3166_1) == 0) {
          // do not use any empty certifications
          if (StringUtils.isEmpty(country.rating)) {
            continue;
          }

          md.addCertification(Certification.getCertification(country.iso_3166_1, country.rating));

        }
      }
    }

    // check if we need to rescrape in the fallback language
    if (complete.name.equals(complete.original_name) && !complete.original_language.equals(options.getLanguage().getLanguage())
        || StringUtils.isBlank(complete.overview)) {
      // title in original language or plot was empty - scrape in fallback language
      Locale oldLang = options.getLanguage();
      try {
        String lang = MediaLanguages.get(providerInfo.getConfig().getValue("titleFallbackLanguage")).name();
        options.setLanguage(new Locale(lang));
        MediaMetadata fallbackMd = getMetadata(options);

        if (StringUtils.isBlank(complete.overview) && !StringUtils.isBlank(fallbackMd.getPlot())) {
          md.setPlot(fallbackMd.getPlot());
        }
        if (complete.name.equals(complete.original_name) && !complete.original_language.equals(options.getLanguage().getLanguage())
            && !StringUtils.isBlank(fallbackMd.getTitle())) {
          md.setTitle(fallbackMd.getTitle());
        }
        if (StringUtils.isBlank(complete.original_name) && !StringUtils.isBlank(fallbackMd.getOriginalTitle())) {
          md.setOriginalTitle(fallbackMd.getOriginalTitle());
        }
      }
      catch (Exception ignored) {
      }
      finally {
        options.setLanguage(oldLang);
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
    String aired = "";
    if (options.getMetadata() != null && options.getMetadata().getReleaseDate() != null) {
      Format formatter = new SimpleDateFormat("yyyy-MM-dd");
      aired = formatter.format(options.getMetadata().getReleaseDate());
    }
    // does not work - we cannot scrape w/o season
    // if (aired.isEmpty() && (seasonNr == -1 || episodeNr == -1)) {
    if (seasonNr == -1 || episodeNr == -1) {
      return md; // not even date set? return
    }

    String language = options.getLanguage().getLanguage();
    if (StringUtils.isNotBlank(options.getLanguage().getCountry())) {
      language += "-" + options.getLanguage().getCountry();
    }
    // get the data from tmdb
    TvEpisode episode = null;
    synchronized (api) {
      // get episode via season listing -> improves caching performance
      TmdbConnectionCounter.trackConnections();
      try {
        TvSeason fullSeason = api.tvSeasonsService().season(tmdbId, seasonNr, language, null).execute().body();
        if (fullSeason != null) {
          for (TvEpisode ep : ListUtils.nullSafe(fullSeason.episodes)) {
            if (ep.season_number == seasonNr && ep.episode_number == episodeNr) {
              episode = ep;
              break;
            }
          }

          // not found? try to match by date
          if (episode == null && !aired.isEmpty()) {
            for (TvEpisode ep : ListUtils.nullSafe(fullSeason.episodes)) {
              if (ep.air_date != null) {
                Format formatter = new SimpleDateFormat("yyyy-MM-dd");
                String epAired = formatter.format(ep.air_date);
                if (epAired.equals(aired)) {
                  episode = ep;
                  break;
                }
              }
            }
          }
        }

        // if the episode has been found, but the texts are empty, scrape in the fallback language
        if (episode != null && StringUtils.isAnyBlank(episode.name, episode.overview)) {
          String languageFallback = MediaLanguages.get(providerInfo.getConfig().getValue("titleFallbackLanguage")).name().replace("_", "-");

          TmdbConnectionCounter.trackConnections();
          TvSeason fullSeasonFallback = api.tvSeasonsService().season(tmdbId, episode.season_number, languageFallback, null).execute().body();
          if (fullSeasonFallback != null) {
            for (TvEpisode ep : ListUtils.nullSafe(fullSeasonFallback.episodes)) {
              if (ep.season_number == seasonNr && ep.episode_number == episodeNr) {
                if (StringUtils.isBlank(episode.name)) {
                  episode.name = ep.name;
                }
                if (StringUtils.isBlank(episode.overview)) {
                  episode.overview = ep.overview;
                }
                break;
              }
            }
          }
        }
      }
      catch (Exception e) {
        LOGGER.debug("failed to get meta data: " + e.getMessage());
      }
    }

    if (episode == null) {
      return md;
    }

    md.setEpisodeNumber(TvUtils.getEpisodeNumber(episode.episode_number));
    md.setSeasonNumber(TvUtils.getSeasonNumber(episode.season_number));
    md.setId(TmdbMetadataProvider.providerInfo.getId(), episode.id);

    // external IDs
    if (episode.external_ids != null) {
      if (episode.external_ids.tvdb_id != null && episode.external_ids.tvdb_id > 0) {
        md.setId(TVDB, episode.external_ids.tvdb_id);
      }
      if (StringUtils.isNotBlank(episode.external_ids.imdb_id)) {
        md.setId(IMDB, episode.external_ids.imdb_id);
      }
      if (episode.external_ids.tvrage_id != null && episode.external_ids.tvrage_id > 0) {
        md.setId("tvrage", episode.external_ids.tvrage_id);
      }
    }

    md.setTitle(episode.name);
    md.setPlot(episode.overview);
    md.setRating(episode.vote_average);
    md.setVoteCount(episode.vote_count);
    md.setReleaseDate(episode.air_date);

    for (CastMember castMember : ListUtils.nullSafe(episode.guest_stars)) {
      MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.ACTOR);
      cm.setName(castMember.name);
      cm.setName(castMember.character);
      md.addCastMember(cm);
    }

    // Thumb
    if (StringUtils.isNotBlank(episode.still_path)
        && (options.getArtworkType() == MediaArtwork.MediaArtworkType.ALL || options.getArtworkType() == MediaArtwork.MediaArtworkType.THUMB)) {
      MediaArtwork ma = new MediaArtwork(TmdbMetadataProvider.providerInfo.getId(), MediaArtworkType.THUMB);
      ma.setPreviewUrl(TmdbMetadataProvider.configuration.images.base_url + "original" + episode.still_path);
      ma.setDefaultUrl(TmdbMetadataProvider.configuration.images.base_url + "original" + episode.still_path);
      md.addMediaArt(ma);
    }

    return md;
  }
}
