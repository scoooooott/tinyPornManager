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

package org.tinymediamanager.scraper.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class JarUtils {

  private JarUtils() {
    // hide the public constructor for utility classes
  }

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
        mf = readManifest(basepath + "/META-INF/MANIFEST.MF");
      }
    }
    catch (Exception e) {
      // do nothing
      mf = null;
    }
    return mf;
  }

  private static Manifest readManifest(String path) throws IOException {
    Manifest manifest;
    try (InputStream is = new FileInputStream(path)) {
      manifest = new Manifest(is);
    }
    return manifest;
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
      if (mf != null) {
        Attributes attr = mf.getMainAttributes();
        s = attr.getValue(entry);
      }
      else {
        // no jar/manifest found, but since it's a version call, try maven props
        if (Attributes.Name.IMPLEMENTATION_VERSION.toString().equals(entry)) {
          s = getVersionFromProject(c);
        }
      }
    }
    catch (Exception e) {
      // NPE if no manifest found
    }
    return s;
  }

  /**
   * tries to read version out of a built maven project
   * 
   * @param c
   *          the classfile (we need to find the correct project)
   * @return version or empty
   */
  @SuppressWarnings("rawtypes")
  public static String getVersionFromProject(Class c) {

    // TODO: further improvement:
    // XML parse pom.xml, since it's always there

    String v = "";
    try {
      String classname = "/" + c.getName().replaceAll("\\.", "/") + ".class";
      URL jarURL = c.getResource(classname);
      if (jarURL.getProtocol().equals("file")) {
        // base path is the filesystem root of the "classpath" (classes | test-classes)
        String basepath = jarURL.getPath().substring(0, jarURL.getPath().indexOf(classname));
        File projectRoot = new File(basepath).getParentFile(); // go one level up
        // assume there is already some generated manifest on filesystem
        v = readPomProperties(projectRoot + "/maven-archiver/pom.properties").getProperty("version");
      }
    }
    catch (Exception e) {
      // do nothing
    }
    return v;
  }

  private static Properties readPomProperties(String path) throws IOException {
    Properties p;
    try (InputStream is = new FileInputStream(path)) {
      p = new Properties();
      p.load(is);
    }
    return p;
  }
}
