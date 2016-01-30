/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import com.jtattoo.plaf.AbstractBorderFactory;

import javax.swing.border.Border;

public class TmmLightBorderFactory implements AbstractBorderFactory {
  private static TmmLightBorderFactory instance = null;

  private TmmLightBorderFactory() {
  }

  public static synchronized TmmLightBorderFactory getInstance() {
    if (instance == null) {
      instance = new TmmLightBorderFactory();
    }
    return instance;
  }

  @Override
  public Border getFocusFrameBorder() {
    return TmmLightBorders.getFocusFrameBorder();
  }

  @Override
  public Border getButtonBorder() {
    return TmmLightBorders.getButtonBorder();
  }

  @Override
  public Border getToggleButtonBorder() {
    return TmmLightBorders.getToggleButtonBorder();
  }

  @Override
  public Border getTextBorder() {
    return TmmLightBorders.getTextBorder();
  }

  @Override
  public Border getSpinnerBorder() {
    return TmmLightBorders.getSpinnerBorder();
  }

  @Override
  public Border getTextFieldBorder() {
    return TmmLightBorders.getTextFieldBorder();
  }

  @Override
  public Border getComboBoxBorder() {
    return TmmLightBorders.getComboBoxBorder();
  }

  @Override
  public Border getTableHeaderBorder() {
    return TmmLightBorders.getTableHeaderBorder();
  }

  @Override
  public Border getTableScrollPaneBorder() {
    return TmmLightBorders.getTableScrollPaneBorder();
  }

  @Override
  public Border getScrollPaneBorder() {
    return TmmLightBorders.getScrollPaneBorder();
  }

  @Override
  public Border getTabbedPaneBorder() {
    return TmmLightBorders.getTabbedPaneBorder();
  }

  @Override
  public Border getMenuBarBorder() {
    return TmmLightBorders.getMenuBarBorder();
  }

  @Override
  public Border getMenuItemBorder() {
    return TmmLightBorders.getMenuItemBorder();
  }

  @Override
  public Border getPopupMenuBorder() {
    return TmmLightBorders.getPopupMenuBorder();
  }

  @Override
  public Border getInternalFrameBorder() {
    return TmmLightBorders.getInternalFrameBorder();
  }

  @Override
  public Border getPaletteBorder() {
    return TmmLightBorders.getPaletteBorder();
  }

  @Override
  public Border getToolBarBorder() {
    return TmmLightBorders.getToolBarBorder();
  }

  @Override
  public Border getProgressBarBorder() {
    return TmmLightBorders.getProgressBarBorder();
  }

  @Override
  public Border getDesktopIconBorder() {
    return TmmLightBorders.getDesktopIconBorder();
  }

  public Border getTitledBorder() {
    return TmmLightBorders.getTitledBorder();
  }
}
