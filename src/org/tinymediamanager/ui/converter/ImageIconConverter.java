/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import org.jdesktop.beansbinding.Converter;
import org.tinymediamanager.ui.IconManager;

/**
 * The Class ImageIconConverter.
 * 
 * @author Manuel Laggner
 */
public class ImageIconConverter extends Converter<Object, Object> {
  @Override
  public Object convertForward(Object arg0) {
    if (arg0 instanceof Boolean && arg0 == Boolean.TRUE) {
      return IconManager.CHECKMARK;
    }

    return IconManager.CROSS;
  }

  @Override
  public Object convertReverse(Object arg0) {
    return null;
  }
}