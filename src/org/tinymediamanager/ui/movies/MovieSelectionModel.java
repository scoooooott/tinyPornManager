package org.tinymediamanager.ui.movies;

import java.util.List;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.movie.Movie;

import ca.odell.glazedlists.swing.EventSelectionModel;

public class MovieSelectionModel extends AbstractModelObject implements ListSelectionListener {

  private static final String SELECTED_MOVIE = "selectedMovie";

  private List<Movie>         selectedMovies;
  private Movie               selectedMovie;

  public MovieSelectionModel(EventSelectionModel<Movie> model) {
    selectedMovies = model.getSelected();
  }

  @Override
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting() == false) {
      if (selectedMovies.size() > 0) {
        Movie oldValue = selectedMovie;
        selectedMovie = selectedMovies.get(0);
        firePropertyChange(SELECTED_MOVIE, oldValue, selectedMovie);
        System.out.println(oldValue + " " + selectedMovies);
      }
    }

  }

  public Movie getSelectedMovie() {
    return selectedMovie;
  }

  public void setSelectedMovie() {

  }

}
