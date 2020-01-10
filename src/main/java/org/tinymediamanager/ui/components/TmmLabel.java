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

package org.tinymediamanager.ui.components;

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.UIManager;

/**
 * an own label class which has bold set and the scaling factor applied
 */
public class TmmLabel extends JLabel {

  private double scaleFactor = 1d;

  public TmmLabel() {
    super();
  }

  /**
   * create a new label with the given text
   *
   * @param text
   *          the text to be set
   */
  public TmmLabel(String text) {
    super(text);
  }

  /**
   * create a new label with the given text and scaled by the given factor
   *
   * @param text
   *          the text to be set
   * @param scaleFactor
   *          the scale factor applied to the default font size
   */
  public TmmLabel(String text, double scaleFactor) {
    super(text);
    this.scaleFactor = scaleFactor;
    setFont(scale(UIManager.getFont("Label.font").deriveFont(Font.BOLD), scaleFactor));
  }

  /**
   * create a new label with the given text and scaled by the given factor
   *
   * @param text
   *          the text to be set
   * @param scaleFactor
   *          the scale factor applied to the default font size
   * @param drawBorder
   *          should we draw a border beneath the text
   */
  public TmmLabel(String text, double scaleFactor, boolean drawBorder) {
    super(text);
    this.scaleFactor = scaleFactor;
    setFont(scale(UIManager.getFont("Label.font").deriveFont(Font.BOLD), scaleFactor));

    if (drawBorder) {
      setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, getForeground()));
    }
  }

  @Override
  public void updateUI() {
    super.updateUI();
    // to re-set the font after UI change
    setFont(scale(UIManager.getFont("Label.font").deriveFont(Font.BOLD), scaleFactor > 0 ? scaleFactor : 1));
  }

  protected Font scale(Font font, double factor) {
    int newSize = Math.round((float) (font.getSize() * factor));
    return font.deriveFont((float) newSize);
  }
}
