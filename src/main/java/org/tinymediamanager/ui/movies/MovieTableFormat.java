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
package org.tinymediamanager.ui.movies;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.BorderTableCellRenderer;
import org.tinymediamanager.ui.DateTableCellRenderer;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;

import ca.odell.glazedlists.gui.AdvancedTableFormat;

/**
 * The Class MovieTableFormat.
 * 
 * @author Manuel Laggner
 */
public class MovieTableFormat implements AdvancedTableFormat<Movie> {
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private Comparator<Movie>           movieComparator  = new MovieComparator();
  private Comparator<String>          stringComparator = new StringComparator();
  private Comparator<Float>           floatComparator  = new FloatComparator();
  private Comparator<ImageIcon>       imageComparator  = new ImageComparator();
  private Comparator<Date>            dateComparator   = new DateComparator();

  @Override
  public int getColumnCount() {
    return 9;
  }

  @Override
  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return BUNDLE.getString("metatag.title"); //$NON-NLS-1$

      case 1:
        return BUNDLE.getString("metatag.year"); //$NON-NLS-1$

      case 2:
        return BUNDLE.getString("metatag.rating"); //$NON-NLS-1$

      case 3:
        return BUNDLE.getString("metatag.dateadded"); //$NON-NLS-1$

      case 4:
        return BUNDLE.getString("tmm.nfo"); //$NON-NLS-1$

      case 5:
        return BUNDLE.getString("tmm.images"); //$NON-NLS-1$

      case 6:
        return BUNDLE.getString("tmm.trailer"); //$NON-NLS-1$

      case 7:
        return BUNDLE.getString("tmm.subtitles"); //$NON-NLS-1$

      case 8:
        return BUNDLE.getString("metatag.watched"); //$NON-NLS-1$
    }

    throw new IllegalStateException();
  }

  @Override
  public Object getColumnValue(Movie movie, int column) {
    switch (column) {
      case 0:
        return movie;

      case 1:
        return movie.getYear();

      case 2:
        return movie.getRating();

      case 3:
        return movie.getDateAdded();

      case 4:
        if (movie.getHasNfoFile()) {
          return IconManager.DOT_AVAILABLE;
        }
        return IconManager.DOT_UNAVAILABLE;

      case 5:
        if (movie.getHasImages()) {
          return IconManager.DOT_AVAILABLE;
        }
        return IconManager.DOT_UNAVAILABLE;

      case 6:
        if (movie.getHasTrailer()) {
          return IconManager.DOT_AVAILABLE;
        }
        return IconManager.DOT_UNAVAILABLE;

      case 7:
        if (movie.hasSubtitles()) {
          return IconManager.DOT_AVAILABLE;
        }
        return IconManager.DOT_UNAVAILABLE;

      case 8:
        if (movie.isWatched()) {
          return IconManager.DOT_AVAILABLE;
        }
        return IconManager.DOT_UNAVAILABLE;
    }

    throw new IllegalStateException();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Class getColumnClass(int column) {
    switch (column) {
      case 0:
        return Movie.class;

      case 1:
        return String.class;

      case 2:
        return Float.class;

      case 3:
        return Date.class;

      case 4:
      case 5:
      case 6:
      case 7:
      case 8:
        return ImageIcon.class;
    }

    throw new IllegalStateException();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Comparator getColumnComparator(int column) {
    switch (column) {
      case 0:
        return movieComparator;

      case 1:
        return stringComparator;

      case 2:
        return floatComparator;

      case 3:
        return dateComparator;

      case 4:
      case 5:
      case 6:
      case 7:
      case 8:
        return imageComparator;
    }

    throw new IllegalStateException();
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

  /**
   * configure columns
   * 
   * @param table
   *          the table to set special header renderer
   */
  public static void configureColumns(JTable table) {
    int width;
    TableColumnModel columnModel = table.getTableHeader().getColumnModel();

    for (int i = 0; i < columnModel.getColumnCount(); i++) {
      TableColumn column = columnModel.getColumn(i);
      switch (i) {
        // title
        case 0:
          column.setCellRenderer(new BorderTableCellRenderer());
          column.setIdentifier("title"); //$NON-NLS-1$
          break;

        // year
        case 1:
          // year column
          width = table.getFontMetrics(table.getFont()).stringWidth(" 2000");
          int titleWidth = table.getFontMetrics(table.getFont()).stringWidth(BUNDLE.getString("metatag.year")); //$NON-NLS-1$
          if (titleWidth > width) {
            width = titleWidth;
          }
          column.setPreferredWidth(width);
          column.setMinWidth(width);
          column.setMaxWidth((int) (width * 1.5));
          column.setIdentifier("year"); //$NON-NLS-1$
          break;

        // rating
        case 2:
          width = (int) (IconManager.RATING.getIconWidth() * 1.3);
          setHeader(column, IconManager.RATING, width, BUNDLE.getString("metatag.rating"), "rating"); //$NON-NLS-1$
          break;

        // date added
        case 3:
          width = (int) (table.getFontMetrics(table.getFont()).stringWidth("10/20/20") * 1.2);
          setHeader(column, IconManager.DATE_ADDED, width, BUNDLE.getString("metatag.dateadded"), "dateadded"); //$NON-NLS-1$
          column.setCellRenderer(new DateTableCellRenderer(SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)));
          break;

        // NFO
        case 4:
          setHeader(column, IconManager.NFO, BUNDLE.getString("tmm.nfo"), "nfo"); //$NON-NLS-1$
          break;

        // images
        case 5:
          setHeader(column, IconManager.IMAGES, BUNDLE.getString("tmm.images"), "images"); //$NON-NLS-1$
          break;

        // trailer
        case 6:
          setHeader(column, IconManager.TRAILER, BUNDLE.getString("tmm.trailer"), "trailer"); //$NON-NLS-1$
          break;

        // subtitle
        case 7:
          setHeader(column, IconManager.SUBTITLES, BUNDLE.getString("tmm.subtitles"), "subtitle"); //$NON-NLS-1$
          break;

        // watched
        case 8:
          setHeader(column, IconManager.WATCHED, BUNDLE.getString("metatag.watched"), "watched"); //$NON-NLS-1$
          break;

        default:
          // do nothing; the default one is good enough
          break;
      }
    }
  }

  /**
   * set the header
   * 
   * @param column
   *          the column to set the header for
   * @param icon
   *          the icon to be displayed
   * @param tooltip
   *          the tooltip for the icon
   */
  private static void setHeader(TableColumn column, ImageIcon icon, String tooltip, String columnId) {
    setHeader(column, icon, TmmUIHelper.getColumnWidthForIcon(icon), tooltip, columnId);
  }

  /**
   * set the header
   * 
   * @param column
   *          the column to set the header for
   * @param icon
   *          the icon to be displayed
   * @param width
   *          the min/max width if the column
   * @param tooltip
   *          the tooltip for the icon
   */
  private static void setHeader(TableColumn column, ImageIcon icon, int width, String tooltip, String columnId) {
    column.setHeaderValue(icon);
    column.setMinWidth(width);
    column.setMaxWidth(width);
    column.setIdentifier(columnId);
    if (column.getHeaderRenderer() instanceof DefaultTableCellRenderer) {
      ((DefaultTableCellRenderer) column.getHeaderRenderer()).setToolTipText(tooltip);
    }
  }
}
