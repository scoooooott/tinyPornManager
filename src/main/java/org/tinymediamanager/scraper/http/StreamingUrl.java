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

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Response;

/**
 * The class StreamingUrl. Used to build streaming downloads (e.g. bigger files which can't the streamed via a ByteArrayInputStream).
 * 
 * @author Manuel Laggner
 */
public class StreamingUrl extends Url {
  public StreamingUrl(String url) throws IOException {
    super(url);
  }

  /**
   * get the InputStream of the content. Be aware: using this class needs you to close the connection per hand calling the method closeConnection()
   * 
   * @return the InputStream of the content
   */
  @Override
  public InputStream getInputStream() throws IOException, InterruptedException {
    // just call super since the only difference is in getInputStreamInternal
    // this Override is just used to provide a documentation, that the caller need to close the connection
    return super.getInputStream();
  }

  @Override
  protected InputStream getInputstreamInternal(Response response) {
    return response.body().byteStream();
  }
}
