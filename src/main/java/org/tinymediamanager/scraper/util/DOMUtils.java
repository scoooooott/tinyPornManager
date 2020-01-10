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
package org.tinymediamanager.scraper.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The class DOMUtils. This class is used for several DOM related tasks
 * 
 * @author Manuel Laggner
 * @since 1.0
 */
public class DOMUtils {

  private DOMUtils() {
    // hide the public constructor for utility classes
  }

  /**
   * Gets the element value.
   * 
   * @param el
   *          the el
   * @param tag
   *          the tag
   * @return the element value
   */
  public static String getElementValue(Element el, String tag) {
    if (el == null || tag == null) {
      return null;
    }
    NodeList nl = el.getElementsByTagName(tag);
    if (nl.getLength() > 0) {
      Node n = nl.item(0);
      return StringUtils.trim(n.getTextContent());
    }
    return null;
  }

  /**
   * Gets the element int value.
   * 
   * @param el
   *          the el
   * @param tag
   *          the tag
   * @return the element int value
   */
  public static int getElementIntValue(Element el, String tag) {
    NodeList nl = el.getElementsByTagName(tag);
    if (nl.getLength() > 0) {
      Node n = nl.item(0);
      return NumberUtils.toInt(StringUtils.trim(n.getTextContent()));
    }
    return 0;
  }

  /**
   * Gets the max element value.
   * 
   * @param el
   *          the el
   * @param tag
   *          the tag
   * @return the max element value
   */
  public static String getMaxElementValue(Element el, String tag) {
    NodeList nl = el.getElementsByTagName(tag);
    String retVal = null;
    for (int i = 0; i < nl.getLength(); i++) {
      String s = nl.item(i).getTextContent();
      if (retVal == null) {
        retVal = s;
      }
      else {
        if (s != null && s.length() > retVal.length()) {
          retVal = s;
        }
      }
    }
    return retVal;
  }

  /**
   * Gets the element by tag name.
   * 
   * @param el
   *          the el
   * @param tag
   *          the tag
   * @return the element by tag name
   */
  public static Element getElementByTagName(Element el, String tag) {
    NodeList nl = el.getElementsByTagName(tag);
    if (nl.getLength() > 0) {
      return (Element) nl.item(0);
    }
    return null;
  }
}
