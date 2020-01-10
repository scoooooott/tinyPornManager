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
package org.tinymediamanager.ui.converter;

import java.net.URL;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.jdesktop.beansbinding.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.Settings;

/**
 * The Class CertificationImageConverter.
 * 
 * @author Manuel Laggner
 */
public class CertificationImageConverter extends Converter<MediaCertification, Icon> {
  private static final Logger   LOGGER     = LoggerFactory.getLogger(CertificationImageConverter.class);

  public static final ImageIcon emptyImage = new ImageIcon();

  @Override
  public Icon convertForward(MediaCertification cert) {
    // we have no certification here
    if (cert == null || cert == MediaCertification.UNKNOWN) {
      return null;
    }
    // try to find an image for this genre
    try {
      StringBuilder sb = new StringBuilder(
          "/org/tinymediamanager/ui/plaf/" + Settings.getInstance().getTheme().toLowerCase(Locale.ROOT) + "/images/certification/");
      sb.append(cert.name().toLowerCase(Locale.ROOT));
      sb.append(".png");

      URL file = getClass().getResource(sb.toString());
      if (file != null) {
        return new ImageIcon(file);
      }
    }
    catch (Exception e) {
      LOGGER.warn("cannot convert certification", e);
    }

    return emptyImage;
  }

  @Override
  public MediaCertification convertReverse(Icon arg0) {
    return null;
  }
}
