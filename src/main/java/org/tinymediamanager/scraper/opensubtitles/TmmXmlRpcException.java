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

package org.tinymediamanager.scraper.opensubtitles;

import de.timroes.axmlrpc.XMLRPCException;

public class TmmXmlRpcException extends XMLRPCException {
  final int    statusCode;
  final String url;

  public TmmXmlRpcException(int statusCode, String url) {
    this.statusCode = statusCode;
    this.url = url;
  }

  public TmmXmlRpcException(Exception ex, String url) {
    super(ex);
    this.statusCode = 0;
    this.url = url;
  }

  public TmmXmlRpcException(String ex, int statusCode, String url) {
    super(ex);
    this.statusCode = statusCode;
    this.url = url;
  }

  public TmmXmlRpcException(String msg, Exception ex, int statusCode, String url) {
    super(msg, ex);
    this.statusCode = statusCode;
    this.url = url;
  }
}
