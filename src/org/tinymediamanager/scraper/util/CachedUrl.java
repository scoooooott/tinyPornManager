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

import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;

/**
 * The Class CachedUrl.
 */
public class CachedUrl extends Url {

  /** The Constant log. */
  private static final Logger LOGGER = Logger.getLogger(CachedUrl.class);

  private static final CacheConfig cacheConfig = new CacheConfig();

  static {
    cacheConfig.setMaxCacheEntries(100);
    cacheConfig.setMaxObjectSize(150000);
  }

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
  }

  @Override
  public InputStream getInputStream() throws IOException {
    DefaultHttpClient httpClient = new DefaultHttpClient();
    if (Globals.settings.useProxy()) {
      List l = null;
      try {
        ProxySelector pl = ProxySelector.getDefault();
        l = ProxySelector.getDefault().select(new URI("http://foo/bar"));
      } catch (URISyntaxException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      setProxy(httpClient, Globals.settings.getProxyHost(), Integer.parseInt(Globals.settings.getProxyPort()), Globals.settings.getProxyUsername(),
          Globals.settings.getProxyPassword());
    }

    CachingHttpClient cachingClient = new CachingHttpClient(httpClient, cacheConfig);
    HttpContext localContext = new BasicHttpContext();
    HttpGet httpget = new HttpGet(url);

    LOGGER.debug("getting " + url);
    HttpResponse response = cachingClient.execute(httpget, localContext);
    HttpEntity entity = response.getEntity();

    if (entity != null) {
      return entity.getContent();
    }
    return null;
  }
}
