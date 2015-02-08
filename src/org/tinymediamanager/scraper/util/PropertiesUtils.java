/*
 * Copyright 2012 - 2015 Manuel Laggner
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * The Class PropertiesUtils.
 * 
 * @author Manuel Laggner
 */
public class PropertiesUtils {

  /**
   * Load.
   * 
   * @param props
   *          the props
   * @param f
   *          the f
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static void load(Properties props, File f) throws IOException {
    InputStream is = null;
    try {
      is = new FileInputStream(f);
      props.load(is);
    }
    finally {
      if (is != null) {
        is.close();
      }
    }
  }

  /**
   * Load.
   * 
   * @param props
   *          the props
   * @param is
   *          the is
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static void load(Properties props, InputStream is) throws IOException {
    try {
      props.load(is);
    }
    finally {
      is.close();
    }
  }

  /**
   * Store.
   * 
   * @param props
   *          the props
   * @param out
   *          the out
   * @param msg
   *          the msg
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static void store(Properties props, File out, String msg) throws IOException {
    OutputStream os = null;
    try {
      os = new FileOutputStream(out);
      props.store(os, msg);
    }
    finally {
      if (os != null) {
        try {
          os.flush();
        }
        catch (Exception e) {
        }
        os.close();
      }
    }
  }
}
