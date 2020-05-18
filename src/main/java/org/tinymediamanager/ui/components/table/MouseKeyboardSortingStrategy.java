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
package org.tinymediamanager.ui.components.table;

import java.util.List;

import ca.odell.glazedlists.impl.gui.SortingState;
import ca.odell.glazedlists.impl.gui.SortingStrategy;

/**
 * this class is used to provide a sorting strategy for our tables, but only with ASCENDING and DESCENDING order (without NONE which is in the default
 * implementation of GlazedLists)
 * 
 * @author Manuel Laggner
 */
public class MouseKeyboardSortingStrategy implements SortingStrategy {

  /** a column is sorted in forward or reverse */
  private static final int NONE    = 0;
  private static final int FORWARD = 1;
  private static final int REVERSE = 2;

  /**
   * Adjust the sorting state based on receiving the specified click event.
   */
  @Override
  public void columnClicked(SortingState sortingState, int column, int clicks, boolean shift, boolean control) {
    SortingState.SortingColumn sortingColumn = sortingState.getColumns().get(column);
    if (sortingColumn.getComparators().isEmpty())
      return;
    List<SortingState.SortingColumn> recentlyClickedColumns = sortingState.getRecentlyClickedColumns();

    // figure out which comparator and reverse state we were on before
    int comparatorIndexBefore = sortingColumn.getComparatorIndex();
    final int forwardReverseNoneBefore;
    if (comparatorIndexBefore == -1)
      forwardReverseNoneBefore = NONE;
    else
      forwardReverseNoneBefore = sortingColumn.isReverse() ? REVERSE : FORWARD;

    // figure out which comparator and reverse state we shall go to
    int forwardReverseNoneAfter;
    int comparatorIndexAfter;
    boolean moreComparators = comparatorIndexBefore + 1 < sortingColumn.getComparators().size();
    boolean lastDirective = shift ? forwardReverseNoneBefore == FORWARD : forwardReverseNoneBefore == REVERSE;

    // if we're on the last mode of this comparator, go to the next comparator
    if (moreComparators && lastDirective) {
      comparatorIndexAfter = (comparatorIndexBefore + 1) % sortingColumn.getComparators().size();
      forwardReverseNoneAfter = forwardReverseNoneBefore == FORWARD ? REVERSE : FORWARD;

      // otherwise merely toggle forward/reverse/none
    }
    else {
      comparatorIndexAfter = comparatorIndexBefore != -1 ? comparatorIndexBefore : 0;
      forwardReverseNoneAfter = (shift ? forwardReverseNoneBefore + 2 : forwardReverseNoneBefore + 1) % 3;
    }

    // clean up if necessary
    if (!control) {
      sortingState.clearComparators();
    }

    // prepare the latest column
    if (forwardReverseNoneAfter == NONE) {
      forwardReverseNoneAfter = FORWARD;
    }

    sortingColumn.setComparatorIndex(comparatorIndexAfter);
    sortingColumn.setReverse(forwardReverseNoneAfter == REVERSE);
    if (!recentlyClickedColumns.contains(sortingColumn)) {
      recentlyClickedColumns.add(sortingColumn);
    }

    // rebuild the sorting state
    sortingState.fireSortingChanged();
  }
}
