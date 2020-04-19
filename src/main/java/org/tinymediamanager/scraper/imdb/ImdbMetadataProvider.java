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
package org.tinymediamanager.scraper.imdb;

import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowEpisodeSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMovieImdbMetadataProvider;
import org.tinymediamanager.scraper.interfaces.IMovieMetadataProvider;
import org.tinymediamanager.scraper.interfaces.ITvShowMetadataProvider;

/**
 * The public ImdbMetadataProvider() {
 * 
 * }Class ImdbMetadataProvider. A meta data provider for the site imdb.com
 *
 * @author Manuel Laggner
 */
public class ImdbMetadataProvider implements IMovieMetadataProvider, ITvShowMetadataProvider, IMovieImdbMetadataProvider {
  public static final String     USE_TMDB_FOR_MOVIES   = "useTmdbForMovies";
  public static final String     USE_TMDB_FOR_TV_SHOWS = "useTmdbForTvShows";
  public static final String     ID                    = "imdb";

  static final MediaProviderInfo providerInfo          = createMediaProviderInfo();
  static final ExecutorService   executor              = Executors.newFixedThreadPool(4);

  static final String            CAT_TITLE             = "&s=tt";
  static final String            CAT_TV                = "&s=tt&ttype=tv&ref_=fn_tv";

  private static MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo(ID, "IMDb.com",
        "<html><h3>Internet Movie Database (IMDb)</h3><br />The most used database for movies all over the world.<br />Does not contain plot/title/tagline in every language. You may choose to download these texts from TMDb<br /><br />Available languages: multiple</html>",
        ImdbMetadataProvider.class.getResource("/org/tinymediamanager/scraper/imdb_com.png"));

    // configure/load settings
    providerInfo.getConfig().addBoolean("filterUnwantedCategories", true);
    providerInfo.getConfig().addBoolean(USE_TMDB_FOR_MOVIES, false);
    providerInfo.getConfig().addBoolean(USE_TMDB_FOR_TV_SHOWS, false);
    providerInfo.getConfig().addBoolean("scrapeCollectionInfo", false);
    providerInfo.getConfig().addBoolean("localReleaseDate", true);
    providerInfo.getConfig().addBoolean("scrapeLanguageNames", true);
    providerInfo.getConfig().addBoolean("scrapeKeywordsPage", false);
    providerInfo.getConfig().addInteger("maxKeywordCount", 10);

    providerInfo.getConfig().load();

    return providerInfo;
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public SortedSet<MediaSearchResult> search(MovieSearchAndScrapeOptions options) throws ScrapeException {
    return (new ImdbMovieParser()).search(options);
  }

  @Override
  public MediaMetadata getMetadata(MovieSearchAndScrapeOptions options) throws ScrapeException, MissingIdException, NothingFoundException {
    return (new ImdbMovieParser()).getMovieMetadata(options);
  }

  @Override
  public MediaMetadata getMetadata(TvShowSearchAndScrapeOptions options) throws ScrapeException, MissingIdException, NothingFoundException {
    return (new ImdbTvShowParser()).getTvShowMetadata(options);
  }

  @Override
  public MediaMetadata getMetadata(TvShowEpisodeSearchAndScrapeOptions options) throws ScrapeException, MissingIdException, NothingFoundException {
    return (new ImdbTvShowParser()).getEpisodeMetadata(options);
  }

  @Override
  public SortedSet<MediaSearchResult> search(TvShowSearchAndScrapeOptions options) throws ScrapeException {
    return (new ImdbTvShowParser()).search(options);
  }

  @Override
  public List<MediaMetadata> getEpisodeList(TvShowSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    return new ImdbTvShowParser().getEpisodeList(options);
  }

  @Override
  public List<MediaMetadata> getEpisodeList(TvShowEpisodeSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    return new ImdbTvShowParser().getEpisodeList(options);
  }

  static void processMediaArt(MediaMetadata md, MediaArtworkType type, String image) {
    MediaArtwork ma = new MediaArtwork(providerInfo.getId(), type);
    ma.setPreviewUrl(image);
    ma.setDefaultUrl(image);
    md.addMediaArt(ma);
  }

  static String cleanString(String oldString) {
    if (StringUtils.isEmpty(oldString)) {
      return "";
    }
    // remove non breaking spaces
    String newString = StringUtils.trim(oldString.replace(String.valueOf((char) 160), " "));

    // if there is a leading AND trailing quotation marks (e.g. at TV shows) - remove them
    if (newString.startsWith("\"") && newString.endsWith("\"")) {
      newString = StringUtils.stripEnd(StringUtils.stripStart(newString, "\""), "\"");
    }

    // and trim
    return newString;
  }

  /*
   * Maps scraper Genres to internal TMM genres
   */
  static MediaGenres getTmmGenre(String genre) {
    MediaGenres g = null;
    if (StringUtils.isBlank(genre)) {
      return null;
    }
    // @formatter:off
    else if (genre.equals("Action")) {
      g = MediaGenres.ACTION;
    } else if (genre.equals("Adult")) {
      g = MediaGenres.EROTIC;
    } else if (genre.equals("Adventure")) {
      g = MediaGenres.ADVENTURE;
    } else if (genre.equals("Animation")) {
      g = MediaGenres.ANIMATION;
    } else if (genre.equals("Biography")) {
      g = MediaGenres.BIOGRAPHY;
    } else if (genre.equals("Comedy")) {
      g = MediaGenres.COMEDY;
    } else if (genre.equals("Crime")) {
      g = MediaGenres.CRIME;
    } else if (genre.equals("Documentary")) {
      g = MediaGenres.DOCUMENTARY;
    } else if (genre.equals("Drama")) {
      g = MediaGenres.DRAMA;
    } else if (genre.equals("Family")) {
      g = MediaGenres.FAMILY;
    } else if (genre.equals("Fantasy")) {
      g = MediaGenres.FANTASY;
    } else if (genre.equals("Film-Noir")) {
      g = MediaGenres.FILM_NOIR;
    } else if (genre.equals("Game-Show")) {
      g = MediaGenres.GAME_SHOW;
    } else if (genre.equals("History")) {
      g = MediaGenres.HISTORY;
    } else if (genre.equals("Horror")) {
      g = MediaGenres.HORROR;
    } else if (genre.equals("Music")) {
      g = MediaGenres.MUSIC;
    } else if (genre.equals("Musical")) {
      g = MediaGenres.MUSICAL;
    } else if (genre.equals("Mystery")) {
      g = MediaGenres.MYSTERY;
    } else if (genre.equals("News")) {
      g = MediaGenres.NEWS;
    } else if (genre.equals("Reality-TV")) {
      g = MediaGenres.REALITY_TV;
    } else if (genre.equals("Romance")) {
      g = MediaGenres.ROMANCE;
    } else if (genre.equals("Sci-Fi")) {
      g = MediaGenres.SCIENCE_FICTION;
    } else if (genre.equals("Short")) {
      g = MediaGenres.SHORT;
    } else if (genre.equals("Sport")) {
      g = MediaGenres.SPORT;
    } else if (genre.equals("Talk-Show")) {
      g = MediaGenres.TALK_SHOW;
    } else if (genre.equals("Thriller")) {
      g = MediaGenres.THRILLER;
    } else if (genre.equals("War")) {
      g = MediaGenres.WAR;
    } else if (genre.equals("Western")) {
      g = MediaGenres.WESTERN;
    }
    // @formatter:on
    if (g == null) {
      g = MediaGenres.getGenre(genre);
    }
    return g;
  }
}
