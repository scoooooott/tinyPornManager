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
import java.net.UnknownHostException;

import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class StreamingUrl. Used to build streaming downloads (e.g. bigger files which can't the streamed via a ByteArrayInputStream).
 * 
 * @author Manuel Laggner
 */
public class StreamingUrl extends Url {
  private static final Logger   LOGGER = LoggerFactory.getLogger(StreamingUrl.class);

  private CloseableHttpResponse response;

  public StreamingUrl(String url) throws IOException {
    super(url);
  }

  /**
   * get the InputStream of the content. Be aware: using this class needs you to close the connection per hand calling the method closeConnection()
   * 
   * @return the InputStream of the content
   */
  @Override
  public InputStream getInputStream() throws IOException {
    // workaround for local files
    if (url.startsWith("file:")) {
      String newUrl = url.replace("file:", "");
      File file = new File(newUrl);
      return new FileInputStream(file);
    }

    BasicHttpContext localContext = new BasicHttpContext();

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

    try {
      response = client.execute(httpget, localContext);
      headersResponse = response.getAllHeaders();
      entity = response.getEntity();
      responseCode = response.getStatusLine().getStatusCode();
      if (entity != null) {
        return entity.getContent();
      }

    }
    catch (UnknownHostException e) {
      LOGGER.error("proxy or host not found/reachable", e);
    }
    catch (Exception e) {
      LOGGER.error("Exception getting url " + logUrl, e);
    }
    return new ByteArrayInputStream("".getBytes());
  }

  /**
   * Proper closing of the connection and resources
   */
  public void closeConnection() {
    if (response != null) {
      try {
        response.close();
      }

      catch (Exception e) {
        LOGGER.warn("could not close connection " + e.getMessage());
      }
    }
  }
}
