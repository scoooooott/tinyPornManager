/*
 * Copyright 2013 Manuel Laggner
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

import java.awt.Rectangle;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class WindowConfig.
 */
@XmlRootElement(name = "WindowConfig")
public class WindowConfig extends AbstractModelObject {

  private HashMap<String, Object> keystore;

  public WindowConfig() {
    keystore = new HashMap<String, Object>();
  }

  public HashMap<String, Object> getKeystore() {
    return keystore;
  }

  public void setKeystore(HashMap<String, Object> keystore) {
    this.keystore = keystore;
  }

  public void addParam(String key, Object value) {
    if (keystore.containsKey(key)) {
      keystore.remove(key);
    }

    keystore.put(key, value);
  }

  private Object getParam(String key) {
    return keystore.get(key);
  }

  public void storeWindowBounds(String name, int x, int y, int width, int height) {
    StringBuilder sb = new StringBuilder(name);
    sb.append("X");
    addParam(sb.toString(), x);

    sb = new StringBuilder(name);
    sb.append("Y");
    addParam(sb.toString(), y);

    sb = new StringBuilder(name);
    sb.append("W");
    addParam(sb.toString(), width);

    sb = new StringBuilder(name);
    sb.append("H");
    addParam(sb.toString(), height);

    firePropertyChange(name, null, x);
  }

  public Rectangle getWindowBounds(String name) {
    Rectangle rect = new Rectangle();

    StringBuilder sb = new StringBuilder(name);
    sb.append("X");
    rect.x = getInteger(sb.toString());

    sb = new StringBuilder(name);
    sb.append("Y");
    rect.y = getInteger(sb.toString());

    sb = new StringBuilder(name);
    sb.append("W");
    rect.width = getInteger(sb.toString());

    sb = new StringBuilder(name);
    sb.append("H");
    rect.height = getInteger(sb.toString());

    return rect;
  }

  public int getInteger(String name) {
    int i = 0;
    Object param = getParam(name);

    if (param instanceof Integer) {
      Integer integer = (Integer) param;
      i = integer;
    }

    return i;
  }

  public boolean getBoolean(String name) {
    boolean b = false;

    Object param = getParam(name);

    if (param instanceof Boolean) {
      Boolean bool = (Boolean) param;
      b = bool;
    }

    return b;
  }

}
