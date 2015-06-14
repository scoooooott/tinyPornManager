/*
 * Copyright 2012 - 2015 Manuel Laggner
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
package org.tinymediamanager;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class TmmOsUtils. Utility class for OS specific tasks
 * 
 * @author Manuel Laggner
 */
public class TmmOsUtils {
  private static final Logger LOGGER       = LoggerFactory.getLogger(TmmOsUtils.class);

  public static final String  DESKTOP_FILE = "tinyMediaManager.desktop";

  /**
   * create a .desktop file for linux and unix (not osx)
   * 
   * @param desktop
   *          .desktop file
   */
  public static void createDesktopFileForLinux(File desktop) {
    if (SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_MAC) {
      return;
    }

    // get the path in a safe way
    String path = new File(TinyMediaManager.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
    try {
      path = URLDecoder.decode(path, "UTF-8");
    }
    catch (UnsupportedEncodingException e1) {
      path = URLDecoder.decode(path);
    }
    StringBuilder sb = new StringBuilder(60);
    sb.append("[Desktop Entry]\n");
    sb.append("Type=Application\n");
    sb.append("Name=tinyMediaManager\n");
    sb.append("Path=");
    sb.append(path);
    sb.append('\n');
    sb.append("Exec=/bin/sh \"");
    sb.append(path);
    sb.append("/tinyMediaManager.sh\"\n");
    sb.append("Icon=");
    sb.append(path);
    sb.append("/tmm.png\n");
    sb.append("Categories=AudioVideo;Video;Database;Java;");
    sb.append("\n");
    FileWriterWithEncoding writer;
    try {
      writer = new FileWriterWithEncoding(desktop, "UTF-8");
      writer.write(sb.toString());
      writer.close();
      desktop.setExecutable(true);
    }
    catch (IOException e) {
      LOGGER.warn(e.getMessage());
    }
  }
}
