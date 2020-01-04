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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;
import com.floreysoft.jmte.message.ParseException;

/**
 * a named date renderer for exporting date values with JMTE
 * 
 * @author Manuel Laggner
 */
public class NamedDateRenderer implements NamedRenderer {
  private static final Logger LOGGER          = LoggerFactory.getLogger(NamedDateRenderer.class);
  private static final String DEFAULT_PATTERN = "dd.MM.yyyy HH:mm:ss Z";

  private Date convert(Object o, DateFormat dateFormat) {
    if (o instanceof Date) {
      return (Date) o;
    }
    else if (o instanceof Number) {
      long longValue = ((Number) o).longValue();
      return new Date(longValue);
    }
    else if (o instanceof String) {
      try {
        try {
          return dateFormat.parse((String) o);
        }
        catch (java.text.ParseException e) {
          LOGGER.warn("cannot convert date format", e);
        }
      }
      catch (ParseException ignored) {
      }
    }
    return null;
  }

  @Override
  public String getName() {
    return "date";
  }

  @Override
  public Class<?>[] getSupportedClasses() {
    return new Class[] { Date.class, String.class, Integer.class, Long.class };
  }

  @Override
  public String render(Object o, String pattern, Locale locale, Map<String, Object> model) {
    String patternToUse = pattern != null ? pattern : DEFAULT_PATTERN;
    try {
      DateFormat dateFormat = new SimpleDateFormat(patternToUse);
      Date value = convert(o, dateFormat);
      if (value != null) {
        return dateFormat.format(value);
      }
    }
    catch (IllegalArgumentException | NullPointerException ignored) {
    }
    return null;
  }

  @Override
  public RenderFormatInfo getFormatInfo() {
    return null;
  }
}
