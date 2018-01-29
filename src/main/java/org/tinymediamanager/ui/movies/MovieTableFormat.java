/*
 * Copyright 2012 - 2018 Manuel Laggner
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
package org.tinymediamanager.ui.movies;

import java.awt.FontMetrics;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.ui.DateTableCellRenderer;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.table.TmmTableFormat;

/**
 * The MovieTableFormat. Used as definition for the movie table in the movie module
 *
 * @author Manuel Laggner
 */
public class MovieTableFormat extends TmmTableFormat<Movie> {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public MovieTableFormat() {

    Comparator<Movie> movieComparator = new MovieComparator();
    Comparator<String> stringComparator = new StringComparator();
    Comparator<Float> floatComparator = new FloatComparator();
    Comparator<ImageIcon> imageComparator = new ImageComparator();
    Comparator<Date> dateComparator = new DateComparator();
    Comparator<String> videoFormatComparator = new VideoFormatComparator();
    Comparator<String> fileSizeComparator = new FileSizeComparator();
    Comparator<Integer> integerComparator = new IntegerComparator();

    FontMetrics fontMetrics = getFontMetrics();

    /*
     * title
     */
    Column col = new Column(BUNDLE.getString("metatag.title"), "title", movie -> movie, Movie.class);
    col.setColumnComparator(movieComparator);
    col.setCellRenderer(new MovieBorderTableCellRenderer());
    addColumn(col);

    /*
     * original title (hidden per default)
     */
    col = new Column(BUNDLE.getString("metatag.originaltitle"), "originalTitle", movie -> movie, Movie.class);
    col.setColumnComparator(movieComparator);
    col.setCellRenderer(new MovieBorderTableCellRenderer());
    addColumn(col);

    /*
     * year
     */
    col = new Column(BUNDLE.getString("metatag.year"), "year", MediaEntity::getYear, Movie.class);
    col.setColumnComparator(integerComparator);
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("2000") * 1.2f));
    addColumn(col);

    /*
     * file name (hidden per default)
     */
    col = new Column(BUNDLE.getString("metatag.filename"), "filename", movie -> {
      MediaFile mf = movie.getMediaFiles(MediaFileType.VIDEO).get(0);
      if (mf != null) {
        return mf.getFilename();
      }
      return "";
    }, String.class);
    col.setColumnComparator(stringComparator);
    col.setColumnResizeable(true);
    addColumn(col);

    /*
     * folder name (hidden per default)
     */
    col = new Column(BUNDLE.getString("metatag.path"), "path", MediaEntity::getPathNIO, String.class);
    col.setColumnComparator(stringComparator);
    col.setColumnResizeable(true);
    addColumn(col);

    /*
     * movie set (hidden per default)
     */
    col = new Column(BUNDLE.getString("metatag.movieset"), "movieset", movie -> {
      MovieSet set = movie.getMovieSet();
      if (set != null) {
        return set.getTitle();
      }
      return "";
    }, String.class);
    col.setColumnComparator(stringComparator);
    col.setColumnResizeable(true);
    addColumn(col);

    /*
     * rating
     */
    col = new Column(BUNDLE.getString("metatag.rating"), "rating", movie -> movie.getRating().getRating(), Float.class);
    col.setColumnComparator(floatComparator);
    col.setHeaderIcon(IconManager.RATING);
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("99.9") * 1.2f));
    addColumn(col);

    /*
     * date added
     */
    col = new Column(BUNDLE.getString("metatag.dateadded"), "dateAdded", MediaEntity::getDateAdded, Date.class);
    col.setColumnComparator(dateComparator);
    col.setHeaderIcon(IconManager.DATE_ADDED);
    DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
    col.setCellRenderer(new DateTableCellRenderer(dateFormat));
    col.setColumnResizeable(false);
    try {
      Date date = StrgUtils.parseDate("2012-12-12");
      col.setMinWidth((int) (fontMetrics.stringWidth(dateFormat.format(date)) * 1.2f));
    }
    catch (Exception ignored) {
    }
    addColumn(col);

    /*
     * video format
     */
    col = new Column(BUNDLE.getString("metatag.format"), "videoFormat", Movie::getMediaInfoVideoFormat, String.class);
    col.setColumnComparator(videoFormatComparator);
    col.setHeaderIcon(IconManager.VIDEO_FORMAT);
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("1080p") * 1.2f));
    addColumn(col);

    /*
     * main video file size (hidden per default)
     */
    col = new Column(BUNDLE.getString("metatag.size"), "fileSize", movie -> {
      long size = 0;
      for (MediaFile mf : movie.getMediaFiles(MediaFileType.VIDEO)) {
        size += mf.getFilesize();
      }

      // looks better everything in M
      // if (size > (2048L * 1024 * 1024)) {
      // return (int) (size / (1024.0 * 1024.0 * 1024)) + " G";
      // }

      return (int) (size / (1024.0 * 1024.0)) + " M";
    }, String.class);
    col.setColumnComparator(fileSizeComparator);
    col.setHeaderIcon(IconManager.FILE_SIZE);
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("50000M") * 1.2f));
    addColumn(col);

    /*
     * NFO
     */
    col = new Column(BUNDLE.getString("tmm.nfo"), "nfo", movie -> getCheckIcon(movie.getHasNfoFile()), ImageIcon.class);
    col.setColumnComparator(imageComparator);
    col.setHeaderIcon(IconManager.NFO);
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * images
     */
    col = new Column(BUNDLE.getString("tmm.images"), "images", movie -> getCheckIcon(movie.getHasImages()), ImageIcon.class);
    col.setColumnComparator(imageComparator);
    col.setHeaderIcon(IconManager.IMAGES);
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * trailer
     */
    col = new Column(BUNDLE.getString("tmm.trailer"), "trailer", movie -> getCheckIcon(movie.getHasTrailer()), ImageIcon.class);
    col.setColumnComparator(imageComparator);
    col.setHeaderIcon(IconManager.TRAILER);
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * subtitles
     */
    col = new Column(BUNDLE.getString("tmm.subtitles"), "subtitles", movie -> getCheckIcon(movie.hasSubtitles()), ImageIcon.class);
    col.setColumnComparator(imageComparator);
    col.setHeaderIcon(IconManager.SUBTITLES);
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * watched
     */
    col = new Column(BUNDLE.getString("metatag.watched"), "watched", movie -> getCheckIcon(movie.isWatched()), ImageIcon.class);
    col.setColumnComparator(imageComparator);
    col.setHeaderIcon(IconManager.WATCHED);
    col.setColumnResizeable(false);
    addColumn(col);
  }
}
