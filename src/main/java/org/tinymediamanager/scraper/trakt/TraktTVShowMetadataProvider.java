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
package org.tinymediamanager.scraper.trakt;

import static org.tinymediamanager.scraper.MediaMetadata.IMDB;
import static org.tinymediamanager.scraper.MediaMetadata.TMDB;
import static org.tinymediamanager.scraper.MediaMetadata.TVDB;

import java.text.Format;
import java.text.SimpleDateFormat;
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
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.entities.MediaRating;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.MetadataUtil;
import org.tinymediamanager.scraper.util.TvUtils;

import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.CastMember;
import com.uwetrottmann.trakt5.entities.Credits;
import com.uwetrottmann.trakt5.entities.CrewMember;
import com.uwetrottmann.trakt5.entities.Episode;
import com.uwetrottmann.trakt5.entities.SearchResult;
import com.uwetrottmann.trakt5.entities.Season;
import com.uwetrottmann.trakt5.entities.Show;
import com.uwetrottmann.trakt5.entities.Translation;
import com.uwetrottmann.trakt5.enums.Extended;

/**
 * The class TraktTvShowMetadataProvider is used to provide metadata for movies from trakt.tv
 */

class TraktTVShowMetadataProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(TraktTVShowMetadataProvider.class);

  private final TraktV2       api;

  TraktTVShowMetadataProvider(TraktV2 api) {
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
    String lang = options.getLanguage().getLanguage();
    lang = lang + ",en"; // fallback search

    synchronized (api) {
      try {
        if (year != 0) {
          searchResults = api.search()
              .textQueryShow(searchString, String.valueOf(year), null, lang, null, null, null, null, null, null, Extended.FULL, 1, 25).execute()
              .body();
        }
        else {
          searchResults = api.search().textQueryShow(searchString, null, null, lang, null, null, null, null, null, null, Extended.FULL, 1, 25)
              .execute().body();
        }
      }
      catch (Exception e) {
        LOGGER.debug("failed to search: " + e.getMessage());
      }
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
      mediaSearchResult.setId(result.show.ids.trakt.toString());
      mediaSearchResult.setIMDBId(result.show.ids.imdb);

      mediaSearchResult.setScore(MetadataUtil.calculateScore(searchString, mediaSearchResult.getTitle()));

      results.add(mediaSearchResult);
    }

    return results;
  }

  // Episode List
  List<MediaMetadata> getEpisodeList(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getEpisodeList() " + options.toString());
    List<MediaMetadata> episodes = new ArrayList<>();

    String id = options.getIdAsString(TraktMetadataProvider.providerInfo.getId());
    if (StringUtils.isBlank(id)) {
      // alternatively we can take the imdbid
      id = options.getIdAsString(IMDB);
    }
    if (StringUtils.isBlank(id)) {
      return episodes;
    }

    // the API does not provide a complete access to all episodes, so we have to
    // fetch the show summary first and every season afterwards..
    List<Season> seasons = null;
    synchronized (api) {
      try {
        seasons = api.seasons().summary(id, Extended.FULLEPISODES).execute().body();
      }
      catch (Exception e) {
        LOGGER.debug("failed to get episode list: " + e.getMessage());
      }
    }

    for (Season season : ListUtils.nullSafe(seasons)) {
      for (Episode episode : season.episodes) {
        MediaMetadata ep = new MediaMetadata(TraktMetadataProvider.providerInfo.getId());
        ep.setEpisodeNumber(TvUtils.getEpisodeNumber(episode.number));
        ep.setSeasonNumber(TvUtils.getSeasonNumber(episode.season));
        ep.setTitle(episode.title);

        if (episode.rating != null) {
          MediaRating rating = new MediaRating(TraktMetadataProvider.providerInfo.getId());
          rating.setRating(episode.rating);
          rating.setVoteCount(episode.votes);
          rating.setMaxValue(10);
          ep.addRating(rating);
        }

        if (episode.first_aired != null) {
          ep.setReleaseDate(episode.first_aired);
        }

        if (episode.ids != null) {
          ep.setId(TraktMetadataProvider.providerInfo.getId(), episode.ids.trakt);
          if (episode.ids.tvdb != null && episode.ids.tvdb > 0) {
            ep.setId(TVDB, episode.ids.tvdb);
          }
          if (episode.ids.tmdb != null && episode.ids.tmdb > 0) {
            ep.setId(TMDB, episode.ids.tmdb);
          }
          if (episode.ids.tvrage != null && episode.ids.tvrage > 0) {
            ep.setId("tvrage", episode.ids.tvrage);
          }
          if (StringUtils.isNotBlank(episode.ids.imdb)) {
            ep.setId(IMDB, episode.ids.imdb);
          }
        }

        episodes.add(ep);
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

    String id = options.getIdAsString(TraktMetadataProvider.providerInfo.getId());
    if (StringUtils.isBlank(id) && options.getResult() != null) {
      // take the id from the search result
      id = options.getResult().getId();
    }
    if (StringUtils.isBlank(id)) {
      // alternatively we can take the imdbid
      id = options.getIdAsString(IMDB);
    }
    if (StringUtils.isBlank(id)) {
      return md;
    }

    String lang = options.getLanguage().getLanguage();
    List<Translation> translations = null;
    Show show = null;
    Credits credits = null;
    synchronized (api) {
      try {
        show = api.shows().summary(id, Extended.FULL).execute().body();
        if (!"en".equals(lang)) {
          // only call translation when we're not already EN ;)
          translations = api.shows().translation(id, lang).execute().body();
        }
        credits = api.shows().people(id).execute().body();
      }
      catch (Exception e) {
        LOGGER.debug("failed to get meta data: " + e.getMessage());
      }
    }

    if (show == null) {
      return md;
    }

    // show meta data
    md.setId(TraktMetadataProvider.providerInfo.getId(), show.ids.trakt);
    if (show.ids.tvdb != null && show.ids.tvdb > 0) {
      md.setId(TVDB, show.ids.tvdb);
    }
    if (show.ids.tmdb != null && show.ids.tmdb > 0) {
      md.setId(TMDB, show.ids.tmdb);
    }
    if (show.ids.tvrage != null && show.ids.tvrage > 0) {
      md.setId("tvrage", show.ids.tvrage);
    }
    if (StringUtils.isNotBlank(show.ids.imdb)) {
      md.setId(IMDB, show.ids.imdb);
    }

    // if foreign language, get new values and overwrite
    Translation trans = translations == null ? null : translations.get(0);
    if (trans != null) {
      md.setTitle(trans.title.isEmpty() ? show.title : trans.title);
      md.setPlot(trans.overview.isEmpty() ? show.overview : trans.overview);
    }
    else {
      md.setTitle(show.title);
      md.setPlot(show.overview);
    }

    md.setYear(show.year);

    MediaRating rating = new MediaRating("trakt");
    rating.setRating(show.rating);
    rating.setVoteCount(show.votes);
    rating.setMaxValue(10);
    md.addRating(rating);

    md.addCertification(Certification.findCertification(show.certification));
    md.addCountry(show.country);
    md.setReleaseDate(show.first_aired);
    if (show.status != null) {
      md.setStatus(show.status.toString());
    }
    md.setRuntime(show.runtime);
    md.addProductionCompany(show.network);

    for (String genreAsString : ListUtils.nullSafe(show.genres)) {
      md.addGenre(MediaGenres.getGenre(genreAsString));
    }

    // cast&crew
    if (credits != null) {
      for (CastMember cast : ListUtils.nullSafe(credits.cast)) {
        MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.ACTOR);
        cm.setName(cast.person.name);
        cm.setCharacter(cast.character);
        md.addCastMember(cm);
      }
      if (credits.crew != null) {
        for (CrewMember crew : ListUtils.nullSafe(credits.crew.directing)) {
          MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.DIRECTOR);
          cm.setName(crew.person.name);
          cm.setPart(crew.job);
          md.addCastMember(cm);
        }

        for (CrewMember crew : ListUtils.nullSafe(credits.crew.production)) {
          MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.PRODUCER);
          cm.setName(crew.person.name);
          cm.setPart(crew.job);
          md.addCastMember(cm);
        }

        for (CrewMember crew : ListUtils.nullSafe(credits.crew.writing)) {
          MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.WRITER);
          cm.setName(crew.person.name);
          cm.setPart(crew.job);
          md.addCastMember(cm);
        }

        for (CrewMember crew : ListUtils.nullSafe(credits.crew.costumeAndMakeUp)) {
          MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.OTHER);
          cm.setName(crew.person.name);
          cm.setPart(crew.job);
          md.addCastMember(cm);
        }

        for (CrewMember crew : ListUtils.nullSafe(credits.crew.sound)) {
          MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.OTHER);
          cm.setName(crew.person.name);
          cm.setPart(crew.job);
          md.addCastMember(cm);
        }

        for (CrewMember crew : ListUtils.nullSafe(credits.crew.camera)) {
          MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.OTHER);
          cm.setName(crew.person.name);
          cm.setPart(crew.job);
          md.addCastMember(cm);
        }

        for (CrewMember crew : ListUtils.nullSafe(credits.crew.art)) {
          MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.OTHER);
          cm.setName(crew.person.name);
          cm.setPart(crew.job);
          md.addCastMember(cm);
        }
      }
    }

    return md;
  }

  private MediaMetadata getEpisodeMetadata(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getEpisodeMetadata() " + options.toString());
    MediaMetadata md = new MediaMetadata(TraktMetadataProvider.providerInfo.getId());

    String id = options.getIdAsString(TraktMetadataProvider.providerInfo.getId());
    if (StringUtils.isBlank(id)) {
      // alternatively we can take the imdbid
      id = options.getIdAsString(IMDB);
    }
    if (StringUtils.isBlank(id)) {
      return md;
    }

    // get episode number and season number
    int seasonNr = options.getIdAsIntOrDefault(MediaMetadata.SEASON_NR, -1);
    int episodeNr = options.getIdAsIntOrDefault(MediaMetadata.EPISODE_NR, -1);

    // parsed valid episode number/season number?
    String aired = "";
    if (options.getMetadata() != null && options.getMetadata().getReleaseDate() != null) {
      Format formatter = new SimpleDateFormat("yyyy-MM-dd");
      aired = formatter.format(options.getMetadata().getReleaseDate());
    }
    if (aired.isEmpty() && (seasonNr == -1 || episodeNr == -1)) {
      return md; // not even date set? return
    }

    // fetch all episode data - this results in less connections, but the initial connection is _bigger_
    Episode episode = null;
    List<Season> seasons = null;
    synchronized (api) {
      try {
        seasons = api.seasons().summary(id, Extended.FULLEPISODES).execute().body();
      }
      catch (Exception e) {
        LOGGER.debug("failed to get meta data: " + e.getMessage());
      }
    }

    for (Season season : ListUtils.nullSafe(seasons)) {
      for (Episode ep : season.episodes) {
        if (ep.season == seasonNr && ep.number == episodeNr) {
          episode = ep;
          break;
        }
      }
    }

    // not found? try to match by date
    if (episode == null && !aired.isEmpty()) {
      for (Season season : ListUtils.nullSafe(seasons)) {
        for (Episode ep : season.episodes) {
          if (ep.first_aired != null) {
            Format formatter = new SimpleDateFormat("yyyy-MM-dd");
            String epAired = formatter.format(ep.first_aired.toDate());
            if (epAired.equals(aired)) {
              episode = ep;
              break;
            }
          }
        }
      }
    }

    if (episode == null) {
      return md;
    }

    md.setEpisodeNumber(TvUtils.getEpisodeNumber(episode.number));
    md.setAbsoluteNumber(TvUtils.getEpisodeNumber(episode.number_abs));
    md.setSeasonNumber(TvUtils.getSeasonNumber(episode.season));
    md.setId(TraktMetadataProvider.providerInfo.getId(), episode.ids.trakt);
    if (episode.ids.tvdb != null && episode.ids.tvdb > 0) {
      md.setId(TVDB, episode.ids.tvdb);
    }
    if (episode.ids.tmdb != null && episode.ids.tmdb > 0) {
      md.setId(TMDB, episode.ids.tmdb);
    }
    if (StringUtils.isNotBlank(episode.ids.imdb)) {
      md.setId(IMDB, episode.ids.imdb);
    }
    if (episode.ids.tvrage != null && episode.ids.tvrage > 0) {
      md.setId("tvrage", episode.ids.tvrage);
    }

    md.setTitle(episode.title);
    md.setPlot(episode.overview);

    MediaRating rating = new MediaRating("trakt");
    rating.setRating(episode.rating);
    rating.setVoteCount(episode.votes);
    rating.setMaxValue(10);
    md.addRating(rating);

    md.setReleaseDate(episode.first_aired);

    return md;
  }
}
