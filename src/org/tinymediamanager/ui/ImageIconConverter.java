package org.tinymediamanager.ui;

import java.net.URL;

import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.Converter;

public class ImageIconConverter extends Converter {

  @Override
  public Object convertForward(Object arg0) {
    if (arg0 instanceof String && !StringUtils.isEmpty((String) arg0)) {
      URL imageURL = MoviePanel.class.getResource("images/Checkmark.png");
      if (imageURL != null) {
        return new ImageIcon(imageURL);
      }
    }
    else {
      URL imageURL = MoviePanel.class.getResource("images/Cross.png");
      if (imageURL != null) {
        return new ImageIcon(imageURL);
      }
    }

    return null;
  }

  @Override
  public Object convertReverse(Object arg0) {
    // TODO Auto-generated method stub
    return null;
  }

}
