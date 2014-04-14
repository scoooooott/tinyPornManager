/*
 * Copyright 2012 - 2014 Manuel Laggner
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Url. Used to make simple, blocking URL requests. The request is temporarily streamed into a ByteArrayInputStream, before the InputStream
 * is passed to the caller.
 * 
 * @author Manuel Laggner / Myron Boyle
 */
public class Url {
  private static final Logger          LOGGER          = LoggerFactory.getLogger(Url.class);
  protected static CloseableHttpClient client;

  protected int                        responseCode    = 0;
  protected String                     url             = null;
  protected Header[]                   headersResponse = null;
  protected List<Header>               headersRequest  = new ArrayList<Header>();
  protected HttpEntity                 entity          = null;

  /**
   * gets the specified header value from this connection<br>
   * You need to call this AFTER getInputstream().
   * 
   * @param header
   *          the header you want to know (like Content-Length)
   * @return the header value
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
   * Instantiates a new url / httpclient with default user-agent.
   * 
   * @param url
   *          the url
   */
  public Url(String url) {
    if (client == null) {
      client = TmmHttpClient.getHttpClient();
    }
    this.url = url;

    // default user agent
    addHeader(HttpHeaders.USER_AGENT, UrlUtil.generateUA());
  }

  /**
   * set a specified User-Agent
   * 
   * @param userAgent
   */
  public void setUserAgent(String userAgent) {
    addHeader(HttpHeaders.USER_AGENT, userAgent);
  }

  /**
   * Gets the url.
   * 
   * @return the url
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public URL getUrl() throws IOException, InterruptedException {
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
    if (StringUtils.isBlank(key)) {
      return;
    }

    // LOGGER.debug("add HTTP header: " + key + "=" + value);

    // check for duplicates
    for (int i = headersRequest.size() - 1; i >= 0; i--) {
      Header header = headersRequest.get(i);
      if (key.equals(header.getName())) {
        headersRequest.remove(i);
      }
    }

    // and add the new one
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
  public InputStream getInputStream() throws IOException, InterruptedException {
    // workaround for local files
    if (url.startsWith("file:")) {
      String newUrl = url.replace("file:", "");
      File file = new File(newUrl);
      return new FileInputStream(file);
    }

    BasicHttpContext localContext = new BasicHttpContext();
    ByteArrayInputStream is = null;

    // replace our API keys for logging...
    String logUrl = url.replaceAll("api_key=\\w+", "api_key=<API_KEY>").replaceAll("api/\\d+\\w+", "api/<API_KEY>");
    LOGGER.debug("getting " + logUrl);
    HttpGet httpget = new HttpGet(url);
    RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).build();
    httpget.setConfig(requestConfig);

    // set custom headers
    for (Header header : headersRequest) {
      httpget.addHeader(header);
    }

    CloseableHttpResponse response = null;
    try {
      response = client.execute(httpget, localContext);
      headersResponse = response.getAllHeaders();
      entity = response.getEntity();
      responseCode = response.getStatusLine().getStatusCode();
      if (entity != null) {
        is = new ByteArrayInputStream(EntityUtils.toByteArray(entity));
      }
      EntityUtils.consume(entity);
    }
    catch (InterruptedIOException e) {
      LOGGER.info("aborted request: " + logUrl);
      throw new InterruptedException();
    }
    catch (UnknownHostException e) {
      LOGGER.error("proxy or host not found/reachable", e);
    }
    catch (Exception e) {
      LOGGER.error("Exception getting url " + logUrl, e);
    }
    finally {
      if (response != null) {
        response.close();
      }
    }
    return is;
  }

  /**
   * is the HTTP status code a 4xx/5xx?
   * 
   * @return true/false
   */
  public boolean isFault() {
    return responseCode >= 400 ? true : false;
  }

  /**
   * Gets the bytes.
   * 
   * @return the bytes
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public byte[] getBytes() throws IOException, InterruptedException {
    InputStream is = getInputStream();
    byte[] bytes = IOUtils.toByteArray(is);
    is.close();
    return bytes;
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

  /**
   * the number of bytes of the content, or a negative number if unknown. If the content length is known but exceeds Long.MAX_VALUE, a negative number
   * is returned.
   * 
   * @return the content length
   */
  public long getContentLength() {
    if (entity == null) {
      return -1;
    }

    return entity.getContentLength();
  }

  @Override
  public String toString() {
    return url;
  }
}
