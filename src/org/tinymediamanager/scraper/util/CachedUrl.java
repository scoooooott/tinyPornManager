/*
 * Copyright 2012 - 2013 Manuel Laggner
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class CachedUrl.
 * 
 * @author Manuel Laggner
 */
public class CachedUrl extends Url {
  private static final Logger  LOGGER       = LoggerFactory.getLogger(CachedUrl.class);
  public static final String   CACHE_DIR    = "cache/url";
  private static final int     CACHE_EXPIRY = 3600;
  private static final int     IMAGE_FACTOR = 48;
  private static final Pattern pattern      = Pattern.compile("([^\\s]+(\\.(?i)(jpg|png|gif|bmp))$)");

  private String               urlId        = null;
  private File                 propFile     = null;
  private Properties           props        = null;
  private File                 urlCacheDir  = null;

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
    if (!url.equalsIgnoreCase(props.getProperty("url"))) {
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

  /**
   * Gets the cached file name.
   * 
   * @param url
   *          the url
   * @return the cached file name
   */
  private String getCachedFileName(String url) {
    try {
      if (url == null)
        return null;
      // now uses a simple md5 hash, which should have a fairly low collision
      // rate, especially for our limited use
      byte[] key = DigestUtils.md5(url);
      return new String(Hex.encodeHex(key));
    }
    catch (Exception e) {
      LOGGER.error("Failed to create cached filename for url: " + url, e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets the cache dir.
   * 
   * @return the cache dir
   */
  private File getCacheDir() {
    if (urlCacheDir == null) {
      urlCacheDir = new File(CACHE_DIR);
      if (!urlCacheDir.exists())
        urlCacheDir.mkdirs();
    }
    return urlCacheDir;
  }

  /**
   * Checks if is expired.
   * 
   * @param cachedFile
   *          the cached file
   * @return true, if is expired
   */
  private boolean isExpired(File cachedFile) {
    int expirySecs = CACHE_EXPIRY;
    if (isImageFile(props)) {
      expirySecs = expirySecs * IMAGE_FACTOR;
    }
    return isExpired(cachedFile, expirySecs);
  }

  /**
   * Checks if is expired.
   * 
   * @param cachedFile
   *          the cached file
   * @param expirySecs
   *          the expiry secs
   * @return true, if is expired
   */
  private static boolean isExpired(File cachedFile, long expirySecs) {
    long diff = (System.currentTimeMillis() - cachedFile.lastModified()) / 1000;
    boolean expired = (diff > expirySecs);
    if (expired) {
      LOGGER.debug("CachedUrl.isExpired(): " + expired + "; File: " + cachedFile + "; LastModified: " + cachedFile.lastModified()
          + "; Current Time: " + System.currentTimeMillis() + "; Expiry: " + expirySecs + "s; Diff: " + diff + "s");
    }
    return expired;
  }

  /**
   * Checks if is image file.
   * 
   * @param props
   *          the props
   * @return true, if is image file
   */
  private static boolean isImageFile(Properties props) {
    String filename = props.getProperty("file");
    Matcher matcher = pattern.matcher(filename);
    return matcher.matches();
  }

  /**
   * Checks if is expired.
   * 
   * @param cachedFile
   *          the cached file
   * @param props
   *          the props
   * @return true, if is expired
   */
  private static boolean isExpired(File cachedFile, Properties props) {
    int expirySecs = CACHE_EXPIRY;
    if (isImageFile(props)) {
      expirySecs = expirySecs * IMAGE_FACTOR;
    }
    return isExpired(cachedFile, expirySecs);
  }

  /**
   * Gets the cached file.
   * 
   * @return the cached file
   */
  public File getCachedFile() {
    return getCachedFile(props);
  }

  /**
   * Removes the cached file. For example if an image download is broken
   */
  public void removeCachedFile() {
    File f = getCachedFile();
    if (f.exists()) {
      LOGGER.info("Removing Cached Url File: " + f);
      f.delete();
    }
  }

  /**
   * Gets the cached file.
   * 
   * @param props
   *          the props
   * @return the cached file
   */
  public static File getCachedFile(Properties props) {
    return new File(props.getProperty("file"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.util.Url#getInputStream()
   */
  @Override
  public InputStream getInputStream() throws IOException {
    URL u = getUrl();
    return u.openStream();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.util.Url#getUrl()
   */
  public URL getUrl() throws IOException {
    File f = getCachedFile();
    if (!f.exists() || f.length() == 0) {
      cache();
    }
    else {
      LOGGER.debug("Cached File exists: " + f.getAbsolutePath() + " so we'll just use it.");
    }
    // check if its still empty (maybe broken download)
    if (!f.exists() || f.length() == 0) {
      // return URI without caching
      return new URL(url);
    }
    return f.toURI().toURL();
  }

  /**
   * Cache.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private void cache() throws IOException {
    LOGGER.debug("Caching Url: " + url);
    long sizeHttp = -1;
    // workaround for local files
    InputStream is = null;
    if (!url.startsWith("file:")) {
      Url u = new Url(url);
      u.addHeaders(headersRequest);
      is = u.getInputStream();
      sizeHttp = u.getContentLength();

      // also store encoding
      if (u.getCharset() != null) {
        props.setProperty("encoding", u.getCharset().toString());
      }
    }
    else {
      String newUrl = url.replace("file:", "");
      File file = new File(newUrl);
      is = new FileInputStream(file);
    }
    File f = getCachedFile();
    if (is == null || isFault()) {
      LOGGER.debug("Url " + url + ": did not receive a response; writing empty file");
      f.createNewFile();
      return;
    }
    FileOutputStream fos = new FileOutputStream(f);
    long sizeCopy = IOUtils.copy(is, fos);
    fos.flush();
    try {
      fos.getFD().sync(); // wait until file has been completely written
    }
    catch (Exception e) {
      // empty -> just do not crash the thread
    }
    fos.close();
    is.close();

    if (sizeHttp > 0 && sizeHttp != sizeCopy) {
      LOGGER.warn("File not fully cached! " + f.getAbsolutePath());
    }
    LOGGER.debug("Url " + url + " Cached To: " + f.getAbsolutePath());
    PropertiesUtils.store(props, getPropertyFile(), "Cached Url Properties");
  }

  /**
   * Gets the property file.
   * 
   * @return the property file
   */
  private File getPropertyFile() {
    return propFile;
  }

  /**
   * Clear expired cache files.
   */
  public static void cleanupCache() {
    // filter all .properties
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        // do not start with .
        if (name.toLowerCase().startsWith("."))
          return false;

        if (name.toLowerCase().endsWith(".properties")) {
          return true;
        }

        return false;
      }
    };

    File urlCacheDir = new File(CACHE_DIR);
    if (!urlCacheDir.exists())
      return;

    File[] cachedFiles = urlCacheDir.listFiles(filter);

    // check all cached files
    for (File propFile : cachedFiles) {
      Properties props = new Properties();
      try {
        PropertiesUtils.load(props, propFile);
        File f = getCachedFile(props);

        // cleanup expired cache file
        if (f.exists() && (isExpired(f, props) || f.length() == 0)) {
          f.delete();
          propFile.delete();
        }
      }
      catch (Exception e) {
        LOGGER.warn(e.getMessage());
      }
    }
  }

  /**
   * Gets the charset.
   * 
   * @return the charset
   */
  @Override
  public Charset getCharset() {
    Charset charset = null;

    // take the charset from the cached file
    Charset.forName(props.getProperty("encoding"));

    if (charset == null) {
      charset = Charset.defaultCharset();
    }

    return charset;
  }
}
