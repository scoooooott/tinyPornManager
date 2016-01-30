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

import javax.swing.Icon;

import com.jtattoo.plaf.AbstractIconFactory;

/**
 * @author Manuel Laggner
 */
public class TmmLightIconFactory implements AbstractIconFactory {

  private static TmmLightIconFactory instance = null;

  private TmmLightIconFactory() {
  }

  public static synchronized TmmLightIconFactory getInstance() {
    if (instance == null) {
      instance = new TmmLightIconFactory();
    }
    return instance;
  }

  @Override
  public Icon getOptionPaneErrorIcon() {
    return TmmLightIcons.getOptionPaneErrorIcon();
  }

  @Override
  public Icon getOptionPaneWarningIcon() {
    return TmmLightIcons.getOptionPaneWarningIcon();
  }

  @Override
  public Icon getOptionPaneInformationIcon() {
    return TmmLightIcons.getOptionPaneInformationIcon();
  }

  @Override
  public Icon getOptionPaneQuestionIcon() {
    return TmmLightIcons.getOptionPaneQuestionIcon();
  }

  @Override
  public Icon getFileChooserDetailViewIcon() {
    return TmmLightIcons.getFileChooserDetailViewIcon();
  }

  @Override
  public Icon getFileChooserHomeFolderIcon() {
    return TmmLightIcons.getFileChooserHomeFolderIcon();
  }

  @Override
  public Icon getFileChooserListViewIcon() {
    return TmmLightIcons.getFileChooserListViewIcon();
  }

  @Override
  public Icon getFileChooserNewFolderIcon() {
    return TmmLightIcons.getFileChooserNewFolderIcon();
  }

  @Override
  public Icon getFileChooserUpFolderIcon() {
    return TmmLightIcons.getFileChooserUpFolderIcon();
  }

  @Override
  public Icon getMenuIcon() {
    return TmmLightIcons.getMenuIcon();
  }

  @Override
  public Icon getIconIcon() {
    return TmmLightIcons.getIconIcon();
  }

  @Override
  public Icon getMaxIcon() {
    return TmmLightIcons.getMaxIcon();
  }

  @Override
  public Icon getMinIcon() {
    return TmmLightIcons.getMinIcon();
  }

  @Override
  public Icon getCloseIcon() {
    return TmmLightIcons.getCloseIcon();
  }

  @Override
  public Icon getPaletteCloseIcon() {
    return TmmLightIcons.getPaletteCloseIcon();
  }

  @Override
  public Icon getRadioButtonIcon() {
    return TmmLightIcons.getRadioButtonIcon();
  }

  @Override
  public Icon getCheckBoxIcon() {
    return TmmLightIcons.getCheckBoxIcon();
  }

  @Override
  public Icon getComboBoxIcon() {
    return TmmLightIcons.getComboBoxIcon();
  }

  @Override
  public Icon getTreeLeafIcon() {
    return TmmLightIcons.getTreeLeafIcon();
  }

  @Override
  public Icon getMenuArrowIcon() {
    return TmmLightIcons.getMenuArrowIcon();
  }

  @Override
  public Icon getMenuCheckBoxIcon() {
    return TmmLightIcons.getMenuCheckBoxIcon();
  }

  @Override
  public Icon getMenuRadioButtonIcon() {
    return TmmLightIcons.getMenuRadioButtonIcon();
  }

  @Override
  public Icon getUpArrowIcon() {
    return TmmLightIcons.getUpArrowIcon();
  }

  @Override
  public Icon getDownArrowIcon() {
    return TmmLightIcons.getDownArrowIcon();
  }

  @Override
  public Icon getLeftArrowIcon() {
    return TmmLightIcons.getLeftArrowIcon();
  }

  @Override
  public Icon getRightArrowIcon() {
    return TmmLightIcons.getRightArrowIcon();
  }

  @Override
  public Icon getSplitterDownArrowIcon() {
    return TmmLightIcons.getSplitterDownArrowIcon();
  }

  @Override
  public Icon getSplitterHorBumpIcon() {
    return TmmLightIcons.getSplitterHorBumpIcon();
  }

  @Override
  public Icon getSplitterLeftArrowIcon() {
    return TmmLightIcons.getSplitterLeftArrowIcon();
  }

  @Override
  public Icon getSplitterRightArrowIcon() {
    return TmmLightIcons.getSplitterRightArrowIcon();
  }

  @Override
  public Icon getSplitterUpArrowIcon() {
    return TmmLightIcons.getSplitterUpArrowIcon();
  }

  @Override
  public Icon getSplitterVerBumpIcon() {
    return TmmLightIcons.getSplitterVerBumpIcon();
  }

  @Override
  public Icon getThumbHorIcon() {
    return TmmLightIcons.getThumbHorIcon();
  }

  @Override
  public Icon getThumbVerIcon() {
    return TmmLightIcons.getThumbVerIcon();
  }

  @Override
  public Icon getThumbHorIconRollover() {
    return TmmLightIcons.getThumbHorIconRollover();
  }

  @Override
  public Icon getThumbVerIconRollover() {
    return TmmLightIcons.getThumbVerIconRollover();
  }

  @Override
  public Icon getFileViewComputerIcon() {
    return TmmLightIcons.getFileViewComputerIcon();
  }

  @Override
  public Icon getFileViewFloppyDriveIcon() {
    return TmmLightIcons.getFileViewFloppyDriveIcon();
  }

  @Override
  public Icon getFileViewHardDriveIcon() {
    return TmmLightIcons.getFileViewHardDriveIcon();
  }

  @Override
  public Icon getTreeCloseIcon() {
    return TmmLightIcons.getTreeClosedIcon();
  }

  @Override
  public Icon getTreeOpenIcon() {
    return TmmLightIcons.getTreeOpenedIcon();
  }

  @Override
  public Icon getTreeCollapsedIcon() {
    return TmmLightIcons.getTreeCollapsedIcon();
  }

  @Override
  public Icon getTreeExpandedIcon() {
    return TmmLightIcons.getTreeExpandedIcon();
  }
}
