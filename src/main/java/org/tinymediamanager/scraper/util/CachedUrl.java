/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.StatusLine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * The class CachedUrl is used to cache some sort of Urls (e.g. when they are accessed several times in a short period)
 */
public class CachedUrl extends Url {
  private final static CacheMap<String, CachedRequest> CACHE = new CacheMap<String, CachedRequest>(60, 5);

  /**
   * Instantiates a new cached url
   *
   * @param url
   *          the url
   */
  public CachedUrl(String url) throws MalformedURLException {
    this.url = url;
    // morph to URI to check syntax of the url
    try {
      this.uri = morphStringToUri(url);
    }
    catch (URISyntaxException e) {
      throw new MalformedURLException(url);
    }
  }

  public InputStream getInputStream() throws IOException, InterruptedException {
    CachedRequest cachedRequest = CACHE.get(url);
    if (cachedRequest == null) {
      // need to fetch it with a real request
      Url url = new Url(this.url);
      url.headersRequest = headersRequest;
      InputStream is = url.getInputStream();
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
      IOUtils.copy(is, gzip);
      IOUtils.closeQuietly(gzip);
      IOUtils.closeQuietly(outputStream);

      // and now fill the CachedRequest object with the result
      cachedRequest = new CachedRequest();
      cachedRequest.url = url.url;
      cachedRequest.uri = url.uri;
      cachedRequest.content = outputStream.toByteArray();
      cachedRequest.responseStatus = url.responseStatus;
      cachedRequest.headersResponse = url.headersResponse;
      cachedRequest.headersRequest = url.headersRequest;
    }

    responseStatus = cachedRequest.responseStatus;
    headersResponse = cachedRequest.headersResponse;

    GZIPInputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(cachedRequest.content));
    return inputStream;
  }

  /**
   * A inner class for representing cached entries
   */
  private static class CachedRequest {
    String       url;
    byte[]       content;
    StatusLine   responseStatus;
    Header[]     headersResponse;
    List<Header> headersRequest;
    URI          uri;
  }
}
