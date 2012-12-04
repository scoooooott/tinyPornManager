/*
 * Copyright 2010 Georgios Migdos <cyberpython@gmail.com>.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */
package org.tinymediamanager.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.UIManager;

/**
 * 
 * @authorGeorgios Migdos <cyberpython@gmail.com>
 */
public class JSearchTextField extends JIconTextField implements FocusListener {

  private String textWhenNotFocused;

  public JSearchTextField() {
    super();
    this.textWhenNotFocused = "Search...";
    this.addFocusListener(this);
  }

  public String getTextWhenNotFocused() {
    return this.textWhenNotFocused;
  }

  public void setTextWhenNotFocused(String newText) {
    this.textWhenNotFocused = newText;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    if (!this.hasFocus() && this.getText().equals("")) {
      int width = this.getWidth();
      int height = this.getHeight();
      Font prev = g.getFont();
      Font italic = prev.deriveFont(Font.ITALIC);
      Color prevColor = g.getColor();
      g.setFont(italic);
      g.setColor(UIManager.getColor("textInactiveText"));
      int h = g.getFontMetrics().getHeight();
      int textBottom = (height - h) / 2 + h - 4;
      int x = this.getInsets().left;
      Graphics2D g2d = (Graphics2D) g;
      RenderingHints hints = g2d.getRenderingHints();
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2d.drawString(textWhenNotFocused, x, textBottom);
      g2d.setRenderingHints(hints);
      g.setFont(prev);
      g.setColor(prevColor);
    }

  }

  // FocusListener implementation:
  public void focusGained(FocusEvent e) {
    this.repaint();
  }

  public void focusLost(FocusEvent e) {
    this.repaint();
  }
}
