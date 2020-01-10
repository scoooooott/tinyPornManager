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

package org.tinymediamanager.ui;

import java.awt.Color;
import java.awt.Insets;

import javax.swing.UIManager;

/**
 * helper class for holding UI relevant constants
 * 
 * @author Manuel Laggner
 */
public class UIConstants {
  /**
   * this is the button margin for small buttons (like add actor, add tag, ..)
   */
  public static final Insets SMALL_BUTTON_MARGIN = new Insets(2, 2, 2, 2);

  public static final Color  LINK_COLOR          = getLinkColor();
  public static final Color  FOCUS_COLOR         = UIManager.getColor("Focus.color");
  public static final Color  FOREGROUND_COLOR    = UIManager.getColor("Label.foreground");
  public static final Color  BACKGROUND_COLOR    = UIManager.getColor("Label.background");

  private static Color getLinkColor() {
    Color linkColor = UIManager.getColor("Link.Foreground");
    if (linkColor == null) {
      // fallback
      linkColor = Color.WHITE; // new Color(40, 123, 222)
    }
    return linkColor;
  }
}
