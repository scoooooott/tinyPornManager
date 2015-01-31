/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;

import org.tinymediamanager.core.IMessageListener;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;

/**
 * The class TmmUIMessageCollector is used the collect all shown messages (popups) in a dialog window
 * 
 * @author Manuel Laggner
 */
public class TmmUIMessageCollector implements IMessageListener {
  private static final ResourceBundle       BUNDLE   = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  public static final TmmUIMessageCollector instance = new TmmUIMessageCollector();

  private List<Message>                     messages = new CopyOnWriteArrayList<Message>();

  private TmmUIMessageCollector() {
  }

  @Override
  public void pushMessage(Message message) {
    messages.add(message);
  }

  public String getMessagesAsString() {
    return getMessagesAsString(0);
  }

  public String getMessagesAsString(int startFromMessage) {
    StringBuilder sb = new StringBuilder();
    for (int i = startFromMessage; i < messages.size(); i++) {
      try {
        Message message = messages.get(i);
        String msgid = "";
        String sender = "";

        if (message.getMessageSender() instanceof MediaEntity) {
          // mediaEntity title: eg. Movie title
          MediaEntity me = (MediaEntity) message.getMessageSender();
          sender = me.getTitle();
        }
        else if (message.getMessageSender() instanceof MediaFile) {
          // mediaFile: filename
          MediaFile mf = (MediaFile) message.getMessageSender();
          sender = mf.getFilename();
        }
        else {
          try {
            sender = Utils.replacePlaceholders(BUNDLE.getString(message.getMessageSender().toString()), message.getSenderParams());
          }
          catch (Exception e) {
            sender = String.valueOf(message.getMessageSender());
          }
        }

        try {
          // try to get a localized version
          msgid = Utils.replacePlaceholders(BUNDLE.getString(message.getMessageId()), message.getIdParams());
        }
        catch (Exception e) {
          // simply take the id
          msgid = message.getMessageId();
        }
        sb.append(msgid);
        sb.append(" - ");
        sb.append(sender);
        sb.append("\n");

      }
      catch (Exception e) {
        break;
      }
    }
    return sb.toString();
  }
}
