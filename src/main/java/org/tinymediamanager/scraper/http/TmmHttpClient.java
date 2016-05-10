/*
 * Copyright 2012 - 2016 Manuel Laggner
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

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
    OkHttpClient.Builder builder = new OkHttpClient.Builder();

    // pool
    builder.connectionPool(new ConnectionPool(5, 5000, TimeUnit.MILLISECONDS));

    // timeouts
    builder.connectTimeout(10, TimeUnit.SECONDS);
    builder.writeTimeout(10, TimeUnit.SECONDS);
    builder.readTimeout(30, TimeUnit.SECONDS);

    // Create a custom set of protocols that excludes HTTP/2, since OkHttp doesn't play
    // nicely with nginx over HTTP/2.
    // FIXME: Remove when https://github.com/square/okhttp/issues/2543 is fixed.
    List<Protocol> protocolList = new ArrayList<>();
    protocolList.add(Protocol.SPDY_3);
    protocolList.add(Protocol.HTTP_1_1);
    builder.protocols(protocolList);

    // proxy
    if ((ProxySettings.INSTANCE.useProxy())) {
      setProxy(builder);
    }

    return builder.build();
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

  private static void setProxy(OkHttpClient.Builder builder) {
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

    builder.proxy(proxyHost);
    // authenticate
    if (StringUtils.isNotBlank(ProxySettings.INSTANCE.getUsername()) && StringUtils.isNotBlank(ProxySettings.INSTANCE.getPassword())) {
      builder.authenticator(new Authenticator() {
        @Override
        public Request authenticate(Route route, Response response) throws IOException {
          String credential = Credentials.basic(ProxySettings.INSTANCE.getUsername(), ProxySettings.INSTANCE.getPassword());
          return response.request().newBuilder().header("Proxy-Authorization", credential).build();
        }
      });
    }
  }
}
