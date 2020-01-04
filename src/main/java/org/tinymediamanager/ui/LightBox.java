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
package org.tinymediamanager.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.ImageLabel.Position;

import net.miginfocom.swing.MigLayout;

/**
 * The class LightBox is used to show images in a lightbox.
 * 
 * @author Manuel Laggner
 */
public class LightBox {
  private JPanel     backgroundPanel;
  private JPanel     imagePanel;
  private ImageLabel image;
  private JFrame     frame;

  private LightBox(JFrame parent, String url, String path) {
    this.frame = parent;

    backgroundPanel = new JPanel() {
      private static final long serialVersionUID = -1543129046893570172L;

      @Override
      protected void paintComponent(Graphics g1) {
        super.paintComponent(g1);
        Graphics2D g = (Graphics2D) g1;
        g.setPaint(Color.black);
        Composite savedComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setComposite(savedComposite);
      }
    };
    backgroundPanel.setOpaque(false);
    backgroundPanel.setSize(Toolkit.getDefaultToolkit().getScreenSize());

    imagePanel = new JPanel();
    imagePanel.setLayout(new MigLayout("", "[75lp,grow]", "[75lp,grow]"));
    imagePanel.setSize(new Dimension((int) (frame.getContentPane().getWidth() * 0.95), (int) (frame.getContentPane().getHeight() * 0.95)));
    imagePanel.setOpaque(false);

    image = new ImageLabel(true);
    image.setPreferCache(false);
    image.setIsLightbox(true);
    image.setPosition(Position.CENTER);
    image.setCacheUrl(true);

    // run later to avoid strange loading artefacts
    SwingUtilities.invokeLater(() -> {
      if (StringUtils.isNotBlank(path)) {
        image.setImagePath(path);
      }
      else if (StringUtils.isNotBlank(url)) {
        image.setImageUrl(url);
      }
    });

    imagePanel.add(image, "cell 0 0,grow");

    backgroundPanel.addMouseListener(new MouseListener() {
      @Override
      public void mouseReleased(MouseEvent e) {
      }

      @Override
      public void mousePressed(MouseEvent e) {
      }

      @Override
      public void mouseExited(MouseEvent e) {
      }

      @Override
      public void mouseEntered(MouseEvent e) {
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        frame.getLayeredPane().remove(imagePanel);
        frame.getLayeredPane().remove(backgroundPanel);
        frame.validate();
        frame.repaint();
      }
    });
  }

  private void show() {
    frame.getLayeredPane().add(backgroundPanel, JLayeredPane.PALETTE_LAYER);
    imagePanel.setLocation(((frame.getLayeredPane().getWidth() - imagePanel.getWidth()) / 2),
        (frame.getLayeredPane().getHeight() - imagePanel.getHeight()) / 2);
    frame.getLayeredPane().add(imagePanel, JLayeredPane.POPUP_LAYER);
  }

  public static void showLightBox(JFrame frame, String path, String url) {
    LightBox lightBox = new LightBox(frame, url, path);
    lightBox.show();
  }
}
