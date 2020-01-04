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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;

/**
 * The Class TmmUILogAppender, receive logs from logback and forward it to the UI.
 *
 * @author Manuel Laggner
 */
public class TmmUILogAppender extends OutputStreamAppender<ILoggingEvent> {
  private final CopyableByteArrayOutputStream BUFFER = new CopyableByteArrayOutputStream(2 * 1024 * 1024);

  public TmmUILogAppender(String level) {
    super();
    ThresholdFilter filter = new ThresholdFilter();
    filter.setLevel(level);
    filter.start();
    addFilter(filter);
  }

  @Override
  public void start() {
    setOutputStream(BUFFER);
    super.start();
  }

  @Override
  public void setOutputStream(final OutputStream outputStream) {
    if (outputStream != BUFFER) {
      throw new IllegalStateException("Invalid output stream (" + CopyableByteArrayOutputStream.class + " expected) !");
    }
    super.setOutputStream(outputStream);
  }

  /**
   * get the new log messages (from offset "from" to the end)
   */
  public LogOutput getLogOutput(final int from) {
    final byte[] buffer;
    final int size;

    try {
      lock.lock();
      size = BUFFER.size();
      buffer = (from < size) ? BUFFER.toByteArray(from) : null;
    }
    finally {
      lock.unlock();
    }

    return new LogOutput(size, (buffer != null) ? new String(buffer, 0, buffer.length) : "");
  }

  private static class CopyableByteArrayOutputStream extends ByteArrayOutputStream {

    protected CopyableByteArrayOutputStream(final int size) {
      super(size);
    }

    protected synchronized byte[] toByteArray(final int from) {
      final int pos = (from < 0 || from > count) ? 0 : from;
      return copyOfRange(this.buf, pos, this.count);
    }

    public static byte[] copyOfRange(final byte[] original, final int from, final int to) {
      final int newLength = to - from;
      if (newLength < 0) {
        throw new IllegalArgumentException(from + " > " + to);
      }
      final byte[] copy = new byte[newLength];
      System.arraycopy(original, from, copy, 0, Math.min(original.length - from, newLength));
      return copy;
    }
  }

  public static class LogOutput {
    private final int    byteCount;
    private final String content;

    protected LogOutput(final int byteCount, final String content) {
      this.byteCount = byteCount;
      this.content = content;
    }

    public int getByteCount() {
      return byteCount;
    }

    public String getContent() {
      return content;
    }
  }
}
