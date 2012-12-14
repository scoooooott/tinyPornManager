package org.tinymediamanager.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import com.l2fprod.common.demo.ButtonBarMain;
import com.l2fprod.common.swing.JButtonBar;
import com.l2fprod.common.swing.plaf.misc.IconPackagerButtonBarUI;

public class ButtonBarPanel extends JPanel {

  private Component  currentComponent;
  private JButtonBar toolbar;

  public ButtonBarPanel() {
    toolbar = new JButtonBar(JButtonBar.VERTICAL);
    toolbar.setUI(new IconPackagerButtonBarUI());
    // toolbar.setUI(new BlueishButtonBarUI());
    setLayout(new BorderLayout());

    add("West", toolbar);

    ButtonGroup group = new ButtonGroup();

    addButton("Punkt 1", "/org/tinymediamanager/ui/images/show_reel.png", makePanel("Panel 1"), toolbar, group);
    addButton("Punkt 2", "/org/tinymediamanager/ui/images/Action-configure-icon.png", makePanel("Panel 2"), toolbar, group);
    addButton("Punkt 2.1", "", makePanel("Panel 3"), toolbar, group);
  }

  private JPanel makePanel(String title) {
    JPanel panel = new JPanel(new BorderLayout());
    JLabel top = new JLabel(title);
    top.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    top.setFont(top.getFont().deriveFont(Font.BOLD));
    top.setOpaque(true);
    top.setBackground(panel.getBackground().brighter());
    panel.add("North", top);
    panel.setPreferredSize(new Dimension(400, 300));
    panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    return panel;
  }

  private void show(Component component) {
    if (currentComponent != null) {
      remove(currentComponent);
    }
    add("Center", currentComponent = component);
    revalidate();
    repaint();
  }

  private void addButton(String title, String iconUrl, final Component component, JButtonBar bar, ButtonGroup group) {
    Action action = new AbstractAction(title, new ImageIcon(ButtonBarMain.class.getResource(iconUrl))) {
      public void actionPerformed(ActionEvent e) {
        show(component);
      }
    };

    JToggleButton button = new JToggleButton(action);
    bar.add(button);

    group.add(button);

    if (group.getSelection() == null) {
      button.setSelected(true);
      show(component);
    }
  }
}
