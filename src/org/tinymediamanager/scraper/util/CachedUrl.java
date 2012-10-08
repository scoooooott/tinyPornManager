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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * The Class CachedUrl.
 */
public class CachedUrl extends Url {

  /** The Constant log. */
  private static final Logger log = Logger.getLogger(CachedUrl.class);

  /** The Constant CACHE_EXPIRY. */
  private static final int CACHE_EXPIRY = 1800;

  /** factor that the cache of image file is longer */
  private static final int IMAGE_FACTOR = 1000;

  /** The Constant IMAGE_PATTERN. */
  private static final String IMAGE_PATTERN = "([^\\s]+(\\.(?i)(jpg|png|gif|bmp))$)";

  /** The Constant CACHE_DIR. */
  private static final String CACHE_DIR = "cache/url";

  /** The url id. */
  private String urlId = null;

  /** The prop file. */
  private File propFile = null;

  /** The props. */
  private Properties props = null;

  /** The url cache dir. */
  public File urlCacheDir = null;

  /** The follow redirects. */
  private boolean followRedirects = false;

  /** The pattern. */
  private Pattern pattern;

  /** The matcher. */
  private Matcher matcher;

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

    pattern = Pattern.compile(IMAGE_PATTERN);
    urlId = getCachedFileName(url);
    propFile = new File(getCacheDir(), urlId + ".properties");
    props = new Properties();
    if (propFile.exists()) {
      log.debug("Reloading existing cached url: " + propFile.getAbsolutePath() + " with id: " + urlId);
      PropertiesUtils.load(props, propFile);
      File f = getCachedFile();
      if (f.exists() && (isExpired(f) || f.length() == 0)) {
        log.info("Removing Cached Url File: " + f);
        FileUtils.deleteQuietly(f);
      }
    } else {
      File f = propFile.getParentFile();
      FileUtils.mkdirsQuietly(f);
      log.debug("Creating a new cached url for: " + url);
      props.setProperty("url", url);
      props.setProperty("file", new File(getCacheDir(), urlId + ".cache").getPath());
    }

    // sanity check
    if (!url.toLowerCase().equals(props.getProperty("url").toLowerCase())) {
      log.error("The Cached url does not match the one passed! " + props.getProperty("url") + " != " + url + "; Propfile Name: " + propFile);
      props.setProperty("url", url);
      File f = getCachedFile();
      if (f.exists()) {
        log.info("Removing cached content for url: " + url);
        if (!f.delete()) {
          log.warn("Failed to delete file: " + f);
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
      // rate, especially for our
      // limited use
      byte[] key = DigestUtils.md5(url);
      return new String(Hex.encodeHex(key));
    } catch (Exception e) {
      log.error("Failed to create cached filename for url: " + url, e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Checks if is expired.
   * 
   * @param cachedFile
   *          the cached file
   * @return true, if is expired
   */
  public boolean isExpired(File cachedFile) {
    int expirySecs = CACHE_EXPIRY;
    if (isImageFile()) {
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
  public static boolean isExpired(File cachedFile, long expirySecs) {
    long diff = (System.currentTimeMillis() - cachedFile.lastModified()) / 1000;
    boolean expired = (diff > expirySecs);
    if (expired) {
      log.debug("CachedUrl.isExpired(): " + expired + "; File: " + cachedFile + "; LastModified: " + cachedFile.lastModified() + "; Current Time: " + System.currentTimeMillis()
          + "; Expiry: " + expirySecs + "s; Diff: " + diff + "s");
    }
    return expired;
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
        FileUtils.mkdirsQuietly(urlCacheDir);
    }
    return urlCacheDir;
  }

  /**
   * Gets the original url.
   * 
   * @return the original url
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public URL getOriginalUrl() throws IOException {
    return new URL(props.getProperty("url"));
  }

  /**
   * Gets the property file.
   * 
   * @return the property file
   */
  public File getPropertyFile() {
    return propFile;
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
   * Gets the cached file.
   * 
   * @param props
   *          the props
   * @return the cached file
   */
  public static File getCachedFile(Properties props) {
    return new File(props.getProperty("file"));
  }

  /**
   * Checks if is image file.
   * 
   * @return true, if is image file
   */
  private boolean isImageFile() {
    String filename = props.getProperty("file");
    matcher = pattern.matcher(filename);
    return matcher.matches();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.util.Url#hasMoved()
   */
  @Override
  public boolean hasMoved() {
    return Boolean.parseBoolean(props.getProperty("moved", "false"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.util.Url#getMovedUrl()
   */
  @Override
  public URL getMovedUrl() throws IOException {
    return new URL(props.getProperty("movedUrl"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.util.Url#getUrl()
   */
  @Override
  public URL getUrl() throws IOException {
    return getUrl(null);
  }

  /**
   * Gets the url.
   * 
   * @param handler
   *          the handler
   * @return the url
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public URL getUrl(CookieHandler handler) throws IOException {
    File f = getCachedFile();
    if (!f.exists() || f.length() == 0) {
      cache(handler);
    } else {
      log.debug("Cached File exists: " + f.getAbsolutePath() + " so we'll just use it.");
    }
    return f.toURI().toURL();
  }

  /**
   * Cache.
   * 
   * @param handler
   *          the handler
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void cache(CookieHandler handler) throws IOException {
    log.debug("Caching Url: " + getOriginalUrl().toExternalForm());
    URL u = getOriginalUrl();
    URLConnection c = u.openConnection();
    sendCookies(u, c, handler);
    if (c instanceof HttpURLConnection) {
      HttpURLConnection conn = (HttpURLConnection) c;
      conn.setInstanceFollowRedirects(followRedirects);
      log.debug("User Agent: " + System.getProperty("http.agent"));
      conn.setRequestProperty("User-Agent", Url.HTTP_USER_AGENT);
      InputStream is = conn.getInputStream();
      int rc = conn.getResponseCode();
      if (rc == HttpURLConnection.HTTP_MOVED_PERM || rc == HttpURLConnection.HTTP_MOVED_TEMP) {
        props.setProperty("moved", "true");
        String redirectUrl = conn.getHeaderField("Location");
        if (redirectUrl != null) {
          int p = redirectUrl.indexOf('?');
          if (p != -1) {
            redirectUrl = redirectUrl.substring(0, p);
          }
          props.setProperty("movedUrl", redirectUrl);
        }
        File f = getCachedFile();
        FileOutputStream fos = new FileOutputStream(f);
        IOUtils.copy(is, fos);
        fos.flush();
        fos.close();
        log.debug("Url " + u.toExternalForm() + " Cached To: " + f.getAbsolutePath());
        log.debug(String.format("Url: %s moved to %s", u.toExternalForm(), redirectUrl));
      } else if (rc == HttpURLConnection.HTTP_OK) {
        handleCookies(u, c, handler);
        File f = getCachedFile();
        FileOutputStream fos = new FileOutputStream(f);
        IOUtils.copy(is, fos);
        fos.flush();
        fos.close();
        log.debug("Url " + u.toExternalForm() + " Cached To: " + f.getAbsolutePath());
      } else {
        throw new IOException("Http Response Code: " + rc + "; Message: " + conn.getResponseMessage());
      }
    } else {
      // do nothing... we can't cache local urls
      log.warn("Cannot Cache Url Connection Type; " + c.getClass().getName());

    }
    PropertiesUtils.store(props, getPropertyFile(), "Cached Url Properties");
    log.debug("Properties for cached url are now stored: " + getPropertyFile().getAbsolutePath());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.tinymediamanager.scraper.util.Url#getInputStream(org.tinymediamanager
   * .scraper.util.CookieHandler, boolean)
   */
  @Override
  public InputStream getInputStream(CookieHandler handler, boolean followRedirects) throws IOException {
    this.followRedirects = followRedirects;

    URL u = getUrl(handler);

    return u.openStream();
  }

  /**
   * Will remove a url from the cache, in the event that url caching is enabled.
   * 
   * @param dataUrl
   *          the data url
   */
  public static void remove(String dataUrl) {
    try {
      CachedUrl cu = new CachedUrl(dataUrl);
      cu.remove();
    } catch (IOException e) {
      log.error("Unabled to remove cached data url: " + dataUrl);
    }
  }

  /**
   * Removes the.
   */
  private void remove() {
    try {
      log.debug("Removing Cached Url: " + this.getOriginalUrl().toExternalForm());
      if (props != null) {
        // remove the data
        File f = getCachedFile();
        if (f.exists()) {
          log.debug("Removing Cached File: " + f.getAbsolutePath());
          f.delete();
        }

        // now remove the propfile
        FileUtils.deleteQuietly(propFile);
      }
    } catch (IOException e) {
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CachedUrl: " + (props != null ? props.getProperty("url") : "N/A") + "; UrlId: " + urlId;
  }
}
