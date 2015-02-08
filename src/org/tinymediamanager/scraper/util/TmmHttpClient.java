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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.tinymediamanager.Globals;

/**
 * The class HttpClient. To construct our HTTP client for internet access
 * 
 * @author Manuel Laggner
 */
public class TmmHttpClient {
  private static CloseableHttpClient client = createHttpClient();

  /**
   * instantiates a new CloseableHttpClient
   * 
   * @return CloseableHttpClient
   */
  public static CloseableHttpClient createHttpClient() {
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    // Increase default max connection per route to 5
    connectionManager.setMaxTotal(5);

    HttpClientBuilder httpClientBuilder = HttpClients.custom().useSystemProperties();
    httpClientBuilder.setConnectionManager(connectionManager);

    // my own retry handler
    HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
      @Override
      public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        if (executionCount >= 5) {
          // Do not retry if over max retry count
          return false;
        }
        if (exception instanceof InterruptedIOException) {
          // Timeout
          return false;
        }
        if (exception instanceof UnknownHostException) {
          // Unknown host
          return false;
        }
        if (exception instanceof ConnectTimeoutException) {
          // Connection refused
          return false;
        }
        if (exception instanceof SSLException) {
          // SSL handshake exception
          return false;
        }
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        HttpRequest request = clientContext.getRequest();
        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
        if (idempotent) {
          // Retry if the request is considered idempotent
          return true;
        }
        return false;
      }

    };
    httpClientBuilder.setRetryHandler(myRetryHandler);

    // set proxy if needed
    if ((Globals.settings.useProxy())) {
      setProxy(httpClientBuilder);
    }

    return httpClientBuilder.build();
  }

  /**
   * Gets the preconfigured http client.
   * 
   * @return the http client
   */
  public static CloseableHttpClient getHttpClient() {
    return client;
  }

  private static void setProxy(HttpClientBuilder httpClientBuilder) {
    HttpHost proxyHost = null;
    if (StringUtils.isNotEmpty(Globals.settings.getProxyPort())) {
      proxyHost = new HttpHost(Globals.settings.getProxyHost(), Integer.parseInt(Globals.settings.getProxyPort()));
    }
    else {
      proxyHost = new HttpHost(Globals.settings.getProxyHost());
    }

    // authenticate
    if (!StringUtils.isEmpty(Globals.settings.getProxyUsername()) && !StringUtils.isEmpty(Globals.settings.getProxyPassword())) {
      CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

      if (Globals.settings.getProxyUsername().contains("\\")) {
        // use NTLM
        int offset = Globals.settings.getProxyUsername().indexOf("\\");
        String domain = Globals.settings.getProxyUsername().substring(0, offset);
        String username = Globals.settings.getProxyUsername().substring(offset + 1, Globals.settings.getProxyUsername().length());

        credentialsProvider.setCredentials(AuthScope.ANY, new NTCredentials(username, Globals.settings.getProxyPassword(), "", domain));
      }
      else {
        credentialsProvider.setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials(Globals.settings.getProxyUsername(), Globals.settings.getProxyPassword()));
      }

      httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
    }

    // set proxy
    DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHost);
    httpClientBuilder.setRoutePlanner(routePlanner);

    // try to get proxy settings from JRE - is probably added in HttpClient 4.3; fixed with 4.3.3
    // (https://issues.apache.org/jira/browse/HTTPCLIENT-1457)
    // SystemDefaultCredentialsProvider credentialsProvider = new SystemDefaultCredentialsProvider();
    // httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
    // SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());
    // httpClientBuilder.setRoutePlanner(routePlanner);
  }
}
