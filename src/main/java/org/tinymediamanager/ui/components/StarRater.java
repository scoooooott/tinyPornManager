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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.tinymediamanager.core.ImageUtils;
import org.tinymediamanager.ui.IconManager;

/**
 * The star rater panel.
 * 
 * @author noblemaster
 * @since August 30, 2010
 */
public class StarRater extends JPanel {
  private static final long  serialVersionUID      = -5601531605019166004L;
  private static final Image STAR_BACKGROUND_IMAGE = ImageUtils.createImage(IconManager.STAR_EMPTY.getImage());
  private static final Image STAR_FOREGROUND_IMAGE = ImageUtils.createImage(IconManager.STAR_FILLED.getImage());

  public interface StarListener {
    void handleSelection(int selection);
  }

  private List<StarListener> listeners = new ArrayList<>();

  /** The number of stars n. */
  private int                stars;

  /**
   * The factor which the rating will divided (i.e. rating 0 - 10 displayed with 5 stars)
   */
  private int                factor;

  /** The rating [0, n]. 0 = no rating. */
  private float              rating;

  /** The rating which has to be painted (rating / factor). */
  private float              paintRating;

  /** The selection [0, n]. 0 = no selection. */
  private int                selection;

  /** The rollover [0, n]. 0 = no rollover. */
  private int                rollover;

  /** True for clicked this time. */
  private boolean            done;

  /**
   * The constructor.
   */
  public StarRater() {
    this(5, 1);
  }

  /**
   * The constructor.
   * 
   * @param stars
   *          The number of stars n.
   * @param factor
   *          the factor
   */
  public StarRater(int stars, int factor) {
    this(stars, factor, 0f);
  }

  /**
   * The constructor.
   * 
   * @param stars
   *          The number of stars n.
   * @param factor
   *          the factor
   * @param rating
   *          The rating [0, n]. 0 = no rating.
   */
  public StarRater(int stars, int factor, float rating) {
    this(stars, factor, rating, 0);
  }

  /**
   * The constructor.
   * 
   * @param stars
   *          The number of stars n.
   * @param factor
   *          the factor
   * @param rating
   *          The rating [0, n]. 0 = no rating.
   * @param selection
   *          The selection [0, n]. 0 = no selection.
   */
  public StarRater(int stars, int factor, float rating, int selection) {
    this.stars = stars;
    this.rating = rating;
    this.selection = selection;
    if (factor > 0) {
      this.factor = factor;
    }
    else {
      this.factor = 1;
    }
    this.paintRating = this.rating / this.factor;
    this.rollover = 0;
    this.done = false;

    // set look
    setOpaque(false);
    setLayout(null);

    // listen to selections
    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseMoved(MouseEvent event) {
        if (isEnabled()) {
          if (!done) {
            rollover = 1 + (event.getX() / STAR_BACKGROUND_IMAGE.getWidth(null));
            repaint();
          }
        }
      }
    });
    addMouseListener(new MouseAdapter() {
      public void mouseExited(MouseEvent event) {
        if (isEnabled()) {
          rollover = 0;
          done = false;
          repaint();
        }
      }

      public void mousePressed(MouseEvent event) {
        if (isEnabled()) {
          rollover = 0;
          done = true;
          StarRater.this.selection = 1 + (event.getX() / STAR_FOREGROUND_IMAGE.getWidth(null));
          for (StarListener listener : listeners) {
            listener.handleSelection(StarRater.this.selection);
          }
          repaint();
        }
      }

      public void mouseReleased(MouseEvent event) {
        if (isEnabled()) {
          if (!done) {
            rollover = 1 + (event.getX() / STAR_BACKGROUND_IMAGE.getWidth(null));
            repaint();
          }
        }
      }
    });
  }

  /**
   * Called to enable/disable.
   * 
   * @param enabled
   *          True for enabled.
   */
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);

    // do stuff
    if (!enabled) {
      rollover = 0;
      repaint();
    }
  }

  /**
   * Returns the rating.
   * 
   * @return The rating [0, n]. 0 = no rating.
   */
  public float getRating() {
    return rating;
  }

  /**
   * Sets the rating.
   * 
   * @param rating
   *          The rating [0, n]. 0 = no rating.
   */
  public void setRating(float rating) {
    this.rating = rating;
    this.paintRating = this.rating / this.factor;
    repaint();
  }

  /**
   * Returns the selection.
   * 
   * @return The selection [0, n]. 0 = no selection.
   */
  public int getSelection() {
    return selection;
  }

  /**
   * Sets the selection.
   * 
   * @param selection
   *          The selection [0, n]. 0 = no selection.
   */
  public void setSelection(int selection) {
    this.selection = selection;
    repaint();
  }

  /**
   * Returns the preferred size.
   * 
   * @return The preferred size.
   */
  @Override
  public Dimension getPreferredSize() {
    return new Dimension(stars * STAR_BACKGROUND_IMAGE.getWidth(null), STAR_BACKGROUND_IMAGE.getHeight(null));
  }

  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  @Override
  public Dimension getMaximumSize() {
    return getPreferredSize();
  }

  /**
   * Paints this component.
   * 
   * @param g
   *          Where to paint to.
   */
  @Override
  protected void paintComponent(Graphics g) {
    if (rating >= 0) {
      // draw stars
      int w = STAR_BACKGROUND_IMAGE.getWidth(null);
      int h = STAR_BACKGROUND_IMAGE.getHeight(null);
      int x = 0;
      for (int i = 0; i < stars; i++) {
        g.drawImage(STAR_BACKGROUND_IMAGE, x, 0, null);
        if (paintRating > i) {
          int dw = (paintRating >= (i + 1)) ? w : Math.round((paintRating - i) * w);
          g.drawImage(STAR_FOREGROUND_IMAGE, x, 0, x + dw, h, 0, 0, dw, h, null);
        }

        // if (rating > i) {
        // int dw = (rating >= (i + 1)) ? w : Math.round((rating - i) * w);
        // g.drawImage(STAR_FOREGROUND_IMAGE, x, 0, x + dw, h, 0, 0, dw, h,
        // null);
        // }

        // if (selection > i) {
        // g.drawImage(STAR_SELECTION_IMAGE, x, 0, null);
        // }
        // if (rollover > i) {
        // g.drawImage(STAR_ROLLOVER_IMAGE, x, 0, null);
        // }
        x += w;
      }
    }
  }

  /**
   * Adds a listener.
   * 
   * @param listener
   *          The listener to add.
   */
  public void addStarListener(StarListener listener) {
    listeners.add(listener);
  }

  /**
   * Removes a listener.
   * 
   * @param listener
   *          The listener to add.
   */
  public void removeStarListener(StarListener listener) {
    listeners.remove(listener);
  }
}
