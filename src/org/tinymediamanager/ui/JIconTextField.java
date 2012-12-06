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

import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * The Class JIconTextField.
 * 
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class JIconTextField extends JTextField {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The icon. */
  private Icon              icon;

  /** The dummy insets. */
  private Insets            dummyInsets;

  /**
   * Instantiates a new j icon text field.
   */
  public JIconTextField() {
    super();
    this.icon = null;

    Border border = UIManager.getBorder("TextField.border");
    JTextField dummy = new JTextField();
    this.dummyInsets = border.getBorderInsets(dummy);
  }

  /**
   * Sets the icon.
   * 
   * @param icon
   *          the new icon
   */
  public void setIcon(Icon icon) {
    this.icon = icon;
  }

  /**
   * Gets the icon.
   * 
   * @return the icon
   */
  public Icon getIcon() {
    return this.icon;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    int textX = 2;

    if (this.icon != null) {
      int iconWidth = icon.getIconWidth();
      int iconHeight = icon.getIconHeight();
      int x = dummyInsets.left + 5;// this is our icon's x
      textX = x + iconWidth + 2; // this is the x where text should start
      int y = (this.getHeight() - iconHeight) / 2;
      icon.paintIcon(this, g, x, y);
    }

    setMargin(new Insets(2, textX, 2, 2));

  }
}
