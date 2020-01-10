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
package org.tinymediamanager;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.Settings;

/**
 * The Class Globals. used to hold global information/fields for the whole application
 * 
 * @author Manuel Laggner
 */
public class Globals {
  private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("tmm.debug", "false"));

  public static final String   DATA_FOLDER;
  public static final String   CACHE_FOLDER;
  public static final String   BACKUP_FOLDER;
  public static final String   LOG_FOLDER;

  public static final Settings settings;

  static {
    // first we look for a dedicated folder property
    // after that we look for tmm.contentfolder
    String dataFolder = System.getProperty("tmm.datafolder");
    String cacheFolder = System.getProperty("tmm.cachefolder");
    String backupFolder = System.getProperty("tmm.backupfolder");
    String logFolder = System.getProperty("tmm.logfolder");

    String contentFolder = System.getProperty("tmm.contentfolder");

    // data
    if (StringUtils.isNotBlank(dataFolder)) {
      DATA_FOLDER = dataFolder;
    }
    else if (StringUtils.isNotBlank(contentFolder)) {
      DATA_FOLDER = contentFolder + "/data";
    }
    else {
      DATA_FOLDER = "data";
    }

    // cache
    if (StringUtils.isNotBlank(cacheFolder)) {
      CACHE_FOLDER = cacheFolder;
    }
    else if (StringUtils.isNotBlank(contentFolder)) {
      CACHE_FOLDER = contentFolder + "/cache";
    }
    else {
      CACHE_FOLDER = "cache";
    }

    // backup
    if (StringUtils.isNotBlank(backupFolder)) {
      BACKUP_FOLDER = backupFolder;
    }
    else if (StringUtils.isNotBlank(contentFolder)) {
      BACKUP_FOLDER = contentFolder + "/backup";
    }
    else {
      BACKUP_FOLDER = "backup";
    }

    // logs
    if (StringUtils.isNotBlank(logFolder)) {
      LOG_FOLDER = logFolder;
    }
    else if (StringUtils.isNotBlank(contentFolder)) {
      LOG_FOLDER = contentFolder + "/logs";
    }
    else {
      LOG_FOLDER = "logs";
    }

    settings = Settings.getInstance();
  }

  /**
   * are we in our internal debug mode?
   * 
   * @return true/false
   */
  public static boolean isDebug() {
    return DEBUG;
  }

  /**
   * Are we running from a webstart instance?
   * 
   * @return true/false
   */
  public static boolean isRunningJavaWebStart() {
    boolean hasJNLP = false;
    try {
      Class.forName("javax.jnlp.ServiceManager");
      hasJNLP = true;
    }
    catch (ClassNotFoundException ex) {
      hasJNLP = false;
    }
    return hasJNLP;
  }

  /**
   * Are we running on a jetty webswing instance?
   * 
   * @return true/false
   */
  public static boolean isRunningWebSwing() {
    return System.getProperty("webswing.classPath") != null;
  }
}
