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
package org.tinymediamanager.ui;

import org.slf4j.LoggerFactory;
import org.tinymediamanager.ui.TmmUILogAppender.LogOutput;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;

/**
 * The Class TmmUILogCollector, collect all logs and store it for the UI.
 */
public class TmmUILogCollector {
  public static final String            APPENDER = "UI";
  public static final TmmUILogCollector instance = new TmmUILogCollector();

  private final TmmUILogAppender        logAppender;

  // just to trigger class loading and initializing
  public static void init() {
  }

  private TmmUILogCollector() {
    Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    // create a new TmmUILogAppender - so we need not to put it in the logback.xml:
    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
    encoder.setContext(rootLogger.getLoggerContext());
    encoder.setPattern("%d{HH:mm:ss.SSS} %-5level %logger{60} - %msg%n");
    encoder.start();

    logAppender = new TmmUILogAppender("ERROR");
    logAppender.setContext(rootLogger.getLoggerContext());
    logAppender.setEncoder(encoder);
    logAppender.start();

    rootLogger.addAppender(logAppender);
  }

  public LogOutput getLogOutput() {
    return getLogOutput(0);
  }

  public LogOutput getLogOutput(final int from) {
    LogOutput output = null;
    synchronized (logAppender) {
      output = logAppender.getLogOutput(from);
    }
    return output;
  }
}
