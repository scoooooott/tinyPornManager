/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jtattoo.plaf.AbstractTheme;

/**
 * The class TmmTheme is the base class for our theme
 * 
 * @author Manuel Laggner
 */
public class TmmTheme extends AbstractTheme {
  private static final Logger LOGGER = LoggerFactory.getLogger(TmmTheme.class);

  public static final String  FONT   = "Dialog";

  static {
    try {
      // InputStream fontStream = TmmTheme.class.getResource("fontawesome-pro-light-300.ttf").openStream();
      InputStream fontStream = TmmTheme.class.getResource("fontawesome-pro-regular-400.ttf").openStream();
      Font materialIconsRegular = Font.createFont(Font.TRUETYPE_FONT, fontStream);
      GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(materialIconsRegular);
      fontStream.close();
    }
    catch (Exception e) {
      LOGGER.error("could not load icon font: " + e.getMessage());
    }
  }
}
