/*
 * Copyright 2012 Manuel Laggner
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

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.Converter;

/**
 * The Class ImageIconConverter.
 * 
 * @author Manuel Laggner
 */
public class MediaInfoVideoFormatConverter extends Converter<String, Icon> {

  /** The Constant LOGGER. */
  private static final Logger   LOGGER     = Logger.getLogger(MediaInfoVideoFormatConverter.class);

  /** The Constant emptyImage. */
  public final static ImageIcon emptyImage = new ImageIcon();

  /*
   * (non-Javadoc)
   * 
   * @see org.jdesktop.beansbinding.Converter#convertForward(java.lang.Object)
   */
  @Override
  public Icon convertForward(String arg0) {
    // try to get the image file

    // a) return null if the Format is empty
    if (StringUtils.isEmpty(arg0)) {
      return null;
    }

    try {
      URL file = null;

      // check 1080p
      if (arg0.contains("1080")) {
        // try to load 1080p.png
        file = MediaInfoVideoFormatConverter.class.getResource("/images/mediainfo/video/1080p.png");
      }

      // check 720p
      if (arg0.contains("720")) {
        // try to load 720p.png
        file = MediaInfoVideoFormatConverter.class.getResource("/images/mediainfo/video/720p.png");
      }

      // everything else is SD
      if (arg0.contains("16:9")) {
        // try to load sd169.png
        file = MediaInfoVideoFormatConverter.class.getResource("/images/mediainfo/video/sd169.png");
      }
      if (arg0.contains("4:3")) {
        // try to load sd43.png
        file = MediaInfoVideoFormatConverter.class.getResource("/images/mediainfo/video/sd43.png");
      }

      // return image
      if (file != null) {
        return new ImageIcon(file);
      }
    }
    catch (Exception e) {
      LOGGER.warn(e.getMessage());
    }

    // we did not get any file: return the empty
    return emptyImage;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jdesktop.beansbinding.Converter#convertReverse(java.lang.Object)
   */
  @Override
  public String convertReverse(Icon arg0) {
    return null;
  }

}
