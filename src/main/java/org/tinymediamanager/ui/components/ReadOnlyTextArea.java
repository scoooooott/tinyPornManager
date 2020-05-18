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
import java.awt.Point;

import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

/**
 * A readonly variant of the JTextArea
 *
 * @author Manuel Laggner
 */
public class ReadOnlyTextArea extends JTextArea {

  public ReadOnlyTextArea() {
    this("");
  }

  public ReadOnlyTextArea(String text) {
    super(text);

    setOpaque(false);
    setBorder(null);
    setLineWrap(true);
    setWrapStyleWord(true);
    setEditable(false);
    setFocusable(false);
    setForeground(UIManager.getColor("Label.foreground"));
    setCaret(new NullCaret());
  }

  /**
   * an empty caret
   */
  private final class NullCaret implements Caret {
    @Override
    public void setVisible(boolean v) {
      // just do nothing
    }

    @Override
    public void setSelectionVisible(boolean v) {
      // just do nothing
    }

    @Override
    public void setMagicCaretPosition(Point p) {
      // just do nothing
    }

    @Override
    public void setDot(int dot) {
      // just do nothing
    }

    @Override
    public void setBlinkRate(int rate) {
      // just do nothing
    }

    @Override
    public void paint(Graphics g) {
      // just do nothing
    }

    @Override
    public void moveDot(int dot) {
      // just do nothing
    }

    @Override
    public boolean isVisible() {
      return false;
    }

    @Override
    public boolean isSelectionVisible() {
      return false;
    }

    @Override
    public void install(JTextComponent c) {
      // just do nothing
    }

    @Override
    public int getMark() {
      return 0;
    }

    @Override
    public Point getMagicCaretPosition() {
      return new Point(0, 0);
    }

    @Override
    public int getDot() {
      return 0;
    }

    @Override
    public int getBlinkRate() {
      return 0;
    }

    @Override
    public void deinstall(JTextComponent c) {
      // just do nothing
    }

    @Override
    public void addChangeListener(ChangeListener l) {
      // just do nothing
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
      // just do nothing
    }
  }
}
