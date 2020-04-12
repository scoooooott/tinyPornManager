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

import java.util.Locale;
import java.util.Map;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;

/**
 * this renderer is used to render the file size in the given form
 * 
 * @author Manuel Laggner
 */
public class NamedFilesizeRenderer implements NamedRenderer {
  @Override
  public String render(Object o, String s, Locale locale, Map<String, Object> map) {
    // part 1 is the string format (default = %.2f)
    String format = "%.2f";

    // part 2 is the unit (default = G)
    float value = 0;
    if (o instanceof Number) {
      value = ((Number) o).longValue() / (1000 * 1000 * 1000f);
    }
    format += " G";

    try {
      return String.format(format, value);
    }
    catch (Exception e) {
      return "";
    }
  }

  @Override
  public String getName() {
    return "filesize";
  }

  @Override
  public RenderFormatInfo getFormatInfo() {
    return null;
  }

  @Override
  public Class<?>[] getSupportedClasses() {
    return new Class[] { Integer.class, Float.class, Double.class };
  }
}
