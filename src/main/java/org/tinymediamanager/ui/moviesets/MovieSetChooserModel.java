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
package org.tinymediamanager.ui.moviesets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.PluginManager;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.IMovieSetProvider;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class MovieSetChooserModel.
 */
public class MovieSetChooserModel extends AbstractModelObject {
  private static final ResourceBundle      BUNDLE      = ResourceBundle.getBundle("messages", new UTF8Control());          //$NON-NLS-1$
  private static final Logger              LOGGER      = LoggerFactory.getLogger(MovieSetChooserModel.class);
  public static final MovieSetChooserModel emptyResult = new MovieSetChooserModel();
  private String                           name        = "";
  private String                           posterUrl   = "";
  private String                           fanartUrl   = "";
  private int                              tmdbId      = 0;
  private MediaSearchResult                result      = null;
  private MediaMetadata                    metadata    = null;
  private List<MovieInSet>                 movies      = ObservableCollections.observableList(new ArrayList<MovieInSet>());
  private IMovieSetProvider                mp;

  private boolean scraped;

  public MovieSetChooserModel(MediaSearchResult result) {
    this.result = result;

    setName(result.getTitle());
    setTmdbId(Integer.valueOf(result.getId()));
    setPosterUrl(result.getPosterUrl());

    try {
      List<IMovieSetProvider> sets = PluginManager.getInstance().getMovieSetPlugins();
      if (sets != null && sets.size() > 0) {
        mp = sets.get(0); // just get first
      }
    }
    catch (Exception e) {
      mp = null;
    }
  }

  /**
   * create the empty search result.
   */
  private MovieSetChooserModel() {
    setName(BUNDLE.getString("chooser.nothingfound")); //$NON-NLS-1$
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    firePropertyChange("name", "", name);
  }

  public int getTmdbId() {
    return tmdbId;
  }

  public void setTmdbId(int tmdbId) {
    this.tmdbId = tmdbId;
  }

  public void setPosterUrl(String posterUrl) {
    this.posterUrl = posterUrl;
    firePropertyChange("posterUrl", "", posterUrl);
  }

  public void setFanartUrl(String fanartUrl) {
    this.fanartUrl = fanartUrl;
    firePropertyChange("fanartUrl", "", fanartUrl);
  }

  public boolean isScraped() {
    return scraped;
  }

  public String getPosterUrl() {
    return posterUrl;
  }

  public String getFanartUrl() {
    return fanartUrl;
  }

  /**
   * Match with existing movies.
   */
  public void matchWithExistingMovies() {
    List<Movie> moviesFromMovieList = MovieList.getInstance().getMovies();
    for (MovieInSet mis : movies) {
      // try to match via tmdbid
      if (mis.tmdbId > 0) {
        for (Movie movie : moviesFromMovieList) {
          if (movie.getTmdbId() == mis.tmdbId) {
            mis.setMovie(movie);
            break;
          }
        }
      }

      // try to match via imdbid if nothing has been found
      if (mis.movie == null) {
        if (StringUtils.isEmpty(mis.imdbId)) {
          // get imdbid for this tmdbid
          if (mp != null) {
            MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE);
            options.setTmdbId(mis.tmdbId);
            options.setLanguage(MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage());
            options.setCountry(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry());
            options.setScrapeImdbForeignLanguage(MovieModuleManager.MOVIE_SETTINGS.isImdbScrapeForeignLanguage());
            try {
              MediaMetadata md = mp.getMetadata(options);
              mis.imdbId = String.valueOf(md.getId(MediaMetadata.IMDB));
            }
            catch (Exception e) {
              LOGGER.warn(e.getMessage());
            }
          }
        }

        if (StringUtils.isNotEmpty(mis.imdbId)) {
          for (Movie movie : moviesFromMovieList) {
            if (mis.imdbId.equals(movie.getImdbId())) {
              mis.setMovie(movie);
              break;
            }
          }
        }
      }
    }
  }

  /**
   * Scrape metadata.
   */
  public void scrapeMetadata() {
    try {
      if (mp != null) {
        MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE_SET);
        options.setTmdbId(Integer.parseInt(result.getId()));
        options.setLanguage(MovieModuleManager.MOVIE_SETTINGS.getScraperLanguage());
        options.setCountry(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry());
        options.setScrapeImdbForeignLanguage(MovieModuleManager.MOVIE_SETTINGS.isImdbScrapeForeignLanguage());

        MediaMetadata info = mp.getMetadata(options);
        // if (info != null && StringUtils.isNotBlank(info.getStringValue(MediaMetadata.TITLE))) {
        // movieSet.setTitle(info.getStringValue(MediaMetadata.TITLE));
        // movieSet.setPlot(info.getStringValue(MediaMetadata.PLOT));
        // movieSet.setArtworkUrl(info.getStringValue(MediaMetadata.POSTER_URL), MediaFileType.POSTER);
        // movieSet.setArtworkUrl(info.getStringValue(MediaMetadata.BACKGROUND_URL), MediaFileType.FANART);
        // }
        if (info != null) {
          this.metadata = info;
          setFanartUrl(info.getStringValue(MediaMetadata.BACKGROUND_URL));
          for (MediaMetadata item : info.getSubItems()) {
            MovieInSet movie = new MovieInSet(item.getStringValue(MediaMetadata.TITLE));
            try {
              movie.setTmdbId(Integer.parseInt(item.getId(MediaMetadata.TMDB).toString()));
            }
            catch (NumberFormatException ignored) {
            }
            movie.setReleaseDate(item.getStringValue(MediaMetadata.RELEASE_DATE));
            movies.add(movie);
          }

          Collections.sort(movies);

          // try to match movies
          matchWithExistingMovies();

          this.scraped = true;
        }
      }
    }
    catch (Exception e) {
      LOGGER.warn("error while scraping metadata", e);
    }

  }

  public String getOverview() {
    if (metadata == null) {
      return null;
    }
    return metadata.getStringValue(MediaMetadata.PLOT);
  }

  public List<MovieInSet> getMovies() {
    return movies;
  }

  public static class MovieInSet extends AbstractModelObject implements Comparable<MovieInSet> {
    private String name        = "";
    private int    tmdbId      = 0;
    private String imdbId      = "";
    private String releaseDate = "";
    private Movie  movie       = null;

    public MovieInSet(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public int getTmdbId() {
      return tmdbId;
    }

    public String getImdbId() {
      return imdbId;
    }

    public String getReleaseDate() {
      return releaseDate;
    }

    public Movie getMovie() {
      return movie;
    }

    public void setTmdbId(int tmdbId) {
      this.tmdbId = tmdbId;
    }

    public void setImdbId(String imdbId) {
      this.imdbId = imdbId;
    }

    public void setReleaseDate(String releaseDate) {
      this.releaseDate = releaseDate;
    }

    public void setMovie(Movie movie) {
      this.movie = movie;
      firePropertyChange("movie", null, movie);
    }

    @Override
    public int compareTo(MovieInSet o) {
      return releaseDate.compareTo(o.releaseDate);
    }
  }
}
