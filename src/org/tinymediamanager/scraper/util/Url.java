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
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;

/**
 * The Class Url.
 */
public class Url {
  /** The log. */
  private static final Logger LOGGER = Logger.getLogger(Url.class);

  /** The url. */
  protected String url = null;

  /** The Constant HTTP_USER_AGENT. */
  protected static final String HTTP_USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:15.0) Gecko/20100101 Firefox/15.0.1";

  /**
   * Instantiates a new url.
   * 
   * @param url
   *          the url
   */
  public Url(String url) {
    this.url = url;
  }

  /**
   * Gets the url.
   * 
   * @return the url
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public URL getUrl() throws IOException {
    return new URL(url);
  }

  /**
   * Gets the input stream.
   * 
   * @param handler
   *          the handler
   * @param followRedirects
   *          the follow redirects
   * @return the input stream
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public InputStream getInputStream() throws IOException {
    DefaultHttpClient httpclient = new DefaultHttpClient();
    if ((Globals.settings.useProxy())) {
      setProxy(httpclient, Globals.settings.getProxyHost(), Integer.parseInt(Globals.settings.getProxyPort()), Globals.settings.getProxyUsername(),
          Globals.settings.getProxyPassword());
    }

    HttpGet httpget = new HttpGet(url);

    LOGGER.debug("getting " + url);
    HttpResponse response = httpclient.execute(httpget);
    HttpEntity entity = response.getEntity();

    if (entity != null) {
      return entity.getContent();
    }
    return null;
  }

  public byte[] getBytes() throws IOException {
    InputStream is = getInputStream();
    byte[] bytes = IOUtils.toByteArray(is);
    return bytes;
  }

  protected void setProxy(DefaultHttpClient httpClient, String host, int port, String user, String password) {
    // authenticate
    if (!StringUtils.isEmpty(user) && !StringUtils.isEmpty(password)) {
      httpClient.getCredentialsProvider().setCredentials(new AuthScope(host, port), new UsernamePasswordCredentials(user, password));
    }

    // set proxy
    HttpHost proxy = new HttpHost(host, port);
    httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
  }
}
