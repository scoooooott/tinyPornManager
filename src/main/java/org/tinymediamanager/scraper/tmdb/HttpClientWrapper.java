package org.tinymediamanager.scraper.tmdb;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.yamj.api.common.http.CommonHttpClient;

/**
 * Created by manuel on 30.01.15.
 */
public class HttpClientWrapper implements CommonHttpClient {
  private static final String INVALID_URL     = "Invalid URL ";
  private static final int    HTTP_STATUS_503 = 503;

  private final HttpClient    httpClient;

  public HttpClientWrapper(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  @Override
  public void setProxy(String host, int port, String username, String password) {
    // do nothing; proxy is set directly in the TmmHttpClient
  }

  @Override
  public void setTimeouts(int connectionTimeout, int socketTimeout) {
    // do nothing; timeouts are set directly in the TmmHttpClient
  }

  @Override
  public String requestContent(URL url) throws IOException {
    return requestContent(url, null);
  }

  @Override
  public String requestContent(URL url, Charset charset) throws IOException {
    URI uri;
    try {
      uri = url.toURI();
    }
    catch (URISyntaxException ex) {
      throw new IllegalArgumentException(INVALID_URL + url, ex);
    }

    return requestContent(uri, charset);
  }

  @Override
  public String requestContent(String uri) throws IOException {
    return requestContent(uri, null);
  }

  @Override
  public String requestContent(String uri, Charset charset) throws IOException {
    final HttpGet httpGet = new HttpGet(uri);
    return requestContent(httpGet, charset);
  }

  @Override
  public String requestContent(URI uri) throws IOException {
    return requestContent(uri, null);
  }

  @Override
  public String requestContent(URI uri, Charset charset) throws IOException {
    final HttpGet httpGet = new HttpGet(uri);
    return requestContent(httpGet, charset);
  }

  @Override
  public String requestContent(HttpGet httpGet) throws IOException {
    return requestContent(httpGet, null);
  }

  @Override
  public String requestContent(HttpGet httpGet, Charset charset) throws IOException {
    try {
      HttpResponse response = execute(httpGet);
      HttpEntity entity = response.getEntity();

      String content = EntityUtils.toString(entity);
      EntityUtils.consume(entity);

      return content;
    }
    catch (IOException e) {
    }
    return "";
  }

  @Override
  public HttpEntity requestResource(URL url) throws IOException {
    URI uri;
    try {
      uri = url.toURI();
    }
    catch (URISyntaxException ex) {
      throw new IllegalArgumentException(INVALID_URL + url, ex);
    }
    return requestResource(uri);
  }

  @Override
  public HttpEntity requestResource(String uri) throws IOException {
    final HttpGet httpGet = new HttpGet(uri);
    return requestResource(httpGet);
  }

  @Override
  public HttpEntity requestResource(URI uri) throws IOException {
    final HttpGet httpGet = new HttpGet(uri);
    return requestResource(httpGet);
  }

  @Override
  public HttpEntity requestResource(HttpGet httpGet) throws IOException {
    return execute(httpGet).getEntity();
  }

  @Override
  public HttpParams getParams() {
    return this.httpClient.getParams();
  }

  @Override
  public ClientConnectionManager getConnectionManager() {
    return this.httpClient.getConnectionManager();
  }

  @Override
  public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
    return httpClient.execute(request);
  }

  @Override
  public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException, ClientProtocolException {
    return httpClient.execute(request, context);
  }

  @Override
  public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
    return httpClient.execute(target, request);
  }

  @Override
  public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
    return httpClient.execute(target, request, context);
  }

  @Override
  public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
    return httpClient.execute(request, responseHandler);
  }

  @Override
  public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException,
      ClientProtocolException {
    return httpClient.execute(request, responseHandler, context);
  }

  @Override
  public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException,
      ClientProtocolException {
    return httpClient.execute(target, request, responseHandler);
  }

  @Override
  public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException,
      ClientProtocolException {
    return httpClient.execute(target, request, responseHandler, context);
  }
}
