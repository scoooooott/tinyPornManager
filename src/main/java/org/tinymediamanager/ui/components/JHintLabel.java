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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.ToolTipManager;

/**
 * The class JHintLabel. To provide a label with a hint icon and an extended tooltip
 * 
 * @author Manuel Laggner
 */
public class JHintLabel extends JLabel {
  private static final long serialVersionUID = 3027595143561381907L;

  private Icon              hintIcon         = null;
  private MouseAdapter      tooltipAdatapter = new MouseAdapter() {
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

  public JHintLabel() {
    super();
  }

  public JHintLabel(String text) {
    super(text);
  }

  public void setHintIcon(Icon icon) {
    this.hintIcon = icon;
    if (icon != null) {
      setBorder(BorderFactory.createEmptyBorder(2, 2, 2, icon.getIconWidth() + 6));
    }
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

    if (this.hintIcon != null) {
      int iconWidth = hintIcon.getIconWidth();
      int x = getWidth() - iconWidth - 2;

      int y = 0;
      hintIcon.paintIcon(this, g, x, y);
    }
  }
}
