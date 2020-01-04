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

import java.util.concurrent.TimeUnit;

import org.jdesktop.beansbinding.Converter;

/**
 * the class {@link RuntimeConverter} is used to display the runtime in a nice way
 *
 * @author Manuel Laggner
 */
public class RuntimeConverter extends Converter<Integer, String> {

  @Override
  public String convertForward(Integer arg0) {
    if (arg0.equals(0)) {
      return "";
    }

    long h = TimeUnit.MINUTES.toHours(arg0);
    long m = TimeUnit.MINUTES.toMinutes(arg0 - TimeUnit.HOURS.toMinutes(h));

    if (h > 0) {
      return String.format("%dh %02dm", h, m);
    }
    else {
      return String.format("%dm", m);
    }
  }

  @Override
  public Integer convertReverse(String s) {
    return null;
  }
}
