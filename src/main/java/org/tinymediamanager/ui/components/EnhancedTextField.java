package org.tinymediamanager.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The class EnhancedTextField is used to create a JTextField with<br>
 * - an icon on the right side<br>
 * and/or<br>
 * - a default text when it is not focused
 * 
 * @author Manuel Laggner
 */
public class EnhancedTextField extends JTextField implements FocusListener {
  private static final long           serialVersionUID = 5397356153111919435L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  protected JLabel                    lblIcon;
  private String                      textWhenNotFocused;
  private Insets                      dummyInsets;

  /**
   * just create a simple JTextField
   */
  public EnhancedTextField() {
    this(null, null);
  }

  /**
   * create a JTextField showing a text when not focused and nothing entered
   * 
   * @param textWhenNotFocused
   *          the text to be shown
   */
  public EnhancedTextField(String textWhenNotFocused) {
    this(textWhenNotFocused, null);
  }

  /**
   * create a JTextField with an image on the right side
   * 
   * @param icon
   *          the icon to be shown
   */
  public EnhancedTextField(Icon icon) {
    this(null, icon);
  }

  /**
   * create a JTextField showing a text when not focused and nothing entered and an image to the right
   * 
   * @param textWhenNotFocused
   *          the text to be shown
   * @param icon
   *          the icon to be shown
   */
  public EnhancedTextField(String textWhenNotFocused, Icon icon) {
    super();

    if (icon != null) {
      setLayout(new BorderLayout());
      lblIcon = new JLabel(icon);
      add(lblIcon, BorderLayout.EAST);
    }

    if (textWhenNotFocused != null) {
      this.textWhenNotFocused = textWhenNotFocused;
    }
    else {
      this.textWhenNotFocused = "";
    }

    if (StringUtils.isNotBlank(textWhenNotFocused)) {
      this.addFocusListener(this);
    }

    Border border = UIManager.getBorder("TextField.border");
    JTextField dummy = new JTextField();
    this.dummyInsets = border.getBorderInsets(dummy);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    int textX = 2;

    setMargin(new Insets(2, textX, 2, 2));

    if (!this.hasFocus() && StringUtils.isEmpty(this.getText())) {
      int height = this.getHeight();
      Font prev = g.getFont();
      Font italic = prev.deriveFont(Font.ITALIC);
      Color prevColor = g.getColor();
      g.setFont(italic);
      g.setColor(UIManager.getColor("textInactiveText"));
      int h = g.getFontMetrics().getHeight();
      int textBottom = (height - h) / 2 + h - 4;
      int x = this.getInsets().left;
      Graphics2D g2d = (Graphics2D) g;
      RenderingHints hints = g2d.getRenderingHints();
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2d.drawString(textWhenNotFocused, x, textBottom);
      g2d.setRenderingHints(hints);
      g.setFont(prev);
      g.setColor(prevColor);
    }
  }

  @Override
  public void focusGained(FocusEvent e) {
    this.repaint();
  }

  @Override
  public void focusLost(FocusEvent e) {
    this.repaint();
  }

  /**
   * create a predefined search text field
   * 
   * @return the JTextField for searching
   */
  public static EnhancedTextField createSearchTextField() {
    EnhancedTextField textField = new EnhancedTextField(BUNDLE.getString("tmm.searchfield"), IconManager.SEARCH_GREY); //$NON-NLS-1$
    textField.lblIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    textField.lblIcon.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (StringUtils.isNotBlank(textField.getText())) {
          textField.setText("");
        }
      }
    });
    textField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        changeIcon();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        changeIcon();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        changeIcon();
      }

      private void changeIcon() {
        if (StringUtils.isBlank(textField.getText())) {
          textField.lblIcon.setIcon(IconManager.SEARCH_GREY);
        }
        else {
          textField.lblIcon.setIcon(IconManager.CLEAR_GREY);
        }
      }
    });
    return textField;
  }
}
