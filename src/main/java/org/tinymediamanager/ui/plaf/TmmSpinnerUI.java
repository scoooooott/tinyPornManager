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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;

import org.tinymediamanager.ui.plaf.light.TmmLightBorderFactory;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseSpinnerUI;
import com.jtattoo.plaf.JTattooUtilities;
import com.jtattoo.plaf.NoFocusButton;

public class TmmSpinnerUI extends BaseSpinnerUI {
  /**
   * Used by the default LayoutManager class - SpinnerLayout for missing (null) editor/nextButton/previousButton children.
   */
  private static final Dimension zeroSize        = new Dimension(0, 0);

  private MyLayoutManager        myLayoutManager = null;
  private FocusListener          focusListener   = null;

  /**
   * Returns a new instance of TmmSpinnerUI. SpinnerListUI delegates are allocated one per JSpinner.
   *
   * @param c
   *          the JSpinner (not used)
   * @return a new BasicSpinnerUI object
   * @see ComponentUI#createUI
   */
  public static ComponentUI createUI(JComponent c) {
    return new TmmSpinnerUI();
  }

  @Override
  public void installDefaults() {
    super.installDefaults();
    spinner.setForeground(AbstractLookAndFeel.getInputForegroundColor());
    spinner.setBackground(AbstractLookAndFeel.getInputBackgroundColor());
    spinner.setBorder(TmmLightBorderFactory.getInstance().getSpinnerBorder());
  }

  @Override
  protected void replaceEditor(JComponent oldEditor, JComponent newEditor) {
    ((JSpinner.DefaultEditor) oldEditor).getTextField().removeFocusListener(focusListener);
    super.replaceEditor(oldEditor, newEditor);
    ((JSpinner.DefaultEditor) newEditor).getTextField().addFocusListener(focusListener);
  }

  @Override
  protected void installListeners() {
    super.installListeners();
    focusListener = new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        if (spinner != null) {
          spinner.invalidate();
          spinner.repaint();
        }
      }

      @Override
      public void focusLost(FocusEvent e) {
        if (spinner != null) {
          spinner.invalidate();
          spinner.repaint();
        }
      }
    };

    ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().addFocusListener(focusListener);
  }

  @Override
  protected void uninstallListeners() {
    ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().removeFocusListener(focusListener);
    focusListener = null;
    super.uninstallListeners();
  }

  /**
   * Create a <code>LayoutManager</code> that manages the <code>editor</code>, <code>nextButton</code>, and <code>previousButton</code> children of
   * the JSpinner. These three children must be added with a constraint that identifies their role: "Editor", "Next", and "Previous". The default
   * layout manager can handle the absence of any of these children.
   *
   * @return a LayoutManager for the editor, next button, and previous button.
   * @see #createNextButton
   * @see #createPreviousButton
   * @see #createEditor
   */
  @Override
  protected LayoutManager createLayout() {
    if (myLayoutManager == null) {
      myLayoutManager = new MyLayoutManager();
    }
    return myLayoutManager;
  }

  @Override
  protected Component createNextButton() {
    JButton button = new SpinButton(SwingConstants.NORTH);
    if (JTattooUtilities.isLeftToRight(spinner)) {
      Border border = BorderFactory.createEmptyBorder(0, 1, 1, 0);
      button.setBorder(border);
    }
    else {
      Border border = BorderFactory.createEmptyBorder(0, 0, 1, 1);
      button.setBorder(border);
    }
    installNextButtonListeners(button);
    return button;
  }

  @Override
  protected Component createPreviousButton() {
    JButton button = new SpinButton(SwingConstants.SOUTH);
    if (JTattooUtilities.isLeftToRight(spinner)) {
      Border border = BorderFactory.createEmptyBorder(0, 1, 0, 0);
      button.setBorder(border);
    }
    else {
      Border border = BorderFactory.createEmptyBorder(0, 0, 0, 1);
      button.setBorder(border);
    }
    installPreviousButtonListeners(button);
    return button;
  }

  // -----------------------------------------------------------------------------------------
  // inner classes
  // -----------------------------------------------------------------------------------------
  public static class SpinButton extends NoFocusButton {
    private static final long      serialVersionUID = -8393323134878803979L;
    private static final Dimension minSize          = new Dimension(14, 12);
    private int                    direction        = SwingConstants.NORTH;

    public SpinButton(int aDirection) {
      super();
      setInheritsPopupMenu(true);
      direction = aDirection;
    }

    @Override
    public Dimension getPreferredSize() {
      Dimension size = super.getPreferredSize();
      size.width = Math.max(size.width, minSize.width);
      size.height = Math.max(size.height, minSize.height);
      return size;
    }

    @Override
    public void paint(Graphics g) {
      g.setColor(AbstractLookAndFeel.getInputBackgroundColor());
      g.fillRect(0, 0, getWidth(), getHeight());

      paintBorder(g);
      g.setColor(getForeground());
      int w = 8;
      int h = 5;
      int x = (getWidth() - w) / 2;
      int y = (getHeight() - h) / 2;
      if (direction == SwingConstants.NORTH) {
        for (int i = 0; i < h; i++) {
          g.drawLine(x + (h - i) - 1, y + i, x + w - (h - i) + 1, y + i);
        }
      }
      else {
        for (int i = 0; i < h; i++) {
          g.drawLine(x + i, y + i, x + w - i, y + i);
        }
      }
    }

  }

  // ----------------------------------------------------------------------------------------------
  // inner classes
  // ----------------------------------------------------------------------------------------------
  private static class MyLayoutManager implements LayoutManager {

    private Component nextButton     = null;
    private Component previousButton = null;
    private Component editor         = null;

    @Override
    public void addLayoutComponent(String name, Component c) {
      if ("Next".equals(name)) {
        nextButton = c;
      }
      else if ("Previous".equals(name)) {
        previousButton = c;
      }
      else if ("Editor".equals(name)) {
        editor = c;
      }
    }

    @Override
    public void removeLayoutComponent(Component c) {
      if (c == nextButton) {
        nextButton = null;
      }
      else if (c == previousButton) {
        previousButton = null;
      }
      else if (c == editor) {
        editor = null;
      }
    }

    private Dimension preferredSize(Component c) {
      return (c == null) ? zeroSize : c.getPreferredSize();
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
      Dimension nextD = preferredSize(nextButton);
      Dimension previousD = preferredSize(previousButton);
      Dimension editorD = preferredSize(editor);

      // Force the editors height to be a multiple of 2
      editorD.height = ((editorD.height + 1) / 2) * 2;

      Dimension size = new Dimension(editorD.width, editorD.height);
      size.width += Math.max(nextD.width, previousD.width);
      Insets insets = parent.getInsets();
      size.width += insets.left + insets.right;
      size.height += insets.top + insets.bottom;
      return size;
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
      return preferredLayoutSize(parent);
    }

    private void setBounds(Component c, int x, int y, int width, int height) {
      if (c != null) {
        c.setBounds(x, y, width, height);
      }
    }

    @Override
    public void layoutContainer(Container parent) {
      int width = parent.getWidth();
      int height = parent.getHeight();

      Insets insets = parent.getInsets();
      Dimension nextD = preferredSize(nextButton);
      Dimension previousD = preferredSize(previousButton);
      int buttonsWidth = Math.max(nextD.width, previousD.width);
      int editorHeight = height - (insets.top + insets.bottom);

      // The arrowButtonInsets value is used instead of the JSpinner's
      // insets if not null. Defining this to be (0, 0, 0, 0) causes the
      // buttons to be aligned with the outer edge of the spinner's
      // border, and leaving it as "null" places the buttons completely
      // inside the spinner's border.
      Insets buttonInsets = UIManager.getInsets("Spinner.arrowButtonInsets");
      if (buttonInsets == null) {
        buttonInsets = insets;
      }

      // Deal with the spinner's componentOrientation property.
      int editorX, editorWidth, buttonsX;
      if (parent.getComponentOrientation().isLeftToRight()) {
        editorX = insets.left;
        editorWidth = width - insets.left - buttonsWidth - buttonInsets.right;
        buttonsX = width - buttonsWidth - buttonInsets.right;
      }
      else {
        buttonsX = buttonInsets.left;
        editorX = buttonsX + buttonsWidth;
        editorWidth = width - buttonInsets.left - buttonsWidth - insets.right;
      }

      int nextY = buttonInsets.top;
      int nextHeight = (height / 2) + (height % 2) - nextY;
      int previousY = buttonInsets.top + nextHeight;
      int previousHeight = height - previousY - buttonInsets.bottom;

      setBounds(editor, editorX, insets.top, editorWidth, editorHeight);
      setBounds(nextButton, buttonsX, nextY, buttonsWidth, nextHeight);
      setBounds(previousButton, buttonsX, previousY, buttonsWidth, previousHeight);
    }

  }

}
