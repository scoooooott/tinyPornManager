/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ReleaseInfo.
 * 
 * @author Manuel Laggner
 */
public class ReleaseInfo {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseInfo.class);

  /** The version. */
  private static String       version;

  /** The build. */
  private static String       build;

  /** The build date. */
  private static String       buildDate;

  static {
    FileInputStream fileInputStream = null;
    try {
      fileInputStream = new FileInputStream("version");
      Properties releaseInfoProp = new Properties();
      releaseInfoProp.load(fileInputStream);
      version = releaseInfoProp.getProperty("version");
      build = releaseInfoProp.getProperty("build");
      buildDate = releaseInfoProp.getProperty("date");
    }
    catch (IOException e) {
      try {
        fileInputStream = new FileInputStream("target/classes/eclipse.properties");
        Properties releaseInfoProp = new Properties();
        releaseInfoProp.load(fileInputStream);
        version = releaseInfoProp.getProperty("version");
        build = "svn";
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        buildDate = formatter.format(new Date());
      }
      catch (IOException e2) {
        version = "";
        build = "svn"; // no file - we must be in SVN
        buildDate = "";
      }
    }
    finally {
      try {
        if (fileInputStream != null) {
          fileInputStream.close();
        }
      }
      catch (IOException e) {
        LOGGER.warn(e.getMessage());
      }
    }
  }

  /**
   * Gets the version.
   * 
   * @return the version
   */
  public static String getVersion() {
    String v = version;
    if (v.isEmpty()) {
      if (isNightly()) {
        v = "NIGHTLY";
      }
      else if (isPreRelease()) {
        v = "PRE-RELEASE";
      }
      else {
        v = "SVN";
      }
    }
    return v;
  }

  /**
   * Gets the live version or nightly/prerel/svn build string<br>
   * useful for reporting
   * 
   * @return the version
   */
  public static String getVersionForReporting() {
    String v = version;
    if (isSvnBuild()) {
      v = "SVN";
    }
    else if (isNightly()) {
      v = "NIGHTLY";
    }
    else if (isPreRelease()) {
      v = "PRE-RELEASE";
    }
    return v;
  }

  /**
   * Gets the builds the.
   * 
   * @return the builds the
   */
  public static String getBuild() {
    return build;
  }

  /**
   * Gets the builds the date.
   * 
   * @return the builds the date
   */
  public static String getBuildDate() {
    return buildDate;
  }

  // @formatter:off
  /*
    Manifest-Version: 1.0
    Ant-Version: Apache Ant 1.9.2
    Created-By: 1.6.0_38-ea-b04 (Sun Microsystems Inc.)
    Main-Class: org.tinymediamanager.TinyMediaManager
    SplashScreen-Image: org/tinymediamanager/ui/images/splashscreen.png
    Implementation-Title: tinyMediaManager
    Implementation-Version: 2.4 (r992)
    Build-Date: 20130924-1832
    Build-By: jenkins
  */
  // @formatter:on

  /**
   * are we nightly?
   * 
   * @return true/false if nightly dev build
   */
  public static boolean isNightly() {
    return getBuild().equalsIgnoreCase("nightly") ? true : false;
  }

  /**
   * are we pre-release?
   * 
   * @return true/false if nightly dev build
   */
  public static boolean isPreRelease() {
    return getBuild().equalsIgnoreCase("prerelease") ? true : false;
  }

  /**
   * are we a SVN version?
   * 
   * @return true/false if SVN build
   */
  public static boolean isSvnBuild() {
    return getBuild().equalsIgnoreCase("svn") ? true : false;
  }

  /**
   * are we on the release version?
   * 
   * @return true/false if release build
   */
  public static boolean isReleaseBuild() {
    return !isNightly() && !isPreRelease() && !isSvnBuild();
  }

  /**
   * gets the REAL version string out of the JAR file's manifest<br>
   * eg: 2.4 (r992)
   * 
   * @return version string
   */
  public static String getRealVersion() {
    String v = getManifestEntry(ReleaseInfo.class, "Implementation-Version");
    if (v.isEmpty()) {
      // no manifest? only happens on svn builds - get the version from special file
      v = getVersion() + " - SVN";
    }
    if (isNightly()) {
      v += " - NIGHTLY";
    }
    else if (isPreRelease()) {
      v += " - PRE-RELEASE";
    }
    return v;
  }

  /**
   * gets the REAL build date string out of the JAR file's manifest<br>
   * eg: 20130924-1832
   * 
   * @return version string
   */
  public static String getRealBuildDate() {
    String b = getManifestEntry(ReleaseInfo.class, "Build-Date");
    if (b.isEmpty()) {
      b = getBuildDate(); // SVN, actual date
    }
    return b;
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
      Manifest mf = ReleaseInfo.getManifest(c);
      Attributes attr = mf.getMainAttributes();
      s = attr.getValue(entry);
    }
    catch (Exception e) {
      // NPE if no manifest found
    }
    return s;
  }
}
