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
package org.tinymediamanager.ui.plaf.dark;

import java.util.Properties;

import org.tinymediamanager.ui.plaf.TmmBorders;
import org.tinymediamanager.ui.plaf.TmmLookAndFeel;

import com.jtattoo.plaf.AbstractBorderFactory;
import com.jtattoo.plaf.AbstractIconFactory;
import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.AbstractTheme;

public class TmmDarkLookAndFeel extends TmmLookAndFeel {
  private static final long          serialVersionUID = 7771660244111173072L;

  private static TmmDarkDefaultTheme myTheme          = null;

  public static void setTheme(String name) {
    setTheme(themesMap.get(name));
    if (myTheme != null) {
      AbstractTheme.setInternalName(name);
    }
  }

  public static void setTheme(String name, String licenseKey, String logoString) {
    Properties props = themesMap.get(name);
    if (props != null) {
      props.put("licenseKey", licenseKey);
      props.put("logoString", logoString);
      setTheme(props);
      if (myTheme != null) {
        AbstractTheme.setInternalName(name);
      }
    }
  }

  public static void setTheme(Properties themesProps) {
    TmmBorders.initDefaults();
    if (myTheme == null) {
      myTheme = new TmmDarkDefaultTheme();
    }
    if ((myTheme != null) && (themesProps != null)) {
      myTheme.setUpColor();
      myTheme.setProperties(themesProps);
      myTheme.setUpColorArrs();
      AbstractLookAndFeel.setTheme(myTheme);
    }
  }

  public static void setCurrentTheme(Properties themesProps) {
    setTheme(themesProps);
  }

  @Override
  public String getName() {
    return "tmmDark";
  }

  @Override
  public String getID() {
    return "tmmDark";
  }

  @Override
  public String getDescription() {
    return "The tinyMediaManager dark Look and Feel";
  }

  @Override
  public AbstractBorderFactory getBorderFactory() {
    return TmmDarkBorderFactory.getInstance();
  }

  @Override
  public AbstractIconFactory getIconFactory() {
    return TmmDarkIconFactory.getInstance();
  }

  @Override
  protected void createDefaultTheme() {
    if (myTheme == null) {
      myTheme = new TmmDarkDefaultTheme();
    }
    setTheme(myTheme);
  }
}
