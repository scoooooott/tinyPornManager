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
package org.tinymediamanager.core;

import java.awt.Rectangle;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class WindowConfig.
 */
@XmlRootElement(name = "WindowConfig")
public class WindowConfig extends AbstractModelObject {

  /** The keystore. */
  private HashMap<String, Object> keystore;

  /**
   * Instantiates a new window config.
   */
  public WindowConfig() {
    keystore = new HashMap<String, Object>();
  }

  /**
   * Gets the keystore.
   * 
   * @return the keystore
   */
  public HashMap<String, Object> getKeystore() {
    return keystore;
  }

  /**
   * Sets the keystore.
   * 
   * @param keystore
   *          the keystore
   */
  public void setKeystore(HashMap<String, Object> keystore) {
    this.keystore = keystore;
  }

  /**
   * Adds the param.
   * 
   * @param key
   *          the key
   * @param value
   *          the value
   */
  public void addParam(String key, Object value) {
    if (keystore.containsKey(key)) {
      keystore.remove(key);
    }

    keystore.put(key, value);
  }

  /**
   * Gets the param.
   * 
   * @param key
   *          the key
   * @return the param
   */
  private Object getParam(String key) {
    return keystore.get(key);
  }

  /**
   * Store window bounds.
   * 
   * @param name
   *          the name
   * @param x
   *          the x
   * @param y
   *          the y
   * @param width
   *          the width
   * @param height
   *          the height
   */
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

  /**
   * Gets the window bounds.
   * 
   * @param name
   *          the name
   * @return the window bounds
   */
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

  /**
   * Gets the integer.
   * 
   * @param name
   *          the name
   * @return the integer
   */
  public int getInteger(String name) {
    int i = 0;
    Object param = getParam(name);

    if (param instanceof Integer) {
      Integer integer = (Integer) param;
      i = integer;
    }

    return i;
  }

  /**
   * Gets the boolean.
   * 
   * @param name
   *          the name
   * @return the boolean
   */
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
