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
package org.tinymediamanager.core;

import java.util.Date;

/**
 * The Class Message, used to transport messages inside tmm.
 * 
 * @author Manuel Laggner
 */
public class Message {
  public enum MessageLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
    SEVERE
  }

  private MessageLevel messageLevel;
  private String       messageId;
  private String[]     messageIdParams;
  private Object       messageSender;
  private String[]     messageSenderParams;
  private Date         messageDate;
  private Throwable    throwable;

  /**
   * Instantiates a new message.
   * 
   * @param sender
   *          the object which is the source of this message. It can be any object. Some well known classes (like Movie.class, MediaFile.class) will
   *          get an extra processing, as on other objects there is simple a .toString() used. If a string is passed here, also a lookup in the bundle
   *          will happen
   * @param id
   *          the message id from the bundle (or a string which will not be localized)
   */
  public Message(Object sender, String id) {
    this(MessageLevel.DEBUG, sender, id);
  }

  /**
   * Instantiates a new message.
   * 
   * @param level
   *          the message level
   * @param sender
   *          the object which is the source of this message. It can be any object. Some well known classes (like Movie.class, MediaFile.class) will
   *          get an extra processing, as on other objects there is simple a .toString() used. If a string is passed here, also a lookup in the bundle
   *          will happen
   * @param id
   *          the message id from the bundle (or a string which will not be localized)
   */
  public Message(MessageLevel level, Object sender, String id) {
    this(level, sender, new String[0], id, new String[0]);
  }

  /**
   * Instantiates a new message.
   * 
   * @param level
   *          the message level
   * @param sender
   *          the object which is the source of this message. It can be any object. Some well known classes (like Movie.class, MediaFile.class) will
   *          get an extra processing, as on other objects there is simple a .toString() used. If a string is passed here, also a lookup in the bundle
   *          will happen
   * @param senderParams
   *          the sender params are an array of string to contain replacements for localizeable messages
   * @param id
   *          the message id from the bundle (or a string which will not be localized)
   */
  public Message(MessageLevel level, Object sender, String[] senderParams, String id) {
    this(level, sender, senderParams, id, new String[0]);
  }

  /**
   * Instantiates a new message.
   * 
   * @param level
   *          the message level
   * @param sender
   *          the object which is the source of this message. It can be any object. Some well known classes (like Movie.class, MediaFile.class) will
   *          get an extra processing, as on other objects there is simple a .toString() used. If a string is passed here, also a lookup in the bundle
   *          will happen
   * @param id
   *          the message id from the bundle (or a string which will not be localized)
   * @param idParams
   *          the id params are an array of string to contain replacements for localizeable messages
   */
  public Message(MessageLevel level, Object sender, String id, String[] idParams) {
    this(level, sender, new String[0], id, idParams);
  }

  /**
   * Instantiates a new message.
   * 
   * @param level
   *          the message level
   * @param sender
   *          the object which is the source of this message. It can be any object. Some well known classes (like Movie.class, MediaFile.class) will
   *          get an extra processing, as on other objects there is simple a .toString() used. If a string is passed here, also a lookup in the bundle
   *          will happen
   * @param senderParams
   *          the sender params are an array of string to contain replacements for localizeable messages
   * @param id
   *          the message id from the bundle (or a string which will not be localized)
   * @param idParams
   *          the id params are an array of string to contain replacements for localizeable messages
   */
  public Message(MessageLevel level, Object sender, String[] senderParams, String id, String[] idParams) {
    messageSender = sender;
    messageLevel = level;
    messageId = id;
    messageIdParams = idParams;
    messageSenderParams = senderParams;
    messageDate = new Date();
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

  public Date getMessageDate() {
    return messageDate;
  }
}
