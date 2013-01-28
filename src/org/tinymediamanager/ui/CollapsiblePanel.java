package org.tinymediamanager.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * The user-triggered collapsible panel containing the component (trigger) in
 * the titled border
 */
public class CollapsiblePanel extends JPanel {
  public static final long serialVersionUID    = -343234;

  CollapsibleTitledBorder  border;                                                           // includes
                                                                                              // upper
                                                                                              // left
                                                                                              // component
                                                                                              // and
                                                                                              // line
                                                                                              // type
  Border                   collapsedBorderLine = BorderFactory.createEmptyBorder(2, 2, 2, 2); // no
                                                                                              // border
  Border                   expandedBorderLine  = null;                                       // default
                                                                                              // is
                                                                                              // used,
                                                                                              // etched
                                                                                              // lowered
                                                                                              // border
                                                                                              // on
                                                                                              // MAC????
  AbstractButton           titleComponent;                                                   // displayed
                                                                                              // in
                                                                                              // the
                                                                                              // titled
                                                                                              // border
  final static int         COLLAPSED           = 0, EXPANDED = 1;                            // Expand/Collapse
                                                                                              // button,image
                                                                                              // States
  ImageIcon[]              iconArrow           = createExpandAndCollapseIcon();
  JButton                  arrow               = createArrowButton();                         // the
                                                                                              // arrow
  protected JPanel         panel;
  boolean                  collapsed;                                                        // stores
                                                                                              // current
                                                                                              // state
                                                                                              // of
                                                                                              // the
                                                                                              // collapsible
                                                                                              // panel

  /**
   * Constructor, using a group of option radio buttons to control the
   * collapsible panel. The buttons should be created, grouped, and then used to
   * construct their own collapsible panels.
   * 
   * @param component
   *          Radio button that expands and collapses the panel based on if it
   *          is selected or not
   */
  public CollapsiblePanel(JRadioButton component) {
    component.addItemListener(new CollapsiblePanel.ExpandAndCollapseAction());
    titleComponent = component;
    collapsed = !component.isSelected();
    commonConstructor();
  }

  /**
   * Constructor, using a label/button to control the collapsible panel.
   * 
   * @param text
   *          Title of the collapsible panel in string format, used to create a
   *          button with text and an arrow icon
   */
  public CollapsiblePanel(String text) {
    arrow.setText(text);
    titleComponent = arrow;
    collapsed = false;
    commonConstructor();
  }

  /**
   * Constructor, using a group of button to control the collapsible panel while
   * will a label text.
   * 
   * @param text
   *          Title of the collapsible panel in string format, used to create a
   *          button with text and an arrow icon
   */
  public CollapsiblePanel(String text, JRadioButton component) {
    collapsed = !component.isSelected();
    // component.addItemListener(new
    // CollapsiblePanel.ExpandAndCollapseAction());
    // arrow.setText(text);
    // /if(!collapsed)
    titleComponent = arrow;
    // else titleComponent=null;

    setLayout(new BorderLayout());
    JLabel label = new JLabel(text);
    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    add(label, BorderLayout.CENTER);
    add(titleComponent, BorderLayout.CENTER);
    add(panel, BorderLayout.CENTER);
    setCollapsed(collapsed);
    placeTitleComponent();
  }

  /**
   * Sets layout, creates the content panel and adds it and the title component
   * to the container, all constructors have this procedure in common.
   */
  private void commonConstructor() {
    setLayout(new BorderLayout());
    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    add(titleComponent, BorderLayout.CENTER);
    add(panel, BorderLayout.CENTER);
    setCollapsed(collapsed);
    placeTitleComponent();
  }

  /**
   * Sets the bounds of the border title component so that it is properly
   * positioned.
   */
  private void placeTitleComponent() {
    Insets insets = this.getInsets();
    Rectangle containerRectangle = this.getBounds();
    Rectangle componentRectangle = border.getComponentRect(containerRectangle, insets);
    titleComponent.setBounds(componentRectangle);
  }

  public void setTitleComponentText(String text) {
    if (titleComponent instanceof JButton) {
      titleComponent.setText(text);
    }
    placeTitleComponent();
  }

  public JPanel getContentPane() {
    return panel;
  }

  /**
   * Collapses or expands the panel. add or remove the content pane, alternate
   * between a frame and empty border, and change the title arrow. The current
   * state is stored in the collapsed boolean.
   * 
   * @param collapse
   *          When set to true, the panel is collapsed, else it is expanded
   */
  public void setCollapsed(boolean collapse) {
    if (collapse) {
      // collapse the panel, remove content and set border to empty border
      remove(panel);
      arrow.setIcon(iconArrow[COLLAPSED]);
      border = new CollapsibleTitledBorder(collapsedBorderLine, titleComponent);
    }
    else {
      // expand the panel, add content and set border to titled border
      add(panel, BorderLayout.NORTH);
      arrow.setIcon(iconArrow[EXPANDED]);
      border = new CollapsibleTitledBorder(expandedBorderLine, titleComponent);
    }
    setBorder(border);
    collapsed = collapse;
    updateUI();
  }

  public boolean isCollapsed() {
    return collapsed;
  }

  /**
   * Returns an ImageIcon array with arrow images used for the different states
   * of the panel.
   * 
   * @return iconArrow An ImageIcon array holding the collapse and expanded
   *         versions of the right hand side arrow
   */
  private ImageIcon[] createExpandAndCollapseIcon() {
    ImageIcon[] iconArrow = new ImageIcon[2];
    URL iconURL;
    iconURL = CollapsiblePanel.class.getResource("images/arrow_collapsed.gif");
    if (iconURL != null) {
      iconArrow[COLLAPSED] = new ImageIcon(iconURL);
    }
    iconURL = CollapsiblePanel.class.getResource("images/arrow_expanded.gif");
    if (iconURL != null) {
      iconArrow[EXPANDED] = new ImageIcon(iconURL);
    }
    return iconArrow;
  }

  /**
   * Returns a button with an arrow icon and a collapse/expand action listener.
   */
  private JButton createArrowButton() {
    JButton button = new JButton("arrow", iconArrow[COLLAPSED]);
    button.setBorder(BorderFactory.createEmptyBorder(0, 1, 5, 1));
    button.setVerticalTextPosition(AbstractButton.CENTER);
    button.setHorizontalTextPosition(AbstractButton.LEFT);
    button.setMargin(new Insets(0, 0, 3, 0));

    // Use the same font as that used in the titled border font
    Font font = BorderFactory.createTitledBorder("Instance").getTitleFont();
    Color color = BorderFactory.createTitledBorder("Instance").getTitleColor();
    button.setFont(font);
    button.setForeground(color);
    button.setFocusable(false);
    button.setContentAreaFilled(false);
    button.addActionListener(new CollapsiblePanel.ExpandAndCollapseAction());
    return button;
  }

  /**
   * Expanding or collapsing of extra content on the user's click of the
   * titledBorder component.
   */
  private class ExpandAndCollapseAction extends AbstractAction implements ActionListener, ItemListener {
    public static final long serialVersionUID = -343231;

    public void actionPerformed(ActionEvent e) {
      setCollapsed(!isCollapsed());
    }

    public void itemStateChanged(ItemEvent e) {
      setCollapsed(!isCollapsed());
    }
  }

  /**
   * Special titled border that includes a component in the title area
   */
  private class CollapsibleTitledBorder extends TitledBorder {
    public static final long serialVersionUID = -343230;
    JComponent               component;

    public CollapsibleTitledBorder(JComponent component) {
      this(null, component, LEFT, TOP);
    }

    public CollapsibleTitledBorder(Border border) {
      this(border, null, LEFT, TOP);
    }

    public CollapsibleTitledBorder(Border border, JComponent component) {
      this(border, component, LEFT, TOP);
    }

    public CollapsibleTitledBorder(Border border, JComponent component, int titleJustification, int titlePosition) {
      // TitledBorder needs border, title, justification, position, font, and
      // color
      super(border, null, titleJustification, titlePosition, null, null);
      this.component = component;
      if (border == null) {
        this.border = super.getBorder();
      }
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      Rectangle borderR = new Rectangle(x + EDGE_SPACING, y + EDGE_SPACING, width - (EDGE_SPACING * 2), height - (EDGE_SPACING * 2));
      Insets borderInsets;
      if (border != null) {
        borderInsets = border.getBorderInsets(c);
      }
      else {
        borderInsets = new Insets(0, 0, 0, 0);
      }

      Rectangle rect = new Rectangle(x, y, width, height);
      Insets insets = getBorderInsets(c);
      Rectangle compR = getComponentRect(rect, insets);
      int diff;
      switch (titlePosition) {
        case ABOVE_TOP:
          diff = compR.height + TEXT_SPACING;
          borderR.y += diff;
          borderR.height -= diff;
          break;
        case TOP:
        case DEFAULT_POSITION:
          diff = insets.top / 2 - borderInsets.top - EDGE_SPACING;
          borderR.y += diff;
          borderR.height -= diff;
          break;
        case BELOW_TOP:
        case ABOVE_BOTTOM:
          break;
        case BOTTOM:
          diff = insets.bottom / 2 - borderInsets.bottom - EDGE_SPACING;
          borderR.height -= diff;
          break;
        case BELOW_BOTTOM:
          diff = compR.height + TEXT_SPACING;
          borderR.height -= diff;
          break;
      }
      border.paintBorder(c, g, borderR.x, borderR.y, borderR.width, borderR.height);
      Color col = g.getColor();
      g.setColor(c.getBackground());
      g.fillRect(compR.x, compR.y, compR.width, compR.height);
      g.setColor(col);
    }

    public Insets getBorderInsets(Component c, Insets insets) {
      Insets borderInsets;
      if (border != null) {
        borderInsets = border.getBorderInsets(c);
      }
      else {
        borderInsets = new Insets(0, 0, 0, 0);
      }
      insets.top = EDGE_SPACING + TEXT_SPACING + borderInsets.top;
      insets.right = EDGE_SPACING + TEXT_SPACING + borderInsets.right;
      insets.bottom = EDGE_SPACING + TEXT_SPACING + borderInsets.bottom;
      insets.left = EDGE_SPACING + TEXT_SPACING + borderInsets.left;

      if (c == null || component == null) {
        return insets;
      }

      int compHeight = component.getPreferredSize().height;

      switch (titlePosition) {
        case ABOVE_TOP:
          insets.top += compHeight + TEXT_SPACING;
          break;
        case TOP:
        case DEFAULT_POSITION:
          insets.top += Math.max(compHeight, borderInsets.top) - borderInsets.top;
          break;
        case BELOW_TOP:
          insets.top += compHeight + TEXT_SPACING;
          break;
        case ABOVE_BOTTOM:
          insets.bottom += compHeight + TEXT_SPACING;
          break;
        case BOTTOM:
          insets.bottom += Math.max(compHeight, borderInsets.bottom) - borderInsets.bottom;
          break;
        case BELOW_BOTTOM:
          insets.bottom += compHeight + TEXT_SPACING;
          break;
      }
      return insets;
    }

    public JComponent getTitleComponent() {
      return component;
    }

    public void setTitleComponent(JComponent component) {
      this.component = component;
    }

    public Rectangle getComponentRect(Rectangle rect, Insets borderInsets) {
      Dimension compD = component.getPreferredSize();
      Rectangle compR = new Rectangle(0, 0, compD.width, compD.height);
      switch (titlePosition) {
        case ABOVE_TOP:
          compR.y = EDGE_SPACING;
          break;
        case TOP:
        case DEFAULT_POSITION:
          if (titleComponent instanceof JButton) {
            compR.y = EDGE_SPACING + (borderInsets.top - EDGE_SPACING - TEXT_SPACING - compD.height) / 2;
          }
          else if (titleComponent instanceof JRadioButton) {
            compR.y = (borderInsets.top - EDGE_SPACING - TEXT_SPACING - compD.height) / 2;
          }
          break;
        case BELOW_TOP:
          compR.y = borderInsets.top - compD.height - TEXT_SPACING;
          break;
        case ABOVE_BOTTOM:
          compR.y = rect.height - borderInsets.bottom + TEXT_SPACING;
          break;
        case BOTTOM:
          compR.y = rect.height - borderInsets.bottom + TEXT_SPACING + (borderInsets.bottom - EDGE_SPACING - TEXT_SPACING - compD.height) / 2;
          break;
        case BELOW_BOTTOM:
          compR.y = rect.height - compD.height - EDGE_SPACING;
          break;
      }
      switch (titleJustification) {
        case LEFT:
        case DEFAULT_JUSTIFICATION:
          // compR.x = TEXT_INSET_H + borderInsets.left;
          compR.x = TEXT_INSET_H + borderInsets.left - EDGE_SPACING;
          break;
        case RIGHT:
          compR.x = rect.width - borderInsets.right - TEXT_INSET_H - compR.width;
          break;
        case CENTER:
          compR.x = (rect.width - compR.width) / 2;
          break;
      }
      return compR;
    }
  }
}