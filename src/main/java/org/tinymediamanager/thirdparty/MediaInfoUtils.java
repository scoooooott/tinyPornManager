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

package org.tinymediamanager.thirdparty;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Utils;

import com.sun.jna.Platform;

public class MediaInfoUtils {
  public static final boolean USE_LIBMEDIAINFO = useMediaInfo();

  private static final Logger LOGGER           = LoggerFactory.getLogger(MediaInfoUtils.class);

  private MediaInfoUtils() {
    // private constructor for utility classes
  }

  /**
   * checks if we should use libMediaInfo
   * 
   * @return true/false
   */
  private static boolean useMediaInfo() {
    return Boolean.parseBoolean(System.getProperty("tmm.uselibmediainfo", "true"));
  }

  /**
   * load media info from /native/*
   */
  public static void loadMediaInfo() {
    if (!USE_LIBMEDIAINFO) {
      return;
    }

    try {
      String miv = "";
      String nativepath = "native/";

      // windows
      if (Platform.isWindows()) {
        nativepath += "windows";
      }
      // linux
      else if (Platform.isLinux()) {
        nativepath += "linux";
      }
      // osx
      else if (Platform.isMac()) {
        nativepath += "mac";
      }

      // mac uses the same lib for 32 and 64 bit
      if (!Platform.isMac()) {
        // https://en.wikipedia.org/wiki/X86-64
        if (Platform.is64Bit()) {
          nativepath += "-x64";
        }
        else {
          nativepath += "-x86";
        }
      }

      // need that, since we cannot try and reload/unload a Class
      // MI does not load over UNC, so copy to temp
      if (System.getProperty("user.dir", "").startsWith("\\\\") || System.getProperty("user.dir", "").startsWith("//")) {
        LOGGER.debug("We're on a network UNC path!");
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"), "tmm");
        Path nativeDir = tmpDir.resolve(nativepath).toAbsolutePath();
        Utils.copyDirectoryRecursive(Paths.get(nativepath), nativeDir);

        System.setProperty("jna.library.path", nativeDir.toString()); // MI
        System.setProperty("org.lwjgl.librarypath", nativeDir.toString()); // nfd
        LOGGER.debug("Loading native libs from: {}", nativeDir.toString());
      }
      else {
        System.setProperty("jna.library.path", nativepath); // MI
        System.setProperty("org.lwjgl.librarypath", nativepath); // nfd
        LOGGER.debug("Loading native libs from: {}", nativepath);
      }

      miv = MediaInfo.version(); // load class

      if (!StringUtils.isEmpty(miv)) {
        LOGGER.info("Using {}", miv);
      }
      else {
        LOGGER.error("could not load MediaInfo!");
        if (Platform.isLinux()) {
          LOGGER.error("Please try do install the library from your distribution");
        }
      }

    }
    catch (IOException e) {
      LOGGER.error("Could not load mediainfo", e);
    }
  }
}
