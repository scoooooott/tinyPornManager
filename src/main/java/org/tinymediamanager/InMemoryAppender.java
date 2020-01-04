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

package org.tinymediamanager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;

/**
 * the class {@link InMemoryAppender} is used to collect logs in memory to provide them per request as text file
 *
 * @author Manuel Laggner
 */
public class InMemoryAppender extends AppenderBase<ILoggingEvent> {
  private static final Logger      LOGGER    = LoggerFactory.getLogger(InMemoryAppender.class);

  private int                      arraySize = 10000;

  private ArrayList<ILoggingEvent> loggingEvents;
  private Encoder<ILoggingEvent>   encoder;

  @Override
  public void start() {
    if (arraySize <= 0) {
      addError("an array size of 0 or lower is not allowed for [\"" + name + "\"].");
      return;
    }

    if (encoder == null) {
      addError("No encoder set for the appender named [\"" + name + "\"].");
      return;
    }

    loggingEvents = new ArrayList<>(arraySize);

    super.start();
  }

  @Override
  public void stop() {
    loggingEvents.clear();
    super.stop();
    loggingEvents = null;
  }

  public void setEncoder(Encoder<ILoggingEvent> encoder) {
    this.encoder = encoder;
  }

  public void setSize(int size) {
    this.arraySize = size;
  }

  @Override
  protected void append(ILoggingEvent e) {
    loggingEvents.add(e);
    if (loggingEvents.size() > arraySize && arraySize > 0) {
      loggingEvents.remove(0);
    }
  }

  public String getLog() {
    // output the events as formatted by our layout
    ArrayList<ILoggingEvent> clone = new ArrayList<>(loggingEvents);

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      for (ILoggingEvent event : clone) {
        out.write(encoder.encode(event));
      }
      return out.toString();
    }
    catch (IOException e) {
      LOGGER.error("could not write trace logging: {}", e.getMessage());
    }

    return "";
  }
}
