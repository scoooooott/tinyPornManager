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
 * 
 * @author Manuel Laggner
 */
public class Message {
  public enum MessageLevel {
    DEBUG, INFO, WARN, ERROR
  }

  private MessageLevel messageLevel;
  private String       messageId;
  private String[]     messageIdParams;
  private Object       messageSender;
  private String[]     messageSenderParams;

  public Message(Object sender, String id) {
    this(MessageLevel.DEBUG, sender, id);
  }

  public Message(MessageLevel level, Object sender, String id) {
    this(level, sender, new String[0], id, new String[0]);
  }

  public Message(MessageLevel level, Object sender, String[] senderParams, String id) {
    this(level, sender, senderParams, id, new String[0]);
  }

  public Message(MessageLevel level, Object sender, String id, String[] idParams) {
    this(level, sender, new String[0], id, idParams);
  }

  public Message(MessageLevel level, Object sender, String[] senderParams, String id, String[] idParams) {
    messageSender = sender;
    messageLevel = level;
    messageId = id;
    messageIdParams = senderParams;
    messageSenderParams = idParams;
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

  public String[] getSenderParams() {
    return messageSenderParams;
  }

  public String[] getIdParams() {
    return messageIdParams;
  }
}
