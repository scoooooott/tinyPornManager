package org.tinymediamanager.core.movie;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.apache.log4j.BasicConfigurator;
import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.scraper.IMediaMetadataProvider;

public class MovieList extends AbstractModelObject {

  private static MovieList       instance;
  private final Settings         settings       = Settings.getInstance();

  private final List<Movie>      movieList      = ObservableCollections.observableList(new ArrayList<Movie>());
  private List<MovieJobConfig>   moviesToScrape = new ArrayList<MovieJobConfig>();

  private IMediaMetadataProvider metadataProvider;

  private MovieList() {
    // Set up a simple configuration that logs on the console.
    BasicConfigurator.configure();

    // load existing movies from database
    loadMoviesFromDatabase();

  }

  public static MovieList getInstance() {
    if (MovieList.instance == null) {
      MovieList.instance = new MovieList();
    }
    return MovieList.instance;
  }

  public void addMovie(Movie movie) {
    movieList.add(movie);
    firePropertyChange("movies", null, movieList);
  }

  public void removeMovie(Movie movie) {
    movieList.remove(movie);
    firePropertyChange("movies", null, movieList);
  }

  public List<Movie> getMovies() {
    return movieList;
  }

  // load movielist from database
  private void loadMoviesFromDatabase() {
    try {
      TypedQuery<Movie> query = Globals.entityManager.createQuery("SELECT movie FROM Movie movie", Movie.class);
      List<Movie> movies = query.getResultList();
      // List<Movie> movies = MovieJdbcDAO.getInstance().getAllMovies();
      for (Movie movie : movies) {
        movie.setObservableCastList();
        addMovie(movie);
      }
    }
    catch (PersistenceException e) {
      e.printStackTrace();
    }
  }

  // Search for new media
  public void updateDataSources() {
    Globals.entityManager.getTransaction().begin();
    // each datasource
    for (String path : settings.getMovieDataSource()) {
      // each subdir
      for (File subdir : new File(path).listFiles()) {
        if (subdir.isDirectory()) {
          findMovieInDirectory(subdir);
        }
      }
    }
    Globals.entityManager.getTransaction().commit();
  }

  // check if there is a movie in this dir
  private void findMovieInDirectory(File dir) {
    // check if there are any videofiles in that subdir
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        boolean typeFound = false;

        // do not start with .
        if (name.toLowerCase().startsWith("."))
          return false;

        // check if filetype is in our settigns
        for (String type : settings.getVideoFileType()) {
          if (name.toLowerCase().endsWith(type)) {
            typeFound = true;
            break;
          }
        }

        return typeFound;
      }
    };

    File[] videoFiles = dir.listFiles(filter);
    // movie files found in directory?
    if (videoFiles.length > 0) {
      // does this path exists for an other movie?
      Movie movie = getMovieByPath(dir.getPath());
      if (movie == null) {
        // movie did not exist - try to parse a NFO file
        movie = Movie.parseNFO(dir.getPath());
        if (movie == null) {
          // movie did not exist - create new one
          movie = new Movie();
          String name = dir.getName().replaceAll("\\[.*[0-9].\\]", ""); // cut
          // year
          // information
          name = name.replaceAll("[._]", " "); // replace ._ in folder
          // name
          movie.setName(name);
          movie.setPath(dir.getPath());
        }
        // persist movie
        if (movie != null) {
          Globals.entityManager.persist(movie);
          addMovie(movie);
        }
      }

      for (File file : videoFiles) {
        // check if that file exists for that movie
        if (!movie.hasFile(file.getName())) {
          // create new movie file
          movie.addToFiles(file.getName());
        }
      }

    }
    else {
      // no - dig deeper
      for (File subdir : dir.listFiles()) {
        if (subdir.isDirectory()) {
          findMovieInDirectory(subdir);
        }
      }
    }
  }

  // // scrape single movie
  // public void searchMovie(Movie movieToScrape, int scrapeSetting) {
  // try {
  // List<MovieChooserModel> moviesFound = new ArrayList<MovieChooserModel>();
  // List<MediaSearchResult> result = metadataProvider
  // .search(new SearchQuery(MediaType.MOVIE,
  // SearchQuery.Field.QUERY, movieToScrape.getName()));
  // for (MediaSearchResult res : result) {
  // MovieChooserModel movieFound = new MovieChooserModel(
  // this.metadataProvider, res);
  // moviesFound.add(movieFound);
  // // System.out.println(res.getTitle() + "  " + res.getScore());
  // // MediaMetadata meta = pr.getMetaData(res);
  // // String movieDetail = mp.getDetails(new XbmcUrl(res.getUrl()),
  // // res.getIMDBId());
  // // movieDetail.charAt(0);
  //
  // }
  // movieToScrape.setMoviesFound(moviesFound);
  // // chooseMovie(movieToScrape);
  // } catch (Exception e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  // // try {
  // // List<net.sf.jtmdb.Movie> movies =
  // // net.sf.jtmdb.Movie.search(movieToScrape.getName());
  // // if (movies != null) {
  // // if ((movies.size() > 1 || movies.size() == 0)
  // // && (scrapeSetting == MovieJobConfig.CHOOSE_MOVIE || scrapeSetting ==
  // // MovieJobConfig.CHOOSE_MOVIE_AND_IMAGES)) {
  // // // more than one movie found (or none) - display moviechooser window
  // // // (if ths user does not force best match)
  // // List<MovieChooserModel> moviesFound = new
  // // ArrayList<MovieChooserModel>();
  // // for (net.sf.jtmdb.Movie movie : movies) {
  // // MovieChooserModel movieFound = new MovieChooserModel(movie);
  // // moviesFound.add(movieFound);
  // // }
  // // movieToScrape.setMoviesFound(moviesFound);
  // // chooseMovie(movieToScrape);
  // //
  // // } else {
  // // // only one movie found
  // // Iterator<net.sf.jtmdb.Movie> iterMovie = movies.iterator();
  // // if (iterMovie.hasNext()) {
  // // net.sf.jtmdb.Movie movie = iterMovie.next();
  // // movieToScrape.setTmdbMovie(net.sf.jtmdb.Movie.getInfo(movie.getID()));
  // // scrapeMovieData(movieToScrape);
  // // }
  // // }
  // // }
  // // } catch (Exception e) {
  // // e.printStackTrace();
  // // }
  // }

  // // scrape single movie
  // public void scrapeMovieData(Movie movieToScrape, MovieChooserModel
  // chosenMovie) {
  // // movieToScrape.scrapeFromTMDB();
  // // movieToScrape.getImagesFromTMDB();
  //
  //
  // }

  // search movie
  public void searchForMovie(Movie movieToScrape, String name) {
    // try {
    // List<net.sf.jtmdb.Movie> movies = net.sf.jtmdb.Movie.search(name);
    // if (movies != null) {
    // List<MovieChooserModel> moviesFound = new
    // ArrayList<MovieChooserModel>();
    // for (net.sf.jtmdb.Movie movie : movies) {
    // MovieChooserModel movieFound = new MovieChooserModel(movie);
    // moviesFound.add(movieFound);
    // }
    // movieToScrape.setMoviesFound(moviesFound);
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
  }

  // get path for each movie
  private Movie getMovieByPath(String path) {

    for (Movie movie : movieList) {
      if (movie.getPath().compareTo(path) == 0) {
        return movie;
      }
    }

    return null;
  }

  public void addMovieToScrapeList(Movie movie, int scrapeSetting) {
    moviesToScrape.add(new MovieJobConfig(movie, scrapeSetting));
  }

  // // scrape all selected movies
  // public void searchMovies() {
  // if (movieScraperJob == null) {
  // movieScraperJob = new Job("Scraping movies") {
  // @Override
  // protected IStatus run(IProgressMonitor monitor) {
  // while (moviesToScrape.size() > 0) {
  // MovieJobConfig job = moviesToScrape.get(0);
  // searchMovie(job.getMovie(), job.getScrapeSetting());
  // moviesToScrape.remove(job);
  // }
  // return Status.OK_STATUS;
  // }
  // };
  // }
  // movieScraperJob.schedule();
  // }
  //
  // // show moviechooser window in UIJob
  // private void chooseMovie(Movie movieToScrape) {
  // if (movieChooserJob == null) {
  // movieChooserJob = new MovieChooserJob("Choose movie");
  // movieChooserJob.setRule(Globals.uiJob);
  // }
  // movieChooserJob.addMovie(movieToScrape);
  // }
  //
  // public IMediaMetadataProvider getMetadataProvider() {
  // return this.metadataProvider;
  // }
}
