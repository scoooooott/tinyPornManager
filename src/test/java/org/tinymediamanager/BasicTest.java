package org.tinymediamanager;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Assert;
import org.tinymediamanager.core.Utils;

public class BasicTest {

  // own method to get some logging ;)
  public static void assertEqual(Object expected, Object actual) {
    try {
      Assert.assertEquals(expected, actual);
      System.out.println(expected + " - passed");
    }
    catch (AssertionError e) {
      System.err.println(expected + " - FAILED: " + e.getMessage());
      throw e;
    }
  }

  public static String getSettingsFolder() {
    StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
    return "target/testdata/" + ste.getClassName();
  }

  public static void deleteSettingsFolder() {
    StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
    try {
      Utils.deleteDirectoryRecursive(Paths.get("target", "testdata", ste.getClassName()));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}
