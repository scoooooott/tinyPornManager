package org.tinymediamanager.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

/**
 * 
 * @author rgd
 */
public class CollapsiblePanel extends JPanel {

  String            title;
  TitledBorder      border;
  int               titleHeight;

  MouseListener     mouseListener;
  ComponentListener contentComponentListener;

  public CollapsiblePanel(String title) {
    super();

    mouseListener = new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        // only toggle, if the upper
        // border was clicked
        if (e.getY() < titleHeight) {
          toggleVisibility();
        }
      }
    };

    contentComponentListener = new ComponentAdapter() {
      @Override
      public void componentShown(ComponentEvent e) {
        updateBorderTitle();
      }

      @Override
      public void componentHidden(ComponentEvent e) {
        updateBorderTitle();
      }
    };

    this.title = title;
    border = BorderFactory.createTitledBorder(title);

    // calculate title height
    FontMetrics fm = getFontMetrics(UIManager.getFont("Label.font"));
    titleHeight = fm.getHeight();
    // titleHeight = 16;

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