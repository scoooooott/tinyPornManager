package org.tinymediamanager.ui.components;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class NotificationMessage extends JPanel implements ActionListener {
  private static final long serialVersionUID = -3575919232938540443L;

  private JLabel            lblMessage;

  private float             opacity          = 0f;
  private Timer             fadeTimer;

  public NotificationMessage(String text) {
    lblMessage = new JLabel(text);
    add(lblMessage);
    beginFade();
  }

  public void beginFade() {
    fadeTimer = new javax.swing.Timer(75, this);
    fadeTimer.setInitialDelay(0);
    fadeTimer.start();
  }

  public void actionPerformed(ActionEvent e) {
    opacity += .03;
    if (opacity > 1) {
      opacity = 1;
      fadeTimer.stop();
      fadeTimer = null;
    }
    repaint();
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
    g.setColor(getBackground());
    g.fillRect(getX(), getY(), getWidth(), getHeight());
  }

}
