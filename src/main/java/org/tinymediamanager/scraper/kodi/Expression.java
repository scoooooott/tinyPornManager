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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

class Expression {
  private String  expression;
  private String  noClean;
  private boolean clear          = true;
  private boolean repeat         = false;
  private String  noCleanArray[] = null;

  public Expression() {
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public String getNoClean() {
    return noClean;
  }

  public void setNoClean(String noClean) {
    this.noClean = noClean;
  }

  public boolean isClear() {
    return clear;
  }

  public void setClear(boolean clear) {
    this.clear = clear;
  }

  public boolean isRepeat() {
    return repeat;
  }

  public void setRepeat(boolean repeat) {
    this.repeat = repeat;
  }

  public String[] getNoCleanArray() {
    if (!StringUtils.isEmpty(getNoClean()) && noCleanArray == null) {
      noCleanArray = new String[21];
      String vals[] = getNoClean().split(",");
      for (String s : vals) {
        noCleanArray[Integer.parseInt(s)] = s;
      }
    }
    return noCleanArray;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
