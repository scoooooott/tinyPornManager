/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import javax.swing.SwingUtilities;

import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.IMessageListener;
import org.tinymediamanager.core.Message;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

/**
 * The class TmmUIMessageCollector is used the collect all messages in a dialog window
 * 
 * @author Manuel Laggner
 */
public class TmmUIMessageCollector extends AbstractModelObject implements IMessageListener {
  public static final TmmUIMessageCollector instance    = new TmmUIMessageCollector();

  private final EventList<Message>          messages;
  private int                               newMessages = 0;

  private TmmUIMessageCollector() {
    messages = GlazedLists.threadSafeList(new BasicEventList<Message>());
  }

  @Override
  public void pushMessage(final Message message) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        messages.add(message);
        int oldValue = newMessages;
        newMessages++;
        firePropertyChange("messages", oldValue, newMessages);
      }
    });
  }

  /**
   * get the count of all collected messages
   * 
   * @return the count of all collected messages
   */
  public int getMessageCount() {
    return messages.size();
  }

  /**
   * get the count of all new messages
   * 
   * @return the count of all new messages
   */
  public int getNewMessagesCount() {
    return newMessages;
  }

  /**
   * reset the counter of new messages
   */
  public void resetNewMessageCount() {
    int oldValue = newMessages;
    newMessages = 0;
    firePropertyChange("messages", oldValue, newMessages);
  }

  /**
   * get a list of all messages
   * 
   * @return the list containing all messages
   */
  public EventList<Message> getMessages() {
    return messages;
  }
}
