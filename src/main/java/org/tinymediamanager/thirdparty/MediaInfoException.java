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
package org.tinymediamanager.thirdparty;

import com.sun.jna.Platform;

/**
 * The Class MediaInfoException.
 * 
 * @author Myron Boyle
 */
public class MediaInfoException extends RuntimeException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new media info exception.
   * 
   * @param e
   *          the e
   */
  public MediaInfoException(LinkageError e) {
    this(String.format("Unable to load %d-bit native library 'mediainfo'", Platform.is64Bit() ? 64 : 32), e);
  }

  /**
   * Instantiates a new media info exception.
   * 
   * @param msg
   *          the msg
   * @param e
   *          the e
   */
  public MediaInfoException(String msg, Throwable e) {
    super(msg, e);
  }

}
