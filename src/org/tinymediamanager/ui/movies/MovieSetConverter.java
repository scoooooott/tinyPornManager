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

import org.jdesktop.beansbinding.Converter;
import org.tinymediamanager.core.movie.MovieSet;

/**
 * The Class ImageIconConverter.
 * 
 * @author Manuel Laggner
 */
public class MovieSetConverter extends Converter<MovieSet, String> {

  /*
   * (non-Javadoc)
   * 
   * @see org.jdesktop.beansbinding.Converter#convertForward(java.lang.Object)
   */
  @Override
  public String convertForward(MovieSet arg0) {
    if (arg0 == null) {
      return "";
    }
    return arg0.getTitle();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jdesktop.beansbinding.Converter#convertReverse(java.lang.Object)
   */
  @Override
  public MovieSet convertReverse(String arg0) {
    return null;
  }

}
