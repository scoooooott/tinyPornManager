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

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

/**
 * The class TriStateCheckBox ist a JCheckBox which has been enhanced by a third state: Mixed<br />
 * BE AWARE: in the mixed state also isSelected() == true
 *
 * @author Manuel Laggner
 */
public class TriStateCheckBox extends JCheckBox implements ActionListener {
  public static final int STATE_UNSELECTED = 0;
  public static final int STATE_SELECTED   = 1;
  public static final int STATE_MIXED      = 2;

  public TriStateCheckBox(String text, Icon icon) {
    super(text, icon);
  }

  public TriStateCheckBox(String text) {
    this(text, null);
  }

  public TriStateCheckBox() {
    this(null);
  }

  @Override
  protected void init(String text, Icon icon) {
    model = createButtonModel();
    setModel(model);
    addActionListener(this);
    super.init(text, icon);
  }

  /**
   * Creates the button model. In this case, it is always a TriStateButtonModel.
   *
   * @return TristateButtonModel
   */

  protected ButtonModel createButtonModel() {
    return new TriStateButtonModel();
  }

  @Override
  public void updateUI() {
    super.updateUI();
    if (isMixed()) {
      adjustMixedIcon();
    }
    else {
      restoreMixedIcon();
    }
  }

  protected void adjustMixedIcon() {
    setIcon(UIManager.getIcon("TriStateCheckBox.icon"));
  }

  protected void restoreMixedIcon() {
    setIcon(null);
  }

  /**
   * Checks if the check box is in mixed selection state.
   *
   * @return true or false.
   */
  public boolean isMixed() {
    return getState() == STATE_MIXED;
  }

  /**
   * Sets the check box to mixed selection state.
   *
   * @param b
   *          true or false. True means mixed state. False means unselected state.
   */

  public void setMixed(boolean b) {
    if (b) {
      setState(STATE_MIXED);
    }
    else {
      setState(STATE_UNSELECTED);
    }
  }

  /**
   * Gets the selection state. It could be one of the three states as defined - {@link #STATE_SELECTED}, {@link #STATE_UNSELECTED} and
   * {@link #STATE_MIXED}.
   *
   * @return one of the three selection states.
   */
  public int getState() {
    if (model instanceof TriStateButtonModel)
      return ((TriStateButtonModel) model).getState();
    else {
      throw new IllegalStateException("TriStateButtonModel is required for TriStateCheckBox");
    }
  }

  /**
   * Sets the selection state. It could be one of the three states as defined - {@link #STATE_SELECTED}, {@link #STATE_UNSELECTED} and
   * {@link #STATE_MIXED}.
   * 
   * @param state
   *          one of the three selection states.
   */
  public void setState(int state) {
    if (model instanceof TriStateButtonModel) {
      int old = ((TriStateButtonModel) model).getState();
      if (old != state) {
        ((TriStateButtonModel) model).setState(state);
      }
      stateUpdated(state);
    }
    else {
      throw new IllegalStateException("TriStateButtonModel is required for TriStateCheckBox");
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    stateUpdated(getState());
  }

  /**
   * This method is called when the selection state changes.
   *
   * @param state
   *          the new selection state.
   */
  protected void stateUpdated(int state) {
    if (state == STATE_MIXED) {
      adjustMixedIcon();
    }
    else {
      restoreMixedIcon();
    }
  }

  public static class TriStateButtonModel extends JToggleButton.ToggleButtonModel {
    private static final long serialVersionUID = 1257832793162900012L;

    /**
     * Identifies the "mixed" bit in the bitmask, which indicates that the button is partial selected.
     */
    public static final int   MIXED            = 1 << 7;

    public TriStateButtonModel() {
    }

    public void setState(int state) {
      switch (state) {
        case TriStateCheckBox.STATE_UNSELECTED:
          setSelected(false);
          break;

        case TriStateCheckBox.STATE_SELECTED:
          setSelected(true);
          break;

        case TriStateCheckBox.STATE_MIXED:
          setMixed(true);
          break;
      }
    }

    public int getState() {
      if (isMixed()) {
        return TriStateCheckBox.STATE_MIXED;
      }
      else if (isSelected()) {
        return TriStateCheckBox.STATE_SELECTED;
      }
      else {
        return TriStateCheckBox.STATE_UNSELECTED;
      }
    }

    /**
     * We rotate between STATE_UNSELECTED, STATE_SELECTED and STATE_MIXED.
     *
     * @param current
     *          the current state
     * @return the next state of the current state.
     */
    protected int getNextState(int current) {
      if (current == TriStateCheckBox.STATE_UNSELECTED) {
        return TriStateCheckBox.STATE_SELECTED;
      }
      else if (current == TriStateCheckBox.STATE_SELECTED) {
        return TriStateCheckBox.STATE_MIXED;
      }
      else /* if (current == STATE_MIXED) */ {
        return TriStateCheckBox.STATE_UNSELECTED;
      }
    }

    @Override
    public void setPressed(boolean b) {
      if ((isPressed() == b) || !isEnabled()) {
        return;
      }

      if (!b && isArmed()) {
        updateState();
      }

      if (b) {
        stateMask |= PRESSED;
      }
      else {
        stateMask &= ~PRESSED;
      }

      fireStateChanged();

      if (!isPressed() && isArmed()) {
        int modifiers = 0;
        AWTEvent currentEvent = EventQueue.getCurrentEvent();
        if (currentEvent instanceof InputEvent) {
          modifiers = ((InputEvent) currentEvent).getModifiers();
        }
        else if (currentEvent instanceof ActionEvent) {
          modifiers = ((ActionEvent) currentEvent).getModifiers();
        }
        fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand(), EventQueue.getMostRecentEventTime(), modifiers));
      }
    }

    /**
     * Updates the state when the mouse is clicked.
     */
    protected void updateState() {
      setState(getNextState(getState()));
    }

    @Override
    public void setSelected(boolean b) {
      boolean mixed = isMixed();
      if (mixed) {
        stateMask &= ~MIXED;
        internalSetSelected(!isSelected());
      }
      super.setSelected(b);
    }

    void internalSetSelected(boolean b) {
      if (b) {
        stateMask |= SELECTED;
      }
      else {
        stateMask &= ~SELECTED;
      }
    }

    public boolean isMixed() {
      return (stateMask & MIXED) != 0;
    }

    public void setMixed(boolean b) {
      if ((isMixed() == b)) {
        return;
      }

      if (b) {
        stateMask |= MIXED;
        stateMask |= SELECTED; // make it selected
      }
      else {
        stateMask &= ~MIXED;
      }

      fireStateChanged();
    }
  }
}
