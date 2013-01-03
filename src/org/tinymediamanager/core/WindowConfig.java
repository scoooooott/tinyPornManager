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

import java.util.HashMap;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class WindowConfig.
 */
@XmlRootElement(name = "WindowConfig")
public class WindowConfig extends AbstractModelObject {

  private int                     mainWindowX                = 0;
  private int                     mainWindowY                = 0;
  private int                     mainWindowWidth            = 0;
  private int                     mainWindowHeight           = 0;
  private boolean                 mainWindowMaximized        = false;

  private int                     movieWindowSlider1Position = 0;
  private int                     movieWindowSlider2Position = 0;

  private int                     movieChooserX              = 0;
  private int                     movieChooserY              = 0;
  private int                     movieChooserWidth          = 0;
  private int                     movieChooserHeight         = 0;

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

  public Object getParam(String key) {
    return keystore.get(key);
  }

  public int getMainWindowX() {
    return mainWindowX;
  }

  public void setMainWindowX(int newValue) {
    int oldValue = this.mainWindowX;
    this.mainWindowX = newValue;
    firePropertyChange("mainWindowX", oldValue, newValue);
  }

  public int getMainWindowY() {
    return mainWindowY;
  }

  public void setMainWindowY(int newValue) {
    int oldValue = this.mainWindowY;
    this.mainWindowY = newValue;
    firePropertyChange("mainWindowY", oldValue, newValue);
  }

  public int getMainWindowWidth() {
    return mainWindowWidth;
  }

  public void setMainWindowWidth(int newValue) {
    int oldValue = this.mainWindowWidth;
    this.mainWindowWidth = newValue;
    firePropertyChange("mainWindowWidth", oldValue, newValue);
  }

  public int getMainWindowHeight() {
    return mainWindowHeight;
  }

  public void setMainWindowHeight(int newValue) {
    int oldValue = this.mainWindowHeight;
    this.mainWindowHeight = newValue;
    firePropertyChange("mainWindowHeight", oldValue, newValue);
  }

  public boolean isMainWindowMaximized() {
    return mainWindowMaximized;
  }

  public void setMainWindowMaximized(boolean newValue) {
    boolean oldValue = this.mainWindowMaximized;
    this.mainWindowMaximized = newValue;
    firePropertyChange("mainWindowMaximized", oldValue, newValue);
  }

  public int getMovieWindowSlider1Position() {
    return movieWindowSlider1Position;
  }

  public int getMovieWindowSlider2Position() {
    return movieWindowSlider2Position;
  }

  public void setMovieWindowSlider1Position(int newValue) {
    int oldValue = this.movieWindowSlider1Position;
    this.movieWindowSlider1Position = newValue;
    firePropertyChange("movieWindowSlider1", oldValue, newValue);
  }

  public void setMovieWindowSlider2Position(int newValue) {
    int oldValue = this.movieWindowSlider2Position;
    this.movieWindowSlider2Position = newValue;
    firePropertyChange("movieWindowSlider2", oldValue, newValue);
  }

  public int getMovieChooserX() {
    return movieChooserX;
  }

  public void setMovieChooserX(int newValue) {
    int oldValue = this.movieChooserX;
    this.movieChooserX = newValue;
    firePropertyChange("movieChooserX", oldValue, newValue);
  }

  public int getMovieChooserY() {
    return movieChooserY;
  }

  public void setMovieChooserY(int newValue) {
    int oldValue = this.movieChooserY;
    this.movieChooserY = newValue;
    firePropertyChange("movieChooserY", oldValue, newValue);
  }

  public int getMovieChooserWidth() {
    return movieChooserWidth;
  }

  public void setMovieChooserWidth(int newValue) {
    int oldValue = this.movieChooserWidth;
    this.movieChooserWidth = newValue;
    firePropertyChange("movieChooserWidth", oldValue, newValue);
  }

  public int getMovieChooserHeight() {
    return movieChooserHeight;
  }

  public void setMovieChooserHeight(int newValue) {
    int oldValue = this.movieChooserHeight;
    this.movieChooserHeight = newValue;
    firePropertyChange("movieChooserHeight", oldValue, newValue);
  }

}
