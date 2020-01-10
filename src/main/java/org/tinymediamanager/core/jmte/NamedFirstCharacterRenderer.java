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
package org.tinymediamanager.core.jmte;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;

/**
 * this renderer is used to extract the first character of the given field in upper case. if the first character is not a letter, it will result in #
 *
 * @author Manuel Laggner
 */
public class NamedFirstCharacterRenderer implements NamedRenderer {

  @Override
  public String render(Object o, String s, Locale locale, Map<String, Object> map) {
    if (o instanceof String && StringUtils.isNotBlank((String) o)) {
      String first = ((String) o).trim().substring(0, 1);
      if (first.matches("[\\p{L}]")) {
        return first.toUpperCase(Locale.ROOT);
      }
      return "#";
    }
    if (o instanceof Number) {
      return "#";
    }
    if (o instanceof Date) {
      return "#";
    }
    return "";
  }

  @Override
  public String getName() {
    return "first";
  }

  @Override
  public RenderFormatInfo getFormatInfo() {
    return null;
  }

  @Override
  public Class<?>[] getSupportedClasses() {
    return new Class[] { Date.class, String.class, Integer.class, Long.class };
  }
}
