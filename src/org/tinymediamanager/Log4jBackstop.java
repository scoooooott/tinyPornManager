/*
 * Copyright 2012-2013 Manuel Laggner
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
package org.tinymediamanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Log4jBackstop.
 * 
 * @author Manuel Laggner
 */
class Log4jBackstop implements Thread.UncaughtExceptionHandler {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(Log4jBackstop.class);

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread , java.lang.Throwable)
   */
  public void uncaughtException(Thread t, Throwable ex) {
    LOGGER.error("Uncaught exception in thread: " + t.getName(), ex);
  }

}
