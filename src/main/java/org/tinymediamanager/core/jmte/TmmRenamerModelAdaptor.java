/*
 * Copyright 2012 - 2018 Manuel Laggner
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

import java.util.List;
import java.util.Map;

import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.token.Token;

/**
 * the class TmmRenamerModelAdaptor is used as a custom ModelAdaptor for JMTE to strip out illegal characters
 *
 * @author Manuel Laggner
 */
public class TmmRenamerModelAdaptor extends TmmModelAdaptor {

  /**
   * replaces all invalid/illegal characters for filenames with ""<br>
   * except the colon, which will be changed to a dash
   *
   * @param source
   *          string to clean
   * @return cleaned string
   */
  public static String replaceInvalidCharacters(String source) {
    source = source.replaceAll(": ", " - "); // nicer
    source = source.replaceAll(":", "-"); // nicer
    return source.replaceAll("([\"\\\\:<>|/?*])", "");
  }

  @Override
  public Object getValue(Map<String, Object> model, String expression) {
    Object value = super.getValue(model, expression);

    if (value != null && value instanceof String) {
      value = replaceInvalidCharacters((String) value);
    }

    return value;
  }

  @Override
  public Object getValue(TemplateContext context, Token token, List<String> segments, String expression) {
    Object value = super.getValue(context, token, segments, expression);

    if (value != null && value instanceof String) {
      value = replaceInvalidCharacters((String) value);
    }

    return value;
  }
}
