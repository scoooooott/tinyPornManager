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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.UIDefaults;

import org.tinymediamanager.ui.plaf.dark.TmmDarkBorderFactory;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseCheckBoxMenuItemUI;
import com.jtattoo.plaf.BaseDesktopPaneUI;
import com.jtattoo.plaf.BaseFileChooserUI;
import com.jtattoo.plaf.BaseFormattedTextFieldUI;
import com.jtattoo.plaf.BaseMenuBarUI;
import com.jtattoo.plaf.BaseMenuItemUI;
import com.jtattoo.plaf.BaseMenuUI;
import com.jtattoo.plaf.BasePopupMenuUI;
import com.jtattoo.plaf.BaseRadioButtonMenuItemUI;
import com.jtattoo.plaf.BaseRootPaneUI;
import com.jtattoo.plaf.BaseSeparatorUI;
import com.jtattoo.plaf.BaseSliderUI;

public abstract class TmmLookAndFeel extends AbstractLookAndFeel {

  protected static final List<String>            themesList     = new ArrayList<>();
  protected static final Map<String, Properties> themesMap      = new HashMap<>();
  protected static final Properties              defaultProps   = new Properties();
  protected static final Properties              smallFontProps = new Properties();
  protected static final Properties              largeFontProps = new Properties();
  protected static final Properties              giantFontProps = new Properties();

  static {
    smallFontProps.setProperty("controlTextFont", TmmTheme.FONT + " 10");
    smallFontProps.setProperty("systemTextFont", TmmTheme.FONT + " 10");
    smallFontProps.setProperty("userTextFont", TmmTheme.FONT + " 10");
    smallFontProps.setProperty("menuTextFont", TmmTheme.FONT + " 10");
    smallFontProps.setProperty("windowTitleFont", TmmTheme.FONT + " bold 10");
    smallFontProps.setProperty("subTextFont", TmmTheme.FONT + " 8");

    largeFontProps.setProperty("controlTextFont", TmmTheme.FONT + " 14");
    largeFontProps.setProperty("systemTextFont", TmmTheme.FONT + " 14");
    largeFontProps.setProperty("userTextFont", TmmTheme.FONT + " 14");
    largeFontProps.setProperty("menuTextFont", TmmTheme.FONT + " 14");
    largeFontProps.setProperty("windowTitleFont", TmmTheme.FONT + " bold 14");
    largeFontProps.setProperty("subTextFont", TmmTheme.FONT + " 12");

    giantFontProps.setProperty("controlTextFont", TmmTheme.FONT + " 18");
    giantFontProps.setProperty("systemTextFont", TmmTheme.FONT + " 18");
    giantFontProps.setProperty("userTextFont", TmmTheme.FONT + " 18");
    giantFontProps.setProperty("menuTextFont", TmmTheme.FONT + " 18");
    giantFontProps.setProperty("windowTitleFont", TmmTheme.FONT + " 18");
    giantFontProps.setProperty("subTextFont", TmmTheme.FONT + " 16");

    themesList.add("Default");
    themesList.add("Small-Font");
    themesList.add("Large-Font");
    themesList.add("Giant-Font");

    themesMap.put("Default", defaultProps);
    themesMap.put("Small-Font", smallFontProps);
    themesMap.put("Large-Font", largeFontProps);
    themesMap.put("Giant-Font", giantFontProps);
  }

  public static List<String> getThemes() {
    return themesList;
  }

  public static Properties getThemeProperties(String name) {
    return themesMap.get(name);
  }

  @Override
  public boolean isNativeLookAndFeel() {
    return false;
  }

  @Override
  public boolean isSupportedLookAndFeel() {
    return true;
  }

  @Override
  protected void initClassDefaults(UIDefaults table) {
    super.initClassDefaults(table);
    // @formatter:off
    Object[] uiDefaults = {
            // BaseLookAndFeel classes
            "SeparatorUI", BaseSeparatorUI.class.getName(),
            "SliderUI", BaseSliderUI.class.getName(),
            "FileChooserUI", BaseFileChooserUI.class.getName(),
            "MenuBarUI", BaseMenuBarUI.class.getName(),
            "MenuUI", BaseMenuUI.class.getName(),
            "PopupMenuUI", BasePopupMenuUI.class.getName(),
            "MenuItemUI", BaseMenuItemUI.class.getName(),
            "CheckBoxMenuItemUI", BaseCheckBoxMenuItemUI.class.getName(),
            "RadioButtonMenuItemUI", BaseRadioButtonMenuItemUI.class.getName(),
            "PopupMenuSeparatorUI", BaseSeparatorUI.class.getName(),
            "DesktopPaneUI", BaseDesktopPaneUI.class.getName(),
            "RootPaneUI", BaseRootPaneUI.class.getName(),

            // TmmLookAndFeel classes
            "LabelUI", TmmLabelUI.class.getName(),
            "PanelUI", TmmPanelUI.class.getName(),
            "ScrollBarUI", TmmScrollBarUI.class.getName(),
            "TabbedPaneUI", TmmTabbedPaneUI.class.getName(),
            "TableUI", TmmTableUI.class.getName(),
            "ButtonUI", TmmButtonUI.class.getName(),
            "ToggleButtonUI", TmmToggleButtonUI.class.getName(),
            "ComboBoxUI", TmmComboBoxUI.class.getName(),
            "TreeUI", TmmTreeUI.class.getName(),
            "ToolBarUI", TmmToolBarUI.class.getName(),
            "TextFieldUI", TmmTextFieldUI.class.getName(),
            "PasswordFieldUI", TmmPasswordFieldUI.class.getName(),
            "CheckBoxUI", TmmCheckBoxUI.class.getName(),
            "RadioButtonUI", TmmRadioButtonUI.class.getName(),
            "TextAreaUI", TmmTextAreaUI.class.getName(),
            "EditorPaneUI", TmmEditorPaneUI.class.getName(),
            "TextPaneUI", TmmTextPaneUI.class.getName(),
            "ScrollPaneUI", TmmScrollPaneUI.class.getName(),
            "ProgressBarUI", TmmProgressBarUI.class.getName(),
            "SliderUI", TmmSliderUI.class.getName(),
            "SplitPaneUI", TmmSplitPaneUI.class.getName(),
            "FormattedTextFieldUI", BaseFormattedTextFieldUI.class.getName(),
            "SpinnerUI", TmmSpinnerUI.class.getName(),
            "ViewportUI", TmmViewportUI.class.getName(),
            "ToolTipUI", TmmToolTipUI.class.getName(),
    };
    table.putDefaults(uiDefaults);
    // @formatter:on
  }

  @Override
  protected void initComponentDefaults(UIDefaults table) {
    super.initComponentDefaults(table);
    table.put("ScrollBar.incrementButtonGap", -1);
    table.put("ScrollBar.decrementButtonGap", -1);
    table.put("CheckBox.icon", getIconFactory().getCheckBoxIcon());
    table.put("TextPane.foreground", getForegroundColor());
    table.put("OptionPane.foreground", getForegroundColor());
    table.put("OptionPane.messageForeground", getForegroundColor());
    table.put("TitledBorder.border", getTheme().getBorderFactory().getTitledBorder());
    table.put("Table.scrollPaneBorder", TmmDarkBorderFactory.getInstance().getScrollPaneBorder());
    table.put("TableHeader.foreground", getForegroundColor());
    table.put("Table.foreground", getForegroundColor());
    table.put("ProgressBar.border", null);
    table.put("ProgressBar.background", getTheme().getBackgroundColorDark());
    table.put("ProgressBar.foreground", getFocusCellColor());
    table.put("TriStateCheckBox.icon", getIconFactory().getCheckBoxIcon());
    table.put("FormattedTextField.foreground", getForegroundColor());
    table.put("TextField.caretForeground", getForegroundColor());
    table.put("FormattedTextField.caretForeground", getForegroundColor());
    table.put("EditorPane.caretForeground", getForegroundColor());
    table.put("PasswordField.caretForeground", getForegroundColor());
    table.put("TextArea.caretForeground", getForegroundColor());
    table.put("Link.Foreground", getTheme().getLinkForegroundColor());
    table.put("Tree.nodeBorder", getTheme().getBorderFactory().getTreeNodeBorder());

    // put some spacing between grid and cell content
    table.put("Table.cellNoFocusBorder", BorderFactory.createEmptyBorder(1, 3, 1, 3));
    table.put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder(1, 3, 1, 3));
  }
}
