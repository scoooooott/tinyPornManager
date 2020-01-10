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
package org.tinymediamanager.core;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The class WolDevice - to reprensent a Wake On LAN device
 * 
 * @author Manuel Laggner
 * 
 */
@XmlRootElement(name = "WolDevice")
public class WolDevice extends AbstractModelObject {
  private static final String NAME        = "name";
  private static final String MAC_ADDRESS = "macAddress";

  private String              name;
  private String              macAddress;

  public String getName() {
    return name;
  }

  public String getMacAddress() {
    return macAddress;
  }

  public void setName(String newValue) {
    String oldValue = this.name;
    this.name = newValue;
    firePropertyChange(NAME, oldValue, newValue);
  }

  public void setMacAddress(String newValue) {
    String oldValue = this.macAddress;
    this.macAddress = newValue;
    firePropertyChange(MAC_ADDRESS, oldValue, newValue);
  }
}
