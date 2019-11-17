//
// Getdown - application installer, patcher and launcher
// Copyright (C) 2004-2013 Three Rings Design, Inc.
// http://code.google.com/p/getdown/source/browse/LICENSE

package org.tinymediamanager;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;

/**
 * Useful routines for launching Java applications from within other Java applications.
 */
public class LaunchUtil {
  private static final Logger LOGGER       = LoggerFactory.getLogger(LaunchUtil.class);
  private static final String USR_BIN_JAVA = "/usr/bin/java";

  /**
   * Reconstructs the path to the JVM used to launch this process.
   */
  public static String getJVMPath() {
    return getJVMPath(false);
  }

  /**
   * Reconstructs the path to the JVM used to launch this process.
   * 
   * @param windebug
   *          if true we will use java.exe instead of javaw.exe on Windows.
   */
  public static String getJVMPath(boolean windebug) {
    // first look in our application directory for an installed VM
    String vmpath = checkJVMPath(System.getProperty("java.home"), windebug);

    // then throw up our hands and hope for the best
    if (vmpath == null) {
      LOGGER.warn("Unable to find java [java.home=" + System.getProperty("java.home") + "]!");
      vmpath = "java";
    }

    // Oddly, the Mac OS X specific java flag -Xdock:name will only work if java is launched
    // from /usr/bin/java, and not if launched by directly referring to <java.home>/bin/java,
    // even though the former is a symlink to the latter! To work around this, see if the
    // desired jvm is in fact pointed to by /usr/bin/java and, if so, use that instead.
    if (Platform.isMac()) {
      try {
        File localVM = new File(USR_BIN_JAVA).getCanonicalFile();
        if (localVM.equals(new File(vmpath).getCanonicalFile())) {
          vmpath = USR_BIN_JAVA;
        }
      }
      catch (IOException ioe) {
        LOGGER.warn("Failed to check Mac OS canonical VM path.", ioe);
      }
    }

    return vmpath;
  }

  /**
   * Checks whether a Java Virtual Machine can be located in the supplied path.
   */
  protected static String checkJVMPath(String vmhome, boolean windebug) {
    String vmbase = vmhome + File.separator + "bin" + File.separator;
    String vmpath = vmbase + "java";
    if (new File(vmpath).exists()) {
      return vmpath;
    }

    if (!windebug) {
      vmpath = vmbase + "javaw.exe";
      if (new File(vmpath).exists()) {
        return vmpath;
      }
    }

    vmpath = vmbase + "java.exe";
    if (new File(vmpath).exists()) {
      return vmpath;
    }

    return null;
  }
}
