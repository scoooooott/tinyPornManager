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

package org.tinymediamanager.scraper.exceptions;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

/**
 * the class {@link HttpException} is thrown if there has been a HTTP exception while querying the external source
 *
 * @author Manuel Laggner
 * @since 3.0
 */
public class HttpException extends IOException {
  private static final long serialVersionUID = 2161692612692312193L;

  private final String      url;
  private final int         statusCode;
  private final String      message;

  public HttpException(int statusCode, String message) {
    this.url = null;
    this.statusCode = statusCode;
    this.message = message;
  }

  public HttpException(String url, int statusCode, String message) {
    super();
    this.url = url;
    this.statusCode = statusCode;
    this.message = message;
  }

  public int getStatusCode() {
    return statusCode;
  }

  @Override
  public String toString() {
    if (StringUtils.isNotBlank(url)) {
      return "HTTP " + statusCode + " / " + message + " | " + url;
    }
    else {
      return "HTTP " + statusCode + " / " + message;
    }
  }

  @Override
  public String getMessage() {
    return toString();
  }
}
