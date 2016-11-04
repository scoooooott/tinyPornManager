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
package org.tinymediamanager.scraper.trakt;

import static org.tinymediamanager.scraper.MediaMetadata.IMDB;
import static org.tinymediamanager.scraper.MediaMetadata.TMDB;
import static org.tinymediamanager.scraper.MediaMetadata.TVDB;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.UnsupportedMediaTypeException;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaEpisode;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.MetadataUtil;

import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.CastMember;
import com.uwetrottmann.trakt5.entities.Credits;
import com.uwetrottmann.trakt5.entities.CrewMember;
import com.uwetrottmann.trakt5.entities.Episode;
import com.uwetrottmann.trakt5.entities.SearchResult;
import com.uwetrottmann.trakt5.entities.Season;
import com.uwetrottmann.trakt5.entities.Show;
import com.uwetrottmann.trakt5.enums.Extended;
import com.uwetrottmann.trakt5.enums.Type;

import retrofit2.Response;

/**
 * The class TraktMovieMetadataProvider is used to provide metadata for movies from trakt.tv
 */

class TraktTVShowMetadataProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(TraktTVShowMetadataProvider.class);

  final TraktV2               api;

  public TraktTVShowMetadataProvider(TraktV2 api) {
    this.api = api;
  }

  // Search
  List<MediaSearchResult> search(MediaSearchOptions options) throws Exception {
    LOGGER.debug("search() " + options.toString());

    if (options.getMediaType() != MediaType.TV_SHOW) {
      throw new UnsupportedMediaTypeException(options.getMediaType());
    }

    String searchString = "";
    int year = 0;

    if (StringUtils.isEmpty(searchString) && StringUtils.isNotEmpty(options.getQuery())) {
      searchString = options.getQuery();
    }

    if (options.getYear() != 0) {
      try {
        year = options.getYear();
      }
      catch (Exception e) {
        year = 0;
      }
    }

    List<MediaSearchResult> results = new ArrayList<>();
    List<SearchResult> searchResults = null;

    try {
      Response<List<SearchResult>> response;
      if (year != 0) {
        response = api.search().textQuery(searchString, Type.SHOW, year, 1, 25).execute();
      }
      else {
        response = api.search().textQuery(searchString, Type.SHOW, null, 1, 25).execute();
      }
      searchResults = response.body();
    }
    catch (Exception e) {
      LOGGER.error("Problem scraping for " + searchString + "; " + e.getMessage());
    }

    if (searchResults == null || searchResults.isEmpty()) {
      LOGGER.info("nothing found");
      return results;
    }

    // set SearchResult Data for every Entry of the result
    for (SearchResult result : searchResults) {
      MediaSearchResult mediaSearchResult = new MediaSearchResult(TraktMetadataProvider.providerInfo.getId(), MediaType.TV_SHOW);

      mediaSearchResult.setTitle(result.show.title);
      mediaSearchResult.setYear(result.show.year);
      mediaSearchResult.setId((result.show.ids.trakt).toString());
      mediaSearchResult.setIMDBId(result.show.ids.imdb);
      mediaSearchResult.setProviderId((result.show.ids.trakt).toString());
      mediaSearchResult.setPosterUrl(result.show.images.poster.full);

      mediaSearchResult.setScore(MetadataUtil.calculateScore(searchString, mediaSearchResult.getTitle()));

      results.add(mediaSearchResult);
    }

    return results;
  }

  // Episode List
  List<MediaEpisode> getEpisodeList(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getEpisodeList() " + options.toString());
    List<MediaEpisode> episodes = new ArrayList<>();

    String id = options.getId(TraktMetadataProvider.providerInfo.getId());
    if (StringUtils.isBlank(id)) {
      // alternatively we can take the imdbid
      id = options.getId(IMDB);
    }
    if (StringUtils.isBlank(id)) {
      return episodes;
    }

    // the API does not provide a complete access to all episodes, so we have to
    // fetch the show summary first and every season afterwards..
    synchronized (api) {
      List<Season> seasons = api.seasons().summary(id, Extended.FULLEPISODES).execute().body();

      for (Season season : ListUtils.nullSafe(seasons)) {
        for (Episode episode : season.episodes) {
          MediaEpisode ep = new MediaEpisode(TraktMetadataProvider.providerInfo.getId());
          ep.episode = episode.number;
          ep.season = episode.season;
          ep.title = episode.title;
          ep.rating = episode.rating.floatValue();
          ep.voteCount = episode.votes;

          ep.ids.put(TraktMetadataProvider.providerInfo.getId(), episode.ids.trakt);
          if (episode.ids.tvdb > 0) {
            ep.ids.put(TVDB, episode.ids.tvdb);
          }
          if (episode.ids.tmdb > 0) {
            ep.ids.put(TMDB, episode.ids.tmdb);
          }
          if (StringUtils.isNotBlank(episode.ids.imdb)) {
            ep.ids.put(IMDB, episode.ids.imdb);
          }

          episodes.add(ep);
        }

      }
    }

    return episodes;
  }

  MediaMetadata scrape(MediaScrapeOptions options) throws Exception {
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
    MediaMetadata md = new MediaMetadata(TraktMetadataProvider.providerInfo.getId());

    String id = options.getId(TraktMetadataProvider.providerInfo.getId());
    if (StringUtils.isBlank(id)) {
      // alternatively we can take the imdbid
      id = options.getId(IMDB);
    }
    if (StringUtils.isBlank(id)) {
      return md;
    }

    Show show = null;
    Credits credits = null;
    synchronized (api) {
      show = api.shows().summary(id, Extended.FULL).execute().body();
      credits = api.shows().people(id).execute().body();
    }

    if (show == null) {
      return md;
    }

    // show meta data
    md.setId(TraktMetadataProvider.providerInfo.getId(), show.ids.trakt);
    if (show.ids.tvdb > 0) {
      md.setId(TVDB, show.ids.tvdb);
    }
    if (show.ids.tmdb > 0) {
      md.setId(TMDB, show.ids.tmdb);
    }
    if (StringUtils.isNotBlank(show.ids.imdb)) {
      md.setId(IMDB, show.ids.imdb);
    }

    md.setTitle(show.title);
    md.setPlot(show.overview);

    if (show.rating != null) {
      md.setRating(show.rating.floatValue());
    }
    md.setVoteCount(show.votes);

    if (show.first_aired != null) {
      md.setReleaseDate(show.first_aired.toDate());
    }
    md.setStatus(show.status.toString());
    md.setRuntime(show.runtime);
    md.addProductionCompany(show.network);

    // cast&crew
    if (credits != null) {
      for (CastMember cast : ListUtils.nullSafe(credits.cast)) {
        MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.ACTOR);
        cm.setName(cast.person.name);
        cm.setName(cast.character);
        md.addCastMember(cm);
      }
      if (credits.crew != null) {
        for (CrewMember crew : ListUtils.nullSafe(credits.crew.directing)) {
          MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.DIRECTOR);
          cm.setName(crew.person.name);
          cm.setName(crew.job);
          md.addCastMember(cm);
        }

        for (CrewMember crew : ListUtils.nullSafe(credits.crew.production)) {
          MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.PRODUCER);
          cm.setName(crew.person.name);
          cm.setName(crew.job);
          md.addCastMember(cm);
        }

        for (CrewMember crew : ListUtils.nullSafe(credits.crew.writing)) {
          MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.WRITER);
          cm.setName(crew.person.name);
          cm.setName(crew.job);
          md.addCastMember(cm);
        }

        for (CrewMember crew : ListUtils.nullSafe(credits.crew.costumeAndMakeUp)) {
          MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.OTHER);
          cm.setName(crew.person.name);
          cm.setName(crew.job);
          md.addCastMember(cm);
        }

        for (CrewMember crew : ListUtils.nullSafe(credits.crew.sound)) {
          MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.OTHER);
          cm.setName(crew.person.name);
          cm.setName(crew.job);
          md.addCastMember(cm);
        }

        for (CrewMember crew : ListUtils.nullSafe(credits.crew.camera)) {
          MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.OTHER);
          cm.setName(crew.person.name);
          cm.setName(crew.job);
          md.addCastMember(cm);
        }

        for (CrewMember crew : ListUtils.nullSafe(credits.crew.art)) {
          MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.OTHER);
          cm.setName(crew.person.name);
          cm.setName(crew.job);
          md.addCastMember(cm);
        }
      }
    }

    return md;
  }

  private MediaMetadata getEpisodeMetadata(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getEpisodeMetadata() " + options.toString());
    MediaMetadata md = new MediaMetadata(TraktMetadataProvider.providerInfo.getId());

    String id = options.getId(TraktMetadataProvider.providerInfo.getId());
    if (StringUtils.isBlank(id)) {
      // alternatively we can take the imdbid
      id = options.getId(IMDB);
    }
    if (StringUtils.isBlank(id)) {
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

    // fetch all episode data - this results in less connections, but the initial connection is _bigger_
    Episode episode = null;
    synchronized (api) {
      List<Season> seasons = api.seasons().summary(id, Extended.FULLEPISODES).execute().body();

      for (Season season : ListUtils.nullSafe(seasons)) {
        for (Episode ep : season.episodes) {
          if (ep.season == seasonNr && ep.number == episodeNr) {
            episode = ep;
            break;
          }
        }
        if (episode != null) {
          break;
        }
      }
    }

    if (episode == null) {
      return md;
    }

    md.setEpisodeNumber(episode.number);
    md.setSeasonNumber(episode.season);
    md.setId(TraktMetadataProvider.providerInfo.getId(), episode.ids.trakt);
    if (episode.ids.tvdb > 0) {
      md.setId(TVDB, episode.ids.tvdb);
    }
    if (episode.ids.tmdb > 0) {
      md.setId(TMDB, episode.ids.tmdb);
    }
    if (StringUtils.isNotBlank(episode.ids.imdb)) {
      md.setId(IMDB, episode.ids.imdb);
    }

    md.setTitle(episode.title);
    md.setPlot(episode.overview);

    if (episode.rating != null) {
      md.setRating(episode.rating.floatValue());
    }
    md.setVoteCount(episode.votes);

    if (episode.first_aired != null) {
      md.setReleaseDate(episode.first_aired.toDate());
    }

    return md;
  }
}
