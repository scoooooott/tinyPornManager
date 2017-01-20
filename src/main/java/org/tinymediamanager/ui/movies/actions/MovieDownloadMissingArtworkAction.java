package org.tinymediamanager.ui.movies.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.tasks.MovieMissingArtworkDownloadTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.movies.MovieUIModule;

/**
 * The class MovieDownloadMissingArtworkAction is used to download missing artwork for the selected movies
 * 
 * @author Manuel Laggner
 */
public class MovieDownloadMissingArtworkAction extends AbstractAction {
  private static final long           serialVersionUID = -4006932829840795735L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public MovieDownloadMissingArtworkAction() {
    putValue(NAME, BUNDLE.getString("movie.downloadmissingartwork")); //$NON-NLS-1$
    putValue(SMALL_ICON, IconManager.IMAGE);
    putValue(LARGE_ICON_KEY, IconManager.IMAGE);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    List<Movie> selectedMovies = new ArrayList<>(MovieUIModule.getInstance().getSelectionModel().getSelectedMovies());

    if (!selectedMovies.isEmpty()) {
      MovieMissingArtworkDownloadTask task = new MovieMissingArtworkDownloadTask(selectedMovies);
      TmmTaskManager.getInstance().addDownloadTask(task);
    }
  }
}
