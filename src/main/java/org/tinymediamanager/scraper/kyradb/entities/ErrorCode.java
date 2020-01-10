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

package org.tinymediamanager.scraper.kyradb.entities;

public enum ErrorCode {

  UNKNOWN(-1),
  SUCCESSFUL(0),
  API_INVALID(1),
  API_BLOCKED(2),
  INVALID_URL(3),
  NO_RESULT(4);

  private final int code;

  private ErrorCode(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
