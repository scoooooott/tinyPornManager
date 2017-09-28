/*
 * Copyright 2012 - 2017 Manuel Laggner
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
package org.tinymediamanager.scraper.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.util.Pair;

import okhttp3.Request;

/**
 * The class StreamingUrl. Used to build streaming downloads (e.g. bigger files
 * which can't the streamed via a ByteArrayInputStream).
 * 
 * @author Manuel Laggner
 */
public class StreamingUrl extends Url {
  private static final Logger LOGGER = LoggerFactory.getLogger(StreamingUrl.class);

  public StreamingUrl(String url) throws IOException {
    super(url);
  }

  /**
   * get the InputStream of the content. Be aware: using this class needs you to
   * close the connection per hand calling the method closeConnection()
   * 
   * @return the InputStream of the content
   */
  @Override
  public InputStream getInputStream() throws IOException, InterruptedException {
    // workaround for local files
    if (url.startsWith("file:")) {
      String newUrl = url.replace("file:", "");
      File file = new File(newUrl);
      return new FileInputStream(file);
    }

    // replace our API keys for logging...
    String logUrl = url.replaceAll("api_key=\\w+", "api_key=<API_KEY>").replaceAll("api/\\d+\\w+", "api/<API_KEY>");
    LOGGER.debug("getting " + logUrl);
    Request.Builder requestBuilder = new Request.Builder();
    requestBuilder.url(url);

    InputStream is = null;

    // set custom headers
    for (Pair<String, String> header : headersRequest) {
      requestBuilder.addHeader(header.first(), header.second());
    }

    request = requestBuilder.build();

    try {
      call = client.newCall(request);
      response = call.execute();
      headersResponse = response.headers();
      responseCode = response.code();
      responseMessage = response.message();
      responseCharset = response.body().contentType().charset();
      responseContentType = response.body().contentType().toString();
      is = response.body().byteStream();
    }
    catch (InterruptedIOException | IllegalStateException e) {
      LOGGER.info("aborted request: " + logUrl + " ;" + e.getMessage());
      cleanup();
      throw new InterruptedException();
    }
    catch (UnknownHostException e) {
      LOGGER.error("proxy or host not found/reachable", e);
    }
    catch (Exception e) {
      LOGGER.error("Exception getting url " + logUrl, e);
    }
    return is;
  }
}
