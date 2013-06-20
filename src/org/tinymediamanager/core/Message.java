/*
 * Copyright 2012 - 2013 Manuel Laggner
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

/**
 * The Class Message, used to transport messages inside tmm.
 */
public class Message {
  public enum MessageLevel {
    DEBUG, INFO, WARN, ERROR
  }

  private MessageLevel messageLevel;
  private String       messageId;
  private Object       messageSender;

  public Message(Object sender, String id) {
    this(sender, MessageLevel.DEBUG, id);
  }

  public Message(Object sender, MessageLevel level, String id) {
    messageSender = sender;
    messageLevel = level;
    messageId = id;
  }

  public String getMessageId() {
    return messageId;
  }

  public MessageLevel getMessageLevel() {
    return messageLevel;
  }

  public Object getMessageSender() {
    return messageSender;
  }
}
