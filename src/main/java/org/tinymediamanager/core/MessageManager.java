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

import java.util.ArrayList;
import java.util.List;

/**
 * The Class MessageManager - use to collect and delegate messages
 * 
 * @author Manuel Laggner
 */
public class MessageManager {
  public static final MessageManager   instance;

  private final List<IMessageListener> listeners;

  static {
    instance = new MessageManager();
  }

  private MessageManager() {
    listeners = new ArrayList<>();
  }

  /**
   * Add a new listener
   * 
   * @param newListener
   *          the new listener to be added
   */
  public void addListener(IMessageListener newListener) {
    synchronized (listeners) {
      listeners.add(newListener);
    }
  }

  /**
   * Remove a listener
   * 
   * @param listener
   *          the listener to be removed
   */
  public void removeListener(IMessageListener listener) {
    synchronized (listeners) {
      listeners.remove(listener);
    }
  }

  /**
   * Push a message to all listeners
   * 
   * @param message
   *          the message to push to all listeners
   */
  public void pushMessage(Message message) {
    for (IMessageListener listener : listeners) {
      listener.pushMessage(message);
    }
  }
}
