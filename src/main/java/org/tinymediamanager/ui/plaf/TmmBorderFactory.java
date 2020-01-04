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

import javax.swing.border.Border;

import com.jtattoo.plaf.AbstractBorderFactory;

public abstract class TmmBorderFactory implements AbstractBorderFactory {

  @Override
  public Border getFocusFrameBorder() {
    return TmmBorders.getFocusFrameBorder();
  }

  @Override
  public Border getButtonBorder() {
    return TmmBorders.getButtonBorder();
  }

  @Override
  public Border getToggleButtonBorder() {
    return TmmBorders.getToggleButtonBorder();
  }

  @Override
  public Border getTextBorder() {
    return TmmBorders.getTextBorder();
  }

  @Override
  public Border getSpinnerBorder() {
    return TmmBorders.getSpinnerBorder();
  }

  @Override
  public Border getTextFieldBorder() {
    return TmmBorders.getTextFieldBorder();
  }

  @Override
  public Border getComboBoxBorder() {
    return TmmBorders.getComboBoxBorder();
  }

  @Override
  public Border getTableHeaderBorder() {
    return TmmBorders.getTableHeaderBorder();
  }

  @Override
  public Border getTableScrollPaneBorder() {
    return TmmBorders.getTableScrollPaneBorder();
  }

  @Override
  public Border getScrollPaneBorder() {
    return TmmBorders.getScrollPaneBorder();
  }

  @Override
  public Border getTabbedPaneBorder() {
    return TmmBorders.getTabbedPaneBorder();
  }

  @Override
  public Border getMenuBarBorder() {
    return TmmBorders.getMenuBarBorder();
  }

  @Override
  public Border getMenuItemBorder() {
    return TmmBorders.getMenuItemBorder();
  }

  @Override
  public Border getPopupMenuBorder() {
    return TmmBorders.getPopupMenuBorder();
  }

  @Override
  public Border getInternalFrameBorder() {
    return TmmBorders.getInternalFrameBorder();
  }

  @Override
  public Border getPaletteBorder() {
    return TmmBorders.getPaletteBorder();
  }

  @Override
  public Border getToolBarBorder() {
    return TmmBorders.getToolBarBorder();
  }

  @Override
  public Border getProgressBarBorder() {
    return TmmBorders.getProgressBarBorder();
  }

  @Override
  public Border getDesktopIconBorder() {
    return TmmBorders.getDesktopIconBorder();
  }

  public Border getTitledBorder() {
    return TmmBorders.getTitledBorder();
  }

  public Border getTreeNodeBorder() {
    return TmmBorders.getTreeNodeBorder();
  }

}
