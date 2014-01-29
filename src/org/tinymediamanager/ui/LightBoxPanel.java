package org.tinymediamanager.ui;

import harsh.p.raval.lightbox.LightBox;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.ImageLabel.Position;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class LightBoxPanel extends JPanel {
  private static final long serialVersionUID = -674987974365646512L;

  private ImageLabel        image;
  private LightBox          lightBox;
  private JFrame            frame;

  public LightBoxPanel() {
    lightBox = new LightBox();
    setOpaque(false);
    setLayout(new BorderLayout(0, 0));
    {
      JPanel panel = new JPanel();
      panel.setOpaque(false);
      add(panel);
      panel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("75px:grow"), }, new RowSpec[] { RowSpec.decode("fill:75px:grow"), }));
      {
        image = new ImageLabel(true);
        image.setUseCache(false);
        image.setPosition(Position.CENTER);
        panel.add(image, "1, 1, fill, fill");
      }
    }
    addMouseListener(new MouseListener() {
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
        lightBox.closeLightBox(frame, LightBoxPanel.this);
        image.setImageUrl("");
        image.setImagePath("");
      }
    });
  }

  public void setImageLocation(String path, String url) {
    image.setImageUrl(url);
    image.setImagePath(path);
  }

  public void showLightBox(JFrame frame) {
    this.frame = frame;
    setSize(new Dimension((int) (frame.getContentPane().getWidth() * 0.95), (int) (frame.getContentPane().getHeight() * 0.95)));
    lightBox.createLightBoxEffect(frame, this);
  }
}
