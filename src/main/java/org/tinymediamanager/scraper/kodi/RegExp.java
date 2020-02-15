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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * This class represents a Kodi addon regexp
 *
 * @author Manuel Laggner, Myron Boyle
 */
class RegExp implements RegExpContainer, Cloneable {
  private String       input, output;
  private int          dest;
  private String       conditional;
  private boolean      appendBuffer = false;
  private List<RegExp> children     = new ArrayList<>();
  private Expression   expression;

  public RegExp() {
  }

  @Override
  public RegExp clone() {
    RegExp c = new RegExp();
    c.input = this.input;
    c.output = this.output;
    c.dest = this.dest;
    c.conditional = this.conditional;
    c.appendBuffer = this.appendBuffer;
    c.expression = this.expression;
    for (RegExp r : this.children) {
      c.addRegExp(r.clone());
    }
    return c;
  }

  @Override
  public void addRegExp(RegExp regexp) {
    children.add(regexp);
  }

  @Override
  public RegExp[] getRegExps() {
    return children.toArray(new RegExp[children.size()]);
  }

  @Override
  public boolean hasRegExps() {
    return children != null && !children.isEmpty();
  }

  public String getInput() {
    return input;
  }

  public String getOutput() {
    return output;
  }

  public int getDest() {
    return dest;
  }

  public void setInput(String input) {
    if (StringUtils.isEmpty(input))
      input = "$$1";
    this.input = input;
  }

  public void setOutput(String output) {
    this.output = output;
  }

  public void setDest(int dest) {
    this.dest = dest;
  }

  public Expression getExpression() {
    return expression;
  }

  public void setExpression(Expression expression) {
    this.expression = expression;
  }

  public boolean isAppendBuffer() {
    return appendBuffer;
  }

  public void setAppendBuffer(boolean appendBuffer) {
    this.appendBuffer = appendBuffer;
  }

  public String getConditional() {
    return conditional;
  }

  public void setConditional(String conditional) {
    this.conditional = conditional;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
