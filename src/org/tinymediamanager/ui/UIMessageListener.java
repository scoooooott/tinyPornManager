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
package org.tinymediamanager.ui;

import javax.swing.SwingUtilities;

import org.tinymediamanager.core.IMessageListener;
import org.tinymediamanager.core.Message;

/**
 * Class UIMessageListener used to push the messaged to the EDT
 * 
 * @author Manuel Laggner
 */
public class UIMessageListener implements IMessageListener {

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.core.IMessageListener#pushMessage(org.tinymediamanager.core.Message)
   */
  @Override
  public void pushMessage(final Message message) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        MainWindow.getActiveInstance().addMessage(message.getMessageId());
      }
    });
  }
}
