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

package org.tinymediamanager.ui;

import java.awt.Font;

import javax.swing.JComponent;

/**
 * The class TmmFontHelper. A helper class for manipulating fonts
 * 
 * @author Manuel Laggner
 */
public class TmmFontHelper {

  public static double H1 = 1.33333;
  public static double H2 = 1.33333;
  public static double H3 = 1.16667;

  public static double L1 = 0.91667;
  public static double L2 = 0.83333;
  public static double L3 = 0.66667;

  /**
   * Scale the original font of a component by a given factor
   * 
   * @param comp
   *          the component for which the font has to be changed
   * @param scaleFactor
   *          the scale factor (the result will be rounded to an integer)
   */
  public static void changeFont(JComponent comp, double scaleFactor) {
    Font font = comp.getFont();
    comp.setFont(scale(font, scaleFactor));
  }

  /**
   * Change the font style of a component
   * 
   * @param comp
   *          the component for which the font has to be changed
   * @param style
   *          the new style
   */
  public static void changeFont(JComponent comp, int style) {
    Font font = comp.getFont();
    comp.setFont(font.deriveFont(style));
  }

  /**
   * Scale the original font of a component by a given factor and change the style
   * 
   * @param comp
   *          the component for which the font has to be changed
   * @param scaleFactor
   *          the scale factor (the result will be rounded to an integer)
   * @param style
   *          the new style
   */
  public static void changeFont(JComponent comp, double scaleFactor, int style) {
    Font font = comp.getFont();
    font = scale(font, scaleFactor);
    comp.setFont(font.deriveFont(style));
  }

  private static Font scale(Font font, double factor) {
    int newSize = Math.round((float) (font.getSize() * factor));
    return font.deriveFont((float) newSize);
  }
}
