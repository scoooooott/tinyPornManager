/*
 * Copyright 2012 - 2018 Manuel Laggner
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.util.Pair;
import org.tinymediamanager.scraper.util.UrlUtil;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * The Class Url. Used to make simple, blocking URL requests. The request is temporarily streamed into a ByteArrayInputStream, before the InputStream
 * is passed to the caller.
 * 
 * @author Manuel Laggner / Myron Boyle
 */
public class Url {
  private static final Logger          LOGGER                = LoggerFactory.getLogger(Url.class);
  protected static OkHttpClient        client;

  protected static final String        USER_AGENT            = "User-Agent";
  // where is such a list in std java?
  // https://github.com/xbmc/xbmc/blob/master/xbmc/addons/kodi-addon-dev-kit/include/kodi/Filesystem.h#L195
  public static final List<String>     KNOWN_HEADERS         = Arrays.asList("accept", "accept-charset", "accept-encoding", "accept-language",
      "authorization", "cookie", "customrequest", "noshout", "postdata", "referer", "user-agent", "seekable", "sslcipherlist", "Via");
  protected int                        responseCode          = 0;
  protected String                     responseMessage       = "";
  protected Charset                    responseCharset       = null;
  protected String                     responseContentType   = "";
  protected long                       responseContentLength = -1;

  protected String                     url                   = null;
  protected Headers                    headersResponse       = null;
  protected List<Pair<String, String>> headersRequest        = new ArrayList<>();
  protected URI                        uri                   = null;

  protected Call                       call                  = null;
  protected Request                    request               = null;
  protected Response                   response              = null;

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

    String h = headersResponse.get(header);

    if (StringUtils.isBlank(h)) {
      return "";
    }
    return h;
  }

  /**
   * get all response headers
   * 
   * @return the response headers
   */
  public Headers getHeadersResponse() {
    return headersResponse;
  }

  /**
   * Instantiates a new url / httpclient with default user-agent.
   * 
   * @param url
   *          the url
   */
  public Url(String url) throws MalformedURLException {
    if (client == null) {
      client = TmmHttpClient.getHttpClient();
    }
    this.url = url;

    if (url.contains("|")) {
      splitHeadersFromUrl();
    }

    // morph to URI to check syntax of the url
    try {
      uri = morphStringToUri(url);
    }
    catch (URISyntaxException e) {
      throw new MalformedURLException(url);
    }

    // default user agent
    addHeader(USER_AGENT, UrlUtil.generateUA());
  }

  /**
   * A constructor for inherited classes which needs a special setup
   */
  protected Url() {
  }

  /**
   * pipe could be delimiter for header values (like seen in Kodi)<br>
   * http://www.asdfcom/page?what=do|Referer=http://my.site.com<br>
   * http://de.clip-1.filmtrailer.com/2845_14749_a_4.flv?log_var=67|491100001-1|-<br>
   * split away from url, and add as header
   * 
   */
  protected void splitHeadersFromUrl() {
    Pattern p = Pattern.compile(".*\\|(.*?)=(.*?)$");
    Matcher m = p.matcher(this.url);
    if (m.find()) {
      if (KNOWN_HEADERS.contains(m.group(1).toLowerCase(Locale.ROOT))) {
        // ok, url might have a pipe, but we now have a recognized header - set it
        this.url = this.url.substring(0, m.start(1) - 1); // -1 is pipe char
        addHeader(m.group(1), m.group(2));
      }
    }
  }

  /**
   * morph the url (string) to an URI to check the syntax and escape the path
   * 
   * @param urlToMorph
   *          the url to morph
   * @return the morphed URI
   * @throws MalformedURLException
   * @throws URISyntaxException
   */
  protected URI morphStringToUri(String urlToMorph) throws MalformedURLException, URISyntaxException {
    URL url = new URL(urlToMorph);
    return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
  }

  /**
   * set a specified User-Agent
   * 
   * @param userAgent
   *          the user agent to be set
   */
  public void setUserAgent(String userAgent) {
    addHeader(USER_AGENT, userAgent);
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

    LOGGER.trace("add HTTP header: " + key + "=" + value);

    // looks like there is no need for duplicate check since some headers can
    // occur several times
    // Typically HTTP headers work like a Map<String, String>: each field has
    // one value or none. But some headers permit multiple values, like Guava's
    // Multimap. For example, it's legal and common for an HTTP response to
    // supply multiple Vary headers.
    for (int i = headersRequest.size() - 1; i >= 0; i--) {
      Pair<String, String> header = headersRequest.get(i);
      if (key.equals(header.first())) {
        headersRequest.remove(i);
      }
    }

    // and add the new one
    headersRequest.add(new Pair<>(key, value));
  }

  /**
   * Adds the header.
   * 
   * @param header
   *          the header
   */
  public void addHeader(Pair<String, String> header) {
    headersRequest.add(header);
  }

  /**
   * Adds the headers.
   * 
   * @param headers
   *          the headers
   */
  public void addHeaders(List<Pair<String, String>> headers) {
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
      String newUrl = url.replace("file:/", "");
      File file = new File(newUrl);
      return new FileInputStream(file);
    }

    InputStream is = null;

    // replace our API keys for logging...
    String logUrl = url.replaceAll("api_key=\\w+", "api_key=<API_KEY>").replaceAll("api/\\d+\\w+", "api/<API_KEY>");
    LOGGER.debug("getting " + logUrl);

    Request.Builder requestBuilder = new Request.Builder();
    requestBuilder.url(url);

    // set custom headers
    for (Pair<String, String> header : headersRequest) {
      requestBuilder.addHeader(header.first().toString(), header.second().toString());
    }

    request = requestBuilder.build();

    try {
      call = client.newCall(request);
      response = call.execute();
      headersResponse = response.headers();
      responseCode = response.code();
      responseMessage = response.message();

      // log any "connection problems"
      if (responseCode < 200 || responseCode >= 400) {
        cleanup();
        LOGGER.error("bad http response: " + responseCode + " ; " + responseMessage);
        return null;
      }

      if (response.body().contentType() != null) { // could be null, see AnimeDB
        responseCharset = response.body().contentType().charset();
        responseContentType = response.body().contentType().toString();
      }
      is = response.body().byteStream();
    }
    catch (InterruptedIOException | IllegalStateException e) {
      LOGGER.info("aborted request: " + logUrl + " ; " + e.getMessage());
      cleanup();
      throw new InterruptedException();
    }
    catch (UnknownHostException e) {
      cleanup();
      LOGGER.error("proxy or host not found/reachable; " + e.getMessage());
    }
    catch (Exception e) {
      cleanup();
      LOGGER.error("Exception getting url " + logUrl + " ; " + e.getMessage(), e);
    }
    return is;
  }

  /**
   * gets the url with the given amount of retries
   * 
   * @param retries
   *          the amount of retries (>0)
   * @return the InputStream or null
   */
  public InputStream getInputStreamWithRetry(int retries) {
    if (retries <= 0) {
      return null;
    }

    InputStream is = null;

    int counter = 0;
    do {
      counter++;
      try {
        is = getInputStream();
      }
      catch (InterruptedIOException | IllegalStateException | InterruptedException e) {
        // has this thread been interrupted?
        return null;
      }
      catch (Exception ignored) {
      }
      if (is != null || (is == null && getStatusCode() > 0 && getStatusCode() < 500)) {
        // we either got a response or a permanent failure
        return is;
      }
      // has this thread been interrupted?
      if (Thread.interrupted()) {
        return null;
      }
      LOGGER.info("could not fetch: " + url + " - retrying");
    } while (counter <= retries);

    return null;
  }

  /**
   * Cleanup the connection
   */
  protected void cleanup() {
    if (call != null) {
      call.cancel();
    }
    if (response != null) {
      response.close();
    }
  }

  /**
   * is the HTTP status code a 4xx/5xx?
   * 
   * @return true/false
   */
  public boolean isFault() {
    return (responseCode >= 400);
  }

  /**
   * http status code
   */
  public int getStatusCode() {
    return responseCode;
  }

  /**
   * http status string
   */
  public String getStatusLine() {
    return responseMessage;
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
    IOUtils.closeQuietly(is);
    return bytes;
  }

  /**
   * Gets the bytes with the given amount of retries
   *
   * @param retries
   *          the amount of retries (>0)
   * @return the bytes or an empty array
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public byte[] getBytesWithRetry(int retries) throws IOException {
    InputStream is = getInputStreamWithRetry(retries);
    if (is != null) {
      byte[] bytes = IOUtils.toByteArray(is);
      IOUtils.closeQuietly(is);
      return bytes;
    }
    return new byte[0];
  }

  /**
   * Download an Url to a file via NIO FileChannel (synchron)
   * 
   * @param file
   * @return successful or not
   */
  public boolean download(File file) {
    try {
      InputStream is = getInputStream();
      if (is == null) {
        return false;
      }
      ReadableByteChannel rbc = Channels.newChannel(is);
      FileOutputStream fos = new FileOutputStream(file);
      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
      fos.close();
      return true;
    }
    catch (IOException e) {
      LOGGER.error("Error downloading " + this.url);
    }
    catch (InterruptedException ignored) {
      if (call != null) {
        call.cancel();
      }
    }
    return false;
  }

  /**
   * Download an Url to a file via NIO FileChannel (synchron)
   * 
   * @param file
   * @return successful or not
   */
  public boolean download(Path file) {
    return download(file.toFile());
  }

  /**
   * Gets the charset.
   * 
   * @return the charset
   */
  public Charset getCharset() {
    if (responseCharset == null) {
      return Charset.defaultCharset();
    }
    return responseCharset;
  }

  /**
   * Gets the content encoding.
   * 
   * @return the content encoding
   */
  public String getContentEncoding() {
    return responseContentType;
  }

  /**
   * the number of bytes of the content, or a negative number if unknown. If the content length is known but exceeds Long.MAX_VALUE, a negative number
   * is returned.
   * 
   * @return the content length
   */
  public long getContentLength() {
    return responseContentLength;
  }

  @Override
  public String toString() {
    return url;
  }
}
