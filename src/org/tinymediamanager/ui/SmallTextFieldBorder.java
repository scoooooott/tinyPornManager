/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;
import javax.swing.plaf.UIResource;

/**
 * The class SmallTextFieldBorder - used for a smaller version of a JTextField
 * 
 * @author Manuel Laggner
 */
public class SmallTextFieldBorder extends AbstractBorder implements UIResource {
  private static final long   serialVersionUID = -4325697995345793715L;
  private static final Insets insets           = new Insets(1, 2, 1, 2);
  private static final Color  fieldBorderColor = new Color(127, 157, 185);

  @Override
  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    width--;
    height--;
    g.setColor(fieldBorderColor);
    g.drawRect(x, y, width, height);
  }

  @Override
  public Insets getBorderInsets(Component c) {
    return new Insets(insets.top, insets.left, insets.bottom, insets.right);
  }

  @Override
  public Insets getBorderInsets(Component c, Insets borderInsets) {
    borderInsets.left = insets.left;
    borderInsets.top = insets.top;
    borderInsets.right = insets.right;
    borderInsets.bottom = insets.bottom;
    return borderInsets;
  }
}