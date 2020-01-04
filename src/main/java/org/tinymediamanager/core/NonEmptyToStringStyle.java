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

package org.tinymediamanager.core;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * A ToStringStyle, which displays only filled values<br>
 * skipping, NULL, empty lists/maps/strings, and negative numbers... (set-able)
 * 
 * @author Myron Boyle
 *
 */
public class NonEmptyToStringStyle extends ToStringStyle {

  private static final long serialVersionUID     = 1L;
  private boolean           ignoreNegativeNumber = true;
  private boolean           ignoreEmptyString    = true; // won't trim()

  public NonEmptyToStringStyle() {
    super();
    this.setUseShortClassName(true);
    this.setUseIdentityHashCode(false);
  }

  @Override
  public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {
    if (value != null) {
      // if it looks stupid, but works - it ain't stupid :p
      if (value instanceof Collection) {
        if (((Collection<?>) value).isEmpty()) {
          return;
        }
      }
      if (value instanceof Map) {
        if (((Map<?, ?>) value).isEmpty()) {
          return;
        }
      }
      if (ignoreNegativeNumber && value instanceof Number) {
        if (((Number) value).intValue() < 0) {
          return;
        }
      }
      if (ignoreEmptyString && value instanceof String) {
        if (((String) value).isEmpty()) {
          return;
        }
      }

      super.append(buffer, fieldName, value, fullDetail);
    }
  }

  public void setIgnoreNegativeNumber(boolean ignoreNegativeNumber) {
    this.ignoreNegativeNumber = ignoreNegativeNumber;
  }

  public void setIgnoreEmptyString(boolean ignoreEmptyString) {
    this.ignoreEmptyString = ignoreEmptyString;
  }
}
