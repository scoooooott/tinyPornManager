/*
 * Copyright 2012 - 2020 Manuel Laggner
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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.scraper.exceptions.HttpException;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class UrlTest {
  private static final MockWebServer mockWebServer = new MockWebServer();

  @BeforeClass
  public static void setUp() throws Exception {
    mockWebServer.start();
  }

  @AfterClass
  public static void shutDown() throws Exception {
    mockWebServer.shutdown();
  }

  private HttpUrl createSuccessfullStringResponse() {
    mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("success"));
    return mockWebServer.url("successfulResponse");
  }

  private HttpUrl create404Response() {
    mockWebServer.enqueue(new MockResponse().setResponseCode(404));
    return mockWebServer.url("404Response");
  }

  @Test
  public void testInputStream() throws Exception {
    HttpUrl baseUrl = createSuccessfullStringResponse();

    Url url = new Url(baseUrl.toString());
    InputStream is = url.getInputStream();
    assertThat(IOUtils.toString(is, StandardCharsets.UTF_8)).isEqualTo("success");
  }

  @Test
  public void testResponseCode() throws Exception {
    HttpUrl baseUrl = createSuccessfullStringResponse();

    Url url = new Url(baseUrl.toString());
    url.getInputStream();
    assertThat(url.responseCode).isEqualTo(200);

    baseUrl = create404Response();

    url = new Url(baseUrl.toString());
    try {
      url.getInputStream();
      Assert.fail();
    }
    catch (HttpException e) {
      assertThat(url.responseCode).isEqualTo(404);
    }
  }
}
