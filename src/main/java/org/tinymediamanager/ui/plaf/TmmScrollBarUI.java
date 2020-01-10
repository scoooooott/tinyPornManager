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

import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

import com.jtattoo.plaf.AbstractLookAndFeel;

/**
 * The Class TmmScrollBarUI.
 *
 * @author Manuel Laggner
 */
public class TmmScrollBarUI extends BasicScrollBarUI {
  protected static int SCROLL_BAR_WIDTH = 16;
  protected static int TRACK_WIDTH      = 8;

  protected static int GAP              = 4;

  protected boolean    swapColors;

  public static ComponentUI createUI(JComponent c) {
    return new TmmScrollBarUI();
  }

  public TmmScrollBarUI() {
    super();
  }

  @Override
  protected void installDefaults() {
    super.installDefaults();

    Object swapColors = scrollbar.getClientProperty("swapColors");
    if (swapColors != null && "true".equals(swapColors.toString())) {
      this.swapColors = true;
    }
    else {
      this.swapColors = false;
    }
  }

  @Override
  public Dimension getPreferredSize(JComponent c) {
    if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
      return new Dimension(SCROLL_BAR_WIDTH, SCROLL_BAR_WIDTH * 2 + 16);
    }
    else {
      return new Dimension(SCROLL_BAR_WIDTH * 2 + 16, SCROLL_BAR_WIDTH);
    }
  }

  @Override
  protected Dimension getMinimumThumbSize() {
    if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
      return new Dimension(TRACK_WIDTH - 2, SCROLL_BAR_WIDTH * 2);
    }
    else {
      return new Dimension(SCROLL_BAR_WIDTH * 2, TRACK_WIDTH - 2);
    }
  }

  @Override
  protected Dimension getMaximumThumbSize() {
    if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
      return new Dimension(TRACK_WIDTH - 2, SCROLL_BAR_WIDTH * 3);
    }
    else {
      return new Dimension(SCROLL_BAR_WIDTH * 3, TRACK_WIDTH - 2);
    }
  }

  @Override
  protected JButton createDecreaseButton(int orientation) {
    return createZeroButton();
  }

  @Override
  protected JButton createIncreaseButton(int orientation) {
    return createZeroButton();
  }

  protected JButton createZeroButton() {
    JButton button = new JButton("zero button");
    Dimension zeroDim = new Dimension(0, 0);
    button.setPreferredSize(zeroDim);
    button.setMinimumSize(zeroDim);
    button.setMaximumSize(zeroDim);
    return button;
  }

  @Override
  protected void layoutVScrollbar(JScrollBar sb) {
    super.layoutVScrollbar(sb);

    // special case: no scrolling is needed; the logic above will paint the thumb at bottom;
    // paint it on top
    if (sb.getValue() == 0) {
      thumbRect.setBounds(thumbRect.x, -1 + GAP, thumbRect.width, thumbRect.height - 2 * GAP);
    }
    else {
      thumbRect.setBounds(thumbRect.x, thumbRect.y + GAP, thumbRect.width, thumbRect.height - 2 * GAP);
    }
    trackRect.setBounds(trackRect.x, trackRect.y + GAP, trackRect.width, trackRect.height - 2 * GAP);
  }

  @Override
  protected void layoutHScrollbar(JScrollBar sb) {
    super.layoutHScrollbar(sb);

    trackRect.setBounds(trackRect.x + GAP, trackRect.y, trackRect.width - 2 * GAP, trackRect.height);
    thumbRect.setBounds(thumbRect.x + GAP, thumbRect.y, thumbRect.width - 2 * GAP, thumbRect.height);
  }

  @Override
  protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
    // do not paint the track if there is no thumb
    if (getThumbBounds().isEmpty()) {
      return;
    }

    // background
    if (scrollbar.isOpaque()) {
      if (swapColors) {
        c.setBackground(AbstractLookAndFeel.getTheme().getTrackColors()[0]);
        g.setColor(AbstractLookAndFeel.getTheme().getBackgroundColorDark());
      }
      else {
        c.setBackground(AbstractLookAndFeel.getTheme().getBackgroundColor());
        g.setColor(AbstractLookAndFeel.getTheme().getTrackColors()[0]);
      }
    }

    // track
    Graphics2D g2D = (Graphics2D) g;
    Composite savedComposite = g2D.getComposite();
    RenderingHints savedRenderingHints = g2D.getRenderingHints();
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
      int x = (SCROLL_BAR_WIDTH - TRACK_WIDTH) / 2;
      g.fillRoundRect(trackBounds.x + x, trackBounds.y, TRACK_WIDTH, trackBounds.height, TRACK_WIDTH, TRACK_WIDTH);
    }
    else {
      int y = (SCROLL_BAR_WIDTH - TRACK_WIDTH) / 2;
      g.fillRoundRect(trackBounds.x, trackBounds.y + y, trackBounds.width, TRACK_WIDTH, TRACK_WIDTH, TRACK_WIDTH);
    }

    g2D.setComposite(savedComposite);
    g2D.setRenderingHints(savedRenderingHints);
  }

  @Override
  protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
    if (!c.isEnabled()) {
      return;
    }

    g.translate(thumbBounds.x, thumbBounds.y);

    Graphics2D g2D = (Graphics2D) g;
    Composite savedComposite = g2D.getComposite();
    RenderingHints savedRenderingHints = g2D.getRenderingHints();
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (swapColors) {
      g.setColor(AbstractLookAndFeel.getTheme().getTrackColors()[0]);
    }
    else {
      g.setColor(AbstractLookAndFeel.getTheme().getThumbColors()[0]);
    }

    if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
      int x = (SCROLL_BAR_WIDTH - TRACK_WIDTH) / 2;
      g.fillRoundRect(x + 1, 2, TRACK_WIDTH - 2, thumbBounds.height - 4, TRACK_WIDTH - 2, TRACK_WIDTH - 2);
    }
    else {
      int y = (SCROLL_BAR_WIDTH - TRACK_WIDTH) / 2;
      g.fillRoundRect(2, y + 1, thumbBounds.width - 4, TRACK_WIDTH - 2, TRACK_WIDTH - 2, TRACK_WIDTH - 2);
    }

    g2D.setComposite(savedComposite);
    g2D.setRenderingHints(savedRenderingHints);

    g.translate(-thumbBounds.x, -thumbBounds.y);
  }
}
