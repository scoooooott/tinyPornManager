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

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.tinymediamanager.ui.IconManager;

import net.miginfocom.swing.MigLayout;

public class CollapsiblePanel extends JPanel {
  private final JButton    btnToggle;
  private final JComponent content;

  private boolean          isCollapsed;

  /**
   * create the {@link CollapsiblePanel} with default settings: button at left, label as {@link TmmLabel}, expanded
   * 
   * @param content
   *          the content to be displayed in the panel
   * @param title
   *          the title to be displayed via a {@link TmmLabel}
   */
  public CollapsiblePanel(JComponent content, String title) {
    this(content, title, true);
  }

  /**
   * create the {@link CollapsiblePanel} with default settings: button at left, the given {@link JLabel}, expanded
   *
   * @param content
   *          the content to be displayed in the panel
   * @param lblTitle
   *          the label to be displayed as title
   */
  public CollapsiblePanel(JComponent content, JLabel lblTitle) {
    this(content, lblTitle, true);
  }

  /**
   * create the {@link CollapsiblePanel} with the button at the specified location, the label as {@link TmmLabel}, expanded
   * 
   * @param content
   *          the content to be displayed in the panel
   * @param title
   *          the title to be displayed via a {@link TmmLabel}
   * @param collapseButtonAtLeft
   *          button at left?
   */
  public CollapsiblePanel(JComponent content, String title, boolean collapseButtonAtLeft) {
    this(content, title, collapseButtonAtLeft, false);
  }

  /**
   * create the {@link CollapsiblePanel} with the button at the specified location, the given {@link JLabel}, expanded
   *
   * @param content
   *          the content to be displayed in the panel
   * @param lblTitle
   *          the label to be displayed as title
   * @param collapseButtonAtLeft
   *          button at left?
   */
  public CollapsiblePanel(JComponent content, JLabel lblTitle, boolean collapseButtonAtLeft) {
    this(content, lblTitle, collapseButtonAtLeft, false);
  }

  /**
   * create the {@link CollapsiblePanel} with the button at the specified location, the label as {@link TmmLabel}, the specified state
   *
   * @param content
   *          the content to be displayed in the panel
   * @param title
   *          the title to be displayed via a {@link TmmLabel}
   * @param collapseButtonAtLeft
   *          button at left?
   * @param isCollapsed
   *          collapsed?
   */
  public CollapsiblePanel(JComponent content, String title, boolean collapseButtonAtLeft, boolean isCollapsed) {
    this(content, new TmmLabel(title), collapseButtonAtLeft, isCollapsed);
  }

  /**
   * create the {@link CollapsiblePanel} with the button at the specified location, the given {@link JLabel}, the specified state
   *
   * @param content
   *          the content to be displayed in the panel
   * @param lblTitle
   *          the label to be displayed as title
   * @param collapseButtonAtLeft
   *          button at left?
   * @param isCollapsed
   *          collapsed?
   */
  public CollapsiblePanel(JComponent content, JLabel lblTitle, boolean collapseButtonAtLeft, boolean isCollapsed) {
    super(new MigLayout("insets 0", "[grow]", "[][]"));
    this.content = content;
    setBackground(content.getBackground());

    JPanel panelTop = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));

    btnToggle = new FlatButton(IconManager.COLLAPSED); // init with this icon; will be set by setCollapsed()

    if (collapseButtonAtLeft) {
      panelTop.add(btnToggle, "cell 0 0, aligny top");
    }
    else {
      panelTop.add(btnToggle, "cell 1 0, aligny top");
    }

    if (lblTitle != null) {
      panelTop.add(lblTitle, "cell 0 0, growx");
    }

    panelTop.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Label.foreground")));
    add(panelTop, "cell 0 0, growx");

    btnToggle.addActionListener(e -> setCollapsed(!this.isCollapsed));
    setCollapsed(isCollapsed);
  }

  protected void setCollapsed(boolean collapse) {
    if (collapse) {
      remove(content);
    }
    else {
      add(content, "cell 0 1 2 1, grow");
    }

    isCollapsed = collapse;

    Icon icon = getIcon();
    if (icon != null) {
      btnToggle.setIcon(icon);
    }

    revalidate();
    repaint();
  }

  private Icon getIcon() {
    if (isCollapsed) {
      return IconManager.EXPANDED;
    }
    else {
      return IconManager.COLLAPSED;
    }
  }

  public boolean isCollapsed() {
    return isCollapsed;
  }

  public void expand() {
    if (isCollapsed) {
      setCollapsed(false);
    }
  }

  public void collapse() {
    if (!isCollapsed) {
      setCollapsed(true);
    }
  }
}
