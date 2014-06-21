/*
 * Copyright 2012 - 2014 Manuel Laggner
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
package org.tinymediamanager.scraper.opensubtitles.model;

import redstone.xmlrpc.XmlRpcStruct;

/**
 * @author Myron Boyle
 */
public class ApiStartSession {
  private double seconds;
  private String status;
  private String token;

  public ApiStartSession(XmlRpcStruct struct) {
    this.seconds = struct.getDouble("seconds");
    this.status = struct.getString("status");
    this.token = struct.getString("token");
  }

  public double getSeconds() {
    return this.seconds;
  }

  public String getStatus() {
    return this.status;
  }

  public String getToken() {
    return this.token;
  }

}