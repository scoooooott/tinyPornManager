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
package org.tinymediamanager.ui.plaf;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

import com.jtattoo.plaf.AbstractTheme;

/**
 * The class TmmTheme is the base class for our theme
 *
 * @author Manuel Laggner
 */
abstract public class TmmTheme extends AbstractTheme {
  public static final String FONT         = "Dialog";

  public static final Font   FONT_AWESOME = loadFontAwesome();

  static {
    try (InputStream fsRegular = TmmTheme.class.getResource("DejaVuSans.ttf").openStream();
        InputStream fsMono = TmmTheme.class.getResource("DejaVuSansMono.ttf").openStream()) {
      Font dejavuRegular = Font.createFont(Font.TRUETYPE_FONT, fsRegular);
      GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(dejavuRegular);

      Font dejavuMono = Font.createFont(Font.TRUETYPE_FONT, fsMono);
      GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(dejavuMono);
    }
    catch (Exception ignored) {
      // nothing to be done here
    }
  }

  private static Font loadFontAwesome() {
    // force font awesome to be loaded from the laf and not the system
    try (InputStream fsAwesome = TmmTheme.class.getResource("fontawesome-pro-regular-400.ttf").openStream()) {
      return Font.createFont(Font.TRUETYPE_FONT, fsAwesome);
    }
    catch (Exception ignored) {
      // nothing to be done here
    }
    return null;
  }
}
