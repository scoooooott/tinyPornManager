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
package org.tinymediamanager.scraper.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * The Class CachedUrl.
 */
public class CachedUrl extends Url {

  /** The Constant log. */
  private static final Logger LOGGER       = Logger.getLogger(CachedUrl.class);

  private static final String CACHE_DIR    = "cache/url";
  private static final int    CACHE_EXPIRY = 3600;

  private String              urlId        = null;
  private File                propFile     = null;
  private Properties          props        = null;
  private File                urlCacheDir  = null;

  /**
   * Instantiates a new cached url.
   * 
   * @param url
   *          the url
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public CachedUrl(String url) throws IOException {
    super(url);

    urlId = getCachedFileName(url);
    propFile = new File(getCacheDir(), urlId + ".properties");
    props = new Properties();
    if (propFile.exists()) {
      LOGGER.debug("Reloading existing cached url: " + propFile.getAbsolutePath() + " with id: " + urlId);
      PropertiesUtils.load(props, propFile);
      File f = getCachedFile();
      if (f.exists() && (isExpired(f) || f.length() == 0)) {
        LOGGER.info("Removing Cached Url File: " + f);
        f.delete();
      }
    }
    else {
      File f = propFile.getParentFile();
      f.mkdirs();
      LOGGER.debug("Creating a new cached url for: " + url);
      props.setProperty("url", url);
      props.setProperty("file", new File(getCacheDir(), urlId + ".cache").getPath());
    }

    // sanity check
    if (!url.toLowerCase().equals(props.getProperty("url").toLowerCase())) {
      LOGGER.error("The Cached url does not match the one passed! " + props.getProperty("url") + " != " + url + "; Propfile Name: " + propFile);
      props.setProperty("url", url);
      File f = getCachedFile();
      if (f.exists()) {
        LOGGER.info("Removing cached content for url: " + url);
        if (!f.delete()) {
          LOGGER.warn("Failed to delete file: " + f);
        }
      }
    }
  }

  private String getCachedFileName(String url) {
    try {
      if (url == null)
        return null;
      // now uses a simple md5 hash, which should have a fairly low collision
      // rate, especially for our
      // limited use
      byte[] key = DigestUtils.md5(url);
      return new String(Hex.encodeHex(key));
    }
    catch (Exception e) {
      LOGGER.error("Failed to create cached filename for url: " + url, e);
      throw new RuntimeException(e);
    }
  }

  private File getCacheDir() {
    if (urlCacheDir == null) {
      urlCacheDir = new File(CACHE_DIR);
      if (!urlCacheDir.exists())
        urlCacheDir.mkdirs();
    }
    return urlCacheDir;
  }

  private static boolean isExpired(File cachedFile) {
    long diff = (System.currentTimeMillis() - cachedFile.lastModified()) / 1000;
    boolean expired = (diff > CACHE_EXPIRY);
    if (expired) {
      LOGGER.debug("CachedUrl.isExpired(): " + expired + "; File: " + cachedFile + "; LastModified: " + cachedFile.lastModified()
          + "; Current Time: " + System.currentTimeMillis() + "; Expiry: " + CACHE_EXPIRY + "s; Diff: " + diff + "s");
    }
    return expired;
  }

  public File getCachedFile() {
    return getCachedFile(props);
  }

  public static File getCachedFile(Properties props) {
    return new File(props.getProperty("file"));
  }

  @Override
  public InputStream getInputStream() throws IOException {
    URL u = getUrl();
    return u.openStream();
  }

  public URL getUrl() throws IOException {
    File f = getCachedFile();
    if (!f.exists() || f.length() == 0) {
      cache();
    }
    else {
      LOGGER.debug("Cached File exists: " + f.getAbsolutePath() + " so we'll just use it.");
    }
    return f.toURI().toURL();
  }

  private void cache() throws IOException {
    LOGGER.debug("Caching Url: " + url);

    Url u = new Url(url);
    InputStream is = u.getInputStream();
    File f = getCachedFile();
    FileOutputStream fos = new FileOutputStream(f);
    IOUtils.copy(is, fos);
    fos.flush();
    fos.close();
    LOGGER.debug("Url " + u + " Cached To: " + f.getAbsolutePath());
    PropertiesUtils.store(props, getPropertyFile(), "Cached Url Properties");
    LOGGER.debug("Properties for cached url are now stored: " + getPropertyFile().getAbsolutePath());
  }

  private File getPropertyFile() {
    return propFile;
  }

}
