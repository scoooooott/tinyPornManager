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
package org.tinymediamanager.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

/**
 * The Class Utils.
 */
public class Utils {

  /**
   * Read file as string.
   * 
   * @param file
   *          the file
   * @return the string
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static String readFileAsString(File file) throws java.io.IOException {
    StringBuffer fileData = new StringBuffer(1000);
    BufferedReader reader = new BufferedReader(new FileReader(file));
    char[] buf = new char[1024];
    int numRead = 0;
    while ((numRead = reader.read(buf)) != -1) {
      String readData = String.valueOf(buf, 0, numRead);
      fileData.append(readData);
      buf = new char[1024];
    }

    reader.close();
    return fileData.toString();
  }

  public static String cleanStackingMarkers(String filename) {
    if (!StringUtils.isEmpty(filename)) {
      return filename.replaceAll("(?i)\\|?((cd|dvd|part|dis[ck])([0-9]))", "");
    }
    return filename;
  }

  public static boolean isValidImdbId(String imdbId) {
    if (StringUtils.isEmpty(imdbId)) {
      return false;
    }

    return imdbId.matches("tt\\d{7}");
  }

  public static String replaceAcutesHTML(String str) {
    str = str.replaceAll("&aacute;", "á");
    str = str.replaceAll("&eacute;", "é");
    str = str.replaceAll("&iacute;", "í");
    str = str.replaceAll("&oacute;", "ó");
    str = str.replaceAll("&uacute;", "ú");
    str = str.replaceAll("&Aacute;", "Á");
    str = str.replaceAll("&Eacute;", "É");
    str = str.replaceAll("&Iacute;", "Í");
    str = str.replaceAll("&Oacute;", "Ó");
    str = str.replaceAll("&Uacute;", "Ú");
    str = str.replaceAll("&ntilde;", "ñ");
    str = str.replaceAll("&Ntilde;", "Ñ");

    return str;
  }

  public static String unquote(String str) {
    if (str == null)
      return null;
    return str.replaceFirst("^\\\"(.*)\\\"$", "$1");
  }
}
