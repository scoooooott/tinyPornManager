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
package org.tinymediamanager.ui.plaf.light;

import java.awt.Color;

import javax.swing.plaf.ColorUIResource;

import org.tinymediamanager.ui.plaf.TmmTheme;

import com.jtattoo.plaf.AbstractBorderFactory;

public class TmmLightDefaultTheme extends TmmTheme {

  public TmmLightDefaultTheme() {
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
    return "TmmLightTheme.properties";
  }

  @Override
  public void setUpColor() {
    super.setUpColor();

    // Defaults for AbstractLookAndFeel
    backgroundColor = new ColorUIResource(235, 235, 235);
    backgroundColorLight = new ColorUIResource(235, 235, 235);
    backgroundColorDark = new ColorUIResource(208, 208, 208);
    alterBackgroundColor = new ColorUIResource(213, 213, 213);

    foregroundColor = new ColorUIResource(107, 107, 107);
    disabledForegroundColor = new ColorUIResource(157, 157, 157);

    selectionForegroundColor = BLACK;
    selectionBackgroundColor = new ColorUIResource(197, 207, 213);

    frameColor = new ColorUIResource(46, 46, 46);
    // focusCellColor = new ColorUIResource(0, 60, 116);
    focusCellColor = new ColorUIResource(141, 165, 179);
    focusColor = new ColorUIResource(85, 142, 239);

    buttonBackgroundColor = new ColorUIResource(76, 76, 76);
    buttonForegroundColor = new ColorUIResource(204, 204, 204);
    buttonColorDark = new ColorUIResource(141, 165, 179);
    buttonColorLight = new ColorUIResource(141, 165, 179);
    pressedForegroundColor = new ColorUIResource(Color.white);
    pressedBackgroundColor = new ColorUIResource(141, 165, 179);
    rolloverForegroundColor = new ColorUIResource(204, 204, 204);

    // inputBackgroundColor = alterBackgroundColor;
    inputForegroundColor = foregroundColor;

    controlForegroundColor = BLACK;
    controlBackgroundColor = alterBackgroundColor;
    controlColorLight = WHITE;
    controlColorDark = new ColorUIResource(214, 208, 197);

    windowTitleForegroundColor = WHITE;
    windowTitleBackgroundColor = new ColorUIResource(46, 46, 46);
    windowTitleColorLight = new ColorUIResource(46, 46, 46);
    windowTitleColorDark = new ColorUIResource(46, 46, 46);
    windowBorderColor = new ColorUIResource(41, 41, 41);

    windowInactiveTitleForegroundColor = windowTitleForegroundColor;
    windowInactiveTitleBackgroundColor = windowTitleBackgroundColor;
    windowInactiveTitleColorLight = windowInactiveTitleBackgroundColor;
    windowInactiveTitleColorDark = windowInactiveTitleBackgroundColor;
    windowInactiveBorderColor = windowBorderColor;

    menuBackgroundColor = WHITE;
    menuSelectionForegroundColor = WHITE;
    menuSelectionBackgroundColor = selectionBackgroundColor;
    menuColorLight = new ColorUIResource(248, 247, 241);
    menuColorDark = backgroundColor;

    toolbarBackgroundColor = backgroundColor;
    toolbarColorLight = menuColorLight;
    toolbarColorDark = backgroundColor;

    tabAreaBackgroundColor = new ColorUIResource(41, 41, 41);
    tabForegroundColor = foregroundColor;
    tabSelectionBackgroundColor = new ColorUIResource(141, 165, 179);
    tabSelectionForegroundColor = new ColorUIResource(Color.white);
    desktopColor = backgroundColor;

    gridColor = new ColorUIResource(206, 206, 206);
    selectedGridColor = new ColorUIResource(78, 107, 126);

    textAntiAliasingMode = TEXT_ANTIALIAS_HRGB;
    textAntiAliasing = true;

    linkForegroundColor = new ColorUIResource(40, 123, 222);
  }

  public void setUpColorArrs() {
    super.setUpColorArrs();

    // needed
    THUMB_COLORS = new Color[] { new Color(131, 131, 131) };
    TRACK_COLORS = new Color[] { new Color(255, 255, 255) };
    SLIDER_COLORS = THUMB_COLORS;
    GRID_COLORS = new Color[] { new Color(206, 206, 206), new Color(248, 248, 248) };
  }

  @Override
  public AbstractBorderFactory getBorderFactory() {
    return TmmLightBorderFactory.getInstance();
  }
}
