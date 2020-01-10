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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.ComponentUI;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseSliderUI;
import com.jtattoo.plaf.JTattooUtilities;

public class TmmSliderUI extends BaseSliderUI {
  protected static int TRACK_WIDTH = 7;
  protected static int THUMB_SIZE  = TRACK_WIDTH * 2 + 1;

  public TmmSliderUI(JSlider slider) {
    super(slider);
  }

  public static ComponentUI createUI(JComponent c) {
    return new TmmSliderUI((JSlider) c);
  }

  @Override
  protected Dimension getThumbSize() {
    return new Dimension(THUMB_SIZE, THUMB_SIZE + 2);
  }

  @Override
  protected int getTrackWidth() {
    return TRACK_WIDTH;
  }

  @Override
  public void paintTrack(Graphics g) {
    boolean leftToRight = JTattooUtilities.isLeftToRight(slider);

    g.translate(trackRect.x, trackRect.y);
    int overhang = 5;
    int trackLeft;
    int trackTop = 0;
    int trackRight;
    int trackBottom;

    if (slider.getOrientation() == JSlider.HORIZONTAL) {
      trackBottom = (trackRect.height - 1) - overhang;
      trackTop = trackBottom - (getTrackWidth() - 1);
      trackLeft = -TRACK_WIDTH / 2;
      trackRight = trackRect.width - 1 + TRACK_WIDTH / 2;
    }
    else {
      if (leftToRight) {
        trackLeft = (trackRect.width - overhang) - getTrackWidth();
        trackRight = (trackRect.width - overhang) - 1;
      }
      else {
        trackLeft = overhang;
        trackRight = overhang + getTrackWidth() - 1;
      }
      trackBottom = trackRect.height - 1;
    }

    Graphics2D g2D = (Graphics2D) g;
    Composite savedComposite = g2D.getComposite();
    RenderingHints savedRenderingHints = g2D.getRenderingHints();
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // draw rect
    g.setColor(AbstractLookAndFeel.getTheme().getTrackColors()[0]);
    g.fillRoundRect(trackLeft, trackTop, (trackRight - trackLeft) - 1, (trackBottom - trackTop) - 1, TRACK_WIDTH, TRACK_WIDTH);

    g.translate(-trackRect.x, -trackRect.y);
    g2D.setComposite(savedComposite);
    g2D.setRenderingHints(savedRenderingHints);
  }

  @Override
  public void paintThumb(Graphics g) {

    Graphics2D g2D = (Graphics2D) g;
    Composite savedComposite = g2D.getComposite();
    RenderingHints savedRenderingHints = g2D.getRenderingHints();
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (!slider.isEnabled()) {
      g.setColor(AbstractLookAndFeel.getBackgroundColor());
      g.fillRect(thumbRect.x + 1, thumbRect.y + 1, thumbRect.width - 2, thumbRect.height - 2);
      AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
      g2D.setComposite(alpha);
    }

    g.setColor(AbstractLookAndFeel.getTheme().getSliderColors()[0]);
    g.fillOval(thumbRect.x, thumbRect.y, THUMB_SIZE, THUMB_SIZE);

    g2D.setComposite(savedComposite);
    g2D.setRenderingHints(savedRenderingHints);
  }

  @Override
  public void paintFocus(Graphics g) {
  }
}
