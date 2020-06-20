/*
 * Copyright 2012 - 2020 Manuel Laggner
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
package org.tinymediamanager.ui.movies.filters;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.table.TmmTableFormat;

/**
 * This class implements a tag filter for movies
 * 
 * @author Manuel Laggner
 */
public class MovieTagFilter extends AbstractCheckComboBoxMovieUIFilter<String> {
  private final Comparator<String> comparator;
  private final MovieList          movieList;
  private final Set<String>        oldTags;

  public MovieTagFilter() {
    super();
    comparator = new TmmTableFormat.StringComparator();
    movieList = MovieList.getInstance();
    oldTags = new HashSet<>();

    buildAndInstallTagsArray();
    movieList.addPropertyChangeListener(Constants.TAG, evt -> SwingUtilities.invokeLater(this::buildAndInstallTagsArray));
  }

  @Override
  public String getId() {
    return "movieTag";
  }

  @Override
  public boolean accept(Movie movie) {
    List<String> tags = checkComboBox.getSelectedItems();

    // check for explicit empty search
    if (tags.isEmpty() && movie.getTags().isEmpty()) {
      return true;
    }

    // check for all values
    for (String tag : movie.getTags()) {
      if (tags.contains(tag)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("movieextendedsearch.tag"));
  }

  private void buildAndInstallTagsArray() {
    // do it lazy because otherwise there is too much UI overhead
    // also use a set for faster lookups
    boolean dirty = false;
    Set<String> tags = new HashSet<>(movieList.getTagsInMovies());

    if (oldTags.size() != tags.size()) {
      dirty = true;
    }

    if (!oldTags.containsAll(tags) || !tags.containsAll(oldTags)) {
      dirty = true;
    }

    if (dirty) {
      oldTags.clear();
      oldTags.addAll(tags);

      setValues(ListUtils.asSortedList(tags, comparator));
    }
  }

  @Override
  protected String parseTypeToString(String type) throws Exception {
    return type;
  }

  @Override
  protected String parseStringToType(String string) throws Exception {
    return string;
  }
}
