package org.tinymediamanager.ui.movies;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.table.TmmTableFormat;

import javax.swing.ImageIcon;
import java.util.Comparator;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Created by manuel on 26.06.16.
 */
public class MovieTableFormat2 extends TmmTableFormat<Movie> {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public MovieTableFormat2() {

    Comparator<Movie> movieComparator = new MovieComparator();
    Comparator<String> stringComparator = new StringComparator();
    Comparator<Float> floatComparator = new FloatComparator();
    Comparator<ImageIcon> imageComparator = new ImageComparator();
    Comparator<Date> dateComparator = new DateComparator();

    // title
    Column col = new ColumnBuilder().setTitle(BUNDLE.getString("metatag.title"))
            .setIdentifier("title")
            .setColumnValue(movie -> movie, Movie.class)
            .setColumnComparator(movieComparator)
            .build();
    addColumn(col);

    // year
    col = new ColumnBuilder().setTitle(BUNDLE.getString("metatag.year"))
            .setIdentifier("year")
            .setColumnValue(MediaEntity::getYear, Movie.class)
            .setColumnComparator(stringComparator)
            .build();
    addColumn(col);

    // rating
    col = new ColumnBuilder().setTitle(BUNDLE.getString("metatag.rating"))
            .setIdentifier("rating")
            .setColumnValue(MediaEntity::getRating, Float.class)
            .setColumnComparator(floatComparator)
            .build();
    addColumn(col);

    // date added
    col = new ColumnBuilder().setTitle(BUNDLE.getString("metatag.dateadded"))
            .setIdentifier("dateAdded")
            .setColumnValue(MediaEntity::getDateAdded, Date.class)
            .setColumnComparator(dateComparator)
            .build();
    addColumn(col);

    // NFO
    col = new ColumnBuilder().setTitle(BUNDLE.getString("tmm.nfo"))
            .setIdentifier("nfo")
            .setColumnValue(movie -> getCheckIcon(movie.getHasNfoFile()), ImageIcon.class)
            .setColumnComparator(imageComparator)
            .build();
    addColumn(col);

    // images
    col = new ColumnBuilder().setTitle(BUNDLE.getString("tmm.images"))
            .setIdentifier("images")
            .setColumnValue(movie -> getCheckIcon(movie.getHasImages()), ImageIcon.class)
            .setColumnComparator(imageComparator)
            .build();
    addColumn(col);

    // trailer
    col = new ColumnBuilder().setTitle(BUNDLE.getString("tmm.trailer"))
            .setIdentifier("trailer")
            .setColumnValue(movie -> getCheckIcon(movie.getHasTrailer()), ImageIcon.class)
            .setColumnComparator(imageComparator)
            .build();
    addColumn(col);

    // subtitles
    col = new ColumnBuilder().setTitle(BUNDLE.getString("tmm.subtitles"))
            .setIdentifier("subtitles")
            .setColumnValue(movie -> getCheckIcon(movie.hasSubtitles()), ImageIcon.class)
            .setColumnComparator(imageComparator)
            .build();
    addColumn(col);

    // watched
    col = new ColumnBuilder().setTitle(BUNDLE.getString("metatag.watched"))
            .setIdentifier("watched")
            .setColumnValue(movie -> getCheckIcon(movie.isWatched()), ImageIcon.class)
            .setColumnComparator(imageComparator)
            .build();
    addColumn(col);

  }

  private ImageIcon getCheckIcon(boolean bool) {
    if (bool) {
      return IconManager.DOT_AVAILABLE;
    }
    return IconManager.DOT_UNAVAILABLE;
  }

  private static class StringComparator implements Comparator<String> {
    @Override
    public int compare(String arg0, String arg1) {
      if (StringUtils.isEmpty(arg0)) {
        return -1;
      }
      if (StringUtils.isEmpty(arg1)) {
        return 1;
      }
      return arg0.toLowerCase().compareTo(arg1.toLowerCase());
    }
  }

  private static class FloatComparator implements Comparator<Float> {
    @Override
    public int compare(Float arg0, Float arg1) {
      return arg0.compareTo(arg1);
    }
  }

  private static class ImageComparator implements Comparator<ImageIcon> {
    @Override
    public int compare(ImageIcon arg0, ImageIcon arg1) {
      if (arg0 == arg1) {
        return 0;
      }
      if (arg0 == IconManager.DOT_AVAILABLE) {
        return 1;
      }
      return -1;
    }
  }

  private static class DateComparator implements Comparator<Date> {
    @Override
    public int compare(Date arg0, Date arg1) {
      return arg0.compareTo(arg1);
    }
  }
}
