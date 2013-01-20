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
package org.tinymediamanager.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.UUID;

import javax.net.ssl.SSLException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.scraper.util.Url;

/**
 * The Class Utils.
 */
public class Utils {

  private static DefaultHttpClient client;
  /** The Constant HTTP_USER_AGENT. */
  protected static final String    HTTP_USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:15.0) Gecko/20100101 Firefox/15.0.1";

  private static final Logger      LOGGER          = Logger.getLogger(Utils.class);

  /**
   * Returns the sortable variant of title/originaltitle<br>
   * eg "The Bourne Legacy" -> "Bourne Legacy, The"
   * 
   * @return the title/originaltitle in its sortable format
   * @author Myron Boyle
   */
  public static String getSortableName(String title) {
    if (title.toLowerCase().matches("^die hard$") || title.toLowerCase().matches("^die hard[:\\s].*")) {
      return title;
    }
    for (String prfx : Settings.getInstance().getTitlePrefix()) {
      title = title.replaceAll("(?i)^" + prfx + " (.*)", "$1, " + prfx);
    }
    return title;
  }

  /**
   * Returns the common name of title/originaltitle when it is named sortable<br>
   * eg "Bourne Legacy, The" -> "The Bourne Legacy"
   * 
   * @param the
   *          title/originaltitle in its sortable format
   * @return the original title
   * @author Myron Boyle
   */
  public static String removeSortableName(String title) {
    for (String prfx : Settings.getInstance().getTitlePrefix()) {
      title = title.replaceAll("(?i)(.*), " + prfx, prfx + " $1");
    }
    return title;
  }

  /**
   * Read file as string. DEPRECATED: use FileUtils.readFileToString(file)
   * 
   * @param file
   *          the file
   * @return the string
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @Deprecated
  public static String readFileAsString(File file) throws java.io.IOException {
    StringBuffer fileData = new StringBuffer(1000);
    BufferedReader reader = new BufferedReader(new FileReader(file));
    char[] buf = new char[1024];
    int numRead = 0;
    while ((numRead = reader.read(buf)) != -1) {
      String readData = String.valueOf(buf, 0, numRead);
      fileData.append(readData);
      buf = new char[1024];
    }

    reader.close();
    return fileData.toString();
  }

  public static String cleanStackingMarkers(String filename) {
    if (!StringUtils.isEmpty(filename)) {
      return filename.replaceAll("(?i)\\|?((cd|dvd|part|dis[ck])([0-9]))", "");
    }
    return filename;
  }

  public static boolean isValidImdbId(String imdbId) {
    if (StringUtils.isEmpty(imdbId)) {
      return false;
    }

    return imdbId.matches("tt\\d{7}");
  }

  @Deprecated
  public static String replaceAcutesHTML(String str) {
    // TODO: use StringEscapeUtils
    str = str.replaceAll("&aacute;", "�");
    str = str.replaceAll("&eacute;", "�");
    str = str.replaceAll("&iacute;", "�");
    str = str.replaceAll("&oacute;", "�");
    str = str.replaceAll("&uacute;", "�");
    str = str.replaceAll("&Aacute;", "�");
    str = str.replaceAll("&Eacute;", "�");
    str = str.replaceAll("&Iacute;", "�");
    str = str.replaceAll("&Oacute;", "�");
    str = str.replaceAll("&Uacute;", "�");
    str = str.replaceAll("&ntilde;", "�");
    str = str.replaceAll("&Ntilde;", "�");

    return str;
  }

  public static String unquote(String str) {
    if (str == null)
      return null;
    return str.replaceFirst("^\\\"(.*)\\\"$", "$1");
  }

  /*
   * provide a httpclient with proxy set
   */
  public static DefaultHttpClient getHttpClient() {
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
    schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

    PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
    // Increase max total connection to 20
    cm.setMaxTotal(20);
    // Increase default max connection per route to 5
    cm.setDefaultMaxPerRoute(5);

    client = new DefaultHttpClient(cm);

    HttpParams params = client.getParams();
    HttpConnectionParams.setConnectionTimeout(params, 10000);
    HttpConnectionParams.setSoTimeout(params, 10000);
    HttpProtocolParams.setUserAgent(params, HTTP_USER_AGENT);

    // my own retry handler
    HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
      public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        if (executionCount >= 5) {
          // Do not retry if over max retry count
          return false;
        }
        if (exception instanceof InterruptedIOException) {
          // Timeout
          return true;
        }
        if (exception instanceof UnknownHostException) {
          // Unknown host
          return false;
        }
        if (exception instanceof ConnectException) {
          // Connection refused
          return false;
        }
        if (exception instanceof SSLException) {
          // SSL handshake exception
          return false;
        }
        HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
        if (idempotent) {
          // Retry if the request is considered idempotent
          return true;
        }
        return false;
      }
    };

    client.setHttpRequestRetryHandler(myRetryHandler);

    if ((Globals.settings.useProxy())) {
      setProxy(client);
    }

    return client;
  }

  /**
   * Sets the proxy.
   * 
   * @param httpClient
   *          the new proxy
   */
  protected static void setProxy(DefaultHttpClient httpClient) {
    HttpHost proxyHost = new HttpHost(Globals.settings.getProxyHost(), Integer.parseInt(Globals.settings.getProxyPort()));

    // authenticate
    if (!StringUtils.isEmpty(Globals.settings.getProxyUsername()) && !StringUtils.isEmpty(Globals.settings.getProxyPassword())) {
      if (Globals.settings.getProxyUsername().contains("\\")) {
        // use NTLM
        int offset = Globals.settings.getProxyUsername().indexOf("\\");
        String domain = Globals.settings.getProxyUsername().substring(0, offset);
        String username = Globals.settings.getProxyUsername().substring(offset + 1, Globals.settings.getProxyUsername().length());
        httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY,
            new NTCredentials(username, Globals.settings.getProxyPassword(), "", domain));
      }
      else {
        httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials(Globals.settings.getProxyUsername(), Globals.settings.getProxyPassword()));
      }
    }

    // set proxy
    httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
  }

  /**
   * Does a "ping" on our tracking server, sending the event (and the random
   * UUID)
   * 
   * @param event
   *          The event for the GET request
   */
  public static void trackEvent(String event) {
    try {
      File uuidFile = new File(Utils.class.getClassLoader().getResource(".").getPath() + "/tmm.uuid");
      if (!uuidFile.exists()) {
        FileUtils.write(uuidFile, UUID.randomUUID().toString());
      }
      /*
       * if (uuidFile.exists()) { String uuid =
       * FileUtils.readFileToString(uuidFile); // TODO: cached or not? depends
       * on usage... Url url = new
       * CachedUrl("http://update.tinymediamanager.org/track.php?uuid=" + uuid +
       * "&event=" + event); InputStream in = url.getInputStream(); in.close();
       * }
       */
    }
    catch (IOException e) {
      LOGGER.warn("Could not create UUID");
    }
  }

}
