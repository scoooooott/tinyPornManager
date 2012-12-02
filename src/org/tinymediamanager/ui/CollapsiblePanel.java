package org.tinymediamanager.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * 
 * @author rgd
 */
public class CollapsiblePanel extends JPanel {

  String            title;
  TitledBorder      border;

  MouseListener     mouseListener            = new MouseAdapter() {
                                               @Override
                                               public void mouseClicked(MouseEvent e) {
                                                 // only toggle, if the upper
                                                 // border was clicked
                                                 if (e.getY() < 16) {
                                                   toggleVisibility();
                                                 }
                                               }
                                             };

  ComponentListener contentComponentListener = new ComponentAdapter() {
                                               @Override
                                               public void componentShown(ComponentEvent e) {
                                                 updateBorderTitle();
                                               }

                                               @Override
                                               public void componentHidden(ComponentEvent e) {
                                                 updateBorderTitle();
                                               }
                                             };

  public CollapsiblePanel(String title) {
    this.title = title;
    border = BorderFactory.createTitledBorder(title);
    setBorder(border);
    BorderLayout borderLayout = new BorderLayout();
    setLayout(borderLayout);
    addMouseListener(mouseListener);
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    firePropertyChange("title", this.title, this.title = title);
  }

  @Override
  public Component add(Component comp) {
    comp.addComponentListener(contentComponentListener);
    Component r = super.add(comp);
    updateBorderTitle();
    return r;
  }

  @Override
  public Component add(String name, Component comp) {
    comp.addComponentListener(contentComponentListener);
    Component r = super.add(name, comp);
    updateBorderTitle();
    return r;
  }

  @Override
  public Component add(Component comp, int index) {
    comp.addComponentListener(contentComponentListener);
    Component r = super.add(comp, index);
    updateBorderTitle();
    return r;
  }

  @Override
  public void add(Component comp, Object constraints) {
    comp.addComponentListener(contentComponentListener);
    super.add(comp, constraints);
    updateBorderTitle();
  }

  @Override
  public void add(Component comp, Object constraints, int index) {
    comp.addComponentListener(contentComponentListener);
    super.add(comp, constraints, index);
    updateBorderTitle();
  }

  @Override
  public void remove(int index) {
    Component comp = getComponent(index);
    comp.removeComponentListener(contentComponentListener);
    super.remove(index);
  }

  @Override
  public void remove(Component comp) {
    comp.removeComponentListener(contentComponentListener);
    super.remove(comp);
  }

  @Override
  public void removeAll() {
    for (Component c : getComponents()) {
      c.removeComponentListener(contentComponentListener);
    }
    super.removeAll();
  }

  public void toggleVisibility() {
    toggleVisibility(hasInvisibleComponent());
  }

  public void toggleVisibility(boolean visible) {
    for (Component c : getComponents()) {
      c.setVisible(visible);
    }
    updateBorderTitle();
  }

  protected void updateBorderTitle() {
    String arrow = "";
    if (getComponentCount() > 0) {
      arrow = (hasInvisibleComponent() ? "▽" : "△");
    }
    border.setTitle(title + " " + arrow);
    repaint();
  }

  protected final boolean hasInvisibleComponent() {
    for (Component c : getComponents()) {
      if (!c.isVisible()) {
        return true;
      }
    }
    return false;
  }

}

// /*
// * Copyright 2012 Manuel Laggner
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
// package org.tinymediamanager.ui;
//
// import java.awt.BorderLayout;
// import java.awt.Component;
// import java.awt.event.ComponentAdapter;
// import java.awt.event.ComponentEvent;
// import java.awt.event.ComponentListener;
// import java.awt.event.MouseAdapter;
// import java.awt.event.MouseEvent;
// import java.awt.event.MouseListener;
//
// import javax.swing.JLabel;
// import javax.swing.JPanel;
// import javax.swing.border.TitledBorder;
//
// // TODO: Auto-generated Javadoc
// /**
// * The Class CollapsiblePanel.
// *
// * @author rgd
// */
// public class CollapsiblePanel extends JPanel {
//
// /** The title. */
// String title;
//
// /** The border. */
// TitledBorder border;
//
// protected JPanel panelContent;
// protected JLabel lblTitle;
//
// /** The mouse listener. */
// private MouseListener mouseListener = new MouseAdapter() {
// @Override
// public void mouseClicked(MouseEvent e) {
// toggleVisibility();
// }
// };
//
// /** The content component listener. */
// private ComponentListener contentComponentListener = new ComponentAdapter() {
// @Override
// public void componentShown(ComponentEvent e) {
// updateBorderTitle();
// }
//
// @Override
// public void componentHidden(ComponentEvent e) {
// updateBorderTitle();
// }
// };
//
// /**
// * Instantiates a new collapsible panel.
// *
// * @param title
// * the title
// */
// public CollapsiblePanel(String title) {
// this.title = title;
// // border = BorderFactory.createTitledBorder("");
// // border = new ScrollPaneBorder(false);
// // setBorder(border);
// BorderLayout borderLayout = new BorderLayout();
// super.setLayout(borderLayout);
//
// lblTitle = new JLabel(this.title);
// super.add(lblTitle, BorderLayout.NORTH);
//
// panelContent = new JPanel();
// super.add(panelContent, BorderLayout.CENTER);
// lblTitle.addMouseListener(mouseListener);
// }
//
// /**
// * Gets the title.
// *
// * @return the title
// */
// public String getTitle() {
// return title;
// }
//
// /**
// * Sets the title.
// *
// * @param title
// * the new title
// */
// public void setTitle(String title) {
// String oldValue = lblTitle.getText();
// lblTitle.setText(title);
// firePropertyChange("title", oldValue, title);
// }
//
// /*
// * (non-Javadoc)
// *
// * @see java.awt.Container#add(java.awt.Component)
// */
// @Override
// public Component add(Component comp) {
// comp.addComponentListener(contentComponentListener);
// Component r = panelContent.add(comp);
// updateBorderTitle();
// return r;
// }
//
// /*
// * (non-Javadoc)
// *
// * @see java.awt.Container#add(java.lang.String, java.awt.Component)
// */
// @Override
// public Component add(String name, Component comp) {
// comp.addComponentListener(contentComponentListener);
// Component r = panelContent.add(name, comp);
// updateBorderTitle();
// return r;
// }
//
// /*
// * (non-Javadoc)
// *
// * @see java.awt.Container#add(java.awt.Component, int)
// */
// @Override
// public Component add(Component comp, int index) {
// comp.addComponentListener(contentComponentListener);
// Component r = panelContent.add(comp, index);
// updateBorderTitle();
// return r;
// }
//
// /*
// * (non-Javadoc)
// *
// * @see java.awt.Container#add(java.awt.Component, java.lang.Object)
// */
// @Override
// public void add(Component comp, Object constraints) {
// comp.addComponentListener(contentComponentListener);
// panelContent.add(comp, constraints);
// updateBorderTitle();
// }
//
// /*
// * (non-Javadoc)
// *
// * @see java.awt.Container#add(java.awt.Component, java.lang.Object, int)
// */
// @Override
// public void add(Component comp, Object constraints, int index) {
// comp.addComponentListener(contentComponentListener);
// panelContent.add(comp, constraints, index);
// updateBorderTitle();
// }
//
// /*
// * (non-Javadoc)
// *
// * @see java.awt.Container#remove(int)
// */
// @Override
// public void remove(int index) {
// Component comp = getComponent(index);
// comp.removeComponentListener(contentComponentListener);
// panelContent.remove(index);
// }
//
// /*
// * (non-Javadoc)
// *
// * @see java.awt.Container#remove(java.awt.Component)
// */
// @Override
// public void remove(Component comp) {
// comp.removeComponentListener(contentComponentListener);
// panelContent.remove(comp);
// }
//
// /*
// * (non-Javadoc)
// *
// * @see java.awt.Container#removeAll()
// */
// @Override
// public void removeAll() {
// for (Component c : getComponents()) {
// c.removeComponentListener(contentComponentListener);
// }
// panelContent.removeAll();
// }
//
// /**
// * Toggle visibility.
// */
// protected void toggleVisibility() {
// toggleVisibility(hasInvisibleComponent());
// }
//
// /**
// * Toggle visibility.
// *
// * @param visible
// * the visible
// */
// protected void toggleVisibility(boolean visible) {
// for (Component c : panelContent.getComponents()) {
// c.setVisible(visible);
// }
// updateBorderTitle();
// }
//
// /**
// * Update border title.
// */
// protected void updateBorderTitle() {
// String arrow = "";
// if (panelContent.getComponentCount() > 0) {
// arrow = (hasInvisibleComponent() ? "▽" : "△");
// }
// // border.setTitle(title + " " + arrow);
// setTitle(title + " " + arrow);
// repaint();
// }
//
// /**
// * Checks for invisible component.
// *
// * @return true, if successful
// */
// protected final boolean hasInvisibleComponent() {
// for (Component c : panelContent.getComponents()) {
// if (!c.isVisible()) {
// return true;
// }
// }
// return false;
// }
//
// }
