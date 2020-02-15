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

import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.IMessageListener;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.Utils;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

/**
 * The class TmmUIMessageCollector is used the collect all messages in a dialog window
 * 
 * @author Manuel Laggner
 */
public class TmmUIMessageCollector extends AbstractModelObject implements IMessageListener {
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle       BUNDLE      = ResourceBundle.getBundle("messages", new UTF8Control());
  public static final TmmUIMessageCollector instance    = new TmmUIMessageCollector();

  private final EventList<Message>          messages;
  private int                               newMessages = 0;

  private TmmUIMessageCollector() {
    messages = GlazedLists.threadSafeList(new BasicEventList<>());
  }

  @Override
  public void pushMessage(final Message message) {
    // display severe messages in a popup directly!
    if (message.getMessageLevel() == MessageLevel.SEVERE) {
      SwingUtilities.invokeLater(() -> {
        String sender = "";
        try {
          sender = Utils.replacePlaceholders(BUNDLE.getString(message.getMessageSender().toString()), message.getSenderParams());
        }
        catch (Exception e) {
          sender = String.valueOf(message.getMessageSender());
        }

        String text = "";
        try {
          text = Utils.replacePlaceholders(BUNDLE.getString(message.getMessageId()), message.getIdParams());
        }
        catch (Exception e) {
          text = String.valueOf(message.getMessageId());
        }

        JOptionPane.showMessageDialog(null, text, sender, JOptionPane.ERROR_MESSAGE);
      });
    }
    else {
      // otherwise push it to the list
      SwingUtilities.invokeLater(() -> {
        messages.add(message);
        int oldValue = newMessages;
        newMessages++;
        firePropertyChange(Constants.MESSAGES, oldValue, newMessages);
      });
    }
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
    firePropertyChange(Constants.MESSAGES, oldValue, newMessages);
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
