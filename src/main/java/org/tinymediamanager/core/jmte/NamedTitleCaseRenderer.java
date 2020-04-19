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

import org.apache.commons.text.WordUtils;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;

/**
 * this renderer is used to render strings in lower case
 * 
 * @author Manuel Laggner
 */
public class NamedTitleCaseRenderer implements NamedRenderer {
  @Override
  public String render(Object o, String s, Locale locale, Map<String, Object> map) {
    if (o == null) {
      return "";
    }

    return WordUtils.capitalizeFully(o.toString());
  }

  @Override
  public String getName() {
    return "title";
  }

  @Override
  public RenderFormatInfo getFormatInfo() {
    return null;
  }

  @Override
  public Class<?>[] getSupportedClasses() {
    return new Class[] { String.class };
  }
}
