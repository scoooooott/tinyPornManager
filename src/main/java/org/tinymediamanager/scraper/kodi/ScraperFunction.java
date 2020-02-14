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
package org.tinymediamanager.scraper.kodi;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * This class represents scraper functions
 *
 * @author Manuel Laggner, Myron Boyle
 */
class ScraperFunction implements RegExpContainer, Cloneable {
  private boolean      clearBuffers = true;
  private int          dest         = 0;
  private boolean      appendBuffer = false;
  private String       name;
  private List<RegExp> regexps      = new ArrayList<>();

  public ScraperFunction() {
  }

  @Override
  public ScraperFunction clone() {
    ScraperFunction c = new ScraperFunction();
    c.setClearBuffers(this.clearBuffers);
    c.setDest(this.dest);
    c.setAppendBuffer(this.appendBuffer);
    c.setName(this.name);
    for (RegExp r : this.regexps) {
      c.addRegExp(r.clone());
    }
    return c;
  }

  public boolean isClearBuffers() {
    return clearBuffers;
  }

  public void setClearBuffers(boolean clearBuffers) {
    this.clearBuffers = clearBuffers;
  }

  public int getDest() {
    return dest;
  }

  public void setDest(int dest) {
    this.dest = dest;
  }

  public void setName(String nodeName) {
    this.name = nodeName;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public void addRegExp(RegExp regexp) {
    regexps.add(regexp);
  }

  @Override
  public RegExp[] getRegExps() {
    return regexps.toArray(new RegExp[regexps.size()]);
  }

  @Override
  public boolean hasRegExps() {
    return regexps != null && !regexps.isEmpty();
  }

  public boolean isAppendBuffer() {
    return appendBuffer;
  }

  public void setAppendBuffer(boolean appendBuffer) {
    this.appendBuffer = appendBuffer;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
