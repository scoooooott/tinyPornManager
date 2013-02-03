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

import java.io.File;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

/**
 * @author manuel
 * 
 */
public class ImageCache {
  /** The static LOGGER. */
  private static final Logger LOGGER    = Logger.getLogger(ImageCache.class);

  /** The Constant CACHE_DIR. */
  public static final String  CACHE_DIR = "cache/image";

  /**
   * Gets the cache dir.
   * 
   * @return the cache dir
   */
  public static File getCacheDir() {
    File imageCacheDir = new File(CACHE_DIR);
    if (!imageCacheDir.exists()) {
      imageCacheDir.mkdirs();
    }
    return imageCacheDir;
  }

  /**
   * Gets the cached file name.
   * 
   * @param path
   *          the url
   * @return the cached file name
   */
  public static String getCachedFileName(String path) {
    try {
      if (path == null)
        return null;
      // now uses a simple md5 hash, which should have a fairly low collision
      // rate, especially for our
      // limited use
      byte[] key = DigestUtils.md5(path);
      return new String(Hex.encodeHex(key));
    }
    catch (Exception e) {
      LOGGER.error("Failed to create cached filename for image: " + path, e);
      throw new RuntimeException(e);
    }
  }

}
