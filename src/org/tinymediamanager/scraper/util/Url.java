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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.core.Utils;

/**
 * The Class Url.
 * 
 * @author Manuel Laggner
 */
public class Url {
  /** The log. */
  private static final Logger      LOGGER  = Logger.getLogger(Url.class);

  /** The client. */
  private static DefaultHttpClient client;

  /** The url. */
  protected String                 url     = null;

  /** the headers sent from server. */
  private Header[]                 headers = null;

  // /** The Constant HTTP_USER_AGENT. */
  // protected static final String HTTP_USER_AGENT =
  // "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:15.0) Gecko/20100101 Firefox/15.0.1";

  /**
   * gets the specified header value from this connection<br>
   * You need to call this AFTER getInputstream().
   * 
   * @param header
   *          the header you want to know (like Content-Length)
   * @return the header value
   * @author Myron Boyle
   */
  public String getHeader(String header) {
    if (headers == null) {
      return "";
    }
    for (Header h : headers) {
      if (h.getName().toLowerCase().equals(header.toLowerCase())) {
        return h.getValue();
      }
    }
    return "";
  }

  /**
   * Instantiates a new url.
   * 
   * @param url
   *          the url
   */
  public Url(String url) {
    if (client == null) {
      client = Utils.getHttpClient();
    }
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
   * @return the input stream
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public InputStream getInputStream() throws IOException {
    DefaultHttpClient httpclient = getHttpClient();
    BasicHttpContext localContext = new BasicHttpContext();
    ByteArrayInputStream is = null;
    HttpEntity entity = null;

    LOGGER.debug("getting " + url);
    HttpGet httpget = new HttpGet(url);
    try {
      HttpResponse response = httpclient.execute(httpget, localContext);
      headers = response.getAllHeaders();
      entity = response.getEntity();

      if (entity != null) {
        is = new ByteArrayInputStream(EntityUtils.toByteArray(entity));
      }
    }
    catch (Exception e) {
      LOGGER.warn("fetch data - " + e.getMessage());
    }
    finally {
      EntityUtils.consume(entity);
    }
    return is;
  }

  /**
   * Gets the bytes.
   * 
   * @return the bytes
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public byte[] getBytes() throws IOException {
    InputStream is = getInputStream();
    byte[] bytes = IOUtils.toByteArray(is);
    return bytes;
  }

  /**
   * Gets the http client.
   * 
   * @return the http client
   */
  protected DefaultHttpClient getHttpClient() {
    return client;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return url;
  }
}
