/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.ui.tvshows;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowActor;
import org.tinymediamanager.core.tvshow.TvShowEpisode;
import org.tinymediamanager.core.tvshow.TvShowSeason;

/**
 * The class TvShowExtendedMatcher. For search&filter TV shows
 * 
 * @author Manuel Laggner
 */
class TvShowExtendedMatcher {

  public enum SearchOptions {
    TEXT, WATCHED, GENRE, CAST, TAG, VIDEO_FORMAT, VIDEO_CODEC, AUDIO_CODEC, DATASOURCE, MISSING_METADATA, MISSING_ARTWORK, MISSING_SUBTITLES
  }

  Map<SearchOptions, Object> searchOptions = Collections.synchronizedMap(new HashMap<SearchOptions, Object>());

  public boolean matches(final Object bean) {
    // do nothing if there's nothing to filter
    if (searchOptions.isEmpty()) {
      return true;
    }

    // if the node is a TvShowNode, we have to check the TvShow and all episodes within it
    if (bean instanceof TvShow) {
      TvShow show = (TvShow) bean;
      return tvShowFilterMatch(show);
    }

    // if the node is a TvShowSeasonNode, we have to check the parent TV show and its episodes
    if (bean instanceof TvShowSeason) {
      TvShowSeason season = (TvShowSeason) bean;
      return tvShowSeasonFilterMatch(season);
    }

    // if the node is a TvShowEpisodeNode, we have to check the parent TV show and the episode
    if (bean instanceof TvShowEpisode) {
      TvShowEpisode episode = (TvShowEpisode) bean;
      return tvShowEpisodeFilterMatch(episode);
    }

    // fallback
    return true;
  }

  private boolean tvShowFilterMatch(TvShow tvShow) {
    if (searchOptions.containsKey(SearchOptions.TEXT)) {
      if (!filterText(tvShow, (String) searchOptions.get(SearchOptions.TEXT))) {
        return false;
      }
    }

    if (searchOptions.containsKey(SearchOptions.DATASOURCE)) {
      if (!filterDatasource(tvShow, (String) searchOptions.get(SearchOptions.DATASOURCE))) {
        return false;
      }
    }

    if (searchOptions.containsKey(SearchOptions.CAST)) {
      if (!filterCrew(tvShow, (String) searchOptions.get(SearchOptions.CAST))) {
        return false;
      }
    }

    if (searchOptions.containsKey(SearchOptions.MISSING_METADATA)) {
      if (!filterMissingMetadata(tvShow)) {
        return false;
      }
    }

    if (searchOptions.containsKey(SearchOptions.MISSING_ARTWORK)) {
      if (!filterMissingArtwork(tvShow)) {
        return false;
      }
    }

    if (searchOptions.containsKey(SearchOptions.MISSING_SUBTITLES)) {
      if (!filterMissingSubtitles(tvShow)) {
        return false;
      }
    }

    // fallback
    return true;
  }

  private boolean tvShowSeasonFilterMatch(TvShowSeason season) {
    if (searchOptions.containsKey(SearchOptions.TEXT)) {
      if (!filterText(season, (String) searchOptions.get(SearchOptions.TEXT))) {
        return false;
      }
    }

    if (searchOptions.containsKey(SearchOptions.CAST)) {
      if (!filterCrew(season, (String) searchOptions.get(SearchOptions.CAST))) {
        return false;
      }
    }

    if (searchOptions.containsKey(SearchOptions.MISSING_METADATA)) {
      if (!filterMissingMetadata(season)) {
        return false;
      }
    }

    if (searchOptions.containsKey(SearchOptions.MISSING_ARTWORK)) {
      if (!filterMissingArtwork(season)) {
        return false;
      }
    }

    if (searchOptions.containsKey(SearchOptions.MISSING_SUBTITLES)) {
      if (!filterMissingSubtitles(season)) {
        return false;
      }
    }

    // fallback
    return true;
  }

  private boolean tvShowEpisodeFilterMatch(TvShowEpisode episode) {
    if (searchOptions.containsKey(SearchOptions.TEXT)) {
      if (!filterText(episode, (String) searchOptions.get(SearchOptions.TEXT))) {
        return false;
      }
    }

    if (searchOptions.containsKey(SearchOptions.CAST)) {
      if (!filterCrew(episode, (String) searchOptions.get(SearchOptions.CAST))) {
        return false;
      }
    }

    if (searchOptions.containsKey(SearchOptions.MISSING_METADATA)) {
      if (!filterMissingMetadata(episode)) {
        return false;
      }
    }

    if (searchOptions.containsKey(SearchOptions.MISSING_ARTWORK)) {
      if (!filterMissingArtwork(episode)) {
        return false;
      }
    }

    if (searchOptions.containsKey(SearchOptions.MISSING_SUBTITLES)) {
      if (!filterMissingSubtitles(episode)) {
        return false;
      }
    }

    // fallback
    return true;
  }

  private boolean filterText(TvShow tvShow, String filterText) {
    return matchesText(tvShow, new ArrayList<TvShowEpisode>(tvShow.getEpisodes()), filterText);
  }

  private boolean filterText(TvShowSeason season, String filterText) {
    return matchesText(season.getTvShow(), new ArrayList<TvShowEpisode>(season.getEpisodes()), filterText);
  }

  private boolean filterText(TvShowEpisode episode, String filterText) {
    return matchesText(episode.getTvShow(), Arrays.asList(episode), filterText);
  }

  private boolean filterDatasource(TvShow tvShow, String datasource) {
    if (new File(tvShow.getDataSource()).equals(new File(datasource))) {
      return true;
    }
    return false;
  }

  private boolean filterCrew(TvShow tvShow, String filterText) {
    return matchesCrew(tvShow, new ArrayList<TvShowEpisode>(tvShow.getEpisodes()), filterText);
  }

  private boolean filterCrew(TvShowSeason season, String filterText) {
    return matchesCrew(season.getTvShow(), new ArrayList<TvShowEpisode>(season.getEpisodes()), filterText);
  }

  private boolean filterCrew(TvShowEpisode episode, String filterText) {
    return matchesCrew(episode.getTvShow(), Arrays.asList(episode), filterText);
  }

  private boolean filterMissingMetadata(TvShow tvShow) {
    return matchesMissingMetadata(tvShow, new ArrayList<TvShowEpisode>(tvShow.getEpisodes()));
  }

  private boolean filterMissingMetadata(TvShowSeason season) {
    return matchesMissingMetadata(season.getTvShow(), new ArrayList<TvShowEpisode>(season.getEpisodes()));
  }

  private boolean filterMissingMetadata(TvShowEpisode episode) {
    return matchesMissingMetadata(episode.getTvShow(), Arrays.asList(episode));
  }

  private boolean filterMissingArtwork(TvShow tvShow) {
    return matchesMissingArtwork(tvShow, new ArrayList<TvShowEpisode>(tvShow.getEpisodes()));
  }

  private boolean filterMissingArtwork(TvShowSeason season) {
    return matchesMissingArtwork(season.getTvShow(), new ArrayList<TvShowEpisode>(season.getEpisodes()));
  }

  private boolean filterMissingArtwork(TvShowEpisode episode) {
    return matchesMissingArtwork(episode.getTvShow(), Arrays.asList(episode));
  }

  private boolean filterMissingSubtitles(TvShow tvShow) {
    return matchesMissingSubtitles(new ArrayList<TvShowEpisode>(tvShow.getEpisodes()));
  }

  private boolean filterMissingSubtitles(TvShowSeason season) {
    return matchesMissingSubtitles(new ArrayList<TvShowEpisode>(season.getEpisodes()));
  }

  private boolean filterMissingSubtitles(TvShowEpisode episode) {
    return matchesMissingSubtitles(Arrays.asList(episode));
  }

  private boolean matchesText(TvShow tvShow, List<TvShowEpisode> episodes, String filterText) {
    if (StringUtils.isBlank(filterText)) {
      return true;
    }

    Pattern pattern = Pattern.compile("(?i)" + Pattern.quote(filterText));

    // first: filter on the TV show title
    Matcher matcher = pattern.matcher(tvShow.getTitle());
    if (matcher.find()) {
      return true;
    }

    // second: filter the episodes title
    for (TvShowEpisode episode : episodes) {
      matcher = pattern.matcher(episode.getTitle());
      if (matcher.find()) {
        return true;
      }
    }

    // fallback
    return false;
  }

  private boolean matchesCrew(TvShow tvShow, List<TvShowEpisode> episodes, String filterText) {
    if (StringUtils.isBlank(filterText)) {
      return true;
    }

    Pattern pattern = Pattern.compile("(?i)" + Pattern.quote(filterText));

    // first: filter on the base cast of the TV show
    for (TvShowActor actor : tvShow.getActors()) {
      Matcher matcher = pattern.matcher(actor.getName());
      if (matcher.find()) {
        return true;
      }
    }

    // second: filter director/writer and guests from episodes
    for (TvShowEpisode episode : episodes) {
      Matcher matcher = pattern.matcher(episode.getDirector());
      if (matcher.find()) {
        return true;
      }
      matcher = pattern.matcher(episode.getWriter());
      if (matcher.find()) {
        return true;
      }
      for (TvShowActor actor : episode.getGuests()) {
        matcher = pattern.matcher(actor.getName());
        if (matcher.find()) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean matchesMissingMetadata(TvShow tvShow, List<TvShowEpisode> episodes) {
    if (!tvShow.isScraped()) {
      return true;
    }

    for (TvShowEpisode episode : episodes) {
      if (!episode.isScraped()) {
        return true;
      }
    }

    return false;
  }

  private boolean matchesMissingArtwork(TvShow tvShow, List<TvShowEpisode> episodes) {
    if (!tvShow.getHasImages()) {
      return true;
    }

    for (TvShowEpisode episode : episodes) {
      if (!episode.getHasImages()) {
        return true;
      }
    }

    return false;
  }

  private boolean matchesMissingSubtitles(List<TvShowEpisode> episodes) {
    for (TvShowEpisode episode : episodes) {
      if (!episode.hasSubtitles()) {
        return true;
      }
    }

    return false;
  }
}
