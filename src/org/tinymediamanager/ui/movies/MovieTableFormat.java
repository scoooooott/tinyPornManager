/*
 * Copyright 2012 - 2013 Manuel Laggner
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

import java.util.Comparator;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.ui.ImageIconConverter;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;

import ca.odell.glazedlists.gui.AdvancedTableFormat;

/**
 * The Class MovieTableFormat.
 * 
 * @author Manuel Laggner
 */
public class MovieTableFormat implements AdvancedTableFormat<Movie> {
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());            //$NON-NLS-1$
  private final static ImageIcon      checkIcon        = new ImageIcon(MainWindow.class.getResource("images/Checkmark.png"));
  private final static ImageIcon      crossIcon        = new ImageIcon(MainWindow.class.getResource("images/Cross.png"));

  private Comparator<Movie>           movieComparator  = new MovieComparator();
  private Comparator<String>          stringComparator = new StringComparator();
  private Comparator<ImageIcon>       imageComparator  = new ImageComparator();

  /*
   * (non-Javadoc)
   * 
   * @see ca.odell.glazedlists.gui.TableFormat#getColumnCount()
   */
  @Override
  public int getColumnCount() {
    return 6;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ca.odell.glazedlists.gui.TableFormat#getColumnName(int)
   */
  @Override
  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return BUNDLE.getString("metatag.title"); //$NON-NLS-1$

      case 1:
        return BUNDLE.getString("metatag.year"); //$NON-NLS-1$

      case 2:
        return BUNDLE.getString("metatag.nfo"); //$NON-NLS-1$

      case 3:
        return BUNDLE.getString("metatag.images"); //$NON-NLS-1$

      case 4:
        return BUNDLE.getString("metatag.trailer"); //$NON-NLS-1$

      case 5:
        return BUNDLE.getString("metatag.subtitles"); //$NON-NLS-1$
    }

    throw new IllegalStateException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see ca.odell.glazedlists.gui.TableFormat#getColumnValue(java.lang.Object, int)
   */
  @Override
  public Object getColumnValue(Movie movie, int column) {
    switch (column) {
      case 0:
        return movie;

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

      case 4:
        if (movie.getHasTrailer()) {
          return checkIcon;
        }
        return crossIcon;

      case 5:
        if (movie.hasSubtitles()) {
          return checkIcon;
        }
        return crossIcon;
    }

    throw new IllegalStateException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnClass(int)
   */
  @SuppressWarnings("rawtypes")
  @Override
  public Class getColumnClass(int column) {
    switch (column) {
      case 0:
        return Movie.class;

      case 1:
        return String.class;

      case 2:
      case 3:
      case 4:
      case 5:
        return ImageIcon.class;
    }

    throw new IllegalStateException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnComparator(int)
   */
  @SuppressWarnings("rawtypes")
  @Override
  public Comparator getColumnComparator(int column) {
    switch (column) {
      case 0:
        return movieComparator;

      case 1:
        return stringComparator;

      case 2:
      case 3:
      case 4:
      case 5:
        return imageComparator;
    }

    throw new IllegalStateException();
  }

  private static class StringComparator implements Comparator<String> {

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
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

  private static class ImageComparator implements Comparator<ImageIcon> {

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(ImageIcon arg0, ImageIcon arg1) {
      if (arg0 == arg1) {
        return 0;
      }
      if (arg0 == ImageIconConverter.checkIcon) {
        return 1;
      }
      return -1;
    }
  }
}
