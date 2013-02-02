/*
 * Copyright 2012 Manuel Laggner
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

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;

import com.omertron.themoviedbapi.model.Collection;
import com.omertron.themoviedbapi.model.CollectionInfo;

public class MovieSetChooserModel extends AbstractModelObject {

  private String               name      = "";

  private String               posterUrl = "";

  private String               fanartUrl = "";

  private Collection           collection;

  private CollectionInfo       info;

  private List<MovieInSet>     movies    = ObservableCollections.observableList(new ArrayList<MovieInSet>());

  private TmdbMetadataProvider mp;

  private boolean              scraped;

  public MovieSetChooserModel(Collection collection) {
    this.collection = collection;

    setName(collection.getName());
    setPosterUrl(collection.getPosterPath());
    setFanartUrl(collection.getBackdropPath());

    try {
      mp = new TmdbMetadataProvider();
    }
    catch (Exception e) {
      mp = null;
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    firePropertyChange("name", "", name);
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
            MediaScrapeOptions options = new MediaScrapeOptions();
            options.setTmdbId(mis.tmdbId);
            try {
              MediaMetadata md = mp.getMetadata(options);
              mis.imdbId = md.getImdbId();
            }
            catch (Exception e) {
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

  public void scrapeMetadata() {
    try {
      if (mp != null) {
        MediaScrapeOptions options = new MediaScrapeOptions();
        options.setTmdbId(collection.getId());

        CollectionInfo info = mp.getMovieSetMetadata(options);
        if (info != null) {
          this.info = info;
          for (Collection collection : info.getParts()) {
            MovieInSet movie = new MovieInSet(collection.getName());
            movie.setTmdbId(collection.getId());
            movie.setReleaseDate(collection.getReleaseDate());
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
      // TODO use logger
      e.printStackTrace();
    }

  }

  public CollectionInfo getInfo() {
    return info;
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(MovieInSet o) {
      return releaseDate.compareTo(o.releaseDate);
    }
  }
}
