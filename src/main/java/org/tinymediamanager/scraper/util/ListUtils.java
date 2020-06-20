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
package org.tinymediamanager.scraper.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The class ListUtils is a helper class, providing some special functions for Lists
 * 
 * @author Manuel Laggner
 * @since 1.0
 */
public class ListUtils {

  private ListUtils() {
    // hide the public constructor for utility classes
  }

  /**
   * Get a nullsafe Iterable. You can pass here any iterable collection and get a iterable collection back. Also works with null
   * 
   * @param it
   *          the iterable collection or null
   * @return the same iterable collection (if <i>it</i> was not null) or an empty iterable collection of the same type
   */
  public static <T> Iterable<T> nullSafe(Iterable<T> it) {
    return it != null ? it : Collections.emptySet();
  }

  /**
   * merges the entries from newItems into the baseList<br />
   * this implementation does adopt items from the baseList to the newItems in the same order<br />
   * - without creating a new list<br />
   * - without touching existing entries (which should not be removed)<br />
   * <br />
   * This implementation only works if .equals() of the type T is implemented in a proper way
   * 
   * @param baseList
   *          the base list which will be altered
   * @param newItems
   *          the new entries which should be merged into the the existing list
   */
  public static <T> void mergeLists(List<T> baseList, List<T> newItems) {
    // if any of these lists is null, we cannot do anything here
    if (baseList == null || newItems == null) {
      return;
    }

    // first remove old ones
    for (int i = baseList.size() - 1; i >= 0; i--) {
      T entry = baseList.get(i);
      if (!newItems.contains(entry)) {
        baseList.remove(entry);
      }
    }

    // second, add new ones in the right order
    for (int i = 0; i < newItems.size(); i++) {
      T entry = newItems.get(i);
      if (!baseList.contains(entry)) {
        try {
          baseList.add(i, entry);
        }
        catch (IndexOutOfBoundsException e) {
          baseList.add(entry);
        }
      }
      else {
        int indexOldList = baseList.indexOf(entry);
        if (i != indexOldList) {
          T oldEntry = baseList.remove(indexOldList);
          try {
            baseList.add(i, oldEntry);
          }
          catch (IndexOutOfBoundsException e) {
            baseList.add(oldEntry);
          }
        }
      }
    }
  }

  /**
   * checks whether a list is null or empty
   * 
   * @param list
   *          the list to check
   * @return true if the list is null or empty
   */
  public static <T> boolean isEmpty(List<T> list) {
    return list == null || list.isEmpty();
  }

  /**
   * checks whether a list contains at least one entry
   *
   * @param list
   *          the list to check
   * @return true if the list is not null and not empty
   */
  public static <T> boolean isNotEmpty(List<T> list) {
    return list != null && !list.isEmpty();
  }

  /**
   * return the provided collection as a sorted list (sorted by default sort algorithm)
   * 
   * @param collection
   *          the collection to sort
   * @param <T>
   *          the type
   * @return a sorted {@link ArrayList} of the given collection
   */
  public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> collection) {
    List<T> list = new ArrayList<>(collection);
    Collections.sort(list);
    return list;
  }

  /**
   * return the provided collection as a sorted list (sorted by the given comparator)
   *
   * @param collection
   *          the collection to sort
   * @param comparator
   *          the comparator to use
   * @param <T>
   *          the type
   * @return a sorted {@link ArrayList} of the given collection
   */
  public static <T> List<T> asSortedList(Collection<T> collection, Comparator<T> comparator) {
    List<T> list = new ArrayList<>(collection);
    list.sort(comparator);
    return list;
  }
}
