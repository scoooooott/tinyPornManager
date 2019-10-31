/*
 * Copyright 2012 - 2019 Manuel Laggner
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
package org.tinymediamanager.ui.converter;

import java.util.Locale;

import org.jdesktop.beansbinding.Converter;
import org.tinymediamanager.core.entities.MediaRating;

/**
 * the class {@link RatingConverter} is used to display the rating with the max value
 *
 * @author Manuel Laggner
 */
public class RatingConverter extends Converter<MediaRating, String> {
  private Locale locale = Locale.getDefault();

  @Override
  public String convertForward(MediaRating arg0) {
    if (arg0 != null) {
      return String.format(locale, "%.1f / %,d", arg0.getRating(), arg0.getMaxValue());
    }
    return "";
  }

  @Override
  public MediaRating convertReverse(String arg0) {
    return null;
  }
}
