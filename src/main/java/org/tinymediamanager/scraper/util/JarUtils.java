package org.tinymediamanager.scraper.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class JarUtils {

  /**
   * gets manifest from JAR containing 'class'<br>
   * If found as 'file://' try to get from execution root<br>
   * returns <i>null</i> if not found.
   * 
   * @param c
   *          the class file
   * @return value of manifest entry
   */
  @SuppressWarnings("rawtypes")
  public static Manifest getManifest(Class c) {
    Manifest mf = null;
    try {
      String classname = "/" + c.getName().replaceAll("\\.", "/") + ".class";
      URL jarURL = c.getResource(classname);
      if (jarURL.getProtocol().equals("jar")) {
        JarURLConnection jurlConn = (JarURLConnection) jarURL.openConnection();
        mf = jurlConn.getManifest();
      }
      else if (jarURL.getProtocol().equals("file")) {
        // started from within eclipse or somehow as exploded
        // DO NOT get from classpath as we get the manifest of the first loaded JAR

        // base path is the filesystem root of the "classpath" (classes | test-classes)
        String basepath = jarURL.getPath().substring(0, jarURL.getPath().indexOf(classname));

        // assume there is already some generated manifest on filesystem
        InputStream is = new FileInputStream(basepath + "/META-INF/MANIFEST.MF");

        if (is != null) {
          mf = new Manifest(is);
        }
      }
    }
    catch (Exception e) {
      // do nothing
      mf = null;
    }
    return mf;
  }

  /**
   * 
   * gets specified manifest entry from JAR containing 'class'
   * 
   * @param c
   *          the class file
   * @param entry
   *          the menifest entry
   * @return value of manifest entry
   */
  @SuppressWarnings("rawtypes")
  public static String getManifestEntry(Class c, String entry) {
    String s = "";
    try {
      Manifest mf = getManifest(c);
      Attributes attr = mf.getMainAttributes();
      s = attr.getValue(entry);
    }
    catch (Exception e) {
      // NPE if no manifest found
    }
    return s;
  }
}
