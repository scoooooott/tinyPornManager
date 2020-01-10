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
package org.tinymediamanager.ui.converter;

import java.util.Locale;

import org.jdesktop.beansbinding.Converter;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaRating;

/**
 * the class {@link RatingConverter} is used to display the rating with the max value
 *
 * @author Manuel Laggner
 */
public class RatingConverter<T extends MediaEntity> extends Converter<T, String> {
  private Locale locale = Locale.getDefault();

  @Override
  public String convertForward(T arg0) {
    if (arg0 != null) {
      MediaRating rating = arg0.getRating();
      return String.format(locale, "%.1f / %,d", rating.getRating(), rating.getMaxValue());
    }
    return "";
  }

  @Override
  public T convertReverse(String arg0) {
    return null;
  }
}
