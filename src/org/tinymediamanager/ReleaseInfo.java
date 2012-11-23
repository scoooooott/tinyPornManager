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
import java.util.Properties;

public class ReleaseInfo {
  private static String version;
  private static String build;
  private static String buildDate;

  static {
    try {
      FileInputStream fileInputStream = new FileInputStream("version");
      Properties releaseInfoProp = new Properties();
      releaseInfoProp.load(fileInputStream);
      version = releaseInfoProp.getProperty("version");
      build = releaseInfoProp.getProperty("build");
      buildDate = releaseInfoProp.getProperty("date");
    } catch (Exception e) {

      version = new String("");
    }
  }

  public static String getVersion() {
    return version;
  }

  public static String getBuild() {
    return build;
  }

  public static String getBuildDate() {
    return buildDate;
  }
}
