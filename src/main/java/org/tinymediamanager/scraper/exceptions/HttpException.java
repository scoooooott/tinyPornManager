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

package org.tinymediamanager.scraper.exceptions;

/**
 * the class {@link HttpException} is thrown if there has been a HTTP exception while querying the external source
 *
 * @author Manuel Laggner
 * @since 3.0
 */
public class HttpException extends Exception {
  private static final long serialVersionUID = 2161692612692312193L;

  private final int         statusCode;

  public HttpException(String message, int statusCode) {
    super(message);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }

  @Override
  public String toString() {
    return "HTTP " + statusCode + " - " + getMessage();
  }
}
