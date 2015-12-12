package org.tinymediamanager.thirdparty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;

public class MediaInfoUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(MediaInfoUtils.class);

  /**
   * load media info from /native/*
   */
  public static void loadMediaInfo() {
    try {

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

      // https://en.wikipedia.org/wiki/X86-64
      if (Platform.is64Bit()) {
        nativepath += "-x64";
      }
      else {
        nativepath += "-x86";
      }

      // on linux try to set the executable bit for the native libs
      if (Platform.isLinux()) {
        File[] nativeFiles = new File(nativepath).listFiles();
        if (nativeFiles != null) {
          // using PosixFilePermission to set file permissions 755
          Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
          // add owners permission
          perms.add(PosixFilePermission.OWNER_READ);
          perms.add(PosixFilePermission.OWNER_WRITE);
          perms.add(PosixFilePermission.OWNER_EXECUTE);
          // add group permissions
          perms.add(PosixFilePermission.GROUP_READ);
          perms.add(PosixFilePermission.GROUP_EXECUTE);
          // add others permissions
          perms.add(PosixFilePermission.OTHERS_READ);
          perms.add(PosixFilePermission.OTHERS_EXECUTE);

          for (File file : nativeFiles) {
            Files.setPosixFilePermissions(file.toPath(), perms);
          }
        }
      }

      String miv = "";
      // need that, since we cannot try and reload/unload a Class
      // MI does not load over UNC, so copy to temp
      if (System.getProperty("user.dir", "").startsWith("\\\\") || System.getProperty("user.dir", "").startsWith("//")) {
        LOGGER.debug("We're on a network UNC path!");
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "tmm");
        File nativeDir = new File(tmpDir, nativepath);
        FileUtils.copyDirectory(new File(nativepath), nativeDir);

        System.setProperty("jna.library.path", nativeDir.getAbsolutePath());
        LOGGER.debug("Loading native mediainfo lib from: {}", nativeDir.getAbsolutePath());
        miv = MediaInfo.version(); // load class
      }
      else {
        System.setProperty("jna.library.path", nativepath);
        LOGGER.debug("Loading native mediainfo lib from: {}", nativepath);
        miv = MediaInfo.version(); // load class
      }

      if (!StringUtils.isEmpty(miv)) {
        LOGGER.info("Using " + miv);
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
