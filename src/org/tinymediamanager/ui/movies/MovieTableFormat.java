package org.tinymediamanager.ui.movies;

import java.util.Comparator;

import javax.swing.ImageIcon;

import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.ui.MoviePanel;

import ca.odell.glazedlists.gui.AdvancedTableFormat;

public class MovieTableFormat implements AdvancedTableFormat<Movie> {

  /** The Constant checkIcon. */
  private final static ImageIcon checkIcon = new ImageIcon(MoviePanel.class.getResource("images/Checkmark.png"));

  /** The Constant crossIcon. */
  private final static ImageIcon crossIcon = new ImageIcon(MoviePanel.class.getResource("images/Cross.png"));

  @Override
  public int getColumnCount() {
    return 4;
  }

  @Override
  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return "Title";

      case 1:
        return "Year";

      case 2:
        return "NFO";

      case 3:
        return "Images";
    }

    throw new IllegalStateException();
  }

  @Override
  public Object getColumnValue(Movie movie, int column) {
    switch (column) {
      case 0:
        return movie.getName();

      case 1:
        return movie.getYear();

      case 2:
        if (movie.getHasNfoFile()) {
          return checkIcon;
        }
        return crossIcon;

      case 3:
        if (movie.getHasImages()) {
          return checkIcon;
        }
        return crossIcon;
    }

    throw new IllegalStateException();
  }

  @Override
  public Class getColumnClass(int column) {
    switch (column) {
      case 0:
      case 1:
        return String.class;

      case 2:
      case 3:
        return ImageIcon.class;
    }

    throw new IllegalStateException();
  }

  @Override
  public Comparator getColumnComparator(int arg0) {
    // TODO Auto-generated method stub
    return null;
  }
}
