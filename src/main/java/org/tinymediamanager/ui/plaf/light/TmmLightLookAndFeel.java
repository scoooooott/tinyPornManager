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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.UIDefaults;

import com.jtattoo.plaf.AbstractBorderFactory;
import com.jtattoo.plaf.AbstractIconFactory;
import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.AbstractTheme;
import com.jtattoo.plaf.BaseCheckBoxMenuItemUI;
import com.jtattoo.plaf.BaseDesktopPaneUI;
import com.jtattoo.plaf.BaseFileChooserUI;
import com.jtattoo.plaf.BaseFormattedTextFieldUI;
import com.jtattoo.plaf.BaseLabelUI;
import com.jtattoo.plaf.BaseMenuBarUI;
import com.jtattoo.plaf.BaseMenuItemUI;
import com.jtattoo.plaf.BaseMenuUI;
import com.jtattoo.plaf.BasePopupMenuUI;
import com.jtattoo.plaf.BaseRadioButtonMenuItemUI;
import com.jtattoo.plaf.BaseRootPaneUI;
import com.jtattoo.plaf.BaseSeparatorUI;
import com.jtattoo.plaf.BaseSliderUI;
import com.jtattoo.plaf.BaseSplitPaneUI;
import com.jtattoo.plaf.BaseToolTipUI;
import com.jtattoo.plaf.JTattooUtilities;

public class TmmLightLookAndFeel extends AbstractLookAndFeel {

  private static final long                    serialVersionUID = 7771660244511173072L;

  private static TmmLightDefaultTheme          myTheme          = null;

  private static final List<String>            themesList       = new ArrayList<>();
  private static final Map<String, Properties> themesMap        = new HashMap<>();
  private static final Properties              defaultProps     = new Properties();
  private static final Properties              smallFontProps   = new Properties();
  private static final Properties              largeFontProps   = new Properties();
  private static final Properties              giantFontProps   = new Properties();

  static {
    smallFontProps.setProperty("controlTextFont", TmmLightDefaultTheme.FONT + " 10");
    smallFontProps.setProperty("systemTextFont", TmmLightDefaultTheme.FONT + " 10");
    smallFontProps.setProperty("userTextFont", TmmLightDefaultTheme.FONT + " 10");
    smallFontProps.setProperty("menuTextFont", TmmLightDefaultTheme.FONT + " 10");
    smallFontProps.setProperty("windowTitleFont", TmmLightDefaultTheme.FONT + " bold 10");
    smallFontProps.setProperty("subTextFont", TmmLightDefaultTheme.FONT + " 8");

    largeFontProps.setProperty("controlTextFont", TmmLightDefaultTheme.FONT + " 14");
    largeFontProps.setProperty("systemTextFont", TmmLightDefaultTheme.FONT + " 14");
    largeFontProps.setProperty("userTextFont", TmmLightDefaultTheme.FONT + " 14");
    largeFontProps.setProperty("menuTextFont", TmmLightDefaultTheme.FONT + " 14");
    largeFontProps.setProperty("windowTitleFont", TmmLightDefaultTheme.FONT + " bold 14");
    largeFontProps.setProperty("subTextFont", TmmLightDefaultTheme.FONT + " 12");

    giantFontProps.setProperty("controlTextFont", TmmLightDefaultTheme.FONT + " 18");
    giantFontProps.setProperty("systemTextFont", TmmLightDefaultTheme.FONT + " 18");
    giantFontProps.setProperty("userTextFont", TmmLightDefaultTheme.FONT + " 18");
    giantFontProps.setProperty("menuTextFont", TmmLightDefaultTheme.FONT + " 18");
    giantFontProps.setProperty("windowTitleFont", TmmLightDefaultTheme.FONT + " 18");
    giantFontProps.setProperty("subTextFont", TmmLightDefaultTheme.FONT + " 16");

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
    return ((Properties) themesMap.get(name));
  }

  public static void setTheme(String name) {
    setTheme((Properties) themesMap.get(name));
    if (myTheme != null) {
      AbstractTheme.setInternalName(name);
    }
  }

  public static void setTheme(String name, String licenseKey, String logoString) {
    Properties props = (Properties) themesMap.get(name);
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
    if (myTheme == null) {
      myTheme = new TmmLightDefaultTheme();
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
    return "tmmLight";
  }

  @Override
  public String getID() {
    return "tmmLight";
  }

  @Override
  public String getDescription() {
    return "The tinyMediaManager light Look and Feel";
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
  public AbstractBorderFactory getBorderFactory() {
    return TmmLightBorderFactory.getInstance();
  }

  @Override
  public AbstractIconFactory getIconFactory() {
    return TmmLightIconFactory.getInstance();
  }

  @Override
  protected void createDefaultTheme() {
    if (myTheme == null) {
      myTheme = new TmmLightDefaultTheme();
    }
    setTheme(myTheme);
  }

  @Override
  protected void initComponentDefaults(UIDefaults table) {
    super.initComponentDefaults(table);
    table.put("ScrollBar.incrementButtonGap", new Integer(-1));
    table.put("ScrollBar.decrementButtonGap", new Integer(-1));
    table.put("CheckBox.icon", getIconFactory().getCheckBoxIcon());
    table.put("Tree.textForeground", getSelectionForegroundColor());
    table.put("TextPane.foreground", getForegroundColor());
    table.put("TitledBorder.border", TmmLightBorderFactory.getInstance().getTitledBorder());
    table.put("Table.scrollPaneBorder", TmmLightBorderFactory.getInstance().getScrollPaneBorder());
    table.put("Tree.textBackground", getBackgroundColor());
    table.put("ProgressBar.border", null);
    table.put("ProgressBar.background", getTheme().getBackgroundColorDark());
    table.put("ProgressBar.foreground", getFocusCellColor());

    // table.put("Table.foreground", getForegroundColor());
  }

  @Override
  protected void initClassDefaults(UIDefaults table) {
    super.initClassDefaults(table);
    // @formatter:off
    Object[] uiDefaults = {
            // BaseLookAndFeel classes
            "LabelUI", BaseLabelUI.class.getName(),
            "SeparatorUI", BaseSeparatorUI.class.getName(),
            "ToolTipUI", BaseToolTipUI.class.getName(),
            "SliderUI", BaseSliderUI.class.getName(),
            "SplitPaneUI", BaseSplitPaneUI.class.getName(),
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
            "PanelUI", TmmLightPanelUI.class.getName(),
            "ScrollBarUI", TmmLightScrollBarUI.class.getName(),
            "TabbedPaneUI", TmmLightTabbedPaneUI.class.getName(),
            "TableUI", TmmLightTableUI.class.getName(),
            "ButtonUI", TmmLightButtonUI.class.getName(),
            "ToggleButtonUI", TmmLightToggleButtonUI.class.getName(),
            "ComboBoxUI", TmmLightComboBoxUI.class.getName(),
            "TreeUI", TmmLightTreeUI.class.getName(),
            "ToolBarUI", TmmLightToolBarUI.class.getName(),
            "TextFieldUI", TmmLightTextFieldUI.class.getName(),
            "PasswordFieldUI", TmmLightPasswordFieldUI.class.getName(),
            "CheckBoxUI", TmmLightCheckBoxUI.class.getName(),
            "RadioButtonUI", TmmLightRadioButtonUI.class.getName(),
            "TextAreaUI", TmmLightTextAreaUI.class.getName(),
            "EditorPaneUI", TmmLightEditorPaneUI.class.getName(),
            "TextPaneUI", TmmLightTextPaneUI.class.getName(),
            "ScrollPaneUI", TmmLightScrollPaneUI.class.getName(),
            "ProgressBarUI", TmmLightProgressBarUI.class.getName(),
            "SliderUI", TmmLightSliderUI.class.getName()
//        "ViewportUI", TmmLightViewportUI.class.getName(),
    };
    table.putDefaults(uiDefaults);
    // @formatter:on

    if (JTattooUtilities.getJavaVersion() >= 1.5) {
      table.put("FormattedTextFieldUI", BaseFormattedTextFieldUI.class.getName());
      table.put("SpinnerUI", TmmLightSpinnerUI.class.getName());
    }
  }
}