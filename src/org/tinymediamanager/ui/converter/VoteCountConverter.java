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

import java.util.Locale;

import org.jdesktop.beansbinding.Converter;

/**
 * The Class VoteCountConverter.
 * 
 * @author Manuel Laggner
 */
public class VoteCountConverter extends Converter<Integer, String> {

  /** The locale. */
  private Locale locale = Locale.getDefault();

  /*
   * (non-Javadoc)
   * 
   * @see org.jdesktop.beansbinding.Converter#convertForward(java.lang.Object)
   */
  @Override
  public String convertForward(Integer arg0) {
    if (arg0 instanceof Integer) {
      StringBuilder sb = new StringBuilder("(");
      sb.append(String.format(locale, "%,d", arg0));
      sb.append(" votes)");
      return sb.toString();
    }
    return "";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jdesktop.beansbinding.Converter#convertReverse(java.lang.Object)
   */
  @Override
  public Integer convertReverse(String arg0) {
    return null;
  }

}
