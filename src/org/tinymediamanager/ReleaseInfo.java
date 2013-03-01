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
package org.tinymediamanager;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * The Class ReleaseInfo.
 */
public class ReleaseInfo {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = Logger.getLogger(ReleaseInfo.class);

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
      version = "";
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
    return version;
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
}
