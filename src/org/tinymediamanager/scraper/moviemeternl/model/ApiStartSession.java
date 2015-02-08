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
package org.tinymediamanager.scraper.moviemeternl.model;

import java.util.Date;

import redstone.xmlrpc.XmlRpcStruct;

/**
 * @author Myron Boyle
 */
public class ApiStartSession {
  private String session_key;
  private Date   valid_till;
  private String disclaimer;

  public ApiStartSession(XmlRpcStruct struct) {
    this.session_key = struct.getString("session_key");
    this.valid_till = new Date((long) struct.getInteger("valid_till") * 1000);
    this.disclaimer = struct.getString("disclaimer");
  }

  public String getSession_key() {
    return this.session_key;
  }

  public Date getValid_till() {
    return this.valid_till;
  }

  public String getDisclaimer() {
    return this.disclaimer;
  }

}