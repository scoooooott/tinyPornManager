/*
 * Copyright 2012 - 2016 Manuel Laggner
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

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.BorderTableCellRenderer;
import org.tinymediamanager.ui.DateTableCellRenderer;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.table.TmmTableFormat;

/**
 * The MovieTableFormat. Used as definition for the movie table in the movie module
 *
 * @author Manuel Laggner
 */
public class MovieTableFormat2 extends TmmTableFormat<Movie> {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public MovieTableFormat2() {

    Comparator<Movie> movieComparator = new MovieComparator();
    Comparator<String> stringComparator = new StringComparator();
    Comparator<Float> floatComparator = new FloatComparator();
    Comparator<ImageIcon> imageComparator = new ImageComparator();
    Comparator<Date> dateComparator = new DateComparator();
    Comparator<String> videoFormatComparator = new VideoFormatComparator();

    /*
     * title
     */
    Column col = new Column(BUNDLE.getString("metatag.title"), "title", movie -> movie, Movie.class);
    col.setColumnComparator(movieComparator);
    col.setCellRenderer(new BorderTableCellRenderer());
    addColumn(col);

    /*
     * year
     */
    col = new Column(BUNDLE.getString("metatag.year"), "year", MediaEntity::getYear, Movie.class);
    col.setColumnComparator(stringComparator);
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * rating
     */
    col = new Column(BUNDLE.getString("metatag.rating"), "rating", MediaEntity::getRating, Float.class);
    col.setColumnComparator(floatComparator);
    col.setHeaderIcon(IconManager.RATING);
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * date added
     */
    col = new Column(BUNDLE.getString("metatag.dateadded"), "dateAdded", MediaEntity::getDateAdded, Date.class);
    col.setColumnComparator(dateComparator);
    col.setHeaderIcon(IconManager.DATE_ADDED);
    col.setCellRenderer(new DateTableCellRenderer(SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)));
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * video format
     */
    col = new Column(BUNDLE.getString("metatag.format"), "videoFormat", Movie::getMediaInfoVideoFormat, String.class);
    col.setColumnComparator(videoFormatComparator);
    col.setHeaderIcon(IconManager.VIDEO_FORMAT);
    col.setColumnResizeable(false);
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
