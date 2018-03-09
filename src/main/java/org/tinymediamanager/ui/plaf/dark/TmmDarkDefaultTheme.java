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
package org.tinymediamanager.ui.plaf.dark;

import java.awt.Color;

import javax.swing.plaf.ColorUIResource;

import org.tinymediamanager.ui.plaf.TmmTheme;

public class TmmDarkDefaultTheme extends TmmTheme {

  public TmmDarkDefaultTheme() {
    super();
    // Setup theme with defaults
    setUpColor();
    // Overwrite defaults with user props
    loadProperties();
    // Setup the color arrays
    setUpColorArrs();
  }

  @Override
  public String getPropertyFileName() {
    return "TmmDarkTheme.properties";
  }

  @Override
  public void setUpColor() {
    super.setUpColor();

    // Defaults for AbstractLookAndFeel
    backgroundColor = new ColorUIResource(46, 46, 46);
    backgroundColorLight = new ColorUIResource(63, 63, 63);
    backgroundColorDark = new ColorUIResource(35, 35, 35);
    alterBackgroundColor = new ColorUIResource(63, 63, 63);

    foregroundColor = new ColorUIResource(206, 206, 206);
    disabledForegroundColor = new ColorUIResource(157, 157, 157);

    selectionForegroundColor = black;
    selectionBackgroundColor = new ColorUIResource(136, 153, 170);

    frameColor = new ColorUIResource(46, 46, 46);
    focusCellColor = new ColorUIResource(141, 165, 179);
    focusColor = new ColorUIResource(85, 142, 239);

    buttonBackgroundColor = new ColorUIResource(76, 81, 83);
    buttonForegroundColor = foregroundColor;
    buttonColorDark = buttonBackgroundColor;
    buttonColorLight = buttonBackgroundColor;
    pressedForegroundColor = foregroundColor;
    pressedBackgroundColor = buttonBackgroundColor;
    rolloverForegroundColor = foregroundColor;

    inputBackgroundColor = new ColorUIResource(73, 73, 73);
    inputForegroundColor = foregroundColor;

    controlForegroundColor = black;
    controlBackgroundColor = new ColorUIResource(235, 235, 235);
    controlColorLight = white;
    controlColorDark = new ColorUIResource(214, 208, 197);

    windowTitleForegroundColor = white;
    windowTitleBackgroundColor = new ColorUIResource(46, 46, 46);
    windowTitleColorLight = new ColorUIResource(46, 46, 46);
    windowTitleColorDark = new ColorUIResource(46, 46, 46);
    windowBorderColor = new ColorUIResource(41, 41, 41);

    windowInactiveTitleForegroundColor = white;
    windowInactiveTitleBackgroundColor = new ColorUIResource(240, 238, 225); // new ColorUIResource(141, 186, 253);
    windowInactiveTitleColorLight = new ColorUIResource(141, 186, 253);
    windowInactiveTitleColorDark = new ColorUIResource(39, 106, 204);
    windowInactiveBorderColor = new ColorUIResource(39, 106, 204);

    menuForegroundColor = foregroundColor;
    menuBackgroundColor = backgroundColorLight;
    menuSelectionForegroundColor = selectionForegroundColor;
    menuSelectionBackgroundColor = selectionBackgroundColor;
    menuColorLight = new ColorUIResource(248, 247, 241);
    menuColorDark = backgroundColor;

    toolbarBackgroundColor = backgroundColor;
    toolbarColorLight = menuColorLight;
    toolbarColorDark = backgroundColor;

    tabAreaBackgroundColor = new ColorUIResource(41, 41, 41);
    tabSelectionBackgroundColor = selectionBackgroundColor;
    tabSelectionForegroundColor = selectionForegroundColor;
    desktopColor = backgroundColor;

    gridColor = new ColorUIResource(85, 85, 85);
    selectedGridColor = new ColorUIResource(78, 107, 126);

    textAntiAliasingMode = TEXT_ANTIALIAS_DEFAULT;
    textAntiAliasing = true;

    linkForegroundColor = new ColorUIResource(40, 123, 222);
  }

  public void setUpColorArrs() {
    super.setUpColorArrs();

    // needed
    THUMB_COLORS = new Color[] { new Color(32, 32, 32) };
    TRACK_COLORS = new Color[] { alterBackgroundColor };
    SLIDER_COLORS = THUMB_COLORS;
    GRID_COLORS = new Color[] { new Color(85, 85, 85), new Color(46, 46, 46) };
  }
}
