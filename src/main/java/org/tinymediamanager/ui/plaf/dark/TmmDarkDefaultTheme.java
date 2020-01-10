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

import java.awt.Color;

import javax.swing.plaf.ColorUIResource;

import org.tinymediamanager.ui.plaf.TmmTheme;

import com.jtattoo.plaf.AbstractBorderFactory;

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
    backgroundColor = new ColorUIResource(43, 43, 44);
    backgroundColorLight = new ColorUIResource(53, 54, 56);
    backgroundColorDark = new ColorUIResource(35, 35, 35);
    alterBackgroundColor = new ColorUIResource(60, 63, 65);

    foregroundColor = new ColorUIResource(192, 192, 192);
    disabledForegroundColor = foregroundColor;
    disabledBackgroundColor = backgroundColorLight;

    selectionForegroundColor = WHITE;
    selectionBackgroundColor = new ColorUIResource(85, 85, 85);
    // selectionBackgroundColor = new ColorUIResource(119, 153, 187);

    frameColor = new ColorUIResource(46, 46, 46);
    focusCellColor = new ColorUIResource(141, 165, 179);
    focusColor = new ColorUIResource(29, 181, 252);

    buttonBackgroundColor = alterBackgroundColor;
    buttonForegroundColor = foregroundColor;
    buttonColorDark = buttonBackgroundColor;
    buttonColorLight = buttonBackgroundColor;
    pressedForegroundColor = foregroundColor;
    pressedBackgroundColor = buttonBackgroundColor;
    rolloverForegroundColor = foregroundColor;

    inputBackgroundColor = alterBackgroundColor;
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

    windowInactiveTitleForegroundColor = WHITE;
    windowInactiveTitleBackgroundColor = windowTitleBackgroundColor;
    windowInactiveTitleColorLight = windowInactiveTitleBackgroundColor;
    windowInactiveTitleColorDark = windowInactiveTitleBackgroundColor;
    windowInactiveBorderColor = windowBorderColor;

    menuForegroundColor = foregroundColor;
    menuBackgroundColor = backgroundColorLight;
    menuSelectionForegroundColor = selectionForegroundColor;
    menuSelectionBackgroundColor = selectionBackgroundColor;
    menuColorLight = new ColorUIResource(248, 247, 241);
    menuColorDark = backgroundColor;

    toolbarBackgroundColor = backgroundColor;
    toolbarColorLight = menuColorLight;
    toolbarColorDark = backgroundColor;

    // tabAreaBackgroundColor = new ColorUIResource(41, 41, 41);
    tabAreaBackgroundColor = new ColorUIResource(38, 38, 38);
    tabForegroundColor = new ColorUIResource(107, 107, 107);
    tabSelectionBackgroundColor = selectionBackgroundColor;
    tabSelectionForegroundColor = new ColorUIResource(240, 240, 240);
    desktopColor = backgroundColor;

    gridColor = new ColorUIResource(85, 85, 85);
    selectedGridColor = new ColorUIResource(55, 55, 56);

    textAntiAliasingMode = TEXT_ANTIALIAS_HRGB;
    textAntiAliasing = true;

    linkForegroundColor = new ColorUIResource(114, 161, 252);
  }

  public void setUpColorArrs() {
    super.setUpColorArrs();

    // needed
    THUMB_COLORS = new Color[] { new Color(32, 32, 32) };
    TRACK_COLORS = new Color[] { alterBackgroundColor };
    SLIDER_COLORS = THUMB_COLORS;
    GRID_COLORS = new Color[] { new Color(85, 85, 85), new Color(46, 46, 46) };
  }

  @Override
  public AbstractBorderFactory getBorderFactory() {
    return TmmDarkBorderFactory.getInstance();
  }
}
