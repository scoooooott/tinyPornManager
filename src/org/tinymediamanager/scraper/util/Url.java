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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Utils;

/**
 * The Class Url.
 * 
 * @author Manuel Laggner
 */
public class Url {
  /** The log. */
  private static final Logger      LOGGER          = LoggerFactory.getLogger(Url.class);

  /** The client. */
  private static DefaultHttpClient client;

  /** The url. */
  protected String                 url             = null;

  /** the headers sent from server. */
  protected Header[]               headersResponse = null;

  /** The headers request. */
  protected List<Header>           headersRequest  = new ArrayList<Header>();

  /** The entity sent from server. */
  protected HttpEntity             entity          = null;

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
    if (headersResponse == null) {
      return "";
    }
    for (Header h : headersResponse) {
      if (h.getName().equalsIgnoreCase(header)) {
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
   * Adds the header.
   * 
   * @param key
   *          the key
   * @param value
   *          the value
   */
  public void addHeader(String key, String value) {
    headersRequest.add(new BasicHeader(key, value));
  }

  /**
   * Adds the header.
   * 
   * @param header
   *          the header
   */
  public void addHeader(Header header) {
    headersRequest.add(header);
  }

  /**
   * Adds the headers.
   * 
   * @param headers
   *          the headers
   */
  public void addHeaders(List<Header> headers) {
    headersRequest.addAll(headers);
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

    // replace our API keys for logging...
    LOGGER.debug("getting " + url.replaceAll("api_key=\\w+", "api_key=<API_KEY>").replaceAll("api/\\d+\\w+", "api/<API_KEY>"));
    HttpGet httpget = new HttpGet(url);

    // set custom headers
    for (Header header : headersRequest) {
      httpget.addHeader(header);
    }

    try {
      HttpResponse response = httpclient.execute(httpget, localContext);
      headersResponse = response.getAllHeaders();
      entity = response.getEntity();

      if (entity != null) {
        is = new ByteArrayInputStream(EntityUtils.toByteArray(entity));
      }
    }
    catch (UnknownHostException e) {
      LOGGER.error("proxy or host not found/reachable", e);
    }
    catch (Exception e) {
      LOGGER.error("Exception getting url", e);
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
    is.close();
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

  /**
   * Gets the charset.
   * 
   * @return the charset
   */
  public Charset getCharset() {
    Charset charset = null;
    if (entity == null || entity.getContentType() == null) {
      return Charset.defaultCharset();
    }

    String contentType = entity.getContentType().getValue();
    if (contentType != null) {
      // changed 'charset' to 'harset' in regexp because some sites send 'Charset'
      Matcher m = Pattern.compile("harset *=[ '\"]*([^ ;'\"]+)[ ;'\"]*").matcher(contentType);
      if (m.find()) {
        String encoding = m.group(1);
        try {
          charset = Charset.forName(encoding);
        }
        catch (UnsupportedCharsetException e) {
          // there will be used default charset
        }
      }
    }
    if (charset == null) {
      charset = Charset.defaultCharset();
    }

    return charset;
  }

  /**
   * Gets the content encoding.
   * 
   * @return the content encoding
   */
  public String getContentEncoding() {
    if (entity == null || entity.getContentEncoding() == null) {
      return null;
    }

    return entity.getContentEncoding().getValue();
  }
}
