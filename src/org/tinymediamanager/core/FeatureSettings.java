/*
 * Copyright 2012 - 2013 Manuel Laggner
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
 * The Class MovieSettings.
 */
@XmlRootElement(name = "FeatureSettings")
public class FeatureSettings extends AbstractModelObject {

  // all named features
  private boolean donator = false;

  public FeatureSettings() {
  }

  /**
   * Have we donated?
   * 
   * @return true/false
   */
  public boolean isDonator() {
    return this.donator;
  }

  private void setDonator(boolean newValue) {
    boolean oldValue = this.donator;
    this.donator = newValue;
    firePropertyChange("donator", oldValue, newValue);
  }

  /**
   * Sets all the features on startup<br>
   * Do not care about actual value, just overwrite.
   */
  public void setFeatures() {
    setDonator(false);
    // setFeatureA(true);
    // setFeatureX(false);
    if (License.isValid()) {
      setDonator(true);
      // setFeatureX(true);
    }
  }

}
