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

import com.squareup.okhttp.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * The class HttpClient. To construct our HTTP client for internet access
 * 
 * @author Manuel Laggner
 * @since 1.0
 */
public class TmmHttpClient {
  private static OkHttpClient client = createHttpClient();

  /**
   * instantiates a new OkHttpClient
   * 
   * @return OkHttpClient
   */
  public static OkHttpClient createHttpClient() {
    OkHttpClient client = new OkHttpClient();

    // pool
    client.setConnectionPool(new ConnectionPool(5, 5000));

    // timeouts
    client.setConnectTimeout(10, TimeUnit.SECONDS);
    client.setWriteTimeout(10, TimeUnit.SECONDS);
    client.setReadTimeout(30, TimeUnit.SECONDS);

    // default caching (cache/url - 5mb)
    try {
      client.setCache(new Cache(new File("cache/url"), 5000000));
    }
    catch (Exception ignored) {
    }

    // proxy
    if ((ProxySettings.INSTANCE.useProxy())) {
      setProxy(client);
    }

    return client;
  }

  /**
   * Gets the preconfigured http client.
   * 
   * @return the http client
   */
  public static OkHttpClient getHttpClient() {
    return client;
  }

  /**
   * proxy settings have been changed in the class ProxySettings
   */
  static void changeProxy() {
    // recreate a new client instance
    client = createHttpClient();
  }

  private static void setProxy(OkHttpClient client) {
    Proxy proxyHost;

    if (ProxySettings.INSTANCE.getPort() > 0) {
      proxyHost = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(ProxySettings.INSTANCE.getHost(), ProxySettings.INSTANCE.getPort()));
    }
    else if (StringUtils.isNotBlank(ProxySettings.INSTANCE.getHost())) {
      proxyHost = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(ProxySettings.INSTANCE.getHost(), 80));
    }
    else {
      // no proxy settings found. return
      return;
    }

    client.setProxy(proxyHost);
    // authenticate
    if (StringUtils.isNotBlank(ProxySettings.INSTANCE.getUsername()) && StringUtils.isNotBlank(ProxySettings.INSTANCE.getPassword())) {
      client.setAuthenticator(new Authenticator() {
        @Override
        public Request authenticate(Proxy proxy, Response response) {
          return null; // Null indicates no attempt to authenticate.
        }

        @Override
        public Request authenticateProxy(Proxy proxy, Response response) {
          String credential = Credentials.basic(ProxySettings.INSTANCE.getUsername(), ProxySettings.INSTANCE.getPassword());
          return response.request().newBuilder().header("Proxy-Authorization", credential).build();
        }
      });
    }
  }
}
