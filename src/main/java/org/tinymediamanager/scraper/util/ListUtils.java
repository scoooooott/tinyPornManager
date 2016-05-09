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
package org.tinymediamanager.scraper.util;

import java.util.Collections;

/**
 * The class ListUtils is a helper class, providing some special functions for Lists
 * 
 * @author Manuel Laggner
 * @since 1.0
 */
public class ListUtils {

  /**
   * Get a nullsafe Iterable. You can pass here any iterable collection and get a iterable collection back. Also works with null
   * 
   * @param it
   *          the iterable collection or null
   * @return the same iterable collection (if <i>it</i> was not null) or an empty iterable collection of the same type
   */
  public static <T> Iterable<T> nullSafe(Iterable<T> it) {
    return it != null ? it : Collections.<T> emptySet();
  }
}
