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

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * The class JHintCheckBox. To provide a Checkbox with a hint icon and an extended tooltip
 * 
 * @author Manuel Laggner
 */
public class JHintCheckBox extends JCheckBox {
  private static final long serialVersionUID = -3513765234706901506L;

  private Icon              hintIcon;
  private Insets            dummyInsets;
  private MouseAdapter      tooltipAdatapter = createMouseAdapter();

  private MouseAdapter createMouseAdapter() {
    return new MouseAdapter() {
      final int defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();
      final int dismissDelayMinutes   = (int) TimeUnit.MINUTES.toMillis(10);              // 10 minutes

      @Override
      public void mouseEntered(MouseEvent me) {
        ToolTipManager.sharedInstance().setDismissDelay(dismissDelayMinutes);
      }

      @Override
      public void mouseExited(MouseEvent me) {
        ToolTipManager.sharedInstance().setDismissDelay(defaultDismissTimeout);
      }
    };
  }

  public JHintCheckBox() {
    super();
    this.hintIcon = null;

    Border border = UIManager.getBorder("CheckBox.border");
    JTextField dummy = new JTextField();
    this.dummyInsets = border.getBorderInsets(dummy);
  }

  public JHintCheckBox(String text) {
    super(text);
    this.hintIcon = null;

    Border border = UIManager.getBorder("CheckBox.border");
    JTextField dummy = new JTextField();
    this.dummyInsets = border.getBorderInsets(dummy);
  }

  public void setHintIcon(Icon icon) {
    this.hintIcon = icon;
  }

  public Icon getHintIcon() {
    return this.hintIcon;
  }

  @Override
  public void setToolTipText(String text) {
    // remove the mouse listener if already added
    removeMouseListener(tooltipAdatapter);
    // and readd
    addMouseListener(tooltipAdatapter);

    super.setToolTipText(text);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    int textX = 2;

    if (this.hintIcon != null) {
      int iconWidth = hintIcon.getIconWidth();
      int x = getWidth() - dummyInsets.right - iconWidth - 2;
      textX = dummyInsets.right + iconWidth + 4;
      int y = 4;
      hintIcon.paintIcon(this, g, x, y);
    }

    setMargin(new Insets(2, 2, 2, textX));
  }
}
