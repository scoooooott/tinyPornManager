/*
 * Copyright 2012 - 2014 Manuel Laggner
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
package org.tinymediamanager.ui;

import javax.swing.Icon;

import org.jdesktop.beansbinding.Converter;

/**
 * The Class WatchedIconConverter2. To Display the unwatched icon
 * 
 * @author Manuel Laggner
 */
public class WatchedIconConverter2 extends Converter<Boolean, Icon> {

  @Override
  public Icon convertForward(Boolean arg0) {
    if (arg0.equals(Boolean.TRUE)) {
      return null;
    }

    return IconManager.UNWATCHED;
  }

  @Override
  public Boolean convertReverse(Icon arg0) {
    return null;
  }
}