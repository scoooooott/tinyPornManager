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

import java.awt.FontMetrics;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;

import com.jtattoo.plaf.BaseLabelUI;

public class TmmLabelUI extends BaseLabelUI {

  private static TmmLabelUI labelUI = null;

  public static ComponentUI createUI(JComponent c) {
    if (labelUI == null) {
      labelUI = new TmmLabelUI();
    }
    return labelUI;
  }

  /**
   * this class supports clip the string on the left/middle/right
   */
  protected String layoutCL(JLabel label, FontMetrics fontMetrics, String text, Icon icon, Rectangle viewR, Rectangle iconR, Rectangle textR) {
    String stringFromSwingUtilities = SwingUtilities.layoutCompoundLabel(label, fontMetrics, text, icon, label.getVerticalAlignment(),
        label.getHorizontalAlignment(), label.getVerticalTextPosition(), label.getHorizontalTextPosition(), viewR, iconR, textR,
        label.getIconTextGap());

    int clipPosition = getClipPosition(label);

    switch (clipPosition) {
      case SwingConstants.LEFT:
      case SwingConstants.CENTER:
        return reLayoutString(label, clipPosition, text, stringFromSwingUtilities);

      case SwingConstants.RIGHT:
      default:
        return stringFromSwingUtilities;
    }
  }

  private int getClipPosition(JLabel label) {
    Object prop = label.getClientProperty("clipPosition");
    if (prop != null && prop instanceof Integer) {
      if ((Integer) prop == SwingConstants.LEFT) {
        return SwingConstants.LEFT;
      }
      else if ((Integer) prop == SwingConstants.RIGHT) {
        return SwingConstants.RIGHT;
      }
      else if ((Integer) prop == SwingConstants.CENTER) {
        return SwingConstants.CENTER;
      }
    }
    return SwingConstants.RIGHT;
  }

  private String reLayoutString(JLabel label, int clipPosition, String originalText, String stringFromSwingUtilities) {
    // clipping needed?
    if (!stringFromSwingUtilities.endsWith("...")) {
      return stringFromSwingUtilities;
    }

    String dots = "...";
    FontMetrics fm = label.getFontMetrics(label.getFont());
    int targetStringWidth = fm.stringWidth(stringFromSwingUtilities);

    if (clipPosition == SwingConstants.LEFT) {
      int i = originalText.length() - stringFromSwingUtilities.length();
      if (i < 0) {
        return stringFromSwingUtilities;
      }
      for (; i < originalText.length(); i++) {
        String substring = dots + originalText.substring(i);
        if (fm.stringWidth(substring) <= targetStringWidth) {
          return substring;
        }
      }
    }
    else if (clipPosition == SwingConstants.CENTER) {
      int i = stringFromSwingUtilities.length() / 2;
      if (i < 0) {
        return stringFromSwingUtilities;
      }
      for (; i > 0; i--) {
        String substring = originalText.substring(0, i) + dots + originalText.substring(originalText.length() - i);
        if (fm.stringWidth(substring) <= targetStringWidth) {
          return substring;
        }
      }
    }

    return stringFromSwingUtilities;
  }
}
